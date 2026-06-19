package com.tanh.datsan.ui.admin

import android.widget.Toast
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.tanh.datsan.data.model.UserAdminDto
import com.tanh.datsan.viewmodel.AdminUserUiState

// ---- Light Design Tokens ----
private val PageBg        = Color(0xFFF8F9FC)
private val CardBg        = Color.White
private val AccentBlue    = Color(0xFF3D7EF5)
private val AccentPurple  = Color(0xFF7C5CDB)
private val RedBan        = Color(0xFFEF4444)
private val GreenUnban    = Color(0xFF22C55E)
private val TextPrimary   = Color(0xFF111827)
private val TextSecondary = Color(0xFF6B7280)
private val DividerColor  = Color(0xFFE5E7EB)
private val ChipSelectedBg   = Color(0xFF3D7EF5)
private val ChipUnselectedBg = Color(0xFFF3F4F6)

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

    LaunchedEffect(uiState.errorMessage) {
        uiState.errorMessage?.let {
            Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
            onClearError()
        }
    }

    Box(modifier = Modifier.fillMaxSize().background(PageBg)) {
        Column(modifier = Modifier.fillMaxSize()) {

            // ---- Header ----
            Surface(
                modifier = Modifier.fillMaxWidth().shadow(elevation = 2.dp),
                color = CardBg
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
                            modifier = Modifier.size(40.dp).background(Color(0xFFF3F4F6), CircleShape)
                        ) {
                            Icon(Icons.Default.ArrowBack, contentDescription = "Trở về", tint = TextPrimary, modifier = Modifier.size(20.dp))
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Quản lý người dùng", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                            Text(
                                text = if (uiState.isLoading) "Đang tải..." else "Tổng cộng: ${uiState.totalFiltered} tài khoản",
                                fontSize = 12.sp,
                                color = TextSecondary
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Search Bar
                    OutlinedTextField(
                        value = uiState.searchQuery,
                        onValueChange = onSearchQueryChanged,
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                        placeholder = {
                            Text("Tìm theo tên, email hoặc số điện thoại...", color = TextSecondary, fontSize = 14.sp)
                        },
                        leadingIcon = {
                            Icon(Icons.Default.Search, contentDescription = null, tint = AccentBlue, modifier = Modifier.size(20.dp))
                        },
                        trailingIcon = {
                            if (uiState.searchQuery.isNotEmpty()) {
                                IconButton(onClick = { onSearchQueryChanged("") }) {
                                    Icon(Icons.Default.Close, contentDescription = "Xóa", tint = TextSecondary, modifier = Modifier.size(18.dp))
                                }
                            }
                        },
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = AccentBlue,
                            unfocusedBorderColor = DividerColor,
                            focusedContainerColor = Color.White,
                            unfocusedContainerColor = Color(0xFFF9FAFB),
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary,
                            cursorColor = AccentBlue,
                        ),
                        singleLine = true
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Role Filter Chips
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState())
                            .padding(horizontal = 16.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        roleFilters.forEach { chip ->
                            val isSelected = uiState.selectedRole == chip.value
                            FilterChip(
                                selected = isSelected,
                                onClick = { onRoleFilterChanged(chip.value) },
                                label = {
                                    Text(chip.label, fontSize = 13.sp, fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal)
                                },
                                shape = RoundedCornerShape(20.dp),
                                colors = FilterChipDefaults.filterChipColors(
                                    containerColor = ChipUnselectedBg,
                                    selectedContainerColor = ChipSelectedBg,
                                    labelColor = TextSecondary,
                                    selectedLabelColor = Color.White
                                ),
                                border = FilterChipDefaults.filterChipBorder(
                                    borderColor = DividerColor,
                                    selectedBorderColor = ChipSelectedBg,
                                    borderWidth = 1.dp,
                                    selectedBorderWidth = 0.dp,
                                    enabled = true,
                                    selected = isSelected
                                )
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(4.dp))
                }
            }

            // ---- Content ----
            when {
                uiState.isLoading -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            CircularProgressIndicator(color = AccentBlue, strokeWidth = 3.dp, modifier = Modifier.size(40.dp))
                            Spacer(modifier = Modifier.height(12.dp))
                            Text("Đang tải dữ liệu...", color = TextSecondary, fontSize = 14.sp)
                        }
                    }
                }
                uiState.displayedUsers.isEmpty() -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Box(
                                modifier = Modifier.size(80.dp).background(Color(0xFFF3F4F6), CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Default.Person, contentDescription = null, tint = TextSecondary, modifier = Modifier.size(40.dp))
                            }
                            Spacer(modifier = Modifier.height(16.dp))
                            Text("Không tìm thấy người dùng", color = TextPrimary, fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text("Thử thay đổi bộ lọc hoặc từ khóa", color = TextSecondary, fontSize = 13.sp)
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
                                onUnbanClick = { pendingAction = ConfirmAction.Unban(it) }
                            )
                        }
                        item { Spacer(modifier = Modifier.height(4.dp)) }
                    }

                    // ---- Pagination ----
                    Surface(
                        modifier = Modifier.fillMaxWidth().shadow(elevation = 4.dp),
                        color = CardBg
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
                                shape = RoundedCornerShape(10.dp),
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = AccentBlue),
                                border = androidx.compose.foundation.BorderStroke(
                                    1.5.dp,
                                    if (uiState.currentPage > 1) AccentBlue else DividerColor
                                )
                            ) {
                                Text("← Trước", fontSize = 13.sp, fontWeight = FontWeight.Medium)
                            }

                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    "${uiState.currentPage} / ${uiState.totalPages}",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 16.sp,
                                    color = TextPrimary
                                )
                                Text("Trang", fontSize = 11.sp, color = TextSecondary)
                            }

                            Button(
                                onClick = { onGoToPage(uiState.currentPage + 1) },
                                enabled = uiState.currentPage < uiState.totalPages,
                                shape = RoundedCornerShape(10.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = AccentBlue,
                                    disabledContainerColor = Color(0xFFD1D5DB)
                                )
                            ) {
                                Text("Tiếp →", fontSize = 13.sp, fontWeight = FontWeight.Medium)
                            }
                        }
                    }
                }
            }
        }
    }

    // ---- Confirm Dialog ----
    pendingAction?.let { action ->
        val isBanning = action is ConfirmAction.Ban
        val user = when (action) {
            is ConfirmAction.Ban -> action.user
            is ConfirmAction.Unban -> action.user
        }
        val displayName = user.userProfile?.fullName ?: user.email

        AlertDialog(
            onDismissRequest = { pendingAction = null },
            containerColor = Color.White,
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
                        fontWeight = FontWeight.Bold,
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
                    fontSize = 14.sp
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
                        containerColor = if (isBanning) RedBan else GreenUnban
                    ),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text("Xác nhận", color = Color.White, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                OutlinedButton(
                    onClick = { pendingAction = null },
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = TextSecondary),
                    border = androidx.compose.foundation.BorderStroke(1.dp, DividerColor)
                ) {
                    Text("Hủy")
                }
            }
        )
    }
}

