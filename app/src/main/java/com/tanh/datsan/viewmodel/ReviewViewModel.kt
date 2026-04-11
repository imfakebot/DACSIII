package com.tanh.datsan.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tanh.datsan.data.model.Review
import com.tanh.datsan.data.repository.ReviewRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class ReviewViewModel : ViewModel() {
    private val repository = ReviewRepository()

    private val _reviews = MutableStateFlow<List<Review>>(emptyList())
    val reviews: StateFlow<List<Review>> = _reviews

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage

    fun fetchReview(fieldId: String) {
        viewModelScope.launch {
            try {
                // có thể thêm tham số phân trang: repository.getFieldReviews(fieldId, page = 1)
                //Todo
                _reviews.value = repository.getFieldReview(fieldId)
            } catch (e: Exception) {
                _errorMessage.value = "Lỗi tải bình luận: ${e.localizedMessage}"
            }
        }
    }

    fun clearError() { _errorMessage.value = null }
}