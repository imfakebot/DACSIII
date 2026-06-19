package com.tanh.datsan.ui.admin.voucher

import android.app.DatePickerDialog
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.tanh.datsan.data.model.CreateVoucherDto
import com.tanh.datsan.viewmodel.AdminVoucherViewModel
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminVoucherScreen(
    onBackClick: () -> Unit,
    viewModel: AdminVoucherViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(uiState.successMessage) {
        uiState.successMessage?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearMessages()
        }
    }
    LaunchedEffect(uiState.errorMessage) {
        uiState.errorMessage?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearMessages()
        }
    }

    val context = LocalContext.current
    val isoFmt = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault()).apply {
        timeZone = TimeZone.getTimeZone("UTC")
    }
    val displayFmt = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())

    var code by remember { mutableStateOf("") }
    var discountType by remember { mutableStateOf("percent") }
    var discountPercentage by remember { mutableStateOf("") }
    var discountAmount by remember { mutableStateOf("") }
    var maxDiscountAmount by remember { mutableStateOf("") }
    var minOrderValue by remember { mutableStateOf("") }
    var quantity by remember { mutableStateOf("") }
    var isCollectible by remember { mutableStateOf(false) }
    var validFrom by remember { mutableStateOf("") }
    var validTo by remember { mutableStateOf("") }
    var validFromCal by remember { mutableStateOf<Calendar?>(null) }
    var validToCal by remember { mutableStateOf<Calendar?>(null) }

    fun showFromPicker() {
        val cal = Calendar.getInstance()
        DatePickerDialog(context, { _, y, m, d ->
            val c = Calendar.getInstance().apply { set(y, m, d, 0, 0, 0); set(Calendar.MILLISECOND, 0) }
            validFromCal = c
            validFrom = displayFmt.format(c.time)
        }, cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH)).show()
    }

    fun showToPicker() {
        val cal = Calendar.getInstance()
        DatePickerDialog(context, { _, y, m, d ->
            val c = Calendar.getInstance().apply { set(y, m, d, 23, 59, 59); set(Calendar.MILLISECOND, 0) }
            validToCal = c
            validTo = displayFmt.format(c.time)
        }, cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH)).show()
    }

    val isFormValid = code.isNotBlank() && minOrderValue.isNotBlank() &&
            quantity.isNotBlank() && validFrom.isNotBlank() && validTo.isNotBlank() &&
            (if (discountType == "percent") discountPercentage.isNotBlank() else discountAmount.isNotBlank())

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = Color(0xFFF1F5F9)
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // Header
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Brush.horizontalGradient(listOf(Color(0xFF0F172A), Color(0xFF1E293B))))
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Rounded.ArrowBack, null, tint = Color.White)
                    }
                    Spacer(Modifier.width(4.dp))
                    Icon(Icons.Rounded.LocalOffer, null, tint = Color(0xFF60A5FA), modifier = Modifier.size(22.dp))
                    Spacer(Modifier.width(8.dp))
                    Column {
                        Text("Tạo Mã Giảm Giá", color = Color.White, fontWeight = FontWeight.ExtraBold, fontSize = 18.sp)
                        Text("POST /voucher", color = Color(0xFF94A3B8), fontSize = 11.sp)
                    }
                }
            }

            // Form
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // Mã voucher
                FormSection(title = "Mã giảm giá") {
                    OutlinedTextField(
                        value = code,
                        onValueChange = { code = it.uppercase() },
                        label = { Text("Mã nhập vào *") },
                        placeholder = { Text("VD: SUMMER2024") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        leadingIcon = { Icon(Icons.Rounded.Tag, null, tint = Color(0xFF3B82F6)) },
                        shape = RoundedCornerShape(12.dp)
                    )
                }

                // Loại giảm
                FormSection(title = "Loại giảm giá") {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        FilterChip(
                            selected = discountType == "percent",
                            onClick = { discountType = "percent" },
                            label = { Text("Theo % (discountPercentage)") },
                            modifier = Modifier.weight(1f)
                        )
                        FilterChip(
                            selected = discountType == "amount",
                            onClick = { discountType = "amount" },
                            label = { Text("Số tiền cố định (discountAmount)") },
                            modifier = Modifier.weight(1f)
                        )
                    }

                    AnimatedVisibility(visible = discountType == "percent") {
                        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            OutlinedTextField(
                                value = discountPercentage,
                                onValueChange = { discountPercentage = it },
                                label = { Text("discountPercentage (%) *") },
                                placeholder = { Text("VD: 10") },
                                modifier = Modifier.fillMaxWidth(),
                                singleLine = true,
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                shape = RoundedCornerShape(12.dp)
                            )
                            OutlinedTextField(
                                value = maxDiscountAmount,
                                onValueChange = { maxDiscountAmount = it },
                                label = { Text("maxDiscountAmount (VNĐ)") },
                                placeholder = { Text("VD: 100000 — Tùy chọn") },
                                modifier = Modifier.fillMaxWidth(),
                                singleLine = true,
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                shape = RoundedCornerShape(12.dp)
                            )
                        }
                    }

                    AnimatedVisibility(visible = discountType == "amount") {
                        OutlinedTextField(
                            value = discountAmount,
                            onValueChange = { discountAmount = it },
                            label = { Text("discountAmount (VNĐ) *") },
                            placeholder = { Text("VD: 50000") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            shape = RoundedCornerShape(12.dp)
                        )
                    }
                }

                // Điều kiện
                FormSection(title = "Điều kiện áp dụng") {
                    OutlinedTextField(
                        value = minOrderValue,
                        onValueChange = { minOrderValue = it },
                        label = { Text("minOrderValue (VNĐ) *") },
                        placeholder = { Text("VD: 500000") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        leadingIcon = { Icon(Icons.Rounded.ShoppingCart, null, tint = Color(0xFF3B82F6)) },
                        shape = RoundedCornerShape(12.dp)
                    )
                    OutlinedTextField(
                        value = quantity,
                        onValueChange = { quantity = it },
                        label = { Text("quantity (Số lượng mã) *") },
                        placeholder = { Text("VD: 100") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        leadingIcon = { Icon(Icons.Rounded.Inventory2, null, tint = Color(0xFF3B82F6)) },
                        shape = RoundedCornerShape(12.dp)
                    )
                }

                // Thời hạn
                FormSection(title = "Thời hạn hiệu lực") {
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        OutlinedTextField(
                            value = validFrom,
                            onValueChange = {},
                            label = { Text("validFrom *") },
                            placeholder = { Text("Chọn ngày") },
                            modifier = Modifier.weight(1f),
                            readOnly = true,
                            singleLine = true,
                            trailingIcon = {
                                IconButton(onClick = { showFromPicker() }) {
                                    Icon(Icons.Rounded.CalendarMonth, null, tint = Color(0xFF3B82F6))
                                }
                            },
                            shape = RoundedCornerShape(12.dp)
                        )
                        OutlinedTextField(
                            value = validTo,
                            onValueChange = {},
                            label = { Text("validTo *") },
                            placeholder = { Text("Chọn ngày") },
                            modifier = Modifier.weight(1f),
                            readOnly = true,
                            singleLine = true,
                            trailingIcon = {
                                IconButton(onClick = { showToPicker() }) {
                                    Icon(Icons.Rounded.CalendarMonth, null, tint = Color(0xFF3B82F6))
                                }
                            },
                            shape = RoundedCornerShape(12.dp)
                        )
                    }
                }

                // Cài đặt thêm
                FormSection(title = "Cài đặt thêm") {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text("isCollectible", fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                            Text("User phải bấm Lưu mới dùng được", fontSize = 12.sp, color = Color(0xFF64748B))
                        }
                        Switch(checked = isCollectible, onCheckedChange = { isCollectible = it })
                    }
                }

                // Submit button
                Button(
                    onClick = {
                        val dto = CreateVoucherDto(
                            code = code.trim(),
                            discountPercentage = if (discountType == "percent") discountPercentage.toDoubleOrNull() else null,
                            discountAmount = if (discountType == "amount") discountAmount.toDoubleOrNull() else null,
                            maxDiscountAmount = maxDiscountAmount.toDoubleOrNull(),
                            minOrderValue = minOrderValue.toDoubleOrNull() ?: 0.0,
                            validFrom = validFromCal?.let { isoFmt.format(it.time) } ?: "",
                            validTo = validToCal?.let { isoFmt.format(it.time) } ?: "",
                            quantity = quantity.toIntOrNull() ?: 1,
                            isCollectible = isCollectible
                        )
                        viewModel.createVoucher(dto)
                    },
                    enabled = isFormValid && !uiState.isCreating,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF3B82F6))
                ) {
                    if (uiState.isCreating) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            color = Color.White,
                            strokeWidth = 2.dp
                        )
                        Spacer(Modifier.width(8.dp))
                        Text("Đang tạo...", fontWeight = FontWeight.Bold)
                    } else {
                        Icon(Icons.Rounded.Add, null, modifier = Modifier.size(20.dp))
                        Spacer(Modifier.width(8.dp))
                        Text("Tạo mã giảm giá", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    }
                }

                Spacer(Modifier.height(24.dp))
            }
        }
    }
}

@Composable
fun FormSection(title: String, content: @Composable ColumnScope.() -> Unit) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = Color.White,
        shadowElevation = 1.dp
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(3.dp, 16.dp)
                        .background(Color(0xFF3B82F6), RoundedCornerShape(2.dp))
                )
                Spacer(Modifier.width(8.dp))
                Text(title, fontWeight = FontWeight.Bold, fontSize = 13.sp, color = Color(0xFF475569))
            }
            content()
        }
    }
}
