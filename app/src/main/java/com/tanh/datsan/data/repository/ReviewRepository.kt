package com.tanh.datsan.data.repository

import com.tanh.datsan.data.model.ReviewPaginateResponse
import com.tanh.datsan.data.network.ReviewApiService
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ReviewRepository @Inject constructor(
    private val reviewApiService: ReviewApiService
) {
    suspend fun getFieldReview(fieldId: String): ReviewPaginateResponse {
        return reviewApiService.getFieldReviews(fieldId)
    }
}