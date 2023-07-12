package com.sola.anime.ai.generator.data.manager

import android.app.Notification
import android.app.NotificationChannel
import android.app.PendingIntent
import android.app.TaskStackBuilder
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Build
import android.os.PowerManager
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import androidx.core.content.getSystemService
import com.sola.anime.ai.generator.R
import com.sola.anime.ai.generator.domain.manager.NotificationManager
import com.sola.anime.ai.generator.feature.splash.SplashActivity
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NotificationManagerImpl @Inject constructor(
    private val context: Context
) : NotificationManager {

    companion object {
        const val DEFAULT_CHANNEL_ID = "notifications_default"

        val VIBRATE_PATTERN = longArrayOf(0, 200, 0, 200)
    }

    private val notificationManager =
        context.getSystemService(Context.NOTIFICATION_SERVICE) as android.app.NotificationManager?

    init {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            createNotificationChannel()
        }
    }

    override fun cancelNotify(id: Int) {
        notificationManager?.cancel(id)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun createNotificationChannel(id: Int) {
        if (getNotificationChannel(id) != null) {
            return
        }

        val channel = when (id) {
            0 -> NotificationChannel(
                DEFAULT_CHANNEL_ID,
                "Default",
                android.app.NotificationManager.IMPORTANCE_HIGH
            ).apply {
                enableLights(true)
                lightColor = Color.WHITE
                enableVibration(true)
                vibrationPattern = VIBRATE_PATTERN
            }
            else -> {
                val channelId = buildNotificationChannelId(id)
                NotificationChannel(
                    channelId,
                    "title",
                    android.app.NotificationManager.IMPORTANCE_HIGH
                ).apply {
                    enableLights(true)
                    lightColor = Color.WHITE
                    enableVibration(true)
                    vibrationPattern = VIBRATE_PATTERN
                    lockscreenVisibility = Notification.VISIBILITY_PUBLIC
                }
            }
        }

        notificationManager?.createNotificationChannel(channel)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun getNotificationChannel(
        id: Int
    ): NotificationChannel? {
        val channelId = buildNotificationChannelId(id)

        return notificationManager?.notificationChannels?.find { channel -> channel.id == channelId }
    }

    override fun buildNotificationChannelId(id: Int): String {
        return when (id) {
            0 -> DEFAULT_CHANNEL_ID
            else -> "notifications_$id"
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun getChannelIdForNotification(id: Int): String {
        return getNotificationChannel(id)?.id ?: DEFAULT_CHANNEL_ID
    }

    override fun notify(title: String, body: String) {
        val id = (0..1000).random()

        val contentIntent = Intent(context, SplashActivity::class.java)

        val taskStackBuilder = TaskStackBuilder.create(context)
            .addParentStack(SplashActivity::class.java)
            .addNextIntent(contentIntent)

        val contentPI = taskStackBuilder.getPendingIntent(
            id,
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT else PendingIntent.FLAG_UPDATE_CURRENT
        )

        val notification = NotificationCompat.Builder(context, if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) getChannelIdForNotification(id) else "Notification Manager")
            .setPriority(NotificationCompat.PRIORITY_MAX)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle(title)
            .setContentText(body)
            .setAutoCancel(true)
            .setContentIntent(contentPI)
            .setLights(Color.WHITE, 500, 2000)
            .setWhen(System.currentTimeMillis())
            .setVibrate(VIBRATE_PATTERN)

        notificationManager?.notify(id, notification.build())

        // Wake screen
        context.getSystemService<PowerManager>()?.let { powerManager ->
            if (!powerManager.isInteractive) {
                val flags = PowerManager.SCREEN_DIM_WAKE_LOCK or PowerManager.ACQUIRE_CAUSES_WAKEUP
                val wakeLock = powerManager.newWakeLock(flags, context.packageName)
                wakeLock.acquire(5000)
            }
        }
    }

}