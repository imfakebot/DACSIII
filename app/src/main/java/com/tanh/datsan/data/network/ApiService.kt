package com.tanh.datsan.data.network

import com.tanh.datsan.data.model.FieldModel
import retrofit2.http.GET

data class FieldResponse(
    val success: Boolean,
    val data : List<FieldModel>
)

interface ApiService {
    @GET("fields")
    suspend fun getAllField(): FieldResponse
}

