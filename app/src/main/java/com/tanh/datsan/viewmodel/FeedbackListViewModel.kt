package com.tanh.datsan.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tanh.datsan.data.model.FeedbackResponse
import com.tanh.datsan.data.repository.FeedbackRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class FeedbackListUiState(
    val feedbacks: List<FeedbackResponse> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
)

@HiltViewModel
class FeedbackListViewModel @Inject constructor(
    private val feedbackRepository: FeedbackRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(FeedbackListUiState())
    val uiState: StateFlow<FeedbackListUiState> = _uiState.asStateFlow()

    fun fetchMyFeedbacks() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            try {
                val response = feedbackRepository.getMyFeedbacks()
                if (response.isSuccessful) {
                    _uiState.value = _uiState.value.copy(
                        feedbacks = response.body() ?: emptyList(),
                        isLoading = false
                    )
                } else {
                    _uiState.value = _uiState.value.copy(
                        error = "Không thể tải danh sách hỗ trợ (Code: ${response.code()})",
                        isLoading = false
                    )
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    error = "Lỗi kết nối: ${e.message}",
                    isLoading = false
                )
            }
        }
    }

    fun createFeedback(title: String, type: String, content: String, images: List<String>?, onSuccess: (String) -> Unit) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            try {
                val response = feedbackRepository.createFeedback(title, type, content, images)
                if (response.isSuccessful && response.body() != null) {
                    _uiState.value = _uiState.value.copy(isLoading = false)
                    onSuccess(response.body()!!.id)
                } else {
                    val errorBodyString = response.errorBody()?.string() ?: ""
                    _uiState.value = _uiState.value.copy(
                        error = "Không thể tạo yêu cầu hỗ trợ (Code: ${response.code()}) - $errorBodyString",
                        isLoading = false
                    )
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    error = "Lỗi kết nối: ${e.message}",
                    isLoading = false
                )
            }
        }
    }
}
