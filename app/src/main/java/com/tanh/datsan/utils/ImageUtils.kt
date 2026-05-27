package com.tanh.datsan.utils

import android.content.Context
import android.net.Uri
import com.tanh.datsan.BuildConfig
import java.io.File
import java.io.FileOutputStream


fun String?.toFullImageUrl(): String {
    if(this.isNullOrEmpty()){
        return ""
    }else{
        val baseUrl = BuildConfig.API_BASE_URL.removeSuffix("/")
        return this.replace(BuildConfig.API_BACKEND, baseUrl)
    }
}

fun Uri.toFile(context: Context): File? {
    val contentResolver = context.contentResolver
    val fileName = "temp_avatar_${System.currentTimeMillis()}.jpg"
    val tempFile = File(context.cacheDir, fileName)
    return try {
        contentResolver.openInputStream(this)?.use { inputStream ->
            FileOutputStream(tempFile).use { outputStream ->
                inputStream.copyTo(outputStream)
            }
        }
        tempFile
    } catch (e: Exception) {
        e.printStackTrace()
        null
    }
}