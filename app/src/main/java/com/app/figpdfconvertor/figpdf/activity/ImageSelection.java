package com.app.figpdfconvertor.figpdf.activity;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.RelativeLayout;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.app.figpdfconvertor.figpdf.R;
import com.app.figpdfconvertor.figpdf.adapter.BottomSheetImagesAdapter;
import com.app.figpdfconvertor.figpdf.adapter.ImageSelectionAdapter;
import com.app.figpdfconvertor.figpdf.databinding.ActivityImageSelectionBinding;
import com.app.figpdfconvertor.figpdf.funnelss.AnalyticsManager;
import com.app.figpdfconvertor.figpdf.utils.MyUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ImageSelection extends BaseActivity {

    private ActivityImageSelectionBinding binding;
    private ImageSelectionAdapter adapter;
    private BottomSheetImagesAdapter bottomSheetAdapter;

    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private final Handler mainHandler = new Handler(Looper.getMainLooper());

    private final List<Long> allImageIds = new ArrayList<>();
    private final List<Uri> capturedImages = new ArrayList<>();
    private ArrayList<Integer> fullScreenSelectedPositions = new ArrayList<>();

    private static final int REQUEST_PERMISSION = 100;
    private static final int PAGE_SIZE = 50;

    private boolean imagesLoaded = false;
    private boolean isFirstSpinnerSelection = true;
    private boolean isLoading = false;
    private boolean allImagesLoaded = false;
    private int currentPage = 0;
    private int totalImagesInFolder = 0;
    private String currentFolderName = null;

    private ActivityResultLauncher<Intent> fullscreenLauncher, cameraLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        MyUtils.fullScreenLightStatusBar(this);
        binding = ActivityImageSelectionBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setupRecyclerViews();
        setupLaunchers();
        setupButtons();
        loadFoldersAsync();
        checkPermissionAndLoad();
        setupBackPressed();
        ArrayList<String> existingImages = getIntent().getStringArrayListExtra("existingImages");
        if (existingImages != null && !existingImages.isEmpty()) {
            for (String uriStr : existingImages) {
                Uri uri = Uri.parse(uriStr);
                capturedImages.add(uri);  // Add to capturedImages list to preserve them
            }
        }
    }

    //  Recycler setup
    private void setupRecyclerViews() {
        binding.imageSelection.setLayoutManager(new GridLayoutManager(this, 3));

        binding.bottomSheetImages.setLayoutManager(
                new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        );

        bottomSheetAdapter = new BottomSheetImagesAdapter(removedUri -> {
            if (adapter != null) adapter.removeSelection(removedUri);
            updateBottomSheetImages();
            updateImportButtonText();
            updateBottomSheetState();
        });
        binding.bottomSheetImages.setAdapter(bottomSheetAdapter);

        binding.imageSelection.addOnScrollListener(new androidx.recyclerview.widget.RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull androidx.recyclerview.widget.RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                GridLayoutManager layoutManager = (GridLayoutManager) recyclerView.getLayoutManager();
                if (layoutManager != null && !isLoading && !allImagesLoaded) {
                    int visibleItemCount = layoutManager.getChildCount();
                    int totalItemCount = layoutManager.getItemCount();
                    int firstVisibleItemPosition = layoutManager.findFirstVisibleItemPosition();
                    if ((visibleItemCount + firstVisibleItemPosition) >= totalItemCount - 10) {
                        loadNextPage();
                    }
                }
            }
        });
    }

    //  Launchers
    private void setupLaunchers() {
        cameraLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                        Bitmap bitmap = (Bitmap) result.getData().getExtras().get("data");
                        if (bitmap != null) {
                            Uri capturedUri = saveImageToCache(bitmap);
                            if (capturedUri != null) {
                                capturedImages.add(capturedUri);

                                // ✅ Refresh bottom sheet immediately
                                updateBottomSheetImages();
                                updateBottomSheetState();
                                updateImportButtonText();
                            }
                        }
                    }
                }
        );

        fullscreenLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                        fullScreenSelectedPositions = result.getData().getIntegerArrayListExtra("updated_positions");
                        if (adapter != null && fullScreenSelectedPositions != null) {
                            adapter.setSelectedPositions(fullScreenSelectedPositions);
                        }
                        updateBottomSheetImages();
                        updateImportButtonText();
                        updateBottomSheetState();
                    }
                }
        );
    }

    // Button listeners
    private void setupButtons() {
        binding.backButtonToProConverterTools.setOnClickListener(v -> finish());

        final boolean[] isAllSelected = {false};
        binding.selectAllButton.setOnClickListener(v -> {
            if (adapter != null) {
                if (isAllSelected[0]) {
                    adapter.clearSelection();
                    binding.resetSelection.setVisibility(View.GONE);
                } else {
                    adapter.selectAllImages();
                    binding.resetSelection.setVisibility(View.VISIBLE);
                }
                isAllSelected[0] = !isAllSelected[0];
                updateBottomSheetImages();
                updateImportButtonText();
                updateBottomSheetState();
            }
        });

        binding.resetSelection.setOnClickListener(v -> {
            if (adapter != null) adapter.clearSelection();
            binding.resetSelection.setVisibility(View.GONE);
            updateBottomSheetImages();
            updateImportButtonText();
            updateBottomSheetState();
        });

        binding.importButton.setOnClickListener(v -> {
            if (adapter != null) {
                ArrayList<Uri> selectedUris = new ArrayList<>();
                // Add images selected in adapter
                for (int pos : adapter.getSelectedPositions()) {
                    selectedUris.add(adapter.getImageUri(pos));
                }
                // Add captured images (from camera or old ones)
                selectedUris.addAll(capturedImages);

                // If nothing selected yet, consider all images in current folder
                if (selectedUris.isEmpty()) {
                    selectedUris.addAll(adapter.getAllImages());
                }

                if (!selectedUris.isEmpty()) {
                    ArrayList<String> newImages = new ArrayList<>();
                    for (Uri uri : selectedUris) newImages.add(uri.toString());
                    AnalyticsManager.INSTANCE.logEvent("feature_selected",
                            java.util.Collections.singletonMap("feature", "import_images"));
                    AnalyticsManager.INSTANCE.logFunnelStep("import_images_selected", null);
                    // Go to ImageEditActivity
                    Intent editIntent = new Intent(this, ImageEditActivity.class);
                    editIntent.putStringArrayListExtra("imageUris", newImages);
                    startActivity(editIntent);
                    finish();
                } else {
                    Toast.makeText(this, "No images available", Toast.LENGTH_SHORT).show();
                }
            }
        });

    }
    private void setupBackPressed() {
        OnBackPressedCallback callback = new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                //showExitDialog(ImageSelection.this);
                finish();
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
    //  Save captured image
    private Uri saveImageToCache(Bitmap bitmap) {
        try {
            File cachePath = new File(getCacheDir(), "captured_images");
            if (!cachePath.exists()) cachePath.mkdirs();
            File imageFile = new File(cachePath, "captured_" + System.currentTimeMillis() + ".jpg");
            try (FileOutputStream stream = new FileOutputStream(imageFile)) {
                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream);
            }
            return Uri.fromFile(imageFile);
        } catch (Exception e) {
            return null;
        }
    }

    //  Permissions
    private void checkPermissionAndLoad() {
        List<String> permissionsToRequest = new ArrayList<>();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_MEDIA_IMAGES) != PackageManager.PERMISSION_GRANTED)
                permissionsToRequest.add(Manifest.permission.READ_MEDIA_IMAGES);
        } else {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED)
                permissionsToRequest.add(Manifest.permission.READ_EXTERNAL_STORAGE);
        }
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED)
            permissionsToRequest.add(Manifest.permission.CAMERA);

        if (!permissionsToRequest.isEmpty()) {
            ActivityCompat.requestPermissions(this,
                    permissionsToRequest.toArray(new String[0]), REQUEST_PERMISSION);
        } else {
            loadImagesIfNotLoaded(null);
        }
    }

    //  Load folders spinner
    private void loadFoldersAsync() {
        executor.execute(() -> {
            List<String> folders = getImageFolders();
            mainHandler.post(() -> {
                ArrayAdapter<String> spinnerAdapter =
                        new ArrayAdapter<>(this, R.layout.spinner_item, folders);
                spinnerAdapter.setDropDownViewResource(R.layout.spinner_dropdown_item);
                binding.imageOptions.setAdapter(spinnerAdapter);

                binding.imageOptions.setOnItemSelectedListener(new android.widget.AdapterView.OnItemSelectedListener() {
                    @Override
                    public void onItemSelected(android.widget.AdapterView<?> parent, View view, int position, long id) {
                        String selectedFolder = parent.getItemAtPosition(position).toString();
                        if (isFirstSpinnerSelection) {
                            isFirstSpinnerSelection = false;
                            return;
                        }
                        currentFolderName = selectedFolder.equals("All Images") ? null : selectedFolder;
                        currentPage = 0;
                        allImagesLoaded = false;
                        if (adapter != null) adapter.clearImages();
                        loadAllImageIds(currentFolderName);
                    }
                    @Override public void onNothingSelected(android.widget.AdapterView<?> parent) {}
                });
            });
        });
    }

    // Load images
    private void loadImagesIfNotLoaded(@Nullable String folderName) {
        if (imagesLoaded) return;
        imagesLoaded = true;
        currentPage = 0;
        allImagesLoaded = false;
        loadAllImageIds(folderName);
    }

    private void loadAllImageIds(@Nullable String folderName) {
        executor.execute(() -> {
            allImageIds.clear();
            Uri collection = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
            String[] projection = {MediaStore.Images.Media._ID};
            String selection = null;
            String[] selectionArgs = null;
            if (folderName != null && !folderName.equals("All Images")) {
                selection = MediaStore.Images.Media.BUCKET_DISPLAY_NAME + "=?";
                selectionArgs = new String[]{folderName};
            }
            try (Cursor cursor = getContentResolver().query(collection, projection, selection, selectionArgs,
                    MediaStore.Images.Media.DATE_ADDED + " DESC")) {
                if (cursor != null) {
                    int idColumn = cursor.getColumnIndexOrThrow(MediaStore.Images.Media._ID);
                    while (cursor.moveToNext()) {
                        allImageIds.add(cursor.getLong(idColumn));
                    }
                }
            }
            totalImagesInFolder = allImageIds.size();
            mainHandler.post(this::loadNextPage);
        });
    }

    private void loadNextPage() {
        if (isLoading) return;
        isLoading = true;
        int start = currentPage * PAGE_SIZE;
        int end = Math.min(start + PAGE_SIZE, allImageIds.size());
        if (start >= end) {
            allImagesLoaded = true;
            isLoading = false;
            return;
        }
        List<Uri> pageUris = new ArrayList<>();
        Uri collection = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
        for (int i = start; i < end; i++) {
            pageUris.add(Uri.withAppendedPath(collection, String.valueOf(allImageIds.get(i))));
        }
        mainHandler.post(() -> {
            if (adapter == null) {
                adapter = new ImageSelectionAdapter(pageUris, new ImageSelectionAdapter.OnItemClickListener() {
                    @Override public void onCameraClick() { cameraLauncher.launch(new Intent(MediaStore.ACTION_IMAGE_CAPTURE)); }
                    @Override public void onImageClick(int position) { updateUI(); }
                    @Override public void onSelectionChanged(boolean hasSelection) {
                        binding.resetSelection.setVisibility(hasSelection ? View.VISIBLE : View.GONE);
                    }
                    @Override public void onExpandClick(int position) {
                        Intent intent = new Intent(ImageSelection.this, FullScreenImageActivity.class);
                        intent.putParcelableArrayListExtra("images", new ArrayList<>(adapter.getAllImages()));
                        intent.putIntegerArrayListExtra("selected_positions", new ArrayList<>(adapter.getSelectedPositions()));
                        intent.putExtra("position", position);
                        fullscreenLauncher.launch(intent);
                    }
                });
                binding.imageSelection.setAdapter(adapter);
            } else adapter.addImages(pageUris);
            currentPage++;
            isLoading = false;
        });
    }

    //  Updates
    private void updateBottomSheetImages() {
        List<Uri> selectedUris = new ArrayList<>();
        if (adapter != null) {
            for (int pos : adapter.getSelectedPositions()) {
                selectedUris.add(adapter.getImageUri(pos));
            }
        }
        selectedUris.addAll(capturedImages);
        bottomSheetAdapter.updateImages(selectedUris);
    }



    private void updateImportButtonText() {
        int count = adapter != null ? adapter.getSelectedCount() : 0;
        binding.importButton.setText(getString(R.string.import_button_count, count));
        binding.importButton.setBackgroundResource(count == 0 ? R.drawable.importbuttonunselected : R.drawable.importbutton);
    }

    private void updateBottomSheetState() {
        binding.bottomSheet.setVisibility(adapter != null && adapter.getSelectedCount() > 0
                ? View.VISIBLE : View.GONE);
    }

    private void updateUI() {
        updateImportButtonText();
        updateBottomSheetImages();
        updateBottomSheetState();
    }

    //  Permissions result
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_PERMISSION) {
            boolean granted = true;
            for (int r : grantResults) if (r != PackageManager.PERMISSION_GRANTED) granted = false;
            if (granted) loadImagesIfNotLoaded(null);
            else Toast.makeText(this, "Permission required to load images", Toast.LENGTH_SHORT).show();
        }
    }

    //  Folder names
    private List<String> getImageFolders() {
        List<String> folders = new ArrayList<>();
        Set<String> folderSet = new HashSet<>();
        Uri collection = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
        String[] projection = {MediaStore.Images.Media.BUCKET_ID, MediaStore.Images.Media.BUCKET_DISPLAY_NAME};
        try (Cursor cursor = getContentResolver().query(collection, projection, null, null,
                MediaStore.Images.Media.DATE_ADDED + " DESC")) {
            if (cursor != null) {
                int idColumn = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.BUCKET_ID);
                int nameColumn = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.BUCKET_DISPLAY_NAME);
                while (cursor.moveToNext()) {
                    String bucketId = cursor.getString(idColumn);
                    String folderName = cursor.getString(nameColumn);
                    if (folderSet.add(bucketId)) folders.add(folderName);
                }
            }
        }
        Collections.sort(folders);
        folders.add(0, "All Images");
        return folders;
    }
}
