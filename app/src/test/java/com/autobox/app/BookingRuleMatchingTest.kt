package com.autobox.app

import com.autobox.app.data.models.BookingRule
import com.autobox.app.data.models.CategoryDto
import com.autobox.app.data.models.SessionDto
import com.autobox.app.util.DateTimeUtils
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.DayOfWeek
import java.time.LocalTime

class BookingRuleMatchingTest {

    @Test
    fun testRuleMatching_exactMatch() {
        // Monday 18:00 CrossFit
        val session = SessionDto(
            id = 201,
            date = "2026-08-31", // A Monday
            time = "18:00:00",
            name = "CrossFit WOD",
            category = CategoryDto(id = 1, name = "CrossFit")
        )

        val rule = BookingRule(
            dayOfWeek = DayOfWeek.MONDAY,
            targetTime = LocalTime.of(18, 0),
            classNamePattern = "CrossFit",
            enabled = true
        )

        assertTrue(DateTimeUtils.matchesRule(session, rule))
    }

    @Test
    fun testRuleMatching_wrongDay_fails() {
        // Tuesday 18:00 CrossFit
        val session = SessionDto(
            id = 202,
            date = "2026-09-01", // Tuesday
            time = "18:00:00",
            name = "CrossFit WOD"
        )

        val rule = BookingRule(
            dayOfWeek = DayOfWeek.MONDAY,
            targetTime = LocalTime.of(18, 0),
            classNamePattern = "CrossFit",
            enabled = true
        )

        assertFalse(DateTimeUtils.matchesRule(session, rule))
    }

    @Test
    fun testRuleMatching_wrongTime_fails() {
        // Monday 07:00 CrossFit (Target was 18:00)
        val session = SessionDto(
            id = 203,
            date = "2026-08-31", // Monday
            time = "07:00:00",
            name = "CrossFit WOD"
        )

        val rule = BookingRule(
            dayOfWeek = DayOfWeek.MONDAY,
            targetTime = LocalTime.of(18, 0),
            classNamePattern = "CrossFit",
            enabled = true
        )

        assertFalse(DateTimeUtils.matchesRule(session, rule))
    }

    @Test
    fun testRuleMatching_wrongCategoryPattern_fails() {
        // Monday 18:00 Pilates (Target was CrossFit)
        val session = SessionDto(
            id = 204,
            date = "2026-08-31", // Monday
            time = "18:00:00",
            name = "Reformer Pilates"
        )

        val rule = BookingRule(
            dayOfWeek = DayOfWeek.MONDAY,
            targetTime = LocalTime.of(18, 0),
            classNamePattern = "CrossFit",
            enabled = true
        )

        assertFalse(DateTimeUtils.matchesRule(session, rule))
    }

    @Test
    fun testRuleMatching_disabledRule_fails() {
        val session = SessionDto(
            id = 205,
            date = "2026-08-31",
            time = "18:00:00",
            name = "CrossFit WOD"
        )

        val rule = BookingRule(
            dayOfWeek = DayOfWeek.MONDAY,
            targetTime = LocalTime.of(18, 0),
            classNamePattern = "CrossFit",
            enabled = false
        )

        assertFalse(DateTimeUtils.matchesRule(session, rule))
    }
}
