package ru.koolmax.cycoffline.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat.getSystemService
import ru.koolmax.cycoffline.R


class DownloadNotifications {
    private val NOTIFICATION_ID = 1
    private val CHANNEL_ID = "notification_channel"
    val manager: NotificationManager
    val builder: NotificationCompat.Builder
    val notification: Notification

    constructor(context: Context) {
        manager = getSystemService(context, NotificationManager::class.java)!!
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "FIT download channel",
                NotificationManager.IMPORTANCE_LOW
            )
            channel.enableVibration(false)
            channel.setSound(null, null)
            //AudioAttributes.Builder().setUsage(AudioAttributes.USAGE_NOTIFICATION).build()
            channel.enableVibration(false)
            manager.createNotificationChannel(channel)
        }

        builder = NotificationCompat.Builder(context, CHANNEL_ID)
            .setContentTitle("Загрузка файлов FIT")
            .setContentText("")
            .setSmallIcon(R.mipmap.ic_launcher_round)
        //.setForegroundServiceBehavior(NotificationCompat.FOREGROUND_SERVICE_IMMEDIATE)
        //.setContentIntent(Intent(context, MainActivity::class.java).let { notificationIntent ->
        //    PendingIntent.getActivity(context, 0, notificationIntent, PendingIntent.FLAG_IMMUTABLE)
        //})
        notification = builder.build()
    }

    fun updateNotification(text: String) {
        val notification = builder.setContentText(text).build()
        manager.notify(NOTIFICATION_ID, notification)
    }
}