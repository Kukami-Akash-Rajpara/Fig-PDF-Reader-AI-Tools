package com.app.figpdfconvertor.figpdf.activity;

import android.os.Bundle;

import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.app.figpdfconvertor.figpdf.databinding.ActivityUnderMaintenanceBinding;
import com.app.figpdfconvertor.figpdf.utils.MyUtils;

public class UnderMaintenanceActivity extends BaseActivity {

    private ActivityUnderMaintenanceBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        MyUtils.fullScreenLightStatusBar(this);
        binding = ActivityUnderMaintenanceBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        ViewCompat.setOnApplyWindowInsetsListener(binding.main, (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        binding.gotItButton.setOnClickListener(v -> {
            // Handle click
            finishAffinity();
            System.exit(0);
        });
    }
}