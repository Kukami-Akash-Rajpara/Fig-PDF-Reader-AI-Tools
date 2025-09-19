package com.app.figpdfconvertor.figpdf.activity;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.app.figpdfconvertor.figpdf.BuildConfig;
import com.app.figpdfconvertor.figpdf.R;
import com.app.figpdfconvertor.figpdf.funnelss.AnalyticsManager;
import com.app.figpdfconvertor.figpdf.preferences.AppHelper;
import com.app.figpdfconvertor.figpdf.utils.MyApp;
import com.app.figpdfconvertor.figpdf.utils.MyUtils;
import com.app.figpdfconvertor.figpdf.utils.LocaleHelper;
import com.app.figpdfconvertor.figpdf.utils.NetworkUtils;
import com.app.figpdfconvertor.figpdf.utils.UtilsDialog;
import com.google.firebase.remoteconfig.FirebaseRemoteConfig;
import com.google.firebase.remoteconfig.FirebaseRemoteConfigSettings;

public class SplashScreenActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash_screen);
        MyUtils.fullScreenLightStatusBar(this);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        if (NetworkUtils.isInternetAvailable(SplashScreenActivity.this)) {
            getFirebaseData();
        } else {
            showNoInterNetDialog();
        }
        MyApp.getInstance().initializeAllAds(this, initializationStatus -> {

        });
        AnalyticsManager.INSTANCE.logAppStart(this);
        AnalyticsManager.INSTANCE.logEvent("app_opened", null);
        AnalyticsManager.INSTANCE.logFunnelStep("app_opened", null);

        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {

            }
        });
    }

    private void getFirebaseData() {
        FirebaseRemoteConfig firebaseRemoteConfig = FirebaseRemoteConfig.getInstance();
        FirebaseRemoteConfigSettings configSettings = new FirebaseRemoteConfigSettings.Builder()
                .setMinimumFetchIntervalInSeconds(0)
                .build();
        firebaseRemoteConfig.setConfigSettingsAsync(configSettings);

        firebaseRemoteConfig.fetchAndActivate().addOnCompleteListener(this, task -> {
            if (task.isSuccessful()) {
                try {
                    // Safely read remote values with fallback defaults
                    AppHelper.setAdsTag(firebaseRemoteConfig.getBoolean("ads_tag"));

                    /*if (BuildConfig.DEBUG) {
                        AppHelper.setAdsTag(true);
                    }*/

                    AppHelper.setGInterTag(getSafeString(firebaseRemoteConfig, "g_inter_tag", ""));
                   /* if (BuildConfig.DEBUG) {
                        AppHelper.setGInterTag("ca-app-pub-3940256099942544/1033173712");
                    }*/

                    AppHelper.setGNativeTag(getSafeString(firebaseRemoteConfig, "g_native_tag", ""));
                   /* if (BuildConfig.DEBUG) {
                        AppHelper.setGNativeTag("ca-app-pub-3940256099942544/2247696110");
                    }*/

                    AppHelper.setGRewardedTag(getSafeString(firebaseRemoteConfig, "g_rewarded_tag", ""));
                   /* if (BuildConfig.DEBUG) {
                        AppHelper.setGRewardedTag("ca-app-pub-3940256099942544/5224354917");
                    }*/

                    AppHelper.setGBannerTag(getSafeString(firebaseRemoteConfig, "g_banner_tag", ""));
                   /* if (BuildConfig.DEBUG) {
                        AppHelper.setGBannerTag("ca-app-pub-3940256099942544/9214589741");
                    }*/

                    AppHelper.setAppUnderMaintenance(firebaseRemoteConfig.getBoolean("app_under_maintenance"));

                    AppHelper.setShowNativeLanguage(firebaseRemoteConfig.getBoolean("show_native_language"));
                    AppHelper.setShowInterSummarize(firebaseRemoteConfig.getBoolean("show_inter_summarize"));
                    AppHelper.setShowInterAnalyzerHiringSubmit(firebaseRemoteConfig.getBoolean("show_inter_analyzer_hiring_submit"));
                    AppHelper.setShowInterAnalyzerCandidateSubmit(firebaseRemoteConfig.getBoolean("show_inter_analyzer_candidate_submit"));
                    AppHelper.setShowInterPdfToImage(firebaseRemoteConfig.getBoolean("show_inter_pdf_to_image"));
                    AppHelper.setShowInterPptToPdf(firebaseRemoteConfig.getBoolean("show_inter_ppt_to_pdf"));
                    AppHelper.setShowInterWordToPdf(firebaseRemoteConfig.getBoolean("show_inter_word_to_pdf"));
                    AppHelper.setShowRewardImageToPdf(firebaseRemoteConfig.getBoolean("show_reward_image_to_pdf_convert"));
                    AppHelper.setShowRewardOcrResult(firebaseRemoteConfig.getBoolean("show_reward_ocr_result"));
                    AppHelper.setShowRewardInterviewbotSubmit(firebaseRemoteConfig.getBoolean("show_reward_interviewbot_submit"));
                    AppHelper.setShowInterOcrDownload(firebaseRemoteConfig.getBoolean("show_inter_ocr_download"));
                } catch (Exception e) {
                    Log.e("TAG", "Error parsing remote config", e);
                }
            } else {
                Log.e("TAG", "Firebase fetch error", task.getException());
            }
            goToTask();
        });
    }

    private String getSafeString(FirebaseRemoteConfig config, String key, String defaultValue) {
        try {
            String value = config.getString(key);
            return value != null ? value : defaultValue;
        } catch (Exception e) {
            Log.w("TAG", "Missing or invalid Firebase key: " + key, e);
            return defaultValue;
        }
    }

    private void goToTask() {
        if (AppHelper.getAppUnderMaintenance()) {
            startActivity(new Intent(this, UnderMaintenanceActivity.class));
            finish();
            return;
        }

        if (AppHelper.isFirstTime()) {
            startActivity(new Intent(this, LanguageActivity.class));
            finish();
        } else {
            startActivity(new Intent(this, MainActivity.class));
            finish();
        }
    }

    private void showNoInterNetDialog() {
        Dialog dialog = UtilsDialog.generateDialogFull(this, R.layout.dialog_no_internet);
        if (dialog != null) {
            dialog.setCancelable(false);
            dialog.findViewById(R.id.btnRetry).setOnClickListener(v -> {
                if (NetworkUtils.isInternetAvailable(SplashScreenActivity.this)) {
                    dialog.dismiss();
                    getFirebaseData();
                } else {
                    Toast.makeText(this, "Please check again", Toast.LENGTH_SHORT).show();
                }
            });
            dialog.show();
        }
    }

    @Override
    protected void attachBaseContext(Context context) {
        super.attachBaseContext(LocaleHelper.onAttach(context));
    }
}

