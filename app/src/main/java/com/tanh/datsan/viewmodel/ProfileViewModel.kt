package com.tanh.datsan.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tanh.datsan.core.UserManager
import com.tanh.datsan.data.model.UpdateProfileRequest
import com.tanh.datsan.data.repository.UserRepository
import com.tanh.datsan.utils.ResponseHelper.parseError
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import retrofit2.HttpException
import java.io.File
import javax.inject.Inject

import kotlinx.coroutines.flow.update

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val userRepository: UserRepository,
    private val userManager: UserManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(ProfileUiState())
    val uiState: StateFlow<ProfileUiState> = _uiState.asStateFlow()

    init {
        fetchProfile()
        viewModelScope.launch {
            userManager.userAvatarUrl.collect { url ->
                _uiState.update { it.copy(avatarUrl = url) }
            }
        }
    }

    fun toggleEditing(edit: Boolean) {
        _uiState.update { it.copy(isEditing = edit) }
        if (edit) {
            fetchCities()
        } else {
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

            _uiState.update {
                it.copy(
                    fullName = currentProfile.fullName ?: "",
                    phoneNumber = currentProfile.phoneNumber ?: "",
                    gender = currentProfile.gender ?: "",
                    dateOfBirth = currentProfile.dateOfBirth ?: "",
                    bio = currentProfile.bio ?: "",
                    street = street,
                    displayCityName = cityName,
                    displayWardName = wardName,
                    selectedCityId = null,
                    selectedWardId = null
                )
            }
            resolveLocationIds()
        }
    }

    private fun resolveLocationIds() {
        val state = _uiState.value
        if (state.selectedCityId == null && state.displayCityName.isNotBlank() && state.cities.isNotEmpty()) {
            val cityId =
                state.cities.find { it.name.equals(state.displayCityName, ignoreCase = true) }?.id
            if (cityId != null) {
                _uiState.update { it.copy(selectedCityId = cityId) }
                fetchWardsAndSelect(cityId, state.displayWardName)
            }
        }
    }

    private fun fetchWardsAndSelect(cityId: Int, wardNameToSelect: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoadingWards = true) }
            try {
                val result = userRepository.getWards(cityId.toString())
                val wardId = result.find { it.name.equals(wardNameToSelect, ignoreCase = true) }?.id
                _uiState.update {
                    it.copy(
                        wards = result,
                        selectedWardId = wardId
                    )
                }
            } catch (e: Exception) {
                Log.e("PROFILE_VM", "Error fetching wards: ${e.message}")
                val errorMessage = if (e is HttpException) {
                    parseError(e.response()?.errorBody()?.string())
                } else {
                    "Lỗi kết nối"
                }
                _uiState.update { it.copy(toastMessage = "Thất bại: $errorMessage") }
            } finally {
                _uiState.update { it.copy(isLoadingWards = false) }
            }
        }
    }

    fun fetchCities() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoadingCities = true) }
            try {
                val result = userRepository.getCities()
                _uiState.update { it.copy(cities = result) }
                resolveLocationIds()
            } catch (e: Exception) {
                Log.e("PROFILE_VM", "Error fetching cities: ${e.message}")
                _uiState.update { it.copy(toastMessage = "Thất bại: Lỗi kết nối") }
            } finally {
                _uiState.update { it.copy(isLoadingCities = false) }
            }
        }
    }

    fun fetchWards(cityId: Int) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoadingWards = true) }
            try {
                val result = userRepository.getWards(cityId.toString())
                _uiState.update { it.copy(wards = result) }
            } catch (_: Exception) {
                _uiState.update { it.copy(toastMessage = "Thất bại: Lỗi kết nối") }
            } finally {
                _uiState.update { it.copy(isLoadingWards = false) }
            }
        }
    }

    fun onCitySelected(cityId: Int) {
        val cityName = _uiState.value.cities.find { it.id == cityId }?.name ?: ""
        _uiState.update {
            it.copy(
                selectedCityId = cityId,
                selectedWardId = null,
                wards = emptyList(),
                displayCityName = cityName,
                displayWardName = ""
            )
        }
        fetchWards(cityId)
    }

    fun onWardSelected(wardId: Int) {
        val wardName = _uiState.value.wards.find { it.id == wardId }?.name ?: ""
        _uiState.update {
            it.copy(
                selectedWardId = wardId,
                displayWardName = wardName
            )
        }
    }

    private suspend fun fetchProfileInternal() {
        _uiState.update { it.copy(isLoading = true) }
        try {
            val accountResponse = userRepository.getUserProfile()
            val profile = accountResponse.userProfile
            Log.d("PROFILE_VM", "Fetched profile: $profile")

            _uiState.update {
                it.copy(
                    profile = accountResponse,
                    fullName = profile?.fullName ?: "",
                    phoneNumber = profile?.phoneNumber ?: "",
                    gender = profile?.gender ?: "",
                    dateOfBirth = profile?.dateOfBirth ?: "",
                    bio = profile?.bio ?: "",
                    street = profile?.address?.street ?: "",
                    displayCityName = profile?.address?.cityName ?: "",
                    displayWardName = profile?.address?.wardName ?: "",
                    avatarUrl = profile?.avatarUrl,
                    email = accountResponse.email
                )
            }
            userManager.setUserInfo(
                name = profile?.fullName ?: "",
                avatarUrl = profile?.avatarUrl,
                branchId = accountResponse.managedBranchId
            )
        } catch (e: Exception) {
            Log.e("PROFILE_VM", "Error fetching profile: ${e.message}")
            val errorMessage = if (e is HttpException) {
                parseError(e.response()?.errorBody()?.string())
            } else "Lỗi kết nối"
            _uiState.update { it.copy(toastMessage = "Thất bại: $errorMessage") }
        } finally {
            _uiState.update { it.copy(isLoading = false) }
        }
    }

    fun fetchProfile() {
        viewModelScope.launch { fetchProfileInternal() }
    }

    fun updateProfile() {
        viewModelScope.launch {
            val state = _uiState.value
            val hasAddressInput = state.selectedCityId != null || state.selectedWardId != null || state.street.isNotBlank()
            val isAddressIncomplete = hasAddressInput && (state.selectedCityId == null || state.selectedWardId == null || state.street.isBlank())

            if (isAddressIncomplete) {
                _uiState.update { it.copy(toastMessage = "Vui lòng nhập đầy đủ Tỉnh, Phường và Tên đường") }
                return@launch
            }

            _uiState.update { it.copy(isLoading = true) }
            try {
                val address = if (hasAddressInput) {
                    com.tanh.datsan.data.model.AddressDto(
                        street = state.street.trim(),
                        cityId = state.selectedCityId!!,
                        wardId = state.selectedWardId!!
                    )
                } else null

                val request = UpdateProfileRequest(
                    fullName = state.fullName.ifBlank { null },
                    phoneNumber = state.phoneNumber.ifBlank { null },
                    gender = state.gender.ifBlank { null },
                    dateOfBirth = state.dateOfBirth.ifBlank { null },
                    bio = state.bio.ifBlank { null },
                    address = address
                )

                val response = userRepository.updateProfile(request)
                if (response.isSuccessful) {
                    _uiState.update { 
                        it.copy(
                            toastMessage = "Cập nhật thành công",
                            isEditing = false,
                            selectedCityId = null,
                            selectedWardId = null
                        )
                    }
                    fetchProfileInternal()
                } else {
                    val errorMsg = parseError(response.errorBody()?.string())
                    _uiState.update { it.copy(toastMessage = "Thất bại: $errorMsg") }
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(toastMessage = "Thất bại: Lỗi kết nối") }
            } finally {
                _uiState.update { it.copy(isLoading = false) }
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
                Log.d("Profileviewmodel","response upload avatar : $response")
                if (response.isSuccessful) {
                    _uiState.value =
                        _uiState.value.copy(toastMessage = "Tải ảnh đại diện thành công")
                    fetchProfile()
                } else {
                    val errorBody = response.errorBody()?.string()
                    Log.e(
                        "PROFILE_VM",
                        "Upload avatar failed: Code ${response.code()}, Body: $errorBody"
                    )
                    val errorMessage = parseError(errorBody)
                    _uiState.value = _uiState.value.copy(toastMessage = "Thất bại: $errorMessage")
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(toastMessage = "Thất bại: Lỗi kết nối")
            } finally {
                _uiState.value = _uiState.value.copy(isLoading = false)
            }
        }
    }

    // Input handlers
    fun onFullNameChange(value: String) {
        _uiState.update { it.copy(fullName = value) }
    }

    fun onPhoneNumberChange(value: String) {
        _uiState.update { it.copy(phoneNumber = value) }
    }

    fun onGenderChange(value: String) {
        _uiState.update { it.copy(gender = value) }
    }

    fun onDobChange(value: String) {
        _uiState.update { it.copy(dateOfBirth = value) }
    }

    fun onBioChange(value: String) {
        _uiState.update { it.copy(bio = value) }
    }

    fun onStreetChange(value: String) {
        _uiState.update { it.copy(street = value) }
    }

    fun clearToast() {
        _uiState.update { it.copy(toastMessage = null) }
    }

    fun logout() {
        viewModelScope.launch { userManager.clearUserInfo() }
    }
}
