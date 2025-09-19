package com.app.figpdfconvertor.figpdf.funnelss

import android.content.Context

object UserSessionManager {
    private const val PREFS_NAME = "app_prefs"
    private const val KEY_FIRST_LAUNCH = "first_launch"

    fun isNewUser(context: Context): Boolean {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val isFirst = prefs.getBoolean(KEY_FIRST_LAUNCH, true)
        if (isFirst) {
            prefs.edit().putBoolean(KEY_FIRST_LAUNCH, false).apply()
        }
        return isFirst
    }
}
