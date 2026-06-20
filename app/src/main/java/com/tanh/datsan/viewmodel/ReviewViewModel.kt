package com.tanh.datsan.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tanh.datsan.data.model.CreateReviewDto
import com.tanh.datsan.data.model.Review
import com.tanh.datsan.data.model.ReviewMeta
import com.tanh.datsan.data.repository.ReviewRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

// ── UiState ──────────────────────────────────────────────────────────────────
sealed class ReviewUiState {
    object Idle : ReviewUiState()
    object Loading : ReviewUiState()
    data class Success(val message: String = "") : ReviewUiState()
    data class Error(val message: String) : ReviewUiState()
}

@HiltViewModel
class ReviewViewModel @Inject constructor(
    private val repository: ReviewRepository
) : ViewModel() {

    // ── Field reviews (AllReviewScreen) ──────────────────────────────────────
    private val _reviews = MutableStateFlow<List<Review>>(emptyList())
    val reviews: StateFlow<List<Review>> = _reviews.asStateFlow()

    private val _reviewMeta = MutableStateFlow<ReviewMeta?>(null)
    val reviewMeta: StateFlow<ReviewMeta?> = _reviewMeta.asStateFlow()

    // ── My reviews ───────────────────────────────────────────────────────────
    private val _myReviews = MutableStateFlow<List<Review>>(emptyList())
    val myReviews: StateFlow<List<Review>> = _myReviews.asStateFlow()

    // ── Shared state ─────────────────────────────────────────────────────────
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _uiState = MutableStateFlow<ReviewUiState>(ReviewUiState.Idle)
    val uiState: StateFlow<ReviewUiState> = _uiState.asStateFlow()

    // Kept for backward-compat with existing AllReviewScreen usage
    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    // ── Fetch field reviews ───────────────────────────────────────────────────
    fun fetchReview(fieldId: String, page: Int = 1, limit: Int = 20) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val response = repository.getFieldReview(fieldId, page, limit)
                _reviews.value = response.data
                _reviewMeta.value = response.meta
            } catch (e: Exception) {
                Log.e("ReviewViewModel", "fetchReview error: ${e.message}")
                _errorMessage.value = e.message
            } finally {
                _isLoading.value = false
            }
        }
    }

    // ── Fetch my reviews ──────────────────────────────────────────────────────
    fun fetchMyReviews(page: Int = 1, limit: Int = 20) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val response = repository.getMyReviews(page, limit)
                _myReviews.value = response.data
            } catch (e: Exception) {
                Log.e("ReviewViewModel", "fetchMyReviews error: ${e.message}")
                _uiState.value = ReviewUiState.Error(e.message ?: "Không thể tải đánh giá")
            } finally {
                _isLoading.value = false
            }
        }
    }

    // ── Create review ─────────────────────────────────────────────────────────
    fun createReview(
        bookingId: String,
        rating: Int,
        comment: String?
    ) {
        viewModelScope.launch {
            _uiState.value = ReviewUiState.Loading
            try {
                repository.createReview(
                    CreateReviewDto(
                        bookingId = bookingId,
                        rating = rating,
                        comment = comment?.trim()?.ifBlank { null }
                    )
                )
                _uiState.value = ReviewUiState.Success("Đánh giá của bạn đã được gửi!")
            } catch (e: Exception) {
                Log.e("ReviewViewModel", "createReview error: ${e.message}")
                val msg = when {
                    e.message?.contains("403") == true || e.message?.contains("400") == true ->
                        "Bạn chỉ có thể đánh giá đơn đặt sân đã hoàn thành."
                    else -> e.message ?: "Không thể gửi đánh giá"
                }
                _uiState.value = ReviewUiState.Error(msg)
            }
        }
    }

    fun clearError() {
        _errorMessage.value = null
    }

    fun resetUiState() {
        _uiState.value = ReviewUiState.Idle
    }
}