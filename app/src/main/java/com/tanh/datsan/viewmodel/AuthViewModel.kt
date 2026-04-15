package com.tanh.datsan.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.tanh.datsan.core.TokenManager
import com.tanh.datsan.data.network.LoginRequest
import com.tanh.datsan.data.network.OtpRequest
import com.tanh.datsan.data.network.RegisterRequest
import com.tanh.datsan.data.network.ResetPasswordRequest
import com.tanh.datsan.data.repository.AuthRepository
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch

sealed class AuthUiEvent {
    data class ShowToast(val message: String) : AuthUiEvent()
    data class NavigateToOtp(val email: String, val isLoginMode: Boolean) : AuthUiEvent()
    data class NavigateToResetPassword(val email: String) : AuthUiEvent()
    data class NavigateToHome(val message: String) : AuthUiEvent()
    object NavigateBackToLogin : AuthUiEvent()
}

class AuthViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = AuthRepository(tokenManager = TokenManager(application))

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _uiEvent = Channel<AuthUiEvent>()
    val uiEvent = _uiEvent.receiveAsFlow()

    fun login(email: String, password: String) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val response = repository.loginInitiate(LoginRequest(email, password))
                if (response.isSuccessful) {
                    sendEvent(AuthUiEvent.ShowToast("Đã gửi mã OTP đến email!"))
                    sendEvent(AuthUiEvent.NavigateToOtp(email, true))
                } else {
                    sendEvent(AuthUiEvent.ShowToast("Sai thông tin đăng nhập!"))
                }
            } catch (e: Exception) {
                sendEvent(AuthUiEvent.ShowToast("Lỗi kết nối mạng!"))
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun loginWithGoogle(idToken: String) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val response = repository.googleAuthNative(idToken)
                if (response.isSuccessful && response.body()?.accessToken != null) {
                    val result = response.body()!!
                    repository.saveTokens(result.accessToken!!, result.refreshToken ?: "")
                    sendEvent(AuthUiEvent.ShowToast("Đăng nhập Google thành công!"))
                    sendEvent(AuthUiEvent.NavigateToHome("Thành công"))
                } else {
                    sendEvent(AuthUiEvent.ShowToast("Server từ chối yêu cầu!"))
                }
            } catch (e: Exception) {
                sendEvent(AuthUiEvent.ShowToast("Lỗi đăng nhập Google!"))
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun register(request: RegisterRequest) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val response = repository.registerInitiate(request)
                if (response.isSuccessful) {
                    sendEvent(AuthUiEvent.ShowToast("Đăng ký thành công!"))
                    sendEvent(AuthUiEvent.NavigateToOtp(request.email, false))
                } else {
                    sendEvent(AuthUiEvent.ShowToast("Đăng ký thất bại, email có thể đã tồn tại!"))
                }
            } catch (e: Exception) {
                sendEvent(AuthUiEvent.ShowToast("Lỗi kết nối mạng!"))
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun verifyOtp(email: String, otpCode: String, isLoginMode: Boolean) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val request = OtpRequest(email, otpCode)
                val response = if (isLoginMode) repository.loginComplete(request) else repository.registerComplete(request)

                if (response.isSuccessful && response.body() != null) {
                    val result = response.body()!!
                    if (result.accessToken != null) {
                        repository.saveTokens(result.accessToken!!, result.refreshToken ?: "")
                        sendEvent(AuthUiEvent.ShowToast("Đăng nhập thành công!"))
                        sendEvent(AuthUiEvent.NavigateToHome("Success"))
                    } else {
                        sendEvent(AuthUiEvent.ShowToast("Xác thực thành công, vui lòng đăng nhập"))
                        sendEvent(AuthUiEvent.NavigateBackToLogin)
                    }
                } else {
                    sendEvent(AuthUiEvent.ShowToast("Mã OTP không đúng hoặc đã hết hạn!"))
                }
            } catch (e: Exception) {
                sendEvent(AuthUiEvent.ShowToast("Lỗi kết nối mạng!"))
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun forgotPassword(email: String) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val response = repository.forgotPassword(email)
                if (response.isSuccessful) {
                    sendEvent(AuthUiEvent.ShowToast("Mã OTP đã được gửi!"))
                    sendEvent(AuthUiEvent.NavigateToResetPassword(email))
                } else {
                    sendEvent(AuthUiEvent.ShowToast("Email không tồn tại!"))
                }
            } catch (e: Exception) {
                sendEvent(AuthUiEvent.ShowToast("Lỗi kết nối mạng!"))
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun resetPassword(token: String, newPassword: String) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val response = repository.resetPassword(ResetPasswordRequest(token, newPassword))
                if (response.isSuccessful) {
                    sendEvent(AuthUiEvent.ShowToast("Đổi mật khẩu thành công!"))
                    sendEvent(AuthUiEvent.NavigateBackToLogin)
                } else {
                    sendEvent(AuthUiEvent.ShowToast("Mã xác nhận không hợp lệ!"))
                }
            } catch (e: Exception) {
                sendEvent(AuthUiEvent.ShowToast("Lỗi kết nối mạng!"))
            } finally {
                _isLoading.value = false
            }
        }
    }

    private fun sendEvent(event: AuthUiEvent) {
        viewModelScope.launch { _uiEvent.send(event) }
    }
}