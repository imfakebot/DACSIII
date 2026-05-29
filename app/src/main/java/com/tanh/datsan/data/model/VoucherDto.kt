package com.tanh.datsan.data.model

data class VoucherDto(
    val id: String,
    val code: String,
    val discountAmount: Double?,
    val discountPercentage: Double?,
    val maxDiscountAmount: Double?,
    val minOrderValue: Double?,
    val validFrom: String,
    val validTo: String,
    val quantity: Int,
    val userProfileId: String?,
    val createdAt: String,
    val updatedAt: String
)