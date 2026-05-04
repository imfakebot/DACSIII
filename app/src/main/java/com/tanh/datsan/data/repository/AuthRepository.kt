package com.tanh.datsan.data.repository

import com.tanh.datsan.core.TokenManager
import com.tanh.datsan.data.model.* // Nhập toàn bộ model từ package data.model mà chúng ta đã di dời
import com.tanh.datsan.data.network.AuthApiService // Dùng interface đã được tách riêng
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthRepository @Inject constructor(
    private val authApiService: AuthApiService, // Hilt sẽ tự động tiêm cái này vào
    private val tokenManager: TokenManager
) {
    suspend fun loginInitiate(request: LoginRequest) = authApiService.loginInitiate(request)
    suspend fun registerInitiate(request: RegisterRequest) = authApiService.registerInitiate(request)
    suspend fun loginComplete(request: OtpRequest) = authApiService.loginComplete(request)
    suspend fun registerComplete(request: OtpRequest) = authApiService.registerComplete(request)
    suspend fun googleAuthNative(idToken: String) = authApiService.googleAuthNative(GoogleLoginRequest(idToken))
    suspend fun forgotPassword(email: String) = authApiService.forgotPassword(ForgotPasswordRequest(email))
    suspend fun resetPassword(request: ResetPasswordRequest) = authApiService.resetPassword(request)

    suspend fun saveTokens(accessToken: String, refreshToken: String) {
        tokenManager.saveTokens(accessToken, refreshToken)
    }
}