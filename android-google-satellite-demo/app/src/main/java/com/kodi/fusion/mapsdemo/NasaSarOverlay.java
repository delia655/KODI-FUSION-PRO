package com.kodi.fusion.mapsdemo;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Handler;
import android.os.Looper;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.GroundOverlay;
import com.google.android.gms.maps.model.GroundOverlayOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Locale;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Public georeferenced SAR *visualization* tiles from NASA GIBS WMS 1.1.1.
 *
 * Layer overlays are NASA/ASF SAR preview mosaics, not raw VV/VH or calibrated
 * radar data. Do not treat colors as underground metal/void detection.
 *
 * NASA WMS API: https://gibs.earthdata.nasa.gov/wms/epsg4326/best/wms.cgi
 * Google Maps SDK ground overlays use the same WGS84 lat/lon map bounds.
 * No credentials or private token needed for the public GIBS browse service.
 */
public final class NasaSarOverlay {
    public static final String SOURCE_S1 = "SENTINEL_1";
    public static final String SOURCE_NISAR = "NISAR_L";
    public static final int FILTER_ORIGINAL = 0;
    public static final int FILTER_GRAY = 1;
    public static final int FILTER_CONTRAST = 2;
    public static final int FILTER_EDGES = 3;
    private static final String ENDPOINT =
        "https://gibs.earthdata.nasa.gov/wms/epsg4326/best/wms.cgi";
    private static final int MAX_BYTES = 8 * 1024 * 1024;
    private static final int IMAGE_SIZE = 512;
    private static final Handler UI = new Handler(Looper.getMainLooper());

    public interface Listener {
        void onStatus(String message);
        void onSuccess(String source, String date, LatLngBounds area);
        void onFailure(String message);
    }

    private final Activity activity;
    private final GoogleMap map;
    private final AtomicInteger generation = new AtomicInteger(0);
    private GroundOverlay overlay;
    private Bitmap image;
    private Bitmap rawImage;
    private int filter = FILTER_ORIGINAL;
    private boolean visible = true;
    private String lastSource;
    private String lastDate;
    private float transparency = 0.35f;

    public NasaSarOverlay(Activity activity, GoogleMap map) {
        this.activity = activity;
        this.map = map;
    }

    public static String label(String source) {
        return SOURCE_NISAR.equals(source) ? "NISAR L-band · GCOV / GIBS"
            : "Sentinel-1 · OPERA RTC / GIBS";
    }

    public void setTransparency(float value) {
        transparency = Math.max(0f, Math.min(1f, value));
        if (overlay != null) overlay.setTransparency(transparency);
    }

    public boolean hasData() { return overlay != null; }
    public boolean isVisible() { return overlay != null && visible; }
    public int getFilter() { return filter; }

    public void setVisible(boolean show) {
        visible = show;
        if (overlay != null) overlay.setVisible(show);
    }

    /**
     * VISUAL ONLY: no calibration, no underlying source-radar processing.
     * These filters work on NASA GIBS PNG colors, not NISAR GCOV/HDF5 data.
     */
    public void setFilter(int selected) {
        if (selected < FILTER_ORIGINAL || selected > FILTER_EDGES) return;
        filter = selected;
        if (overlay == null || rawImage == null) return;
        Bitmap next = processImage(rawImage, filter);
        if (next == null) return;
        Bitmap old = image;
        image = next;
        overlay.setImage(BitmapDescriptorFactory.fromBitmap(image));
        if (old != null && old != rawImage && old != image) old.recycle();
    }

    private static int channel(int val) {
        return Math.max(0, Math.min(255, val));
    }

