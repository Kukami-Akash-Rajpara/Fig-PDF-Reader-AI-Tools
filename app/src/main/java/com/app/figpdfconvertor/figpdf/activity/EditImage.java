package com.app.figpdfconvertor.figpdf.activity;

import static android.view.View.GONE;
import static android.view.View.VISIBLE;

import android.content.Context;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.app.figpdfconvertor.figpdf.R;
import com.app.figpdfconvertor.figpdf.adapter.ColorAdapter;
import com.app.figpdfconvertor.figpdf.adapter.FiltersAdapter;
import com.app.figpdfconvertor.figpdf.customwidget.DraggableTextView;
import com.app.figpdfconvertor.figpdf.customwidget.GridOverlayView;
import com.app.figpdfconvertor.figpdf.databinding.ActivityEditImageBinding;
import com.app.figpdfconvertor.figpdf.utils.EditImageUtils;
import com.app.figpdfconvertor.figpdf.utils.ImageFilterUtils;
import com.app.figpdfconvertor.figpdf.utils.ImageUtils;
import com.app.figpdfconvertor.figpdf.utils.MyUtils;
import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.CustomTarget;
import com.bumptech.glide.request.transition.Transition;
import com.google.android.material.textfield.TextInputEditText;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


public class EditImage extends BaseActivity {

    private ActivityEditImageBinding binding; //  ViewBinding field

    private DraggableTextView currentTextView;

    private enum SliderMode {DRAW, FONT}

    private SliderMode currentMode = SliderMode.DRAW; // default

