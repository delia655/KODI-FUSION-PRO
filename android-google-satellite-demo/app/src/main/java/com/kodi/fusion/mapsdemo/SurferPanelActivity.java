package com.kodi.fusion.mapsdemo;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.drawable.GradientDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Locale;

/**
 * Functional file-based previews. A simple numeric CSV preview is NOT a
 * georeferenced SAR, DEM, GPR, ERT, or validated underground interpretation.
 */
public final class SurferPanelActivity extends Activity {
    public static final String EXTRA_PANEL = "surfer.panel";
    private static final int REQUEST_IMPORT = 27;
    private String panel = "HEATMAP";
    private double latitude = 42.312015;
    private double longitude = 21.119746;
    private int sizeMeters = 25;
    private double[][] matrix = null;
    private String fileName = null;
    private int samples = 0;
    private double minValue = 0, maxValue = 0;

    @Override protected void onCreate(Bundle state) {
        super.onCreate(state);
        if (getIntent() != null) {
            panel = getIntent().getStringExtra(EXTRA_PANEL);
            if (panel == null) panel = "HEATMAP";
            latitude = getIntent().getDoubleExtra(GoogleSatelliteActivity.EXTRA_LAT, latitude);
            longitude = getIntent().getDoubleExtra(GoogleSatelliteActivity.EXTRA_LON, longitude);
            sizeMeters = getIntent().getIntExtra(GoogleSatelliteActivity.EXTRA_GRID, 25) == 50 ? 50 : 25;
        }
        getWindow().setStatusBarColor(Color.WHITE);
        getWindow().setNavigationBarColor(Color.WHITE);
        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
        render();
    }

    private int dp(int n) { return Math.round(n * getResources().getDisplayMetrics().density); }
    private GradientDrawable bg(int color, int radius) {
        GradientDrawable g = new GradientDrawable();
        g.setColor(color); g.setCornerRadius(dp(radius)); return g;
    }
    private TextView text(String s, int size, int color, boolean bold) {
        TextView v = new TextView(this);
        v.setText(s); v.setTextSize(size); v.setTextColor(color);
        if (bold) v.setTypeface(null, 1);
        return v;
    }
    private Button button(String caption) {
        Button v = new Button(this);
        v.setText(caption); v.setAllCaps(false); v.setTextSize(14);
        v.setMinHeight(0); v.setMinimumHeight(0);
        return v;
    }

