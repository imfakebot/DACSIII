package com.tanh.datsan.data.repository

import com.tanh.datsan.data.model.AdminReplyDto
import com.tanh.datsan.data.model.CreateReviewDto
import com.tanh.datsan.data.model.ReviewPaginateResponse
import com.tanh.datsan.data.network.ReviewApiService
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ReviewRepository @Inject constructor(
    private val reviewApiService: ReviewApiService
) {
    // ── USER ──────────────────────────────────────────────────────────────────

    /** Tạo đánh giá. Backend check booking.status == completed */
    suspend fun createReview(dto: CreateReviewDto) =
        reviewApiService.createReview(dto)

    /** Danh sách đánh giá của một sân + averageRating trong meta */
    suspend fun getFieldReview(
        fieldId: String,
        page: Int = 1,
        limit: Int = 20
    ): ReviewPaginateResponse =
        reviewApiService.getFieldReview(fieldId, page, limit)

    /** Đánh giá mà user hiện tại đã viết */
    suspend fun getMyReviews(
        page: Int = 1,
        limit: Int = 20
    ): ReviewPaginateResponse =
        reviewApiService.getMyReviews(page, limit)

    // ── ADMIN / MANAGER ───────────────────────────────────────────────────────

    /** Toàn bộ đánh giá trong hệ thống (có thể filter branchId, rating) */
    suspend fun getAdminReviews(
        branchId: String? = null,
        rating: Int? = null,
        page: Int = 1,
        limit: Int = 20
    ): ReviewPaginateResponse =
        reviewApiService.getAdminReviews(branchId, rating, page, limit)

    /** Xóa đánh giá vi phạm */
    suspend fun deleteReview(reviewId: String) =
        reviewApiService.deleteReview(reviewId)

    /** Phản hồi đánh giá của khách hàng */
    suspend fun replyReview(reviewId: String, reply: String) =
        reviewApiService.replyReview(reviewId, AdminReplyDto(reply))
}