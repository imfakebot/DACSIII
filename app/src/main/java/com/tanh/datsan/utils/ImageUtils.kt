package com.tanh.datsan.utils

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
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
        val uploadsIndex = this.indexOf("/uploads/")
        if (uploadsIndex != -1) {
            val path = this.substring(uploadsIndex)
            return "$baseUrl$path"
        }
        return this.replace(BuildConfig.API_BACKEND, baseUrl)
    }

    return if (this.startsWith("/uploads/")) {
        "$baseUrl$this"
    } else if (this.startsWith("uploads/")) {
        "$baseUrl/$this"
    } else if (this.startsWith("/")) {
        "$baseUrl/uploads$this"
    } else {
        "$baseUrl/uploads/$this"
    }
}

fun File.compressImage(context: Context): File {
    // 1. Đọc ảnh gốc
    val bitmap = BitmapFactory.decodeFile(this.absolutePath)

    // 2. Tạo một file tạm trong bộ nhớ đệm (Cache) để chứa ảnh đã nén
    val compressedFile = File(
        context.cacheDir,
        "avatar_compressed_${System.currentTimeMillis()}.jpg"
    )
    val outputStream = FileOutputStream(compressedFile)

    // 3. Nén ảnh thành chuẩn JPEG, chất lượng 70% (mắt thường không phân biệt được)
    bitmap.compress(Bitmap.CompressFormat.JPEG,70,outputStream)

    outputStream.flush()
    outputStream.close()

    return compressedFile
}