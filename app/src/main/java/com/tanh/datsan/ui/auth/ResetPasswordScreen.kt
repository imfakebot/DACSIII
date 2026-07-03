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
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.tanh.datsan.R
import com.tanh.datsan.ui.auth.AuthUiEvent
import com.tanh.datsan.ui.auth.AuthViewModel

@Composable
fun ResetPasswordScreen(
    viewModel: AuthViewModel,
    emailSent: String,
    onNavigateBackToLogin: () -> Unit
) {
    val context = LocalContext.current
    val primaryBlue = Color(0xFF1877F2)
    val isLoading by viewModel.isLoading.collectAsStateWithLifecycle()

    var token by remember { mutableStateOf("") }
    var newPassword by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }

    var newPasswordVisible by remember { mutableStateOf(false) }
    var confirmPasswordVisible by remember { mutableStateOf(false) }

    // Lắng nghe Event từ ViewModel
    LaunchedEffect(Unit) {
        viewModel.uiEvent.collect { event ->
            when (event) {
                is AuthUiEvent.ShowToast -> Toast.makeText(context, event.message, Toast.LENGTH_SHORT).show()
                is AuthUiEvent.NavigateBackToLogin -> onNavigateBackToLogin()
                else -> Unit
            }
        }
    }

    Column(
        modifier = Modifier.fillMaxSize().padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(stringResource(R.string.reset_title), color = primaryBlue, fontSize = 32.sp, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = stringResource(R.string.reset_token_desc, emailSent),
            color = Color.Gray, fontSize = 14.sp, textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(30.dp))

        OutlinedTextField(
            value = token, onValueChange = { token = it }, label = { Text(stringResource(R.string.reset_token_hint)) },
            modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text), singleLine = true
        )
        Spacer(modifier = Modifier.height(12.dp))

        OutlinedTextField(
            value = newPassword, onValueChange = { newPassword = it }, label = { Text(stringResource(R.string.reset_new_password)) },
            modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp),
            visualTransformation = if (newPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
            trailingIcon = {
                val image = if (newPasswordVisible) Icons.Filled.Visibility else Icons.Filled.VisibilityOff
                IconButton(onClick = { newPasswordVisible = !newPasswordVisible }) { Icon(imageVector = image, contentDescription = "Toggle") }
            },
            singleLine = true
        )
        Spacer(modifier = Modifier.height(12.dp))

        OutlinedTextField(
            value = confirmPassword, onValueChange = { confirmPassword = it }, label = { Text(stringResource(R.string.reset_confirm_password)) },
            modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp),
            visualTransformation = if (confirmPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
            trailingIcon = {
                val image = if (confirmPasswordVisible) Icons.Filled.Visibility else Icons.Filled.VisibilityOff
                IconButton(onClick = { confirmPasswordVisible = !confirmPasswordVisible }) { Icon(imageVector = image, contentDescription = "Toggle") }
            },
            singleLine = true
        )
        Spacer(modifier = Modifier.height(30.dp))

        Button(
            onClick = {
                val cleanToken = token.trim()
                if (cleanToken.isEmpty()) {
                    Toast.makeText(context, context.getString(R.string.reset_val_empty_token), Toast.LENGTH_SHORT).show()
                    return@Button
                }
                if (newPassword.length < 8) {
                    Toast.makeText(context, context.getString(R.string.reset_val_password_short), Toast.LENGTH_SHORT).show()
                    return@Button
                }
                if (newPassword != confirmPassword) {
                    Toast.makeText(context, context.getString(R.string.val_password_mismatch), Toast.LENGTH_SHORT).show()
                    return@Button
                }

                // Gọi ViewModel
                viewModel.resetPassword(cleanToken, newPassword)
            },
            modifier = Modifier.fillMaxWidth().height(50.dp),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(containerColor = primaryBlue),
            enabled = !isLoading
        ) {
            if (isLoading) CircularProgressIndicator(modifier = Modifier.size(24.dp), color = Color.White, strokeWidth = 2.dp)
            else Text(stringResource(R.string.reset_btn_submit), fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.White)
        }
    }
}