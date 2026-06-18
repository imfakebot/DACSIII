package com.tanh.datsan.data.repository

import com.tanh.datsan.data.network.StatisticsApiService
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class StatisticsRepository @Inject constructor(
    private val apiService: StatisticsApiService
) {
    suspend fun getOverview(startDate: String?, endDate: String?, branchId: String?) =
        apiService.getOverview(startDate, endDate, branchId)

    suspend fun getRevenueChart(year: Int?, branchId: String?) =
        apiService.getRevenueChart(year, branchId)

    suspend fun getRecentBookings(startDate: String?, endDate: String?, branchId: String?) =
        apiService.getRecentBookings(startDate, endDate, branchId)
}
