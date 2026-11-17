package com.kodi.fusion.mapsdemo;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.graphics.Shader;
import android.view.View;

/**
 * Original vector symbol for the GeoTrace section:
 * radar, map grid, GPS pin and a manual highlighted point.
 */
public final class GeoTraceLogoView extends View {
    private final Paint p = new Paint(Paint.ANTI_ALIAS_FLAG);
    public GeoTraceLogoView(Context context) { super(context); }

    @Override protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        int save = canvas.save();
        canvas.scale(getWidth()/64f, getHeight()/64f);

        p.setStyle(Paint.Style.FILL);
        p.setShader(new LinearGradient(0, 0, 64, 64,
            new int[]{0xff093044, 0xff111529, 0xff39224f}, null, Shader.TileMode.CLAMP));
        canvas.drawRoundRect(new RectF(1,1,63,63), 14,14,p);
        p.setShader(null);

        p.setColor(0xff00dbdb);
        p.setStyle(Paint.Style.STROKE);
        p.setStrokeWidth(1.6f);
        canvas.drawRoundRect(new RectF(2,2,62,62), 13,13,p);

        p.setColor(0x5545ffff);
        p.setStrokeWidth(.6f);
        for (int v=12; v<60; v+=11) {
            canvas.drawLine(v,7,v,57,p);
            canvas.drawLine(7,v,57,v,p);
        }
        p.setColor(0xff00d9e9);
        p.setStrokeWidth(2.1f);
        canvas.drawCircle(32,32,20,p);
        canvas.drawCircle(32,32,12,p);

        Path ping = new Path();
        ping.moveTo(32,32);
        ping.lineTo(16,16);
        ping.arcTo(new RectF(12,12,52,52), 225,40);
        ping.close();
        p.setColor(0x6600ffff);
        p.setStyle(Paint.Style.FILL);
        canvas.drawPath(ping,p);

        p.setColor(0xffe9ff2d);
        p.setStyle(Paint.Style.STROKE);
        p.setStrokeWidth(2.8f);
        canvas.drawLine(14,45,26,39,p);
        canvas.drawLine(26,39,44,48,p);

        // Pin and manual annotation dot, not a detected mineral.
        p.setStyle(Paint.Style.FILL);
        p.setColor(0xff05f0f0);
        canvas.drawCircle(37,25,8,p);
        Path point = new Path();
        point.moveTo(31,29);
        point.lineTo(37,41);
        point.lineTo(43,29);
        point.close();
        canvas.drawPath(point,p);
        p.setColor(0xff18273b);
        canvas.drawCircle(37,25,3.6f,p);

        p.setColor(0xffff7b2a);
        canvas.drawCircle(26,39,3.4f,p);
        p.setColor(Color.WHITE);
        canvas.drawCircle(14,45,2f,p);

        canvas.restoreToCount(save);
    }
}
