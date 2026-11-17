package com.kodi.fusion.mapsdemo;

import android.app.Activity;
import android.os.Handler;
import android.os.Looper;

import com.google.android.gms.maps.model.LatLng;
import org.json.JSONArray;
import org.json.JSONObject;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Locale;

/**
 * NASA CMR discovery of actual NISAR L2 GCOV granule metadata in the app.
 *
 * Public search does not need an Earthdata credential; product HDF5 download
 * requires a separate authenticated Earthdata Login flow (NOT implemented here).
 * A CMR scene date is not a guarantee a corresponding GIBS browse pixel exists.
 */
public final class NisarCatalogSearch {
    private static final String BASE = "https://cmr.earthdata.nasa.gov/search/granules.json";
    private static final String[] COLLECTIONS = {
        "NISAR_L2_GCOV_V1",
        "NISAR_L2_GCOV_PROVISIONAL_V1",
        "NISAR_L2_GCOV_BETA_V1"
    };
    private static final Handler UI = new Handler(Looper.getMainLooper());
    private static final int MAX_BYTES = 1024 * 1024;

    public static final class Scene {
        public final String title, date, collection, conceptId;
        public final String timeStart;
        public Scene(String title, String date, String collection,
                     String conceptId, String timeStart) {
            this.title=title;
            this.date=date;
            this.collection=collection;
            this.conceptId=conceptId;
            this.timeStart=timeStart;
        }
        public String label() {
            String quality = collection.contains("PROVISIONAL") ? "PROVISIONAL"
                : collection.contains("BETA") ? "BETA" : "VALIDATED";
            return date + " · " + quality + "\n" + shorten(title, 72);
        }
        private static String shorten(String value, int n) {
            return value.length() <= n ? value : value.substring(0,n) + "…";
        }
    }

    public interface Listener {
        void onLoading();
        void onScenes(ArrayList<Scene> scenes);
        void onError(String error);
    }

    private NisarCatalogSearch() {}

    public static void search(Activity activity, LatLng point, Listener listener) {
        if (point == null || !Double.isFinite(point.latitude) ||
                !Double.isFinite(point.longitude) ||
                Math.abs(point.latitude) > 83 || Math.abs(point.longitude) > 179) {
            listener.onError("Qendra e zonës nuk është e vlefshme.");
            return;
        }
        listener.onLoading();
        new Thread(() -> {
            ArrayList<Scene> collected = new ArrayList<>();
            HashSet<String> seen = new HashSet<>();
            String lastError = null;
            boolean anyResponse = false;

            // A 2 km search area around the selected center. SAR footprints
            // may be much larger than the 25 m / 50 m analytical grid.
            double dLat=1000.0/111320.0;
            double dLon=1000.0/(111320.0*Math.max(.05,
                Math.abs(Math.cos(Math.toRadians(point.latitude)))));
            String bbox=String.format(Locale.US,"%.6f,%.6f,%.6f,%.6f",
                point.longitude-dLon,point.latitude-dLat,
                point.longitude+dLon,point.latitude+dLat);

            for (String shortName : COLLECTIONS) {
                try {
                    String url = BASE+"?provider=ASF&short_name="+
                        URLEncoder.encode(shortName,"UTF-8")
                        +"&bounding_box="+URLEncoder.encode(bbox,"UTF-8")
                        +"&sort_key=-start_date&page_size=12";
                    HttpURLConnection conn=(HttpURLConnection)new URL(url).openConnection();
                    conn.setConnectTimeout(12000);
                    conn.setReadTimeout(15000);
                    conn.setRequestProperty("Accept","application/json");
                    conn.setRequestProperty("User-Agent","KODI-Fusion-Pro-CMR/0.5");
                    try {
                        if (conn.getResponseCode()!=200)
                            throw new IllegalStateException("CMR HTTP "+conn.getResponseCode());
                        ByteArrayOutputStream output=new ByteArrayOutputStream();
                        try(InputStream input=conn.getInputStream()){
                            byte[] buf=new byte[4096];
                            int len;
                            while((len=input.read(buf))!=-1) {
                                if (output.size()+len>MAX_BYTES)
                                    throw new IllegalStateException("CMR përgjigje tepër e madhe");
                                output.write(buf,0,len);
                            }
                        }
                        JSONObject root=new JSONObject(output.toString("UTF-8"));
                        JSONArray entries=root.getJSONObject("feed").optJSONArray("entry");
                        anyResponse=true;
                        if(entries==null) continue;
                        for(int i=0;i<entries.length();i++) {
                            JSONObject e=entries.optJSONObject(i);
                            if(e==null)continue;
                            String id=e.optString("id","");
                            String timestamp=e.optString("time_start","");
                            if(id.isEmpty()||timestamp.length()<10||!seen.add(id))continue;
                            String date=timestamp.substring(0,10);
                            collected.add(new Scene(e.optString("title",id),
                                date,shortName,id,timestamp));
                        }
                    } finally { conn.disconnect(); }
                } catch(Exception ex) {
                    lastError=ex.getMessage();
                }
            }
            Collections.sort(collected, (a,b)->b.timeStart.compareTo(a.timeStart));
            while(collected.size()>18)collected.remove(collected.size()-1);
            final ArrayList<Scene> results=collected;
            final String error=lastError;
            final boolean responded=anyResponse;
            UI.post(()->{
                if(activity.isFinishing()||activity.isDestroyed())return;
                if(!responded)listener.onError("NASA CMR nuk u arrit. "+
                    (error==null?"Kontrollo internetin.":error));
                else listener.onScenes(results);
            });
        },"kodi-nisar-cmr").start();
    }
}
