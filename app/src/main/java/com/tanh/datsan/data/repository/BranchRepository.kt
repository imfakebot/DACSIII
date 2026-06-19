package com.tanh.datsan.data.repository

import com.tanh.datsan.data.model.CreateBranchRequest
import com.tanh.datsan.data.model.CreateFieldRequest
import com.tanh.datsan.data.model.UpdateBranchRequest
import com.tanh.datsan.data.model.UpdateFieldRequest
import com.tanh.datsan.data.network.BranchApiService
import com.tanh.datsan.data.network.FieldApiService
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BranchRepository @Inject constructor(
    private val branchApiService: BranchApiService,
    private val fieldApiService: FieldApiService
) {
    // Branch
    suspend fun getAllBranches() = branchApiService.getAllBranches()
    suspend fun getAvailableManagers() = branchApiService.getAvailableManagers()
    suspend fun createBranch(request: CreateBranchRequest) = branchApiService.createBranch(request)
    suspend fun updateBranch(id: String, request: UpdateBranchRequest) = branchApiService.updateBranch(id, request)
    suspend fun deleteBranch(id: String) = branchApiService.deleteBranch(id)

    // Field (Admin)
    suspend fun getFieldsByBranch(branchId: String) = fieldApiService.getAllFields(branchId = branchId)
    suspend fun getAllFieldTypes() = fieldApiService.getAllFieldTypes()
    suspend fun createField(request: CreateFieldRequest) = fieldApiService.createField(request)
    suspend fun updateField(id: String, request: UpdateFieldRequest) = fieldApiService.updateField(id, request)
    suspend fun deleteField(id: String) = fieldApiService.deleteField(id)

    suspend fun uploadFieldImage(fieldId: String, file: okhttp3.MultipartBody.Part) =
        fieldApiService.uploadFieldImage(fieldId, file)
}
