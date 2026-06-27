package com.tanh.datsan.ui.state

sealed class ActionState {
    object Idle : ActionState()
    object Loading : ActionState()
    data class Success(val message: String? = null) : ActionState()
    data class Error(val message: String) : ActionState()
}
