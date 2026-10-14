package com.example.reminder

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.os.Build
import androidx.core.app.NotificationCompat
import com.example.MainActivity
import com.example.R

object MorningEveningNotificationHelper {

    private const val CHANNEL_ID = "morning_evening_silent_channel_v2"
    private const val CHANNEL_NAME = "تذكير أذكار الصباح والمساء"
    private const val NOTIFICATION_ID_MORNING = 3001
    private const val NOTIFICATION_ID_EVENING = 3002

    fun createNotificationChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "إشعارات التذكير بأذكار الصباح والمساء صامتة بدون صوت"
                setSound(null, null)
                enableVibration(false)
                vibrationPattern = null
            }
            val manager = context.getSystemService(NotificationManager::class.java)
            manager?.createNotificationChannel(channel)
        }
    }

    fun showMorningNotification(context: Context) {
        createNotificationChannel(context)

        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent = PendingIntent.getActivity(
            context,
            301,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val largeIcon = try {
            BitmapFactory.decodeStream(context.assets.open("app-logo-192.png"))
        } catch (e: Exception) {
            BitmapFactory.decodeResource(context.resources, R.mipmap.ic_launcher)
        }

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setLargeIcon(largeIcon)
            .setContentTitle("أذكار الصباح 🌅")
            .setContentText("حان وقت أذكار الصباح 🌅 - لا تنسَ ذكر الله")
            .setStyle(
                NotificationCompat.BigTextStyle()
                    .bigText("حان وقت أذكار الصباح 🌅 - لا تنسَ ذكر الله\n«أَصْبَحْنَا وَأَصْبَحَ الْمُلْكُ لِلَّهِ وَالْحَمْدُ لِلَّهِ»")
            )
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setCategory(NotificationCompat.CATEGORY_REMINDER)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .setSilent(true)
            .setOnlyAlertOnce(true)
            .setVibrate(null)
            .build()

        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager
        manager?.notify(NOTIFICATION_ID_MORNING, notification)
    }

    fun showEveningNotification(context: Context) {
        createNotificationChannel(context)

        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent = PendingIntent.getActivity(
            context,
            302,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val largeIcon = try {
            BitmapFactory.decodeStream(context.assets.open("app-logo-192.png"))
        } catch (e: Exception) {
            BitmapFactory.decodeResource(context.resources, R.mipmap.ic_launcher)
        }

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setLargeIcon(largeIcon)
            .setContentTitle("أذكار المساء 🌆")
            .setContentText("حان وقت أذكار المساء 🌆 - لا تنسَ ذكر الله")
            .setStyle(
                NotificationCompat.BigTextStyle()
                    .bigText("حان وقت أذكار المساء 🌆 - لا تنسَ ذكر الله\n«أَمْسَيْنَا وَأَمْسَى الْمُلْكُ لِلَّهِ وَالْحَمْدُ لِلَّهِ»")
            )
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setCategory(NotificationCompat.CATEGORY_REMINDER)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .setSilent(true)
            .setOnlyAlertOnce(true)
            .setVibrate(null)
            .build()

        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager
        manager?.notify(NOTIFICATION_ID_EVENING, notification)
    }
}
