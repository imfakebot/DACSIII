package com.tanh.datsan.utils

import com.tanh.datsan.BuildConfig


fun String?.toFullImageUrl(): String {
    if (this.isNullOrEmpty()) {
        return ""
    }
    
    val baseUrl = BuildConfig.API_BASE_URL.removeSuffix("/")
    
    if (this.startsWith("http://") || this.startsWith("https://")) {
        return this.replace(BuildConfig.API_BACKEND, baseUrl)
    }
    
    return if (this.startsWith("/")) {
        "$baseUrl$this"
    } else {
        "$baseUrl/$this"
    }
}