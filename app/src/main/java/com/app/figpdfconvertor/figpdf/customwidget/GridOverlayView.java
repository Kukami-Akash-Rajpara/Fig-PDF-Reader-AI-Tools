package com.app.figpdfconvertor.figpdf.customwidget;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;

public class GridOverlayView extends View {

    private Paint paint;

    private static final int HANDLE_MOVE = 4;

    private float lastX, lastY;

    private RectF gridRect = new RectF();
    private float handleRadius = 30f;
    private int activeHandle = -1;

    public enum CropMode { ORIGINAL, FREE, FIXED }
    private CropMode cropMode = CropMode.ORIGINAL;
    private float aspectRatioW = 0f, aspectRatioH = 0f;

    private ImageView attachedImageView; // the ImageView we overlay

    public GridOverlayView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init() {
        paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        paint.setColor(Color.WHITE);
        paint.setStrokeWidth(4f);
        paint.setStyle(Paint.Style.STROKE);
    }

    /** Attach the ImageView this grid overlays */
    public void attachToImageView(ImageView imageView) {
        this.attachedImageView = imageView;
        invalidate();
    }

    /** Set crop mode */
    public void setCropMode(CropMode mode, float ratioW, float ratioH) {
        this.cropMode = mode;
        this.aspectRatioW = ratioW;
        this.aspectRatioH = ratioH;
        updateCropRect();
        invalidate();
    }

    private void updateCropRect() {
        if (getWidth() == 0 || getHeight() == 0 || attachedImageView == null) return;

        RectF imageBounds = getBitmapBounds(attachedImageView);

        if (cropMode == CropMode.ORIGINAL) {
            gridRect.set(imageBounds);
        } else if (cropMode == CropMode.FREE) {
            gridRect.set(
                    imageBounds.left + imageBounds.width() / 4f,
                    imageBounds.top + imageBounds.height() / 4f,
                    imageBounds.right - imageBounds.width() / 4f,
                    imageBounds.bottom - imageBounds.height() / 4f
            );
        } else if (cropMode == CropMode.FIXED && aspectRatioW > 0 && aspectRatioH > 0) {
            float targetWidth = imageBounds.width();
            float targetHeight = imageBounds.height();
            float ratio = aspectRatioW / aspectRatioH;

            if (targetWidth / targetHeight > ratio) {
                targetWidth = targetHeight * ratio;
            } else {
                targetHeight = targetWidth / ratio;
            }

            float left = imageBounds.left + (imageBounds.width() - targetWidth) / 2f;
            float top = imageBounds.top + (imageBounds.height() - targetHeight) / 2f;
            float right = left + targetWidth;
            float bottom = top + targetHeight;

            gridRect.set(left, top, right, bottom);
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        canvas.drawRect(gridRect, paint);

        float thirdW = gridRect.width() / 3;
        float thirdH = gridRect.height() / 3;
        for (int i = 1; i < 3; i++) {
            canvas.drawLine(gridRect.left + i * thirdW, gridRect.top,
                    gridRect.left + i * thirdW, gridRect.bottom, paint);
            canvas.drawLine(gridRect.left, gridRect.top + i * thirdH,
                    gridRect.right, gridRect.top + i * thirdH, paint);
        }

        if (cropMode == CropMode.FREE) drawHandles(canvas);
    }

    private void drawHandles(Canvas canvas) {
        canvas.drawCircle(gridRect.left, gridRect.top, handleRadius, paint);
        canvas.drawCircle(gridRect.right, gridRect.top, handleRadius, paint);
        canvas.drawCircle(gridRect.left, gridRect.bottom, handleRadius, paint);
        canvas.drawCircle(gridRect.right, gridRect.bottom, handleRadius, paint);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (cropMode != CropMode.FREE) return false;

        float x = event.getX();
        float y = event.getY();

        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                activeHandle = getTouchedHandle(x, y);
                lastX = x;
                lastY = y;
                return activeHandle != -1;
            case MotionEvent.ACTION_MOVE:
                if (activeHandle != -1) {
                    if (activeHandle == HANDLE_MOVE) moveGrid(x - lastX, y - lastY);
                    else resizeGrid(x, y);
                    lastX = x;
                    lastY = y;
                    invalidate();
                }
                return true;
            case MotionEvent.ACTION_UP:
                activeHandle = -1;
                return true;
        }
        return super.onTouchEvent(event);
    }

    private void moveGrid(float dx, float dy) {
        gridRect.offset(dx, dy);
        if (attachedImageView != null) {
            RectF bounds = getBitmapBounds(attachedImageView);
            if (gridRect.left < bounds.left) gridRect.offset(bounds.left - gridRect.left, 0);
            if (gridRect.top < bounds.top) gridRect.offset(0, bounds.top - gridRect.top);
            if (gridRect.right > bounds.right) gridRect.offset(bounds.right - gridRect.right, 0);
            if (gridRect.bottom > bounds.bottom) gridRect.offset(0, bounds.bottom - gridRect.bottom);
        }
    }

    private int getTouchedHandle(float x, float y) {
        if (isNear(x, y, gridRect.left, gridRect.top)) return 0;
        if (isNear(x, y, gridRect.right, gridRect.top)) return 1;
        if (isNear(x, y, gridRect.left, gridRect.bottom)) return 2;
        if (isNear(x, y, gridRect.right, gridRect.bottom)) return 3;
        return -1;
    }

    private boolean isNear(float x, float y, float tx, float ty) {
        return Math.hypot(x - tx, y - ty) < handleRadius * 1.5;
    }

    private void resizeGrid(float x, float y) {
        if (attachedImageView == null) return;

        RectF bounds = getBitmapBounds(attachedImageView);
        float minW = 150f;
        float minH = 150f;

        switch (activeHandle) {
            case 0:
                gridRect.left = clamp(x, bounds.left, gridRect.right - minW);
                gridRect.top = clamp(y, bounds.top, gridRect.bottom - minH);
                break;
            case 1:
                gridRect.right = clamp(x, gridRect.left + minW, bounds.right);
                gridRect.top = clamp(y, bounds.top, gridRect.bottom - minH);
                break;
            case 2:
                gridRect.left = clamp(x, bounds.left, gridRect.right - minW);
                gridRect.bottom = clamp(y, gridRect.top + minH, bounds.bottom);
                break;
            case 3:
                gridRect.right = clamp(x, gridRect.left + minW, bounds.right);
                gridRect.bottom = clamp(y, gridRect.top + minH, bounds.bottom);
                break;
        }
    }

    private float clamp(float val, float min, float max) {
        return Math.max(min, Math.min(val, max));
    }

    /** Return the current crop rectangle */
    public RectF getCropRect() {
        return new RectF(gridRect);
    }

    /** Get the visible bitmap bounds in the attached ImageView */
    private RectF getBitmapBounds(ImageView imageView) {
        Drawable drawable = imageView.getDrawable();
        if (drawable == null) return new RectF(0, 0, imageView.getWidth(), imageView.getHeight());

        float[] values = new float[9];
        imageView.getImageMatrix().getValues(values);
        float scaleX = values[Matrix.MSCALE_X];
        float scaleY = values[Matrix.MSCALE_Y];
        float transX = values[Matrix.MTRANS_X];
        float transY = values[Matrix.MTRANS_Y];

        int dw = drawable.getIntrinsicWidth();
        int dh = drawable.getIntrinsicHeight();

        return new RectF(
                transX,
                transY,
                transX + dw * scaleX,
                transY + dh * scaleY
        );
    }
}