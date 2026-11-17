package com.kodi.fusion.mapsdemo;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
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
    private final List<Marker> vertexPins = new ArrayList<>();
    private LatLng center = new LatLng(42.312015, 21.119746);
    private int gridMeters = 25;
    private int selectedLayer = 0;
    private boolean drawMode = false;
    private boolean demoReady = false;

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
        root.setPadding(dp(7), dp(5), dp(7), dp(7));
        TextView title = label("KODI FUSION PRO · ANOMALY STUDIO", 16, Color.WHITE);
        title.setPadding(dp(5), dp(6), dp(5), dp(1));
        root.addView(title);
        TextView disclaimer = label("DEMO VIZUALE · pika sintetike · jo rezultat satelitor", 11, ORANGE);
        disclaimer.setPadding(dp(5), dp(2), dp(5), dp(3));
        root.addView(disclaimer);
        LinearLayout tools = row();
        Button grid25 = button("25×25 m");
        Button grid50 = button("50×50 m");
        drawButton = button("Vizato zonë");
        addWeighted(tools,grid25);
        addWeighted(tools,grid50);
        addWeighted(tools,drawButton);
        root.addView(tools);

        mapView = new MapView(this);
        mapView.onCreate(state);
        root.addView(mapView,new LinearLayout.LayoutParams(-1,0,1f));

        status = label("Harta satelitore. Zgjidh gridin ose vizato poligonin.", 12, 0xffa7f3ee);
        status.setPadding(dp(5), dp(7), dp(5), dp(5));
        root.addView(status);

        LinearLayout layers = row();
        Button anomalies = button("Anomali");
        Button plants = button("Bimësi");
        Button moisture = button("Lagështi");
        addWeighted(layers, anomalies);
        addWeighted(layers, plants);
        addWeighted(layers, moisture);
        root.addView(layers);

        LinearLayout actions = row();
        Button simulate = button("DEMO · ANALIZË");
        Button report = button("RAPORTI");
        Button volume = button("3D");
        addWeighted(actions, simulate);
        addWeighted(actions, report);
        addWeighted(actions, volume);
        root.addView(actions);

        LinearLayout footer = row();
        Button real = button("BURIMET REALE");
        Button export = button("NDAJ RAPORTIN");
        addWeighted(footer, real);
        addWeighted(footer, export);
        root.addView(footer);
        setContentView(root);

        grid25.setOnClickListener(v -> { gridMeters=25; vertices.clear(); drawMode=false;
            drawButton.setText("Vizato zonë"); clearDemo(); drawArea(); });
        grid50.setOnClickListener(v -> { gridMeters=50; vertices.clear(); drawMode=false;
            drawButton.setText("Vizato zonë"); clearDemo(); drawArea(); });
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
            double[] dy={0.17,-0.12,0.04};
            double[] dx={-0.15,0.18,0.08};
            double scale=gridMeters*0.70;
            for(int i=0;i<3;i++)
                out.add(new LatLng(center.latitude+dy[i]*scale/111320.0,
                    center.longitude+dx[i]*scale/
                        (111320.0*Math.max(0.05,
                            Math.abs(Math.cos(Math.toRadians(center.latitude)))))));
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
        // Use three interior points at different positions of the scan.
        if(candidates.isEmpty()) return out;
        int[] ids={candidates.size()/5,candidates.size()/2,
            candidates.size()*4/5};
        for(int i:ids) {
            LatLng p=candidates.get(Math.min(i,candidates.size()-1));
            if(out.isEmpty() || !p.equals(out.get(out.size()-1))) out.add(p);
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
        int[] colors={ORANGE,TEAL,0xffe84e5e};
        int count=Math.min(colors.length,positions.size());
        for(int i=0;i<count;i++) {
            LatLng p=positions.get(i);
            heatSpots.add(googleMap.addCircle(new CircleOptions()
                .center(p).radius(Math.max(1.0,gridMeters*0.07))
                .fillColor(Color.argb(65,
                    Color.red(colors[i]),Color.green(colors[i]),Color.blue(colors[i])))
                .strokeColor(Color.argb(140,
                    Color.red(colors[i]),Color.green(colors[i]),Color.blue(colors[i])))
                .strokeWidth(1.5f)));
            Marker marker=googleMap.addMarker(new MarkerOptions().position(p)
                .icon(BitmapDescriptorFactory.fromBitmap(numberCircle(i+1,colors[i])))
                .title("DEMO · Pika "+(i+1))
                .snippet("Shembull sintetik. Jo anomali e zbuluar."));
            if (marker!=null){marker.setTag(i+1);candidatePins.add(marker);}
        }
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
        return String.format(Locale.US,
            "KODI FUSION PRO · RAPORT DEMONSTRUES\n"+
            "WGS84: %.6f, %.6f\n"+
            "Zona: %s\n"+
            "Pikat: %s\n"+
            "Skenat satelitore: ASNJË\n"+
            "Matje GPR/ERT: ASNJË\n"+
            "Thellësia: E PANJOHUR\n"+
            "Besueshmëri statistikore: NUK ËSHTË LLOGARITUR\n"+
            "Nuk janë identifikuar zgavra, metale apo objekte. "+
            "Pikat e DEMO-s janë të krijuara artificialisht për të provuar ndërfaqen.",
            center.latitude,center.longitude,
            vertices.size()>=3 ? "Poligon me "+vertices.size()+" kulme" : gridMeters+"×"+gridMeters+" m",
            demoReady ? candidatePins.size()+" sintetike" : "0");
    }
    private void showCandidate(int i) {
        new AlertDialog.Builder(this).setTitle("Pika #"+i+" · DEMO")
            .setMessage("Ky është një shembull i krijuar në aplikacion. "+
                "Nuk është produkt i radarit, sensorit termik apo analizës optike. "+
                "Për një vëzhgim real duhen data/skedarë satelitorë, "+
                "rezolucioni, algoritmi dhe verifikimi në terren.")
            .setPositiveButton("3D demo",(d,w)->show3D())
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
        if (!demoReady) {
            new AlertDialog.Builder(this).setTitle("3D · pa të dhëna")
                .setMessage("Nuk ka pikë aktive. Shtyp DEMO · ANALIZË për një "
                    +"ilustrim 3D; një model 3D real kërkon matje të vërteta.")
                .setPositiveButton("OK",null).show();
            return;
        }
        LinearLayout root=new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setPadding(dp(8),dp(8),dp(8),dp(8));
        root.setBackgroundColor(NAVY);
        TextView note=label("3D ILLUSTRIM · rrotullo me gisht / zmadho me dy gishta. "+
            "Boshti Z është grafik; JO thellësi e matur.",12,Color.WHITE);
        note.setPadding(dp(6),dp(8),dp(6),dp(12));
        root.addView(note);
        VolumePreview3D preview=new VolumePreview3D();
        root.addView(preview,new LinearLayout.LayoutParams(-1,dp(350)));
        SeekBar slider=new SeekBar(this);
        slider.setMax(100);slider.setProgress(62);
        slider.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            public void onProgressChanged(SeekBar s,int p,boolean fromUser) {preview.setThreshold(p);}
            public void onStartTrackingTouch(SeekBar s) {}
            public void onStopTrackingTouch(SeekBar s) {}
        });
        root.addView(label("Pragu i vizualizimit demonstrues",12,TEAL));
        root.addView(slider);
        new AlertDialog.Builder(this).setTitle("KODI · Pamja 3D DEMO")
            .setView(root).setPositiveButton("Mbyll",null).show();
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
        VolumePreview3D(){super(KodiAnomalyStudioActivity.this);}
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
            float[] hotspot=projected(-0.13f,0.11f,-0.08f,cx,cy,scale);
            paint.setColor(0xffef8d59);
            paint.setAlpha(50+threshold*2);
            canvas.drawCircle(hotspot[0],hotspot[1],dp(12)+threshold/10f,paint);
            paint.setAlpha(255);paint.setColor(0xffadffed);paint.setTextSize(dp(12));
            canvas.drawText("VËLLIM DEMONSTRUES · Z JO I MATUR",dp(10),dp(23),paint);
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
