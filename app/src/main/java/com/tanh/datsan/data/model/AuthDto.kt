package com.tanh.datsan.data.model

import com.google.gson.annotations.SerializedName

data class RegisterRequest(
    val email: String,
    val password: String,
    @SerializedName("full_name") val fullName: String,
    @SerializedName("phone_number") val phoneNumber: String,
    val gender: String? = null
)

data class VerifyEmailRequest(
    val email: String,
    val verificationCode: String
)

data class LoginRequest(
    val email: String,
    val password: String
)

data class LoginCompleteRequest(
    val email: String,
    val verificationCode: String
)

data class LoginResponse(
    val accessToken: String,
    val user: UserProfileLoggedInResponseDto
)

data class AuthMessageResponse(
    val message: String
)

data class TokenResponse(
    val accessToken: String
)
