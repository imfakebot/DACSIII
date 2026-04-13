package com.tanh.datsan.data.network

import com.google.gson.annotations.SerializedName
import com.tanh.datsan.data.model.FieldResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query

// --- DATA MODELS ---
data class LoginResponse(
    val status: String? = null,
    val message: String? = null,
    val account_id: String? = null,
    @SerializedName("accessToken")
    val accessToken: String? = null,
    val refreshToken: String? = null
)

data class LoginRequest(val email: String, val password: String)
data class RegisterRequest(val full_name: String, val email: String, val phone_number: String, val gender: String, val password: String)
data class OtpRequest(val email: String, val verificationCode: String)
data class ForgotPasswordRequest(val email: String)
data class ResetPasswordRequest(val token: String, var newPassword: String)

// Model gửi Token Google lên
data class GoogleLoginRequest(
    val idToken: String
)

// --- INTERFACE API ---
interface ApiService {

    // 1. Nhóm Sân Bóng
    @GET("fields")
    suspend fun getAllFields(
        @Query("latitude") lat: String? = null,
        @Query("longitude") lng: String? = null,
        @Query("radius") radius: Int? = 10,
        @Query("cityId") cityId: Int? = null
    ): List<FieldResponse>

    // 2. Nhóm Đăng nhập/Đăng ký truyền thống
    @POST("auth/login/initiate")
    suspend fun loginInitiate(@Body request: LoginRequest): Response<LoginResponse>

    @POST("auth/register/initiate")
    suspend fun registerInitiate(@Body request: RegisterRequest): Response<LoginResponse>

    @POST("auth/login/complete")
    suspend fun loginComplete(@Body request: OtpRequest): Response<LoginResponse>

    @POST("auth/register/complete")
    suspend fun registerComplete(@Body request: OtpRequest): Response<LoginResponse>

    // 3. ĐĂNG NHẬP GOOGLE NATIVE (Đã cập nhật tên cho khớp Backend)
    // Khớp với @Post('google/mobile') trong auth.controller.ts
    @POST("auth/google/mobile")
    suspend fun googleAuthNative(
        @Body request: GoogleLoginRequest
    ): Response<LoginResponse>

    // 4. Quên mật khẩu
    @POST("auth/forgot-password")
    suspend fun forgotPassword(@Body request: ForgotPasswordRequest): Response<LoginResponse>

    @POST("auth/reset-password")
    suspend fun resetPassword(@Body request: ResetPasswordRequest): Response<LoginResponse>
}