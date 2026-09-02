package com.autobox.app.data.models

import com.google.gson.JsonElement
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
// User Profile Models (api/v2/user/profile)
// ==========================================

data class UserProfileResponse(
    @SerializedName("data") val data: UserProfileData? = null
)

data class UserProfileData(
    @SerializedName("id") val id: Long? = null,
    @SerializedName("email") val email: String? = null,
    @SerializedName("first_name") val firstName: String? = null,
    @SerializedName("last_name") val lastName: String? = null,
    @SerializedName("full_name") val fullName: String? = null,
    @SerializedName("boxes") val boxes: List<Long>? = null,
    @SerializedName("activeBoxes") val activeBoxes: List<Long>? = null,
    @SerializedName("activeLocationsBox") val activeLocationsBox: List<Long>? = null,
    @SerializedName("inactiveBoxes") val inactiveBoxes: List<Long>? = null,
    @SerializedName("allBoxes") val allBoxes: List<Long>? = null,
    @SerializedName("locations") val locations: List<Long>? = null,
    @SerializedName("refreshToken") val refreshToken: String? = null,
    @SerializedName("users_boxes") val usersBoxes: List<UserBoxDto>? = null,
    @SerializedName("lastEndedMembership") val lastEndedMembership: LastEndedMembershipDto? = null
)

data class UserBoxDto(
    @SerializedName("ub_id") val ubId: Long? = null,
    @SerializedName("id") val id: Long? = null,
    @SerializedName("user_fk") val userFk: Long? = null,
    @SerializedName("box_fk") val boxFk: Long? = null,
    @SerializedName("locations_box_fk") val locationsBoxFk: Long? = null,
    @SerializedName("active") val active: Int? = null,
    @SerializedName("first_name") val firstName: String? = null,
    @SerializedName("last_name") val lastName: String? = null,
    @SerializedName("full_name") val fullName: String? = null,
    @SerializedName("box") val box: ProfileBoxDto? = null,
    @SerializedName("locations_box") val locationsBox: ProfileLocationBoxDto? = null,
    @SerializedName("group_connection") val groupConnection: GroupConnectionDto? = null
)

data class ProfileBoxDto(
    @SerializedName("id") val id: Long? = null,
    @SerializedName("name") val name: String? = null,
    @SerializedName("phone") val phone: String? = null,
    @SerializedName("email") val email: String? = null,
    @SerializedName("address") val address: String? = null,
    @SerializedName("city") val city: String? = null
)

data class ProfileLocationBoxDto(
    @SerializedName("id") val id: Long? = null,
    @SerializedName("timezone") val timezone: String? = null
)

data class GroupConnectionDto(
    @SerializedName("id") val id: Long? = null,
    @SerializedName("box_fk") val boxFk: Long? = null,
    @SerializedName("group_members") val groupMembers: List<GroupMemberDto>? = null
)

data class GroupMemberDto(
    @SerializedName("id") val id: Long? = null,
    @SerializedName("user_fk") val userFk: Long? = null,
    @SerializedName("first_name") val firstName: String? = null,
    @SerializedName("last_name") val lastName: String? = null,
    @SerializedName("full_name") val fullName: String? = null,
    @SerializedName("active") val active: Int? = null,
    @SerializedName("locations_box_fk") val locationsBoxFk: Long? = null,
    @SerializedName("memberships") val memberships: List<ProfileMembershipDto>? = null
)

data class ProfileMembershipDto(
    @SerializedName("id") val id: Long? = null,
    @SerializedName("mu_id") val muId: Long? = null,
    @SerializedName("user_fk") val userFk: Long? = null,
    @SerializedName("box_fk") val boxFk: Long? = null,
    @SerializedName("active") val active: Int? = null,
    @SerializedName("name") val name: String? = null,
    @SerializedName("type") val type: String? = null,
    @SerializedName("start") val start: String? = null,
    @SerializedName("end") val end: String? = null
)

data class LastEndedMembershipDto(
    @SerializedName("id") val id: Long? = null,
    @SerializedName("box_fk") val boxFk: Long? = null
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
    @SerializedName("data") val data: JsonElement? = null,
    @SerializedName("sessions") val sessions: JsonElement? = null
)

data class ScheduleDayGroup(
    @SerializedName("date") val date: String? = null,
    @SerializedName("schedule") val schedule: List<SessionDto>? = null
)

