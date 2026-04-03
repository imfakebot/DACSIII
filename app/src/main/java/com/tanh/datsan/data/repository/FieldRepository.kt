package com.tanh.datsan.data.repository

import com.tanh.datsan.data.model.BookingResponse
import com.tanh.datsan.data.model.CreateBookingDto
import com.tanh.datsan.data.model.FieldResponse
import com.tanh.datsan.data.network.RetrofitClient

class FieldRepository{
    private val apiService = RetrofitClient.apiService

    suspend fun getFieldDetail(fieldId:String): FieldResponse {
        return apiService.getFieldDetail(fieldId)
    }

    suspend fun createBooking(request: CreateBookingDto): BookingResponse {
        return apiService.createBooking(request)
    }
}