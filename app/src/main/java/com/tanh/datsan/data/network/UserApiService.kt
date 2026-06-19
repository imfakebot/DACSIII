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

    @GET("users/admin/all")
    suspend fun getAdminUsers(
        @Query("page") page: Int? = 1,
        @Query("limit") limit: Int? = 10
    ): AccountPaginatedResponseDto

    @PATCH("users/admin/{id}/ban")
    suspend fun banUser(@Path("id") id: String): MessageResponseDto

    @PATCH("users/admin/{id}/unban")
    suspend fun unbanUser(@Path("id") id: String): MessageResponseDto

    @POST("users/create-employee")
    suspend fun createEmployee(@Body dto: CreateEmployeeDto): AccountResponseDto
}
