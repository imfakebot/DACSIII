package com.tanh.datsan.ui.profile

import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.*
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.automirrored.rounded.ListAlt
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.draw.*
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.*
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import coil.compose.AsyncImage
import com.tanh.datsan.R
import com.tanh.datsan.utils.compressImage
import com.tanh.datsan.utils.toFile
import com.tanh.datsan.utils.toFullImageUrl
import com.tanh.datsan.viewmodel.ProfileViewModel
import com.tanh.datsan.viewmodel.MainViewModel
import com.tanh.datsan.viewmodel.AuthViewModel
import com.tanh.datsan.viewmodel.AuthUiEvent
import kotlinx.coroutines.flow.collectLatest
import java.io.File
import java.util.*
import java.text.SimpleDateFormat

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    onBackClick: () -> Unit,
    onLogoutClick: () -> Unit,
    onNavigateToResetPassword: (String) -> Unit,
    onNavigateToFeedbacks: () -> Unit = {},
    showBackButton: Boolean = true,
    viewModel: ProfileViewModel = hiltViewModel(),
    mainViewModel: MainViewModel = hiltViewModel(),
    authViewModel: AuthViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsState()
    val authIsLoading by authViewModel.isLoading.collectAsState()

    val currentTheme by mainViewModel.theme.collectAsState()
    val currentLanguage by mainViewModel.language.collectAsState()

    var showLogoutDialog by remember { mutableStateOf(false) }
    var showAvatarConfirmDialog by remember { mutableStateOf(false) }
    var pendingAvatarFile by remember { mutableStateOf<File?>(null) }
    var showDatePicker by remember { mutableStateOf(false) }
    var showCitySheet by remember { mutableStateOf(false) }
    var showWardSheet by remember { mutableStateOf(false) }
    var showChangePasswordDialog by remember { mutableStateOf(false) }

    LaunchedEffect(uiState.toastMessage) {
        uiState.toastMessage?.let {
            Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
            viewModel.clearToast()
        }
    }

    LaunchedEffect(Unit) {
        authViewModel.uiEvent.collectLatest { event ->
            when (event) {
                is AuthUiEvent.NavigateToResetPassword -> onNavigateToResetPassword(event.email)
                is AuthUiEvent.ShowToast -> Toast.makeText(
                    context,
                    event.message,
                    Toast.LENGTH_SHORT
                ).show()

                else -> {}
            }
        }
    }

    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        uri?.let {
            val file = it.toFile(context)
            if (file != null) {
                pendingAvatarFile = file
                showAvatarConfirmDialog = true
            } else {
                Toast.makeText(context, "Không thể lấy được đường dẫn ảnh!", Toast.LENGTH_SHORT)
                    .show()
            }
        }
    }

    Scaffold(
        containerColor = Color(0xFFF1F5F9),
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        stringResource(R.string.profile_title),
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 20.sp,
                        color = Color.White
                    )
                },
                navigationIcon = {
                    if (showBackButton) {
                        IconButton(onClick = onBackClick) {
                            Icon(
                                Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = null,
                                tint = Color.White
                            )
                        }
                    }
                },
                actions = {
                    Surface(
                        onClick = { viewModel.toggleEditing(!uiState.isEditing) },
                        color = if (uiState.isEditing) Color.White else Color.White.copy(alpha = 0.2f),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.padding(end = 12.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                if (uiState.isEditing) Icons.Default.Close else Icons.Default.Edit,
                                contentDescription = null,
                                tint = if (uiState.isEditing) Color.Red else Color.White,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(Modifier.width(6.dp))
                            Text(
                                if (uiState.isEditing) stringResource(R.string.profile_btn_cancel) else stringResource(
                                    R.string.profile_btn_edit
                                ),
                                color = if (uiState.isEditing) Color.Red else Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFF0F172A),
                    scrolledContainerColor = Color.Unspecified,
                    navigationIconContentColor = Color.White,
                    titleContentColor = Color.White,
                    actionIconContentColor = Color.White
                )
            )
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            PremiumHeaderBackground()

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
            ) {
                // --- AVATAR & NAME SECTION ---
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 32.dp, bottom = 24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(contentAlignment = Alignment.BottomEnd) {
                        Box(
                            modifier = Modifier
                                .size(130.dp)
                                .background(Color.White.copy(alpha = 0.1f), CircleShape)
                                .border(1.dp, Color.White.copy(alpha = 0.2f), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Surface(
                                modifier = Modifier.size(110.dp),
                                shape = CircleShape,
                                color = Color(0xFFF1F5F9),
                                border = BorderStroke(3.dp, Color.White),
                                shadowElevation = 16.dp
                            ) {
                                AsyncImage(
                                    model = uiState.avatarUrl?.toFullImageUrl(),
                                    contentDescription = null,
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .clip(CircleShape),
                                    contentScale = ContentScale.Crop,
                                    placeholder = painterResource(id = R.drawable.ic_default_avatar),
                                    error = painterResource(id = R.drawable.ic_default_avatar)
                                )
                                if (uiState.isLoading || authIsLoading) {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .background(Color.Black.copy(alpha = 0.3f)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        CircularProgressIndicator(
                                            color = Color.White,
                                            modifier = Modifier.size(24.dp)
                                        )
                                    }
                                }
                            }
                        }
                        Surface(
                            modifier = Modifier
                                .size(36.dp)
                                .clickable {
                                    imagePickerLauncher.launch(
                                        PickVisualMediaRequest(
                                            ActivityResultContracts.PickVisualMedia.ImageOnly
                                        )
                                    )
                                },
                            shape = CircleShape,
                            color = Color(0xFF3B82F6),
                            border = BorderStroke(2.dp, Color.White),
                            shadowElevation = 6.dp
                        ) {
                            Icon(
                                Icons.Default.CameraAlt,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.padding(8.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = uiState.fullName.ifBlank { stringResource(R.string.profile_new_member) },
                        fontSize = 26.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color.White
                    )
                    Surface(
                        color = Color.White.copy(alpha = 0.15f),
                        shape = RoundedCornerShape(20.dp),
                        modifier = Modifier.padding(top = 8.dp)
                    ) {
                        Text(
                            text = uiState.email,
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp),
                            fontSize = 13.sp,
                            color = Color.White.copy(alpha = 0.9f),
                            fontWeight = FontWeight.Medium
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = Color(0xFFF8FAFC),
                    shape = RoundedCornerShape(topStart = 40.dp, topEnd = 40.dp)
                ) {
                    Column(modifier = Modifier.padding(horizontal = 20.dp, vertical = 32.dp)) {

                        PremiumSectionTitle(
                            stringResource(R.string.profile_section_personal),
                            Icons.Rounded.AccountCircle
                        )
                        PremiumProfileCard {
                            EditablePremiumItem(
                                icon = Icons.Rounded.Badge,
                                label = stringResource(R.string.reg_full_name),
                                value = uiState.fullName,
                                isEditing = uiState.isEditing,
                                onValueChange = { viewModel.onFullNameChange(it) }
                            )
                            PremiumDivider()
                            EditablePremiumItem(
                                icon = Icons.Rounded.Phone,
                                label = stringResource(R.string.reg_phone),
                                value = uiState.phoneNumber,
                                isEditing = uiState.isEditing,
                                onValueChange = { viewModel.onPhoneNumberChange(it) },
                                keyboardType = KeyboardType.Phone
                            )
                            PremiumDivider()
                            PremiumGenderSelector(
                                gender = uiState.gender,
                                isEditing = uiState.isEditing,
                                onGenderSelected = { viewModel.onGenderChange(it) }
                            )
                            PremiumDivider()
                            PremiumDatePickerItem(
                                dob = uiState.dateOfBirth,
                                isEditing = uiState.isEditing,
                                onClick = { showDatePicker = true }
                            )
                        }

                        Spacer(modifier = Modifier.height(28.dp))

                        PremiumSectionTitle(
                            stringResource(R.string.profile_section_location),
                            Icons.Rounded.Explore
                        )
                        PremiumProfileCard {
                            PremiumLocationSelector(
                                label = stringResource(R.string.profile_label_city),
                                value = uiState.displayCityName,
                                isEditing = uiState.isEditing,
                                onClick = { showCitySheet = true }
                            )
                            PremiumDivider()
                            PremiumLocationSelector(
                                label = stringResource(R.string.profile_label_ward),
                                value = uiState.displayWardName,
                                isEditing = uiState.isEditing,
                                enabled = !uiState.isEditing || uiState.selectedCityId != null,
                                onClick = { showWardSheet = true }
                            )
                            PremiumDivider()
                            EditablePremiumItem(
                                icon = Icons.Rounded.Home,
                                label = stringResource(R.string.profile_label_street),
                                value = uiState.street,
                                isEditing = uiState.isEditing,
                                onValueChange = { viewModel.onStreetChange(it) }
                            )
                            PremiumDivider()
                            EditablePremiumItem(
                                icon = Icons.Rounded.Description,
                                label = stringResource(R.string.profile_label_bio),
                                value = uiState.bio,
                                isEditing = uiState.isEditing,
                                onValueChange = { viewModel.onBioChange(it) }
                            )
                        }

                        Spacer(modifier = Modifier.height(28.dp))

                        PremiumSectionTitle(
                            stringResource(R.string.profile_section_system),
                            Icons.Rounded.Security
                        )
                        PremiumProfileCard {
                            PremiumSettingsItem(
                                icon = Icons.Rounded.Palette,
                                label = stringResource(R.string.profile_label_theme),
                                value = when (currentTheme) {
                                    "light" -> stringResource(R.string.profile_theme_light)
                                    "dark" -> stringResource(R.string.profile_theme_dark)
                                    else -> stringResource(R.string.profile_theme_system)
                                },
                                color = Color(0xFF8B5CF6),
                                onClick = { /* mainViewModel theme logic */ }
                            )
                            PremiumDivider()
                            PremiumSettingsItem(
                                icon = Icons.Rounded.Translate,
                                label = stringResource(R.string.profile_label_lang),
                                value = if (currentLanguage == "vi") stringResource(R.string.profile_lang_vi) else stringResource(
                                    R.string.profile_lang_en
                                ),
                                color = Color(0xFF10B981),
                                onClick = { /* mainViewModel language logic */ }
                            )
                            PremiumDivider()
                            PremiumSettingsItem(
                                icon = Icons.Rounded.LockReset,
                                label = stringResource(R.string.profile_label_reset_pwd),
                                value = stringResource(R.string.profile_reset_pwd_hint),
                                color = Color(0xFFF59E0B),
                                onClick = {
                                    showChangePasswordDialog = true
                                }
                            )
                            PremiumDivider()
                            PremiumSettingsItem(
                                icon = Icons.Rounded.Feedback,
                                label = "Góp ý & Báo lỗi",
                                value = "Gửi phản hồi cho chúng tôi",
                                color = Color(0xFF3B82F6),
                                onClick = onNavigateToFeedbacks
                            )
                        }

                        Spacer(modifier = Modifier.height(40.dp))

                        AnimatedVisibility(
                            visible = uiState.isEditing,
                            enter = expandVertically() + fadeIn(),
                            exit = shrinkVertically() + fadeOut()
                        ) {
                            Button(
                                onClick = { viewModel.updateProfile() },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(58.dp),
                                shape = RoundedCornerShape(18.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Color(
                                        0xFF0F172A
                                    )
                                ),
                                elevation = ButtonDefaults.buttonElevation(defaultElevation = 8.dp)
                            ) {
                                if (uiState.isLoading) CircularProgressIndicator(
                                    color = Color.White,
                                    modifier = Modifier.size(24.dp)
                                )
                                else Text(
                                    stringResource(R.string.profile_btn_save),
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 16.sp
                                )
                            }
                        }

                        if (uiState.isEditing) Spacer(modifier = Modifier.height(16.dp))

                        OutlinedButton(
                            onClick = { showLogoutDialog = true },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(58.dp),
                            shape = RoundedCornerShape(18.dp),
                            border = BorderStroke(1.5.dp, Color(0xFFEF4444).copy(alpha = 0.3f)),
                            colors = ButtonDefaults.outlinedButtonColors(
                                contentColor = Color(
                                    0xFFEF4444
                                )
                            )
                        ) {
                            Icon(
                                Icons.AutoMirrored.Filled.Logout,
                                modifier = Modifier.size(20.dp),
                                contentDescription = null
                            )
                            Spacer(Modifier.width(8.dp))
                            Text(
                                stringResource(R.string.profile_btn_logout),
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp
                            )
                        }

                        Spacer(modifier = Modifier.height(100.dp))
                    }
                }
            }
        }

        if (showAvatarConfirmDialog && pendingAvatarFile != null) {
            AlertDialog(
                onDismissRequest = {
                    showAvatarConfirmDialog = false
                    pendingAvatarFile = null
                },
                title = {
                    Text(stringResource(R.string.update_avatar), fontWeight = FontWeight.Bold)
                },
                text = {
                    Text(stringResource(R.string.avatar_confirm))
                },
                confirmButton = {
                    Button(
                        onClick = {
                            showAvatarConfirmDialog = false
                            pendingAvatarFile?.let {
                                try {
                                    val compressedFile = it.compressImage(context)
                                    viewModel.uploadAvatar(imageFile=compressedFile)
                                }catch (e:Exception){
                                    Toast.makeText(context, "Lỗi khi tải ảnh lên: ${e.message}", Toast.LENGTH_SHORT).show()
                                }
                            }
                            pendingAvatarFile = null
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF3B82F6))
                    ) {
                        Text(
                            stringResource(R.string.update),
                            color = Color.White,
                            fontWeight = FontWeight.Bold
                        )
                    }
                },
                dismissButton = {
                    TextButton(onClick = {
                        showAvatarConfirmDialog = false
                        pendingAvatarFile = null
                    }) {
                        Text(stringResource(R.string.profile_btn_cancel), color = Color.Gray)
                    }
                },
                shape = RoundedCornerShape(24.dp)
            )
        }
        if (showLogoutDialog) {
            AlertDialog(
                onDismissRequest = { showLogoutDialog = false },
                title = {
                    Text(
                        stringResource(R.string.profile_logout_confirm_title),
                        fontWeight = FontWeight.Bold
                    )
                },
                text = { Text(stringResource(R.string.profile_logout_confirm_msg)) },
                confirmButton = {
                    Button(onClick = {
                        showLogoutDialog = false; authViewModel.logout(); onLogoutClick()
                    }, colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEF4444))) {
                        Text(
                            stringResource(R.string.profile_btn_agree),
                            color = Color.White,
                            fontWeight = FontWeight.Bold
                        )
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showLogoutDialog = false }) {
                        Text(
                            stringResource(R.string.profile_btn_dismiss),
                            color = Color.Gray
                        )
                    }
                },
                shape = RoundedCornerShape(24.dp)
            )
        }

        if (showChangePasswordDialog) {
            var oldPassword by remember { mutableStateOf("") }
            var newPassword by remember { mutableStateOf("") }
            var confirmPassword by remember { mutableStateOf("") }
            var oldPasswordVisible by remember { mutableStateOf(false) }
            var newPasswordVisible by remember { mutableStateOf(false) }

            AlertDialog(
                onDismissRequest = { showChangePasswordDialog = false },
                title = { Text("Đổi mật khẩu", fontWeight = FontWeight.Bold) },
                text = {
                    Column {
                        OutlinedTextField(
                            value = oldPassword,
                            onValueChange = { oldPassword = it },
                            label = { Text("Mật khẩu hiện tại") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            visualTransformation = if (oldPasswordVisible) androidx.compose.ui.text.input.VisualTransformation.None else androidx.compose.ui.text.input.PasswordVisualTransformation(),
                            trailingIcon = {
                                IconButton(onClick = { oldPasswordVisible = !oldPasswordVisible }) {
                                    Icon(if (oldPasswordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff, contentDescription = null)
                                }
                            }
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedTextField(
                            value = newPassword,
                            onValueChange = { newPassword = it },
                            label = { Text("Mật khẩu mới (tối thiểu 8 ký tự)") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            visualTransformation = if (newPasswordVisible) androidx.compose.ui.text.input.VisualTransformation.None else androidx.compose.ui.text.input.PasswordVisualTransformation(),
                            trailingIcon = {
                                IconButton(onClick = { newPasswordVisible = !newPasswordVisible }) {
                                    Icon(if (newPasswordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff, contentDescription = null)
                                }
                            }
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedTextField(
                            value = confirmPassword,
                            onValueChange = { confirmPassword = it },
                            label = { Text("Xác nhận mật khẩu mới") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            visualTransformation = if (newPasswordVisible) androidx.compose.ui.text.input.VisualTransformation.None else androidx.compose.ui.text.input.PasswordVisualTransformation()
                        )
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            if (newPassword.length < 8) {
                                Toast.makeText(context, "Mật khẩu mới phải từ 8 ký tự trở lên", Toast.LENGTH_SHORT).show()
                                return@Button
                            }
                            if (newPassword != confirmPassword) {
                                Toast.makeText(context, "Mật khẩu xác nhận không khớp", Toast.LENGTH_SHORT).show()
                                return@Button
                            }
                            viewModel.changePassword(oldPassword, newPassword)
                            showChangePasswordDialog = false
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF3B82F6))
                    ) {
                        Text("Xác nhận", color = Color.White)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showChangePasswordDialog = false }) {
                        Text("Hủy", color = Color.Gray)
                    }
                },
                shape = RoundedCornerShape(16.dp)
            )
        }

        if (showDatePicker) {
            val datePickerState = rememberDatePickerState()
            DatePickerDialog(
                onDismissRequest = { showDatePicker = false },
                confirmButton = {
                    TextButton(onClick = {
                        datePickerState.selectedDateMillis?.let {
                            val formatter = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                            viewModel.onDobChange(formatter.format(Date(it)))
                        }
                        showDatePicker = false
                    }) { Text(stringResource(R.string.profile_btn_select_date)) }
                },
                dismissButton = {
                    TextButton(onClick = { showDatePicker = false }) {
                        Text(
                            stringResource(R.string.profile_btn_close)
                        )
                    }
                }
            ) {
                DatePicker(state = datePickerState)
            }
        }

        if (showCitySheet) {
            ModalBottomSheet(
                onDismissRequest = { showCitySheet = false },
                sheetState = rememberModalBottomSheetState(),
                containerColor = Color.White
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 32.dp)
                ) {
                    Text(
                        stringResource(R.string.profile_label_city),
                        modifier = Modifier.padding(16.dp),
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                    if (uiState.isLoadingCities) {
                        CircularProgressIndicator(
                            modifier = Modifier
                                .align(Alignment.CenterHorizontally)
                                .padding(16.dp)
                        )
                    }
                    LazyColumn {
                        items(uiState.cities.size) { index ->
                            val city = uiState.cities[index]
                            ListItem(
                                headlineContent = {
                                    Text(
                                        city.name,
                                        fontWeight = FontWeight.Medium
                                    )
                                },
                                modifier = Modifier.clickable {
                                    viewModel.onCitySelected(city.id)
                                    showCitySheet = false
                                },
                                trailingContent = {
                                    if (uiState.selectedCityId == city.id) {
                                        Icon(Icons.Default.Check, null, tint = Color(0xFF3B82F6))
                                    }
                                }
                            )
                        }
                    }
                }
            }
        }

        if (showWardSheet) {
            ModalBottomSheet(
                onDismissRequest = { showWardSheet = false },
                sheetState = rememberModalBottomSheetState(),
                containerColor = Color.White
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 32.dp)
                ) {
                    Text(
                        stringResource(R.string.profile_label_ward),
                        modifier = Modifier.padding(16.dp),
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                    if (uiState.isLoadingWards) {
                        CircularProgressIndicator(
                            modifier = Modifier
                                .align(Alignment.CenterHorizontally)
                                .padding(16.dp)
                        )
                    }
                    LazyColumn {
                        items(uiState.wards.size) { index ->
                            val ward = uiState.wards[index]
                            ListItem(
                                headlineContent = {
                                    Text(
                                        ward.name,
                                        fontWeight = FontWeight.Medium
                                    )
                                },
                                modifier = Modifier.clickable {
                                    viewModel.onWardSelected(ward.id)
                                    showWardSheet = false
                                },
                                trailingContent = {
                                    if (uiState.selectedWardId == ward.id) {
                                        Icon(Icons.Default.Check, null, tint = Color(0xFF3B82F6))
                                    }
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun PremiumHeaderBackground() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(350.dp)
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(Color(0xFF0F172A), Color(0xFF1E293B))
                )
            )
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(Color(0xFF3B82F6).copy(alpha = 0.15f), Color.Transparent),
                    center = Offset(size.width * 0.1f, size.height * 0.2f),
                    radius = size.width * 0.8f
                ),
                radius = size.width * 0.8f,
                center = Offset(size.width * 0.1f, size.height * 0.2f)
            )
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(Color(0xFF8B5CF6).copy(alpha = 0.1f), Color.Transparent),
                    center = Offset(size.width * 0.9f, size.height * 0.8f),
                    radius = size.width * 0.6f
                ),
                radius = size.width * 0.6f,
                center = Offset(size.width * 0.9f, size.height * 0.8f)
            )
        }
    }
}

