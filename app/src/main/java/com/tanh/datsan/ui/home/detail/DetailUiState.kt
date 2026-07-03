package com.tanh.datsan.ui.home.detail

import com.tanh.datsan.data.model.FieldResponse

sealed interface DetailUiState{
    data object Loading: DetailUiState
    data class Success(val field: FieldResponse): DetailUiState
    data class Error(val message: String?): DetailUiState
}