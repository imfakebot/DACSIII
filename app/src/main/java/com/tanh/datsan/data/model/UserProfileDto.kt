package com.tanh.datsan.data.model

import com.google.gson.annotations.SerializedName

data class UserProfile(
    @SerializedName("id") val id: String?,
    @SerializedName("full_name") val fullName: String?,
    @SerializedName("avatar_url") val avatarUrl: String?
)