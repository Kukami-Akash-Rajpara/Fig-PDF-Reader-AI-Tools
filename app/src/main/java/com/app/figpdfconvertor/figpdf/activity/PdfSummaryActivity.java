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
import com.app.figpdfconvertor.figpdf.model.PdfSummarizer.AnswerResponse;
import com.app.figpdfconvertor.figpdf.model.PdfSummarizer.QAItem;
import com.app.figpdfconvertor.figpdf.utils.DoubleClickListener;
import com.app.figpdfconvertor.figpdf.utils.MyUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class PdfSummaryActivity extends BaseActivity {

    private ActivityPdfSummaryBinding binding;

    private ChatAdapter chatAdapter;
    private final List<QAItem> chatList = new ArrayList<>();

    private String pdfUriString;
    private String sessionId;
    private int appVersion;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        MyUtils.fullScreenLightStatusBar(this);
        binding = ActivityPdfSummaryBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        ViewCompat.setOnApplyWindowInsetsListener(binding.main, (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        ViewCompat.setOnApplyWindowInsetsListener(binding.bottomInput, (v, insets) -> {
            Insets ime = insets.getInsets(WindowInsetsCompat.Type.ime());
            Insets sys = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            int bottom = Math.max(ime.bottom, sys.bottom);
            v.setPadding(v.getPaddingLeft(), v.getPaddingTop(), v.getPaddingRight(), bottom);
            return insets;
        });

        setupBackPressed();

        pdfUriString = getIntent().getStringExtra("pdf_uri");
        sessionId = getIntent().getStringExtra("sessionId");
        appVersion = BuildConfig.VERSION_CODE;

        showPdfDetails(Uri.parse(pdfUriString));

        // Show summary
        String summary = getIntent().getStringExtra("summary");
        binding.txtSummary.setText(HtmlCompat.fromHtml(summary, HtmlCompat.FROM_HTML_MODE_LEGACY));

        // Setup Chat RecyclerView
        binding.rvQuestionAns.setLayoutManager(new LinearLayoutManager(this));
        chatAdapter = new ChatAdapter(chatList);
        binding.rvQuestionAns.setAdapter(chatAdapter);

        // Send typed question
        binding.btnSend.setOnClickListener(v -> {
            String q = binding.etQuestion.getText().toString().trim();
            if (!q.isEmpty()) {
                sendUserQuestion(q);
                binding.etQuestion.setText("");
            }
        });


        // Suggested questions
        ArrayList<String> questions = getIntent().getStringArrayListExtra("questions");
        if (questions != null) {
            SuggestedQuestionAdapter suggestedAdapter = new SuggestedQuestionAdapter(questions, q -> {
                sendUserQuestion(q);
            });
            binding.rvSuggestedQA.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
            binding.rvSuggestedQA.setAdapter(suggestedAdapter);
        }

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
                String summaryText = HtmlCompat.fromHtml(
                        getIntent().getStringExtra("summary"),
                        HtmlCompat.FROM_HTML_MODE_LEGACY
                ).toString();

                if (summaryText.isEmpty()) {
                    Toast.makeText(PdfSummaryActivity.this, "No summary to download", Toast.LENGTH_SHORT).show();
                    return;
                }

                try {
                    // Get public Documents directory
                    File documentsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS);

                    // Create subfolder: Documents/AppName/PDF Summarizer
                    String appName = getString(R.string.app_name); // use your app name from strings.xml
                    File dir = new File(documentsDir, appName + "/PDF Summarizer");
                    if (!dir.exists()) dir.mkdirs();

                    // Create file with timestamp
                    String fileName = "Pdf_summary_" + System.currentTimeMillis() + ".txt";
                    File file = new File(dir, fileName);

                    // Write text to file
                    FileOutputStream fos = new FileOutputStream(file);
                    fos.write(summaryText.getBytes());
                    fos.close();

                    // Scan so it appears in file manager immediately
                    MediaScannerConnection.scanFile(
                            PdfSummaryActivity.this,
                            new String[]{file.getAbsolutePath()},
                            new String[]{"text/plain"},
                            (path, uri) -> Log.d("PdfSummaryActivity", "File scanned: " + path)
                    );

                    Toast.makeText(PdfSummaryActivity.this,
                            "Summary saved successfully", Toast.LENGTH_LONG).show();

                } catch (Exception e) {
                    e.printStackTrace();
                    Toast.makeText(PdfSummaryActivity.this, "Failed to save file", Toast.LENGTH_SHORT).show();
                }
            }
        });

        binding.imgCopy.setOnClickListener(new DoubleClickListener() {
            @Override
            public void performClick(View v) {
                String summaryText = HtmlCompat.fromHtml(
                        getIntent().getStringExtra("summary"),
                        HtmlCompat.FROM_HTML_MODE_LEGACY
                ).toString();

                if (summaryText.isEmpty()) {
                    Toast.makeText(PdfSummaryActivity.this, "No summary to copy", Toast.LENGTH_SHORT).show();
                    return;
                }

                ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
                ClipData clip = ClipData.newPlainText("Summary", summaryText);
                clipboard.setPrimaryClip(clip);

                /*Toast.makeText(PdfSummaryActivity.this, "Summary copied to clipboard", Toast.LENGTH_SHORT).show();*/
            }
        });
    }

    private void showPdfDetails(Uri uri) {
        // Get the PDF file name from Uri
        String pdfName = MyUtils.getFileNameFromUri(this, uri);
        binding.txtPdfName.setText(pdfName != null ? pdfName : "Selected PDF");
        binding.txtPdfName.setSelected(true);
    }

    private void sendUserQuestion(String question) {
        // Add user question to chat
        chatList.add(new QAItem(question, null, true));
        chatAdapter.notifyItemInserted(chatList.size() - 1);

        // FIX: Scroll after layout pass
        scrollToBottom();

        Log.d("Akash", "sendUserQuestion: sessionId : " + sessionId);
        Log.d("Akash", "sendUserQuestion: " + question);
        // Call API
        ApiClient.INSTANCE.getApiService().answerQuestion(sessionId, appVersion, question, "en")
                .enqueue(new Callback<AnswerResponse>() {
                    @Override
                    public void onResponse(@NonNull Call<AnswerResponse> call, @NonNull Response<AnswerResponse> response) {
                        Log.d("Akash", "sendUserQuestion : response : " + response);

                        if (response.isSuccessful() && response.body() != null) {
                            AnswerResponse res = response.body();
                            chatList.add(new QAItem(null, res.getAnswer(), false));
                            chatAdapter.notifyItemInserted(chatList.size() - 1);

                            scrollToBottom();
                        } else {
                            Toast.makeText(PdfSummaryActivity.this, "Failed to get answer", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onFailure(@NonNull Call<AnswerResponse> call, @NonNull Throwable t) {
                        Log.d("Akash", "sendUserQuestion : onFailure : " + t.getMessage());
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

        binding.backKey.setOnClickListener(view -> {
            callback.handleOnBackPressed();
        });
    }

    private AlertDialog exitDialog = null;

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

            // ✅ Add left & right margin
            int margin = (int) (context.getResources().getDisplayMetrics().widthPixels * 0.05); // 5% margin
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