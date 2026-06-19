package com.tanh.datsan.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tanh.datsan.data.model.BookingPaginatedResponseDto
import com.tanh.datsan.data.model.BookingResponse
import com.tanh.datsan.data.repository.BookingRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed class BookingHistoryUiState {
    object Loading : BookingHistoryUiState()
    data class Success(val message: String? = null) : BookingHistoryUiState()
    data class Error(val message: String) : BookingHistoryUiState()
    object Idle : BookingHistoryUiState()
}

@HiltViewModel
class BookingHistoryViewModel @Inject constructor(
    private val bookingRepository: BookingRepository
) : ViewModel() {

    private val _bookings = MutableStateFlow<List<BookingResponse>>(emptyList())
    val bookings: StateFlow<List<BookingResponse>> = _bookings.asStateFlow()

    private val _paginationInfo = MutableStateFlow<BookingPaginatedResponseDto?>(null)
    val paginationInfo: StateFlow<BookingPaginatedResponseDto?> = _paginationInfo.asStateFlow()

    private val _uiState = MutableStateFlow<BookingHistoryUiState>(BookingHistoryUiState.Idle)
    val uiState: StateFlow<BookingHistoryUiState> = _uiState.asStateFlow()

    private val _currentStatus = MutableStateFlow<String?>(null)
    val currentStatus: StateFlow<String?> = _currentStatus.asStateFlow()

    fun fetchMyBookings(status: String? = null, page: Int = 1, limit: Int = 20) {
        viewModelScope.launch {
            _uiState.value = BookingHistoryUiState.Loading
            _currentStatus.value = status
            try {
                val response = bookingRepository.getMyBookings(status, page, limit)
                _paginationInfo.value = response
                // Replace list for simplicity on tab change or first page
                if (page == 1) {
                    _bookings.value = response.data
                } else {
                    _bookings.value = _bookings.value + response.data
                }
                _uiState.value = BookingHistoryUiState.Idle
            } catch (e: Exception) {
                _uiState.value = BookingHistoryUiState.Error(e.message ?: "Lỗi khi tải lịch sử đặt sân")
            }
        }
    }

    fun resetUiState() {
        _uiState.value = BookingHistoryUiState.Idle
    }
}
