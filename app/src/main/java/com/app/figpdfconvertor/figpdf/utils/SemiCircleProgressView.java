package com.app.figpdfconvertor.figpdf.utils;

import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;

public class SemiCircleProgressView extends View {
    private Paint paintBackground, paintProgress, paintNeedle, paintCenterDot;
    private RectF rect;
    private int progress = 0;
    private int max = 100;

    public SemiCircleProgressView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init() {
        paintBackground = new Paint(Paint.ANTI_ALIAS_FLAG);
        paintBackground.setColor(Color.LTGRAY);
        paintBackground.setStrokeWidth(40);
        paintBackground.setStyle(Paint.Style.STROKE);

        paintProgress = new Paint(Paint.ANTI_ALIAS_FLAG);
        paintProgress.setColor(Color.parseColor("#2A8401")); // green
        paintProgress.setStrokeWidth(40);
        paintProgress.setStyle(Paint.Style.STROKE);

        paintNeedle = new Paint(Paint.ANTI_ALIAS_FLAG);
        paintNeedle.setColor(Color.BLACK);
        paintNeedle.setStrokeWidth(5);

        paintCenterDot = new Paint(Paint.ANTI_ALIAS_FLAG);
        paintCenterDot.setColor(Color.BLACK);

        rect = new RectF();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        int width = getWidth();
        int height = getHeight();

        rect.set(50, 50, width - 50, height * 2 - 50);

        // Draw background arc (180 degrees)
        canvas.drawArc(rect, 180, 180, false, paintBackground);

        // Draw progress arc
        float angle = (progress * 180f) / max;
        canvas.drawArc(rect, 180, angle, false, paintProgress);

        // Draw the needle
        float centerX = width / 2f;
        float centerY = height;

        float radius = (width - 100) / 2f; // considering padding (50 left + 50 right)
        float needleLength = radius * 1.1f;

        double needleAngleRad = Math.toRadians(180 + angle); // angle from left (180°) to right (360°)

        float needleX = (float) (centerX + needleLength * Math.cos(needleAngleRad));
        float needleY = (float) (centerY + needleLength * Math.sin(needleAngleRad));

        centerY = centerY - 12;

        canvas.drawLine(centerX, centerY, needleX, needleY, paintNeedle);

        // Draw center dot
        canvas.drawCircle(centerX, centerY, 12, paintCenterDot);
    }

    public void setProgress(int value) {
        this.progress = value;
        invalidate();
    }

    public void animateProgress(int target, long duration) {
        ValueAnimator animator = ValueAnimator.ofInt(0, target);
        animator.setDuration(duration);
        animator.addUpdateListener(animation -> {
            progress = (int) animation.getAnimatedValue();
            invalidate();
        });
        animator.start();
    }
}
