package com.example.reminder

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.media.AudioManager
import android.os.Build
import androidx.core.app.NotificationCompat
import com.example.MainActivity
import com.example.R

object SalawatNotificationHelper {

    private const val CHANNEL_ID = "salawat_reminder_channel_v3"
    private const val CHANNEL_NAME = "تذكير بالصلاة على النبي ﷺ"
    private const val NOTIFICATION_ID = 1001

    fun createNotificationChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "إشعارات التذكير بالصلاة على النبي ﷺ"
                setSound(null, null)
                enableVibration(true)
                vibrationPattern = longArrayOf(0, 350, 150, 350)
            }
            val manager = context.getSystemService(NotificationManager::class.java)
            manager?.createNotificationChannel(channel)
        }
    }

    fun showSalawatNotification(context: Context) {
        createNotificationChannel(context)

        val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as? AudioManager
        val ringerMode = audioManager?.ringerMode ?: AudioManager.RINGER_MODE_NORMAL

        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent = PendingIntent.getActivity(
            context,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val largeIcon = try {
            BitmapFactory.decodeStream(context.assets.open("app-logo-192.png"))
        } catch (e: Exception) {
            BitmapFactory.decodeResource(context.resources, R.mipmap.ic_launcher)
        }

        val builder = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setLargeIcon(largeIcon)
            .setContentTitle("صَلِّ عَلَى مُحَمَّدٍ ﷺ")
            .setContentText("صَلِّ عَلَى مُحَمَّدٍ ﷺ - أذكرك بالصلاة على النبي الآن")
            .setStyle(
                NotificationCompat.BigTextStyle()
                    .bigText("صَلِّ عَلَى مُحَمَّدٍ ﷺ - أذكرك بالصلاة على النبي الآن\nاللَّهُمَّ صَلِّ وَسَلِّمْ عَلَى نَبِيِّنَا مُحَمَّدٍ")
            )
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setCategory(NotificationCompat.CATEGORY_REMINDER)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .setOnlyAlertOnce(true)

        when (ringerMode) {
            AudioManager.RINGER_MODE_SILENT -> {
                // Completely silent: no sound, no vibration
                builder.setSilent(true)
                builder.setVibrate(null)
            }
            AudioManager.RINGER_MODE_VIBRATE -> {
                // Vibrate only: no sound, vibrate pattern active
                builder.setSilent(false)
                builder.setVibrate(longArrayOf(0, 350, 150, 350))
            }
            else -> {
                // Normal mode: audio is played by SalawatAudioPlayer, notification appears cleanly
                builder.setSilent(true)
            }
        }

        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager
        manager?.notify(NOTIFICATION_ID, builder.build())
    }
}
