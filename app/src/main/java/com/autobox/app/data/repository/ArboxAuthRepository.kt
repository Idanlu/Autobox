package com.autobox.app.data.repository

import com.autobox.app.data.api.ArboxApiService
import com.autobox.app.data.local.EncryptedPreferencesManager
import com.autobox.app.data.models.LoginRequest
import com.autobox.app.data.models.LoginResponse
import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class ArboxAuthRepository(
    private val apiService: ArboxApiService,
    private val prefs: EncryptedPreferencesManager
) {

    private val gson = Gson()

    suspend fun login(email: String, password: String): Result<LoginResponse> = withContext(Dispatchers.IO) {
        val cleanEmail = email.trim()
        try {
            val response = apiService.login(LoginRequest(email = cleanEmail, password = password))
            if (response.isSuccessful && response.body() != null) {
                val body = response.body()!!
                val token = body.token
                    ?: body.jwt
                    ?: body.accessToken
                    ?: body.data?.token
                    ?: body.data?.jwt
                    ?: body.data?.accessToken
                    ?: body.data?.user?.token

                if (!token.isNullOrBlank()) {
                    prefs.email = cleanEmail
                    prefs.password = password
                    prefs.authToken = token

                    val user = body.data
                    val nestedUser = user?.user
                    val userId = user?.id ?: user?.userId ?: nestedUser?.id
                    val userName = user?.name ?: "${user?.firstName ?: ""} ${user?.lastName ?: ""}".trim().ifBlank { nestedUser?.name }
                    val boxId = user?.boxId ?: user?.boxes?.firstOrNull()?.id ?: nestedUser?.boxId

                    val memberships = user?.memberships ?: nestedUser?.memberships
                    val activeMembership = memberships?.firstOrNull { it.isActive != false }
                        ?: memberships?.firstOrNull()

                    userId?.let { prefs.userId = it }
                    if (!userName.isNullOrBlank()) prefs.userName = userName
                    boxId?.let { prefs.boxId = it }

                    // Store refresh token if provided
                    user?.refreshToken?.let { prefs.refreshToken = it }

                    activeMembership?.let { membership ->
                        prefs.membershipId = membership.id
                        membership.name?.let { prefs.membershipName = it }
                        membership.boxId?.let { prefs.boxId = it }
                    }

                    Result.success(body)
                } else {
                    val errorMsg = parseErrorMessage(body.errorCode ?: body.errorCodeSnake, body.message ?: body.error ?: body.description)
                    Result.failure(Exception(errorMsg))
                }
            } else {
                val rawError = response.errorBody()?.string()
                val parsedError = tryParseErrorBody(rawError)
                val finalMessage = parsedError ?: "HTTP ${response.code()}: ${response.message()}"
                Result.failure(Exception(finalMessage))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun saveDirectSession(
        token: String,
        boxId: Long,
        membershipId: Long,
        email: String = "",
        userName: String = "Direct Session"
    ) {
        val cleanToken = token.trim().removePrefix("Bearer ").removePrefix("bearer ")
        prefs.authToken = cleanToken
        prefs.boxId = boxId
        prefs.membershipId = membershipId
        prefs.email = email
        prefs.userName = userName
        prefs.membershipName = "Direct Membership (ID: $membershipId)"
    }

    suspend fun reAuthenticateIfNeeded(): Boolean = withContext(Dispatchers.IO) {
        val savedEmail = prefs.email
        val savedPassword = prefs.password
        if (!savedEmail.isNullOrBlank() && !savedPassword.isNullOrBlank()) {
            val result = login(savedEmail, savedPassword)
            result.isSuccess
        } else {
            // If using direct token without password, verify token is present
            !prefs.authToken.isNullOrBlank()
        }
    }

    private fun tryParseErrorBody(rawJson: String?): String? {
        if (rawJson.isNullOrBlank()) return null
        return try {
            val resp = gson.fromJson(rawJson, LoginResponse::class.java)
            val code = resp.errorCode ?: resp.errorCodeSnake
            val msg = resp.message ?: resp.error ?: resp.description
            parseErrorMessage(code, msg)
        } catch (_: Exception) {
            rawJson
        }
    }

    private fun parseErrorMessage(errorCode: Int?, serverMessage: String?): String {
        val baseMsg = serverMessage?.takeIf { it.isNotBlank() }
        return when (errorCode) {
            1001 -> "Invalid credentials (Error 1001). Please check your email & password or try direct token login."
            1002 -> "User account not found or inactive (Error 1002)."
            1003 -> "Account is locked or password expired (Error 1003)."
            1004 -> "Missing required parameters (Error 1004)."
            null -> baseMsg ?: "Authentication failed. Please verify your credentials."
            else -> "Error $errorCode: ${baseMsg ?: "Authentication rejected by Arbox."}"
        }
    }

    fun getBearerToken(): String? {
        val token = prefs.authToken ?: return null
        return if (token.startsWith("Bearer ", ignoreCase = true)) token else "Bearer $token"
    }

    fun getMembershipId(): Long {
        return prefs.membershipId
    }

    fun getBoxId(): Long {
        return prefs.boxId
    }

    fun isLoggedIn(): Boolean {
        return prefs.isLoggedIn()
    }

    fun logout() {
        prefs.clearAuth()
    }
}
