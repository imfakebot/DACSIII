package com.tanh.datsan.ui.admin.branch

import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.*
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.tanh.datsan.data.model.Branch
import com.tanh.datsan.viewmodel.BranchUiState

// ─── Design tokens ────────────────────────────────────────────────────────────
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
fun BranchScreen(
    branches: List<Branch>,
    uiState: BranchUiState,
    onFetchBranches: () -> Unit,
    onNavigateToCreate: () -> Unit,
    onNavigateToEdit: (String) -> Unit,
    onDeleteBranch: (String) -> Unit,
    onResetUiState: () -> Unit
) {
    val context = LocalContext.current
    var showDeleteDialog by remember { mutableStateOf<Branch?>(null) }

    LaunchedEffect(Unit) { onFetchBranches() }

    LaunchedEffect(uiState) {
        when (uiState) {
            is BranchUiState.Success -> {
                uiState.message?.let { Toast.makeText(context, it, Toast.LENGTH_SHORT).show() }
                onResetUiState()
            }
            is BranchUiState.Error -> {
                Toast.makeText(context, uiState.message, Toast.LENGTH_SHORT).show()
                onResetUiState()
            }
            else -> {}
        }
    }

    Scaffold(
        containerColor = AppBg,
        floatingActionButton = {
            FloatingActionButton(
                onClick = onNavigateToCreate,
                shape = RoundedCornerShape(18.dp),
                containerColor = DarkBg,
                contentColor = Color.White,
                elevation = FloatingActionButtonDefaults.elevation(defaultElevation = 8.dp)
            ) {
                Icon(Icons.Default.Add, contentDescription = "Thêm chi nhánh", modifier = Modifier.size(26.dp))
            }
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(innerPadding),
            contentPadding = PaddingValues(bottom = 100.dp)
        ) {
            // ── Header ──────────────────────────────────────────────────
            item {
                BranchHeader(branchCount = branches.size)
            }

            // ── Loading ─────────────────────────────────────────────────
            if (uiState is BranchUiState.Loading) {
                items(3) { BranchCardSkeleton() }
            } else if (branches.isEmpty()) {
                item { BranchEmptyState(onNavigateToCreate) }
            } else {
                items(branches, key = { it.id }) { branch ->
                    BranchCard(
                        branch = branch,
                        onEdit = { onNavigateToEdit(branch.id) },
                        onDelete = { showDeleteDialog = branch }
                    )
                }
            }
        }
    }

    // Delete confirm dialog
    showDeleteDialog?.let { branch ->
        AlertDialog(
            onDismissRequest = { showDeleteDialog = null },
            shape = RoundedCornerShape(24.dp),
            containerColor = CardWhite,
            icon = {
                Box(
                    modifier = Modifier.size(56.dp).clip(RoundedCornerShape(16.dp)).background(AccentRed.copy(0.1f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.DeleteForever, null, tint = AccentRed, modifier = Modifier.size(28.dp))
                }
            },
            title = {
                Text(
                    "Xóa chi nhánh",
                    fontWeight = FontWeight.ExtraBold,
                    color = TextPrimary,
                    textAlign = TextAlign.Center
                )
            },
            text = {
                Text(
                    "Bạn có chắc muốn xóa chi nhánh\n\"${branch.name}\" không?\nHành động này không thể hoàn tác.",
                    color = TextSecond,
                    textAlign = TextAlign.Center,
                    lineHeight = 20.sp
                )
            },
            confirmButton = {
                Button(
                    onClick = { branch.id.let { onDeleteBranch(it) }; showDeleteDialog = null },
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = AccentRed)
                ) {
                    Text("Xóa", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                OutlinedButton(
                    onClick = { showDeleteDialog = null },
                    shape = RoundedCornerShape(14.dp),
                    border = BorderStroke(1.dp, DividerColor)
                ) {
                    Text("Hủy", color = TextSecond)
                }
            }
        )
    }
}

// ─── Dark header with canvas blobs ───────────────────────────────────────────
@Composable
fun BranchHeader(branchCount: Int) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(180.dp)
            .background(Brush.verticalGradient(listOf(DarkBg, DarkBg2)))
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            drawCircle(
                brush = Brush.radialGradient(
                    listOf(AccentBlue.copy(0.18f), Color.Transparent),
                    center = Offset(size.width * 0.85f, size.height * 0.2f),
                    radius = size.width * 0.7f
                ),
                radius = size.width * 0.7f,
                center = Offset(size.width * 0.85f, size.height * 0.2f)
            )
            drawCircle(
                brush = Brush.radialGradient(
                    listOf(AccentPurple.copy(0.12f), Color.Transparent),
                    center = Offset(size.width * 0.1f, size.height * 0.9f),
                    radius = size.width * 0.55f
                ),
                radius = size.width * 0.55f,
                center = Offset(size.width * 0.1f, size.height * 0.9f)
            )
        }
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp, vertical = 24.dp),
            verticalArrangement = Arrangement.Bottom
        ) {
            Text(
                "Quản lý chi nhánh",
                color = Color.White,
                fontSize = 26.sp,
                fontWeight = FontWeight.ExtraBold,
                letterSpacing = 0.3.sp
            )
            Spacer(Modifier.height(4.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(10.dp))
                        .background(Color.White.copy(alpha = 0.15f))
                        .padding(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    Text(
                        "$branchCount chi nhánh",
                        color = Color.White.copy(0.9f),
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
    }
}

// ─── Branch card ─────────────────────────────────────────────────────────────
@Composable
fun BranchCard(
    branch: Branch,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    val isActive = branch.status
    val fullAddress = listOfNotNull(
        branch.address?.street,
        branch.address?.wardName ?: branch.address?.ward?.name,
        branch.address?.cityName ?: branch.address?.city?.name
    ).joinToString(", ")

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp)
            .shadow(2.dp, RoundedCornerShape(24.dp))
            .clickable { onEdit() },
        shape = RoundedCornerShape(24.dp),
        color = CardWhite,
        border = BorderStroke(1.dp, DividerColor)
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            // ── Top row: icon + name + status badge ──────────────────
            Row(verticalAlignment = Alignment.CenterVertically) {
                // Icon avatar
                Box(
                    modifier = Modifier
                        .size(52.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(
                            if (isActive) AccentBlue.copy(0.1f) else AppBg
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Rounded.Store,
                        null,
                        tint = if (isActive) AccentBlue else TextTertiary,
                        modifier = Modifier.size(28.dp)
                    )
                }
                Spacer(Modifier.width(14.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        branch.name,
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 16.sp,
                        color = TextPrimary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(Modifier.height(4.dp))
                    // Status badge
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (isActive) AccentGreen.copy(0.12f) else AccentRed.copy(0.1f))
                            .padding(horizontal = 8.dp, vertical = 3.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(6.dp)
                                    .clip(CircleShape)
                                    .background(if (isActive) AccentGreen else AccentRed)
                            )
                            Spacer(Modifier.width(5.dp))
                            Text(
                                if (isActive) "Đang hoạt động" else "Ngưng hoạt động",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isActive) AccentGreen else AccentRed
                            )
                        }
                    }
                }
            }

            // ── Divider ───────────────────────────────────────────────
            HorizontalDivider(
                modifier = Modifier.padding(vertical = 14.dp),
                color = DividerColor,
                thickness = 1.dp
            )

            // ── Info rows ─────────────────────────────────────────────
            if (branch.phoneNumber != null) {
                BranchInfoRow(
                    icon = Icons.Rounded.Phone,
                    iconTint = AccentBlue,
                    label = branch.phoneNumber
                )
                Spacer(Modifier.height(8.dp))
            }
            if (fullAddress.isNotBlank()) {
                BranchInfoRow(
                    icon = Icons.Rounded.LocationOn,
                    iconTint = AccentGreen,
                    label = fullAddress
                )
                Spacer(Modifier.height(8.dp))
            }
            BranchInfoRow(
                icon = Icons.Rounded.Schedule,
                iconTint = AccentAmber,
                label = "${branch.openTime} – ${branch.closeTime}"
            )

            // ── Action buttons ────────────────────────────────────────
            Spacer(Modifier.height(14.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedButton(
                    onClick = onEdit,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(14.dp),
                    border = BorderStroke(1.5.dp, AccentBlue.copy(0.4f)),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = AccentBlue)
                ) {
                    Icon(Icons.Default.Edit, null, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(6.dp))
                    Text("Chỉnh sửa", fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                }
                OutlinedButton(
                    onClick = onDelete,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(14.dp),
                    border = BorderStroke(1.5.dp, AccentRed.copy(0.3f)),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = AccentRed)
                ) {
                    Icon(Icons.Default.Delete, null, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(6.dp))
                    Text("Xóa", fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                }
            }
        }
    }
}

