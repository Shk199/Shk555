package com.example.util

import android.content.Context
import android.content.SharedPreferences

object TasbeehSettingsManager {

    private const val PREFS_NAME = "tasbeeh_control_settings"
    private const val KEY_SHAKE_ENABLED = "key_shake_to_count_enabled"
    private const val KEY_VOLUME_ENABLED = "key_volume_keys_to_count_enabled"

    private fun getPrefs(context: Context): SharedPreferences {
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }

    fun isShakeToCountEnabled(context: Context): Boolean {
        return getPrefs(context).getBoolean(KEY_SHAKE_ENABLED, false)
    }

    fun setShakeToCountEnabled(context: Context, enabled: Boolean) {
        getPrefs(context).edit().putBoolean(KEY_SHAKE_ENABLED, enabled).apply()
    }

    fun isVolumeKeysToCountEnabled(context: Context): Boolean {
        return getPrefs(context).getBoolean(KEY_VOLUME_ENABLED, false)
    }

    fun setVolumeKeysToCountEnabled(context: Context, enabled: Boolean) {
        getPrefs(context).edit().putBoolean(KEY_VOLUME_ENABLED, enabled).apply()
    }
}
