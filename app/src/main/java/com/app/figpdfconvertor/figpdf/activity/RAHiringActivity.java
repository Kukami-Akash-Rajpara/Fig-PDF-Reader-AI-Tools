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
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.app.figpdfconvertor.figpdf.ads.AdEventListener;
import com.app.figpdfconvertor.figpdf.ads.AdManagerInter;
import com.app.figpdfconvertor.figpdf.databinding.ActivityRaHiringBinding;
import com.app.figpdfconvertor.figpdf.preferences.AppHelper;
import com.app.figpdfconvertor.figpdf.utils.DoubleClickListener;
import com.app.figpdfconvertor.figpdf.utils.MyUtils;

public class RAHiringActivity extends AppCompatActivity {

    private ActivityRaHiringBinding binding;

    private ActivityResultLauncher<Intent> pdfPickerLauncher;
    private Uri selectedPdfUri;
    private boolean isJobDesRequired;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        MyUtils.fullScreenLightStatusBar(this);
        binding = ActivityRaHiringBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        ViewCompat.setOnApplyWindowInsetsListener(binding.main, (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        setupBackPressed();

        Intent intent = getIntent();
        if (intent != null) {
            isJobDesRequired = intent.getBooleanExtra("isJobDes", true);
        }

        pdfPickerLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                        selectedPdfUri = result.getData().getData();
                        if (selectedPdfUri != null) {
                            showPdfDetails(selectedPdfUri);
                        }
                    }
                });

        binding.layUpload.setOnClickListener(new DoubleClickListener() {
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

        binding.txtClear.setOnClickListener(new DoubleClickListener() {
            @Override
            public void performClick(View v) {
                binding.etJobDesc.setText(""); // clears the EditText
            }
        });

        binding.layViewFile.setOnClickListener(new DoubleClickListener() {
            @Override
            public void performClick(View v) {
                if (selectedPdfUri != null) {
                    Intent intent = new Intent(RAHiringActivity.this, ViewFileActivity.class);
                    intent.putExtra("pdf_uri", selectedPdfUri.toString());
                    startActivity(intent);
                } else {
                    Toast.makeText(RAHiringActivity.this, "Please select a PDF first", Toast.LENGTH_SHORT).show();
                }
            }
        });

        binding.txtSubmit.setOnClickListener(new DoubleClickListener() {
            @Override
            public void performClick(View v) {
                if (selectedPdfUri == null || selectedPdfUri.toString().isEmpty()) {
                    Toast.makeText(RAHiringActivity.this, "Please select a PDF first", Toast.LENGTH_SHORT).show();
                } else if (binding.etJobDesc.getText().toString().isEmpty() && isJobDesRequired) {
                    Toast.makeText(RAHiringActivity.this, "Please enter job description", Toast.LENGTH_SHORT).show();
                } else {
                    if (isJobDesRequired) {
                        Intent intent = new Intent(RAHiringActivity.this, LoadingHiringActivity.class);
                        intent.putExtra("pdf_uri", selectedPdfUri.toString());
                        intent.putExtra("job_description", binding.etJobDesc.getText().toString());
                        startActivity(intent);
                    } else {
                        if (AppHelper.getShowInterAnalyzerCandidateSubmit()) {
                            AdManagerInter.renderInterAdFixed(RAHiringActivity.this, new AdEventListener() {
                                @Override
                                public void onAdFinish() {
                                    Intent intent = new Intent(RAHiringActivity.this, LoadingCandidatesActivity.class);
                                    intent.putExtra("pdf_uri", selectedPdfUri.toString());
                                    startActivity(intent);
                                }
                            });
                        } else {
                            Intent intent = new Intent(RAHiringActivity.this, LoadingCandidatesActivity.class);
                            intent.putExtra("pdf_uri", selectedPdfUri.toString());
                            startActivity(intent);
                        }
                    }
                }
            }
        });
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
        Log.d("ResumeAnalyzer", "PDF Uri: " + uri);
        Log.d("ResumeAnalyzer", "PDF Path: " + pdfPath);
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