package com.tanh.datsan.data.network

import com.tanh.datsan.data.model.BookedSlotsResponse
import com.tanh.datsan.data.model.BookingResponse
import com.tanh.datsan.data.model.CreateBookingDto
import com.tanh.datsan.data.model.CreateBookingResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

data class CheckInDto(
    val identifier: String
)

interface BookingApiService {
    @POST("bookings")
    suspend fun createBooking(
        @Body bookingRequest: CreateBookingDto
    ): CreateBookingResponse

    @GET("bookings/field/{fieldId}/schedule")
    suspend fun getFieldScheduleInAday(
      @Path("fieldId") fieldId: String,
      @Query("date") date: String
    ):Response<BookedSlotsResponse>

    @GET("bookings/{bookingId}")
    suspend fun getBookingById(
        @Path("bookingId") bookingId: String
    ): BookingResponse

    @POST("bookings/check-in")
    suspend fun checkIn(
        @Body checkInDto: CheckInDto
    ): BookingResponse

}