package com.turnimator.forthesp32terminal;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.widget.ImageView;

public class RadarView extends ImageView {
    Paint radarPaint;
    Paint crossHairPaint;

    int w = 800;
    int h = 600;
    int wMid = w /2;
    int hMid = h / 2;

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

        crossHairPaint = new Paint();
        crossHairPaint.setColor(Color.BLACK);

    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        canvas.drawCircle(wMid, hMid, hMid, radarPaint);
        canvas.drawLine(wMid, 0,wMid, h, crossHairPaint);
        canvas.drawLine(0, hMid,w, hMid, crossHairPaint);
    }

    public void plotPolar(int angle, int distance){

    }
}
