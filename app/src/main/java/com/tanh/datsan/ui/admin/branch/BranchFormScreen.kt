package com.tanh.datsan.ui.admin.branch

import android.content.Intent
import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
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
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.PersonOff
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.rounded.Business
import androidx.compose.material.icons.rounded.Description
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material.icons.rounded.LocationCity
import androidx.compose.material.icons.rounded.LocationOn
import androidx.compose.material.icons.rounded.ManageAccounts
import androidx.compose.material.icons.rounded.Map
import androidx.compose.material.icons.rounded.MyLocation
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material.icons.rounded.Phone
import androidx.compose.material.icons.rounded.Schedule
import androidx.compose.material.icons.rounded.Store
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.tanh.datsan.data.model.CityDto
import com.tanh.datsan.data.model.CreateBranchDto
import com.tanh.datsan.data.model.UpdateBranchDto
import com.tanh.datsan.data.model.WardDto
import com.tanh.datsan.viewmodel.BranchUiState
import androidx.core.net.toUri
import com.tanh.datsan.utils.Constants

// ─── Design tokens (shared with BranchScreen) ─────────────────────────────────
private val DarkBg      = Color(0xFF0F172A)
private val DarkBg2     = Color(0xFF1E293B)
private val AccentBlue  = Color(0xFF3B82F6)
private val AccentGreen = Color(0xFF10B981)
private val AccentRed   = Color(0xFFEF4444)
private val AccentAmber = Color(0xFFF59E0B)
private val AccentPurple= Color(0xFF8B5CF6)
private val AppBg       = Color(0xFFF1F5F9)
private val CardWhite   = Color.White
private val TextPrimary = Color(0xFF0F172A)
private val TextSecond  = Color(0xFF64748B)
private val TextTertiary= Color(0xFF94A3B8)
private val DividerColor= Color(0xFFF1F5F9)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BranchFormScreen(
    branchId: String?,
    uiState: BranchUiState,
    selectedBranch: com.tanh.datsan.data.model.Branch?,
    onFetchBranch: (String) -> Unit,
    onCreateBranch: (CreateBranchDto) -> Unit,
    onUpdateBranch: (String, UpdateBranchDto) -> Unit,
    availableManagers: List<com.tanh.datsan.data.model.AccountResponseDto>,
    // Location
    cities: List<CityDto>,
    wards: List<WardDto>,
    isLoadingCities: Boolean,
    isLoadingWards: Boolean,
    onFetchCities: () -> Unit,
    onFetchWards: (Int) -> Unit,
    onBackClick: () -> Unit,
    onResetUiState: () -> Unit,
    onClearSelectedBranch: () -> Unit
) {
    val context = LocalContext.current
    val isEditing = branchId != null

    // Form state
    var name         by remember { mutableStateOf("") }
    var phoneNumber  by remember { mutableStateOf("") }
    var description  by remember { mutableStateOf("") }
    var openTime     by remember { mutableStateOf("") }
    var closeTime    by remember { mutableStateOf("") }
    var status       by remember { mutableStateOf(true) }
    var street       by remember { mutableStateOf("") }
    var latitude     by remember { mutableStateOf("") }
    var longitude    by remember { mutableStateOf("") }
    var selectedManagerId by remember { mutableStateOf<String?>(null) }
    var selectedCityId    by remember { mutableStateOf<Int?>(null) }
    var selectedWardId    by remember { mutableStateOf<Int?>(null) }
    var selectedCityName  by remember { mutableStateOf("") }
    var selectedWardName  by remember { mutableStateOf("") }

    // Sheet visibility
    var showManagerSheet by remember { mutableStateOf(false) }
    var showCitySheet    by remember { mutableStateOf(false) }
    var showWardSheet    by remember { mutableStateOf(false) }

    // Init
    LaunchedEffect(Unit) { onFetchCities() }

    LaunchedEffect(branchId) {
        if (isEditing) onFetchBranch(branchId)
        else onClearSelectedBranch()
    }

    LaunchedEffect(selectedBranch) {
        if (isEditing && selectedBranch != null) {
            name        = selectedBranch.name
            phoneNumber = selectedBranch.phoneNumber ?: ""
            description = selectedBranch.description ?: ""
            openTime    = selectedBranch.openTime
            closeTime   = selectedBranch.closeTime
            status      = selectedBranch.status
            street      = selectedBranch.address?.street ?: ""
            latitude    = selectedBranch.address?.latitude?.toString() ?: ""
            longitude   = selectedBranch.address?.longitude?.toString() ?: ""
            // Populate city/ward display names from branch
            selectedCityName = selectedBranch.address?.cityName
                ?: selectedBranch.address?.city?.name ?: ""
            selectedWardName = selectedBranch.address?.wardName
                ?: selectedBranch.address?.ward?.name ?: ""
        }
    }

    // Try to resolve city/ward ID from name once cities are loaded
    LaunchedEffect(cities, selectedCityName) {
        if (selectedCityId == null && selectedCityName.isNotBlank() && cities.isNotEmpty()) {
            val city = cities.find { it.name.equals(selectedCityName, ignoreCase = true) }
            if (city != null) {
                selectedCityId = city.id
                onFetchWards(city.id)
            }
        }
    }
    LaunchedEffect(wards, selectedWardName) {
        if (selectedWardId == null && selectedWardName.isNotBlank() && wards.isNotEmpty()) {
            val ward = wards.find { it.name.equals(selectedWardName, ignoreCase = true) }
            if (ward != null) selectedWardId = ward.id
        }
    }

    LaunchedEffect(uiState) {
        when (uiState) {
            is BranchUiState.Success -> {
                uiState.message?.let { Toast.makeText(context, it, Toast.LENGTH_SHORT).show() }
                onResetUiState()
                onBackClick()
            }
            is BranchUiState.Error -> {
                Toast.makeText(context, uiState.message, Toast.LENGTH_SHORT).show()
                onResetUiState()
            }
            else -> {}
        }
    }

    fun submit() {
        if (name.isBlank() || openTime.isBlank() || closeTime.isBlank()) {
            Toast.makeText(context, "Vui lòng nhập tên, giờ mở và đóng cửa", Toast.LENGTH_SHORT).show()
            return
        }
        val lat = latitude.toDoubleOrNull()
        val lng = longitude.toDoubleOrNull()

        if (isEditing) {
            onUpdateBranch(
                branchId,
                UpdateBranchDto(
                    name        = name,
                    phoneNumber = phoneNumber.ifBlank { null },
                    description = description.ifBlank { null },
                    status      = status,
                    openTime    = openTime,
                    closeTime   = closeTime,
                    street      = street.ifBlank { null },
                    cityId      = selectedCityId,
                    wardId      = selectedWardId,
                    latitude    = lat,
                    longitude   = lng,
                    managerId   = selectedManagerId
                )
            )
        } else {
            onCreateBranch(
                CreateBranchDto(
                    name        = name,
                    phoneNumber = phoneNumber.ifBlank { null },
                    description = description.ifBlank { null },
                    status      = status,
                    openTime    = openTime,
                    closeTime   = closeTime,
                    street      = street.ifBlank { null },
                    cityId      = selectedCityId,
                    wardId      = selectedWardId,
                    latitude    = lat,
                    longitude   = lng,
                    managerId   = selectedManagerId
                )
            )
        }
    }

    Scaffold(
        containerColor = AppBg,
        topBar = {
            // Dark top bar
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Brush.horizontalGradient(listOf(DarkBg, DarkBg2)))
                    .statusBarsPadding()
                    .padding(horizontal = 8.dp, vertical = 6.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, null, tint = Color.White)
                    }
                    Text(
                        if (isEditing) "Chỉnh sửa chi nhánh" else "Thêm chi nhánh mới",
                        color = Color.White,
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 18.sp,
                        modifier = Modifier.weight(1f)
                    )
                    if (uiState is BranchUiState.Loading) {
                        CircularProgressIndicator(
                            color = Color.White,
                            modifier = Modifier.size(24.dp).padding(end = 12.dp),
                            strokeWidth = 2.dp
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(0.dp)
        ) {
            Spacer(Modifier.height(12.dp))

            // ── Section: Thông tin cơ bản ────────────────────────────────
            FormSectionTitle("Thông tin cơ bản", Icons.Rounded.Business)
            FormCard {
                FormField(
                    label = "Tên chi nhánh *",
                    value = name,
                    onValueChange = { name = it },
                    icon = Icons.Rounded.Store,
                    placeholder = "VD: Chi nhánh Quận 1"
                )
                FormDivider()
                FormField(
                    label = "Số điện thoại",
                    value = phoneNumber,
                    onValueChange = { phoneNumber = it },
                    icon = Icons.Rounded.Phone,
                    placeholder = "VD: 0909 123 456",
                    keyboardType = KeyboardType.Phone
                )
                FormDivider()
                FormField(
                    label = "Mô tả",
                    value = description,
                    onValueChange = { description = it },
                    icon = Icons.Rounded.Description,
                    placeholder = "Mô tả chi nhánh...",
                    singleLine = false,
                    minLines = 2
                )
                FormDivider()
                // Status toggle
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background((if (status) AccentGreen else AccentRed).copy(0.1f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            if (status) Icons.Default.CheckCircle else Icons.Default.Cancel,
                            null,
                            tint = if (status) AccentGreen else AccentRed,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Trạng thái hoạt động", fontSize = 11.sp, color = TextTertiary, fontWeight = FontWeight.Bold)
                        Text(
                            if (status) "Đang hoạt động" else "Ngưng hoạt động",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = if (status) AccentGreen else AccentRed
                        )
                    }
                    Switch(
                        checked = status,
                        onCheckedChange = { status = it },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color.White,
                            checkedTrackColor = AccentGreen,
                            uncheckedTrackColor = AccentRed.copy(0.3f)
                        )
                    )
                }
            }

            Spacer(Modifier.height(16.dp))

            // ── Section: Giờ hoạt động ───────────────────────────────────
            FormSectionTitle("Giờ hoạt động", Icons.Rounded.Schedule)
            FormCard {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Giờ mở cửa *", fontSize = 11.sp, color = TextTertiary, fontWeight = FontWeight.Bold)
                        Spacer(Modifier.height(6.dp))
                        OutlinedTextField(
                            value = openTime,
                            onValueChange = { openTime = it },
                            placeholder = { Text("07:00", color = TextTertiary) },
                            singleLine = true,
                            shape = RoundedCornerShape(14.dp),
                            leadingIcon = { Icon(Icons.Default.LightMode, null, tint = AccentAmber, modifier = Modifier.size(18.dp)) },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = AccentBlue,
                                unfocusedBorderColor = DividerColor,
                                focusedContainerColor = CardWhite,
                                unfocusedContainerColor = AppBg
                            ),
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Giờ đóng cửa *", fontSize = 11.sp, color = TextTertiary, fontWeight = FontWeight.Bold)
                        Spacer(Modifier.height(6.dp))
                        OutlinedTextField(
                            value = closeTime,
                            onValueChange = { closeTime = it },
                            placeholder = { Text("22:00", color = TextTertiary) },
                            singleLine = true,
                            shape = RoundedCornerShape(14.dp),
                            leadingIcon = { Icon(Icons.Default.DarkMode, null, tint = AccentPurple, modifier = Modifier.size(18.dp)) },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = AccentBlue,
                                unfocusedBorderColor = DividerColor,
                                focusedContainerColor = CardWhite,
                                unfocusedContainerColor = AppBg
                            ),
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }

            Spacer(Modifier.height(16.dp))

            FormSectionTitle("Địa chỉ & Vị trí", Icons.Rounded.LocationOn)
            FormCard {
                // City selector
                FormSelectorRow(
                    icon = Icons.Rounded.LocationCity,
                    iconTint = AccentBlue,
                    label = "Tỉnh / Thành phố",
                    value = selectedCityName.ifBlank { "Chọn tỉnh/thành phố" },
                    placeholder = selectedCityName.isBlank(),
                    onClick = { showCitySheet = true }
                )
                FormDivider()
                // Ward selector
                FormSelectorRow(
                    icon = Icons.Rounded.Map,
                    iconTint = AccentGreen,
                    label = "Phường / Xã",
                    value = selectedWardName.ifBlank { if (selectedCityId == null) "Chọn tỉnh trước" else "Chọn phường/xã" },
                    placeholder = selectedWardName.isBlank(),
                    enabled = selectedCityId != null,
                    onClick = { if (selectedCityId != null) showWardSheet = true }
                )
                FormDivider()
                // Street
                FormField(
                    label = "Số nhà / Tên đường",
                    value = street,
                    onValueChange = { street = it },
                    icon = Icons.Rounded.Home,
                    placeholder = "VD: 123 Nguyễn Văn Linh"
                )
            }

            Spacer(Modifier.height(16.dp))

            // ── Section: Toạ độ ──────────────────────────────────────────
            FormSectionTitle("Toạ độ GPS", Icons.Rounded.MyLocation)
            FormCard {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Vĩ độ (Latitude)", fontSize = 11.sp, color = TextTertiary, fontWeight = FontWeight.Bold)
                        Spacer(Modifier.height(6.dp))
                        OutlinedTextField(
                            value = latitude,
                            onValueChange = { latitude = it },
                            placeholder = { Text("VD: 10.7769", color = TextTertiary) },
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            shape = RoundedCornerShape(14.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = AccentBlue,
                                unfocusedBorderColor = DividerColor,
                                focusedContainerColor = CardWhite,
                                unfocusedContainerColor = AppBg
                            ),
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Kinh độ (Longitude)", fontSize = 11.sp, color = TextTertiary, fontWeight = FontWeight.Bold)
                        Spacer(Modifier.height(6.dp))
                        OutlinedTextField(
                            value = longitude,
                            onValueChange = { longitude = it },
                            placeholder = { Text("VD: 106.7009", color = TextTertiary) },
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            shape = RoundedCornerShape(14.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = AccentBlue,
                                unfocusedBorderColor = DividerColor,
                                focusedContainerColor = CardWhite,
                                unfocusedContainerColor = AppBg
                            ),
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }

                // Open Google Maps button
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                        .padding(bottom = 16.dp)
                        .clickable {
                            val query = if (latitude.isNotBlank() && longitude.isNotBlank())
                                "$latitude,$longitude" else Constants.DEFAULT_LOCATION_QUERY

                            val geoUri = "${Constants.GOOGLE_MAPS_APP_URI}$query".toUri()
                            val intent = Intent(Intent.ACTION_VIEW, geoUri)
                            
                            try { 
                                context.startActivity(intent) 
                            } catch (_: Exception) {
                                // Fallback: Nếu máy không có app bản đồ nào, mở bằng trình duyệt (Web Deeplink)
                                val webUri = "${Constants.GOOGLE_MAPS_WEB_URL}$query".toUri()
                                context.startActivity(Intent(Intent.ACTION_VIEW, webUri))
                            }
                        },
                    shape = RoundedCornerShape(14.dp),
                    color = Color(0xFF1A73E8).copy(alpha = 0.08f),
                    border = BorderStroke(1.dp, Color(0xFF1A73E8).copy(alpha = 0.2f))
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Icon(Icons.Rounded.Map, null, tint = Color(0xFF1A73E8), modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(8.dp))
                        Text(
                            "Mở Google Maps để lấy toạ độ",
                            color = Color(0xFF1A73E8),
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 13.sp
                        )
                    }
                }

                // Hint
                Text(
                    "💡 Trong Google Maps: nhấn giữ vào vị trí → toạ độ hiện ở thanh tìm kiếm (lat, lng)",
                    modifier = Modifier.padding(horizontal = 16.dp).padding(bottom = 14.dp),
                    fontSize = 11.sp,
                    color = TextTertiary,
                    lineHeight = 16.sp
                )
            }

            Spacer(Modifier.height(16.dp))

            // ── Section: Quản lý ─────────────────────────────────────────
            FormSectionTitle("Quản lý chi nhánh", Icons.Rounded.ManageAccounts)
            FormCard {
                FormSelectorRow(
                    icon = Icons.Rounded.Person,
                    iconTint = AccentPurple,
                    label = "Quản lý phụ trách",
                    value = availableManagers.find { it.id == selectedManagerId }
                        ?.let { "${it.userProfile?.fullName ?: "N/A"} · ${it.email}" }
                        ?: "Chưa chọn quản lý",
                    placeholder = selectedManagerId == null,
                    onClick = { showManagerSheet = true }
                )
            }

            Spacer(Modifier.height(24.dp))

            // ── Submit button ────────────────────────────────────────────
            Button(
                onClick = ::submit,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .height(56.dp),
                shape = RoundedCornerShape(18.dp),
                colors = ButtonDefaults.buttonColors(containerColor = DarkBg),
                enabled = uiState !is BranchUiState.Loading,
                elevation = ButtonDefaults.buttonElevation(defaultElevation = 6.dp)
            ) {
                if (uiState is BranchUiState.Loading) {
                    CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp), strokeWidth = 2.dp)
                } else {
                    Icon(
                        if (isEditing) Icons.Default.Save else Icons.Default.Add,
                        null, modifier = Modifier.size(20.dp)
                    )
                    Spacer(Modifier.width(10.dp))
                    Text(
                        if (isEditing) "Lưu thay đổi" else "Tạo chi nhánh",
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 16.sp
                    )
                }
            }

            Spacer(Modifier.height(40.dp))
        }
    }

    // ── City bottom sheet ────────────────────────────────────────────────────
    if (showCitySheet) {
        ModalBottomSheet(
            onDismissRequest = { showCitySheet = false },
            containerColor = CardWhite,
            shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp)
        ) {
            Column(modifier = Modifier.fillMaxWidth().padding(bottom = 32.dp)) {
                Text(
                    "Chọn Tỉnh / Thành phố",
                    modifier = Modifier.padding(start = 20.dp, top = 4.dp, bottom = 8.dp),
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 18.sp,
                    color = TextPrimary
                )
                HorizontalDivider(color = DividerColor)
                if (isLoadingCities) {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.CenterHorizontally).padding(24.dp))
                }
                LazyColumn {
                    items(cities) { city ->
                        val isSel = city.id == selectedCityId
                        ListItem(
                            headlineContent = {
                                Text(city.name, fontWeight = if (isSel) FontWeight.Bold else FontWeight.Normal, color = if (isSel) AccentBlue else TextPrimary)
                            },
                            leadingContent = {
                                Box(
                                    modifier = Modifier.size(36.dp).clip(RoundedCornerShape(10.dp))
                                        .background(if (isSel) AccentBlue.copy(0.1f) else AppBg),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(Icons.Rounded.LocationCity, null, tint = if (isSel) AccentBlue else TextTertiary, modifier = Modifier.size(18.dp))
                                }
                            },
                            trailingContent = {
                                if (isSel) Icon(Icons.Default.Check, null, tint = AccentBlue)
                            },
                            modifier = Modifier.clickable {
                                selectedCityId = city.id
                                selectedCityName = city.name
                                selectedWardId = null
                                selectedWardName = ""
                                onFetchWards(city.id)
                                showCitySheet = false
                            }
                        )
                        HorizontalDivider(modifier = Modifier.padding(start = 68.dp), color = DividerColor)
                    }
                }
            }
        }
    }

    // ── Ward bottom sheet ────────────────────────────────────────────────────
    if (showWardSheet) {
        ModalBottomSheet(
            onDismissRequest = { showWardSheet = false },
            containerColor = CardWhite,
            shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp)
        ) {
            Column(modifier = Modifier.fillMaxWidth().padding(bottom = 32.dp)) {
                Text(
                    "Chọn Phường / Xã",
                    modifier = Modifier.padding(start = 20.dp, top = 4.dp, bottom = 8.dp),
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 18.sp,
                    color = TextPrimary
                )
                HorizontalDivider(color = DividerColor)
                if (isLoadingWards) {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.CenterHorizontally).padding(24.dp))
                }
                LazyColumn {
                    items(wards) { ward ->
                        val isSel = ward.id == selectedWardId
                        ListItem(
                            headlineContent = {
                                Text(ward.name, fontWeight = if (isSel) FontWeight.Bold else FontWeight.Normal, color = if (isSel) AccentGreen else TextPrimary)
                            },
                            leadingContent = {
                                Box(
                                    modifier = Modifier.size(36.dp).clip(RoundedCornerShape(10.dp))
                                        .background(if (isSel) AccentGreen.copy(0.1f) else AppBg),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(Icons.Rounded.Map, null, tint = if (isSel) AccentGreen else TextTertiary, modifier = Modifier.size(18.dp))
                                }
                            },
                            trailingContent = {
                                if (isSel) Icon(Icons.Default.Check, null, tint = AccentGreen)
                            },
                            modifier = Modifier.clickable {
                                selectedWardId = ward.id
                                selectedWardName = ward.name
                                showWardSheet = false
                            }
                        )
                        HorizontalDivider(modifier = Modifier.padding(start = 68.dp), color = DividerColor)
                    }
                }
            }
        }
    }

    // ── Manager bottom sheet ─────────────────────────────────────────────────
    if (showManagerSheet) {
        ModalBottomSheet(
            onDismissRequest = { showManagerSheet = false },
            containerColor = CardWhite,
            shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp)
        ) {
            Column(modifier = Modifier.fillMaxWidth().padding(bottom = 32.dp)) {
                Text(
                    "Chọn Quản lý",
                    modifier = Modifier.padding(start = 20.dp, top = 4.dp, bottom = 8.dp),
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 18.sp,
                    color = TextPrimary
                )
                HorizontalDivider(color = DividerColor)
                LazyColumn {
                    item {
                        ListItem(
                            headlineContent = { Text("Không chọn quản lý", color = AccentRed) },
                            leadingContent = {
                                Box(
                                    modifier = Modifier.size(36.dp).clip(RoundedCornerShape(10.dp))
                                        .background(AccentRed.copy(0.08f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(Icons.Default.PersonOff, null, tint = AccentRed, modifier = Modifier.size(18.dp))
                                }
                            },
                            modifier = Modifier.clickable { selectedManagerId = null; showManagerSheet = false }
                        )
                        HorizontalDivider(modifier = Modifier.padding(start = 68.dp), color = DividerColor)
                    }
                    items(availableManagers) { mgr ->
                        val isSel = mgr.id == selectedManagerId
                        ListItem(
                            headlineContent = {
                                Text(
                                    mgr.userProfile?.fullName ?: "Không tên",
                                    fontWeight = if (isSel) FontWeight.Bold else FontWeight.Normal,
                                    color = if (isSel) AccentPurple else TextPrimary
                                )
                            },
                            supportingContent = {
                                Text(mgr.email, color = TextTertiary, fontSize = 12.sp)
                            },
                            leadingContent = {
                                Box(
                                    modifier = Modifier.size(36.dp).clip(CircleShape)
                                        .background(if (isSel) AccentPurple.copy(0.12f) else AppBg),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(Icons.Rounded.Person, null, tint = if (isSel) AccentPurple else TextTertiary, modifier = Modifier.size(20.dp))
                                }
                            },
                            trailingContent = {
                                if (isSel) Icon(Icons.Default.Check, null, tint = AccentPurple)
                            },
                            modifier = Modifier.clickable { selectedManagerId = mgr.id; showManagerSheet = false }
                        )
                        HorizontalDivider(modifier = Modifier.padding(start = 68.dp), color = DividerColor)
                    }
                }
            }
        }
    }
}

