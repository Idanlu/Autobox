package com.autobox.app.data.local

import android.content.Context
import android.content.SharedPreferences
import com.autobox.app.data.models.ScheduledSnipe
import com.autobox.app.data.models.SnipeLog
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class SnipeLogsRepository(context: Context) {

    private val prefs: SharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    private val gson = Gson()

    private val _logsFlow = MutableStateFlow<List<SnipeLog>>(emptyList())
    val logsFlow: StateFlow<List<SnipeLog>> = _logsFlow.asStateFlow()

    private val _scheduledSnipesFlow = MutableStateFlow<List<ScheduledSnipe>>(emptyList())
    val scheduledSnipesFlow: StateFlow<List<ScheduledSnipe>> = _scheduledSnipesFlow.asStateFlow()

    init {
        loadLogs()
        loadScheduledSnipes()
    }

    private fun loadLogs() {
        val json = prefs.getString(KEY_LOGS, null)
        if (!json.isNullOrBlank()) {
            try {
                val type = object : TypeToken<List<SnipeLog>>() {}.type
                _logsFlow.value = gson.fromJson(json, type) ?: emptyList()
            } catch (e: Exception) {
                _logsFlow.value = emptyList()
            }
        }
    }

    private fun loadScheduledSnipes() {
        val json = prefs.getString(KEY_SCHEDULED, null)
        if (!json.isNullOrBlank()) {
            try {
                val type = object : TypeToken<List<ScheduledSnipe>>() {}.type
                _scheduledSnipesFlow.value = gson.fromJson(json, type) ?: emptyList()
            } catch (e: Exception) {
                _scheduledSnipesFlow.value = emptyList()
            }
        }
    }

    fun addLog(log: SnipeLog) {
        val current = _logsFlow.value.toMutableList()
        current.add(0, log)
        // Keep max 100 recent logs
        val trimmed = if (current.size > 100) current.subList(0, 100) else current
        _logsFlow.value = trimmed
        prefs.edit().putString(KEY_LOGS, gson.toJson(trimmed)).apply()
    }

    fun clearLogs() {
        _logsFlow.value = emptyList()
        prefs.edit().remove(KEY_LOGS).apply()
    }

    fun getScheduledSnipes(): List<ScheduledSnipe> {
        return _scheduledSnipesFlow.value
    }

    fun saveScheduledSnipes(snipes: List<ScheduledSnipe>) {
        _scheduledSnipesFlow.value = snipes
        prefs.edit().putString(KEY_SCHEDULED, gson.toJson(snipes)).apply()
    }

    fun updateScheduledSnipe(updated: ScheduledSnipe) {
        val list = _scheduledSnipesFlow.value.map { if (it.id == updated.id) updated else it }
        saveScheduledSnipes(list)
    }

    fun removeScheduledSnipe(sessionId: Long) {
        val list = _scheduledSnipesFlow.value.filterNot { it.sessionId == sessionId }
        saveScheduledSnipes(list)
    }

    companion object {
        private const val PREFS_NAME = "autobox_snipe_logs_prefs"
        private const val KEY_LOGS = "key_snipe_logs"
        private const val KEY_SCHEDULED = "key_scheduled_snipes"
    }
}