    private void render() {
        ScrollView scroll = new ScrollView(this);
        scroll.setBackgroundColor(Color.WHITE);
        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setPadding(dp(18), dp(12), dp(18), dp(28));
        scroll.addView(root);
        setContentView(scroll);

        LinearLayout head = new LinearLayout(this);
        head.setGravity(Gravity.CENTER_VERTICAL);
        TextView back = text("‹", 32, 0xff1b2738, false);
        back.setContentDescription("Kthehu");
        back.setGravity(Gravity.CENTER);
        head.addView(back, new LinearLayout.LayoutParams(dp(46), dp(46)));
        back.setOnClickListener(v -> finish());
        LinearLayout titleCol = new LinearLayout(this);
        titleCol.setOrientation(LinearLayout.VERTICAL);
        titleCol.addView(text("SURFER · ANALIZA 6D", 12, 0xff657080, true));
        titleCol.addView(text(panel, 23, 0xff172232, true));
        head.addView(titleCol, new LinearLayout.LayoutParams(0, -2, 1f));
        root.addView(head);

        TextView area = text(String.format(Locale.US,
            "WGS84: %.6f, %.6f · Grid %d × %d m",
            latitude, longitude, sizeMeters, sizeMeters), 13, 0xff516174, false);
        area.setPadding(dp(4), dp(15), 0, dp(12));
        root.addView(area);

        if ("KRAHASO 6 PAMJET".equals(panel)) {
            showComparison(root);
            return;
        }
        if ("NISAR".equals(panel)) {
            showNisar(root);
            return;
        }
        String description = explainPanel(panel);
        TextView info = text(description, 15, 0xff293745, false);
        info.setBackground(bg(0xfff2f4f6, 16));
        info.setPadding(dp(16), dp(16), dp(16), dp(16));
        root.addView(info);

        TextView heading = text("PAMJA E TË DHËNAVE", 14, 0xff182638, true);
        heading.setPadding(0, dp(23), 0, dp(11));
        root.addView(heading);

        if (matrix == null) {
            TextView empty = text(
                "NUK KA TË DHËNA TË IMPORTUARA\n\n"
                + "Kjo pamje nuk përdor anomali, thellësi apo besueshmëri të sajuara. "
                + "Zgjidh një skedar numerik nga telefoni për një pamje paraprake.", 
                15, 0xff627083, false);
            empty.setGravity(Gravity.CENTER);
            empty.setBackground(bg(0xffeef1f4, 18));
            empty.setPadding(dp(22), dp(22), dp(22), dp(22));
            root.addView(empty, new LinearLayout.LayoutParams(-1, dp(240)));
        } else {
            MatrixPreview preview = new MatrixPreview(this, matrix, minValue, maxValue);
            preview.setBackground(bg(0xffe8eef2, 13));
            root.addView(preview, new LinearLayout.LayoutParams(-1, dp(280)));
            TextView details = text(String.format(Locale.US,
                "%s · %d vlera numerike · min %.4g · max %.4g",
                fileName, samples, minValue, maxValue), 12, 0xff526174, false);
            details.setPadding(0, dp(8), 0, dp(10));
            root.addView(details);
        }

        Button importFile = button("IMPORTO CSV / TXT NUMERIK");
        importFile.setTextColor(Color.WHITE);
        importFile.setBackground(bg(0xff162838, 13));
        LinearLayout.LayoutParams btnLp = new LinearLayout.LayoutParams(-1, dp(54));
        btnLp.topMargin = dp(15);
        root.addView(importFile, btnLp);
        importFile.setOnClickListener(v -> chooseDocument());

        TextView caution = text(
            "Pamja paraprake tregon vetëm vlerat nga skedari i zgjedhur, sipas rendit të rreshtave. "
                + "Nuk bën gjeoreferencim automatik, identifikim metalesh, matje thellësie apo volum 3D. "
                + "Për analiza shkencore duhen metadata, kalibrim dhe verifikim i matjeve.",
            12, 0xff6e7480, false);
        caution.setPadding(0, dp(18), 0, 0);
        root.addView(caution);
    }

    private String explainPanel(String key) {
        switch (key) {
            case "HEATMAP":
                return "Hartë e vlerave numerike të një skedari real CSV/TXT. "
                    + "Ngjyrat pasqyrojnë minimumin dhe maksimumin e të dhënave të importuara.";
            case "SAR VV/VH":
                return "Sentinel-1 VV/VH dhe NISAR janë matje radar të ndryshme. "
                    + "Importo një matricë numerike të përpunuar për ta parë; "
                    + "ky panel nuk i shkarkon ende skenat automatikisht.";
            case "SHTRESAT":
                return "Krahasimi i shtresave kërkon skedarë të gjeoreferencuar. "
                    + "Këtu mund të shohësh paraprakisht një matricë numerike të importuar.";
            case "PROFIL 3D":
                return "Model 3D nëntokësor kërkon matje GPR/ERT ose volume të kalibruara. "
                    + "Importi aktual shfaq vetëm matricën 2D të vlerave, jo thellësi 3D.";
            case "TERREN 3D":
                return "Për terren real 3D nevojitet DEM me koordinata dhe lartësi. "
                    + "Importi aktual është pamje 2D e vlerave; modeli 3D është fazë tjetër.";
            default:
                return "Panel paraprak i të dhënave reale.";
        }
    }

