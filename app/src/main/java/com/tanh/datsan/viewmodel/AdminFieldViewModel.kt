package com.tanh.datsan.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tanh.datsan.data.model.CreateFieldRequest
import com.tanh.datsan.data.model.FieldResponse
import com.tanh.datsan.data.model.FieldType
import com.tanh.datsan.data.model.UpdateFieldRequest
import com.tanh.datsan.data.repository.BranchRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class AdminFieldUiState(
    val isLoading: Boolean = false,
    val fields: List<FieldResponse> = emptyList(),
    val fieldTypes: List<FieldType> = emptyList(),
    val errorMessage: String? = null,
    val successMessage: String? = null,
    val isSubmitting: Boolean = false,
    val branchId: String = "",
    val branchName: String = ""
)

@HiltViewModel
class AdminFieldViewModel @Inject constructor(
    private val branchRepository: BranchRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(AdminFieldUiState())
    val uiState: StateFlow<AdminFieldUiState> = _uiState.asStateFlow()

    fun init(branchId: String, branchName: String) {
        _uiState.update { it.copy(branchId = branchId, branchName = branchName) }
        fetchFields(branchId)
        fetchFieldTypes()
    }

    fun fetchFields(branchId: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            try {
                val response = branchRepository.getFieldsByBranch(branchId)
                _uiState.update { it.copy(isLoading = false, fields = response.data) }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, errorMessage = "Lỗi kết nối: ${e.message}") }
            }
        }
    }

    private fun fetchFieldTypes() {
        viewModelScope.launch {
            try {
                val types = branchRepository.getAllFieldTypes()
                _uiState.update { it.copy(fieldTypes = types) }
            } catch (e: Exception) {
                // Silently ignore field type loading error
            }
        }
    }

    fun createField(request: CreateFieldRequest, onSuccess: () -> Unit) {
        viewModelScope.launch {
            _uiState.update { it.copy(isSubmitting = true, errorMessage = null) }
            try {
                val response = branchRepository.createField(request)
                if (response.isSuccessful && response.body() != null) {
                    val newField = response.body()!!
                    _uiState.update {
                        it.copy(
                            isSubmitting = false,
                            fields = it.fields + newField,
                            successMessage = "Tạo sân thành công!"
                        )
                    }
                    onSuccess()
                } else {
                    _uiState.update { it.copy(isSubmitting = false, errorMessage = "Không thể tạo sân: ${response.message()}") }
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(isSubmitting = false, errorMessage = "Lỗi kết nối: ${e.message}") }
            }
        }
    }

    fun updateField(id: String, request: UpdateFieldRequest, onSuccess: () -> Unit) {
        viewModelScope.launch {
            _uiState.update { it.copy(isSubmitting = true, errorMessage = null) }
            try {
                val response = branchRepository.updateField(id, request)
                if (response.isSuccessful && response.body() != null) {
                    val updated = response.body()!!
                    _uiState.update {
                        it.copy(
                            isSubmitting = false,
                            fields = it.fields.map { f -> if (f.id == id) updated else f },
                            successMessage = "Cập nhật sân thành công!"
                        )
                    }
                    onSuccess()
                } else {
                    _uiState.update { it.copy(isSubmitting = false, errorMessage = "Không thể cập nhật: ${response.message()}") }
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(isSubmitting = false, errorMessage = "Lỗi kết nối: ${e.message}") }
            }
        }
    }

    fun deleteField(id: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(errorMessage = null) }
            try {
                val response = branchRepository.deleteField(id)
                if (response.isSuccessful) {
                    _uiState.update {
                        it.copy(
                            fields = it.fields.filter { f -> f.id != id },
                            successMessage = "Đã xóa sân!"
                        )
                    }
                } else {
                    _uiState.update { it.copy(errorMessage = "Không thể xóa sân: ${response.message()}") }
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(errorMessage = "Lỗi kết nối: ${e.message}") }
            }
        }
    }

    fun clearMessages() = _uiState.update { it.copy(errorMessage = null, successMessage = null) }
}
