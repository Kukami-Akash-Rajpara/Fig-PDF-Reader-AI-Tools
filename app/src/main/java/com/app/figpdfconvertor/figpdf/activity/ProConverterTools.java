package com.app.figpdfconvertor.figpdf.activity;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.app.figpdfconvertor.figpdf.ads.AdManagerInter;
import com.app.figpdfconvertor.figpdf.ads.AdManagerRewarded;
import com.app.figpdfconvertor.figpdf.databinding.ActivityProConverterToolsBinding;
import com.app.figpdfconvertor.figpdf.funnelss.AnalyticsManager;
import com.app.figpdfconvertor.figpdf.preferences.AppHelper;
import com.app.figpdfconvertor.figpdf.utils.DoubleClickListener;
import com.app.figpdfconvertor.figpdf.utils.MyUtils;

public class ProConverterTools extends BaseActivity {

    private ActivityResultLauncher<Intent> filePickerLauncher;
    private String selectedFileType;        // for TO_PDF
    private String selectedConversionType;  // for FROM_PDF
    private boolean isFromPdf;              // true if conversion FROM PDF

    private ActivityProConverterToolsBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        MyUtils.fullScreenLightStatusBar(this);
        binding = ActivityProConverterToolsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        ViewCompat.setOnApplyWindowInsetsListener(binding.main, (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Back button
        binding.backKey.setOnClickListener(new DoubleClickListener() {
            @Override
            public void performClick(View v) {
                getOnBackPressedDispatcher().onBackPressed();
            }
        });

        // ------------------ PDF → Other formats ------------------
        binding.cardWordfromPdf.setOnClickListener(v -> pickPdf("WORD"));
        binding.cardExcelFromPDF.setOnClickListener(v -> pickPdf("EXCEL"));
        binding.cardPPTFromPDF.setOnClickListener(v -> pickPdf("PPT"));
        binding.cardTextFromPDF.setOnClickListener(v -> pickPdf("TEXT"));
        binding.cardHTMLToPDF.setOnClickListener(v -> pickPdf("HTML"));
        binding.cardImageFromPDF.setOnClickListener(v -> pickPdf("IMAGE"));

        // ------------------ Other formats → PDF ------------------
        binding.llWordToPDF.setOnClickListener(v -> pickFileForPdf(FileViewerActivity.FILE_TYPE_WORD, "word_to_pdf"));
        binding.cardExcelToPDF.setOnClickListener(v -> pickFileForPdf(FileViewerActivity.FILE_TYPE_EXCEL, "excel_to_pdf"));
        binding.cardPPTToPDF.setOnClickListener(v -> pickFileForPdf(FileViewerActivity.FILE_TYPE_PPT, "ppt_to_pdf"));
        binding.cardTextToPDF.setOnClickListener(v -> pickFileForPdf(FileViewerActivity.FILE_TYPE_TEXT, "text_to_pdf"));
        binding.cardHTMLToPDF.setOnClickListener(v -> pickFileForPdf(FileViewerActivity.FILE_TYPE_HTML, "html_to_pdf"));

        // ------------------ Image to PDF ------------------
        binding.imageToPdfButton.setOnClickListener(v -> {
            userExplored();
            AnalyticsManager.INSTANCE.logEvent("feature_selected",
                    java.util.Collections.singletonMap("feature", "image_to_pdf"));
            AnalyticsManager.INSTANCE.logFunnelStep("image_to_pdf_selected", null);
            if (AppHelper.getShowRewardImageToPdf()) {
                AdManagerRewarded.showRewardedAd(this, () -> startActivity(new Intent(ProConverterTools.this, ImageSelection.class)));
            } else {
                // No ad → just continue, PDF flow runs anyway
                startActivity(new Intent(this, ImageSelection.class));
            }
        });

        // ------------------ Merge, Split, Compress ------------------
        binding.mergePDF.setOnClickListener(v -> {
            userExplored();
            AnalyticsManager.INSTANCE.logEvent("feature_selected",
                    java.util.Collections.singletonMap("feature", "merge_pdf"));
            AnalyticsManager.INSTANCE.logFunnelStep("merge_pdf_selected", null);
            Intent intent = new Intent(this, PdfMergeActivity.class);
            intent.putExtra("mode", "merge");
            startActivity(intent);
        });

        binding.splitPdf.setOnClickListener(v -> {
            userExplored();
            AnalyticsManager.INSTANCE.logEvent("feature_selected",
                    java.util.Collections.singletonMap("feature", "split_pdf"));
            AnalyticsManager.INSTANCE.logFunnelStep("split_pdf_selected", null);
            Intent intent = new Intent(this, PdfMergeActivity.class);
            intent.putExtra("mode", "split");
            startActivity(intent);
        });

        binding.compressPDF.setOnClickListener(v -> {
            userExplored();
            AnalyticsManager.INSTANCE.logEvent("feature_selected",
                    java.util.Collections.singletonMap("feature", "compress_pdf"));
            AnalyticsManager.INSTANCE.logFunnelStep("compress_pdf_selected", null);
            startActivity(new Intent(this, PdfMergeActivity.class));
        });

        // ------------------ Unified File Picker ------------------
        setupFilePickerLauncher();
    }

    private void setupFilePickerLauncher() {
        filePickerLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                        Uri selectedUri = result.getData().getData();
                        if (selectedUri == null) return;

                        userExplored();

                        if (isFromPdf) {
                            if ("IMAGE".equals(selectedConversionType)) {
                                // PDF → Images
                                if (AppHelper.getShowInterPdfToImage()) {
                                    AdManagerInter.renderInterAdFixed(this, () -> {
                                        Intent intent = new Intent(this, PdfToImagesActivity.class);
                                        intent.putExtra("extra_file_uri", selectedUri);
                                        startActivity(intent);
                                    });
                                } else {
                                    Intent intent = new Intent(this, PdfToImagesActivity.class);
                                    intent.putExtra("extra_file_uri", selectedUri);
                                    startActivity(intent);
                                }

                            } else {
                                // PDF → Word/Excel/PPT/Text/HTML
                                Intent intent = new Intent(this, FileViewerActivity.class);
                                intent.putExtra(FileViewerActivity.EXTRA_FILE_URI, selectedUri);
                                intent.putExtra(FileViewerActivity.EXTRA_FILE_TYPE, FileViewerActivity.FILE_TYPE_PDF);
                                intent.putExtra(FileViewerActivity.EXTRA_CONVERSION_MODE, FileViewerActivity.MODE_FROM_PDF);
                                intent.putExtra("conversion_type", selectedConversionType);
                                startActivity(intent);
                            }
                        } else {
                            // Word/Excel/PPT/Text/HTML → PDF
                            Intent intent = new Intent(this, FileViewerActivity.class);
                            intent.putExtra(FileViewerActivity.EXTRA_FILE_URI, selectedUri);
                            intent.putExtra(FileViewerActivity.EXTRA_FILE_TYPE, selectedFileType);
                            intent.putExtra(FileViewerActivity.EXTRA_CONVERSION_MODE, FileViewerActivity.MODE_TO_PDF);
                            startActivity(intent);
                        }
                    }
                }
        );
    }

    // ------------------ Helper methods ------------------

    private void pickPdf(String conversionType) {
        userExplored();
        AnalyticsManager.INSTANCE.logEvent("feature_selected",
                java.util.Collections.singletonMap("feature", "pdf_to_" + conversionType.toLowerCase()));
        AnalyticsManager.INSTANCE.logFunnelStep("pdf_to_" + conversionType.toLowerCase() + "_selected", null);

        isFromPdf = true;
        selectedConversionType = conversionType;

        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("application/pdf");
        filePickerLauncher.launch(intent);
    }

    private void pickFileForPdf(String fileType, String featureName) {
        userExplored();
        AnalyticsManager.INSTANCE.logEvent("feature_selected",
                java.util.Collections.singletonMap("feature", featureName));
        AnalyticsManager.INSTANCE.logFunnelStep(featureName + "_selected", null);

        isFromPdf = false;
        selectedFileType = fileType;

        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);

        switch (fileType) {
            case FileViewerActivity.FILE_TYPE_WORD:
                intent.setType("*/*");
                intent.putExtra(Intent.EXTRA_MIME_TYPES, new String[]{
                        "application/msword",
                        "application/vnd.openxmlformats-officedocument.wordprocessingml.document"
                });
                break;
            case FileViewerActivity.FILE_TYPE_EXCEL:
                intent.setType("*/*");
                intent.putExtra(Intent.EXTRA_MIME_TYPES, new String[]{
                        "application/vnd.ms-excel",
                        "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"
                });
                break;
            case FileViewerActivity.FILE_TYPE_PPT:
                intent.setType("*/*");
                intent.putExtra(Intent.EXTRA_MIME_TYPES, new String[]{
                        "application/vnd.ms-powerpoint",
                        "application/vnd.openxmlformats-officedocument.presentationml.presentation"
                });
                break;
            case FileViewerActivity.FILE_TYPE_TEXT:
                intent.setType("text/plain");
                break;
            case FileViewerActivity.FILE_TYPE_HTML:
                intent.setType("text/html");
                break;
            default:
                intent.setType("*/*");
                break;
        }

        filePickerLauncher.launch(intent);
    }
}
