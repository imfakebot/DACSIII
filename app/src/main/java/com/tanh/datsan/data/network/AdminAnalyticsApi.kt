package com.tanh.datsan.data.network

import com.tanh.datsan.data.model.RevenueChartItem
import com.tanh.datsan.data.model.StatsOverviewResponse
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query

interface AdminAnalyticsApi {
    @GET("payment/stats/overview")
    suspend fun getOverviewStats(
        @Query("startDate") startDate: String?,
        @Query("endDate") endDate: String?,
        @Query("branchId") branchId: String? = null
    ): Response<StatsOverviewResponse>

    @GET("payment/chart")
    suspend fun getRevenueChart(
        @Query("year") year: Int,
        @Query("branchId") branchId: String? = null
    ): Response<List<RevenueChartItem>>
}