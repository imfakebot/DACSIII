package com.tanh.datsan.data.model

import com.google.gson.annotations.SerializedName

// --- AUTH DATA MODELS ---
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