package com.tanh.datsan.data.repository

import com.tanh.datsan.data.model.AdminCreateBookingDto
import com.tanh.datsan.data.model.BookedSlotsResponse
import com.tanh.datsan.data.model.BookingPaginatedResponseDto
import com.tanh.datsan.data.model.BookingResponse
import com.tanh.datsan.data.model.CreateBookingDto
import com.tanh.datsan.data.model.CreateBookingResponse
import com.tanh.datsan.data.network.BookingApiService
import com.tanh.datsan.data.network.CheckInDto
import okhttp3.ResponseBody
import retrofit2.Response
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BookingRepository @Inject constructor(
    private val bookingApiService: BookingApiService
) {
    suspend fun createBooking(request: CreateBookingDto): CreateBookingResponse {
        return bookingApiService.createBooking(request)
    }

    suspend fun getBookingSlotsOfAFieldInADay(
        fieldId: String,
        date: String
    ): Response<BookedSlotsResponse> {
        return bookingApiService.getFieldScheduleInAday(fieldId, date)
    }

    suspend fun getBookingById(bookingId: String): BookingResponse {
        return bookingApiService.getBookingById(bookingId)
    }

    suspend fun checkIn(identifier: String): BookingResponse {
        return bookingApiService.checkIn(CheckInDto(identifier))
    }

    suspend fun downloadTicket(bookingId: String): Response<ResponseBody> {
        return bookingApiService.downloadTicket(bookingId)
    }

    suspend fun getMyBookings(
        status: String? = null,
        page: Int = 1,
        limit: Int = 10
    ): BookingPaginatedResponseDto {
        return bookingApiService.getMyBookings(status, page, limit)
    }

    suspend fun getAdminBookings(
        branchId: String? = null,
        status: String? = null,
        page: Int = 1,
        limit: Int = 10
    ): BookingPaginatedResponseDto {
        return bookingApiService.getAdminBookings(branchId, status, page, limit)
    }

    suspend fun createAdminBooking(request: AdminCreateBookingDto): BookingResponse {
        return bookingApiService.createAdminBooking(request)
    }

    suspend fun cancelBooking(bookingId: String): Response<com.tanh.datsan.data.model.MessageResponseDto> {
        return bookingApiService.cancelBooking(bookingId)
    }
}