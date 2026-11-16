package com.kodi.fusion.mapsdemo;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.text.InputType;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import java.util.Locale;

/**
 * Six-panel SURFER navigation dashboard based on the user's six-tile reference.
 * No synthetic data is displayed as scientific measurement.
 */
public final class SurferSixDActivity extends Activity {
    private static final String[] TITLES = {
        "SATELIT", "HEATMAP", "SAR VV/VH",
        "SHTRESAT", "PROFIL 3D", "TERREN 3D"
    };
    private static final int NAVY = Color.rgb(18, 29, 45);
    private static final int GREY = Color.rgb(96, 104, 114);
    private static final int CYAN = Color.rgb(0, 168, 183);
    private double latitude = 42.312015;
    private double longitude = 21.119746;
    private int gridMeters = 25;

    @Override
    protected void onCreate(Bundle saved) {
        super.onCreate(saved);
        SharedPreferences p = getSharedPreferences("surfer6d", MODE_PRIVATE);
        latitude = p.getFloat("lat", (float) latitude);
        longitude = p.getFloat("lon", (float) longitude);
        gridMeters = p.getInt("side", 25) == 50 ? 50 : 25;
        getWindow().setStatusBarColor(Color.WHITE);
        getWindow().setNavigationBarColor(Color.WHITE);
        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
        render();
    }

    private int dp(float value) {
        return Math.round(value * getResources().getDisplayMetrics().density);
    }

    private GradientDrawable rounded(int color, int radius) {
        GradientDrawable bg = new GradientDrawable();
        bg.setColor(color);
        bg.setCornerRadius(dp(radius));
        return bg;
    }

    private TextView label(String value, int sp, int color, boolean bold) {
        TextView v = new TextView(this);
        v.setText(value);
        v.setTextSize(sp);
        v.setTextColor(color);
        if (bold) v.setTypeface(null, 1);
        return v;
    }

    private Button makeButton(String text) {
        Button b = new Button(this);
        b.setText(text);
        b.setTextSize(12);
        b.setAllCaps(false);
        b.setMinHeight(0);
        b.setMinimumHeight(0);
        return b;
    }

    private void render() {
        ScrollView scroll = new ScrollView(this);
        scroll.setFillViewport(true);
        scroll.setBackgroundColor(Color.WHITE);
        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setPadding(dp(16), dp(12), dp(16), dp(24));
        scroll.addView(root);
        setContentView(scroll);

        LinearLayout nav = new LinearLayout(this);
        nav.setGravity(Gravity.CENTER_VERTICAL);
        TextView hamburger = label("\u2630", 30, NAVY, false);
        hamburger.setGravity(Gravity.CENTER);
        hamburger.setContentDescription("Hap menynë");
        hamburger.setBackground(rounded(0xfff1f3f5, 16));
        hamburger.setOnClickListener(v -> openMenu());
        nav.addView(hamburger, new LinearLayout.LayoutParams(dp(52), dp(46)));
        LinearLayout heading = new LinearLayout(this);
        heading.setOrientation(LinearLayout.VERTICAL);
        heading.setPadding(dp(12), 0, 0, 0);
        heading.addView(label("KODI FUSION PRO", 11, GREY, true));
        heading.addView(label("SURFER · ANALIZA 6D", 20, NAVY, true));
        nav.addView(heading, new LinearLayout.LayoutParams(0, -2, 1f));
        root.addView(nav);

        TextView intro = label(
            "Gjashtë pamje për të njëjtën zonë — secila hapet veçmas për analizë.",
            13, GREY, false);
        intro.setPadding(dp(4), dp(12), dp(4), dp(12));
        root.addView(intro);

        for (int row=0; row<2; row++) {
            LinearLayout line = new LinearLayout(this);
            line.setOrientation(LinearLayout.HORIZONTAL);
            LinearLayout.LayoutParams lineLp = new LinearLayout.LayoutParams(-1, dp(124));
            lineLp.bottomMargin = dp(8);
            root.addView(line, lineLp);
            for (int col=0; col<3; col++) {
                int index = row * 3 + col;
                LinearLayout tile = makeTile(index);
                LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(0, -1, 1f);
                lp.leftMargin = dp(3);
                lp.rightMargin = dp(3);
                line.addView(tile, lp);
            }
        }

        TextView note = label("Panel demonstrues: rezultatet shfaqen vetëm pasi të importohen të dhëna reale.",
            12, GREY, false);
        note.setPadding(dp(4), dp(4), dp(4), dp(14));
        root.addView(note);

        LinearLayout area = new LinearLayout(this);
        area.setOrientation(LinearLayout.VERTICAL);
        area.setPadding(dp(15), dp(16), dp(15), dp(15));
        area.setBackground(rounded(0xfff4f6f8, 18));
        root.addView(area);
        area.addView(label("ZONA E PUNËS · WGS84", 13, NAVY, true));
        TextView coords = label(String.format(Locale.US,
            "%.6f, %.6f", latitude, longitude), 16, NAVY, true);
        coords.setPadding(0, dp(8), 0, dp(4));
        area.addView(coords);

        LinearLayout controls = new LinearLayout(this);
        Button grid25 = makeButton("25 × 25 m");
        Button grid50 = makeButton("50 × 50 m");
        Button setPoint = makeButton("Koordinatat");
        grid25.setBackground(rounded(gridMeters == 25 ? 0xffc3f3f1 : Color.WHITE, 12));
        grid50.setBackground(rounded(gridMeters == 50 ? 0xffc3f3f1 : Color.WHITE, 12));
        setPoint.setBackground(rounded(Color.WHITE, 12));
        for (Button b : new Button[]{grid25, grid50, setPoint}) {
            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(0, dp(48), 1f);
            lp.rightMargin = dp(4);
            controls.addView(b, lp);
        }
        area.addView(controls);
        grid25.setOnClickListener(v -> saveGrid(25));
        grid50.setOnClickListener(v -> saveGrid(50));
        setPoint.setOnClickListener(v -> editCoordinates());

        Button compare = makeButton("KRAHASO 6 PAMJET");
        compare.setTextColor(Color.WHITE);
        compare.setBackground(rounded(NAVY, 14));
        LinearLayout.LayoutParams compareLp = new LinearLayout.LayoutParams(-1, dp(54));
        compareLp.topMargin = dp(16);
        root.addView(compare, compareLp);
        compare.setOnClickListener(v -> openPanel("KRAHASO 6 PAMJET"));

        Button nisar = makeButton("NISAR · L-BAND / S-BAND");
        nisar.setTextColor(NAVY);
        nisar.setBackground(rounded(0xffe3f7f8, 14));
        LinearLayout.LayoutParams nisarLp = new LinearLayout.LayoutParams(-1, dp(51));
        nisarLp.topMargin = dp(8);
        root.addView(nisar, nisarLp);
        nisar.setOnClickListener(v -> openPanel("NISAR"));

        TextView footer = label(
            "Google Satellite: pamje vizuale. SAR/DEM/heatmap: import ose burim real. "
                + "Pa matje të verifikuara nuk paraqiten zbulime nëntokësore.",
            12, GREY, false);
        footer.setPadding(dp(4), dp(15), dp(4), 0);
        root.addView(footer);
    }

