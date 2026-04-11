package com.tanh.datsan.data.repository

import com.tanh.datsan.data.model.Review
import com.tanh.datsan.data.network.RetrofitClient

class ReviewRepository {
    suspend fun getFieldReview(fieldId: String):List<Review>{
        return RetrofitClient.apiService.getFieldReview(fieldId).data
    }
}