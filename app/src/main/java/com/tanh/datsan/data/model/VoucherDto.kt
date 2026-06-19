package com.tanh.datsan.data.model

import com.google.gson.annotations.SerializedName

data class Voucher(
    val id: String,
    val code: String,
    val discountAmount: Double?,
    val discountPercentage: Double?,
    val maxDiscountAmount: Double?,
    val minOrderValue: Double?,
    val validFrom: String,
    val validTo: String,
    val quantity: Int,
    val usedCount: Int? = 0,
    val isCollectible: Boolean? = false,
    val userProfileId: String? = null
)

data class CheckVoucherResponse(
    val discountAmount: Double,
    val finalAmount: Double
)

data class CreateVoucherDto(
    @SerializedName("code") val code: String,
    @SerializedName("discountAmount") val discountAmount: Double? = null,
    @SerializedName("discountPercentage") val discountPercentage: Double? = null,
    @SerializedName("maxDiscountAmount") val maxDiscountAmount: Double? = null,
    @SerializedName("minOrderValue") val minOrderValue: Double,
    @SerializedName("validFrom") val validFrom: String,
    @SerializedName("validTo") val validTo: String,
    @SerializedName("quantity") val quantity: Int,
    @SerializedName("userProfileId") val userProfileId: String? = null,
    @SerializedName("isCollectible") val isCollectible: Boolean = false
)