package com.autobox.app.ui.viewmodels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.autobox.app.background.ScheduleSyncWorker
import com.autobox.app.data.local.RulesRepository
import com.autobox.app.data.models.BookingRule
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class RulesViewModel(application: Application) : AndroidViewModel(application) {

    private val rulesRepo = RulesRepository(application)

    val rules: StateFlow<List<BookingRule>> = rulesRepo.rulesFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun addRule(rule: BookingRule) {
        viewModelScope.launch {
            rulesRepo.addRule(rule)
            triggerSync()
        }
    }

    fun updateRule(rule: BookingRule) {
        viewModelScope.launch {
            rulesRepo.updateRule(rule)
            triggerSync()
        }
    }

    fun deleteRule(ruleId: String) {
        viewModelScope.launch {
            rulesRepo.deleteRule(ruleId)
            triggerSync()
        }
    }

    fun toggleRule(ruleId: String, enabled: Boolean) {
        viewModelScope.launch {
            rulesRepo.toggleRule(ruleId, enabled)
            triggerSync()
        }
    }

    private fun triggerSync() {
        ScheduleSyncWorker.enqueueImmediateSync(getApplication())
    }
}
