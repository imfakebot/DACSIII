package com.tanh.datsan.ui.admin.voucher

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tanh.datsan.data.model.CreateVoucherDto
import com.tanh.datsan.data.repository.VoucherRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

import com.tanh.datsan.ui.state.AdminVoucherUiState

@HiltViewModel
class AdminVoucherViewModel @Inject constructor(
    private val voucherRepository: VoucherRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(AdminVoucherUiState())
    val uiState: StateFlow<AdminVoucherUiState> = _uiState.asStateFlow()

    fun createVoucher(request: CreateVoucherDto) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isCreating = true, errorMessage = null, createSuccess = false)
            try {
                val response = voucherRepository.createVoucher(request)
                if (response.isSuccessful) {
                    _uiState.value = _uiState.value.copy(
                        isCreating = false,
                        createSuccess = true,
                        successMessage = "Tạo mã \"${response.body()?.code}\" thành công!"
                    )
                } else {
                    _uiState.value = _uiState.value.copy(
                        isCreating = false,
                        errorMessage = "Tạo thất bại (Code: ${response.code()})"
                    )
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isCreating = false,
                    errorMessage = "Lỗi kết nối: ${e.message}"
                )
            }
        }
    }

    fun clearMessages() {
        _uiState.value = _uiState.value.copy(
            errorMessage = null,
            successMessage = null,
            createSuccess = false
        )
    }
}
