package com.tanh.datsan

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
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
        // Logo chữ Facebook (có thể sau này đổi thành logo app Đặt Sân của bạn)
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
            shape = RoundedCornerShape(12.dp),
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
            shape = RoundedCornerShape(12.dp),
            singleLine = true
        )

        Spacer(modifier = Modifier.height(20.dp))

        // Nút Đăng nhập
        Button(
            onClick = { /* Xử lý đăng nhập */ },
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1877F2))
        ) {
            Text("Đăng nhập", fontSize = 16.sp, fontWeight = FontWeight.Bold)
        }

        TextButton(onClick = { /* Quên mật khẩu */ }) {
            Text("Quên mật khẩu?", color = Color.Black)
        }

        // Thanh phân cách "Hoặc"
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            HorizontalDivider(modifier = Modifier.weight(1f), color = Color.LightGray)
            Text(
                text = "Hoặc",
                color = Color.Gray,
                modifier = Modifier.padding(horizontal = 16.dp),
                fontSize = 14.sp
            )
            HorizontalDivider(modifier = Modifier.weight(1f), color = Color.LightGray)
        }

        // Nút Đăng nhập bằng Google ĐÃ CÓ ICON
        OutlinedButton(
            onClick = { /* Xử lý logic gọi Google Sign-In */ },
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp),
            shape = RoundedCornerShape(12.dp),
            // Fix lỗi BorderStroke ở đây
            border = BorderStroke(width = 1.dp, color = Color.LightGray)
        ) {
            // Gọi logo Google từ file xml vừa tạo
            Image(
                painter = painterResource(id = R.drawable.ic_google),
                contentDescription = "Google Logo",
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))

            Text("Đăng nhập bằng Google", color = Color.Black, fontWeight = FontWeight.SemiBold)
        }

        Spacer(modifier = Modifier.height(30.dp))

        // Nút Tạo tài khoản mới
        OutlinedButton(
            onClick = onNavigateToRegister,
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp),
            shape = RoundedCornerShape(12.dp),
            // Fix lỗi BorderStroke ở đây luôn cho an toàn
            border = BorderStroke(width = 1.dp, color = Color.LightGray)
        ) {
            Text("Tạo tài khoản mới", color = Color(0xFF1877F2))
        }
    }
}

//@Preview(showBackground = true, showSystemUi = true)
//@Composable
//fun LoginPreview() {
//    LoginScreen(onNavigateToRegister = {})
//}