data class SessionDto(
    @SerializedName("id") val id: Long,
    @SerializedName("time") val time: String? = null,
    @SerializedName("end_time") val endTime: String? = null,
    @SerializedName("date") val date: String? = null,
    @SerializedName("second_coach_fk") val secondCoachFk: Long? = null,
    @SerializedName("coach_fk") val coachFk: Long? = null,
    @SerializedName("box_category_fk") val boxCategoryFk: Long? = null,
    @SerializedName("locations_box_fk") val locationsBoxFk: Long? = null,
    @SerializedName("box_fk") val boxFk: Long? = null,
    @SerializedName("max_users") val maxUsers: Int? = null,
    @SerializedName("series_fk") val seriesFk: Long? = null,
    @SerializedName("live_link") val liveLink: String? = null,
    @SerializedName("has_spots") val hasSpots: Int? = null,
    @SerializedName("availability_id") val availabilityId: Long? = null,
    @SerializedName("disable_cancellation_time") val disableCancellationTime: Int? = null,
    @SerializedName("enable_late_cancellation") val enableLateCancellation: Int? = null,
    @SerializedName("enable_registration_time") val enableRegistrationTime: Int? = null,
    @SerializedName("status") val status: String? = null,
    @SerializedName("spaces_id") val spacesId: Long? = null,
    @SerializedName("workout_id") val workoutId: Long? = null,
    @SerializedName("late_cancellation") val lateCancellation: Int? = null,
    @SerializedName("past") val past: Int? = null,
    @SerializedName("user_booked") val userBooked: Int? = null,
    @SerializedName("user_in_standby") val userInStandby: Int? = null,
    @SerializedName("stand_by_position") val standbyPosition: Int? = null,
    @SerializedName("booked_users") val bookedUsers: List<BookedUserDto>? = null,
    @SerializedName("stand_by") val standbyCount: Int? = null,
    @SerializedName("free") val free: Int? = null,
    @SerializedName("registered") val registered: Int? = null,
    @SerializedName("booking_option") val bookingOption: String? = null,
    @SerializedName("is_swappable_schedule") val isSwappableSchedule: Boolean? = null,
    @SerializedName("reschedule") val reschedule: Boolean? = null,
    @SerializedName("day_of_week") val dayOfWeek: Int? = null,
    @SerializedName("date_time") val dateTime: SessionDateTimeDto? = null,
    @SerializedName("end_date_time") val endDateTime: SessionDateTimeDto? = null,
    @SerializedName("box") val box: SessionBoxDto? = null,
    @SerializedName("box_categories") val boxCategories: BoxCategoryDto? = null,
    @SerializedName("locations_box") val locationsBox: LocationBoxDto? = null,
    @SerializedName("series") val series: SessionSeriesDto? = null,
    @SerializedName("disable_pages_app") val disablePagesApp: List<DisablePageDto>? = null,
    @SerializedName("spaces") val spaces: SpaceDto? = null,
    @SerializedName("custom_field_value") val customFieldValue: List<Any>? = null,
    @SerializedName("schedule_stand_by") val scheduleStandby: List<Any>? = null,
    @SerializedName("coach") val coach: CoachDto? = null,
    @SerializedName("second_coach") val secondCoach: CoachDto? = null
) {
    val boxId: Long? get() = boxFk
    val name: String? get() = boxCategories?.name
    val category: CategoryDto? get() = boxCategories?.let { CategoryDto(it.id, it.name, it.categoryColor) }
    val duration: Int? get() = boxCategories?.length
    val maxParticipants: Int? get() = maxUsers
    val bookedParticipants: Int? get() = registered
    val isBooked: Boolean get() = userBooked == 1
    val isStandby: Boolean get() = userInStandby == 1
    val bookingOpenDate: String? get() = null
    val bookingOpenDaysBefore: Int? get() = null
    val bookingOpenHoursBefore: Int? get() = enableRegistrationTime
}

data class CategoryDto(
    @SerializedName("id") val id: Long? = null,
    @SerializedName("name") val name: String? = null,
    @SerializedName("color") val color: String? = null
)

data class BookedUserDto(
    @SerializedName("id") val id: Long? = null,
    @SerializedName("schedule_user_id") val scheduleUserId: Long? = null,
    @SerializedName("late_cancellation") val lateCancellation: Int? = null,
    @SerializedName("spot") val spot: Int? = null,
    @SerializedName("membership_user_fk") val membershipUserFk: Long? = null,
    @SerializedName("checked_in") val checkedIn: Int? = null,
    @SerializedName("laravel_through_key") val laravelThroughKey: Long? = null,
    @SerializedName("is_user") val isUser: Boolean? = null
)

data class SessionDateTimeDto(
    @SerializedName("date") val date: String? = null,
    @SerializedName("timezone") val timezone: String? = null
)

data class SessionBoxDto(
    @SerializedName("id") val id: Long? = null,
    @SerializedName("name") val name: String? = null,
    @SerializedName("has_regular_clients") val hasRegularClients: Int? = null,
    @SerializedName("cloudinary_image") val cloudinaryImage: String? = null,
    @SerializedName("phone") val phone: String? = null
)

data class BoxCategoryDto(
    @SerializedName("id") val id: Long? = null,
    @SerializedName("name") val name: String? = null,
    @SerializedName("bio") val bio: String? = null,
    @SerializedName("category_color") val categoryColor: String? = null,
    @SerializedName("length") val length: Int? = null,
    @SerializedName("price") val price: Double? = null,
    @SerializedName("type") val type: Int? = null,
    @SerializedName("trial_limit") val trialLimit: Int? = null,
    @SerializedName("color_name") val colorName: Boolean? = null,
    @SerializedName("membership_types") val membershipTypes: List<Any>? = null,
    @SerializedName("box_categories_groups") val boxCategoriesGroups: List<Any>? = null,
    @SerializedName("redirect_prop") val redirectProp: Any? = null,
    @SerializedName("category_type") val categoryType: CategoryTypeDto? = null
)

