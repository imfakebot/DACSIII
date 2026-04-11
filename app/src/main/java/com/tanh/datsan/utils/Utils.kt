package com.tanh.datsan.utils

import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

fun FormatReviewTime(isoString: String): String {
    return try {
        val instant = Instant.parse(isoString)
        val formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy").withZone(ZoneId.systemDefault())
        formatter.format(instant)
    } catch (e: Exception) {
        "Ngày không xác định"
    }
}