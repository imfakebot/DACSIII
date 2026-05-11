package com.tanh.datsan.utils

import com.tanh.datsan.BuildConfig


fun String?.toFullImageUrl(): String {
    if(this.isNullOrEmpty()){
        return ""
    }else{
        val baseUrl = BuildConfig.API_BASE_URL.removeSuffix("/")
        return this.replace(BuildConfig.API_BACKEND, baseUrl)
    }
}