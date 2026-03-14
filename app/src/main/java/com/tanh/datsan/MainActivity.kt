package com.tanh.datsan

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.ui.Modifier
import com.tanh.datsan.ui.theme.DatsanTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            DatsanTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    // Bọc AppNavigation trong Box để sử dụng innerPadding
                    // Điều này giúp giao diện không bị chìm dưới thanh trạng thái (status bar)
                    Box(modifier = Modifier.padding(innerPadding)) {
                        AppNavigation()
                    }
                }
            }
        }
    }
}