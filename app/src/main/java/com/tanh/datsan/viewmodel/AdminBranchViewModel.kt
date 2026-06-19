package com.tanh.datsan.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tanh.datsan.data.model.AvailableManagerDto
import com.tanh.datsan.data.model.BranchDetailDto
import com.tanh.datsan.data.model.CreateBranchRequest
import com.tanh.datsan.data.model.UpdateBranchRequest
import com.tanh.datsan.data.model.CityDto
import com.tanh.datsan.data.model.WardDto
import com.tanh.datsan.data.repository.BranchRepository
import com.tanh.datsan.data.repository.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class AdminBranchUiState(
    val isLoading: Boolean = false,
    val branches: List<BranchDetailDto> = emptyList(),
    val errorMessage: String? = null,
    val successMessage: String? = null,
    val isSubmitting: Boolean = false,
    // Form
    val availableManagers: List<AvailableManagerDto> = emptyList(),
    val cities: List<CityDto> = emptyList(),
    val wards: List<WardDto> = emptyList(),
    val isLoadingManagers: Boolean = false,
    val isLoadingCities: Boolean = false,
    val isLoadingWards: Boolean = false
)

@HiltViewModel
class AdminBranchViewModel @Inject constructor(
    private val branchRepository: BranchRepository,
    private val userRepository: UserRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(AdminBranchUiState())
    val uiState: StateFlow<AdminBranchUiState> = _uiState.asStateFlow()

    init {
        fetchBranches()
    }

    fun fetchBranches() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            try {
                val response = branchRepository.getAllBranches()
                if (response.isSuccessful) {
                    _uiState.update { it.copy(isLoading = false, branches = response.body() ?: emptyList()) }
                } else {
                    _uiState.update { it.copy(isLoading = false, errorMessage = "Lỗi: ${response.message()}") }
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, errorMessage = "Lỗi kết nối: ${e.message}") }
            }
        }
    }

    fun loadFormData() {
        fetchAvailableManagers()
        fetchCities()
    }

    private fun fetchAvailableManagers() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoadingManagers = true) }
            try {
                val response = branchRepository.getAvailableManagers()
                if (response.isSuccessful) {
                    _uiState.update { it.copy(isLoadingManagers = false, availableManagers = response.body() ?: emptyList()) }
                } else {
                    _uiState.update { it.copy(isLoadingManagers = false) }
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoadingManagers = false) }
            }
        }
    }

    private fun fetchCities() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoadingCities = true) }
            try {
                val cities = userRepository.getCities()
                _uiState.update { it.copy(isLoadingCities = false, cities = cities) }
            } catch (e: Exception) {
                android.util.Log.e("AdminBranchViewModel", "fetchCities error: ${e.message}", e)
                _uiState.update { it.copy(isLoadingCities = false) }
            }
        }
    }

    fun fetchWards(cityId: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoadingWards = true, wards = emptyList()) }
            try {
                val wards = userRepository.getWards(cityId)
                _uiState.update { it.copy(isLoadingWards = false, wards = wards) }
            } catch (e: Exception) {
                android.util.Log.e("AdminBranchViewModel", "fetchWards error: ${e.message}", e)
                _uiState.update { it.copy(isLoadingWards = false) }
            }
        }
    }

    fun createBranch(request: CreateBranchRequest, onSuccess: () -> Unit) {
        viewModelScope.launch {
            _uiState.update { it.copy(isSubmitting = true, errorMessage = null) }
            try {
                val response = branchRepository.createBranch(request)
                if (response.isSuccessful && response.body() != null) {
                    val newBranch = response.body()!!
                    _uiState.update {
                        it.copy(
                            isSubmitting = false,
                            branches = it.branches + newBranch,
                            successMessage = "Tạo chi nhánh thành công!"
                        )
                    }
                    onSuccess()
                } else {
                    _uiState.update { it.copy(isSubmitting = false, errorMessage = "Không thể tạo chi nhánh: ${response.message()}") }
                }
            } catch (e: Exception) {
                android.util.Log.e("AdminBranchViewModel", "createBranch error: ${e.message}", e)
                _uiState.update { it.copy(isSubmitting = false, errorMessage = "Lỗi kết nối: ${e.message}") }
            }
        }
    }

    fun updateBranch(id: String, request: UpdateBranchRequest, onSuccess: () -> Unit) {
        viewModelScope.launch {
            _uiState.update { it.copy(isSubmitting = true, errorMessage = null) }
            try {
                val response = branchRepository.updateBranch(id, request)
                if (response.isSuccessful && response.body() != null) {
                    val updated = response.body()!!
                    _uiState.update {
                        it.copy(
                            isSubmitting = false,
                            branches = it.branches.map { b -> if (b.id == id) updated else b },
                            successMessage = "Cập nhật chi nhánh thành công!"
                        )
                    }
                    onSuccess()
                } else {
                    _uiState.update { it.copy(isSubmitting = false, errorMessage = "Không thể cập nhật: ${response.message()}") }
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(isSubmitting = false, errorMessage = "Lỗi kết nối: ${e.message}") }
            }
        }
    }

    fun deleteBranch(id: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(errorMessage = null) }
            try {
                val response = branchRepository.deleteBranch(id)
                if (response.isSuccessful) {
                    _uiState.update {
                        it.copy(
                            branches = it.branches.filter { b -> b.id != id },
                            successMessage = "Đã xóa chi nhánh!"
                        )
                    }
                } else {
                    _uiState.update { it.copy(errorMessage = "Không thể xóa chi nhánh: ${response.message()}") }
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(errorMessage = "Lỗi kết nối: ${e.message}") }
            }
        }
    }

    fun clearMessages() = _uiState.update { it.copy(errorMessage = null, successMessage = null) }
}
