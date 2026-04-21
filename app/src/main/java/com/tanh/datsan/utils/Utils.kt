package com.tanh.datsan.utils

import java.time.Instant
import java.time.LocalDate
import java.time.LocalTime
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

fun getUpcomingDates(todayLabel: String): List<Pair<String, String>> {
    val today = LocalDate.now()
    return (0..6).map {
        val d = today.plusDays(it.toLong())
        val label = if (it == 0) todayLabel else d.format(DateTimeFormatter.ofPattern("dd/MM"))
        label to d.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))
    }
}

fun generateSlots(open: String, close: String, dur: Int): List<String> {
    val slots = mutableListOf<String>()
    try {
        var curr = LocalTime.parse(open)
        val end = LocalTime.parse(close)
        while (curr.plusMinutes(dur.toLong())
                .isBefore(end) || curr.plusMinutes(dur.toLong()) == end
        ) {
            slots.add(curr.format(DateTimeFormatter.ofPattern("HH:mm")))
            curr = curr.plusMinutes(30)
        }
    } catch (e: Exception) {
        e.printStackTrace()
    }
    return slots
}