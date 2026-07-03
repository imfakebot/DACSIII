package com.tanh.datsan.data.model

import com.google.gson.annotations.SerializedName
import com.tanh.datsan.BuildConfig

data class AuthMessageResponse(
    val message: String = ""
)

data class UserBranch(
    val branchId: String? = null
)

data class UserResponse(
    val id: String,
    val email: String,
    val role: String?,
    val managedBranchId: String?,
    @SerializedName("is_profile_complete", alternate = ["isProfileComplete"])
    val isProfileComplete: Boolean?,
    val branch: UserBranch?,
    @SerializedName("userProfile")
    val userProfile: AccountProfile?,
    
    // Fallback in case they are at root
    @SerializedName("avatar_url", alternate = ["avatarUrl"])
    val avatarUrl: String?,
    @SerializedName("full_name", alternate = ["fullName"])
    val fullName: String?,

    val status: Boolean?
)

data class LoginResponse(
    @SerializedName("accessToken", alternate = ["access_token"])
    val accessToken: String,
    @SerializedName("refreshToken", alternate = ["refresh_token"])
    val refreshToken: String?,
    val user: UserResponse?
)

data class TokenResponse(
    @SerializedName("accessToken")
    val accessToken: String,
    @SerializedName("refreshToken")
    val refreshToken: String?
)

data class VerifyEmailRequest(
    val email: String,
    val verificationCode: String
)

data class LoginCompleteRequest(
    val email: String,
    val verificationCode: String
)

data class LoginRequest(
    val email: String,
    val password: String? = null
)
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
    val returnUrl: String? = BuildConfig.API_DEEPlINK_FORGOT_PASSWORD
)
data class ResetPasswordRequest(
    val token: String,
    val newPassword: String)

// Model gửi Token Google lên
data class GoogleLoginRequest(
    val idToken: String
)