package com.tanh.datsan.utils

import android.content.Context
import android.net.Uri
import java.io.File
import java.io.FileOutputStream
import com.tanh.datsan.BuildConfig

fun Uri.toFile(context: Context): File? {
    return try {
        val inputStream = context.contentResolver.openInputStream(this) ?: return null
        val file = File(context.cacheDir, "temp_image_${System.currentTimeMillis()}.jpg")
        val outputStream = FileOutputStream(file)
        inputStream.use { input ->
            outputStream.use { output ->
                input.copyTo(output)
            }
        }
        file
    } catch (e: Exception) {
        null
    }
}

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