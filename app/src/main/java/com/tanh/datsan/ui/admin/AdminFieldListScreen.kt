package com.tanh.datsan.ui.admin

import android.widget.Toast
import androidx.compose.foundation.background
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
import com.tanh.datsan.data.model.CreateFieldRequest
import com.tanh.datsan.data.model.FieldResponse
import com.tanh.datsan.data.model.UpdateFieldRequest
import com.tanh.datsan.viewmodel.AdminFieldUiState

private val FLAccentBlue  = Color(0xFF3D7EF5)
private val FLTextPri     = Color(0xFF111827)
private val FLTextSec     = Color(0xFF6B7280)
private val FLDivider     = Color(0xFFE5E7EB)
private val FLCardBg      = Color.White
private val FLPageBg      = Color(0xFFF8F9FC)
private val FLGreen       = Color(0xFF22C55E)
private val FLRed         = Color(0xFFEF4444)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminFieldListScreen(
    uiState: AdminFieldUiState,
    onAddField: () -> Unit,
    onEditField: (FieldResponse) -> Unit,
    onDeleteField: (FieldResponse) -> Unit,
    onClearMessages: () -> Unit,
    onBackClick: () -> Unit,
    // BottomSheet form state
    showForm: Boolean,
    editingField: FieldResponse?,
    onSubmitCreate: (CreateFieldRequest) -> Unit,
    onSubmitUpdate: (String, UpdateFieldRequest) -> Unit,
    onDismissForm: () -> Unit
) {
    val context = LocalContext.current
    var fieldToDelete by remember { mutableStateOf<FieldResponse?>(null) }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

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

    Box(modifier = Modifier.fillMaxSize().background(FLPageBg)) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Header
            Surface(modifier = Modifier.fillMaxWidth().shadow(elevation = 2.dp), color = FLCardBg) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 48.dp, bottom = 16.dp, start = 16.dp, end = 16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = onBackClick, modifier = Modifier.size(40.dp).background(Color(0xFFF3F4F6), CircleShape)) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Trở về", tint = FLTextPri, modifier = Modifier.size(20.dp))
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Danh sách sân", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = FLTextPri)
                        Text(uiState.branchName, fontSize = 12.sp, color = FLAccentBlue, fontWeight = FontWeight.Medium, maxLines = 1, overflow = TextOverflow.Ellipsis)
                    }
                    Button(
                        onClick = onAddField,
                        colors = ButtonDefaults.buttonColors(containerColor = FLAccentBlue),
                        shape = RoundedCornerShape(10.dp),
                        contentPadding = PaddingValues(horizontal = 14.dp, vertical = 8.dp)
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Thêm sân", fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                    }
                }
            }

            // Content
            when {
                uiState.isLoading -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            CircularProgressIndicator(color = FLAccentBlue, strokeWidth = 3.dp, modifier = Modifier.size(40.dp))
                            Spacer(modifier = Modifier.height(12.dp))
                            Text("Đang tải danh sách sân...", color = FLTextSec, fontSize = 14.sp)
                        }
                    }
                }
                uiState.fields.isEmpty() -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Box(modifier = Modifier.size(80.dp).background(Color(0xFFF3F4F6), CircleShape), contentAlignment = Alignment.Center) {
                                Icon(Icons.Default.SportsFootball, contentDescription = null, tint = FLTextSec, modifier = Modifier.size(40.dp))
                            }
                            Spacer(modifier = Modifier.height(16.dp))
                            Text("Chưa có sân nào", fontSize = 16.sp, fontWeight = FontWeight.SemiBold, color = FLTextPri)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text("Bấm \"Thêm sân\" để tạo sân đầu tiên", fontSize = 13.sp, color = FLTextSec)
                        }
                    }
                }
                else -> {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        items(uiState.fields, key = { it.id }) { field ->
                            FieldCard(
                                field = field,
                                onEdit = { onEditField(it) },
                                onDelete = { fieldToDelete = it }
                            )
                        }
                    }
                }
            }
        }
    }

    // Delete Dialog
    fieldToDelete?.let { field ->
        AlertDialog(
            onDismissRequest = { fieldToDelete = null },
            containerColor = Color.White,
            titleContentColor = FLTextPri,
            textContentColor = FLTextSec,
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Warning, contentDescription = null, tint = FLRed, modifier = Modifier.size(22.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Xóa sân", fontWeight = FontWeight.Bold, fontSize = 17.sp)
                }
            },
            text = { Text("Bạn có chắc muốn xóa sân \"${field.name}\" không?", lineHeight = 22.sp, fontSize = 14.sp) },
            confirmButton = {
                Button(
                    onClick = { onDeleteField(field); fieldToDelete = null },
                    colors = ButtonDefaults.buttonColors(containerColor = FLRed),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text("Xóa", color = Color.White, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                OutlinedButton(
                    onClick = { fieldToDelete = null },
                    shape = RoundedCornerShape(10.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, FLDivider)
                ) {
                    Text("Hủy", color = FLTextSec)
                }
            }
        )
    }

    // Field Form BottomSheet
    if (showForm) {
        ModalBottomSheet(
            onDismissRequest = onDismissForm,
            sheetState = sheetState,
            containerColor = Color.White
        ) {
            AdminFieldFormBottomSheet(
                uiState = uiState,
                editingField = editingField,
                onSubmitCreate = onSubmitCreate,
                onSubmitUpdate = onSubmitUpdate,
                onDismiss = onDismissForm
            )
        }
    }
}

