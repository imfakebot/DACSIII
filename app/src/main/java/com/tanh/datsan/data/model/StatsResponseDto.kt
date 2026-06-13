package com.tanh.datsan.data.model

data class StatsResponseDto(
    val revenue: Double,
    val transactions: Map<String, Int>
)
