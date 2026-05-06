package com.tanh.datsan.data.model

import com.google.gson.annotations.SerializedName

// --- AUTH DATA MODELS ---
data class AuthMessageResponse(
    val message: String? = null
)

data class UserBranch(
    val branchId: String? = null
)

data class UserResponse(
    val id: String? = null,
    val email: String? = null,
    val role: String? = null,
    @SerializedName("is_profile_complete")
    val isProfileComplete: Boolean? = null,
    val branch: UserBranch? = null
)

data class LoginResponse(
    @SerializedName("accessToken")
    val accessToken: String? = null,
    val user: UserResponse? = null
)

data class LoginRequest(val email: String, val password: String)
data class RegisterRequest(val full_name: String, val email: String, val phone_number: String, val gender: String, val password: String)
data class OtpRequest(val email: String, val verificationCode: String)
data class ForgotPasswordRequest(
    val email: String,
    val returnUrl: String? = null
)
data class ResetPasswordRequest(val token: String, var newPassword: String)

// Model gửi Token Google lên
data class GoogleLoginRequest(
    val idToken: String
)