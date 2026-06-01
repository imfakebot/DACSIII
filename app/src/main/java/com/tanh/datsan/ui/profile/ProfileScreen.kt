package com.tanh.datsan.ui.profile

import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.draw.*
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.*
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.tanh.datsan.utils.toFile
import com.tanh.datsan.utils.toFullImageUrl
import com.tanh.datsan.viewmodel.ProfileViewModel
import com.tanh.datsan.viewmodel.MainViewModel
import com.tanh.datsan.viewmodel.AuthViewModel
import com.tanh.datsan.viewmodel.AuthUiEvent
import kotlinx.coroutines.flow.collectLatest
import java.util.*
import java.text.SimpleDateFormat

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    viewModel: ProfileViewModel = hiltViewModel(),
    mainViewModel: MainViewModel = hiltViewModel(),
    authViewModel: AuthViewModel = hiltViewModel(),
    onBackClick: () -> Unit = {},
    onLogoutClick: () -> Unit = {},
    onNavigateToResetPassword: (String) -> Unit = {}
) {
    val context = LocalContext.current
    
    // States from ViewModel
    val profile by viewModel.profileState.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val isEditing by viewModel.isEditing.collectAsState()
    val authIsLoading by authViewModel.isLoading.collectAsState()
    val toastMessage by viewModel.toastMessage.collectAsState()
    val avatarUrl by viewModel.userAvatarUrl.collectAsState()
    
    val cities by viewModel.cities.collectAsState()
    val wards by viewModel.wards.collectAsState()

    // Editable fields from ViewModel
    val fullName by viewModel.fullName.collectAsState()
    val phoneNumber by viewModel.phoneNumber.collectAsState()
    val gender by viewModel.gender.collectAsState()
    val dateOfBirth by viewModel.dateOfBirth.collectAsState()
    val bio by viewModel.bio.collectAsState()
    val street by viewModel.street.collectAsState()
    val selectedCityId by viewModel.selectedCityId.collectAsState()
    val selectedWardId by viewModel.selectedWardId.collectAsState()

    // Global settings from MainViewModel
    val currentTheme by mainViewModel.theme.collectAsState()
    val currentLanguage by mainViewModel.language.collectAsState()

    // Dialog states
    var showLogoutDialog by remember { mutableStateOf(false) }
    var showAvatarConfirmDialog by remember { mutableStateOf(false) }
    var pendingAvatarFile by remember { mutableStateOf<java.io.File?>(null) }
    var showDatePicker by remember { mutableStateOf(false) }

    // Handle Toast
    LaunchedEffect(toastMessage) {
        toastMessage?.let {
            Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
            viewModel.clearToast()
        }
    }

    // Handle Auth UI Events
    LaunchedEffect(Unit) {
        authViewModel.uiEvent.collectLatest { event ->
            when (event) {
                is AuthUiEvent.NavigateToResetPassword -> {
                    onNavigateToResetPassword(event.email)
                }
                is AuthUiEvent.ShowToast -> {
                    Toast.makeText(context, event.message, Toast.LENGTH_SHORT).show()
                }
                else -> {}
            }
        }
    }

    // Image Picker Launcher
    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        uri?.let {
            val file = it.toFile(context)
            if (file != null) {
                pendingAvatarFile = file
                showAvatarConfirmDialog = true
            }
        }
    }

    val gradientColors = listOf(Color(0xFF0056B3), Color(0xFF00A2FF))

    Box(modifier = Modifier.fillMaxSize().background(if (isSystemInDarkTheme()) Color(0xFF121212) else Color(0xFFF5F7FA))) {

        // ── HEADER ──
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(220.dp)
                .background(Brush.verticalGradient(gradientColors))
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(androidx.compose.foundation.rememberScrollState())
        ) {
            // Header buttons
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 8.dp)
                    .statusBarsPadding(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBackClick) {
                    Icon(Icons.Default.ArrowBack, contentDescription = "Quay lại", tint = Color.White)
                }
                Text("Thông tin cá nhân", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                IconButton(onClick = { 
                    viewModel.toggleEditing(!isEditing)
                }) {
                    Icon(if (isEditing) Icons.Default.Close else Icons.Default.Edit, contentDescription = "Chỉnh sửa", tint = Color.White)
                }
            }

            // ── AVATAR ──
            Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                Box(contentAlignment = Alignment.BottomEnd) {
                    Box(
                        modifier = Modifier
                            .size(110.dp)
                            .clip(CircleShape)
                            .background(Color.White)
                            .border(3.dp, Color.White, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        AsyncImage(
                            model = avatarUrl?.toFullImageUrl(),
                            contentDescription = "Avatar",
                            modifier = Modifier.fillMaxSize().clip(CircleShape),
                            contentScale = ContentScale.Crop,
                            placeholder = androidx.compose.ui.res.painterResource(id = com.tanh.datsan.R.drawable.avartar_default),
                            error = androidx.compose.ui.res.painterResource(id = com.tanh.datsan.R.drawable.avartar_default)
                        )
                        if (isLoading || authIsLoading) {
                            Box(modifier = Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.3f)), contentAlignment = Alignment.Center) {
                                CircularProgressIndicator(color = Color.White, modifier = Modifier.size(30.dp))
                            }
                        }
                    }
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF007BFF))
                            .border(2.dp, Color.White, CircleShape)
                            .clickable { imagePickerLauncher.launch("image/*") },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.CameraAlt, contentDescription = "Đổi ảnh", tint = Color.White, modifier = Modifier.size(20.dp))
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))
            Text(text = fullName.ifEmpty { "Người dùng" }, modifier = Modifier.fillMaxWidth(), textAlign = TextAlign.Center, fontWeight = FontWeight.Bold, fontSize = 22.sp, color = Color.White)
            Text(text = profile?.email ?: "", modifier = Modifier.fillMaxWidth(), textAlign = TextAlign.Center, fontSize = 14.sp, color = Color.White.copy(alpha = 0.8f))

            Spacer(modifier = Modifier.height(24.dp))

            // ── CARD THÔNG TIN TÀI KHOẢN ──
            Card(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text("Thông tin tài khoản", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Color(0xFF0056B3))
                    Spacer(modifier = Modifier.height(16.dp))

                    ProfileField(icon = Icons.Default.Person, label = "Họ và tên", value = fullName, isEditing = isEditing, onValueChange = { viewModel.fullName.value = it })
                    HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp), color = Color(0xFFF0F0F0))

                    ProfileField(icon = Icons.Default.Phone, label = "Số điện thoại", value = phoneNumber, isEditing = isEditing, onValueChange = { viewModel.phoneNumber.value = it }, keyboardType = KeyboardType.Phone)
                    HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp), color = Color(0xFFF0F0F0))

                    GenderDropdown(gender = gender, isEditing = isEditing, onGenderSelected = { viewModel.gender.value = it })
                    HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp), color = Color(0xFFF0F0F0))

                    BirthdayPicker(dob = dateOfBirth, isEditing = isEditing, onClick = { showDatePicker = true })
                    HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp), color = Color(0xFFF0F0F0))

                    ProfileField(icon = Icons.Default.Info, label = "Tiểu sử", value = bio, isEditing = isEditing, onValueChange = { viewModel.bio.value = it })
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // ── CARD ĐỊA CHỈ ──
            Card(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text("Địa chỉ liên hệ", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Color(0xFF0056B3))
                    Spacer(modifier = Modifier.height(16.dp))

    val isLoadingCities by viewModel.isLoadingCities.collectAsState()

    // ... (phần code khác)

                    LocationDropdown(
                        label = "Tỉnh / Thành phố",
                        items = cities.map { it.id to it.name },
                        selectedId = selectedCityId,
                        isEditing = isEditing,
                        onItemSelected = { viewModel.onCitySelected(it) },
                        placeholderText = if (isLoadingCities) "Đang tải dữ liệu..." else if (cities.isEmpty()) "Không có dữ liệu" else "Chọn Tỉnh / Thành phố"
                    )
                    HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp), color = Color(0xFFF0F0F0))

                    LocationDropdown(
                        label = "Quận / Huyện / Phường",
                        items = wards.map { it.id to it.name },
                        selectedId = selectedWardId,
                        isEditing = isEditing,
                        onItemSelected = { viewModel.selectedWardId.value = it },
                        enabled = selectedCityId != null,
                        placeholderText = if (selectedCityId == null) "Vui lòng chọn Tỉnh/Thành trước" else if (wards.isEmpty()) "Đang tải dữ liệu..." else "Chọn Quận / Huyện"
                    )
                    HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp), color = Color(0xFFF0F0F0))

                    ProfileField(icon = Icons.Default.Map, label = "Số nhà, tên đường", value = street, isEditing = isEditing, onValueChange = { viewModel.street.value = it })
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // ── CARD CÀI ĐẶT ──
            Card(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text("Cài đặt & Bảo mật", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Color(0xFF0056B3))
                    Spacer(modifier = Modifier.height(16.dp))

                    SettingItem(icon = if (currentTheme == "dark") Icons.Default.DarkMode else Icons.Default.LightMode, label = "Chế độ hiển thị", value = when (currentTheme) { "light" -> "Sáng"; "dark" -> "Tối"; else -> "Theo hệ thống" }, onClick = { mainViewModel.setTheme(if (currentTheme == "light") "dark" else if (currentTheme == "dark") "system" else "light") })
                    HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp), color = Color(0xFFF0F0F0))
                    SettingItem(icon = Icons.Default.Language, label = "Ngôn ngữ", value = if (currentLanguage == "vi") "Tiếng Việt" else "English", onClick = { mainViewModel.setLanguage(if (currentLanguage == "vi") "en" else "vi") })
                    HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp), color = Color(0xFFF0F0F0))
                    SettingItem(icon = Icons.Default.Lock, label = "Đổi mật khẩu", value = "Gửi mã xác nhận qua email", onClick = { profile?.email?.let { authViewModel.forgotPassword(it) } })
                }
            }

            // ── NÚT LƯU ──
            AnimatedVisibility(visible = isEditing, enter = fadeIn() + expandVertically(), exit = fadeOut() + shrinkVertically()) {
                Button(
                    onClick = { viewModel.updateProfile() },
                    modifier = Modifier.fillMaxWidth().padding(16.dp).height(52.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF007BFF)),
                    enabled = !isLoading
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                    } else {
                        Icon(Icons.Default.Save, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Lưu thay đổi", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))
            OutlinedButton(onClick = { showLogoutDialog = true }, modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp).height(52.dp), shape = RoundedCornerShape(12.dp), border = BorderStroke(1.5.dp, Color(0xFFE53935).copy(alpha = 0.5f)), colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFE53935))) {
                Icon(Icons.Default.Logout, contentDescription = null); Spacer(modifier = Modifier.width(8.dp)); Text("Đăng xuất", fontWeight = FontWeight.Bold, fontSize = 16.sp)
            }
            Spacer(modifier = Modifier.height(32.dp))
        }

        // DIALOGS
        if (showLogoutDialog) {
            AlertDialog(onDismissRequest = { showLogoutDialog = false }, shape = RoundedCornerShape(16.dp), title = { Text("Đăng xuất", fontWeight = FontWeight.Bold) }, text = { Text("Bạn có chắc chắn muốn đăng xuất khỏi tài khoản này?") }, confirmButton = { Button(onClick = { showLogoutDialog = false; viewModel.logout(); onLogoutClick() }, colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE53935))) { Text("Đăng xuất", color = Color.White) } }, dismissButton = { TextButton(onClick = { showLogoutDialog = false }) { Text("Huỷ", color = Color(0xFF007BFF)) } })
        }

        if (showAvatarConfirmDialog) {
            AlertDialog(onDismissRequest = { showAvatarConfirmDialog = false }, shape = RoundedCornerShape(16.dp), title = { Text("Đổi ảnh đại diện", fontWeight = FontWeight.Bold) }, text = { Text("Bạn có chắc chắn muốn thay đổi ảnh đại diện này không?") }, confirmButton = { Button(onClick = { showAvatarConfirmDialog = false; pendingAvatarFile?.let { viewModel.uploadAvatar(it) } }, colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF007BFF))) { Text("Đồng ý", color = Color.White) } }, dismissButton = { TextButton(onClick = { showAvatarConfirmDialog = false; pendingAvatarFile = null }) { Text("Huỷ", color = Color.Gray) } })
        }

        if (showDatePicker) {
            val datePickerState = rememberDatePickerState()
            DatePickerDialog(
                onDismissRequest = { showDatePicker = false },
                confirmButton = {
                    TextButton(onClick = {
                        val selectedDate = datePickerState.selectedDateMillis
                        if (selectedDate != null) {
                            val formatter = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                            viewModel.dateOfBirth.value = formatter.format(Date(selectedDate))
                        }
                        showDatePicker = false
                    }) { Text("Chọn") }
                },
                dismissButton = { TextButton(onClick = { showDatePicker = false }) { Text("Huỷ") } }
            ) {
                DatePicker(state = datePickerState)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GenderDropdown(gender: String, isEditing: Boolean, onGenderSelected: (String) -> Unit) {
    val options = listOf("male", "female", "other")
    val labels = mapOf("male" to "Nam", "female" to "Nữ", "other" to "Khác")
    var expanded by remember { mutableStateOf(false) }

    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
        Box(modifier = Modifier.size(40.dp).clip(CircleShape).background(Color(0xFFE3F2FD)), contentAlignment = Alignment.Center) {
            Icon(Icons.Default.Wc, contentDescription = null, tint = Color(0xFF007BFF), modifier = Modifier.size(20.dp))
        }
        Spacer(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text("Giới tính", fontSize = 12.sp, color = Color.Gray)
            if (isEditing) {
                ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = it }) {
                    OutlinedTextField(
                        value = labels[gender] ?: "Chọn giới tính",
                        onValueChange = {},
                        readOnly = true,
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                        modifier = Modifier.menuAnchor().fillMaxWidth(),
                        textStyle = LocalTextStyle.current.copy(fontSize = 15.sp),
                        colors = OutlinedTextFieldDefaults.colors(
                            unfocusedBorderColor = Color(0xFF007BFF).copy(alpha = 0.3f),
                            focusedBorderColor = Color(0xFF007BFF)
                        ),
                        shape = RoundedCornerShape(8.dp)
                    )
                    ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                        options.forEach { option ->
                            DropdownMenuItem(text = { Text(labels[option]!!) }, onClick = { onGenderSelected(option); expanded = false })
                        }
                    }
                }
            } else {
                Text(text = labels[gender] ?: "Chưa cập nhật", fontSize = 15.sp, fontWeight = FontWeight.Medium, color = if (gender.isEmpty()) Color.LightGray else MaterialTheme.colorScheme.onSurface, modifier = Modifier.padding(top = 2.dp))
            }
        }
    }
}

