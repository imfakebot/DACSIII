package com.tanh.datsan.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tanh.datsan.BuildConfig
import com.tanh.datsan.core.TokenManager
import com.tanh.datsan.data.repository.BookingRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class BookingSuccessViewModel @Inject constructor(
    private val bookingRepository: BookingRepository,
    tokenManager: TokenManager
): ViewModel(){
    private val _uiState = MutableStateFlow<BookingReceiptUiState>(BookingReceiptUiState.Loading)
    val uiState : StateFlow<BookingReceiptUiState> = _uiState.asStateFlow()

    val tokenFlow : StateFlow<String> = tokenManager.token
        .map { it ?:"" }
        .stateIn(
            scope= viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = ""
        )

    fun fetchBookingReceipt(bookingId:String){
        viewModelScope.launch {
            _uiState.value = BookingReceiptUiState.Loading
            try{
                val receipt = bookingRepository.getBookingById(bookingId)
                _uiState.value = BookingReceiptUiState.Success(receipt)
            }catch (e :Exception){
                _uiState.value = BookingReceiptUiState.Error(e.message ?: "Unknown error")
            }
        }
    }

    fun getDownloadUrl(bookingId : String):String{
        return "${BuildConfig.API_BASE_URL}/bookings/$bookingId/download"
    }
}