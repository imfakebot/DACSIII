package com.tanh.datsan.data.network

import com.tanh.datsan.data.model.AdminReplyDto
import com.tanh.datsan.data.model.CreateReviewDto
import com.tanh.datsan.data.model.MessageResponseDto
import com.tanh.datsan.data.model.Review
import com.tanh.datsan.data.model.ReviewPaginateResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

interface ReviewApiService {

    // ── USER ──────────────────────────────────────────────────────────────────

    /** Tạo đánh giá mới. Backend tự check booking.status == completed */
    @POST("review")
    suspend fun createReview(
        @Body dto: CreateReviewDto
    ): Review

    /** Xem tất cả đánh giá của một sân, kèm averageRating trong meta */
    @GET("review/field/{fieldId}")
    suspend fun getFieldReview(
        @Path("fieldId") fieldId: String,
        @Query("page") page: Int = 1,
        @Query("limit") limit: Int = 20
    ): ReviewPaginateResponse

    /** Xem lại những đánh giá mình đã từng viết */
    @GET("review/my-reviews")
    suspend fun getMyReviews(
        @Query("page") page: Int = 1,
        @Query("limit") limit: Int = 20
    ): ReviewPaginateResponse

    // ── ADMIN / MANAGER ───────────────────────────────────────────────────────

    /** Xem toàn bộ đánh giá trong hệ thống, filter theo branchId / rating */
    @GET("review/management/all")
    suspend fun getAdminReviews(
        @Query("branchId") branchId: String? = null,
        @Query("rating") rating: Int? = null,
        @Query("page") page: Int = 1,
        @Query("limit") limit: Int = 20
    ): ReviewPaginateResponse

    /** Xóa đánh giá vi phạm */
    @DELETE("review/{id}")
    suspend fun deleteReview(
        @Path("id") reviewId: String
    ): Response<MessageResponseDto>

    /** Phản hồi đánh giá của khách hàng */
    @PATCH("review/{id}/reply")
    suspend fun replyReview(
        @Path("id") reviewId: String,
        @Body dto: AdminReplyDto
    ): Review
}