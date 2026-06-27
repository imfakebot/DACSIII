package com.tanh.datsan.ui.state

sealed class BookingHistoryUiState {
    object Loading : BookingHistoryUiState()
    data class Success(val message: String? = null) : BookingHistoryUiState()
    data class Error(val message: String) : BookingHistoryUiState()
    object Idle : BookingHistoryUiState()
}
