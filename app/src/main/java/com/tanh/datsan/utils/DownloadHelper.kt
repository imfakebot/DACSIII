package com.tanh.datsan.utils

import android.app.DownloadManager
import android.content.ContentValues
import android.content.Context
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import android.widget.Toast
import androidx.core.net.toUri
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.ResponseBody
import java.io.File
import java.io.FileOutputStream

object DownloadHelper {
    suspend fun saveTicketPdf(context: Context, responseBody: ResponseBody, bookingCode: String) : Uri?{
        return withContext(Dispatchers.IO) {
            val fileName = "VeDatSan_$bookingCode.pdf"
            try {
                val resolver = context.contentResolver
                val contentValues = ContentValues().apply {
                    put(MediaStore.MediaColumns.DISPLAY_NAME, fileName)
                    put(MediaStore.MediaColumns.MIME_TYPE, "application/pdf")
                    put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS)
                }

                val uri = resolver.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, contentValues)
                if (uri != null) {
                    resolver.openOutputStream(uri)?.use { outputStream ->
                        responseBody.byteStream().use { inputStream ->
                            inputStream.copyTo(outputStream)
                        }
                    }
                    Log.d("Download helper", "Lưu file thành công tại: $uri")
                } else {
                    throw Exception("Không thể tạo URI để lưu file")
                }
                uri
            } catch (e: Exception) {
                Log.e("Download helper", "Lỗi tải vé: ${e.message}", e)
              null
            }
        }
    }
}