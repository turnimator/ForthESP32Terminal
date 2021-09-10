package com.turnimator.forthesp32terminal;

import static java.lang.Math.atan2;
import static java.lang.Math.cos;
import static java.lang.Math.sin;
import static java.lang.Math.sqrt;
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
    class Polar {
        public double a;
        public double r;

        public Polar(double deg, double distance){
            a = deg;
            r = distance;
        }
    }

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
    ArrayList<Polar> pt = new ArrayList<>();

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

        for (Polar p : pt) {
            double theta = toRadians(p.a);
            double xcm = p.r  * cos(theta);
            double ycm = p.r * sin(theta);
            float x = (float) (xcm* cmM);
            float y = (float) (ycm * cmM);
            if (x >radius || y > radius)
                continue;
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

    }

    /**
     * Plot a polar coordinate on the screen
     * @param deg Angle in degrees
     * @param distance Distance in cm
     */
    public void plotPolar(double deg, double distance) {
        pt.add(new  Polar(deg, distance));
        invalidate();
    }

    /**
     * Rotate all points angle degrees
     * @param deg Angle in degrees
     */
    public void rotate(double deg){
        for (Polar p:pt){
            p.a += deg;
            Log.d("rotate", ":" + deg);
            invalidate();
        }
        invalidate();
    }

    /**
     *
     * @param deg angle in degrees
     * @param distance distance in cm
     */
    public void translate(double deg, double distance){
        double angle = toRadians(deg);
        double dx = distance * cos(angle);
        double dy = distance * sin(angle);
        for (Polar p:pt){
            angle = toRadians(p.a);
            double xcm = p.r  * cos(angle);
            double ycm = p.r * sin(angle);
            xcm += dx;
            ycm += dy;
            String s = "Translating from (" + xcm + "," + ycm + ") to (";
            p.r = sqrt(xcm * xcm + ycm*ycm);
            p.a = atan2(ycm, xcm);
            s+= ""+xcm+","+ycm+")";
            Log.d("translate", s);
            invalidate();
        }
        invalidate();
    }
}