    private LinearLayout makeTile(int position) {
        LinearLayout card = new LinearLayout(this);
        card.setOrientation(LinearLayout.VERTICAL);
        card.setGravity(Gravity.CENTER);
        card.setPadding(dp(3), dp(6), dp(3), dp(6));
        card.setBackground(rounded(0xfff1f2f4, 17));
        card.setContentDescription(TITLES[position] + " — hap panelin");
        card.setClickable(true);
        card.setFocusable(true);
        IconView glyph = new IconView(this, position);
        card.addView(glyph, new LinearLayout.LayoutParams(dp(38), dp(38)));
        TextView title = label(TITLES[position], 12, NAVY, true);
        title.setGravity(Gravity.CENTER);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(-1, -2);
        lp.topMargin = dp(8);
        card.addView(title, lp);
        card.setOnClickListener(v -> {
            if (position == 0) openGoogleSatellite();
            else openPanel(TITLES[position]);
        });
        return card;
    }

    private void openGoogleSatellite() {
        Intent map = new Intent(this, GoogleSatelliteActivity.class);
        map.putExtra(GoogleSatelliteActivity.EXTRA_LAT, latitude);
        map.putExtra(GoogleSatelliteActivity.EXTRA_LON, longitude);
        map.putExtra(GoogleSatelliteActivity.EXTRA_GRID, gridMeters);
        startActivity(map);
    }

    private void openPanel(String panel) {
        Intent page = new Intent(this, SurferPanelActivity.class);
        page.putExtra(SurferPanelActivity.EXTRA_PANEL, panel);
        page.putExtra(GoogleSatelliteActivity.EXTRA_LAT, latitude);
        page.putExtra(GoogleSatelliteActivity.EXTRA_LON, longitude);
        page.putExtra(GoogleSatelliteActivity.EXTRA_GRID, gridMeters);
        startActivity(page);
    }

    private void saveGrid(int meters) {
        gridMeters = meters;
        persist();
        render();
    }

    private void persist() {
        getSharedPreferences("surfer6d", MODE_PRIVATE).edit()
            .putFloat("lat", (float)latitude)
            .putFloat("lon", (float)longitude)
            .putInt("side", gridMeters).apply();
    }

