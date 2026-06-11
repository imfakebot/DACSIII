package com.tanh.datsan.ui.auth

import android.widget.Toast
import androidx.annotation.StringRes
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.tanh.datsan.R
import com.tanh.datsan.data.model.RegisterRequest
import com.tanh.datsan.viewmodel.AuthUiEvent
import com.tanh.datsan.viewmodel.AuthViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegisterScreen(
    viewModel: AuthViewModel,
    onBackToLogin: () -> Unit,
    onNavigateToOtp: (String, Boolean) -> Unit
) {
    val context = LocalContext.current
    val scrollState = rememberScrollState()
    val primaryColor = MaterialTheme.colorScheme.primary

    val isLoading by viewModel.isLoading.collectAsStateWithLifecycle()

    var fullName by remember { mutableStateOf("") }
    var phoneNumber by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }

    // Quản lý trạng thái giới tính bằng Enum nội bộ
    var selectedGender by remember { mutableStateOf<GenderType?>(null) }
    var genderExpanded by remember { mutableStateOf(false) }

    var passwordVisible by remember { mutableStateOf(false) }
    var confirmPasswordVisible by remember { mutableStateOf(false) }

    // Đọc chuỗi thông báo lỗi ra biến trước để dùng trong onClick
    val errorEmptyFields = stringResource(R.string.error_empty_fields)
    val errorPasswordMismatch = stringResource(R.string.error_password_mismatch)
    val errorPasswordTooShort = stringResource(R.string.error_password_too_short)

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
        Text(
            text = stringResource(R.string.register_title),
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
            color = primaryColor,
            modifier = Modifier.padding(top = 50.dp)
        )
        Text(
            text = stringResource(R.string.register_subtitle),
            color = Color.Gray,
            modifier = Modifier.padding(bottom = 30.dp, top = 8.dp)
        )

        OutlinedTextField(
            value = fullName, onValueChange = { fullName = it },
            label = { Text(stringResource(R.string.label_full_name)) },
            modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp), singleLine = true
        )
        Spacer(modifier = Modifier.height(12.dp))

        OutlinedTextField(
            value = email, onValueChange = { email = it },
            label = { Text(stringResource(R.string.label_email)) },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
            modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp), singleLine = true
        )
        Spacer(modifier = Modifier.height(12.dp))

        OutlinedTextField(
            value = phoneNumber, onValueChange = { phoneNumber = it },
            label = { Text(stringResource(R.string.label_phone)) },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
            modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp), singleLine = true
        )
        Spacer(modifier = Modifier.height(12.dp))

        ExposedDropdownMenuBox(
            expanded = genderExpanded, onExpandedChange = { genderExpanded = !genderExpanded },
        ) {
            val genderText = selectedGender?.let { stringResource(it.resId) } ?: stringResource(R.string.label_gender_placeholder)
            OutlinedTextField(
                value = genderText,
                onValueChange = {}, readOnly = true,
                label = { Text(stringResource(R.string.label_gender)) },
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = genderExpanded) },
                modifier = Modifier.menuAnchor().fillMaxWidth(), shape = RoundedCornerShape(12.dp)
            )
            ExposedDropdownMenu(expanded = genderExpanded, onDismissRequest = { genderExpanded = false }) {
                GenderType.values().forEach { gender ->
                    DropdownMenuItem(
                        text = { Text(stringResource(gender.resId)) },
                        onClick = {
                            selectedGender = gender
                            genderExpanded = false
                        }
                    )
                }
            }
        }
        Spacer(modifier = Modifier.height(12.dp))

        OutlinedTextField(
            value = password, onValueChange = { password = it },
            label = { Text(stringResource(R.string.label_password)) },
            visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
            trailingIcon = {
                val image = if (passwordVisible) Icons.Filled.Visibility else Icons.Filled.VisibilityOff
                IconButton(onClick = { passwordVisible = !passwordVisible }) {
                    Icon(imageVector = image, contentDescription = stringResource(R.string.cd_toggle_password))
                }
            },
            modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp), singleLine = true
        )
        Spacer(modifier = Modifier.height(12.dp))

        OutlinedTextField(
            value = confirmPassword, onValueChange = { confirmPassword = it },
            label = { Text(stringResource(R.string.label_confirm_password)) },
            visualTransformation = if (confirmPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
            trailingIcon = {
                val image = if (confirmPasswordVisible) Icons.Filled.Visibility else Icons.Filled.VisibilityOff
                IconButton(onClick = { confirmPasswordVisible = !confirmPasswordVisible }) {
                    Icon(imageVector = image, contentDescription = stringResource(R.string.cd_toggle_password))
                }
            },
            modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp), singleLine = true
        )
        Spacer(modifier = Modifier.height(30.dp))

        Button(
            onClick = {
                if (fullName.isEmpty() || email.isEmpty() || password.isEmpty() || phoneNumber.isEmpty()) {
                    Toast.makeText(context, errorEmptyFields, Toast.LENGTH_SHORT).show()
                    return@Button
                }
                if (password != confirmPassword) {
                    Toast.makeText(context, errorPasswordMismatch, Toast.LENGTH_SHORT).show()
                    return@Button
                }
                if (password.length < 8) {
                    Toast.makeText(context, errorPasswordTooShort, Toast.LENGTH_SHORT).show()
                    return@Button
                }

                // Lấy trực tiếp apiKey từ enum nội bộ
                val genderApi = selectedGender?.apiKey ?: "other"

                val requestBody = RegisterRequest(
                    full_name = fullName.trim(),
                    email = email.trim(),
                    phone_number = phoneNumber.trim(),
                    gender = genderApi,
                    password = password
                )
                viewModel.register(requestBody)
            },
            modifier = Modifier.fillMaxWidth().height(50.dp),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(containerColor = primaryColor),
            enabled = !isLoading
        ) {
            if (isLoading) {
                CircularProgressIndicator(modifier = Modifier.size(24.dp), color = Color.White, strokeWidth = 2.dp)
            } else {
                Text(text = stringResource(R.string.btn_register), color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
            }
        }

        Spacer(modifier = Modifier.height(20.dp))
        TextButton(onClick = onBackToLogin) {
            Text(text = stringResource(R.string.btn_already_have_account), color = primaryColor)
        }
        Spacer(modifier = Modifier.height(20.dp))
    }
}

        Spacer(modifier = Modifier.height(20.dp))
        TextButton(onClick = onBackToLogin) {
            Text(text = stringResource(R.string.btn_already_have_account), color = primaryColor)
        }
        Spacer(modifier = Modifier.height(20.dp))
    }
}

// Định nghĩa Enum nội bộ ngay trong cùng file để gọn code và dễ quản lý
private enum class GenderType(val apiKey: String, @StringRes val resId: Int) {
    MALE("male", R.string.gender_male),
    FEMALE("female", R.string.gender_female),
    OTHER("other", R.string.gender_other)
}