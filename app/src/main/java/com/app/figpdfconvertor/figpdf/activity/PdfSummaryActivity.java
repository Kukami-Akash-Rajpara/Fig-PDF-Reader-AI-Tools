package com.app.figpdfconvertor.figpdf.activity;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.core.graphics.Insets;
import androidx.core.text.HtmlCompat;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.app.figpdfconvertor.figpdf.BuildConfig;
import com.app.figpdfconvertor.figpdf.R;
import com.app.figpdfconvertor.figpdf.adapter.ChatAdapter;
import com.app.figpdfconvertor.figpdf.adapter.SuggestedQuestionAdapter;
import com.app.figpdfconvertor.figpdf.api.ApiClient;
import com.app.figpdfconvertor.figpdf.databinding.ActivityPdfSummaryBinding;
import com.app.figpdfconvertor.figpdf.model.PdfSummarizer.AnalyzeResponse;
import com.app.figpdfconvertor.figpdf.model.PdfSummarizer.AnswerResponse;
import com.app.figpdfconvertor.figpdf.model.PdfSummarizer.QAItem;
import com.app.figpdfconvertor.figpdf.model.PdfSummarizer.UploadResponse;
import com.app.figpdfconvertor.figpdf.utils.DoubleClickListener;
import com.app.figpdfconvertor.figpdf.utils.MyUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class PdfSummaryActivity extends BaseActivity {

    private ActivityPdfSummaryBinding binding;
    private ChatAdapter chatAdapter;
    private final List<QAItem> chatList = new ArrayList<>();

    private String pdfUriString;
    private String sessionIdMain;
    private int appVersion;
    private Call<UploadResponse> callUploadPdf;
    private Call<AnalyzeResponse> callAnalyzeResponse;
    private boolean isProcessing = false;
    private AlertDialog exitDialog = null;
    private String summaryText = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        MyUtils.fullScreenLightStatusBar(this);
        binding = ActivityPdfSummaryBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Handle insets
        ViewCompat.setOnApplyWindowInsetsListener(binding.main, (v, insets) -> {
            Insets sys = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(sys.left, sys.top, sys.right, sys.bottom);
            return insets;
        });
        ViewCompat.setOnApplyWindowInsetsListener(binding.bottomInput, (v, insets) -> {
            Insets ime = insets.getInsets(WindowInsetsCompat.Type.ime());
            Insets sys = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(v.getPaddingLeft(), v.getPaddingTop(), v.getPaddingRight(),
                    Math.max(ime.bottom, sys.bottom));
            return insets;
        });

        setupBackPressed();

        pdfUriString = getIntent().getStringExtra("pdf_uri");
        if (pdfUriString != null) {
            uploadPdf(Uri.parse(pdfUriString));
            showPdfDetails(Uri.parse(pdfUriString));
        } else {
            Toast.makeText(this, "No PDF selected", Toast.LENGTH_SHORT).show();
            finish();
        }

        appVersion = BuildConfig.VERSION_CODE;

        // Setup Chat RecyclerView
        binding.rvQuestionAns.setLayoutManager(new LinearLayoutManager(this));
        chatAdapter = new ChatAdapter(chatList);
        binding.rvQuestionAns.setAdapter(chatAdapter);

        // ✅ Always set click listeners here
        binding.btnSend.setOnClickListener(v -> {
            String q = binding.etQuestion.getText().toString().trim();
            if (q.isEmpty()) return;

            if (isProcessing) {
                Toast.makeText(this, "Please wait... Processing your PDF", Toast.LENGTH_SHORT).show();
            } else {
                sendUserQuestion(q);
                binding.etQuestion.setText("");
            }
        });

        binding.layViewFile.setOnClickListener(new DoubleClickListener() {
            @Override
            public void performClick(View v) {
                if (pdfUriString != null) {
                    Intent intent = new Intent(PdfSummaryActivity.this, ViewFileActivity.class);
                    intent.putExtra("pdf_uri", pdfUriString);
                    startActivity(intent);
                } else {
                    Toast.makeText(PdfSummaryActivity.this, "No PDF found", Toast.LENGTH_SHORT).show();
                }
            }
        });

        binding.imgDownload.setOnClickListener(new DoubleClickListener() {
            @Override
            public void performClick(View v) {
                if (isProcessing) {
                    Toast.makeText(PdfSummaryActivity.this, "Please wait... summary is not ready yet", Toast.LENGTH_SHORT).show();
                    return;
                }
                if (summaryText.isEmpty()) {
                    Toast.makeText(PdfSummaryActivity.this, "No summary to download", Toast.LENGTH_SHORT).show();
                    return;
                }
                saveSummaryToFile(summaryText);
            }
        });

        binding.imgCopy.setOnClickListener(new DoubleClickListener() {
            @Override
            public void performClick(View v) {
                if (isProcessing) {
                    Toast.makeText(PdfSummaryActivity.this, "Please wait... summary is not ready yet", Toast.LENGTH_SHORT).show();
                    return;
                }
                if (summaryText.isEmpty()) {
                    Toast.makeText(PdfSummaryActivity.this, "No summary to copy", Toast.LENGTH_SHORT).show();
                    return;
                }
                ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
                ClipData clip = ClipData.newPlainText("Summary", summaryText);
                clipboard.setPrimaryClip(clip);
                Toast.makeText(PdfSummaryActivity.this, "Summary copied to clipboard", Toast.LENGTH_SHORT).show();
            }
        });
        binding.txtTryAgain.setOnClickListener(new DoubleClickListener() {
            @Override
            public void performClick(View v) {
                finish();
            }
        });
    }

    private void showPdfDetails(Uri uri) {
        String pdfName = MyUtils.getFileNameFromUri(this, uri);
        binding.txtPdfName.setText(pdfName != null ? pdfName : "Selected PDF");
        binding.txtPdfName.setSelected(true);
    }

    private void sendUserQuestion(String question) {
        chatList.add(new QAItem(question, null, true));
        chatAdapter.notifyItemInserted(chatList.size() - 1);
        scrollToBottom();

        ApiClient.INSTANCE.getApiService().answerQuestion(sessionIdMain, appVersion, question, "en")
                .enqueue(new Callback<AnswerResponse>() {
                    @Override
                    public void onResponse(@NonNull Call<AnswerResponse> call, @NonNull Response<AnswerResponse> response) {
                        if (response.isSuccessful() && response.body() != null) {
                            chatList.add(new QAItem(null, response.body().getAnswer(), false));
                            chatAdapter.notifyItemInserted(chatList.size() - 1);
                            scrollToBottom();
                        } else {
                            Toast.makeText(PdfSummaryActivity.this, "Failed to get answer", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onFailure(@NonNull Call<AnswerResponse> call, @NonNull Throwable t) {
                        Toast.makeText(PdfSummaryActivity.this, "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void scrollToBottom() {
        binding.nestedScrollView.post(() ->
                binding.nestedScrollView.smoothScrollTo(
                        0, binding.nestedScrollView.getChildAt(0).getBottom()
                )
        );
    }

    private void setupBackPressed() {
        OnBackPressedCallback callback = new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                showExitDialog(PdfSummaryActivity.this);
            }
        };
        getOnBackPressedDispatcher().addCallback(this, callback);
        binding.backKey.setOnClickListener(view -> callback.handleOnBackPressed());
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
                    if (response.isSuccessful() && response.body() != null) {
                        callAnalyzeDocument(response.body().getSessionId(), "en");
                    } else {
                        isProcessing = false;
                        dismissExitDialogIfShowing();
                        binding.layLoading.setVisibility(View.GONE);
                        binding.cvTryAgain.setVisibility(View.VISIBLE);
                    }
                }
                @Override
                public void onFailure(@NonNull Call<UploadResponse> call, @NonNull Throwable t) {
                    isProcessing = false;
                    dismissExitDialogIfShowing();
                }
            });
        } catch (Exception e) {
            isProcessing = false;
            dismissExitDialogIfShowing();
        }
    }

    private void callAnalyzeDocument(String sessionId, String targetLang) {
        callAnalyzeResponse = ApiClient.INSTANCE.getApiService().analyzeDocument(BuildConfig.VERSION_CODE, sessionId, targetLang);
        callAnalyzeResponse.enqueue(new Callback<>() {
            @Override
            public void onResponse(@NonNull Call<AnalyzeResponse> call, @NonNull Response<AnalyzeResponse> response) {
                isProcessing = false;
                dismissExitDialogIfShowing();
                if (response.isSuccessful() && response.body() != null) {
                    AnalyzeResponse res = response.body();
                    sessionIdMain = res.getSessionId();
                    summaryText = HtmlCompat.fromHtml(res.getSummary(), HtmlCompat.FROM_HTML_MODE_LEGACY).toString();

                    binding.shimmerLayout.stopShimmer();
                    binding.shimmerLayout.setVisibility(View.GONE);
                    binding.txtSummary.setVisibility(View.VISIBLE);
                    binding.txtSummary.setText(HtmlCompat.fromHtml(res.getSummary(), HtmlCompat.FROM_HTML_MODE_LEGACY));

                    ArrayList<String> questions = new ArrayList<>(res.getQuestions());
                    if (!questions.isEmpty()) {
                        SuggestedQuestionAdapter suggestedAdapter =
                                new SuggestedQuestionAdapter(questions, q -> sendUserQuestion(q));
                        binding.rvSuggestedQA.setLayoutManager(
                                new LinearLayoutManager(PdfSummaryActivity.this, LinearLayoutManager.HORIZONTAL, false)
                        );
                        binding.rvSuggestedQA.setAdapter(suggestedAdapter);
                    }
                }
            }
            @Override
            public void onFailure(@NonNull Call<AnalyzeResponse> call, @NonNull Throwable t) {
                isProcessing = false;
                dismissExitDialogIfShowing();
            }
        });
    }

    private void saveSummaryToFile(String summaryText) {
        try {
            File documentsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS);
            String appName = getString(R.string.app_name);
            File dir = new File(documentsDir, appName + "/PDF Summarizer");
            if (!dir.exists()) dir.mkdirs();

            String fileName = "Pdf_summary_" + System.currentTimeMillis() + ".txt";
            File file = new File(dir, fileName);

            FileOutputStream fos = new FileOutputStream(file);
            fos.write(summaryText.getBytes());
            fos.close();

            MediaScannerConnection.scanFile(
                    this,
                    new String[]{file.getAbsolutePath()},
                    new String[]{"text/plain"},
                    null
            );
            Toast.makeText(this, "Summary saved successfully", Toast.LENGTH_LONG).show();
        } catch (Exception e) {
            Toast.makeText(this, "Failed to save file", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (callUploadPdf != null && !callUploadPdf.isCanceled()) callUploadPdf.cancel();
        if (callAnalyzeResponse != null && !callAnalyzeResponse.isCanceled()) callAnalyzeResponse.cancel();
    }

    private void dismissExitDialogIfShowing() {
        if (exitDialog != null && exitDialog.isShowing()) exitDialog.dismiss();
    }

    public void showExitDialog(Context context) {
        if (exitDialog != null && exitDialog.isShowing()) return;
        View dialogView = LayoutInflater.from(context).inflate(R.layout.dialog_exit, null);
        RelativeLayout cancelBtn = dialogView.findViewById(R.id.cancelButton);
        RelativeLayout okBtn = dialogView.findViewById(R.id.okButton);

        exitDialog = new AlertDialog.Builder(context)
                .setView(dialogView)
                .setCancelable(true)
                .create();
        if (exitDialog.getWindow() != null) {
            exitDialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
            int margin = (int) (context.getResources().getDisplayMetrics().widthPixels * 0.05);
            exitDialog.getWindow().setLayout(
                    context.getResources().getDisplayMetrics().widthPixels - 2 * margin,
                    RelativeLayout.LayoutParams.WRAP_CONTENT
            );
        }
        cancelBtn.setOnClickListener(v -> exitDialog.dismiss());
        okBtn.setOnClickListener(v -> {
            finish();
            exitDialog.dismiss();
        });
        exitDialog.show();
    }
}
