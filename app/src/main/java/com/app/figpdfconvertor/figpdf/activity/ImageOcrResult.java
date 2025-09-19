package com.app.figpdfconvertor.figpdf.activity;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.ContentValues;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.pdf.PdfDocument;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.provider.MediaStore;
import android.text.method.KeyListener;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.RelativeLayout;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.text.HtmlCompat;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.app.figpdfconvertor.figpdf.R;
import com.app.figpdfconvertor.figpdf.ads.AdManagerInter;
import com.app.figpdfconvertor.figpdf.api.ApiClient;
import com.app.figpdfconvertor.figpdf.api.ApiService;
import com.app.figpdfconvertor.figpdf.databinding.ActivityImageOcrResultBinding;
import com.app.figpdfconvertor.figpdf.model.ocr.GetResponse;
import com.app.figpdfconvertor.figpdf.model.ocr.OcrResponse;
import com.app.figpdfconvertor.figpdf.preferences.AppHelper;
import com.app.figpdfconvertor.figpdf.utils.MyUtils;
import com.google.gson.Gson;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.concurrent.TimeUnit;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class ImageOcrResult extends AppCompatActivity {

    private ActivityImageOcrResultBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        MyUtils.fullScreenLightStatusBar(this);
        // Setup binding
        binding = ActivityImageOcrResultBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        ViewCompat.setOnApplyWindowInsetsListener(binding.main, (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        setupBackPressed();

        //copy button click event
        binding.copyButton.setOnClickListener(v -> {
            String textToCopy = binding.ocrResultEditText.getText().toString().trim();
            if (!textToCopy.isEmpty()) {
                ClipboardManager clipboard = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
                ClipData clip = ClipData.newPlainText("OCR Text", textToCopy);
                clipboard.setPrimaryClip(clip);

                Toast.makeText(this, "Copied to clipboard", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "No text to copy", Toast.LENGTH_SHORT).show();
            }
        });

        // editable text and its click events
        // inside onCreate

        // Save original KeyListener BEFORE disabling
        final KeyListener[] originalKeyListener = new KeyListener[1];
        originalKeyListener[0] = binding.ocrResultEditText.getKeyListener();

        // Make EditText non-editable by default
        binding.ocrResultEditText.setFocusable(false);
        binding.ocrResultEditText.setFocusableInTouchMode(false);
        binding.ocrResultEditText.setCursorVisible(false);
        binding.ocrResultEditText.setKeyListener(null);

        // Edit button click → enable editing
        binding.editButton.setOnClickListener(v -> {
            binding.ocrResultEditText.setFocusable(true);
            binding.ocrResultEditText.setFocusableInTouchMode(true);
            binding.ocrResultEditText.setCursorVisible(true);
            binding.ocrResultEditText.setKeyListener(originalKeyListener[0]); // restore key listener
            binding.ocrResultEditText.requestFocus();

            // Show keyboard
            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.showSoftInput(binding.ocrResultEditText, InputMethodManager.SHOW_IMPLICIT);

            // Show confirm and "editing..." text
            binding.editButton.setVisibility(View.GONE);
           // binding.scanResult.setVisibility(View.GONE);
            binding.confirmTextButton.setVisibility(View.VISIBLE);
         //   binding.editText.setVisibility(View.VISIBLE);
        });

        // Confirm button click → disable editing
        binding.confirmTextButton.setOnClickListener(v -> {
            binding.ocrResultEditText.setFocusable(false);
            binding.ocrResultEditText.setFocusableInTouchMode(false);
            binding.ocrResultEditText.setCursorVisible(false);
            binding.ocrResultEditText.setKeyListener(null);

            // Hide confirm and "editing..." text
            binding.editButton.setVisibility(View.VISIBLE);
         //   binding.scanResult.setVisibility(View.VISIBLE);
            binding.confirmTextButton.setVisibility(View.GONE);
         //   binding.editText.setVisibility(View.GONE);

            // Hide keyboard
            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(binding.ocrResultEditText.getWindowToken(), 0);
        });

        // Back button click
        binding.backButtonToMainActivity.setOnClickListener(view -> {
            if (binding.responseProcessOverlay.getVisibility() == View.VISIBLE) {
                // Show exit dialog only if not already showing
                if (exitDialog == null || !exitDialog.isShowing()) {
                    showExitDialog(ImageOcrResult.this);
                }
            } else {
                finish();
            }
        });

        // Retry button click (in case of failure)
        binding.txtTryAgain.setOnClickListener(v -> finish());

        // Get image path from intent
        String imagePath = getIntent().getStringExtra("image_path");
        if (imagePath != null) {
            File photoFile = new File(imagePath);
            if (photoFile.exists()) {
                // Show busy overlay
                binding.responseProcessOverlay.setVisibility(View.VISIBLE);
                binding.textNotFound.setVisibility(View.GONE);

                // Start uploading the image
                uploadImageToApi(photoFile);
            } else {
                Toast.makeText(this, "Image file not found", Toast.LENGTH_SHORT).show();
            }
        }

        //download pdf click event
        binding.downloadPDF.setOnClickListener(v ->
                {
                    if (AppHelper.getShowInterOcrDownload()) {
                        AdManagerInter.renderInterAdFixed(this, () -> {
                            createPdfFromText();
                        });
                    } else {
                        createPdfFromText();
                    }
                }

        );

    }

    private void createPdfFromText() {
        String text = binding.ocrResultEditText.getText().toString().trim();

        if (text.isEmpty()) {
            Toast.makeText(this, "No text to save!", Toast.LENGTH_SHORT).show();
            return;
        }

        // Create a new document
        PdfDocument pdfDocument = new PdfDocument();
        PdfDocument.PageInfo pageInfo = new PdfDocument.PageInfo.Builder(595, 842, 1).create();
        PdfDocument.Page page = pdfDocument.startPage(pageInfo);

        Canvas canvas = page.getCanvas();
        Paint paint = new Paint();
        paint.setTextSize(14);

        int x = 40;
        int y = 50;

        for (String line : text.split("\n")) {
            canvas.drawText(line, x, y, paint);
            y += paint.descent() - paint.ascent();
        }

        pdfDocument.finishPage(page);

        // ✅ Unique filename
        String fileName = "OcrResult_" + System.currentTimeMillis() + ".pdf";

       /* if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q) {
            // 👉 Android 10+ → Use MediaStore (publicly visible)
            ContentValues values = new ContentValues();
            values.put(MediaStore.MediaColumns.DISPLAY_NAME, fileName);
            values.put(MediaStore.MediaColumns.MIME_TYPE, "application/pdf");
            values.put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS + "/FIG AI");

            Uri uri = getContentResolver().insert(MediaStore.Files.getContentUri("external"), values);

            try {
                OutputStream outputStream = getContentResolver().openOutputStream(uri);
                pdfDocument.writeTo(outputStream);
                outputStream.close();
                Toast.makeText(this, "PDF saved in Downloads/FIG AI", Toast.LENGTH_LONG).show();
            } catch (Exception e) {
                e.printStackTrace();
                Toast.makeText(this, "Error saving PDF!", Toast.LENGTH_SHORT).show();
            }

        } else {
            // 👉 Android 9 and below → write directly
            File downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
            File figAiDir = new File(downloadsDir, "FIG AI");
            if (!figAiDir.exists()) figAiDir.mkdirs();

            File file = new File(figAiDir, fileName);

            try {
                pdfDocument.writeTo(new FileOutputStream(file));
                Toast.makeText(this, "PDF saved in: " + file.getAbsolutePath(), Toast.LENGTH_LONG).show();
            } catch (IOException e) {
                e.printStackTrace();
                Toast.makeText(this, "Error saving PDF!", Toast.LENGTH_SHORT).show();
            }
        }*/
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q) {
            // 👉 Android 10+ → Use MediaStore (Documents folder)
            ContentValues values = new ContentValues();
            values.put(MediaStore.MediaColumns.DISPLAY_NAME, fileName);
            values.put(MediaStore.MediaColumns.MIME_TYPE, "application/pdf");
            values.put(MediaStore.MediaColumns.RELATIVE_PATH,
                    Environment.DIRECTORY_DOCUMENTS + "/" + getString(R.string.app_name) + "/OCR with PDF");

            Uri uri = getContentResolver().insert(MediaStore.Files.getContentUri("external"), values);

            try {
                OutputStream outputStream = getContentResolver().openOutputStream(uri);
                pdfDocument.writeTo(outputStream);
                outputStream.close();
                Toast.makeText(this, "PDF saved in Documents/" + getString(R.string.app_name) + "/OCR with PDF", Toast.LENGTH_LONG).show();
            } catch (Exception e) {
                e.printStackTrace();
                Toast.makeText(this, "Error saving PDF!", Toast.LENGTH_SHORT).show();
            }

        } else {
            // 👉 Android 9 and below → write directly
            File documentsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS);
            File appDir = new File(documentsDir, getString(R.string.app_name) + "/OCR with PDF");
            if (!appDir.exists()) appDir.mkdirs();

            File file = new File(appDir, fileName);

            try {
                pdfDocument.writeTo(new FileOutputStream(file));
                Toast.makeText(this, "PDF saved in: " + file.getAbsolutePath(), Toast.LENGTH_LONG).show();
            } catch (IOException e) {
                e.printStackTrace();
                Toast.makeText(this, "Error saving PDF!", Toast.LENGTH_SHORT).show();
            }
        }
        pdfDocument.close();

        finish();
    }


    private Call<OcrResponse> uploadCall;
    private Call<GetResponse> pollCall;
    private void uploadImageToApi(File photoFile) {
        RequestBody requestFile = RequestBody.create(photoFile, MediaType.parse("image/*"));
        MultipartBody.Part filePart = MultipartBody.Part.createFormData("file", photoFile.getName(), requestFile);

        uploadCall = ApiClient.INSTANCE.getApiService().uploadOcrImage(filePart, 1);
        uploadCall.enqueue(new Callback<OcrResponse>() {
            @Override
            public void onResponse(Call<OcrResponse> call, Response<OcrResponse> response) {
                if (isFinishing() || binding == null) return;

                if (response.isSuccessful() && response.body() != null) {
                    String jobId = response.body().getJobId();
                    Log.d("API", "Image uploaded. Job ID: " + jobId);
                    pollJobStatus(ApiClient.INSTANCE.getApiService(), jobId);
                } else {
                    binding.responseProcessOverlay.setVisibility(View.GONE);
                    if (exitDialog != null && exitDialog.isShowing()) exitDialog.dismiss();
                    binding.textNotFound.setVisibility(View.VISIBLE);
                    try {
                        Log.e("API_ERROR", "Upload failed: " + response.errorBody().string());
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    Toast.makeText(ImageOcrResult.this, "Failed to upload image", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<OcrResponse> call, Throwable t) {
                if (isFinishing() || binding == null || call.isCanceled()) return;

                binding.responseProcessOverlay.setVisibility(View.GONE);
                if (exitDialog != null && exitDialog.isShowing()) exitDialog.dismiss();
                binding.textNotFound.setVisibility(View.VISIBLE);
                Log.e("API_ERROR", "Upload error", t);
                Toast.makeText(ImageOcrResult.this, "Upload error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }


    private void pollJobStatus(ApiService apiService, String jobId) {
        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            pollCall = apiService.getJobStatus(jobId);
            pollCall.enqueue(new Callback<GetResponse>() {
                @Override
                public void onResponse(Call<GetResponse> call, Response<GetResponse> response) {
                    if (isFinishing() || binding == null) return;

                    if (response.isSuccessful() && response.body() != null) {
                        GetResponse jobStatus = response.body();
                        Log.d("API_DEBUG_JSON", new Gson().toJson(jobStatus));

                        switch (jobStatus.getStatus()) {
                            case "queued":
                            case "processing":
                                pollJobStatus(apiService, jobStatus.getJobId());
                                break;

                            case "done":
                                binding.responseProcessOverlay.setVisibility(View.GONE);
                                if (exitDialog != null && exitDialog.isShowing()) exitDialog.dismiss();

                                if (jobStatus.getResult() != null) {
                                    String rawHtml = jobStatus.getResult().getText();
                                    if (rawHtml != null && !rawHtml.trim().isEmpty()) {
                                        String decodedText = HtmlCompat.fromHtml(rawHtml,
                                                HtmlCompat.FROM_HTML_MODE_LEGACY).toString();

                                        if (!decodedText.trim().isEmpty()) {
                                            binding.ocrResultEditText.setText(decodedText);
                                        } else {
                                            binding.textNotFound.setVisibility(View.VISIBLE);
                                            Toast.makeText(ImageOcrResult.this, "No text detected in image", Toast.LENGTH_SHORT).show();
                                        }
                                    } else {
                                        binding.textNotFound.setVisibility(View.VISIBLE);
                                        Toast.makeText(ImageOcrResult.this, "No text detected in image", Toast.LENGTH_SHORT).show();
                                    }
                                } else {
                                    binding.textNotFound.setVisibility(View.VISIBLE);
                                    Toast.makeText(ImageOcrResult.this, "No text detected in image", Toast.LENGTH_SHORT).show();
                                }
                                break;

                            default:
                                binding.responseProcessOverlay.setVisibility(View.GONE);
                                if (exitDialog != null && exitDialog.isShowing()) exitDialog.dismiss();
                                Toast.makeText(ImageOcrResult.this, "Unknown job status", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        if (isFinishing() || binding == null) return;
                        binding.responseProcessOverlay.setVisibility(View.GONE);
                        if (exitDialog != null && exitDialog.isShowing()) exitDialog.dismiss();
                        binding.textNotFound.setVisibility(View.VISIBLE);
                    }
                }

                @Override
                public void onFailure(Call<GetResponse> call, Throwable t) {
                    if (isFinishing() || binding == null || call.isCanceled()) return;

                    binding.responseProcessOverlay.setVisibility(View.GONE);
                    if (exitDialog != null && exitDialog.isShowing()) exitDialog.dismiss();
                    binding.textNotFound.setVisibility(View.VISIBLE);
                    Log.e("API_ERROR", "Polling error", t);
                    Toast.makeText(ImageOcrResult.this, "Polling error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        }, 2000);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        binding = null;
    }

    private void setupBackPressed() {
        OnBackPressedCallback callback = new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                if (binding.responseProcessOverlay.getVisibility() == View.VISIBLE) {
                    // Show exit dialog only if not already showing
                    if (exitDialog == null || !exitDialog.isShowing()) {
                        showExitDialog(ImageOcrResult.this);
                    }
                } else {
                    finish();
                }


            }
        };
        getOnBackPressedDispatcher().addCallback(this, callback);
    }
    private AlertDialog exitDialog;
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
            finish();
            exitDialog.dismiss();
        });

        exitDialog.show();
    }
}