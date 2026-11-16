package com.kodi.fusion.mapsdemo;

import android.app.Activity;
import android.os.Bundle;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ApplicationInfo;
import android.net.Uri;
import android.graphics.Color;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polygon;
import com.google.android.gms.maps.model.PolygonOptions;

import java.util.Locale;

/**
 * Standalone demonstration, not the original Kodi Fusion V6.2 Android activity.
 * The grid represents map geometry; it does NOT sense underground targets.
 */
public class GoogleSatelliteActivity extends Activity implements OnMapReadyCallback {
    private MapView mapView;
    private GoogleMap map;
    private Polygon gridPolygon;
    private Marker centerMarker;
    private TextView coordinatesLabel;
    private Button mapTypeButton;
    private int sideMeters = 25;
    private boolean hybrid = false;
    private LatLng center = new LatLng(42.312015, 21.119746);

    @Override
    protected void onCreate(Bundle state) {
        super.onCreate(state);
        if (!hasConfiguredKey()) {
            showMissingKeyMessage();
            return;
        }

        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setPadding(8, 8, 8, 8);
        root.setBackgroundColor(Color.rgb(7, 19, 32));

        TextView title = new TextView(this);
        title.setText("KODI FUSION PRO · GOOGLE SATELLITE");
        title.setTextColor(Color.WHITE);
        title.setTextSize(17);
        title.setPadding(6, 10, 6, 10);
        root.addView(title);

        LinearLayout controls = new LinearLayout(this);
        controls.setOrientation(LinearLayout.HORIZONTAL);
        Button small = makeButton("25×25 m");
        Button large = makeButton("50×50 m");
        mapTypeButton = makeButton("Hibrid");
        controls.addView(small, weightedButtonParams());
        controls.addView(large, weightedButtonParams());
        controls.addView(mapTypeButton, weightedButtonParams());
        root.addView(controls);

        coordinatesLabel = new TextView(this);
        coordinatesLabel.setTextColor(Color.WHITE);
        coordinatesLabel.setPadding(6, 12, 6, 12);
        root.addView(coordinatesLabel);

        mapView = new MapView(this);
        mapView.onCreate(state);
        root.addView(mapView, new LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT, 0, 1f));

