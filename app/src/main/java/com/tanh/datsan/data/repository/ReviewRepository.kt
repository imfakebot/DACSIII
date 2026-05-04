package com.tanh.datsan.data.repository

import com.tanh.datsan.data.model.Review
import com.tanh.datsan.data.network.ReviewApiService
import javax.inject.Inject

// Thêm @Singleton để Hilt chỉ tạo instance này 1 lần duy nhất trong toàn app
class ReviewRepository @Inject constructor(
    private val reviewApiService: ReviewApiService
) {
    suspend fun getFieldReview(fieldId: String): List<Review> {

        // Dùng toán tử Elvis (?: emptyList()) để nếu .data bị null thì trả về danh sách rỗng, tránh Crash app.
        return reviewApiService.getFieldReviews(fieldId).data ?: emptyList()
    }
}