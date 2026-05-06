package com.tanh.datsan.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tanh.datsan.data.model.LoginRequest
import com.tanh.datsan.data.model.OtpRequest
import com.tanh.datsan.data.model.RegisterRequest
import com.tanh.datsan.data.model.ResetPasswordRequest
import com.tanh.datsan.data.repository.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import org.json.JSONObject
import javax.inject.Inject

// Kênh sự kiện UI giữ nguyên để không làm lỗi file giao diện
sealed class AuthUiEvent {
    data class ShowToast(val message: String) : AuthUiEvent()
    data class NavigateToOtp(val email: String, val isLoginMode: Boolean) : AuthUiEvent()
    data class NavigateToResetPassword(val email: String) : AuthUiEvent()
    data class NavigateToHome(val message: String) : AuthUiEvent()
    object NavigateBackToLogin : AuthUiEvent()
}

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val repository: AuthRepository // Hilt sẽ tự động tiêm Repository vào đây
) : ViewModel() { // Đổi từ AndroidViewModel sang ViewModel chuẩn

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
                    val message = response.body()?.message ?: "Đã gửi mã OTP đến email!"
                    sendEvent(AuthUiEvent.ShowToast(message))
                    sendEvent(AuthUiEvent.NavigateToOtp(email, true))
                } else {
                    sendEvent(AuthUiEvent.ShowToast(extractErrorMessage(response.errorBody()?.string(), "Sai thông tin đăng nhập!")))
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
                    // Backend hiện trả refresh token qua HttpOnly cookie, không nằm trong JSON body.
                    repository.saveTokens(result.accessToken!!, "")
                    sendEvent(AuthUiEvent.ShowToast("Đăng nhập Google thành công!"))
                    sendEvent(AuthUiEvent.NavigateToHome("Thành công"))
                } else {
                    sendEvent(AuthUiEvent.ShowToast(extractErrorMessage(response.errorBody()?.string(), "Server từ chối yêu cầu!")))
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
                    val message = response.body()?.message ?: "Đăng ký thành công, vui lòng kiểm tra email để lấy mã xác thực!"
                    sendEvent(AuthUiEvent.ShowToast(message))
                    sendEvent(AuthUiEvent.NavigateToOtp(request.email, false))
                } else {
                    sendEvent(AuthUiEvent.ShowToast(extractErrorMessage(response.errorBody()?.string(), "Đăng ký thất bại, email hoặc số điện thoại có thể đã tồn tại!")))
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
                if (isLoginMode) {
                    val response = repository.loginComplete(request)
                    if (response.isSuccessful && response.body()?.accessToken != null) {
                        val result = response.body()!!
                        // Backend hiện trả refresh token qua HttpOnly cookie, không nằm trong JSON body.
                        repository.saveTokens(result.accessToken!!, "")
                        sendEvent(AuthUiEvent.ShowToast("Đăng nhập thành công!"))
                        sendEvent(AuthUiEvent.NavigateToHome("Success"))
                    } else {
                        sendEvent(
                            AuthUiEvent.ShowToast(
                                extractErrorMessage(
                                    response.errorBody()?.string(),
                                    "Mã OTP không đúng hoặc đã hết hạn!",
                                ),
                            ),
                        )
                    }
                } else {
                    val response = repository.registerComplete(request)
                    if (response.isSuccessful) {
                        val message = response.body()?.message ?: "Xác thực thành công, vui lòng đăng nhập"
                        sendEvent(AuthUiEvent.ShowToast(message))
                        sendEvent(AuthUiEvent.NavigateBackToLogin)
                    } else {
                        sendEvent(
                            AuthUiEvent.ShowToast(
                                extractErrorMessage(
                                    response.errorBody()?.string(),
                                    "Mã OTP không đúng hoặc đã hết hạn!",
                                ),
                            ),
                        )
                    }
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
                    val message = response.body()?.message
                        ?: "Nếu email tồn tại, hệ thống đã gửi hướng dẫn đặt lại mật khẩu."
                    sendEvent(AuthUiEvent.ShowToast(message))
                    sendEvent(AuthUiEvent.NavigateToResetPassword(email))
                } else {
                    sendEvent(AuthUiEvent.ShowToast(extractErrorMessage(response.errorBody()?.string(), "Không thể gửi yêu cầu đặt lại mật khẩu.")))
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
                    val message = response.body()?.message ?: "Đổi mật khẩu thành công!"
                    sendEvent(AuthUiEvent.ShowToast(message))
                    sendEvent(AuthUiEvent.NavigateBackToLogin)
                } else {
                    sendEvent(AuthUiEvent.ShowToast(extractErrorMessage(response.errorBody()?.string(), "Mã xác nhận không hợp lệ!")))
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

    private fun extractErrorMessage(rawError: String?, fallback: String): String {
        if (rawError.isNullOrBlank()) return fallback
        return try {
            val payload = JSONObject(rawError)
            val messageValue = payload.opt("message")
            when (messageValue) {
                is String -> messageValue.ifBlank { fallback }
                is org.json.JSONArray -> {
                    if (messageValue.length() > 0) {
                        messageValue.optString(0, fallback).ifBlank { fallback }
                    } else {
                        fallback
                    }
                }
                else -> fallback
            }
        } catch (_: Exception) {
            fallback
        }
    }
}