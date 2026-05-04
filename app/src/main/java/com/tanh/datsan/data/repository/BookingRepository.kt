package com.tanh.datsan.data.repository

import com.tanh.datsan.data.model.BookingResponse
import com.tanh.datsan.data.model.CreateBookingDto
import com.tanh.datsan.data.network.BookingApiService
import javax.inject.Inject

class BookingRepository @Inject constructor(
    private val bookingApiService: BookingApiService
){
    suspend fun createBooking(request: CreateBookingDto): BookingResponse {
        return bookingApiService.createBooking(request)
    }
}