package com.autobox.app.data.api

import com.google.gson.JsonElement
import com.autobox.app.data.models.BookingRequest
import com.autobox.app.data.models.BookingResponse
import com.autobox.app.data.models.LoginRequest
import com.autobox.app.data.models.LoginResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST

interface ArboxApiService {

    @POST("api/v2/user/login")
    suspend fun login(
        @Body request: LoginRequest
    ): Response<LoginResponse>

    @GET("api/v2/user/profile")
    suspend fun getUserProfile(
        @Header("accesstoken") bearerToken: String
    ): Response<com.autobox.app.data.models.UserProfileResponse>

    @POST("api/v2/schedule/betweenDates")
    suspend fun getSchedule(
        @Header("accesstoken") bearerToken: String,
        @Body request: com.autobox.app.data.models.ScheduleRequest
    ): Response<JsonElement>

    @POST("api/v2/scheduleUser/insert")
    suspend fun bookSession(
        @Header("accesstoken") bearerToken: String,
        @Body request: BookingRequest
    ): Response<BookingResponse>
}
