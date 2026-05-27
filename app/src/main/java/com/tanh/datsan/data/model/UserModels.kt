package com.tanh.datsan.data.model

import com.google.gson.annotations.SerializedName

// Mẫu JSON mới từ Backend: Thông tin chi tiết nằm trong userProfile
data class UserProfileDto(
    @SerializedName("id") val id: String,
    @SerializedName("full_name") val fullName: String?,
    @SerializedName("phone_number") val phoneNumber: String?,
    @SerializedName("address") val address: String?,
    @SerializedName("gender") val gender: String?,
    @SerializedName("avatar_url") val avatarUrl: String?,
    @SerializedName("is_profile_complete") val isProfileComplete: Boolean?
)

data class UserMeResponse(
    @SerializedName("id") val id: String,
    @SerializedName("email") val email: String,
    @SerializedName("role") val role: UserRole?,
    @SerializedName("userProfile") val userProfile: UserProfileDto?
)

data class UserRole(
    @SerializedName("id") val id: Int,
    @SerializedName("name") val name: String
)

data class UpdateProfileRequest(
    @SerializedName("full_name") val fullName: String?,
    @SerializedName("phone_number") val phoneNumber: String?,
    @SerializedName("address") val address: String?
)

data class AvatarUpdateResponse(
    @SerializedName("message") val message: String,
    @SerializedName("avatarUrl") val avatarUrl: String
)
