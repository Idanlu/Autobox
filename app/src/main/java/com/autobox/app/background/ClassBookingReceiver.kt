package com.autobox.app.background

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.PowerManager
import android.util.Log
import com.autobox.app.data.api.NetworkModule
import com.autobox.app.data.local.EncryptedPreferencesManager
import com.autobox.app.data.local.RulesRepository
import com.autobox.app.data.local.SnipeLogsRepository
import com.autobox.app.data.repository.ArboxAuthRepository
import com.autobox.app.data.repository.ArboxBookingRepository
import com.autobox.app.data.repository.ArboxScheduleRepository
import com.autobox.app.data.repository.SnipeExecutionResult
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

class ClassBookingReceiver : BroadcastReceiver() {

    private val receiverScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override fun onReceive(context: Context, intent: Intent) {
        val sessionId = intent.getLongExtra(AlarmScheduler.EXTRA_SESSION_ID, -1L)
        val bookingOpenEpochMs = intent.getLongExtra(AlarmScheduler.EXTRA_BOOKING_OPEN_EPOCH_MS, 0L)
        val membershipId = intent.getLongExtra(AlarmScheduler.EXTRA_MEMBERSHIP_ID, -1L)
        val className = intent.getStringExtra(AlarmScheduler.EXTRA_CLASS_NAME) ?: "Gym Session"

        if (sessionId == -1L || bookingOpenEpochMs == 0L) {
            Log.e(TAG, "Invalid intent extras received in ClassBookingReceiver")
            return
        }

        Log.i(TAG, "T-5s Alarm Triggered for session $sessionId ($className). Acquiring WakeLock...")

        val powerManager = context.getSystemService(Context.POWER_SERVICE) as PowerManager
        val wakeLock = powerManager.newWakeLock(
            PowerManager.PARTIAL_WAKE_LOCK,
            "autobox:ClassBookingWakeLock_$sessionId"
        ).apply {
            // Set 30 seconds safety timeout
            acquire(30_000L)
        }

        val pendingResult = goAsync()

        receiverScope.launch {
            try {
                val prefs = EncryptedPreferencesManager(context.applicationContext)
                val rulesRepo = RulesRepository(context.applicationContext)
                val logsRepo = SnipeLogsRepository(context.applicationContext)
                val authRepo = ArboxAuthRepository(NetworkModule.arboxApiService, prefs)
                val scheduleRepo = ArboxScheduleRepository(NetworkModule.arboxApiService, authRepo, rulesRepo, logsRepo)
                val bookingRepo = ArboxBookingRepository(authRepo, scheduleRepo, logsRepo, prefs)

                val effectiveMembershipId = if (membershipId > 0) membershipId else authRepo.getMembershipId()

                Log.i(TAG, "Executing precision snipe countdown for session $sessionId...")
                val result = bookingRepo.executePrecisionSnipe(
                    sessionId = sessionId,
                    targetOpenEpochMs = bookingOpenEpochMs,
                    membershipId = effectiveMembershipId,
                    className = className,
                    allowWaitlist = true
                )

                when (result) {
                    is SnipeExecutionResult.Success -> {
                        Log.i(TAG, "Snipe Success: ${result.message}")
                        NotificationHelper.showSnipeResultNotification(
                            context = context,
                            sessionId = sessionId,
                            className = className,
                            isSuccess = true,
                            isWaitlisted = false,
                            message = result.message,
                            durationMs = result.durationMs
                        )
                    }
                    is SnipeExecutionResult.Waitlisted -> {
                        Log.w(TAG, "Snipe Waitlisted: ${result.message}")
                        NotificationHelper.showSnipeResultNotification(
                            context = context,
                            sessionId = sessionId,
                            className = className,
                            isSuccess = true,
                            isWaitlisted = true,
                            message = result.message,
                            durationMs = result.durationMs
                        )
                    }
                    is SnipeExecutionResult.Failed -> {
                        Log.e(TAG, "Snipe Failed: ${result.reason}")
                        NotificationHelper.showSnipeResultNotification(
                            context = context,
                            sessionId = sessionId,
                            className = className,
                            isSuccess = false,
                            isWaitlisted = false,
                            message = result.reason,
                            durationMs = result.durationMs
                        )
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Exception during ClassBookingReceiver execution", e)
                NotificationHelper.showSnipeResultNotification(
                    context = context,
                    sessionId = sessionId,
                    className = className,
                    isSuccess = false,
                    isWaitlisted = false,
                    message = e.localizedMessage ?: "Unexpected error",
                    durationMs = 0
                )
            } finally {
                if (wakeLock.isHeld) {
                    wakeLock.release()
                    Log.i(TAG, "WakeLock released for session $sessionId")
                }
                pendingResult.finish()
            }
        }
    }

    companion object {
        private const val TAG = "ClassBookingReceiver"
    }
}
