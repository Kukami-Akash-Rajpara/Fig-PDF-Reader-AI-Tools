package com.app.figpdfconvertor.figpdf.ads;


import static com.app.figpdfconvertor.figpdf.ads.ConstantKt.canLoadAds;

import android.app.Activity;
import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;

import com.app.figpdfconvertor.figpdf.BuildConfig;
import com.app.figpdfconvertor.figpdf.preferences.AppHelper;
import com.google.android.gms.ads.AdError;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.FullScreenContentCallback;
import com.google.android.gms.ads.LoadAdError;
import com.google.android.gms.ads.rewarded.RewardedAd;
import com.google.android.gms.ads.rewarded.RewardedAdLoadCallback;

public class AdManagerRewarded {

    public static RewardedAd mRewardedVideoAd;
    public static boolean rewardedAdIsLoading;
    public static boolean userRewarded = false;
    public static AdFinished adFinished;

    public interface AdFinished {
        void onFinish();
    }

    public static void loadRewardAd(Context mActivity) {
        int currentVersionCode = BuildConfig.VERSION_CODE;
        String remoteCodeStr = AppHelper.getAppUpdateVersionCode();

        if (AppHelper.getAdsTag()
                && currentVersionCode != Integer.parseInt(remoteCodeStr)
                && canLoadAds()) {
            loadRewardAd_Admob(mActivity, AppHelper.getGRewardedTag());
        } else {
            Log.d("AdManagerRewarded", "Consent not given OR Ads disabled");
        }
    }

    private static void loadRewardAd_Admob(Context mActivity, String rewardedId) {
        if (rewardedAdIsLoading || mRewardedVideoAd != null) {
            return;
        }
        rewardedAdIsLoading = true;

        AdRequest adRequest = new AdRequest.Builder().build();
        RewardedAd.load(mActivity, rewardedId, adRequest, new RewardedAdLoadCallback() {
            @Override
            public void onAdFailedToLoad(@NonNull LoadAdError loadAdError) {
                Log.d("AdManagerRewarded", "onAdFailedToLoad : " + loadAdError.getMessage());
                mRewardedVideoAd = null;
                rewardedAdIsLoading = false;
            }

            @Override
            public void onAdLoaded(@NonNull RewardedAd rewardedAd) {
                Log.d("AdManagerRewarded", "onAdLoaded : ");
                rewardedAdIsLoading = false;
                mRewardedVideoAd = rewardedAd;
                rewardedContentCallback(mActivity);
            }
        });
    }

    private static void rewardedContentCallback(Context mActivity) {
        mRewardedVideoAd.setFullScreenContentCallback(new FullScreenContentCallback() {
            @Override
            public void onAdDismissedFullScreenContent() {
                super.onAdDismissedFullScreenContent();

                if (userRewarded) {
                    if (adFinished != null) {
                        adFinished.onFinish();
                        adFinished = null;
                    }
                    userRewarded = false;
                }
                mRewardedVideoAd = null;
                rewardedAdIsLoading = false;

                // Load next ad
                loadRewardAd(mActivity);
            }

            @Override
            public void onAdFailedToShowFullScreenContent(@NonNull AdError adError) {
                super.onAdFailedToShowFullScreenContent(adError);

                if (adFinished != null) {
                    adFinished.onFinish();
                    adFinished = null;
                }
                userRewarded = false;
                mRewardedVideoAd = null;
                rewardedAdIsLoading = false;

                loadRewardAd(mActivity);
            }

            @Override
            public void onAdShowedFullScreenContent() {
                super.onAdShowedFullScreenContent();
                userRewarded = false;
            }
        });
    }

    public static void showRewardedAd(Activity mActivity, AdFinished mAdFinished) {
        adFinished = mAdFinished;
        userRewarded = false;

        int currentVersionCode = BuildConfig.VERSION_CODE;
        String remoteCodeStr = AppHelper.getAppUpdateVersionCode();

        if (AppHelper.getAdsTag()
                && currentVersionCode != Integer.parseInt(remoteCodeStr)
                && canLoadAds()) {

            if (mRewardedVideoAd != null && !rewardedAdIsLoading) {
                mRewardedVideoAd.show(mActivity, rewardItem -> userRewarded = true);
            } else {
                if (adFinished != null) {
                    adFinished.onFinish();
                    adFinished = null;
                }
                loadRewardAd(mActivity);
            }
        } else {
            Log.d("AdManagerRewarded", "Consent not given OR Ads disabled. Skipping Rewarded Ad.");
            if (adFinished != null) {
                adFinished.onFinish();
                adFinished = null;
            }
        }
    }

}
