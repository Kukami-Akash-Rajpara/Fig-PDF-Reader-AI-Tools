package com.app.figpdfconvertor.figpdf.ads;

import static com.app.figpdfconvertor.figpdf.ads.ConstantKt.canLoadAds;
import static com.app.figpdfconvertor.figpdf.ads.ConstantKt.isNetworkAvailable;


import android.app.Activity;
import android.content.Intent;
import android.os.Handler;
import android.util.Log;

import androidx.annotation.NonNull;

import com.app.figpdfconvertor.figpdf.BuildConfig;
import com.app.figpdfconvertor.figpdf.preferences.AppHelper;
import com.app.figpdfconvertor.figpdf.utils.MyApp;
import com.google.android.gms.ads.AdError;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.FullScreenContentCallback;
import com.google.android.gms.ads.LoadAdError;
import com.google.android.gms.ads.interstitial.InterstitialAd;
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback;

public class AdManagerSplash {

    private static InterstitialAd admobInterstitialAd;

    public static void renderStartAd(Activity activity, Intent intent) {
        if (isNetworkAvailable(activity)) {
            int currentVersionCode = BuildConfig.VERSION_CODE;
            String remoteCodeStr = AppHelper.getAppUpdateVersionCode();

            if (AppHelper.getAdsTag()
                    && currentVersionCode != Integer.parseInt(remoteCodeStr)
                    && canLoadAds()) {
                MyApp.getInstance().initializeAllAds(activity, initializationStatus -> {
                    initializeInterstitialAd(activity, () -> {
                        activity.startActivity(intent);
                        activity.finish();
                    });
                });
            } else {
                Log.d("AdManagerSplash", "Consent not given OR ads disabled → Skipping ad");
                activity.startActivity(intent);
                activity.finish();
            }
        } else {
            activity.startActivity(intent);
            activity.finish();
        }
    }


    public static void initializeInterstitialAd(Activity activity, AdEventListener callback) {
        int currentVersionCode = BuildConfig.VERSION_CODE;
        String remoteCodeStr = AppHelper.getAppUpdateVersionCode();

        if (AppHelper.getAdsTag()
                && currentVersionCode != Integer.parseInt(remoteCodeStr)
                && canLoadAds()) {
            if (AppHelper.getShowInterSplash()) {
                displayLoadAdmobAd(activity, callback);
            } else if (AppHelper.isFirstTime() && AppHelper.getShowNativeLanguage()) {
                new Handler().postDelayed(() -> {
                    if (callback != null) callback.onAdFinish();
                }, 4000);
            } else {
                if (callback != null) callback.onAdFinish();
            }
        } else {
            Log.d("AdManagerSplash", "Consent not given OR ads disabled → Skipping Interstitial");
            if (callback != null) callback.onAdFinish();
        }
    }


    private static void displayLoadAdmobAd(Activity activity, AdEventListener callback) {
        AdRequest adRequest = new AdRequest.Builder().build();
        InterstitialAd.load(activity, AppHelper.getGInterTag(), adRequest, new InterstitialAdLoadCallback() {
            @Override
            public void onAdLoaded(@NonNull InterstitialAd interstitialAd) {
                admobInterstitialAd = interstitialAd;
                admobInterstitialAd.show(activity);
                admobInterstitialAd.setFullScreenContentCallback(new FullScreenContentCallback() {
                    @Override
                    public void onAdFailedToShowFullScreenContent(@NonNull AdError adError) {
                        invokeAdCallback(callback);
                    }

                    @Override
                    public void onAdShowedFullScreenContent() {
                        admobInterstitialAd = null;
                        MyApp.isAdVisibleInter = true;
                    }

                    @Override
                    public void onAdDismissedFullScreenContent() {
                        invokeAdCallback(callback);
                    }
                });
            }

            @Override
            public void onAdFailedToLoad(@NonNull LoadAdError loadAdError) {
                invokeAdCallback(callback);
            }
        });
    }

    private static void invokeAdCallback(AdEventListener callback) {
        admobInterstitialAd = null;
        MyApp.isAdVisibleInter = false;
        if (callback != null) {
            callback.onAdFinish();
        }
    }
}
