package com.tanh.datsan.ui.auth

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle


import com.tanh.datsan.viewmodel.AuthUiEvent
import com.tanh.datsan.viewmodel.AuthViewModel
import com.tanh.datsan.data.model.RegisterRequest

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegisterScreen(
    viewModel: AuthViewModel,
    onBackToLogin: () -> Unit,
    onNavigateToOtp: (String, Boolean) -> Unit
) {
    val context = LocalContext.current
    val scrollState = rememberScrollState()
    val primaryBlue = Color(0xFF1877F2)

    val isLoading by viewModel.isLoading.collectAsStateWithLifecycle()

    var fullName by remember { mutableStateOf("") }
    var phoneNumber by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }

    val genderOptions = listOf("Nam", "Nữ", "Khác")
    var selectedGender by remember { mutableStateOf("") }
    var expanded by remember { mutableStateOf(false) }

    var passwordVisible by remember { mutableStateOf(false) }
    var confirmPasswordVisible by remember { mutableStateOf(false) }

    // Lắng nghe Event từ ViewModel
    LaunchedEffect(Unit) {
        viewModel.uiEvent.collect { event ->
            when (event) {
                is AuthUiEvent.ShowToast -> Toast.makeText(context, event.message, Toast.LENGTH_SHORT).show()
                is AuthUiEvent.NavigateToOtp -> onNavigateToOtp(event.email, event.isLoginMode)
                else -> Unit
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(20.dp)
            .verticalScroll(scrollState),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Tạo Tài Khoản", fontSize = 28.sp, fontWeight = FontWeight.Bold, color = primaryBlue, modifier = Modifier.padding(top = 50.dp))
        Text("Điền thông tin để bắt đầu đặt sân bóng", color = Color.Gray, modifier = Modifier.padding(bottom = 30.dp, top = 8.dp))

        OutlinedTextField(
            value = fullName, onValueChange = { fullName = it }, label = { Text("Họ và tên") },
            modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp), singleLine = true
        )
        Spacer(modifier = Modifier.height(12.dp))

        OutlinedTextField(
            value = email, onValueChange = { email = it }, label = { Text("Email liên hệ") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
            modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp), singleLine = true
        )
        Spacer(modifier = Modifier.height(12.dp))

        OutlinedTextField(
            value = phoneNumber, onValueChange = { phoneNumber = it }, label = { Text("Số điện thoại") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
            modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp), singleLine = true
        )
        Spacer(modifier = Modifier.height(12.dp))

        ExposedDropdownMenuBox(
            expanded = expanded, onExpandedChange = { expanded = !expanded },
        ) {
            OutlinedTextField(
                value = if (selectedGender.isEmpty()) "--Giới tính--" else selectedGender,
                onValueChange = {}, readOnly = true,
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                modifier = Modifier.menuAnchor().fillMaxWidth(), shape = RoundedCornerShape(12.dp)
            )
            ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                genderOptions.forEach { selectionOption ->
                    DropdownMenuItem(text = { Text(selectionOption) }, onClick = { selectedGender = selectionOption; expanded = false })
                }
            }
        }
        Spacer(modifier = Modifier.height(12.dp))

        OutlinedTextField(
            value = password, onValueChange = { password = it }, label = { Text("Mật khẩu (Tối thiểu 8 ký tự)") },
            visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
            trailingIcon = {
                val image = if (passwordVisible) Icons.Filled.Visibility else Icons.Filled.VisibilityOff
                IconButton(onClick = { passwordVisible = !passwordVisible }) { Icon(imageVector = image, contentDescription = "Toggle") }
            },
            modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp), singleLine = true
        )
        Spacer(modifier = Modifier.height(12.dp))

        OutlinedTextField(
            value = confirmPassword, onValueChange = { confirmPassword = it }, label = { Text("Xác nhận mật khẩu") },
            visualTransformation = if (confirmPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
            trailingIcon = {
                val image = if (confirmPasswordVisible) Icons.Filled.Visibility else Icons.Filled.VisibilityOff
                IconButton(onClick = { confirmPasswordVisible = !confirmPasswordVisible }) { Icon(imageVector = image, contentDescription = "Toggle") }
            },
            modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp), singleLine = true
        )
        Spacer(modifier = Modifier.height(30.dp))

        Button(
            onClick = {
                if (fullName.isEmpty() || email.isEmpty() || password.isEmpty() || phoneNumber.isEmpty()) {
                    Toast.makeText(context, "Vui lòng nhập đủ các trường bắt buộc", Toast.LENGTH_SHORT).show()
                    return@Button
                }
                if (password != confirmPassword) {
                    Toast.makeText(context, "Mật khẩu xác nhận không khớp", Toast.LENGTH_SHORT).show()
                    return@Button
                }
                if (password.length < 8) {
                    Toast.makeText(context, "Mật khẩu phải có ít nhất 8 ký tự", Toast.LENGTH_SHORT).show()
                    return@Button
                }

                val genderMap = mapOf("Nam" to "male", "Nữ" to "female", "Khác" to "other")
                val genderApi = genderMap[selectedGender] ?: "other"

                // Gọi ViewModel
                val requestBody = RegisterRequest(fullName.trim(), email.trim(), phoneNumber.trim(), genderApi, password)
                viewModel.register(requestBody)
            },
            modifier = Modifier.fillMaxWidth().height(50.dp),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(containerColor = primaryBlue),
            enabled = !isLoading
        ) {
            if (isLoading) CircularProgressIndicator(modifier = Modifier.size(24.dp), color = Color.White, strokeWidth = 2.dp)
            else Text("Đăng ký", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
        }

        Spacer(modifier = Modifier.height(20.dp))
        TextButton(onClick = onBackToLogin) { Text(text = "Đã có tài khoản? Đăng nhập ngay", color = primaryBlue) }
        Spacer(modifier = Modifier.height(20.dp))
    }
}