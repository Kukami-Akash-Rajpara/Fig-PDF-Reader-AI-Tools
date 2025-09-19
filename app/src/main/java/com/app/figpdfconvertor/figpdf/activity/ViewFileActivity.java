package com.app.figpdfconvertor.figpdf.activity;

import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.app.figpdfconvertor.figpdf.databinding.ActivityViewFileBinding;
import com.app.figpdfconvertor.figpdf.utils.MyUtils;

import kotlin.Unit;

public class ViewFileActivity extends BaseActivity {

    private ActivityViewFileBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        MyUtils.fullScreenLightStatusBar(this);
        binding = ActivityViewFileBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        ViewCompat.setOnApplyWindowInsetsListener(binding.main, (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        setupBackPressed();

        // Get PDF Uri from intent
        String pdfUriString = getIntent().getStringExtra("pdf_uri");
        Log.d("TAG", "pdfUriString: " + pdfUriString);
        if (pdfUriString != null) {
            Uri pdfUri = Uri.parse(pdfUriString);
            binding.pdfViewer.onReady(viewer -> {
                viewer.load(pdfUri);
                return Unit.INSTANCE;
            });
        } else {
            Toast.makeText(this, "No PDF path received", Toast.LENGTH_SHORT).show();
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

        binding.backKey.setOnClickListener(view -> {
            callback.handleOnBackPressed();
        });
    }
}