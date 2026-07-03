package com.tanh.datsan.data.model

import com.google.gson.annotations.SerializedName

data class FeedbackResponse(
    val id: String,
    val title: String?,
    val content: String?,
    @SerializedName("category")
    val type: String?, // "bug", "suggestion", "complaint", "other"
    val images: List<String>?,
    val status: String,
    val adminReply: String?,
    @SerializedName("created_at")
    val createdAt: String,
    @SerializedName("updated_at")
    val updatedAt: String,
    val user: FeedbackUser?
)

data class FeedbackUser(
    val id: String?,
    val email: String,
    @SerializedName("userProfile")
    val profile: FeedbackUserProfile?
)

data class FeedbackUserProfile(
    @SerializedName("full_name", alternate = ["fullName"])
    val fullName: String?,
    @SerializedName("avatar_url", alternate = ["avatarUrl"])
    val avatarUrl: String?
)

data class CreateFeedbackRequest(
    val title: String,
    @SerializedName("category")
    val type: String,
    val content: String,
    val images: List<String>? = null
)

data class ReplyFeedbackRequest(
    val adminReply: String
)

data class UpdateFeedbackStatusRequest(
    val status: String
)

data class FeedbackPaginateResponse(
    val data: List<FeedbackResponse>,
    val total: Int,
    val page: Int,
    val limit: Int
)

data class AdminFeedbackUiState(
    val feedbacks: List<FeedbackResponse> = emptyList(),
    val currentFeedback: FeedbackResponse? = null,
    val isLoading: Boolean = false,
    val error: String? = null,
    val toastMessage: String? = null,


    val currentPage: Int = 1,
    val totalRecords: Int = 0,
    val currentStatusFilter: String? = null,
    val currentTypeFilter: String? = null  
)
