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

                    // Step 1: Attempt to retrieve full user profile via api/v2/user/profile
                    val profileResult = fetchUserProfile(token)

                    // Step 2: If profile API fails or returns incomplete info, fallback to login response payload
                    if (profileResult.isFailure || prefs.boxId <= 0) {
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

                        user?.refreshToken?.let { prefs.refreshToken = it }

                        activeMembership?.let { membership ->
                            prefs.membershipId = membership.id
                            membership.name?.let { prefs.membershipName = it }
                            membership.boxId?.let { prefs.boxId = it }
                        }
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

    /**
     * Retrieves user profile from api/v2/user/profile and extracts box_id, location_id,
     * memberships, user info, and refresh token.
     */
    suspend fun fetchUserProfile(explicitToken: String? = null): Result<com.autobox.app.data.models.UserProfileData> = withContext(Dispatchers.IO) {
        val rawToken = explicitToken ?: prefs.authToken ?: return@withContext Result.failure(
            IllegalStateException("No authentication token available.")
        )
        val bearerToken = rawToken

        try {
            val response = apiService.getUserProfile(bearerToken)
            if (response.isSuccessful && response.body()?.data != null) {
                val profile = response.body()!!.data!!
                applyProfileData(profile)
                Result.success(profile)
            } else {
                val code = response.code()
                val err = response.errorBody()?.string()
                Result.failure(Exception("Failed to fetch profile: HTTP $code ${err ?: ""}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private fun applyProfileData(profile: com.autobox.app.data.models.UserProfileData) {
        // User identity
        profile.id?.let { prefs.userId = it }
        val displayName = profile.fullName?.takeIf { it.isNotBlank() }
            ?: "${profile.firstName ?: ""} ${profile.lastName ?: ""}".trim().takeIf { it.isNotBlank() }
        displayName?.let { prefs.userName = it }
        profile.email?.takeIf { it.isNotBlank() }?.let { prefs.email = it }
        profile.refreshToken?.takeIf { it.isNotBlank() }?.let { prefs.refreshToken = it }

        // Find active user box
        val usersBoxes = profile.usersBoxes ?: emptyList()
        val activeUserBox = usersBoxes.firstOrNull { it.active == 1 }
            ?: usersBoxes.firstOrNull()

        // Extract Box ID:
        // 1. From active users_boxes (box_fk or box.id)
        // 2. From activeBoxes list
        // 3. From boxes or allBoxes list
        val extractedBoxId = activeUserBox?.boxFk
            ?: activeUserBox?.box?.id
            ?: profile.activeBoxes?.firstOrNull()
            ?: profile.boxes?.firstOrNull()
            ?: profile.allBoxes?.firstOrNull()

        extractedBoxId?.let { prefs.boxId = it }

        // Extract Location ID:
        // 1. From active users_boxes (locations_box_fk or locations_box.id)
        // 2. From activeLocationsBox list
        // 3. From locations list
        val extractedLocationId = activeUserBox?.locationsBoxFk
            ?: activeUserBox?.locationsBox?.id
            ?: profile.activeLocationsBox?.firstOrNull()
            ?: profile.locations?.firstOrNull()

        extractedLocationId?.let { prefs.locationId = it }

        // Box Name
        val gymName = activeUserBox?.box?.name?.takeIf { it.isNotBlank() }
        gymName?.let { prefs.boxName = it }

        // Extract Membership:
        // Look inside group_connection -> group_members for the user's active membership
        val groupMembers = activeUserBox?.groupConnection?.groupMembers ?: emptyList()
        val profileUserId = profile.id
        val targetMember = groupMembers.firstOrNull { it.userFk == profileUserId }
            ?: groupMembers.firstOrNull { it.active == 1 }
            ?: groupMembers.firstOrNull()

        val memberMemberships = targetMember?.memberships ?: emptyList()
        val activeMembership = memberMemberships.firstOrNull { it.active == 1 }
            ?: memberMemberships.firstOrNull()

        val membershipId = activeMembership?.muId
            ?: activeMembership?.id
            ?: activeUserBox?.ubId
            ?: activeUserBox?.id
            ?: profile.lastEndedMembership?.id

        membershipId?.let { prefs.membershipId = it }
        activeMembership?.name?.let { prefs.membershipName = it }
    }

    fun saveDirectSession(
        token: String,
        boxId: Long,
        membershipId: Long,
        locationId: Long = boxId,
        email: String = "",
        userName: String = "Direct Session"
    ) {
        val cleanToken = token.trim().removePrefix("Bearer ").removePrefix("bearer ")
        prefs.authToken = cleanToken
        prefs.boxId = boxId
        prefs.locationId = locationId
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
            val hasToken = !prefs.authToken.isNullOrBlank()
            if (hasToken && (prefs.boxId <= 0 || prefs.locationId <= 0)) {
                fetchUserProfile()
            }
            hasToken
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

    fun getLocationId(): Long {
        return prefs.locationId
    }

    fun getBoxName(): String? {
        return prefs.boxName
    }

    fun isLoggedIn(): Boolean {
        return prefs.isLoggedIn()
    }

    fun logout() {
        prefs.clearAuth()
    }
}
