package com.tanh.datsan.ui.admin.feedback

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.res.stringResource
import androidx.hilt.navigation.compose.hiltViewModel
import com.tanh.datsan.ui.admin.feedback.AdminFeedbackViewModel
import com.tanh.datsan.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminFeedbackDetailScreen(
    feedbackId: String,
    onNavigateBack: () -> Unit,
    viewModel: AdminFeedbackViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    var replyText by remember { mutableStateOf("") }
    var showDeleteDialog by remember { mutableStateOf(false) }

    LaunchedEffect(feedbackId) {
        viewModel.fetchFeedbackDetail(feedbackId)
    }

    LaunchedEffect(uiState.toastMessage) {
        uiState.toastMessage?.let {
            Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
            viewModel.clearToast()
        }
    }

    // Removed delete dialog since backend doesn't support deletion

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(id = R.string.feedback_detail_title), fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    // Removed delete action
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.White
                )
            )
        },
        containerColor = Color(0xFFF1F5F9)
    ) { padding ->
        val feedback = uiState.currentFeedback

        if (uiState.isLoading && feedback == null) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else if (feedback != null) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp)
            ) {
                // Info Card
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = feedback.title ?: stringResource(id = R.string.feedback_no_title),
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = stringResource(id = R.string.feedback_category, feedback.type?.uppercase() ?: stringResource(id = R.string.feedback_type_other)),
                                fontSize = 14.sp,
                                color = Color.Gray
                            )
                            val statusText = when (feedback.status.lowercase()) {
                                "pending" -> stringResource(id = R.string.feedback_status_pending)
                                "processing" -> stringResource(id = R.string.feedback_status_processing)
                                "resolved" -> stringResource(id = R.string.feedback_status_resolved)
                                else -> feedback.status
                            }
                            Text(
                                text = stringResource(id = R.string.feedback_status, statusText),
                                fontSize = 14.sp,
                                color = Color.Gray,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Divider(modifier = Modifier.padding(vertical = 12.dp))
                        Text(
                            text = feedback.content ?: "",
                            fontSize = 16.sp,
                            lineHeight = 24.sp
                        )
                        
                        // Hình ảnh đính kèm (nếu có)
                        if (!feedback.images.isNullOrEmpty()) {
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(text = stringResource(id = R.string.feedback_attached_images), fontWeight = FontWeight.Bold)
                            Spacer(modifier = Modifier.height(8.dp))
                            // TODO: Add Image loading row/grid here
                            Text(text = stringResource(id = R.string.feedback_images_count, feedback.images.size), color = Color.Blue)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Action Card for status update has been removed because it is managed automatically by the backend.

                // Reply Card
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(stringResource(id = R.string.feedback_reply_title), fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        if (!feedback.adminReply.isNullOrEmpty()) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(Color(0xFFE0F2FE))
                                    .padding(12.dp)
                            ) {
                                Text(
                                    text = feedback.adminReply,
                                    color = Color(0xFF0369A1),
                                    fontSize = 15.sp
                                )
                            }
                            Spacer(modifier = Modifier.height(12.dp))
                        }

                        OutlinedTextField(
                            value = replyText,
                            onValueChange = { replyText = it },
                            placeholder = { Text(stringResource(id = R.string.feedback_reply_hint)) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(100.dp),
                            maxLines = 4
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Button(
                            onClick = { 
                                viewModel.replyFeedback(feedbackId, replyText)
                                replyText = ""
                            },
                            modifier = Modifier.align(Alignment.End),
                            enabled = replyText.isNotBlank() && !uiState.isLoading
                        ) {
                            Icon(Icons.Default.Send, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(stringResource(id = R.string.feedback_reply_btn))
                        }
                    }
                }
            }
        }
    }
}