    private void showComparison(LinearLayout root) {
        TextView note = text(
            "Krahasimi i gjashtë pamjeve për të njëjtën qendër dhe madhësi gridi. "
                + "Çdo pamje hapet veçmas. Pa skedarë të importuar nuk jepen rezultate të sajuara.",
            15, 0xff5f6874, false);
        note.setPadding(0, dp(8), 0, dp(20));
        root.addView(note);
        String[] names = {"SATELIT", "HEATMAP", "SAR VV/VH", "SHTRESAT", "PROFIL 3D", "TERREN 3D"};
        for (int i = 0; i < 2; i++) {
            LinearLayout row = new LinearLayout(this);
            LinearLayout.LayoutParams rowLp = new LinearLayout.LayoutParams(-1, dp(135));
            rowLp.bottomMargin = dp(9);
            root.addView(row, rowLp);
            for (int j = 0; j < 3; j++) {
                String key = names[i * 3 + j];
                LinearLayout cell = new LinearLayout(this);
                cell.setOrientation(LinearLayout.VERTICAL);
                cell.setGravity(Gravity.CENTER);
                cell.setBackground(bg(0xfff1f2f4, 16));
                TextView shortName = text(key, 13, 0xff243242, true);
                shortName.setGravity(Gravity.CENTER);
                cell.addView(shortName);
                TextView status = text(key.equals("SATELIT") ? "Hap hartën" : "Pa të dhëna",
                    11, 0xff78818b, false);
                status.setGravity(Gravity.CENTER);
                cell.addView(status);
                LinearLayout.LayoutParams cp = new LinearLayout.LayoutParams(0, -1, 1f);
                cp.leftMargin = dp(3); cp.rightMargin = dp(3);
                row.addView(cell, cp);
                cell.setOnClickListener(v -> {
                    if (key.equals("SATELIT")) {
                        Intent map = new Intent(this, GoogleSatelliteActivity.class);
                        map.putExtra(GoogleSatelliteActivity.EXTRA_LAT, latitude);
                        map.putExtra(GoogleSatelliteActivity.EXTRA_LON, longitude);
                        map.putExtra(GoogleSatelliteActivity.EXTRA_GRID, sizeMeters);
                        startActivity(map);
                    } else {
                        Intent item = new Intent(this, SurferPanelActivity.class);
                        item.putExtra(EXTRA_PANEL, key);
                        item.putExtra(GoogleSatelliteActivity.EXTRA_LAT, latitude);
                        item.putExtra(GoogleSatelliteActivity.EXTRA_LON, longitude);
                        item.putExtra(GoogleSatelliteActivity.EXTRA_GRID, sizeMeters);
                        startActivity(item);
                    }
                });
            }
        }
        TextView key = text("Shfaqje 6-panele · pa raster të sajuar · NISAR hapet nga menuja kryesore",
            12, 0xff687481, false);
        key.setPadding(0, dp(16), 0, 0);
        root.addView(key);
    }

    private void showNisar(LinearLayout root) {
        TextView note = text(
            "NISAR përdor radarët L-band dhe S-band. Ky modul hap katalogët "
            + "zyrtarë për të gjetur skena reale; ende nuk i shfaq rasterët HDF5 në hartë.",
            15, 0xff283b4e, false);
        note.setPadding(dp(12), dp(16), dp(12), dp(16));
        note.setBackground(bg(0xffeef4f8, 16));
        root.addView(note);

        Button l = button("NISAR L-band · NASA / ASF Vertex");
        Button s = button("NISAR S-band · ISRO Bhoonidhi");
        for (Button b : new Button[]{l,s}) {
            b.setBackground(bg(0xffd6f4f0, 13));
            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(-1, dp(56));
            lp.topMargin = dp(14);
            root.addView(b, lp);
        }
        l.setOnClickListener(v -> openPortal("https://vertex-plus.asf.alaska.edu/"));
        s.setOnClickListener(v -> openPortal("https://bhoonidhi.nrsc.gov.in/NISAR/"));
        TextView caution = text(
            "Për skena NASA mund të kërkohet Earthdata Login. "
                + "Një imazh NISAR nuk mat automatikisht tunele ose metale në thellësi.",
            13, 0xff65707d, false);
        caution.setPadding(0, dp(20), 0, 0);
        root.addView(caution);
    }

