package com.tanh.datsan.data.network

import com.tanh.datsan.data.model.*
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

interface AuthApiService {

    // 1. Nhóm Đăng nhập/Đăng ký truyền thống
    @POST("auth/login/initiate")
    suspend fun loginInitiate(@Body request: LoginRequest): Response<LoginResponse>

    @POST("auth/register/initiate")
    suspend fun registerInitiate(@Body request: RegisterRequest): Response<LoginResponse>

    @POST("auth/login/complete")
    suspend fun loginComplete(@Body request: OtpRequest): Response<LoginResponse>

    @POST("auth/register/complete")
    suspend fun registerComplete(@Body request: OtpRequest): Response<LoginResponse>

    // 2. ĐĂNG NHẬP GOOGLE NATIVE
    @POST("auth/google/mobile")
    suspend fun googleAuthNative(
        @Body request: GoogleLoginRequest
    ): Response<LoginResponse>

    // 3. Quên mật khẩu
    @POST("auth/forgot-password")
    suspend fun forgotPassword(@Body request: ForgotPasswordRequest): Response<LoginResponse>

    @POST("auth/reset-password")
    suspend fun resetPassword(@Body request: ResetPasswordRequest): Response<LoginResponse>
}