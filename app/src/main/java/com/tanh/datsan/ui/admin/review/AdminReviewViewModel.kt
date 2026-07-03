package com.tanh.datsan.ui.admin.review

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tanh.datsan.data.model.Review
import com.tanh.datsan.data.model.ReviewMeta
import com.tanh.datsan.data.repository.ReviewRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject



@HiltViewModel
class AdminReviewViewModel @Inject constructor(
    private val repository: ReviewRepository
) : ViewModel() {

    private val _reviews = MutableStateFlow<List<Review>>(emptyList())
    val reviews: StateFlow<List<Review>> = _reviews.asStateFlow()

    private val _meta = MutableStateFlow<ReviewMeta?>(null)
    val meta: StateFlow<ReviewMeta?> = _meta.asStateFlow()

    private val _uiState = MutableStateFlow<AdminReviewUiState>(AdminReviewUiState.Idle)
    val uiState: StateFlow<AdminReviewUiState> = _uiState.asStateFlow()

    // Active filters
    private val _filterBranchId = MutableStateFlow<String?>(null)
    val filterBranchId: StateFlow<String?> = _filterBranchId.asStateFlow()

    private val _filterRating = MutableStateFlow<Int?>(null)
    val filterRating: StateFlow<Int?> = _filterRating.asStateFlow()

    // ── Fetch ─────────────────────────────────────────────────────────────────
    fun fetchReviews(
        branchId: String? = _filterBranchId.value,
        rating: Int? = _filterRating.value,
        page: Int = 1,
        limit: Int = 20
    ) {
        viewModelScope.launch {
            _uiState.value = AdminReviewUiState.Loading
            _filterBranchId.value = branchId
            _filterRating.value = rating
            try {
                val response = repository.getAdminReviews(branchId, rating, page, limit)
                _reviews.value = response.data
                _meta.value = response.meta
                _uiState.value = AdminReviewUiState.Idle
            } catch (e: Exception) {
                Log.e("AdminReviewVM", "fetchReviews error: ${e.message}")
                _uiState.value = AdminReviewUiState.Error(e.message ?: "Không thể tải đánh giá")
            }
        }
    }

    // ── Delete ────────────────────────────────────────────────────────────────
    fun deleteReview(reviewId: String) {
        viewModelScope.launch {
            _uiState.value = AdminReviewUiState.Loading
            try {
                val response = repository.deleteReview(reviewId)
                if (response.isSuccessful) {
                    // Remove locally for instant UI feedback
                    _reviews.value = _reviews.value.filter { it.id != reviewId }
                    _uiState.value = AdminReviewUiState.Success("Đã xóa đánh giá thành công")
                } else {
                    _uiState.value = AdminReviewUiState.Error("Xóa thất bại: ${response.code()}")
                }
            } catch (e: Exception) {
                Log.e("AdminReviewVM", "deleteReview error: ${e.message}")
                _uiState.value = AdminReviewUiState.Error(e.message ?: "Lỗi khi xóa")
            }
        }
    }

    // ── Reply ─────────────────────────────────────────────────────────────────
    fun replyReview(reviewId: String, replyText: String) {
        viewModelScope.launch {
            _uiState.value = AdminReviewUiState.Loading
            try {
                val updated = repository.replyReview(reviewId, replyText)
                // Update the item in-place
                _reviews.value = _reviews.value.map {
                    if (it.id == reviewId) updated else it
                }
                _uiState.value = AdminReviewUiState.Success("Đã gửi phản hồi")
            } catch (e: Exception) {
                Log.e("AdminReviewVM", "replyReview error: ${e.message}")
                _uiState.value = AdminReviewUiState.Error(e.message ?: "Lỗi khi phản hồi")
            }
        }
    }

    fun resetUiState() {
        _uiState.value = AdminReviewUiState.Idle
    }
}