    private void openPortal(String url) {
        try {
            startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(url)));
        } catch (RuntimeException ex) {
            new AlertDialog.Builder(this)
                .setMessage("Nuk u hap portali. Kontrollo shfletuesin dhe lidhjen me internetin.")
                .setPositiveButton("OK", null).show();
        }
    }

    private void chooseDocument() {
        Intent pick = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        pick.setType("*/*");
        pick.addCategory(Intent.CATEGORY_OPENABLE);
        pick.putExtra(Intent.EXTRA_MIME_TYPES, new String[]{
            "text/plain", "text/csv", "application/csv", "application/octet-stream",
            "application/vnd.ms-excel"
        });
        startActivityForResult(pick, REQUEST_IMPORT);
    }

    @Override protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode != REQUEST_IMPORT || resultCode != RESULT_OK || data == null
            || data.getData() == null) return;
        try {
            importNumbers(data.getData());
            render();
        } catch (Exception ex) {
            new AlertDialog.Builder(this)
                .setTitle("Importi nuk u krye")
                .setMessage("Zgjidh skedar CSV/TXT me matricë numerike (kolona të ndara "
                    + "me presje, pikëpresje ose hapësirë). "
                    + "HDF5, GeoTIFF dhe formate binare kërkojnë modul të veçantë. "
                    + "Detaji: " + ex.getMessage())
                .setPositiveButton("OK", null).show();
        }
    }

    private void importNumbers(Uri uri) throws Exception {
        ArrayList<double[]> rows = new ArrayList<>();
        int commonColumns = -1, total = 0;
        double min = Double.POSITIVE_INFINITY, max = Double.NEGATIVE_INFINITY;
        try (InputStream input = getContentResolver().openInputStream(uri);
             BufferedReader reader = new BufferedReader(
                 new InputStreamReader(input, StandardCharsets.UTF_8))) {
            String line;
            int visited = 0;
            while ((line = reader.readLine()) != null && visited < 256) {
                visited++;
                if (line.length() > 4000) throw new Exception("Rresht shumë i gjatë.");
                line = line.trim();
                if (line.isEmpty() || line.startsWith("#") || line.startsWith("DSAA")) continue;
                String[] words = line.split("[,;\\s]+");
                if (words.length < 2 || words.length > 256) continue;
                double[] values = new double[words.length];
                boolean numeric = true;
                for (int i=0; i<words.length; i++) {
                    try {
                        values[i] = Double.parseDouble(words[i]);
                        if (!Double.isFinite(values[i])) numeric = false;
                    } catch (NumberFormatException ex) { numeric = false; break; }
                }
                if (!numeric) continue;
                if (commonColumns < 0) commonColumns = values.length;
                if (values.length != commonColumns) continue;
                rows.add(values);
                for (double value: values) {
                    min = Math.min(min, value);
                    max = Math.max(max, value);
                }
                total += values.length;
                if (rows.size() >= 128) break;
            }
        }
        if (rows.size() < 2 || total < 4) {
            throw new Exception("Nuk u gjetën të paktën dy rreshta numerikë të njëjtës madhësi.");
        }
        matrix = rows.toArray(new double[0][]);
        samples = total;
        minValue = min; maxValue = max;
        fileName = uri.getLastPathSegment() == null ? "skedar" :
            uri.getLastPathSegment().replace("\n"," ");
    }

    private static final class MatrixPreview extends View {
        private final double[][] values;
        private final double min,max;
        private final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        MatrixPreview(Activity activity, double[][] vals, double lower, double upper) {
            super(activity); this.values=vals; this.min=lower; this.max=upper;
        }
        @Override protected void onDraw(Canvas c) {
            super.onDraw(c);
            if (values.length==0) return;
            float cellW = (float)getWidth()/values[0].length;
            float cellH = (float)getHeight()/values.length;
            for (int y=0; y<values.length; y++) {
                for (int x=0; x<values[y].length; x++) {
                    double ratio = max > min ? (values[y][x] - min)/(max-min) : 0.5;
                    paint.setColor(colorMap(ratio));
                    paint.setStyle(Paint.Style.FILL);
                    c.drawRect(new RectF(x*cellW, y*cellH,
                        (x+1)*cellW+0.2f, (y+1)*cellH+0.2f), paint);
                }
            }
        }
        private int colorMap(double fraction) {
            float t = (float)Math.max(0, Math.min(1,fraction));
            int[] low = t < .5f ? new int[]{23,66,112} : new int[]{44,202,190};
            int[] high= t < .5f ? new int[]{44,202,190} : new int[]{247,174,61};
            float k = t < .5f ? t * 2f : (t-.5f)*2f;
            return Color.rgb((int)(low[0]+(high[0]-low[0])*k),
                (int)(low[1]+(high[1]-low[1])*k),
                (int)(low[2]+(high[2]-low[2])*k));
        }
    }
}
