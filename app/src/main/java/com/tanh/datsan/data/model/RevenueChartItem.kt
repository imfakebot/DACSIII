package com.tanh.datsan.data.model

import com.google.gson.annotations.SerializedName

data class RevenueChartItem(
    @SerializedName("month") val month: Int,
    @SerializedName("revenue") val revenue: Double
)
