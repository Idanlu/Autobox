package com.autobox.app

import android.app.Application
import android.util.Log
import com.autobox.app.background.NotificationHelper
import com.autobox.app.background.ScheduleSyncWorker

class AutoboxApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        Log.i(TAG, "Initializing Autobox Application...")

        // Setup notification channels for snipe execution and background sync
        NotificationHelper.createNotificationChannels(this)

        // Enqueue periodic schedule synchronization worker
        ScheduleSyncWorker.enqueuePeriodicWork(this, repeatHours = 12)
    }

    companion object {
        private const val TAG = "AutoboxApplication"
    }
}
