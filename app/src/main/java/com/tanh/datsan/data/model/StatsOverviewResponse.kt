package com.tanh.datsan.data.model

import com.google.gson.annotations.SerializedName

data class StatsOverviewResponse(
    @SerializedName("revenue") val revenue: Double,
    @SerializedName("transactions") val transactions: Map<String, Int>
)
