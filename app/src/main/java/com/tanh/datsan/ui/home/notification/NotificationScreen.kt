package com.tanh.datsan.ui.home.notification

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.DeleteSweep
import androidx.compose.material.icons.rounded.DoneAll
import androidx.compose.material.icons.rounded.Notifications
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.tanh.datsan.R
import com.tanh.datsan.data.model.NotificationModel
import com.tanh.datsan.ui.component.CustomRefreshLayout
import com.tanh.datsan.utils.DateUtil.formatNotificationTime

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificationScreen(
    notifications: List<NotificationModel>,
    isLoading: Boolean,
    onBackClick: () -> Unit,
    onMarkAsRead: (String) -> Unit,
    onMarkAllAsRead: () -> Unit,
    onDeleteNotification: (String) -> Unit,
    onClearAllNotifications: () -> Unit,
    onRefresh: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.notification), fontWeight = FontWeight.Bold, fontSize = 18.sp) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(imageVector = Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = stringResource(R.string.back))
                    }
                },
                actions = {
                    if (notifications.any { !it.isRead }) {
                        IconButton(onClick = onMarkAllAsRead) {
                            Icon(Icons.Rounded.DoneAll, contentDescription = stringResource(R.string.read_all))
                        }
                    }
                    if (notifications.isNotEmpty()) {
                        IconButton(onClick = onClearAllNotifications) {
                            Icon(Icons.Rounded.DeleteSweep, contentDescription = stringResource(R.string.delete_all_notification), tint = Color.Red)
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White, titleContentColor = Color.Black, navigationIconContentColor = Color.Black)
            )
        },
        containerColor = Color(0xFFF5F7FA)
    ) { paddingValues ->
        Box(modifier = Modifier.fillMaxSize().padding(paddingValues)) {
            CustomRefreshLayout(onRefresh = onRefresh) {
                if (notifications.isEmpty() && !isLoading) {
                    EmptyNotificationState()
                } else {
                    LazyColumn(modifier = Modifier.fillMaxSize(), contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        items(notifications) { noti ->
                            NotificationItem(
                                notification = noti,
                                onClick = { if (!noti.isRead) onMarkAsRead(noti.id) },
                                onDelete = { onDeleteNotification(noti.id) }
                            )
                        }
                    }
                }
            }
            if (isLoading && notifications.isEmpty()) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            }
        }
    }
}

@Composable
fun NotificationItem(notification: NotificationModel, onClick: () -> Unit, onDelete: () -> Unit) {
    Surface(
        modifier = Modifier.fillMaxWidth().clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        color = if (notification.isRead) Color.White else Color(0xFFE0F2FE),
        shadowElevation = 2.dp
    ) {
        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(modifier = Modifier.size(48.dp).clip(CircleShape).background(if (notification.isRead) Color(0xFFF1F5F9) else Color(0xFF3B82F6).copy(alpha = 0.1f)), contentAlignment = Alignment.Center) {
                Icon(Icons.Rounded.Notifications, contentDescription = null, tint = if (notification.isRead) Color(0xFF64748B) else Color(0xFF3B82F6), modifier = Modifier.size(24.dp))
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(text = notification.title, fontWeight = if (notification.isRead) FontWeight.SemiBold else FontWeight.Bold, fontSize = 16.sp, color = Color(0xFF1E293B))
                Spacer(modifier = Modifier.height(4.dp))
                Text(text = notification.content, fontSize = 14.sp, color = Color(0xFF64748B), maxLines = 2, overflow = TextOverflow.Ellipsis)
                Spacer(modifier = Modifier.height(8.dp))
                Text(text = formatNotificationTime(notification.createdAt), fontSize = 12.sp, color = Color(0xFF94A3B8))
            }
            IconButton(onClick = onDelete) {
                Icon(Icons.Rounded.Delete, contentDescription = null, tint = Color(0xFFCBD5E1), modifier = Modifier.size(20.dp))
            }
        }
    }
}

@Composable
fun EmptyNotificationState() {
    Column(modifier = Modifier.fillMaxSize(), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
        Icon(Icons.Rounded.Notifications, contentDescription = null, modifier = Modifier.size(80.dp), tint = Color(0xFFCBD5E1))
        Spacer(modifier = Modifier.height(16.dp))
        Text(text = stringResource(R.string.no_notification), color = Color(0xFF94A3B8), fontWeight = FontWeight.Medium)
    }
}
