package com.example.reminder

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

class SalawatReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        // Play ONLY the included salawat.mp3 audio file
        SalawatAudioPlayer.playSalawatReminderAudio(context)

        // Display the silent heads-up notification
        SalawatNotificationHelper.showSalawatNotification(context)

        // Reschedule next periodic alarm if enabled
        val prefs = context.getSharedPreferences(SalawatScheduler.PREFS_NAME, Context.MODE_PRIVATE)
        val isEnabled = prefs.getBoolean(SalawatScheduler.KEY_ENABLED, false)
        val interval = prefs.getInt(SalawatScheduler.KEY_INTERVAL, 30)
        if (isEnabled) {
            SalawatScheduler.scheduleNextAlarm(context, interval)
        }
    }
}