// ---- User Card with Dropdown ----
@Composable
fun UserAdminCard(
    user: UserAdminDto,
    onBanClick: (UserAdminDto) -> Unit,
    onUnbanClick: (UserAdminDto) -> Unit
) {
    var showMenu by remember { mutableStateOf(false) }

    val initial = (user.userProfile?.fullName?.firstOrNull()
        ?: user.email.firstOrNull() ?: '?').uppercaseChar().toString()

    val avatarGradient = if ((initial.firstOrNull()?.code ?: 0) % 2 == 0)
        Brush.linearGradient(listOf(Color(0xFF3D7EF5), Color(0xFF7C5CDB)))
    else
        Brush.linearGradient(listOf(Color(0xFFFF6B6B), Color(0xFFFF8E53)))

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(elevation = 1.dp, shape = RoundedCornerShape(16.dp)),
        colors = CardDefaults.cardColors(containerColor = CardBg),
        shape = RoundedCornerShape(16.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 14.dp),
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
                if (!user.userProfile?.avatarUrl.isNullOrBlank()) {
                    AsyncImage(
                        model = user.userProfile?.avatarUrl,
                        contentDescription = "Avatar",
                        modifier = Modifier.fillMaxSize().clip(CircleShape)
                    )
                } else {
                    Text(initial, fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Color.White)
                }
            }

            Spacer(modifier = Modifier.width(14.dp))

            // Info
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = user.userProfile?.fullName ?: "Chưa cập nhật tên",
                    fontWeight = FontWeight.SemiBold,
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
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
                    // Role badge
                    Box(
                        modifier = Modifier
                            .background(AccentPurple.copy(alpha = 0.1f), RoundedCornerShape(6.dp))
                            .padding(horizontal = 8.dp, vertical = 3.dp)
                    ) {
                        Text(
                            text = user.role?.name ?: "N/A",
                            fontSize = 10.sp,
                            color = AccentPurple,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                    // Status badge
                    val isSuspended = user.status == "suspended"
                    val isDeleted = user.status == "deleted"
                    val statusColor = if (isSuspended) RedBan else if (isDeleted) TextSecondary else GreenUnban
                    val statusText = if (isSuspended) "Đã khóa" else if (isDeleted) "Đã xóa" else "Hoạt động"
                    Box(
                        modifier = Modifier
                            .background(statusColor.copy(alpha = 0.1f), RoundedCornerShape(6.dp))
                            .padding(horizontal = 8.dp, vertical = 3.dp)
                    ) {
                        Text(
                            text = statusText,
                            fontSize = 10.sp,
                            color = statusColor,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }

            // Action Dropdown
            Box {
                OutlinedButton(
                    onClick = { showMenu = true },
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = TextSecondary),
                    border = androidx.compose.foundation.BorderStroke(1.dp, DividerColor),
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                    modifier = Modifier.height(36.dp)
                ) {
                    Text("Hành động", fontSize = 11.sp, fontWeight = FontWeight.Medium)
                    Spacer(modifier = Modifier.width(4.dp))
                    Icon(Icons.Default.KeyboardArrowDown, contentDescription = null, modifier = Modifier.size(16.dp))
                }

                DropdownMenu(
                    expanded = showMenu,
                    onDismissRequest = { showMenu = false },
                    containerColor = Color.White
                ) {
                    if (user.status != "suspended") {
                        DropdownMenuItem(
                            text = {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.Block, contentDescription = null, tint = RedBan, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("Khóa tài khoản", fontSize = 14.sp, color = RedBan, fontWeight = FontWeight.Medium)
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
                                    Icon(Icons.Default.CheckCircle, contentDescription = null, tint = GreenUnban, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("Mở khóa tài khoản", fontSize = 14.sp, color = GreenUnban, fontWeight = FontWeight.Medium)
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
