package com.tanh.datsan.viewmodel

import android.util.Log
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

    private val _isLoadingCities = MutableStateFlow(false)
    val isLoadingCities: StateFlow<Boolean> = _isLoadingCities.asStateFlow()

    private val _isEditing = MutableStateFlow(false)
    val isEditing: StateFlow<Boolean> = _isEditing.asStateFlow()

    // --- Editable Fields ---
    val fullName = MutableStateFlow("")
    val phoneNumber = MutableStateFlow("")
    val gender = MutableStateFlow("")
    val dateOfBirth = MutableStateFlow("")
    val bio = MutableStateFlow("")
    val street = MutableStateFlow("")
    val selectedCityId = MutableStateFlow<Int?>(null)
    val selectedWardId = MutableStateFlow<Int?>(null)

    private val _toastMessage = MutableStateFlow<String?>(null)
    val toastMessage: StateFlow<String?> = _toastMessage.asStateFlow()

    val userAvatarUrl: StateFlow<String?> = userRepository.userAvatarUrl
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    private val _cities = MutableStateFlow<List<com.tanh.datsan.data.model.CityDto>>(emptyList())
    val cities: StateFlow<List<com.tanh.datsan.data.model.CityDto>> = _cities.asStateFlow()

    private val _wards = MutableStateFlow<List<com.tanh.datsan.data.model.WardDto>>(emptyList())
    val wards: StateFlow<List<com.tanh.datsan.data.model.WardDto>> = _wards.asStateFlow()

    init {
        refreshAll()
    }

    fun refreshAll() {
        fetchProfile()
        fetchCities()
    }

    fun toggleEditing(edit: Boolean) {
        _isEditing.value = edit
        if (!edit) {
            resetEditingFields()
        }
    }

    private fun resetEditingFields() {
        val currentProfile = _profileState.value?.userProfile
        if (currentProfile != null) {
            fullName.value = currentProfile.fullName ?: ""
            phoneNumber.value = currentProfile.phoneNumber ?: ""
            gender.value = currentProfile.gender ?: ""
            dateOfBirth.value = currentProfile.dateOfBirth ?: ""
            bio.value = currentProfile.bio ?: ""
            street.value = currentProfile.street ?: ""
            selectedCityId.value = currentProfile.city?.id
            selectedWardId.value = currentProfile.ward?.id
            currentProfile.city?.id?.let { cityId -> fetchWards(cityId) }
        }
    }

    fun fetchCities() {
        viewModelScope.launch {
            _isLoadingCities.value = true
            try {
                val result = userRepository.getCities()
                _cities.value = result
            } catch (e: Exception) {
                Log.e("PROFILE_VM", "Error fetching cities: ${e.message}")
            } finally {
                _isLoadingCities.value = false
            }
        }
    }

    fun fetchWards(cityId: Int) {
        viewModelScope.launch {
            try {
                val result = userRepository.getWards(cityId)
                _wards.value = result
            } catch (e: Exception) {
                Log.e("PROFILE_VM", "Error fetching wards: ${e.message}")
            }
        }
    }

    fun onCitySelected(cityId: Int) {
        selectedCityId.value = cityId
        selectedWardId.value = null 
        fetchWards(cityId)
    }

    fun fetchProfile() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val response = userRepository.getProfile()
                if (response.isSuccessful) {
                    val userMe = response.body()
                    _profileState.value = userMe
                    resetEditingFields()

                    userRepository.saveUserInfo(
                        avatarUrl = userMe?.userProfile?.avatarUrl,
                        userName = userMe?.userProfile?.fullName,
                        phone = userMe?.userProfile?.phoneNumber,
                        address = userMe?.userProfile?.street, // Lưu street làm address
                        gender = userMe?.userProfile?.gender,
                        dob = userMe?.userProfile?.dateOfBirth,
                        bio = userMe?.userProfile?.bio
                    )
                }
            } catch (e: Exception) {
                Log.e("PROFILE_VM", "Exception: ${e.message}")
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun updateProfile() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                // Cấu trúc address Object như Backend yêu cầu
                val address = if (selectedCityId.value != null && selectedWardId.value != null) {
                    com.tanh.datsan.data.model.AddressDto(
                        street = street.value,
                        cityId = selectedCityId.value!!,
                        wardId = selectedWardId.value!!
                    )
                } else null

                val request = com.tanh.datsan.data.model.UpdateProfileRequest(
                    fullName = fullName.value.ifBlank { null },
                    gender = gender.value.ifBlank { null },
                    dateOfBirth = dateOfBirth.value.ifBlank { null },
                    bio = bio.value.ifBlank { null },
                    address = address
                )

                val response = userRepository.updateProfile(request)
                if (response.isSuccessful) {
                    _toastMessage.value = "Cập nhật thành công"
                    _isEditing.value = false
                    fetchProfile() 
                } else {
                    _toastMessage.value = "Thất bại: ${response.code()}"
                }
            } catch (e: Exception) {
                _toastMessage.value = "Lỗi kết nối"
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
                // Tên Part là 'avatar' đúng như Backend "chốt hạ"
                val body = MultipartBody.Part.createFormData("avatar", imageFile.name, requestFile)
                
                val response = userRepository.updateAvatar(body)
                if (response.isSuccessful) {
                    _toastMessage.value = "Tải ảnh đại diện thành công"
                    fetchProfile()
                } else {
                    _toastMessage.value = "Lỗi upload: ${response.code()}"
                }
            } catch (e: Exception) {
                _toastMessage.value = "Lỗi: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun clearToast() { _toastMessage.value = null }
    fun logout() { viewModelScope.launch { userRepository.clearUserData() } }
}
