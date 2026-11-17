package com.kodi.fusion.mapsdemo;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import org.osmdroid.config.Configuration;
import org.osmdroid.events.MapEventsReceiver;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.MapEventsOverlay;
import org.osmdroid.views.overlay.Marker;
import org.osmdroid.views.overlay.Polygon;
import org.osmdroid.views.overlay.Polyline;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Independent installation repair test.
 * Uses OpenStreetMap basemap, not Google Satellite: no API key or app SHA-1 required.
 * Observations are generated solely to verify user interactions;
 * no geophysical data are downloaded or analyzed here.
 */
public final class KodiOsmRepairActivity extends Activity {
    private static final int NAVY=0xff081522, AQUA=0xff00d7da;
    private static final int GOLD=0xffffc875;
    private MapView map;
    private TextView status, headline;
    private Button drawButton;
    private GeoPoint center=new GeoPoint(42.312015,21.119746);
    private int gridMeters=25;
    private boolean drawing=false;
    private boolean demoActive=false;
    private Polygon perimeter;
    private Polyline incomplete;
    private Marker centerMarker;
    private final List<GeoPoint> vertices=new ArrayList<>();
    private final List<GeoPoint> demoPoints=new ArrayList<>();
    private final List<Marker> pinOverlays=new ArrayList<>();
    private final List<Marker> vertexOverlays=new ArrayList<>();

    private int dp(int px){ return Math.round(px*getResources().getDisplayMetrics().density); }
    private TextView text(String value,int size,int color){
        TextView t=new TextView(this);
        t.setText(value); t.setTextSize(size); t.setTextColor(color);
        return t;
    }
    private Button button(String name){
        Button b=new Button(this);
        b.setText(name); b.setTextSize(11); b.setAllCaps(false);
        b.setMinHeight(0);
        return b;
    }
    private LinearLayout row(){
        LinearLayout r=new LinearLayout(this);
        r.setOrientation(LinearLayout.HORIZONTAL);
        return r;
    }
    private void add(LinearLayout r, View b){
        LinearLayout.LayoutParams p=new LinearLayout.LayoutParams(0,dp(53),1);
        p.setMargins(dp(1),dp(2),dp(1),dp(2));
        r.addView(b,p);
    }