@Composable
fun BirthdayPicker(dob: String, isEditing: Boolean, onClick: () -> Unit) {
    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth().clickable(enabled = isEditing) { onClick() }) {
        Box(modifier = Modifier.size(40.dp).clip(CircleShape).background(Color(0xFFE3F2FD)), contentAlignment = Alignment.Center) {
            Icon(Icons.Default.CalendarToday, contentDescription = null, tint = Color(0xFF007BFF), modifier = Modifier.size(20.dp))
        }
        Spacer(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text("Ngày sinh", fontSize = 12.sp, color = Color.Gray)
            Text(text = dob.ifEmpty { "Chọn ngày sinh" }, fontSize = 15.sp, fontWeight = FontWeight.Medium, color = if (dob.isEmpty()) Color.LightGray else MaterialTheme.colorScheme.onSurface, modifier = Modifier.padding(top = 2.dp))
        }
        if (isEditing) Icon(Icons.Default.ArrowDropDown, contentDescription = null, tint = Color.Gray)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LocationDropdown(
    label: String, 
    items: List<Pair<Int, String>>, 
    selectedId: Int?, 
    isEditing: Boolean, 
    onItemSelected: (Int) -> Unit, 
    enabled: Boolean = true,
    placeholderText: String? = null
) {
    var expanded by remember { mutableStateOf(false) }
    val selectedName = items.find { it.first == selectedId }?.second ?: ""

    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
        Box(modifier = Modifier.size(40.dp).clip(CircleShape).background(Color(0xFFE3F2FD)), contentAlignment = Alignment.Center) {
            Icon(Icons.Default.LocationOn, contentDescription = null, tint = Color(0xFF007BFF), modifier = Modifier.size(20.dp))
        }
        Spacer(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(label, fontSize = 12.sp, color = Color.Gray)
            if (isEditing) {
                ExposedDropdownMenuBox(
                    expanded = expanded && enabled, 
                    onExpandedChange = { if (enabled) expanded = it },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    OutlinedTextField(
                        value = if (placeholderText != null && items.isEmpty() && enabled) placeholderText else selectedName.ifEmpty { placeholderText ?: "Chọn $label" },
                        onValueChange = {},
                        readOnly = true,
                        enabled = enabled && items.isNotEmpty(),
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                        modifier = Modifier.menuAnchor().fillMaxWidth(),
                        textStyle = LocalTextStyle.current.copy(fontSize = 15.sp),
                        shape = RoundedCornerShape(8.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            unfocusedBorderColor = Color(0xFF007BFF).copy(alpha = 0.3f),
                            focusedBorderColor = Color(0xFF007BFF),
                            disabledBorderColor = Color.LightGray.copy(alpha = 0.2f)
                        )
                    )
                    if (items.isNotEmpty()) {
                        ExposedDropdownMenu(
                            expanded = expanded && enabled, 
                            onDismissRequest = { expanded = false },
                            modifier = Modifier.background(MaterialTheme.colorScheme.surface)
                        ) {
                            items.forEach { (id, name) ->
                                DropdownMenuItem(
                                    text = { Text(name, fontSize = 15.sp) }, 
                                    onClick = { onItemSelected(id); expanded = false }
                                )
                            }
                        }
                    }
                }
            } else {
                Text(text = selectedName.ifEmpty { "Chưa cập nhật" }, fontSize = 15.sp, fontWeight = FontWeight.Medium, color = if (selectedName.isEmpty()) Color.LightGray else MaterialTheme.colorScheme.onSurface, modifier = Modifier.padding(top = 2.dp))
            }
        }
    }
}

