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
    val isLoadingWards: Boolean = false,
    val isEditing: Boolean = false,
    val toastMessage: String? = null,
    val avatarUrl: String? = null,
    val cities: List<com.tanh.datsan.data.model.CityDto> = emptyList(),
    val wards: List<com.tanh.datsan.data.model.WardDto> = emptyList(),
    // Read-only fields for display
    val displayCityName: String = "",
    val displayWardName: String = "",
    
    // Editable fields
    val fullName: String = "",
    val phoneNumber: String = "",
    val gender: String = "",
    val dateOfBirth: String = "",
    val bio: String = "",
    val street: String = "",
    val selectedCityId: String? = null,
    val selectedWardId: String? = null
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
            val address = currentProfile.address
            val street = address?.street ?: ""
            val cityName = address?.cityName ?: ""
            val wardName = address?.wardName ?: ""

            // Giữ lại tên vừa cập nhật nếu API trả về rỗng (trường hợp BE chưa kịp đồng bộ)
            val finalCityName = cityName.ifBlank { _uiState.value.displayCityName }
            val finalWardName = wardName.ifBlank { _uiState.value.displayWardName }
            
            _uiState.value = _uiState.value.copy(
                fullName = currentProfile.fullName ?: "",
                phoneNumber = currentProfile.phoneNumber ?: "",
                gender = currentProfile.gender ?: "",
                dateOfBirth = currentProfile.dateOfBirth ?: "",
                bio = currentProfile.bio ?: "",
                street = street.ifBlank { _uiState.value.street },
                displayCityName = finalCityName,
                displayWardName = finalWardName,
                selectedCityId = null, // Will be resolved
                selectedWardId = null // Will be resolved
            )
            
            resolveLocationIds()
        }
    }

    private fun resolveLocationIds() {
        val state = _uiState.value
        if (state.selectedCityId == null && state.displayCityName.isNotBlank() && state.cities.isNotEmpty()) {
            val cityId = state.cities.find { it.name.equals(state.displayCityName, ignoreCase = true) }?.id
            if (cityId != null) {
                _uiState.value = _uiState.value.copy(selectedCityId = cityId)
                fetchWardsAndSelect(cityId, state.displayWardName)
            }
        }
    }

    private fun fetchWardsAndSelect(cityId: String, wardNameToSelect: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoadingWards = true)
            try {
                val result = userRepository.getWards(cityId)
                val wardId = result.find { it.name.equals(wardNameToSelect, ignoreCase = true) }?.id
                _uiState.value = _uiState.value.copy(
                    wards = result,
                    selectedWardId = wardId
                )
            } catch (e: Exception) {
                Log.e("PROFILE_VM", "Error fetching wards: ${e.message}")
            } finally {
                _uiState.value = _uiState.value.copy(isLoadingWards = false)
            }
        }
    }

    fun fetchCities() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoadingCities = true)
            try {
                val result = userRepository.getCities()
                _uiState.value = _uiState.value.copy(cities = result)
                // Cố gắng map lại ID nếu Profile đã load xong trước Cities
                resolveLocationIds()
            } catch (e: Exception) {
                Log.e("PROFILE_VM", "Error fetching cities: ${e.message}")
            } finally {
                _uiState.value = _uiState.value.copy(isLoadingCities = false)
            }
        }
    }

    fun fetchWards(cityId: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoadingWards = true)
            try {
                val result = userRepository.getWards(cityId)
                _uiState.value = _uiState.value.copy(wards = result)
            } catch (e: Exception) {
                Log.e("PROFILE_VM", "Error fetching wards: ${e.message}")
            } finally {
                _uiState.value = _uiState.value.copy(isLoadingWards = false)
            }
        }
    }

    fun onCitySelected(cityId: String) {
        _uiState.value = _uiState.value.copy(
            selectedCityId = cityId,
            selectedWardId = null, // Reset ward khi đổi city
            wards = emptyList()
        )
        fetchWards(cityId)
    }

    fun onWardSelected(wardId: String) {
        _uiState.value = _uiState.value.copy(
            selectedWardId = wardId
        )
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
                        address = userMe?.userProfile?.address?.street,
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
            val state = _uiState.value
            
            // Validate: Nếu đã chọn address thì các trường street, cityId, wardId là bắt buộc
            val hasAddressInput = state.selectedCityId != null || state.selectedWardId != null || state.street.isNotBlank()
            val isAddressIncomplete = hasAddressInput && (state.selectedCityId == null || state.selectedWardId == null || state.street.isBlank())

            if (isAddressIncomplete) {
                _uiState.value = _uiState.value.copy(toastMessage = "Vui lòng nhập đầy đủ Tỉnh, Phường và Tên đường")
                return@launch
            }

            _uiState.value = _uiState.value.copy(isLoading = true)
            try {
                val address = if (hasAddressInput) {
                    com.tanh.datsan.data.model.AddressDto(
                        street = state.street.trim(),
                        cityId = state.selectedCityId!!,
                        wardId = state.selectedWardId!!
                    )
                } else null

                val request = com.tanh.datsan.data.model.UpdateProfileRequest(
                    fullName = state.fullName.ifBlank { null },
                    phoneNumber = state.phoneNumber.ifBlank { null },
                    gender = state.gender.ifBlank { null },
                    dateOfBirth = state.dateOfBirth.ifBlank { null },
                    bio = state.bio.ifBlank { null },
                    address = address
                )

                val response = userRepository.updateProfile(request)
                if (response.isSuccessful) {
                    // Cập nhật ngay lập tức giao diện (Optimistic Update)
                    val updatedCityName = state.cities.find { it.id == state.selectedCityId }?.name ?: state.displayCityName
                    val updatedWardName = state.wards.find { it.id == state.selectedWardId }?.name ?: state.displayWardName

                    _uiState.value = _uiState.value.copy(
                        toastMessage = "Cập nhật thành công",
                        isEditing = false,
                        displayCityName = updatedCityName,
                        displayWardName = updatedWardName
                    )
                    fetchProfile() 
                } else {
                    val errorBody = response.errorBody()?.string()
                    Log.e("PROFILE_VM", "Update failed: Code ${response.code()}, Body: $errorBody")
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

    fun clearToast() { _uiState.value = _uiState.value.copy(toastMessage = null) }
    fun logout() { viewModelScope.launch { userRepository.clearUserData() } }
}
