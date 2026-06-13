package com.tanh.datsan.utils

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
}