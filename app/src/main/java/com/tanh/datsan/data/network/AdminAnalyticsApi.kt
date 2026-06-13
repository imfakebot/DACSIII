package com.tanh.datsan.data.network

import com.tanh.datsan.data.model.RevenueChartItemDto
import com.tanh.datsan.data.model.StatsResponseDto
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Query

interface AdminAnalyticsApi {
    @GET("payment/stats/overview")
    suspend fun getOverviewStats(
        @Header("Authorization") token: String,
        @Query("startDate") startDate: String?,
        @Query("endDate") endDate: String?,
        @Query("branchId") branchId: String? = null
    ): Response<StatsResponseDto>

    @GET("payment/chart")
    suspend fun getRevenueChart(
        @Header("Authorization") token: String,
        @Query("year") year: Int,
        @Query("branchId") branchId: String? = null
    ): Response<List<RevenueChartItemDto>>
}
