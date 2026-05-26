package com.tanh.datsan.ui.auth

import android.widget.Toast
import androidx.compose.foundation.clickable
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
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.tanh.datsan.viewmodel.AuthUiEvent
import com.tanh.datsan.viewmodel.AuthViewModel

@Composable
fun VerifyOtpScreen(
    viewModel: AuthViewModel,
    email: String,
    isLoginMode: Boolean,
    onNavigateToHome: (String) -> Unit,
    onNavigateToLogin: ()-> Unit,
    onBackToLogin: () -> Unit
) {
    val context = LocalContext.current
    val primaryBlue = Color(0xFF1877F2)
    val isLoading by viewModel.isLoading.collectAsStateWithLifecycle()

    var otpCode by remember { mutableStateOf("") }
    
    // Quản lý đếm ngược (60 giây)
    var ticks by remember { mutableIntStateOf(60) }
    var isTimerRunning by remember { mutableStateOf(true) }

    LaunchedEffect(isTimerRunning) {
        if (isTimerRunning) {
            while (ticks > 0) {
                kotlinx.coroutines.delay(1000L)
                ticks--
            }
            isTimerRunning = false
        }
    }

    // Lắng nghe Event từ ViewModel
    LaunchedEffect(Unit) {
        viewModel.uiEvent.collect { event ->
            when (event) {
                is AuthUiEvent.ShowToast -> Toast.makeText(context, event.message, Toast.LENGTH_SHORT).show()
                is AuthUiEvent.NavigateToHome -> onNavigateToHome(event.message)
                is AuthUiEvent.NavigateBackToLogin -> onNavigateToLogin()
                is AuthUiEvent.OtpResent -> {
                    ticks = 60
                    isTimerRunning = true
                }
                else -> Unit
            }
        }
    }

    Column(
        modifier = Modifier.fillMaxSize().padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(text = "Xác thực OTP", color = primaryBlue, fontSize = 32.sp, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Chúng tôi đã gửi mã xác thực gồm 6 kí tự đến email:\n$email",
            color = Color.Gray, fontSize = 16.sp, modifier = Modifier.padding(bottom = 30.dp),
            textAlign = androidx.compose.ui.text.style.TextAlign.Center
        )

        OutlinedTextField(
            value = otpCode, onValueChange = { if (it.length <= 6) otpCode = it },
            label = { Text("Nhập mã OTP ") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text),
            modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp), singleLine = true
        )
        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = {
                if (otpCode.length < 6) {
                    Toast.makeText(context, "Vui lòng nhập đủ 6 số", Toast.LENGTH_SHORT).show()
                } else {
                    viewModel.verifyOtp(email, otpCode, isLoginMode)
                }
            },
            modifier = Modifier.fillMaxWidth().height(50.dp),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(containerColor = primaryBlue),
            enabled = !isLoading
        ) {
            if (isLoading) CircularProgressIndicator(modifier = Modifier.size(24.dp), color = Color.White, strokeWidth = 2.dp)
            else Text("Xác nhận", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.White)
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Hàng chứa nút Gửi lại và đếm ngược
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(text = "Chưa nhận được mã? ", color = Color.Gray, fontSize = 14.sp)
            if (isTimerRunning) {
                Text(
                    text = "Gửi lại sau ${ticks}s",
                    color = primaryBlue,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                )
            } else {
                Text(
                    text = "Gửi lại ngay",
                    color = primaryBlue,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    modifier = Modifier.clickable(enabled = !isLoading) {
                        viewModel.resendOtp(email, isLoginMode)
                    }
                )
            }
        }

        Spacer(modifier = Modifier.height(32.dp))
        TextButton(onClick = onBackToLogin) { Text("Quay lại", color = Color.Gray) }
    }
}