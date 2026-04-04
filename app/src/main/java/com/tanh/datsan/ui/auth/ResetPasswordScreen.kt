package com.tanh.datsan.ui.auth

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.tanh.datsan.data.network.ResetPasswordRequest
import com.tanh.datsan.data.network.RetrofitClient
import kotlinx.coroutines.launch

@Composable
fun ResetPasswordScreen(
    emailSent: String, // Vẫn giữ để hiển thị thông báo, nhưng KHÔNG gửi lên server nữa
    onNavigateBackToLogin: () -> Unit
) {
    // ĐỔI 1: Đổi tên biến otp thành token
    var token by remember { mutableStateOf("") }
    var newPassword by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }

    var newPasswordVisible by remember { mutableStateOf(false) }
    var confirmPasswordVisible by remember { mutableStateOf(false) }

    var isLoading by remember { mutableStateOf(false) }

    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // ĐỔI 2: Cập nhật lại Text hiển thị
        Text("Đặt lại mật khẩu", color = Color(0xFF1877F2), fontSize = 32.sp, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Vui lòng nhập mã xác nhận (Token) đã được gửi đến email:\n$emailSent",
            color = Color.Gray,
            fontSize = 14.sp,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(30.dp))

        // ĐỔI 3: Ô nhập Token - Bỏ chặn 6 số, cho phép nhập chuỗi (Text)
        OutlinedTextField(
            value = token,
            onValueChange = { token = it }, // Nhận mọi ký tự chữ và số
            label = { Text("Mã xác nhận (Token)") },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text), // Bàn phím chữ bình thường
            singleLine = true
        )
        Spacer(modifier = Modifier.height(12.dp))

        OutlinedTextField(
            value = newPassword,
            onValueChange = { newPassword = it },
            label = { Text("Mật khẩu mới (tối thiểu 8 ký tự)") }, // Nhắc nhở user
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            visualTransformation = if (newPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
            trailingIcon = {
                val image = if (newPasswordVisible) Icons.Filled.Visibility else Icons.Filled.VisibilityOff
                IconButton(onClick = { newPasswordVisible = !newPasswordVisible }) {
                    Icon(imageVector = image, contentDescription = "Toggle password visibility")
                }
            },
            singleLine = true
        )
        Spacer(modifier = Modifier.height(12.dp))

        OutlinedTextField(
            value = confirmPassword,
            onValueChange = { confirmPassword = it },
            label = { Text("Xác nhận mật khẩu") },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            visualTransformation = if (confirmPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
            trailingIcon = {
                val image = if (confirmPasswordVisible) Icons.Filled.Visibility else Icons.Filled.VisibilityOff
                IconButton(onClick = { confirmPasswordVisible = !confirmPasswordVisible }) {
                    Icon(imageVector = image, contentDescription = "Toggle confirm password visibility")
                }
            },
            singleLine = true
        )
        Spacer(modifier = Modifier.height(30.dp))

        Button(
            onClick = {
                val cleanToken = token.trim()

                // ĐỔI 4: Check điều kiện Token và Mật khẩu (>= 8 ký tự)
                if (cleanToken.isEmpty()) {
                    Toast.makeText(context, "Vui lòng nhập mã xác nhận", Toast.LENGTH_SHORT).show()
                    return@Button
                }
                if (newPassword.length < 8) { // Sửa từ 6 thành 8
                    Toast.makeText(context, "Mật khẩu phải từ 8 ký tự trở lên", Toast.LENGTH_SHORT).show()
                    return@Button
                }
                if (newPassword != confirmPassword) {
                    Toast.makeText(context, "Mật khẩu xác nhận không khớp", Toast.LENGTH_SHORT).show()
                    return@Button
                }

                isLoading = true
                scope.launch {
                    try {
                        // ĐỔI 5: Gửi request chỉ gồm Token và Password
                        val request = ResetPasswordRequest(
                            token = cleanToken,
                            newPassword = newPassword
                        )
                        val response = RetrofitClient.apiService.resetPassword(request)

                        if (response.isSuccessful) {
                            Toast.makeText(context, "Đổi mật khẩu thành công! Vui lòng đăng nhập lại.", Toast.LENGTH_LONG).show()
                            onNavigateBackToLogin()
                        } else {
                            // Xử lý lỗi từ server (ví dụ: Token sai, token hết hạn)
                            Toast.makeText(context, "Mã xác nhận không hợp lệ hoặc đã hết hạn", Toast.LENGTH_SHORT).show()
                        }
                    } catch (e: Exception) {
                        Toast.makeText(context, "Lỗi kết nối mạng: ${e.message}", Toast.LENGTH_SHORT).show()
                    } finally {
                        isLoading = false
                    }
                }
            },
            modifier = Modifier.fillMaxWidth().height(50.dp),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1877F2)),
            enabled = !isLoading
        ) {
            if (isLoading) {
                CircularProgressIndicator(modifier = Modifier.size(24.dp), color = Color.White, strokeWidth = 2.dp)
            } else {
                Text("Xác nhận đổi mật khẩu", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.White)
            }
        }
    }
}