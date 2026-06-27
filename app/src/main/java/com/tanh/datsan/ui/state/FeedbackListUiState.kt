package com.tanh.datsan.ui.state

import com.tanh.datsan.data.model.FeedbackResponse

data class FeedbackListUiState(
    val feedbacks: List<FeedbackResponse> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
)
