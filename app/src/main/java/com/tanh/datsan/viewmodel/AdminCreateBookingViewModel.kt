package com.tanh.datsan.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tanh.datsan.data.model.AdminCreateBookingDto
import com.tanh.datsan.data.model.BookingTimeSlot
import com.tanh.datsan.data.model.Branch
import com.tanh.datsan.data.model.FieldResponse
import com.tanh.datsan.data.repository.BookingRepository
import com.tanh.datsan.data.repository.BranchRepository
import com.tanh.datsan.data.repository.FieldRepository
import com.tanh.datsan.data.repository.PricingRepository
import com.tanh.datsan.data.model.CheckPriceResponseDto
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject

sealed class AdminCreateBookingUiState {
    object Idle : AdminCreateBookingUiState()
    object Loading : AdminCreateBookingUiState()
    data class Success(val message: String) : AdminCreateBookingUiState()
    data class Error(val message: String) : AdminCreateBookingUiState()
}

@HiltViewModel
class AdminCreateBookingViewModel @Inject constructor(
    private val branchRepository: BranchRepository,
    private val fieldRepository: FieldRepository,
    private val bookingRepository: BookingRepository,
    private val pricingRepository: PricingRepository
) : ViewModel() {

    private val _uiState =
        MutableStateFlow<AdminCreateBookingUiState>(AdminCreateBookingUiState.Idle)
    val uiState: StateFlow<AdminCreateBookingUiState> = _uiState.asStateFlow()

    private val _branches = MutableStateFlow<List<Branch>>(emptyList())
    val branches: StateFlow<List<Branch>> = _branches.asStateFlow()

    private val _fields = MutableStateFlow<List<FieldResponse>>(emptyList())
    val fields: StateFlow<List<FieldResponse>> = _fields.asStateFlow()

    private val _availableSlots = MutableStateFlow<List<BookingTimeSlot>>(emptyList())
    val availableSlots: StateFlow<List<BookingTimeSlot>> = _availableSlots.asStateFlow()

    private val _selectedBranch = MutableStateFlow<Branch?>(null)
    val selectedBranch: StateFlow<Branch?> = _selectedBranch.asStateFlow()

    private val _selectedField = MutableStateFlow<FieldResponse?>(null)
    val selectedField: StateFlow<FieldResponse?> = _selectedField.asStateFlow()

    private val _selectedDate = MutableStateFlow<String>(
        SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
    )
    val selectedDate: StateFlow<String> = _selectedDate.asStateFlow()

    private val _selectedSlot = MutableStateFlow<BookingTimeSlot?>(null)
    val selectedSlot: StateFlow<BookingTimeSlot?> = _selectedSlot.asStateFlow()

    private val _priceState = MutableStateFlow<CheckPriceResponseDto?>(null)
    val priceState: StateFlow<CheckPriceResponseDto?> = _priceState.asStateFlow()

    private val _selectedDuration = MutableStateFlow<Int>(90)
    val selectedDuration: StateFlow<Int> = _selectedDuration.asStateFlow()

    fun fetchBranches() {
        viewModelScope.launch {
            try {
                _uiState.value = AdminCreateBookingUiState.Loading
                val branchesList = branchRepository.getBranches()
                _branches.value = branchesList
                _uiState.value = AdminCreateBookingUiState.Idle
            } catch (e: Exception) {
                _uiState.value = AdminCreateBookingUiState.Error("Lỗi tải chi nhánh: ${e.message}")
            }
        }
    }

    fun selectBranch(branch: Branch) {
        _selectedBranch.value = branch
        _selectedField.value = null
        _availableSlots.value = emptyList()
        _selectedSlot.value = null
        _priceState.value = null
        fetchFieldsByBranch(branch.id)
    }

    private fun fetchFieldsByBranch(branchId: String) {
        viewModelScope.launch {
            try {
                _uiState.value = AdminCreateBookingUiState.Loading
                val response = fieldRepository.getAllField(
                    branchId = branchId,
                    limit = 100,
                    lat = null,
                    lon = null
                )
                _fields.value = response.data
                _uiState.value = AdminCreateBookingUiState.Idle
            } catch (e: Exception) {
                _uiState.value =
                    AdminCreateBookingUiState.Error("Lỗi tải danh sách sân: ${e.message}")
            }
        }
    }

    fun selectField(field: FieldResponse) {
        _selectedField.value = field
        _selectedSlot.value = null
        _priceState.value = null
        fetchAvailableSlots(field.id, _selectedDate.value)
    }

    fun selectDate(date: String) {
        _selectedDate.value = date
        _selectedSlot.value = null
        _priceState.value = null
        _selectedField.value?.let { field ->
            fetchAvailableSlots(field.id, date)
        }
    }

    fun selectSlot(slot: BookingTimeSlot) {
        _selectedSlot.value = slot
        calculatePrice()
    }

    fun selectDuration(duration: Int) {
        _selectedDuration.value = duration
        _selectedSlot.value = null
        _priceState.value = null
        _selectedField.value?.let { field ->
            fetchAvailableSlots(field.id, _selectedDate.value)
        }
    }

    private fun calculatePrice() {
        val slot = _selectedSlot.value ?: return
        val fieldId = _selectedField.value?.id ?: return
        
        viewModelScope.launch {
            try {
                // Determine timezone offset string
                val tz = java.util.TimeZone.getDefault()
                val offsetInMillis = tz.getOffset(System.currentTimeMillis())
                val offsetHours = Math.abs(offsetInMillis / 3600000)
                val offsetMinutes = Math.abs((offsetInMillis / 60000) % 60)
                val sign = if (offsetInMillis >= 0) "+" else "-"
                val offsetString = String.format(Locale.getDefault(), "%s%02d:%02d", sign, offsetHours, offsetMinutes)
                
                val startTimeFull = "${_selectedDate.value}T${slot.startTime}:00.000$offsetString"
                
                val response = pricingRepository.checkPrice(fieldId, startTimeFull, _selectedDuration.value)
                _priceState.value = response
            } catch (e: Exception) {
                _priceState.value = null
                Log.e("AdminCreateBooking", "Error calculating price: ${e.message}")
            }
        }
    }

    private fun fetchAvailableSlots(fieldId: String, date: String) {
        viewModelScope.launch {
            try {
                _uiState.value = AdminCreateBookingUiState.Loading
                val response = bookingRepository.getBookingSlotsOfAFieldInADay(fieldId, date)

                if (response.isSuccessful && response.body() != null) {
                    val bookedSlotsList = response.body()!!.bookings
                    val currentDuration = _selectedDuration.value

                    val allBaseSlots = generate30MinSteps()

                    val calendar = java.util.Calendar.getInstance()
                    val today = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault()).format(calendar.time)
                    val currentMins = calendar.get(java.util.Calendar.HOUR_OF_DAY) * 60 + calendar.get(java.util.Calendar.MINUTE)
                    val isToday = date == today

                    val finalSlotsForUi = allBaseSlots.map { startTimeString ->
                        val reqStartMins = timeToMins(startTimeString)
                        val reqEndMins = reqStartMins + currentDuration

                        val isBooked = bookedSlotsList.any { booked ->
                            val bStartMins = timeToMins(booked.startTime)
                            val bEndMins = timeToMins(booked.endTime)

                            reqStartMins < bEndMins && reqEndMins > bStartMins
                        }

                        val isPast = isToday && reqStartMins <= currentMins

                        val endHour = reqEndMins / 60
                        val endMin = reqEndMins % 60
                        val endTimeString = String.format(Locale.getDefault(), "%02d:%02d", endHour, endMin)

                        BookingTimeSlot(
                            startTime = startTimeString,
                            endTime = endTimeString,
                            status = if (isBooked || isPast) "booked" else "available"
                        )
                    }

                    _availableSlots.value = finalSlotsForUi
                    _uiState.value = AdminCreateBookingUiState.Idle
                } else {
                    _uiState.value = AdminCreateBookingUiState.Error("Không thể tải lịch sân")
                }
            } catch (e: Exception) {
                _uiState.value = AdminCreateBookingUiState.Error("Lỗi tải lịch sân: ${e.message}")
            }
        }
    }

    private fun generate30MinSteps(): List<String> {
        val list = mutableListOf<String>()
        for (h in 5..22) {
            list.add(String.format(Locale.getDefault(), "%02d:00", h))
            list.add(String.format(Locale.getDefault(), "%02d:30", h))
        }
        return list
    }

    private fun timeToMins(timeString: String): Int {
        if (timeString.contains("T")) {
            try {
                val isUTC = timeString.endsWith("Z")
                val formatString = if (isUTC) "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'" else "yyyy-MM-dd'T'HH:mm:ss.SSS"
                val format = java.text.SimpleDateFormat(formatString, java.util.Locale.getDefault())
                if (isUTC) {
                    format.timeZone = java.util.TimeZone.getTimeZone("UTC")
                }
                val date = format.parse(timeString)
                val calendar = java.util.Calendar.getInstance()
                calendar.time = date!!
                return calendar.get(java.util.Calendar.HOUR_OF_DAY) * 60 + calendar.get(java.util.Calendar.MINUTE)
            } catch (e: Exception) {
                val timePart = timeString.substringAfter("T").take(5)
                val parts = timePart.split(":")
                return parts[0].toInt() * 60 + parts[1].toInt()
            }
        } else {
            val parts = timeString.take(5).split(":")
            return parts[0].toInt() * 60 + parts[1].toInt()
        }
    }
    private fun generateBaseTimeSlots(): List<BookingTimeSlot> {
        return listOf(
            BookingTimeSlot(startTime = "05:00", endTime = "06:30", status = "unknown"),
            BookingTimeSlot(startTime = "06:30", endTime = "08:00", status = "unknown"),
            BookingTimeSlot(startTime = "08:00", endTime = "09:30", status = "unknown"),
            BookingTimeSlot(startTime = "15:00", endTime = "16:30", status = "unknown"),
            BookingTimeSlot(startTime = "16:30", endTime = "18:00", status = "unknown"),
            BookingTimeSlot(startTime = "18:00", endTime = "19:30", status = "unknown"),
            BookingTimeSlot(startTime = "19:30", endTime = "21:00", status = "unknown"),
            BookingTimeSlot(startTime = "21:00", endTime = "22:30", status = "unknown")
        )
    }

    fun createBooking(customerName: String, customerPhone: String) {
        val fieldId = _selectedField.value?.id
        val slot = _selectedSlot.value
        if (fieldId == null || slot == null) {
            _uiState.value = AdminCreateBookingUiState.Error("Vui lòng chọn sân và khung giờ")
            return
        }

        viewModelScope.launch {
            try {
                _uiState.value = AdminCreateBookingUiState.Loading

                val tz = java.util.TimeZone.getDefault()
                val offsetInMillis = tz.getOffset(System.currentTimeMillis())
                val offsetHours = Math.abs(offsetInMillis / 3600000)
                val offsetMinutes = Math.abs((offsetInMillis / 60000) % 60)
                val sign = if (offsetInMillis >= 0) "+" else "-"
                val offsetString = String.format(Locale.getDefault(), "%s%02d:%02d", sign, offsetHours, offsetMinutes)
                val startTimeFull = "${_selectedDate.value}T${slot.startTime}:00.000$offsetString"

                val startParts = slot.startTime.split(":")
                val endParts = slot.endTime.split(":")
                val startMins = startParts[0].toInt() * 60 + startParts[1].toInt()
                val endMins = endParts[0].toInt() * 60 + endParts[1].toInt()
                val duration = endMins - startMins


                val request = AdminCreateBookingDto(
                    fieldId = fieldId,
                    startTime = startTimeFull,
                    durationMinutes = duration,
                    customerName = customerName,
                    customerPhone = customerPhone,
                    status = "completed"
                )

                bookingRepository.createAdminBooking(request)

                _uiState.value = AdminCreateBookingUiState.Success("Chốt đơn thành công rực rỡ!")
                // Do NOT call fetchAvailableSlots here because it immediately overwrites uiState to Loading,
                // causing the Success dialog to never appear. The user will navigate back anyway.
            } catch (e: Exception) {
                _uiState.value = AdminCreateBookingUiState.Error("Lỗi tạo đơn: ${e.message}")
            }
        }
    }
    fun resetUiState() {
        _uiState.value = AdminCreateBookingUiState.Idle
    }
}
