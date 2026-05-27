package com.tanh.datsan.ui.profile

import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.*
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.draw.*
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.*
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.tanh.datsan.utils.toFile
import com.tanh.datsan.utils.toFullImageUrl
import com.tanh.datsan.viewmodel.ProfileViewModel

@Composable
fun ProfileScreen(
    viewModel: ProfileViewModel = hiltViewModel(),
    onBackClick: () -> Unit = {},
    onLogoutClick: () -> Unit = {}
) {
    val context = LocalContext.current
    val profile by viewModel.profileState.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val toastMessage by viewModel.toastMessage.collectAsState()
    val avatarUrl by viewModel.userAvatarUrl.collectAsState()
    val initialUserName by viewModel.userName.collectAsState()
    val initialPhone by viewModel.userPhone.collectAsState()
    val initialAddress by viewModel.userAddress.collectAsState()

    // Local states for editing
    var fullName by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var address by remember { mutableStateOf("") }
    var isEditing by remember { mutableStateOf(false) }
    var showLogoutDialog by remember { mutableStateOf(false) }

    // Khởi tạo giá trị từ TokenManager ngay khi vào màn hình để hiển thị ngay lập tức
    LaunchedEffect(initialUserName, initialPhone, initialAddress) {
        if (!isEditing) {
            if (fullName.isEmpty()) fullName = initialUserName ?: ""
            if (phone.isEmpty()) phone = initialPhone ?: ""
            if (address.isEmpty()) address = initialAddress ?: ""
        }
    }

    // Sync local state when profile data arrives
    LaunchedEffect(profile) {
        profile?.userProfile?.let {
            fullName = it.fullName ?: ""
            phone = it.phoneNumber ?: ""
            address = it.address ?: ""
        }
    }

    // Handle Toast
    LaunchedEffect(toastMessage) {
        toastMessage?.let {
            Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
            viewModel.clearToast()
        }
    }

    // Image Picker Launcher
    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        uri?.let {
            val file = it.toFile(context)
            if (file != null) {
                viewModel.uploadAvatar(file)
            }
        }
    }

    val gradientColors = listOf(Color(0xFF0056B3), Color(0xFF00A2FF))

    Box(modifier = Modifier.fillMaxSize().background(Color(0xFFF5F7FA))) {

        // ── HEADER nền xanh gradient ──
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
                    Icon(
                        Icons.Default.ArrowBack,
                        contentDescription = "Quay lại",
                        tint = Color.White
                    )
                }
                Text(
                    "Thông tin cá nhân",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                )
                IconButton(onClick = { 
                    if (isEditing) {
                        // Cancel editing -> reset state
                        profile?.userProfile?.let {
                            fullName = it.fullName ?: ""
                            phone = it.phoneNumber ?: ""
                            address = it.address ?: ""
                        }
                    }
                    isEditing = !isEditing 
                }) {
                    Icon(
                        if (isEditing) Icons.Default.Close else Icons.Default.Edit,
                        contentDescription = "Chỉnh sửa",
                        tint = Color.White
                    )
                }
            }

            // ── AVATAR ──
            Box(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                Box(contentAlignment = Alignment.BottomEnd) {
                    // Avatar circle
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
                        
                        if (isLoading) {
                            Box(
                                modifier = Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.3f)),
                                contentAlignment = Alignment.Center
                            ) {
                                CircularProgressIndicator(color = Color.White, modifier = Modifier.size(30.dp))
                            }
                        }
                    }

                    // Nút camera đổi ảnh
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF007BFF))
                            .border(2.dp, Color.White, CircleShape)
                            .clickable { imagePickerLauncher.launch("image/*") },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Default.CameraAlt,
                            contentDescription = "Đổi ảnh",
                            tint = Color.White,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Tên hiển thị
            Text(
                text = profile?.userProfile?.fullName ?: "Người dùng",
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center,
                fontWeight = FontWeight.Bold,
                fontSize = 22.sp,
                color = Color(0xFF1A1A2E)
            )
            Text(
                text = profile?.email ?: "",
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center,
                fontSize = 14.sp,
                color = Color.Gray
            )

            Spacer(modifier = Modifier.height(24.dp))

            // ── CARD THÔNG TIN ──
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text(
                        "Thông tin tài khoản",
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = Color(0xFF0056B3)
                    )
                    Spacer(modifier = Modifier.height(16.dp))

                    ProfileField(
                        icon = Icons.Default.Person,
                        label = "Họ và tên",
                        value = fullName,
                        isEditing = isEditing,
                        onValueChange = { fullName = it }
                    )
                    HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp), color = Color(0xFFF0F0F0))

                    ProfileField(
                        icon = Icons.Default.Phone,
                        label = "Số điện thoại",
                        value = phone,
                        isEditing = isEditing,
                        onValueChange = { phone = it }
                    )
                    HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp), color = Color(0xFFF0F0F0))

                    ProfileField(
                        icon = Icons.Default.LocationOn,
                        label = "Địa chỉ",
                        value = address,
                        isEditing = isEditing,
                        onValueChange = { address = it }
                    )
                }
            }

            // ── NÚT LƯU ──
            AnimatedVisibility(
                visible = isEditing,
                enter = fadeIn() + expandVertically(),
                exit = fadeOut() + shrinkVertically()
            ) {
                Button(
                    onClick = {
                        viewModel.updateProfile(fullName, phone, address)
                        isEditing = false
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                        .height(52.dp),
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

            // ── NÚT ĐĂNG XUẤT ──
            OutlinedButton(
                onClick = { showLogoutDialog = true },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .height(52.dp),
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(1.5.dp, Color(0xFFE53935).copy(alpha = 0.5f)),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFE53935))
            ) {
                Icon(Icons.Default.Logout, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Đăng xuất", fontWeight = FontWeight.Bold, fontSize = 16.sp)
            }

            Spacer(modifier = Modifier.height(32.dp))
        }

        // Loading Overlay for full screen actions
        if (isLoading && !isEditing) {
            Box(
                modifier = Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.1f)),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = Color(0xFF007BFF))
            }
        }

        // ── DIALOG XÁC NHẬN ĐĂNG XUẤT ──
        if (showLogoutDialog) {
            AlertDialog(
                onDismissRequest = { showLogoutDialog = false },
                shape = RoundedCornerShape(16.dp),
                title = { Text("Đăng xuất", fontWeight = FontWeight.Bold) },
                text = { Text("Bạn có chắc chắn muốn đăng xuất khỏi tài khoản này?") },
                confirmButton = {
                    Button(
                        onClick = {
                            showLogoutDialog = false
                            viewModel.logout()
                            onLogoutClick()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE53935))
                    ) {
                        Text("Đăng xuất", color = Color.White)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showLogoutDialog = false }) {
                        Text("Huỷ", color = Color(0xFF007BFF))
                    }
                }
            )
        }
    }
}

