package com.example.bubble

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.example.reminder.MorningEveningScheduler
import com.example.reminder.SalawatScheduler

class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val action = intent.action
        if (action == Intent.ACTION_BOOT_COMPLETED ||
            action == "android.intent.action.QUICKBOOT_POWERON" ||
            action == "com.htc.intent.action.QUICKBOOT_POWERON"
        ) {
            // 1. Reschedule Salawat periodic reminder if enabled
            try {
                SalawatScheduler.rescheduleIfEnabled(context)
            } catch (e: Exception) {
                e.printStackTrace()
            }

            // 2. Reschedule Morning & Evening reminders if enabled
            try {
                MorningEveningScheduler.rescheduleIfEnabled(context)
            } catch (e: Exception) {
                e.printStackTrace()
            }

            // 3. Floating bubble overlay service if enabled and auto-start permitted
            try {
                val autoStart = SmartTasbeehManager.isAutoStartOnBootEnabled(context)
                val isEnabled = SmartTasbeehManager.isEnabled(context)
                if (autoStart && isEnabled && SmartTasbeehManager.canDrawOverlays(context)) {
                    SmartTasbeehManager.startBubbleService(context)
                } else {
                    SmartTasbeehManager.setEnabled(context, false)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}
