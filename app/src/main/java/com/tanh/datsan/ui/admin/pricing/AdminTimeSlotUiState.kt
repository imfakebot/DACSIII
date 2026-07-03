package com.tanh.datsan.ui.admin.pricing

sealed class AdminTimeSlotUiState {
    object Idle : AdminTimeSlotUiState()
    object Loading : AdminTimeSlotUiState()
    data class Success(val message: String) : AdminTimeSlotUiState()
    data class Error(val message: String) : AdminTimeSlotUiState()
}