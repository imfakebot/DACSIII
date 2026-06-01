package com.tanh.datsan.utils

import android.content.Context
import android.net.Uri
import com.tanh.datsan.BuildConfig
import java.io.File
import java.io.FileOutputStream


fun String?.toFullImageUrl(): String {
    if (this.isNullOrEmpty()) return ""

    val baseUrl = BuildConfig.API_BASE_URL.trim().removeSuffix("/")
    
    // Nếu nó đã là URL tuyệt đối (chứa http), ta chỉ lấy phần path sau domain
    var path = this
    if (this.startsWith("http")) {
        val protocolIndex = this.indexOf("://")
        if (protocolIndex != -1) {
            val firstSlashAfterHost = this.indexOf("/", protocolIndex + 3)
            if (firstSlashAfterHost != -1) {
                path = this.substring(firstSlashAfterHost)
            }
        }
    }

    // Làm sạch path: Xóa tất cả dấu / ở đầu/cuối và khử dấu // ở giữa
    var rawPath = path.trim()
    while (rawPath.startsWith("/")) {
        rawPath = rawPath.removePrefix("/")
    }
    while (rawPath.endsWith("/")) {
        rawPath = rawPath.removeSuffix("/")
    }
    while (rawPath.contains("//")) {
        rawPath = rawPath.replace("//", "/")
    }

    // Ghép lại với BaseUrl chuẩn của App
    return "$baseUrl/$rawPath"
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