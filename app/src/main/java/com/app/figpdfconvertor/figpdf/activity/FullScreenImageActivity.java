package com.app.figpdfconvertor.figpdf.activity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.widget.AppCompatCheckBox;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.widget.ViewPager2;

import com.app.figpdfconvertor.figpdf.R;
import com.app.figpdfconvertor.figpdf.adapter.BottomSheetImagesAdapter;
import com.app.figpdfconvertor.figpdf.adapter.ImageGalleryAdapter;
import com.app.figpdfconvertor.figpdf.utils.MyUtils;

import java.util.ArrayList;
import java.util.List;

public class FullScreenImageActivity extends BaseActivity {

    private TextView imageSlideIndex;
    private ImageGalleryAdapter imageGalleryAdapter;
    private ViewPager2 viewPager;
    private AppCompatCheckBox checkbox;
    private LinearLayout bottomSheet;
    private RecyclerView bottomSheetRecycler;
    private BottomSheetImagesAdapter bottomSheetAdapter;

    // Keep track of selected positions
    private ArrayList<Integer> selectedPositions;

    private TextView importButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        MyUtils.fullScreenLightStatusBar(this);
        setContentView(R.layout.activity_full_screen_image);

        // Apply padding for system bars
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        //import button
         importButton = findViewById(R.id.importButton);

        // Get images and starting position from intent
        ArrayList<Uri> images = getIntent().getParcelableArrayListExtra("images");
        int startPosition = getIntent().getIntExtra("position", 0);
        selectedPositions = getIntent().getIntegerArrayListExtra("selected_positions");
        if (selectedPositions == null) selectedPositions = new ArrayList<>();

        // Setup ViewPager2 with adapter
        viewPager = findViewById(R.id.full_screen_gallery);
        imageGalleryAdapter = new ImageGalleryAdapter(images);
        viewPager.setAdapter(imageGalleryAdapter);
        viewPager.setCurrentItem(startPosition, false);

        // Top index TextView
        imageSlideIndex = findViewById(R.id.image_slide_index);
        updateSlideIndex(startPosition, images.size());

        // Top checkbox
        checkbox = findViewById(R.id.custom_checkbox);
        checkbox.setChecked(selectedPositions.contains(startPosition));

        // Bottom sheet setup
        bottomSheet = findViewById(R.id.bottom_sheet_gallery);
        bottomSheetRecycler = findViewById(R.id.bottom_sheet_images);
        bottomSheetRecycler.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        bottomSheetAdapter = new BottomSheetImagesAdapter(removedUri -> {
            // Remove image from selection
            int index = images.indexOf(removedUri);
            selectedPositions.remove((Integer) index);

            // Update checkbox
            checkbox.setChecked(selectedPositions.contains(viewPager.getCurrentItem()));

            // Refresh bottom sheet and ViewPager
            updateBottomSheetImages();
            updateBottomSheetState();
            imageGalleryAdapter.notifyItemChanged(index);
        });
        bottomSheetRecycler.setAdapter(bottomSheetAdapter);

        // Update bottom sheet initially
        updateImportButtonText();
        updateBottomSheetImages();
        updateBottomSheetState();

        // Handle page changes
        viewPager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                super.onPageSelected(position);
                updateSlideIndex(position, images.size());
                checkbox.setChecked(selectedPositions.contains(position));
            }
        });

        // Checkbox click toggles selection for current image
        checkbox.setOnClickListener(v -> {
            int currentPos = viewPager.getCurrentItem();
            if (checkbox.isChecked()) {
                if (!selectedPositions.contains(currentPos)) selectedPositions.add(currentPos);
            } else {
                selectedPositions.remove(Integer.valueOf(currentPos));
            }

            // Update bottom sheet and adapter
            updateBottomSheetImages();
            updateBottomSheetState();
            updateImportButtonText();
            //notify the change in the adapter
            imageGalleryAdapter.notifyItemChanged(currentPos);

            // Immediately send updated selection back
            sendSelectionResult();
        });

        // Back button: return updated selection
        ImageView backButton = findViewById(R.id.back_to_image_selection_activity);
        backButton.setOnClickListener(v -> {
            sendSelectionResult();
            updateBottomSheetState();
            updateBottomSheetImages();
            updateImportButtonText();
            finish();
        });

        importButton.setOnClickListener(v -> {
            // Collect selected image URIs
            ArrayList<Uri> allImages = new ArrayList<>(imageGalleryAdapter.getImageUris());
            ArrayList<String> newImages = new ArrayList<>();

            for (int pos : selectedPositions) {
                if (pos >= 0 && pos < allImages.size()) {
                    newImages.add(allImages.get(pos).toString());
                }
            }

            if (!newImages.isEmpty()) {
                // Start ImageEditActivity
                Intent editIntent = new Intent(this, ImageEditActivity.class);
                editIntent.putStringArrayListExtra("imageUris", newImages);
                startActivity(editIntent);
                finish();
            } else {
                Toast.makeText(this, "Please select at least one image", Toast.LENGTH_SHORT).show();
            }
        });
    }

    /** Updates "current / total" index display */
    private void updateSlideIndex(int position, int total) {
        imageSlideIndex.setText((position + 1) + "/" + total);
    }

    /** Updates the bottom sheet adapter with current selection */
    private void updateBottomSheetImages() {
        List<Uri> selectedUris = new ArrayList<>();
        ArrayList<Uri> allImages = new ArrayList<>(imageGalleryAdapter.getImageUris());
        for (int pos : selectedPositions) {
            if (pos >= 0 && pos < allImages.size()) {
                selectedUris.add(allImages.get(pos));
            }
        }
        bottomSheetAdapter.updateImages(selectedUris);
    }

    /** Shows/hides the bottom sheet based on selection count */
    private void updateBottomSheetState() {
        if (selectedPositions.size() > 0) {
            bottomSheet.setVisibility(View.VISIBLE);
        } else {
            bottomSheet.setVisibility(View.GONE);
        }
    }

    /** Sends the updated selection back to the calling activity */
    private void sendSelectionResult() {
        Intent resultIntent = new Intent();
        resultIntent.putIntegerArrayListExtra("updated_positions", new ArrayList<>(selectedPositions));
        setResult(RESULT_OK, resultIntent);
    }

    private void updateImportButtonText() {
        int selectedCount = selectedPositions != null ? selectedPositions.size() : 0;

        if (selectedCount == 0) {
            importButton.setText(getString(R.string.import_button_count, selectedCount));
            importButton.setBackgroundResource(R.drawable.importbuttonunselected);
        } else {
            importButton.setText(getString(R.string.import_button_count, selectedCount));
            importButton.setBackgroundResource(R.drawable.importbutton);
        }
    }
}