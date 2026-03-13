package com.tanh.datsan.data.network

import com.tanh.datsan.data.model.FieldResponse
import retrofit2.http.GET
import retrofit2.http.Query

interface ApiService {
    @GET("fields")
    suspend fun getAllFields(
        @Query("latitude") lat: String? = null,
        @Query("longitude") lng: String? = null,
        @Query("radius") radius: Int? = 10, // Mặc định bán kính 10km
        @Query("cityId") cityId: Int? = null
    ): List<FieldResponse>
}