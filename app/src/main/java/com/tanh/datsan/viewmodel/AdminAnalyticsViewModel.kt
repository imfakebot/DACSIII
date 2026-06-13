package com.tanh.datsan.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tanh.datsan.data.model.RevenueChartItem
import com.tanh.datsan.data.model.StatsOverviewResponse
import com.tanh.datsan.data.network.AdminAnalyticsApi
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.util.Calendar
import javax.inject.Inject

@HiltViewModel
class AdminAnalyticsViewModel @Inject constructor(
    private val api: AdminAnalyticsApi
) : ViewModel() {

    private val _overviewStats = MutableStateFlow<StatsOverviewResponse?>(null)
    val overviewStats: StateFlow<StatsOverviewResponse?> = _overviewStats

    private val _chartData = MutableStateFlow<List<RevenueChartItem>>(emptyList())
    val chartData: StateFlow<List<RevenueChartItem>> = _chartData

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error

    val totalBookings = MutableStateFlow(0)
    val cancellationRate = MutableStateFlow(0.0)

    fun fetchAnalytics(startDate: String?, endDate: String?, branchId: String? = null) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            try {
                val overviewRes = api.getOverviewStats(
                    startDate = startDate,
                    endDate = endDate,
                    branchId = branchId
                )

                if (overviewRes.isSuccessful) {
                    val body = overviewRes.body()
                    _overviewStats.value = body
                    if (body != null) {
                        calculateMetrics(body)
                    }
                } else {
                    _error.value = "Lỗi khi tải tổng quan: \${overviewRes.message()}"
                }

                val currentYear = Calendar.getInstance().get(Calendar.YEAR)
                val chartRes = api.getRevenueChart(
                    year = currentYear,
                    branchId = branchId
                )

                if (chartRes.isSuccessful) {
                    _chartData.value = chartRes.body() ?: emptyList()
                } else {
                    _error.value = "Lỗi khi tải biểu đồ: \${chartRes.message()}"
                }

            } catch (e: Exception) {
                _error.value = e.message ?: "Đã có lỗi xảy ra"
            } finally {
                _isLoading.value = false
            }
        }
    }

    private fun calculateMetrics(response: StatsOverviewResponse) {
        var bookings = 0
        var cancelledOrFailed = 0
        
        response.transactions.forEach { (status, count) ->
            bookings += count
            if (status == "failed" || status == "cancelled") { 
                cancelledOrFailed += count
            }
        }
        
        totalBookings.value = bookings
        cancellationRate.value = if (bookings > 0) {
            (cancelledOrFailed.toDouble() / bookings) * 100
        } else {
            0.0
        }
    }
}