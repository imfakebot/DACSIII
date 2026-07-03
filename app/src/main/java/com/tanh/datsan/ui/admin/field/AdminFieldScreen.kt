package com.tanh.datsan.ui.admin.field

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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.tanh.datsan.R
import com.tanh.datsan.data.model.Branch
import com.tanh.datsan.data.model.FieldResponse
import com.tanh.datsan.ui.admin.field.AdminUiState
import androidx.compose.foundation.lazy.LazyRow
private val DarkBg      = Color(0xFF0F172A)
private val DarkBg2     = Color(0xFF1E293B)
private val AccentBlue  = Color(0xFF3B82F6)
private val AccentGreen = Color(0xFF10B981)
private val AccentRed   = Color(0xFFEF4444)
private val AccentPurple= Color(0xFF8B5CF6)
private val AppBg       = Color(0xFFF1F5F9)
private val CardWhite   = Color.White
private val TextPrimary = Color(0xFF0F172A)
private val TextSecond  = Color(0xFF64748B)
private val TextTertiary= Color(0xFF94A3B8)
private val DividerColor= Color(0xFFF1F5F9)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminFieldScreen(
    userRole: String,
    fields: List<FieldResponse>,
    branches: List<Branch>,
    uiState: AdminUiState,
    onFetchData: () -> Unit,
    onNavigateToCreate: () -> Unit,
    onNavigateToEdit: (String) -> Unit,
    onNavigateToUploadImages: (String) -> Unit,
    onNavigateToTimeSlot: (String) -> Unit,
    onDeleteField: (String) -> Unit,
    onResetUiState: () -> Unit,
    isLoadingMore: Boolean = false,
    onLoadMore: () -> Unit = {}
) {
    val context = LocalContext.current
    var showDeleteDialog by remember { mutableStateOf<FieldResponse?>(null) }
    var selectedBranchFilterId by remember { mutableStateOf<String?>(null) }
    
    val canEdit = userRole == "branch_manager" || userRole == "super_admin"
    
    val filteredFields = remember(fields, selectedBranchFilterId) {
        if (selectedBranchFilterId == null) fields else fields.filter { it.branch.id == selectedBranchFilterId }
    }

    LaunchedEffect(Unit) { onFetchData() }

    LaunchedEffect(uiState) {
        when (uiState) {
            is AdminUiState.Success -> {
                uiState.message?.let { Toast.makeText(context, it, Toast.LENGTH_SHORT).show() }
                onResetUiState()
            }
            is AdminUiState.Error -> {
                Toast.makeText(context, uiState.message, Toast.LENGTH_SHORT).show()
                onResetUiState()
            }
            else -> {}
        }
    }

    val listState = androidx.compose.foundation.lazy.rememberLazyListState()

    LaunchedEffect(listState) {
        snapshotFlow { listState.layoutInfo.visibleItemsInfo.lastOrNull()?.index }
            .collect { lastIndex ->
                if (lastIndex != null && lastIndex >= filteredFields.size && !isLoadingMore) {
                    onLoadMore()
                }
            }
    }

    Scaffold(
        containerColor = AppBg,
        floatingActionButton = {
            if (canEdit) {
                FloatingActionButton(
                    onClick = onNavigateToCreate,
                    shape = RoundedCornerShape(18.dp),
                    containerColor = DarkBg,
                    contentColor = Color.White,
                    elevation = FloatingActionButtonDefaults.elevation(defaultElevation = 8.dp)
                ) {
                    Icon(Icons.Default.Add, contentDescription = stringResource(id = R.string.field_add_new), modifier = Modifier.size(26.dp))
                }
            }
        }
    ) { innerPadding ->
        LazyColumn(
            state = listState,
            modifier = Modifier.fillMaxSize().padding(innerPadding),
            contentPadding = PaddingValues(bottom = 100.dp)
        ) {
            item {
                FieldHeader(fieldCount = filteredFields.size)
            }
            item {
                var expanded by remember { mutableStateOf(false) }
                val selectedName = if (selectedBranchFilterId == null) stringResource(id = R.string.field_filter_all_branch) 
                                   else branches.find { it.id == selectedBranchFilterId }?.name ?: stringResource(id = R.string.field_filter_all_branch)
                
                Box(modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 8.dp)) {
                    ExposedDropdownMenuBox(
                        expanded = expanded,
                        onExpandedChange = { expanded = !expanded }
                    ) {
                        OutlinedTextField(
                            value = selectedName,
                            onValueChange = {},
                            readOnly = true,
                            leadingIcon = { Icon(Icons.Rounded.FilterList, contentDescription = null, tint = TextSecond) },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                            colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors(
                                focusedBorderColor = AccentBlue,
                                unfocusedBorderColor = DividerColor,
                                focusedContainerColor = CardWhite,
                                unfocusedContainerColor = CardWhite
                            ),
                            shape = RoundedCornerShape(16.dp),
                            modifier = Modifier.menuAnchor(ExposedDropdownMenuAnchorType.PrimaryEditable, true).fillMaxWidth()
                        )
                        ExposedDropdownMenu(
                            expanded = expanded,
                            onDismissRequest = { expanded = false },
                            containerColor = CardWhite,
                            shape = RoundedCornerShape(16.dp)
                        ) {
                            DropdownMenuItem(
                                text = { 
                                    Text(stringResource(id = R.string.field_filter_all_branch), 
                                        fontWeight = if (selectedBranchFilterId == null) FontWeight.Bold else FontWeight.Normal,
                                        color = if (selectedBranchFilterId == null) AccentBlue else TextPrimary
                                    ) 
                                },
                                onClick = { 
                                    selectedBranchFilterId = null
                                    expanded = false
                                }
                            )
                            branches.forEach { branch ->
                                DropdownMenuItem(
                                    text = { 
                                        Text(branch.name,
                                            fontWeight = if (selectedBranchFilterId == branch.id) FontWeight.Bold else FontWeight.Normal,
                                            color = if (selectedBranchFilterId == branch.id) AccentBlue else TextPrimary
                                        ) 
                                    },
                                    onClick = { 
                                        selectedBranchFilterId = branch.id
                                        expanded = false
                                    }
                                )
                            }
                        }
                    }
                }
            }
            if (uiState is AdminUiState.Loading) {
                items(3) { FieldCardSkeleton() }
            } else if (filteredFields.isEmpty()) {
                item { FieldEmptyState(onNavigateToCreate, canEdit) }
            } else {
                items(filteredFields, key = { it.id }) { field ->
                    FieldCard(
                        field = field,
                        canEdit = canEdit,
                        onClick = { onNavigateToEdit(field.id) },
                        onUploadImages = { onNavigateToUploadImages(field.id) },
                        onTimeSlot = { onNavigateToTimeSlot(field.id) },
                        onDelete = { showDeleteDialog = field }
                    )
                }
            } // Close the else block

            if (isLoadingMore) {
                item {
                    Box(modifier = Modifier.fillMaxWidth().padding(16.dp), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = AccentBlue)
                    }
                }
            }
        }
    }

    // Delete confirm dialog
    showDeleteDialog?.let { field ->
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
                    stringResource(id = R.string.field_delete_title),
                    fontWeight = FontWeight.ExtraBold,
                    color = TextPrimary,
                    textAlign = TextAlign.Center
                )
            },
            text = {
                Text(
                    stringResource(id = R.string.field_delete_confirm, field.name),
                    color = TextSecond,
                    textAlign = TextAlign.Center,
                    lineHeight = 20.sp
                )
            },
            confirmButton = {
                Button(
                    onClick = { field.id.let { onDeleteField(it) }; showDeleteDialog = null },
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = AccentRed)
                ) {
                    Text(stringResource(id = R.string.field_delete), fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                OutlinedButton(
                    onClick = { showDeleteDialog = null },
                    shape = RoundedCornerShape(14.dp),
                    border = BorderStroke(1.dp, DividerColor)
                ) {
                    Text(stringResource(id = R.string.field_cancel), color = TextSecond)
                }
            }
        )
    }
}
@Composable
fun FieldHeader(fieldCount: Int) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(240.dp)
            .clip(RoundedCornerShape(bottomStart = 40.dp, bottomEnd = 40.dp))
            .background(Brush.verticalGradient(listOf(DarkBg, DarkBg2)))
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            drawCircle(
                color = AccentPurple.copy(alpha = 0.2f),
                radius = 200f,
                center = Offset(size.width * 0.9f, size.height * 0.1f)
            )
            drawCircle(
                color = AccentBlue.copy(alpha = 0.15f),
                radius = 300f,
                center = Offset(size.width * 0.2f, size.height * 0.8f)
            )
        }
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(32.dp),
            verticalArrangement = Arrangement.Center
        ) {
            Text(stringResource(id = R.string.field_management), color = TextTertiary, fontSize = 16.sp, fontWeight = FontWeight.Medium)
            Spacer(Modifier.height(4.dp))
            Text(stringResource(id = R.string.field_soccer), color = Color.White, fontSize = 32.sp, fontWeight = FontWeight.Black)
            Spacer(Modifier.height(16.dp))
            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color.White.copy(alpha = 0.1f))
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(Icons.Rounded.SportsSoccer, contentDescription = null, tint = AccentGreen, modifier = Modifier.size(20.dp))
                Spacer(Modifier.width(8.dp))
                Text(stringResource(id = R.string.field_total_count, fieldCount), color = Color.White, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
            }
        }
    }
    Spacer(modifier = Modifier.height(16.dp))
}
@Composable
fun FieldCard(
    field: FieldResponse,
    canEdit: Boolean,
    onClick: () -> Unit,
    onUploadImages: () -> Unit,
    onTimeSlot: () -> Unit,
    onDelete: () -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 8.dp)
            .shadow(16.dp, RoundedCornerShape(24.dp), spotColor = Color(0x1A000000))
            .clickable { onClick() },
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = CardWhite)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            // Header: Status + Actions
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                StatusBadge(status = field.status)
                
                if (canEdit) {
                    IconButton(onClick = { expanded = !expanded }, modifier = Modifier.size(32.dp)) {
                        Icon(Icons.Rounded.MoreVert, contentDescription = stringResource(id = R.string.field_options), tint = TextSecond)
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(12.dp))

            // Main Info
            Text(
                text = field.name,
                fontSize = 20.sp,
                fontWeight = FontWeight.ExtraBold,
                color = TextPrimary,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Info rows
            InfoRow(icon = Icons.Rounded.Category, text = field.fieldType.name, tint = AccentPurple)
            Spacer(modifier = Modifier.height(6.dp))
            InfoRow(icon = Icons.Rounded.LocationCity, text = field.branch.name, tint = AccentBlue)

            Spacer(modifier = Modifier.height(12.dp))

            // Divider & Bottom Stats
            HorizontalDivider(color = DividerColor, modifier = Modifier.padding(vertical = 12.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Rounded.PhotoLibrary, null, tint = TextTertiary, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(4.dp))
                    Text(stringResource(id = R.string.field_image_count, field.images?.size ?: 0), color = TextSecond, fontSize = 13.sp, fontWeight = FontWeight.Medium)
                }
                
                if (canEdit) {
                    TextButton(
                        onClick = onUploadImages,
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                        modifier = Modifier.height(30.dp)
                    ) {
                        Text(stringResource(id = R.string.field_update_images), color = AccentBlue, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }

            // Expandable actions menu
            AnimatedVisibility(
                visible = expanded && canEdit,
                enter = fadeIn() + expandVertically(),
                exit = fadeOut() + shrinkVertically()
            ) {
                Column(modifier = Modifier.padding(top = 16.dp)) {
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        OutlinedButton(
                            onClick = { expanded = false; onClick() },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp),
                            border = BorderStroke(1.dp, DividerColor),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = TextPrimary)
                        ) {
                            Icon(Icons.Rounded.Edit, null, modifier = Modifier.size(16.dp))
                            Spacer(Modifier.width(6.dp))
                            Text(stringResource(id = R.string.field_edit))
                        }
                        
                        OutlinedButton(
                            onClick = { expanded = false; onTimeSlot() },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp),
                            border = BorderStroke(1.dp, DividerColor),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = AccentPurple)
                        ) {
                            Icon(Icons.Rounded.Schedule, null, modifier = Modifier.size(16.dp))
                            Spacer(Modifier.width(6.dp))
                            Text("Khung giờ")
                        }

                        Button(
                            onClick = { expanded = false; onDelete() },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = AccentRed.copy(0.1f), contentColor = AccentRed),
                            elevation = null
                        ) {
                            Icon(Icons.Rounded.DeleteOutline, null, modifier = Modifier.size(16.dp))
                            Spacer(Modifier.width(6.dp))
                            Text(stringResource(id = R.string.field_delete))
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun InfoRow(icon: androidx.compose.ui.graphics.vector.ImageVector, text: String, tint: Color) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier.size(28.dp).clip(CircleShape).background(tint.copy(0.1f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, contentDescription = null, tint = tint, modifier = Modifier.size(14.dp))
        }
        Spacer(modifier = Modifier.width(10.dp))
        Text(text, color = TextSecond, fontSize = 14.sp, fontWeight = FontWeight.Medium)
    }
}

