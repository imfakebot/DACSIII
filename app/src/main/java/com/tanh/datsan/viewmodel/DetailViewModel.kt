package com.tanh.datsan.viewmodel

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tanh.datsan.data.model.CheckPriceResponseDto
import com.tanh.datsan.data.model.CreateBookingDto
import com.tanh.datsan.data.repository.BookingRepository
import com.tanh.datsan.data.repository.FieldRepository
import com.tanh.datsan.data.repository.PricingRepository
import com.tanh.datsan.data.repository.ReviewRepository
import com.tanh.datsan.utils.LocationHelper
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
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
    private val bookingRepository: BookingRepository,
    private val pricingRepository: PricingRepository,
    private val reviewRepository: ReviewRepository,
    private val locationHelper: LocationHelper
) : ViewModel() {

    private val _uiState = MutableStateFlow<DetailUiState>(DetailUiState.Loading)
    val uiState: StateFlow<DetailUiState> = _uiState.asStateFlow()


    private val _bookingState = mutableStateOf<BookingUiState>(BookingUiState.Idle)
    val bookingState: State<BookingUiState> = _bookingState

    private val _bookedSlots = mutableStateOf<List<String>>(emptyList())
    val bookedSlots: State<List<String>> = _bookedSlots

    private val _priceState = MutableStateFlow<CheckPriceResponseDto?>(null)
    val priceState: StateFlow<CheckPriceResponseDto?> = _priceState.asStateFlow()

    private val _eventFlow = MutableSharedFlow<UiEvent>()
    val eventFlow = _eventFlow.asSharedFlow()

    fun fetchFieldDetail(fieldId: String) {
        viewModelScope.launch {
            _uiState.value = DetailUiState.Loading
            locationHelper.getCurrentLocation { lat, lon ->
                viewModelScope.launch {
                    try {
                        val fieldDeferred = async {
                            fieldRepository.getFieldDetail(
                                fieldId = fieldId,
                                latitude = lat,
                                longitude = lon
                            )
                        }
                        val reviewDeferred = async {
                            try {
                                reviewRepository.getFieldReview(fieldId)
                            } catch (e: Exception) {
                                null
                            }
                        }

                        val field = fieldDeferred.await()
                        val reviews = reviewDeferred.await()

                        val reviewList = reviews?.data ?: emptyList()
                        val reviewMeta = reviews?.meta

                        val updatedField = field.copy(
                            reviews = reviewList,
                            reviewCount = reviewMeta?.total?:field.reviewCount,
                            averageRating = reviewMeta?.averageRating?:field.averageRating
                        )

                        _uiState.value = DetailUiState.Success( updatedField)
                    } catch (e: Exception) {
                    }
                }
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
                        } catch (e: Exception) {
                            null
                        }
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

    fun checkPrice(fieldId: String, startTime: String, durationMinutes: Int) {
        viewModelScope.launch {
            try {
                val response = pricingRepository.checkPrice(fieldId, startTime, durationMinutes)
                _priceState.value = response
            } catch (e: Exception) {
                _priceState.value = null
            }
        }
    }

    fun createBooking(fieldId: String, startTime: String, durationMinutes: Int, voucherCode: String? = null) {
        viewModelScope.launch {
            _bookingState.value = BookingUiState.Loading
            try {
                val response = bookingRepository.createBooking(
                    request = CreateBookingDto(
                        fieldId = fieldId,
                        startTime = startTime,
                        durationMinutes = durationMinutes,
                        voucherCode = voucherCode,
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

    fun resetBookingState() {
        _bookingState.value = BookingUiState.Idle
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
}