@Composable
fun ProfileField(
    icon: ImageVector,
    label: String,
    value: String,
    isEditing: Boolean,
    onValueChange: (String) -> Unit
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.fillMaxWidth()
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(Color(0xFFE3F2FD)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                icon,
                contentDescription = null,
                tint = Color(0xFF007BFF),
                modifier = Modifier.size(20.dp)
            )
        }
        Spacer(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(label, fontSize = 12.sp, color = Color.Gray)
            if (isEditing) {
                BasicTextField(
                    value = value,
                    onValueChange = onValueChange,
                    textStyle = androidx.compose.ui.text.TextStyle(
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color(0xFF1A1A2E)
                    ),
                    modifier = Modifier.fillMaxWidth().padding(top = 4.dp),
                    decorationBox = { innerTextField ->
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .border(1.dp, Color(0xFF007BFF).copy(alpha = 0.3f), RoundedCornerShape(8.dp))
                                .padding(horizontal = 12.dp, vertical = 8.dp)
                        ) {
                            if (value.isEmpty()) {
                                Text("Nhập $label...", color = Color.LightGray, fontSize = 15.sp)
                            }
                            innerTextField()
                        }
                    }
                )
            } else {
                Text(
                    text = value.ifEmpty { "Chưa cập nhật" },
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Medium,
                    color = if (value.isEmpty()) Color.LightGray else Color(0xFF1A1A2E),
                    modifier = Modifier.padding(top = 2.dp)
                )
            }
        }
    }
}
