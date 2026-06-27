package com.tanh.datsan.ui.state

data class AdminVoucherUiState(
    val isCreating: Boolean = false,
    val createSuccess: Boolean = false,
    val errorMessage: String? = null,
    val successMessage: String? = null
)
