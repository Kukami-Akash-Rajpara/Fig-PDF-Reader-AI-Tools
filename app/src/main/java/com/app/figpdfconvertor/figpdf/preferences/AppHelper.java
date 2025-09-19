package com.app.figpdfconvertor.figpdf.preferences;


import android.content.Context;

public class AppHelper {
    private static final String KEY_CONSENT_COMPLETED = "consent_completed";

    public static boolean isConsentCompleted() {
        return PrefHelper.getInstance().getBoolean(KEY_CONSENT_COMPLETED, false);
    }

    public static void setConsentCompleted(boolean completed) {
        PrefHelper.getInstance().setBoolean(KEY_CONSENT_COMPLETED, completed);
    }
    public static String getAppUpdateVersionCode() {
        return PrefHelper.getInstance().getString("versionCode", "1");
    }

    public static void setAppUpdateVersionCode(String str) {
        PrefHelper.getInstance().setString("versionCode", str);
    }

    public static Boolean getAdsTag() {
        return PrefHelper.getInstance().getBoolean("adsTag", false);
    }

    public static void setAdsTag(Boolean str) {
        PrefHelper.getInstance().setBoolean("adsTag", str);
    }

    public static String getGInterTag() {
        return PrefHelper.getInstance().getString("gInterTag", "");
    }

    public static void setGInterTag(String str) {
        PrefHelper.getInstance().setString("gInterTag", str);
    }

    public static String getGNativeTag() {
        return PrefHelper.getInstance().getString("gNativeTag", "");
    }

    public static Boolean getShowInterSummarize() {
        return PrefHelper.getInstance().getBoolean("showInterSummarize", false);
    }

    public static void setShowInterSummarize(Boolean str) {
        PrefHelper.getInstance().setBoolean("showInterSummarize", str);
    }

    public static Boolean getShowInterPdfToImage() {
        return PrefHelper.getInstance().getBoolean("showInterPdfToImage", false);
    }

    public static void setShowInterPdfToImage(Boolean str) {
        PrefHelper.getInstance().setBoolean("showInterPdfToImage", str);
    }

    public static Boolean getShowInterOcrDownload() {
        return PrefHelper.getInstance().getBoolean("showInterOcrDownload", false);
    }

    public static void setShowInterOcrDownload(Boolean str) {
        PrefHelper.getInstance().setBoolean("showInterOcrDownload", str);
    }

    public static Boolean getShowInterAnalyzerHiringSubmit() {
        return PrefHelper.getInstance().getBoolean("showInterAnalyzerHiringSubmit", false);
    }

    public static void setShowInterAnalyzerCandidateSubmit(Boolean str) {
        PrefHelper.getInstance().setBoolean("showInterAnalyzerHiringSubmit", str);
    }

    public static Boolean getShowInterAnalyzerCandidateSubmit() {
        return PrefHelper.getInstance().getBoolean("showInterAnalyzerCandidateSubmit", false);
    }

    public static void setShowInterAnalyzerHiringSubmit(Boolean str) {
        PrefHelper.getInstance().setBoolean("showInterAnalyzerCandidateSubmit", str);
    }

    public static Boolean getShowRewardInterviewbotSubmit() {
        return PrefHelper.getInstance().getBoolean("showRewardInterviewbotSubmit", false);
    }

    public static void setShowRewardInterviewbotSubmit(Boolean str) {
        PrefHelper.getInstance().setBoolean("showRewardInterviewbotSubmit", str);
    }

    public static Boolean getShowRewardOcrResult() {
        return PrefHelper.getInstance().getBoolean("showRewardOcrResult", false);
    }

    public static void setShowRewardOcrResult(Boolean str) {
        PrefHelper.getInstance().setBoolean("showRewardOcrResult", str);
    }

    public static Boolean getShowRewardImageToPdf() {
        return PrefHelper.getInstance().getBoolean("showRewardImageToPdf", false);
    }

    public static void setShowRewardImageToPdf(Boolean str) {
        PrefHelper.getInstance().setBoolean("showRewardImageToPdf", str);
    }

    public static void setShowInterWordToPdf(Boolean str) {
        PrefHelper.getInstance().setBoolean("showInterWordToPdf", str);
    }

    public static Boolean getShowInterWordToPdf() {
        return PrefHelper.getInstance().getBoolean("showInterWordToPdf", false);
    }

    public static void setShowInterPptToPdf(Boolean str) {
        PrefHelper.getInstance().setBoolean("showInterPptToPdf", str);
    }

    public static Boolean getShowInterPptToPdf() {
        return PrefHelper.getInstance().getBoolean("showIInterPptToPdf", false);
    }

    public static Boolean getShowNativeLanguage() {
        return PrefHelper.getInstance().getBoolean("showNativeLanguage", false);
    }

    public static void setShowNativeLanguage(Boolean str) {
        PrefHelper.getInstance().setBoolean("showNativeLanguage", str);
    }

    public static String getGRewardedTag() {
        return PrefHelper.getInstance().getString("gRewardedTag", "");
    }

    public static void setGRewardedTag(String str) {
        PrefHelper.getInstance().setString("gRewardedTag", str);
    }
    public static Boolean getShowInterSplash() {
        return PrefHelper.getInstance().getBoolean("showInterSplash", false);
    }

    public static void setShowInterSplash(Boolean str) {
        PrefHelper.getInstance().setBoolean("showInterSplash", str);
    }
    public static void setGNativeTag(String str) {
        PrefHelper.getInstance().setString("gNativeTag", str);
    }
    public static String getInterTimeTag() {
        return PrefHelper.getInstance().getString("interTimeTag", "15");
    }

    public static void setInterTimeTag(String str) {
        PrefHelper.getInstance().setString("interTimeTag", str);
    }
    public static String getGBannerTag() {
        return PrefHelper.getInstance().getString("gBannerTag", "");
    }

    public static void setGBannerTag(String str) {
        PrefHelper.getInstance().setString("gBannerTag", str);
    }

    public static String getLanguageCode() {
        return PrefHelper.getInstance().getString("languageCode", "en");
    }

    public static void setLanguageCode(String str) {
        PrefHelper.getInstance().setString("languageCode", str);
    }

    public static Boolean isFirstTime() {
        return PrefHelper.getInstance().getBoolean("firstTime", true);
    }

    public static void setFirstTime(Boolean bool) {
        PrefHelper.getInstance().setBoolean("firstTime", bool);
    }

    public static Boolean isFirstTimeForAds() {
        return PrefHelper.getInstance().getBoolean("firstTimeAds", true);
    }

    public static void setFirstTisFirstTimeForAdtime(Boolean bool) {
        PrefHelper.getInstance().setBoolean("firstTimeAds", bool);
    }
    public static String getAdxInterTag() {
        return PrefHelper.getInstance().getString("adxInterTag", "");
    }

    public static void setAdxInterTag(String str) {
        PrefHelper.getInstance().setString("adxInterTag", str);
    }



    public static Boolean getAppUnderMaintenance() {
        return PrefHelper.getInstance().getBoolean("appUnderMaintenance", false);
    }

    public static void setAppUnderMaintenance(Boolean bool) {
        PrefHelper.getInstance().setBoolean("appUnderMaintenance", bool);
    }
}