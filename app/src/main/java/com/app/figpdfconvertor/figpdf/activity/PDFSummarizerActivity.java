package com.app.figpdfconvertor.figpdf.activity;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.app.figpdfconvertor.figpdf.ads.AdManagerInter;
import com.app.figpdfconvertor.figpdf.databinding.ActivityPdfSummarizerBinding;
import com.app.figpdfconvertor.figpdf.preferences.AppHelper;
import com.app.figpdfconvertor.figpdf.utils.DoubleClickListener;
import com.app.figpdfconvertor.figpdf.utils.MyUtils;

public class PDFSummarizerActivity extends BaseActivity {

    private ActivityPdfSummarizerBinding binding;

    private ActivityResultLauncher<Intent> pdfPickerLauncher;
    private Uri selectedPdfUri;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        MyUtils.fullScreenLightStatusBar(this);
        binding = ActivityPdfSummarizerBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        ViewCompat.setOnApplyWindowInsetsListener(binding.main, (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        setupBackPressed();

        pdfPickerLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                        selectedPdfUri = result.getData().getData();
                        if (selectedPdfUri != null) {
                            showPdfDetails(selectedPdfUri);
                            updateSummarizeButtonState();
                        }
                    }
                });


        binding.btnUpload.setOnClickListener(new DoubleClickListener() {
            @Override
            public void performClick(View v) {
                openPdfPicker();
            }
        });

        binding.layChangePdf.setOnClickListener(new DoubleClickListener() {
            @Override
            public void performClick(View v) {
                openPdfPicker();
            }
        });

        binding.layViewFile.setOnClickListener(new DoubleClickListener() {
            @Override
            public void performClick(View v) {
                if (selectedPdfUri != null) {
                    Intent intent = new Intent(PDFSummarizerActivity.this, ViewFileActivity.class);
                    intent.putExtra("pdf_uri", selectedPdfUri.toString());
                    startActivity(intent);
                } else {
                    Toast.makeText(PDFSummarizerActivity.this, "Please select a PDF first", Toast.LENGTH_SHORT).show();
                }
            }
        });

        binding.txtSummarizer.setOnClickListener(new DoubleClickListener() {
            @Override
            public void performClick(View v) {
                if (selectedPdfUri != null) {
                    if (AppHelper.getShowInterSummarize()) {
                        AdManagerInter.renderInterAdFixed(PDFSummarizerActivity.this, () -> {
                            Intent intent = new Intent(PDFSummarizerActivity.this, PdfSummaryActivity.class);
                            intent.putExtra("pdf_uri", selectedPdfUri.toString());
                            startActivity(intent);
                        });
                    } else {
                        Intent intent = new Intent(PDFSummarizerActivity.this, PdfSummaryActivity.class);
                        intent.putExtra("pdf_uri", selectedPdfUri.toString());
                        startActivity(intent);
                    }
                } else {
                    Toast.makeText(PDFSummarizerActivity.this, "Please select a PDF first", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void updateSummarizeButtonState() {
        if (selectedPdfUri != null) {
            // Enable button
            binding.txtSummarizer.setAlpha(1f);
            binding.txtSummarizer.setEnabled(true);

            // Show shimmer continuously
            binding.shimmer.setVisibility(View.VISIBLE);
            binding.shimmer.playAnimation();
        } else {
            // Disable button
            binding.txtSummarizer.setAlpha(0.5f);
            binding.txtSummarizer.setEnabled(false);

            // Hide shimmer
            binding.shimmer.cancelAnimation();
            binding.shimmer.setVisibility(View.GONE);
        }
    }


    private void openPdfPicker() {
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.setType("application/pdf");
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        pdfPickerLauncher.launch(intent);
    }

    private void showPdfDetails(Uri uri) {
        // Show layPdfView
        binding.layUpload.setVisibility(View.GONE);
        binding.layPdfView.setVisibility(View.VISIBLE);
        binding.layChangePdf.setVisibility(View.VISIBLE);

        // Get the PDF file name from Uri
        String pdfName = MyUtils.getFileNameFromUri(this, uri);
        binding.txtPdfName.setText(pdfName != null ? pdfName : "Selected PDF");

        // If you need actual file path (optional, might not always work on newer Android)
        String pdfPath = MyUtils.getPathFromUri(this, uri);
        Log.d("PDFSummarizer", "PDF Uri: " + uri);
        Log.d("PDFSummarizer", "PDF Path: " + pdfPath);
    }

    private void setupBackPressed() {
        OnBackPressedCallback callback = new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                finish();
            }
        };
        getOnBackPressedDispatcher().addCallback(this, callback);

        binding.backKey.setOnClickListener(view -> {
            callback.handleOnBackPressed();
        });
    }
}