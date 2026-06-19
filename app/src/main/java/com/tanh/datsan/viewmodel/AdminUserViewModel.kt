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

    fun fetchUsers(page: Int = 1, limit: Int = 20) {
        viewModelScope.launch {
            _uiState.value = AdminUserUiState.Loading
            try {
                val response = adminUserRepository.getAdminUsers(page, limit)
                _paginationInfo.value = response
                // If it's page 1, replace. Otherwise, append (for infinite scroll). For simplicity, just replace.
                _users.value = response.data
                _uiState.value = AdminUserUiState.Idle
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

    fun banUser(id: String) {
        viewModelScope.launch {
            _uiState.value = AdminUserUiState.Loading
            try {
                adminUserRepository.banUser(id)
                _uiState.value = AdminUserUiState.Success("Khóa tài khoản thành công")
                // Refresh list
                fetchUsers(_paginationInfo.value?.page ?: 1)
            } catch (e: Exception) {
                _uiState.value = AdminUserUiState.Error(e.message ?: "Lỗi khi khóa tài khoản")
            }
        }
    }

    fun unbanUser(id: String) {
        viewModelScope.launch {
            _uiState.value = AdminUserUiState.Loading
            try {
                adminUserRepository.unbanUser(id)
                _uiState.value = AdminUserUiState.Success("Mở khóa tài khoản thành công")
                // Refresh list
                fetchUsers(_paginationInfo.value?.page ?: 1)
            } catch (e: Exception) {
                _uiState.value = AdminUserUiState.Error(e.message ?: "Lỗi khi mở khóa tài khoản")
            }
        }
    }

    fun createEmployee(dto: CreateEmployeeDto) {
        viewModelScope.launch {
            _uiState.value = AdminUserUiState.Loading
            try {
                adminUserRepository.createEmployee(dto)
                _uiState.value = AdminUserUiState.Success("Tạo nhân viên thành công")
                fetchUsers()
            } catch (e: Exception) {
                _uiState.value = AdminUserUiState.Error(e.message ?: "Lỗi khi tạo nhân viên")
            }
        }
    }

    fun resetUiState() {
        _uiState.value = AdminUserUiState.Idle
    }
}
