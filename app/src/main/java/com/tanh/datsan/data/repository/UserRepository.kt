package com.tanh.datsan.data.repository

import com.tanh.datsan.data.model.UpdateProfileRequest
import com.tanh.datsan.data.network.UserApiService
import okhttp3.MultipartBody
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UserRepository @Inject constructor(
    private val userApiService: UserApiService
) {
    suspend fun getProfile() = userApiService.getProfile()

    suspend fun updateProfile(fullName: String?, phoneNumber: String?, address: String?) =
        userApiService.updateProfile(UpdateProfileRequest(fullName, phoneNumber, address))

    suspend fun updateAvatar(avatar: MultipartBody.Part) =
        userApiService.updateAvatar(avatar)
}
