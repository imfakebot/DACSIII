package com.tanh.datsan.ui.admin

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tanh.datsan.data.model.BookingResponse
import com.tanh.datsan.data.model.OverviewStatisticsResponse
import com.tanh.datsan.data.model.RevenueChartItem
import com.tanh.datsan.data.repository.BookingRepository
import com.tanh.datsan.data.repository.StatisticsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone
import javax.inject.Inject

import com.tanh.datsan.ui.state.StatisticsUiState

@HiltViewModel
class StatisticsViewModel @Inject constructor(
    private val statisticsRepository: StatisticsRepository,
    private val bookingRepository: BookingRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(StatisticsUiState())
    val uiState: StateFlow<StatisticsUiState> = _uiState.asStateFlow()

    // Theo yêu cầu BE, định dạng phải chuẩn ISO 8601
    private fun formatToISO8601(date: Date?): String? {
        if (date == null) return null
        val sdf = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault())
        sdf.timeZone = TimeZone.getTimeZone("UTC")
        return sdf.format(date)
    }

    fun fetchStatistics(startDate: Date? = null, endDate: Date? = null, year: Int? = null, branchId: String? = null) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null, isForbidden = false)
            
            val isoStartDate = formatToISO8601(startDate)
            val isoEndDate = formatToISO8601(endDate)
            val currentYear = year ?: java.util.Calendar.getInstance().get(java.util.Calendar.YEAR)

            try {
                // Gọi song song cả 3 API
                val overviewResponse = statisticsRepository.getOverview(isoStartDate, isoEndDate, branchId)
                val chartResponse = statisticsRepository.getRevenueChart(currentYear, branchId)

                // Kiểm tra 403 Forbidden (Yêu cầu quyền Admin)
                if (overviewResponse.code() == 403 || chartResponse.code() == 403) {
                    _uiState.value = _uiState.value.copy(isLoading = false, isForbidden = true)
                    return@launch
                }

                val isOverviewSuccess = overviewResponse.isSuccessful
                val isChartSuccess = chartResponse.isSuccessful

                // Lấy danh sách booking gần đây (endpoint admin)
                val recentBookings = try {
                    bookingRepository.getAdminBookings(branchId = branchId, status = null, page = 1, limit = 10).data
                } catch (e: Exception) {
                    emptyList()
                }

                if (isOverviewSuccess && isChartSuccess) {
                    _uiState.value = _uiState.value.copy(
                        overview = overviewResponse.body(),
                        chartData = chartResponse.body() ?: emptyList(),
                        recentBookings = recentBookings,
                        isLoading = false
                    )
                } else {
                    val errorCode = if (!isOverviewSuccess) overviewResponse.code() else chartResponse.code()
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = "Lỗi khi tải dữ liệu thống kê (Code: $errorCode)"
                    )
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = "Lỗi kết nối: ${e.message}"
                )
            }
        }
    }
}
