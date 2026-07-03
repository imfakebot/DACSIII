package com.tanh.datsan.ui.staff

import com.tanh.datsan.data.model.BookingResponse

sealed interface CheckInUiState {
    object Idle : CheckInUiState
    object Loading : CheckInUiState
    data class Success(val booking: BookingResponse) : CheckInUiState
    data class Error(val message: String) : CheckInUiState
}