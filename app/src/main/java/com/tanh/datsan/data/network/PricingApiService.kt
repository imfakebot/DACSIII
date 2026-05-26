package com.tanh.datsan.data.network

import com.tanh.datsan.data.model.CheckPriceDto
import com.tanh.datsan.data.model.CheckPriceResponseDto
import retrofit2.http.Body
import retrofit2.http.POST

interface PricingApiService {
    @POST("pricing/check-availability")
    suspend fun checkPrice(@Body request: CheckPriceDto): CheckPriceResponseDto
}