@Composable
fun ProfileField(
    icon: ImageVector, 
    label: String, 
    value: String, 
    isEditing: Boolean, 
    onValueChange: (String) -> Unit,
    keyboardType: KeyboardType = KeyboardType.Text
) {
    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
        Box(modifier = Modifier.size(40.dp).clip(CircleShape).background(Color(0xFFE3F2FD)), contentAlignment = Alignment.Center) {
            Icon(icon, contentDescription = null, tint = Color(0xFF007BFF), modifier = Modifier.size(20.dp))
        }
        Spacer(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(label, fontSize = 12.sp, color = Color.Gray)
            if (isEditing) {
                OutlinedTextField(
                    value = value,
                    onValueChange = onValueChange,
                    modifier = Modifier.fillMaxWidth().padding(top = 4.dp),
                    textStyle = LocalTextStyle.current.copy(fontSize = 15.sp, fontWeight = FontWeight.Medium),
                    placeholder = { Text("Nhập $label...", color = Color.LightGray, fontSize = 15.sp) },
                    keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
                    colors = OutlinedTextFieldDefaults.colors(
                        unfocusedBorderColor = Color(0xFF007BFF).copy(alpha = 0.3f),
                        focusedBorderColor = Color(0xFF007BFF)
                    ),
                    shape = RoundedCornerShape(8.dp),
                    singleLine = label != "Tiểu sử"
                )
            } else {
                Text(text = value.ifEmpty { "Chưa cập nhật" }, fontSize = 15.sp, fontWeight = FontWeight.Medium, color = if (value.isEmpty()) Color.LightGray else MaterialTheme.colorScheme.onSurface, modifier = Modifier.padding(top = 2.dp))
            }
        }
    }
}

@Composable
fun SettingItem(icon: ImageVector, label: String, value: String, onClick: () -> Unit) {
    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth().clickable { onClick() }.padding(vertical = 4.dp)) {
        Box(modifier = Modifier.size(40.dp).clip(CircleShape).background(Color(0xFFE3F2FD).copy(alpha = 0.5f)), contentAlignment = Alignment.Center) {
            Icon(icon, contentDescription = null, tint = Color(0xFF007BFF), modifier = Modifier.size(20.dp))
        }
        Spacer(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(label, fontSize = 14.sp, fontWeight = FontWeight.Medium, color = MaterialTheme.colorScheme.onSurface)
            Text(value, fontSize = 12.sp, color = Color.Gray)
        }
        Icon(Icons.Default.ChevronRight, contentDescription = null, tint = Color.LightGray, modifier = Modifier.size(20.dp))
    }
}
