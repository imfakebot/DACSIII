package com.tanh.datsan.data.network

import com.tanh.datsan.data.model.BookingResponse
import com.tanh.datsan.data.model.CreateBookingDto
import com.tanh.datsan.data.model.FieldResponse
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

interface ApiService {
    @GET("fields")
    suspend fun getAllFields(
        @Query("latitude") lat: String? = null,
        @Query("longitude") lng: String? = null,
        @Query("radius") radius: Int? = 10, // Mặc định bán kính 10km
        @Query("cityId") cityId: Int? = null
    ): List<FieldResponse>
    @GET("fields/{id}")
    suspend fun getFieldDetail(@Path("id") fieldId: String): FieldResponse

    @POST("bookings")
    suspend fun createBooking(
        @Body bookingRequest: CreateBookingDto
    ): BookingResponse
}