package com.tanh.datsan

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.tanh.datsan.navigation.AppNavigation

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Xử lý Deep Link nếu app được mở từ trạng thái tắt hoàn toàn
        handleDeepLinkIntent(intent)

        setContent {
             AppNavigation()
        }
    }

    // Xử lý Deep Link nếu app đang chạy ngầm và được gọi lên lại
    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        handleDeepLinkIntent(intent)
    }

    private fun handleDeepLinkIntent(intent: Intent?) {
        val uri = intent?.data
        if (uri != null && uri.scheme == "datsanapp" && uri.host == "google-callback") {
            // Lấy token từ URL (ví dụ: datsanapp://google-callback?accessToken=abc&refreshToken=xyz)
            val accessToken = uri.getQueryParameter("accessToken")
            val refreshToken = uri.getQueryParameter("refreshToken")

            if (!accessToken.isNullOrEmpty()) {
                Log.d("GOOGLE_LOGIN", "Thành công! Access Token: $accessToken")
                Toast.makeText(this, "Đăng nhập Google thành công!", Toast.LENGTH_LONG).show()

                // TODO: Bạn lưu token này vào SharedPreferences hoặc DataStore
                // TODO: Chuyển hướng người dùng vào màn hình Home
            } else {
                Toast.makeText(this, "Đăng nhập Google thất bại (Không có token)", Toast.LENGTH_SHORT).show()
            }
        }
    }
}