@Composable
fun FieldCard(
    field: FieldResponse,
    onEdit: (FieldResponse) -> Unit,
    onDelete: (FieldResponse) -> Unit
) {
    var showMenu by remember { mutableStateOf(false) }
    val isActive = field.status

    Card(
        modifier = Modifier.fillMaxWidth().shadow(elevation = 1.dp, shape = RoundedCornerShape(14.dp)),
        colors = CardDefaults.cardColors(containerColor = FLCardBg),
        shape = RoundedCornerShape(14.dp)
    ) {
        Row(modifier = Modifier.fillMaxWidth().padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(Brush.linearGradient(listOf(Color(0xFF3D7EF5), Color(0xFF7C5CDB)))),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.SportsFootball, contentDescription = null, tint = Color.White, modifier = Modifier.size(22.dp))
            }
            Spacer(modifier = Modifier.width(14.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(field.name, fontWeight = FontWeight.SemiBold, fontSize = 15.sp, color = FLTextPri, maxLines = 1, overflow = TextOverflow.Ellipsis)
                Spacer(modifier = Modifier.height(4.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .background(Color(0xFF7C5CDB).copy(alpha = 0.1f), RoundedCornerShape(6.dp))
                            .padding(horizontal = 8.dp, vertical = 3.dp)
                    ) {
                        Text(field.fieldType.name, fontSize = 10.sp, color = Color(0xFF7C5CDB), fontWeight = FontWeight.SemiBold)
                    }
                    Box(
                        modifier = Modifier
                            .background((if (isActive) FLGreen else FLRed).copy(alpha = 0.1f), RoundedCornerShape(6.dp))
                            .padding(horizontal = 8.dp, vertical = 3.dp)
                    ) {
                        Text(if (isActive) "Hoạt động" else "Tạm đóng", fontSize = 10.sp, color = if (isActive) FLGreen else FLRed, fontWeight = FontWeight.SemiBold)
                    }
                }
            }
            Box {
                IconButton(onClick = { showMenu = true }, modifier = Modifier.size(36.dp)) {
                    Icon(Icons.Default.MoreVert, contentDescription = "Menu", tint = FLTextSec, modifier = Modifier.size(20.dp))
                }
                DropdownMenu(expanded = showMenu, onDismissRequest = { showMenu = false }, containerColor = Color.White) {
                    DropdownMenuItem(
                        text = {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Edit, contentDescription = null, tint = Color(0xFFF59E0B), modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Chỉnh sửa", fontSize = 14.sp, color = FLTextPri, fontWeight = FontWeight.Medium)
                            }
                        },
                        onClick = { showMenu = false; onEdit(field) }
                    )
                    Divider(color = FLDivider, thickness = 0.5.dp)
                    DropdownMenuItem(
                        text = {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Delete, contentDescription = null, tint = FLRed, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Xóa sân", fontSize = 14.sp, color = FLRed, fontWeight = FontWeight.Medium)
                            }
                        },
                        onClick = { showMenu = false; onDelete(field) }
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminFieldFormBottomSheet(
    uiState: AdminFieldUiState,
    editingField: FieldResponse?,
    onSubmitCreate: (CreateFieldRequest) -> Unit,
    onSubmitUpdate: (String, UpdateFieldRequest) -> Unit,
    onDismiss: () -> Unit
) {
    val isEdit = editingField != null

    var name by remember { mutableStateOf(editingField?.name ?: "") }
    var description by remember { mutableStateOf(editingField?.description ?: "") }
    var selectedTypeId by remember { mutableStateOf(editingField?.fieldType?.id ?: "") }
    var selectedTypeName by remember { mutableStateOf(editingField?.fieldType?.name ?: "") }
    var typeDropdownExpanded by remember { mutableStateOf(false) }
    var nameError by remember { mutableStateOf(false) }
    var typeError by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 20.dp, end = 20.dp, top = 8.dp, bottom = 32.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Drag handle & title
        Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
            Box(modifier = Modifier.width(40.dp).height(4.dp).background(Color(0xFFE5E7EB), RoundedCornerShape(2.dp)))
        }
        Text(
            if (isEdit) "Chỉnh sửa sân" else "Thêm sân mới",
            fontSize = 18.sp, fontWeight = FontWeight.Bold, color = FLTextPri
        )
        Divider(color = FLDivider)

        // Name
        Column {
            Text("Tên sân *", fontSize = 13.sp, fontWeight = FontWeight.Medium, color = if (nameError) FLRed else FLTextSec, modifier = Modifier.padding(bottom = 6.dp))
            OutlinedTextField(
                value = name,
                onValueChange = { name = it; nameError = false },
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("VD: Sân 5 số 1", color = FLTextSec.copy(alpha = 0.6f), fontSize = 14.sp) },
                leadingIcon = { Icon(Icons.Default.SportsFootball, contentDescription = null, tint = FLAccentBlue, modifier = Modifier.size(20.dp)) },
                isError = nameError,
                singleLine = true,
                shape = RoundedCornerShape(10.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = FLAccentBlue, unfocusedBorderColor = FLDivider,
                    focusedContainerColor = Color.White, unfocusedContainerColor = Color(0xFFF9FAFB),
                    focusedTextColor = FLTextPri, unfocusedTextColor = FLTextPri, cursorColor = FLAccentBlue
                ),
                textStyle = LocalTextStyle.current.copy(fontSize = 14.sp, color = FLTextPri)
            )
            if (nameError) Text("Vui lòng nhập tên sân", fontSize = 11.sp, color = FLRed, modifier = Modifier.padding(top = 2.dp, start = 4.dp))
        }

        // Description
        Column {
            Text("Mô tả", fontSize = 13.sp, fontWeight = FontWeight.Medium, color = FLTextSec, modifier = Modifier.padding(bottom = 6.dp))
            OutlinedTextField(
                value = description,
                onValueChange = { description = it },
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("Mô tả về sân...", color = FLTextSec.copy(alpha = 0.6f), fontSize = 14.sp) },
                singleLine = false, maxLines = 3,
                shape = RoundedCornerShape(10.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = FLAccentBlue, unfocusedBorderColor = FLDivider,
                    focusedContainerColor = Color.White, unfocusedContainerColor = Color(0xFFF9FAFB),
                    focusedTextColor = FLTextPri, unfocusedTextColor = FLTextPri, cursorColor = FLAccentBlue
                ),
                textStyle = LocalTextStyle.current.copy(fontSize = 14.sp, color = FLTextPri)
            )
        }

        // Field Type Dropdown
        Column {
            Text("Loại sân *", fontSize = 13.sp, fontWeight = FontWeight.Medium, color = if (typeError) FLRed else FLTextSec, modifier = Modifier.padding(bottom = 6.dp))
            ExposedDropdownMenuBox(expanded = typeDropdownExpanded, onExpandedChange = { typeDropdownExpanded = it }) {
                OutlinedTextField(
                    value = selectedTypeName.ifBlank { "Chọn loại sân" },
                    onValueChange = {},
                    readOnly = true,
                    isError = typeError,
                    modifier = Modifier.fillMaxWidth().menuAnchor(),
                    leadingIcon = { Icon(Icons.Default.Category, contentDescription = null, tint = if (typeError) FLRed else FLAccentBlue, modifier = Modifier.size(20.dp)) },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = typeDropdownExpanded) },
                    shape = RoundedCornerShape(10.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = if (typeError) FLRed else FLAccentBlue,
                        unfocusedBorderColor = if (typeError) FLRed else FLDivider,
                        focusedContainerColor = Color.White, unfocusedContainerColor = Color(0xFFF9FAFB),
                        cursorColor = FLAccentBlue
                    ),
                    textStyle = LocalTextStyle.current.copy(color = if (selectedTypeName.isBlank()) FLTextSec else FLTextPri, fontSize = 14.sp)
                )
                ExposedDropdownMenu(expanded = typeDropdownExpanded, onDismissRequest = { typeDropdownExpanded = false }) {
                    if (uiState.fieldTypes.isEmpty()) {
                        DropdownMenuItem(text = { Text("Đang tải...", color = FLTextSec, fontSize = 14.sp) }, onClick = {})
                    }
                    uiState.fieldTypes.forEach { type ->
                        DropdownMenuItem(
                            text = {
                                Column {
                                    Text(type.name, fontSize = 14.sp, fontWeight = FontWeight.Medium)
                                    type.description?.let { Text(it, fontSize = 12.sp, color = FLTextSec) }
                                }
                            },
                            onClick = {
                                selectedTypeId = type.id
                                selectedTypeName = type.name
                                typeError = false
                                typeDropdownExpanded = false
                            }
                        )
                    }
                }
            }
            if (typeError) Text("Vui lòng chọn loại sân", fontSize = 11.sp, color = FLRed, modifier = Modifier.padding(top = 2.dp, start = 4.dp))
        }

        Spacer(modifier = Modifier.height(4.dp))

        // Submit
        Button(
            onClick = {
                nameError = name.isBlank()
                typeError = selectedTypeId.isBlank()
                if (!nameError && !typeError) {
                    if (isEdit) {
                        onSubmitUpdate(editingField!!.id, UpdateFieldRequest(name.trim(), description.trim().ifBlank { null }, selectedTypeId))
                    } else {
                        onSubmitCreate(CreateFieldRequest(name.trim(), description.trim().ifBlank { null }, selectedTypeId, uiState.branchId))
                    }
                }
            },
            modifier = Modifier.fillMaxWidth().height(50.dp),
            enabled = !uiState.isSubmitting,
            colors = ButtonDefaults.buttonColors(containerColor = FLAccentBlue),
            shape = RoundedCornerShape(12.dp)
        ) {
            if (uiState.isSubmitting) {
                CircularProgressIndicator(color = Color.White, strokeWidth = 2.dp, modifier = Modifier.size(22.dp))
            } else {
                Icon(if (isEdit) Icons.Default.Save else Icons.Default.Add, contentDescription = null, modifier = Modifier.size(20.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text(if (isEdit) "Lưu thay đổi" else "Tạo sân", fontSize = 15.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}