    private static Bitmap processImage(Bitmap input, int selected) {
        if (selected == FILTER_ORIGINAL) return input;
        int w = input.getWidth(), h = input.getHeight();
        int[] source = new int[w * h];
        int[] result = new int[w * h];
        input.getPixels(source, 0, w, 0, 0, w, h);
        for (int y = 0; y < h; y++) {
            for (int x = 0; x < w; x++) {
                int i = y * w + x;
                int color = source[i];
                int a = Color.alpha(color);
                if (a <= 12) { result[i] = Color.TRANSPARENT; continue; }
                int r = Color.red(color), g = Color.green(color), b = Color.blue(color);
                int out;
                if (selected == FILTER_GRAY) {
                    int lum = channel((int)(r * .299 + g * .587 + b * .114));
                    out = Color.argb(a, lum, lum, lum);
                } else if (selected == FILTER_CONTRAST) {
                    out = Color.argb(a, channel((int)((r - 128) * 1.65 + 128)),
                        channel((int)((g - 128) * 1.65 + 128)),
                        channel((int)((b - 128) * 1.65 + 128)));
                } else {
                    // Visual contours using adjacent pixel color differences.
                    // NOT a scientific edge map or an anomaly probability score.
                    int right=source[y*w+Math.min(w-1,x+1)];
                    int bottom=source[Math.min(h-1,y+1)*w+x];
                    if(Color.alpha(right) <= 12 || Color.alpha(bottom) <= 12) {
                        result[i]=Color.TRANSPARENT; continue;
                    }
                    int diff=Math.abs(r-Color.red(right))+Math.abs(g-Color.green(right))+
                        Math.abs(b-Color.blue(right))+Math.abs(r-Color.red(bottom))+
                        Math.abs(g-Color.green(bottom))+Math.abs(b-Color.blue(bottom));
                    int edge=channel(diff*2);
                    out=Color.argb(a,edge,edge,edge);
                }
                result[i] = out;
            }
        }
        Bitmap output=Bitmap.createBitmap(w,h,Bitmap.Config.ARGB_8888);
        output.setPixels(result,0,w,0,0,w,h);
        return output;
    }
    public String getLastSource() { return lastSource; }
    public String getLastDate() { return lastDate; }

    /** Hides data on request; outstanding downloads are ignored. */
    public void clear() {
        generation.incrementAndGet();
        if (overlay != null) { overlay.remove(); overlay = null; }
        if (image != null && image != rawImage) image.recycle();
        if (rawImage != null) rawImage.recycle();
        image = null;
        rawImage = null;
        visible = true;
        lastSource = null;
        lastDate = null;
    }

    /**
     * Request a centered 1,000m footprint. The selected 25/50m grid remains
     * separate, retaining the source sensor's native spatial detail.
     * No zoom-dependent virtual resolution is claimed.
     */
    public void load(String source, String date, LatLng center, Listener callback) {
        if (!SOURCE_NISAR.equals(source) && !SOURCE_S1.equals(source)) {
            callback.onFailure("Burimi SAR nuk njihet.");
            return;
        }
        if (date == null || !date.matches("\\d{4}-\\d{2}-\\d{2}")) {
            callback.onFailure("Data duhet të jetë YYYY-MM-DD.");
            return;
        }
        if (center == null || Math.abs(center.latitude) > 84 ||
                Math.abs(center.longitude) > 179) {
            callback.onFailure("Koordinatat nuk janë të vlefshme.");
            return;
        }

        // Remove any prior overlay immediately: a failed date/source must not
        // leave stale SAR imagery visible and mislabel it as a new acquisition.
        clear();
        final LatLngBounds bounds = boundsFor(center, 1000.0);
        final int requestId = generation.incrementAndGet();
        callback.onStatus("Po shkarkohet pamja reale " + label(source)
            + " · " + date + " · NASA GIBS ...");

        new Thread(() -> {
            Bitmap fetched = null;
            String error = null;
            try {
                URL url = new URL(buildUrl(source, date, bounds));
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setConnectTimeout(14000);
                connection.setReadTimeout(18000);
                connection.setRequestProperty("Accept", "image/png");
                connection.setRequestProperty("User-Agent", "KODI-Fusion-Pro-SAR-Overlay/0.4");
                connection.setInstanceFollowRedirects(false);
                try {
                    if (connection.getResponseCode() != HttpURLConnection.HTTP_OK) {
                        throw new Exception("HTTP " + connection.getResponseCode());
                    }
                    String type = connection.getContentType();
                    if (type == null || !type.toLowerCase(Locale.ROOT).contains("image/")) {
                        throw new Exception("NASA nuk ktheu imazh; kontrollo datën/burimin.");
                    }
                    try (InputStream input = connection.getInputStream();
                         ByteArrayOutputStream output = new ByteArrayOutputStream()) {
                        byte[] buffer = new byte[8192];
                        int length;
                        while ((length = input.read(buffer)) != -1) {
                            if (output.size() + length > MAX_BYTES) {
                                throw new Exception("Pamja e shërbimit tejkalon kufirin e sigurt.");
                            }
                            output.write(buffer, 0, length);
                        }
                        byte[] bytes = output.toByteArray();
                        BitmapFactory.Options options = new BitmapFactory.Options();
                        options.inPreferredConfig = Bitmap.Config.ARGB_8888;
                        fetched = BitmapFactory.decodeByteArray(bytes, 0, bytes.length, options);
                    }
                } finally {
                    connection.disconnect();
                }
                if (fetched == null || fetched.getWidth() < 32 ||
                        fetched.getHeight() < 32 ||
                        fetched.getWidth() > 2048 || fetched.getHeight() > 2048) {
                    throw new Exception("Pamje SAR e papërdorshme për datën e zgjedhur.");
                }
                if (isEffectivelyBlank(fetched)) {
                    throw new Exception("Nuk ka pikselë SAR në këtë zonë dhe datë. Provo një datë tjetër.");
                }
            } catch (Exception ex) {
                error = ex.getMessage();
            }
            final Bitmap result = fetched;
            final String failure = error;
            UI.post(() -> {
                if (requestId != generation.get() || activity.isFinishing()) {
                    if (result != null) result.recycle();
                    return;
                }
                if (failure != null) {
                    if (result != null) result.recycle();
                    callback.onFailure("NASA GIBS: " + failure);
                    return;
                }
                if (overlay != null) { overlay.remove(); overlay = null; }
                if (image != null && image != rawImage) image.recycle();
                if (rawImage != null) rawImage.recycle();
                rawImage = result;
                image = processImage(rawImage, filter);
                visible = true;
                overlay = map.addGroundOverlay(new GroundOverlayOptions()
                    .image(BitmapDescriptorFactory.fromBitmap(image))
                    .positionFromBounds(bounds)
                    .transparency(transparency)
                    .visible(true)
                    .zIndex(0.5f));
                if (overlay == null) {
                    callback.onFailure("Google Maps nuk pranoi shtresën SAR.");
                    return;
                }
                lastSource = source;
                lastDate = date;
                callback.onSuccess(source, date, bounds);
            });
        }, "nasa-sar-wms").start();
    }

