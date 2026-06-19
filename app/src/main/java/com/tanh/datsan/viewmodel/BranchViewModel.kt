package com.tanh.datsan.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tanh.datsan.data.model.Branch
import com.tanh.datsan.data.model.CityDto
import com.tanh.datsan.data.model.CreateBranchDto
import com.tanh.datsan.data.model.UpdateBranchDto
import com.tanh.datsan.data.model.UserProfileDto
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

sealed class BranchUiState {
    object Loading : BranchUiState()
    data class Success(val message: String? = null) : BranchUiState()
    data class Error(val message: String) : BranchUiState()
    object Idle : BranchUiState()
}

@HiltViewModel
class BranchViewModel @Inject constructor(
    private val branchRepository: BranchRepository,
    private val userRepository: UserRepository
) : ViewModel() {

    private val _branches = MutableStateFlow<List<Branch>>(emptyList())
    val branches: StateFlow<List<Branch>> = _branches.asStateFlow()

    private val _availableManagers = MutableStateFlow<List<com.tanh.datsan.data.model.AccountResponseDto>>(emptyList())
    val availableManagers: StateFlow<List<com.tanh.datsan.data.model.AccountResponseDto>> = _availableManagers.asStateFlow()

    private val _uiState = MutableStateFlow<BranchUiState>(BranchUiState.Idle)
    val uiState: StateFlow<BranchUiState> = _uiState.asStateFlow()

    private val _selectedBranch = MutableStateFlow<Branch?>(null)
    val selectedBranch: StateFlow<Branch?> = _selectedBranch.asStateFlow()

    // Location
    private val _cities = MutableStateFlow<List<CityDto>>(emptyList())
    val cities: StateFlow<List<CityDto>> = _cities.asStateFlow()

    private val _wards = MutableStateFlow<List<WardDto>>(emptyList())
    val wards: StateFlow<List<WardDto>> = _wards.asStateFlow()

    private val _isLoadingCities = MutableStateFlow(false)
    val isLoadingCities: StateFlow<Boolean> = _isLoadingCities.asStateFlow()

    private val _isLoadingWards = MutableStateFlow(false)
    val isLoadingWards: StateFlow<Boolean> = _isLoadingWards.asStateFlow()

    fun fetchBranches() {
        viewModelScope.launch {
            _uiState.value = BranchUiState.Loading
            try {
                val list = branchRepository.getBranches()
                _branches.value = list
                _uiState.value = BranchUiState.Idle
            } catch (e: Exception) {
                _uiState.value = BranchUiState.Error(e.message ?: "Lỗi khi lấy danh sách chi nhánh")
            }
        }
    }

    fun fetchAvailableManagers() {
        viewModelScope.launch {
            try {
                val list = branchRepository.getAvailableManagers()
                _availableManagers.value = list
            } catch (e: Exception) {
                android.util.Log.e("BranchViewModel", "fetchAvailableManagers error", e)
            }
        }
    }

    fun getBranchById(id: String) {
        viewModelScope.launch {
            _uiState.value = BranchUiState.Loading
            try {
                val branch = branchRepository.getBranch(id)
                _selectedBranch.value = branch
                _uiState.value = BranchUiState.Idle
            } catch (e: Exception) {
                _uiState.value = BranchUiState.Error(e.message ?: "Lỗi khi lấy thông tin chi nhánh")
            }
        }
    }

    fun createBranch(dto: CreateBranchDto) {
        viewModelScope.launch {
            _uiState.value = BranchUiState.Loading
            try {
                branchRepository.createBranch(dto)
                _uiState.value = BranchUiState.Success("Tạo chi nhánh thành công")
                fetchBranches()
            } catch (e: Exception) {
                _uiState.value = BranchUiState.Error(e.message ?: "Lỗi khi tạo chi nhánh")
            }
        }
    }

    fun updateBranch(id: String, dto: UpdateBranchDto) {
        viewModelScope.launch {
            _uiState.value = BranchUiState.Loading
            try {
                branchRepository.updateBranch(id, dto)
                _uiState.value = BranchUiState.Success("Cập nhật chi nhánh thành công")
                fetchBranches()
            } catch (e: Exception) {
                _uiState.value = BranchUiState.Error(e.message ?: "Lỗi khi cập nhật chi nhánh")
            }
        }
    }

    fun deleteBranch(id: String) {
        viewModelScope.launch {
            _uiState.value = BranchUiState.Loading
            try {
                branchRepository.deleteBranch(id)
                _uiState.value = BranchUiState.Success("Xóa chi nhánh thành công")
                fetchBranches()
            } catch (e: Exception) {
                _uiState.value = BranchUiState.Error(e.message ?: "Lỗi khi xóa chi nhánh")
            }
        }
    }

    // ─── Location ─────────────────────────────────────────────────────────────
    fun fetchCities() {
        viewModelScope.launch {
            _isLoadingCities.value = true
            try {
                _cities.value = userRepository.getCities()
            } catch (_: Exception) {}
            finally { _isLoadingCities.value = false }
        }
    }

    fun fetchWards(cityId: Int) {
        viewModelScope.launch {
            _isLoadingWards.value = true
            _wards.value = emptyList()
            try {
                _wards.value = userRepository.getWards(cityId.toString())
            } catch (_: Exception) {}
            finally { _isLoadingWards.value = false }
        }
    }

    fun clearWards() { _wards.value = emptyList() }

    // ─── State helpers ────────────────────────────────────────────────────────
    fun resetUiState() { _uiState.value = BranchUiState.Idle }
    fun clearSelectedBranch() { _selectedBranch.value = null }
}
