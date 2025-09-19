package com.app.figpdfconvertor.figpdf.utils;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;

import androidx.annotation.Nullable;

public class SpotlightView extends View {
    private final Paint paint;
    private RectF targetRect = null;

    public SpotlightView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        setLayerType(LAYER_TYPE_HARDWARE, null);

        paint = new Paint();
        paint.setColor(0xAA000000); // semi-transparent black
    }

    public void setTarget(View target) {
        int[] targetLoc = new int[2];
        int[] parentLoc = new int[2];

        // get location of target
        target.getLocationOnScreen(targetLoc);

        // get location of this SpotlightView (overlay parent)
        this.getLocationOnScreen(parentLoc);

        // adjust to local coords of SpotlightView
        float left = targetLoc[0] - parentLoc[0];
        float top = targetLoc[1] - parentLoc[1];
        float right = left + target.getWidth();
        float bottom = top + target.getHeight();

        targetRect = new RectF(left, top, right, bottom);
        invalidate();
    }


    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        // Dim background
        canvas.drawColor(0xAA000000);

        if (targetRect != null) {
            // Create hole
            Paint clearPaint = new Paint();
            clearPaint.setAntiAlias(true);
            clearPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.CLEAR));
            canvas.drawRoundRect(targetRect, 30, 30, clearPaint);
        }
    }
}
