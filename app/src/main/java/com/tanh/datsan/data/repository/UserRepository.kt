package com.tanh.datsan.data.repository

import com.tanh.datsan.core.TokenManager
import com.tanh.datsan.data.model.UpdateProfileRequest
import com.tanh.datsan.data.network.UserApiService
import kotlinx.coroutines.flow.map
import okhttp3.MultipartBody
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UserRepository @Inject constructor(
    private val userApiService: UserApiService,
    private val tokenManager: TokenManager
) {
    // Remote API calls
    suspend fun getProfile() = userApiService.getProfile()

    suspend fun updateProfile(fullName: String?, phoneNumber: String?, address: String?) =
        userApiService.updateProfile(UpdateProfileRequest(fullName, phoneNumber, address))

    suspend fun updateAvatar(avatar: MultipartBody.Part) =
        userApiService.updateAvatar(avatar)

    // Local data from TokenManager (DataStore)
    val userAvatarUrl = tokenManager.getUserAvatar
    val userName = tokenManager.getUserName
    val userPhone = tokenManager.getUserPhone
    val userAddress = tokenManager.getUserAddress
    
    val isLoggedIn = tokenManager.getAccessToken.map { token ->
        !token.isNullOrBlank() && token != "null" && token != "undefined"
    }

    suspend fun saveUserInfo(
        avatarUrl: String?,
        userName: String?,
        phone: String? = null,
        address: String? = null
    ) {
        tokenManager.saveUserInfo(avatarUrl, userName, phone, address)
    }

    suspend fun clearUserData() {
        tokenManager.clearTokens()
    }
}
