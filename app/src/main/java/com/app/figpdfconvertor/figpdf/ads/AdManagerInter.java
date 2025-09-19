package com.app.figpdfconvertor.figpdf.ads;

import static com.app.figpdfconvertor.figpdf.ads.ConstantKt.canLoadAds;

import android.app.Activity;
import android.content.Context;

import androidx.annotation.NonNull;

import com.app.figpdfconvertor.figpdf.BuildConfig;
import com.app.figpdfconvertor.figpdf.preferences.AppHelper;
import com.app.figpdfconvertor.figpdf.utils.MyApp;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.FullScreenContentCallback;
import com.google.android.gms.ads.LoadAdError;
import com.google.android.gms.ads.admanager.AdManagerAdRequest;
import com.google.android.gms.ads.admanager.AdManagerInterstitialAd;
import com.google.android.gms.ads.admanager.AdManagerInterstitialAdLoadCallback;
import com.google.android.gms.ads.interstitial.InterstitialAd;
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback;

public class AdManagerInter {

    private static boolean consentGranted = false;
    private static long lastAdTimestamp = 0;
    private static int adClickCount = 0;

    // ✅ Two types of ads
    private static InterstitialAd admobInterstitialAd;
    private static AdManagerInterstitialAd adxInterstitialAd;

    public static void initializePreferredAdNetwork(Context context) {
        if (!(context instanceof Activity)) {
            // Only request consent if context is an Activity
            checkAndInitAds(context);
            return;
        }

        Activity activity = (Activity) context;

        com.app.figpdfconvertor.figpdf.ads.ConstantKt.getGoogleMobileAdsConsentManager().requestConsentUpdate(activity,
                new GoogleMobileAdsConsentManager.ConsentMangerRequestAd() {
                    @Override
                    public void onSuccess() {
                        checkAndInitAds(context);
                    }

                    @Override
                    public void onFailed() {
                        if (!com.app.figpdfconvertor.figpdf.ads.ConstantKt.getGoogleMobileAdsConsentManager().isConsentRequired()) {
                            checkAndInitAds(context);
                        }
                    }
                });
    }

    private static void checkAndInitAds(Context context) {
        int currentVersionCode = BuildConfig.VERSION_CODE;
        String remoteCodeStr = AppHelper.getAppUpdateVersionCode();

        if (AppHelper.getAdsTag() && currentVersionCode != Integer.parseInt(remoteCodeStr)) {
            initializeAdmobAd(context);
            initializeAdxAd(context);
        }
    }



    private static void checkAndInitAds(Activity activity) {
        int currentVersionCode = BuildConfig.VERSION_CODE;
        String remoteCodeStr = AppHelper.getAppUpdateVersionCode();

        if (AppHelper.getAdsTag() && currentVersionCode != Integer.parseInt(remoteCodeStr)) {
            initializeAdmobAd(activity);
            initializeAdxAd(activity);
        }
    }

    public static void renderInterAd(Activity activity, AdEventListener eventListener) {
        if (!canLoadAds()) {
            triggerAdCallback(eventListener);
            return;
        }

        int currentVersionCode = BuildConfig.VERSION_CODE;
        String remoteCodeStr = AppHelper.getAppUpdateVersionCode();

        if (AppHelper.getAdsTag() && currentVersionCode != Integer.parseInt(remoteCodeStr)) {
            handleTimeBasedAd(activity, eventListener);
        } else {
            triggerAdCallback(eventListener);
        }
    }

    public static void renderInterAdFixed(Activity activity, AdEventListener eventListener) {
        if (!canLoadAds()) {
            triggerAdCallback(eventListener);
            return;
        }

        int currentVersionCode = BuildConfig.VERSION_CODE;
        String remoteCodeStr = AppHelper.getAppUpdateVersionCode();

        if (AppHelper.getAdsTag() && currentVersionCode != Integer.parseInt(remoteCodeStr)) {
            handleClickBasedAd(activity, eventListener);
        } else {
            triggerAdCallback(eventListener);
        }
    }

    // ================= TIME / CLICK CONTROL ==================

