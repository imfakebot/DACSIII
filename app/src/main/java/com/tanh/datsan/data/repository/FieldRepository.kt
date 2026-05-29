package com.tanh.datsan.data.repository

import com.tanh.datsan.data.model.FieldResponse
import com.tanh.datsan.data.model.FieldType
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
        radius: Int? = null,
        cityId: Int? = null,
        name: String? = null,
        typeId: String? = null,
        branchId: String? = null
    ): List<FieldResponse> {
        return fieldApiService.getAllFields(
            lat = lat,
            lng = lon,
            fieldTypeId = typeId,
            radius = radius,
            cityId = cityId,
            name = name,
            branchId = branchId
        )
    }

    suspend fun getAllFieldTypes(): List<FieldType> {
        return fieldApiService.getAllFieldTypes()
    }
}