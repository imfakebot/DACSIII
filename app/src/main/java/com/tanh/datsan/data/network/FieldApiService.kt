package com.tanh.datsan.data.network

import com.tanh.datsan.data.model.FieldResponse
import com.tanh.datsan.data.model.FieldType
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface FieldApiService {
    @GET("fields")
    suspend fun getAllFields(
        @Query("latitude") lat: String? = null,
        @Query("longitude") lng: String? = null,
        @Query("fieldTypeId") fieldTypeId: String? = null,
        @Query("radius") radius: Int? = 10,
        @Query("cityId") cityId: Int? = null,
        @Query("name") name: String? = null,
        @Query("branchId") branchId: String? = null
    ): List<FieldResponse>

    @GET("fields/{id}")
    suspend fun getFieldDetail(
        @Path("id") fieldId: String
    ): FieldResponse

    @GET("field-types")
    suspend fun getAllFieldTypes(): List<FieldType>
}