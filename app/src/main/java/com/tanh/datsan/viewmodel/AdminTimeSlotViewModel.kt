package com.tanh.datsan.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tanh.datsan.data.model.TimeSlotResponse
import com.tanh.datsan.data.model.UpdateTimeSlotRequest
import com.tanh.datsan.data.repository.PricingRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed class AdminTimeSlotUiState {
    object Idle : AdminTimeSlotUiState()
    object Loading : AdminTimeSlotUiState()
    data class Success(val message: String) : AdminTimeSlotUiState()
    data class Error(val message: String) : AdminTimeSlotUiState()
}

@HiltViewModel
class AdminTimeSlotViewModel @Inject constructor(
    private val pricingRepository: PricingRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<AdminTimeSlotUiState>(AdminTimeSlotUiState.Idle)
    val uiState: StateFlow<AdminTimeSlotUiState> = _uiState.asStateFlow()

    private val _timeSlots = MutableStateFlow<List<TimeSlotResponse>>(emptyList())
    val timeSlots: StateFlow<List<TimeSlotResponse>> = _timeSlots.asStateFlow()

    fun fetchTimeSlots() {
        viewModelScope.launch {
            _uiState.value = AdminTimeSlotUiState.Loading
            try {
                val response = pricingRepository.getAllTimeSlots()
                if (response.isSuccessful && response.body() != null) {
                    _timeSlots.value = response.body()!!
                    _uiState.value = AdminTimeSlotUiState.Idle
                } else {
                    _uiState.value = AdminTimeSlotUiState.Error("Không thể tải danh sách khung giờ.")
                }
            } catch (e: Exception) {
                _uiState.value = AdminTimeSlotUiState.Error("Lỗi: ${e.message}")
            }
        }
    }

    fun updateTimeSlot(id: Int, price: Double?, isPeakHour: Boolean?) {
        viewModelScope.launch {
            _uiState.value = AdminTimeSlotUiState.Loading
            try {
                val request = UpdateTimeSlotRequest(
                    price = price,
                    isPeakHour = isPeakHour
                )
                val response = pricingRepository.updateTimeSlot(id, request)
                if (response.isSuccessful) {
                    _uiState.value = AdminTimeSlotUiState.Success("Cập nhật thành công")
                    fetchTimeSlots() // Reload the list
                } else {
                    _uiState.value = AdminTimeSlotUiState.Error("Cập nhật thất bại.")
                }
            } catch (e: Exception) {
                _uiState.value = AdminTimeSlotUiState.Error("Lỗi cập nhật: ${e.message}")
            }
        }
    }

    fun resetUiState() {
        _uiState.value = AdminTimeSlotUiState.Idle
    }
}
