package com.tanh.datsan.data.network

import com.tanh.datsan.data.model.*
import retrofit2.Call
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

interface AuthApiService {
    @POST("auth/register/initiate")
    suspend fun initiateRegistration(@Body request: RegisterRequest): Response<AuthMessageResponse>

    @POST("auth/register/complete")
    suspend fun completeRegistration(@Body request: VerifyEmailRequest): Response<LoginResponse>

    @POST("auth/login/initiate")
    suspend fun initiateLogin(@Body request: LoginRequest): Response<AuthMessageResponse>

    @POST("auth/login/complete")
    suspend fun completeLogin(@Body request: LoginCompleteRequest): Response<LoginResponse>

    @POST("auth/logout")
    suspend fun logout(): Response<AuthMessageResponse>

    @POST("auth/reset-password")
    suspend fun resetPassword(@Body request: ResetPasswordRequest): Response<AuthMessageResponse>

    @POST("auth/refresh")
    fun refreshToken(): Call<TokenResponse>

    @POST("auth/forgot-password")
    suspend fun forgotPassword(@Body body: ForgotPasswordRequest): Response<AuthMessageResponse>

    @POST("auth/google/mobile")
    suspend fun googleLogin(@Body request: GoogleLoginRequest): Response<LoginResponse>
}