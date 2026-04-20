package com.tanh.datsan.data.network

import com.tanh.datsan.data.model.ReviewPaginateResponse
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface ReviewApiService {
    @GET("review/field/{fieldId}")
    suspend fun getFieldReview(
        @Path("fieldId") fieldId: String,
        @Query("page") page: Int = 1,
        @Query("limit") limit: Int = 50
    ): ReviewPaginateResponse
}