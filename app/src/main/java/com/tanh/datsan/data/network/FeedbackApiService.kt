package com.tanh.datsan.data.network

import com.tanh.datsan.data.model.CreateFeedbackRequest
import com.tanh.datsan.data.model.FeedbackResponse
import com.tanh.datsan.data.model.ReplyFeedbackRequest
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path

interface FeedbackApiService {
    @POST("feedbacks")
    suspend fun createFeedback(@Body request: CreateFeedbackRequest): Response<FeedbackResponse>

    @GET("feedbacks/{id}")
    suspend fun getFeedbackDetail(@Path("id") id: String): Response<FeedbackResponse>

    @POST("feedbacks/{id}/reply")
    suspend fun replyFeedback(
        @Path("id") id: String,
        @Body request: ReplyFeedbackRequest
    ): Response<Any>

    @GET("feedbacks/me")
    suspend fun getMyFeedbacks(): Response<List<FeedbackResponse>>
}
