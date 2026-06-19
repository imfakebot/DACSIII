package com.tanh.datsan.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tanh.datsan.data.model.BookingResponse
import com.tanh.datsan.data.repository.BookingRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed class AdminBookingUiState {
    object Loading : AdminBookingUiState()
    data class Success(val message: String? = null) : AdminBookingUiState()
    data class Error(val message: String) : AdminBookingUiState()
    object Idle : AdminBookingUiState()
}

@HiltViewModel
class AdminBookingViewModel @Inject constructor(
    private val bookingRepository: BookingRepository
) : ViewModel() {

    private val _bookings = MutableStateFlow<List<BookingResponse>>(emptyList())
    val bookings: StateFlow<List<BookingResponse>> = _bookings.asStateFlow()

    private val _uiState = MutableStateFlow<AdminBookingUiState>(AdminBookingUiState.Idle)
    val uiState: StateFlow<AdminBookingUiState> = _uiState.asStateFlow()

    // Pagination State
    private var currentPage = 1
    private val limit = 10
    var isLastPage = false
        private set

    private val _isLoadingMore = MutableStateFlow(false)
    val isLoadingMore: StateFlow<Boolean> = _isLoadingMore.asStateFlow()

    private var currentBranchId: String? = null
    private var currentStatus: String? = null

    fun fetchAdminBookings(branchId: String? = null, status: String? = null, refresh: Boolean = false) {
        if (refresh) {
            currentPage = 1
            isLastPage = false
            _bookings.value = emptyList()
            _uiState.value = AdminBookingUiState.Loading
        } else {
            _uiState.value = AdminBookingUiState.Loading
        }

        currentBranchId = branchId
        currentStatus = status

        viewModelScope.launch {
            try {
                val response = bookingRepository.getAdminBookings(
                    branchId = currentBranchId,
                    status = currentStatus,
                    page = currentPage,
                    limit = limit
                )
                
                if (refresh) {
                    _bookings.value = response.data
                } else {
                    _bookings.value = _bookings.value + response.data
                }
                
                isLastPage = currentPage >= (response.meta?.lastPage ?: 1)
                _uiState.value = AdminBookingUiState.Idle
            } catch (e: Exception) {
                _uiState.value = AdminBookingUiState.Error(e.message ?: "Lỗi tải danh sách Booking")
            }
        }
    }

    fun loadMoreBookings() {
        if (isLastPage || _isLoadingMore.value || _uiState.value is AdminBookingUiState.Loading) return

        _isLoadingMore.value = true
        currentPage++

        viewModelScope.launch {
            try {
                val response = bookingRepository.getAdminBookings(
                    branchId = currentBranchId,
                    status = currentStatus,
                    page = currentPage,
                    limit = limit
                )
                _bookings.value = _bookings.value + response.data
                isLastPage = currentPage >= (response.meta?.lastPage ?: 1)
            } catch (e: Exception) {
                // Giữ nguyên trang nếu lỗi
                currentPage--
            } finally {
                _isLoadingMore.value = false
            }
        }
    }

    fun resetUiState() {
        _uiState.value = AdminBookingUiState.Idle
    }
}
