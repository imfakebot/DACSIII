package com.tanh.datsan.data.repository

import com.tanh.datsan.data.model.ApiFieldResponse
import com.tanh.datsan.data.model.FieldResponse
import com.tanh.datsan.data.model.FieldType
import com.tanh.datsan.data.model.CreateFieldDto
import com.tanh.datsan.data.model.MessageResponseDto
import com.tanh.datsan.data.model.UpdateFieldDto
import com.tanh.datsan.data.network.FieldApiService
import okhttp3.MultipartBody
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FieldRepository @Inject constructor(
    private val fieldApiService: FieldApiService
) {
    suspend fun getFieldDetail(fieldId: String,latitude:String?,longitude:String?): FieldResponse {
        return fieldApiService.getFieldDetail(fieldId,latitude,longitude)
    }

    suspend fun getAllField(
        lat: String?,
        lon: String?,
        radius: Int? = null,
        cityId: Int? = null,
        name: String? = null,
        typeId: String? = null,
        branchId: String? = null,
        page: Int? = 1,
        limit: Int? = 10
    ): ApiFieldResponse<List<FieldResponse>>{
        return fieldApiService.getAllFields(
            lat = lat,
            lng = lon,
            fieldTypeId = typeId,
            radius = radius,
            cityId = cityId,
            name = name,
            branchId = branchId,
            page = page,
            limit = limit
        )
    }

    suspend fun getAllFieldTypes(): List<FieldType> {
        return fieldApiService.getAllFieldTypes()
    }

    suspend fun createFieldType(dto: com.tanh.datsan.data.model.CreateFieldTypeDto): FieldType {
        return fieldApiService.createFieldType(dto)
    }

    suspend fun updateFieldType(id: String, dto: com.tanh.datsan.data.model.UpdateFieldTypeDto): FieldType {
        return fieldApiService.updateFieldType(id, dto)
    }

    suspend fun deleteFieldType(id: String): MessageResponseDto {
        return fieldApiService.deleteFieldType(id)
    }

    suspend fun getAllUtilities(): List<com.tanh.datsan.data.model.Utility> {
        return fieldApiService.getAllUtilities()
    }

    suspend fun createUtility(dto: com.tanh.datsan.data.model.CreateUtilityDto): com.tanh.datsan.data.model.Utility {
        return fieldApiService.createUtility(dto)
    }

    suspend fun updateUtility(id: Int, dto: com.tanh.datsan.data.model.UpdateUtilityDto): com.tanh.datsan.data.model.Utility {
        return fieldApiService.updateUtility(id, dto)
    }

    suspend fun deleteUtility(id: Int): MessageResponseDto {
        return fieldApiService.deleteUtility(id)
    }


    suspend fun createField(dto: CreateFieldDto): FieldResponse {
        return fieldApiService.createField(dto)
    }

    suspend fun updateField(id: String, dto: UpdateFieldDto): FieldResponse {
        return fieldApiService.updateField(id, dto)
    }

    suspend fun deleteField(id: String): MessageResponseDto {
        return fieldApiService.deleteField(id)
    }

    suspend fun uploadImages(id: String, images: List<MultipartBody.Part>): List<com.tanh.datsan.data.model.FieldImage> {
        return fieldApiService.uploadImages(id, images)
    }
}