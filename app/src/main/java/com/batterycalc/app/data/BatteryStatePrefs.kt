package com.batterycalc.app.data

import android.content.Context

class BatteryStatePrefs(context: Context) {
    private val prefs = context.applicationContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    fun isInitialized(): Boolean = prefs.getBoolean(KEY_INITIALIZED, false)

    fun getWasCharging(fallback: Boolean): Boolean =
        if (isInitialized()) prefs.getBoolean(KEY_WAS_CHARGING, fallback) else fallback

    fun setWasCharging(charging: Boolean) {
        prefs.edit()
            .putBoolean(KEY_INITIALIZED, true)
            .putBoolean(KEY_WAS_CHARGING, charging)
            .apply()
    }

    companion object {
        private const val PREFS_NAME = "battery_state"
        private const val KEY_INITIALIZED = "initialized"
        private const val KEY_WAS_CHARGING = "was_charging"
    }
}
