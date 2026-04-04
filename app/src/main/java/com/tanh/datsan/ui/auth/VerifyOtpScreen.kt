package com.tanh.datsan.ui.auth

import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.tanh.datsan.data.network.OtpRequest
import com.tanh.datsan.data.network.RetrofitClient
import kotlinx.coroutines.launch

@Composable
fun VerifyOtpScreen(
    email: String, // Nhận email từ trang trước truyền sang
    isLoginMode: Boolean, // True nếu từ Đăng nhập tới, False nếu từ Đăng ký tới
    onNavigateToHome: (String) -> Unit,
    onBackToLogin: () -> Unit
) {
    var otpCode by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }

    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    val primaryBlue = Color(0xFF1877F2)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(text = "Xác thực OTP", color = primaryBlue, fontSize = 32.sp, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Chúng tôi đã gửi mã xác thực gồm 6 số đến email:\n$email",
            color = Color.Gray,
            fontSize = 16.sp,
            modifier = Modifier.padding(bottom = 30.dp)
        )

        OutlinedTextField(
            value = otpCode,
            onValueChange = { if (it.length <= 6) otpCode = it },
            label = { Text("Nhập mã OTP (6 số)") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text),
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            singleLine = true
        )

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = {
                if (otpCode.length < 6) {
                    Toast.makeText(context, "Vui lòng nhập đủ 6 số", Toast.LENGTH_SHORT).show()
                    return@Button
                }

                isLoading = true
                scope.launch {
                    try {
                        val request = OtpRequest(email = email, verificationCode = otpCode)

                        // Gọi API tương ứng dựa vào việc user đang đăng nhập hay đăng ký
                        val response = if (isLoginMode) {
                            RetrofitClient.apiService.loginComplete(request)
                        } else {
                            RetrofitClient.apiService.registerComplete(request)
                        }

                        if (response.isSuccessful && response.body() != null) {
                            val result = response.body()!!

                            // SỬA Ở ĐÂY: Đổi access_token thành accessToken
                            if (result.accessToken != null) {
                                Toast.makeText(context, "Đăng nhập thành công!", Toast.LENGTH_SHORT).show()
                                // Thoát vòng lặp, vinh quang tiến vào Home!
                                onNavigateToHome("Success")
                            } else {
                                // Nút này giờ chỉ dành cho Đăng ký xong (vì đăng ký xong ko có token)
                                Toast.makeText(context, "Xác thực thành công, vui lòng đăng nhập", Toast.LENGTH_SHORT).show()
                                onBackToLogin()
                            }
                        } else {
                            val errorBody = response.errorBody()?.string()
                            Log.e("OTP_ERROR", "Lỗi: $errorBody")
                            Toast.makeText(context, "Mã OTP không đúng hoặc đã hết hạn!", Toast.LENGTH_SHORT).show()
                        }
                    } catch (e: Exception) {
                        Toast.makeText(context, "Lỗi mạng: ${e.message}", Toast.LENGTH_SHORT).show()
                    } finally {
                        isLoading = false
                    }
                }
            },
            modifier = Modifier.fillMaxWidth().height(50.dp),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(containerColor = primaryBlue),
            enabled = !isLoading
        ) {
            if (isLoading) {
                CircularProgressIndicator(modifier = Modifier.size(24.dp), color = Color.White, strokeWidth = 2.dp)
            } else {
                Text("Xác nhận", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.White)
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
        TextButton(onClick = onBackToLogin) {
            Text("Quay lại", color = Color.Gray)
        }
    }
}