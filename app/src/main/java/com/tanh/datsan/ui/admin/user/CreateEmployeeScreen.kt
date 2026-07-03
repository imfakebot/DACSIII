package com.tanh.datsan.ui.admin.user

import android.annotation.SuppressLint
import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.res.stringResource
import com.tanh.datsan.data.model.Branch
import com.tanh.datsan.data.model.CreateEmployeeDto
import com.tanh.datsan.ui.state.ActionState
import com.tanh.datsan.R

@SuppressLint("LocalContextGetResourceValueCall")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateEmployeeScreen(
    actionState: ActionState,
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
        Pair("staff", stringResource(id = R.string.create_employee_role_staff)),
        Pair("branch_manager", stringResource(id = R.string.create_employee_role_manager))
    )

    LaunchedEffect(actionState) {
        when (actionState) {
            is ActionState.Success -> {
                actionState.message?.let {
                    Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
                }
                onResetUiState()
                onBackClick()
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
                title = { Text(stringResource(id = R.string.create_employee_title)) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
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
                label = { Text(stringResource(id = R.string.create_employee_email)) },
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                label = { Text(stringResource(id = R.string.create_employee_password)) },
                modifier = Modifier.fillMaxWidth()
            )

            ExposedDropdownMenuBox(
                expanded = expandedRole,
                onExpandedChange = { expandedRole = !expandedRole }
            ) {
                OutlinedTextField(
                    readOnly = true,
                    value = roles.find { it.first == selectedRole }?.second ?: stringResource(id = R.string.create_employee_role_hint),
                    onValueChange = { },
                    label = { Text(stringResource(id = R.string.create_employee_role_label)) },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedRole) },
                    colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors(),
                    modifier = Modifier.menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable, true).fillMaxWidth()
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
                    value = branches.find { it.id == selectedBranchId }?.name ?: stringResource(id = R.string.create_employee_branch_hint),
                    onValueChange = { },
                    label = { Text(stringResource(id = R.string.create_employee_branch_label)) },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedBranch) },
                    colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors(),
                    modifier = Modifier.menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable, true).fillMaxWidth()
                )
                ExposedDropdownMenu(
                    expanded = expandedBranch,
                    onDismissRequest = { expandedBranch = false }
                ) {
                    DropdownMenuItem(
                        text = { Text(text = stringResource(id = R.string.create_employee_branch_none)) },
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
                        Toast.makeText(context, context.getString(R.string.create_employee_val_required), Toast.LENGTH_SHORT).show()
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
                enabled = actionState !is ActionState.Loading
            ) {
                if (actionState is ActionState.Loading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                } else {
                    Text(stringResource(id = R.string.user_create_employee))
                }
            }
        }
    }
}
