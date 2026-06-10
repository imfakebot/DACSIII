package com.tanh.datsan.utils

import java.time.Instant
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter

object DateUtil {
    private val zoneId = ZoneId.systemDefault()
    private val dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy")
    private val timeFormatter = DateTimeFormatter.ofPattern("HH:mm")
    private val dateTimeFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")

    fun formatReviewTime(isoString: String): String {
        return try {
            val instant = Instant.parse(isoString)
            dateFormatter.withZone(zoneId).format(instant)
        } catch (e: Exception) {
            "Ngày không xác định"
        }
    }

    fun formatBookingTimeRange(startIso: String, endIso: String): String {
        return try {
            val start = Instant.parse(startIso).atZone(zoneId)
            val end = Instant.parse(endIso).atZone(zoneId)

            "${start.format(timeFormatter)} - ${end.format(timeFormatter)} | ${
                start.format(
                    dateFormatter
                )
            }"
        } catch (e: Exception) {
            "Thời gian không xác định"
        }
    }

    fun formatNotificationTime(isoString: String): String {
        return try {
            val instant = Instant.parse(isoString)
            dateTimeFormatter.withZone(zoneId).format(instant)
        } catch (e: Exception) {
            isoString
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
}