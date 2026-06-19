package com.tanh.datsan.data.model

import com.google.gson.annotations.SerializedName

data class CreateBranchDto(
    val name: String,
    @SerializedName("phone_number")
    val phoneNumber: String?,
    val description: String?,
    val status: Boolean = true,
    @SerializedName("open_time")
    val openTime: String,
    @SerializedName("close_time")
    val closeTime: String,
    val latitude: Double? = null,
    val longitude: Double? = null,
    val cityId: Int? = null,
    val wardId: Int? = null,
    val street: String? = null,
    val managerId: String? = null
)

data class UpdateBranchDto(
    val name: String? = null,
    @SerializedName("phone_number")
    val phoneNumber: String? = null,
    val description: String? = null,
    val status: Boolean? = null,
    @SerializedName("open_time")
    val openTime: String? = null,
    @SerializedName("close_time")
    val closeTime: String? = null,
    val latitude: Double? = null,
    val longitude: Double? = null,
    val cityId: Int? = null,
    val wardId: Int? = null,
    val street: String? = null,
    val managerId: String? = null
)

data class MessageResponseDto(
    val message: String
)
