package com.tanh.datsan.data.repository

import com.tanh.datsan.core.TokenManager
import com.tanh.datsan.data.model.UpdateProfileRequest
import com.tanh.datsan.data.model.UserProfileLoggedInResponseDto // Thêm import từ Git
import com.tanh.datsan.data.network.UserApiService
import kotlinx.coroutines.flow.map
import okhttp3.MultipartBody
import okhttp3.Response
import javax.inject.Inject // Giữ javax.inject.Inject của bạn để hợp với Dagger/Hilt cục bộ
import javax.inject.Singleton

@Singleton // Rất quan trọng để Repository không bị tạo nhiều instance gây rò rỉ bộ nhớ
class UserRepository @Inject constructor(
    private val userApiService: UserApiService,
    private val tokenManager: TokenManager // Giữ lại TokenManager để cấp dữ liệu cho ViewModel
) {
    // ================= REMOTE API CALLS =================

    // Các hàm gọi API từ bản cục bộ (Local) của bạn
    suspend fun getProfile() = userApiService.getProfile()

    suspend fun updateProfile(fullName: String?, phoneNumber: String?, address: String?) =
        userApiService.updateProfile(UpdateProfileRequest(fullName, phoneNumber, address))

    suspend fun updateAvatar(avatar: MultipartBody.Part) =
        userApiService.updateAvatar(avatar)

    // Hàm gọi API mới được bổ sung từ bản Git
    suspend fun getProfileLoggedIn(): UserProfileLoggedInResponseDto {
        val response = userApiService.getProfile()
        val body = response.body() ?: throw Exception("Empty response")

        return UserProfileLoggedInResponseDto(
            id = body.id,
            email = body.email,
            fullName = body.userProfile?.fullName ?: "",
            avatarUrl = body.userProfile?.avatarUrl,
            role = body.role?.name ?: "",
            isProfileComplete = body.userProfile?.isProfileComplete ?: false
        )
    }

    // ================= LOCAL DATA (TOKEN MANAGER) =================
    // Giữ lại toàn bộ luồng Flow để MVVM tự động cập nhật UI khi có thay đổi

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