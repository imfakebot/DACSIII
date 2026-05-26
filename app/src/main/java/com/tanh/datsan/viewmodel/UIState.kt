package com.tanh.datsan.viewmodel


sealed class UiEvent {
    object NavigateToLogin : UiEvent()
    data class ShowSnackbar(val message: String) : UiEvent()
}