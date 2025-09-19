package com.app.figpdfconvertor.figpdf.activity;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.viewpager2.widget.ViewPager2;

import com.app.figpdfconvertor.figpdf.R;
import com.app.figpdfconvertor.figpdf.adapter.ImagePagerAdapter;
import com.app.figpdfconvertor.figpdf.databinding.ActivityEditScreenBinding;
import com.app.figpdfconvertor.figpdf.utils.MyUtils;
import com.google.android.material.textfield.TextInputEditText;

import java.io.File;
import java.util.ArrayList;

public class EditScreen extends BaseActivity {

    private ActivityEditScreenBinding binding;

    public static class Constants {
        public static final String EXTRA_SELECTED_TOOL = "selected_tool";
        public static final int TOOL_CROP = 0;
        public static final int TOOL_DOODLE = 1;
        public static final int TOOL_FILTER = 2;
        public static final int TOOL_TEXT = 3;
    }
    private ArrayList<String> imageList;
    private int startPosition;
    private ActivityResultLauncher<Intent> editImageLauncher;

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

        binding = ActivityEditScreenBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        LinearLayout[] toolButtons = {
                binding.cropButton,
                binding.doodleButton,
                binding.filterButton,
                binding.textButton
        };

        int[] toolIndices = {
                Constants.TOOL_CROP,
                Constants.TOOL_DOODLE,
                Constants.TOOL_FILTER,
                Constants.TOOL_TEXT
        };


        ViewCompat.setOnApplyWindowInsetsListener(binding.main, (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        imageList = getIntent().getStringArrayListExtra("IMAGE_LIST");
        startPosition = getIntent().getIntExtra("POSITION", 0);
        ImagePagerAdapter pagerAdapter = new ImagePagerAdapter(this, imageList);
        binding.imageSlider.setAdapter(pagerAdapter);
        binding.imageSlider.setCurrentItem(startPosition, false);
        editImageLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                        String editedPath = result.getData().getStringExtra("EDITED_IMAGE_PATH");
                        int pos = result.getData().getIntExtra("POSITION", -1);
                        if (editedPath != null && pos != -1) {
                            imageList.set(pos, editedPath);
                            binding.imageSlider.getAdapter().notifyDataSetChanged();
                        }
                    }
                }
        );
        // pass on which tool is clicked to EditImage activity
        for (int i = 0; i < toolButtons.length; i++) {
            final int index = i;
            toolButtons[i].setOnClickListener(v -> {
                /*int currentPos = binding.imageSlider.getCurrentItem();
                String currentImage = imageList.get(currentPos);
                Intent intent = new Intent(EditScreen.this, EditImage.class);
                intent.putExtra(Constants.EXTRA_SELECTED_TOOL, toolIndices[index]);
                intent.putExtra("IMAGE_PATH", currentImage);
                startActivity(intent);*/
                int currentPos = binding.imageSlider.getCurrentItem();
                String currentImage = imageList.get(currentPos);
                Intent intent = new Intent(EditScreen.this, EditImage.class);
                intent.putExtra("IMAGE_PATH", currentImage);
                intent.putExtra("POSITION", currentPos);
                intent.putExtra(Constants.EXTRA_SELECTED_TOOL, toolIndices[index]);
                editImageLauncher.launch(intent);
              /*  Intent intent = new Intent(EditScreen.this, EditImage.class);
                intent.putExtra(Constants.EXTRA_SELECTED_TOOL, toolIndices[index]);
                startActivity(intent);*/
            });
        }

        // rename dialog
        binding.editImageName.setOnClickListener(v -> {
            showRenameDialog(this);
        });

        // back button
        binding.backButtonToActivity.setOnClickListener(v -> finish());

        binding.doneButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent resultIntent = new Intent();
                resultIntent.putStringArrayListExtra("IMAGE_LIST", imageList);
                setResult(RESULT_OK, resultIntent);
                finish();
            }
        });

        String firstImage = imageList.get(startPosition);
        binding.imageNameText.setText(getFileName(firstImage));

        binding.imageSlider.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                super.onPageSelected(position);
                String currentImage = imageList.get(position);
                binding.imageNameText.setText(getFileName(currentImage));
            }
        });
    }
    public void showRenameDialog(Context context) {
        View dialogView = LayoutInflater.from(context).inflate(R.layout.dialog_rename, null);
        TextInputEditText etRename = dialogView.findViewById(R.id.etRename);
        ImageView imgRemove = dialogView.findViewById(R.id.imgRemove);
        RelativeLayout cancelBtn = dialogView.findViewById(R.id.cancelButton);
        RelativeLayout okBtn = dialogView.findViewById(R.id.okButton);

        // Get current image path
        int currentIndex = binding.imageSlider.getCurrentItem();
        String currentPath = imageList.get(currentIndex);
        File currentFile = new File(Uri.parse(currentPath).getPath());
        String currentFileName = currentFile.getName();
        String nameWithoutExt = currentFileName.contains(".") ? currentFileName.substring(0, currentFileName.lastIndexOf(".")) : currentFileName;

        etRename.setText(nameWithoutExt);

        // Clear text when remove icon clicked
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
                    // Create new file in the same folder with new name
                    File newFile = new File(currentFile.getParent(), newName + ".jpg");
                    if (currentFile.exists()) {
                        currentFile.renameTo(newFile);
                    }

                    // Update list and adapter
                    String newUri = Uri.fromFile(newFile).toString();
                    imageList.set(currentIndex, newUri);
                    binding.imageSlider.getAdapter().notifyItemChanged(currentIndex);

                    // Update displayed name
                    binding.imageNameText.setText(newName);

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            dialog.dismiss();
        });

        dialog.show();
    }



}
