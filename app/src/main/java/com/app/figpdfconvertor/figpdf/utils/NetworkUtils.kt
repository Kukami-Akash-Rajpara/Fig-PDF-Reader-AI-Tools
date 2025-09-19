package com.app.figpdfconvertor.figpdf.utils

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities

object NetworkUtils {
    @JvmStatic
    fun isInternetAvailable(context: Context): Boolean {
        val cm = checkNotNull(
            context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        )
        val networkCapabilities = cm.getNetworkCapabilities(cm.getActiveNetwork())
        if (networkCapabilities == null) {
            return false
        } else {
            if (networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)) {
                return true
            } else if (networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR)) {
                return true
            } else {
                return false
            }
        }
    }
}