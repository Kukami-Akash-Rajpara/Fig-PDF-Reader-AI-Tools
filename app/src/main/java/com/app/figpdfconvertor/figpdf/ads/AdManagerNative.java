package com.app.figpdfconvertor.figpdf.ads;


import static com.app.figpdfconvertor.figpdf.ads.ConstantKt.isNetworkAvailable;

import android.app.Activity;
import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.app.figpdfconvertor.figpdf.BuildConfig;
import com.app.figpdfconvertor.figpdf.R;
import com.app.figpdfconvertor.figpdf.preferences.AppHelper;
import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdLoader;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.LoadAdError;
import com.google.android.gms.ads.VideoController;
import com.google.android.gms.ads.VideoOptions;
import com.google.android.gms.ads.nativead.NativeAdOptions;
import com.google.android.gms.ads.nativead.NativeAdView;

import java.util.Objects;

public class AdManagerNative {

    public static com.google.android.gms.ads.nativead.NativeAd googleAdNative = null;
    public static boolean isAdLoadingNative = false;

    public static void initializePreferredAdNetwork(Context context) {
        if (!isNetworkAvailable(context)) return;

        int currentVersionCode = BuildConfig.VERSION_CODE;
        String remoteCodeStr = AppHelper.getAppUpdateVersionCode();

        if (AppHelper.getAdsTag()
                && currentVersionCode != Integer.parseInt(remoteCodeStr)
                && ConstantKt.getGoogleMobileAdsConsentManager() != null) {

            // ✅ Consent check
            if (ConstantKt.getGoogleMobileAdsConsentManager().isConsentRequired()) {
                if (ConstantKt.getGoogleMobileAdsConsentManager().canRequestAds()) {
                    initializeGoogleAd(context);
                }
            } else {
                // ✅ If consent not required → always load ads
                initializeGoogleAd(context);
            }
        }
    }


    public static void renderNativeAdLarge(Activity activity, ViewGroup adContainer, ViewGroup adMainLayout) {
        if (googleAdNative != null && !isAdLoadingNative) {
            fetchGoogleNativeAd(activity, adContainer, adMainLayout, R.layout.ads_native_google_large);
        } else {
            hideAdAndRetry(activity, adContainer, adMainLayout);
        }
    }

    public static void renderNativeAdSmall(Activity activity, ViewGroup adContainer, ViewGroup adMainLayout) {
        if (googleAdNative != null && !isAdLoadingNative) {
            fetchGoogleNativeAd(activity, adContainer, adMainLayout, R.layout.ads_native_google_small);
        } else {
            hideAdAndRetry(activity, adContainer, adMainLayout);
        }
    }
    private static void initializeGoogleAd(Context context) {
        if (isAdLoadingNative || googleAdNative != null) return;

        isAdLoadingNative = true;
        AdLoader.Builder builder = new AdLoader.Builder(context, AppHelper.getGNativeTag());

        builder.forNativeAd(nativeAd -> {
            isAdLoadingNative = false;
            googleAdNative = nativeAd;
        });

        VideoOptions videoOptions = new VideoOptions.Builder().setStartMuted(true).build();
        NativeAdOptions adOptions = new NativeAdOptions.Builder().setVideoOptions(videoOptions).build();
        builder.withNativeAdOptions(adOptions);

        AdLoader adLoader = builder.withAdListener(new AdListener() {
            @Override
            public void onAdFailedToLoad(@NonNull LoadAdError error) {
                googleAdNative = null;
                isAdLoadingNative = false;
            }
        }).build();

        adLoader.loadAd(new AdRequest.Builder().build());
    }

    private static void fetchGoogleNativeAd(Activity activity, ViewGroup adContainer, ViewGroup adMainLayout, int adLayoutResource) {
        adMainLayout.setVisibility(View.VISIBLE);
        adContainer.setVisibility(View.VISIBLE);
        NativeAdView adView = (NativeAdView) activity.getLayoutInflater().inflate(adLayoutResource, adContainer, false);
        populateGoogleNativeAdView(googleAdNative, adView);
        adContainer.removeAllViews();
        adContainer.addView(adView);
        googleAdNative = null;
        isAdLoadingNative = false;
        initializePreferredAdNetwork(activity);
    }

    private static void hideAdAndRetry(Activity activity, ViewGroup adContainer, ViewGroup adMainLayout) {
        initializePreferredAdNetwork(activity);
        adContainer.removeAllViews();
        adContainer.setVisibility(View.GONE);
        adMainLayout.setVisibility(View.GONE);
    }

