package com.tanh.datsan.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tanh.datsan.core.TokenManager
import com.tanh.datsan.core.UserManager
import com.tanh.datsan.data.model.*
import com.tanh.datsan.data.repository.AuthRepository
import com.tanh.datsan.utils.JwtUtil.getRoleFromToken
import com.tanh.datsan.utils.ResponseHelper.parseError
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val tokenManager: TokenManager,
    private val userManager: UserManager
) : ViewModel() {

    private val _uiState = MutableStateFlow<AuthUiState>(AuthUiState.Idle)
    val uiState: StateFlow<AuthUiState> = _uiState.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _uiEvent = MutableSharedFlow<AuthUiEvent>()
    val uiEvent = _uiEvent.asSharedFlow()

    private val _email = MutableStateFlow("")
    val email: StateFlow<String> = _email.asStateFlow()

    fun onEmailChange(email: String) {
        _email.value = email
    }

    fun forgotPassword(email: String) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val response = authRepository.forgotPassword(email)
                if (response.isSuccessful) {
                    _uiEvent.emit(AuthUiEvent.NavigateToResetPassword(email))
                } else {
                    val errorMsg = parseError(response.errorBody()?.string())
                    _uiEvent.emit(AuthUiEvent.ShowToast(errorMsg))
                }
            } catch (e: Exception) {
                _uiEvent.emit(AuthUiEvent.ShowToast(e.message ?: "Unknown error"))
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun resetPassword(token: String, newPassword: String) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val response =
                    authRepository.resetPassword(ResetPasswordRequest(token, newPassword))
                if (response.isSuccessful) {
                    _uiEvent.emit(AuthUiEvent.ShowToast("Đặt lại mật khẩu thành công!"))
                    _uiEvent.emit(AuthUiEvent.NavigateBackToLogin)
                } else {
                    val errorMsg = parseError(response.errorBody()?.string())
                    _uiEvent.emit(AuthUiEvent.ShowToast(errorMsg))
                }
            } catch (e: Exception) {
                _uiEvent.emit(AuthUiEvent.ShowToast(e.message ?: "Unknown error"))
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun verifyOtp(email: String, otp: String, isRegister: Boolean) {
        if (isRegister) {
            completeRegistration(email, otp)
        } else {
            completeLogin(email, otp)
        }
    }

    fun resendOtp(email: String, isRegister: Boolean) {
        viewModelScope.launch {
            try {
                val response = if (isRegister) {
                    // Logic gửi lại OTP cho đăng ký
                    authRepository.initiateRegistration(
                        RegisterRequest(
                            full_name = "",
                            email,
                            phone_number = "",
                            gender = "",
                            password = ""
                        )
                    )
                } else {
                    authRepository.initiateLogin(LoginRequest(email))
                }

                if (response.isSuccessful) {
                    _uiEvent.emit(AuthUiEvent.OtpResent)
                } else {
                    val errorMsg = parseError(response.errorBody()?.string())
                    _uiEvent.emit(AuthUiEvent.ShowToast(errorMsg))
                }
            } catch (e: Exception) {
                _uiEvent.emit(AuthUiEvent.ShowToast(e.message ?: "Unknown error"))
            }
        }
    }

    fun initiateRegistration(request: RegisterRequest) {
        Log.d("AuthViewModel", "Request: $request")
        viewModelScope.launch {
            _uiState.value = AuthUiState.Loading
            try {
                Log.d("AuthViewModel", "Initiating registration for email=${request.email}")
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

    fun completeRegistration(email: String, verificationCode: String) {
        viewModelScope.launch {
            _uiState.value = AuthUiState.Loading
            try {
                Log.d(
                    "AuthViewModel",
                    "Completing registration for email=$email with code=$verificationCode"
                )
                val response = authRepository.completeRegistration(
                    VerifyEmailRequest(email, verificationCode)
                )
                if (response.isSuccessful) {
                    val loginResponse = response.body()
                    if (loginResponse != null) {
                        tokenManager.saveToken(loginResponse.accessToken)
                        userManager.setUserInfo(
                            loginResponse.user?.fullName,
                            loginResponse.user?.avatarUrl
                        )
                    }
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
                Log.d("AuthViewModel", "Initiating login for email=${request.email}")
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

    fun completeLogin(email: String, verificationCode: String) {
        viewModelScope.launch {
            _uiState.value = AuthUiState.Loading
            try {
                Log.d(
                    "AuthViewModel",
                    "Completing login for email=$email with code=$verificationCode"
                )
                val response = authRepository.completeLogin(
                    LoginCompleteRequest(email, verificationCode)
                )
                if (response.isSuccessful) {
                    val loginResponse = response.body()
                    if (loginResponse != null) {
                        Log.d(
                            "AuthViewModel",
                            "Login successful for user: ${loginResponse.user?.fullName}"
                        )
                        if (loginResponse.user?.status == false) {
                            _uiState.value = AuthUiState.Error("Tài khoản của bạn đã bị khóa.")
                            _isLoading.value = false
                            return@launch
                        } else {
                            tokenManager.saveToken(loginResponse.accessToken)
                            userManager.setUserInfo(
                                loginResponse.user?.fullName,
                                loginResponse.user?.avatarUrl
                            )
                            val role = getRoleFromToken(loginResponse.accessToken)
                            _uiState.value = AuthUiState.Authenticated(role)
                        }
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

    fun loginWithGoogle(idToken: String) {
        viewModelScope.launch {
            _uiState.value = AuthUiState.Loading
            try {
                val response = authRepository.loginWithGoogle(idToken)
                if (response.isSuccessful) {
                    val loginResponse = response.body()
                    if (loginResponse != null) {
                        tokenManager.saveToken(loginResponse.accessToken)
                        userManager.setUserInfo(
                            loginResponse.user?.fullName,
                            loginResponse.user?.avatarUrl
                        )
                        val role =
                            getRoleFromToken(loginResponse.accessToken)
                        _uiState.value = AuthUiState.Authenticated(role)
                    } else {
                        Log.d("AuthViewModel", "Login response body is null")
                        _uiState.value = AuthUiState.Error("Login response body is null")
                    }
                } else {
                    val errorMsg = parseError(response.errorBody()?.string())
                    _uiState.value = AuthUiState.Error(errorMsg)
                }
            } catch (e: Exception) {
                Log.d("AuthViewModel", "Google login error: ${e.message}")
                _uiState.value = AuthUiState.Error(e.message ?: "Unknown error")
            }
        }
    }

    fun logout() {
        viewModelScope.launch {
            tokenManager.clearToken()
            userManager.clearUserInfo()
            authRepository.logout()
            _uiState.value = AuthUiState.Idle
        }
    }

    fun resetState() {
        _uiState.value = AuthUiState.Idle
    }
}
