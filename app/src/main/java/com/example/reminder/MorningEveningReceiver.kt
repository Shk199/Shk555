package com.example.reminder

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import java.util.Calendar

class MorningEveningReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val action = intent.action
        val currentHour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
        val isMorning = action == "ACTION_MORNING_REMINDER" || (action == null && currentHour < 12)

        // Show silent notification only (no sound, no TTS, no chime)
        if (isMorning) {
            MorningEveningNotificationHelper.showMorningNotification(context)
        } else {
            MorningEveningNotificationHelper.showEveningNotification(context)
        }

        // Reschedule if still enabled
        if (MorningEveningScheduler.isEnabled(context)) {
            MorningEveningScheduler.scheduleAlarms(context)
        }
    }
}
