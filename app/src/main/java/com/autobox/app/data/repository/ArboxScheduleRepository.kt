package com.autobox.app.data.repository

import com.autobox.app.data.api.ArboxApiService
import com.autobox.app.data.local.RulesRepository
import com.autobox.app.data.local.SnipeLogsRepository
import com.autobox.app.data.models.BookingRule
import com.autobox.app.data.models.ScheduledSnipe
import com.autobox.app.data.models.SessionDto
import com.autobox.app.data.models.SnipeStatus
import com.autobox.app.util.DateTimeUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class ArboxScheduleRepository(
    private val apiService: ArboxApiService,
    private val authRepo: ArboxAuthRepository,
    private val rulesRepo: RulesRepository,
    private val logsRepo: SnipeLogsRepository
) {

    suspend fun fetchUpcomingSchedule(
        boxId: Long = authRepo.getBoxId(),
        daysAhead: Long = 7
    ): Result<List<SessionDto>> = withContext(Dispatchers.IO) {
        val token = authRepo.getBearerToken() ?: return@withContext Result.failure(
            IllegalStateException("User is not authenticated. Please log in first.")
        )

        val (fromDate, toDate) = DateTimeUtils.getScheduleDateRangeStr(daysAhead)
        val request = com.autobox.app.data.models.ScheduleRequest(
            from = fromDate,
            to = toDate,
            boxesId = boxId,
            locationsBoxId = boxId
        )

        try {
            val response = apiService.getSchedule(token, request)
            if (response.isSuccessful && response.body() != null) {
                val body = response.body()!!
                val allSessions = mutableListOf<SessionDto>()

                // Format 1: sessions directly on response
                body.sessions?.let { allSessions.addAll(it) }

                // Format 2: grouped by date
                body.data?.forEach { dayGroup ->
                    dayGroup.schedule?.let { allSessions.addAll(it) }
                }

                Result.success(allSessions.distinctBy { it.id })
            } else if (response.code() == 401) {
                // Token expired - try re-auth
                val refreshed = authRepo.reAuthenticateIfNeeded()
                if (refreshed) {
                    val freshToken = authRepo.getBearerToken() ?: return@withContext Result.failure(Exception("Re-auth failed"))
                    val retryResp = apiService.getSchedule(freshToken, request)
                    if (retryResp.isSuccessful && retryResp.body() != null) {
                        val body = retryResp.body()!!
                        val list = mutableListOf<SessionDto>()
                        body.sessions?.let { list.addAll(it) }
                        body.data?.forEach { it.schedule?.let { s -> list.addAll(s) } }
                        Result.success(list.distinctBy { it.id })
                    } else {
                        Result.failure(Exception("HTTP ${retryResp.code()} after re-auth"))
                    }
                } else {
                    Result.failure(Exception("Session expired, please re-login."))
                }
            } else {
                Result.failure(Exception("Failed to fetch schedule: HTTP ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Matches raw gym sessions against all active user rules and computes target scheduled snipes.
     */
    suspend fun computeMatchingSnipes(
        sessions: List<SessionDto>
    ): List<Pair<SessionDto, ScheduledSnipe>> = withContext(Dispatchers.Default) {
        val rules = rulesRepo.getAllRules().filter { it.enabled }
        val membershipId = authRepo.getMembershipId()
        val defaultBoxId = authRepo.getBoxId()
        val now = System.currentTimeMillis()

        val matchingResults = mutableListOf<Pair<SessionDto, ScheduledSnipe>>()

        for (session in sessions) {
            // Check if already booked
            if (session.isBooked == true) continue

            // Find matching rule
            val matchedRule = rules.firstOrNull { rule -> DateTimeUtils.matchesRule(session, rule) }
            if (matchedRule != null) {
                val openEpochMs = DateTimeUtils.calculateBookingOpenEpochMs(session, matchedRule) ?: continue

                // Only schedule if opening time is in the future
                if (openEpochMs > now) {
                    val alarmEpochMs = openEpochMs - 5000L // T - 5s
                    val sessionName = session.name ?: session.category?.name ?: "Class"
                    val sessionLdt = DateTimeUtils.parseSessionDateTime(session)
                    val classDateTimeStr = sessionLdt?.let { DateTimeUtils.formatLocalDateTime(it) } ?: "Upcoming"

                    val scheduledSnipe = ScheduledSnipe(
                        sessionId = session.id,
                        boxId = session.boxId ?: defaultBoxId,
                        membershipId = membershipId,
                        className = sessionName,
                        classDateTime = classDateTimeStr,
                        bookingOpenEpochMs = openEpochMs,
                        alarmEpochMs = alarmEpochMs,
                        status = SnipeStatus.SCHEDULED
                    )
                    matchingResults.add(Pair(session, scheduledSnipe))
                }
            }
        }

        matchingResults
    }

    suspend fun getFreshSession(sessionId: Long): SessionDto? = withContext(Dispatchers.IO) {
        val token = authRepo.getBearerToken() ?: return@withContext null
        try {
            val resp = apiService.getSessionDetails(token, sessionId)
            if (resp.isSuccessful) resp.body() else null
        } catch (e: Exception) {
            null
        }
    }
}
