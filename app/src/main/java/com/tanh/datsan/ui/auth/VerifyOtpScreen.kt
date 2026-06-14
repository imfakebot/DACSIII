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

import androidx.compose.foundation.Image
import androidx.compose.ui.res.painterResource
import com.tanh.datsan.R

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

    AuthBackground {
        Column(
            modifier = Modifier.fillMaxSize().padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Image(
                painter = painterResource(id = R.drawable.ic_app_logo),
                contentDescription = null,
                modifier = Modifier.size(100.dp)
            )

            Spacer(modifier = Modifier.height(24.dp))

            Text(text = "Xác thực OTP", color = Color.White, fontSize = 32.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Chúng tôi đã gửi mã xác thực gồm 6 kí tự đến email:\n$email",
                color = Color.White.copy(alpha = 0.8f), fontSize = 16.sp, modifier = Modifier.padding(bottom = 30.dp),
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )

            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                color = Color.White.copy(alpha = 0.12f),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(alpha = 0.2f))
            ) {
                Column(modifier = Modifier.padding(24.dp)) {
                    TextField(
                        value = otpCode,
                        onValueChange = { if (it.length <= 6) otpCode = it },
                        placeholder = { Text("Nhập mã OTP ", color = Color.White.copy(alpha = 0.6f)) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text),
                        modifier = Modifier.fillMaxWidth(),
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = Color.Transparent,
                            unfocusedContainerColor = Color.Transparent,
                            focusedIndicatorColor = Color.White,
                            unfocusedIndicatorColor = Color.White.copy(alpha = 0.4f),
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White
                        ),
                        singleLine = true
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
                        modifier = Modifier.fillMaxWidth().height(56.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color.White, contentColor = Color(0xFF0F2027)),
                        enabled = !isLoading
                    ) {
                        if (isLoading) CircularProgressIndicator(modifier = Modifier.size(24.dp), color = Color(0xFF0F2027), strokeWidth = 2.dp)
                        else Text("XÁC NHẬN", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Hàng chứa nút Gửi lại và đếm ngược
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(text = "Chưa nhận được mã? ", color = Color.White.copy(alpha = 0.7f), fontSize = 14.sp)
                if (isTimerRunning) {
                    Text(
                        text = "Gửi lại sau ${ticks}s",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                } else {
                    Text(
                        text = "Gửi lại ngay",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        modifier = Modifier.clickable(enabled = !isLoading) {
                            viewModel.resendOtp(email, isLoginMode)
                        }
                    )
                }
            }

            Spacer(modifier = Modifier.height(32.dp))
            TextButton(onClick = onBackToLogin) { Text("Quay lại", color = Color.White.copy(alpha = 0.6f)) }
        }
    }
}