    private Bitmap currentBitmap;
    private Bitmap copyBitmap;
    private Bitmap originalBitmap;
    private String getFileName(String path) {
        if (path == null) return "";
        int lastSlash = path.lastIndexOf("/");
        if (lastSlash != -1 && lastSlash < path.length() - 1) {
            return path.substring(lastSlash + 1);
        } else {
            return path; // fallback if no slash found
        }
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        MyUtils.fullScreenLightStatusBar(this);

        binding = ActivityEditImageBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        //bind the drawing to imagetoedit

        setupBackPressed();

        String imagePath = getIntent().getStringExtra("IMAGE_PATH");
        binding.imageNameText.setText(getFileName(imagePath));
        if (imagePath != null) {
            Glide.with(this)
                    .asBitmap() // ensures you get a Bitmap
                    .load(imagePath)
                    .into(new CustomTarget<Bitmap>() {
                        @Override
                        public void onResourceReady(@NonNull Bitmap resource, @Nullable Transition<? super Bitmap> transition) {
                            originalBitmap = resource.copy(resource.getConfig(), true); // permanent backup
                            currentBitmap = resource;
                            copyBitmap = resource.copy(resource.getConfig(), true);
                            binding.imageToEdit.setImageBitmap(copyBitmap);

                            initFiltersRecycler();
                        }

                        @Override
                        public void onLoadCleared(@Nullable Drawable placeholder) {
                            // optional: clear or set a placeholder
                        }

                        @Override
                        public void onLoadFailed(@Nullable Drawable errorDrawable) {
                            super.onLoadFailed(errorDrawable);
                            Toast.makeText(EditImage.this, "Failed to load image", Toast.LENGTH_SHORT).show();
                        }
                    });
        } else {
            Toast.makeText(this, "No image path found", Toast.LENGTH_SHORT).show();
        }
        binding.drawingView.setImageView(binding.imageToEdit);

        //  =======  drawing matches the imageview and drawingview  =======
        // After setting the bitmap in the ImageView
        binding.imageToEdit.post(() -> {
            int imageWidth = binding.imageToEdit.getWidth();
            int imageHeight = binding.imageToEdit.getHeight();

            binding.drawingView.getLayoutParams().width = imageWidth;
            binding.drawingView.getLayoutParams().height = imageHeight;
            binding.drawingView.requestLayout();
        });
        boolean showOnlyDone = getIntent().getBooleanExtra("SHOW_ONLY_DONE", false);
        if (showOnlyDone) {
            // Hide all tool buttons
            binding.toolContainer.setVisibility(View.GONE); // LinearLayout with crop, doodle, filter, text

            // Show only Done button
            binding.onlyCrop.setVisibility(View.VISIBLE);

        } else {
            binding.toolContainer.setVisibility(View.VISIBLE);
            binding.onlyCrop.setVisibility(GONE);
        }
        // ======== Tool Buttons ========
        LinearLayout[] toolButtons = {
                binding.cropButton,
                binding.doodleButton,
                binding.filterButton,
                binding.textButton
        };

        int[] toolIndices = {
                EditScreen.Constants.TOOL_CROP,
                EditScreen.Constants.TOOL_DOODLE,
                EditScreen.Constants.TOOL_FILTER,
                EditScreen.Constants.TOOL_TEXT
        };

        // Restore selection from Activity A
        int selectedTool = getIntent().getIntExtra(EditScreen.Constants.EXTRA_SELECTED_TOOL, -1);
        if (selectedTool >= 0 && selectedTool < toolButtons.length) {
            toolButtons[selectedTool].setSelected(true);
            switch (toolIndices[selectedTool]) {
                case EditScreen.Constants.TOOL_CROP:
                    if (binding.llCrop.getVisibility() == VISIBLE) {
                        binding.llCrop.setVisibility(GONE);
                    } else {
                        cropUI();
                    }
                    binding.drawingView.setDrawingEnabled(false);
                    break;
                case EditScreen.Constants.TOOL_DOODLE:
                    if (binding.llEdit.getVisibility() == VISIBLE) {
                        binding.llEdit.setVisibility(GONE);
                    } else {
                        doodleUI();
                    }
                    binding.drawingView.setDrawingEnabled(true);
                    break;
                case EditScreen.Constants.TOOL_FILTER:
                    if (binding.llFilter.getVisibility() == VISIBLE) {
                        binding.llFilter.setVisibility(GONE);
                    } else {
                        filterUI();
                    }

                    binding.drawingView.setDrawingEnabled(false);

                    break;
                case EditScreen.Constants.TOOL_TEXT:
                    if (binding.llText.getVisibility() == VISIBLE) {
                        binding.llText.setVisibility(GONE);
                    } else {
                        textUI();
                        customText();
                    }

                    binding.drawingView.setDrawingEnabled(false);
                    break;
            }
        }

        // Keep track of the current tool
        final int[] currentTool = {selectedTool};

        // Tool button click handling
        for (int i = 0; i < toolButtons.length; i++) {
            final int index = i;
            toolButtons[i].setOnClickListener(v -> {
                // Reset all buttons
                for (LinearLayout button : toolButtons) button.setSelected(false);

                // Select clicked button
                toolButtons[index].setSelected(true);

                // Save the clicked tool
                currentTool[0] = toolIndices[index];

                // Update UI based on selected tool
                switch (currentTool[0]) {
                    case EditScreen.Constants.TOOL_CROP:
                        cropUI();
                        break;
                    case EditScreen.Constants.TOOL_DOODLE:
                        doodleUI();
                        break;
                    case EditScreen.Constants.TOOL_FILTER:
                        filterUI();
                        break;
                    case EditScreen.Constants.TOOL_TEXT:
                        textUI();
                        break;
                }
            });
        }

        // ======== List of Crop options ========
        List<View> cropOptions = new ArrayList<>();
        cropOptions.add(binding.originalCrop);
        cropOptions.add(binding.freeCrop);
        cropOptions.add(binding.oneOneCrop);
        cropOptions.add(binding.threeFourCrop);
        cropOptions.add(binding.threeTwoCrop);
        cropOptions.add(binding.sixteenNineCrop);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // ===== Get the bitmap copy here and make a duplicate to edit =====

        // 1. Get the current image from the ImageView

        // 2. Make a copy (so edits don’t overwrite the original reference)
        if (currentBitmap != null) {
            copyBitmap = currentBitmap.copy(currentBitmap.getConfig(), true);
            binding.imageToEdit.setImageBitmap(copyBitmap);
        } else {
            //Toast.makeText(this, "No valid image loaded", Toast.LENGTH_SHORT).show();
        }
        // 3. Replace the ImageView content with the copy
        binding.imageToEdit.setImageBitmap(copyBitmap);

        binding.gridOverlay.attachToImageView(binding.imageToEdit);

        // ========= Back Button to Image Screen Activity =========

        binding.backButtonToMainActivity.setOnClickListener(v -> {
            showExitDialog(this);
        });


        // --- Flip ---
        binding.flipButton.setOnClickListener(v -> {
            if (copyBitmap != null) {
                // Flip the current bitmap
                Bitmap flipped = EditImageUtils.flipHorizontal(copyBitmap);

                // Merge current doodles with flipped bitmap and update references
                updateBitmapWithDrawing(flipped);
            }

            // Restore crop UI state
            binding.sliderSelector.setVisibility(VISIBLE);
            binding.cropButtonEditor.setSelected(false);
            binding.cropSelectionWrapper.setVisibility(GONE);
        });


        // --- Rotate ---
        binding.rotateButton.setOnClickListener(v -> {
            if (copyBitmap != null) {
                // Rotate the current bitmap
                Bitmap rotated = EditImageUtils.rotateImage(copyBitmap, 90);

                // Merge current doodles with rotated bitmap and update references
                updateBitmapWithDrawing(rotated);
            }

            // Restore crop UI state
            binding.sliderSelector.setVisibility(VISIBLE);
            binding.cropButtonEditor.setSelected(false);
            binding.cropSelectionWrapper.setVisibility(GONE);
        });

// --- Crop ---
        binding.cropButtonEditor.setOnClickListener(v -> {
            boolean newState = !v.isSelected();
            v.setSelected(newState);

            if (newState) {
                // crop selected → hide view
                binding.sliderSelector.setVisibility(GONE);
                binding.cropSelectionWrapper.setVisibility(VISIBLE);
            } else {
                // crop deselected → show view
                binding.sliderSelector.setVisibility(VISIBLE);
                binding.cropSelectionWrapper.setVisibility(GONE);
            }
        });

        //set the on click listeners for the crop options
        View.OnClickListener cropClickListener = v -> {
            for (View option : cropOptions) {
                option.setSelected(option == v); // only clicked one is selected
            }
        };

        //  Assign click listener to each selection
        for (View option : cropOptions) {
            option.setOnClickListener(cropClickListener);
        }

// setup the grid overlay on top of the visible image
        binding.originalCrop.setOnClickListener(v -> {
            binding.gridOverlay.setCropMode(GridOverlayView.CropMode.ORIGINAL, 0, 0);

            if (currentBitmap != null) {
                // Reset back to the permanent original
                copyBitmap = currentBitmap.copy(currentBitmap.getConfig(), true);
                binding.imageToEdit.setImageBitmap(copyBitmap);
            }
        });

        binding.freeCrop.setOnClickListener(v ->
                binding.gridOverlay.setCropMode(GridOverlayView.CropMode.FREE, 0, 0));


        binding.oneOneCrop.setOnClickListener(v ->
                applyCropWithDoodles(GridOverlayView.CropMode.FIXED, 1, 1, EditImageUtils::crop1to1)
        );

        binding.threeFourCrop.setOnClickListener(v ->
                applyCropWithDoodles(GridOverlayView.CropMode.FIXED, 3, 4, EditImageUtils::crop3to4)
        );

        binding.threeTwoCrop.setOnClickListener(v ->
                applyCropWithDoodles(GridOverlayView.CropMode.FIXED, 3, 2, EditImageUtils::crop3to2)
        );

        binding.sixteenNineCrop.setOnClickListener(v ->
                applyCropWithDoodles(GridOverlayView.CropMode.FIXED, 16, 9, EditImageUtils::crop16to9)
        );

// rotate the image when the slider moves
        binding.sliderSelector.setOnValueChangeListener(value -> {
            try {
                if (currentBitmap != null) {
                    Bitmap rotated = EditImageUtils.rotate(currentBitmap, value);

                    // Ensure ARGB_8888 copy before merging
                    rotated = rotated.copy(Bitmap.Config.ARGB_8888, true);

                    Bitmap merged = EditImageUtils.mergeDrawingWithImage(rotated, binding.drawingView);
                    binding.drawingView.clearDrawing();
                    binding.imageToEdit.setImageBitmap(merged);

                    copyBitmap = merged.copy(Bitmap.Config.ARGB_8888, true); // keep transparent safe
                }
            } catch (Exception e) {
                e.printStackTrace();
                Toast.makeText(this, "Error rotating image: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });


        // ======== setup the active track ========
        binding.utilitySlider.setCustomThumbDrawable(R.drawable.custom_thumb);
        binding.textSlider.setCustomThumbDrawable(R.drawable.custom_thumb);

        // ======== setup the listner for the slider to adjust the stroke of the color ========
        binding.utilitySlider.addOnChangeListener((slider, value, fromUser) -> {
            if (currentMode == SliderMode.DRAW) {
                if (binding.drawingView.isEraserMode()) {
                    binding.drawingView.setEraserSize(value);
                } else {
                    binding.drawingView.setBrushSize(value);
                }
            } /*else if (currentMode == SliderMode.FONT) {
                if (currentTextView != null) {
                    currentTextView.setFontSize(value); // 🔥 applies font size
                }
            }*/

            if (fromUser) {
                slider.performHapticFeedback(android.view.HapticFeedbackConstants.VIRTUAL_KEY);
            }
        });

        binding.textSlider.addOnChangeListener((slider, value, fromUser) -> {

            if (currentTextView != null) {
                currentTextView.setFontSize(value); // 🔥 applies font size
            }

            if (fromUser) {
                slider.performHapticFeedback(android.view.HapticFeedbackConstants.VIRTUAL_KEY);
            }
        });

        // ======== setup the slider to lister to change the font size  ========

        // ======== setup the eraser ========
        binding.eraserButton.setOnClickListener(v -> {
            binding.drawingView.setEraserMode(true);
            binding.utilitySlider.setValue(binding.drawingView.getCurrentEraserSize());
            binding.textSlider.setValue(binding.drawingView.getCurrentEraserSize());
            binding.colorPickerRecycler.setVisibility(View.INVISIBLE);
        });

        // ========  setup undo and redo for the paint strokes  ========

        binding.undoButton.setOnClickListener(v -> {
            binding.drawingView.undo();
        });

        binding.redoButton.setOnClickListener(v -> {
            binding.drawingView.redo();
        });


        // ======== setup the pen click listener ========
        binding.penButton.setOnClickListener(v -> {
            binding.drawingView.setEraserMode(false);
            binding.utilitySlider.setValue(binding.drawingView.getCurrentBrushSize());
            binding.textSlider.setValue(binding.drawingView.getCurrentBrushSize());
            binding.colorPickerRecycler.setVisibility(VISIBLE);
        });

        //======== setting up the color for the stroke selection  ========
        RecyclerView recyclerView = binding.colorPickerRecycler;
        RecyclerView recyclerView1 = binding.colorPickerRecycler1;

        int[] colors = {
                Color.RED,
                Color.GREEN,
                Color.BLUE,
                Color.YELLOW,
                Color.CYAN,
                Color.MAGENTA,
                Color.BLACK,
                Color.WHITE,
                Color.parseColor("#FF5733"),
                Color.parseColor("#9B59B6"),
                Color.parseColor("#1ABC9C"),
                Color.parseColor("#2ECC71"),
                // ... add up to 30–40 colors
        };

        ColorAdapter adapter = new ColorAdapter(colors, selectedColor -> {
            if (currentMode == SliderMode.DRAW) {
                // 🎨 apply color to brush
                binding.drawingView.setBrushColor(selectedColor);

                // reset stroke size to default
                binding.utilitySlider.setValue(0f);
                binding.textSlider.setValue(0f);
            } else if (currentMode == SliderMode.FONT) {
                // 🔤 apply color to text
                if (currentTextView != null) {
                    currentTextView.setFontColor(selectedColor);
                }
            }

            // always update slider thumb tint for user feedback
            binding.utilitySlider.setThumbTintList(ColorStateList.valueOf(selectedColor));
            binding.textSlider.setThumbTintList(ColorStateList.valueOf(selectedColor));
        });

        recyclerView.setLayoutManager(
                new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        );
        recyclerView.setAdapter(adapter);

        recyclerView1.setLayoutManager(
                new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        );
        recyclerView1.setAdapter(adapter);

        //======== create a editing text on the TextButton click ========
        // When text button is clicked
        binding.textButton.setOnClickListener(v -> {
            customText();
            textUI();
        });

        //======== bind a click event for the edit text done button to confirm the edited text =======
        binding.confirmTextButton.setOnClickListener(v -> {
            if (currentTextView != null) {
                currentTextView.disableEditing(); //  just hides keyboard + removes bg
            }
        });

       /* binding.doneButton.setOnClickListener(v -> {
            binding.imageToEdit.post(() -> {
                // Merge doodles onto copyBitmap
                Bitmap finalBitmap = EditImageUtils.mergeDrawingWithImage(copyBitmap, binding.drawingView);

                // Save to gallery
                ContentValues values = new ContentValues();
                values.put(MediaStore.Images.Media.DISPLAY_NAME, "edited_" + System.currentTimeMillis() + ".png");
                values.put(MediaStore.Images.Media.MIME_TYPE, "image/png");
                values.put(MediaStore.Images.Media.RELATIVE_PATH, Environment.DIRECTORY_PICTURES + "/figconverter");

                Uri uri = getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);

                if (uri != null) {
                    try (OutputStream out = getContentResolver().openOutputStream(uri)) {
                        finalBitmap.compress(Bitmap.CompressFormat.PNG, 100, out);
                        Toast.makeText(this, "Saved to Pictures/figconverter", Toast.LENGTH_LONG).show();

                        // 🔥 Update both references
                        currentBitmap = finalBitmap;
                        copyBitmap = finalBitmap.copy(finalBitmap.getConfig(), true);

                        binding.imageToEdit.setImageBitmap(copyBitmap);
                        binding.drawingView.clearDrawing();
                    } catch (Exception e) {
                        e.printStackTrace();
                        Toast.makeText(this, "Error saving: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(this, "Failed to create MediaStore entry", Toast.LENGTH_SHORT).show();
                }
            });
        });*/
        binding.doneButton.setOnClickListener(v -> {
            binding.progressOverlay.setVisibility(VISIBLE);

            binding.canvasContainer.post(() -> {
                new Thread(() -> {
                    Bitmap finalBitmap = Bitmap.createBitmap(
                            binding.canvasContainer.getWidth(),
                            binding.canvasContainer.getHeight(),
                            Bitmap.Config.ARGB_8888
                    );
                    Canvas canvas = new Canvas(finalBitmap);
                    binding.canvasContainer.draw(canvas); // ✅ capture ALL views

                    Uri tempUri = ImageUtils.saveBitmapToCache(EditImage.this, finalBitmap);

                    runOnUiThread(() -> {
                        binding.progressOverlay.setVisibility(GONE);
                        if (tempUri != null) {
                            Intent resultIntent = new Intent();
                            resultIntent.putExtra("EDITED_IMAGE_PATH", tempUri.toString());
                            resultIntent.putExtra("POSITION", getIntent().getIntExtra("POSITION", -1));
                            setResult(RESULT_OK, resultIntent);
                            finish();
                        } else {
                            Toast.makeText(EditImage.this, "Failed to save temp image", Toast.LENGTH_SHORT).show();
                        }
                    });
                }).start();
            });
        });


        binding.onlyCrop.setOnClickListener(v -> {
            binding.progressOverlay.setVisibility(VISIBLE); // show progress

            binding.imageToEdit.post(() -> {
                new Thread(() -> { // use a background thread
                    Bitmap finalBitmap = EditImageUtils.mergeDrawingWithImage(copyBitmap, binding.drawingView);

                    Uri tempUri = com.app.figpdfconvertor.figpdf.utils.ImageUtils.saveBitmapToCache(EditImage.this, finalBitmap);

                    runOnUiThread(() -> { // update UI back on main thread
                        binding.progressOverlay.setVisibility(GONE); // hide progress

                        if (tempUri != null) {
                            Intent resultIntent = new Intent();
                            resultIntent.putExtra("EDITED_IMAGE_PATH", tempUri.toString());
                            resultIntent.putExtra("POSITION", getIntent().getIntExtra("POSITION", -1));
                            setResult(RESULT_OK, resultIntent);
                            finish();
                        } else {
                            Toast.makeText(EditImage.this, "Failed to save temp image", Toast.LENGTH_SHORT).show();
                        }
                    });
                }).start();
            });
        });


        // ======= cancel confirm and add more text  =======
        // Close → remove text completely
        binding.icClose.setOnClickListener(v -> {
            if (currentTextView != null) {
                binding.canvasContainer.removeView(currentTextView);
                currentTextView = null;
                binding.textInterationContainer.setVisibility(GONE);
                binding.llText.setVisibility(GONE);
                binding.confirmTextButton.setVisibility(GONE);
                binding.resetButton.setVisibility(VISIBLE);
            }
            binding.bottomBar.setVisibility(VISIBLE);
        });

// Confirm → lock text (no drag, no edit)
        binding.resetButton.setOnClickListener(v -> {
            resetToOriginal();
        });

        binding.icConfirmText.setOnClickListener(v -> {
            if (currentTextView != null) {
                currentTextView.lock();

                String text = currentTextView.getText();
                int color = currentTextView.getFontColor();
                float textSize = currentTextView.getFontSize();

                float viewX = currentTextView.getX();
                float viewY = currentTextView.getY();

                Bitmap baseBitmap = EditImageUtils.getBitmapFromImageView(binding.imageToEdit);
                if (baseBitmap == null) return;


                float scaleX = (float) baseBitmap.getWidth() / binding.imageToEdit.getWidth();
                float scaleY = (float) baseBitmap.getHeight() / binding.imageToEdit.getHeight();

// pick one scale (usually Y for text height)
                float scale = Math.min(scaleX, scaleY);

// 🔥 scale the font size
                float scaledTextSize = textSize * scale;

                float bitmapX = viewX * scaleX;
                float bitmapY = (viewY + currentTextView.getHeight()) * scaleY;

                Bitmap merged = EditImageUtils.mergeDrawingWithImage(baseBitmap, binding.drawingView);

                Bitmap finalBitmap = EditImageUtils.drawTextOnBitmap(
                        this,
                        merged,
                        text,
                        bitmapX,
                        bitmapY,
                        color,
                        scaledTextSize
                );

                // ✅ update both references
                currentBitmap = finalBitmap; // permanent
                copyBitmap = finalBitmap.copy(finalBitmap.getConfig(), true);

                binding.imageToEdit.setImageBitmap(copyBitmap);

                ((ViewGroup) currentTextView.getParent()).removeView(currentTextView);
                currentTextView = null;

                binding.textInterationContainer.setVisibility(GONE);
                binding.llText.setVisibility(GONE);
                binding.bottomBar.setVisibility(VISIBLE);
                binding.confirmTextButton.setVisibility(GONE);
                binding.resetButton.setVisibility(VISIBLE);
            }
        });

//        binding.icConfirmText.setOnClickListener(v -> {
//            if (currentTextView != null) {
//                currentTextView.lock(); //  freeze completely
//                binding.textInterationContainer.setVisibility(View.GONE);
//                currentTextView = null;
//            }
//            binding.bottomBar.setVisibility(View.VISIBLE);
//            binding.confirmTextButton.setVisibility(View.GONE);
//            binding.resetButton.setVisibility(View.VISIBLE);
//        });

// Add → create new text
        binding.addMoreText.setOnClickListener(v -> {
            customText(); // adds new text & enters edit mode
        });


        initFiltersRecycler();
        binding.editImageName.setOnClickListener(v -> {
            if (imagePath != null) {
                showRenameDialog(EditImage.this, imagePath);
            } else {
                Toast.makeText(EditImage.this, "No image loaded", Toast.LENGTH_SHORT).show();
            }
        });
    }
    private void clearAllTexts() {
        for (int i = binding.canvasContainer.getChildCount() - 1; i >= 0; i--) {
            View child = binding.canvasContainer.getChildAt(i);
            if (child instanceof DraggableTextView) {
                binding.canvasContainer.removeViewAt(i);
            }
        }
    }
    private void resetToOriginal() {
        if (originalBitmap != null) {
            // Restore original safely
            currentBitmap = originalBitmap.copy(originalBitmap.getConfig(), true);
            copyBitmap = currentBitmap.copy(currentBitmap.getConfig(), true);

            // Reset ImageView
            binding.imageToEdit.setImageBitmap(copyBitmap);

            // Clear drawings
            binding.drawingView.clearDrawing();

            // Clear only text overlays (keep image + drawing + grid)
            clearAllTexts();

            // Reset grid overlay
            binding.gridOverlay.attachToImageView(binding.imageToEdit);
            binding.gridOverlay.setVisibility(View.GONE);

            // Reset UI
            binding.llCrop.setVisibility(View.GONE);
            binding.llEdit.setVisibility(View.GONE);
            binding.llFilter.setVisibility(View.GONE);
            binding.llText.setVisibility(View.GONE);
            binding.filterRecycler.setVisibility(View.GONE);
            binding.textInterationContainer.setVisibility(View.GONE);
            binding.bottomBar.setVisibility(View.VISIBLE);

            // Reset states
            currentTextView = null;
            currentMode = SliderMode.DRAW;

            Toast.makeText(this, "Image reset to original", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, "No original image found", Toast.LENGTH_SHORT).show();
        }
    }



    private FiltersAdapter filterAdapter;
    private List<String> filterNames = Arrays.asList(
            "Original", "Docs", "Image", "Super", "Enhance", "Enhance2", "B&W", "B&W2", "Gray", "Invert"
    );

    private void initFiltersRecycler() {
        if (copyBitmap == null) return;

        filterAdapter = new FiltersAdapter(
                this,
                copyBitmap,
                filterNames,
                new kotlin.jvm.functions.Function1<String, kotlin.Unit>() {
                    @Override
                    public kotlin.Unit invoke(String filterName) {
                        // Apply filter in background thread
                        new Thread(() -> {
                            Bitmap filteredBitmap = ImageFilterUtils.INSTANCE.applyFilter(copyBitmap, filterName);
                            runOnUiThread(() -> {
                                copyBitmap = filteredBitmap.copy(filteredBitmap.getConfig(), true);
                                binding.imageToEdit.setImageBitmap(copyBitmap);
                            });
                        }).start();
                        return kotlin.Unit.INSTANCE;
                    }
                }
        );


        binding.filterRecycler.setLayoutManager(
                new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        );
        binding.filterRecycler.setAdapter(filterAdapter);
    }


    //    clicked on the crop
    private void cropUI() {
        binding.sliderSelector.setVisibility(VISIBLE);
        binding.llCrop.setVisibility(VISIBLE);
        binding.utilitySliderContainer.setVisibility(GONE);
        binding.llEdit.setVisibility(GONE);
        binding.llFilter.setVisibility(GONE);
        binding.sliderSelector.setVisibility(VISIBLE);
        binding.colorPickerRecycler.setVisibility(GONE);
        binding.llText.setVisibility(GONE);
        binding.cropIcons.setVisibility(VISIBLE);
        binding.doodleIcons.setVisibility(GONE);
        binding.gridOverlay.setVisibility(VISIBLE);
        binding.bottomBar.setVisibility(VISIBLE);
        binding.drawingView.setDrawingEnabled(false);
        binding.textInterationContainer.setVisibility(GONE);
        binding.filterRecycler.setVisibility(GONE);
        currentMode = SliderMode.DRAW;
        currentTextView = null;
        System.out.println(currentMode);
    }

    //    clicked on the doodle
    private void doodleUI() {
        binding.llCrop.setVisibility(GONE);
        binding.llFilter.setVisibility(GONE);
        binding.sliderSelector.setVisibility(VISIBLE);
        binding.utilitySliderContainer.setVisibility(VISIBLE);
        binding.llEdit.setVisibility(VISIBLE);
        binding.sliderSelector.setVisibility(View.INVISIBLE);
        binding.colorPickerRecycler.setVisibility(VISIBLE);
        binding.llText.setVisibility(GONE);
        binding.cropIcons.setVisibility(View.INVISIBLE);
        binding.doodleIcons.setVisibility(VISIBLE);
        binding.cropSelectionWrapper.setVisibility(GONE);
        binding.gridOverlay.setVisibility(GONE);
        binding.bottomBar.setVisibility(VISIBLE);
        binding.drawingView.setDrawingEnabled(true);
        binding.textInterationContainer.setVisibility(GONE);
        binding.filterRecycler.setVisibility(GONE);
        currentMode = SliderMode.DRAW;
        currentTextView = null;
        System.out.println(currentMode);
    }

    //    clicked on the filter
    private void filterUI() {
        binding.llEdit.setVisibility(GONE);
        binding.llCrop.setVisibility(GONE);
        binding.sliderSelector.setVisibility(View.INVISIBLE);
        binding.utilitySliderContainer.setVisibility(GONE);
        binding.sliderSelector.setVisibility(View.INVISIBLE);
        binding.colorPickerRecycler.setVisibility(View.INVISIBLE);
        binding.llText.setVisibility(GONE);
        binding.filterRecycler.setVisibility(VISIBLE);
        binding.llFilter.setVisibility(VISIBLE);
        binding.cropIcons.setVisibility(View.INVISIBLE);
        binding.doodleIcons.setVisibility(View.INVISIBLE);
        binding.gridOverlay.setVisibility(View.INVISIBLE);
        binding.bottomBar.setVisibility(VISIBLE);
        binding.textInterationContainer.setVisibility(GONE);
        binding.drawingView.setDrawingEnabled(false);
        currentMode = SliderMode.DRAW;
        currentTextView = null;
        System.out.println(currentMode);
    }

    //    clicked on the text
    private void textUI() {
        binding.sliderSelector.setVisibility(VISIBLE);
        binding.utilitySliderContainer.setVisibility(VISIBLE);
        binding.sliderSelector.setVisibility(View.INVISIBLE);
        binding.llText.setVisibility(VISIBLE);
        binding.cropIcons.setVisibility(GONE);
        binding.doodleIcons.setVisibility(GONE);
        binding.gridOverlay.setVisibility(View.INVISIBLE);
        binding.bottomBar.setVisibility(GONE);
        binding.filterRecycler.setVisibility(GONE);
        binding.drawingView.setDrawingEnabled(false);
        currentMode = SliderMode.FONT;
        System.out.println(currentMode);
    }

    private void updateBitmapWithDrawing(Bitmap newBitmap) {
        // Merge the current doodles onto the new bitmap
        Bitmap merged = EditImageUtils.mergeDrawingWithImage(newBitmap, binding.drawingView);

        // Update both references
        copyBitmap = merged.copy(merged.getConfig(), true);

        // Set to ImageView
        binding.imageToEdit.setImageBitmap(copyBitmap);

        // Clear the drawing view (strokes are now part of the bitmap)
        binding.drawingView.clearDrawing();
    }

    // Helper method to apply crop and preserve doodles
    private void applyCropWithDoodles(GridOverlayView.CropMode mode, int ratioX, int ratioY, java.util.function.Function<Bitmap, Bitmap> cropFunction) {
        binding.gridOverlay.setCropMode(mode, ratioX, ratioY);

        if (currentBitmap != null) {
            // Merge current doodles with permanent bitmap
            Bitmap merged = EditImageUtils.mergeDrawingWithImage(currentBitmap, binding.drawingView);

            // Clear DrawingView strokes
            binding.drawingView.clearDrawing();

            // Crop using the provided crop function
            copyBitmap = cropFunction.apply(merged);

            // Update permanent reference
            currentBitmap = copyBitmap.copy(copyBitmap.getConfig(), true);

            // Update ImageView
            binding.imageToEdit.setImageBitmap(copyBitmap);
        }
    }

    private void customText() {
        binding.confirmTextButton.setVisibility(VISIBLE);
        binding.resetButton.setVisibility(GONE);
        DraggableTextView newTextView = new DraggableTextView(this);

        FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.WRAP_CONTENT,
                FrameLayout.LayoutParams.WRAP_CONTENT
        );
        params.gravity = Gravity.CENTER;
        newTextView.setLayoutParams(params);

        binding.canvasContainer.addView(newTextView);

        // Enable editing immediately
        newTextView.enableEditing();

        // Update reference
        currentTextView = newTextView;

        // Show the text interaction toolbar
        binding.textInterationContainer.setVisibility(VISIBLE);
        binding.llText.setVisibility(VISIBLE);

        // Setup slider + color to sync with this text
        binding.utilitySlider.setValue(newTextView.getFontSize());
        binding.utilitySlider.setThumbTintList(ColorStateList.valueOf(newTextView.getFontColor()));

        binding.textSlider.setValue(newTextView.getFontSize());
        binding.textSlider.setThumbTintList(ColorStateList.valueOf(newTextView.getFontColor()));
    }

    public void showRenameDialog(Context context, String imagePath) {
        View dialogView = LayoutInflater.from(context).inflate(R.layout.dialog_rename, null);
        TextInputEditText etRename = dialogView.findViewById(R.id.etRename);
        ImageView imgRemove = dialogView.findViewById(R.id.imgRemove);
        RelativeLayout cancelBtn = dialogView.findViewById(R.id.cancelButton);
        RelativeLayout okBtn = dialogView.findViewById(R.id.okButton);

        File currentFile = new File(Uri.parse(imagePath).getPath());
        String currentFileName = currentFile.getName();
        String nameWithoutExt = currentFileName.contains(".") ?
                currentFileName.substring(0, currentFileName.lastIndexOf(".")) : currentFileName;

        etRename.setText(nameWithoutExt);

        imgRemove.setOnClickListener(v -> {
            if (etRename.getText() != null) {
                etRename.getText().clear();
            }
        });

        AlertDialog dialog = new AlertDialog.Builder(context)
                .setView(dialogView)
                .setCancelable(true)
                .create();
        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        }

        cancelBtn.setOnClickListener(v -> dialog.dismiss());

        okBtn.setOnClickListener(v -> {
            String newName = etRename.getText() != null ? etRename.getText().toString().trim() : "";
            if (!newName.isEmpty()) {
                try {
                    File newFile = new File(currentFile.getParent(), newName + ".jpg");

                    if (currentFile.exists()) {
                        boolean renamed = currentFile.renameTo(newFile);
                        if (renamed) {
                            // Update displayed name
                            binding.imageNameText.setText(newName);
                            Toast.makeText(context, "Renamed successfully", Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(context, "Rename failed", Toast.LENGTH_SHORT).show();
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    Toast.makeText(context, "Error renaming file", Toast.LENGTH_SHORT).show();
                }
            }
            dialog.dismiss();
        });

        dialog.show();
    }

    private void setupBackPressed() {
        OnBackPressedCallback callback = new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                showExitDialog(EditImage.this);
            }
        };
        getOnBackPressedDispatcher().addCallback(this, callback);
    }

    private AlertDialog exitDialog;

    public void showExitDialog(Context context) {
        if (exitDialog != null && exitDialog.isShowing()) return; // avoid multiple dialogs

        View dialogView = LayoutInflater.from(context).inflate(R.layout.dialog_exit, null);

        RelativeLayout cancelBtn = dialogView.findViewById(R.id.cancelButton);
        RelativeLayout okBtn = dialogView.findViewById(R.id.okButton);

        exitDialog = new AlertDialog.Builder(context)
                .setView(dialogView)
                .setCancelable(true)
                .create();

        if (exitDialog.getWindow() != null) {
            exitDialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        }

        cancelBtn.setOnClickListener(v -> exitDialog.dismiss());

        okBtn.setOnClickListener(v -> {
            finish();
            exitDialog.dismiss();
        });

        exitDialog.show();
    }
}