package com.app.figpdfconvertor.figpdf.fragments;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.fragment.app.Fragment;

import com.app.figpdfconvertor.figpdf.activity.ComingSoonActivity;
import com.app.figpdfconvertor.figpdf.activity.LanguageActivity;
import com.app.figpdfconvertor.figpdf.activity.OcrCapturedImage;
import com.app.figpdfconvertor.figpdf.activity.PptMainActivity;
import com.app.figpdfconvertor.figpdf.activity.ProConverterTools;
import com.app.figpdfconvertor.figpdf.activity.ResumeAnalyzerActivity;
import com.app.figpdfconvertor.figpdf.databinding.FragmentHomeBinding;
import com.app.figpdfconvertor.figpdf.funnelss.AnalyticsManager;
import com.app.figpdfconvertor.figpdf.activity.InterviewBotMainActivity;
import com.app.figpdfconvertor.figpdf.activity.PDFSummarizerActivity;
import com.app.figpdfconvertor.figpdf.pptedit.MainActivity;
import com.app.figpdfconvertor.figpdf.utils.DoubleClickListener;
import com.app.figpdfconvertor.figpdf.utils.OcrImageUtils;

import java.io.File;
import java.io.IOException;

public class HomeFragment extends BaseFragment {
    private Uri photoUri;
    private File photoFile;

    private FragmentHomeBinding binding;

    // Launcher to handle camera result
    private final ActivityResultLauncher<Intent> cameraLauncher =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
                if (result.getResultCode() == Activity.RESULT_OK) {
                    if (photoFile != null) {
                        Intent intent = new Intent(getContext(), OcrCapturedImage.class);
                        intent.putExtra("image_path", photoFile.getAbsolutePath());
                        startActivity(intent);
                    }
                }
            });

    // Launcher to request camera permission
    private final ActivityResultLauncher<String> requestPermissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
                if (isGranted) {
                    openCamera();
                } else {
                    Toast.makeText(requireContext(), "Camera permission is required", Toast.LENGTH_SHORT).show();
                }
            });

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentHomeBinding.inflate(inflater, container, false);
        View view = binding.getRoot();
        
        binding.pdfConverterProButton.setOnClickListener(v -> {
            userInteracted();

            AnalyticsManager.INSTANCE.logEvent("feature_selected",
                    java.util.Collections.singletonMap("feature", "pdf_converter"));
            AnalyticsManager.INSTANCE.logFunnelStep("pdf_converter_selected", null);
            startActivity(new Intent(getContext(), ProConverterTools.class));
        });

        binding.cardRecommendation.setOnClickListener(v -> {
            checkCameraPermissionAndOpenCamera();
//            startActivity(new Intent(getContext(), ProConverterTools.class));
        });

        binding.cardInterviewbot.setOnClickListener(v -> {
            AnalyticsManager.INSTANCE.logEvent("feature_selected",
                    java.util.Collections.singletonMap("feature", "interview_bot"));
            AnalyticsManager.INSTANCE.logFunnelStep("interview_bot_selected", null);

            Intent pdfToolsActivity = new Intent(getContext(), InterviewBotMainActivity.class);
            startActivity(pdfToolsActivity);
        });

        binding.layAiPptMaker.setOnClickListener(v -> {
            AnalyticsManager.INSTANCE.logEvent("feature_selected",
                    java.util.Collections.singletonMap("feature", "ppt_maker"));
            AnalyticsManager.INSTANCE.logFunnelStep("ppt_maker_selected", null);
            startActivity(new Intent(requireContext(), ComingSoonActivity.class));
//            startActivity(new Intent(requireContext(), PptMainActivity.class));
        });

        binding.layAiResumeAnalyzer.setOnClickListener(new DoubleClickListener() {
            @Override
            public void performClick(View v) {
                AnalyticsManager.INSTANCE.logEvent("feature_selected",
                        java.util.Collections.singletonMap("feature", "resume_analyzer"));
                AnalyticsManager.INSTANCE.logFunnelStep("resume_analyzer_selected", null);
                startActivity(new Intent(requireContext(), ResumeAnalyzerActivity.class));
            }
        });

        binding.languagePicker.setOnClickListener(v -> {
            AnalyticsManager.INSTANCE.logEvent("feature_selected",
                    java.util.Collections.singletonMap("feature", "Language_Picker"));
            startActivity(new Intent(getContext(), LanguageActivity.class));
        });

        binding.layPdfSummarize.setOnClickListener(v -> {
            AnalyticsManager.INSTANCE.logEvent("feature_selected",
                    java.util.Collections.singletonMap("feature", "PDF_Summarizer"));
            AnalyticsManager.INSTANCE.logFunnelStep("Summarizer_Selected", null);
            startActivity(new Intent(requireContext(), PDFSummarizerActivity.class));
        });
        binding.cdOcrWithPdf.setOnClickListener(v -> {
            AnalyticsManager.INSTANCE.logEvent("feature_selected",
                    java.util.Collections.singletonMap("feature", "ocr_pdf"));
            AnalyticsManager.INSTANCE.logFunnelStep("ocr_pdf_selected", null);
            checkCameraPermissionAndOpenCamera();
        });
        return view;
    }

    // Check camera permission and open camera if granted
    private void checkCameraPermissionAndOpenCamera() {
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.CAMERA)
                == PackageManager.PERMISSION_GRANTED) {
            openCamera();
        } else {
            requestPermissionLauncher.launch(Manifest.permission.CAMERA);
        }
    }

    // Create file and launch camera
    private void openCamera() {
        try {
            // Step 1: Create a file to save the full-resolution image
            photoFile = OcrImageUtils.createImageFile(requireContext());

            // Step 2: Get a content URI using FileProvider
            photoUri = FileProvider.getUriForFile(
                    requireContext(),
                    requireContext().getPackageName() + ".provider",
                    photoFile
            );

            // Step 3: Launch the camera with the output URI
            Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoUri);
            cameraLauncher.launch(cameraIntent);

        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(requireContext(), "Failed to create file for camera", Toast.LENGTH_SHORT).show();
        }
    }


    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}