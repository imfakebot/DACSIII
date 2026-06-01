package com.tanh.datsan.data.repository

import com.tanh.datsan.core.TokenManager
import com.tanh.datsan.core.UserManager
import com.tanh.datsan.data.model.UpdateProfileRequest
import com.tanh.datsan.data.network.UserApiService
import kotlinx.coroutines.flow.map
import okhttp3.MultipartBody
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UserRepository @Inject constructor(
    private val userApiService: UserApiService,
    private val tokenManager: TokenManager,
    private val userManager: UserManager,
    private val locationRepository: LocationRepository
) {
    // ================= REMOTE API CALLS =================

    suspend fun getProfile() = userApiService.getProfile()

    suspend fun updateProfile(request: UpdateProfileRequest) =
        userApiService.updateProfile(request)

    suspend fun updateAvatar(avatar: MultipartBody.Part) =
        userApiService.updateAvatar(avatar)

    suspend fun getCities() = locationRepository.getCities()

    suspend fun getWards(cityId: Int) = locationRepository.getWards(cityId)

    // ================= LOCAL DATA =================

    val userAvatarUrl = userManager.userAvatar
    val userName = userManager.userName
    val userPhone = userManager.userPhone
    val userAddress = userManager.userAddress
    val userGender = userManager.userGender
    val userDob = userManager.userDob
    val userBio = userManager.userBio

    val isLoggedIn = tokenManager.getAccessToken.map { token ->
        !token.isNullOrBlank() && token != "null" && token != "undefined"
    }

    fun saveUserInfo(
        avatarUrl: String?,
        userName: String?,
        phone: String? = null,
        address: String? = null,
        gender: String? = null,
        dob: String? = null,
        bio: String? = null
    ) {
        userManager.setUserInfo(userName, avatarUrl, phone, address, gender, dob, bio)
    }

    suspend fun clearUserData() {
        tokenManager.clearTokens()
        userManager.clearUserInfo()
    }
}