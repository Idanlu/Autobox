package com.autobox.app

import com.autobox.app.data.models.BookingRule
import com.autobox.app.data.models.SessionDto
import com.autobox.app.util.DateTimeUtils
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.DayOfWeek
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId

class DateTimeUtilsTest {

    @Test
    fun testParseSessionDateTime_combinedString() {
        val session = SessionDto(
            id = 101,
            time = "2026-08-28 18:00:00"
        )
        val result = DateTimeUtils.parseSessionDateTime(session)
        assertNotNull(result)
        assertEquals(2026, result?.year)
        assertEquals(8, result?.monthValue)
        assertEquals(28, result?.dayOfMonth)
        assertEquals(18, result?.hour)
        assertEquals(0, result?.minute)
    }

    @Test
    fun testParseSessionDateTime_separateDateAndTime() {
        val session = SessionDto(
            id = 102,
            date = "2026-08-29",
            time = "07:30:00"
        )
        val result = DateTimeUtils.parseSessionDateTime(session)
        assertNotNull(result)
        assertEquals(2026, result?.year)
        assertEquals(8, result?.monthValue)
        assertEquals(29, result?.dayOfMonth)
        assertEquals(7, result?.hour)
        assertEquals(30, result?.minute)
    }

    @Test
    fun testCalculateBookingOpenEpochMs_withRuleLeadDays() {
        val session = SessionDto(
            id = 103,
            date = "2026-08-28",
            time = "18:00:00"
        )
        val rule = BookingRule(
            dayOfWeek = DayOfWeek.FRIDAY,
            targetTime = LocalTime.of(18, 0),
            leadDaysBefore = 1,
            leadHoursBefore = 24
        )

        val zoneId = ZoneId.of("UTC")
        val epochMs = DateTimeUtils.calculateBookingOpenEpochMs(session, rule, zoneId)
        assertNotNull(epochMs)

        // Class is 2026-08-28 18:00 UTC, opening is 24 hours earlier: 2026-08-27 18:00 UTC
        val openLdt = LocalDateTime.ofInstant(java.time.Instant.ofEpochMilli(epochMs!!), zoneId)
        assertEquals(2026, openLdt.year)
        assertEquals(8, openLdt.monthValue)
        assertEquals(27, openLdt.dayOfMonth)
        assertEquals(18, openLdt.hour)
    }

    @Test
    fun testFormatCountdown() {
        val now = 1000000L
        val in10Seconds = now + 10_000L
        val in5Minutes = now + 300_000L
        val in2Hours = now + 7200_000L
        val in2Days = now + (2 * 24 * 3600 * 1000L)
        val passed = now - 10_000L

        assertEquals("Opens in 10s", DateTimeUtils.formatCountdown(in10Seconds, now))
        assertEquals("Opens in 5m 0s", DateTimeUtils.formatCountdown(in5Minutes, now))
        assertEquals("Opens in 2h 0m", DateTimeUtils.formatCountdown(in2Hours, now))
        assertEquals("Opens in 2d 0h", DateTimeUtils.formatCountdown(in2Days, now))
        assertEquals("OPEN NOW", DateTimeUtils.formatCountdown(passed, now))
    }
}
