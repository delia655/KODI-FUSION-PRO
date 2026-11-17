package com.kodi.fusion.mapsdemo;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;

import org.json.JSONArray;
import org.json.JSONException;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * GeoTrace: manual point/path annotations over the authorized Google Maps SDK.
 * No radar measurement, AI anomaly prediction, or underground sensing is performed.
 * Data remain local in this app until the user explicitly shares the GeoJSON.
 */
public final class GeoTraceOverlay {
    private static final int LIMIT = 200;
    private static final String PREFS = "geotrace_local_annotations_v1";
    private static final String KEY = "wgs84_points";
    private final Activity activity;
    private final ArrayList<LatLng> points = new ArrayList<>();
    private final ArrayList<Marker> pins = new ArrayList<>();
    private SharedPreferences preferences;
    private GoogleMap map;
    private Polyline path;
    private Circle lastPointRing;
    private boolean enabled;

    public GeoTraceOverlay(Activity activity, boolean enabled) {
        this.activity = activity;
        this.enabled = enabled;
        this.preferences = activity.getSharedPreferences(PREFS, Activity.MODE_PRIVATE);
        restore();
    }

    private static boolean valid(double lat, double lon) {
        return Double.isFinite(lat) && Double.isFinite(lon)
            && Math.abs(lat) <= 85.0 && Math.abs(lon) <= 180.0;
    }

    private void restore() {
        String content = preferences.getString(KEY, "[]");
        try {
            JSONArray array = new JSONArray(content);
            for (int i = 0; i < Math.min(array.length(), LIMIT); i++) {
                JSONArray pair = array.optJSONArray(i);
                if (pair == null || pair.length() != 2) continue;
                double lat = pair.optDouble(0, Double.NaN);
                double lon = pair.optDouble(1, Double.NaN);
                if (valid(lat, lon)) points.add(new LatLng(lat, lon));
            }
        } catch (JSONException ignored) {
            // Data may be from an old or malformed build; leave a clean local track.
        }
    }

    private void persist() {
        JSONArray array = new JSONArray();
        for (LatLng point : points) {
            JSONArray pair = new JSONArray();
            pair.put(point.latitude);
            pair.put(point.longitude);
            array.put(pair);
        }
        preferences.edit().putString(KEY, array.toString()).apply();
    }

    public boolean isEnabled() { return enabled; }
    public int count() { return points.size(); }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public void bind(GoogleMap googleMap) {
        map = googleMap;
        draw();
    }

    public boolean addManualPoint(LatLng point) {
        if (!enabled || !valid(point.latitude, point.longitude) || points.size() >= LIMIT) {
            return false;
        }
        points.add(point);
        persist();
        draw();
        return true;
    }

    public void undo() {
        if (points.isEmpty()) return;
        points.remove(points.size() - 1);
        persist();
        draw();
    }

    public void clear() {
        points.clear();
        persist();
        draw();
    }

    private void draw() {
        if (path != null) { path.remove(); path = null; }
        if (lastPointRing != null) { lastPointRing.remove(); lastPointRing = null; }
        for (Marker pin : pins) pin.remove();
        pins.clear();
        if (map == null) return;

        for (int i = 0; i < points.size(); i++) {
            Marker marker = map.addMarker(new MarkerOptions()
                .position(points.get(i))
                .title("GeoTrace · Pika " + (i + 1))
                .snippet("Shënuar manualisht · WGS84")
                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE)));
            if (marker != null) pins.add(marker);
        }
        if (points.size() >= 2) {
            path = map.addPolyline(new PolylineOptions()
                .addAll(points).color(Color.rgb(0, 224, 223)).width(7.5f)
                .geodesic(true));
        }
        if (!points.isEmpty()) {
            LatLng last = points.get(points.size() - 1);
            lastPointRing = map.addCircle(new CircleOptions().center(last)
                .radius(3.5).strokeColor(Color.rgb(218, 255, 0))
                .strokeWidth(3f).fillColor(0x22d9ff00));
        }
    }

    /** A manually created track in GeoJSON; not a scientific interpretation. */
    public String toGeoJson() {
        StringBuilder output = new StringBuilder();
        output.append("{\"type\":\"FeatureCollection\",\"properties\":")
            .append("{\"source\":\"GeoTrace manual annotations\",\"sensor_measurement\":false},")
            .append("\"features\":[");
        for (int i = 0; i < points.size(); i++) {
            if (i > 0) output.append(',');
            LatLng p = points.get(i);
            output.append("{\"type\":\"Feature\",\"properties\":{\"point_number\":")
                .append(i + 1).append(",\"kind\":\"manual_marker\"},")
                .append("\"geometry\":{\"type\":\"Point\",\"coordinates\":")
                .append(coords(p)).append("}}");
        }
        if (points.size() >= 2) {
            output.append(",{\"type\":\"Feature\",\"properties\":{\"kind\":\"manual_path\"},")
                .append("\"geometry\":{\"type\":\"LineString\",\"coordinates\":[");
            for (int i = 0; i < points.size(); i++) {
                if (i > 0) output.append(',');
                output.append(coords(points.get(i)));
            }
            output.append("]}}");
        }
        output.append("]}");
        return output.toString();
    }

    private String coords(LatLng p) {
        return String.format(Locale.US, "[%.8f,%.8f]", p.longitude, p.latitude);
    }

    public void shareGeoJson() {
        Intent share = new Intent(Intent.ACTION_SEND);
        share.setType("application/geo+json");
        share.putExtra(Intent.EXTRA_TEXT, toGeoJson());
        activity.startActivity(Intent.createChooser(share, "Ndaj gjurmën GeoTrace"));
    }
}
