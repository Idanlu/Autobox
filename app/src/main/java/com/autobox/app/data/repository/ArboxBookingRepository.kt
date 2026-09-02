package com.autobox.app.data.repository

import com.autobox.app.data.api.ArboxApiService
import com.autobox.app.data.api.NetworkModule
import com.autobox.app.data.local.EncryptedPreferencesManager
import com.autobox.app.data.local.SnipeLogsRepository
import com.autobox.app.data.models.BookingRequest
import com.autobox.app.data.models.BookingResponse
import com.autobox.app.data.models.SnipeLog
import com.autobox.app.data.models.SnipeStatus
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import retrofit2.Response

sealed class SnipeExecutionResult {
    data class Success(val message: String, val durationMs: Long) : SnipeExecutionResult()
    data class Waitlisted(val message: String, val durationMs: Long) : SnipeExecutionResult()
    data class Failed(val reason: String, val httpCode: Int? = null, val durationMs: Long) : SnipeExecutionResult()
}

class ArboxBookingRepository(
    private val authRepo: ArboxAuthRepository,
    private val scheduleRepo: ArboxScheduleRepository,
    private val logsRepo: SnipeLogsRepository,
    private val prefs: EncryptedPreferencesManager
) {

    private val snipeApi: ArboxApiService = NetworkModule.snipeApiService

    /**
     * Executes the high-precision booking countdown and parallel HTTP request burst.
     * Sequence:
     * - T - 5s to T - 3s: Sleep until T - 3s
     * - T - 3s: Query fresh session state to confirm ID
     * - T - 3s to T - 0s: Sleep until T - 50ms (or user calibration offset)
     * - T - 0s: Fire parallel burst requests
     * - T + 1s: Collect first successful response, log and return result.
     */
    suspend fun executePrecisionSnipe(
        sessionId: Long,
        targetOpenEpochMs: Long,
        membershipId: Long = authRepo.getMembershipId(),
        className: String = "Gym Class",
        allowWaitlist: Boolean = true
    ): SnipeExecutionResult = withContext(Dispatchers.IO) {
        val startTime = System.currentTimeMillis()
        val settings = prefs.getSnipeSettings()
        val token = authRepo.getBearerToken() ?: run {
            val log = SnipeLog(
                sessionId = sessionId,
                className = className,
                durationMs = 0,
                status = SnipeStatus.FAILED,
                message = "Not authenticated. Missing Bearer Token."
            )
            logsRepo.addLog(log)
            return@withContext SnipeExecutionResult.Failed(log.message, null, 0)
        }

        // -------------------------------------------------------------
        // Phase 1: Wait until T - 3s and verify session
        // -------------------------------------------------------------
        val tMinus3sEpoch = targetOpenEpochMs - (settings.refreshPreCheckSeconds * 1000L)
        val preCheckDelay = tMinus3sEpoch - System.currentTimeMillis()
        if (preCheckDelay > 0) {
            delay(preCheckDelay)
        }

        // T - 3s Fresh Pre-check
        try {
            val freshSession = scheduleRepo.getFreshSession(sessionId)
            if (freshSession != null && freshSession.isBooked == true) {
                val log = SnipeLog(
                    sessionId = sessionId,
                    className = className,
                    durationMs = System.currentTimeMillis() - startTime,
                    status = SnipeStatus.SUCCESS,
                    message = "Session is already booked!"
                )
                logsRepo.addLog(log)
                logsRepo.removeScheduledSnipe(sessionId)
                return@withContext SnipeExecutionResult.Success(log.message, log.durationMs)
            }
        } catch (_: Exception) {
            // Ignore pre-check failure and continue to snipe
        }

        // -------------------------------------------------------------
        // Phase 2: Precision sleep until T0 + calibration offset
        // -------------------------------------------------------------
        val targetBurstEpoch = targetOpenEpochMs + settings.calibrationOffsetMs
        val remainingWait = targetBurstEpoch - System.currentTimeMillis()

        if (remainingWait > 100) {
            // Coarse delay up to 50ms before target
            delay(remainingWait - 50)
        }

        // High precision spin-wait for the final milliseconds
        while (System.currentTimeMillis() < targetBurstEpoch) {
            // Micro spin loop for sub-millisecond accuracy
            Thread.yield()
        }

        // -------------------------------------------------------------
        // Phase 3: Parallel HTTP Burst (T - 0s)
        // -------------------------------------------------------------
        val burstCount = settings.burstParallelRequests.coerceIn(1, 8)
        val burstStartTime = System.currentTimeMillis()

        val results = coroutineScope {
            (0 until burstCount).map { index ->
                async(Dispatchers.IO) {
                    if (index > 0) {
                        // Micro-stagger subsequent requests by 25ms to avoid exact duplicate collision
                        delay((index * 25).toLong())
                    }
                    performSingleBooking(token, membershipId, sessionId, allowWaitlist)
                }
            }.awaitAll()
        }

        val totalDurationMs = System.currentTimeMillis() - burstStartTime

        // -------------------------------------------------------------
        // Phase 4: Parse & Analyze Response
        // -------------------------------------------------------------
        val firstSuccess = results.firstOrNull { it.isSuccess }
        val executionResult = if (firstSuccess != null) {
            val responseBody = firstSuccess.body
            val msg = responseBody?.message ?: "Session booked successfully!"
            val isStandby = msg.contains("standby", ignoreCase = true) || msg.contains("waitlist", ignoreCase = true)

            val status = if (isStandby) SnipeStatus.WAITLISTED else SnipeStatus.SUCCESS
            val log = SnipeLog(
                sessionId = sessionId,
                className = className,
                durationMs = totalDurationMs,
                status = status,
                httpCode = 200,
                message = msg
            )
            logsRepo.addLog(log)
            logsRepo.removeScheduledSnipe(sessionId)

            if (isStandby) {
                SnipeExecutionResult.Waitlisted(msg, totalDurationMs)
            } else {
                SnipeExecutionResult.Success(msg, totalDurationMs)
            }
        } else {
            val firstFailure = results.firstOrNull()
            val failMsg = firstFailure?.errorMessage ?: "All parallel booking attempts failed"
            val log = SnipeLog(
                sessionId = sessionId,
                className = className,
                durationMs = totalDurationMs,
                status = SnipeStatus.FAILED,
                httpCode = firstFailure?.httpCode,
                message = failMsg
            )
            logsRepo.addLog(log)

            SnipeExecutionResult.Failed(failMsg, firstFailure?.httpCode, totalDurationMs)
        }

        executionResult
    }

    private suspend fun performSingleBooking(
        token: String,
        membershipId: Long,
        sessionId: Long,
        standby: Boolean
    ): BookingAttemptResult {
        return try {
            val request = BookingRequest(scheduleId = sessionId, membershipUserId = membershipId)
            val response: Response<BookingResponse> = snipeApi.bookSession(token, request)
            if (response.isSuccessful && response.body() != null) {
                val body = response.body()!!
                if (body.status != false && body.error == null) {
                    BookingAttemptResult(isSuccess = true, httpCode = response.code(), body = body)
                } else {
                    BookingAttemptResult(
                        isSuccess = false,
                        httpCode = response.code(),
                        errorMessage = body.message ?: body.error ?: "API rejected booking"
                    )
                }
            } else {
                BookingAttemptResult(
                    isSuccess = false,
                    httpCode = response.code(),
                    errorMessage = response.errorBody()?.string() ?: "HTTP ${response.code()}"
                )
            }
        } catch (e: Exception) {
            BookingAttemptResult(
                isSuccess = false,
                httpCode = null,
                errorMessage = e.localizedMessage ?: "Network error"
            )
        }
    }

    private data class BookingAttemptResult(
        val isSuccess: Boolean,
        val httpCode: Int? = null,
        val body: BookingResponse? = null,
        val errorMessage: String? = null
    )
}
