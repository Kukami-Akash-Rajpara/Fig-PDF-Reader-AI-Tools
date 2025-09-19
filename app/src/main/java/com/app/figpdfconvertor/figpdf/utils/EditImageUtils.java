package com.app.figpdfconvertor.figpdf.utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.RectF;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.util.TypedValue;
import android.widget.ImageView;

import com.app.figpdfconvertor.figpdf.customwidget.DrawingView;


public class EditImageUtils {

    // get the image from the image_to_edit id
    public static Bitmap getBitmapFromImageView(ImageView imageView) {
        Drawable drawable = imageView.getDrawable();

        if (drawable instanceof BitmapDrawable) {
            // If it's already a BitmapDrawable → just return its bitmap
            return ((BitmapDrawable) drawable).getBitmap();
        } else {
            // Convert any other drawable type (Vector, Shape, etc.) to Bitmap
            int width = drawable.getIntrinsicWidth() > 0 ? drawable.getIntrinsicWidth() : imageView.getWidth();
            int height = drawable.getIntrinsicHeight() > 0 ? drawable.getIntrinsicHeight() : imageView.getHeight();

            Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
            Canvas canvas = new Canvas(bitmap);
            drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
            drawable.draw(canvas);
            return bitmap;
        }
    }

    // rotate the image 90 deg
    public static Bitmap rotateImage(Bitmap source, float angle) {
        Matrix matrix = new Matrix();
        matrix.postRotate(angle);
        return Bitmap.createBitmap(
                source, 0, 0,
                source.getWidth(),
                source.getHeight(),
                matrix,
                true);
    }

    // horizontal flip for the image
    public static Bitmap flipHorizontal(Bitmap source) {
        Matrix matrix = new Matrix();
        matrix.preScale(-1.0f, 1.0f);
        return Bitmap.createBitmap(
                source,
                0, 0,
                source.getWidth(),
                source.getHeight(),
                matrix,
                true
        );
    }

    // rotate the image with the help of a slider
    public static Bitmap rotate(Bitmap source, float angle) {
        Matrix matrix = new Matrix();
        matrix.postRotate(angle);

        // Calculate new bounds so the rotated image fits
        RectF bounds = new RectF(0, 0, source.getWidth(), source.getHeight());
        matrix.mapRect(bounds);

        int newWidth = Math.round(bounds.width());
        int newHeight = Math.round(bounds.height());

        Bitmap rotated = Bitmap.createBitmap(newWidth, newHeight, Bitmap.Config.ARGB_8888);

        Canvas canvas = new Canvas(rotated);
        canvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR); // ensure transparent bg
        canvas.translate(-bounds.left, -bounds.top); // shift to center
        canvas.drawBitmap(source, matrix, null);

        return rotated;
    }


    //===== write the text on the image  =====

    public static Bitmap drawTextOnBitmap(
            Context context,
            Bitmap baseBitmap,
            String text,
            float x,
            float y,
            int color,
            float textSizeSp
    ) {
        if (baseBitmap == null || text == null) return null;

        // Copy base bitmap
        Bitmap result = baseBitmap.copy(Bitmap.Config.ARGB_8888, true);
        Canvas canvas = new Canvas(result);

        Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        paint.setColor(color);
        paint.setTextSize(
                TypedValue.applyDimension(
                        TypedValue.COMPLEX_UNIT_SP,
                        textSizeSp,
                        context.getResources().getDisplayMetrics() // ✅ fixed
                )
        );
        paint.setShadowLayer(2f, 1f, 1f, Color.BLACK);

        canvas.drawText(text, x, y, paint);
        return result;
    }

    /* Crop to different aspect ratio  */
    private static Bitmap cropToAspectRatio(Bitmap source, int aspectX, int aspectY) {
        if (source == null) return null;

        int srcWidth = source.getWidth();
        int srcHeight = source.getHeight();

        float srcRatio = (float) srcWidth / srcHeight;
        float targetRatio = (float) aspectX / aspectY;

        int newWidth, newHeight;
        int offsetX, offsetY;

        if (srcRatio > targetRatio) {
            // Source is wider than target ratio → limit width
            newHeight = srcHeight;
            newWidth = (int) (srcHeight * targetRatio);
            offsetX = (srcWidth - newWidth) / 2;
            offsetY = 0;
        } else {
            // Source is taller than target ratio → limit height
            newWidth = srcWidth;
            newHeight = (int) (srcWidth / targetRatio);
            offsetX = 0;
            offsetY = (srcHeight - newHeight) / 2;
        }

        return Bitmap.createBitmap(source, offsetX, offsetY, newWidth, newHeight);
    }

    // =====================
    // Public methods to crop to common aspect ratios
    // =====================

    public static Bitmap crop1to1(Bitmap source) {
        return cropToAspectRatio(source, 1, 1);
    }

    public static Bitmap crop3to4(Bitmap source) {
        return cropToAspectRatio(source, 3, 4);
    }

    public static Bitmap crop3to2(Bitmap source) {
        return cropToAspectRatio(source, 3, 2);
    }

    public static Bitmap crop16to9(Bitmap source) {
        return cropToAspectRatio(source, 16, 9);
    }

    // ========  merge the canvas drawing with the image ========

   /* public static Bitmap mergeDrawingWithImage(Bitmap baseBitmap, DrawingView drawingView) {
        Bitmap result = baseBitmap.copy(Bitmap.Config.ARGB_8888, true);
        Canvas canvas = new Canvas(result);

        // export doodle at drawingView size
        Bitmap doodleBitmap = drawingView.exportDrawing(drawingView.getWidth(), drawingView.getHeight());

        // scale doodle to match base bitmap size
        Bitmap scaledDoodle = Bitmap.createScaledBitmap(
                doodleBitmap,
                baseBitmap.getWidth(),
                baseBitmap.getHeight(),
                true
        );

        canvas.drawBitmap(scaledDoodle, 0, 0, null);
        return result;
    }*/
   public static Bitmap mergeDrawingWithImage(Bitmap baseBitmap, DrawingView drawingView) {
       // Export drawing scaled to the image size
       Bitmap drawingBitmap = drawingView.exportDrawing(
               baseBitmap.getWidth(),
               baseBitmap.getHeight()
       );

       // Create a copy of the base image to draw onto
       Bitmap result = baseBitmap.copy(Bitmap.Config.ARGB_8888, true);
       Canvas canvas = new Canvas(result);

       // Overlay the drawing
       canvas.drawBitmap(drawingBitmap, 0f, 0f, null);

       return result;
   }
}
