package com.tanh.datsan.ui.admin.category

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tanh.datsan.data.model.CreateUtilityDto
import com.tanh.datsan.data.model.UpdateUtilityDto
import com.tanh.datsan.data.repository.FieldRepository
import com.tanh.datsan.ui.state.ActionState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

import com.tanh.datsan.ui.state.AdminUtilityState

@HiltViewModel
class AdminUtilityViewModel @Inject constructor(
    private val fieldRepository: FieldRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(AdminUtilityState())
    val uiState: StateFlow<AdminUtilityState> = _uiState.asStateFlow()

    fun fetchUtilities() {
        viewModelScope.launch {
            _uiState.update { it.copy(actionState = ActionState.Loading) }
            try {
                val items = fieldRepository.getAllUtilities()
                _uiState.update { it.copy(utilities = items, actionState = ActionState.Idle) }
            } catch (e: Exception) {
                _uiState.update { it.copy(actionState = ActionState.Error(e.message ?: "Lỗi khi tải tiện ích")) }
            }
        }
    }

    fun createUtility(name: String, iconUrl: String?, price: Double?, type: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(actionState = ActionState.Loading) }
            try {
                fieldRepository.createUtility(CreateUtilityDto(name, iconUrl, price, type))
                _uiState.update { it.copy(actionState = ActionState.Success("Thêm tiện ích thành công!")) }
                fetchUtilities()
            } catch (e: Exception) {
                _uiState.update { it.copy(actionState = ActionState.Error(e.message ?: "Lỗi khi thêm tiện ích")) }
            }
        }
    }

    fun updateUtility(id: Int, name: String?, iconUrl: String?, price: Double?, type: String?) {
        viewModelScope.launch {
            _uiState.update { it.copy(actionState = ActionState.Loading) }
            try {
                fieldRepository.updateUtility(id, UpdateUtilityDto(name, iconUrl, price, type))
                _uiState.update { it.copy(actionState = ActionState.Success("Cập nhật tiện ích thành công!")) }
                fetchUtilities()
            } catch (e: Exception) {
                _uiState.update { it.copy(actionState = ActionState.Error(e.message ?: "Lỗi khi cập nhật")) }
            }
        }
    }

    fun deleteUtility(id: Int) {
        viewModelScope.launch {
            _uiState.update { it.copy(actionState = ActionState.Loading) }
            try {
                fieldRepository.deleteUtility(id)
                _uiState.update { it.copy(actionState = ActionState.Success("Xóa tiện ích thành công!")) }
                fetchUtilities()
            } catch (e: Exception) {
                _uiState.update { it.copy(actionState = ActionState.Error(e.message ?: "Lỗi khi xóa tiện ích")) }
            }
        }
    }

    fun resetActionState() {
        _uiState.update { it.copy(actionState = ActionState.Idle) }
    }
}
