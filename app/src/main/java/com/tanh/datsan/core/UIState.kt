package com.tanh.datsan.core


sealed class UiEvent {
    object NavigateToLogin : UiEvent()
    data class ShowSnackbar(val message: String) : UiEvent()
}