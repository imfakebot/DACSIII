package com.tanh.datsan.ui.admin.category

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tanh.datsan.data.model.CreateFieldTypeDto
import com.tanh.datsan.data.model.FieldType
import com.tanh.datsan.data.model.UpdateFieldTypeDto
import com.tanh.datsan.data.repository.FieldRepository
import com.tanh.datsan.ui.state.ActionState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

import com.tanh.datsan.ui.state.AdminFieldTypeState

@HiltViewModel
class AdminFieldTypeViewModel @Inject constructor(
    private val fieldRepository: FieldRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(AdminFieldTypeState())
    val uiState: StateFlow<AdminFieldTypeState> = _uiState.asStateFlow()

    fun fetchFieldTypes() {
        viewModelScope.launch {
            _uiState.update { it.copy(actionState = ActionState.Loading) }
            try {
                val types = fieldRepository.getAllFieldTypes()
                _uiState.update { it.copy(fieldTypes = types, actionState = ActionState.Idle) }
            } catch (e: Exception) {
                _uiState.update { it.copy(actionState = ActionState.Error(e.message ?: "Lỗi khi tải danh sách loại sân")) }
            }
        }
    }

    fun createFieldType(name: String, description: String?) {
        viewModelScope.launch {
            _uiState.update { it.copy(actionState = ActionState.Loading) }
            try {
                fieldRepository.createFieldType(CreateFieldTypeDto(name, description))
                _uiState.update { it.copy(actionState = ActionState.Success("Thêm loại sân thành công!")) }
                fetchFieldTypes() // Refresh list
            } catch (e: Exception) {
                _uiState.update { it.copy(actionState = ActionState.Error(e.message ?: "Lỗi khi thêm loại sân")) }
            }
        }
    }

    fun updateFieldType(id: String, name: String, description: String?) {
        viewModelScope.launch {
            _uiState.update { it.copy(actionState = ActionState.Loading) }
            try {
                fieldRepository.updateFieldType(id, UpdateFieldTypeDto(name, description))
                _uiState.update { it.copy(actionState = ActionState.Success("Cập nhật thành công!")) }
                fetchFieldTypes()
            } catch (e: Exception) {
                _uiState.update { it.copy(actionState = ActionState.Error(e.message ?: "Lỗi khi cập nhật")) }
            }
        }
    }

    fun deleteFieldType(id: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(actionState = ActionState.Loading) }
            try {
                fieldRepository.deleteFieldType(id)
                _uiState.update { it.copy(actionState = ActionState.Success("Xóa thành công!")) }
                fetchFieldTypes()
            } catch (e: Exception) {
                _uiState.update { it.copy(actionState = ActionState.Error(e.message ?: "Lỗi khi xóa")) }
            }
        }
    }

    fun resetActionState() {
        _uiState.update { it.copy(actionState = ActionState.Idle) }
    }
}
