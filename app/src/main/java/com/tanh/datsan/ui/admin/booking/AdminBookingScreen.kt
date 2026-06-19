package com.tanh.datsan.ui.admin.booking

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.rounded.CalendarToday
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material.icons.rounded.Phone
import androidx.compose.material.icons.rounded.SportsSoccer
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.tanh.datsan.data.model.BookingResponse
import com.tanh.datsan.viewmodel.AdminBookingUiState
import com.tanh.datsan.viewmodel.AdminBookingViewModel

// Colors matching Premium UI
private val AppBg = Color(0xFFF8FAFC)
private val CardWhite = Color.White
private val TextPrimary = Color(0xFF1E293B)
private val TextSecond = Color(0xFF64748B)
private val AccentBlue = Color(0xFF3B82F6)
private val DividerColor = Color(0xFFE2E8F0)

// Status colors
private val StatusPendingColor = Color(0xFFF59E0B)
private val StatusApprovedColor = Color(0xFF10B981)
private val StatusRejectedColor = Color(0xFFEF4444)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminBookingScreen(
    onBackClick: () -> Unit,
    onNavigateToCreateBooking: () -> Unit,
    viewModel: AdminBookingViewModel = hiltViewModel()
) {
    val bookings by viewModel.bookings.collectAsState()
    val uiState by viewModel.uiState.collectAsState()
    val isLoadingMore by viewModel.isLoadingMore.collectAsState()
    val listState = androidx.compose.foundation.lazy.rememberLazyListState()

    LaunchedEffect(Unit) {
        viewModel.fetchAdminBookings(refresh = true)
    }

    LaunchedEffect(listState) {
        snapshotFlow { listState.layoutInfo.visibleItemsInfo.lastOrNull()?.index }
            .collect { lastIndex ->
                if (lastIndex != null && lastIndex >= bookings.size - 1 && !isLoadingMore && !viewModel.isLastPage) {
                    viewModel.loadMoreBookings()
                }
            }
    }

    Scaffold(
        containerColor = AppBg,
        topBar = {
            TopAppBar(
                title = { Text("Đơn đặt sân", fontWeight = FontWeight.ExtraBold) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Trở về")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = AppBg,
                    titleContentColor = TextPrimary,
                    navigationIconContentColor = TextPrimary
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onNavigateToCreateBooking,
                shape = RoundedCornerShape(18.dp),
                containerColor = AccentBlue,
                contentColor = Color.White,
                elevation = FloatingActionButtonDefaults.elevation(defaultElevation = 8.dp)
            ) {
                Icon(Icons.Default.Add, contentDescription = "Tạo đơn", modifier = Modifier.size(26.dp))
            }
        }
    ) { innerPadding ->
        Column(modifier = Modifier.padding(innerPadding).fillMaxSize()) {
            if (uiState is AdminBookingUiState.Loading && bookings.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = AccentBlue)
                }
            } else if (bookings.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("Chưa có đơn đặt sân nào", color = TextSecond)
                }
            } else {
                LazyColumn(
                    state = listState,
                    contentPadding = PaddingValues(bottom = 100.dp)
                ) {
                    items(bookings, key = { it.id ?: it.code ?: it.hashCode().toString() }) { booking ->
                        BookingCard(booking = booking)
                    }

                    if (isLoadingMore) {
                        item {
                            Box(modifier = Modifier.fillMaxWidth().padding(16.dp), contentAlignment = Alignment.Center) {
                                CircularProgressIndicator(color = AccentBlue)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun BookingCard(booking: BookingResponse) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 8.dp)
            .shadow(12.dp, RoundedCornerShape(20.dp), spotColor = Color(0x1A000000)),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = CardWhite)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Mã: ${booking.code ?: "N/A"}",
                    fontWeight = FontWeight.Bold,
                    color = AccentBlue,
                    fontSize = 14.sp
                )
                BookingStatusBadge(status = booking.status)
            }
            Spacer(modifier = Modifier.height(12.dp))
            
            val customerName = booking.customerName ?: booking.userProfile?.fullName ?: "Khách vãng lai"
            val customerPhone = booking.customerPhone ?: "Không có SDT"
            
            BookingInfoRow(icon = Icons.Rounded.Person, text = customerName)
            Spacer(modifier = Modifier.height(4.dp))
            BookingInfoRow(icon = Icons.Rounded.Phone, text = customerPhone)
            Spacer(modifier = Modifier.height(4.dp))
            BookingInfoRow(icon = Icons.Rounded.SportsSoccer, text = booking.field?.name ?: "Sân đã xóa")
            Spacer(modifier = Modifier.height(4.dp))
            BookingInfoRow(
                icon = Icons.Rounded.CalendarToday, 
                text = "${booking.bookingDate ?: ""} | ${booking.startTime ?: ""} - ${booking.endTime ?: ""}"
            )

            Spacer(modifier = Modifier.height(12.dp))
            HorizontalDivider(color = DividerColor)
            Spacer(modifier = Modifier.height(12.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                Text(
                    text = "${booking.totalPrice ?: 0} VND",
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 18.sp,
                    color = TextPrimary
                )
            }
        }
    }
}

@Composable
fun BookingInfoRow(icon: androidx.compose.ui.graphics.vector.ImageVector, text: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(icon, contentDescription = null, tint = TextSecond, modifier = Modifier.size(16.dp))
        Spacer(modifier = Modifier.width(8.dp))
        Text(text, color = TextPrimary, fontSize = 14.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
    }
}

@Composable
fun BookingStatusBadge(status: String?) {
    val (bgColor, textColor, text) = when (status?.lowercase()) {
        "pending" -> Triple(StatusPendingColor.copy(alpha = 0.1f), StatusPendingColor, "Chờ duyệt")
        "approved" -> Triple(StatusApprovedColor.copy(alpha = 0.1f), StatusApprovedColor, "Đã duyệt")
        "rejected" -> Triple(StatusRejectedColor.copy(alpha = 0.1f), StatusRejectedColor, "Từ chối")
        "completed" -> Triple(AccentBlue.copy(alpha = 0.1f), AccentBlue, "Hoàn thành")
        else -> Triple(DividerColor, TextSecond, status ?: "Unknown")
    }

    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .background(bgColor)
            .padding(horizontal = 8.dp, vertical = 4.dp)
    ) {
        Text(text, color = textColor, fontSize = 12.sp, fontWeight = FontWeight.Bold)
    }
}
