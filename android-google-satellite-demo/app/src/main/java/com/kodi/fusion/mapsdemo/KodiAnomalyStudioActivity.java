package com.kodi.fusion.mapsdemo;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.drawable.GradientDrawable;
import android.widget.FrameLayout;
import com.google.android.gms.maps.model.CameraPosition;
import android.os.Bundle;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polygon;
import com.google.android.gms.maps.model.PolygonOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * KODI Anomaly Studio – independent VeldScan-style WORKFLOW prototype.
 *
 * Map base is Google Satellite (configured with the existing Maps SDK key).
 * ALL example anomaly pins, colors, reports and 3D shapes are deterministic
 * SYNTHETIC DEMO DATA. No satellite products are downloaded or analyzed here.
 * Never interpret the 3D volume as a measured underground cavity or depth.
 */
public final class KodiAnomalyStudioActivity extends Activity implements OnMapReadyCallback {
    private static final int NAVY = Color.rgb(7,19,32);
    private static final int TEAL = Color.rgb(0,220,220);
    private static final int ORANGE = Color.rgb(255,147,66);
    private MapView mapView;
    private GoogleMap googleMap;
    private TextView status;
    private Button drawButton;
    private Polygon area;
    private Polyline incompleteLine;
    private Marker centerPin;
    private final List<LatLng> vertices = new ArrayList<>();
    private final List<Marker> candidatePins = new ArrayList<>();
    private final List<Circle> heatSpots = new ArrayList<>();
    private final List<Polyline> candidateLinks = new ArrayList<>();
    private final List<LatLng> demoPoints = new ArrayList<>();
    private final List<Marker> vertexPins = new ArrayList<>();
    private LatLng center = new LatLng(42.312015, 21.119746);
    private int gridMeters = 25;
    private int selectedLayer = 0;
    private boolean drawMode = false;
    private boolean demoReady = false;
    private TextView scanCounter;

    private int dp(int value) {
        return Math.round(value * getResources().getDisplayMetrics().density);
    }
    private TextView label(String value, int size, int color) {
        TextView text = new TextView(this);
        text.setText(value);
        text.setTextSize(size);
        text.setTextColor(color);
        return text;
    }
    private Button button(String name) {
        Button b = new Button(this);
        b.setText(name);
        b.setTextSize(11);
        b.setAllCaps(false);
        b.setMinHeight(0);
        b.setTextColor(Color.WHITE);
        GradientDrawable bg=new GradientDrawable();
        bg.setColor(0xff533d94);
        bg.setCornerRadius(dp(15));
        bg.setStroke(dp(1),0xff786bb0);
        b.setBackground(bg);
        return b;
    }
    private void addWeighted(LinearLayout row, View view) {
        LinearLayout.LayoutParams p = new LinearLayout.LayoutParams(0, dp(52), 1f);
        p.setMargins(dp(2), dp(2), dp(2), dp(2));
        row.addView(view, p);
    }
    private LinearLayout row() {
        LinearLayout r = new LinearLayout(this);
        r.setOrientation(LinearLayout.HORIZONTAL);
        return r;
    }

