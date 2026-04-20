package com.tanh.datsan.viewmodel

import com.tanh.datsan.data.model.FieldResponse

sealed interface DetailUiState{
    data object Loading: DetailUiState
    data class Success(val field: FieldResponse): DetailUiState
    data class Error(val message: String?): DetailUiState
}