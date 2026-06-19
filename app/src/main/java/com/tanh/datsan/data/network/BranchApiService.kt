package com.tanh.datsan.data.network

import com.tanh.datsan.data.model.AvailableManagerDto
import com.tanh.datsan.data.model.BranchDetailDto
import com.tanh.datsan.data.model.CreateBranchRequest
import com.tanh.datsan.data.model.UpdateBranchRequest
import retrofit2.Response
import retrofit2.http.*

interface BranchApiService {

    @GET("branches")
    suspend fun getAllBranches(): Response<List<BranchDetailDto>>

    @GET("branches/available-managers")
    suspend fun getAvailableManagers(): Response<List<AvailableManagerDto>>

    @POST("branches")
    suspend fun createBranch(@Body request: CreateBranchRequest): Response<BranchDetailDto>

    @PUT("branches/{id}")
    suspend fun updateBranch(
        @Path("id") id: String,
        @Body request: UpdateBranchRequest
    ): Response<BranchDetailDto>

    @DELETE("branches/{id}")
    suspend fun deleteBranch(@Path("id") id: String): Response<Unit>
}
