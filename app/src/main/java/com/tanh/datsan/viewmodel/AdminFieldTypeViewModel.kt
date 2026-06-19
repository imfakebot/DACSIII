package com.tanh.datsan.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tanh.datsan.data.model.CreateFieldTypeDto
import com.tanh.datsan.data.model.FieldType
import com.tanh.datsan.data.model.UpdateFieldTypeDto
import com.tanh.datsan.data.repository.FieldRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed class AdminFieldTypeUiState {
    object Idle : AdminFieldTypeUiState()
    object Loading : AdminFieldTypeUiState()
    data class Success(val message: String) : AdminFieldTypeUiState()
    data class Error(val message: String) : AdminFieldTypeUiState()
}

@HiltViewModel
class AdminFieldTypeViewModel @Inject constructor(
    private val fieldRepository: FieldRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<AdminFieldTypeUiState>(AdminFieldTypeUiState.Idle)
    val uiState: StateFlow<AdminFieldTypeUiState> = _uiState.asStateFlow()

    private val _fieldTypes = MutableStateFlow<List<FieldType>>(emptyList())
    val fieldTypes: StateFlow<List<FieldType>> = _fieldTypes.asStateFlow()

    fun fetchFieldTypes() {
        viewModelScope.launch {
            _uiState.value = AdminFieldTypeUiState.Loading
            try {
                val types = fieldRepository.getAllFieldTypes()
                _fieldTypes.value = types
                _uiState.value = AdminFieldTypeUiState.Idle
            } catch (e: Exception) {
                _uiState.value = AdminFieldTypeUiState.Error(e.message ?: "Lỗi khi tải danh sách loại sân")
            }
        }
    }

    fun createFieldType(name: String, description: String?) {
        viewModelScope.launch {
            _uiState.value = AdminFieldTypeUiState.Loading
            try {
                fieldRepository.createFieldType(CreateFieldTypeDto(name, description))
                _uiState.value = AdminFieldTypeUiState.Success("Thêm loại sân thành công!")
                fetchFieldTypes() // Refresh list
            } catch (e: Exception) {
                _uiState.value = AdminFieldTypeUiState.Error(e.message ?: "Lỗi khi thêm loại sân")
            }
        }
    }

    fun updateFieldType(id: String, name: String, description: String?) {
        viewModelScope.launch {
            _uiState.value = AdminFieldTypeUiState.Loading
            try {
                fieldRepository.updateFieldType(id, UpdateFieldTypeDto(name, description))
                _uiState.value = AdminFieldTypeUiState.Success("Cập nhật thành công!")
                fetchFieldTypes()
            } catch (e: Exception) {
                _uiState.value = AdminFieldTypeUiState.Error(e.message ?: "Lỗi khi cập nhật")
            }
        }
    }

    fun deleteFieldType(id: String) {
        viewModelScope.launch {
            _uiState.value = AdminFieldTypeUiState.Loading
            try {
                fieldRepository.deleteFieldType(id)
                _uiState.value = AdminFieldTypeUiState.Success("Xóa thành công!")
                fetchFieldTypes()
            } catch (e: Exception) {
                _uiState.value = AdminFieldTypeUiState.Error(e.message ?: "Lỗi khi xóa")
            }
        }
    }

    fun resetUiState() {
        _uiState.value = AdminFieldTypeUiState.Idle
    }
}
