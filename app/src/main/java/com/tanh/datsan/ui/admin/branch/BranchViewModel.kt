package com.tanh.datsan.ui.admin.branch

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tanh.datsan.data.model.Branch
import com.tanh.datsan.data.model.CityDto
import com.tanh.datsan.data.model.CreateBranchDto
import com.tanh.datsan.data.model.UpdateBranchDto
import com.tanh.datsan.data.model.WardDto
import com.tanh.datsan.data.repository.BranchRepository
import com.tanh.datsan.data.repository.UserRepository
import com.tanh.datsan.ui.state.ActionState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

import com.tanh.datsan.ui.state.BranchState

@HiltViewModel
class BranchViewModel @Inject constructor(
    private val branchRepository: BranchRepository,
    private val userRepository: UserRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(BranchState())
    val uiState: StateFlow<BranchState> = _uiState.asStateFlow()

    fun fetchBranches() {
        viewModelScope.launch {
            _uiState.update { it.copy(actionState = ActionState.Loading) }
            try {
                val list = branchRepository.getBranches()
                _uiState.update { it.copy(branches = list, actionState = ActionState.Idle) }
            } catch (e: Exception) {
                _uiState.update { it.copy(actionState = ActionState.Error(e.message ?: "Lỗi khi lấy danh sách chi nhánh")) }
            }
        }
    }

    fun fetchAvailableManagers() {
        viewModelScope.launch {
            try {
                val list = branchRepository.getAvailableManagers()
                _uiState.update { it.copy(availableManagers = list) }
            } catch (e: Exception) {
                android.util.Log.e("BranchViewModel", "fetchAvailableManagers error", e)
            }
        }
    }

    fun getBranchById(id: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(actionState = ActionState.Loading) }
            try {
                val branch = branchRepository.getBranch(id)
                _uiState.update { it.copy(selectedBranch = branch, actionState = ActionState.Idle) }
            } catch (e: Exception) {
                _uiState.update { it.copy(actionState = ActionState.Error(e.message ?: "Lỗi khi lấy thông tin chi nhánh")) }
            }
        }
    }

    fun createBranch(dto: CreateBranchDto) {
        viewModelScope.launch {
            _uiState.update { it.copy(actionState = ActionState.Loading) }
            try {
                branchRepository.createBranch(dto)
                _uiState.update { it.copy(actionState = ActionState.Success("Tạo chi nhánh thành công")) }
                fetchBranches()
            } catch (e: Exception) {
                _uiState.update { it.copy(actionState = ActionState.Error(e.message ?: "Lỗi khi tạo chi nhánh")) }
            }
        }
    }

    fun updateBranch(id: String, dto: UpdateBranchDto) {
        viewModelScope.launch {
            _uiState.update { it.copy(actionState = ActionState.Loading) }
            try {
                branchRepository.updateBranch(id, dto)
                _uiState.update { it.copy(actionState = ActionState.Success("Cập nhật chi nhánh thành công")) }
                fetchBranches()
            } catch (e: Exception) {
                _uiState.update { it.copy(actionState = ActionState.Error(e.message ?: "Lỗi khi cập nhật chi nhánh")) }
            }
        }
    }

    fun deleteBranch(id: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(actionState = ActionState.Loading) }
            try {
                branchRepository.deleteBranch(id)
                _uiState.update { it.copy(actionState = ActionState.Success("Xóa chi nhánh thành công")) }
                fetchBranches()
            } catch (e: Exception) {
                _uiState.update { it.copy(actionState = ActionState.Error(e.message ?: "Lỗi khi xóa chi nhánh")) }
            }
        }
    }

    // ─── Location ─────────────────────────────────────────────────────────────
    fun fetchCities() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoadingCities = true) }
            try {
                val cities = userRepository.getCities()
                _uiState.update { it.copy(cities = cities) }
            } catch (_: Exception) {}
            finally { _uiState.update { it.copy(isLoadingCities = false) } }
        }
    }

    fun fetchWards(cityId: Int) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoadingWards = true, wards = emptyList()) }
            try {
                val wards = userRepository.getWards(cityId.toString())
                _uiState.update { it.copy(wards = wards) }
            } catch (_: Exception) {}
            finally { _uiState.update { it.copy(isLoadingWards = false) } }
        }
    }

    fun clearWards() { _uiState.update { it.copy(wards = emptyList()) } }

    // ─── State helpers ────────────────────────────────────────────────────────
    fun resetActionState() { _uiState.update { it.copy(actionState = ActionState.Idle) } }
    fun clearSelectedBranch() { _uiState.update { it.copy(selectedBranch = null) } }
}
