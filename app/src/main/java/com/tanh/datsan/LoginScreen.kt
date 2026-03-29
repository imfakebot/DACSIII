package com.tanh.datsan

import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.credentials.CredentialManager
import androidx.credentials.GetCredentialRequest
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import kotlinx.coroutines.launch

@Composable
fun LoginScreen(
    onNavigateToRegister: () -> Unit,
    onNavigateToOtp: (String) -> Unit,
    onNavigateToResetPassword: (String) -> Unit,
    onNavigateToHome: (String) -> Unit
) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }

    var showForgotDialog by remember { mutableStateOf(false) }
    var forgotEmail by remember { mutableStateOf("") }
    var isLoadingForgot by remember { mutableStateOf(false) }
    var isLoadingLogin by remember { mutableStateOf(false) }

    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val primaryBlue = Color(0xFF1877F2)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // --- TIÊU ĐỀ ---
        Text("Đăng nhập", fontSize = 28.sp, fontWeight = FontWeight.Bold, color = primaryBlue)
        Spacer(modifier = Modifier.height(32.dp))

        // --- NHẬP EMAIL ---
        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            label = { Text("Email") },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            singleLine = true
        )
        Spacer(modifier = Modifier.height(16.dp))

        // --- NHẬP MẬT KHẨU ---
        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("Mật khẩu") },
            visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
            trailingIcon = {
                val image = if (passwordVisible) Icons.Filled.Visibility else Icons.Filled.VisibilityOff
                IconButton(onClick = { passwordVisible = !passwordVisible }) {
                    Icon(imageVector = image, contentDescription = "Toggle password visibility")
                }
            },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            singleLine = true
        )

        // --- QUÊN MẬT KHẨU ---
        Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.CenterEnd) {
            Text(
                text = "Quên mật khẩu?",
                color = primaryBlue,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier
                    .padding(vertical = 8.dp)
                    .clickable { showForgotDialog = true }
            )
        }
        Spacer(modifier = Modifier.height(16.dp))

        // --- NÚT ĐĂNG NHẬP THƯỜNG ---
        Button(
            onClick = {
                val validEmail = email.trim()
                if (validEmail.isEmpty() || password.isEmpty()) {
                    Toast.makeText(context, "Vui lòng nhập đầy đủ thông tin", Toast.LENGTH_SHORT).show()
                    return@Button
                }

                isLoadingLogin = true
                scope.launch {
                    try {
                        val response = RetrofitClient.instance.loginInitiate(
                            LoginRequest(email = validEmail, password = password)
                        )

                        if (response.isSuccessful) {
                            Toast.makeText(context, "Đã gửi mã OTP đến email!", Toast.LENGTH_SHORT).show()
                            onNavigateToOtp(validEmail) // Chuyển sang OTP
                        } else {
                            val errorBody = response.errorBody()?.string() ?: "Sai thông tin"
                            Toast.makeText(context, "Đăng nhập thất bại: $errorBody", Toast.LENGTH_LONG).show()
                        }
                    } catch (e: Exception) {
                        Toast.makeText(context, "Lỗi kết nối mạng: ${e.message}", Toast.LENGTH_SHORT).show()
                    } finally {
                        isLoadingLogin = false
                    }
                }
            },
            modifier = Modifier.fillMaxWidth().height(50.dp),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(containerColor = primaryBlue),
            enabled = !isLoadingLogin
        ) {
            if (isLoadingLogin) {
                CircularProgressIndicator(modifier = Modifier.size(24.dp), color = Color.White, strokeWidth = 2.dp)
            } else {
                Text("Đăng nhập", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
        Text("HOẶC", color = Color.Gray, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(16.dp))

        // --- NÚT ĐĂNG NHẬP GOOGLE ---
        OutlinedButton(
            onClick = {
                scope.launch {
                    try {
                        val idToken = signInWithGoogle(context)
                        if (idToken != null) {
                            Toast.makeText(context, "Đang xác thực với server...", Toast.LENGTH_SHORT).show()

                            // Gọi API lên Backend
                            val response = RetrofitClient.instance.loginWithGoogle(GoogleLoginRequest(idToken))

                            if (response.isSuccessful && response.body() != null) {
                                val result = response.body()!!
                                if (result.accessToken != null) {
                                    Toast.makeText(context, "Đăng nhập Google thành công!", Toast.LENGTH_SHORT).show()
                                    onNavigateToHome("Người dùng Google")
                                } else {
                                    Toast.makeText(context, "Lỗi: Không nhận được Token từ server", Toast.LENGTH_SHORT).show()
                                }
                            } else {
                                val errorBody = response.errorBody()?.string()
                                android.util.Log.e("GOOGLE_API", "Lỗi Backend: $errorBody")
                                Toast.makeText(context, "Lỗi server hoặc chưa có API Backend!", Toast.LENGTH_SHORT).show()
                            }
                        } else {
                            Toast.makeText(context, "Đã hủy đăng nhập Google", Toast.LENGTH_SHORT).show()
                        }
                    } catch (e: Exception) {
                        Toast.makeText(context, "Lỗi: ${e.message}", Toast.LENGTH_SHORT).show()
                    }
                }
            },
            modifier = Modifier.fillMaxWidth().height(50.dp),
            shape = RoundedCornerShape(12.dp),
            border = BorderStroke(width = 1.dp, color = Color.LightGray)
        ) {
            // Lưu ý: Nếu máy bạn không có file ic_google, hãy tạm xóa dòng Image này hoặc đổi icon
            Image(painter = painterResource(id = R.drawable.ic_google), contentDescription = "Google", modifier = Modifier.size(24.dp))
            Spacer(modifier = Modifier.width(12.dp))
            Text("Đăng nhập bằng Google", color = Color.Black, fontWeight = FontWeight.SemiBold)
        }

        Spacer(modifier = Modifier.height(24.dp))

        // --- CHUYỂN SANG ĐĂNG KÝ ---
        Row(horizontalArrangement = Arrangement.Center, modifier = Modifier.fillMaxWidth()) {
            Text("Chưa có tài khoản? ", color = Color.Gray)
            Text(
                text = "Đăng ký ngay",
                color = primaryBlue,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.clickable { onNavigateToRegister() }
            )
        }
    }
    
    // --- DIALOG QUÊN MẬT KHẨU ---
    if (showForgotDialog) {
        AlertDialog(
            onDismissRequest = { showForgotDialog = false },
            title = { Text("Quên mật khẩu", fontWeight = FontWeight.Bold) },
            text = {
                Column {
                    Text("Nhập email của bạn để nhận mã OTP khôi phục mật khẩu.")
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = forgotEmail,
                        onValueChange = { forgotEmail = it },
                        label = { Text("Email") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val validEmail = forgotEmail.trim()
                        if (validEmail.isEmpty()) {
                            Toast.makeText(context, "Vui lòng nhập email", Toast.LENGTH_SHORT).show()
                            return@Button
                        }
                        isLoadingForgot = true
                        scope.launch {
                            try {
                                // Gọi API quên mật khẩu
                                val response = RetrofitClient.instance.forgotPassword(ForgotRequest(email = validEmail))
                                if (response.isSuccessful) {
                                    Toast.makeText(context, "Mã OTP đã được gửi!", Toast.LENGTH_LONG).show()
                                    showForgotDialog = false
                                    forgotEmail = ""
                                    onNavigateToResetPassword(validEmail) // Chuyển sang Reset Password
                                } else {
                                    Toast.makeText(context, "Email không tồn tại hoặc lỗi máy chủ", Toast.LENGTH_SHORT).show()
                                }
                            } catch (e: Exception) {
                                Toast.makeText(context, "Lỗi kết nối mạng!", Toast.LENGTH_SHORT).show()
                            } finally {
                                isLoadingForgot = false
                            }
                        }
                    },
                    enabled = !isLoadingForgot
                ) {
                    if (isLoadingForgot) {
                        CircularProgressIndicator(modifier = Modifier.size(20.dp), color = Color.White, strokeWidth = 2.dp)
                    } else {
                        Text("Gửi mã")
                    }
                }
            },
            dismissButton = {
                TextButton(onClick = { showForgotDialog = false }, enabled = !isLoadingForgot) {
                    Text("Hủy", color = Color.Gray)
                }
            }
        )
    }
}

// =========================================================================
// HÀM HỖ TRỢ ĐĂNG NHẬP GOOGLE NẰM NGOÀI @Composable ĐỂ TRÁNH LỖI CRASH
// =========================================================================
suspend fun signInWithGoogle(context: Context): String? {
    val credentialManager = CredentialManager.create(context)

    // ĐIỀN ĐÚNG WEB CLIENT ID CỦA BẠN VÀO ĐÂY NHÉ:
    val webClientId = context.getString(R.string.web_client_id)

    val googleIdOption = GetGoogleIdOption.Builder()
        .setFilterByAuthorizedAccounts(false)
        .setServerClientId(webClientId)
        .setAutoSelectEnabled(true)
        .build()

    val request = GetCredentialRequest.Builder()
        .addCredentialOption(googleIdOption)
        .build()

    return try {
        val result = credentialManager.getCredential(context, request)
        val credential = result.credential

        if (credential is GoogleIdTokenCredential) {
            credential.idToken
        } else {
            null
        }
    } catch (e: Exception) {
        android.util.Log.e("GOOGLE_SIGN_IN", "Lỗi: ${e.message}")
        null
    }
}