        LinearLayout bottom = new LinearLayout(this);
        Button export = makeButton("Ndaj GeoJSON");
        bottom.addView(export, new LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT));
        root.addView(bottom);
        setContentView(root);

        updateCoordinates();
        small.setOnClickListener(v -> { sideMeters = 25; drawGrid(); });
        large.setOnClickListener(v -> { sideMeters = 50; drawGrid(); });
        mapTypeButton.setOnClickListener(v -> {
            hybrid = !hybrid;
            if (map != null) {
                map.setMapType(hybrid ? GoogleMap.MAP_TYPE_HYBRID : GoogleMap.MAP_TYPE_SATELLITE);
            }
            mapTypeButton.setText(hybrid ? "Satelit" : "Hibrid");
        });
        export.setOnClickListener(v -> shareGeoJson());
        mapView.getMapAsync(this);
    }


    /** An absent key should never produce a mysterious empty satellite screen. */
    private boolean hasConfiguredKey() {
        try {
            ApplicationInfo info = getPackageManager().getApplicationInfo(
                getPackageName(), PackageManager.GET_META_DATA);
            if (info.metaData == null) return false;
            String key = info.metaData.getString("com.google.android.geo.API_KEY");
            return key != null && !key.trim().isEmpty() && !key.contains("\u0024");
        } catch (PackageManager.NameNotFoundException ex) {
            return false;
        }
    }

    private void showMissingKeyMessage() {
        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setPadding(24, 40, 24, 20);
        root.setBackgroundColor(Color.rgb(7, 19, 32));
        TextView note = new TextView(this);
        note.setText("GOOGLE SATELLITE — MODUL TESTIMI\\n\\n"
            + "Çelësi Google Maps nuk është konfiguruar në këtë APK. "
            + "Krijo GitHub Actions secret KODI_GOOGLE_MAPS_KEY dhe rindërto aplikacionin. "
            + "Çelësi duhet të lejojë package com.kodi.fusion.mapsdemo dhe "
            + "gjurmën SHA-1 të certifikatës së APK-së së provës.\\n\\n"
            + "Gridi është gjeometrik, jo matje e nëntokës.");
        note.setTextColor(Color.WHITE);
        note.setTextSize(17);
        root.addView(note);
        Button open = makeButton("Hap Google Maps veçmas");
        open.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(
                "https://www.google.com/maps/@42.312015,21.119746,18z/data=!3m1!1e3"));
            startActivity(intent);
        });
        root.addView(open);
        setContentView(root);
    }

    private LinearLayout.LayoutParams weightedButtonParams() {
        return new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f);
    }

    private Button makeButton(String label) {
        Button button = new Button(this);
        button.setText(label);
        button.setTextSize(12f);
        button.setAllCaps(false);
        return button;
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        map = googleMap;
        map.setMapType(GoogleMap.MAP_TYPE_SATELLITE);
        map.moveCamera(CameraUpdateFactory.newLatLngZoom(center, 19f));
        map.setOnMapClickListener(point -> {
            center = point;
            drawGrid();
        });
        drawGrid();
    }

    private LatLng[] corners() {
        double half = sideMeters / 2.0;
        // WGS84 local tangent approximation for a grid of 25 or 50 meters.
        double dLat = half / 111_320.0;
        double cos = Math.cos(Math.toRadians(center.latitude));
        double dLon = half / (111_320.0 * Math.max(0.01, Math.abs(cos)));
        return new LatLng[] {
            new LatLng(center.latitude - dLat, center.longitude - dLon),
            new LatLng(center.latitude - dLat, center.longitude + dLon),
            new LatLng(center.latitude + dLat, center.longitude + dLon),
            new LatLng(center.latitude + dLat, center.longitude - dLon)
        };
    }

    private void updateCoordinates() {
        coordinatesLabel.setText(String.format(Locale.US,
            "WGS84 %.6f, %.6f  ·  GRID %d × %d m (gjeometrik)",
            center.latitude, center.longitude, sideMeters, sideMeters));
    }

    private void drawGrid() {
        updateCoordinates();
        if (map == null) return;
        if (gridPolygon != null) gridPolygon.remove();
        if (centerMarker != null) centerMarker.remove();
        LatLng[] p = corners();
        gridPolygon = map.addPolygon(new PolygonOptions()
            .add(p[0], p[1], p[2], p[3])
            .strokeWidth(3.5f)
            .strokeColor(Color.rgb(0, 225, 220))
            .fillColor(Color.argb(45, 0, 225, 220)));
        centerMarker = map.addMarker(new MarkerOptions().position(center).title("Qendra WGS84"));
    }

    private void shareGeoJson() {
        LatLng[] p = corners();
        StringBuilder ring = new StringBuilder();
        for (int i = 0; i <= 4; i++) {
            LatLng point = p[i % 4];
            if (i > 0) ring.append(',');
            ring.append(String.format(Locale.US,
                "[%.8f,%.8f]", point.longitude, point.latitude));
        }
        String geojson = "{\"type\":\"Feature\",\"properties\":{\"source\":\"KODI geometric grid\",\"side_m\":"
            + sideMeters + ",\"sensor_measurement\":false},\"geometry\":{\"type\":\"Polygon\",\"coordinates\":[["
            + ring + "]]}}";
        Intent share = new Intent(Intent.ACTION_SEND);
        share.setType("application/geo+json");
        share.putExtra(Intent.EXTRA_TEXT, geojson);
        startActivity(Intent.createChooser(share, "Ndaj GeoJSON"));
    }

    @Override protected void onStart() { super.onStart(); if (mapView != null) mapView.onStart(); }
    @Override protected void onResume() { super.onResume(); if (mapView != null) mapView.onResume(); }
    @Override protected void onPause() { if (mapView != null) mapView.onPause(); super.onPause(); }
    @Override protected void onStop() { if (mapView != null) mapView.onStop(); super.onStop(); }
    @Override protected void onDestroy() { if (mapView != null) mapView.onDestroy(); super.onDestroy(); }
    @Override public void onLowMemory() { super.onLowMemory(); if (mapView != null) mapView.onLowMemory(); }
    @Override protected void onSaveInstanceState(Bundle state) {
        super.onSaveInstanceState(state);
        if (mapView != null) mapView.onSaveInstanceState(state);
    }
}