    private static LatLngBounds boundsFor(LatLng center, double sideM) {
        double half = sideM / 2.0;
        double dy = half / 111320.0;
        double dx = half / (111320.0 * Math.cos(Math.toRadians(center.latitude)));
        return new LatLngBounds(
            new LatLng(center.latitude - dy, center.longitude - dx),
            new LatLng(center.latitude + dy, center.longitude + dx));
    }

    public static String buildUrl(String source, String date, LatLngBounds bounds) {
        String layer = SOURCE_NISAR.equals(source)
            ? "NISAR_L2_Geocoded_Polarimetric_Covariance"
            : "OPERA_L2_Radiometric_Terrain_Corrected_SAR_Sentinel-1";
        // WMS 1.1.1 uses lon,lat axis ordering with EPSG:4326.
        String box = String.format(Locale.US, "%.8f,%.8f,%.8f,%.8f",
            bounds.southwest.longitude, bounds.southwest.latitude,
            bounds.northeast.longitude, bounds.northeast.latitude);
        return ENDPOINT + "?SERVICE=WMS&VERSION=1.1.1&REQUEST=GetMap"
            + "&LAYERS=" + layer + "&STYLES=&SRS=EPSG%3A4326"
            + "&FORMAT=image%2Fpng&TRANSPARENT=TRUE"
            + "&WIDTH=" + IMAGE_SIZE + "&HEIGHT=" + IMAGE_SIZE
            + "&BBOX=" + box + "&TIME=" + date;
    }

    private static boolean isEffectivelyBlank(Bitmap bitmap) {
        int w = bitmap.getWidth(), h = bitmap.getHeight();
        int opaque = 0;
        // Sample a 24x24 lattice; avoid treating a transparent WMS response as data.
        for (int j = 0; j < 24; j++) {
            for (int i = 0; i < 24; i++) {
                int c = bitmap.getPixel((i * (w - 1)) / 23, (j * (h - 1)) / 23);
                if (Color.alpha(c) > 12) opaque++;
            }
        }
        return opaque < 2;
    }
}
