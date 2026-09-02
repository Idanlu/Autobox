package com.autobox.app.ui.viewmodels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.autobox.app.data.api.NetworkModule
import com.autobox.app.data.local.EncryptedPreferencesManager
import com.autobox.app.data.repository.ArboxAuthRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class AuthUiState(
    val isLoading: Boolean = false,
    val isLoggedIn: Boolean = false,
    val email: String = "",
    val userName: String? = null,
    val membershipName: String? = null,
    val membershipId: Long = -1L,
    val boxId: Long = -1L,
    val locationId: Long = -1L,
    val boxName: String? = null,
    val errorMessage: String? = null
)

class AuthViewModel(application: Application) : AndroidViewModel(application) {

    private val prefs = EncryptedPreferencesManager(application)
    private val authRepo = ArboxAuthRepository(NetworkModule.arboxApiService, prefs)

    private val _uiState = MutableStateFlow(
        AuthUiState(
            isLoggedIn = prefs.isLoggedIn(),
            email = prefs.email ?: "",
            userName = prefs.userName,
            membershipName = prefs.membershipName,
            membershipId = prefs.membershipId,
            boxId = prefs.boxId,
            locationId = prefs.locationId,
            boxName = prefs.boxName
        )
    )
    val uiState: StateFlow<AuthUiState> = _uiState.asStateFlow()

    fun login(emailInput: String, passwordInput: String) {
        if (emailInput.isBlank() || passwordInput.isBlank()) {
            _uiState.value = _uiState.value.copy(errorMessage = "Please enter email and password")
            return
        }

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
            val result = authRepo.login(emailInput, passwordInput)
            if (result.isSuccess) {
                _uiState.value = AuthUiState(
                    isLoading = false,
                    isLoggedIn = true,
                    email = prefs.email ?: "",
                    userName = prefs.userName,
                    membershipName = prefs.membershipName,
                    membershipId = prefs.membershipId,
                    boxId = prefs.boxId,
                    locationId = prefs.locationId,
                    boxName = prefs.boxName,
                    errorMessage = null
                )
            } else {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = result.exceptionOrNull()?.localizedMessage ?: "Login failed"
                )
            }
        }
    }


    fun logout() {
        authRepo.logout()
        _uiState.value = AuthUiState(isLoggedIn = false)
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(errorMessage = null)
    }
}
