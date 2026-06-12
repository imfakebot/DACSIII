package com.tanh.datsan.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tanh.datsan.core.TokenManager
import com.tanh.datsan.core.UserManager
import com.tanh.datsan.data.model.*
import com.tanh.datsan.data.repository.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import org.json.JSONObject
import javax.inject.Inject

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val tokenManager: TokenManager,
    private val userManager: UserManager
) : ViewModel() {

    private val _uiState = MutableStateFlow<AuthUiState>(AuthUiState.Idle)
    val uiState: StateFlow<AuthUiState> = _uiState.asStateFlow()

    private val _email = MutableStateFlow("")
    val email: StateFlow<String> = _email.asStateFlow()

    fun onEmailChange(email: String) {
        _email.value = email
    }

    fun initiateRegistration(request: RegisterRequest) {
        viewModelScope.launch {
            _uiState.value = AuthUiState.Loading
            try {
                val response = authRepository.initiateRegistration(request)
                if (response.isSuccessful) {
                    _email.value = request.email
                    _uiState.value = AuthUiState.OtpSent(request.email, isRegister = true)
                } else {
                    val errorMsg = parseError(response.errorBody()?.string())
                    _uiState.value = AuthUiState.Error(errorMsg)
                }
            } catch (e: Exception) {
                _uiState.value = AuthUiState.Error(e.message ?: "Unknown error")
            }
        }
    }

    fun completeRegistration(verificationCode: String) {
        viewModelScope.launch {
            _uiState.value = AuthUiState.Loading
            try {
                val response = authRepository.completeRegistration(
                    VerifyEmailRequest(_email.value, verificationCode)
                )
                if (response.isSuccessful) {
                    _uiState.value = AuthUiState.Success("Registration complete. Please login.")
                } else {
                    val errorMsg = parseError(response.errorBody()?.string())
                    _uiState.value = AuthUiState.Error(errorMsg)
                }
            } catch (e: Exception) {
                _uiState.value = AuthUiState.Error(e.message ?: "Unknown error")
            }
        }
    }

    fun initiateLogin(request: LoginRequest) {
        viewModelScope.launch {
            _uiState.value = AuthUiState.Loading
            try {
                val response = authRepository.initiateLogin(request)
                if (response.isSuccessful) {
                    _email.value = request.email
                    _uiState.value = AuthUiState.OtpSent(request.email, isRegister = false)
                } else {
                    val errorMsg = parseError(response.errorBody()?.string())
                    _uiState.value = AuthUiState.Error(errorMsg)
                }
            } catch (e: Exception) {
                _uiState.value = AuthUiState.Error(e.message ?: "Unknown error")
            }
        }
    }

    fun completeLogin(verificationCode: String) {
        viewModelScope.launch {
            _uiState.value = AuthUiState.Loading
            try {
                val response = authRepository.completeLogin(
                    LoginCompleteRequest(_email.value, verificationCode)
                )
                if (response.isSuccessful) {
                    val loginResponse = response.body()
                    if (loginResponse != null) {
                        tokenManager.saveToken(loginResponse.accessToken)
                        userManager.setUserInfo(
                            loginResponse.user.fullName,
                            loginResponse.user.avatarUrl
                        )
                        _uiState.value = AuthUiState.Authenticated
                    } else {
                        _uiState.value = AuthUiState.Error("Login response body is null")
                    }
                } else {
                    val errorMsg = parseError(response.errorBody()?.string())
                    _uiState.value = AuthUiState.Error(errorMsg)
                }
            } catch (e: Exception) {
                _uiState.value = AuthUiState.Error(e.message ?: "Unknown error")
            }
        }
    }

    fun resetState() {
        _uiState.value = AuthUiState.Idle
    }

    private fun parseError(errorBody: String?): String {
        return try {
            val jsonObject = JSONObject(errorBody ?: "")
            jsonObject.optString("message", "Unknown error")
        } catch (e: Exception) {
            "Unknown error"
        }
    }
}
