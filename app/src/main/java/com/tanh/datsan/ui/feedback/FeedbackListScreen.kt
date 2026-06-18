package com.tanh.datsan.ui.feedback

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.tanh.datsan.data.model.FeedbackResponse
import com.tanh.datsan.viewmodel.FeedbackListViewModel
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.TimeZone

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FeedbackListScreen(
    onBackClick: () -> Unit,
    onNavigateToChat: (String) -> Unit,
    viewModel: FeedbackListViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    var showCreateDialog by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        viewModel.fetchMyFeedbacks()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Hỗ trợ khách hàng", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Quay lại")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.White
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showCreateDialog = true },
                containerColor = Color(0xFF007BFF),
                contentColor = Color.White
            ) {
                Icon(Icons.Default.Add, contentDescription = "Tạo yêu cầu mới")
            }
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            if (uiState.isLoading && uiState.feedbacks.isEmpty()) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            } else if (uiState.error != null && uiState.feedbacks.isEmpty()) {
                Text(
                    text = uiState.error ?: "",
                    modifier = Modifier.align(Alignment.Center),
                    color = Color.Red
                )
            } else if (uiState.feedbacks.isEmpty()) {
                Text(
                    text = "Chưa có đoạn chat nào. Hãy tạo một yêu cầu mới!",
                    modifier = Modifier.align(Alignment.Center),
                    color = Color.Gray
                )
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(uiState.feedbacks.sortedByDescending { it.createdAt }) { feedback ->
                        FeedbackItem(
                            feedback = feedback,
                            onClick = { onNavigateToChat(feedback.id) }
                        )
                    }
                }
            }
        }

        if (showCreateDialog) {
            CreateFeedbackDialog(
                onDismiss = { showCreateDialog = false },
                onCreate = { title, description ->
                    showCreateDialog = false
                    viewModel.createFeedback(title, description) { newId ->
                        onNavigateToChat(newId)
                    }
                }
            )
        }
    }
}

@Composable
fun FeedbackItem(feedback: FeedbackResponse, onClick: () -> Unit) {
    // Format thời gian
    val timeString = remember(feedback.createdAt) {
        try {
            val inputFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault())
            inputFormat.timeZone = TimeZone.getTimeZone("UTC")
            val date = inputFormat.parse(feedback.createdAt)
            val outputFormat = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
            outputFormat.format(date ?: java.util.Date())
        } catch (e: Exception) {
            feedback.createdAt
        }
    }

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        color = Color.White,
        shadowElevation = 2.dp
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Text(
                    text = feedback.title,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = Color(0xFF1976D2),
                    modifier = Modifier.weight(1f)
                )
                Text(
                    text = timeString,
                    fontSize = 12.sp,
                    color = Color.Gray,
                    modifier = Modifier.padding(start = 8.dp)
                )
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = feedback.description,
                fontSize = 14.sp,
                color = Color.DarkGray,
                maxLines = 2,
                overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
            )
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                val statusColor = when(feedback.status.uppercase()) {
                    "PENDING" -> Color(0xFFF57C00)
                    "RESOLVED" -> Color(0xFF388E3C)
                    "CLOSED" -> Color(0xFFD32F2F)
                    else -> Color.Gray
                }
                Text(
                    text = feedback.status,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = statusColor
                )
            }
        }
    }
}

@Composable
fun CreateFeedbackDialog(
    onDismiss: () -> Unit,
    onCreate: (String, String) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(text = "Tạo yêu cầu hỗ trợ mới", fontWeight = FontWeight.Bold) },
        text = {
            Column {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Tiêu đề") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Nội dung") },
                    modifier = Modifier.fillMaxWidth().height(100.dp),
                    maxLines = 3
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { onCreate(title, description) },
                enabled = title.isNotBlank() && description.isNotBlank(),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF007BFF))
            ) {
                Text("Bắt đầu chat")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Hủy")
            }
        }
    )
}
