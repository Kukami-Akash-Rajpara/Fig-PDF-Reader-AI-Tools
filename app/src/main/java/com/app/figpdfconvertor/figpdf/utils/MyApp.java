package com.app.figpdfconvertor.figpdf.utils;

import android.app.Activity;
import android.app.Application;
import android.os.Bundle;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.multidex.MultiDexApplication;

import com.app.figpdfconvertor.figpdf.BuildConfig;
import com.app.figpdfconvertor.figpdf.ads.AdManagerInter;
import com.app.figpdfconvertor.figpdf.ads.AdManagerNative;
import com.app.figpdfconvertor.figpdf.ads.AdManagerRewarded;
import com.app.figpdfconvertor.figpdf.ads.ConstantKt;
import com.app.figpdfconvertor.figpdf.ads.GoogleMobileAdsConsentManager;
import com.app.figpdfconvertor.figpdf.funnelss.AnalyticsManager;
import com.app.figpdfconvertor.figpdf.preferences.AppHelper;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.initialization.OnInitializationCompleteListener;
import com.google.firebase.FirebaseApp;
import com.microsoft.clarity.Clarity;
import com.microsoft.clarity.ClarityConfig;

import io.appmetrica.analytics.AppMetrica;
import io.appmetrica.analytics.AppMetricaConfig;

import java.util.Collections;
import java.util.Locale;

public class MyApp extends MultiDexApplication implements Application.ActivityLifecycleCallbacks {
    private static MyApp instance;
    private int activityReferences = 0;
    private boolean isActivityChangingConfigurations = false;
    private boolean userHasExplored = false;
    public static boolean isAdVisibleInter = false;
    public void setUserHasExplored(boolean explored) {
        this.userHasExplored = explored;
    }

    public boolean hasUserExplored() {
        return userHasExplored;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        instance = this;

//        AppHelper.setAppUpdateVersionCode(String.valueOf(BuildConfig.VERSION_CODE));
        try {
            // ✅ Always initialize AppHelper and language
            ConstantKt.setGoogleMobileAdsConsentManager(GoogleMobileAdsConsentManager.Companion.getInstance(this));
            setDefaultLanguageIfFirstLaunch();

            // Only initialize analytics / tracking in release mode
            if (!BuildConfig.DEBUG) {
                // Firebase Analytics
                try {
                    FirebaseApp.initializeApp(this);
                    AnalyticsManager.INSTANCE.init(this); // this internally gets FirebaseAnalytics
                    Log.d("MyApp", "Firebase Analytics initialized");
                } catch (Exception e) {
                    e.printStackTrace();
                    Log.e("MyApp", "Firebase init failed: " + e.getMessage());
                }

                // Microsoft Clarity
                try {
                    ClarityConfig config = new ClarityConfig("qyv47gjkaf");
                    Clarity.initialize(getApplicationContext(), config);
                    Log.d("MyApp", "Microsoft Clarity initialized");
                } catch (Exception e) {
                    Log.e("MyApp", "Clarity init failed: " + e.getMessage());
                }

                // AppMetrica
                try {
                    String apiKey = "9e786f9c-02b1-42ca-8bb3-01eb9105dd3d";
                    AppMetricaConfig config = AppMetricaConfig.newConfigBuilder(apiKey)
                            .withLogs()
                            .build();
                    AppMetrica.activate(getApplicationContext(), config);
                    AppMetrica.enableActivityAutoTracking(this);
                    Log.d("MyApp", "AppMetrica initialized");
                } catch (Exception e) {
                    Log.e("MyApp", "AppMetrica init failed: " + e.getMessage());
                }
            } else {
                // ✅ Debug mode → skip analytics init
                Log.d("MyApp", "Debug mode → skipping Firebase / Clarity / AppMetrica");
            }

            // Register lifecycle callbacks for global exit tracking
            registerActivityLifecycleCallbacks(this);

            // Ads (you can also wrap in debug check if needed)
            // loadPreferredAds();

        } catch (Exception e) {
            e.printStackTrace();
            Log.e("MyApp", "App init failed: " + e.getMessage());
        }
    }

    public static MyApp getInstance() {
        return instance;
    }

    // ---------------- Activity Lifecycle ----------------

