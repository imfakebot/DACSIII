package com.tanh.datsan.data.repository

import com.tanh.datsan.data.model.BookedSlotsResponse
import com.tanh.datsan.data.model.BookingResponse
import com.tanh.datsan.data.model.CreateBookingDto
import com.tanh.datsan.data.model.CreateBookingResponse
import com.tanh.datsan.data.network.BookingApiService
import retrofit2.Response
import javax.inject.Inject

class BookingRepository @Inject constructor(
    private val bookingApiService: BookingApiService
){
    suspend fun createBooking(request: CreateBookingDto): CreateBookingResponse {
        return bookingApiService.createBooking(request)
    }

    suspend fun getBookingSlotsOfAFieldInADay(fieldId: String, date: String): Response<BookedSlotsResponse>{
        return bookingApiService.getFieldScheduleInAday(fieldId, date)
    }

    suspend fun getBookingById(bookingId: String): BookingResponse {
        return bookingApiService.getBookingById(bookingId)
    }
}