@Composable
fun PremiumSectionTitle(title: String, icon: ImageVector) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.padding(start = 4.dp, bottom = 12.dp)
    ) {
        Icon(icon, null, modifier = Modifier.size(16.dp), tint = Color(0xFF64748B))
        Spacer(Modifier.width(8.dp))
        Text(
            text = title,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF64748B),
            letterSpacing = 1.2.sp
        )
    }
}

@Composable
fun PremiumProfileCard(content: @Composable ColumnScope.() -> Unit) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(28.dp),
        color = Color.White,
        shadowElevation = 2.dp,
        border = BorderStroke(1.dp, Color(0xFFF1F5F9))
    ) {
        Column(content = content)
    }
}

@Composable
fun PremiumDivider() {
    HorizontalDivider(
        modifier = Modifier.padding(start = 64.dp),
        thickness = 1.dp,
        color = Color(0xFFF1F5F9)
    )
}

@Composable
fun EditablePremiumItem(
    icon: ImageVector,
    label: String,
    value: String,
    isEditing: Boolean,
    onValueChange: (String) -> Unit,
    keyboardType: KeyboardType = KeyboardType.Text
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(20.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(44.dp)
                .background(Color(0xFFF1F5F9), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, null, modifier = Modifier.size(22.dp), tint = Color(0xFF1E293B))
        }
        Spacer(Modifier.width(20.dp))
        Column(Modifier.weight(1f)) {
            Text(label, fontSize = 12.sp, color = Color(0xFF94A3B8), fontWeight = FontWeight.Bold)
            if (isEditing) {
                BasicTextField(
                    value = value,
                    onValueChange = onValueChange,
                    textStyle = MaterialTheme.typography.bodyLarge.copy(
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF0F172A)
                    ),
                    keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
                )
                Box(
                    Modifier
                        .fillMaxWidth()
                        .height(1.5.dp)
                        .background(Color(0xFF3B82F6))
                )
            } else {
                Text(
                    text = value.ifBlank { stringResource(R.string.profile_not_updated) },
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (value.isBlank()) Color(0xFFCBD5E1) else Color(0xFF0F172A)
                )
            }
        }
    }
}

