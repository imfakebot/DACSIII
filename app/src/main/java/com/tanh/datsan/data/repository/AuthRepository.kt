package com.tanh.datsan.data.repository

import com.tanh.datsan.data.model.*
import com.tanh.datsan.data.network.AuthApiService
import retrofit2.Response
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthRepository @Inject constructor(
    private val authApiService: AuthApiService
) {
    suspend fun initiateRegistration(request: RegisterRequest): Response<AuthMessageResponse> {
        return authApiService.initiateRegistration(request)
    }

    suspend fun completeRegistration(request: VerifyEmailRequest): Response<AuthMessageResponse> {
        return authApiService.completeRegistration(request)
    }

    suspend fun initiateLogin(request: LoginRequest): Response<AuthMessageResponse> {
        return authApiService.initiateLogin(request)
    }

    suspend fun completeLogin(request: LoginCompleteRequest): Response<LoginResponse> {
        return authApiService.completeLogin(request)
    }

    suspend fun logout(): Response<AuthMessageResponse> {
        return authApiService.logout()
    }
}
