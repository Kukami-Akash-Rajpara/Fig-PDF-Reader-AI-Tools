package com.app.figpdfconvertor.figpdf.utils;

import static com.app.figpdfconvertor.figpdf.ads.AdManagerSplash.initializeInterstitialAd;
import static com.app.figpdfconvertor.figpdf.ads.ConstantKt.getGoogleMobileAdsConsentManager;

import android.app.Activity;
import android.app.Application;
import android.os.Bundle;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.multidex.MultiDexApplication;

import com.app.figpdfconvertor.figpdf.BuildConfig;
import com.app.figpdfconvertor.figpdf.activity.BaseActivity;
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
import com.google.firebase.analytics.FirebaseAnalytics;
import com.google.firebase.crashlytics.FirebaseCrashlytics;
import com.microsoft.clarity.Clarity;
import com.microsoft.clarity.ClarityConfig;

import io.appmetrica.analytics.AppMetrica;
import io.appmetrica.analytics.AppMetricaConfig;

import java.util.Collections;

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

        try {
            FirebaseApp.initializeApp(this);
            AnalyticsManager.INSTANCE.init(this); // this will get FirebaseAnalytics inside
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (!BuildConfig.DEBUG) {
            ClarityConfig config = new ClarityConfig("qyv47gjkaf");
            Clarity.initialize(getApplicationContext(), config);
        }
        ConstantKt.setGoogleMobileAdsConsentManager(GoogleMobileAdsConsentManager.Companion.getInstance(this));

        String apiKey = "9e786f9c-02b1-42ca-8bb3-01eb9105dd3d";
        try {
            AppMetricaConfig config = AppMetricaConfig.newConfigBuilder(apiKey)
                    .withLogs()
                    .build();
            AppMetrica.activate(getApplicationContext(), config);
            AppMetrica.enableActivityAutoTracking(this);
            Log.d("TAG", "App Metrica initialized successfully");
        } catch (Exception e) {
            Log.e("TAG", "Failed to initialize App Metrica", e);
        }

        // Register lifecycle callbacks for hard exit tracking
        registerActivityLifecycleCallbacks(this);

    //    loadPreferredAds();

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
    @Override public void onActivityCreated(@NonNull Activity activity, @Nullable Bundle bundle) {}
    @Override public void onActivityResumed(@NonNull Activity activity) {}
    @Override public void onActivityPaused(@NonNull Activity activity) {}
    @Override public void onActivitySaveInstanceState(@NonNull Activity activity, @NonNull Bundle bundle) {}
    @Override public void onActivityDestroyed(@NonNull Activity activity) {}


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

}
