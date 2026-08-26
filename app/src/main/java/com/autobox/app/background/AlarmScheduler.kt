package com.autobox.app.background

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import com.autobox.app.data.models.ScheduledSnipe

object AlarmScheduler {

    private const val TAG = "AlarmScheduler"

    const val EXTRA_SESSION_ID = "extra_session_id"
    const val EXTRA_BOOKING_OPEN_EPOCH_MS = "extra_booking_open_epoch_ms"
    const val EXTRA_MEMBERSHIP_ID = "extra_membership_id"
    const val EXTRA_CLASS_NAME = "extra_class_name"
    const val EXTRA_CLASS_DATE_TIME = "extra_class_date_time"

    /**
     * Sets an exact alarm at T - 5s using AlarmManager.setExactAndAllowWhileIdle().
     */
    fun scheduleExactSnipeAlarm(context: Context, snipe: ScheduledSnipe): Boolean {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager ?: return false

        // Verify Android 12+ permission
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (!alarmManager.canScheduleExactAlarms()) {
                Log.w(TAG, "Cannot schedule exact alarms: permission missing")
                return false
            }
        }

        val intent = Intent(context, ClassBookingReceiver::class.java).apply {
            action = "com.autobox.app.ACTION_SNIPE_${snipe.sessionId}"
            putExtra(EXTRA_SESSION_ID, snipe.sessionId)
            putExtra(EXTRA_BOOKING_OPEN_EPOCH_MS, snipe.bookingOpenEpochMs)
            putExtra(EXTRA_MEMBERSHIP_ID, snipe.membershipId)
            putExtra(EXTRA_CLASS_NAME, snipe.className)
            putExtra(EXTRA_CLASS_DATE_TIME, snipe.classDateTime)
        }

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            snipe.sessionId.toInt(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val triggerAtEpochMs = snipe.alarmEpochMs // Exactly T - 5s

        try {
            alarmManager.setExactAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                triggerAtEpochMs,
                pendingIntent
            )
            Log.i(TAG, "Scheduled exact alarm for session ${snipe.sessionId} (${snipe.className}) at $triggerAtEpochMs")
            return true
        } catch (e: SecurityException) {
            Log.e(TAG, "SecurityException while scheduling exact alarm", e)
            return false
        }
    }

    /**
     * Cancels an existing pending alarm for a session.
     */
    fun cancelSnipeAlarm(context: Context, sessionId: Long) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager ?: return

        val intent = Intent(context, ClassBookingReceiver::class.java).apply {
            action = "com.autobox.app.ACTION_SNIPE_$sessionId"
        }

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            sessionId.toInt(),
            intent,
            PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE
        )

        if (pendingIntent != null) {
            alarmManager.cancel(pendingIntent)
            pendingIntent.cancel()
            Log.i(TAG, "Cancelled exact alarm for session $sessionId")
        }
    }
}
