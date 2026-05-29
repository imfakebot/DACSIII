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

sealed interface CheckInUiState {
    object Idle : CheckInUiState
    object Loading : CheckInUiState
    data class Success(val booking: BookingResponse) : CheckInUiState
    data class Error(val message: String) : CheckInUiState
}

@HiltViewModel
class QrScannerViewModel @Inject constructor(
    private val bookingRepository: BookingRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<CheckInUiState>(CheckInUiState.Idle)
    val uiState: StateFlow<CheckInUiState> = _uiState.asStateFlow()

    fun checkIn(identifier: String) {
        viewModelScope.launch {
            _uiState.value = CheckInUiState.Loading
            try {
                val response = bookingRepository.checkIn(identifier)
                _uiState.value = CheckInUiState.Success(response)
            } catch (e: Exception) {
                _uiState.value = CheckInUiState.Error(e.message ?: "Unknown error occurred")
            }
        }
    }

    fun resetState() {
        _uiState.value = CheckInUiState.Idle
    }
}
