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

data class ProfileUiState(
    val profile: UserMeResponse? = null,
    val isLoading: Boolean = false,
    val isLoadingCities: Boolean = false,
    val isEditing: Boolean = false,
    val toastMessage: String? = null,
    val avatarUrl: String? = null,
    val cities: List<com.tanh.datsan.data.model.CityDto> = emptyList(),
    val wards: List<com.tanh.datsan.data.model.WardDto> = emptyList(),
    // Editable fields
    val fullName: String = "",
    val phoneNumber: String = "",
    val gender: String = "",
    val dateOfBirth: String = "",
    val bio: String = "",
    val street: String = "",
    val selectedCityId: Int? = null,
    val selectedWardId: Int? = null
)

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val userRepository: UserRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ProfileUiState())
    val uiState: StateFlow<ProfileUiState> = _uiState.asStateFlow()

    init {
        // Collect userAvatarUrl from repository and update uiState
        viewModelScope.launch {
            userRepository.userAvatarUrl.collect { url ->
                _uiState.value = _uiState.value.copy(avatarUrl = url)
            }
        }
        refreshAll()
    }

    fun refreshAll() {
        fetchProfile()
        fetchCities()
    }

    fun toggleEditing(edit: Boolean) {
        _uiState.value = _uiState.value.copy(isEditing = edit)
        if (!edit) {
            resetEditingFields()
        }
    }

    private fun resetEditingFields() {
        val currentProfile = _uiState.value.profile?.userProfile
        if (currentProfile != null) {
            _uiState.value = _uiState.value.copy(
                fullName = currentProfile.fullName ?: "",
                phoneNumber = currentProfile.phoneNumber ?: "",
                gender = currentProfile.gender ?: "",
                dateOfBirth = currentProfile.dateOfBirth ?: "",
                bio = currentProfile.bio ?: "",
                street = currentProfile.street ?: "",
                selectedCityId = currentProfile.city?.id,
                selectedWardId = currentProfile.ward?.id
            )
            currentProfile.city?.id?.let { cityId -> fetchWards(cityId) }
        }
    }

    fun fetchCities() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoadingCities = true)
            try {
                val result = userRepository.getCities()
                _uiState.value = _uiState.value.copy(cities = result)
            } catch (e: Exception) {
                Log.e("PROFILE_VM", "Error fetching cities: ${e.message}")
            } finally {
                _uiState.value = _uiState.value.copy(isLoadingCities = false)
            }
        }
    }

    fun fetchWards(cityId: Int) {
        viewModelScope.launch {
            try {
                val result = userRepository.getWards(cityId)
                _uiState.value = _uiState.value.copy(wards = result)
            } catch (e: Exception) {
                Log.e("PROFILE_VM", "Error fetching wards: ${e.message}")
            }
        }
    }

    fun onCitySelected(cityId: Int) {
        _uiState.value = _uiState.value.copy(
            selectedCityId = cityId,
            selectedWardId = null,
            wards = emptyList()
        )
        fetchWards(cityId)
    }

    fun fetchProfile() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            try {
                val response = userRepository.getProfile()
                if (response.isSuccessful) {
                    val userMe = response.body()
                    _uiState.value = _uiState.value.copy(profile = userMe)
                    resetEditingFields()

                    userRepository.saveUserInfo(
                        avatarUrl = userMe?.userProfile?.avatarUrl,
                        userName = userMe?.userProfile?.fullName,
                        phone = userMe?.userProfile?.phoneNumber,
                        address = userMe?.userProfile?.street,
                        gender = userMe?.userProfile?.gender,
                        dob = userMe?.userProfile?.dateOfBirth,
                        bio = userMe?.userProfile?.bio
                    )
                }
            } catch (e: Exception) {
                Log.e("PROFILE_VM", "Exception: ${e.message}")
            } finally {
                _uiState.value = _uiState.value.copy(isLoading = false)
            }
        }
    }

    fun updateProfile() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            try {
                val state = _uiState.value
                val address = if (state.selectedCityId != null && state.selectedWardId != null) {
                    com.tanh.datsan.data.model.AddressDto(
                        street = state.street,
                        cityId = state.selectedCityId,
                        wardId = state.selectedWardId
                    )
                } else null

                val request = com.tanh.datsan.data.model.UpdateProfileRequest(
                    fullName = state.fullName.ifBlank { null },
                    gender = state.gender.ifBlank { null },
                    dateOfBirth = state.dateOfBirth.ifBlank { null },
                    bio = state.bio.ifBlank { null },
                    address = address
                )

                val response = userRepository.updateProfile(request)
                if (response.isSuccessful) {
                    _uiState.value = _uiState.value.copy(
                        toastMessage = "Cập nhật thành công",
                        isEditing = false
                    )
                    fetchProfile() 
                } else {
                    _uiState.value = _uiState.value.copy(toastMessage = "Thất bại: ${response.code()}")
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(toastMessage = "Lỗi kết nối")
            } finally {
                _uiState.value = _uiState.value.copy(isLoading = false)
            }
        }
    }

    fun uploadAvatar(imageFile: File) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            try {
                val requestFile = imageFile.asRequestBody("image/*".toMediaTypeOrNull())
                val body = MultipartBody.Part.createFormData("avatar", imageFile.name, requestFile)
                
                val response = userRepository.updateAvatar(body)
                if (response.isSuccessful) {
                    _uiState.value = _uiState.value.copy(toastMessage = "Tải ảnh đại diện thành công")
                    fetchProfile()
                } else {
                    _uiState.value = _uiState.value.copy(toastMessage = "Lỗi upload: ${response.code()}")
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(toastMessage = "Lỗi: ${e.message}")
            } finally {
                _uiState.value = _uiState.value.copy(isLoading = false)
            }
        }
    }

    // Input handlers
    fun onFullNameChange(value: String) { _uiState.value = _uiState.value.copy(fullName = value) }
    fun onPhoneNumberChange(value: String) { _uiState.value = _uiState.value.copy(phoneNumber = value) }
    fun onGenderChange(value: String) { _uiState.value = _uiState.value.copy(gender = value) }
    fun onDobChange(value: String) { _uiState.value = _uiState.value.copy(dateOfBirth = value) }
    fun onBioChange(value: String) { _uiState.value = _uiState.value.copy(bio = value) }
    fun onStreetChange(value: String) { _uiState.value = _uiState.value.copy(street = value) }
    fun onWardSelected(value: Int) { _uiState.value = _uiState.value.copy(selectedWardId = value) }

    fun clearToast() { _uiState.value = _uiState.value.copy(toastMessage = null) }
    fun logout() { viewModelScope.launch { userRepository.clearUserData() } }
}
