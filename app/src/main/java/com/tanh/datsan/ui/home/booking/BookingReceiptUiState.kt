package com.tanh.datsan.ui.home.booking

import com.tanh.datsan.data.model.BookingResponse

sealed interface BookingReceiptUiState {
    object Loading : BookingReceiptUiState
    data class Success(val booking: BookingResponse) : BookingReceiptUiState
    data class Error(val message: String) : BookingReceiptUiState
}