/*
package com.app.figpdfconvertor.figpdf.activity;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.app.figpdfconvertor.figpdf.BuildConfig;
import com.app.figpdfconvertor.figpdf.R;
import com.app.figpdfconvertor.figpdf.funnelss.AnalyticsManager;
import com.app.figpdfconvertor.figpdf.preferences.AppHelper;
import com.app.figpdfconvertor.figpdf.utils.MyApp;
import com.app.figpdfconvertor.figpdf.utils.MyUtils;
import com.app.figpdfconvertor.figpdf.utils.LocaleHelper;
import com.app.figpdfconvertor.figpdf.utils.NetworkUtils;
import com.app.figpdfconvertor.figpdf.utils.UtilsDialog;
import com.google.firebase.remoteconfig.FirebaseRemoteConfig;
import com.google.firebase.remoteconfig.FirebaseRemoteConfigSettings;

public class SplashScreenActivity extends AppCompatActivity {
    private com.google.android.gms.ads.interstitial.InterstitialAd mInterstitialAd = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash_screen);
        MyUtils.fullScreenLightStatusBar(this);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        preloadInterstitialAd();
        if (NetworkUtils.isInternetAvailable(SplashScreenActivity.this)) {
            getFirebaseData();
        } else {
            showNoInterNetDialog();
        }
        MyApp.getInstance().initializeAllAds(this, initializationStatus -> {

        });
        AnalyticsManager.INSTANCE.logAppStart(this);
        AnalyticsManager.INSTANCE.logEvent("app_opened", null);
        AnalyticsManager.INSTANCE.logFunnelStep("app_opened", null);

        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {

            }
        });
    }

    private void getFirebaseData() {
        FirebaseRemoteConfig firebaseRemoteConfig = FirebaseRemoteConfig.getInstance();
        FirebaseRemoteConfigSettings configSettings = new FirebaseRemoteConfigSettings.Builder()
                .setMinimumFetchIntervalInSeconds(0)
                .build();
        firebaseRemoteConfig.setConfigSettingsAsync(configSettings);

        firebaseRemoteConfig.fetchAndActivate().addOnCompleteListener(this, task -> {
            if (task.isSuccessful()) {
                try {
                    // Safely read remote values with fallback defaults
                    AppHelper.setAdsTag(firebaseRemoteConfig.getBoolean("ads_tag"));

                    if (BuildConfig.DEBUG) {
                        AppHelper.setAdsTag(false);
                    }

                    AppHelper.setGInterTag(getSafeString(firebaseRemoteConfig, "g_inter_tag", ""));
                    if (BuildConfig.DEBUG) {
                        AppHelper.setGInterTag("ca-app-pub-3940256099942544/1033173712");
                    }

                    AppHelper.setGNativeTag(getSafeString(firebaseRemoteConfig, "g_native_tag", ""));
                    if (BuildConfig.DEBUG) {
                        AppHelper.setGNativeTag("ca-app-pub-3940256099942544/2247696110");
                    }

                    AppHelper.setGBannerTag(getSafeString(firebaseRemoteConfig, "g_banner_tag", ""));
                    if (BuildConfig.DEBUG) {
                        AppHelper.setGBannerTag("ca-app-pub-3940256099942544/9214589741");
                    }

                    AppHelper.setAppUnderMaintenance(firebaseRemoteConfig.getBoolean("app_under_maintenance"));

                    AppHelper.setShowNativeLanguage(firebaseRemoteConfig.getBoolean("show_native_language"));
                    AppHelper.setShowInterSummarize(firebaseRemoteConfig.getBoolean("show_inter_summarize"));
                    AppHelper.setShowInterAnalyzerHiringSubmit(firebaseRemoteConfig.getBoolean("show_inter_analyzer_hiring_submit"));
                    AppHelper.setShowInterAnalyzerCandidateSubmit(firebaseRemoteConfig.getBoolean("show_inter_analyzer_candidate_submit"));
                    AppHelper.setShowInterPdfToImage(firebaseRemoteConfig.getBoolean("show_inter_pdf_to_image"));
                    AppHelper.setShowInterPptToPdf(firebaseRemoteConfig.getBoolean("show_inter_ppt_to_pdf"));
                    AppHelper.setShowInterWordToPdf(firebaseRemoteConfig.getBoolean("show_inter_word_to_pdf"));
                    AppHelper.setShowRewardImageToPdf(firebaseRemoteConfig.getBoolean("show_reward_image_to_pdf_convert"));
                    AppHelper.setShowRewardOcrResult(firebaseRemoteConfig.getBoolean("show_reward_ocr_result"));
                    AppHelper.setShowRewardInterviewbotSubmit(firebaseRemoteConfig.getBoolean("show_reward_interviewbot_submit"));
                    AppHelper.setShowInterOcrDownload(firebaseRemoteConfig.getBoolean("show_inter_ocr_download"));
                } catch (Exception e) {
                    Log.e("TAG", "Error parsing remote config", e);
                }
            } else {
                Log.e("TAG", "Firebase fetch error", task.getException());
            }
            goToTask();
        });
    }

    private String getSafeString(FirebaseRemoteConfig config, String key, String defaultValue) {
        try {
            String value = config.getString(key);
            return value != null ? value : defaultValue;
        } catch (Exception e) {
            Log.w("TAG", "Missing or invalid Firebase key: " + key, e);
            return defaultValue;
        }
    }
    String adUnitId;


    private void preloadInterstitialAd() {
        if (!AppHelper.getAdsTag() || AppHelper.getGInterTag().isEmpty()) return;
        if (BuildConfig.DEBUG) {
            adUnitId = "ca-app-pub-3940256099942544/1033173712"; // test interstitial
        } else {
            adUnitId = AppHelper.getGInterTag(); // real ad unit from remote config
        }
        com.google.android.gms.ads.interstitial.InterstitialAd.load(
                this,
                adUnitId,
                new com.google.android.gms.ads.AdRequest.Builder().build(),
                new com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback() {
                    @Override
                    public void onAdLoaded(@NonNull com.google.android.gms.ads.interstitial.InterstitialAd interstitialAd) {
                        mInterstitialAd = interstitialAd; // store preloaded ad
                    }

                    @Override
                    public void onAdFailedToLoad(@NonNull com.google.android.gms.ads.LoadAdError loadAdError) {
                        mInterstitialAd = null;
                    }
                });
    }

    private void showInterstitialThenNavigate(Intent nextIntent) {
        if (mInterstitialAd != null) {
            mInterstitialAd.show(SplashScreenActivity.this);
            mInterstitialAd.setFullScreenContentCallback(new com.google.android.gms.ads.FullScreenContentCallback() {
                @Override
                public void onAdDismissedFullScreenContent() {
                    startActivity(nextIntent);
                    finish();
                }

                @Override
                public void onAdFailedToShowFullScreenContent(@NonNull com.google.android.gms.ads.AdError adError) {
                    startActivity(nextIntent);
                    finish();
                }
            });
        } else {
            // Ad not loaded → navigate immediately
            startActivity(nextIntent);
            finish();
        }
    }


    */
