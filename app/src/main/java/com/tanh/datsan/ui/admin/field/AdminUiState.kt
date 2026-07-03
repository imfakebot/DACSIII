package com.tanh.datsan.ui.admin.field

sealed class AdminUiState {
    object Loading : AdminUiState()
    data class Success(val message: String? = null, val data: Any? = null) : AdminUiState()
    data class Error(val message: String) : AdminUiState()
    object Idle : AdminUiState()
}
