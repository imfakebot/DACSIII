package com.tanh.datsan

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.tanh.datsan.navigation.AppNavigation

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Chỉ cần gọi giao diện chính của bạn lên là xong!
        setContent {
            AppNavigation()
        }
    }
}