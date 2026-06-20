package com.tanh.datsan.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tanh.datsan.data.model.AccountPaginatedResponseDto
import com.tanh.datsan.data.model.AccountResponseDto
import com.tanh.datsan.data.model.Branch
import com.tanh.datsan.data.model.CreateEmployeeDto
import com.tanh.datsan.data.repository.AdminUserRepository
import com.tanh.datsan.data.repository.BranchRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed class AdminUserUiState {
    object Loading : AdminUserUiState()
    data class Success(val message: String? = null) : AdminUserUiState()
    data class Error(val message: String) : AdminUserUiState()
    object Idle : AdminUserUiState()
}

@HiltViewModel
class AdminUserViewModel @Inject constructor(
    private val adminUserRepository: AdminUserRepository,
    private val branchRepository: BranchRepository
) : ViewModel() {

    private val _users = MutableStateFlow<List<AccountResponseDto>>(emptyList())
    val users: StateFlow<List<AccountResponseDto>> = _users.asStateFlow()

    private val _paginationInfo = MutableStateFlow<AccountPaginatedResponseDto?>(null)
    val paginationInfo: StateFlow<AccountPaginatedResponseDto?> = _paginationInfo.asStateFlow()

    private val _branches = MutableStateFlow<List<Branch>>(emptyList())
    val branches: StateFlow<List<Branch>> = _branches.asStateFlow()

    private val _uiState = MutableStateFlow<AdminUserUiState>(AdminUserUiState.Idle)
    val uiState: StateFlow<AdminUserUiState> = _uiState.asStateFlow()

    fun fetchInitialData() {
        viewModelScope.launch {
            try {
                _branches.value = branchRepository.getBranches()
            } catch (e: Exception) {
                // Ignore
            }
        }
        fetchUsers()
    }

    fun fetchUsers(page: Int = 1, limit: Int = 20, showLoading: Boolean = true) {
        viewModelScope.launch {
            if (showLoading) _uiState.value = AdminUserUiState.Loading
            try {
                val response = adminUserRepository.getAdminUsers(page, limit)
                _paginationInfo.value = response
                // If it's page 1, replace. Otherwise, append (for infinite scroll). For simplicity, just replace.
                _users.value = response.data
                if (showLoading) _uiState.value = AdminUserUiState.Idle
            } catch (e: Exception) {
                android.util.Log.e("AdminUserViewModel", "Lỗi lấy danh sách người dùng", e)
                if (e is retrofit2.HttpException) {
                    val errorBody = e.response()?.errorBody()?.string()
                    android.util.Log.e("AdminUserViewModel", "HTTP Error body: $errorBody")
                }
                _uiState.value = AdminUserUiState.Error(e.message ?: "Lỗi khi lấy danh sách người dùng")
            }
        }
    }

    fun toggleActive(id: String, isActive: Boolean) {
        viewModelScope.launch {
            _uiState.value = AdminUserUiState.Loading
            try {
                if (isActive) {
                    adminUserRepository.banUser(id)
                } else {
                    adminUserRepository.unbanUser(id)
                }
                val actionMsg = if (isActive) "Khóa" else "Mở khóa"
                
                // Cập nhật danh sách local để giao diện đổi ngay lập tức
                val updatedUsers = _users.value.map {
                    if (it.id == id) it.copy(isActive = !isActive) else it
                }
                _users.value = updatedUsers

                _uiState.value = AdminUserUiState.Success("$actionMsg tài khoản thành công")
                fetchUsers(showLoading = false) // Cập nhật lại danh sách sau khi đổi trạng thái (chạy ngầm)
            } catch (e: Exception) {
                _uiState.value = AdminUserUiState.Error("Lỗi: ${e.message}")
            }
        }
    }

    fun createEmployee(dto: CreateEmployeeDto) {
        viewModelScope.launch {
            _uiState.value = AdminUserUiState.Loading
            try {
                adminUserRepository.createEmployee(dto)
                _uiState.value = AdminUserUiState.Success("Tạo nhân viên thành công")
                fetchUsers(showLoading = false)
            } catch (e: Exception) {
                _uiState.value = AdminUserUiState.Error(e.message ?: "Lỗi khi tạo nhân viên")
            }
        }
    }

    fun resetUiState() {
        _uiState.value = AdminUserUiState.Idle
    }
}
