package com.tanh.datsan.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tanh.datsan.data.model.Review
import com.tanh.datsan.data.repository.ReviewRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ReviewViewModel @Inject constructor(
    private val repository: ReviewRepository
) : ViewModel() {

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
                Log.d("ReviewViewModel", "Error fetching reviews: ${e.message}")
                _errorMessage.value = e.message
            }
        }
    }

    fun clearError() { _errorMessage.value = null }
}