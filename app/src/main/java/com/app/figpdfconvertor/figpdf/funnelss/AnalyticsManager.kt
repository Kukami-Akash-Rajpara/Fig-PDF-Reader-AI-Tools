package com.app.figpdfconvertor.figpdf.funnelss

import android.content.Context
import android.os.Bundle
import com.google.firebase.analytics.FirebaseAnalytics
import io.appmetrica.analytics.AppMetrica

object AnalyticsManager {

    private var firebase: FirebaseAnalytics? = null

    fun init(context: Context) {
        firebase = FirebaseAnalytics.getInstance(context)
    }

    fun logAppStart(context: Context) {
        if (UserSessionManager.isNewUser(context)) {
            AppMetrica.reportEvent("user_new")
            firebase?.logEvent("user_new", null)
        } else {
            AppMetrica.reportEvent("user_returning")
            firebase?.logEvent("user_returning", null)
        }
        AppMetrica.reportEvent("app_opened")
        firebase?.logEvent("app_opened", null)
    }

    fun logEvent(eventName: String, params: Map<String, Any?>? = null) {
        firebase?.let { fa ->
            val bundle = Bundle()
            params?.forEach { (key, value) ->
                when (value) {
                    is String -> bundle.putString(key, value)
                    is Int -> bundle.putInt(key, value)
                    is Long -> bundle.putLong(key, value)
                    is Double -> bundle.putDouble(key, value)
                    is Float -> bundle.putDouble(key, value.toDouble()) // fixed
                    is Boolean -> bundle.putString(key, value.toString())
                    else -> bundle.putString(key, value?.toString())
                }
            }
            firebase?.logEvent(eventName, bundle)
        }
        if (params != null) AppMetrica.reportEvent(eventName, params)
        else AppMetrica.reportEvent(eventName)
    }
    fun logFunnelStep(stepName: String, params: Map<String, Any?>? = null) {
        val eventName = "funnel_$stepName"
        logEvent(eventName, params)
    }

    fun logError(errorName: String, throwable: Throwable) {
        AppMetrica.reportError(errorName, throwable)

        val bundle = Bundle().apply {
            putString("error_message", throwable.message)
            putString("error_type", throwable.javaClass.simpleName)
        }
        firebase?.logEvent(errorName, bundle)
    }
}
