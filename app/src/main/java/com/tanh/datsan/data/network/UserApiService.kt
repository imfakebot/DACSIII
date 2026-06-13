package com.tanh.datsan.data.network

import com.tanh.datsan.data.model.AccountResponse
import retrofit2.http.GET

interface UserApiService {
    @GET("users/me")
    suspend fun getCurrentUserProfile(): AccountResponse
}
