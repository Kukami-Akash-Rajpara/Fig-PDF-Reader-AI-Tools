package com.app.figpdfconvertor.figpdf.activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.text.HtmlCompat;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.app.figpdfconvertor.figpdf.R;
import com.app.figpdfconvertor.figpdf.adapter.KeywordAdapter;
import com.app.figpdfconvertor.figpdf.databinding.ActivityResumeDetailsBinding;
import com.app.figpdfconvertor.figpdf.model.ResumeAnalyzer.ResumeAnalyzerHiring;
import com.app.figpdfconvertor.figpdf.utils.MyUtils;
import com.google.android.flexbox.FlexDirection;
import com.google.android.flexbox.FlexboxLayoutManager;
import com.google.android.flexbox.JustifyContent;

import java.util.List;

public class ResumeDetailsActivity extends BaseActivity {

    private ActivityResumeDetailsBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        MyUtils.fullScreenLightStatusBar(this);
        binding = ActivityResumeDetailsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        ViewCompat.setOnApplyWindowInsetsListener(binding.main, (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        setupBackPressed();

        Intent intent = getIntent();
        if (intent != null) {
            ResumeAnalyzerHiring result = intent.getParcelableExtra("resumeResponse");

            int overallScore = result.getOverallScore();
            int technicalSkillsScore = result.getTechnicalSkillsScore();
            int requirementsScore = result.getRequirementsScore();
            int keywordsScore = result.getKeywordsScore();
            String analysis = result.getAnalysis();
            String resumeFeedback = result.getResumeFeedback();
            String hiringOverview = result.getHiringOverview();

            binding.progressTechnical.setProgressWithAnimation(technicalSkillsScore, 1500L);
            binding.txtPerTechnical.setText(technicalSkillsScore + "%");

            binding.progressRequirements.setProgressWithAnimation(requirementsScore, 1500L);
            binding.txtPerRequirements.setText(requirementsScore + "%");

            binding.progressKeywords.setProgressWithAnimation(keywordsScore, 1500L);
            binding.txtPerKeywords.setText(keywordsScore + "%");

            binding.txtRequirementsAnalysis.setText(HtmlCompat.fromHtml(analysis, HtmlCompat.FROM_HTML_MODE_LEGACY));
            binding.txtResumeFeedback.setText(HtmlCompat.fromHtml(resumeFeedback, HtmlCompat.FROM_HTML_MODE_LEGACY));

            if (!hiringOverview.isEmpty()) {
                binding.layHiringOverview.setVisibility(View.VISIBLE);
                binding.txtHiringOverview.setText(HtmlCompat.fromHtml(hiringOverview, HtmlCompat.FROM_HTML_MODE_LEGACY));
            } else {
                binding.layHiringOverview.setVisibility(View.GONE);
            }

            binding.txtOverallScore.setText(overallScore + "/100");

            binding.overallScoreView.setProgress(overallScore);
            binding.overallScoreView.animateProgress(overallScore, 2000);

            RecyclerView recyclerFound = binding.recyclerFoundKeywords;
            RecyclerView recyclerMissing = binding.recyclerMissingKeywords;

            FlexboxLayoutManager flexboxLayoutManager1 = new FlexboxLayoutManager(this);
            flexboxLayoutManager1.setFlexDirection(FlexDirection.ROW);
            flexboxLayoutManager1.setJustifyContent(JustifyContent.FLEX_START);

            FlexboxLayoutManager flexboxLayoutManager2 = new FlexboxLayoutManager(this);
            flexboxLayoutManager2.setFlexDirection(FlexDirection.ROW);
            flexboxLayoutManager2.setJustifyContent(JustifyContent.FLEX_START);

            recyclerFound.setLayoutManager(flexboxLayoutManager1);
            recyclerMissing.setLayoutManager(flexboxLayoutManager2);

            List<String> foundList = result.getAtsKeywordsAnalysis().getKeywordsFound();
            List<String> missingList = result.getAtsKeywordsAnalysis().getKeywordsMissing();

            KeywordAdapter foundAdapter = new KeywordAdapter(foundList, true);
            KeywordAdapter missingAdapter = new KeywordAdapter(missingList, false);

            recyclerFound.setAdapter(foundAdapter);
            recyclerMissing.setAdapter(missingAdapter);
        }

        binding.imgArrow1.setOnClickListener(view -> {
            String state = (String) binding.imgArrow1.getTag();

            if ("up1".equals(state)) {
                binding.imgArrow1.setImageResource(R.drawable.icon_arrow_up);
                binding.imgArrow1.setTag("down1");
                binding.txtRequirementsAnalysis.setVisibility(View.VISIBLE);
            } else {
                binding.imgArrow1.setImageResource(R.drawable.icon_arrow_down);
                binding.imgArrow1.setTag("up1");
                binding.txtRequirementsAnalysis.setVisibility(View.GONE);
            }
        });

        binding.imgArrow2.setOnClickListener(view -> {
            String state = (String) binding.imgArrow2.getTag();

            if ("up2".equals(state)) {
                binding.imgArrow2.setImageResource(R.drawable.icon_arrow_up);
                binding.imgArrow2.setTag("down2");
                binding.layKeywordsAnalysis.setVisibility(View.VISIBLE);
            } else {
                binding.imgArrow2.setImageResource(R.drawable.icon_arrow_down);
                binding.imgArrow2.setTag("up2");
                binding.layKeywordsAnalysis.setVisibility(View.GONE);
            }
        });

        binding.imgArrow3.setOnClickListener(view -> {
            String state = (String) binding.imgArrow3.getTag();

            if ("up3".equals(state)) {
                binding.imgArrow3.setImageResource(R.drawable.icon_arrow_up);
                binding.imgArrow3.setTag("down3");
                binding.txtResumeFeedback.setVisibility(View.VISIBLE);
            } else {
                binding.imgArrow3.setImageResource(R.drawable.icon_arrow_down);
                binding.imgArrow3.setTag("up3");
                binding.txtResumeFeedback.setVisibility(View.GONE);
            }
        });

        binding.imgArrow4.setOnClickListener(view -> {
            String state = (String) binding.imgArrow4.getTag();

            if ("up4".equals(state)) {
                binding.imgArrow4.setImageResource(R.drawable.icon_arrow_up);
                binding.imgArrow4.setTag("down4");
                binding.txtHiringOverview.setVisibility(View.VISIBLE);
            } else {
                binding.imgArrow4.setImageResource(R.drawable.icon_arrow_down);
                binding.imgArrow4.setTag("up4");
                binding.txtHiringOverview.setVisibility(View.GONE);
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