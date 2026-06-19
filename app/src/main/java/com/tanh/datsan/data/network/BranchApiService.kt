package com.tanh.datsan.data.network

import com.tanh.datsan.data.model.Branch
import com.tanh.datsan.data.model.CreateBranchDto
import com.tanh.datsan.data.model.MessageResponseDto
import com.tanh.datsan.data.model.UpdateBranchDto
import com.tanh.datsan.data.model.UserProfileDto
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path

interface BranchApiService {
    @POST("branches")
    suspend fun createBranch(@Body dto: CreateBranchDto): Branch

    @GET("branches")
    suspend fun getBranches(): List<Branch>

    @GET("branches/available-managers")
    suspend fun getAvailableManagers(): List<com.tanh.datsan.data.model.AccountResponseDto>

    @GET("branches/{id}")
    suspend fun getBranch(@Path("id") id: String): Branch

    @PUT("branches/{id}")
    suspend fun updateBranch(@Path("id") id: String, @Body dto: UpdateBranchDto): Branch

    @DELETE("branches/{id}")
    suspend fun deleteBranch(@Path("id") id: String): MessageResponseDto
}
