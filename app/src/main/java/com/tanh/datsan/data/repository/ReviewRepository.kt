package com.tanh.datsan.data.repository

import com.tanh.datsan.data.model.Review
import com.tanh.datsan.data.network.ReviewApiService
import javax.inject.Inject

class ReviewRepository @Inject constructor(
    private val ReviewApiService: ReviewApiService
) {
    suspend fun getFieldReview(fieldId: String): List<Review> {
        return ReviewApiService.getFieldReview(fieldId).data
    }
}