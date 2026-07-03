package com.tanh.datsan.utils

import android.util.Log
import java.time.Duration
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
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
            Log.d("DateUtil", "Error parsing review time: $isoString", e)
            "Ngày không xác định"
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

    /**
     * Tạo chuỗi ISO 8601 kèm Timezone cho Backend (VD: 2026-06-20T17:30:00.000+07:00)
     * @param date Format: yyyy-MM-dd
     * @param time Format: HH:mm
     */
    fun getFormattedStartTimeWithTimezone(date: String, time: String): String {
        return try {
            val localDateTime = LocalDateTime.parse("${date}T${time}:00")
            val zonedDateTime = localDateTime.atZone(zoneId)
            val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSXXX")
            zonedDateTime.format(formatter)
        } catch (e: Exception) {
            e.printStackTrace()
            "${date}T${time}:00.000+07:00"
        }
    }

    /**
     * Tính khoảng cách thời gian (phút) giữa 2 mốc giờ
     * @param startTime Format: HH:mm
     * @param endTime Format: HH:mm
     */
    fun calculateDurationMinutes(startTime: String, endTime: String): Int {
        return try {
            val start = LocalTime.parse(startTime)
            val end = LocalTime.parse(endTime)
            Duration.between(start, end).toMinutes().toInt()
        } catch (e: Exception) {
            e.printStackTrace()
            90
        }
    }

    fun formatDateDash(isoDate: String): String {
        return try {
            val parser = java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", java.util.Locale.getDefault()).apply {
                timeZone = java.util.TimeZone.getTimeZone("UTC")
            }
            val date = parser.parse(isoDate) ?: return isoDate
            java.text.SimpleDateFormat("dd/MM/yyyy HH:mm", java.util.Locale.getDefault()).format(date)
        } catch (e: Exception) { isoDate }
    }
}