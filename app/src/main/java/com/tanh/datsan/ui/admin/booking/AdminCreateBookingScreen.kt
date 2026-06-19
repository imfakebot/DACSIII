package com.tanh.datsan.ui.admin.booking

import android.app.DatePickerDialog
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.tanh.datsan.data.model.BookingTimeSlot
import com.tanh.datsan.data.model.Branch
import com.tanh.datsan.data.model.FieldResponse
import com.tanh.datsan.data.model.CheckPriceResponseDto
import com.tanh.datsan.viewmodel.AdminCreateBookingUiState
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminCreateBookingScreen(
    branches: List<Branch>,
    fields: List<FieldResponse>,
    availableSlots: List<BookingTimeSlot>,
    selectedBranch: Branch?,
    selectedField: FieldResponse?,
    selectedDate: String,
    selectedSlot: BookingTimeSlot?,
    uiState: AdminCreateBookingUiState,
    onFetchBranches: () -> Unit,
    onSelectBranch: (Branch) -> Unit,
    onSelectField: (FieldResponse) -> Unit,
    onSelectDate: (String) -> Unit,
    onSelectSlot: (BookingTimeSlot) -> Unit,
    onCreateBooking: (String, String) -> Unit,
    onBackClick: () -> Unit,
    onResetUiState: () -> Unit,
    selectedDuration: Int,
    onSelectDuration: (Int) -> Unit,
    priceState: CheckPriceResponseDto?
) {
    val context = LocalContext.current
    var customerName by remember { mutableStateOf("") }
    var customerPhone by remember { mutableStateOf("") }
    var showBranchDropdown by remember { mutableStateOf(false) }
    var showFieldDropdown by remember { mutableStateOf(false) }


    LaunchedEffect(Unit) {
        if (branches.isEmpty()) {
            onFetchBranches()
        }
    }

    LaunchedEffect(uiState) {
        when (uiState) {
            is AdminCreateBookingUiState.Success -> {
                customerName = ""
                customerPhone = ""
            }
            is AdminCreateBookingUiState.Error -> {
            }
            else -> {}
        }
    }

    val calendar = Calendar.getInstance()
    val year = calendar.get(Calendar.YEAR)
    val month = calendar.get(Calendar.MONTH)
    val day = calendar.get(Calendar.DAY_OF_MONTH)

    val datePickerDialog = DatePickerDialog(
        context,
        { _, y, m, d ->
            val formattedDate = String.format(Locale.getDefault(), "%04d-%02d-%02d", y, m + 1, d)
            onSelectDate(formattedDate)
        },
        year, month, day
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Tạo Đơn Tại Quầy", color = Color.White, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFF1E293B)
                )
            )
        },
        containerColor = Color(0xFFF1F5F9)
    ) { paddingValues ->
        Box(modifier = Modifier.fillMaxSize().padding(paddingValues)) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {

                // Premium Card for Selection
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(4.dp),
                    shape = RoundedCornerShape(24.dp)
                ) {
                    Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                        Text("1. Chọn Sân & Thời Gian", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = Color(0xFF1E293B))

                        // Branch Selection
                        Box {
                            OutlinedTextField(
                                value = selectedBranch?.name ?: "Chọn Chi Nhánh",
                                onValueChange = {},
                                readOnly = true,
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(16.dp),
                                trailingIcon = {
                                    IconButton(onClick = { showBranchDropdown = !showBranchDropdown }) {
                                        Icon(Icons.Default.KeyboardArrowDown, "Dropdown")
                                    }
                                },
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                                    unfocusedBorderColor = Color.LightGray
                                )
                            )
                            DropdownMenu(
                                expanded = showBranchDropdown,
                                onDismissRequest = { showBranchDropdown = false },
                                modifier = Modifier.fillMaxWidth(0.9f)
                            ) {
                                branches.forEach { branch ->
                                    DropdownMenuItem(
                                        text = { Text(branch.name) },
                                        onClick = {
                                            onSelectBranch(branch)
                                            showBranchDropdown = false
                                        }
                                    )
                                }
                            }
                        }

                        // Field Selection
                        if (selectedBranch != null) {
                            Box {
                                OutlinedTextField(
                                    value = selectedField?.name ?: "Chọn Sân",
                                    onValueChange = {},
                                    readOnly = true,
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(16.dp),
                                    trailingIcon = {
                                        IconButton(onClick = { showFieldDropdown = !showFieldDropdown }) {
                                            Icon(Icons.Default.KeyboardArrowDown, "Dropdown")
                                        }
                                    },
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                                        unfocusedBorderColor = Color.LightGray
                                    )
                                )
                                DropdownMenu(
                                    expanded = showFieldDropdown,
                                    onDismissRequest = { showFieldDropdown = false },
                                    modifier = Modifier.fillMaxWidth(0.9f)
                                ) {
                                    fields.forEach { field ->
                                        DropdownMenuItem(
                                            text = { Text(field.name) },
                                            onClick = {
                                                onSelectField(field)
                                                showFieldDropdown = false
                                            }
                                        )
                                    }
                                }
                            }
                        }

                        // Date Selection
                        OutlinedTextField(
                            value = selectedDate,
                            onValueChange = {},
                            readOnly = true,
                            modifier = Modifier.fillMaxWidth().clickable { datePickerDialog.show() },
                            enabled = false,
                            shape = RoundedCornerShape(16.dp),
                            leadingIcon = { Icon(Icons.Default.DateRange, contentDescription = null, tint = MaterialTheme.colorScheme.primary) },
                            colors = OutlinedTextFieldDefaults.colors(
                                disabledTextColor = Color.Black,
                                disabledBorderColor = Color.LightGray,
                                disabledLeadingIconColor = MaterialTheme.colorScheme.primary
                            )
                        )
                    }
                }

                // Slots Grid
                if (selectedField != null) {
                    Text("2. Khung Giờ Trống", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = Color(0xFF1E293B))
                    if (availableSlots.isEmpty() && uiState !is AdminCreateBookingUiState.Loading) {
                     Text("Không có khung giờ nào trong ngày này.", color = Color.Gray, modifier = Modifier.padding(start = 8.dp))
                 } else {
                     Text("Thời lượng đá", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Color(0xFF1E293B))
                     Row(
                         modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
                         horizontalArrangement = Arrangement.spacedBy(8.dp)
                     ) {
                         listOf(60, 90, 120).forEach { duration ->
                             val isSelected = selectedDuration == duration
                             FilterChip(
                                 selected = isSelected,
                                 onClick = { onSelectDuration(duration) },
                                 label = { Text("$duration phút") },
                                 colors = FilterChipDefaults.filterChipColors(
                                     selectedContainerColor = MaterialTheme.colorScheme.primary,
                                     selectedLabelColor = Color.White
                                 )
                             )
                         }
                     }
                     LazyVerticalGrid(
                         columns = GridCells.Fixed(3),
                         modifier = Modifier.fillMaxWidth().heightIn(max = 200.dp),
                         horizontalArrangement = Arrangement.spacedBy(8.dp),
                         verticalArrangement = Arrangement.spacedBy(8.dp)
                     ) {
                         items(availableSlots) { slot ->
                             val isSelected = selectedSlot == slot
                             val isAvailable = slot.status.lowercase() == "available"
                             Box(
                                 modifier = Modifier
                                     .clip(RoundedCornerShape(12.dp))
                                     .background(
                                         when {
                                             isSelected -> MaterialTheme.colorScheme.primary
                                             isAvailable -> Color.White
                                             else -> Color(0xFFF1F5F9)
                                         }
                                     )
                                     .border(
                                         width = if (isSelected) 2.dp else 1.dp,
                                         color = if (isSelected) MaterialTheme.colorScheme.primary else Color.LightGray,
                                         shape = RoundedCornerShape(12.dp)
                                     )
                                     .clickable(enabled = isAvailable) {
                                         onSelectSlot(slot)
                                     }
                                     .padding(12.dp),
                                 contentAlignment = Alignment.Center
                             ) {
                                 Text(
                                     text = "${slot.startTime} - ${slot.endTime}",
                                     color = when {
                                         isSelected -> Color.White
                                         isAvailable -> Color.Black
                                         else -> Color.Gray
                                     },
                                     fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                     fontSize = 12.sp
                                 )
                             }
                         }
                     }
                 }
                 }

                // Customer Info & Submit
                if (selectedSlot != null) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        elevation = CardDefaults.cardElevation(4.dp),
                        shape = RoundedCornerShape(24.dp)
                    ) {
                        Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                            Text("3. Thông Tin Khách Hàng", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = Color(0xFF1E293B))

                            OutlinedTextField(
                                value = customerName,
                                onValueChange = { customerName = it },
                                label = { Text("Tên khách hàng") },
                                leadingIcon = { Icon(Icons.Default.Person, contentDescription = null) },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(16.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                                    unfocusedBorderColor = Color.LightGray
                                )
                            )

                            OutlinedTextField(
                                value = customerPhone,
                                onValueChange = { customerPhone = it },
                                label = { Text("Số điện thoại") },
                                leadingIcon = { Icon(Icons.Default.Phone, contentDescription = null) },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(16.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                                    unfocusedBorderColor = Color.LightGray
                                )
                            )

                            if (priceState != null) {
                                val price = priceState.pricing.totalPrice
                                val currency = priceState.pricing.currency
                                Spacer(modifier = Modifier.height(16.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text("Tổng tiền:", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = Color.Gray)
                                    Text(
                                        text = String.format("%,.0f %s", price, currency),
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 20.sp,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                }
                            }

                            Button(
                                onClick = { onCreateBooking(customerName, customerPhone) },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(56.dp)
                                    .clip(RoundedCornerShape(24.dp))
                                    .background(Brush.horizontalGradient(listOf(MaterialTheme.colorScheme.primary, MaterialTheme.colorScheme.secondary))),
                                colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
                                enabled = customerName.isNotBlank() && customerPhone.isNotBlank() && uiState !is AdminCreateBookingUiState.Loading
                            ) {
                                Text("Tạo Đơn", color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }

            if (uiState is AdminCreateBookingUiState.Loading) {
                Box(
                    modifier = Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.3f)),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                }
            }
            
            if (uiState is AdminCreateBookingUiState.Success) {
                AlertDialog(
                    onDismissRequest = { onResetUiState() },
                    title = { Text("Thành công", fontWeight = FontWeight.Bold) },
                    text = { Text(uiState.message) },
                    confirmButton = {
                        TextButton(onClick = { 
                            onResetUiState() 
                            onBackClick()
                        }) {
                            Text("OK")
                        }
                    }
                )
            }

            if (uiState is AdminCreateBookingUiState.Error) {
                AlertDialog(
                    onDismissRequest = { onResetUiState() },
                    title = { Text("Lỗi", fontWeight = FontWeight.Bold) },
                    text = { Text(uiState.message) },
                    confirmButton = {
                        TextButton(onClick = { onResetUiState() }) {
                            Text("Đóng")
                        }
                    }
                )
            }
        }
    }
}