@Composable
fun PremiumGenderSelector(gender: String, isEditing: Boolean, onGenderSelected: (String) -> Unit) {
    val options = mapOf(
        "male" to stringResource(R.string.profile_gender_male),
        "female" to stringResource(R.string.profile_gender_female),
        "other" to stringResource(R.string.profile_gender_other)
    )
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(20.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(44.dp)
                .background(Color(0xFFF1F5F9), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(Icons.Rounded.Wc, null, modifier = Modifier.size(22.dp), tint = Color(0xFF1E293B))
        }
        Spacer(Modifier.width(20.dp))
        Column(Modifier.weight(1f)) {
            Text(
                stringResource(R.string.reg_gender),
                fontSize = 12.sp,
                color = Color(0xFF94A3B8),
                fontWeight = FontWeight.Bold
            )
            if (isEditing) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 10.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    options.forEach { (key, label) ->
                        val selected = gender == key
                        Surface(
                            modifier = Modifier
                                .weight(1f)
                                .clickable { onGenderSelected(key) },
                            shape = RoundedCornerShape(14.dp),
                            color = if (selected) Color(0xFF0F172A) else Color(0xFFF1F5F9),
                        ) {
                            Text(
                                label,
                                modifier = Modifier.padding(vertical = 10.dp),
                                textAlign = TextAlign.Center,
                                fontWeight = FontWeight.Bold,
                                color = if (selected) Color.White else Color(0xFF64748B),
                                fontSize = 14.sp
                            )
                        }
                    }
                }
            } else {
                Text(
                    options[gender] ?: stringResource(R.string.profile_gender_not_selected),
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF0F172A)
                )
            }
        }
    }
}

