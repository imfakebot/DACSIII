package com.tanh.datsan.data.network

import com.tanh.datsan.data.model.*
import retrofit2.Response
import retrofit2.http.*
import okhttp3.MultipartBody

interface UserApiService {
    @GET("users/me")
    suspend fun getCurrentUserProfile(): AccountResponse

    @PUT("users/me/profile")
    suspend fun updateProfile(@Body request: UpdateProfileRequest): Response<AuthMessageResponse>

    @Multipart
    @PATCH("users/me/avatar")
    suspend fun uploadAvatar(@Part avatar: MultipartBody.Part): Response<AvatarUpdateResponse>
}
