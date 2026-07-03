package com.tanh.datsan.ui.admin.review

sealed class AdminReviewUiState {
    object Idle : AdminReviewUiState()
    object Loading : AdminReviewUiState()
    data class Success(val message: String = "") : AdminReviewUiState()
    data class Error(val message: String) : AdminReviewUiState()
}