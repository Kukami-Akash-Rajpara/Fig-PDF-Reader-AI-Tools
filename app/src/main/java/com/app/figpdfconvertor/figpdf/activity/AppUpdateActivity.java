package com.app.figpdfconvertor.figpdf.activity;

import android.os.Bundle;

import androidx.core.graphics.Insets;
import androidx.core.text.HtmlCompat;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.app.figpdfconvertor.figpdf.R;
import com.app.figpdfconvertor.figpdf.databinding.ActivityAppUpdateBinding;
import com.app.figpdfconvertor.figpdf.utils.MyUtils;

public class AppUpdateActivity extends BaseActivity {

    private ActivityAppUpdateBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        MyUtils.fullScreenLightStatusBar(this);
        binding = ActivityAppUpdateBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Apply window insets
        ViewCompat.setOnApplyWindowInsetsListener(binding.main, (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        binding.updateMessage.setText(
                HtmlCompat.fromHtml(
                        getString(R.string.update_app_text),
                        HtmlCompat.FROM_HTML_MODE_LEGACY
                )
        );
    }
}