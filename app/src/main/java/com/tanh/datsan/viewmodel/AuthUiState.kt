package com.tanh.datsan.viewmodel

sealed class AuthUiState {
    object Idle : AuthUiState()
    object Loading : AuthUiState()
    data class OtpSent(val email: String, val isRegister: Boolean) : AuthUiState()
    object Authenticated : AuthUiState()
    data class Success(val message: String) : AuthUiState()
    data class Error(val message: String) : AuthUiState()
}