    @Override public void onCreate(Bundle state){
        super.onCreate(state);
        Configuration.getInstance().setUserAgentValue(getPackageName());
        Configuration.getInstance().setTileDownloadThreads((short)2);
        LinearLayout root=new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setPadding(dp(5),dp(8),dp(5),dp(8));
        root.setBackgroundColor(NAVY);
        headline=text("KODI FUSION PRO · HARTA RIPARIM",17,Color.WHITE);
        headline.setPadding(dp(6),0,dp(3),dp(5));
        root.addView(headline);
        TextView info=text("OPENSTREETMAP · test pa Google API · JO skanim real",11,GOLD);
        info.setPadding(dp(6),0,0,dp(6));root.addView(info);
        LinearLayout tools=row();
        Button grid25=button("25×25");
        Button grid50=button("50×50");
        drawButton=button("Vizato zonë");
        Button reset=button("Pastro");
        add(tools,grid25);add(tools,grid50);add(tools,drawButton);add(tools,reset);
        root.addView(tools);

        map=new MapView(this);
        map.setTileSource(TileSourceFactory.MAPNIK);
        map.setMultiTouchControls(true);
        map.getController().setZoom(18.5);
        map.getController().setCenter(center);
        root.addView(map,new LinearLayout.LayoutParams(-1,0,1f));

        status=text("Harta duhet të ngarkohet me internet. Prek hartën për të zgjedhur qendrën.",
            12,0xffa4f6ea);
        status.setPadding(dp(7),dp(6),dp(7),dp(6));root.addView(status);

        LinearLayout actions=row();
        Button scan=button("DEMO · PIKA");
        Button report=button("Raport");
        Button view3D=button("3D demo");
        Button menu=button("Menu");
        add(actions,scan);add(actions,report);add(actions,view3D);add(actions,menu);
        root.addView(actions);

        TextView legal=text("© OpenStreetMap contributors · Kartografi publike. "
          +"Pikat janë shembuj artificialë, pa thellësi apo përqindje zbulimi.",
          10,0xffbfc6d0);
        legal.setPadding(dp(7),dp(5),dp(7),0);root.addView(legal);
        setContentView(root);

        map.getOverlays().add(new MapEventsOverlay(new MapEventsReceiver(){
            @Override public boolean singleTapConfirmedHelper(GeoPoint p){
                clearDemo();
                if(drawing){
                    vertices.add(p);
                    status.setText("Kulmet e zonës: "+vertices.size()
                        +" · Shtyp 'Mbyll zonën' pas 3+ pikash");
                } else {
                    vertices.clear();
                    center=p;
                    status.setText(String.format(Locale.US,
                        "Qendra WGS84: %.6f, %.6f",p.getLatitude(),p.getLongitude()));
                }
                renderArea();
                return true;
            }
            @Override public boolean longPressHelper(GeoPoint p){return false;}
        }));

        grid25.setOnClickListener(v->{gridMeters=25;drawing=false;vertices.clear();
            drawButton.setText("Vizato zonë");clearDemo();renderArea();});
        grid50.setOnClickListener(v->{gridMeters=50;drawing=false;vertices.clear();
            drawButton.setText("Vizato zonë");clearDemo();renderArea();});
        drawButton.setOnClickListener(v->{
            clearDemo();
            if(!drawing){
                drawing=true;vertices.clear();
                drawButton.setText("Mbyll zonën");
                status.setText("Prek hartën të paktën 3 herë për të krijuar poligonin");
            } else {
                drawing=false;
                drawButton.setText("Vizato zonë");
                if(vertices.size()<3){
                    vertices.clear();status.setText("Duhet poligon me 3+ pika");
                }else{
                    double lat=0,lon=0;
                    for(GeoPoint p:vertices){lat+=p.getLatitude();lon+=p.getLongitude();}
                    center=new GeoPoint(lat/vertices.size(),lon/vertices.size());
                    status.setText("Poligoni u ruajt · Qendra WGS84 e përditësuar");
                }
            }
            renderArea();
        });
        reset.setOnClickListener(v->{drawing=false;vertices.clear();clearDemo();
            drawButton.setText("Vizato zonë");renderArea();
            status.setText("Pikat e provës u pastruan. Arkivat e tjera nuk preken.");});
        scan.setOnClickListener(v->makeDemo());
        report.setOnClickListener(v->showReport());
        view3D.setOnClickListener(v->show3D());
        menu.setOnClickListener(v->startActivity(new Intent(this,KodiHomeActivity.class)));
        renderArea();
    }

