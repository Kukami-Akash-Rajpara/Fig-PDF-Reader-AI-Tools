package com.app.figpdfconvertor.figpdf.ads

import android.app.Activity
import android.app.Application.ActivityLifecycleCallbacks
import android.os.Bundle
import android.util.Log
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ProcessLifecycleOwner
import com.app.figpdfconvertor.figpdf.utils.MyApp
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.appopen.AppOpenAd
import com.google.android.gms.ads.appopen.AppOpenAd.AppOpenAdLoadCallback


class AppOpenManager(private val myApplication: MyApp) : ActivityLifecycleCallbacks, DefaultLifecycleObserver {
    private var currentActivity: Activity? = null
    var storeRequest: AdRequest? = null
    private var loadCallback: AppOpenAdLoadCallback? = null
    private val tag = "AppOpenManager"
    private var isShowingAd: Boolean = false
    private var appOpenAd: AppOpenAd? = null
    private var showingAd: Boolean = true

    init {
        myApplication.registerActivityLifecycleCallbacks(this)
        ProcessLifecycleOwner.get().lifecycle.addObserver(this)
    }

    override fun onStart(owner: LifecycleOwner) {
        fetchAd()
    }

    private val adRequest: AdRequest
        get() {
            val newAdRequest: AdRequest = if (storeRequest == null) {
                AdRequest.Builder().build()
            } else {
                storeRequest!!
            }
            return newAdRequest
        }

    private val isAdAvailable: Boolean
        get() = appOpenAd != null

    private fun showAdsScreen(): Boolean {
//        return currentActivity is MainActivity || currentActivity is SecondActivity
        return false
    }

    override fun onActivityCreated(activity: Activity, bundle: Bundle?) {}

    override fun onActivityStarted(activity: Activity) {
        currentActivity = activity
    }

    override fun onActivityResumed(activity: Activity) {
        currentActivity = activity
    }

    override fun onActivityPaused(activity: Activity) {}

    override fun onActivityStopped(activity: Activity) {}

    override fun onActivitySaveInstanceState(activity: Activity, bundle: Bundle) {}

    override fun onActivityDestroyed(activity: Activity) {
    }

    private fun fetchAd() {
        if (isAdAvailable) {
            Log.i(tag, "fetchAd: not load ads direct show ")
            showAdIfAvailable()
            return
        } else if (!showingAd || isShowingAd || !showAdsScreen()) {
            Log.i(tag, "fetchAd: not load ads other issue ")
            return
        }

        val request = adRequest
        Log.i(tag, "ads request")
        loadCallback = object : AppOpenAdLoadCallback() {
            override fun onAdLoaded(ad: AppOpenAd) {
                Log.d(tag, "onAdLoaded: ")
                appOpenAd = ad
                showAdIfAvailable()
                storeRequest = null
            }
            
            override fun onAdFailedToLoad(loadAdError: LoadAdError) {
                Log.d(tag, "LoadAdError: " + loadAdError.message)
            }
        }
        storeRequest = request

        AppOpenAd.load(myApplication, "ca-app-pub-3940256099942544/9257395921", request, loadCallback as AppOpenAdLoadCallback)
    }

    fun adNotDisplay() {
        showingAd = false
    }

    fun adDisplay() {
        showingAd = true
    }

    fun showAdIfAvailable() {
        Log.i(tag, "showAdIfAvailable: " + showingAd + " " + isShowingAd + " " + isAdAvailable + " " + showAdsScreen())
        if (!showingAd) return
        /*if (AdsManager.getInstance().isInterstitialShowing()) {
            Log.i(tag, "Interstitial showing, skipping App Open Ad")
            return
        }*/
        if (!isShowingAd && isAdAvailable && showAdsScreen()) {
            val fullScreenContentCallback: FullScreenContentCallback =
                object : FullScreenContentCallback() {
                    override fun onAdDismissedFullScreenContent() {
                        appOpenAd = null
                        isShowingAd = false
                    }

                    override fun onAdFailedToShowFullScreenContent(adError: AdError) {
                    }

                    override fun onAdShowedFullScreenContent() {
                        isShowingAd = true
                    }

                    override fun onAdImpression() {
                        super.onAdImpression()
                        try {
                            checkNotNull(appOpenAd!!.responseInfo.loadedAdapterResponseInfo)
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                    }
                }

            appOpenAd!!.fullScreenContentCallback = fullScreenContentCallback
            appOpenAd!!.show(currentActivity!!)
        }
    }

    fun setShowingAd(showingAd1: Boolean) {
        showingAd = showingAd1
    }
}
