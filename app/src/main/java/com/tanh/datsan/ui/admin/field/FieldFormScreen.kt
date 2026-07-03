package com.tanh.datsan.ui.admin.field

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.tanh.datsan.R
import com.tanh.datsan.data.model.Branch
import com.tanh.datsan.data.model.CreateFieldDto
import com.tanh.datsan.data.model.FieldResponse
import com.tanh.datsan.data.model.FieldType
import com.tanh.datsan.data.model.UpdateFieldDto
import com.tanh.datsan.ui.admin.field.AdminUiState
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.Notes
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
private val DarkBg      = Color(0xFF0F172A)
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
fun FieldFormScreen(
    fieldId: String?,
    userRole: String,
    uiState: AdminUiState,
    selectedField: FieldResponse?,
    branches: List<Branch>,
    fieldTypes: List<FieldType>,
    utilities: List<com.tanh.datsan.data.model.Utility>,
    onFetchField: (String) -> Unit,
    onCreateField: (CreateFieldDto) -> Unit,
    onUpdateField: (String, UpdateFieldDto) -> Unit,
    onBackClick: () -> Unit,
    onNavigateToUploadImages: (String) -> Unit,
    onNavigateToTimeSlot: (String) -> Unit,
    onResetUiState: () -> Unit,
    onClearSelectedField: () -> Unit
) {
    val context = LocalContext.current
    val isEditing = fieldId != null
    
    val canEdit = userRole == "branch_manager" || userRole == "super_admin"

    var name by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var status by remember { mutableStateOf(true) }

    var selectedBranchId by remember { mutableStateOf("") }
    var selectedFieldTypeId by remember { mutableStateOf("") }
    var selectedUtilityIds by remember { mutableStateOf<List<Int>>(emptyList()) }

    var showBranchSheet by remember { mutableStateOf(false) }
    var showTypeSheet by remember { mutableStateOf(false) }

    LaunchedEffect(fieldId) {
        if (isEditing) {
            onFetchField(fieldId!!)
        } else {
            onClearSelectedField()
        }
    }

    LaunchedEffect(selectedField) {
        if (isEditing && selectedField != null) {
            name = selectedField.name
            description = selectedField.description ?: ""
            status = selectedField.status
            selectedBranchId = selectedField.branch.id
            selectedFieldTypeId = selectedField.fieldType.id
            selectedUtilityIds = selectedField.utilities?.map { it.id } ?: emptyList()
        }
    }

    LaunchedEffect(uiState) {
        when (uiState) {
            is AdminUiState.Success -> {
                uiState.message?.let {
                    Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
                }
                onResetUiState()
                if (isEditing) {
                    onBackClick()
                } else {
                    if (uiState.data is String) {
                        onNavigateToUploadImages(uiState.data as String)
                    }
                }
            }
            is AdminUiState.Error -> {
                Toast.makeText(context, uiState.message, Toast.LENGTH_SHORT).show()
                onResetUiState()
            }
            else -> {}
        }
    }

    Scaffold(
        containerColor = AppBg,
        topBar = {
            TopAppBar(
                title = { 
                    Text(
                        if (!canEdit) stringResource(id = R.string.field_form_detail_title) 
                        else if (isEditing) stringResource(id = R.string.field_form_edit_title) 
                        else stringResource(id = R.string.field_form_add_title),
                        fontWeight = FontWeight.ExtraBold,
                        color = Color.White
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = DarkBg
                )
            )
        },
        bottomBar = {
            if (canEdit) {
                Surface(
                    color = CardWhite,
                    shadowElevation = 16.dp,
                    shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
                ) {
                    Box(modifier = Modifier.padding(16.dp).navigationBarsPadding()) {
                        Button(
                            onClick = {
                                if (name.isBlank() || selectedBranchId.isBlank() || selectedFieldTypeId.isBlank()) {
                                    Toast.makeText(context, context.getString(R.string.field_form_val_required), Toast.LENGTH_SHORT).show()
                                    return@Button
                                }
                                val reqDto = UpdateFieldDto(
                                    name = name,
                                    description = description.ifBlank { null },
                                    status = status,
                                    fieldTypeId = selectedFieldTypeId,
                                    branchId = selectedBranchId,
                                    utilityIds = selectedUtilityIds.ifEmpty { null }
                                )
                                if (isEditing) {
                                    onUpdateField(fieldId, reqDto)
                                } else {
                                    onCreateField(
                                        CreateFieldDto(
                                            name = name,
                                            description = description.ifBlank { null },
                                            status = status,
                                            fieldTypeId = selectedFieldTypeId,
                                            branchId = selectedBranchId,
                                            utilityIds = selectedUtilityIds.ifEmpty { null }
                                        )
                                    )
                                }
                            },
                            modifier = Modifier.fillMaxWidth().height(54.dp),
                            shape = RoundedCornerShape(16.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = DarkBg),
                            enabled = uiState !is AdminUiState.Loading
                        ) {
                            if (uiState is AdminUiState.Loading) {
                                CircularProgressIndicator(modifier = Modifier.size(24.dp), color = Color.White, strokeWidth = 3.dp)
                            } else {
                                Text(if (isEditing) stringResource(id = R.string.field_form_btn_update) else stringResource(id = R.string.field_form_btn_create), fontWeight = FontWeight.Bold, fontSize = 16.sp)
                            }
                        }
                    }
                }
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 20.dp)
        ) {
            FormSectionTitle(stringResource(id = R.string.field_form_basic_info), Icons.Rounded.Info)
            FormCard {
                FormField(
                    label = stringResource(id = R.string.field_form_name_label),
                    value = name,
                    onValueChange = { if (canEdit) name = it },
                    icon = Icons.Rounded.SportsSoccer,
                    placeholder = stringResource(id = R.string.field_form_name_hint),
                    enabled = canEdit
                )
                FormDivider()
                FormSelectorRow(
                    icon = Icons.Rounded.Category,
                    iconTint = AccentPurple,
                    label = stringResource(id = R.string.field_form_type_label),
                    value = fieldTypes.find { it.id == selectedFieldTypeId }?.name ?: stringResource(id = R.string.field_form_type_hint),
                    placeholder = selectedFieldTypeId.isBlank(),
                    enabled = canEdit,
                    onClick = { if (canEdit) showTypeSheet = true }
                )
                FormDivider()
                FormSelectorRow(
                    icon = Icons.Rounded.LocationCity,
                    iconTint = AccentBlue,
                    label = stringResource(id = R.string.field_form_branch_label),
                    value = branches.find { it.id == selectedBranchId }?.name ?: stringResource(id = R.string.field_form_branch_hint),
                    placeholder = selectedBranchId.isBlank(),
                    enabled = canEdit,
                    onClick = { if (canEdit) showBranchSheet = true }
                )
            }

            Spacer(Modifier.height(16.dp))
            @OptIn(ExperimentalLayoutApi::class)
            if (utilities.isNotEmpty()) {
                FormSectionTitle(stringResource(id = R.string.field_form_utility_section), Icons.Rounded.Star)
                FormCard {
                    Column(modifier = Modifier.padding(16.dp)) {
                        FlowRow(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(8.dp),
                            verticalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(4.dp)
                        ) {
                            utilities.forEach { utility ->
                                val isSelected = selectedUtilityIds.contains(utility.id)
                                FilterChip(
                                    selected = isSelected,
                                    onClick = {
                                        if (canEdit) {
                                            selectedUtilityIds = if (isSelected) {
                                                selectedUtilityIds - utility.id
                                            } else {
                                                selectedUtilityIds + utility.id
                                            }
                                        }
                                    },
                                    label = { Text(utility.name, fontSize = 13.sp) },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = AccentBlue.copy(alpha = 0.15f),
                                        selectedLabelColor = AccentBlue
                                    ),
                                    border = FilterChipDefaults.filterChipBorder(
                                        enabled = canEdit,
                                        selected = isSelected,
                                        borderColor = if (isSelected) AccentBlue else DividerColor,
                                        selectedBorderColor = AccentBlue
                                    ),
                                    shape = RoundedCornerShape(12.dp)
                                )
                            }
                        }
                    }
                }
                Spacer(Modifier.height(16.dp))
            }
            FormSectionTitle(stringResource(id = R.string.field_form_desc_status_section), Icons.Rounded.Description)
            FormCard {
                FormField(
                    label = stringResource(id = R.string.field_type_desc_label),
                    value = description,
                    onValueChange = { if (canEdit) description = it },
                    icon = Icons.AutoMirrored.Rounded.Notes,
                    placeholder = stringResource(id = R.string.field_form_desc_hint),
                    enabled = canEdit
                )
                FormDivider()
                Row(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier.size(36.dp).clip(CircleShape).background(if (status) AccentGreen.copy(0.12f) else DividerColor),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Rounded.PowerSettingsNew, null, tint = if (status) AccentGreen else TextTertiary, modifier = Modifier.size(20.dp))
                        }
                        Spacer(Modifier.width(12.dp))
                        Text(
                            if (status) stringResource(id = R.string.field_active) else stringResource(id = R.string.field_inactive),
                            color = if (status) AccentGreen else TextSecond,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Switch(
                        checked = status,
                        onCheckedChange = { if (canEdit) status = it },
                        enabled = canEdit,
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color.White,
                            checkedTrackColor = AccentGreen,
                            uncheckedThumbColor = TextTertiary,
                            uncheckedTrackColor = DividerColor
                        )
                    )
                }
            }
            
            Spacer(Modifier.height(100.dp)) // padding for bottom bar
        }
    }

    // Bottom Sheet: Chọn Chi Nhánh
    if (showBranchSheet && canEdit) {
        ModalBottomSheet(
            onDismissRequest = { showBranchSheet = false },
            containerColor = CardWhite,
            shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
            dragHandle = { BottomSheetDefaults.DragHandle() }
        ) {
            Column(modifier = Modifier.fillMaxWidth().padding(bottom = 32.dp)) {
                Text(
                    stringResource(id = R.string.field_form_branch_hint),
                    modifier = Modifier.padding(start = 20.dp, top = 4.dp, bottom = 8.dp),
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 18.sp,
                    color = TextPrimary
                )
                HorizontalDivider(color = DividerColor)
                LazyColumn {
                    items(branches) { branch ->
                        val isSel = branch.id == selectedBranchId
                        ListItem(
                            headlineContent = {
                                Text(
                                    branch.name,
                                    fontWeight = if (isSel) FontWeight.Bold else FontWeight.Normal,
                                    color = if (isSel) AccentBlue else TextPrimary
                                )
                            },
                            leadingContent = {
                                Box(
                                    modifier = Modifier.size(36.dp).clip(CircleShape)
                                        .background(if (isSel) AccentBlue.copy(0.12f) else AppBg),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(Icons.Rounded.LocationCity, null, tint = if (isSel) AccentBlue else TextTertiary, modifier = Modifier.size(20.dp))
                                }
                            },
                            trailingContent = {
                                if (isSel) Icon(Icons.Default.Check, null, tint = AccentBlue)
                            },
                            modifier = Modifier.clickable { selectedBranchId = branch.id; showBranchSheet = false }
                        )
                        HorizontalDivider(modifier = Modifier.padding(start = 68.dp), color = DividerColor)
                    }
                }
            }
        }
    }

    // Bottom Sheet: Chọn Loại Sân
    if (showTypeSheet && canEdit) {
        ModalBottomSheet(
            onDismissRequest = { showTypeSheet = false },
            containerColor = CardWhite,
            shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
            dragHandle = { BottomSheetDefaults.DragHandle() }
        ) {
            Column(modifier = Modifier.fillMaxWidth().padding(bottom = 32.dp)) {
                Text(
                    stringResource(id = R.string.field_form_type_hint),
                    modifier = Modifier.padding(start = 20.dp, top = 4.dp, bottom = 8.dp),
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 18.sp,
                    color = TextPrimary
                )
                HorizontalDivider(color = DividerColor)
                LazyColumn {
                    items(fieldTypes) { fType ->
                        val isSel = fType.id == selectedFieldTypeId
                        ListItem(
                            headlineContent = {
                                Text(
                                    fType.name,
                                    fontWeight = if (isSel) FontWeight.Bold else FontWeight.Normal,
                                    color = if (isSel) AccentPurple else TextPrimary
                                )
                            },
                            leadingContent = {
                                Box(
                                    modifier = Modifier.size(36.dp).clip(CircleShape)
                                        .background(if (isSel) AccentPurple.copy(0.12f) else AppBg),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(Icons.Rounded.Category, null, tint = if (isSel) AccentPurple else TextTertiary, modifier = Modifier.size(20.dp))
                                }
                            },
                            trailingContent = {
                                if (isSel) Icon(Icons.Default.Check, null, tint = AccentPurple)
                            },
                            modifier = Modifier.clickable { selectedFieldTypeId = fType.id; showTypeSheet = false }
                        )
                        HorizontalDivider(modifier = Modifier.padding(start = 68.dp), color = DividerColor)
                    }
                }
            }
        }
    }
}
@Composable
private fun FormSectionTitle(title: String, icon: ImageVector) {
    Row(
        modifier = Modifier.padding(start = 20.dp, end = 16.dp, top = 4.dp, bottom = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icon, null, tint = TextSecond, modifier = Modifier.size(14.dp))
        Spacer(Modifier.width(6.dp))
        Text(title, color = TextSecond, fontSize = 13.sp, fontWeight = FontWeight.Bold)
    }
}