@Composable
private fun StatusBadge(status: Boolean) {
    val color = if (status) AccentGreen else TextTertiary
    val text = if (status) stringResource(id = R.string.field_active) else stringResource(id = R.string.field_inactive)
    
    // Pulsing animation for active status
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val alpha by infiniteTransition.animateFloat(
        initialValue = 0.4f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseAlpha"
    )

    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(30.dp))
            .background(color.copy(alpha = 0.1f))
            .padding(horizontal = 10.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(8.dp)
                .clip(CircleShape)
                .background(if (status) color.copy(alpha = alpha) else color)
        )
        Spacer(Modifier.width(6.dp))
        Text(text, color = color, fontSize = 12.sp, fontWeight = FontWeight.Bold)
    }
}
@Composable
fun FieldCardSkeleton() {
    Card(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 8.dp),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = CardWhite)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Box(modifier = Modifier.width(100.dp).height(24.dp).clip(RoundedCornerShape(12.dp)).background(DividerColor))
            Spacer(modifier = Modifier.height(16.dp))
            Box(modifier = Modifier.fillMaxWidth(0.7f).height(24.dp).clip(RoundedCornerShape(8.dp)).background(DividerColor))
            Spacer(modifier = Modifier.height(12.dp))
            Box(modifier = Modifier.fillMaxWidth(0.4f).height(16.dp).clip(RoundedCornerShape(8.dp)).background(DividerColor))
            Spacer(modifier = Modifier.height(8.dp))
            Box(modifier = Modifier.fillMaxWidth(0.5f).height(16.dp).clip(RoundedCornerShape(8.dp)).background(DividerColor))
            Spacer(modifier = Modifier.height(16.dp))
            HorizontalDivider(color = DividerColor)
            Spacer(modifier = Modifier.height(12.dp))
            Box(modifier = Modifier.width(80.dp).height(16.dp).clip(RoundedCornerShape(8.dp)).background(DividerColor))
        }
    }
}

@Composable
fun FieldEmptyState(onNavigateToCreate: () -> Unit, canEdit: Boolean) {
    Column(
        modifier = Modifier.fillMaxWidth().padding(40.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier.size(100.dp).clip(CircleShape).background(Color.White),
            contentAlignment = Alignment.Center
        ) {
            Icon(Icons.Rounded.SportsSoccer, null, modifier = Modifier.size(50.dp), tint = DividerColor)
        }
        Spacer(Modifier.height(24.dp))
        Text(stringResource(id = R.string.field_empty_title), fontWeight = FontWeight.Bold, fontSize = 18.sp, color = TextPrimary)
        Spacer(Modifier.height(8.dp))
        Text(stringResource(id = R.string.field_empty_desc), color = TextSecond, fontSize = 14.sp, textAlign = TextAlign.Center)
        
        if (canEdit) {
            Spacer(Modifier.height(24.dp))
            Button(
                onClick = onNavigateToCreate,
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(containerColor = DarkBg)
            ) {
                Icon(Icons.Default.Add, null, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(8.dp))
                Text(stringResource(id = R.string.field_add_now), fontWeight = FontWeight.Bold)
            }
        }
    }
}
