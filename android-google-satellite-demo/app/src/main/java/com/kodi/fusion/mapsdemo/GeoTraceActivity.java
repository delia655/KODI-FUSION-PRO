package com.kodi.fusion.mapsdemo;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.Typeface;
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

import com.google.android.gms.maps.model.LatLng;
import java.util.Locale;

/**
 * A standalone GeoTrace field-annotation module, independent of SURFER.
 * Numbered points are MANUAL chronological notes, not automatically ranked
 * satellite anomalies. Report fields must never suggest otherwise.
 */
public final class GeoTraceActivity extends Activity {
    private static final int DARK = 0xff071b2a;
    private static final int CYAN = 0xff00ddd9;
    private static final int SOFT = 0xffd9f7f2;
    private static final String[] TYPES = {
        "Vëzhgim manual", "Gjurmë sipërfaqësore", "Ndryshim bimësie",
        "Ndryshim relievi", "Strukturë e dukshme", "Pikë reference",
        "Për kontroll në terren"
    };
    private GeoTraceOverlay geo;
    private double latitude=42.312015, longitude=21.119746;
    private int side=25;

    @Override public void onCreate(Bundle state) {
        super.onCreate(state);
        getWindow().setStatusBarColor(DARK);
        getWindow().setNavigationBarColor(DARK);
        getWindow().getDecorView().setSystemUiVisibility(0);
        refresh();
    }

    @Override protected void onResume() {
        super.onResume();
        refresh();
    }

    private void refresh() {
        SharedPreferences p = getSharedPreferences("surfer6d", MODE_PRIVATE);
        latitude = p.getFloat("lat", (float)latitude);
        longitude = p.getFloat("lon", (float)longitude);
        side = p.getInt("side",25)==50?50:25;
        geo = new GeoTraceOverlay(this,true);
        draw();
    }

    private int dp(int x) {
        return Math.round(x * getResources().getDisplayMetrics().density);
    }
    private GradientDrawable bg(int c,int radius) {
        GradientDrawable d = new GradientDrawable();
        d.setColor(c);d.setCornerRadius(dp(radius));return d;
    }
    private TextView text(String body,int sp,int color, boolean heavy) {
        TextView v = new TextView(this);
        v.setText(body);v.setTextSize(sp);v.setTextColor(color);
        if (heavy) v.setTypeface(Typeface.DEFAULT,Typeface.BOLD);
        return v;
    }
    private Button btn(String label,int color,int ink) {
        Button v = new Button(this);
        v.setAllCaps(false);
        v.setText(label);v.setTextColor(ink);v.setTextSize(13);
        v.setMinHeight(0);v.setMinimumHeight(0);
        v.setBackground(bg(color,13));return v;
    }
    private LinearLayout.LayoutParams margin(int height,int top) {
        LinearLayout.LayoutParams lp=new LinearLayout.LayoutParams(-1,dp(height));
        lp.topMargin=dp(top);return lp;
    }

