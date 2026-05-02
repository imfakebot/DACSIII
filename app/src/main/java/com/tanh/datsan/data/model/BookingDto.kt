package com.tanh.datsan.data.model

import com.google.gson.annotations.SerializedName


// Cục Data gửi lên NestJS khi bấm Đặt sân
data class CreateBookingDto(
    val fieldId: String,
    val startTime: String,
    val durationMinutes: Int,
    val voucherCode: String? = null
)

data class BookingResponse(
    @SerializedName("id")
    val id: String?,

    @SerializedName("code")
    val code: String?,

    @SerializedName("check_in_at")
    val checkInAt: String?,

    @SerializedName("bookingDate")
    val bookingDate: String?,

    @SerializedName("start_time")
    val startTime: String?,

    @SerializedName("end_time")
    val endTime: String?,

    @SerializedName("total_price")
    val totalPrice: Long?,

    @SerializedName("status")
    val status: String?,

    @SerializedName("customerName")
    val customerName: String?,

    @SerializedName("customerPhone")
    val customerPhone: String?,

    @SerializedName("createdAt")
    val createdAt: String?,

    @SerializedName("updatedAt")
    val updatedAt: String?,

    // Object chứa thông tin user
    @SerializedName("userProfile")
    val userProfile: UserProfile?,

    // Object chứa thông tin sân
    @SerializedName("field")
    val field: FieldResponse?
)
data class CreateBookingResponse(
    @SerializedName("booking")
    val booking: BookingResponse?,

    @SerializedName("paymentUrl")
    val paymentUrl: String?,

    @SerializedName("finalAmount")
    val finalAmount: Long?,

    @SerializedName("message")
    val message: String?
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