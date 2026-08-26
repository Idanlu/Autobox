package com.autobox.app.data.repository

import com.autobox.app.data.api.ArboxApiService
import com.autobox.app.data.local.EncryptedPreferencesManager
import com.autobox.app.data.models.LoginRequest
import com.autobox.app.data.models.LoginResponse
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class ArboxAuthRepository(
    private val apiService: ArboxApiService,
    private val prefs: EncryptedPreferencesManager
) {

    suspend fun login(email: String, password: String): Result<LoginResponse> = withContext(Dispatchers.IO) {
        try {
            val response = apiService.login(LoginRequest(email.trim(), password))
            if (response.isSuccessful && response.body() != null) {
                val body = response.body()!!
                val token = body.token
                if (!token.isNullOrBlank()) {
                    prefs.email = email.trim()
                    prefs.password = password
                    prefs.authToken = token

                    body.data?.let { user ->
                        user.id?.let { prefs.userId = it }
                        user.name?.let { prefs.userName = it }

                        // Save primary box ID
                        user.boxes?.firstOrNull()?.id?.let { boxId ->
                            prefs.boxId = boxId
                        }

                        // Save primary active membership ID
                        val activeMembership = user.memberships?.firstOrNull { it.isActive != false }
                            ?: user.memberships?.firstOrNull()

                        activeMembership?.let { membership ->
                            prefs.membershipId = membership.id
                            membership.name?.let { prefs.membershipName = it }
                            membership.boxId?.let { prefs.boxId = it }
                        }
                    }

                    Result.success(body)
                } else {
                    Result.failure(Exception(body.message ?: body.error ?: "Invalid credentials or empty token"))
                }
            } else {
                val errorMsg = response.errorBody()?.string() ?: "HTTP ${response.code()}: ${response.message()}"
                Result.failure(Exception(errorMsg))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun reAuthenticateIfNeeded(): Boolean = withContext(Dispatchers.IO) {
        val savedEmail = prefs.email
        val savedPassword = prefs.password
        if (!savedEmail.isNullOrBlank() && !savedPassword.isNullOrBlank()) {
            val result = login(savedEmail, savedPassword)
            result.isSuccess
        } else {
            false
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
