package com.tanh.datsan.data.network

import com.tanh.datsan.data.model.ReviewPaginateResponse
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface ReviewApiService {

    // Lưu ý: Sửa lại đường dẫn "fields/{fieldId}/reviews" sao cho khớp chính xác với Backend của bạn
    @GET("fields/{fieldId}/reviews")
    suspend fun getFieldReviews(
        @Path("fieldId") fieldId: String,
        @Query("page") page: Int = 1, // Hỗ trợ phân trang luôn cho mượt
        @Query("limit") limit: Int = 10
    ): ReviewPaginateResponse
}