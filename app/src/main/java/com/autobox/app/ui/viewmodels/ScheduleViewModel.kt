package com.autobox.app.ui.viewmodels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.autobox.app.background.AlarmScheduler
import com.autobox.app.background.ScheduleSyncWorker
import com.autobox.app.data.api.NetworkModule
import com.autobox.app.data.local.EncryptedPreferencesManager
import com.autobox.app.data.local.RulesRepository
import com.autobox.app.data.local.SnipeLogsRepository
import com.autobox.app.data.models.ScheduledSnipe
import com.autobox.app.data.models.SessionDto
import com.autobox.app.data.models.SnipeStatus
import com.autobox.app.data.repository.ArboxAuthRepository
import com.autobox.app.data.repository.ArboxBookingRepository
import com.autobox.app.data.repository.ArboxScheduleRepository
import com.autobox.app.data.repository.SnipeExecutionResult
import com.autobox.app.util.DateTimeUtils
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class ScheduleUiState(
    val isLoading: Boolean = false,
    val sessions: List<SessionDto> = emptyList(),
    val errorMessage: String? = null,
    val isSnipingManualId: Long? = null,
    val manualSnipeMessage: String? = null
)

class ScheduleViewModel(application: Application) : AndroidViewModel(application) {

    private val prefs = EncryptedPreferencesManager(application)
    private val rulesRepo = RulesRepository(application)
    private val logsRepo = SnipeLogsRepository(application)
    private val authRepo = ArboxAuthRepository(NetworkModule.arboxApiService, prefs)
    private val scheduleRepo = ArboxScheduleRepository(NetworkModule.arboxApiService, authRepo, rulesRepo, logsRepo)
    private val bookingRepo = ArboxBookingRepository(authRepo, scheduleRepo, logsRepo, prefs)

    private val _uiState = MutableStateFlow(ScheduleUiState())
    val uiState: StateFlow<ScheduleUiState> = _uiState.asStateFlow()

    val scheduledSnipes: StateFlow<List<ScheduledSnipe>> = logsRepo.scheduledSnipesFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    init {
        loadSchedule()
    }

    fun loadSchedule() {
        if (!authRepo.isLoggedIn()) return

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
            val result = scheduleRepo.fetchUpcomingSchedule()
            if (result.isSuccess) {
                val sessions = result.getOrNull() ?: emptyList()
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    sessions = sessions,
                    errorMessage = null
                )
                // Also update background alarms
                val matching = scheduleRepo.computeMatchingSnipes(sessions)
                val scheduledList = mutableListOf<ScheduledSnipe>()
                for ((_, snipe) in matching) {
                    val ok = AlarmScheduler.scheduleExactSnipeAlarm(getApplication(), snipe)
                    if (ok) scheduledList.add(snipe)
                }
                logsRepo.saveScheduledSnipes(scheduledList)
            } else {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = result.exceptionOrNull()?.localizedMessage ?: "Failed to fetch schedule"
                )
            }
        }
    }

    fun manualSnipeNow(session: SessionDto) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isSnipingManualId = session.id, manualSnipeMessage = null)
            val sessionName = session.name ?: session.category?.name ?: "Gym Class"
            // Fire snipe immediately (targetOpenEpochMs = now)
            val result = bookingRepo.executePrecisionSnipe(
                sessionId = session.id,
                targetOpenEpochMs = System.currentTimeMillis(),
                membershipId = authRepo.getMembershipId(),
                className = sessionName,
                allowWaitlist = true
            )

            val msg = when (result) {
                is SnipeExecutionResult.Success -> "Success: ${result.message}"
                is SnipeExecutionResult.Waitlisted -> "Waitlisted: ${result.message}"
                is SnipeExecutionResult.Failed -> "Failed: ${result.reason}"
            }

            _uiState.value = _uiState.value.copy(
                isSnipingManualId = null,
                manualSnipeMessage = msg
            )
            // Reload schedule to reflect updated booking status
            loadSchedule()
        }
    }

    fun scheduleSnipeManually(session: SessionDto) {
        val openEpochMs = DateTimeUtils.calculateBookingOpenEpochMs(session) ?: return
        val sessionName = session.name ?: session.category?.name ?: "Gym Class"
        val sessionLdt = DateTimeUtils.parseSessionDateTime(session)
        val dateStr = sessionLdt?.let { DateTimeUtils.formatLocalDateTime(it) } ?: "Upcoming"

        val snipe = ScheduledSnipe(
            sessionId = session.id,
            boxId = session.boxId ?: authRepo.getBoxId(),
            membershipId = authRepo.getMembershipId(),
            className = sessionName,
            classDateTime = dateStr,
            bookingOpenEpochMs = openEpochMs,
            alarmEpochMs = openEpochMs - 5000L,
            status = SnipeStatus.SCHEDULED
        )

        AlarmScheduler.scheduleExactSnipeAlarm(getApplication(), snipe)
        val current = logsRepo.getScheduledSnipes().toMutableList()
        current.removeAll { it.sessionId == session.id }
        current.add(snipe)
        logsRepo.saveScheduledSnipes(current)
    }

    fun cancelScheduledSnipe(sessionId: Long) {
        AlarmScheduler.cancelSnipeAlarm(getApplication(), sessionId)
        logsRepo.removeScheduledSnipe(sessionId)
    }

    fun clearManualMessage() {
        _uiState.value = _uiState.value.copy(manualSnipeMessage = null)
    }
}