// ─── Shared form components ───────────────────────────────────────────────────
@Composable
private fun FormSectionTitle(title: String, icon: ImageVector) {
    Row(
        modifier = Modifier.padding(start = 20.dp, end = 16.dp, top = 4.dp, bottom = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icon, null, tint = TextSecond, modifier = Modifier.size(14.dp))
        Spacer(Modifier.width(6.dp))
        Text(
            title.uppercase(),
            fontSize = 11.sp,
            fontWeight = FontWeight.ExtraBold,
            color = TextSecond,
            letterSpacing = 1.sp
        )
    }
}

@Composable
private fun FormCard(content: @Composable ColumnScope.() -> Unit) {
    Surface(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
        shape = RoundedCornerShape(24.dp),
        color = CardWhite,
        border = BorderStroke(1.dp, DividerColor),
        shadowElevation = 1.dp
    ) {
        Column(content = content)
    }
}

@Composable
private fun FormDivider() {
    HorizontalDivider(modifier = Modifier.padding(start = 66.dp), color = DividerColor, thickness = 1.dp)
}

@Composable
private fun FormField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    icon: ImageVector,
    placeholder: String,
    singleLine: Boolean = true,
    minLines: Int = 1,
    keyboardType: KeyboardType = KeyboardType.Text
) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = if (singleLine) Alignment.CenterVertically else Alignment.Top
    ) {
        Box(
            modifier = Modifier.size(38.dp).clip(RoundedCornerShape(10.dp)).background(AppBg),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, null, tint = TextSecond, modifier = Modifier.size(20.dp))
        }
        Spacer(Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(label, fontSize = 11.sp, color = TextTertiary, fontWeight = FontWeight.Bold)
            BasicFormTextField(
                value = value,
                onValueChange = onValueChange,
                placeholder = placeholder,
                singleLine = singleLine,
                minLines = minLines,
                keyboardType = keyboardType
            )
        }
    }
}

