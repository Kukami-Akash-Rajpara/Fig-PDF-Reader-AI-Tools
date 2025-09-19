package com.app.figpdfconvertor.figpdf.activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.app.figpdfconvertor.figpdf.ads.AdEventListener;
import com.app.figpdfconvertor.figpdf.ads.AdManagerInter;
import com.app.figpdfconvertor.figpdf.databinding.ActivityResumeAnalyzerBinding;
import com.app.figpdfconvertor.figpdf.preferences.AppHelper;
import com.app.figpdfconvertor.figpdf.utils.DoubleClickListener;
import com.app.figpdfconvertor.figpdf.utils.MyUtils;

public class ResumeAnalyzerActivity extends AppCompatActivity {

    private ActivityResumeAnalyzerBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        MyUtils.fullScreenLightStatusBar(this);
        binding = ActivityResumeAnalyzerBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        ViewCompat.setOnApplyWindowInsetsListener(binding.main, (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        setupBackPressed();

        binding.cvHiring.setOnClickListener(new DoubleClickListener() {
            @Override
            public void performClick(View v) {
                if (AppHelper.getShowInterAnalyzerHiringSubmit()) {
                    AdManagerInter.renderInterAdFixed(ResumeAnalyzerActivity.this, new AdEventListener() {
                        @Override
                        public void onAdFinish() {
                            Intent intent = new Intent(ResumeAnalyzerActivity.this, RAHiringActivity.class);
                            intent.putExtra("isJobDes", true);
                            startActivity(intent);
                        }
                    });
                } else {
                    Intent intent = new Intent(ResumeAnalyzerActivity.this, RAHiringActivity.class);
                    intent.putExtra("isJobDes", true);
                    startActivity(intent);
                }

            }
        });

        binding.cvCandidates.setOnClickListener(new DoubleClickListener() {
            @Override
            public void performClick(View v) {

                Intent intent = new Intent(ResumeAnalyzerActivity.this, RAHiringActivity.class);
                intent.putExtra("isJobDes", false);
                startActivity(intent);
            }
        });
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