/* private void goToTask() {
        if (AppHelper.getAppUnderMaintenance()) {
            startActivity(new Intent(this, UnderMaintenanceActivity.class));
            finish();
            return;
        }

        if (AppHelper.isFirstTime()) {
            startActivity(new Intent(this, LanguageActivity.class));
            finish();
        } else {
            startActivity(new Intent(this, MainActivity.class));
            finish();
        }
    }*//*

    private void goToTask() {
        if (AppHelper.getAppUnderMaintenance()) {
            startActivity(new Intent(this, UnderMaintenanceActivity.class));
            finish();
            return;
        }

        Intent nextIntent;
        if (AppHelper.isFirstTime()) {
            nextIntent = new Intent(this, LanguageActivity.class);
        } else {
            nextIntent = new Intent(this, MainActivity.class);
        }

        // Show interstitial ad first, then navigate
        if (!AppHelper.isFirstTime()){
            showInterstitialThenNavigate(nextIntent);
        }

    }


    private void showNoInterNetDialog() {
        Dialog dialog = UtilsDialog.generateDialogFull(this, R.layout.dialog_no_internet);
        if (dialog != null) {
            dialog.setCancelable(false);
            dialog.findViewById(R.id.btnRetry).setOnClickListener(v -> {
                if (NetworkUtils.isInternetAvailable(SplashScreenActivity.this)) {
                    dialog.dismiss();
                    getFirebaseData();
                } else {
                    Toast.makeText(this, "Please check again", Toast.LENGTH_SHORT).show();
                }
            });
            dialog.show();
        }
    }

    @Override
    protected void attachBaseContext(Context context) {
        super.attachBaseContext(LocaleHelper.onAttach(context));
    }
}*/
