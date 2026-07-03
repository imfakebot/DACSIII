package com.tanh.datsan.ui.admin.pricing

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.res.stringResource
import com.tanh.datsan.data.model.TimeSlotResponse
import com.tanh.datsan.data.model.FieldResponse
import com.tanh.datsan.ui.admin.pricing.AdminTimeSlotUiState
import com.tanh.datsan.R
import androidx.compose.material.icons.filled.Add
import java.text.NumberFormat
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminTimeSlotScreen(
    timeSlots: List<TimeSlotResponse>,
    fields: List<FieldResponse>,
    uiState: AdminTimeSlotUiState,
    onFetchData: () -> Unit,
    onUpdateSlot: (Int, Double?, Boolean?, String) -> Unit,
    onCreateSlot: (String, String, String, Double, Boolean) -> Unit,
    onResetState: () -> Unit,
    onBackClick: () -> Unit
) {
    var showEditDialog by remember { mutableStateOf<TimeSlotResponse?>(null) }
    var showCreateDialog by remember { mutableStateOf(false) }
    val currencyFormatter = remember {
        NumberFormat.getCurrencyInstance(java.util.Locale.Builder().setLanguage("vi").setRegion("VN").build())
    }

    LaunchedEffect(Unit) {
        onFetchData()
    }

    LaunchedEffect(uiState) {
        if (uiState is AdminTimeSlotUiState.Success) {
            showEditDialog = null
            showCreateDialog = false
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(id = R.string.timeslot_management), color = Color.White, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFF1E293B))
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showCreateDialog = true },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = Color.White
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add Time Slot")
            }
        },
        containerColor = Color(0xFFF1F5F9)
    ) { paddingValues ->
        Box(modifier = Modifier.fillMaxSize().padding(paddingValues)) {
            if (timeSlots.isEmpty() && uiState !is AdminTimeSlotUiState.Loading) {
                Text(
                    text = stringResource(id = R.string.timeslot_empty),
                    modifier = Modifier.align(Alignment.Center),
                    color = Color.Gray
                )
            } else {
                val otherFieldType = stringResource(id = R.string.timeslot_field_type_other)
                LazyColumn(
                    modifier = Modifier.fillMaxSize().padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Group by field name
                    val grouped = timeSlots.groupBy { it.field?.name ?: "Sân khác" }
                    
                    grouped.forEach { (fieldName, slots) ->
                        item {
                            Text(
                                text = "Sân: $fieldName",
                                fontWeight = FontWeight.Bold,
                                fontSize = 18.sp,
                                color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.padding(top = 8.dp, bottom = 4.dp)
                            )
                        }
                        
                        items(slots) { slot ->
                            TimeSlotItem(
                                slot = slot,
                                formatter = currencyFormatter,
                                onEditClick = { showEditDialog = slot }
                            )
                        }
                    }
                }
            }

            if (uiState is AdminTimeSlotUiState.Loading) {
                Box(
                    modifier = Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.3f)),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                }
            }

            if (uiState is AdminTimeSlotUiState.Error) {
                AlertDialog(
                    onDismissRequest = onResetState,
                    title = { Text(stringResource(id = R.string.timeslot_error)) },
                    text = { Text(uiState.message) },
                    confirmButton = {
                        TextButton(onClick = onResetState) { Text(stringResource(id = R.string.timeslot_ok)) }
                    }
                )
            }

            if (uiState is AdminTimeSlotUiState.Success) {
                AlertDialog(
                    onDismissRequest = onResetState,
                    title = { Text(stringResource(id = R.string.timeslot_success)) },
                    text = { Text(uiState.message) },
                    confirmButton = {
                        TextButton(onClick = onResetState) { Text(stringResource(id = R.string.timeslot_ok)) }
                    }
                )
            }
        }
    }

    showEditDialog?.let { slot ->
        EditTimeSlotDialog(
            slot = slot,
            onDismiss = { showEditDialog = null },
            onConfirm = { price, isPeak ->
                onUpdateSlot(slot.id, price, isPeak, slot.field?.id ?: "")
            }
        )
    }
    
    if (showCreateDialog) {
        CreateTimeSlotDialog(
            fields = fields,
            onDismiss = { showCreateDialog = false },
            onConfirm = { fieldId, start, end, price, isPeak ->
                onCreateSlot(fieldId, start, end, price, isPeak)
            }
        )
    }
}

@Composable
fun TimeSlotItem(slot: TimeSlotResponse, formatter: NumberFormat, onEditClick: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth().clickable { onEditClick() },
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text(
                    text = "${slot.startTime} - ${slot.endTime}",
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = Color(0xFF1E293B)
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = stringResource(id = R.string.timeslot_price_label, formatter.format(slot.price)),
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Medium
                )
            }
            
            Row(verticalAlignment = Alignment.CenterVertically) {
                if (slot.isPeakHour) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color(0xFFFEF3C7))
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = stringResource(id = R.string.timeslot_peak),
                            color = Color(0xFFD97706),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                }
                Icon(Icons.Default.Edit, contentDescription = "Edit", tint = Color.Gray, modifier = Modifier.size(20.dp))
            }
        }
    }
}

