package com.tanh.datsan.data.repository

import com.tanh.datsan.data.model.AccountResponse
import com.tanh.datsan.data.network.LocationApiService
import com.tanh.datsan.data.network.UserApiService
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UserRepository @Inject constructor(
    private val userApiService: UserApiService,
    private val locationApiService: LocationApiService
) {
    suspend fun getUserProfile(): AccountResponse {
        return userApiService.getCurrentUserProfile()
    }

    suspend fun updateProfile(request: com.tanh.datsan.data.model.UpdateProfileRequest) =
        userApiService.updateProfile(request)

    suspend fun updateAvatar(avatar: okhttp3.MultipartBody.Part) =
        userApiService.uploadAvatar(avatar)

    suspend fun getCities() = locationApiService.getCities()

    suspend fun getWards(cityId: String) = locationApiService.getWards(cityId)
}
