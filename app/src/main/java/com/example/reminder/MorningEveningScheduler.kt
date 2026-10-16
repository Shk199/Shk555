package com.example.reminder

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import java.util.Calendar

object MorningEveningScheduler {

    const val PREFS_NAME = "morning_evening_reminder_prefs"
    const val KEY_ENABLED = "key_me_enabled"
    const val KEY_MORNING_HOUR = "key_morning_hour"
    const val KEY_MORNING_MINUTE = "key_morning_minute"
    const val KEY_EVENING_HOUR = "key_evening_hour"
    const val KEY_EVENING_MINUTE = "key_evening_minute"
    const val KEY_REPEAT_INTERVAL = "key_repeat_interval" // 0: once, 30: every 30m, 60: every 1h

    private const val REQ_MORNING = 3101
    private const val REQ_EVENING = 3102

    fun isEnabled(context: Context): Boolean {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return prefs.getBoolean(KEY_ENABLED, false)
    }

    fun getMorningHour(context: Context): Int {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return prefs.getInt(KEY_MORNING_HOUR, 6)
    }

    fun getMorningMinute(context: Context): Int {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return prefs.getInt(KEY_MORNING_MINUTE, 30)
    }

    fun getEveningHour(context: Context): Int {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return prefs.getInt(KEY_EVENING_HOUR, 17)
    }

    fun getEveningMinute(context: Context): Int {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return prefs.getInt(KEY_EVENING_MINUTE, 0)
    }

    fun getRepeatInterval(context: Context): Int {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return prefs.getInt(KEY_REPEAT_INTERVAL, 0)
    }

    fun updateSettings(
        context: Context,
        enabled: Boolean,
        morningHour: Int = getMorningHour(context),
        morningMinute: Int = getMorningMinute(context),
        eveningHour: Int = getEveningHour(context),
        eveningMinute: Int = getEveningMinute(context),
        repeatInterval: Int = getRepeatInterval(context)
    ) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit()
            .putBoolean(KEY_ENABLED, enabled)
            .putInt(KEY_MORNING_HOUR, morningHour)
            .putInt(KEY_MORNING_MINUTE, morningMinute)
            .putInt(KEY_EVENING_HOUR, eveningHour)
            .putInt(KEY_EVENING_MINUTE, eveningMinute)
            .putInt(KEY_REPEAT_INTERVAL, repeatInterval)
            .apply()

        if (enabled) {
            scheduleAlarms(context, morningHour, morningMinute, eveningHour, eveningMinute)
        } else {
            cancelAlarms(context)
        }
    }

    fun scheduleAlarms(
        context: Context,
        morningHour: Int = getMorningHour(context),
        morningMinute: Int = getMorningMinute(context),
        eveningHour: Int = getEveningHour(context),
        eveningMinute: Int = getEveningMinute(context)
    ) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager ?: return

        // Schedule Morning Alarm
        scheduleSpecificAlarm(
            context = context,
            alarmManager = alarmManager,
            requestCode = REQ_MORNING,
            action = "ACTION_MORNING_REMINDER",
            hour = morningHour,
            minute = morningMinute
        )

        // Schedule Evening Alarm
        scheduleSpecificAlarm(
            context = context,
            alarmManager = alarmManager,
            requestCode = REQ_EVENING,
            action = "ACTION_EVENING_REMINDER",
            hour = eveningHour,
            minute = eveningMinute
        )
    }

    private fun scheduleSpecificAlarm(
        context: Context,
        alarmManager: AlarmManager,
        requestCode: Int,
        action: String,
        hour: Int,
        minute: Int
    ) {
        val intent = Intent(context, MorningEveningReceiver::class.java).apply {
            this.action = action
        }
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            requestCode,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val calendar = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, hour)
            set(Calendar.MINUTE, minute)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
            if (timeInMillis <= System.currentTimeMillis()) {
                add(Calendar.DAY_OF_YEAR, 1)
            }
        }

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                alarmManager.setAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    calendar.timeInMillis,
                    pendingIntent
                )
            } else {
                alarmManager.set(
                    AlarmManager.RTC_WAKEUP,
                    calendar.timeInMillis,
                    pendingIntent
                )
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun rescheduleIfEnabled(context: Context) {
        if (isEnabled(context)) {
            scheduleAlarms(context)
        }
    }

    fun cancelAlarms(context: Context) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager ?: return

        val morningIntent = Intent(context, MorningEveningReceiver::class.java).apply {
            action = "ACTION_MORNING_REMINDER"
        }
        val morningPending = PendingIntent.getBroadcast(
            context,
            REQ_MORNING,
            morningIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        alarmManager.cancel(morningPending)

        val eveningIntent = Intent(context, MorningEveningReceiver::class.java).apply {
            action = "ACTION_EVENING_REMINDER"
        }
        val eveningPending = PendingIntent.getBroadcast(
            context,
            REQ_EVENING,
            eveningIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        alarmManager.cancel(eveningPending)
    }
}
