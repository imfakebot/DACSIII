package com.tanh.datsan.ui.auth

sealed class AuthUiEvent {
    data class ShowToast(val message: String) : AuthUiEvent()
    data class NavigateToResetPassword(val email: String) : AuthUiEvent()
    data class NavigateToHome(val message: String) : AuthUiEvent()
    object NavigateBackToLogin : AuthUiEvent()
    object OtpResent : AuthUiEvent()
}
