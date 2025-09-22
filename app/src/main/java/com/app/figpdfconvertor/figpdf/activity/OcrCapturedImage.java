package com.app.figpdfconvertor.figpdf.activity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;


import com.app.figpdfconvertor.figpdf.ads.AdManagerInter;
import com.app.figpdfconvertor.figpdf.ads.AdManagerRewarded;
import com.app.figpdfconvertor.figpdf.databinding.ActivityOcrCapturedImageBinding;
import com.app.figpdfconvertor.figpdf.preferences.AppHelper;
import com.app.figpdfconvertor.figpdf.utils.MyUtils;
import com.app.figpdfconvertor.figpdf.utils.OcrImageUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

public class OcrCapturedImage extends BaseActivity {

    private ActivityOcrCapturedImageBinding binding;
    private Uri imageUri;
    private File photoFile;

    // Zoom variables
    private float currentScale = 1f;
    private float baseScale = 1f; // initial fit-to-view scale
    private final float scaleStep = 0.2f;
    private final float maxScale = 3f;
    private final float minScale = 1f;

    // Matrix for zoom + pan
    private final Matrix matrix = new Matrix();
    private final Matrix savedMatrix = new Matrix();
    private final float[] matrixValues = new float[9];

    // Dragging state
    private float startX, startY;
    private static final int NONE = 0;
    private static final int DRAG = 1;
    private int mode = NONE;

    // Pinch zoom
    private ScaleGestureDetector scaleDetector;

