package com.turnimator.forthesp32terminal;

import static java.lang.Math.atan2;
import static java.lang.Math.cos;
import static java.lang.Math.sin;
import static java.lang.Math.sqrt;
import static java.lang.Math.toRadians;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.DashPathEffect;
import android.graphics.Paint;
import android.graphics.RectF;
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
    double heading, bearing;

    Paint radarPaint;
    Paint crossHairPaint;
    Paint pointPaint;
    Paint compassPaint;

    int w = 800;
    int h = 600;
    int cX = w / 2;
    int cY = h / 2;
    double radius = cY;
    int radiusInCm = 40;
    double cmM = ((double)radius / (double)radiusInCm) ;

    RectF oval = new RectF();

    ArrayList<Polar> pt = new ArrayList<>();

    public static float pxFromDp(final Context context, final float dp) {
        return dp * context.getResources().getDisplayMetrics().density;
    }

    public void setHeading(double angle){
        for (Polar p: pt){
            p.a += angle;
        }
        heading = angle;
        invalidate();
    }

    public void move(double direction, double distance){

    }

    public RadarView(Context context) {
        super(context);
        oval.left = (float) (w - radius);
        oval.right = (float) (cX + radius);
        oval.top = (float) (cY + radius);
        oval.bottom = cY + (float) radius;

        compassPaint = new Paint();
        compassPaint.setColor(Color.BLUE);

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

    /**
     * Screen x from world X
     * @param a angle in degrees
     * @param r radius in cm
     * @return
     */
    float sX(double a, double r){
        double theta = toRadians(a);
        double x = cmM * r * sin(theta);
        return (float) (cX + x);
    }

    /**
     * Screen y from world y
     * @param a angle in degrees
     * @param r radius in cm
     * @return
     */
    float sY(double a, double r){
        double theta = toRadians(a);
        double y = cmM * r * cos(theta);
        return (float) (cY - y);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        canvas.drawCircle(cX, cY, cY, radarPaint);
        canvas.drawLine(cX, 0, cX, h, crossHairPaint);
        canvas.drawLine(0, cY, w, cY, crossHairPaint);
        canvas.drawCircle(sX(heading, 2), sY(heading, 2), 10, compassPaint);

        for (Polar p : pt) {
  //          canvas.drawCircle(cX + y, cY - x, 10, pointPaint); // remember: We are plotting in the Widget, not the actual radar
            canvas.drawCircle(sX(p.a, p.r), sY(p.a, p.r), 10, pointPaint); // remember: We are plotting in the Widget, not the actual radar
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
    void rotate(double deg){
        for (Polar p:pt){
            p.a += deg;
            Log.d("rotate", ":" + deg);
            invalidate();
        }
        invalidate();
    }

    /**
     * NB! All points are stored in degrees, cm
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
            p.r = sqrt(xcm*xcm + ycm*ycm);
            p.a += atan2(ycm, xcm);

            s+= ""+xcm+","+ycm+")";
            Log.d("translate", s);
            invalidate();
        }
        invalidate();
    }
}
