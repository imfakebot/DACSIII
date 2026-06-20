package com.tanh.datsan.data.repository

import com.tanh.datsan.data.model.AccountPaginatedResponseDto
import com.tanh.datsan.data.model.AccountResponseDto
import com.tanh.datsan.data.model.CreateEmployeeDto
import com.tanh.datsan.data.model.MessageResponseDto
import com.tanh.datsan.data.network.UserApiService
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AdminUserRepository @Inject constructor(
    private val apiService: UserApiService
) {
    suspend fun getAdminUsers(page: Int = 1, limit: Int = 10, search: String? = null): AccountPaginatedResponseDto =
        apiService.getAdminUsers(page, limit, search)

    suspend fun banUser(id: String): MessageResponseDto =
        apiService.banUser(id)

    suspend fun unbanUser(id: String): MessageResponseDto =
        apiService.unbanUser(id)

    suspend fun createEmployee(dto: CreateEmployeeDto): AccountResponseDto =
        apiService.createEmployee(dto)
}
