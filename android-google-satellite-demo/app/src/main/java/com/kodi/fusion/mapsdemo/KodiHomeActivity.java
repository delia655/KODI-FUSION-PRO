package com.kodi.fusion.mapsdemo;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

/** Home separates GeoTrace from SURFER 6D; they are sibling modules. */
public final class KodiHomeActivity extends Activity {
    private int dp(int n) {
        return Math.round(n * getResources().getDisplayMetrics().density);
    }
    private GradientDrawable bg(int color,int radius) {
        GradientDrawable g=new GradientDrawable();
        g.setColor(color);
        g.setCornerRadius(dp(radius));
        return g;
    }
    private TextView text(String value,int sp,int color,boolean bold) {
        TextView t=new TextView(this);
        t.setText(value);
        t.setTextSize(sp);
        t.setTextColor(color);
        if(bold)t.setTypeface(Typeface.DEFAULT,Typeface.BOLD);
        return t;
    }

    @Override public void onCreate(Bundle state) {
        super.onCreate(state);
        getWindow().setStatusBarColor(0xff091523);
        getWindow().setNavigationBarColor(0xff091523);
        ScrollView scroll=new ScrollView(this);
        scroll.setBackgroundColor(0xff091523);
        LinearLayout root=new LinearLayout(this);
        root.setPadding(dp(18),dp(32),dp(18),dp(25));
        root.setOrientation(LinearLayout.VERTICAL);
        scroll.addView(root);
        setContentView(scroll);
        root.addView(text("KODI I THESARËVE",13,0xffffd269,true));
        TextView title=text("KODI FUSION PRO",28,Color.WHITE,true);
        title.setPadding(0,dp(8),0,dp(5));
        root.addView(title);
        TextView info=text("Zgjidh modulin e punës",15,0xff9eb3c5,false);
        info.setPadding(0,0,0,dp(24));root.addView(info);

        LinearLayout surfer=new LinearLayout(this);
        surfer.setOrientation(LinearLayout.HORIZONTAL);
        surfer.setGravity(Gravity.CENTER_VERTICAL);
        surfer.setPadding(dp(17),dp(17),dp(17),dp(17));
        surfer.setBackground(bg(0xff182940,20));
        TextView surferIcon=text("▦",48,0xffffc36e,true);
        surferIcon.setGravity(Gravity.CENTER);
        surfer.addView(surferIcon,new LinearLayout.LayoutParams(dp(83),dp(85)));
        LinearLayout surferText=new LinearLayout(this);
        surferText.setOrientation(LinearLayout.VERTICAL);
        surferText.addView(text("SURFER · ANALIZA 6D",18,Color.WHITE,true));
        TextView surferHint=text("Satelit · Heatmap · SAR · Shtresa · 3D",12,0xffc2d3e0,false);
        surferHint.setPadding(0,dp(8),0,0);
        surferText.addView(surferHint);
        surfer.addView(surferText,new LinearLayout.LayoutParams(0,-2,1));
        root.addView(surfer,new LinearLayout.LayoutParams(-1,dp(135)));
        surfer.setOnClickListener(v->startActivity(new Intent(this,SurferSixDActivity.class)));

        LinearLayout geotrace=new LinearLayout(this);
        geotrace.setOrientation(LinearLayout.HORIZONTAL);
        geotrace.setGravity(Gravity.CENTER_VERTICAL);
        geotrace.setPadding(dp(14),dp(14),dp(14),dp(14));
        geotrace.setBackground(bg(0xff073849,20));
        LinearLayout.LayoutParams geoLp=new LinearLayout.LayoutParams(-1,dp(139));
        geoLp.topMargin=dp(13);
        root.addView(geotrace,geoLp);
        GeoTraceLogoView icon=new GeoTraceLogoView(this);
        geotrace.addView(icon,new LinearLayout.LayoutParams(dp(83),dp(83)));
        LinearLayout geoText=new LinearLayout(this);
        geoText.setOrientation(LinearLayout.VERTICAL);
        geoText.setPadding(dp(11),0,0,0);
        geoText.addView(text("GeoTrace",22,0xff26fff2,true));
        TextView hint=text("Harta Google · Pika 1, 2… · Gjurmim GPS · Raporte",
            12,Color.WHITE,false);
        hint.setPadding(0,dp(8),0,0);geoText.addView(hint);
        geotrace.addView(geoText,new LinearLayout.LayoutParams(0,-2,1));
        geotrace.setOnClickListener(v->startActivity(new Intent(this,GeoTraceActivity.class)));

        LinearLayout anomaly=new LinearLayout(this);
        anomaly.setOrientation(LinearLayout.VERTICAL);
        anomaly.setPadding(dp(16),dp(14),dp(16),dp(14));
        anomaly.setBackground(bg(0xff12352c,20));
        LinearLayout.LayoutParams anomalyLp=new LinearLayout.LayoutParams(-1,-2);
        anomalyLp.topMargin=dp(13);
        root.addView(anomaly,anomalyLp);
        anomaly.addView(text("◎ KODI ANOMALY STUDIO",20,0xff86fbe6,true));
        TextView anomalyHint=text(
            "Harta satelitore · Zona / Grid · Pika të numëruara · Raport · 3D",
            12,Color.WHITE,false);
        anomalyHint.setPadding(0,dp(7),0,dp(8));
        anomaly.addView(anomalyHint);
        anomaly.addView(text(
            "Version provë: të gjitha anomalitë në DEMO janë sintetike, jo zbulime.",
            11,0xffffc27b,false));
        anomaly.setOnClickListener(v->startActivity(
            new Intent(this,KodiAnomalyStudioActivity.class)));

        TextView footer=text(
            "Dy module të ndara brenda një aplikacioni. "
            +"GeoTrace ruan vëzhgimet lokale; Google Satellite kërkon çelës API të konfiguruar. "
            +"Nuk bëhen parashikime nëntokësore pa matje.",
            12,0xffa4b7c9,false);
        footer.setPadding(dp(5),dp(22),dp(5),0);root.addView(footer);
    }
}
