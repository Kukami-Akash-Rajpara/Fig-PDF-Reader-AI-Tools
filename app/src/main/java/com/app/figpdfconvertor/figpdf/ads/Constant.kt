package com.app.figpdfconvertor.figpdf.ads

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import java.util.TimeZone


var googleMobileAdsConsentManager: GoogleMobileAdsConsentManager? = null


fun checkCountry(): Boolean {
    val tz: TimeZone = TimeZone.getDefault()
    val timeZoneName: String = tz.id
    return timeZoneName.startsWith("Europe")
}

 fun canLoadAds(): Boolean {
    if (googleMobileAdsConsentManager == null) return false

    if (googleMobileAdsConsentManager!!.isConsentRequired()) {
        // GDPR country -> only if user consented
        return googleMobileAdsConsentManager!!.canRequestAds()
    } else {
        // Non-GDPR country -> always allow
        return true
    }
}
fun isNetworkAvailable(context: Context): Boolean {
    val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
    val network = connectivityManager.activeNetwork ?: return false
    val capabilities = connectivityManager.getNetworkCapabilities(network) ?: return false
    return capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) && capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED)
}