    @Override
    public void onActivityStarted(@NonNull Activity activity) {
        activityReferences++;
    }

    @Override
    public void onActivityStopped(@NonNull Activity activity) {
        activityReferences--;
        isActivityChangingConfigurations = activity.isChangingConfigurations();

        if (activityReferences == 0 && !isActivityChangingConfigurations) {
            // App went to background → track exit globally
            boolean goodExit = userHasExplored; // use global flag
            String exitType = goodExit ? "good_exit" : "bad_exit";

            AnalyticsManager.INSTANCE.logEvent(exitType,
                    Collections.singletonMap("screen", activity.getClass().getSimpleName()));
            AnalyticsManager.INSTANCE.logFunnelStep(exitType,
                    Collections.singletonMap("screen", activity.getClass().getSimpleName()));
        }
    }

    // ---------------- Unused lifecycle methods ----------------
    @Override
    public void onActivityCreated(@NonNull Activity activity, @Nullable Bundle bundle) {
    }

    @Override
    public void onActivityResumed(@NonNull Activity activity) {
    }

    @Override
    public void onActivityPaused(@NonNull Activity activity) {
    }

    @Override
    public void onActivitySaveInstanceState(@NonNull Activity activity, @NonNull Bundle bundle) {
    }

    @Override
    public void onActivityDestroyed(@NonNull Activity activity) {
    }


    public void initializeAllAds(Activity mActivity, OnInitializationCompleteListener mInitListener) {
        new Thread(() ->
                MobileAds.initialize(mActivity, initializationStatus ->
                        mActivity.runOnUiThread(() -> {
                            mInitListener.onInitializationComplete(initializationStatus);
                            loadPreferredAds();
                        })
                )
        ).start();
    }

    public void loadPreferredAds() {
        if (AppHelper.getAdsTag()) {
            AdManagerNative.initializePreferredAdNetwork(this);
            AdManagerInter.initializePreferredAdNetwork(this);
            AdManagerRewarded.loadRewardAd(this);
        }
    }

    private void setDefaultLanguageIfFirstLaunch() {
        if (AppHelper.isFirstTime()) {
            String deviceCountry = Locale.getDefault().getCountry(); // e.g., "DE", "IN"
            String defaultLangCode = "en"; // fallback

            switch (deviceCountry) {
                case "BD": // Bangladesh
                    defaultLangCode = "bn"; // Bengali
                    break;
                case "CN": // China
                    defaultLangCode = "zh"; // Chinese
                    break;
                case "NL": // Netherlands
                    defaultLangCode = "nl"; // Dutch
                    break;
                case "PH": // Philippines
                    defaultLangCode = "fil"; // Filipino
                    break;
                case "FR": // France
                    defaultLangCode = "fr"; // French
                    break;
                case "DE": // Germany
                    defaultLangCode = "de"; // German
                    break;
                case "IN": // India
                    defaultLangCode = "en"; // English
                    break;
                case "ID": // Indonesia
                    defaultLangCode = "id"; // Indonesian
                    break;
                case "IT": // Italy
                    defaultLangCode = "it"; // Italian
                    break;
                case "JP": // Japan
                    defaultLangCode = "ja"; // Japanese
                    break;
                case "KR": // South Korea
                    defaultLangCode = "ko"; // Korean
                    break;
                case "MY": // Malaysia
                    defaultLangCode = "ms"; // Malay
                    break;
                case "PL": // Poland
                    defaultLangCode = "pl"; // Polish
                    break;
                case "BR": // Brazil
                    defaultLangCode = "pt"; // Portuguese
                    break;
                case "RU": // Russia
                    defaultLangCode = "ru"; // Russian
                    break;
                case "ES": // Spain
                    defaultLangCode = "es"; // Spanish
                    break;
                case "TH": // Thailand
                    defaultLangCode = "th"; // Thai
                    break;
                case "TR": // Turkey
                    defaultLangCode = "tr"; // Turkish
                    break;
                case "VN": // Vietnam
                    defaultLangCode = "vi"; // Vietnamese
                    break;
                default:
                    defaultLangCode = "en"; // fallback English
                    break;
            }

            AppHelper.setLanguageCode(defaultLangCode);
            LocaleHelper.setLocale(this, defaultLangCode);
        }
    }


}
