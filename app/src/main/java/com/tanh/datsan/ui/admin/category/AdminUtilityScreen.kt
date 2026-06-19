package com.tanh.datsan.ui.admin.category

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.DeleteForever
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.tanh.datsan.data.model.Utility
import com.tanh.datsan.viewmodel.AdminUtilityUiState
import com.tanh.datsan.viewmodel.AdminUtilityViewModel

private val DarkBg = Color(0xFF0F172A)
private val AccentGreen = Color(0xFF10B981)
private val AccentRed = Color(0xFFEF4444)
private val AppBg = Color(0xFFF1F5F9)
private val CardWhite = Color.White
private val TextPrimary = Color(0xFF0F172A)
private val TextSecond = Color(0xFF64748B)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminUtilityScreen(
    viewModel: AdminUtilityViewModel = hiltViewModel(),
    onBackClick: () -> Unit
) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsState()
    val utilities by viewModel.utilities.collectAsState()

    var showFormDialog by remember { mutableStateOf(false) }
    var editingUtility by remember { mutableStateOf<Utility?>(null) }
    var showDeleteDialog by remember { mutableStateOf<Utility?>(null) }

    LaunchedEffect(Unit) {
        viewModel.fetchUtilities()
    }

    LaunchedEffect(uiState) {
        when (uiState) {
            is AdminUtilityUiState.Success -> {
                Toast.makeText(context, (uiState as AdminUtilityUiState.Success).message, Toast.LENGTH_SHORT).show()
                viewModel.resetUiState()
                showFormDialog = false
                showDeleteDialog = null
            }
            is AdminUtilityUiState.Error -> {
                Toast.makeText(context, (uiState as AdminUtilityUiState.Error).message, Toast.LENGTH_SHORT).show()
                viewModel.resetUiState()
            }
            else -> {}
        }
    }

    Scaffold(
        containerColor = AppBg,
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Quản lý Tiện Ích", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = CardWhite,
                    titleContentColor = TextPrimary
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    editingUtility = null
                    showFormDialog = true
                },
                containerColor = DarkBg,
                contentColor = Color.White,
                shape = RoundedCornerShape(18.dp)
            ) {
                Icon(Icons.Default.Add, contentDescription = "Thêm mới")
            }
        }
    ) { padding ->
        if (uiState is AdminUtilityUiState.Loading && utilities.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = AccentGreen)
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(utilities) { util ->
                    UtilityItemCard(
                        utility = util,
                        onEdit = {
                            editingUtility = util
                            showFormDialog = true
                        },
                        onDelete = {
                            showDeleteDialog = util
                        }
                    )
                }
            }
        }
    }

    if (showFormDialog) {
        UtilityFormDialog(
            initialUtility = editingUtility,
            onDismiss = { showFormDialog = false },
            onConfirm = { name, price, type ->
                if (editingUtility == null) {
                    viewModel.createUtility(name, null, price, type)
                } else {
                    viewModel.updateUtility(editingUtility!!.id, name, null, price, type)
                }
            }
        )
    }

    showDeleteDialog?.let { util ->
        AlertDialog(
            onDismissRequest = { showDeleteDialog = null },
            shape = RoundedCornerShape(24.dp),
            containerColor = CardWhite,
            title = {
                Text(
                    "Xóa Tiện Ích",
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
            },
            text = {
                Text(
                    "Bạn có chắc muốn xóa tiện ích '${util.name}' không?",
                    color = TextSecond,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
            },
            confirmButton = {
                Button(
                    onClick = { viewModel.deleteUtility(util.id) },
                    colors = ButtonDefaults.buttonColors(containerColor = AccentRed),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Text("Xóa", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                OutlinedButton(
                    onClick = { showDeleteDialog = null },
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Text("Hủy", color = TextSecond)
                }
            }
        )
    }
}

@Composable
fun UtilityItemCard(utility: Utility, onEdit: () -> Unit, onDelete: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = CardWhite),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(AccentGreen.copy(alpha = 0.1f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.Star, contentDescription = null, tint = AccentGreen)
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = utility.name,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = TextPrimary
                )
                Spacer(modifier = Modifier.height(4.dp))
                val priceText = if (utility.price != null && utility.price > 0) "${utility.price.toInt()}đ" else "Miễn phí"
                Text(
                    text = priceText,
                    fontSize = 14.sp,
                    color = if (utility.price != null && utility.price > 0) AccentRed else AccentGreen,
                    fontWeight = FontWeight.SemiBold
                )
            }
            IconButton(onClick = onEdit) {
                Icon(Icons.Default.Edit, contentDescription = "Edit", tint = AccentGreen)
            }
            IconButton(onClick = onDelete) {
                Icon(Icons.Default.DeleteForever, contentDescription = "Delete", tint = AccentRed)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UtilityFormDialog(
    initialUtility: Utility?,
    onDismiss: () -> Unit,
    onConfirm: (name: String, price: Double?, type: String) -> Unit
) {
    var name by remember { mutableStateOf(initialUtility?.name ?: "") }
    var priceStr by remember { mutableStateOf(initialUtility?.price?.toInt()?.toString() ?: "") }

    AlertDialog(
        onDismissRequest = onDismiss,
        shape = RoundedCornerShape(24.dp),
        containerColor = CardWhite,
        title = {
            Text(
                if (initialUtility == null) "Thêm Tiện Ích" else "Chỉnh Sửa Tiện Ích",
                fontWeight = FontWeight.Bold,
                color = TextPrimary,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Tên tiện ích") },
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = AccentGreen,
                        focusedLabelColor = AccentGreen
                    ),
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                OutlinedTextField(
                    value = priceStr,
                    onValueChange = { priceStr = it },
                    label = { Text("Giá (để trống nếu miễn phí)") },
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = AccentGreen,
                        focusedLabelColor = AccentGreen
                    ),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val price = priceStr.toDoubleOrNull()
                    onConfirm(name, price, "other") 
                },
                enabled = name.isNotBlank(),
                colors = ButtonDefaults.buttonColors(containerColor = AccentGreen),
                shape = RoundedCornerShape(14.dp)
            ) {
                Text("Lưu", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            OutlinedButton(
                onClick = onDismiss,
                shape = RoundedCornerShape(14.dp)
            ) {
                Text("Hủy", color = TextSecond)
            }
        }
    )
}