@Composable
private fun BasicFormTextField(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    singleLine: Boolean = true,
    minLines: Int = 1,
    keyboardType: KeyboardType = KeyboardType.Text
) {
    androidx.compose.foundation.text.BasicTextField(
        value = value,
        onValueChange = onValueChange,
        singleLine = singleLine,
        minLines = if (singleLine) 1 else minLines,
        textStyle = androidx.compose.ui.text.TextStyle(
            color = TextPrimary,
            fontSize = 15.sp,
            fontWeight = FontWeight.SemiBold
        ),
        keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
        decorationBox = { inner ->
            Box(modifier = Modifier.fillMaxWidth().padding(top = 4.dp)) {
                if (value.isBlank()) {
                    Text(placeholder, color = TextTertiary, fontSize = 15.sp)
                }
                inner()
            }
        },
        modifier = Modifier.fillMaxWidth()
    )
}

@Composable
private fun FormSelectorRow(
    icon: ImageVector,
    iconTint: Color,
    label: String,
    value: String,
    placeholder: Boolean,
    enabled: Boolean = true,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(enabled = enabled, onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier.size(38.dp).clip(RoundedCornerShape(10.dp))
                .background(iconTint.copy(if (enabled) 0.1f else 0.05f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, null, tint = if (enabled) iconTint else TextTertiary, modifier = Modifier.size(20.dp))
        }
        Spacer(Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(label, fontSize = 11.sp, color = TextTertiary, fontWeight = FontWeight.Bold)
            Text(
                value,
                fontSize = 15.sp,
                fontWeight = FontWeight.SemiBold,
                color = if (placeholder || !enabled) TextTertiary else TextPrimary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
        Icon(
            Icons.Default.ChevronRight, null,
            tint = if (enabled) TextTertiary else TextTertiary.copy(0.4f),
            modifier = Modifier.size(20.dp)
        )
    }
}
