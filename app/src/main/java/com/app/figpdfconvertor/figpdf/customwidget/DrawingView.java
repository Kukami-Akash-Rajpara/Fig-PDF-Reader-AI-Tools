package com.app.figpdfconvertor.figpdf.customwidget;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;

import java.util.ArrayList;
import java.util.List;

public class DrawingView extends View {

    private boolean eraserMode = false;

    private ImageView imageView;

    public void setImageView(ImageView imageView) {
        this.imageView = imageView;
    }

    private static class Stroke {
        Path path;
        Paint paint;

        Stroke(int color, float strokeWidth, boolean eraser) {
            path = new Path();
            paint = new Paint(Paint.ANTI_ALIAS_FLAG);
            paint.setStrokeWidth(strokeWidth);
            paint.setStyle(Paint.Style.STROKE);
            paint.setStrokeJoin(Paint.Join.ROUND);
            paint.setStrokeCap(Paint.Cap.ROUND);

            if (eraser) {
                paint.setColor(Color.TRANSPARENT);
                paint.setXfermode(new android.graphics.PorterDuffXfermode(android.graphics.PorterDuff.Mode.CLEAR));
            } else {
                paint.setColor(color);
            }
        }
    }

    private final List<Stroke> strokes = new ArrayList<>();
    private final List<Stroke> redoStrokes = new ArrayList<>();
    private Stroke currentStroke;

    private int currentColor = Color.RED;
    private float currentBrushSize = 8f;
    private float currentEraserSize = 20f;

    private boolean isDrawingEnabled = false;

    private float[] getImageViewBounds() {
        if (imageView == null || imageView.getDrawable() == null) {
            return new float[]{0, 0, getWidth(), getHeight()};
        }

        int ivWidth = imageView.getWidth();
        int ivHeight = imageView.getHeight();

        int drawableWidth = imageView.getDrawable().getIntrinsicWidth();
        int drawableHeight = imageView.getDrawable().getIntrinsicHeight();

        float imageAspect = (float) drawableWidth / drawableHeight;
        float viewAspect = (float) ivWidth / ivHeight;

        float left, top, right, bottom;

        if (imageAspect > viewAspect) {
            // Image scaled to fit width
            float scale = (float) ivWidth / drawableWidth;
            float scaledHeight = drawableHeight * scale;
            left = 0;
            top = (ivHeight - scaledHeight) / 2f;
            right = ivWidth;
            bottom = top + scaledHeight;
        } else {
            // Image scaled to fit height
            float scale = (float) ivHeight / drawableHeight;
            float scaledWidth = drawableWidth * scale;
            left = (ivWidth - scaledWidth) / 2f;
            top = 0;
            right = left + scaledWidth;
            bottom = ivHeight;
        }

        return new float[]{left, top, right, bottom};
    }

    public void setDrawingEnabled(boolean enabled) {
        isDrawingEnabled = enabled;
    }


    public DrawingView(Context context, AttributeSet attrs) {
        super(context, attrs);
        setLayerType(LAYER_TYPE_SOFTWARE, null); // important for proper export
    }

    // ========= Brush =========
    public void setBrushColor(int color) {
        currentColor = color;
        eraserMode = false;
    }

    public void setBrushSize(float size) {
        currentBrushSize = size;
    }

    public float getCurrentBrushSize() { return currentBrushSize; }

    // ========= Eraser =========
    public void setEraserSize(float size) {
        currentEraserSize = size;
        eraserMode = true;
    }

    public float getCurrentEraserSize() { return currentEraserSize; }

    public boolean isEraserMode() { return eraserMode; }

    // ========= Undo / Redo =========
    public void undo() {
        if (!strokes.isEmpty()) {
            redoStrokes.add(strokes.remove(strokes.size() - 1));
            invalidate();
        }
    }

    public void redo() {
        if (!redoStrokes.isEmpty()) {
            strokes.add(redoStrokes.remove(redoStrokes.size() - 1));
            invalidate();
        }
    }

    // ========= Clear =========
    public void clearDrawing() {
        strokes.clear();
        redoStrokes.clear();
        invalidate();
    }

    // ========= Eraser Mode =========
    public void setEraserMode(boolean enabled) {
        eraserMode = enabled;
    }

    // ========= Export doodles =========`
    public Bitmap exportDrawing(int targetWidth, int targetHeight) {
        // Create a transparent bitmap at the same size as the original image
        Bitmap bitmap = Bitmap.createBitmap(targetWidth, targetHeight, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);

        // Get bounds where the image is displayed inside ImageView
        float[] bounds = getImageViewBounds();
        float left = bounds[0];
        float top = bounds[1];
        float right = bounds[2];
        float bottom = bounds[3];

        float displayedWidth = right - left;
        float displayedHeight = bottom - top;

        // Calculate how the displayed image maps to the real bitmap
        float scaleX = (float) targetWidth / displayedWidth;
        float scaleY = (float) targetHeight / displayedHeight;

        // Translate so drawing aligns with the image area
        canvas.translate(-left, -top);

        // Scale strokes to match original bitmap resolution
        canvas.scale(scaleX, scaleY);

        // Draw strokes
        for (Stroke stroke : strokes) {
            canvas.drawPath(stroke.path, stroke.paint);
        }

        return bitmap;
    }





    // ========= Drawing =========
    @Override
    protected void onDraw(Canvas canvas) {
        if (imageView == null || imageView.getDrawable() == null) {
            super.onDraw(canvas);
            return;
        }

        // Clip canvas to visible image
        float[] bounds = getImageViewBounds();
        canvas.save();
        canvas.clipRect(bounds[0], bounds[1], bounds[2], bounds[3]);

        // Draw strokes
        for (Stroke stroke : strokes) {
            canvas.drawPath(stroke.path, stroke.paint);
        }

        canvas.restore();
    }

    public Bitmap getBitmap() {
        Bitmap b = Bitmap.createBitmap(getWidth(), getHeight(), Bitmap.Config.ARGB_8888);
        Canvas c = new Canvas(b);
        draw(c); // draw all paths onto this canvas
        return b;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (!isDrawingEnabled || imageView == null || imageView.getDrawable() == null) return false;

        // Get visible image bounds inside the ImageView
        float[] bounds = getImageViewBounds();
        float left = bounds[0];
        float top = bounds[1];
        float right = bounds[2];
        float bottom = bounds[3];

        float x = Math.max(left, Math.min(event.getX(), right));
        float y = Math.max(top, Math.min(event.getY(), bottom));

        float size = eraserMode ? currentEraserSize : currentBrushSize;

        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                currentStroke = new Stroke(currentColor, size, eraserMode);
                currentStroke.path.moveTo(x, y);
                strokes.add(currentStroke);
                redoStrokes.clear();
                invalidate();
                return true;

            case MotionEvent.ACTION_MOVE:
                if (currentStroke != null) {
                    currentStroke.path.lineTo(x, y);
                    invalidate();
                }
                return true;

            case MotionEvent.ACTION_UP:
                currentStroke = null;
                invalidate();
                return true;

            default:
                return false;
        }
    }
}