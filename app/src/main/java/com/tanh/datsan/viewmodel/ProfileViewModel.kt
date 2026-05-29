package com.tanh.datsan.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tanh.datsan.data.model.UserMeResponse
import com.tanh.datsan.data.model.UserProfileDto
import com.tanh.datsan.data.repository.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import java.io.File
import javax.inject.Inject

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val userRepository: UserRepository
) : ViewModel() {

    private val _profileState = MutableStateFlow<UserMeResponse?>(null)
    val profileState: StateFlow<UserMeResponse?> = _profileState.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _toastMessage = MutableStateFlow<String?>(null)
    val toastMessage: StateFlow<String?> = _toastMessage.asStateFlow()

    val userAvatarUrl: StateFlow<String?> = userRepository.userAvatarUrl
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val userName: StateFlow<String?> = userRepository.userName
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val userPhone: StateFlow<String?> = userRepository.userPhone
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val userAddress: StateFlow<String?> = userRepository.userAddress
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    init {
        fetchProfile()
    }

    fun fetchProfile() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val response = userRepository.getProfile()
                if (response.isSuccessful) {
                    val userMe = response.body()
                    _profileState.value = userMe
                    // Sync with repository local storage
                    userRepository.saveUserInfo(
                        avatarUrl = userMe?.userProfile?.avatarUrl,
                        userName = userMe?.userProfile?.fullName,
                        phone = userMe?.userProfile?.phoneNumber,
                        address = userMe?.userProfile?.address
                    )
                }
            } catch (e: Exception) {
                _toastMessage.value = "Lỗi tải thông tin cá nhân"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun updateProfile(fullName: String, phoneNumber: String, address: String) {
        if (fullName.isBlank()) {
            _toastMessage.value = "Họ tên không được để trống"
            return
        }
        if (phoneNumber.isNotEmpty() && !phoneNumber.all { it.isDigit() }) {
            _toastMessage.value = "Số điện thoại chỉ được chứa chữ số"
            return
        }
        
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val response = userRepository.updateProfile(fullName, phoneNumber, address)
                if (response.isSuccessful) {
                    _toastMessage.value = "Cập nhật thành công"
                    userRepository.saveUserInfo(
                        avatarUrl = userAvatarUrl.value,
                        userName = fullName,
                        phone = phoneNumber,
                        address = address
                    )
                    fetchProfile() 
                } else {
                    val errorBody = response.errorBody()?.string()
                    android.util.Log.e("PROFILE_ERROR", "Update failed: ${response.code()} - $errorBody")
                    _toastMessage.value = "Cập nhật thất bại: ${response.code()}"
                }
            } catch (e: Exception) {
                _toastMessage.value = "Lỗi kết nối: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun uploadAvatar(imageFile: File) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val requestFile = imageFile.asRequestBody("image/*".toMediaTypeOrNull())
                val body = MultipartBody.Part.createFormData("avatar", imageFile.name, requestFile)
                
                val response = userRepository.updateAvatar(body)
                if (response.isSuccessful) {
                    _toastMessage.value = "Tải ảnh đại diện thành công"
                    val newAvatarUrl = response.body()?.avatarUrl
                    userRepository.saveUserInfo(
                        avatarUrl = newAvatarUrl,
                        userName = userName.value,
                        phone = userPhone.value,
                        address = userAddress.value
                    )
                    fetchProfile()
                } else {
                    val errorBody = response.errorBody()?.string()
                    android.util.Log.e("PROFILE_ERROR", "Upload failed: ${response.code()} - $errorBody")
                    _toastMessage.value = "Lỗi upload: ${response.code()}"
                }
            } catch (e: Exception) {
                android.util.Log.e("PROFILE_ERROR", "Upload exception", e)
                _toastMessage.value = "Lỗi: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun clearToast() {
        _toastMessage.value = null
    }

    fun logout() {
        viewModelScope.launch {
            userRepository.clearUserData()
        }
    }
}
