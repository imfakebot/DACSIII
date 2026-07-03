package com.tanh.datsan.data.repository

import com.tanh.datsan.data.model.CheckPriceDto
import com.tanh.datsan.data.model.CheckPriceResponseDto
import com.tanh.datsan.data.model.TimeSlotResponse
import com.tanh.datsan.data.model.UpdateTimeSlotRequest
import com.tanh.datsan.data.model.CreateTimeSlotRequest
import com.tanh.datsan.data.network.PricingApiService
import retrofit2.Response
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PricingRepository @Inject constructor(
    private val pricingApiService: PricingApiService
) {
    suspend fun checkPrice(fieldId: String, startTime: String, durationMinutes: Int): CheckPriceResponseDto {
        return pricingApiService.checkPrice(
            CheckPriceDto(fieldId, startTime, durationMinutes)
        )
    }

    suspend fun getAllTimeSlots(): Response<List<TimeSlotResponse>> {
        return pricingApiService.getAllTimeSlots()
    }

    suspend fun updateTimeSlot(id: Int, request: UpdateTimeSlotRequest): Response<TimeSlotResponse> {
        return pricingApiService.updateTimeSlot(id, request)
    }

    suspend fun createTimeSlot(request: CreateTimeSlotRequest): Response<TimeSlotResponse> {
        return pricingApiService.createTimeSlot(request)
    }
}
