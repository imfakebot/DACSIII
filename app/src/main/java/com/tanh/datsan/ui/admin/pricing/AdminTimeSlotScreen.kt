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
import com.tanh.datsan.data.model.TimeSlotResponse
import com.tanh.datsan.viewmodel.AdminTimeSlotUiState
import java.text.NumberFormat
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminTimeSlotScreen(
    timeSlots: List<TimeSlotResponse>,
    uiState: AdminTimeSlotUiState,
    onFetchData: () -> Unit,
    onUpdateSlot: (Int, Double?, Boolean?) -> Unit,
    onResetState: () -> Unit,
    onBackClick: () -> Unit
) {
    var showEditDialog by remember { mutableStateOf<TimeSlotResponse?>(null) }
    val currencyFormatter = NumberFormat.getCurrencyInstance(Locale("vi", "VN"))

    LaunchedEffect(Unit) {
        onFetchData()
    }

    LaunchedEffect(uiState) {
        if (uiState is AdminTimeSlotUiState.Success) {
            showEditDialog = null
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Quản lý Khung Giờ & Giá", color = Color.White, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFF1E293B))
            )
        },
        containerColor = Color(0xFFF1F5F9)
    ) { paddingValues ->
        Box(modifier = Modifier.fillMaxSize().padding(paddingValues)) {
            if (timeSlots.isEmpty() && uiState !is AdminTimeSlotUiState.Loading) {
                Text(
                    text = "Không có khung giờ nào.",
                    modifier = Modifier.align(Alignment.Center),
                    color = Color.Gray
                )
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize().padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Group by field type
                    val grouped = timeSlots.groupBy { it.fieldType?.name ?: "Khác" }
                    
                    grouped.forEach { (fieldTypeName, slots) ->
                        item {
                            Text(
                                text = "Loại sân: $fieldTypeName",
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
                    title = { Text("Lỗi") },
                    text = { Text(uiState.message) },
                    confirmButton = {
                        TextButton(onClick = onResetState) { Text("OK") }
                    }
                )
            }

            if (uiState is AdminTimeSlotUiState.Success) {
                AlertDialog(
                    onDismissRequest = onResetState,
                    title = { Text("Thành công") },
                    text = { Text(uiState.message) },
                    confirmButton = {
                        TextButton(onClick = onResetState) { Text("OK") }
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
                onUpdateSlot(slot.id, price, isPeak)
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
                    text = "Giá: ${formatter.format(slot.price)}",
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
                            text = "Giờ vàng",
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
        title = { Text("Cập nhật Khung Giờ", fontWeight = FontWeight.Bold) },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                Text("Khung giờ: ${slot.startTime} - ${slot.endTime}", fontWeight = FontWeight.Medium)
                Spacer(modifier = Modifier.height(16.dp))
                
                OutlinedTextField(
                    value = priceInput,
                    onValueChange = { priceInput = it },
                    label = { Text("Giá tiền (VNĐ)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth()
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Là giờ vàng?")
                    Switch(checked = isPeak, onCheckedChange = { isPeak = it })
                }
            }
        },
        confirmButton = {
            Button(onClick = {
                val priceDouble = priceInput.toDoubleOrNull() ?: slot.price
                onConfirm(priceDouble, isPeak)
            }) {
                Text("Lưu")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Hủy") }
        }
    )
}
