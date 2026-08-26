package com.autobox.app.background

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import com.autobox.app.MainActivity
import com.autobox.app.R

object NotificationHelper {

    const val CHANNEL_SNIPER_ID = "autobox_sniper_channel"
    const val CHANNEL_SYNC_ID = "autobox_sync_channel"

    private const val NOTIF_SNIPER_RESULT_BASE_ID = 1000
    private const val NOTIF_SYNC_ID = 2001

    fun createNotificationChannels(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val sniperChannel = NotificationChannel(
                CHANNEL_SNIPER_ID,
                context.getString(R.string.channel_sniper_name),
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = context.getString(R.string.channel_sniper_desc)
                enableVibration(true)
                setShowBadge(true)
            }

            val syncChannel = NotificationChannel(
                CHANNEL_SYNC_ID,
                context.getString(R.string.channel_sync_name),
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = context.getString(R.string.channel_sync_desc)
            }

            val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            manager.createNotificationChannel(sniperChannel)
            manager.createNotificationChannel(syncChannel)
        }
    }

    fun showSnipeResultNotification(
        context: Context,
        sessionId: Long,
        className: String,
        isSuccess: Boolean,
        isWaitlisted: Boolean,
        message: String,
        durationMs: Long
    ) {
        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent = PendingIntent.getActivity(
            context,
            0,
            intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val title = when {
            isWaitlisted -> "⚠️ Waitlisted: $className"
            isSuccess -> "🎯 Snipe Success: $className"
            else -> "❌ Snipe Failed: $className"
        }

        val body = "$message (${durationMs}ms)"

        val notification = NotificationCompat.Builder(context, CHANNEL_SNIPER_ID)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(title)
            .setContentText(body)
            .setStyle(NotificationCompat.BigTextStyle().bigText(body))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .build()

        val notifId = (NOTIF_SNIPER_RESULT_BASE_ID + (sessionId % 1000)).toInt()
        manager.notify(notifId, notification)
    }

    fun showSyncUpdateNotification(
        context: Context,
        scheduledCount: Int
    ) {
        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent = PendingIntent.getActivity(
            context,
            0,
            intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val text = if (scheduledCount > 0) {
            "Schedule synced. Armed $scheduledCount automatic snipe alarms."
        } else {
            "Schedule synced. No new sessions pending booking."
        }

        val notification = NotificationCompat.Builder(context, CHANNEL_SYNC_ID)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle("Autobox Sync")
            .setContentText(text)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .build()

        manager.notify(NOTIF_SYNC_ID, notification)
    }
}
