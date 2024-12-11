package com.envmonitor.app;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;

public class GaugeView extends View {
    private Paint arcPaint;
    private Paint textPaint;
    private Paint valuePaint;
    private RectF arcRect;
    private float value = 0;
    private float minValue = 0;
    private float maxValue = 100;
    private String label = "";
    private String unit = "";

    private static final float START_ANGLE = 135;
    private static final float SWEEP_ANGLE = 270;
    private static final int ARC_STROKE_WIDTH = 30;

    public GaugeView(Context context) {
        super(context);
        init();
    }

    public GaugeView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init() {
        arcPaint = new Paint();
        arcPaint.setAntiAlias(true);
        arcPaint.setStyle(Paint.Style.STROKE);
        arcPaint.setStrokeWidth(ARC_STROKE_WIDTH);
        arcPaint.setColor(Color.BLUE);
        arcPaint.setStrokeCap(Paint.Cap.ROUND);

        textPaint = new Paint();
        textPaint.setAntiAlias(true);
        textPaint.setColor(Color.BLACK);
        textPaint.setTextSize(40);
        textPaint.setTextAlign(Paint.Align.CENTER);

        valuePaint = new Paint();
        valuePaint.setAntiAlias(true);
        valuePaint.setColor(Color.BLACK);
        valuePaint.setTextSize(60);
        valuePaint.setTextAlign(Paint.Align.CENTER);

        arcRect = new RectF();
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        
        // Calculate the rectangle for the arc, leaving padding for the stroke width
        int padding = ARC_STROKE_WIDTH + 10;
        arcRect.set(padding, padding, w - padding, h - padding);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        // Draw background arc
        arcPaint.setColor(Color.LTGRAY);
        canvas.drawArc(arcRect, START_ANGLE, SWEEP_ANGLE, false, arcPaint);

        // Draw value arc
        arcPaint.setColor(getColorForValue(value));
        float sweepAngle = (value - minValue) / (maxValue - minValue) * SWEEP_ANGLE;
        canvas.drawArc(arcRect, START_ANGLE, sweepAngle, false, arcPaint);

        // Draw label
        float centerX = arcRect.centerX();
        float centerY = arcRect.centerY();
        canvas.drawText(label, centerX, centerY - 40, textPaint);

        // Draw value with one decimal place
        String valueText = String.format("%.1f", value);
        Log.d("GaugeView", String.format("Drawing value %s for %s", valueText, label));
        canvas.drawText(valueText, centerX, centerY + 20, valuePaint);

        // Draw unit
        canvas.drawText(unit, centerX, centerY + 80, textPaint);
    }

    private int getColorForValue(float value) {
        float percentage = (value - minValue) / (maxValue - minValue);
        if (percentage < 0.33f) {
            return Color.rgb(0, 150, 0); // Green
        } else if (percentage < 0.66f) {
            return Color.rgb(255, 165, 0); // Orange
        } else {
            return Color.rgb(200, 0, 0); // Red
        }
    }

    public void setValue(float value) {
        if (this.value != value) {
            this.value = Math.max(minValue, Math.min(maxValue, value));
            Log.d("GaugeView", String.format("Setting value to %.1f for %s", this.value, label));
            postInvalidate(); // Use postInvalidate() instead of invalidate() for thread safety
        }
    }

    public void setRange(float min, float max) {
        this.minValue = min;
        this.maxValue = max;
        invalidate();
    }

    public void setLabel(String label) {
        this.label = label;
        invalidate();
    }

    public void setUnit(String unit) {
        this.unit = unit;
        invalidate();
    }
}
