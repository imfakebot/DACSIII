package com.tanh.datsan.ui.profile

import android.annotation.SuppressLint
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.rounded.AccountCircle
import androidx.compose.material.icons.rounded.Badge
import androidx.compose.material.icons.rounded.CalendarToday
import androidx.compose.material.icons.rounded.Description
import androidx.compose.material.icons.rounded.Explore
import androidx.compose.material.icons.rounded.Feedback
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material.icons.rounded.LocationOn
import androidx.compose.material.icons.rounded.LockReset
import androidx.compose.material.icons.rounded.Palette
import androidx.compose.material.icons.rounded.Phone
import androidx.compose.material.icons.rounded.Security
import androidx.compose.material.icons.rounded.Translate
import androidx.compose.material.icons.rounded.Wc
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import coil.compose.AsyncImage
import com.tanh.datsan.R
import com.tanh.datsan.utils.compressImage
import com.tanh.datsan.utils.toFile
import com.tanh.datsan.utils.toFullImageUrl
import com.tanh.datsan.ui.home.main.MainViewModel
import com.tanh.datsan.ui.profile.ProfileViewModel
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import com.tanh.datsan.ui.profile.components.*

@SuppressLint("LocalContextGetResourceValueCall")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    onBackClick: () -> Unit,
    onLogoutClick: () -> Unit,
    onNavigateToFeedbacks: () -> Unit = {},
    showBackButton: Boolean = true,
    viewModel: ProfileViewModel = hiltViewModel(),
    mainViewModel: MainViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsState()

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

    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        uri?.let {
            val file = it.toFile(context)
            if (file != null) {
                pendingAvatarFile = file
                showAvatarConfirmDialog = true
            } else {
                Toast.makeText(context, context.getString(R.string.error_unknown), Toast.LENGTH_SHORT)
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
                                if (uiState.isLoading) {
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
                                label = stringResource(R.string.profile_label_feedback),
                                value = stringResource(R.string.profile_desc_feedback),
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
                                    Toast.makeText(context, context.getString(R.string.error_upload_image, e.message), Toast.LENGTH_SHORT).show()
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
                        showLogoutDialog = false
                        onLogoutClick()
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
            ChangePasswordDialog(
                onConfirm = { old, new, confirm ->
                    viewModel.changePassword(old, new, confirm)
                    showChangePasswordDialog = false
                },
                onDismiss = { showChangePasswordDialog = false }
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
            LocationPickerSheet(
                title = stringResource(R.string.profile_label_city),
                items = uiState.cities.map { it.id to it.name },
                selectedId = uiState.selectedCityId,
                isLoading = uiState.isLoadingCities,
                onSelect = { viewModel.onCitySelected(it) },
                onDismiss = { showCitySheet = false }
            )
        }

        if (showWardSheet) {
            LocationPickerSheet(
                title = stringResource(R.string.profile_label_ward),
                items = uiState.wards.map { it.id to it.name },
                selectedId = uiState.selectedWardId,
                isLoading = uiState.isLoadingWards,
                onSelect = { viewModel.onWardSelected(it) },
                onDismiss = { showWardSheet = false }
            )
        }
    }
}
