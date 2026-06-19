package com.tanh.datsan.data.repository

import com.tanh.datsan.data.network.StatisticsApiService
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class StatisticsRepository @Inject constructor(
    private val staticApiService: StatisticsApiService
) {
    suspend fun getOverview(startDate: String?, endDate: String?, branchId: String?) =
        staticApiService.getOverview(startDate, endDate, branchId)

    suspend fun getRevenueChart(year: Int?, branchId: String?) =
        staticApiService.getRevenueChart(year, branchId)
}
