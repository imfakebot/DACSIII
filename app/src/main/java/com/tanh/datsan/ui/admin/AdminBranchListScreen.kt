package com.tanh.datsan.ui.admin

import android.widget.Toast
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

// Shared design tokens (same as AdminUserManagementScreen)
private val BListPageBg     = Color(0xFFF8F9FC)
private val BListCardBg     = Color.White
private val BListAccentBlue = Color(0xFF3D7EF5)
private val BListGreen      = Color(0xFF22C55E)
private val BListRed        = Color(0xFFEF4444)
private val BListTextPri    = Color(0xFF111827)
private val BListTextSec    = Color(0xFF6B7280)
private val BListDivider    = Color(0xFFE5E7EB)

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

    Box(modifier = Modifier.fillMaxSize().background(BListPageBg)) {
        Column(modifier = Modifier.fillMaxSize()) {

            // ---- Header ----
            Surface(
                modifier = Modifier.fillMaxWidth().shadow(elevation = 2.dp),
                color = BListCardBg
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 48.dp, bottom = 16.dp, start = 16.dp, end = 16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = onBackClick,
                        modifier = Modifier.size(40.dp).background(Color(0xFFF3F4F6), CircleShape)
                    ) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Trở về", tint = BListTextPri, modifier = Modifier.size(20.dp))
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Quản lý chi nhánh", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = BListTextPri)
                        Text("${uiState.branches.size} chi nhánh", fontSize = 12.sp, color = BListTextSec)
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = onAddBranch,
                        colors = ButtonDefaults.buttonColors(containerColor = BListAccentBlue),
                        shape = RoundedCornerShape(10.dp),
                        contentPadding = PaddingValues(horizontal = 14.dp, vertical = 8.dp)
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Thêm", fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                    }
                }
            }

            // ---- Content ----
            when {
                uiState.isLoading -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            CircularProgressIndicator(color = BListAccentBlue, strokeWidth = 3.dp, modifier = Modifier.size(40.dp))
                            Spacer(modifier = Modifier.height(12.dp))
                            Text("Đang tải danh sách chi nhánh...", color = BListTextSec, fontSize = 14.sp)
                        }
                    }
                }
                uiState.branches.isEmpty() -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Box(
                                modifier = Modifier.size(80.dp).background(Color(0xFFF3F4F6), CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Default.Store, contentDescription = null, tint = BListTextSec, modifier = Modifier.size(40.dp))
                            }
                            Spacer(modifier = Modifier.height(16.dp))
                            Text("Chưa có chi nhánh nào", fontSize = 16.sp, fontWeight = FontWeight.SemiBold, color = BListTextPri)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text("Bấm \"Thêm\" để tạo chi nhánh đầu tiên", fontSize = 13.sp, color = BListTextSec)
                        }
                    }
                }
                else -> {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(uiState.branches, key = { it.id }) { branch ->
                            BranchCard(
                                branch = branch,
                                onEdit = { onEditBranch(it) },
                                onDelete = { branchToDelete = it },
                                onViewFields = { onViewFields(it) }
                            )
                        }
                    }
                }
            }
        }
    }

    // Delete Confirm Dialog
    branchToDelete?.let { branch ->
        AlertDialog(
            onDismissRequest = { branchToDelete = null },
            containerColor = Color.White,
            titleContentColor = BListTextPri,
            textContentColor = BListTextSec,
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Warning, contentDescription = null, tint = BListRed, modifier = Modifier.size(22.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Xóa chi nhánh", fontWeight = FontWeight.Bold, fontSize = 17.sp)
                }
            },
            text = {
                Text("Bạn có chắc muốn xóa chi nhánh \"${branch.name}\" không? Hành động này không thể hoàn tác.", lineHeight = 22.sp, fontSize = 14.sp)
            },
            confirmButton = {
                Button(
                    onClick = {
                        onDeleteBranch(branch)
                        branchToDelete = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = BListRed),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text("Xóa", color = Color.White, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                OutlinedButton(
                    onClick = { branchToDelete = null },
                    shape = RoundedCornerShape(10.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, BListDivider)
                ) {
                    Text("Hủy", color = BListTextSec)
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
    onViewFields: (BranchDetailDto) -> Unit
) {
    var showMenu by remember { mutableStateOf(false) }

    val gradients = listOf(
        Brush.linearGradient(listOf(Color(0xFF3D7EF5), Color(0xFF7C5CDB))),
        Brush.linearGradient(listOf(Color(0xFFFF6B6B), Color(0xFFFF8E53))),
        Brush.linearGradient(listOf(Color(0xFF22C55E), Color(0xFF06B6D4))),
        Brush.linearGradient(listOf(Color(0xFFF59E0B), Color(0xFFEF4444))),
    )
    val gradient = gradients[branch.name.length % gradients.size]

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(elevation = 1.dp, shape = RoundedCornerShape(16.dp)),
        colors = CardDefaults.cardColors(containerColor = BListCardBg),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            // Colored top accent bar
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(4.dp)
                    .background(gradient)
            )
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.Top
            ) {
                // Icon
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(gradient),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.Store, contentDescription = null, tint = Color.White, modifier = Modifier.size(24.dp))
                }
                Spacer(modifier = Modifier.width(14.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(branch.name, fontWeight = FontWeight.Bold, fontSize = 16.sp, color = BListTextPri, maxLines = 1, overflow = TextOverflow.Ellipsis)
                    Spacer(modifier = Modifier.height(4.dp))
                    if (!branch.phoneNumber.isNullOrBlank()) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Phone, contentDescription = null, tint = BListTextSec, modifier = Modifier.size(13.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(branch.phoneNumber, fontSize = 12.sp, color = BListTextSec)
                        }
                        Spacer(modifier = Modifier.height(2.dp))
                    }
                    val address = branch.address?.let {
                        listOfNotNull(it.street, it.wardName, it.cityName).joinToString(", ")
                    }
                    if (!address.isNullOrBlank()) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.LocationOn, contentDescription = null, tint = BListTextSec, modifier = Modifier.size(13.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(address, fontSize = 12.sp, color = BListTextSec, maxLines = 2, overflow = TextOverflow.Ellipsis)
                        }
                        Spacer(modifier = Modifier.height(2.dp))
                    }
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Schedule, contentDescription = null, tint = BListTextSec, modifier = Modifier.size(13.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("${branch.openTime} – ${branch.closeTime}", fontSize = 12.sp, color = BListTextSec)
                    }
                    // Manager
                    branch.manager?.let { mgr ->
                        Spacer(modifier = Modifier.height(6.dp))
                        Box(
                            modifier = Modifier
                                .background(BListAccentBlue.copy(alpha = 0.08f), RoundedCornerShape(6.dp))
                                .padding(horizontal = 8.dp, vertical = 3.dp)
                        ) {
                            Text(
                                text = "QL: ${mgr.fullName ?: mgr.account?.email ?: "Unknown"}",
                                fontSize = 11.sp,
                                color = BListAccentBlue,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }
                // 3-dot menu
                Box {
                    IconButton(onClick = { showMenu = true }, modifier = Modifier.size(36.dp)) {
                        Icon(Icons.Default.MoreVert, contentDescription = "Menu", tint = BListTextSec, modifier = Modifier.size(20.dp))
                    }
                    DropdownMenu(expanded = showMenu, onDismissRequest = { showMenu = false }, containerColor = Color.White) {
                        DropdownMenuItem(
                            text = {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.SportsFootball, contentDescription = null, tint = BListAccentBlue, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("Xem danh sách sân", fontSize = 14.sp, color = BListTextPri, fontWeight = FontWeight.Medium)
                                }
                            },
                            onClick = { showMenu = false; onViewFields(branch) }
                        )
                        Divider(color = BListDivider, thickness = 0.5.dp)
                        DropdownMenuItem(
                            text = {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.Edit, contentDescription = null, tint = Color(0xFFF59E0B), modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("Chỉnh sửa", fontSize = 14.sp, color = BListTextPri, fontWeight = FontWeight.Medium)
                                }
                            },
                            onClick = { showMenu = false; onEdit(branch) }
                        )
                        Divider(color = BListDivider, thickness = 0.5.dp)
                        DropdownMenuItem(
                            text = {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.Delete, contentDescription = null, tint = BListRed, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("Xóa chi nhánh", fontSize = 14.sp, color = BListRed, fontWeight = FontWeight.Medium)
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
