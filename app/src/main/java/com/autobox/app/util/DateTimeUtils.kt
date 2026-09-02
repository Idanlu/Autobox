package com.autobox.app.util

import com.autobox.app.data.models.BookingRule
import com.autobox.app.data.models.SessionDto
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale

object DateTimeUtils {

    private val DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss", Locale.US)
    private val DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd", Locale.US)
    private val TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm:ss", Locale.US)
    private val TIME_SHORT_FORMATTER = DateTimeFormatter.ofPattern("HH:mm", Locale.US)
    private val DISPLAY_DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("EEE, MMM d, HH:mm", Locale.US)

    /**
     * Parses a session date and time into a LocalDateTime.
     * Handles combined strings ("2026-08-28 18:00:00") or separate date ("2026-08-28") and time ("18:00:00").
     */
    fun parseSessionDateTime(session: SessionDto): LocalDateTime? {
        val timeStr = session.time?.trim() ?: return null
        val dateStr = session.date?.trim()

        return try {
            if (timeStr.contains(" ") || timeStr.contains("T")) {
                val clean = timeStr.replace("T", " ")
                LocalDateTime.parse(clean.take(19), DATE_TIME_FORMATTER)
            } else if (!dateStr.isNullOrBlank()) {
                val localDate = LocalDate.parse(dateStr, DATE_FORMATTER)
                val localTime = if (timeStr.length == 5) {
                    LocalTime.parse(timeStr, TIME_SHORT_FORMATTER)
                } else {
                    LocalTime.parse(timeStr.take(8), TIME_FORMATTER)
                }
                LocalDateTime.of(localDate, localTime)
            } else {
                null
            }
        } catch (e: Exception) {
            null
        }
    }

    /**
     * Computes the exact epoch millisecond when the booking window opens for this session.
     * Priority:
     * 1. session.bookingOpenDate if explicitly returned by API.
     * 2. Derived from session.bookingOpenDaysBefore / bookingOpenHoursBefore.
     * 3. Fallback to rule.leadDaysBefore (default 1 day / 24 hours before class).
     */
    fun calculateBookingOpenEpochMs(
        session: SessionDto,
        matchingRule: BookingRule? = null,
        zoneId: ZoneId = ZoneId.systemDefault()
    ): Long? {
        // 1. Direct API timestamp
        session.bookingOpenDate?.let { openDateStr ->
            try {
                val clean = openDateStr.replace("T", " ").trim()
                val ldt = LocalDateTime.parse(clean.take(19), DATE_TIME_FORMATTER)
                return ldt.atZone(zoneId).toInstant().toEpochMilli()
            } catch (_: Exception) { }
        }

        // 2. Relative offset from session start
        val sessionStart = parseSessionDateTime(session) ?: return null

        val daysBefore = session.bookingOpenDaysBefore
            ?: matchingRule?.leadDaysBefore
            ?: 1

        val hoursBefore = session.bookingOpenHoursBefore
            ?: matchingRule?.leadHoursBefore
            ?: (daysBefore * 24)

        val openDateTime = sessionStart.minusHours(hoursBefore.toLong())
        return openDateTime.atZone(zoneId).toInstant().toEpochMilli()
    }

    /**
     * Formats an epoch millisecond timestamp into a human-readable display string.
     */
    fun formatDateTime(epochMs: Long, zoneId: ZoneId = ZoneId.systemDefault()): String {
        val zdt = ZonedDateTime.ofInstant(java.time.Instant.ofEpochMilli(epochMs), zoneId)
        return zdt.format(DISPLAY_DATE_TIME_FORMATTER)
    }

    /**
     * Formats a LocalDateTime into a clean display string.
     */
    fun formatLocalDateTime(ldt: LocalDateTime): String {
        return ldt.format(DISPLAY_DATE_TIME_FORMATTER)
    }

    /**
     * Produces a dynamic countdown string (e.g., "Opens in 2d 5h", "Opens in 03:45", "OPEN NOW", "PASSED").
     */
    fun formatCountdown(targetEpochMs: Long, currentEpochMs: Long = System.currentTimeMillis()): String {
        val diffMs = targetEpochMs - currentEpochMs
        if (diffMs <= 0) {
            return if (diffMs > -3600_000) "OPEN NOW" else "PASSED"
        }

        val totalSeconds = diffMs / 1000
        val days = totalSeconds / (24 * 3600)
        val hours = (totalSeconds % (24 * 3600)) / 3600
        val minutes = (totalSeconds % 3600) / 60
        val seconds = totalSeconds % 60

        return when {
            days > 0 -> "Opens in ${days}d ${hours}h"
            hours > 0 -> "Opens in ${hours}h ${minutes}m"
            minutes > 0 -> "Opens in ${minutes}m ${seconds}s"
            else -> "Opens in ${seconds}s"
        }
    }

    /**
     * Checks if a session matches a defined BookingRule.
     */
    fun matchesRule(session: SessionDto, rule: BookingRule): Boolean {
        if (!rule.enabled) return false

        // Check box ID if restricted
        if (rule.boxId != null && session.boxId != null && rule.boxId != session.boxId) {
            return false
        }

        val sessionDateTime = parseSessionDateTime(session) ?: return false

        // Match Day of Week
        if (sessionDateTime.dayOfWeek != rule.dayOfWeek) {
            return false
        }

        // Match Time (within +/- 5 minutes tolerance for minor gym schedule variances)
        val sessionTime = sessionDateTime.toLocalTime()
        val diffMinutes = Math.abs(sessionTime.toSecondOfDay() - rule.targetTime.toSecondOfDay()) / 60
        if (diffMinutes > 5) {
            return false
        }

        // Match Class Name Pattern (case-insensitive substring)
        if (rule.classNamePattern.isNotBlank()) {
            val sessionName = session.name ?: session.category?.name ?: ""
            if (!sessionName.contains(rule.classNamePattern, ignoreCase = true)) {
                return false
            }
        }

        return true
    }

    fun getTodayString(): String {
        return LocalDate.now().format(DATE_FORMATTER)
    }

    fun getFutureDateString(daysAhead: Long): String {
        return LocalDate.now().plusDays(daysAhead).format(DATE_FORMATTER)
    }

    fun getScheduleDateRangeStr(daysAhead: Long): Pair<String, String> {
        val now = LocalDate.now()
        val future = now.plusDays(daysAhead)
        val zone = ZoneId.systemDefault()
        
        val fromLdt = now.atStartOfDay()
        val toLdt = future.atTime(LocalTime.MAX)

        val fromStr = fromLdt.atZone(zone).format(DateTimeFormatter.ISO_OFFSET_DATE_TIME)
        val toStr = toLdt.atZone(zone).format(DateTimeFormatter.ISO_OFFSET_DATE_TIME)

        return Pair(fromStr, toStr)
    }
}
