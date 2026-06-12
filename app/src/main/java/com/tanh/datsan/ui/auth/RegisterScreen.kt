package com.tanh.datsan.ui.auth

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.tanh.datsan.R
import com.tanh.datsan.data.model.RegisterRequest
import com.tanh.datsan.viewmodel.AuthUiState
import com.tanh.datsan.viewmodel.AuthViewModel

@Composable
fun RegisterScreen(
    viewModel: AuthViewModel,
    onNavigateToLogin: () -> Unit,
    onOtpSent: (String, Boolean) -> Unit
) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var fullName by remember { mutableStateOf("") }
    var phoneNumber by remember { mutableStateOf("") }
    var gender by remember { mutableStateOf("male") }
    var passwordVisible by remember { mutableStateOf(false) }
    
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(uiState) {
        if (uiState is AuthUiState.OtpSent) {
            val state = uiState as AuthUiState.OtpSent
            onOtpSent(state.email, state.isRegister)
            viewModel.resetState()
        }
    }

    AuthBackground {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(60.dp))
            
            Text(
                text = stringResource(R.string.reg_title),
                color = Color.White,
                fontSize = 32.sp,
                fontWeight = FontWeight.ExtraBold,
                letterSpacing = 1.sp
            )
            Text(
                text = stringResource(R.string.reg_subtitle),
                color = Color.White.copy(alpha = 0.7f),
                fontSize = 15.sp,
                style = MaterialTheme.typography.bodyMedium
            )
            
            Spacer(modifier = Modifier.height(40.dp))

            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                color = Color.White.copy(alpha = 0.12f),
                border = BorderStroke(1.dp, Color.White.copy(alpha = 0.2f))
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    AuthTextField(
                        value = fullName,
                        onValueChange = { fullName = it },
                        placeholder = stringResource(R.string.reg_full_name),
                        icon = Icons.Default.Person
                    )
                    Spacer(modifier = Modifier.height(16.dp))

                    AuthTextField(
                        value = phoneNumber,
                        onValueChange = { phoneNumber = it },
                        placeholder = stringResource(R.string.reg_phone),
                        icon = Icons.Default.Phone,
                        keyboardType = KeyboardType.Phone
                    )
                    Spacer(modifier = Modifier.height(16.dp))

                    AuthTextField(
                        value = email,
                        onValueChange = { email = it },
                        placeholder = stringResource(R.string.reg_email),
                        icon = Icons.Default.Email,
                        keyboardType = KeyboardType.Email
                    )
                    Spacer(modifier = Modifier.height(16.dp))

                    AuthTextField(
                        value = password,
                        onValueChange = { password = it },
                        placeholder = stringResource(R.string.reg_password),
                        icon = Icons.Default.Lock,
                        isPassword = true,
                        passwordVisible = passwordVisible,
                        onPasswordToggle = { passwordVisible = !passwordVisible }
                    )
                    Spacer(modifier = Modifier.height(16.dp))

                    AuthTextField(
                        value = confirmPassword,
                        onValueChange = { confirmPassword = it },
                        placeholder = stringResource(R.string.reg_confirm_password),
                        icon = Icons.Default.LockReset,
                        isPassword = true,
                        passwordVisible = passwordVisible,
                        onPasswordToggle = { passwordVisible = !passwordVisible }
                    )
                    
                    Spacer(modifier = Modifier.height(24.dp))
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Start
                    ) {
                        Text(
                            text = stringResource(R.string.reg_gender),
                            color = Color.White,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.width(16.dp))
                        
                        GenderChip(
                            label = "Nam",
                            selected = gender == "male",
                            onClick = { gender = "male" }
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        GenderChip(
                            label = "Nữ",
                            selected = gender == "female",
                            onClick = { gender = "female" }
                        )
                    }

                    Spacer(modifier = Modifier.height(40.dp))

                    Button(
                        onClick = {
                            if (password == confirmPassword && email.isNotBlank() && fullName.isNotBlank() && phoneNumber.isNotBlank()) {
                                viewModel.initiateRegistration(
                                    RegisterRequest(email, password, fullName, phoneNumber, gender)
                                )
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color.White,
                            contentColor = Color(0xFF0F2027)
                        ),
                        enabled = uiState !is AuthUiState.Loading
                    ) {
                        if (uiState is AuthUiState.Loading) {
                            CircularProgressIndicator(color = Color(0xFF0F2027), modifier = Modifier.size(24.dp))
                        } else {
                            Text(
                                stringResource(R.string.reg_btn_submit).uppercase(),
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 2.sp
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            TextButton(onClick = onNavigateToLogin) {
                Text(
                    stringResource(R.string.reg_has_account),
                    color = Color.White.copy(alpha = 0.9f),
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 15.sp
                )
            }

            if (uiState is AuthUiState.Error) {
                Surface(
                    modifier = Modifier.padding(vertical = 20.dp),
                    color = Color(0xFFFF5252).copy(alpha = 0.15f),
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, Color(0xFFFF5252).copy(alpha = 0.5f))
                ) {
                    Text(
                        text = (uiState as AuthUiState.Error).message,
                        color = Color(0xFFFF8A80),
                        modifier = Modifier.padding(horizontal = 20.dp, vertical = 12.dp),
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(60.dp))
        }
    }
}

@Composable
fun GenderChip(
    label: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    val backgroundColor by animateColorAsState(
        if (selected) Color.White else Color.White.copy(alpha = 0.1f),
        label = "chip_bg"
    )
    val textColor by animateColorAsState(
        if (selected) Color(0xFF0F2027) else Color.White,
        label = "chip_text"
    )
    
    Surface(
        modifier = Modifier.clickable { onClick() },
        shape = RoundedCornerShape(12.dp),
        color = backgroundColor,
        border = if (!selected) BorderStroke(1.dp, Color.White.copy(alpha = 0.5f)) else null
    ) {
        Text(
            text = label,
            modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp),
            color = textColor,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
fun AuthTextField(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    keyboardType: KeyboardType = KeyboardType.Text,
    isPassword: Boolean = false,
    passwordVisible: Boolean = false,
    onPasswordToggle: (() -> Unit)? = null
) {
    TextField(
        value = value,
        onValueChange = onValueChange,
        placeholder = { Text(placeholder, color = Color.White.copy(alpha = 0.6f)) },
        modifier = Modifier.fillMaxWidth(),
        leadingIcon = { Icon(icon, contentDescription = null, tint = Color.White) },
        trailingIcon = if (isPassword && onPasswordToggle != null) {
            {
                val image = if (passwordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff
                IconButton(onClick = onPasswordToggle) {
                    Icon(imageVector = image, contentDescription = null, tint = Color.White)
                }
            }
        } else null,
        colors = TextFieldDefaults.colors(
            focusedContainerColor = Color.Transparent,
            unfocusedContainerColor = Color.Transparent,
            focusedIndicatorColor = Color.White,
            unfocusedIndicatorColor = Color.White.copy(alpha = 0.4f),
            focusedTextColor = Color.White,
            unfocusedTextColor = Color.White
        ),
        visualTransformation = if (isPassword && !passwordVisible) PasswordVisualTransformation() else VisualTransformation.None,
        keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
        singleLine = true
    )
}
