package com.tanh.datsan.ui.home.booking

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.SportsSoccer
import androidx.compose.material3.*
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.tanh.datsan.data.model.BookingResponse
import com.tanh.datsan.viewmodel.HistoryUiState
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistoryScreen(
    uiState: HistoryUiState,
    onRefresh: () -> Unit,
    onNavigateToHome: () -> Unit,
    onNavigateToLogin: () -> Unit,
    onResetTokenExpired: () -> Unit
) {
    LaunchedEffect(uiState.isTokenExpired) {
        if (uiState.isTokenExpired) {
            onNavigateToLogin()
            onResetTokenExpired()
        }
    }

    Box(modifier = Modifier.fillMaxSize().background(Color(0xFFF8F9FC))) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Header
            Surface(
                modifier = Modifier.fillMaxWidth().shadow(elevation = 2.dp),
                color = Color.White
            ) {
                Text(
                    text = "Lịch sử đặt sân",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF111827),
                    modifier = Modifier.padding(start = 16.dp, top = 48.dp, bottom = 16.dp)
                )
            }

            PullToRefreshBox(
                isRefreshing = uiState.isRefreshing,
                onRefresh = onRefresh,
                modifier = Modifier.fillMaxSize()
            ) {
                when {
                    uiState.isLoading && uiState.bookings.isEmpty() -> {
                        // Loading State
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            CircularProgressIndicator(color = Color(0xFF3D7EF5))
                        }
                    }
                    !uiState.isLoading && uiState.bookings.isEmpty() -> {
                        // Empty State
                        Column(
                            modifier = Modifier.fillMaxSize().padding(32.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.SportsSoccer,
                                contentDescription = null,
                                modifier = Modifier.size(80.dp),
                                tint = Color.Gray
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = "Ông chưa đi đá bóng bao giờ à? Đặt sân ngay đi!",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Color(0xFF111827),
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center
                            )
                            Spacer(modifier = Modifier.height(24.dp))
                            Button(
                                onClick = onNavigateToHome,
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF3D7EF5)),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text("Trang chủ", color = Color.White, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                    else -> {
                        // List
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(16.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            items(uiState.bookings, key = { it.id ?: it.hashCode() }) { booking ->
                                BookingHistoryCard(booking = booking)
                            }
                        }
                    }
                }

            }
        }
    }
}

@Composable
fun BookingHistoryCard(booking: BookingResponse) {
    val fieldName = booking.field?.name ?: "Sân bóng"
    
    // Format date and time
    val startTime = booking.startTime?.take(5) ?: "00:00"
    val endTime = booking.endTime?.take(5) ?: "00:00"
    val formattedDate = formatBookingDate(booking.bookingDate)
    val timeString = "$startTime - $endTime, $formattedDate"

    // Format price
    val format = NumberFormat.getCurrencyInstance(Locale("vi", "VN"))
    val priceString = format.format(booking.totalPrice ?: 0)

    // Status Badge
    val statusMap = getStatusBadge(booking.status)

    Card(
        modifier = Modifier.fillMaxWidth().shadow(elevation = 1.dp, shape = RoundedCornerShape(12.dp)),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = fieldName,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF111827)
                )
                // Badge
                Box(
                    modifier = Modifier
                        .background(statusMap.bgColor, RoundedCornerShape(6.dp))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = statusMap.label,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = statusMap.textColor
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = "Thời gian: $timeString",
                fontSize = 14.sp,
                color = Color(0xFF4B5563)
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = "Tổng tiền: $priceString",
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color(0xFF3D7EF5)
            )
        }
    }
}

data class StatusBadgeData(val label: String, val textColor: Color, val bgColor: Color)

fun getStatusBadge(status: String?): StatusBadgeData {
    return when (status?.lowercase()) {
        "completed", "checked_in", "finished" -> 
            StatusBadgeData("Thành công", Color(0xFF22C55E), Color(0xFF22C55E).copy(alpha = 0.15f))
        "pending", "confirmed" -> 
            StatusBadgeData("Chờ duyệt", Color(0xFFF59E0B), Color(0xFFF59E0B).copy(alpha = 0.15f))
        "cancelled" -> 
            StatusBadgeData("Đã hủy", Color(0xFFEF4444), Color(0xFFEF4444).copy(alpha = 0.15f))
        else -> 
            StatusBadgeData(status ?: "Unknown", Color.Gray, Color.Gray.copy(alpha = 0.15f))
    }
}

fun formatBookingDate(rawDate: String?): String {
    if (rawDate == null) return "Unknown"
    return try {
        // Assume format "yyyy-MM-dd"
        val inputFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val date = inputFormat.parse(rawDate) ?: return rawDate
        val outputFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        outputFormat.format(date)
    } catch (e: Exception) {
        // Fallback or full ISO datetime string
        try {
            val inputFormatIso = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault())
            val date = inputFormatIso.parse(rawDate) ?: return rawDate
            val outputFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
            outputFormat.format(date)
        } catch (ex: Exception) {
            rawDate
        }
    }
}
