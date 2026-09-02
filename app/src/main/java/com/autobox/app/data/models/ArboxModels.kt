package com.autobox.app.data.models

import com.google.gson.annotations.SerializedName
import java.time.DayOfWeek
import java.time.LocalTime
import java.util.UUID

// ==========================================
// Authentication Models
// ==========================================

data class LoginRequest(
    @SerializedName("email") val email: String,
    @SerializedName("password") val password: String
)

data class LoginResponse(
    @SerializedName("status") val status: Any? = null,
    @SerializedName("token") val token: String? = null,
    @SerializedName("jwt") val jwt: String? = null,
    @SerializedName("access_token") val accessToken: String? = null,
    @SerializedName("data") val data: UserData? = null,
    @SerializedName("error") val error: String? = null,
    @SerializedName("errorCode") val errorCode: Int? = null,
    @SerializedName("error_code") val errorCodeSnake: Int? = null,
    @SerializedName("message") val message: String? = null,
    @SerializedName("description") val description: String? = null
)

data class UserData(
    @SerializedName("id") val id: Long? = null,
    @SerializedName("user_id") val userId: Long? = null,
    @SerializedName("token") val token: String? = null,
    @SerializedName("refreshToken") val refreshToken: String? = null,
    @SerializedName("jwt") val jwt: String? = null,
    @SerializedName("access_token") val accessToken: String? = null,
    @SerializedName("name") val name: String? = null,
    @SerializedName("first_name") val firstName: String? = null,
    @SerializedName("last_name") val lastName: String? = null,
    @SerializedName("email") val email: String? = null,
    @SerializedName("phone") val phone: String? = null,
    @SerializedName("box_id") val boxId: Long? = null,
    @SerializedName("boxes") val boxes: List<BoxInfo>? = null,
    @SerializedName("memberships") val memberships: List<MembershipInfo>? = null,
    @SerializedName("user") val user: UserNestedDto? = null
)

data class UserNestedDto(
    @SerializedName("id") val id: Long? = null,
    @SerializedName("token") val token: String? = null,
    @SerializedName("name") val name: String? = null,
    @SerializedName("email") val email: String? = null,
    @SerializedName("box_id") val boxId: Long? = null,
    @SerializedName("memberships") val memberships: List<MembershipInfo>? = null
)

data class BoxInfo(
    @SerializedName("id") val id: Long,
    @SerializedName("name") val name: String? = null,
    @SerializedName("address") val address: String? = null
)

data class MembershipInfo(
    @SerializedName("id") val id: Long,
    @SerializedName("box_id") val boxId: Long? = null,
    @SerializedName("name") val name: String? = null,
    @SerializedName("status") val status: String? = null,
    @SerializedName("is_active") val isActive: Boolean? = true
)

// ==========================================
// Schedule Models
// ==========================================

data class ScheduleRequest(
    @SerializedName("from") val from: String,
    @SerializedName("to") val to: String,
    @SerializedName("boxes_id") val boxesId: Long,
    @SerializedName("locations_box_id") val locationsBoxId: Long
)

data class ScheduleResponse(
    @SerializedName("status") val status: Any? = null,
    @SerializedName("data") val data: List<ScheduleDayGroup>? = null,
    @SerializedName("sessions") val sessions: List<SessionDto>? = null
)

data class ScheduleDayGroup(
    @SerializedName("date") val date: String? = null,
    @SerializedName("schedule") val schedule: List<SessionDto>? = null
)

data class SessionDto(
    @SerializedName("id") val id: Long,
    @SerializedName("schedule_id") val scheduleId: Long? = null,
    @SerializedName("box_id") val boxId: Long? = null,
    @SerializedName("name") val name: String? = null,
    @SerializedName("description") val description: String? = null,
    @SerializedName("category") val category: CategoryDto? = null,
    @SerializedName("coach") val coach: CoachDto? = null,
    @SerializedName("time") val time: String? = null, // e.g. "18:00:00" or "2026-08-28 18:00:00"
    @SerializedName("date") val date: String? = null, // e.g. "2026-08-28"
    @SerializedName("duration") val duration: Int? = null, // in minutes
    @SerializedName("max_participants") val maxParticipants: Int? = null,
    @SerializedName("booked_participants") val bookedParticipants: Int? = null,
    @SerializedName("is_booked") val isBooked: Boolean? = false,
    @SerializedName("is_standby") val isStandby: Boolean? = false,
    @SerializedName("booking_open_date") val bookingOpenDate: String? = null,
    @SerializedName("booking_open_days_before") val bookingOpenDaysBefore: Int? = null,
    @SerializedName("booking_open_hours_before") val bookingOpenHoursBefore: Int? = null
)

data class CategoryDto(
    @SerializedName("id") val id: Long? = null,
    @SerializedName("name") val name: String? = null,
    @SerializedName("color") val color: String? = null
)

data class CoachDto(
    @SerializedName("id") val id: Long? = null,
    @SerializedName("name") val name: String? = null
)

// ==========================================
// Booking Models
// ==========================================

data class BookingRequest(
    @SerializedName("session_id") val sessionId: Long,
    @SerializedName("standby") val standby: Boolean = false
)

data class BookingResponse(
    @SerializedName("status") val status: Any? = null,
    @SerializedName("success") val success: Boolean? = null,
    @SerializedName("message") val message: String? = null,
    @SerializedName("data") val data: Any? = null,
    @SerializedName("error") val error: String? = null,
    @SerializedName("errorCode") val errorCode: Int? = null
)

// ==========================================
// Local Domain & Configuration Models
// ==========================================

enum class SnipeStatus {
    SCHEDULED,
    WAITING_COUNTDOWN,
    EXECUTING,
    SUCCESS,
    WAITLISTED,
    FAILED,
    CANCELLED
}

data class BookingRule(
    val id: String = UUID.randomUUID().toString(),
    val dayOfWeek: DayOfWeek,
    val targetTime: LocalTime,
    val classNamePattern: String = "",
    val boxId: Long? = null,
    val enabled: Boolean = true,
    val allowWaitlist: Boolean = true,
    val leadDaysBefore: Int = 1,
    val leadHoursBefore: Int = 24
)

data class ScheduledSnipe(
    val id: String = UUID.randomUUID().toString(),
    val sessionId: Long,
    val boxId: Long,
    val membershipId: Long,
    val className: String,
    val classDateTime: String,
    val bookingOpenEpochMs: Long,
    val alarmEpochMs: Long,
    val status: SnipeStatus = SnipeStatus.SCHEDULED,
    val lastAttemptEpochMs: Long? = null,
    val logMessage: String? = null
)

data class SnipeLog(
    val id: String = UUID.randomUUID().toString(),
    val timestampMs: Long = System.currentTimeMillis(),
    val sessionId: Long,
    val className: String,
    val durationMs: Long,
    val status: SnipeStatus,
    val httpCode: Int? = null,
    val message: String
)

data class SnipeSettings(
    val burstParallelRequests: Int = 4,
    val calibrationOffsetMs: Long = -50L,
    val refreshPreCheckSeconds: Int = 3,
    val autoSyncScheduleHours: Int = 12
)
