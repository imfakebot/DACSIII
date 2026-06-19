package com.tanh.datsan.ui.admin

import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
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
import com.tanh.datsan.data.model.BranchDetailDto
import com.tanh.datsan.viewmodel.AdminBranchUiState

// ── Sporty Premium Design Tokens ──────────────────────────────────────────────
private val SPPageBg       = Color(0xFF0F1923)
private val SPCardBg       = Color(0xFF1A2733)
private val SPAccentGreen  = Color(0xFF00E676)
private val SPAccentTeal   = Color(0xFF00BFA5)
private val SPTextPri      = Color(0xFFFFFFFF)
private val SPTextSec      = Color(0xFF90A4AE)
private val SPRed          = Color(0xFFFF5252)
private val SPDivider      = Color(0xFF263238)
private val SPSurface      = Color(0xFF1E2D3A)
private val SPCardShape    = RoundedCornerShape(20.dp)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminBranchListScreen(
    uiState: AdminBranchUiState,
    onRefresh: () -> Unit,
    onAddBranch: () -> Unit,
    onEditBranch: (BranchDetailDto) -> Unit,
    onDeleteBranch: (BranchDetailDto) -> Unit,
    onViewFields: (BranchDetailDto) -> Unit,
    onClearMessages: () -> Unit,
    onBackClick: () -> Unit
) {
    val context = LocalContext.current
    var branchToDelete by remember { mutableStateOf<BranchDetailDto?>(null) }
    var selectedBranchForDetails by remember { mutableStateOf<BranchDetailDto?>(null) }

    LaunchedEffect(uiState.errorMessage) {
        uiState.errorMessage?.let {
            Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
            onClearMessages()
        }
    }
    LaunchedEffect(uiState.successMessage) {
        uiState.successMessage?.let {
            Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
            onClearMessages()
        }
    }

    Box(modifier = Modifier.fillMaxSize().background(SPPageBg)) {
        Column(modifier = Modifier.fillMaxSize()) {

            // ── Header ──────────────────────────────────────────────────────
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        Brush.verticalGradient(
                            listOf(Color(0xFF0F1923), Color(0xFF1A3A2A))
                        )
                    )
                    .shadow(elevation = 4.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 48.dp, bottom = 20.dp, start = 16.dp, end = 16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = onBackClick,
                        modifier = Modifier
                            .size(42.dp)
                            .background(SPSurface, CircleShape)
                    ) {
                        Icon(
                            Icons.Default.ArrowBack,
                            contentDescription = "Trở về",
                            tint = SPTextPri,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(14.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            "Quản lý chi nhánh",
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Black,
                            color = SPTextPri
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            "${uiState.branches.size} chi nhánh",
                            fontSize = 12.sp,
                            color = SPAccentTeal
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = onAddBranch,
                        colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
                        shape = RoundedCornerShape(14.dp),
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 10.dp),
                        modifier = Modifier
                            .background(
                                Brush.linearGradient(listOf(SPAccentGreen, SPAccentTeal)),
                                RoundedCornerShape(14.dp)
                            )
                    ) {
                        Icon(
                            Icons.Default.Add,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp),
                            tint = Color(0xFF0F1923)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            "Thêm",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF0F1923)
                        )
                    }
                }
            }

            // ── Content ─────────────────────────────────────────────────────
            when {
                uiState.isLoading -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            CircularProgressIndicator(
                                color = SPAccentGreen,
                                strokeWidth = 3.dp,
                                modifier = Modifier.size(44.dp)
                            )
                            Spacer(modifier = Modifier.height(14.dp))
                            Text(
                                "Đang tải danh sách chi nhánh...",
                                color = SPTextSec,
                                fontSize = 14.sp
                            )
                        }
                    }
                }
                uiState.branches.isEmpty() -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Box(
                                modifier = Modifier
                                    .size(88.dp)
                                    .background(
                                        Brush.radialGradient(
                                            listOf(
                                                SPAccentGreen.copy(alpha = 0.20f),
                                                SPAccentGreen.copy(alpha = 0.05f)
                                            )
                                        ),
                                        CircleShape
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    Icons.Default.Store,
                                    contentDescription = null,
                                    tint = SPAccentGreen,
                                    modifier = Modifier.size(42.dp)
                                )
                            }
                            Spacer(modifier = Modifier.height(18.dp))
                            Text(
                                "Chưa có chi nhánh nào",
                                fontSize = 17.sp,
                                fontWeight = FontWeight.Bold,
                                color = SPTextPri
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                "Bấm \"Thêm\" để tạo chi nhánh đầu tiên",
                                fontSize = 13.sp,
                                color = SPTextSec
                            )
                        }
                    }
                }
                else -> {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 16.dp),
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        items(uiState.branches, key = { it.id }) { branch ->
                            BranchCard(
                                branch = branch,
                                onEdit = { onEditBranch(branch) },
                                onDelete = { branchToDelete = branch },
                                onViewFields = { onViewFields(branch) },
                                onClick = { selectedBranchForDetails = branch }
                            )
                        }
                    }
                }
            }
        }
    }

    // ── Delete Confirm Dialog (Dark themed) ─────────────────────────────────
    branchToDelete?.let { branch ->
        AlertDialog(
            onDismissRequest = { branchToDelete = null },
            containerColor = SPCardBg,
            titleContentColor = SPTextPri,
            textContentColor = SPTextSec,
            shape = RoundedCornerShape(24.dp),
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .background(SPRed.copy(alpha = 0.15f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Default.Warning,
                            contentDescription = null,
                            tint = SPRed,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        "Xóa chi nhánh",
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        color = SPTextPri
                    )
                }
            },
            text = {
                Text(
                    "Bạn có chắc muốn xóa chi nhánh \"${branch.name}\" không? Hành động này không thể hoàn tác.",
                    lineHeight = 22.sp,
                    fontSize = 14.sp,
                    color = SPTextSec
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        onDeleteBranch(branch)
                        branchToDelete = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = SPRed),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Xóa", color = Color.White, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                OutlinedButton(
                    onClick = { branchToDelete = null },
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, SPDivider)
                ) {
                    Text("Hủy", color = SPTextSec)
                }
            }
        )
    }

    // ── Branch Details Dialog (Dark themed) ─────────────────────────────────
    selectedBranchForDetails?.let { branch ->
        AlertDialog(
            onDismissRequest = { selectedBranchForDetails = null },
            containerColor = SPCardBg,
            shape = RoundedCornerShape(24.dp),
            title = {
                Text(
                    text = "Thông tin chi tiết chi nhánh",
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    color = SPTextPri
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(
                        "Tên chi nhánh: ${branch.name}",
                        color = SPTextPri,
                        fontWeight = FontWeight.Bold
                    )
                    HorizontalDivider(color = SPDivider)

                    val address = branch.address?.let {
                        listOfNotNull(it.street, it.wardName, it.cityName).joinToString(", ")
                    } ?: "Chưa cập nhật địa chỉ"

                    Text("Địa chỉ: $address", color = SPTextSec)
                    Text(
                        "Số điện thoại: ${branch.phoneNumber ?: "N/A"}",
                        color = SPTextSec
                    )
                    Text(
                        "Giờ hoạt động: ${branch.openTime} - ${branch.closeTime}",
                        color = SPTextSec
                    )

                    branch.manager?.let { mgr ->
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            "Người quản lý: ${mgr.fullName ?: mgr.account?.email ?: "N/A"}",
                            color = SPAccentGreen,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = { selectedBranchForDetails = null },
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .background(
                            Brush.linearGradient(listOf(SPAccentGreen, SPAccentTeal)),
                            RoundedCornerShape(12.dp)
                        )
                ) {
                    Text("Đóng", color = Color(0xFF0F1923), fontWeight = FontWeight.Bold)
                }
            }
        )
    }
}

