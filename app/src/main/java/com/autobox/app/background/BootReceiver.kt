package com.autobox.app.background

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log

class BootReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val action = intent.action
        Log.i(TAG, "BootReceiver received action: $action")

        if (action == Intent.ACTION_BOOT_COMPLETED ||
            action == Intent.ACTION_MY_PACKAGE_REPLACED ||
            action == "android.intent.action.QUICKBOOT_POWERON" ||
            action == "com.htc.intent.action.QUICKBOOT_POWERON"
        ) {
            Log.i(TAG, "Reboot/Update detected: Rescheduling all exact alarms via ScheduleSyncWorker...")
            ScheduleSyncWorker.enqueueImmediateSync(context)
            ScheduleSyncWorker.enqueuePeriodicWork(context)
        }
    }

    companion object {
        private const val TAG = "BootReceiver"
    }
}