@Composable
private fun BranchInfoRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    iconTint: Color,
    label: String
) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(30.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(iconTint.copy(alpha = 0.1f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, null, tint = iconTint, modifier = Modifier.size(16.dp))
        }
        Spacer(Modifier.width(10.dp))
        Text(
            label,
            fontSize = 13.sp,
            color = TextSecond,
            fontWeight = FontWeight.Medium,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis
        )
    }
}

// ─── Empty state ─────────────────────────────────────────────────────────────
@Composable
fun BranchEmptyState(onAdd: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(40.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(100.dp)
                .clip(RoundedCornerShape(28.dp))
                .background(AppBg),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                Icons.Rounded.Store,
                null,
                tint = Color(0xFFCBD5E1),
                modifier = Modifier.size(52.dp)
            )
        }
        Spacer(Modifier.height(20.dp))
        Text(
            "Chưa có chi nhánh nào",
            fontWeight = FontWeight.ExtraBold,
            fontSize = 18.sp,
            color = TextPrimary
        )
        Spacer(Modifier.height(8.dp))
        Text(
            "Tạo chi nhánh đầu tiên để bắt đầu\nquản lý sân của bạn",
            color = TextSecond,
            fontSize = 14.sp,
            textAlign = TextAlign.Center,
            lineHeight = 20.sp
        )
        Spacer(Modifier.height(24.dp))
        Button(
            onClick = onAdd,
            shape = RoundedCornerShape(16.dp),
            colors = ButtonDefaults.buttonColors(containerColor = DarkBg),
            contentPadding = PaddingValues(horizontal = 28.dp, vertical = 14.dp)
        ) {
            Icon(Icons.Default.Add, null, modifier = Modifier.size(18.dp))
            Spacer(Modifier.width(8.dp))
            Text("Thêm chi nhánh", fontWeight = FontWeight.Bold)
        }
    }
}

