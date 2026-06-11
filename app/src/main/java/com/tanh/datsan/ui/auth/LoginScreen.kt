package com.tanh.datsan.ui.auth

import android.content.Context
import android.util.Log
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.credentials.exceptions.GetCredentialCancellationException
import androidx.credentials.exceptions.NoCredentialException
import androidx.lifecycle.compose.collectAsStateWithLifecycle
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
    val primaryBlue = MaterialTheme.colorScheme.primary

    // 1. ĐỊNH NGHĨA CHUỖI
    val textLoginTitle = stringResource(R.string.login_title)
    val textEmailHint = stringResource(R.string.login_email_hint)
    val textPasswordHint = stringResource(R.string.login_password_hint)
    val textForgotPassword = stringResource(R.string.login_forgot_password)
    val textBtnSubmit = stringResource(R.string.login_btn_submit)
    val textLoginOr = stringResource(R.string.login_or)
    val textLoginGoogle = stringResource(R.string.login_google)
    val textNoAccountPrompt = stringResource(R.string.login_no_account_prompt)
    val textRegisterLink = stringResource(R.string.login_register_link)

    val textDialogTitle = stringResource(R.string.dialog_forgot_password_title)
    val textDialogDesc = stringResource(R.string.dialog_forgot_password_desc)
    val textDialogEmailHint = stringResource(R.string.dialog_forgot_password_email_hint)
    val textBtnSendCode = stringResource(R.string.btn_send_code)
    val textBtnCancel = stringResource(R.string.btn_cancel)

    val errorEmptyFields = stringResource(R.string.val_empty_fields)
    val errorEmptyEmail = stringResource(R.string.error_empty_email)
    val msgConnectingServer = stringResource(R.string.msg_connecting_server)
    val errorGoogleCancelled = stringResource(R.string.error_google_cancelled)
    val errorNoGoogleAccount = stringResource(R.string.error_no_google_account)
    val errorWithPrefixTemplate = stringResource(R.string.error_with_prefix)
    val errorUnsupportedCredential = stringResource(R.string.error_unsupported_credential)

    val cdTogglePassword = stringResource(R.string.cd_toggle_password)
    val cdGoogleLogo = stringResource(R.string.cd_google_logo)

    // Trạng thái dữ liệu
    val isLoading by viewModel.isLoading.collectAsStateWithLifecycle()
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var showForgotDialog by remember { mutableStateOf(false) }
    var forgotEmail by remember { mutableStateOf("") }

    // Khởi tạo SnackbarHostState
    val snackbarHostState = remember { SnackbarHostState() }

    // Logic đăng nhập dùng chung (Chuyển Toast -> Snackbar sử dụng scope)
    val handleLogin = {
        val validEmail = email.trim()
        if (validEmail.isEmpty() || password.isEmpty()) {
            scope.launch { snackbarHostState.showSnackbar(errorEmptyFields) }
        } else {
            viewModel.login(validEmail, password)
        }
    }

    // Lắng nghe sự kiện từ ViewModel
    LaunchedEffect(viewModel.uiEvent) {
        viewModel.uiEvent.collect { event ->
            when (event) {
                is AuthUiEvent.ShowToast -> {
                    snackbarHostState.showSnackbar(
                        message = event.message,
                        duration = SnackbarDuration.Short
                    )
                }
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

    // BẮT BUỘC: Phải bọc giao diện trong Scaffold và truyền snackbarHost vào
    Scaffold(
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues) // Đảm bảo UI không bị đè bởi hệ thống
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(text = textLoginTitle, fontSize = 28.sp, fontWeight = FontWeight.Bold, color = primaryBlue)
            Spacer(modifier = Modifier.height(32.dp))

            // Ô nhập Email
            OutlinedTextField(
                value = email, onValueChange = { email = it },
                label = { Text(text = textEmailHint) },
                modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp), singleLine = true,
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Email,
                    imeAction = ImeAction.Next
                )
            )
            Spacer(modifier = Modifier.height(16.dp))

            // Ô nhập Mật khẩu
            OutlinedTextField(
                value = password, onValueChange = { password = it },
                label = { Text(text = textPasswordHint) },
                visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                trailingIcon = {
                    val image = if (passwordVisible) Icons.Filled.Visibility else Icons.Filled.VisibilityOff
                    IconButton(onClick = { passwordVisible = !passwordVisible }) {
                        Icon(imageVector = image, contentDescription = cdTogglePassword)
                    }
                },
                modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp), singleLine = true,
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Password,
                    imeAction = ImeAction.Done
                ),
                keyboardActions = KeyboardActions(
                    onDone = { handleLogin() }
                )
            )

            Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.CenterEnd) {
                Text(
                    text = textForgotPassword, color = primaryBlue, fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.padding(vertical = 8.dp).clickable { showForgotDialog = true }
                )
            }
            Spacer(modifier = Modifier.height(16.dp))

            // Nút Đăng nhập
            Button(
                onClick = { handleLogin() },
                modifier = Modifier.fillMaxWidth().height(50.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = primaryBlue),
                enabled = !isLoading
            ) {
                if (isLoading) {
                    CircularProgressIndicator(modifier = Modifier.size(24.dp), color = Color.White, strokeWidth = 2.dp)
                } else {
                    Text(text = textBtnSubmit, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
            Text(text = textLoginOr, color = Color.Gray, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(16.dp))

            // Nút Đăng nhập Google (Chuyển đổi Toast -> Snackbar trực tiếp trong scope.launch)
            OutlinedButton(
                onClick = {
                    if (isLoading) return@OutlinedButton
                    scope.launch {
                        val authResult = viewModel.googleAuthHelper.signInWithGoogle(context, errorUnsupportedCredential)

                        authResult.onSuccess { idToken ->
                            snackbarHostState.showSnackbar(msgConnectingServer)
                            viewModel.loginWithGoogle(idToken)
                        }.onFailure { e ->
                            when (e) {
                                is GetCredentialCancellationException -> {
                                    Log.w("Auth", "Google Sign-In Cancelled", e)
                                    snackbarHostState.showSnackbar(errorGoogleCancelled)
                                }
                                is NoCredentialException -> {
                                    Log.e("Auth", "No Google Account Found", e)
                                    snackbarHostState.showSnackbar(errorNoGoogleAccount)
                                }
                                else -> {
                                    val formattedError = String.format(errorWithPrefixTemplate, e.localizedMessage ?: "")
                                    snackbarHostState.showSnackbar(formattedError)
                                }
                            }
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
                    Image(painter = painterResource(id = R.drawable.ic_google), contentDescription = cdGoogleLogo, modifier = Modifier.size(24.dp))
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(text = textLoginGoogle, color = Color.Black, fontWeight = FontWeight.SemiBold)
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
            Row(horizontalArrangement = Arrangement.Center, modifier = Modifier.fillMaxWidth()) {
                Text(text = textNoAccountPrompt, color = Color.Gray)
                Text(text = textRegisterLink, color = primaryBlue, fontWeight = FontWeight.Bold, modifier = Modifier.clickable { onNavigateToRegister() })
            }
        }
    }

    // Dialog Quên Mật Khẩu
    if (showForgotDialog) {
        val handleForgotSubmit = {
            val validEmail = forgotEmail.trim()
            if (validEmail.isEmpty()) {
                scope.launch { snackbarHostState.showSnackbar(errorEmptyEmail) }
            } else {
                viewModel.forgotPassword(validEmail)
            }
        }

        AlertDialog(
            onDismissRequest = { showForgotDialog = false },
            title = { Text(text = textDialogTitle, fontWeight = FontWeight.Bold) },
            text = {
                Column {
                    Text(text = textDialogDesc)
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = forgotEmail, onValueChange = { forgotEmail = it },
                        label = { Text(text = textDialogEmailHint) },
                        singleLine = true, modifier = Modifier.fillMaxWidth(),
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Email,
                            imeAction = ImeAction.Done
                        ),
                        keyboardActions = KeyboardActions(
                            onDone = { handleForgotSubmit() }
                        )
                    )
                }
            },
            confirmButton = {
                Button(onClick = { handleForgotSubmit() }, enabled = !isLoading) {
                    if (isLoading) CircularProgressIndicator(modifier = Modifier.size(20.dp), color = Color.White, strokeWidth = 2.dp)
                    else Text(text = textBtnSendCode)
                }
            },
            dismissButton = {
                TextButton(onClick = { showForgotDialog = false }, enabled = !isLoading) { Text(text = textBtnCancel, color = Color.Gray) }
            }
        )
    }
}