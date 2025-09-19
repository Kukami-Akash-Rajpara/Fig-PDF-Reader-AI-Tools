package com.app.figpdfconvertor.figpdf.utils

import android.R
import android.app.Activity
import android.app.Dialog
import android.view.Gravity
import android.view.WindowManager

object UtilsDialog {
    @JvmStatic
    fun generateDialog(activity: Activity, layoutResId: Int): Dialog? {
        try {
            val dialog = Dialog(activity)
            dialog.setContentView(layoutResId)
            val window = dialog.getWindow()
            if (window != null) {
                window.setLayout(
                    WindowManager.LayoutParams.WRAP_CONTENT,
                    WindowManager.LayoutParams.WRAP_CONTENT
                )
                window.setGravity(Gravity.CENTER)
                window.setBackgroundDrawableResource(R.color.transparent)
            }
            return dialog
        } catch (e: Exception) {
            e.printStackTrace()
            return null
        }
    }

    @JvmStatic
    fun generateDialogFull(activity: Activity, layoutResId: Int): Dialog? {
        try {
            val dialog = Dialog(activity)
            dialog.setContentView(layoutResId)
            val window = dialog.getWindow()
            if (window != null) {
                window.setLayout(
                    WindowManager.LayoutParams.MATCH_PARENT,
                    WindowManager.LayoutParams.WRAP_CONTENT
                )
                window.setGravity(Gravity.CENTER)
                window.setBackgroundDrawableResource(R.color.transparent)
            }
            return dialog
        } catch (e: Exception) {
            e.printStackTrace()
            return null
        }
    }
}