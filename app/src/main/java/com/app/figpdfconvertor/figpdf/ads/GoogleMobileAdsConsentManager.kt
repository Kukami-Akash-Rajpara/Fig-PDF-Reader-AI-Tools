package com.app.figpdfconvertor.figpdf.ads

import android.app.Activity
import android.content.Context
import android.provider.Settings
import android.util.Log
import android.view.View
import com.app.figpdfconvertor.figpdf.activity.MainActivity
import com.google.android.ump.ConsentDebugSettings
import com.google.android.ump.ConsentInformation
import com.google.android.ump.ConsentRequestParameters
import com.google.android.ump.FormError
import com.google.android.ump.UserMessagingPlatform
import java.security.MessageDigest
import java.util.Locale

class GoogleMobileAdsConsentManager private constructor(context: Context) {
    private val consentInformation: ConsentInformation = UserMessagingPlatform.getConsentInformation(context)

    interface OnConsentGatheringCompleteListener {
        fun consentGatheringComplete(error: FormError?)
    }

    fun canRequestAds(): Boolean {
        return consentInformation.canRequestAds()
    }
    fun isConsentRequired(): Boolean {
        return consentInformation.consentStatus == ConsentInformation.ConsentStatus.REQUIRED
    }
    fun gatherConsent(
        activity: Activity,
        onConsentGatheringCompleteListener: OnConsentGatheringCompleteListener
    ) {
        val testId = getTestDeviceHashedId(activity)
        Log.e("UMP-TestDeviceID", "Your hashed test device ID: $testId")
        val debugSettings = ConsentDebugSettings.Builder(activity)
//            .setDebugGeography(ConsentDebugSettings.DebugGeography.DEBUG_GEOGRAPHY_EEA) // Force EEA
//            .addTestDeviceHashedId(testId) // from Logcat
            .build()

        val params = ConsentRequestParameters.Builder()
            .setConsentDebugSettings(debugSettings)
            .build()

        consentInformation.requestConsentInfoUpdate(
            activity,
            params,
            {
                // 🔥 Force loading + showing form
              /*  UserMessagingPlatform.loadConsentForm(
                    activity,
                    { consentForm ->
                        consentForm.show(activity) { formError ->
                            onConsentGatheringCompleteListener.consentGatheringComplete(formError)
                        }
                    },
                    { formError ->
                        onConsentGatheringCompleteListener.consentGatheringComplete(formError)
                    }
                )*/
                UserMessagingPlatform.loadConsentForm(
                    activity,
                    { consentForm ->
                        // Hide progress bar here when form is ready to show
                        if (activity is MainActivity) {
                           // activity.progressBar.visibility = View.GONE
                        }

                        consentForm.show(activity) { formError ->
                            onConsentGatheringCompleteListener.consentGatheringComplete(formError)
                        }
                    },
                    { formError ->
                        // Form failed to load → hide progress
                        if (activity is MainActivity) {
                         //   activity.progressBar.visibility = View.GONE
                        }
                        onConsentGatheringCompleteListener.consentGatheringComplete(formError)
                    }
                )
            },
            { requestConsentError ->
                onConsentGatheringCompleteListener.consentGatheringComplete(requestConsentError)
            }
        )
    }


    fun requestConsentUpdate(activity: Activity?, conSentMangerRequestAd: ConsentMangerRequestAd) {
        /*val id: String =
            Settings.Secure.getString(activity.getContentResolver(), Settings.Secure.ANDROID_ID)
        val deviceId = md5(id).uppercase(Locale.getDefault())*/
        val debugSettings = ConsentDebugSettings.Builder(activity!!)
//            .setDebugGeography(ConsentDebugSettings.DebugGeography.DEBUG_GEOGRAPHY_EEA)
//            .addTestDeviceHashedId("1024CDE787A6EA8E4462E21AC859A13C")
            .build()
        val params = ConsentRequestParameters.Builder()
            .setConsentDebugSettings(debugSettings)
            .build()
        consentInformation.requestConsentInfoUpdate(activity, params, {
            if (canRequestAds()) conSentMangerRequestAd.onSuccess() else conSentMangerRequestAd.onFailed()
        }
        ) { formError ->
            Log.e("AdsConsent", "=====onConsentInfoUpdateFailure======> " + formError.message)
        }
    }

    /*private fun md5(s: String): String {
        try {
            val digest = MessageDigest.getInstance("MD5")
            digest.update(s.toByteArray())
            val messageDigest = digest.digest()

            val hexString = StringBuffer()
            for (i in messageDigest.indices) {
                var h = Integer.toHexString(0xFF and messageDigest[i].toInt())
                while (h.length < 2) h = "0$h"
                hexString.append(h)
            }
            return hexString.toString()
        } catch (e: NoSuchAlgorithmException) {
            e.printStackTrace()
        }
        return ""
    }*/

    interface ConsentMangerRequestAd {
        fun onSuccess()
        fun onFailed()
    }

    companion object {
        private var instance: GoogleMobileAdsConsentManager? = null

        fun getInstance(context: Context): GoogleMobileAdsConsentManager? {
            if (instance == null) {
                instance = GoogleMobileAdsConsentManager(context)
            }
            return instance
        }
    }

    fun getTestDeviceHashedId(context: Context): String {
        val androidId =
            Settings.Secure.getString(context.contentResolver, Settings.Secure.ANDROID_ID)

        val digest = MessageDigest.getInstance("MD5")
        digest.update(androidId.toByteArray())
        val messageDigest = digest.digest()

        // convert to hex uppercase
        val hexString = StringBuilder()
        for (b in messageDigest) {
            var h = Integer.toHexString(0xFF and b.toInt())
            while (h.length < 2) h = "0$h"
            hexString.append(h)
        }
        return hexString.toString().uppercase(Locale.getDefault())
    }
}