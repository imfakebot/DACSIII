package com.tanh.datsan.ui.admin.pricing

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tanh.datsan.data.model.FieldResponse
import com.tanh.datsan.data.model.TimeSlotResponse
import com.tanh.datsan.data.model.UpdateTimeSlotRequest
import com.tanh.datsan.data.model.CreateTimeSlotRequest
import com.tanh.datsan.data.repository.PricingRepository
import com.tanh.datsan.data.repository.FieldRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject



@HiltViewModel
class AdminTimeSlotViewModel @Inject constructor(
    private val pricingRepository: PricingRepository,
    private val fieldRepository: FieldRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<AdminTimeSlotUiState>(AdminTimeSlotUiState.Idle)
    val uiState: StateFlow<AdminTimeSlotUiState> = _uiState.asStateFlow()

    private val _timeSlots = MutableStateFlow<List<TimeSlotResponse>>(emptyList())
    val timeSlots: StateFlow<List<TimeSlotResponse>> = _timeSlots.asStateFlow()

    private val _fields = MutableStateFlow<List<FieldResponse>>(emptyList())
    val fields: StateFlow<List<FieldResponse>> = _fields.asStateFlow()

    fun fetchTimeSlots(fieldId: String) {
        viewModelScope.launch {
            _uiState.value = AdminTimeSlotUiState.Loading
            try {
                val response = pricingRepository.getAllTimeSlots()
                if (response.isSuccessful && response.body() != null) {
                    _timeSlots.value = response.body()!!.filter { it.field?.id == fieldId }
                }
                
                // Fetch specific field
                val fieldResponse = fieldRepository.getFieldDetail(fieldId, null, null)
                if (fieldResponse != null) {
                    _fields.value = listOf(fieldResponse)
                } else {
                    _fields.value = emptyList()
                }
                
                _uiState.value = AdminTimeSlotUiState.Idle
            } catch (e: Exception) {
                _uiState.value = AdminTimeSlotUiState.Error("Lỗi: ${e.message}")
            }
        }
    }

    fun updateTimeSlot(id: Int, price: Double?, isPeakHour: Boolean?, fieldId: String) {
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
                    fetchTimeSlots(fieldId)
                } else {
                    _uiState.value = AdminTimeSlotUiState.Error("Cập nhật thất bại.")
                }
            } catch (e: Exception) {
                _uiState.value = AdminTimeSlotUiState.Error("Lỗi cập nhật: ${e.message}")
            }
        }
    }

    fun createTimeSlot(fieldId: String, startTime: String, endTime: String, price: Double, isPeakHour: Boolean) {
        viewModelScope.launch {
            _uiState.value = AdminTimeSlotUiState.Loading
            try {
                val request = CreateTimeSlotRequest(
                    fieldId = fieldId,
                    startTime = startTime,
                    endTime = endTime,
                    price = price,
                    isPeakHour = isPeakHour
                )
                val response = pricingRepository.createTimeSlot(request)
                if (response.isSuccessful) {
                    _uiState.value = AdminTimeSlotUiState.Success("Thêm khung giờ thành công")
                    fetchTimeSlots(fieldId) // Reload the list
                } else {
                    _uiState.value = AdminTimeSlotUiState.Error("Thêm thất bại.")
                }
            } catch (e: Exception) {
                _uiState.value = AdminTimeSlotUiState.Error("Lỗi thêm: ${e.message}")
            }
        }
    }

    fun resetUiState() {
        _uiState.value = AdminTimeSlotUiState.Idle
    }
}
