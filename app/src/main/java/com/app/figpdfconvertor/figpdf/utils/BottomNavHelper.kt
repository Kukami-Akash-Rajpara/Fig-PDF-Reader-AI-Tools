package com.app.figpdfconvertor.figpdf.utils

import com.app.figpdfconvertor.figpdf.R
import np.com.susanthapa.curved_bottom_navigation.CbnMenuItem

object BottomNavHelper {

    @JvmStatic
    fun getMenuItems(): Array<CbnMenuItem> {
        return arrayOf(
            CbnMenuItem(R.drawable.disabled_home, R.drawable.home_active, R.id.nav_home, "Home"),
            CbnMenuItem(
                R.drawable.community,
                R.drawable.home_community_active,
                R.id.nav_community,
                "Community"
            ),
            CbnMenuItem(
                R.drawable.history,
                R.drawable.home_history_active,
                R.id.nav_history,
                "History"
            ),
            CbnMenuItem(
                R.drawable.settings,
                R.drawable.home_settings_active,
                R.id.nav_settings,
                "Settings"
            )
        )
    }
}