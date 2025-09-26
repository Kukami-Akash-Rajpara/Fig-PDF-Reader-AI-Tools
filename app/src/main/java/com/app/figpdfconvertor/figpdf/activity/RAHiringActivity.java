package com.app.figpdfconvertor.figpdf.activity;

import android.animation.ArgbEvaluator;
import android.animation.ValueAnimator;
import android.app.Activity;
import android.content.Intent;
import android.graphics.drawable.GradientDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.text.HtmlCompat;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.app.figpdfconvertor.figpdf.R;
import com.app.figpdfconvertor.figpdf.ads.AdEventListener;
import com.app.figpdfconvertor.figpdf.ads.AdManagerInter;
import com.app.figpdfconvertor.figpdf.databinding.ActivityRaHiringBinding;
import com.app.figpdfconvertor.figpdf.preferences.AppHelper;
import com.app.figpdfconvertor.figpdf.utils.DoubleClickListener;
import com.app.figpdfconvertor.figpdf.utils.MyUtils;

public class RAHiringActivity extends BaseActivity {

    private ActivityRaHiringBinding binding;

    private ActivityResultLauncher<Intent> pdfPickerLauncher;
    private Uri selectedPdfUri;
    private boolean isJobDesRequired;

    public void blinkEditTextBorder(final View editText) {
        final GradientDrawable background = (GradientDrawable) editText.getBackground();

        final int colorFrom = ContextCompat.getColor(this, R.color.stroke_color); // normal
        final int colorTo = ContextCompat.getColor(this, R.color.dark_pink_color); // blink

        final int strokeWidthFrom = 2; // normal width in px
        final int strokeWidthTo = 6;   // highlight width in px

        ValueAnimator colorWidthAnimator = ValueAnimator.ofFloat(0f, 1f);
        colorWidthAnimator.setDuration(500); // 0.5 sec per blink
        colorWidthAnimator.setRepeatCount(5); // number of blinks
        colorWidthAnimator.setRepeatMode(ValueAnimator.REVERSE);

        colorWidthAnimator.addUpdateListener(animator -> {
            float fraction = animator.getAnimatedFraction();

            // Animate color
            int newColor = (int) new ArgbEvaluator().evaluate(fraction, colorFrom, colorTo);

            // Animate stroke width
            int newWidth = (int) (strokeWidthFrom + fraction * (strokeWidthTo - strokeWidthFrom));

            background.setStroke(newWidth, newColor);
        });

        colorWidthAnimator.start();
    }


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

        if (isJobDesRequired){
            binding.txtJobDesc.setText(getString(R.string.job_description));
            binding.etJobDesc.setHint(getString(R.string.eg_hiring_job_description));
        }
        else {
            binding.txtJobDesc.setText(getString(R.string.candidate_job_description));
            binding.etJobDesc.setHint(getString(R.string.eg_candidate_job_description));
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

        binding.txtClear.setOnClickListener(new DoubleClickListener() {
            @Override
            public void performClick(View v) {
                binding.etJobDesc.setText(""); // clears the EditText
                checkAllFieldsFilled();
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
                String jobDesc = binding.etJobDesc.getText().toString().trim();

                if (selectedPdfUri == null || selectedPdfUri.toString().isEmpty()) {
                    Toast.makeText(RAHiringActivity.this, "Please select a PDF first", Toast.LENGTH_SHORT).show();
                } else if (isJobDesRequired && jobDesc.isEmpty()) {
                    // Empty field
                    blinkEditTextBorder(binding.etJobDesc);
                    Toast.makeText(RAHiringActivity.this, "Please enter job description", Toast.LENGTH_SHORT).show();
                } else if (isJobDesRequired && jobDesc.length() < 10) {
                    // Less than 10 characters
                    blinkEditTextBorder(binding.etJobDesc);
                    Toast.makeText(RAHiringActivity.this, "Job description must be at least 10 characters", Toast.LENGTH_SHORT).show();
                } else {
                    // Valid → proceed
                    if (isJobDesRequired) {
                        Intent intent = new Intent(RAHiringActivity.this, LoadingHiringActivity.class);
                        intent.putExtra("pdf_uri", selectedPdfUri.toString());
                        intent.putExtra("job_description", jobDesc);
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


        setupFieldWatchers();
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
        checkAllFieldsFilled();
        blinkEditTextBorder(binding.etJobDesc);
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

    private void checkAllFieldsFilled() {
        boolean isReady = selectedPdfUri != null
                && (!isJobDesRequired || !binding.etJobDesc.getText().toString().isEmpty());

        if (isReady) {
            // Enable button
            binding.txtSubmit.setAlpha(1f);
            binding.txtSubmit.setEnabled(true);

            // Show shimmer
            binding.shimmer.setVisibility(View.VISIBLE);
        } else {
            // Disable button
            binding.txtSubmit.setAlpha(0.5f);
            binding.txtSubmit.setEnabled(false);

            // Hide shimmer
            binding.shimmer.setVisibility(View.GONE);
        }
    }


    private void setupFieldWatchers() {
        binding.etJobDesc.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) { }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                checkAllFieldsFilled();
            }

            @Override
            public void afterTextChanged(Editable s) { }
        });
    }
}