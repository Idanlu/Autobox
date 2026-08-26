package com.autobox.app.ui.viewmodels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.autobox.app.background.ScheduleSyncWorker
import com.autobox.app.data.local.EncryptedPreferencesManager
import com.autobox.app.data.local.SnipeLogsRepository
import com.autobox.app.data.models.SnipeLog
import com.autobox.app.data.models.SnipeSettings
import com.autobox.app.util.BatteryOptimizationHelper
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class SettingsUiState(
    val isBatteryWhitelisted: Boolean = true,
    val canScheduleExactAlarms: Boolean = true,
    val settings: SnipeSettings = SnipeSettings()
)

class SettingsViewModel(application: Application) : AndroidViewModel(application) {

    private val prefs = EncryptedPreferencesManager(application)
    private val logsRepo = SnipeLogsRepository(application)

    private val _uiState = MutableStateFlow(
        SettingsUiState(
            isBatteryWhitelisted = BatteryOptimizationHelper.isIgnoringBatteryOptimizations(application),
            canScheduleExactAlarms = BatteryOptimizationHelper.canScheduleExactAlarms(application),
            settings = prefs.getSnipeSettings()
        )
    )
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()

    val logs: StateFlow<List<SnipeLog>> = logsRepo.logsFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun refreshStatus() {
        _uiState.value = _uiState.value.copy(
            isBatteryWhitelisted = BatteryOptimizationHelper.isIgnoringBatteryOptimizations(getApplication()),
            canScheduleExactAlarms = BatteryOptimizationHelper.canScheduleExactAlarms(getApplication()),
            settings = prefs.getSnipeSettings()
        )
    }

    fun updateSettings(settings: SnipeSettings) {
        prefs.saveSnipeSettings(settings)
        _uiState.value = _uiState.value.copy(settings = settings)
    }

    fun setBurstCount(count: Int) {
        val current = _uiState.value.settings
        updateSettings(current.copy(burstParallelRequests = count.coerceIn(1, 8)))
    }

    fun setCalibrationOffset(offsetMs: Long) {
        val current = _uiState.value.settings
        updateSettings(current.copy(calibrationOffsetMs = offsetMs.coerceIn(-500L, 500L)))
    }

    fun triggerManualSync() {
        ScheduleSyncWorker.enqueueImmediateSync(getApplication())
    }

    fun clearLogs() {
        logsRepo.clearLogs()
    }
}
