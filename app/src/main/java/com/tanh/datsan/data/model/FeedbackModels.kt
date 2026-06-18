package com.tanh.datsan.data.model

import com.google.gson.annotations.SerializedName

data class FeedbackResponse(
    val id: String,
    val title: String?,
    val content: String?,
    val description: String?,
    val status: String,
    @SerializedName("created_at")
    val createdAt: String,
    @SerializedName("updated_at")
    val updatedAt: String,
    val user: FeedbackUser?,
    val responses: List<ChatMessage>?
)

data class FeedbackUser(
    val id: String,
    val email: String,
    @SerializedName("userProfile")
    val profile: FeedbackUserProfile?
)

data class FeedbackUserProfile(
    @SerializedName("fullName")
    val fullName: String?,
    @SerializedName("avatarUrl")
    val avatarUrl: String?
)

data class ChatMessage(
    val id: String,
    val content: String,
    @SerializedName("created_at")
    val createdAt: String,
    val responder: ChatResponder?
)

data class ChatResponder(
    val id: String,
    val fullName: String?,
    val avatarUrl: String?,
    val role: String // "user", "admin", "staff"
)

data class CreateFeedbackRequest(
    val title: String,
    val category: String,
    val content: String
)

data class ReplyFeedbackRequest(
    val content: String
)