    private static void handleTimeBasedAd(Activity activity, AdEventListener eventListener) {
        long currentTime = System.currentTimeMillis();
        long timeInMillis = Long.parseLong(AppHelper.getInterTimeTag()) * 1000;

        if (lastAdTimestamp == 0) {
            lastAdTimestamp = currentTime;
            triggerAdCallback(eventListener);
            return;
        }

        long timeSinceLastAd = currentTime - lastAdTimestamp;

        if (timeSinceLastAd >= timeInMillis) {
            displayPreferredAd(activity, eventListener);
        } else {
            initializePreferredAdNetwork(activity);
            triggerAdCallback(eventListener);
        }
    }

    private static void handleClickBasedAd(Activity activity, AdEventListener eventListener) {
        adClickCount++;
        int requiredClicks = 1; // or from Pref

        if (adClickCount >= requiredClicks) {
            displayPreferredAd(activity, eventListener);
        } else {
            initializePreferredAdNetwork(activity);
            triggerAdCallback(eventListener);
        }
    }

    // ================== DISPLAY ==================

    private static void displayPreferredAd(Activity activity, AdEventListener eventListener) {
        MyApp.isAdVisibleInter = true;

        if (admobInterstitialAd != null) {
            displayAdmobAd(activity, eventListener);
        } else if (adxInterstitialAd != null) {
            displayAdxAd(activity, eventListener);
        } else {
            triggerAdCallback(eventListener);
        }
    }

    // ================== ADMOB ==================

    private static void initializeAdmobAd(Context context) {
        if (admobInterstitialAd == null) {
            AdRequest adRequest = new AdRequest.Builder().build();
            InterstitialAd.load(context, AppHelper.getGInterTag(), adRequest, new InterstitialAdLoadCallback() {
                @Override
                public void onAdLoaded(@NonNull InterstitialAd interstitialAd) {
                    admobInterstitialAd = interstitialAd;
                }

                @Override
                public void onAdFailedToLoad(@NonNull LoadAdError loadAdError) {
                    admobInterstitialAd = null;
                }
            });
        }
    }

    private static void displayAdmobAd(Activity activity, AdEventListener eventListener) {
        admobInterstitialAd.show(activity);
        admobInterstitialAd.setFullScreenContentCallback(new FullScreenContentCallback() {
            @Override
            public void onAdDismissedFullScreenContent() {
                resetAdData();
                admobInterstitialAd = null;
                initializeAdmobAd(activity);
                triggerAdCallback(eventListener);
            }
        });
    }

    // ================== AdX ==================

    private static void initializeAdxAd(Context context) {
        if (adxInterstitialAd == null) {
            AdManagerAdRequest adRequest = new AdManagerAdRequest.Builder().build();
            AdManagerInterstitialAd.load(context, AppHelper.getAdxInterTag(), adRequest,
                    new AdManagerInterstitialAdLoadCallback() {
                        @Override
                        public void onAdLoaded(@NonNull AdManagerInterstitialAd interstitialAd) {
                            adxInterstitialAd = interstitialAd;
                        }

                        @Override
                        public void onAdFailedToLoad(@NonNull LoadAdError loadAdError) {
                            adxInterstitialAd = null;
                        }
                    });
        }
    }

    private static void displayAdxAd(Activity activity, AdEventListener eventListener) {
        adxInterstitialAd.show(activity);
        adxInterstitialAd.setFullScreenContentCallback(new FullScreenContentCallback() {
            @Override
            public void onAdDismissedFullScreenContent() {
                resetAdData();
                adxInterstitialAd = null;
                initializeAdxAd(activity);
                triggerAdCallback(eventListener);
            }
        });
    }

    // ================== HELPERS ==================

    private static void resetAdData() {
        adClickCount = 0;
        lastAdTimestamp = System.currentTimeMillis();
        MyApp.isAdVisibleInter = false;
    }

    private static void triggerAdCallback(AdEventListener eventListener) {
        if (eventListener != null) {
            eventListener.onAdFinish();
        }
    }
}
