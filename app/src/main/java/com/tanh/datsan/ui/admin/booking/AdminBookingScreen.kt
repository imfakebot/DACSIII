package com.tanh.datsan.ui.admin.booking

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
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
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.tanh.datsan.R
import com.tanh.datsan.data.model.BookingResponse
import com.tanh.datsan.ui.admin.field.AdminUiState
import com.tanh.datsan.utils.DateUtil.formatDateDash

// Colors matching Premium UI
private val DarkBg      = Color(0xFF0F172A)
private val AccentBlue  = Color(0xFF3B82F6)
private val AccentGreen = Color(0xFF10B981)
private val AccentRed   = Color(0xFFEF4444)
private val AccentAmber = Color(0xFFF59E0B)
private val AppBg       = Color(0xFFF1F5F9)
private val CardWhite   = Color.White
private val TextPrimary = Color(0xFF0F172A)
private val TextSecond  = Color(0xFF64748B)
private val DividerColor= Color(0xFFF1F5F9)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminBookingScreen(
    onBackClick: () -> Unit,
    onNavigateToCreateBooking: () -> Unit,
    viewModel: AdminBookingViewModel = hiltViewModel()
) {
    val context = LocalContext.current
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

    LaunchedEffect(uiState) {
        when (uiState) {
            is AdminUiState.Success -> {
                Toast.makeText(context, (uiState as AdminUiState.Success).message ?: context.getString(R.string.success_default), Toast.LENGTH_SHORT).show()
                viewModel.resetUiState()
            }
            is AdminUiState.Error -> {
                Toast.makeText(context, (uiState as AdminUiState.Error).message, Toast.LENGTH_SHORT).show()
                viewModel.resetUiState()
            }
            else -> {}
        }
    }

    Scaffold(
        containerColor = AppBg,
        topBar = {
            TopAppBar(
                title = { Text(stringResource(id = R.string.admin_booking_title), fontWeight = FontWeight.ExtraBold) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(id = R.string.cd_back))
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
                Icon(Icons.Default.Add, contentDescription = stringResource(id = R.string.admin_booking_create), modifier = Modifier.size(26.dp))
            }
        }
    ) { innerPadding ->
        Column(modifier = Modifier.padding(innerPadding).fillMaxSize()) {
            if (uiState is AdminUiState.Loading && bookings.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = AccentBlue)
                }
            } else if (bookings.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(stringResource(id = R.string.admin_booking_empty), color = TextSecond, fontSize = 16.sp)
                }
            } else {
                LazyColumn(
                    state = listState,
                    contentPadding = PaddingValues(bottom = 100.dp)
                ) {
                    items(bookings, key = { it.id ?: it.code ?: it.hashCode().toString() }) { booking ->
                        BookingCard(
                            booking = booking,
                            onCancelBooking = { booking.id?.let { viewModel.cancelBooking(it) } }
                        )
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
fun BookingCard(
    booking: BookingResponse,
    onCancelBooking: () -> Unit = {}
) {
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
                    text = stringResource(id = R.string.booking_code_prefix, booking.code ?: "N/A"),
                    fontWeight = FontWeight.Bold,
                    color = AccentBlue,
                    fontSize = 14.sp
                )
                BookingStatusBadge(status = booking.status)
            }
            Spacer(modifier = Modifier.height(12.dp))
            
            val customerName = booking.customerName ?: booking.userProfile?.fullName ?: stringResource(id = R.string.customer_guest)
            val customerPhone = booking.customerPhone ?: stringResource(id = R.string.customer_no_phone)
            
            BookingInfoRow(icon = Icons.Rounded.Person, text = customerName)
            Spacer(modifier = Modifier.height(4.dp))
            BookingInfoRow(icon = Icons.Rounded.Phone, text = customerPhone)
            Spacer(modifier = Modifier.height(4.dp))
            BookingInfoRow(icon = Icons.Rounded.SportsSoccer, text = booking.field?.name ?: "Sân đã xóa")
            Spacer(modifier = Modifier.height(4.dp))
            BookingInfoRow(
                icon = Icons.Rounded.CalendarToday, 
                text = "${booking.bookingDate ?: ""} | ${formatDateDash(booking.startTime ?: "")} - ${formatDateDash(booking.endTime ?: "")}"
            )

            Spacer(modifier = Modifier.height(12.dp))
            HorizontalDivider(color = DividerColor)
            Spacer(modifier = Modifier.height(12.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                val status = booking.status?.lowercase()
                if (status == "pending" || status == "approved" || status == "confirmed") {
                    TextButton(
                        onClick = onCancelBooking,
                        colors = ButtonDefaults.textButtonColors(contentColor = AccentRed)
                    ) {
                        Text(stringResource(id = R.string.admin_btn_cancel), fontWeight = FontWeight.Bold, color = AccentRed)                  }
                } else {
                    Spacer(modifier = Modifier.width(1.dp))
                }

                Text(
                        text = stringResource(id = R.string.admin_booking_total, "${booking.totalPrice?.toInt()}đ"),
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
    val (bgColor, textColor, textRes) = when (status?.lowercase()) {
        "pending" -> Triple(AccentAmber.copy(alpha = 0.1f), AccentAmber, R.string.status_pending)
        "approved" -> Triple(AccentGreen.copy(alpha = 0.1f), AccentGreen, R.string.status_approved)
        "rejected" -> Triple(AccentRed.copy(alpha = 0.1f), AccentRed, R.string.status_rejected)
        "completed" -> Triple(AccentBlue.copy(alpha = 0.1f), AccentBlue, R.string.status_completed)
        else -> Triple(DividerColor, TextSecond, R.string.status_unknown)
    }

    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .background(bgColor)
            .padding(horizontal = 8.dp, vertical = 4.dp)
    ) {
        val displayStr = if (status?.lowercase() in listOf("pending", "approved", "rejected", "completed")) {
            stringResource(id = textRes)
        } else {
            status ?: stringResource(id = R.string.status_unknown)
        }
        Text(displayStr, color = textColor, fontSize = 12.sp, fontWeight = FontWeight.Bold)
    }
}
