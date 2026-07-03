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
import androidx.compose.ui.res.stringResource
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.tanh.datsan.ui.profile.ProfileViewModel
import com.tanh.datsan.R

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
                title = { Text(stringResource(id = R.string.menu_title), color = Color.White, fontWeight = FontWeight.Bold) },
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
                        text = stringResource(id = R.string.menu_category_management),
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
                                label = stringResource(id = R.string.menu_bookings),
                                color = Color(0xFFF59E0B),
                                onClick = onNavigateToBookings
                            )
                            MenuDivider()
                            MenuItem(
                                icon = Icons.Rounded.SportsSoccer,
                                label = stringResource(id = R.string.menu_field_types),
                                color = Color(0xFF3B82F6),
                                onClick = onNavigateToFieldTypes
                            )
                            MenuDivider()
                            MenuItem(
                                icon = Icons.Rounded.Star,
                                label = stringResource(id = R.string.menu_utilities),
                                color = Color(0xFF10B981),
                                onClick = onNavigateToUtilities
                            )

                            MenuDivider()
                            MenuItem(
                                icon = Icons.Rounded.ChatBubble,
                                label = stringResource(id = R.string.menu_reviews),
                                color = Color(0xFFEC4899),
                                onClick = onNavigateToReviews
                            )
                            MenuDivider()
                            MenuItem(
                                icon = Icons.Rounded.Feedback,
                                label = stringResource(id = R.string.menu_feedbacks),
                                color = Color(0xFFEF4444),
                                onClick = onNavigateToFeedbacks
                            )
                        }
                    }
                } else {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text(text = stringResource(id = R.string.menu_no_access), color = Color.Gray)
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
