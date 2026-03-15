package com.tanh.datsan

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun HomeScreen(userName: String, onLogout: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // In ra dòng Hello = Tên người dùng
        Text(
            text = "Hello = $userName",
            fontSize = 30.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF1877F2)
        )

        Spacer(modifier = Modifier.height(30.dp))

        // Nút đăng xuất để quay lại màn hình Login
        Button(
            onClick = onLogout,
            colors = ButtonDefaults.buttonColors(containerColor = Color.Red)
        ) {
            Text("Đăng xuất", color = Color.White)
        }
    }
}