    @Override public void onCreate(Bundle state) {
        super.onCreate(state);
        if (getIntent() != null) {
            double lat = getIntent().getDoubleExtra(GoogleSatelliteActivity.EXTRA_LAT, center.latitude);
            double lon = getIntent().getDoubleExtra(GoogleSatelliteActivity.EXTRA_LON, center.longitude);
            if (Double.isFinite(lat) && Double.isFinite(lon)
                    && Math.abs(lat) < 85 && Math.abs(lon) <= 180) {
                center = new LatLng(lat, lon);
            }
            gridMeters = getIntent().getIntExtra(GoogleSatelliteActivity.EXTRA_GRID, 25) == 50 ? 50 : 25;
        }
        LinearLayout root = new LinearLayout(this);
        root.setBackgroundColor(NAVY);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setPadding(dp(5), dp(5), dp(5), dp(6));

        LinearLayout top = row();
        top.setGravity(Gravity.CENTER_VERTICAL);
        TextView title=label("KODI FUSION PRO  ·  GEO STUDIO",15,Color.WHITE);
        title.setPadding(dp(7),dp(9),dp(4),dp(4));
        top.addView(title,new LinearLayout.LayoutParams(0,dp(46),1f));
        Button topReport=button("Raporti");
        LinearLayout.LayoutParams reportTopParams=
            new LinearLayout.LayoutParams(dp(101),dp(42));
        top.addView(topReport,reportTopParams);
        root.addView(top);

        TextView disclaimer = label(
            "DEMO E NDËRFAQES · Pa imazhe SAR/optike të analizuara",11,ORANGE);
        disclaimer.setPadding(dp(8), dp(3), dp(5), dp(7));
        root.addView(disclaimer);

        LinearLayout tools = row();
        Button grid25=button("25×25 m");
        Button grid50=button("50×50 m");
        drawButton=button("Vizato zonën");
        addWeighted(tools,grid25);
        addWeighted(tools,grid50);
        addWeighted(tools,drawButton);
        root.addView(tools);

        FrameLayout mapFrame = new FrameLayout(this);
        mapView = new MapView(this);
        mapView.onCreate(state);
        mapFrame.addView(mapView,new FrameLayout.LayoutParams(-1,-1));

        LinearLayout mapLeft = new LinearLayout(this);
        mapLeft.setOrientation(LinearLayout.VERTICAL);
        Button centerMap=button("⌖");
        Button satellite=button("Satelit");
        Button clearMap=button("Pastro");
        Button sourcesMap=button("Shtresat");
        for(Button item:new Button[]{centerMap,satellite,clearMap,sourcesMap}){
            LinearLayout.LayoutParams p=new LinearLayout.LayoutParams(dp(80),dp(44));
            p.setMargins(0,dp(2),0,dp(5));
            mapLeft.addView(item,p);
        }
        FrameLayout.LayoutParams leftParams=new FrameLayout.LayoutParams(
            dp(82),-2,Gravity.START|Gravity.TOP);
        leftParams.setMargins(dp(9),dp(12),0,0);
        mapFrame.addView(mapLeft,leftParams);

        LinearLayout mapRight = new LinearLayout(this);
        mapRight.setOrientation(LinearLayout.VERTICAL);
        Button view2D=button("2D");
        Button view3D=button("3D hartë");
        for(Button item:new Button[]{view2D,view3D}){
            LinearLayout.LayoutParams p=new LinearLayout.LayoutParams(dp(85),dp(43));
            p.setMargins(0,dp(2),0,dp(5));
            mapRight.addView(item,p);
        }
        FrameLayout.LayoutParams rightParams=new FrameLayout.LayoutParams(
            dp(90),-2,Gravity.END|Gravity.TOP);
        rightParams.setMargins(0,dp(12),dp(8),0);
        mapFrame.addView(mapRight,rightParams);

        TextView mapFlag=label(
            "KODI · PAMJE PROVE — ASNJË ZBULIM I KONFIRMUAR",10,Color.WHITE);
        mapFlag.setBackgroundColor(0xc9001e2c);
        mapFlag.setPadding(dp(10),dp(8),dp(10),dp(8));
        FrameLayout.LayoutParams flagParams=new FrameLayout.LayoutParams(
            -1,-2,Gravity.BOTTOM);
        flagParams.setMargins(dp(5),0,dp(5),dp(7));
        mapFrame.addView(mapFlag,flagParams);
        root.addView(mapFrame,new LinearLayout.LayoutParams(-1,0,1f));

        LinearLayout bottomPanel=new LinearLayout(this);
        bottomPanel.setBackgroundColor(0xff071826);
        bottomPanel.setPadding(dp(4),dp(7),dp(4),dp(5));
        bottomPanel.setOrientation(LinearLayout.VERTICAL);
        root.addView(bottomPanel);

        scanCounter=label("VËZHGIME: 0  ·  WGS84 · PA MATJE TË DHËNASH",11,TEAL);
        scanCounter.setPadding(dp(5),dp(4),dp(5),dp(5));
        bottomPanel.addView(scanCounter);

        status=label("Prek hartën për qendrën. Vizato poligonin ose zgjidh gridin.",
            12,0xffb8e3e0);
        status.setPadding(dp(5),dp(4),dp(5),dp(7));
        bottomPanel.addView(status);

        TextView layerHeader=label("SHTRESAT E TË DHËNAVE",10,0xffb2bed4);
        layerHeader.setPadding(dp(4),dp(4),0,dp(2));
        bottomPanel.addView(layerHeader);
        LinearLayout layers=row();
        Button anomalies=button("Anomali");
        Button plants=button("Bimësi");
        Button moisture=button("Lagështi");
        Button radar=button("Radar SAR");
        addWeighted(layers,anomalies);
        addWeighted(layers,plants);
        addWeighted(layers,moisture);
        addWeighted(layers,radar);
        bottomPanel.addView(layers);

        TextView scanHeader=label("MODALITETI I SKANIMIT",10,0xffb2bed4);
        scanHeader.setPadding(dp(4),dp(6),0,dp(2));
        bottomPanel.addView(scanHeader);
        LinearLayout scanModes=row();
        Button visualMode=button("Optik / Reliev");
        Button thermalMode=button("Termik");
        Button gpsMode=button("Mbulimi GPS");
        addWeighted(scanModes,visualMode);
        addWeighted(scanModes,thermalMode);
        addWeighted(scanModes,gpsMode);
        bottomPanel.addView(scanModes);

        LinearLayout actions=row();
        Button simulate=button("DEMO · SKANO ZONËN");
        Button report=button("Raport");
        Button volume=button("3D volum");
        addWeighted(actions,simulate);
        addWeighted(actions,report);
        addWeighted(actions,volume);
        bottomPanel.addView(actions);

        LinearLayout footer=row();
        Button real=button("Burimet reale");
        Button export=button("Ndaj raportin");
        addWeighted(footer,real);
        addWeighted(footer,export);
        bottomPanel.addView(footer);
        setContentView(root);

        topReport.setOnClickListener(v->showReport());
        centerMap.setOnClickListener(v->{
            if(googleMap!=null)
                googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(center,19f));
        });
        satellite.setOnClickListener(v->{
            if(googleMap!=null){
                boolean hybrid=googleMap.getMapType()==GoogleMap.MAP_TYPE_HYBRID;
                googleMap.setMapType(hybrid?GoogleMap.MAP_TYPE_SATELLITE:
                    GoogleMap.MAP_TYPE_HYBRID);
                satellite.setText(hybrid?"Satelit":"Hibrid");
            }
        });
        clearMap.setOnClickListener(v->{
            clearDemo();vertices.clear();drawMode=false;
            drawButton.setText("Vizato zonën");drawArea();
            status.setText("Pikat DEMO dhe poligoni u pastruan. "
                +"Qendra dhe gridi mbeten të disponueshëm.");
        });
        sourcesMap.setOnClickListener(v->showDataSources());
        view2D.setOnClickListener(v->setMapTilt(false));
        view3D.setOnClickListener(v->setMapTilt(true));
        radar.setOnClickListener(v->showUnavailableLayer("Radar Sentinel-1"));
        visualMode.setOnClickListener(v->showUnavailableLayer("Optik/Reliev"));
        thermalMode.setOnClickListener(v->showUnavailableLayer("Termik Landsat"));
        gpsMode.setOnClickListener(v->new AlertDialog.Builder(this)
            .setTitle("Mbulimi GPS")
            .setMessage("Nuk është aktivizuar regjistrimi GPS në këtë modul. "
                +"Nuk krijojmë gjurmë artificiale. "
                +"GeoTrace në aplikacion shërben për gjurmim real të pikave.")
            .setPositiveButton("OK",null).show());

