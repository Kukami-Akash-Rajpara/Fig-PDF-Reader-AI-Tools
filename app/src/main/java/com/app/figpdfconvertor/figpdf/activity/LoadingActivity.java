package com.app.figpdfconvertor.figpdf.activity;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.app.figpdfconvertor.figpdf.BuildConfig;
import com.app.figpdfconvertor.figpdf.R;
import com.app.figpdfconvertor.figpdf.api.ApiClient;
import com.app.figpdfconvertor.figpdf.databinding.ActivityLoadingBinding;
import com.app.figpdfconvertor.figpdf.model.PdfSummarizer.AnalyzeResponse;
import com.app.figpdfconvertor.figpdf.model.PdfSummarizer.UploadResponse;
import com.app.figpdfconvertor.figpdf.utils.DoubleClickListener;
import com.app.figpdfconvertor.figpdf.utils.MyUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class LoadingActivity extends BaseActivity {
    private ActivityLoadingBinding binding;
    private String pdfUriString;
    private Call<UploadResponse> callUploadPdf;
    private Call<AnalyzeResponse> callAnalyzeResponse;
    private boolean isProcessing = false;
    private AlertDialog exitDialog = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        MyUtils.fullScreenLightStatusBar(this);
        binding = ActivityLoadingBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        ViewCompat.setOnApplyWindowInsetsListener(binding.main, (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        setupBackPressed();

        pdfUriString = getIntent().getStringExtra("pdf_uri");
        Log.d("TAG", "pdfUriString: " + pdfUriString);
        if (pdfUriString != null) {
            uploadPdf(Uri.parse(pdfUriString));
        } else {
            Toast.makeText(this, "No PDF selected", Toast.LENGTH_SHORT).show();
            finish();
        }

        binding.txtTryAgain.setOnClickListener(new DoubleClickListener() {
            @Override
            public void performClick(View v) {
                finish();
            }
        });
    }

    private void uploadPdf(Uri pdfUri) {
        isProcessing = true;
        try {
            File pdfFile = new File(getCacheDir(), "upload.pdf");

            try (InputStream inputStream = getContentResolver().openInputStream(pdfUri);
                 OutputStream outputStream = new FileOutputStream(pdfFile)) {
                byte[] buffer = new byte[1024];
                int len;
                while ((len = inputStream.read(buffer)) > 0) {
                    outputStream.write(buffer, 0, len);
                }
            }

            RequestBody requestFile = RequestBody.create(pdfFile, MediaType.parse("application/pdf"));
            MultipartBody.Part body = MultipartBody.Part.createFormData("file", pdfFile.getName(), requestFile);

            callUploadPdf = ApiClient.INSTANCE.getApiService().uploadPdf(BuildConfig.VERSION_CODE, body);
            callUploadPdf.enqueue(new Callback<>() {
                @Override
                public void onResponse(@NonNull Call<UploadResponse> call, @NonNull Response<UploadResponse> response) {
                    Log.d("Akash", "onResponse :-> " + response.body());
                    if (response.isSuccessful() && response.body() != null) {
                        String sessionId = response.body().getSessionId();
                        callAnalyzeDocument(sessionId, "en");
                    } else {
                        isProcessing = false;
                        dismissExitDialogIfShowing();
                        Log.d("Akash", "Upload failed :-> " + response.code());
                        binding.layLoading.setVisibility(View.GONE);
                        binding.cvTryAgain.setVisibility(View.VISIBLE);
                    }
                }

                @Override
                public void onFailure(@NonNull Call<UploadResponse> call, @NonNull Throwable t) {
                    isProcessing = false;
                    dismissExitDialogIfShowing();
                    Log.d("Akash", "onFailure :-> " + t.getMessage());
                }
            });
        } catch (Exception e) {
            isProcessing = false;
            dismissExitDialogIfShowing();
            Log.d("Akash", "Exception :-> " + e.getMessage());
        }
    }

    private void callAnalyzeDocument(String sessionId, String targetLang) {
        callAnalyzeResponse = ApiClient.INSTANCE.getApiService().analyzeDocument(BuildConfig.VERSION_CODE, sessionId, targetLang);
        callAnalyzeResponse.enqueue(new Callback<>() {
            @Override
            public void onResponse(@NonNull Call<AnalyzeResponse> call, @NonNull Response<AnalyzeResponse> response) {
                Log.d("Akash", "onResponse ::-> " + response.body());
                isProcessing = false; //  mark finished
                dismissExitDialogIfShowing(); //  dismiss dialog if visible
                if (response.isSuccessful() && response.body() != null) {
                    AnalyzeResponse analyzeRes = response.body();

                    // Open PdfSummaryActivity
                    Intent intent = new Intent(LoadingActivity.this, PdfSummaryActivity.class);
                    intent.putExtra("pdf_uri", pdfUriString);
                    intent.putExtra("summary", analyzeRes.getSummary());
                    intent.putStringArrayListExtra("questions", new ArrayList<>(analyzeRes.getQuestions()));
                    intent.putExtra("sessionId", analyzeRes.getSessionId());
                    startActivity(intent);
                    finish();
                } else {
                    Log.d("Akash", "response code ::-> " + response.code());
                }
            }

            @Override
            public void onFailure(@NonNull Call<AnalyzeResponse> call, @NonNull Throwable t) {
                isProcessing = false;
                dismissExitDialogIfShowing();
                Log.d("Akash", "onFailure ::-> " + t.getMessage());
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (callUploadPdf != null && !callUploadPdf.isCanceled()) {
            callUploadPdf.cancel();
        }
        if (callAnalyzeResponse != null && !callAnalyzeResponse.isCanceled()) {
            callAnalyzeResponse.cancel();
        }
    }

    private void setupBackPressed() {
        OnBackPressedCallback callback = new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                if (isProcessing) {
                    showExitDialog(LoadingActivity.this);
                } else {
                    finish();
                }
            }
        };
        getOnBackPressedDispatcher().addCallback(this, callback);
    }

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
            if (callUploadPdf != null && !callUploadPdf.isCanceled()) {
                callUploadPdf.cancel();
            }
            if (callAnalyzeResponse != null && !callAnalyzeResponse.isCanceled()) {
                callAnalyzeResponse.cancel();
            }
            finish();
            exitDialog.dismiss();
        });

        exitDialog.show();
    }
    private void dismissExitDialogIfShowing() {
        if (exitDialog != null && exitDialog.isShowing()) {
            exitDialog.dismiss();
        }
    }
}