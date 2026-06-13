package com.tanh.datsan.data.model

import com.google.gson.annotations.SerializedName

data class UserProfileLoggedInResponseDto(
    val id: String,
    val email: String,
    @SerializedName("full_name") val fullName: String,
    @SerializedName("avatar_url") val avatarUrl: String?,
    val role: String,
    @SerializedName("is_profile_complete") val isProfileComplete: Boolean,
    val branch: BranchDto?
)

data class BranchDto(
    val branchId: String?
)