package com.tanh.datsan.ui.admin.menu

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ListAlt
import androidx.compose.material.icons.rounded.Category
import androidx.compose.material.icons.rounded.ChatBubble
import androidx.compose.material.icons.rounded.Schedule
import androidx.compose.material.icons.rounded.SportsSoccer
import androidx.compose.material.icons.rounded.Star
import androidx.compose.material.icons.rounded.Feedback
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.tanh.datsan.viewmodel.ProfileViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminMenuScreen(
    onNavigateToFieldTypes: () -> Unit,
    onNavigateToUtilities: () -> Unit,
    onNavigateToBookings: () -> Unit,
    onNavigateToTimeSlots: () -> Unit,
    onNavigateToReviews: () -> Unit = {},
    onNavigateToFeedbacks: () -> Unit = {},
    viewModel: ProfileViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Menu Quản Trị", color = Color.White, fontWeight = FontWeight.Bold) },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFF0F172A))
            )
        },
        containerColor = Color(0xFFF1F5F9)
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(20.dp)
            ) {
                val userRole = uiState.profile?.role?.name
                if (userRole == "super_admin" || userRole == "branch_manager") {
                    Text(
                        text = "Quản trị danh mục",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF0F172A),
                        modifier = Modifier.padding(bottom = 16.dp)
                    )

                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(20.dp),
                        color = Color.White,
                        shadowElevation = 2.dp
                    ) {
                        Column {
                            MenuItem(
                                icon = Icons.AutoMirrored.Rounded.ListAlt,
                                label = "Quản lý Lịch Đặt Sân",
                                color = Color(0xFFF59E0B),
                                onClick = onNavigateToBookings
                            )
                            MenuDivider()
                            MenuItem(
                                icon = Icons.Rounded.SportsSoccer,
                                label = "Quản lý Loại sân",
                                color = Color(0xFF3B82F6),
                                onClick = onNavigateToFieldTypes
                            )
                            MenuDivider()
                            MenuItem(
                                icon = Icons.Rounded.Star,
                                label = "Quản lý Tiện ích",
                                color = Color(0xFF10B981),
                                onClick = onNavigateToUtilities
                            )
                            MenuDivider()
                            MenuItem(
                                icon = Icons.Rounded.Schedule,
                                label = "Quản lý Khung Giờ & Giá",
                                color = Color(0xFF8B5CF6),
                                onClick = onNavigateToTimeSlots
                            )
                            MenuDivider()
                            MenuItem(
                                icon = Icons.Rounded.ChatBubble,
                                label = "Quản lý Đánh Giá",
                                color = Color(0xFFEC4899),
                                onClick = onNavigateToReviews
                            )
                            MenuDivider()
                            MenuItem(
                                icon = Icons.Rounded.Feedback,
                                label = "Quản lý Feedback",
                                color = Color(0xFFEF4444),
                                onClick = onNavigateToFeedbacks
                            )
                        }
                    }
                } else {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text(text = "Bạn không có quyền truy cập menu này.", color = Color.Gray)
                    }
                }
            }
        }
    }
}

@Composable
fun MenuItem(
    icon: ImageVector,
    label: String,
    color: Color,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        color = Color.Transparent,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(color.copy(alpha = 0.15f), RoundedCornerShape(12.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, contentDescription = null, tint = color, modifier = Modifier.size(22.dp))
            }
            Spacer(modifier = Modifier.width(16.dp))
            Text(
                text = label,
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium,
                color = Color(0xFF1E293B)
            )
        }
    }
}

@Composable
fun MenuDivider() {
    HorizontalDivider(
        modifier = Modifier.padding(start = 76.dp),
        thickness = 1.dp,
        color = Color(0xFFF1F5F9)
    )
}
