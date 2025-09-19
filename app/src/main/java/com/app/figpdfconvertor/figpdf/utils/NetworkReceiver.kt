package com.app.figpdfconvertor.figpdf.utils

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.app.figpdfconvertor.figpdf.utils.NetworkUtils.isInternetAvailable

class NetworkReceiver(private val listener: NetworkChangeListener?) : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent?) {
        if (listener != null) {
            listener.onNetworkChange(isInternetAvailable(context))
        }
    }

    interface NetworkChangeListener {
        fun onNetworkChange(isConnected: Boolean)
    }
}