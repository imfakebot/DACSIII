package com.tanh.datsan.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tanh.datsan.data.model.UserAdminDto
import com.tanh.datsan.data.repository.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.math.ceil

data class AdminUserUiState(
    val isLoading: Boolean = false,
    val allUsers: List<UserAdminDto> = emptyList(),      // Full list from API
    val filteredUsers: List<UserAdminDto> = emptyList(), // After search + role filter
    val displayedUsers: List<UserAdminDto> = emptyList(),// Current page slice
    val errorMessage: String? = null,
    val currentPage: Int = 1,
    val totalPages: Int = 1,
    val totalFiltered: Int = 0,
    val searchQuery: String = "",
    val selectedRole: String? = null,
    val isActionLoading: Boolean = false
)

private const val PAGE_SIZE = 10

@HiltViewModel
class AdminUserViewModel @Inject constructor(
    private val userRepository: UserRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(AdminUserUiState())
    val uiState: StateFlow<AdminUserUiState> = _uiState.asStateFlow()

    private var searchJob: Job? = null

    init {
        fetchAllUsers()
    }

    /** Fetch everything once with limit=9999, then process locally */
    fun fetchAllUsers() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            try {
                val response = userRepository.getAllUsers(page = 1, limit = 9999)
                if (response.isSuccessful) {
                    val data = response.body()?.data ?: emptyList()
                    _uiState.update { it.copy(isLoading = false, allUsers = data) }
                    applyFiltersAndPagination(page = 1)
                } else {
                    _uiState.update { it.copy(isLoading = false, errorMessage = "Lỗi tải dữ liệu: ${response.message()}") }
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, errorMessage = "Lỗi kết nối: ${e.message}") }
            }
        }
    }

    private fun applyFiltersAndPagination(page: Int = _uiState.value.currentPage) {
        val state = _uiState.value
        val query = state.searchQuery.trim().lowercase()
        val role = state.selectedRole

        // 1. Filter
        val filtered = state.allUsers.filter { user ->
            val matchSearch = if (query.isEmpty()) true else
                user.email.lowercase().contains(query) ||
                user.userProfile?.fullName?.lowercase()?.contains(query) == true ||
                user.userProfile?.phoneNumber?.lowercase()?.contains(query) == true

            val matchRole = if (role == null) true else
                user.role?.name?.equals(role, ignoreCase = true) == true

            matchSearch && matchRole
        }

        // 2. Paginate
        val safePage = page.coerceIn(1, maxOf(1, ceil(filtered.size.toDouble() / PAGE_SIZE).toInt()))
        val totalPages = maxOf(1, ceil(filtered.size.toDouble() / PAGE_SIZE).toInt())
        val from = (safePage - 1) * PAGE_SIZE
        val to = minOf(from + PAGE_SIZE, filtered.size)
        val displayed = if (from < filtered.size) filtered.subList(from, to) else emptyList()

        _uiState.update {
            it.copy(
                filteredUsers = filtered,
                displayedUsers = displayed,
                currentPage = safePage,
                totalPages = totalPages,
                totalFiltered = filtered.size
            )
        }
    }

    fun goToPage(page: Int) = applyFiltersAndPagination(page)

    fun onSearchQueryChanged(query: String) {
        _uiState.update { it.copy(searchQuery = query) }
        searchJob?.cancel()
        searchJob = viewModelScope.launch {
            delay(300)
            applyFiltersAndPagination(page = 1)
        }
    }

    fun onRoleFilterChanged(role: String?) {
        _uiState.update { it.copy(selectedRole = role) }
        applyFiltersAndPagination(page = 1)
    }

    fun banUser(user: UserAdminDto) {
        viewModelScope.launch {
            _uiState.update { it.copy(isActionLoading = true, errorMessage = null) }
            try {
                val response = userRepository.banUser(user.id)
                if (response.isSuccessful) {
                    val updatedAllUsers = _uiState.value.allUsers.map { 
                        if (it.id == user.id) it.copy(status = "suspended") else it 
                    }
                    _uiState.update { it.copy(isActionLoading = false, allUsers = updatedAllUsers) }
                    applyFiltersAndPagination()
                } else {
                    _uiState.update { it.copy(isActionLoading = false, errorMessage = "Không thể khóa tài khoản") }
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(isActionLoading = false, errorMessage = "Lỗi kết nối: ${e.message}") }
            }
        }
    }

    fun unbanUser(user: UserAdminDto) {
        viewModelScope.launch {
            _uiState.update { it.copy(isActionLoading = true, errorMessage = null) }
            try {
                val response = userRepository.unbanUser(user.id)
                if (response.isSuccessful) {
                    val updatedAllUsers = _uiState.value.allUsers.map { 
                        if (it.id == user.id) it.copy(status = "active") else it 
                    }
                    _uiState.update { it.copy(isActionLoading = false, allUsers = updatedAllUsers) }
                    applyFiltersAndPagination()
                } else {
                    _uiState.update { it.copy(isActionLoading = false, errorMessage = "Không thể mở khóa tài khoản") }
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(isActionLoading = false, errorMessage = "Lỗi kết nối: ${e.message}") }
            }
        }
    }

    fun clearError() = _uiState.update { it.copy(errorMessage = null) }
}
