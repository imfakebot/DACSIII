package com.tanh.datsan.ui.admin.booking

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tanh.datsan.data.model.BookingResponse
import com.tanh.datsan.data.repository.BookingRepository
import com.tanh.datsan.ui.admin.field.AdminUiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.collections.emptyList


@HiltViewModel
class AdminBookingViewModel @Inject constructor(
    private val bookingRepository: BookingRepository
) : ViewModel() {

    private val _bookings = MutableStateFlow<List<BookingResponse>>(emptyList())
    val bookings: StateFlow<List<BookingResponse>> = _bookings.asStateFlow()

    private val _uiState = MutableStateFlow<AdminUiState>(AdminUiState.Idle)
    val uiState: StateFlow<AdminUiState> = _uiState.asStateFlow()
    private var currentPage = 1
    private val limit = 10
    var isLastPage = false
        private set

    private val _isLoadingMore = MutableStateFlow(false)
    val isLoadingMore: StateFlow<Boolean> = _isLoadingMore.asStateFlow()

    private var currentBranchId: String? = null
    private var currentStatus: String? = null

    fun fetchAdminBookings(
        branchId: String? = null,
        status: String? = null,
        refresh: Boolean = false
    ) {
        if (refresh) {
            currentPage = 1
            isLastPage = false
            _bookings.value = emptyList()
            _uiState.value = AdminUiState.Loading
        } else {
            _uiState.value = AdminUiState.Loading
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
                    _bookings.value += response.data
                }

                isLastPage = currentPage >= (response.meta?.lastPage ?: 1)
                _uiState.value = AdminUiState.Idle
            } catch (e: Exception) {
                _uiState.value = AdminUiState.Error(e.message ?: "Lỗi tải danh sách Booking")
            }
        }
    }

    fun loadMoreBookings() {
        if (isLastPage || _isLoadingMore.value || _uiState.value is AdminUiState.Loading) return

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
                _bookings.value += response.data
                isLastPage = currentPage >= (response.meta?.lastPage ?: 1)
            } catch (e: Exception) {
                Log.d("AdminBookingViewModel", "Load more error: ${e.message}")
                currentPage--
            } finally {
                _isLoadingMore.value = false
            }
        }
    }

    fun cancelBooking(bookingId: String) {
        viewModelScope.launch {
            _uiState.value = AdminUiState.Loading
            try {
                val response = bookingRepository.cancelBooking(bookingId)
                if (response.isSuccessful) {
                    _uiState.value = AdminUiState.Success(
                        response.body()?.message ?: "Hủy đơn thành công"
                    )
                    fetchAdminBookings(
                        branchId = currentBranchId,
                        status = currentStatus,
                        refresh = true
                    )
                } else {
                    _uiState.value =
                        AdminUiState.Error("Hủy đơn thất bại: ${response.code()}")
                }
            } catch (e: Exception) {
                _uiState.value = AdminUiState.Error("Lỗi kết nối: ${e.message}")
            }
        }
    }

    fun resetUiState() {
        _uiState.value = AdminUiState.Idle
    }
}