    private void editCoordinates() {
        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(dp(24), dp(8), dp(24), dp(8));
        EditText lat = new EditText(this);
        lat.setSingleLine(true);
        lat.setInputType(InputType.TYPE_CLASS_NUMBER |
            InputType.TYPE_NUMBER_FLAG_SIGNED | InputType.TYPE_NUMBER_FLAG_DECIMAL);
        lat.setHint("Gjerësia (latitude)");
        lat.setText(String.format(Locale.US, "%.6f", latitude));
        layout.addView(lat);
        EditText lon = new EditText(this);
        lon.setSingleLine(true);
        lon.setInputType(InputType.TYPE_CLASS_NUMBER |
            InputType.TYPE_NUMBER_FLAG_SIGNED | InputType.TYPE_NUMBER_FLAG_DECIMAL);
        lon.setHint("Gjatësia (longitude)");
        lon.setText(String.format(Locale.US, "%.6f", longitude));
        layout.addView(lon);
        new AlertDialog.Builder(this)
            .setTitle("Ndrysho qendrën WGS84")
            .setView(layout)
            .setNegativeButton("Anulo", null)
            .setPositiveButton("Ruaj", (dialog, which) -> {
                try {
                    double a = Double.parseDouble(lat.getText().toString().trim().replace(',', '.'));
                    double b = Double.parseDouble(lon.getText().toString().trim().replace(',', '.'));
                    if (!Double.isFinite(a) || !Double.isFinite(b)
                        || Math.abs(a) > 85 || Math.abs(b) > 180) {
                        showError("Koordinatat janë jashtë kufijve të lejuar.");
                        return;
                    }
                    latitude = a;
                    longitude = b;
                    persist();
                    render();
                } catch (NumberFormatException ex) {
                    showError("Shkruaj dy koordinata numerike të vlefshme.");
                }
            }).show();
    }

    private void showError(String msg) {
        new AlertDialog.Builder(this).setMessage(msg).setPositiveButton("OK", null).show();
    }

    private void openMenu() {
        String[] options = {"SURFER · ANALIZA 6D", "Google Satellite",
            "NISAR L/S", "Krahaso 6 pamjet", "Informacion"};
        new AlertDialog.Builder(this).setTitle("KODI FUSION PRO")
            .setItems(options, (dialog, which) -> {
                if (which == 1) openGoogleSatellite();
                else if (which == 2) openPanel("NISAR");
                else if (which == 3) openPanel("KRAHASO 6 PAMJET");
                else if (which == 4) showError(
                    "Version prove Android: kjo menu është e ndërtuar në kod. "
                    + "Modulet kërkojnë të dhëna reale; nuk krijohen zbulime të sajuara.");
            }).show();
    }

    /** Uniform line-art glyphs matching the six cards from the reference screenshot. */
    private static final class IconView extends View {
        private final int style;
        private final Paint p = new Paint(Paint.ANTI_ALIAS_FLAG);
        IconView(Activity activity, int style) {
            super(activity);
            this.style = style;
            setLayerType(View.LAYER_TYPE_SOFTWARE, null);
        }
        @Override protected void onDraw(Canvas canvas) {
            super.onDraw(canvas);
            int save = canvas.save();
            canvas.scale(getWidth()/40f, getHeight()/40f);
            p.setColor(0xff5b626b);
            p.setStrokeWidth(2.3f);
            p.setStrokeCap(Paint.Cap.ROUND);
            p.setStrokeJoin(Paint.Join.ROUND);
            p.setStyle(Paint.Style.STROKE);
            if (style == 0) {
                canvas.rotate(-35,20,20);
                canvas.drawRect(15,15,25,25,p);
                canvas.drawRect(5,16,12,24,p);
                canvas.drawRect(28,16,35,24,p);
                canvas.drawLine(20,25,20,32,p);
                canvas.drawCircle(20,12,2,p);
            } else if (style == 1) {
                canvas.drawRoundRect(7,8,33,32,4,4,p);
                Path q = new Path();
                q.moveTo(9,24);q.lineTo(14,18);q.lineTo(18,23);
                q.lineTo(23,14);q.lineTo(27,21);q.lineTo(32,17);
                canvas.drawPath(q,p);
            } else if (style == 2) {
                canvas.drawCircle(20,17,2.5f,p);
                canvas.drawLine(20,20,13,34,p);
                canvas.drawLine(20,20,27,34,p);
                canvas.drawLine(16,28,24,28,p);
                canvas.drawArc(11,7,29,27,-55,110,false,p);
                canvas.drawArc(5,1,35,33,-55,110,false,p);
            } else if (style == 3) {
                Path q = new Path();
                q.moveTo(20,7);q.lineTo(32,14);q.lineTo(20,21);
                q.lineTo(8,14);q.close();canvas.drawPath(q,p);
                canvas.drawLine(8,21,20,28,p);canvas.drawLine(20,28,32,21,p);
                canvas.drawLine(8,27,20,34,p);canvas.drawLine(20,34,32,27,p);
            } else if (style == 4) {
                canvas.drawRect(9,11,25,28,p);
                canvas.drawLine(9,11,17,5,p);canvas.drawLine(25,11,33,5,p);
                canvas.drawLine(33,5,33,22,p);canvas.drawLine(25,28,33,22,p);
                canvas.drawLine(17,5,33,5,p);
                canvas.drawLine(17,5,17,22,p);
                canvas.drawLine(17,22,25,28,p);
            } else {
                Path q = new Path();
                q.moveTo(5,31);q.lineTo(15,12);q.lineTo(21,23);
                q.lineTo(26,16);q.lineTo(35,31);q.close();
                canvas.drawPath(q,p);
                canvas.drawLine(10,22,16,22,p);
            }
            canvas.restoreToCount(save);
        }
    }
}