@Composable
private fun FormCard(content: @Composable ColumnScope.() -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth().shadow(12.dp, RoundedCornerShape(24.dp), spotColor = Color(0x1A000000)),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = CardWhite)
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            content()
        }
    }
}

@Composable
private fun FormDivider() {
    HorizontalDivider(modifier = Modifier.padding(start = 60.dp), color = DividerColor)
}

@Composable
private fun FormField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    icon: ImageVector,
    placeholder: String = "",
    enabled: Boolean = true
) {
    Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp)) {
        Text(label, fontSize = 11.sp, color = TextTertiary, fontWeight = FontWeight.Bold)
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth().padding(top = 8.dp)) {
            Box(
                modifier = Modifier.size(36.dp).clip(RoundedCornerShape(10.dp)).background(AppBg),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, null, tint = TextSecond, modifier = Modifier.size(18.dp))
            }
            Spacer(Modifier.width(12.dp))
            Box(modifier = Modifier.weight(1f)) {
                if (value.isEmpty() && placeholder.isNotEmpty()) {
                    Text(placeholder, color = TextTertiary, fontSize = 15.sp)
                }
                androidx.compose.foundation.text.BasicTextField(
                    value = value,
                    onValueChange = onValueChange,
                    enabled = enabled,
                    textStyle = androidx.compose.ui.text.TextStyle(
                        color = TextPrimary,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Medium
                    ),
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
            }
        }
    }
}

@Composable
private fun FormSelectorRow(
    icon: ImageVector,
    iconTint: Color,
    label: String,
    value: String,
    placeholder: Boolean,
    enabled: Boolean,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(enabled = enabled) { onClick() }
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier.size(36.dp).clip(RoundedCornerShape(10.dp)).background(iconTint.copy(0.1f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, null, tint = iconTint, modifier = Modifier.size(18.dp))
        }
        Spacer(Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(label, fontSize = 11.sp, color = TextTertiary, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(4.dp))
            Text(value, color = if (placeholder) TextTertiary else TextPrimary, fontSize = 15.sp, fontWeight = FontWeight.Medium)
        }
        if (enabled) {
            Icon(Icons.Rounded.ChevronRight, null, tint = TextTertiary)
        }
    }
}
