package com.tanh.datsan.data.repository

import com.tanh.datsan.data.model.UserProfileLoggedInResponseDto
import com.tanh.datsan.data.network.UserApiService
import jakarta.inject.Inject

class UserRepository @Inject constructor(
    private val userApiService: UserApiService
) {
    suspend fun getProfileLogginedIn(): UserProfileLoggedInResponseDto {
        return userApiService.getCurrentUserProfileWhenLoggedIn()
    }
}