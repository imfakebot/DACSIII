package com.tanh.datsan.ui.admin

import android.widget.Toast
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Block
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.tanh.datsan.data.model.UserAdminDto
import com.tanh.datsan.viewmodel.AdminUserUiState
import com.tanh.datsan.utils.toFullImageUrl

// ---- Sporty Premium Design Tokens ----
private val PageBg        = Color(0xFF0F1923)
private val CardBg        = Color(0xFF1A2733)
private val AccentGreen   = Color(0xFF00E676)
private val AccentTeal    = Color(0xFF00BFA5)
private val RedBan        = Color(0xFFFF5252)
private val GreenUnban    = Color(0xFF00E676)
private val TextPrimary   = Color(0xFFFFFFFF)
private val TextSecondary = Color(0xFF90A4AE)
private val DividerColor  = Color(0xFF263238)

private val HeaderGradient = Brush.linearGradient(listOf(Color(0xFF0F1923), Color(0xFF1A3A2A)))
private val SportyAvatarGradientA = Brush.linearGradient(listOf(Color(0xFF00E676), Color(0xFF00BFA5)))
private val SportyAvatarGradientB = Brush.linearGradient(listOf(Color(0xFFFF6D00), Color(0xFFFF1744)))

data class RoleFilterChip(val label: String, val value: String?)

val roleFilters = listOf(
    RoleFilterChip("Tất cả", null),
    RoleFilterChip("Admin", "super_admin"),
    RoleFilterChip("Quản lý", "branch_manager"),
    RoleFilterChip("Nhân viên", "staff"),
    RoleFilterChip("Khách hàng", "user"),
)

