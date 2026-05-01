package com.tanh.datsan.data.model

import com.google.gson.annotations.SerializedName


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

data class BookedSlotsResponse(
    @SerializedName("date") val date: String,
    @SerializedName("fieldId") val fieldId: String,
    @SerializedName("bookings") val bookings: List<BookingTimeSlot>
)

data class BookingTimeSlot(
    @SerializedName("startTime") val startTime: String,
    @SerializedName("endTime") val endTime: String,
    @SerializedName("status") val status: String
)