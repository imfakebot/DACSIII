package com.tanh.datsan.ui.admin.user

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.tanh.datsan.data.model.AccountResponseDto
import com.tanh.datsan.viewmodel.AdminUserUiState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminUserScreen(
    users: List<AccountResponseDto>,
    uiState: AdminUserUiState,
    onFetchUsers: () -> Unit,
    onNavigateToCreateEmployee: () -> Unit,
    onToggleActive: (String, Boolean) -> Unit,
    onResetUiState: () -> Unit
) {
    val context = LocalContext.current
    var showConfirmDialog by remember { mutableStateOf<Pair<String, Boolean>?>(null) } // Pair(userId, isBan)

    LaunchedEffect(Unit) {
        onFetchUsers()
    }

    LaunchedEffect(uiState) {
        when (uiState) {
            is AdminUserUiState.Success -> {
                uiState.message?.let {
                    Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
                }
                onResetUiState()
            }
            is AdminUserUiState.Error -> {
                Toast.makeText(context, uiState.message, Toast.LENGTH_SHORT).show()
                onResetUiState()
            }
            else -> {}
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Quản lý người dùng") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = onNavigateToCreateEmployee) {
                Icon(Icons.Filled.Add, contentDescription = "Tạo nhân viên")
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            if (uiState is AdminUserUiState.Loading) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            } else if (users.isEmpty()) {
                Text(
                    text = "Không có người dùng nào.",
                    modifier = Modifier.align(Alignment.Center)
                )
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(users) { user ->
                        UserItem(
                            user = user,
                            onToggleBanStatus = { isBan ->
                                showConfirmDialog = Pair(user.id, isBan)
                            }
                        )
                    }
                }
            }
        }
    }

    showConfirmDialog?.let { (userId, isBan) ->
        AlertDialog(
            onDismissRequest = { showConfirmDialog = null },
            title = { Text(if (isBan) "Khóa tài khoản" else "Mở khóa tài khoản") },
            text = { Text(if (isBan) "Bạn có chắc chắn muốn khóa tài khoản này không?" else "Bạn có chắc chắn muốn mở khóa tài khoản này không?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        onToggleActive(userId, isBan)
                        showConfirmDialog = null
                    }
                ) {
                    Text(if (isBan) "Khóa" else "Mở khóa", color = if (isBan) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary)
                }
            },
            dismissButton = {
                TextButton(onClick = { showConfirmDialog = null }) {
                    Text("Hủy")
                }
            }
        )
    }
}

@Composable
fun UserItem(
    user: AccountResponseDto,
    onToggleBanStatus: (Boolean) -> Unit
) {
    val isActive = user.isActive ?: true

    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = user.userProfile?.fullName ?: "N/A",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Email: ${user.email}",
                    style = MaterialTheme.typography.bodyMedium
                )
                val roleName = user.role?.name ?: user.role?.roleName ?: user.role?.code ?: "N/A"
                Text(
                    text = "Role: $roleName",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.secondary
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Trạng thái: ${if (isActive) "Hoạt động" else "Bị khóa"}",
                    style = MaterialTheme.typography.bodySmall,
                    color = if (isActive) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error
                )
            }
            IconButton(onClick = { onToggleBanStatus(isActive) }) { // If active, click to ban
                Icon(
                    imageVector = if (isActive) Icons.Filled.Lock else Icons.Filled.LockOpen,
                    contentDescription = if (isActive) "Khóa User" else "Mở khóa User",
                    tint = if (isActive) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}