    public static void populateGoogleNativeAdView(com.google.android.gms.ads.nativead.NativeAd nativeAd, NativeAdView adView) {
        // MediaView is optional (for small native layout we skip it)
        if (adView.findViewById(R.id.ad_media) != null) {
            adView.setMediaView((com.google.android.gms.ads.nativead.MediaView) adView.findViewById(R.id.ad_media));
        }

        adView.setHeadlineView(adView.findViewById(R.id.ad_headline));
        adView.setBodyView(adView.findViewById(R.id.ad_body));
        adView.setCallToActionView(adView.findViewById(R.id.ad_call_to_action));
        adView.setIconView(adView.findViewById(R.id.ad_app_icon));
        adView.setPriceView(adView.findViewById(R.id.ad_price));
        adView.setStarRatingView(adView.findViewById(R.id.ad_stars));
        adView.setStoreView(adView.findViewById(R.id.ad_store));
        adView.setAdvertiserView(adView.findViewById(R.id.ad_advertiser));

        setGoogleNativeAdData(nativeAd, adView);
    }

    private static void setGoogleNativeAdData(com.google.android.gms.ads.nativead.NativeAd nativeAd, NativeAdView adView) {
        if (nativeAd.getHeadline() == null) {
            Objects.requireNonNull(adView.getHeadlineView()).setVisibility(View.INVISIBLE);
        } else {
            Objects.requireNonNull(adView.getHeadlineView()).setVisibility(View.VISIBLE);
            ((TextView) Objects.requireNonNull(adView.getHeadlineView())).setText(nativeAd.getHeadline());
        }

        if (nativeAd.getMediaContent() == null) {
            Objects.requireNonNull(adView.getMediaView()).setVisibility(View.INVISIBLE);
        } else {
            Objects.requireNonNull(adView.getMediaView()).setVisibility(View.VISIBLE);
            Objects.requireNonNull(adView.getMediaView()).setMediaContent(nativeAd.getMediaContent());
        }

        if (nativeAd.getBody() == null) {
            Objects.requireNonNull(adView.getBodyView()).setVisibility(View.INVISIBLE);
        } else {
            Objects.requireNonNull(adView.getBodyView()).setVisibility(View.VISIBLE);
            ((TextView) adView.getBodyView()).setText(nativeAd.getBody());
        }

        if (nativeAd.getCallToAction() == null) {
            Objects.requireNonNull(adView.getCallToActionView()).setVisibility(View.GONE);
        } else {
            Objects.requireNonNull(adView.getCallToActionView()).setVisibility(View.VISIBLE);
            ((TextView) adView.getCallToActionView()).setText(nativeAd.getCallToAction());
        }

        if (nativeAd.getIcon() == null) {
            Objects.requireNonNull(adView.getIconView()).setVisibility(View.GONE);
        } else {
            if (Objects.requireNonNull(adView.getIconView()).getVisibility() == View.VISIBLE) {
                ((ImageView) Objects.requireNonNull(adView.getIconView())).setImageDrawable(
                        nativeAd.getIcon().getDrawable());
                adView.getIconView().setVisibility(View.VISIBLE);
            }
        }

        if (nativeAd.getPrice() == null) {
            Objects.requireNonNull(adView.getPriceView()).setVisibility(View.GONE);
        } else {
            Objects.requireNonNull(adView.getPriceView()).setVisibility(View.VISIBLE);
            ((TextView) adView.getPriceView()).setText(nativeAd.getPrice());
        }

        if (nativeAd.getStore() == null) {
            Objects.requireNonNull(adView.getStoreView()).setVisibility(View.GONE);
        } else {
            Objects.requireNonNull(adView.getStoreView()).setVisibility(View.VISIBLE);
            ((TextView) adView.getStoreView()).setText(nativeAd.getStore());
        }

        if (nativeAd.getStarRating() == null) {
            Objects.requireNonNull(adView.getStarRatingView()).setVisibility(View.GONE);
        } else {
            ((RatingBar) Objects.requireNonNull(adView.getStarRatingView()))
                    .setRating(nativeAd.getStarRating().floatValue());
            adView.getStarRatingView().setVisibility(View.VISIBLE);
        }

        if (nativeAd.getAdvertiser() == null) {
            Objects.requireNonNull(adView.getAdvertiserView()).setVisibility(View.GONE);
        } else {
            ((TextView) Objects.requireNonNull(adView.getAdvertiserView()))
                    .setText(nativeAd.getAdvertiser());
            adView.getAdvertiserView().setVisibility(View.VISIBLE);
        }

        adView.setNativeAd(nativeAd);

        VideoController vc = Objects.requireNonNull(nativeAd.getMediaContent()).getVideoController();

        if (nativeAd.getMediaContent() != null && nativeAd.getMediaContent().hasVideoContent()) {
            vc.setVideoLifecycleCallbacks(new VideoController.VideoLifecycleCallbacks() {
                @Override
                public void onVideoEnd() {
                    super.onVideoEnd();
                }
            });
        }
    }
}