package com.tanh.datsan.data.repository

import com.tanh.datsan.data.model.FieldResponse
import com.tanh.datsan.data.network.FieldApiService
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FieldRepository @Inject constructor(
    private val fieldApiService: FieldApiService
) {
    suspend fun getFieldDetail(fieldId: String): FieldResponse {
        return fieldApiService.getFieldDetail(fieldId)
    }

    suspend fun getAllField(
        lat: String?,
        lon: String?,
        radius: Int?,
        cityId: Int?
    ): List<FieldResponse> {
        return fieldApiService.getAllFields(lat, lon, radius, cityId)
    }


}