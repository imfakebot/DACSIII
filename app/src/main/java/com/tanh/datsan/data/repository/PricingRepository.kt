package com.tanh.datsan.data.repository

import com.tanh.datsan.data.model.CheckPriceDto
import com.tanh.datsan.data.model.CheckPriceResponseDto
import com.tanh.datsan.data.network.PricingApiService
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
}