    private void draw() {
        ScrollView scroll=new ScrollView(this);
        scroll.setFillViewport(true);scroll.setBackgroundColor(DARK);
        LinearLayout root=new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setPadding(dp(16),dp(18),dp(16),dp(25));
        scroll.addView(root);setContentView(scroll);
        LinearLayout header=new LinearLayout(this);
        header.setGravity(Gravity.CENTER_VERTICAL);
        TextView back=text("‹",32,CYAN,false);
        back.setGravity(Gravity.CENTER);
        back.setContentDescription("Kthehu në KODI");
        back.setOnClickListener(v->finish());
        header.addView(back,new LinearLayout.LayoutParams(dp(43),dp(47)));
        GeoTraceLogoView icon=new GeoTraceLogoView(this);
        header.addView(icon,new LinearLayout.LayoutParams(dp(57),dp(57)));
        LinearLayout captions=new LinearLayout(this);
        captions.setOrientation(LinearLayout.VERTICAL);
        captions.setPadding(dp(10),0,0,0);
        captions.addView(text("GeoTrace",23,Color.WHITE,true));
        captions.addView(text("Harta • Pika • Raporti",12,SOFT,false));
        header.addView(captions);
        root.addView(header);

        TextView info=text("MODUL I VEÇANTË NGA SURFER\n"
             +"Zgjidh pikën në Google Satellite, shëno vëzhgimin dhe ruaj raportin lokal.",
            13,0xffb9e6e9,false);
        info.setPadding(dp(13),dp(16),dp(13),dp(16));
        info.setBackground(bg(0xff112d42,15));
        LinearLayout.LayoutParams ilp=new LinearLayout.LayoutParams(-1,-2);
        ilp.topMargin=dp(16);root.addView(info,ilp);

        TextView area=text(String.format(Locale.US,
            "Zona WGS84: %.6f, %.6f  ·  %d × %d m",
            latitude,longitude,side,side),13,SOFT,false);
        area.setPadding(dp(4),dp(16),dp(4),0);root.addView(area);

        Button mapButton=btn("◎ HARTA GOOGLE · SHËNO PIKA",CYAN,DARK);
        root.addView(mapButton,margin(55,16));
        mapButton.setOnClickListener(v->{
            Intent map=new Intent(this,GoogleSatelliteActivity.class);
            map.putExtra(GoogleSatelliteActivity.EXTRA_LAT,latitude);
            map.putExtra(GoogleSatelliteActivity.EXTRA_LON,longitude);
            map.putExtra(GoogleSatelliteActivity.EXTRA_GRID,side);
            map.putExtra(GoogleSatelliteActivity.EXTRA_GEOTRACE,true);
            startActivity(map);
        });

        Button manual=btn("+ SHTO PIKË ME KOORDINATA",0xff223e52,Color.WHITE);
        root.addView(manual,margin(52,9));
        manual.setOnClickListener(v->addPointDialog());

        LinearLayout counts=new LinearLayout(this);
        counts.setGravity(Gravity.CENTER_VERTICAL);
        TextView total=text("PIKAT E MIA · "+geo.count(),17,Color.WHITE,true);
        counts.addView(total,new LinearLayout.LayoutParams(0,-2,1));
        Button share=btn("⇧ EKSPORT",0xff163948,SOFT);
        counts.addView(share,new LinearLayout.LayoutParams(dp(117),dp(42)));
        share.setOnClickListener(v->{
            if(geo.count()>0) geo.shareGeoJson();
            else alert("Ende nuk ke pika për eksport.");
        });
        LinearLayout.LayoutParams clp=new LinearLayout.LayoutParams(-1,-2);
        clp.topMargin=dp(24);clp.bottomMargin=dp(10);
        root.addView(counts,clp);

        if(geo.count()==0) {
            TextView empty=text("Ende nuk ka pika.\n\n"
               +"Prek hartën Google për pikën 1, pikën 2, e kështu me radhë. "
               +"Mund të shtosh pika edhe me koordinata pa çelës Google.",
                14,0xffc2d8df,false);
            empty.setGravity(Gravity.CENTER);
            empty.setPadding(dp(18),dp(22),dp(18),dp(22));
            empty.setBackground(bg(0xff122c40,16));
            root.addView(empty,new LinearLayout.LayoutParams(-1,dp(160)));
        }
        for(int i=0;i<geo.count();i++) {
            final int index=i;
            LatLng p=geo.getPoint(i);
            LinearLayout card=new LinearLayout(this);
            card.setPadding(dp(13),dp(13),dp(13),dp(13));
            card.setOrientation(LinearLayout.VERTICAL);
            card.setBackground(bg(0xff102f43,16));
            LinearLayout.LayoutParams row=new LinearLayout.LayoutParams(-1,-2);
            row.bottomMargin=dp(8);root.addView(card,row);
            card.addView(text(String.format(Locale.US,
                "Pika %d  ·  %s",i+1,geo.getType(i)),16,CYAN,true));
            TextView coordinates=text(String.format(Locale.US,"%.6f, %.6f",
                p.latitude,p.longitude),13,SOFT,false);
            coordinates.setPadding(0,dp(6),0,dp(4));card.addView(coordinates);
            String note=geo.getNote(i);
            TextView summary=text(note.isEmpty()
                ? "Pa shënim · Jo i verifikuar në terren"
                : "Shënimi: "+note,13,0xffe1e8f0,false);
            summary.setMaxLines(2);
            card.addView(summary);
            TextView open=text("Hap raportin / Ndrysho shënimin  ›",12,0xffffd16c,true);
            open.setPadding(0,dp(9),0,0);card.addView(open);
            card.setOnClickListener(v->showReport(index));
        }

        TextView footer=text(
            "Numrat janë rendi i pikave të shënuara manualisht, jo intensitet "
             +"satelitor apo zbulime. Nuk llogarisim përqindje besueshmërie, "
             +"metale ose thellësi pa matje të verifikuara.",
             12,0xffa9bcc7,false);
        footer.setPadding(dp(4),dp(12),dp(4),dp(8));root.addView(footer);
    }

