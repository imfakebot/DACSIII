package com.tanh.datsan.ui.auth

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
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialCancellationException
import androidx.credentials.exceptions.NoCredentialException
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.tanh.datsan.R
import com.tanh.datsan.viewmodel.AuthUiEvent
import com.tanh.datsan.viewmodel.AuthViewModel
import kotlinx.coroutines.launch

@Composable
fun LoginScreen(
    viewModel: AuthViewModel,
    onNavigateToRegister: () -> Unit,
    onNavigateToOtp: (String, Boolean) -> Unit,
    onNavigateToResetPassword: (String) -> Unit,
    onNavigateToHome: (String) -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val primaryBlue = Color(0xFF1877F2)

    // Quan sát State
    val isLoading by viewModel.isLoading.collectAsStateWithLifecycle()

    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }

    var showForgotDialog by remember { mutableStateOf(false) }
    var forgotEmail by remember { mutableStateOf("") }

    // Lắng nghe Event từ ViewModel
    LaunchedEffect(Unit) {
        viewModel.uiEvent.collect { event ->
            when (event) {
                is AuthUiEvent.ShowToast -> Toast.makeText(context, event.message, Toast.LENGTH_SHORT).show()
                is AuthUiEvent.NavigateToOtp -> onNavigateToOtp(event.email, event.isLoginMode)
                is AuthUiEvent.NavigateToHome -> onNavigateToHome(event.message)
                is AuthUiEvent.NavigateToResetPassword -> {
                    showForgotDialog = false
                    forgotEmail = ""
                    onNavigateToResetPassword(event.email)
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
        Text("Đăng nhập", fontSize = 28.sp, fontWeight = FontWeight.Bold, color = primaryBlue)
        Spacer(modifier = Modifier.height(32.dp))

        OutlinedTextField(
            value = email, onValueChange = { email = it }, label = { Text("Email") },
            modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp), singleLine = true
        )
        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = password, onValueChange = { password = it }, label = { Text("Mật khẩu") },
            visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
            trailingIcon = {
                val image = if (passwordVisible) Icons.Filled.Visibility else Icons.Filled.VisibilityOff
                IconButton(onClick = { passwordVisible = !passwordVisible }) {
                    Icon(imageVector = image, contentDescription = "Toggle password")
                }
            },
            modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp), singleLine = true
        )

        Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.CenterEnd) {
            Text(
                text = "Quên mật khẩu?", color = primaryBlue, fontWeight = FontWeight.SemiBold,
                modifier = Modifier.padding(vertical = 8.dp).clickable { showForgotDialog = true }
            )
        }
        Spacer(modifier = Modifier.height(16.dp))

        // Nút Đăng nhập thường
        Button(
            onClick = {
                val validEmail = email.trim()
                if (validEmail.isEmpty() || password.isEmpty()) {
                    Toast.makeText(context, "Vui lòng nhập đầy đủ thông tin", Toast.LENGTH_SHORT).show()
                } else {
                    viewModel.login(validEmail, password)
                }
            },
            modifier = Modifier.fillMaxWidth().height(50.dp),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(containerColor = primaryBlue),
            enabled = !isLoading
        ) {
            if (isLoading) CircularProgressIndicator(modifier = Modifier.size(24.dp), color = Color.White, strokeWidth = 2.dp)
            else Text("Đăng nhập", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
        }

        Spacer(modifier = Modifier.height(16.dp))
        Text("HOẶC", color = Color.Gray, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(16.dp))

        // Nút Đăng nhập Google
        OutlinedButton(
            onClick = {
                if (isLoading) return@OutlinedButton
                scope.launch {
                    try {
                        val idToken = signInWithGoogle(context)
                        Toast.makeText(context, "Đang kết nối với Server...", Toast.LENGTH_SHORT).show()
                        viewModel.loginWithGoogle(idToken)
                    } catch (e: GetCredentialCancellationException) {
                        Toast.makeText(context, "Đã hủy chọn tài khoản.", Toast.LENGTH_SHORT).show()
                    } catch (e: NoCredentialException) {
                        Toast.makeText(context, "Không tìm thấy tài khoản Google.", Toast.LENGTH_SHORT).show()
                    } catch (e: Exception) {
                        Toast.makeText(context, "Lỗi: ${e.localizedMessage}", Toast.LENGTH_LONG).show()
                    }
                }
            },
            modifier = Modifier.fillMaxWidth().height(50.dp),
            shape = RoundedCornerShape(12.dp),
            border = BorderStroke(width = 1.dp, color = Color.LightGray),
            enabled = !isLoading
        ) {
            if (isLoading) {
                CircularProgressIndicator(modifier = Modifier.size(24.dp), color = primaryBlue, strokeWidth = 2.dp)
            } else {
                Image(painter = painterResource(id = R.drawable.ic_google), contentDescription = "Google", modifier = Modifier.size(24.dp))
                Spacer(modifier = Modifier.width(12.dp))
                Text("Đăng nhập bằng Google", color = Color.Black, fontWeight = FontWeight.SemiBold)
            }
        }

        Spacer(modifier = Modifier.height(24.dp))
        Row(horizontalArrangement = Arrangement.Center, modifier = Modifier.fillMaxWidth()) {
            Text("Chưa có tài khoản? ", color = Color.Gray)
            Text("Đăng ký ngay", color = primaryBlue, fontWeight = FontWeight.Bold, modifier = Modifier.clickable { onNavigateToRegister() })
        }
    }

    // Dialog Quên Mật Khẩu
    if (showForgotDialog) {
        AlertDialog(
            onDismissRequest = { showForgotDialog = false },
            title = { Text("Quên mật khẩu", fontWeight = FontWeight.Bold) },
            text = {
                Column {
                    Text("Nhập email của bạn để nhận mã OTP khôi phục mật khẩu.")
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = forgotEmail, onValueChange = { forgotEmail = it }, label = { Text("Email") }, singleLine = true, modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val validEmail = forgotEmail.trim()
                        if (validEmail.isEmpty()) Toast.makeText(context, "Vui lòng nhập email", Toast.LENGTH_SHORT).show()
                        else viewModel.forgotPassword(validEmail)
                    },
                    enabled = !isLoading
                ) {
                    if (isLoading) CircularProgressIndicator(modifier = Modifier.size(20.dp), color = Color.White, strokeWidth = 2.dp)
                    else Text("Gửi mã")
                }
            },
            dismissButton = {
                TextButton(onClick = { showForgotDialog = false }, enabled = !isLoading) { Text("Hủy", color = Color.Gray) }
            }
        )
    }
}

suspend fun signInWithGoogle(context: Context): String {
    val credentialManager = CredentialManager.create(context)
    val webClientId = context.getString(R.string.default_web_client_id)

    val googleIdOption = GetGoogleIdOption.Builder()
        .setFilterByAuthorizedAccounts(false)
        .setServerClientId(webClientId)
        .setAutoSelectEnabled(true)
        .build()

    val request = GetCredentialRequest.Builder()
        .addCredentialOption(googleIdOption)
        .build()

    val result = credentialManager.getCredential(context, request)
    val credential = result.credential

    if (credential is CustomCredential && credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {
        val googleIdTokenCredential = GoogleIdTokenCredential.createFrom(credential.data)
        return googleIdTokenCredential.idToken
    } else {
        throw RuntimeException("Định dạng tài khoản trả về không được hỗ trợ.")
    }
}