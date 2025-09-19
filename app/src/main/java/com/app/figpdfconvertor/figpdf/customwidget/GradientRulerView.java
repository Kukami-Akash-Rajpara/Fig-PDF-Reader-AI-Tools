package com.app.figpdfconvertor.figpdf.customwidget;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Shader;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

public class GradientRulerView extends View {
    private Paint linePaint;         // paint for normal tick lines
    private Paint centerLinePaint;   // paint for center orange line
    private Paint textPaint;         // paint for number text

    private int lineSpacing = 40;    // spacing between lines (px)
    private int lineHeight = 40;     // small tick height
    private int totalLines = 400;    // number of lines to draw in viewport

    private float offsetX = 0;       // scroll offset (changes as we drag)
    private float lastTouchX;        // last X touch position

    // Value range
    private float minValue = -180f;
    private float maxValue = 180f;
    private float stepPerLine = 1f;  // each line = 1 unit

    public interface OnValueChangeListener {
        void onValueChanged(float value);
    }

    private OnValueChangeListener listener;

    public void setOnValueChangeListener(OnValueChangeListener listener) {
        this.listener = listener;
    }

    public GradientRulerView(Context context) {
        super(context);
        init();
    }

    public GradientRulerView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init() {
        // Gradient lines
        linePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        linePaint.setStrokeWidth(3f);

        // Center orange line
        centerLinePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        centerLinePaint.setColor(Color.parseColor("#FF8800")); // orange
        centerLinePaint.setStrokeWidth(6f);

        // Number text
        textPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        textPaint.setColor(Color.parseColor("#FF8800")); // orange text
        textPaint.setTextSize(48f);
        textPaint.setTextAlign(Paint.Align.CENTER);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);

        // Gradient effect across tick lines
        LinearGradient gradient = new LinearGradient(
                0, 0, w, 0,
                new int[]{Color.TRANSPARENT, Color.BLACK, Color.TRANSPARENT},
                new float[]{0f, 0.5f, 1f},
                Shader.TileMode.CLAMP
        );
        linePaint.setShader(gradient);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        int centerY = getHeight() / 2;
        int centerX = getWidth() / 2;

        // Calculate how many ticks fit across screen
        int visibleTicks = getWidth() / lineSpacing + 4; // extra padding

        // Find the "center value" at the orange line
        float valueAtCenter = -offsetX / lineSpacing * stepPerLine;

        // Draw ticks left and right relative to center
        for (int i = -visibleTicks; i <= visibleTicks; i++) {
            float x = centerX + i * lineSpacing + (offsetX % lineSpacing);

            canvas.drawLine(
                    x,
                    centerY - lineHeight / 2f,
                    x,
                    centerY + lineHeight / 2f,
                    linePaint
            );
        }

        // Draw orange center line
        float orangeLineHeight = lineHeight * 1.5f;
        canvas.drawLine(
                centerX,
                centerY - orangeLineHeight / 2f,
                centerX,
                centerY + orangeLineHeight / 2f,
                centerLinePaint
        );

        // Wrap center value between min/max (infinite loop)
        float range = (maxValue - minValue);
        if (range > 0) {
            valueAtCenter = ((valueAtCenter - minValue) % range + range) % range + minValue;
        }

        // Draw center value in orange below the line
        canvas.drawText(
                String.format("%.1f", valueAtCenter),
                centerX,
                centerY + orangeLineHeight / 2f + 60,
                textPaint
        );

        if (listener != null) {
            listener.onValueChanged(valueAtCenter);
        }
    }


    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                lastTouchX = event.getX();
                return true;

            case MotionEvent.ACTION_MOVE:
                float dx = event.getX() - lastTouchX;
                offsetX += dx;            // move ruler
                lastTouchX = event.getX();
                invalidate();             // redraw
                return true;
        }
        return super.onTouchEvent(event);
    }
}
