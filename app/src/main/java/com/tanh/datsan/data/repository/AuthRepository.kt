package com.tanh.datsan.data.repository

import com.tanh.datsan.core.TokenManager
import com.tanh.datsan.core.UserManager
import com.tanh.datsan.data.model.*
import com.tanh.datsan.data.network.AuthApiService
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthRepository @Inject constructor(
    private val authApiService: AuthApiService,
    private val tokenManager: TokenManager,
    private val userManager: UserManager
) {
    suspend fun loginInitiate(request: LoginRequest) = authApiService.loginInitiate(request)
    suspend fun registerInitiate(request: RegisterRequest) = authApiService.registerInitiate(request)
    suspend fun loginComplete(request: OtpRequest) = authApiService.loginComplete(request)
    suspend fun registerComplete(request: OtpRequest) = authApiService.registerComplete(request)
    suspend fun googleAuthNative(idToken: String) = authApiService.googleAuthNative(GoogleLoginRequest(idToken))
    suspend fun forgotPassword(email: String, returnUrl: String? = null) =
        authApiService.forgotPassword(ForgotPasswordRequest(email, returnUrl))
    suspend fun resetPassword(request: ResetPasswordRequest) = authApiService.resetPassword(request)

    suspend fun saveTokens(accessToken: String, refreshToken: String) {
        tokenManager.saveTokens(accessToken, refreshToken)
    }

    fun saveUserInfo(avatarUrl: String?, userName: String?) {
        userManager.setUserInfo(userName, avatarUrl)
    }

    suspend fun logout() {
        tokenManager.clearTokens()
        userManager.clearUserInfo()
    }
}