package com.tanh.datsan.ui.admin.booking

sealed class AdminCreateBookingUiState {
    object Idle : AdminCreateBookingUiState()
    object Loading : AdminCreateBookingUiState()
    data class Success(val message: String) : AdminCreateBookingUiState()
    data class Error(val message: String) : AdminCreateBookingUiState()
}