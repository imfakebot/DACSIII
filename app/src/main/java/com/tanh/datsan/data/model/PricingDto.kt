package com.tanh.datsan.data.model

import com.google.gson.annotations.SerializedName

data class CheckPriceDto(
    val fieldId: String,
    val startTime: String,
    val durationMinutes: Int
)

data class CheckPriceResponseDto(
    @SerializedName("available") val available: Boolean,
    @SerializedName("field_name") val fieldName: String,
    @SerializedName("booking_details") val bookingDetails: BookingDetailsDto,
    @SerializedName("pricing") val pricing: PricingDetailsDto,
    @SerializedName("message") val message: String
)

data class BookingDetailsDto(
    @SerializedName("date") val date: String,
    @SerializedName("start_time") val startTime: String,
    @SerializedName("end_time") val endTime: String,
    @SerializedName("duration") val duration: String
)

data class PricingDetailsDto(
    @SerializedName("price_per_hour") val pricePerHour: Double,
    @SerializedName("total_price") val totalPrice: Double,
    @SerializedName("currency") val currency: String
)

data class UpdateTimeSlotRequest(
    val price: Double? = null,
    @SerializedName("start_time") val startTime: String? = null,
    @SerializedName("end_time") val endTime: String? = null,
    @SerializedName("is_peak_hour") val isPeakHour: Boolean? = null
)

data class TimeSlotResponse(
    val id: Int,
    @SerializedName("start_time") val startTime: String,
    @SerializedName("end_time") val endTime: String,
    val price: Double,
    @SerializedName("is_peak_hour") val isPeakHour: Boolean,
    val fieldType: FieldType? = null
)
