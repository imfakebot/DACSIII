package com.tanh.datsan.ui.admin.user

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tanh.datsan.data.model.AccountPaginatedResponseDto
import com.tanh.datsan.data.model.AccountResponseDto
import com.tanh.datsan.data.model.Branch
import com.tanh.datsan.data.model.CreateEmployeeDto
import com.tanh.datsan.data.repository.AdminUserRepository
import com.tanh.datsan.data.repository.BranchRepository
import com.tanh.datsan.ui.state.ActionState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

import com.tanh.datsan.ui.state.AdminUserState

@HiltViewModel
class AdminUserViewModel @Inject constructor(
    private val adminUserRepository: AdminUserRepository,
    private val branchRepository: BranchRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(AdminUserState())
    val uiState: StateFlow<AdminUserState> = _uiState.asStateFlow()

    fun updateSearchQuery(query: String) {
        _uiState.update { it.copy(searchQuery = query) }
    }

    fun fetchUsers(page: Int = 1, limit: Int = 20, search: String? = _uiState.value.searchQuery.takeIf { it.isNotBlank() }, showLoading: Boolean = true) {
        viewModelScope.launch {
            if (showLoading) _uiState.update { it.copy(actionState = ActionState.Loading) }
            try {
                val response = adminUserRepository.getAdminUsers(page, limit, search)
                _uiState.update { 
                    it.copy(
                        paginationInfo = response,
                        users = response.data,
                        actionState = if (showLoading) ActionState.Idle else it.actionState
                    )
                }
            } catch (e: Exception) {
                android.util.Log.e("AdminUserViewModel", "Lỗi lấy danh sách người dùng", e)
                if (e is retrofit2.HttpException) {
                    val errorBody = e.response()?.errorBody()?.string()
                    android.util.Log.e("AdminUserViewModel", "HTTP Error body: $errorBody")
                }
                _uiState.update { it.copy(actionState = ActionState.Error(e.message ?: "Lỗi khi lấy danh sách người dùng")) }
            }
        }
    }

    fun toggleActive(id: String, isActive: Boolean) {
        viewModelScope.launch {
            _uiState.update { it.copy(actionState = ActionState.Loading) }
            try {
                if (isActive) {
                    adminUserRepository.banUser(id)
                } else {
                    adminUserRepository.unbanUser(id)
                }
                val actionMsg = if (isActive) "Khóa" else "Mở khóa"
                
                // Cập nhật danh sách local để giao diện đổi ngay lập tức
                val updatedUsers = _uiState.value.users.map {
                    if (it.id == id) it.copy(isActive = !isActive) else it
                }

                _uiState.update { 
                    it.copy(
                        users = updatedUsers,
                        actionState = ActionState.Success("$actionMsg tài khoản thành công")
                    )
                }
                fetchUsers(showLoading = false) // Cập nhật lại danh sách sau khi đổi trạng thái (chạy ngầm)
            } catch (e: Exception) {
                _uiState.update { it.copy(actionState = ActionState.Error("Lỗi: ${e.message}")) }
            }
        }
    }

    fun createEmployee(dto: CreateEmployeeDto) {
        viewModelScope.launch {
            _uiState.update { it.copy(actionState = ActionState.Loading) }
            try {
                adminUserRepository.createEmployee(dto)
                _uiState.update { it.copy(actionState = ActionState.Success("Tạo nhân viên thành công")) }
                fetchUsers(showLoading = false)
            } catch (e: Exception) {
                _uiState.update { it.copy(actionState = ActionState.Error(e.message ?: "Lỗi khi tạo nhân viên")) }
            }
        }
    }

    fun resetActionState() {
        _uiState.update { it.copy(actionState = ActionState.Idle) }
    }
}
