package com.tanh.datsan.ui.home.review

sealed class ReviewUiState {
    object Idle : ReviewUiState()
    object Loading : ReviewUiState()
    data class Success(val message: String = "") : ReviewUiState()
    data class Error(val message: String) : ReviewUiState()
}
