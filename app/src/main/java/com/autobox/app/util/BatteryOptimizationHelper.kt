package com.autobox.app.util

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.PowerManager
import android.provider.Settings

object BatteryOptimizationHelper {

    /**
     * Checks if the app is currently whitelisted from OS battery optimizations (Doze mode).
     */
    fun isIgnoringBatteryOptimizations(context: Context): Boolean {
        val powerManager = context.getSystemService(Context.POWER_SERVICE) as? PowerManager ?: return true
        return powerManager.isIgnoringBatteryOptimizations(context.packageName)
    }

    /**
     * Returns an intent to request the user whitelist the app from battery optimizations.
     */
    @SuppressLint("BatteryLife")
    fun createIgnoreBatteryOptimizationsIntent(context: Context): Intent {
        return Intent().apply {
            action = Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS
            data = Uri.parse("package:${context.packageName}")
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
    }

    /**
     * Fallback intent to open the battery optimization settings screen if direct dialog is unsupported.
     */
    fun createBatteryOptimizationSettingsIntent(): Intent {
        return Intent().apply {
            action = Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
    }

    /**
     * Check if exact alarms can be scheduled on Android 12+ (API 31+).
     */
    fun canScheduleExactAlarms(context: Context): Boolean {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as? android.app.AlarmManager
            return alarmManager?.canScheduleExactAlarms() == true
        }
        return true
    }

    /**
     * Intent to open exact alarm permission settings on Android 12+.
     */
    fun createExactAlarmSettingsIntent(context: Context): Intent {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            Intent().apply {
                action = Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM
                data = Uri.parse("package:${context.packageName}")
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
        } else {
            Intent(Settings.ACTION_SETTINGS).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
        }
    }
}
