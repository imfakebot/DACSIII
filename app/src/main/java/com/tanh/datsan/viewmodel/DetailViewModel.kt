package com.tanh.datsan.viewmodel

import android.util.Log
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import androidx.compose.runtime.State
import androidx.lifecycle.ViewModel
import com.tanh.datsan.core.TokenManager
import com.tanh.datsan.data.model.CreateBookingDto
import com.tanh.datsan.data.repository.FieldRepository
import com.tanh.datsan.data.repository.BookingRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.collections.emptyList

@HiltViewModel
class DetailViewModel @Inject constructor(
    private val fieldRepository: FieldRepository,
    tokenManager: TokenManager,
    private val bookingRepository: BookingRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<DetailUiState>(DetailUiState.Loading)
    val uiState: StateFlow<DetailUiState> = _uiState.asStateFlow()
    val isLoggedIn: StateFlow<Boolean> = tokenManager.token
        .map { token -> !token.isNullOrEmpty() }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = false
        )

    fun fetchFieldDetail(fieldId: String) {
        viewModelScope.launch {
            _uiState.value = DetailUiState.Loading
            try {
                // Gọi API lấy chi tiết 1 sân theo ID
                val response = fieldRepository.getFieldDetail(fieldId)
                _uiState.value = DetailUiState.Success(response)
            } catch (e: Exception) {
                _uiState.value = DetailUiState.Error(e.message)
            }
        }
    }

    private val _bookingState = mutableStateOf<BookingUiState>(BookingUiState.Idle)
    val bookingState: State<BookingUiState> = _bookingState

    private val _bookedSlots = mutableStateOf<List<String>>(emptyList())
    val bookedSlots: State<List<String>> = _bookedSlots

    fun fetchBookedSlots(fieldId: String, date: String) {
        viewModelScope.launch {
            try {
              _bookedSlots.value = emptyList()
                val response = bookingRepository.getBookingSlotsOfAFieldInADay(fieldId, date)
                if(response.isSuccessful){
                    val bookingList = response.body()?.bookings?: emptyList()

                    val extractTime = bookingList.mapNotNull { booking ->
                        val startTime = booking.startTime
                        if (startTime.length >= 16) {
                            startTime.substring(11, 16) // Trích xuất "HH:mm"
                        } else null
                    }
                    _bookedSlots.value = extractTime
                } else{
                    _bookedSlots.value = emptyList()
                    Log.d("DetailViewModel", "Error fetching booked slots: ${response.errorBody()?.string()}")
                }
            } catch (e: Exception) {
                Log.d("DetailViewModel", "Exception fetching booked slots: ${e.message}")
                _bookedSlots.value = emptyList()
            }
        }
    }

    fun createBooking(fieldId: String, startTime: String, durationMinutes: Int) {
        viewModelScope.launch {
            _bookingState.value = BookingUiState.Loading
            try {
                // Gọi API POST /bookings với DTO
                val response = bookingRepository.createBooking(
                    CreateBookingDto(
                        fieldId = fieldId,
                        startTime = startTime,
                        durationMinutes = durationMinutes,
                        voucherCode = null //TODO
                    )
                )

                if (response.paymentUrl != null) {
                    _bookingState.value = BookingUiState.Success(response.paymentUrl)
                } else {
                    _bookingState.value = BookingUiState.Error("")
                }
            } catch (e: Exception) {
                _bookingState.value = BookingUiState.Error(e.message)
            }
        }
    }

    fun resetBookingState() {
        _bookingState.value = BookingUiState.Idle
    }
}

