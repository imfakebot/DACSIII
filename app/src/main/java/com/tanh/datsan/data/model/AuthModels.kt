package com.tanh.datsan.data.model

import com.google.gson.annotations.SerializedName

// --- AUTH DATA MODELS ---
data class AuthMessageResponse(
    val message: String = ""
)

data class UserBranch(
    val branchId: String? = null
)

data class UserResponse(
    val id: String,
    val email: String,
    val role: String,
    @SerializedName("is_profile_complete", alternate = ["isProfileComplete"])
    val isProfileComplete: Boolean,
    val branch: UserBranch?,
    @SerializedName("avatar_url", alternate = ["avatarUrl"])
    val avatarUrl: String?,
    @SerializedName("full_name", alternate = ["fullName", "userName"])
    val userName: String?
)

data class LoginResponse(
    @SerializedName("accessToken")
    val accessToken: String,
    @SerializedName("refreshToken")
    val refreshToken: String?,
    val user: UserResponse?
)

data class LoginRequest(
    val email: String,
    val password: String)
data class RegisterRequest(
    val full_name: String,
    val email: String,
    val phone_number: String,
    val gender: String,
    val password: String
)
data class OtpRequest(
    val email: String,
    val verificationCode: String
)
data class ForgotPasswordRequest(
    val email: String,
    val returnUrl: String? = null
)
data class ResetPasswordRequest(
    val token: String,
    val newPassword: String)

// Model gửi Token Google lên
data class GoogleLoginRequest(
    val idToken: String
)