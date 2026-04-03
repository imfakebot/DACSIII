package com.tanh.datsan.viewmodel

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import androidx.compose.runtime.State
import com.tanh.datsan.data.model.CreateBookingDto
import com.tanh.datsan.data.repository.FieldRepository
import kotlinx.coroutines.launch

class DetailViewModel : ViewModel() {

    private val repository = FieldRepository()

    // 1. Kho chứa trạng thái UI (Mặc định là đang Loading)
    private val _uiState = MutableStateFlow<DetailUiState>(DetailUiState.Loading)
    val uiState: StateFlow<DetailUiState> = _uiState.asStateFlow()

    fun fetchFieldDetail(fieldId: String) {
        viewModelScope.launch {
            _uiState.value = DetailUiState.Loading
            try {
                // Gọi API lấy chi tiết 1 sân theo ID
                val response = repository.getFieldDetail(fieldId)
                _uiState.value = DetailUiState.Success(response)
            } catch (e: Exception) {
                _uiState.value = DetailUiState.Error(e.message ?: "Lỗi không xác định")
            }
        }
    }

    private val _bookingState = mutableStateOf<BookingUiState>(BookingUiState.Idle)
    val bookingState: State<BookingUiState> = _bookingState

    fun createBooking(fieldId: String, startTime: String, durationMinutes: Int) {
        viewModelScope.launch {
            _bookingState.value = BookingUiState.Loading
            try {
                // Gọi API POST /bookings với DTO khớp Swagger
                val response = repository.createBooking(
                    CreateBookingDto(
                        fieldId = fieldId,
                        startTime = startTime,
                        durationMinutes = durationMinutes,
                        voucherCode = null // Sếp có thể thêm logic nhập mã sau
                    )
                )

                if (response.paymentUrl != null) {
                    _bookingState.value = BookingUiState.Success(response.paymentUrl)
                } else {
                    _bookingState.value = BookingUiState.Error("Không lấy được link thanh toán")
                }
            } catch (e: Exception) {
                _bookingState.value = BookingUiState.Error(e.message ?: "Lỗi kết nối")
            }
        }
    }

    fun resetBookingState() {
        _bookingState.value = BookingUiState.Idle
    }
}

sealed class BookingUiState {
    object Idle : BookingUiState()
    object Loading : BookingUiState()
    data class Success(val paymentUrl: String) : BookingUiState()
    data class Error(val message: String) : BookingUiState()
}