@Composable
fun BranchCard(
    branch: BranchDetailDto,
    onEdit: (BranchDetailDto) -> Unit,
    onDelete: (BranchDetailDto) -> Unit,
    onViewFields: (BranchDetailDto) -> Unit,
    onClick: () -> Unit
) {
    var showMenu by remember { mutableStateOf(false) }

    val gradients = listOf(
        Brush.linearGradient(listOf(Color(0xFF3D7EF5), Color(0xFF7C5CDB))),
        Brush.linearGradient(listOf(Color(0xFFFF6B6B), Color(0xFFFF8E53))),
        Brush.linearGradient(listOf(Color(0xFF00E676), Color(0xFF00BFA5))),
        Brush.linearGradient(listOf(Color(0xFFF59E0B), Color(0xFFEF4444))),
    )
    val gradient = gradients[branch.name.length % gradients.size]

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(elevation = 6.dp, shape = SPCardShape, ambientColor = Color.Black),
        colors = CardDefaults.cardColors(containerColor = SPCardBg),
        shape = SPCardShape,
        onClick = onClick
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            // Colored top accent bar — 6.dp tall
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .clip(RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp))
                    .background(gradient)
            )
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.Top
            ) {
                // Icon — bigger 52.dp with sporty gradient
                Box(
                    modifier = Modifier
                        .size(52.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .background(gradient),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Default.Store,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(26.dp)
                    )
                }
                Spacer(modifier = Modifier.width(14.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        branch.name,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = SPTextPri,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(modifier = Modifier.height(5.dp))
                    if (!branch.phoneNumber.isNullOrBlank()) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                Icons.Default.Phone,
                                contentDescription = null,
                                tint = SPTextSec,
                                modifier = Modifier.size(13.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(branch.phoneNumber, fontSize = 12.sp, color = SPTextSec)
                        }
                        Spacer(modifier = Modifier.height(3.dp))
                    }
                    val address = branch.address?.let {
                        listOfNotNull(it.street, it.wardName, it.cityName).joinToString(", ")
                    }
                    if (!address.isNullOrBlank()) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                Icons.Default.LocationOn,
                                contentDescription = null,
                                tint = SPTextSec,
                                modifier = Modifier.size(13.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                address,
                                fontSize = 12.sp,
                                color = SPTextSec,
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                        Spacer(modifier = Modifier.height(3.dp))
                    }
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Default.Schedule,
                            contentDescription = null,
                            tint = SPTextSec,
                            modifier = Modifier.size(13.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            "${branch.openTime} – ${branch.closeTime}",
                            fontSize = 12.sp,
                            color = SPTextSec
                        )
                    }
                    // Manager badge — green accent background
                    branch.manager?.let { mgr ->
                        Spacer(modifier = Modifier.height(8.dp))
                        Box(
                            modifier = Modifier
                                .background(
                                    SPAccentGreen.copy(alpha = 0.12f),
                                    RoundedCornerShape(8.dp)
                                )
                                .border(
                                    BorderStroke(1.dp, SPAccentGreen.copy(alpha = 0.25f)),
                                    RoundedCornerShape(8.dp)
                                )
                                .padding(horizontal = 10.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = "QL: ${mgr.fullName ?: mgr.account?.email ?: "Unknown"}",
                                fontSize = 11.sp,
                                color = SPAccentGreen,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }
                // 3-dot menu — dark themed
                Box {
                    IconButton(
                        onClick = { showMenu = true },
                        modifier = Modifier
                            .size(38.dp)
                            .background(SPSurface.copy(alpha = 0.6f), CircleShape)
                    ) {
                        Icon(
                            Icons.Default.MoreVert,
                            contentDescription = "Menu",
                            tint = SPTextSec,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    DropdownMenu(
                        expanded = showMenu,
                        onDismissRequest = { showMenu = false },
                        containerColor = Color(0xFF1E2D3A)
                    ) {
                        DropdownMenuItem(
                            text = {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        Icons.Default.SportsFootball,
                                        contentDescription = null,
                                        tint = SPAccentTeal,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        "Xem danh sách sân",
                                        fontSize = 14.sp,
                                        color = SPTextPri,
                                        fontWeight = FontWeight.Medium
                                    )
                                }
                            },
                            onClick = { showMenu = false; onViewFields(branch) }
                        )
                        HorizontalDivider(color = SPDivider, thickness = 0.5.dp)
                        DropdownMenuItem(
                            text = {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        Icons.Default.Edit,
                                        contentDescription = null,
                                        tint = Color(0xFFF59E0B),
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        "Chỉnh sửa",
                                        fontSize = 14.sp,
                                        color = SPTextPri,
                                        fontWeight = FontWeight.Medium
                                    )
                                }
                            },
                            onClick = { showMenu = false; onEdit(branch) }
                        )
                        HorizontalDivider(color = SPDivider, thickness = 0.5.dp)
                        DropdownMenuItem(
                            text = {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        Icons.Default.Delete,
                                        contentDescription = null,
                                        tint = SPRed,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        "Xóa chi nhánh",
                                        fontSize = 14.sp,
                                        color = SPRed,
                                        fontWeight = FontWeight.Medium
                                    )
                                }
                            },
                            onClick = { showMenu = false; onDelete(branch) }
                        )
                    }
                }
            }
        }
    }
}
