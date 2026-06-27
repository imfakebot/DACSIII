package com.tanh.datsan.ui.state

import com.tanh.datsan.data.model.BookingResponse
import com.tanh.datsan.data.model.OverviewStatisticsResponse
import com.tanh.datsan.data.model.RevenueChartItem

data class StatisticsUiState(
    val overview: OverviewStatisticsResponse? = null,
    val chartData: List<RevenueChartItem> = emptyList(),
    val recentBookings: List<BookingResponse> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val isForbidden: Boolean = false // Xá»­ lÃ½ lá»—i 403 (KhÃ´ng Ä‘á»§ quyá»n)
)
