package com.tanh.datsan.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tanh.datsan.core.TokenManager
import com.tanh.datsan.data.model.BookingResponse
import com.tanh.datsan.data.repository.BookingRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class HistoryUiState(
    val isLoading: Boolean = false,
    val isRefreshing: Boolean = false,
    val bookings: List<BookingResponse> = emptyList(),
    val isTokenExpired: Boolean = false,
    val errorMessage: String? = null
)

@HiltViewModel
class HistoryViewModel @Inject constructor(
    private val bookingRepository: BookingRepository,
    private val tokenManager: TokenManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(HistoryUiState())
    val uiState: StateFlow<HistoryUiState> = _uiState.asStateFlow()

    init {
        fetchMyBookings()
    }

    fun fetchMyBookings(isRefresh: Boolean = false) {
        viewModelScope.launch {
            if (isRefresh) {
                _uiState.update { it.copy(isRefreshing = true, errorMessage = null) }
            } else {
                _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            }

            try {
                // Fetch all/first 50 for now
                val response = bookingRepository.getMyBookings(page = 1, limit = 50)
                if (response.isSuccessful) {
                    val data = response.body()?.data ?: emptyList()
                    _uiState.update { 
                        it.copy(
                            isLoading = false,
                            isRefreshing = false,
                            bookings = data
                        ) 
                    }
                } else {
                    if (response.code() == 401) {
                        tokenManager.clearToken()
                        _uiState.update { it.copy(isLoading = false, isRefreshing = false, isTokenExpired = true) }
                    } else {
                        _uiState.update { 
                            it.copy(
                                isLoading = false, 
                                isRefreshing = false, 
                                errorMessage = "Lỗi tải lịch sử đặt sân: ${response.message()}"
                            ) 
                        }
                    }
                }
            } catch (e: Exception) {
                _uiState.update { 
                    it.copy(
                        isLoading = false, 
                        isRefreshing = false, 
                        errorMessage = "Lỗi mạng: ${e.message}"
                    ) 
                }
            }
        }
    }

    fun resetTokenExpired() {
        _uiState.update { it.copy(isTokenExpired = false) }
    }
}
