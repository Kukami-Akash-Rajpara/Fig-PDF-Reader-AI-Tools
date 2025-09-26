package com.app.figpdfconvertor.figpdf.funnelss

import android.content.Context
import android.os.Bundle
import android.util.Log
import com.google.firebase.analytics.FirebaseAnalytics
import io.appmetrica.analytics.AppMetrica
import com.app.figpdfconvertor.figpdf.BuildConfig

object AnalyticsManager {

    private var firebase: FirebaseAnalytics? = null
    private const val TAG = "AnalyticsManager"

    fun init(context: Context) {
        firebase = FirebaseAnalytics.getInstance(context)
        if (BuildConfig.DEBUG) {
            Log.d(TAG, "FirebaseAnalytics initialized")
        }
    }

    fun logAppStart(context: Context) {
        try {
            if (UserSessionManager.isNewUser(context)) {
                if (!BuildConfig.DEBUG) AppMetrica.reportEvent("user_new")
                firebase?.logEvent("user_new", null)
                if (BuildConfig.DEBUG) Log.d(TAG, "user_new event logged")
            } else {
                if (!BuildConfig.DEBUG) AppMetrica.reportEvent("user_returning")
                firebase?.logEvent("user_returning", null)
                if (BuildConfig.DEBUG) Log.d(TAG, "user_returning event logged")
            }

            if (!BuildConfig.DEBUG) AppMetrica.reportEvent("app_opened")
            firebase?.logEvent("app_opened", null)
            if (BuildConfig.DEBUG) Log.d(TAG, "app_opened event logged")

        } catch (e: Exception) {
            Log.w(TAG, "logAppStart failed: ${e.message}", e)
        }
    }

    fun logEvent(eventName: String, params: Map<String, Any?>? = null) {
        try {
            // Firebase
            firebase?.let { fa ->
                val bundle = Bundle()
                params?.forEach { (key, value) ->
                    when (value) {
                        is String -> bundle.putString(key, value)
                        is Int -> bundle.putInt(key, value)
                        is Long -> bundle.putLong(key, value)
                        is Double -> bundle.putDouble(key, value)
                        is Float -> bundle.putDouble(key, value.toDouble())
                        is Boolean -> bundle.putString(key, value.toString())
                        else -> bundle.putString(key, value?.toString())
                    }
                }
                fa.logEvent(eventName, bundle)
                if (BuildConfig.DEBUG) Log.d(TAG, "Firebase logEvent: $eventName, params=$params")
            }

            // AppMetrica
            if (!BuildConfig.DEBUG) {
                if (params != null) AppMetrica.reportEvent(eventName, params)
                else AppMetrica.reportEvent(eventName)
            } else {
                Log.d(TAG, "AppMetrica skipped in debug: $eventName, params=$params")
            }

        } catch (e: Exception) {
            Log.w(TAG, "logEvent failed: ${e.message}", e)
        }
    }

    fun logFunnelStep(stepName: String, params: Map<String, Any?>? = null) {
        val eventName = "funnel_$stepName"
        logEvent(eventName, params)
    }

    fun logError(errorName: String, throwable: Throwable) {
        try {
            if (!BuildConfig.DEBUG) AppMetrica.reportError(errorName, throwable)

            val bundle = Bundle().apply {
                putString("error_message", throwable.message)
                putString("error_type", throwable.javaClass.simpleName)
            }
            firebase?.logEvent(errorName, bundle)

            if (BuildConfig.DEBUG) {
                Log.d(TAG, "Error logged: $errorName, ${throwable.message}")
            }

        } catch (e: Exception) {
            Log.w(TAG, "logError failed: ${e.message}", e)
        }
    }
}
