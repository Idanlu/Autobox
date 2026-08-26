package com.autobox.app.data.local

import android.content.Context
import android.content.SharedPreferences
import com.autobox.app.data.models.BookingRule
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.time.DayOfWeek
import java.time.LocalTime

class RulesRepository(context: Context) {

    private val prefs: SharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    private val gson = Gson()

    private val _rulesFlow = MutableStateFlow<List<BookingRule>>(emptyList())
    val rulesFlow: StateFlow<List<BookingRule>> = _rulesFlow.asStateFlow()

    init {
        loadRules()
    }

    private fun loadRules() {
        val json = prefs.getString(KEY_RULES, null)
        if (json.isNullOrBlank()) {
            val defaults = getDefaultRules()
            _rulesFlow.value = defaults
            saveRules(defaults)
        } else {
            try {
                val type = object : TypeToken<List<BookingRuleModel>>() {}.type
                val serializableList: List<BookingRuleModel> = gson.fromJson(json, type) ?: emptyList()
                _rulesFlow.value = serializableList.map { it.toDomain() }
            } catch (e: Exception) {
                _rulesFlow.value = getDefaultRules()
            }
        }
    }

    fun getAllRules(): List<BookingRule> {
        return _rulesFlow.value
    }

    fun addRule(rule: BookingRule) {
        val updated = _rulesFlow.value + rule
        _rulesFlow.value = updated
        saveRules(updated)
    }

    fun updateRule(updatedRule: BookingRule) {
        val updated = _rulesFlow.value.map { if (it.id == updatedRule.id) updatedRule else it }
        _rulesFlow.value = updated
        saveRules(updated)
    }

    fun deleteRule(ruleId: String) {
        val updated = _rulesFlow.value.filterNot { it.id == ruleId }
        _rulesFlow.value = updated
        saveRules(updated)
    }

    fun toggleRule(ruleId: String, enabled: Boolean) {
        val updated = _rulesFlow.value.map {
            if (it.id == ruleId) it.copy(enabled = enabled) else it
        }
        _rulesFlow.value = updated
        saveRules(updated)
    }

    private fun saveRules(rules: List<BookingRule>) {
        val serializable = rules.map { BookingRuleModel.fromDomain(it) }
        val json = gson.toJson(serializable)
        prefs.edit().putString(KEY_RULES, json).apply()
    }

    private fun getDefaultRules(): List<BookingRule> {
        return listOf(
            BookingRule(
                dayOfWeek = DayOfWeek.MONDAY,
                targetTime = LocalTime.of(18, 0),
                classNamePattern = "CrossFit",
                enabled = true,
                leadDaysBefore = 1,
                leadHoursBefore = 24
            ),
            BookingRule(
                dayOfWeek = DayOfWeek.WEDNESDAY,
                targetTime = LocalTime.of(18, 0),
                classNamePattern = "CrossFit",
                enabled = true,
                leadDaysBefore = 1,
                leadHoursBefore = 24
            ),
            BookingRule(
                dayOfWeek = DayOfWeek.FRIDAY,
                targetTime = LocalTime.of(17, 0),
                classNamePattern = "CrossFit",
                enabled = true,
                leadDaysBefore = 1,
                leadHoursBefore = 24
            )
        )
    }

    companion object {
        private const val PREFS_NAME = "autobox_rules_prefs"
        private const val KEY_RULES = "key_booking_rules"
    }

    // Helper DTO for clean JSON serialization of LocalTime and DayOfWeek
    private data class BookingRuleModel(
        val id: String,
        val dayOfWeek: String,
        val hour: Int,
        val minute: Int,
        val classNamePattern: String,
        val boxId: Long?,
        val enabled: Boolean,
        val allowWaitlist: Boolean,
        val leadDaysBefore: Int,
        val leadHoursBefore: Int
    ) {
        fun toDomain(): BookingRule {
            return BookingRule(
                id = id,
                dayOfWeek = DayOfWeek.valueOf(dayOfWeek),
                targetTime = LocalTime.of(hour, minute),
                classNamePattern = classNamePattern,
                boxId = boxId,
                enabled = enabled,
                allowWaitlist = allowWaitlist,
                leadDaysBefore = leadDaysBefore,
                leadHoursBefore = leadHoursBefore
            )
        }

        companion object {
            fun fromDomain(domain: BookingRule): BookingRuleModel {
                return BookingRuleModel(
                    id = domain.id,
                    dayOfWeek = domain.dayOfWeek.name,
                    hour = domain.targetTime.hour,
                    minute = domain.targetTime.minute,
                    classNamePattern = domain.classNamePattern,
                    boxId = domain.boxId,
                    enabled = domain.enabled,
                    allowWaitlist = domain.allowWaitlist,
                    leadDaysBefore = domain.leadDaysBefore,
                    leadHoursBefore = domain.leadHoursBefore
                )
            }
        }
    }
}
