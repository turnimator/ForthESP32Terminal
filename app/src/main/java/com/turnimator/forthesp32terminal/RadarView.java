package com.turnimator.forthesp32terminal;

import static java.lang.Math.cos;
import static java.lang.Math.sin;
import static java.lang.Math.toDegrees;
import static java.lang.Math.toRadians;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.DashPathEffect;
import android.graphics.Paint;
import android.graphics.PointF;
import android.util.Log;
import android.widget.ImageView;

import java.util.ArrayList;

public class RadarView extends ImageView {
    Paint radarPaint;
    Paint crossHairPaint;
    Paint pointPaint;

    int w = 800;
    int h = 600;
    int wMid = w / 2;
    int hMid = h / 2;
    double radius = hMid;
    int radiusInCm = 40;
    double cmM = ((double)radius / (double)radiusInCm) ;
    ArrayList<PointF> pt = new ArrayList<PointF>();

    public static float pxFromDp(final Context context, final float dp) {
        return dp * context.getResources().getDisplayMetrics().density;
    }

    public RadarView(Context context) {
        super(context);
        this.setBackgroundColor(Color.BLACK);
        this.setMinimumWidth(w);
        this.setMinimumHeight(h);
        radarPaint = new Paint();
        radarPaint.setColor(Color.GREEN);

        pointPaint = new Paint();
        pointPaint.setColor(Color.RED);

        crossHairPaint = new Paint();
        crossHairPaint.setColor(Color.BLACK);
        crossHairPaint.setPathEffect(new DashPathEffect(new float[] { 4, 2, 4, 2 }, 0));
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        canvas.drawCircle(wMid, hMid, hMid, radarPaint);
        canvas.drawLine(wMid, 0, wMid, h, crossHairPaint);
        canvas.drawLine(0, hMid, w, hMid, crossHairPaint);

        for (PointF p : pt) {
            float x = (float) (p.x * cmM);
            float y = (float) (p.y * cmM);

            canvas.drawCircle(wMid + y, hMid - x, 10, pointPaint); // remember: We are plotting in the Widget, not the actual radar
            Log.d("RadarView", "plotting: " + x + "," + y);
        }
    }

    /**
     * Plot a point on the radar screen. Convert from cm to pixels
     * @param x in cm
     * @param y in cm
     */
    public void plotXY(float x, float y) {
        PointF p = new PointF();
        p.x = x;
        p.y = y;
        pt.add(p);
        invalidate();
    }

    /**
     * Plot a polar coordinate on the screen
     * @param deg Angle in degrees
     * @param distance Distance in cm
     */
    public void plotPolar(double deg, float distance) {
        double theta = toRadians(deg);
        float x = (float) (distance  * cos(theta));
        float y = (float) (distance * sin(theta));
        pt.add(new  PointF(x,y));
        invalidate();
    }

    /**
     * Rotate all points angle degrees
     * @param deg Angle in degrees
     */
    public void rotate(double deg){
        for (PointF p:pt){
            float angle = (float) toRadians(deg);
            String s = "Rotating from (" + p.x + "," + p.y + ") to (";
            p.x = (float) (p.x * cos(angle) - p.y * sin(angle));
            p.y = (float) (p.x * sin(angle) + p.y * cos(angle));
            s += "(" + p.x + "," + p.y + ")";
            Log.d("rotate", s);
            invalidate();
        }
        invalidate();
    }

    /**
     *
     * @param deg angle in degrees
     * @param distance distance in cm
     */
    public void translate(double deg, float distance){
        double angle = toRadians(deg);
        float x = (float) (distance  * cos(angle));
        float y = (float) (distance * sin(angle));
        for (PointF p:pt){
            String s = "Translating from (" + p.x + "," + p.y + ") to (";
            p.x += x;
            p.y += y;
            s += "(" + p.x + "," + p.y + ")";
            Log.d("translate", s);
            invalidate();
        }
        invalidate();
    }
}
