package com.tanh.datsan.data.network

import com.tanh.datsan.data.model.ApiFieldResponse
import com.tanh.datsan.data.model.CreateFieldRequest
import com.tanh.datsan.data.model.FieldResponse
import com.tanh.datsan.data.model.FieldType
import com.tanh.datsan.data.model.UpdateFieldRequest
import retrofit2.Response
import retrofit2.http.*

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
    ): ApiFieldResponse<List<FieldResponse>>

    @GET("fields/{id}")
    suspend fun getFieldDetail(
        @Path("id") fieldId: String,
        @Query("latitude") latitude: String?,
        @Query("longitude") longitude: String?
    ): FieldResponse

    @GET("field-types")
    suspend fun getAllFieldTypes(): List<FieldType>

    // Admin CRUD
    @POST("fields")
    suspend fun createField(@Body request: CreateFieldRequest): Response<FieldResponse>

    @PUT("fields/{id}")
    suspend fun updateField(
        @Path("id") id: String,
        @Body request: UpdateFieldRequest
    ): Response<FieldResponse>

    @DELETE("fields/{id}")
    suspend fun deleteField(@Path("id") id: String): Response<Unit>
}
