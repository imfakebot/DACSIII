package com.tanh.datsan.data.network

import com.tanh.datsan.data.model.CheckPriceDto
import com.tanh.datsan.data.model.CheckPriceResponseDto
import com.tanh.datsan.data.model.TimeSlotResponse
import com.tanh.datsan.data.model.UpdateTimeSlotRequest
import com.tanh.datsan.data.model.CreateTimeSlotRequest
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.Path

interface PricingApiService {
    @POST("pricing/check-availability")
    suspend fun checkPrice(@Body request: CheckPriceDto): CheckPriceResponseDto

    @GET("pricing/time-slots")
    suspend fun getAllTimeSlots(): Response<List<TimeSlotResponse>>

    @PATCH("pricing/time-slot/{id}")
    suspend fun updateTimeSlot(
        @Path("id") id: Int,
        @Body request: UpdateTimeSlotRequest
    ): Response<TimeSlotResponse>

    @POST("pricing/time-slots")
    suspend fun createTimeSlot(
        @Body request: CreateTimeSlotRequest
    ): Response<TimeSlotResponse>
}