sealed class ConfirmAction {
    data class Ban(val user: UserAdminDto) : ConfirmAction()
    data class Unban(val user: UserAdminDto) : ConfirmAction()
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminUserManagementScreen(
    uiState: AdminUserUiState,
    onGoToPage: (Int) -> Unit,
    onRefresh: () -> Unit,
    onBanUser: (UserAdminDto) -> Unit,
    onUnbanUser: (UserAdminDto) -> Unit,
    onClearError: () -> Unit,
    onSearchQueryChanged: (String) -> Unit,
    onRoleFilterChanged: (String?) -> Unit,
    onBackClick: () -> Unit
) {
    val context = LocalContext.current
    var pendingAction by remember { mutableStateOf<ConfirmAction?>(null) }
    var selectedUserForDetails by remember { mutableStateOf<UserAdminDto?>(null) }

    LaunchedEffect(uiState.errorMessage) {
        uiState.errorMessage?.let {
            Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
            onClearError()
        }
    }

    Box(modifier = Modifier.fillMaxSize().background(PageBg)) {
        Column(modifier = Modifier.fillMaxSize()) {

            // ---- Header with gradient background ----
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(HeaderGradient)
            ) {
                Column(modifier = Modifier.padding(top = 48.dp, bottom = 16.dp)) {
                    // Title Row
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(
                            onClick = onBackClick,
                            modifier = Modifier
                                .size(40.dp)
                                .background(Color(0xFF263238), CircleShape)
                        ) {
                            Icon(
                                Icons.Default.ArrowBack,
                                contentDescription = "Trở về",
                                tint = AccentGreen,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                "Quản lý người dùng",
                                fontSize = 22.sp,
                                fontWeight = FontWeight.Black,
                                color = TextPrimary
                            )
                            Text(
                                text = if (uiState.isLoading) "Đang tải..."
                                else "Tổng cộng: ${uiState.totalFiltered} tài khoản",
                                fontSize = 12.sp,
                                color = AccentGreen.copy(alpha = 0.7f)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Search Bar — dark toned with neon green focus border
                    OutlinedTextField(
                        value = uiState.searchQuery,
                        onValueChange = onSearchQueryChanged,
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                        placeholder = {
                            Text(
                                "Tìm theo tên, email hoặc số điện thoại...",
                                color = TextSecondary,
                                fontSize = 14.sp
                            )
                        },
                        leadingIcon = {
                            Icon(
                                Icons.Default.Search,
                                contentDescription = null,
                                tint = AccentGreen,
                                modifier = Modifier.size(20.dp)
                            )
                        },
                        trailingIcon = {
                            if (uiState.searchQuery.isNotEmpty()) {
                                IconButton(onClick = { onSearchQueryChanged("") }) {
                                    Icon(
                                        Icons.Default.Close,
                                        contentDescription = "Xóa",
                                        tint = TextSecondary,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }
                        },
                        shape = RoundedCornerShape(14.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = AccentGreen,
                            unfocusedBorderColor = DividerColor,
                            focusedContainerColor = CardBg,
                            unfocusedContainerColor = Color(0xFF162029),
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary,
                            cursorColor = AccentGreen,
                        ),
                        singleLine = true
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Role Filter Dropdown — dark theme with green accent
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            "Lọc theo vai trò:",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium,
                            color = TextSecondary
                        )
                        Spacer(modifier = Modifier.width(12.dp))

                        var expandedRole by remember { mutableStateOf(false) }

                        ExposedDropdownMenuBox(
                            expanded = expandedRole,
                            onExpandedChange = { expandedRole = !expandedRole }
                        ) {
                            OutlinedTextField(
                                value = roleFilters.find { it.value == uiState.selectedRole }?.label
                                    ?: "Tất cả",
                                onValueChange = {},
                                readOnly = true,
                                trailingIcon = {
                                    ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedRole)
                                },
                                modifier = Modifier
                                    .menuAnchor()
                                    .fillMaxWidth(),
                                shape = RoundedCornerShape(14.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = AccentGreen,
                                    unfocusedBorderColor = DividerColor,
                                    focusedContainerColor = CardBg,
                                    unfocusedContainerColor = CardBg,
                                    focusedTextColor = TextPrimary,
                                    unfocusedTextColor = TextPrimary,
                                    focusedTrailingIconColor = AccentGreen,
                                    unfocusedTrailingIconColor = TextSecondary
                                )
                            )
                            ExposedDropdownMenu(
                                expanded = expandedRole,
                                onDismissRequest = { expandedRole = false },
                                containerColor = CardBg
                            ) {
                                roleFilters.forEach { chip ->
                                    DropdownMenuItem(
                                        text = {
                                            Text(
                                                chip.label,
                                                fontWeight = if (uiState.selectedRole == chip.value) FontWeight.Bold else FontWeight.Normal,
                                                color = if (uiState.selectedRole == chip.value) AccentGreen else TextPrimary
                                            )
                                        },
                                        onClick = {
                                            onRoleFilterChanged(chip.value)
                                            expandedRole = false
                                        }
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(4.dp))
                }
            }

            // ---- Content ----
            when {
                uiState.isLoading -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            CircularProgressIndicator(
                                color = AccentGreen,
                                strokeWidth = 3.dp,
                                modifier = Modifier.size(40.dp)
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            Text("Đang tải dữ liệu...", color = TextSecondary, fontSize = 14.sp)
                        }
                    }
                }
                uiState.displayedUsers.isEmpty() -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Box(
                                modifier = Modifier
                                    .size(80.dp)
                                    .background(CardBg, CircleShape)
                                    .border(BorderStroke(2.dp, AccentGreen.copy(alpha = 0.3f)), CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    Icons.Default.Person,
                                    contentDescription = null,
                                    tint = AccentGreen.copy(alpha = 0.5f),
                                    modifier = Modifier.size(40.dp)
                                )
                            }
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                "Không tìm thấy người dùng",
                                color = TextPrimary,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                "Thử thay đổi bộ lọc hoặc từ khóa",
                                color = TextSecondary,
                                fontSize = 13.sp
                            )
                        }
                    }
                }
                else -> {
                    LazyColumn(
                        modifier = Modifier.weight(1f).fillMaxWidth(),
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        items(uiState.displayedUsers, key = { it.id }) { user ->
                            UserAdminCard(
                                user = user,
                                onBanClick = { pendingAction = ConfirmAction.Ban(it) },
                                onUnbanClick = { pendingAction = ConfirmAction.Unban(it) },
                                onClick = { selectedUserForDetails = user }
                            )
                        }
                        item { Spacer(modifier = Modifier.height(4.dp)) }
                    }

                    // ---- Pagination — dark themed, green accent for active ----
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        color = CardBg,
                        shadowElevation = 8.dp
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 20.dp, vertical = 12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            OutlinedButton(
                                onClick = { onGoToPage(uiState.currentPage - 1) },
                                enabled = uiState.currentPage > 1,
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.outlinedButtonColors(
                                    contentColor = AccentGreen,
                                    disabledContentColor = TextSecondary.copy(alpha = 0.4f)
                                ),
                                border = BorderStroke(
                                    1.5.dp,
                                    if (uiState.currentPage > 1) AccentGreen else DividerColor
                                )
                            ) {
                                Text("← Trước", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                            }

                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    "${uiState.currentPage} / ${uiState.totalPages}",
                                    fontWeight = FontWeight.Black,
                                    fontSize = 16.sp,
                                    color = AccentGreen
                                )
                                Text("Trang", fontSize = 11.sp, color = TextSecondary)
                            }

                            Button(
                                onClick = { onGoToPage(uiState.currentPage + 1) },
                                enabled = uiState.currentPage < uiState.totalPages,
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = AccentGreen,
                                    contentColor = Color(0xFF0F1923),
                                    disabledContainerColor = DividerColor,
                                    disabledContentColor = TextSecondary.copy(alpha = 0.5f)
                                )
                            ) {
                                Text("Tiếp →", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }
    }

    // ---- Confirm Dialog — dark themed ----
    pendingAction?.let { action ->
        val isBanning = action is ConfirmAction.Ban
        val user = when (action) {
            is ConfirmAction.Ban -> action.user
            is ConfirmAction.Unban -> action.user
        }
        val displayName = user.userProfile?.fullName ?: user.email

        AlertDialog(
            onDismissRequest = { pendingAction = null },
            containerColor = CardBg,
            titleContentColor = TextPrimary,
            textContentColor = TextSecondary,
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        if (isBanning) Icons.Default.Block else Icons.Default.CheckCircle,
                        contentDescription = null,
                        tint = if (isBanning) RedBan else GreenUnban,
                        modifier = Modifier.size(22.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        if (isBanning) "Khóa tài khoản" else "Mở khóa tài khoản",
                        fontWeight = FontWeight.Black,
                        fontSize = 17.sp
                    )
                }
            },
            text = {
                Text(
                    text = if (isBanning)
                        "Tài khoản \"$displayName\" sẽ bị tạm khóa và không thể đăng nhập. Bạn có chắc không?"
                    else
                        "Tài khoản \"$displayName\" sẽ được kích hoạt trở lại. Bạn có chắc không?",
                    lineHeight = 22.sp,
                    fontSize = 14.sp,
                    color = TextSecondary
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        when (action) {
                            is ConfirmAction.Ban -> onBanUser(action.user)
                            is ConfirmAction.Unban -> onUnbanUser(action.user)
                        }
                        pendingAction = null
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isBanning) RedBan else GreenUnban,
                        contentColor = if (isBanning) Color.White else Color(0xFF0F1923)
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Xác nhận", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                OutlinedButton(
                    onClick = { pendingAction = null },
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = TextSecondary),
                    border = BorderStroke(1.dp, DividerColor)
                ) {
                    Text("Hủy")
                }
            }
        )
    }

    // ---- User Details Dialog — dark themed ----
    selectedUserForDetails?.let { user ->
        AlertDialog(
            onDismissRequest = { selectedUserForDetails = null },
            containerColor = CardBg,
            title = {
                Text(
                    text = "Thông tin chi tiết",
                    fontWeight = FontWeight.Black,
                    fontSize = 18.sp,
                    color = TextPrimary
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                        // Avatar in details
                        val initial = (user.userProfile?.fullName?.firstOrNull()
                            ?: user.email.firstOrNull() ?: '?').uppercaseChar().toString()
                        val avatarGradient =
                            if ((initial.firstOrNull()?.code ?: 0) % 2 == 0)
                                SportyAvatarGradientA
                            else
                                SportyAvatarGradientB

                        Box(
                            modifier = Modifier
                                .size(80.dp)
                                .clip(CircleShape)
                                .background(avatarGradient),
                            contentAlignment = Alignment.Center
                        ) {
                            if (!user.userProfile?.avatarUrl.isNullOrBlank()) {
                                AsyncImage(
                                    model = user.userProfile?.avatarUrl?.toFullImageUrl(),
                                    contentDescription = "Avatar",
                                    modifier = Modifier.fillMaxSize().clip(CircleShape),
                                    contentScale = ContentScale.Crop
                                )
                            } else {
                                Text(
                                    initial,
                                    fontSize = 32.sp,
                                    fontWeight = FontWeight.Black,
                                    color = Color.White
                                )
                            }
                        }
                    }

                    HorizontalDivider(color = DividerColor)

                    Text("Tên: ${user.userProfile?.fullName ?: "N/A"}", color = TextPrimary)
                    Text("Email: ${user.email}", color = TextPrimary)
                    Text("SĐT: ${user.userProfile?.phoneNumber ?: "N/A"}", color = TextPrimary)
                    Text("Vai trò: ${user.role?.name ?: "N/A"}", color = TextPrimary)
                    Text(
                        "Trạng thái: ${if (user.status == "suspended") "Bị khóa" else "Hoạt động"}",
                        color = TextPrimary
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = { selectedUserForDetails = null },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = AccentGreen,
                        contentColor = Color(0xFF0F1923)
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Đóng", fontWeight = FontWeight.Bold)
                }
            }
        )
    }
}

