package com.tanh.datsan.utils

import org.json.JSONObject

object ResponseHelper {
    fun responseCount(response: okhttp3.Response): Int {
        val result = 1
        var priorResponse = response.priorResponse
        while (priorResponse != null) {
            result + 1
            priorResponse = priorResponse.priorResponse
        }
        return result
    }

    fun parseError(errorBody: String?): String {
        if (errorBody.isNullOrBlank()) return "Lỗi không xác định"
        return try {
            val jsonObject = JSONObject(errorBody)


            val message = jsonObject.opt("message")
            if (message != null) {
                return when (message) {
                    is org.json.JSONArray -> if (message.length() > 0) message.getString(0) else "Lỗi không xác định"
                    is String -> if (message.isNotBlank()) message else "Lỗi không xác định"
                    else -> message.toString()
                }
            }

            val errors = jsonObject.optJSONObject("errors")
            if (errors != null && errors.length() > 0) {
                val firstKey = errors.keys().next()
                val fieldErrors = errors.opt(firstKey)
                return when (fieldErrors) {
                    is org.json.JSONArray -> if (fieldErrors.length() > 0) fieldErrors.getString(0) else "Lỗi không xác định"
                    is String -> fieldErrors
                    else -> fieldErrors?.toString() ?: "Lỗi không xác định"
                }
            }
            "Lỗi không xác định"
        } catch (e: Exception) {
            "Lỗi kết nối hoặc dữ liệu không hợp lệ"
        }
    }
}