package com.tanh.datsan.data.model

import com.google.gson.annotations.SerializedName

// Mẫu JSON mới từ Backend: Thông tin chi tiết nằm trong userProfile
data class AddressDto(
    @SerializedName("street") val street: String,
    @SerializedName("cityId") val cityId: Int,
    @SerializedName("wardId") val wardId: Int
)

data class UserProfileDto(
    @SerializedName("id") val id: String,
    @SerializedName("full_name") val fullName: String?,
    @SerializedName("phone_number") val phoneNumber: String?,
    @SerializedName("address") val address: String?,
    @SerializedName("street") val street: String?,
    @SerializedName("city") val city: CityDto?,
    @SerializedName("ward") val ward: WardDto?,
    @SerializedName("gender") val gender: String?,
    @SerializedName("date_of_birth") val dateOfBirth: String?,
    @SerializedName("bio") val bio: String?,
    @SerializedName("avatar_url") val avatarUrl: String?,
    @SerializedName("is_profile_complete") val isProfileComplete: Boolean?
)

data class CityDto(
    @SerializedName("id") val id: Int,
    @SerializedName("name") val name: String,
    @SerializedName("type") val type: String? = null
)

data class WardDto(
    @SerializedName("id") val id: Int,
    @SerializedName("name") val name: String,
    @SerializedName("type") val type: String? = null
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
    @SerializedName("full_name") val fullName: String? = null,
    @SerializedName("phone_number") val phoneNumber: String? = null,
    @SerializedName("gender") val gender: String? = null,
    @SerializedName("date_of_birth") val dateOfBirth: String? = null,
    @SerializedName("bio") val bio: String? = null,
    @SerializedName("address") val address: AddressDto? = null
)

data class AvatarUpdateResponse(
    @SerializedName("message") val message: String,
    @SerializedName("avatarUrl") val avatarUrl: String
)
