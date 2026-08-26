package com.autobox.app.data.api

import com.autobox.app.data.models.BookingRequest
import com.autobox.app.data.models.BookingResponse
import com.autobox.app.data.models.LoginRequest
import com.autobox.app.data.models.LoginResponse
import com.autobox.app.data.models.ScheduleResponse
import com.autobox.app.data.models.SessionDto
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

interface ArboxApiService {

    @POST("api/v2/user/login")
    suspend fun login(
        @Body request: LoginRequest
    ): Response<LoginResponse>

    @GET("api/v2/schedule")
    suspend fun getSchedule(
        @Header("Authorization") bearerToken: String,
        @Query("box_id") boxId: Long,
        @Query("startDate") startDate: String,
        @Query("endDate") endDate: String
    ): Response<ScheduleResponse>

    @GET("api/v2/schedule/session/{sessionId}")
    suspend fun getSessionDetails(
        @Header("Authorization") bearerToken: String,
        @Path("sessionId") sessionId: Long
    ): Response<SessionDto>

    @POST("api/v2/user/memberships/{membership_id}/book")
    suspend fun bookSession(
        @Header("Authorization") bearerToken: String,
        @Path("membership_id") membershipId: Long,
        @Body request: BookingRequest
    ): Response<BookingResponse>
}
