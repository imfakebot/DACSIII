package com.tanh.datsan.data.model

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
    val isCollectible: Boolean? = false
)

data class CheckVoucherResponse(
    val discountAmount: Double,
    val finalAmount: Double
)