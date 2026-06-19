package com.tanh.datsan.data.network

import com.tanh.datsan.data.model.ApiFieldResponse
import com.tanh.datsan.data.model.FieldResponse
import com.tanh.datsan.data.model.FieldType
import com.tanh.datsan.data.model.CreateFieldDto
import com.tanh.datsan.data.model.MessageResponseDto
import com.tanh.datsan.data.model.UpdateFieldDto
import okhttp3.MultipartBody
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Part
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
        @Query("branchId") branchId: String? = null,
        @Query("page") page: Int? = 1,
        @Query("limit") limit: Int? = 10
    ): ApiFieldResponse<List<FieldResponse>>

    @GET("fields/{id}")
    suspend fun getFieldDetail(
        @Path("id") fieldId: String,
        @Query("latitude") latitude: String?,
        @Query("longitude") longitude: String?
    ): FieldResponse

    @GET("field-types")
    suspend fun getAllFieldTypes(): List<FieldType>

    @POST("field-types")
    suspend fun createFieldType(@Body dto: com.tanh.datsan.data.model.CreateFieldTypeDto): com.tanh.datsan.data.model.FieldType

    @retrofit2.http.PATCH("field-types/{id}")
    suspend fun updateFieldType(@Path("id") id: String, @Body dto: com.tanh.datsan.data.model.UpdateFieldTypeDto): com.tanh.datsan.data.model.FieldType

    @DELETE("field-types/{id}")
    suspend fun deleteFieldType(@Path("id") id: String): MessageResponseDto

    @GET("utilities")
    suspend fun getAllUtilities(): List<com.tanh.datsan.data.model.Utility>

    @POST("utilities")
    suspend fun createUtility(@Body dto: com.tanh.datsan.data.model.CreateUtilityDto): com.tanh.datsan.data.model.Utility

    @PUT("utilities/{id}")
    suspend fun updateUtility(@Path("id") id: Int, @Body dto: com.tanh.datsan.data.model.UpdateUtilityDto): com.tanh.datsan.data.model.Utility

    @DELETE("utilities/{id}")
    suspend fun deleteUtility(@Path("id") id: Int): MessageResponseDto

    @POST("fields")
    suspend fun createField(@Body dto: CreateFieldDto): FieldResponse

    @PUT("fields/{id}")
    suspend fun updateField(@Path("id") id: String, @Body dto: UpdateFieldDto): FieldResponse

    @DELETE("fields/{id}")
    suspend fun deleteField(@Path("id") id: String): MessageResponseDto

    @Multipart
    @POST("fields/{id}/images")
    suspend fun uploadImages(
        @Path("id") id: String,
        @Part images: List<MultipartBody.Part>
    ): List<com.tanh.datsan.data.model.FieldImage>
}
