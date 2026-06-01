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
        fetchProfile()
        fetchCities()
    }

    fun toggleEditing(edit: Boolean) {
        _isEditing.value = edit
        if (!edit) {
            // If canceling, reset to current profile data
            resetEditingFields()
        }
    }

    private fun resetEditingFields() {
        _profileState.value?.userProfile?.let {
            fullName.value = it.fullName ?: ""
            phoneNumber.value = it.phoneNumber ?: ""
            gender.value = it.gender ?: ""
            dateOfBirth.value = it.dateOfBirth ?: ""
            bio.value = it.bio ?: ""
            street.value = it.street ?: ""
            selectedCityId.value = it.city?.id
            selectedWardId.value = it.ward?.id
            it.city?.id?.let { cityId -> fetchWards(cityId) }
        }
    }

    fun fetchCities() {
        viewModelScope.launch {
            try {
                _cities.value = userRepository.getCities()
            } catch (e: Exception) {
                android.util.Log.e("PROFILE_VM", "Error fetching cities: ${e.message}")
            }
        }
    }

    fun fetchWards(cityId: Int) {
        viewModelScope.launch {
            try {
                _wards.value = userRepository.getWards(cityId)
                android.util.Log.d("PROFILE_VM", "Fetched ${_wards.value.size} wards for city $cityId")
            } catch (e: Exception) {
                android.util.Log.e("PROFILE_VM", "Error fetching wards: ${e.message}")
            }
        }
    }

    fun onCitySelected(cityId: Int) {
        selectedCityId.value = cityId
        selectedWardId.value = null // Reset ward when city changes
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
                    
                    // Sync fields for editing
                    resetEditingFields()

                    // Sync with repository local storage
                    userRepository.saveUserInfo(
                        avatarUrl = userMe?.userProfile?.avatarUrl,
                        userName = userMe?.userProfile?.fullName,
                        phone = userMe?.userProfile?.phoneNumber,
                        address = userMe?.userProfile?.address,
                        gender = userMe?.userProfile?.gender,
                        dob = userMe?.userProfile?.dateOfBirth,
                        bio = userMe?.userProfile?.bio
                    )
                }
            } catch (e: Exception) {
                _toastMessage.value = "Lỗi tải thông tin cá nhân"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun updateProfile() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val address = if (selectedCityId.value != null && selectedWardId.value != null) {
                    com.tanh.datsan.data.model.AddressDto(
                        street = street.value,
                        cityId = selectedCityId.value!!,
                        wardId = selectedWardId.value!!
                    )
                } else null

                val request = com.tanh.datsan.data.model.UpdateProfileRequest(
                    fullName = fullName.value,
                    phoneNumber = phoneNumber.value,
                    gender = gender.value,
                    dateOfBirth = dateOfBirth.value,
                    bio = bio.value,
                    address = address
                )

                android.util.Log.d("PROFILE_UPDATE", "Sending request: $request")

                val response = userRepository.updateProfile(request)
                if (response.isSuccessful) {
                    _toastMessage.value = "Cập nhật thành công"
                    _isEditing.value = false
                    fetchProfile() // Refresh data from server
                } else {
                    val errorBody = response.errorBody()?.string()
                    android.util.Log.e("PROFILE_ERROR", "Update failed: ${response.code()} - $errorBody")
                    _toastMessage.value = "Cập nhật thất bại: ${response.code()}"
                }
            } catch (e: Exception) {
                android.util.Log.e("PROFILE_ERROR", "Update exception", e)
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
                    
                    // Thêm timestamp để ép Flow và Coil cập nhật ảnh mới (tránh cache trùng URL)
                    val timestampedUrl = if (!newAvatarUrl.isNullOrBlank()) {
                        if (newAvatarUrl.contains("?")) "$newAvatarUrl&t=${System.currentTimeMillis()}" 
                        else "$newAvatarUrl?t=${System.currentTimeMillis()}"
                    } else newAvatarUrl

                    userRepository.saveUserInfo(
                        avatarUrl = timestampedUrl,
                        userName = fullName.value,
                        phone = phoneNumber.value,
                        address = street.value,
                        gender = gender.value,
                        dob = dateOfBirth.value,
                        bio = bio.value
                    )
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
