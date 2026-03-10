package com.tanh.datsan

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun LoginScreen(onNavigateToRegister: () -> Unit) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Logo chữ Facebook
        Text(
            text = "Đăng nhập",
            color = Color(0xFF1877F2),
            fontSize = 45.sp,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(40.dp))

        // Ô nhập Email/SĐT
        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            label = { Text("Số điện thoại hoặc email") },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp), // Bo góc ô nhập liệu
            singleLine = true
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Ô nhập Mật khẩu
        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("Mật khẩu") },
            visualTransformation = PasswordVisualTransformation(),
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp), // Bo góc ô nhập liệu
            singleLine = true
        )

        Spacer(modifier = Modifier.height(20.dp))

        // Nút Đăng nhập bo góc mạnh
        Button(
            onClick = { /* Xử lý đăng nhập */ },
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp),
            shape = RoundedCornerShape(12.dp), // Bo góc nút bấm
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1877F2))
        ) {
            Text("Đăng nhập", fontSize = 16.sp, fontWeight = FontWeight.Bold)
        }

        TextButton(onClick = { /* Quên mật khẩu */ }) {
            Text("Quên mật khẩu?", color = Color.Black)
        }

        Spacer(modifier = Modifier.height(30.dp))

        // Nút Tạo tài khoản mới (thường nằm dưới cùng)
        OutlinedButton(
            onClick = onNavigateToRegister,
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp),
            shape = RoundedCornerShape(12.dp),
            border = ButtonDefaults.outlinedButtonBorder.copy(width = 1.dp)
        ) {
            Text("Tạo tài khoản mới", color = Color(0xFF1877F2))
        }
    }
}

// --- ĐOẠN CODE ĐỂ XEM TRƯỚC GIAO DIỆN ---
@Preview(showBackground = true, showSystemUi = true)
@Composable
fun LoginPreview() {
    // Gọi hàm LoginScreen ở đây, onNavigateToRegister để trống vì chỉ là xem trước
    LoginScreen(onNavigateToRegister = {})
}