package com.tanh.datsan.data.network

import com.tanh.datsan.data.model.CreateFeedbackRequest
import com.tanh.datsan.data.model.FeedbackPaginateResponse
import com.tanh.datsan.data.model.FeedbackResponse
import com.tanh.datsan.data.model.ReplyFeedbackRequest
import com.tanh.datsan.data.model.UpdateFeedbackStatusRequest
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

interface FeedbackApiService {
    // --- USER ENDPOINTS ---
    @POST("feedback")
    suspend fun createFeedback(@Body request: CreateFeedbackRequest): Response<FeedbackResponse>

    @GET("feedback/me")
    suspend fun getMyFeedbacks(): Response<List<FeedbackResponse>>

    // --- ADMIN ENDPOINTS ---
    @GET("feedback/management/all")
    suspend fun getAllFeedbacks(
        @Query("page") page: Int = 1,
        @Query("limit") limit: Int = 10,
        @Query("status") status: String? = null,
        @Query("type") type: String? = null
    ): Response<FeedbackPaginateResponse>

    @GET("feedback/management/{id}")
    suspend fun getAdminFeedbackDetail(@Path("id") id: String): Response<FeedbackResponse>

    @PATCH("feedback/management/{id}/status")
    suspend fun updateFeedbackStatus(
        @Path("id") id: String,
        @Body request: UpdateFeedbackStatusRequest
    ): Response<Any>

    @PATCH("feedback/management/{id}/reply")
    suspend fun replyFeedback(
        @Path("id") id: String,
        @Body request: ReplyFeedbackRequest
    ): Response<Any>

    @DELETE("feedback/management/{id}")
    suspend fun deleteFeedback(@Path("id") id: String): Response<Any>
}
