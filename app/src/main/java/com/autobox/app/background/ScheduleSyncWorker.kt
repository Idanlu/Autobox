package com.autobox.app.background

import android.content.Context
import android.util.Log
import androidx.work.Constraints
import androidx.work.CoroutineWorker
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import com.autobox.app.data.api.NetworkModule
import com.autobox.app.data.local.EncryptedPreferencesManager
import com.autobox.app.data.local.RulesRepository
import com.autobox.app.data.local.SnipeLogsRepository
import com.autobox.app.data.repository.ArboxAuthRepository
import com.autobox.app.data.repository.ArboxScheduleRepository
import java.util.concurrent.TimeUnit

class ScheduleSyncWorker(
    private val appContext: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result {
        Log.i(TAG, "ScheduleSyncWorker started schedule sync...")

        val prefs = EncryptedPreferencesManager(appContext)
        if (!prefs.isLoggedIn()) {
            Log.w(TAG, "User not logged in, skipping schedule sync.")
            return Result.success()
        }

        val rulesRepo = RulesRepository(appContext)
        val logsRepo = SnipeLogsRepository(appContext)
        val authRepo = ArboxAuthRepository(NetworkModule.arboxApiService, prefs)
        val scheduleRepo = ArboxScheduleRepository(NetworkModule.arboxApiService, authRepo, rulesRepo, logsRepo)

        return try {
            val scheduleResult = scheduleRepo.fetchUpcomingSchedule()
            if (scheduleResult.isSuccess) {
                val sessions = scheduleResult.getOrNull() ?: emptyList()
                val matchingSnipes = scheduleRepo.computeMatchingSnipes(sessions)

                val scheduledList = mutableListOf<com.autobox.app.data.models.ScheduledSnipe>()
                var armedCount = 0

                for ((_, snipe) in matchingSnipes) {
                    val scheduled = AlarmScheduler.scheduleExactSnipeAlarm(appContext, snipe)
                    if (scheduled) {
                        armedCount++
                        scheduledList.add(snipe)
                    }
                }

                // Persist the active scheduled snipes
                logsRepo.saveScheduledSnipes(scheduledList)
                Log.i(TAG, "Schedule sync completed. Armed $armedCount exact alarms.")

                NotificationHelper.showSyncUpdateNotification(appContext, armedCount)
                Result.success()
            } else {
                Log.e(TAG, "Schedule fetch failed: ${scheduleResult.exceptionOrNull()?.message}")
                Result.retry()
            }
        } catch (e: Exception) {
            Log.e(TAG, "Exception during ScheduleSyncWorker", e)
            Result.retry()
        }
    }

    companion object {
        private const val TAG = "ScheduleSyncWorker"
        const val PERIODIC_WORK_NAME = "autobox_periodic_schedule_sync"
        const val ONE_TIME_WORK_NAME = "autobox_immediate_schedule_sync"

        fun enqueuePeriodicWork(context: Context, repeatHours: Long = 12) {
            val constraints = Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build()

            val periodicRequest = PeriodicWorkRequestBuilder<ScheduleSyncWorker>(
                repeatHours, TimeUnit.HOURS,
                15, TimeUnit.MINUTES // Flex interval
            )
                .setConstraints(constraints)
                .build()

            WorkManager.getInstance(context).enqueueUniquePeriodicWork(
                PERIODIC_WORK_NAME,
                ExistingPeriodicWorkPolicy.UPDATE,
                periodicRequest
            )
            Log.i(TAG, "Enqueued periodic schedule sync every $repeatHours hours.")
        }

        fun enqueueImmediateSync(context: Context) {
            val constraints = Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build()

            val request = OneTimeWorkRequestBuilder<ScheduleSyncWorker>()
                .setConstraints(constraints)
                .build()

            WorkManager.getInstance(context).enqueueUniqueWork(
                ONE_TIME_WORK_NAME,
                ExistingWorkPolicy.REPLACE,
                request
            )
            Log.i(TAG, "Enqueued immediate schedule sync.")
        }
    }
}
