package com.tanh.datsan.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tanh.datsan.data.model.FeedbackResponse
import com.tanh.datsan.data.repository.FeedbackRepository
import com.tanh.datsan.utils.ResponseHelper.parseError
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class AdminFeedbackUiState(
    val feedbacks: List<FeedbackResponse> = emptyList(),
    val currentFeedback: FeedbackResponse? = null,
    val isLoading: Boolean = false,
    val error: String? = null,
    val toastMessage: String? = null,
    
    // Pagination and Filter
    val currentPage: Int = 1,
    val totalRecords: Int = 0,
    val currentStatusFilter: String? = null, // null for all
    val currentTypeFilter: String? = null    // null for all
)

@HiltViewModel
class AdminFeedbackViewModel @Inject constructor(
    private val repository: FeedbackRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(AdminFeedbackUiState())
    val uiState: StateFlow<AdminFeedbackUiState> = _uiState.asStateFlow()

    fun fetchAllFeedbacks(page: Int = 1) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            try {
                val state = _uiState.value
                val response = repository.getAllFeedbacks(
                    page = page,
                    limit = 20,
                    status = state.currentStatusFilter,
                    type = state.currentTypeFilter
                )
                if (response.isSuccessful && response.body() != null) {
                    val paginateData = response.body()!!
                    _uiState.update {
                        it.copy(
                            feedbacks = paginateData.data,
                            currentPage = paginateData.page,
                            totalRecords = paginateData.total,
                            isLoading = false
                        )
                    }
                } else {
                    val msg = parseError(response.errorBody()?.string())
                    _uiState.update { it.copy(error = msg, isLoading = false) }
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(error = "Lỗi kết nối: ${e.message}", isLoading = false) }
            }
        }
    }

    fun setFilter(status: String?, type: String?) {
        _uiState.update {
            it.copy(currentStatusFilter = status, currentTypeFilter = type)
        }
        fetchAllFeedbacks(1)
    }

    fun fetchFeedbackDetail(id: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null, currentFeedback = null) }
            try {
                val response = repository.getAdminFeedbackDetail(id)
                if (response.isSuccessful && response.body() != null) {
                    _uiState.update { it.copy(currentFeedback = response.body()!!, isLoading = false) }
                } else {
                    val msg = parseError(response.errorBody()?.string())
                    _uiState.update { it.copy(error = msg, isLoading = false) }
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(error = "Lỗi kết nối: ${e.message}", isLoading = false) }
            }
        }
    }

    fun updateStatus(id: String, newStatus: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            try {
                val response = repository.updateFeedbackStatus(id, newStatus)
                if (response.isSuccessful) {
                    _uiState.update { it.copy(toastMessage = "Cập nhật trạng thái thành công") }
                    fetchFeedbackDetail(id) // refresh detail
                    fetchAllFeedbacks(_uiState.value.currentPage) // refresh list
                } else {
                    val msg = parseError(response.errorBody()?.string())
                    _uiState.update { it.copy(toastMessage = "Lỗi: $msg", isLoading = false) }
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(toastMessage = "Lỗi kết nối: ${e.message}", isLoading = false) }
            }
        }
    }

    fun replyFeedback(id: String, reply: String) {
        if (reply.isBlank()) return
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            try {
                val response = repository.replyFeedback(id, reply)
                if (response.isSuccessful) {
                    _uiState.update { it.copy(toastMessage = "Gửi phản hồi thành công") }
                    fetchFeedbackDetail(id)
                } else {
                    val msg = parseError(response.errorBody()?.string())
                    _uiState.update { it.copy(toastMessage = "Lỗi: $msg", isLoading = false) }
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(toastMessage = "Lỗi kết nối: ${e.message}", isLoading = false) }
            }
        }
    }

    fun deleteFeedback(id: String, onSuccess: () -> Unit) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            try {
                val response = repository.deleteFeedback(id)
                if (response.isSuccessful) {
                    _uiState.update { it.copy(toastMessage = "Đã xóa Feedback", isLoading = false) }
                    fetchAllFeedbacks(_uiState.value.currentPage)
                    onSuccess()
                } else {
                    val msg = parseError(response.errorBody()?.string())
                    _uiState.update { it.copy(toastMessage = "Lỗi: $msg", isLoading = false) }
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(toastMessage = "Lỗi kết nối: ${e.message}", isLoading = false) }
            }
        }
    }

    fun clearToast() {
        _uiState.update { it.copy(toastMessage = null) }
    }
}
