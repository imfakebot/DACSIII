package com.tanh.datsan.data.network

import com.tanh.datsan.data.model.UserProfileLoggedInResponseDto
import jakarta.inject.Singleton
import retrofit2.http.GET

interface UserApiService {
    @GET("users/me")
    suspend fun getCurrentUserProfileWhenLoggedIn(): UserProfileLoggedInResponseDto
}