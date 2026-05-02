package com.tanh.datsan.utils

import android.app.DownloadManager
import android.content.Context
import android.os.Environment
import android.util.Log
import android.widget.Toast

import androidx.core.net.toUri

object DownloadHelper {
    fun downLoadTicketPDF(
        context: Context,
        url: String,
        bookingCode: String,
        token: String
    ) {
        try {
            val request = DownloadManager.Request(url.toUri())
                .setTitle("Vé Đặt Sân - $bookingCode")
                .setDescription("Đang tải vé PDF...")
                .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
                .setDestinationInExternalPublicDir(
                    Environment.DIRECTORY_DOWNLOADS,
                    "VeDatSan_$bookingCode.pdf"
                )
                .setMimeType("application/pdf")

            if(token.isNotBlank()){
                request.addRequestHeader("Authorization","Bearer $token")
            }

            val dowmLoadManager =
                context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
            dowmLoadManager.enqueue(request)

            Log.d("Download helper", "Đạng tải vé xuống")
            Toast.makeText(context, "Đang tải vé xuống...", Toast.LENGTH_SHORT).show()
        } catch (e: Exception) {
            Log.d("Download helper", "ERROR: ${e.message}")
            Toast.makeText(context, "Lỗi tải file: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }
}