package com.example.bubble

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.core.content.ContextCompat

object SmartTasbeehManager {
    const val PREFS_NAME = "smart_tasbeeh_prefs"
    const val KEY_ENABLED = "key_smart_tasbeeh_enabled"
    const val KEY_AUTO_START_BOOT = "key_smart_tasbeeh_auto_start_boot"
    const val KEY_LAST_X = "key_bubble_last_x"
    const val KEY_LAST_Y = "key_bubble_last_y"
    const val KEY_CURRENT_DHIKR_ID = "key_bubble_current_dhikr_id"
    const val KEY_PENDING_ENABLE = "key_smart_tasbeeh_pending_enable"

    const val ACTION_BUBBLE_STATE_CHANGED = "com.example.bubble.ACTION_STATE_CHANGED"

    fun canDrawOverlays(context: Context): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            Settings.canDrawOverlays(context)
        } else {
            true
        }
    }

    fun setPendingEnable(context: Context, pending: Boolean) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putBoolean(KEY_PENDING_ENABLE, pending).apply()
    }

    fun isPendingEnable(context: Context): Boolean {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return prefs.getBoolean(KEY_PENDING_ENABLE, false)
    }

    fun checkAndResumePendingEnable(context: Context): Boolean {
        if (isPendingEnable(context) && canDrawOverlays(context)) {
            setPendingEnable(context, false)
            setEnabled(context, true)
            return true
        }
        return false
    }

    fun openOverlaySettings(context: Context) {
        setPendingEnable(context, true)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            try {
                val intent = Intent(
                    Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                    Uri.parse("package:${context.packageName}")
                ).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK
                }
                context.startActivity(intent)
            } catch (e: Exception) {
                val fallbackIntent = Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK
                }
                context.startActivity(fallbackIntent)
            }
        }
    }

    fun isEnabled(context: Context): Boolean {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return prefs.getBoolean(KEY_ENABLED, false)
    }

    fun setEnabled(context: Context, enabled: Boolean) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putBoolean(KEY_ENABLED, enabled).apply()
        if (enabled) {
            startBubbleService(context)
        } else {
            stopBubbleService(context)
        }
        notifyStateChanged(context)
    }

    fun isAutoStartOnBootEnabled(context: Context): Boolean {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return prefs.getBoolean(KEY_AUTO_START_BOOT, false)
    }

    fun setAutoStartOnBootEnabled(context: Context, enabled: Boolean) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putBoolean(KEY_AUTO_START_BOOT, enabled).apply()
    }

    fun saveBubblePosition(context: Context, x: Int, y: Int) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit()
            .putInt(KEY_LAST_X, x)
            .putInt(KEY_LAST_Y, y)
            .apply()
    }

    fun getBubblePosition(context: Context): Pair<Int, Int> {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val x = prefs.getInt(KEY_LAST_X, 60)
        val y = prefs.getInt(KEY_LAST_Y, 240)
        return Pair(x, y)
    }

    fun getCurrentDhikrId(context: Context): Int {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return prefs.getInt(KEY_CURRENT_DHIKR_ID, 1)
    }

    fun setCurrentDhikrId(context: Context, id: Int) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putInt(KEY_CURRENT_DHIKR_ID, id).apply()
    }

    fun startBubbleService(context: Context) {
        if (!canDrawOverlays(context)) return
        val intent = Intent(context, TasbihBubbleService::class.java)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            ContextCompat.startForegroundService(context, intent)
        } else {
            context.startService(intent)
        }
    }

    fun stopBubbleService(context: Context) {
        val intent = Intent(context, TasbihBubbleService::class.java)
        context.stopService(intent)
    }

    private fun notifyStateChanged(context: Context) {
        try {
            val intent = Intent(ACTION_BUBBLE_STATE_CHANGED).apply {
                setPackage(context.packageName)
            }
            context.sendBroadcast(intent)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