@Composable
fun EditTimeSlotDialog(
    slot: TimeSlotResponse,
    onDismiss: () -> Unit,
    onConfirm: (Double, Boolean) -> Unit
) {
    var priceInput by remember { mutableStateOf(slot.price.toLong().toString()) }
    var isPeak by remember { mutableStateOf(slot.isPeakHour) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(id = R.string.timeslot_edit_title), fontWeight = FontWeight.Bold) },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(stringResource(id = R.string.timeslot_edit_slot, slot.startTime, slot.endTime), fontWeight = FontWeight.Medium)
                Spacer(modifier = Modifier.height(16.dp))
                
                OutlinedTextField(
                    value = priceInput,
                    onValueChange = { priceInput = it },
                    label = { Text(stringResource(id = R.string.timeslot_edit_price_input)) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth()
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(stringResource(id = R.string.timeslot_edit_is_peak))
                    Switch(checked = isPeak, onCheckedChange = { isPeak = it })
                }
            }
        },
        confirmButton = {
            Button(onClick = {
                val priceDouble = priceInput.toDoubleOrNull() ?: slot.price
                onConfirm(priceDouble, isPeak)
            }) {
                Text(stringResource(id = R.string.timeslot_save))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text(stringResource(id = R.string.timeslot_cancel)) }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateTimeSlotDialog(
    fields: List<FieldResponse>,
    onDismiss: () -> Unit,
    onConfirm: (String, String, String, Double, Boolean) -> Unit
) {
    val selectedField = fields.firstOrNull()
    var startTime by remember { mutableStateOf("00:00") }
    var endTime by remember { mutableStateOf("01:00") }
    var priceInput by remember { mutableStateOf("") }
    var isPeak by remember { mutableStateOf(false) }

    val timeOptions = remember {
        val list = mutableListOf<String>()
        for (h in 0..23) {
            val hs = h.toString().padStart(2, '0')
            list.add("$hs:00")
            list.add("$hs:30")
        }
        list
    }
    var startExpanded by remember { mutableStateOf(false) }
    var endExpanded by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Thêm Khung Giờ", fontWeight = FontWeight.Bold) },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                OutlinedTextField(
                    value = selectedField?.name ?: "Không xác định",
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Sân Bóng") },
                    colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors(),
                    modifier = Modifier.fillMaxWidth()
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    ExposedDropdownMenuBox(
                        expanded = startExpanded,
                        onExpandedChange = { startExpanded = !startExpanded },
                        modifier = Modifier.weight(1f)
                    ) {
                        OutlinedTextField(
                            value = startTime,
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Bắt đầu") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = startExpanded) },
                            colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors(),
                            modifier = Modifier.menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable, true).fillMaxWidth()
                        )
                        ExposedDropdownMenu(
                            expanded = startExpanded,
                            onDismissRequest = { startExpanded = false },
                            modifier = Modifier.heightIn(max = 200.dp)
                        ) {
                            timeOptions.forEach { time ->
                                DropdownMenuItem(
                                    text = { Text(time) },
                                    onClick = { startTime = time; startExpanded = false }
                                )
                            }
                        }
                    }

                    ExposedDropdownMenuBox(
                        expanded = endExpanded,
                        onExpandedChange = { endExpanded = !endExpanded },
                        modifier = Modifier.weight(1f)
                    ) {
                        OutlinedTextField(
                            value = endTime,
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Kết thúc") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = endExpanded) },
                            colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors(),
                            modifier = Modifier.menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable, true).fillMaxWidth()
                        )
                        ExposedDropdownMenu(
                            expanded = endExpanded,
                            onDismissRequest = { endExpanded = false },
                            modifier = Modifier.heightIn(max = 200.dp)
                        ) {
                            timeOptions.forEach { time ->
                                DropdownMenuItem(
                                    text = { Text(time) },
                                    onClick = { endTime = time; endExpanded = false }
                                )
                            }
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(8.dp))
                
                OutlinedTextField(
                    value = priceInput,
                    onValueChange = { priceInput = it },
                    label = { Text("Giá tiền") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth()
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Là Giờ Cao Điểm?")
                    Switch(checked = isPeak, onCheckedChange = { isPeak = it })
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (selectedField != null && priceInput.isNotEmpty()) {
                        val priceDouble = priceInput.toDoubleOrNull() ?: 0.0
                        // Add seconds
                        val start = if (startTime.length == 5) "$startTime:00" else startTime
                        val end = if (endTime.length == 5) "$endTime:00" else endTime
                        
                        onConfirm(selectedField.id, start, end, priceDouble, isPeak)
                        onDismiss()
                    }
                },
                enabled = selectedField != null && priceInput.isNotEmpty() && startTime.isNotEmpty() && endTime.isNotEmpty()
            ) {
                Text("Lưu")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text(stringResource(id = R.string.timeslot_cancel)) }
        }
    )
}
