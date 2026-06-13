package com.tanh.datsan.data.repository

import com.tanh.datsan.data.model.AccountResponse
import com.tanh.datsan.data.network.UserApiService
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UserRepository @Inject constructor(
    private val userApiService: UserApiService,
) {
    suspend fun getUserProfile(): AccountResponse {
        return userApiService.getCurrentUserProfile()
    }
}
