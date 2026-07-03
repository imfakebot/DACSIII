package com.tanh.datsan.data.model

import com.google.gson.annotations.SerializedName

data class OverviewStatisticsResponse(
    @SerializedName("revenue")
    val revenue: Double = 0.0,
    @SerializedName("transactions")
    val transactions: TransactionStats = TransactionStats()
)

data class TransactionStats(
    @SerializedName("pending")
    val pending: Int = 0,
    @SerializedName("completed")
    val completed: Int = 0,
    @SerializedName("failed")
    val failed: Int = 0
)

data class RevenueChartItem(
    @SerializedName("month")
    val month: Int,
    @SerializedName("revenue")
    val revenue: Double
)

// Có thể tái sử dụng BookingResponse có sẵn, nhưng định nghĩa tạm để tránh lỗi compile nếu chưa rõ schema
data class RecentBookingItem(
    val id: String,
    val totalAmount: Double,
    val status: String,
    val createdAt: String
)
