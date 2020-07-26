package de.heoegbr.diabeatit.ui.home;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.AttributeSet;

import com.github.mikephil.charting.charts.CombinedChart;

import java.util.ArrayList;
import java.util.List;

public class TargetZoneCombinedChart extends CombinedChart {

    protected Paint mYAxisSafeZonePaint;
    private List<TargetZone> mTargetZones;

    public TargetZoneCombinedChart(Context context) {
        super(context);
    }

    public TargetZoneCombinedChart(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public TargetZoneCombinedChart(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    @Override
    protected void init() {
        super.init();
        mYAxisSafeZonePaint = new Paint();
        mYAxisSafeZonePaint.setStyle(Paint.Style.FILL);
        // mGridBackgroundPaint.setColor(Color.rgb(240, 240, 240));
        mTargetZones = new ArrayList<>();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        if (mData != null) {
            for (TargetZone targetZone : mTargetZones) {
                // prepare coordinates
                float[] pts = new float[4];
                pts[1] = mAxisLeft.mAxisMinimum < targetZone.lowerLimit ? targetZone.lowerLimit : mAxisLeft.mAxisMinimum;
                pts[3] = mAxisLeft.mAxisMaximum > targetZone.upperLimit ? targetZone.upperLimit : mAxisLeft.mAxisMaximum;
                mLeftAxisTransformer.pointValuesToPixel(pts);

                // draw
                mYAxisSafeZonePaint.setColor(targetZone.color);
                canvas.drawRect(mViewPortHandler.contentLeft(), pts[1], mViewPortHandler.contentRight(),
                        pts[3], mYAxisSafeZonePaint);
            }
        }
        super.onDraw(canvas);
    }

    public void addTargetZone(TargetZone targetZone) {
        mTargetZones.add(targetZone);
    }

    public List<TargetZone> getTargetZones() {
        return mTargetZones;
    }

    public void clearTargetZones() {
        mTargetZones = new ArrayList<>();
    }

    public static class TargetZone {
        public final int color;
        public final float lowerLimit;
        public final float upperLimit;

        public TargetZone(int color, float lowerLimit, float upperLimit) {
            this.color = color;
            this.lowerLimit = lowerLimit;
            this.upperLimit = upperLimit;
        }
    }
}