@Composable
fun PremiumDatePickerItem(dob: String, isEditing: Boolean, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(20.dp)
            .clickable(enabled = isEditing) { onClick() },
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(44.dp)
                .background(Color(0xFFF1F5F9), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                Icons.Rounded.CalendarToday,
                null,
                modifier = Modifier.size(20.dp),
                tint = Color(0xFF1E293B)
            )
        }
        Spacer(Modifier.width(20.dp))
        Column(Modifier.weight(1f)) {
            Text(
                stringResource(R.string.profile_label_dob),
                fontSize = 12.sp,
                color = Color(0xFF94A3B8),
                fontWeight = FontWeight.Bold
            )
            Text(
                dob.ifBlank { stringResource(R.string.profile_not_updated) },
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF0F172A)
            )
        }
        if (isEditing) Icon(
            Icons.AutoMirrored.Filled.KeyboardArrowRight,
            null,
            tint = Color(0xFFCBD5E1)
        )
    }
}

@Composable
fun PremiumLocationSelector(
    label: String,
    value: String,
    isEditing: Boolean,
    enabled: Boolean = true,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(20.dp)
            .clickable(enabled = isEditing && enabled) { onClick() },
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(44.dp)
                .background(if (enabled) Color(0xFFF1F5F9) else Color(0xFFF8FAFC), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                Icons.Rounded.LocationOn,
                null,
                modifier = Modifier.size(22.dp),
                tint = if (enabled) Color(0xFF1E293B) else Color(0xFFCBD5E1)
            )
        }
        Spacer(Modifier.width(20.dp))
        Column(Modifier.weight(1f)) {
            Text(label, fontSize = 12.sp, color = Color(0xFF94A3B8), fontWeight = FontWeight.Bold)
            Text(
                value.ifBlank { stringResource(R.string.profile_gender_not_selected) },
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = if (enabled) Color(0xFF0F172A) else Color(0xFFCBD5E1)
            )
        }
        if (isEditing && enabled) Icon(
            Icons.AutoMirrored.Filled.KeyboardArrowRight,
            null,
            tint = Color(0xFFCBD5E1)
        )
    }
}

@Composable
fun PremiumSettingsItem(
    icon: ImageVector,
    label: String,
    value: String,
    color: Color,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(20.dp)
            .clickable { onClick() },
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(44.dp)
                .background(color.copy(alpha = 0.1f), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, null, modifier = Modifier.size(20.dp), tint = color)
        }
        Spacer(Modifier.width(20.dp))
        Column(Modifier.weight(1f)) {
            Text(
                label,
                fontSize = 16.sp,
                fontWeight = FontWeight.ExtraBold,
                color = Color(0xFF0F172A)
            )
            Text(value, fontSize = 12.sp, color = Color(0xFF64748B), fontWeight = FontWeight.Medium)
        }
        Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, null, tint = Color(0xFFCBD5E1))
    }
}
