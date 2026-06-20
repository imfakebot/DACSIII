package com.tanh.datsan.data.model

import com.google.gson.annotations.SerializedName

data class Role(
    val id: Int,
    val name: String
)

data class AccountProfile(
    val id: String,
    @SerializedName("full_name") val fullName: String?,
    @SerializedName("date_of_birth") val dateOfBirth: String?,
    val gender: String?,
    @SerializedName("phone_number") val phoneNumber: String?,
    @SerializedName("avatar_url", alternate = ["avatarUrl"]) val avatarUrl: String?,
    val bio: String?,
    @SerializedName("is_profile_complete") val isProfileComplete: Boolean?,
    @SerializedName("created_at") val createdAt: String?,
    @SerializedName("updated_at") val updatedAt: String?,
    val address: AddressResponseDto?
)

data class AccountResponse(
    val id: String,
    val email: String,
    val provider: String?,
    @SerializedName("is_verified") val isVerified: Boolean?,
    @SerializedName("last_login") val lastLogin: String?,
    @SerializedName("created_at") val createdAt: String?,
    @SerializedName("updated_at") val updatedAt: String?,
    val role: Role?,
    @SerializedName("userProfile") val userProfile: AccountProfile?,
    val managedBranchId: String?
)
