package com.tanh.datsan.data.network

import com.tanh.datsan.data.model.OverviewStatisticsResponse
import com.tanh.datsan.data.model.RecentBookingItem
import com.tanh.datsan.data.model.RevenueChartItem
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query

interface StatisticsApiService {
    @GET("payment/stats/overview")
    suspend fun getOverview(
        @Query("startDate") startDate: String?,
        @Query("endDate") endDate: String?,
        @Query("branchId") branchId: String?
    ): Response<OverviewStatisticsResponse>

    @GET("payment/chart")
    suspend fun getRevenueChart(
        @Query("year") year: Int?,
        @Query("branchId") branchId: String?
    ): Response<List<RevenueChartItem>>

    @GET("statistics/recent-bookings")
    suspend fun getRecentBookings(
        @Query("startDate") startDate: String?,
        @Query("endDate") endDate: String?,
        @Query("branchId") branchId: String?
    ): Response<List<RecentBookingItem>>
}
