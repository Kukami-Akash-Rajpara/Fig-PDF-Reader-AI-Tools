package com.app.figpdfconvertor.figpdf.activity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.app.figpdfconvertor.figpdf.BuildConfig;
import com.app.figpdfconvertor.figpdf.api.ApiClient;
import com.app.figpdfconvertor.figpdf.databinding.ActivityLoadingCandidatesBinding;
import com.app.figpdfconvertor.figpdf.model.ResumeAnalyzer.ResumeAnalyzerHiring;
import com.app.figpdfconvertor.figpdf.utils.DoubleClickListener;
import com.app.figpdfconvertor.figpdf.utils.MyUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class LoadingCandidatesActivity extends AppCompatActivity {

    private ActivityLoadingCandidatesBinding binding;
    private String pdfUriString;
    private Call<ResumeAnalyzerHiring> analyzerHiringCall;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        MyUtils.fullScreenLightStatusBar(this);
        binding = ActivityLoadingCandidatesBinding.inflate(getLayoutInflater());
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
        try {
            File pdfFile = new File(getCacheDir(), "upload.pdf");

            try (InputStream inputStream = getContentResolver().openInputStream(pdfUri); OutputStream outputStream = new FileOutputStream(pdfFile)) {
                byte[] buffer = new byte[1024];
                int len;
                while ((len = inputStream.read(buffer)) > 0) {
                    outputStream.write(buffer, 0, len);
                }
            }

            RequestBody requestFile = RequestBody.create(pdfFile, MediaType.parse("application/pdf"));
            MultipartBody.Part body = MultipartBody.Part.createFormData("resume", pdfFile.getName(), requestFile);

            Log.d("Akash", "File -> " + pdfFile.getName());

            analyzerHiringCall = ApiClient.INSTANCE.getApiService().getCandidates(BuildConfig.VERSION_CODE, body);
            analyzerHiringCall.enqueue(new Callback<>() {
                @Override
                public void onResponse(@NonNull Call<ResumeAnalyzerHiring> call, @NonNull Response<ResumeAnalyzerHiring> response) {
                    Log.d("Akash", "Request URL: " + analyzerHiringCall.request().url());
                    Log.d("Akash", "onResponse :-> " + response.body());

                    if (response.isSuccessful() && response.body() != null) {
                        ResumeAnalyzerHiring result = response.body();

                        Intent intent = new Intent(LoadingCandidatesActivity.this, ResumeDetailsActivity.class);
                        intent.putExtra("resumeResponse", result); // whole object
                        startActivity(intent);
                        finish();

                    } else {
                        Log.d("Akash", "Upload failed :-> " + response.code());
                        binding.layLoading.setVisibility(View.GONE);
                        binding.cvTryAgain.setVisibility(View.VISIBLE);
                    }
                }

                @Override
                public void onFailure(@NonNull Call<ResumeAnalyzerHiring> call, @NonNull Throwable t) {
                    Log.d("Akash", "onFailure :-> " + t.getMessage());
                }
            });
        } catch (Exception e) {
            Log.d("Akash", "Exception :-> " + e.getMessage());
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (analyzerHiringCall != null && !analyzerHiringCall.isCanceled()) {
            analyzerHiringCall.cancel();
        }
    }

    private void setupBackPressed() {
        OnBackPressedCallback callback = new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                finish();
            }
        };
        getOnBackPressedDispatcher().addCallback(this, callback);
    }
}