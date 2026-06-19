package com.tanh.datsan.ui.admin.user

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.tanh.datsan.data.model.Branch
import com.tanh.datsan.data.model.CreateEmployeeDto
import com.tanh.datsan.viewmodel.AdminUserUiState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateEmployeeScreen(
    uiState: AdminUserUiState,
    branches: List<Branch>,
    onCreateEmployee: (CreateEmployeeDto) -> Unit,
    onBackClick: () -> Unit,
    onResetUiState: () -> Unit
) {
    val context = LocalContext.current

    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var selectedRole by remember { mutableStateOf("") }
    var selectedBranchId by remember { mutableStateOf("") }

    var expandedRole by remember { mutableStateOf(false) }
    var expandedBranch by remember { mutableStateOf(false) }

    val roles = listOf(
        Pair("staff", "Nhân viên"),
        Pair("branch_manager", "Quản lý chi nhánh")
    )

    LaunchedEffect(uiState) {
        when (uiState) {
            is AdminUserUiState.Success -> {
                uiState.message?.let {
                    Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
                }
                onResetUiState()
                onBackClick()
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
                title = { Text("Tạo tài khoản nhân viên") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            OutlinedTextField(
                value = email,
                onValueChange = { email = it },
                label = { Text("Email *") },
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                label = { Text("Mật khẩu") },
                modifier = Modifier.fillMaxWidth()
            )

            ExposedDropdownMenuBox(
                expanded = expandedRole,
                onExpandedChange = { expandedRole = !expandedRole }
            ) {
                OutlinedTextField(
                    readOnly = true,
                    value = roles.find { it.first == selectedRole }?.second ?: "Chọn phân quyền *",
                    onValueChange = { },
                    label = { Text("Phân quyền") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedRole) },
                    colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors(),
                    modifier = Modifier.menuAnchor().fillMaxWidth()
                )
                ExposedDropdownMenu(
                    expanded = expandedRole,
                    onDismissRequest = { expandedRole = false }
                ) {
                    roles.forEach { role ->
                        DropdownMenuItem(
                            text = { Text(text = role.second) },
                            onClick = {
                                selectedRole = role.first
                                expandedRole = false
                            }
                        )
                    }
                }
            }

            ExposedDropdownMenuBox(
                expanded = expandedBranch,
                onExpandedChange = { expandedBranch = !expandedBranch }
            ) {
                OutlinedTextField(
                    readOnly = true,
                    value = branches.find { it.id == selectedBranchId }?.name ?: "Chọn chi nhánh (Tùy chọn)",
                    onValueChange = { },
                    label = { Text("Chi nhánh") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedBranch) },
                    colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors(),
                    modifier = Modifier.menuAnchor().fillMaxWidth()
                )
                ExposedDropdownMenu(
                    expanded = expandedBranch,
                    onDismissRequest = { expandedBranch = false }
                ) {
                    DropdownMenuItem(
                        text = { Text(text = "Không chọn chi nhánh") },
                        onClick = {
                            selectedBranchId = ""
                            expandedBranch = false
                        }
                    )
                    branches.forEach { branch ->
                        DropdownMenuItem(
                            text = { Text(text = branch.name) },
                            onClick = {
                                selectedBranchId = branch.id
                                expandedBranch = false
                            }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = {
                    if (email.isBlank() || selectedRole.isBlank()) {
                        Toast.makeText(context, "Vui lòng nhập Email và Phân quyền", Toast.LENGTH_SHORT).show()
                        return@Button
                    }
                    onCreateEmployee(
                        CreateEmployeeDto(
                            email = email,
                            role = selectedRole,
                            password = password.ifBlank { null },
                            branchId = selectedBranchId.ifBlank { null }
                        )
                    )
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = uiState !is AdminUserUiState.Loading
            ) {
                if (uiState is AdminUserUiState.Loading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                } else {
                    Text("Tạo nhân viên")
                }
            }
        }
    }
}