    private void addPointDialog() {
        LinearLayout form=new LinearLayout(this);
        form.setOrientation(LinearLayout.VERTICAL);
        form.setPadding(dp(20),dp(8),dp(20),dp(6));
        EditText lat=new EditText(this);
        lat.setHint("Latitude");
        lat.setSingleLine();
        lat.setInputType(InputType.TYPE_CLASS_NUMBER |
            InputType.TYPE_NUMBER_FLAG_SIGNED|InputType.TYPE_NUMBER_FLAG_DECIMAL);
        lat.setText(String.format(Locale.US,"%.6f",latitude));form.addView(lat);
        EditText lon=new EditText(this);
        lon.setHint("Longitude");
        lon.setSingleLine();
        lon.setInputType(InputType.TYPE_CLASS_NUMBER |
            InputType.TYPE_NUMBER_FLAG_SIGNED|InputType.TYPE_NUMBER_FLAG_DECIMAL);
        lon.setText(String.format(Locale.US,"%.6f",longitude));form.addView(lon);
        new AlertDialog.Builder(this).setTitle("Shto pikë WGS84")
            .setMessage("Pika është shënim manual, jo rezultat automatik i satelitit.")
            .setView(form)
            .setNegativeButton("Anulo",null)
            .setPositiveButton("Shto", (d,which)->{
                try{
                    double a=Double.parseDouble(lat.getText().toString().trim().replace(',','.'));
                    double b=Double.parseDouble(lon.getText().toString().trim().replace(',','.'));
                    if(!Double.isFinite(a)||!Double.isFinite(b)||Math.abs(a)>85||Math.abs(b)>180){
                        alert("Koordinata jashtë kufijve.");return;
                    }
                    if(!geo.addManualPoint(new LatLng(a,b))) {
                        alert("Nuk mund të shtohen më shumë se 200 pika.");return;
                    }
                    draw();
                    showReport(geo.count()-1);
                }catch(NumberFormatException ex){alert("Shkruaj koordinata numerike të vlefshme.");}
            }).show();
    }

    private void showReport(int i) {
        if(i<0||i>=geo.count())return;
        LatLng p=geo.getPoint(i);
        String details=String.format(Locale.US,
            "Koordinata WGS84: %.6f, %.6f\n\n"
            +"Burimi: pikë e shënuar nga përdoruesi\n"
            +"Kategoria: %s\n"
            +"Shpjegimi: %s\n\n"
            +"Shtresat satelitore: nuk ka analizë të bashkëngjitur\n"
            +"Besueshmëria: nuk është llogaritur\n"
            +"Thellësia: nuk është matur\n"
            +"Statusi: JO I VERIFIKUAR NË TERREN\n\n"
            +"Plan kontrolli: dokumento fotografinë, mjedisin, "
            +"matjet e terrenit dhe burimin para çdo interpretimi.",
            p.latitude,p.longitude,geo.getType(i),
            geo.getNote(i).isEmpty()?"Nuk ka shënim.":geo.getNote(i));
        new AlertDialog.Builder(this)
            .setTitle("GeoTrace · Raporti i pikës "+(i+1))
            .setMessage(details)
            .setNegativeButton("Mbyll",null)
            .setNeutralButton("Shiko në hartë",(dialog,which)->{
                Intent map=new Intent(this,GoogleSatelliteActivity.class);
                map.putExtra(GoogleSatelliteActivity.EXTRA_LAT,p.latitude);
                map.putExtra(GoogleSatelliteActivity.EXTRA_LON,p.longitude);
                map.putExtra(GoogleSatelliteActivity.EXTRA_GRID,side);
                map.putExtra(GoogleSatelliteActivity.EXTRA_GEOTRACE,true);
                startActivity(map);
            })
            .setPositiveButton("Ndrysho",(dialog,which)->editNote(i))
            .show();
    }

    private void editNote(int i) {
        LinearLayout form=new LinearLayout(this);
        form.setOrientation(LinearLayout.VERTICAL);
        form.setPadding(dp(22),dp(8),dp(22),0);
        TextView caption=text("Lloji i vëzhgimit (i zgjedhur nga përdoruesi):",
            14,Color.DKGRAY,false);
        form.addView(caption);
        android.widget.Spinner spinner=new android.widget.Spinner(this);
        android.widget.ArrayAdapter<String> adapter=new android.widget.ArrayAdapter<>(
            this,android.R.layout.simple_spinner_dropdown_item,TYPES);
        spinner.setAdapter(adapter);
        for(int j=0;j<TYPES.length;j++)if(TYPES[j].equals(geo.getType(i))){
            spinner.setSelection(j);break;
        }
        form.addView(spinner);
        EditText note=new EditText(this);
        note.setHint("Shpjegimi, përse u shënua pika, kontrolli në terren...");
        note.setText(geo.getNote(i));
        note.setMinLines(3);
        note.setMaxLines(6);
        form.addView(note);
        new AlertDialog.Builder(this).setTitle("Pika "+(i+1)+" · Shënimet")
            .setView(form).setNegativeButton("Anulo",null)
            .setPositiveButton("Ruaj",(dialog,which)->{
                geo.setPointDescription(i,
                    spinner.getSelectedItem().toString(),
                    note.getText().toString().trim());
                draw();
            }).show();
    }

    private void alert(String message) {
        new AlertDialog.Builder(this).setMessage(message)
            .setPositiveButton("OK",null).show();
    }
}
