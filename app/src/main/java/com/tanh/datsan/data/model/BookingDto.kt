package com.tanh.datsan.data.model


// Cục Data gửi lên NestJS khi bấm Đặt sân
data class CreateBookingDto(
    val fieldId: String,
    val startTime: String,
    val durationMinutes: Int,
    val voucherCode: String? = null
)

// Cục Data hứng về từ NestJS sau khi đặt thành công
data class BookingResponse(
    val message: String,
    val paymentUrl: String?,
    val finalAmount: Int
)