package com.autobox.app

import com.autobox.app.data.models.BookingRequest
import com.autobox.app.data.models.SnipeSettings
import com.autobox.app.data.models.SnipeStatus
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class SnipeBurstStrategyTest {

    @Test
    fun testSnipeSettings_defaultValues() {
        val settings = SnipeSettings()
        assertEquals(4, settings.burstParallelRequests)
        assertEquals(-50L, settings.calibrationOffsetMs)
        assertEquals(3, settings.refreshPreCheckSeconds)
        assertEquals(12, settings.autoSyncScheduleHours)
    }

    @Test
    fun testBookingRequest_creation() {
        val request = BookingRequest(sessionId = 998877L, standby = true)
        assertEquals(998877L, request.sessionId)
        assertTrue(request.standby)
    }

    @Test
    fun testSnipeStatus_enumValues() {
        val statusList = SnipeStatus.values().toList()
        assertTrue(statusList.contains(SnipeStatus.SCHEDULED))
        assertTrue(statusList.contains(SnipeStatus.WAITING_COUNTDOWN))
        assertTrue(statusList.contains(SnipeStatus.EXECUTING))
        assertTrue(statusList.contains(SnipeStatus.SUCCESS))
        assertTrue(statusList.contains(SnipeStatus.WAITLISTED))
        assertTrue(statusList.contains(SnipeStatus.FAILED))
    }
}
