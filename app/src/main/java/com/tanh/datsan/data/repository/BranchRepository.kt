package com.tanh.datsan.data.repository

import com.tanh.datsan.data.model.Branch
import com.tanh.datsan.data.model.CreateBranchDto
import com.tanh.datsan.data.model.MessageResponseDto
import com.tanh.datsan.data.model.UpdateBranchDto
import com.tanh.datsan.data.model.UserProfileDto
import com.tanh.datsan.data.network.BranchApiService
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BranchRepository @Inject constructor(
    private val apiService: BranchApiService
) {
    suspend fun createBranch(dto: CreateBranchDto): Branch =
        apiService.createBranch(dto)

    suspend fun getBranches(): List<Branch> =
        apiService.getBranches()

    suspend fun getAvailableManagers(): List<com.tanh.datsan.data.model.AccountResponseDto> =
        apiService.getAvailableManagers()

    suspend fun getBranch(id: String): Branch =
        apiService.getBranch(id)

    suspend fun updateBranch(id: String, dto: UpdateBranchDto): Branch =
        apiService.updateBranch(id, dto)

    suspend fun deleteBranch(id: String): MessageResponseDto =
        apiService.deleteBranch(id)
}