    private final ActivityResultLauncher<Intent> cameraLauncher =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
                if (result.getResultCode() == RESULT_OK && photoFile != null) {
                    Bitmap bitmap = BitmapFactory.decodeFile(photoFile.getAbsolutePath());
                    binding.ocrCapturedImage.setImageBitmap(bitmap);

                    binding.ocrCapturedImage.post(this::fitImageToView);
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        MyUtils.fullScreenLightStatusBar(this);
        binding = ActivityOcrCapturedImageBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        ViewCompat.setOnApplyWindowInsetsListener(binding.main, (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        // Show initial captured image if coming from previous activity
        String imagePath = getIntent().getStringExtra("image_path");
        if (imagePath != null) {
            photoFile = new File(imagePath);
            imageUri = FileProvider.getUriForFile(
                    this,
                    getPackageName() + ".provider",
                    photoFile
            );
            Bitmap bitmap = BitmapFactory.decodeFile(photoFile.getAbsolutePath());
            binding.ocrCapturedImage.setImageBitmap(bitmap);

            binding.ocrCapturedImage.post(this::fitImageToView);
        }

        // Back button click
        binding.backButtonToMainActivity.setOnClickListener(v -> finish());

        // Retake button click → reopen camera
        binding.retakeButton.setOnClickListener(v -> openCamera());

        // Done button click → go to OCR result activity
        binding.doneButton.setOnClickListener(v -> {
            if (binding.ocrCapturedImage.getDrawable() != null) {
                // Create a bitmap of what’s currently visible in the ImageView
                Bitmap visibleBitmap = Bitmap.createBitmap(
                        binding.ocrCapturedImage.getWidth(),
                        binding.ocrCapturedImage.getHeight(),
                        Bitmap.Config.ARGB_8888
                );

                Canvas canvas = new Canvas(visibleBitmap);
                binding.ocrCapturedImage.draw(canvas);

                // Save this visible portion to a temporary file
                File croppedFile = new File(getCacheDir(), "visible_image.png");
                try (FileOutputStream out = new FileOutputStream(croppedFile)) {
                    visibleBitmap.compress(Bitmap.CompressFormat.PNG, 100, out);
                } catch (IOException e) {
                    e.printStackTrace();
                    Toast.makeText(this, "Failed to save cropped image", Toast.LENGTH_SHORT).show();
                    return;
                }

                // Send the path of this new file to next activity

                if (AppHelper.getShowRewardOcrResult()) {
                    AdManagerRewarded.showRewardedAd(this, () -> {
                        Intent intent = new Intent(OcrCapturedImage.this, ImageOcrResult.class);
                        intent.putExtra("image_path", croppedFile.getAbsolutePath());
                        startActivity(intent);
                        finish();
                    });
                } else {
                    Intent intent = new Intent(OcrCapturedImage.this, ImageOcrResult.class);
                    intent.putExtra("image_path", croppedFile.getAbsolutePath());
                    startActivity(intent);
                    finish();
                }

            } else {
                Toast.makeText(this, "No image found", Toast.LENGTH_SHORT).show();
            }
        });

        // Zoom-in button click
        binding.zoomInButton.setOnClickListener(v -> {
            if (currentScale < maxScale) {
                currentScale += scaleStep;
                applyZoom();
            }
        });

        // Zoom-out button click
        binding.zoomOutButton.setOnClickListener(v -> {
            if (currentScale > minScale) {
                currentScale -= scaleStep;
                applyZoom();
            }
        });

        // Scale detector for pinch zoom
        scaleDetector = new ScaleGestureDetector(this, new ScaleListener());

        // Enable dragging + pinch zoom
        binding.ocrCapturedImage.setOnTouchListener((v, event) -> {
            scaleDetector.onTouchEvent(event); // handle pinch

            ImageView view = (ImageView) v;
            switch (event.getAction() & MotionEvent.ACTION_MASK) {
                case MotionEvent.ACTION_DOWN:
                    savedMatrix.set(matrix);
                    startX = event.getX();
                    startY = event.getY();
                    mode = DRAG;
                    break;

                case MotionEvent.ACTION_MOVE:
                    if (mode == DRAG && currentScale > 1f && !scaleDetector.isInProgress()) {
                        matrix.set(savedMatrix);
                        float dx = event.getX() - startX;
                        float dy = event.getY() - startY;
                        matrix.postTranslate(dx, dy);
                        fixTranslation();
                    }
                    break;

                case MotionEvent.ACTION_UP:
                case MotionEvent.ACTION_POINTER_UP:
                    mode = NONE;
                    break;
            }

            view.setImageMatrix(matrix);
            return true;
        });

        // Open camera immediately if no image yet
        if (photoFile == null) openCamera();
    }

    /** Fit the image into ImageView initially */
    private void fitImageToView() {
        BitmapDrawable drawable = (BitmapDrawable) binding.ocrCapturedImage.getDrawable();
        if (drawable != null) {
            Bitmap bitmap = drawable.getBitmap();
            if (bitmap != null) {
                float scaleX = (float) binding.ocrCapturedImage.getWidth() / bitmap.getWidth();
                float scaleY = (float) binding.ocrCapturedImage.getHeight() / bitmap.getHeight();
                baseScale = Math.min(scaleX, scaleY);
            }
        }
        resetImageScale();
    }

    /** Apply zoom with panning */
    private void applyZoom() {
        BitmapDrawable drawable = (BitmapDrawable) binding.ocrCapturedImage.getDrawable();
        if (drawable == null) return;

        Bitmap bitmap = drawable.getBitmap();
        if (bitmap == null) return;

        float viewWidth = binding.ocrCapturedImage.getWidth();
        float viewHeight = binding.ocrCapturedImage.getHeight();
        float bitmapWidth = bitmap.getWidth();
        float bitmapHeight = bitmap.getHeight();

        matrix.reset();

        // Scale
        float scale = baseScale * currentScale;
        matrix.postScale(scale, scale);

        // Center
        float translateX = (viewWidth - bitmapWidth * scale) / 2f;
        float translateY = (viewHeight - bitmapHeight * scale) / 2f;
        matrix.postTranslate(translateX, translateY);

        binding.ocrCapturedImage.setScaleType(ImageView.ScaleType.MATRIX);
        binding.ocrCapturedImage.setImageMatrix(matrix);
    }

    /** Reset zoom to default */
    private void resetImageScale() {
        currentScale = 1f;
        applyZoom();
    }

    /** Keep image inside view bounds */
    private void fixTranslation() {
        matrix.getValues(matrixValues);
        float transX = matrixValues[Matrix.MTRANS_X];
        float transY = matrixValues[Matrix.MTRANS_Y];

        BitmapDrawable drawable = (BitmapDrawable) binding.ocrCapturedImage.getDrawable();
        if (drawable == null) return;

        float viewWidth = binding.ocrCapturedImage.getWidth();
        float viewHeight = binding.ocrCapturedImage.getHeight();

        float scale = baseScale * currentScale;
        float scaledWidth = drawable.getBitmap().getWidth() * scale;
        float scaledHeight = drawable.getBitmap().getHeight() * scale;

        float maxTransX = 0;
        float minTransX = viewWidth - scaledWidth;
        float maxTransY = 0;
        float minTransY = viewHeight - scaledHeight;

        float clampedX = Math.min(maxTransX, Math.max(transX, minTransX));
        float clampedY = Math.min(maxTransY, Math.max(transY, minTransY));

        float dx = clampedX - transX;
        float dy = clampedY - transY;

        matrix.postTranslate(dx, dy);
    }

    private void openCamera() {
        try {
            photoFile = OcrImageUtils.createImageFile(this);
            imageUri = FileProvider.getUriForFile(
                    this,
                    getPackageName() + ".provider",
                    photoFile
            );

            Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);
            cameraLauncher.launch(cameraIntent);

        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(this, "Could not create image file", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        binding = null;
    }

    /** Listener for pinch zoom */
    private class ScaleListener extends ScaleGestureDetector.SimpleOnScaleGestureListener {
        @Override
        public boolean onScale(ScaleGestureDetector detector) {
            float scaleFactor = detector.getScaleFactor();
            currentScale *= scaleFactor;

            // Clamp scale
            currentScale = Math.max(minScale, Math.min(currentScale, maxScale));

            applyZoom();
            return true;
        }
    }
}