// ---- User Card with Dropdown — sporty dark card with green left border accent ----
@Composable
fun UserAdminCard(
    user: UserAdminDto,
    onBanClick: (UserAdminDto) -> Unit,
    onUnbanClick: (UserAdminDto) -> Unit,
    onClick: () -> Unit
) {
    var showMenu by remember { mutableStateOf(false) }

    val initial = (user.userProfile?.fullName?.firstOrNull()
        ?: user.email.firstOrNull() ?: '?').uppercaseChar().toString()

    val avatarGradient = if ((initial.firstOrNull()?.code ?: 0) % 2 == 0)
        SportyAvatarGradientA
    else
        SportyAvatarGradientB

    Card(
        modifier = Modifier
            .fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = CardBg),
        shape = RoundedCornerShape(20.dp),
        onClick = onClick
    ) {
        Row(modifier = Modifier.fillMaxWidth()) {
            // Green left accent strip
            Box(
                modifier = Modifier
                    .width(4.dp)
                    .fillMaxHeight()
                    .background(
                        Brush.linearGradient(listOf(AccentGreen, AccentTeal)),
                        RoundedCornerShape(topStart = 20.dp, bottomStart = 20.dp)
                    )
            )

            Row(
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 14.dp, vertical = 14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Avatar
                Box(
                    modifier = Modifier
                        .size(50.dp)
                        .clip(CircleShape)
                        .background(avatarGradient),
                    contentAlignment = Alignment.Center
                ) {
                    val raw = user.userProfile?.avatarUrl
                    val full = raw?.toFullImageUrl()
                    android.util.Log.d("AVATAR", "raw=$raw | full=$full")

                    if (!user.userProfile?.avatarUrl.isNullOrBlank()) {
                        AsyncImage(
                            model = user.userProfile?.avatarUrl?.toFullImageUrl(),
                            contentDescription = "Avatar",
                            modifier = Modifier.fillMaxSize().clip(CircleShape),
                            contentScale = ContentScale.Crop
                        )
                    } else {
                        Text(
                            initial,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                }

                Spacer(modifier = Modifier.width(14.dp))

                // Info
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = user.userProfile?.fullName ?: "Chưa cập nhật tên",
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = TextPrimary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = user.email,
                        fontSize = 12.sp,
                        color = TextSecondary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    user.userProfile?.phoneNumber?.let { phone ->
                        if (phone.isNotBlank()) {
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(text = phone, fontSize = 12.sp, color = TextSecondary)
                        }
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Role badge — pill shaped with glow border
                        Box(
                            modifier = Modifier
                                .border(
                                    BorderStroke(1.dp, AccentTeal.copy(alpha = 0.5f)),
                                    RoundedCornerShape(20.dp)
                                )
                                .background(
                                    AccentTeal.copy(alpha = 0.12f),
                                    RoundedCornerShape(20.dp)
                                )
                                .padding(horizontal = 10.dp, vertical = 3.dp)
                        ) {
                            Text(
                                text = user.role?.name ?: "N/A",
                                fontSize = 10.sp,
                                color = AccentTeal,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        // Status badge — pill shaped with glow effect
                        val isSuspended = user.status == "suspended"
                        val isDeleted = user.status == "deleted"
                        val statusColor =
                            if (isSuspended) RedBan else if (isDeleted) TextSecondary else GreenUnban
                        val statusText =
                            if (isSuspended) "Đã khóa" else if (isDeleted) "Đã xóa" else "Hoạt động"
                        Box(
                            modifier = Modifier
                                .border(
                                    BorderStroke(1.dp, statusColor.copy(alpha = 0.5f)),
                                    RoundedCornerShape(20.dp)
                                )
                                .background(
                                    statusColor.copy(alpha = 0.12f),
                                    RoundedCornerShape(20.dp)
                                )
                                .padding(horizontal = 10.dp, vertical = 3.dp)
                        ) {
                            Text(
                                text = statusText,
                                fontSize = 10.sp,
                                color = statusColor,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                // Action Dropdown — outlined green
                Box {
                    OutlinedButton(
                        onClick = { showMenu = true },
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = AccentGreen
                        ),
                        border = BorderStroke(1.dp, AccentGreen.copy(alpha = 0.5f)),
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                        modifier = Modifier.height(36.dp)
                    ) {
                        Text(
                            "Hành động",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = AccentGreen
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Icon(
                            Icons.Default.KeyboardArrowDown,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp),
                            tint = AccentGreen
                        )
                    }

                    DropdownMenu(
                        expanded = showMenu,
                        onDismissRequest = { showMenu = false },
                        containerColor = CardBg
                    ) {
                        if (user.status != "suspended") {
                            DropdownMenuItem(
                                text = {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            Icons.Default.Block,
                                            contentDescription = null,
                                            tint = RedBan,
                                            modifier = Modifier.size(16.dp)
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            "Khóa tài khoản",
                                            fontSize = 14.sp,
                                            color = RedBan,
                                            fontWeight = FontWeight.Medium
                                        )
                                    }
                                },
                                onClick = {
                                    showMenu = false
                                    onBanClick(user)
                                }
                            )
                        }
                        if (user.status == "suspended") {
                            DropdownMenuItem(
                                text = {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            Icons.Default.CheckCircle,
                                            contentDescription = null,
                                            tint = GreenUnban,
                                            modifier = Modifier.size(16.dp)
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            "Mở khóa tài khoản",
                                            fontSize = 14.sp,
                                            color = GreenUnban,
                                            fontWeight = FontWeight.Medium
                                        )
                                    }
                                },
                                onClick = {
                                    showMenu = false
                                    onUnbanClick(user)
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}
