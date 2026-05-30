package com.tanh.datsan.viewmodel

import android.util.Log
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tanh.datsan.core.TokenManager
import com.tanh.datsan.data.model.CheckPriceResponseDto
import com.tanh.datsan.data.model.CreateBookingDto
import com.tanh.datsan.data.model.Review
import com.tanh.datsan.data.model.VoucherDto
import com.tanh.datsan.data.repository.BookingRepository
import com.tanh.datsan.data.repository.FieldRepository
import com.tanh.datsan.data.repository.PricingRepository
import com.tanh.datsan.data.repository.ReviewRepository
import com.tanh.datsan.data.repository.VoucherRepository
import com.tanh.datsan.utils.calculateDiscount
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject
import retrofit2.HttpException
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import javax.inject.Inject

@HiltViewModel
class DetailViewModel @Inject constructor(
    private val fieldRepository: FieldRepository,
    tokenManager: TokenManager,
    private val bookingRepository: BookingRepository,
    private val voucherRepository: VoucherRepository,
    private val pricingRepository: PricingRepository,
    private val reviewRepository: ReviewRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<DetailUiState>(DetailUiState.Loading)
    val uiState: StateFlow<DetailUiState> = _uiState.asStateFlow()

    private val _eventFlow = MutableSharedFlow<UiEvent>()
    val eventFlow = _eventFlow.asSharedFlow()

    val isLoggedIn: StateFlow<Boolean> = tokenManager.token
        .map { token -> !token.isNullOrEmpty() }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = false
        )

    private val _priceState = MutableStateFlow<CheckPriceResponseDto?>(null)
    val priceState: StateFlow<CheckPriceResponseDto?> = _priceState.asStateFlow()

    private val _bookingState = mutableStateOf<BookingUiState>(BookingUiState.Idle)
    val bookingState: State<BookingUiState> = _bookingState

    private val _bookedSlots = mutableStateOf<List<String>>(emptyList())
    val bookedSlots: State<List<String>> = _bookedSlots

    private val _voucher = MutableStateFlow<List<VoucherDto>>(emptyList())
    val voucher: StateFlow<List<VoucherDto>> = _voucher.asStateFlow()

    private val _selectedVoucher = MutableStateFlow<VoucherDto?>(null)
    val selectedVoucher: StateFlow<VoucherDto?> = _selectedVoucher.asStateFlow()

    private val _discountAmount = MutableStateFlow(0.0)
    val discountAmount: StateFlow<Double> = _discountAmount.asStateFlow()

    fun fetchFieldDetail(fieldId: String) {
        viewModelScope.launch {
            _uiState.value = DetailUiState.Loading
            try {
                val fieldDeferred = async { fieldRepository.getFieldDetail(fieldId) }
                val reviewsDeferred = async {
                    try {
                        reviewRepository.getFieldReview(fieldId)
                    } catch (e: Exception) {
                        null
                    }
                }

                val field = fieldDeferred.await()
                val reviews = reviewsDeferred.await()

                _uiState.value = DetailUiState.Success(field.copy(reviews = reviews))
            } catch (e: Exception) {
                _uiState.value = DetailUiState.Error(e.message)
            }
        }
    }

    fun fetchBookedSlots(fieldId: String, date: String) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                _bookedSlots.value = emptyList()
                val response = bookingRepository.getBookingSlotsOfAFieldInADay(fieldId, date)
                if (response.isSuccessful) {
                    val bookingList = response.body()?.bookings ?: emptyList()
                    val blockedTimes = bookingList.mapNotNull { booking ->
                        try {
                            val instant = Instant.parse(booking.startTime)
                            val localTime = instant.atZone(ZoneId.systemDefault())
                            localTime.format(DateTimeFormatter.ofPattern("HH:mm"))
                        } catch (e: Exception) { null }
                    }
                    withContext(Dispatchers.Main) {
                        _bookedSlots.value = blockedTimes
                    }
                }
            } catch (e: Exception) {
                _bookedSlots.value = emptyList()
            }
        }
    }

    fun createBooking(fieldId: String, startTime: String, durationMinutes: Int) {
        viewModelScope.launch {
            _bookingState.value = BookingUiState.Loading
            try {
                val response = bookingRepository.createBooking(
                    request = CreateBookingDto(
                        fieldId = fieldId,
                        startTime = startTime,
                        durationMinutes = durationMinutes,
                        voucherCode = _selectedVoucher.value?.code,
                        platform = "mobile"
                    )
                )
                _bookingState.value = BookingUiState.Success(response.paymentUrl ?: "")
            } catch (e: HttpException) {
                handleHttpError(e)
            } catch (e: Exception) {
                _bookingState.value = BookingUiState.Error("Lỗi kết nối: ${e.localizedMessage}")
            }
        }
    }

    fun fetchVoucher(orderValue: Double) {
        viewModelScope.launch {
            try {
                val publicVouchers = async { voucherRepository.getAvailableVoucher(orderValue) }
                val myVouchers = async { voucherRepository.getMyVoucher() }
                _voucher.value = (publicVouchers.await() + myVouchers.await()).distinctBy { it.id }
            } catch (e: HttpException) {
                handleHttpError(e)
            } catch (e: Exception) {
                _voucher.value = emptyList()
            }
        }
    }

    private suspend fun handleHttpError(e: HttpException) {
        if (e.code() == 401) {
            _eventFlow.emit(UiEvent.NavigateToLogin)
        } else {
            val errorBody = e.response()?.errorBody()?.string()
            val message = try {
                JSONObject(errorBody ?: "").getString("message")
            } catch (ex: Exception) {
                "Lỗi server: ${e.code()}"
            }
            _bookingState.value = BookingUiState.Error(message)
        }
    }

    fun checkPrice(fieldId: String, startTime: String, durationMinutes: Int) {
        viewModelScope.launch {
            try {
                val response = pricingRepository.checkPrice(fieldId, startTime, durationMinutes)
                _priceState.value = response
                fetchVoucher(response.pricing.totalPrice)
            } catch (e: Exception) {
                Log.e("DetailViewModel", "Lỗi check giá: ${e.message}")
            }
        }
    }

    fun selectVoucher(voucher: VoucherDto?, orderValue: Double) {
        _selectedVoucher.value = voucher
        _discountAmount.value = voucher?.calculateDiscount(orderValue) ?: 0.0
    }

    fun resetBookingState() {
        _bookingState.value = BookingUiState.Idle
        _selectedVoucher.value = null
        _discountAmount.value = 0.0
    }
}