    private List<GeoPoint> grid(){
        List<GeoPoint> result=new ArrayList<>();
        double dLat=gridMeters/(2.0*111320.0);
        double dLon=gridMeters/(2.0*111320.0*Math.max(0.05,
            Math.abs(Math.cos(Math.toRadians(center.getLatitude())))));
        result.add(new GeoPoint(center.getLatitude()-dLat,center.getLongitude()-dLon));
        result.add(new GeoPoint(center.getLatitude()-dLat,center.getLongitude()+dLon));
        result.add(new GeoPoint(center.getLatitude()+dLat,center.getLongitude()+dLon));
        result.add(new GeoPoint(center.getLatitude()+dLat,center.getLongitude()-dLon));
        return result;
    }
    private void renderArea(){
        if(map==null)return;
        if(perimeter!=null){map.getOverlays().remove(perimeter);perimeter=null;}
        if(incomplete!=null){map.getOverlays().remove(incomplete);incomplete=null;}
        if(centerMarker!=null){map.getOverlays().remove(centerMarker);centerMarker=null;}
        for(Marker m:vertexOverlays) map.getOverlays().remove(m);
        vertexOverlays.clear();
        List<GeoPoint> pts=vertices.isEmpty()?(drawing?vertices:grid()):vertices;
        if(pts.size()>=3){
            perimeter=new Polygon(map);
            perimeter.setPoints(pts);
            perimeter.getOutlinePaint().setColor(AQUA);
            perimeter.getOutlinePaint().setStrokeWidth(dp(3));
            perimeter.getFillPaint().setColor(0x3300e0df);
            map.getOverlays().add(perimeter);
        }else if(pts.size()==2){
            incomplete=new Polyline(map);
            incomplete.setPoints(pts);
            incomplete.getOutlinePaint().setColor(AQUA);
            incomplete.getOutlinePaint().setStrokeWidth(dp(3));
            map.getOverlays().add(incomplete);
        }
        if(!vertices.isEmpty()){
            int i=1;
            for(GeoPoint p:vertices){
                Marker m=new Marker(map);
                m.setPosition(p);m.setTitle("Kulmi "+i++);
                m.setAnchor(Marker.ANCHOR_CENTER,Marker.ANCHOR_BOTTOM);
                map.getOverlays().add(m);vertexOverlays.add(m);
            }
        }else if(!drawing){
            centerMarker=new Marker(map);
            centerMarker.setPosition(center);centerMarker.setTitle("Qendra WGS84");
            map.getOverlays().add(centerMarker);
        }
        map.invalidate();
    }
    private static boolean inside(GeoPoint p,List<GeoPoint> polygon){
        boolean yes=false;
        for(int i=0,j=polygon.size()-1;i<polygon.size();j=i++){
            GeoPoint a=polygon.get(i),b=polygon.get(j);
            if((a.getLatitude()>p.getLatitude())!=(b.getLatitude()>p.getLatitude())){
                double hit=(b.getLongitude()-a.getLongitude())*
                    (p.getLatitude()-a.getLatitude())/
                    (b.getLatitude()-a.getLatitude())+a.getLongitude();
                if(p.getLongitude()<hit)yes=!yes;
            }
        }
        return yes;
    }
    private void clearDemo(){
        for(Marker m:pinOverlays)map.getOverlays().remove(m);
        pinOverlays.clear(); demoPoints.clear(); demoActive=false;
        map.invalidate();
    }
    private BitmapDrawable pin(int number){
        Bitmap b=Bitmap.createBitmap(dp(42),dp(42),Bitmap.Config.ARGB_8888);
        Canvas c=new Canvas(b); Paint p=new Paint(Paint.ANTI_ALIAS_FLAG);
        p.setColor(Color.WHITE);c.drawCircle(dp(21),dp(21),dp(19),p);
        p.setColor(number%3==0?0xffef5759:(number%3==1?0xffff9a35:0xff02abc8));
        c.drawCircle(dp(21),dp(21),dp(16),p);
        p.setColor(Color.WHITE);p.setTypeface(android.graphics.Typeface.DEFAULT_BOLD);
        p.setTextSize(dp(16));p.setTextAlign(Paint.Align.CENTER);
        c.drawText(""+number,dp(21),dp(27),p);
        return new BitmapDrawable(getResources(),b);
    }
    private void makeDemo(){
        if(drawing){
            Toast.makeText(this,"Mbyll poligonin më parë",Toast.LENGTH_SHORT).show();
            return;
        }
        clearDemo();
        List<GeoPoint> area=vertices.size()>=3?vertices:grid();
        double minLat=90,maxLat=-90,minLon=180,maxLon=-180;
        for(GeoPoint p:area){
            minLat=Math.min(minLat,p.getLatitude());maxLat=Math.max(maxLat,p.getLatitude());
            minLon=Math.min(minLon,p.getLongitude());maxLon=Math.max(maxLon,p.getLongitude());
        }
        List<GeoPoint> sample=new ArrayList<>();
        for(int y=1;y<=12;y++)for(int x=1;x<=12;x++){
            GeoPoint p=new GeoPoint(minLat+(maxLat-minLat)*(y/13.0),
                minLon+(maxLon-minLon)*(x/13.0));
            if(inside(p,area))sample.add(p);
        }
        int n=Math.min(12,sample.size());
        for(int i=0;i<n;i++){
            GeoPoint p=sample.get((i*sample.size()+sample.size()/2)/n);
            demoPoints.add(p);
            final int id=i+1;
            Marker m=new Marker(map);
            m.setPosition(p);m.setTitle("DEMO #"+id);
            m.setSnippet("Pikë sintetike, jo rezultat i satelitit");
            m.setIcon(pin(id));
            m.setAnchor(Marker.ANCHOR_CENTER,Marker.ANCHOR_CENTER);
            m.setOnMarkerClickListener((marker,mapView)->{
                new AlertDialog.Builder(this)
                    .setTitle("Vëzhgimi #"+id+" · DEMO")
                    .setMessage(String.format(Locale.US,
                        "WGS84 %.6f, %.6f\nPikë e gjeneruar për provë. "+
                        "Nuk ka produkt satelitor, sinjal GPR apo thellësi të matur.",
                        p.getLatitude(),p.getLongitude()))
                    .setPositiveButton("OK",null).show();
                return true;
            });
            map.getOverlays().add(m);pinOverlays.add(m);
        }
        demoActive=n>0;
        status.setText("DEMO: "+n+" pika sintetike brenda zonës · JO zbulime");
        map.invalidate();
    }
    private String report(){
        StringBuilder r=new StringBuilder();
        r.append("KODI FUSION PRO · RAPORT PROVE\n");
        r.append(String.format(Locale.US,"Qendra WGS84 %.6f, %.6f\n",
            center.getLatitude(),center.getLongitude()));
        r.append("Zona: ").append(vertices.size()>=3?
            "poligon me "+vertices.size()+" kulme":gridMeters+"×"+gridMeters+" m");
        r.append("\nBurimi hartografik: OpenStreetMap (jo satelit).\n");
        r.append("Sentinel-1/2, NISAR, GPR, ERT: ASNJË MATJE.\n");
        r.append("Thellësia/besueshmëria e zbulimit: E PANJOHUR.\n");
        for(int i=0;i<demoPoints.size();i++){
            GeoPoint p=demoPoints.get(i);
            r.append(String.format(Locale.US,"DEMO #%02d: %.6f, %.6f\n",
                i+1,p.getLatitude(),p.getLongitude()));
        }
        return r.toString();
    }
    private void showReport(){
        new AlertDialog.Builder(this).setTitle("KODI · Raport prove")
            .setMessage(report())
            .setPositiveButton("Ndaj",(d,w)->{
                Intent send=new Intent(Intent.ACTION_SEND);
                send.setType("text/plain");
                send.putExtra(Intent.EXTRA_TEXT,report());
                startActivity(Intent.createChooser(send,"Ndaj raportin"));
            })
            .setNegativeButton("Mbyll",null).show();
    }
    private void show3D(){
        if(!demoActive){
            Toast.makeText(this,"Shtyp DEMO · PIKA më parë",Toast.LENGTH_SHORT).show();
            return;
        }
        LinearLayout body=new LinearLayout(this);
        body.setPadding(dp(8),dp(12),dp(8),dp(8));
        body.setOrientation(LinearLayout.VERTICAL);
        body.addView(text("Ilustrim 3D. Asnjë thellësi reale nuk është matur.",
            12,0xff294b55));
        body.addView(new VolumePreview(),new LinearLayout.LayoutParams(-1,dp(300)));
        new AlertDialog.Builder(this).setTitle("KODI · 3D DEMO")
            .setView(body).setPositiveButton("Mbyll",null).show();
    }
    private final class VolumePreview extends View {
        final Paint p=new Paint(Paint.ANTI_ALIAS_FLAG);
        float rotation=0.5f,startX=0;
        VolumePreview(){super(KodiOsmRepairActivity.this);}
        @Override protected void onDraw(Canvas canvas){
            canvas.drawColor(NAVY);
            float size=Math.min(getWidth(),getHeight())*0.27f;
            float cx=getWidth()/2f,cy=getHeight()/2f;
            float[][] v=new float[8][2];
            for(int i=0;i<8;i++){
                float x=(i&1)==0?-1:1,y=(i&2)==0?-1:1,z=(i&4)==0?-1:1;
                float rx=(float)(x*Math.cos(rotation)-z*Math.sin(rotation));
                float rz=(float)(x*Math.sin(rotation)+z*Math.cos(rotation));
                v[i][0]=cx+rx*size;v[i][1]=cy-y*size*.75f-rz*size*.27f;
            }
            p.setColor(0xffbdd2df);p.setStrokeWidth(dp(2));
            int[][] edges={{0,1},{0,2},{0,4},{1,3},{1,5},
              {2,3},{2,6},{3,7},{4,5},{4,6},{5,7},{6,7}};
            for(int[] e:edges)canvas.drawLine(v[e[0]][0],v[e[0]][1],
                v[e[1]][0],v[e[1]][1],p);
            p.setColor(0xffffa571);canvas.drawCircle(cx,cy,dp(15),p);
            p.setColor(0xffa3f2e9);p.setTextSize(dp(12));
            canvas.drawText("DEMO · Z JO I MATUR",dp(8),dp(18),p);
        }
        @Override public boolean onTouchEvent(MotionEvent e){
            if(e.getActionMasked()==MotionEvent.ACTION_DOWN){
                startX=e.getX();return true;
            }
            if(e.getActionMasked()==MotionEvent.ACTION_MOVE){
                rotation+=(e.getX()-startX)/160f;
                startX=e.getX();invalidate();return true;
            }
            return e.getActionMasked()==MotionEvent.ACTION_UP;
        }
    }
    @Override protected void onResume(){super.onResume();if(map!=null)map.onResume();}
    @Override protected void onPause(){if(map!=null)map.onPause();super.onPause();}
    @Override protected void onDestroy(){if(map!=null)map.onDetach();super.onDestroy();}
}
