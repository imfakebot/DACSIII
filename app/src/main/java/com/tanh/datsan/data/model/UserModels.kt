package com.tanh.datsan.data.model

import com.google.gson.annotations.SerializedName

// --- PHẦN GỬI LÊN (REQUEST) ---

data class AddressDto(
    @SerializedName("street") val street: String,
    // Backend yêu cầu gửi cityId kiểu Số (Integer)
    @SerializedName("cityId") val cityId: Int
)

data class UpdateProfileRequest(
    @SerializedName("full_name") val fullName: String? = null,
    @SerializedName("gender") val gender: String? = null,
    @SerializedName("date_of_birth") val dateOfBirth: String? = null,
    @SerializedName("bio") val bio: String? = null,
    @SerializedName("address") val address: AddressDto? = null
)

// --- PHẦN NHẬN VỀ (RESPONSE) ---

data class UserProfileDto(
    @SerializedName("id") val id: String,
    @SerializedName("full_name", alternate = ["fullName"]) val fullName: String?,
    @SerializedName("phone_number", alternate = ["phoneNumber"]) val phoneNumber: String?,
    @SerializedName("street") val street: String?,
    @SerializedName("city") val city: CityDto?,
    @SerializedName("ward") val ward: WardDto?,
    @SerializedName("gender") val gender: String?,
    @SerializedName("date_of_birth", alternate = ["dateOfBirth"]) val dateOfBirth: String?,
    @SerializedName("bio") val bio: String?,
    @SerializedName("avatar_url", alternate = ["avatarUrl"]) val avatarUrl: String?,
    @SerializedName("is_profile_complete", alternate = ["isProfileComplete"]) val isProfileComplete: Boolean?
)

data class CityDto(
    @SerializedName("id") val id: Int,
    @SerializedName("name") val name: String
)

data class WardDto(
    @SerializedName("id") val id: Int,
    @SerializedName("name") val name: String
)

data class UserMeResponse(
    @SerializedName("id") val id: String,
    @SerializedName("email") val email: String,
    @SerializedName("userProfile") val userProfile: UserProfileDto?
)

data class AvatarUpdateResponse(
    @SerializedName("message") val message: String,
    @SerializedName("avatarUrl") val avatarUrl: String
)
