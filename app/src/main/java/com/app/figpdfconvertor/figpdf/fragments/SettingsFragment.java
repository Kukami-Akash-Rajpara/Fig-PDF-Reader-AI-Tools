package com.app.figpdfconvertor.figpdf.fragments;

import static android.content.Intent.ACTION_VIEW;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.browser.customtabs.CustomTabsIntent;
import androidx.fragment.app.Fragment;

import com.app.figpdfconvertor.figpdf.BuildConfig;
import com.app.figpdfconvertor.figpdf.activity.LanguageActivity;
import com.app.figpdfconvertor.figpdf.databinding.FragmentSettingsBinding;
import com.app.figpdfconvertor.figpdf.utils.DoubleClickListener;

public class SettingsFragment extends Fragment {

    private FragmentSettingsBinding binding;

    public SettingsFragment() {
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        binding = FragmentSettingsBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        binding.txtVersionNumber.setText("V-" + BuildConfig.VERSION_NAME);

        binding.laySettingsLanguage.setOnClickListener(new DoubleClickListener() {
            @Override
            public void performClick(View v) {
                Intent intent = new Intent(getActivity(), LanguageActivity.class);
                startActivity(intent);
            }
        });

        binding.laySettingsRateUs.setOnClickListener(new DoubleClickListener() {
            @Override
            public void performClick(View v) {
                rateUs();
            }
        });

        binding.laySettingsFigUniverse.setOnClickListener(new DoubleClickListener() {
            @Override
            public void performClick(View v) {
                try {
                    Intent intent = new Intent(Intent.ACTION_VIEW,
                            Uri.parse("market://search?q=pub:Kukami-Fig"));
                    intent.setPackage("com.android.vending");
                    startActivity(intent);
                } catch (ActivityNotFoundException e) {
                    Intent intent = new Intent(Intent.ACTION_VIEW,
                            Uri.parse("https://play.google.com/store/search?q=pub:Kukami-Fig"));
                    startActivity(intent);
                }
            }
        });

        binding.laySettingsTerms.setOnClickListener(new DoubleClickListener() {
            @Override
            public void performClick(View v) {
                LinkOpen(requireActivity(), "https://fig-pdf-reader-ai-tools.blogspot.com/2025/09/terms-conditions.html");
            }
        });

        binding.laySettingsPrivacy.setOnClickListener(new DoubleClickListener() {
            @Override
            public void performClick(View v) {
                LinkOpen(requireActivity(), "https://fig-pdf-reader-ai-tools.blogspot.com/2025/09/privacy-policy.html");
            }
        });

        binding.laySettingsAbout.setOnClickListener(new DoubleClickListener() {
            @Override
            public void performClick(View v) {
                LinkOpen(requireActivity(), "https://fig-pdf-reader-ai-tools.blogspot.com/2025/09/about-us.html");
            }
        });

        binding.txtVisitWeb.setOnClickListener(new DoubleClickListener() {
            @Override
            public void performClick(View v) {
                LinkOpen(requireActivity(), "https://kukamitechnology.com");
            }
        });
    }

    private void rateUs() {
        try {
            Intent intent = new Intent(ACTION_VIEW);
            intent.setData(Uri.parse("https://play.google.com/store/apps/details?id=" + requireActivity().getPackageName()));
            startActivity(intent);
        } catch (Exception e) {
            Intent intent = new Intent(ACTION_VIEW);
            intent.setData(Uri.parse("market://details?id=" + requireActivity().getPackageName()));
            startActivity(intent);
        }
    }

    public static void LinkOpen(Activity mActivity, String url) {
        CustomTabsIntent customTabsIntent = new CustomTabsIntent.Builder()
                .setShowTitle(true)
                .build();
        customTabsIntent.launchUrl(mActivity, Uri.parse(url));
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}