        grid25.setOnClickListener(v -> { gridMeters=25; vertices.clear(); drawMode=false;
            drawButton.setText("Vizato zonën"); clearDemo(); drawArea(); });
        grid50.setOnClickListener(v -> { gridMeters=50; vertices.clear(); drawMode=false;
            drawButton.setText("Vizato zonën"); clearDemo(); drawArea(); });
        drawButton.setOnClickListener(v -> {
            if (!drawMode) {
                vertices.clear();
                drawMode=true;
                drawButton.setText("Mbyll zonën");
                clearDemo();
                drawArea();
                status.setText("Prek 3 ose më shumë pika në hartë për poligonin.");
            } else {
                drawMode=false;
                drawButton.setText("Vizato zonë");
                if (vertices.size() < 3) {
                    vertices.clear();
                    Toast.makeText(this,"Poligoni kërkon të paktën 3 pika",Toast.LENGTH_LONG).show();
                } else {
                    // The report's WGS84 reference center must follow the AOI,
                    // rather than retaining a coordinate from a previous grid.
                    double lat=0,lon=0;
                    for (LatLng point:vertices) {
                        lat+=point.latitude;
                        lon+=point.longitude;
                    }
                    center=new LatLng(lat/vertices.size(),lon/vertices.size());
                }
                drawArea();
            }
        });
        anomalies.setOnClickListener(v -> changeLayer(0));
        plants.setOnClickListener(v -> changeLayer(1));
        moisture.setOnClickListener(v -> changeLayer(2));
        simulate.setOnClickListener(v -> generateSyntheticDemo());
        report.setOnClickListener(v -> showReport());
        volume.setOnClickListener(v -> show3D());
        real.setOnClickListener(v -> showDataSources());
        export.setOnClickListener(v -> shareReport());
        mapView.getMapAsync(this);
    }

    @Override public void onMapReady(GoogleMap map) {
        googleMap = map;
        map.setMapType(GoogleMap.MAP_TYPE_SATELLITE);
        map.moveCamera(CameraUpdateFactory.newLatLngZoom(center, 19f));
        map.setOnMapClickListener(p -> {
            clearDemo();
            if (drawMode) {
                vertices.add(p);
                drawArea();
                status.setText("Poligoni: "+vertices.size()+" pika. Shtyp 'Mbyll zonën'.");
            } else {
                // A tap outside drawing mode starts a new grid centered here.
                // Do not leave an old polygon drawn around another location.
                vertices.clear();
                center = p;
                drawArea();
                status.setText(String.format(Locale.US,
                    "Gridi u zhvendos · Qendra WGS84: %.6f, %.6f",p.latitude,p.longitude));
            }
        });
        drawArea();
    }

    private List<LatLng> square() {
        double half=gridMeters/2.0;
        double dLat=half/111320.0;
        double dLon=half/(111320.0*Math.max(0.05,
            Math.abs(Math.cos(Math.toRadians(center.latitude)))));
        List<LatLng> p = new ArrayList<>();
        p.add(new LatLng(center.latitude-dLat,center.longitude-dLon));
        p.add(new LatLng(center.latitude-dLat,center.longitude+dLon));
        p.add(new LatLng(center.latitude+dLat,center.longitude+dLon));
        p.add(new LatLng(center.latitude+dLat,center.longitude-dLon));
        return p;
    }
    private void drawArea() {
        if (googleMap == null) return;
        if (area != null) {area.remove(); area=null;}
        if (incompleteLine != null) {incompleteLine.remove(); incompleteLine=null;}
        if (centerPin != null) {centerPin.remove(); centerPin=null;}
        for (Marker pin: vertexPins) pin.remove();
        vertexPins.clear();
        List<LatLng> p = drawMode ? vertices :
            vertices.isEmpty() ? square() : vertices;
        for (int i=0; i<vertices.size(); i++) {
            Marker pin=googleMap.addMarker(new MarkerOptions()
                .position(vertices.get(i))
                .title("Kulmi "+(i+1)));
            if (pin!=null) vertexPins.add(pin);
        }
        if (p.size() >= 3) {
            area=googleMap.addPolygon(new PolygonOptions().addAll(p)
                .strokeColor(TEAL).strokeWidth(4f)
                .fillColor(Color.argb(32,0,220,220)));
        } else if (p.size() == 2) {
            incompleteLine=googleMap.addPolyline(new PolylineOptions()
                .addAll(p).color(TEAL).width(4f));
        }
        if (vertices.isEmpty() && !drawMode) {
            centerPin=googleMap.addMarker(new MarkerOptions()
                .position(center).title("Qendra WGS84"));
        }
    }
    private void clearDemo() {
        demoReady = false;
        for (Marker m:candidatePins) m.remove();
        for (Circle c:heatSpots) c.remove();
        candidatePins.clear();
        heatSpots.clear();
        for (Polyline link: candidateLinks) link.remove();
        candidateLinks.clear();
        demoPoints.clear();
        if(scanCounter!=null)scanCounter.setText("VËZHGIME: 0 · WGS84 · PA MATJE TË DHËNASH");
    }
    private Bitmap numberCircle(int number, int color) {
        Bitmap b=Bitmap.createBitmap(dp(45), dp(45), Bitmap.Config.ARGB_8888);
        Canvas c=new Canvas(b);
        Paint p=new Paint(Paint.ANTI_ALIAS_FLAG);
        p.setColor(Color.WHITE);
        c.drawCircle(b.getWidth()/2f,b.getHeight()/2f,dp(21),p);
        p.setColor(color);
        c.drawCircle(b.getWidth()/2f,b.getHeight()/2f,dp(18),p);
        p.setColor(Color.WHITE);
        p.setTextSize(dp(19));
        p.setTypeface(android.graphics.Typeface.create("sans-serif",android.graphics.Typeface.BOLD));
        p.setTextAlign(Paint.Align.CENTER);
        c.drawText(String.valueOf(number), b.getWidth()/2f,
            b.getHeight()/2f+dp(7),p);
        return b;
    }
    /** Point-in-polygon with longitude on X, latitude on Y. */
    private static boolean insidePolygon(LatLng point, List<LatLng> polygon) {
        if (polygon.size() < 3) return false;
        boolean inside=false;
        for (int i=0,j=polygon.size()-1;i<polygon.size();j=i++) {
            LatLng a=polygon.get(i),b=polygon.get(j);
            boolean crosses=(a.latitude>point.latitude)!=(b.latitude>point.latitude);
            if (crosses) {
                double xAtY=(b.longitude-a.longitude)*
                    (point.latitude-a.latitude)/(b.latitude-a.latitude)+a.longitude;
                if (point.longitude < xAtY) inside=!inside;
            }
        }
        return inside;
    }

    /**
     * For hand-drawn AOIs, synthetic points MUST belong to the actual polygon.
     * This is only visual geometry; it is not a geophysical algorithm.
     */
    private List<LatLng> syntheticPositions() {
        List<LatLng> out=new ArrayList<>();
        if (vertices.size()<3) {
            // 18 evenly distributed examples, all strictly inside the grid.
            for(int row=0;row<3;row++) {
                for(int col=0;col<6;col++) {
                    double fx=(col+0.5)/6.0-0.5;
                    double fy=(row+0.5)/3.0-0.5;
                    double jitterX=Math.sin((col+1)*(row+3)*2.7)*0.025;
                    double jitterY=Math.cos((col+4)*(row+1)*1.9)*0.025;
                    double dy=(fy+jitterY)*gridMeters/111320.0;
                    double dx=(fx+jitterX)*gridMeters/
                        (111320.0*Math.max(0.05,
                            Math.abs(Math.cos(Math.toRadians(center.latitude)))));
                    out.add(new LatLng(center.latitude+dy,center.longitude+dx));
                }
            }
            return out;
        }
        double minLat=Double.POSITIVE_INFINITY,maxLat=Double.NEGATIVE_INFINITY;
        double minLon=Double.POSITIVE_INFINITY,maxLon=Double.NEGATIVE_INFINITY;
        for (LatLng v:vertices) {
            minLat=Math.min(minLat,v.latitude);maxLat=Math.max(maxLat,v.latitude);
            minLon=Math.min(minLon,v.longitude);maxLon=Math.max(maxLon,v.longitude);
        }
        ArrayList<LatLng> candidates=new ArrayList<>();
        final int resolution=48;
        for(int y=1;y<resolution;y++) {
            for(int x=1;x<resolution;x++) {
                LatLng p=new LatLng(minLat+(maxLat-minLat)*y/resolution,
                    minLon+(maxLon-minLon)*x/resolution);
                if(insidePolygon(p,vertices)) candidates.add(p);
            }
        }
        if(candidates.isEmpty()) return out;
        int count=Math.min(18,candidates.size());
        for(int i=0;i<count;i++) {
            int index=(int)Math.floor((i+0.5)*candidates.size()/count);
            out.add(candidates.get(Math.min(index,candidates.size()-1)));
        }
        return out;
    }

    private void generateSyntheticDemo() {
        if (googleMap==null) return;
        if (drawMode) {
            Toast.makeText(this,"Mbyll fillimisht zonën",Toast.LENGTH_SHORT).show();
            return;
        }
        if(selectedLayer!=0){
            clearDemo();
            status.setText("Bimësi/Lagështi: nuk ka skena reale të lidhura. "
                +"DEMO është vetëm për shënjimin e anomalive sintetike.");
            return;
        }
        List<LatLng> positions=syntheticPositions();
        clearDemo();
        if(positions.isEmpty()) {
            status.setText("Poligoni është tepër i vogël ose i pavlefshëm për pikat DEMO. "
                +"Vizato një zonë me sipërfaqe.");
            return;
        }
        demoReady=true;
        int[] colors={0xfff5a623,0xff20b9dc,0xff5bc582,
            0xffee565f,0xfff5a623,0xff20b9dc,
            0xffee565f,0xff5bc582,0xffee565f};
        int count=positions.size();
        for(int i=0;i<count;i++) {
            LatLng p=positions.get(i);
            demoPoints.add(p);
            int color=colors[i % colors.length];
            heatSpots.add(googleMap.addCircle(new CircleOptions()
                .center(p).radius(Math.max(1.0,gridMeters*0.07))
                .fillColor(Color.argb(65,
                    Color.red(color),Color.green(color),Color.blue(color)))
                .strokeColor(Color.argb(140,
                    Color.red(color),Color.green(color),Color.blue(color)))
                .strokeWidth(1.5f)));
            Marker marker=googleMap.addMarker(new MarkerOptions().position(p)
                .icon(BitmapDescriptorFactory.fromBitmap(numberCircle(i+1,color)))
                .title("DEMO · Pika "+(i+1))
                .snippet("Shembull sintetik. Jo anomali e zbuluar."));
            if (marker!=null){marker.setTag(i+1);candidatePins.add(marker);}
        }
        // Cyan lines are schematic DEMO connectivity, not detected tunnels.
        for(int i=0;i+1<demoPoints.size();i++) {
            if(i%6==5) continue;
            candidateLinks.add(googleMap.addPolyline(new PolylineOptions()
                .add(demoPoints.get(i),demoPoints.get(i+1))
                .color(0xff00d3d9).width(3.2f)));
        }
        if(scanCounter!=null)
            scanCounter.setText("VËZHGIME DEMO: "+candidatePins.size()
                +"  ·  MODELI: SINTETIK  ·  THELLËSIA: E PANJOHUR");
        googleMap.setOnMarkerClickListener(marker -> {
            Object tag=marker.getTag();
            if(tag instanceof Number) {
                showCandidate(((Number)tag).intValue());
                return true;
            }
            return false;
        });
        status.setText("DEMO AKTIV · "+candidatePins.size()
            +" pika sintetike brenda zonës. Nuk ka skanim real satelitor.");
    }

    private void setMapTilt(boolean is3D) {
        if(googleMap==null) return;
        CameraPosition next=new CameraPosition.Builder(googleMap.getCameraPosition())
            .tilt(is3D?55f:0f).build();
        googleMap.animateCamera(CameraUpdateFactory.newCameraPosition(next));
        status.setText(is3D
            ? "3D HARTË · pjerrësi e pamjes Google; jo model i nëntokës."
            : "2D HARTË · pamja satelitore nga lart.");
    }
    private void showUnavailableLayer(String name) {
        selectedLayer=3;
        clearDemo();
        status.setText(name+" · nuk ka produkt real të ngarkuar.");
        new AlertDialog.Builder(this).setTitle(name+" · PA TË DHËNA")
            .setMessage("Kjo shtresë do të funksionojë pasi të lidhen "
                +"produktet reale dhe përpunimi për zonën. "
                +"Nuk shfaqen ngjyra të sajuara si të dhëna të matura.")
            .setPositiveButton("OK",null).show();
    }
    private void changeLayer(int mode) {
        selectedLayer=mode;
        clearDemo();
        if (mode==0) {
            status.setText("ANOMALI · vetëm simulim i qartë. "
                +"Shtyp DEMO · ANALIZË për pika shembull.");
        } else {
            String name=mode==1?"Bimësi":"Lagështi";
            status.setText(name.toUpperCase(Locale.ROOT)
                +" · PA TË DHËNA. Nuk është lidhur Sentinel-2 L2A. "
                +"Pikat e mëparshme DEMO janë fshehur.");
        }
    }
    private String reportText() {
        StringBuilder r=new StringBuilder();
        r.append("KODI FUSION PRO · RAPORT DEMONSTRUES\n");
        r.append(String.format(Locale.US,"Qendra WGS84: %.6f, %.6f\n",
            center.latitude,center.longitude));
        r.append("Zona: ").append(vertices.size()>=3
            ? "Poligon / "+vertices.size()+" kulme"
            : gridMeters+" × "+gridMeters+" m").append("\n");
        r.append("Gjendja e burimeve: NUK JANË SHKARKUAR RASTERE\n");
        r.append("Sentinel-1 / Sentinel-2 / NISAR: JO ANALIZUAR\n");
        r.append("GPR / ERT / magnetometri: NUK KA MATJE\n");
        r.append("Zgavër ose metal i konfirmuar: ASNJË\n");
        r.append("Thellësia: E PANJOHUR\n");
        r.append("Besueshmëria shkencore: NUK ËSHTË LLOGARITUR\n\n");
        r.append("Pika sintetike: ").append(demoPoints.size()).append("\n");
        for(int i=0;i<demoPoints.size();i++) {
            LatLng point=demoPoints.get(i);
            r.append(String.format(Locale.US,"#%02d  %.6f, %.6f [DEMO]\n",
                i+1,point.latitude,point.longitude));
        }
        r.append("\nKëto pika ilustrojnë navigimin dhe raportimin. ");
        r.append("Asnjëra nuk është zbulim nga sateliti apo matje nëntokësore.");
        return r.toString();
    }
    private void showCandidate(int i) {
        if(i<1||i>demoPoints.size())return;
        LatLng p=demoPoints.get(i-1);
        String coordinates=String.format(Locale.US,"%.6f, %.6f",
            p.latitude,p.longitude);
        new AlertDialog.Builder(this).setTitle("Vëzhgimi #"+i+" · DEMO")
            .setMessage("Koordinata WGS84: "+coordinates
                +"\nBurimi: pikë sintetike për testim të ndërfaqes."
                +"\nThellësi e matur: NUK KA."
                +"\nRadar/termik/optik: JO TË ANALIZUAR."
                +"\nAsnjë përqindje besueshmërie nuk llogaritet.")
            .setPositiveButton("3D demo",(d,w)->show3DFor(i))
            .setNegativeButton("Mbyll",null).show();
    }
    private void showReport() {
        new AlertDialog.Builder(this)
            .setTitle("Raport analitik · DEMO")
            .setMessage(reportText())
            .setPositiveButton("3D",(d,w)->show3D())
            .setNegativeButton("Mbyll",null).show();
    }
    private void shareReport() {
        Intent send=new Intent(Intent.ACTION_SEND);
        send.setType("text/plain");
        send.putExtra(Intent.EXTRA_SUBJECT,"KODI FUSION PRO · Raport DEMO");
        send.putExtra(Intent.EXTRA_TEXT,reportText());
        startActivity(Intent.createChooser(send,"Ndaj raportin DEMO"));
    }
    private void showDataSources() {
        new AlertDialog.Builder(this).setTitle("Shtresat reale · ende pa integrim")
            .setMessage("Sentinel-1 GRD: radar i sipërfaqes.\n"+
                "Sentinel-2 L2A: spektër / bimësi / lagështi relative.\n"+
                "Landsat: temperaturë e sipërfaqes, rezolucion më i ulët.\n"+
                "Copernicus DEM: reliev dhe pjerrësi.\n"+
                "NISAR GCOV: kërkon raster dhe dekodim të vërtetë.\n\n"+
                "Asnjëra nuk është e lidhur me këtë DEMO. "+
                "Satelitët nuk japin vetvetiu thellësi të metaleve ose zgavrave.")
            .setPositiveButton("OK",null).show();
    }
    private void show3D() {
        show3DFor(1);
    }
    private void show3DFor(int observation) {
        if (!demoReady) {
            new AlertDialog.Builder(this).setTitle("3D · pa të dhëna")
                .setMessage("Nuk ka pikë aktive. Shtyp DEMO · ANALIZË për një "
                    +"ilustrim 3D; një model 3D real kërkon matje të vërteta.")
                .setPositiveButton("OK",null).show();
            return;
        }
        android.app.Dialog dialog=new android.app.Dialog(this);
        dialog.requestWindowFeature(android.view.Window.FEATURE_NO_TITLE);
        LinearLayout root=new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setPadding(dp(12),dp(18),dp(12),dp(12));
        root.setBackgroundColor(NAVY);

        LinearLayout head=row();
        head.setGravity(Gravity.CENTER_VERTICAL);
        TextView title=label("VËZHGIMI #"+observation+" · MODEL 3D",19,Color.WHITE);
        head.addView(title,new LinearLayout.LayoutParams(0,dp(52),1f));
        Button close=button("Mbyll");
        head.addView(close,new LinearLayout.LayoutParams(dp(84),dp(48)));
        root.addView(head);
        close.setOnClickListener(v->dialog.dismiss());

        TextView note=label(
            "DEMO: Model gjeometrik i simuluar. Z nuk është thellësi e matur. "
            +"Rrotullo me një gisht; zmadho me dy gishta.",12,0xffa9d6e3);
        note.setPadding(dp(6),dp(8),dp(6),dp(12));
        root.addView(note);
        VolumePreview3D preview=new VolumePreview3D(observation);
        root.addView(preview,new LinearLayout.LayoutParams(-1,0,1f));

        TextView thresholdLabel=label("PRAGU I SHFAQJES SË SINJALIT DEMO",12,TEAL);
        thresholdLabel.setPadding(dp(8),dp(14),dp(8),dp(4));
        root.addView(thresholdLabel);
        SeekBar slider=new SeekBar(this);
        slider.setMax(100);slider.setProgress(62);
        slider.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            public void onProgressChanged(SeekBar seek,int value,boolean user) {
                preview.setThreshold(value);
            }
            public void onStartTrackingTouch(SeekBar seek) {}
            public void onStopTrackingTouch(SeekBar seek) {}
        });
        root.addView(slider);
        TextView proof=label(
            "Vetëm tregues vizual. Pa GPR, ERT, imazh SAR të dekoduar "
            +"ose verifikim në terren.",11,ORANGE);
        proof.setPadding(dp(8),dp(8),dp(8),dp(16));
        root.addView(proof);
        dialog.setContentView(root);
        dialog.show();
        if(dialog.getWindow()!=null)dialog.getWindow().setLayout(-1,-1);
    }

    @Override protected void onResume(){super.onResume();if(mapView!=null)mapView.onResume();}
    @Override protected void onPause(){if(mapView!=null)mapView.onPause();super.onPause();}
    @Override protected void onStart(){super.onStart();if(mapView!=null)mapView.onStart();}
    @Override protected void onStop(){if(mapView!=null)mapView.onStop();super.onStop();}
    @Override protected void onDestroy(){if(mapView!=null)mapView.onDestroy();super.onDestroy();}
    @Override public void onLowMemory(){super.onLowMemory();if(mapView!=null)mapView.onLowMemory();}

    private final class VolumePreview3D extends View {
        private final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        private float azimuth=0.45f, elevation=0.22f, zoom=1f;
        private float touchX,touchY,touchDist;
        private int threshold=62;
        private final int observation;
        VolumePreview3D(int observation){
            super(KodiAnomalyStudioActivity.this);
            this.observation=Math.max(1,observation);
        }
        void setThreshold(int threshold){this.threshold=threshold;invalidate();}
        private float[] projected(float x,float y,float z,float cx,float cy,float s) {
            float a=(float)Math.cos(azimuth),b=(float)Math.sin(azimuth);
            float rx=x*a-z*b, rz=x*b+z*a;
            float c=(float)Math.cos(elevation),d=(float)Math.sin(elevation);
            float ry=y*c-rz*d;
            float depth=y*d+rz*c;
            float factor=s*zoom/(1f+Math.max(-0.8f,depth)*0.13f);
            return new float[]{cx+rx*factor,cy-ry*factor};
        }
        @Override protected void onDraw(Canvas canvas) {
            super.onDraw(canvas);
            float w=getWidth(),h=getHeight(),cx=w/2f,cy=h/2f;
            canvas.drawColor(0xff07131e);
            float scale=Math.min(w,h)*0.27f;
            float[][] cube=new float[8][];
            for(int i=0;i<8;i++){
                float x=(i&1)==0?-1:1;
                float y=(i&2)==0?-0.65f:0.65f;
                float z=(i&4)==0?-1:1;
                cube[i]=projected(x,y,z,cx,cy,scale);
            }
            int[][] edge={{0,1},{0,2},{0,4},{1,3},{1,5},
                {2,3},{2,6},{3,7},{4,5},{4,6},{5,7},{6,7}};
            paint.setColor(0xffd0eaf4);paint.setStrokeWidth(dp(1));
            for(int[] e:edge)canvas.drawLine(cube[e[0]][0],cube[e[0]][1],
                cube[e[1]][0],cube[e[1]][1],paint);
            float hx=(float)(Math.sin(observation*1.37)*0.45);
            float hz=(float)(Math.cos(observation*0.83)*0.42);
            float[] hotspot=projected(hx,0.11f,hz,cx,cy,scale);
            paint.setColor(0xffef8d59);
            paint.setAlpha(50+threshold*2);
            canvas.drawCircle(hotspot[0],hotspot[1],dp(12)+threshold/10f,paint);
            paint.setAlpha(255);paint.setColor(0xffadffed);paint.setTextSize(dp(12));
            canvas.drawText("VËZHGIMI #"+observation+" · Z JO I MATUR",dp(10),dp(23),paint);
            canvas.drawText("Asnjë thellësi / zgavër nuk është identifikuar.",dp(10),h-dp(15),paint);
        }
        @Override public boolean onTouchEvent(MotionEvent event){
            int action=event.getActionMasked();
            if(action==MotionEvent.ACTION_DOWN) {
                touchX=event.getX();touchY=event.getY();return true;
            }
            if(action==MotionEvent.ACTION_POINTER_DOWN && event.getPointerCount()>=2) {
                touchDist=distance(event);return true;
            }
            if(action==MotionEvent.ACTION_MOVE) {
                if(event.getPointerCount()>=2) {
                    float d=distance(event);
                    if(touchDist>0 && d>0)
                        zoom=Math.max(0.5f,Math.min(2.5f,zoom*d/touchDist));
                    touchDist=d;
                } else {
                    azimuth+=(event.getX()-touchX)/180f;
                    elevation=Math.max(-1.1f,Math.min(1.1f,
                        elevation+(event.getY()-touchY)/210f));
                    touchX=event.getX();touchY=event.getY();
                }
                invalidate();return true;
            }
            if(action==MotionEvent.ACTION_POINTER_UP){touchDist=0;return true;}
            return action==MotionEvent.ACTION_UP || super.onTouchEvent(event);
        }
        private float distance(MotionEvent e){
            float dx=e.getX(0)-e.getX(1),dy=e.getY(0)-e.getY(1);
            return (float)Math.hypot(dx,dy);
        }
    }
}
