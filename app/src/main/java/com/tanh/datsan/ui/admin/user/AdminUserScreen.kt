package com.tanh.datsan.ui.admin.user

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.res.stringResource
import com.tanh.datsan.data.model.AccountResponseDto
import com.tanh.datsan.ui.state.ActionState
import com.tanh.datsan.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminUserScreen(
    users: List<AccountResponseDto>,
    actionState: ActionState,
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit,
    onSearch: () -> Unit,
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

    LaunchedEffect(actionState) {
        when (actionState) {
            is ActionState.Success -> {
                actionState.message?.let {
                    Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
                }
                onResetUiState()
            }
            is ActionState.Error -> {
                Toast.makeText(context, actionState.message, Toast.LENGTH_SHORT).show()
                onResetUiState()
            }
            else -> {}
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(id = R.string.user_management)) },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = onNavigateToCreateEmployee) {
                Icon(Icons.Filled.Add, contentDescription = stringResource(id = R.string.user_create_employee))
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = onSearchQueryChange,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    placeholder = { Text(stringResource(id = R.string.user_search_hint)) },
                    singleLine = true,
                    trailingIcon = {
                        IconButton(onClick = onSearch) {
                            Icon(Icons.Filled.Search, contentDescription = stringResource(id = R.string.user_search_btn))
                        }
                    }
                )

                Box(modifier = Modifier.fillMaxSize().weight(1f)) {
                    if (actionState is ActionState.Loading) {
                        CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                    } else if (users.isEmpty()) {
                        Text(
                            text = stringResource(id = R.string.user_empty),
                            modifier = Modifier.align(Alignment.Center)
                        )
                    } else {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
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
        }
    }

    showConfirmDialog?.let { (userId, isBan) ->
        AlertDialog(
            onDismissRequest = { showConfirmDialog = null },
            title = { Text(if (isBan) stringResource(id = R.string.user_ban_title) else stringResource(id = R.string.user_unban_title)) },
            text = { Text(if (isBan) stringResource(id = R.string.user_ban_confirm) else stringResource(id = R.string.user_unban_confirm)) },
            confirmButton = {
                TextButton(
                    onClick = {
                        onToggleActive(userId, isBan)
                        showConfirmDialog = null
                    }
                ) {
                    Text(if (isBan) stringResource(id = R.string.user_ban_btn) else stringResource(id = R.string.user_unban_btn), color = if (isBan) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary)
                }
            },
            dismissButton = {
                TextButton(onClick = { showConfirmDialog = null }) {
                    Text(stringResource(id = R.string.user_cancel))
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
                    text = stringResource(id = R.string.user_email_label, user.email),
                    style = MaterialTheme.typography.bodyMedium
                )
                val roleName = user.role?.name ?: user.role?.roleName ?: user.role?.code ?: "N/A"
                Text(
                    text = stringResource(id = R.string.user_role_label, roleName),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.secondary
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = stringResource(id = R.string.user_status_label, if (isActive) stringResource(id = R.string.user_status_active) else stringResource(id = R.string.user_status_banned)),
                    style = MaterialTheme.typography.bodySmall,
                    color = if (isActive) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error
                )
            }
            IconButton(onClick = { onToggleBanStatus(isActive) }) { // If active, click to ban
                Icon(
                    imageVector = if (isActive) Icons.Filled.Lock else Icons.Filled.LockOpen,
                    contentDescription = if (isActive) stringResource(id = R.string.user_ban_icon_desc) else stringResource(id = R.string.user_unban_icon_desc),
                    tint = if (isActive) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}
