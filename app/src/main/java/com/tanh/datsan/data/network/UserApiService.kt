package com.tanh.datsan.data.network

import com.tanh.datsan.data.model.*
import okhttp3.MultipartBody
import retrofit2.Response
import retrofit2.http.*

interface UserApiService {
    @GET("users/me")
    suspend fun getProfile(): Response<UserMeResponse>

    // Backend gợi ý dùng PUT /users/me/profile
    @PUT("users/me/profile")
    suspend fun updateProfile(@Body request: UpdateProfileRequest): Response<AuthMessageResponse>

    @Multipart
    @PATCH("users/me/avatar")
    suspend fun updateAvatar(
        @Part avatar: MultipartBody.Part
    ): Response<AvatarUpdateResponse>
}
