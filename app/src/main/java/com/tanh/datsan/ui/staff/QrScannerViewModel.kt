package com.tanh.datsan.ui.staff

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tanh.datsan.data.repository.BookingRepository
import com.tanh.datsan.utils.ResponseHelper.parseError
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import retrofit2.HttpException
import javax.inject.Inject


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
            } catch (e: HttpException) {
                val errorBody = e.response()?.errorBody()?.string()
                val errorMessage = parseError(errorBody)
                _uiState.value = CheckInUiState.Error(errorMessage)
            } catch (e: Exception) {
                _uiState.value = CheckInUiState.Error(e.message ?: "Unknown error occurred")
            }
        }
    }

    fun resetState() {
        _uiState.value = CheckInUiState.Idle
    }
}
