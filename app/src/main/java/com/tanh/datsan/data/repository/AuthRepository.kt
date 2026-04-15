package com.tanh.datsan.data.repository

import com.tanh.datsan.core.TokenManager
import com.tanh.datsan.data.network.ApiService
import com.tanh.datsan.data.network.ForgotPasswordRequest
import com.tanh.datsan.data.network.GoogleLoginRequest
import com.tanh.datsan.data.network.LoginRequest
import com.tanh.datsan.data.network.OtpRequest
import com.tanh.datsan.data.network.RegisterRequest
import com.tanh.datsan.data.network.ResetPasswordRequest
import com.tanh.datsan.data.network.RetrofitClient

class AuthRepository(
    private val apiService: ApiService = RetrofitClient.apiService,
    private val tokenManager: TokenManager
) {
    suspend fun loginInitiate(request: LoginRequest) = apiService.loginInitiate(request)
    suspend fun registerInitiate(request: RegisterRequest) = apiService.registerInitiate(request)
    suspend fun loginComplete(request: OtpRequest) = apiService.loginComplete(request)
    suspend fun registerComplete(request: OtpRequest) = apiService.registerComplete(request)
    suspend fun googleAuthNative(idToken: String) = apiService.googleAuthNative(GoogleLoginRequest(idToken))
    suspend fun forgotPassword(email: String) = apiService.forgotPassword(ForgotPasswordRequest(email))
    suspend fun resetPassword(request: ResetPasswordRequest) = apiService.resetPassword(request)

    suspend fun saveTokens(accessToken: String, refreshToken: String) {
        tokenManager.saveTokens(accessToken, refreshToken)
    }
}