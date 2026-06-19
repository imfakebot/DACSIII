package com.tanh.datsan.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tanh.datsan.data.model.CreateUtilityDto
import com.tanh.datsan.data.model.UpdateUtilityDto
import com.tanh.datsan.data.model.Utility
import com.tanh.datsan.data.repository.FieldRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed class AdminUtilityUiState {
    object Idle : AdminUtilityUiState()
    object Loading : AdminUtilityUiState()
    data class Success(val message: String) : AdminUtilityUiState()
    data class Error(val message: String) : AdminUtilityUiState()
}

@HiltViewModel
class AdminUtilityViewModel @Inject constructor(
    private val fieldRepository: FieldRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<AdminUtilityUiState>(AdminUtilityUiState.Idle)
    val uiState: StateFlow<AdminUtilityUiState> = _uiState.asStateFlow()

    private val _utilities = MutableStateFlow<List<Utility>>(emptyList())
    val utilities: StateFlow<List<Utility>> = _utilities.asStateFlow()

    fun fetchUtilities() {
        viewModelScope.launch {
            _uiState.value = AdminUtilityUiState.Loading
            try {
                val items = fieldRepository.getAllUtilities()
                _utilities.value = items
                _uiState.value = AdminUtilityUiState.Idle
            } catch (e: Exception) {
                _uiState.value = AdminUtilityUiState.Error(e.message ?: "Lỗi khi tải tiện ích")
            }
        }
    }

    fun createUtility(name: String, iconUrl: String?, price: Double?, type: String) {
        viewModelScope.launch {
            _uiState.value = AdminUtilityUiState.Loading
            try {
                fieldRepository.createUtility(CreateUtilityDto(name, iconUrl, price, type))
                _uiState.value = AdminUtilityUiState.Success("Thêm tiện ích thành công!")
                fetchUtilities()
            } catch (e: Exception) {
                _uiState.value = AdminUtilityUiState.Error(e.message ?: "Lỗi khi thêm tiện ích")
            }
        }
    }

    fun updateUtility(id: Int, name: String?, iconUrl: String?, price: Double?, type: String?) {
        viewModelScope.launch {
            _uiState.value = AdminUtilityUiState.Loading
            try {
                fieldRepository.updateUtility(id, UpdateUtilityDto(name, iconUrl, price, type))
                _uiState.value = AdminUtilityUiState.Success("Cập nhật tiện ích thành công!")
                fetchUtilities()
            } catch (e: Exception) {
                _uiState.value = AdminUtilityUiState.Error(e.message ?: "Lỗi khi cập nhật")
            }
        }
    }

    fun deleteUtility(id: Int) {
        viewModelScope.launch {
            _uiState.value = AdminUtilityUiState.Loading
            try {
                fieldRepository.deleteUtility(id)
                _uiState.value = AdminUtilityUiState.Success("Xóa tiện ích thành công!")
                fetchUtilities()
            } catch (e: Exception) {
                _uiState.value = AdminUtilityUiState.Error(e.message ?: "Lỗi khi xóa tiện ích")
            }
        }
    }

    fun resetUiState() {
        _uiState.value = AdminUtilityUiState.Idle
    }
}