// ─── Skeleton ─────────────────────────────────────────────────────────────────
@Composable
fun BranchCardSkeleton() {
    val anim = rememberInfiniteTransition(label = "sk")
    val alpha by anim.animateFloat(
        initialValue = 0.3f, targetValue = 0.8f,
        animationSpec = infiniteRepeatable(tween(900), RepeatMode.Reverse), label = "a"
    )
    val skColor = Color(0xFFE2E8F0).copy(alpha = alpha)

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp),
        shape = RoundedCornerShape(24.dp),
        color = CardWhite,
        border = BorderStroke(1.dp, DividerColor)
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(modifier = Modifier.size(52.dp).clip(RoundedCornerShape(16.dp)).background(skColor))
                Spacer(Modifier.width(14.dp))
                Column {
                    Box(modifier = Modifier.width(160.dp).height(18.dp).clip(RoundedCornerShape(6.dp)).background(skColor))
                    Spacer(Modifier.height(8.dp))
                    Box(modifier = Modifier.width(90.dp).height(12.dp).clip(RoundedCornerShape(6.dp)).background(skColor))
                }
            }
            Spacer(Modifier.height(14.dp))
            Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(DividerColor))
            Spacer(Modifier.height(14.dp))
            repeat(3) {
                Box(modifier = Modifier.fillMaxWidth(0.7f).height(12.dp).clip(RoundedCornerShape(6.dp)).background(skColor))
                Spacer(Modifier.height(10.dp))
            }
        }
    }
}
