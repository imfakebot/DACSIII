package com.tanh.datsan.ui.home.history

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.tanh.datsan.data.model.BookingResponse
import com.tanh.datsan.viewmodel.BookingHistoryUiState
import java.text.NumberFormat
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BookingHistoryScreen(
    bookings: List<BookingResponse>,
    uiState: BookingHistoryUiState,
    currentStatus: String?,
    onFetchBookings: (String?) -> Unit,
    onCancelBooking: (String) -> Unit
) {
    val tabs = listOf(
        Pair(null, "Tất cả"),
        Pair("pending", "Chờ xác nhận"),
        Pair("confirmed", "Đã xác nhận"),
        Pair("checked_in", "Đã nhận sân"),
        Pair("finished", "Đã hoàn thành"),
        Pair("cancelled", "Đã hủy")
    )

    val selectedTabIndex = tabs.indexOfFirst { it.first == currentStatus }.takeIf { it >= 0 } ?: 0

    LaunchedEffect(currentStatus) {
        onFetchBookings(currentStatus)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Lịch sử đặt sân") },
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
        ) {
            ScrollableTabRow(
                selectedTabIndex = selectedTabIndex,
                edgePadding = 8.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                tabs.forEachIndexed { index, tab ->
                    Tab(
                        selected = selectedTabIndex == index,
                        onClick = { onFetchBookings(tab.first) },
                        text = { Text(tab.second) }
                    )
                }
            }

            Box(modifier = Modifier.fillMaxSize()) {
                if (uiState is BookingHistoryUiState.Loading && bookings.isEmpty()) {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                } else if (bookings.isEmpty()) {
                    Text(
                        text = "Không có lịch sử đặt sân.",
                        modifier = Modifier.align(Alignment.Center)
                    )
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(bookings) { booking ->
                            BookingItem(
                                booking = booking,
                                onCancelBooking = { booking.id?.let { onCancelBooking(it) } }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun BookingItem(
    booking: BookingResponse,
    onCancelBooking: () -> Unit = {}
) {
    val statusText = when (booking.status?.lowercase()) {
        "pending" -> "Chờ xác nhận"
        "confirmed" -> "Đã xác nhận"
        "checked_in" -> "Đã nhận sân"
        "finished" -> "Đã hoàn thành"
        "cancelled" -> "Đã hủy"
        else -> booking.status ?: "Không xác định"
    }

    val statusColor = when (booking.status?.lowercase()) {
        "pending" -> MaterialTheme.colorScheme.tertiary
        "confirmed", "checked_in", "finished" -> MaterialTheme.colorScheme.primary
        "cancelled" -> MaterialTheme.colorScheme.error
        else -> MaterialTheme.colorScheme.onSurface
    }

    val currencyFormatter = NumberFormat.getCurrencyInstance(Locale("vi", "VN"))

    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Mã đơn: ${booking.code ?: "N/A"}",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = statusText,
                    style = MaterialTheme.typography.labelLarge,
                    color = statusColor,
                    fontWeight = FontWeight.SemiBold
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Sân: ${booking.field?.name ?: "N/A"} - ${booking.field?.fieldType?.name ?: ""}",
                style = MaterialTheme.typography.bodyMedium
            )
            Text(
                text = "Chi nhánh: ${booking.field?.branch?.name ?: "N/A"}",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.secondary
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Ngày đá: ${booking.bookingDate ?: "N/A"}",
                style = MaterialTheme.typography.bodyMedium
            )
            Text(
                text = "Khung giờ: ${booking.startTime ?: ""} - ${booking.endTime ?: ""}",
                style = MaterialTheme.typography.bodyMedium
            )
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                val status = booking.status?.lowercase()
                if (status == "pending" || status == "approved" || status == "confirmed") {
                    TextButton(
                        onClick = onCancelBooking,
                        colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)
                    ) {
                        Text("Hủy đơn", fontWeight = FontWeight.Bold)
                    }
                } else {
                    Spacer(modifier = Modifier.width(1.dp))
                }

                Text(
                    text = "Tổng: ${currencyFormatter.format(booking.totalPrice ?: 0)}",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}
