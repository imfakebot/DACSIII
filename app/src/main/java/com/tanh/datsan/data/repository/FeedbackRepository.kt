package com.tanh.datsan.data.repository

import com.tanh.datsan.data.model.CreateFeedbackRequest
import com.tanh.datsan.data.model.ReplyFeedbackRequest
import com.tanh.datsan.data.network.FeedbackApiService
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FeedbackRepository @Inject constructor(
    private val apiService: FeedbackApiService
) {
    // --- USER ENDPOINTS ---
    suspend fun createFeedback(title: String, type: String, content: String, images: List<String>? = null) =
        apiService.createFeedback(CreateFeedbackRequest(title, type, content, images))

    suspend fun getMyFeedbacks() = apiService.getMyFeedbacks()

    // --- ADMIN ENDPOINTS ---
    suspend fun getAllFeedbacks(status: String? = null, type: String? = null) =
        apiService.getAllFeedbacks(status, type)

    suspend fun getAdminFeedbackDetail(id: String) = apiService.getAdminFeedbackDetail(id)

    suspend fun replyFeedback(id: String, adminReply: String) =
        apiService.replyFeedback(id, ReplyFeedbackRequest(adminReply))
}
