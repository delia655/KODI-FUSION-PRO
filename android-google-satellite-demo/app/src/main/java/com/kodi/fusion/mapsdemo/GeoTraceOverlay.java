package com.kodi.fusion.mapsdemo;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;

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
    private static final String NOTES_KEY = "point_notes";
    private static final String TYPES_KEY = "point_types";
    private static final String DEFAULT_TYPE = "Vëzhgim manual";
    private final Activity activity;
    private final ArrayList<LatLng> points = new ArrayList<>();
    private final ArrayList<String> notes = new ArrayList<>();
    private final ArrayList<String> types = new ArrayList<>();
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
        try {
            JSONArray savedNotes = new JSONArray(preferences.getString(NOTES_KEY, "[]"));
            JSONArray savedTypes = new JSONArray(preferences.getString(TYPES_KEY, "[]"));
            for (int i = 0; i < points.size(); i++) {
                notes.add(savedNotes.optString(i, ""));
                types.add(savedTypes.optString(i, DEFAULT_TYPE));
            }
        } catch (JSONException ignored) {
            for (int i = 0; i < points.size(); i++) {
                notes.add("");
                types.add(DEFAULT_TYPE);
            }
        }
    }

    private void persist() {
        JSONArray array = new JSONArray();
        for (LatLng point : points) {
            JSONArray pair = new JSONArray();
            pair.put((Object) Double.valueOf(point.latitude));
            pair.put((Object) Double.valueOf(point.longitude));
            array.put(pair);
        }
        JSONArray n = new JSONArray();
        JSONArray t = new JSONArray();
        for (String note : notes) n.put(note);
        for (String type : types) t.put(type);
        preferences.edit()
            .putString(KEY, array.toString())
            .putString(NOTES_KEY, n.toString())
            .putString(TYPES_KEY, t.toString())
            .apply();
    }

    public boolean isEnabled() { return enabled; }
    public int count() { return points.size(); }

    public LatLng getPoint(int index) {
        if (index < 0 || index >= points.size()) throw new IndexOutOfBoundsException();
        return points.get(index);
    }

    public String getNote(int index) { return notes.get(index); }
    public String getType(int index) { return types.get(index); }

    public void setPointDescription(int index, String type, String note) {
        if (index < 0 || index >= points.size()) return;
        types.set(index, type == null ? DEFAULT_TYPE : type.substring(0, Math.min(type.length(), 60)));
        notes.set(index, note == null ? "" : note.substring(0, Math.min(note.length(), 500)));
        persist();
        draw();
    }

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
        notes.add("");
        types.add(DEFAULT_TYPE);
        persist();
        draw();
        return true;
    }

    public void undo() {
        if (points.isEmpty()) return;
        points.remove(points.size() - 1);
        notes.remove(notes.size() - 1);
        types.remove(types.size() - 1);
        persist();
        draw();
    }

    public void clear() {
        points.clear();
        notes.clear();
        types.clear();
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
                .snippet(types.get(i) + " · " + (notes.get(i).isEmpty() ? "Pa shënim" : notes.get(i)))
                .icon(BitmapDescriptorFactory.fromBitmap(numberedPin(i + 1))));
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

    private Bitmap numberedPin(int label) {
        Bitmap bitmap = Bitmap.createBitmap(82, 92, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        paint.setColor(Color.WHITE);
        canvas.drawCircle(41, 39, 35, paint);
        paint.setColor(0xff087fcd);
        canvas.drawCircle(41, 39, 31, paint);
        paint.setColor(Color.WHITE);
        paint.setTextAlign(Paint.Align.CENTER);
        paint.setFakeBoldText(true);
        paint.setTextSize(label < 10 ? 37 : 30);
        canvas.drawText(Integer.toString(label), 41, 51, paint);
        return bitmap;
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
                .append(i + 1).append(",\"kind\":\"manual_marker\",\"type\":")
                .append(org.json.JSONObject.quote(types.get(i))).append(",\"note\":")
                .append(org.json.JSONObject.quote(notes.get(i))).append("},")
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