data class CategoryTypeDto(
    @SerializedName("id") val id: Long? = null,
    @SerializedName("name") val name: String? = null,
    @SerializedName("created_at") val createdAt: String? = null,
    @SerializedName("updated_at") val updatedAt: String? = null,
    @SerializedName("deleted_at") val deletedAt: String? = null
)

data class CoachDto(
    @SerializedName("id") val id: Long? = null,
    @SerializedName("bio") val bio: String? = null,
    @SerializedName("image") val image: String? = null,
    @SerializedName("user_fk") val userFk: Long? = null,
    @SerializedName("first_name") val firstName: String? = null,
    @SerializedName("last_name") val lastName: String? = null,
    @SerializedName("full_name") val fullName: String? = null,
    @SerializedName("full_name_shorten") val fullNameShorten: String? = null
) {
    val name: String? get() = fullName ?: fullNameShorten
}

data class LocationBoxDto(
    @SerializedName("id") val id: Long? = null,
    @SerializedName("location") val location: String? = null,
    @SerializedName("logo") val logo: String? = null,
    @SerializedName("date_format") val dateFormat: String? = null,
    @SerializedName("time_format") val timeFormat: String? = null,
    @SerializedName("timezone") val timezone: String? = null,
    @SerializedName("debit_block") val debitBlock: Int? = null,
    @SerializedName("medical_cert") val medicalCert: Int? = null,
    @SerializedName("without_waiver") val withoutWaiver: Int? = null,
    @SerializedName("epidemic_statement") val epidemicStatement: Int? = null,
    @SerializedName("min_age_block") val minAgeBlock: Int? = null,
    @SerializedName("gender_block") val genderBlock: Int? = null,
    @SerializedName("address") val address: String? = null,
    @SerializedName("currency_symbol") val currencySymbol: String? = null,
    @SerializedName("has_shop") val hasShop: Boolean? = null
)

data class SessionSeriesDto(
    @SerializedName("id") val id: Long? = null,
    @SerializedName("series_name") val seriesName: String? = null,
    @SerializedName("box_fk") val boxFk: Long? = null,
    @SerializedName("locations_box_fk") val locationsBoxFk: Long? = null,
    @SerializedName("spaces_id") val spacesId: Long? = null,
    @SerializedName("season_id") val seasonId: Long? = null,
    @SerializedName("box_category_fk") val boxCategoryFk: Long? = null,
    @SerializedName("tracks") val tracks: Int? = null,
    @SerializedName("start_date") val startDate: String? = null,
    @SerializedName("end_date") val endDate: String? = null,
    @SerializedName("start_time") val startTime: String? = null,
    @SerializedName("end_time") val endTime: String? = null,
    @SerializedName("price") val price: Double? = null,
    @SerializedName("status") val status: String? = null,
    @SerializedName("day") val day: String? = null,
    @SerializedName("max_users") val maxUsers: Int? = null,
    @SerializedName("min_users") val minUsers: Int? = null,
    @SerializedName("coach_fk") val coachFk: Long? = null,
    @SerializedName("second_coach_fk") val secondCoachFk: Long? = null,
    @SerializedName("live_link") val liveLink: String? = null,
    @SerializedName("gender") val gender: String? = null,
    @SerializedName("min_age") val minAge: Int? = null,
    @SerializedName("max_age") val maxAge: Int? = null,
    @SerializedName("enable_registration_time") val enableRegistrationTime: Int? = null,
    @SerializedName("block_registration_time") val blockRegistrationTime: Int? = null,
    @SerializedName("disable_cancellation_time") val disableCancellationTime: Int? = null,
    @SerializedName("enable_late_cancellation") val enableLateCancellation: Int? = null,
    @SerializedName("transparent") val transparent: Int? = null,
    @SerializedName("register_group_member") val registerGroupMember: Int? = null,
    @SerializedName("allow_mid_booking") val allowMidBooking: Int? = null,
    @SerializedName("custom_field_value") val customFieldValue: List<Any>? = null,
    @SerializedName("membership_types") val membershipTypes: List<Any>? = null
)

data class DisablePageDto(
    @SerializedName("locations_box_id") val locationsBoxId: Long? = null,
    @SerializedName("area") val area: String? = null,
    @SerializedName("section_name") val sectionName: String? = null,
    @SerializedName("laravel_through_key") val laravelThroughKey: Long? = null
)

data class SpaceDto(
    @SerializedName("id") val id: Long? = null,
    @SerializedName("name") val name: String? = null,
    @SerializedName("image") val image: String? = null,
    @SerializedName("locations_box_id") val locationsBoxId: Long? = null,
    @SerializedName("boxes_id") val boxesId: Long? = null,
    @SerializedName("created_at") val createdAt: String? = null,
    @SerializedName("updated_at") val updatedAt: String? = null,
    @SerializedName("deleted_at") val deletedAt: String? = null
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
