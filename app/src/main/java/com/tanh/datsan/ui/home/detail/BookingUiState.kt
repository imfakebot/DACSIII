package com.tanh.datsan.ui.home.detail

sealed class BookingUiState {
    object Idle : BookingUiState()
    object Loading : BookingUiState()
    data class Success(val paymentUrl: String) : BookingUiState()
    data class Error(val message: String?) : BookingUiState()
}