package com.tanh.datsan.data.network

import com.tanh.datsan.data.model.AdminCreateBookingDto
import com.tanh.datsan.data.model.BookedSlotsResponse
import com.tanh.datsan.data.model.BookingPaginatedResponseDto
import com.tanh.datsan.data.model.BookingResponse
import com.tanh.datsan.data.model.CreateBookingDto
import com.tanh.datsan.data.model.CreateBookingResponse
import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query
import retrofit2.http.Streaming

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

    @Streaming
    @GET("bookings/{bookingId}/download")
    suspend fun downloadTicket(
        @Path("bookingId") bookingId: String
    ): Response<ResponseBody>

    @GET("bookings/me")
    suspend fun getMyBookings(
        @Query("status") status: String? = null,
        @Query("page") page: Int? = 1,
        @Query("limit") limit: Int? = 10
    ): BookingPaginatedResponseDto

    @GET("bookings/management/all")
    suspend fun getAdminBookings(
        @Query("branchId") branchId: String? = null,
        @Query("status") status: String? = null,
        @Query("page") page: Int? = 1,
        @Query("limit") limit: Int? = 10
    ):BookingPaginatedResponseDto

    @POST("bookings/management/create")
    suspend fun createAdminBooking(@Body request: AdminCreateBookingDto): BookingResponse

    @retrofit2.http.PATCH("bookings/{id}/cancel")
    suspend fun cancelBooking(
        @Path("id") bookingId: String
    ): Response<com.tanh.datsan.data.model.MessageResponseDto>
}