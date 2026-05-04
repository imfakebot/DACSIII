package com.tanh.datsan.data.network

import com.tanh.datsan.data.model.BookingResponse
import com.tanh.datsan.data.model.CreateBookingDto
import retrofit2.http.Body
import retrofit2.http.POST

interface BookingApiService {
    @POST("bookings")
    suspend fun createBooking(
        @Body bookingRequest: CreateBookingDto
    ): BookingResponse
}