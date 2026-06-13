package com.tanh.datsan.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.tanh.datsan.core.TokenManager
import com.tanh.datsan.data.model.RevenueChartItemDto
import com.tanh.datsan.data.model.StatsResponseDto
import com.tanh.datsan.data.network.RetrofitClient
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import java.util.Calendar

class AdminAnalyticsViewModel(application: Application) : AndroidViewModel(application) {

    private val tokenManager = TokenManager(application)
    private val api = RetrofitClient.adminAnalyticsApi

    private val _overviewStats = MutableStateFlow<StatsResponseDto?>(null)
    val overviewStats: StateFlow<StatsResponseDto?> = _overviewStats

    private val _chartData = MutableStateFlow<List<RevenueChartItemDto>>(emptyList())
    val chartData: StateFlow<List<RevenueChartItemDto>> = _chartData

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error

    fun fetchAnalytics(startDate: String?, endDate: String?, branchId: String? = null) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            try {
                val token = tokenManager.getToken.firstOrNull()
                if (token.isNullOrEmpty()) {
                    _error.value = "Chưa đăng nhập hoặc token hết hạn"
                    return@launch
                }
                
                val bearerToken = "Bearer \$token"

                val overviewRes = api.getOverviewStats(
                    token = bearerToken,
                    startDate = startDate,
                    endDate = endDate,
                    branchId = branchId
                )

                if (overviewRes.isSuccessful) {
                    _overviewStats.value = overviewRes.body()
                } else {
                    _error.value = "Lỗi khi tải tổng quan: \${overviewRes.message()}"
                }

                val currentYear = Calendar.getInstance().get(Calendar.YEAR)
                val chartRes = api.getRevenueChart(
                    token = bearerToken,
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
}
