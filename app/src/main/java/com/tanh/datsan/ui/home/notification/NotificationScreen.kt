package com.tanh.datsan.ui.home.notification

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.DeleteSweep
import androidx.compose.material.icons.rounded.DoneAll
import androidx.compose.material.icons.rounded.Notifications
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.tanh.datsan.data.model.NotificationModel
import com.tanh.datsan.viewmodel.NotificationViewModel
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.rounded.Delete
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.tanh.datsan.R
import com.tanh.datsan.ui.component.CustomRefreshLayout
import com.tanh.datsan.utils.DateUtil.formatNotificationTime


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificationScreen(
    viewModel: NotificationViewModel = hiltViewModel(),
    onBackClick: () -> Unit = {}
) {
    val notification by viewModel.notifications.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        stringResource(R.string.notification),
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                            contentDescription = stringResource(R.string.back)
                        )
                    }
                },
                actions = {
                    if (notification.any { !it.isRead }) {
                        IconButton(
                            onClick = {
                                viewModel.markAllAsRead()
                            }
                        ) {
                            Icon(
                                Icons.Rounded.DoneAll,
                                contentDescription = stringResource(R.string.read_all)
                            )
                        }
                    }
                    if (notification.isNotEmpty()) {
                        IconButton(
                            onClick = {
                                viewModel.clearAllNotifications()
                            }
                        ) {
                            Icon(
                                Icons.Rounded.DeleteSweep,
                                contentDescription = stringResource(R.string.delete_all_notification),
                                tint = Color.Red
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.White,
                    titleContentColor = Color.Black,
                    navigationIconContentColor = Color.Black
                )
            )
        },
        containerColor = Color(0xFFF5F7FA)
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            CustomRefreshLayout(
                onRefresh = {
                    viewModel.fetchNotification()
                },
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            ) {
                Box(
                    modifier = Modifier.fillMaxSize()
                ) {
                    when {
                        isLoading && notification.isEmpty() -> {
                            CircularProgressIndicator(
                                modifier = Modifier.align(Alignment.Center),
                                color = Color(0xFF007BFF)
                            )
                        }

                        !isLoading && notification.isEmpty() -> {
                            Text(
                                text = stringResource(R.string.no_notification),
                                color = Color.Gray,
                                modifier = Modifier.align(Alignment.Center)
                            )
                        }

                        else -> {
                            LazyColumn(
                                modifier = Modifier.fillMaxSize(),
                                contentPadding = PaddingValues(16.dp),
                                verticalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                items(notification) { noti ->
                                    NotificationItem(
                                        notification = noti,
                                        onReadClick = {
                                            if (!noti.isRead) viewModel.markAsRead(noti.id)
                                        },
                                        onDeleteClick = {
                                            viewModel.deleteNotification(noti.id)
                                        }
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun NotificationItem(
    notification: NotificationModel,
    onReadClick: () -> Unit,
    onDeleteClick: () -> Unit
) {
    val backgroundColor = if (!notification.isRead) Color(0xFFE3F2FD) else Color.White

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onReadClick() },
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = backgroundColor),
        elevation = CardDefaults.cardElevation(defaultElevation = if (!notification.isRead) 2.dp else 0.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(if (!notification.isRead) Color(0xFF007BFF) else Color(0xFFE0E0E0)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Rounded.Notifications,
                    contentDescription = null,
                    tint = if (!notification.isRead) Color.White else Color.Gray,
                    modifier = Modifier.size(24.dp)
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = notification.title,
                    fontWeight = if (!notification.isRead) FontWeight.Bold else FontWeight.Medium,
                    fontSize = 16.sp,
                    color = if (!notification.isRead) Color.Black else Color.DarkGray
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = notification.content, // Sửa message thành content
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.Gray,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = formatNotificationTime(notification.createdAt),
                    style = MaterialTheme.typography.labelSmall,
                    color = Color(0xFF007BFF)
                )
            }

            IconButton(
                onClick = onDeleteClick,
                modifier = Modifier.size(24.dp)
            ) {
                Icon(
                    imageVector = Icons.Rounded.Delete,
                    contentDescription = "Xóa",
                    tint = Color.LightGray
                )
            }
        }
    }
}
