package com.tanh.datsan.data.repository

import com.tanh.datsan.core.SocketManager
import com.tanh.datsan.data.model.CreateFeedbackRequest
import com.tanh.datsan.data.model.ReplyFeedbackRequest
import com.tanh.datsan.data.network.FeedbackApiService
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FeedbackRepository @Inject constructor(
    private val apiService: FeedbackApiService,
    private val socketManager: SocketManager
) {
    val realTimeMessages = socketManager.messageFlow
    val notifications = socketManager.notificationFlow

    suspend fun createFeedback(title: String, description: String) =
        apiService.createFeedback(CreateFeedbackRequest(title, description))

    suspend fun getFeedbackDetail(id: String) = apiService.getFeedbackDetail(id)

    suspend fun getMyFeedbacks() = apiService.getMyFeedbacks()

    suspend fun replyFeedback(id: String, content: String) =
        apiService.replyFeedback(id, ReplyFeedbackRequest(content))

    fun connectSocket() = socketManager.connect()
    fun disconnectSocket() = socketManager.disconnect()
    fun joinRoom(feedbackId: String) = socketManager.joinFeedbackRoom(feedbackId)
    fun leaveRoom(feedbackId: String) = socketManager.leaveFeedbackRoom(feedbackId)
}
