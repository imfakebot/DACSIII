package com.tanh.datsan.ui.admin

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.tanh.datsan.data.model.OverviewStatisticsResponse
import com.tanh.datsan.data.model.RecentBookingItem
import com.tanh.datsan.data.model.RevenueChartItem
import com.tanh.datsan.viewmodel.StatisticsViewModel
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminStatisticsScreen(
    onBackClick: () -> Unit,
    viewModel: StatisticsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.fetchStatistics()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Dashboard Thống Kê", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Quay lại")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.White,
                    titleContentColor = Color(0xFF1E293B)
                )
            )
        },
        containerColor = Color(0xFFF5F7FA) // Nền xám nhạt hiện đại
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            when {
                uiState.isForbidden -> {
                    AccessDeniedView(onBackClick)
                }
                uiState.isLoading -> {
                    DashboardSkeleton()
                }
                uiState.error != null -> {
                    ErrorView(uiState.error!!, onRetry = { viewModel.fetchStatistics() })
                }
                else -> {
                    DashboardContent(
                        overview = uiState.overview,
                        chartData = uiState.chartData,
                        recentBookings = uiState.recentBookings
                    )
                }
            }
        }
    }
}

@Composable
fun DashboardContent(
    overview: OverviewStatisticsResponse?,
    chartData: List<RevenueChartItem>,
    recentBookings: List<RecentBookingItem>
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        item {
            overview?.let { OverviewSection(it) }
        }

        item {
            RevenueChartSection(chartData)
        }

        item {
            Text(
                text = "Giao dịch gần đây",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF1E293B),
                modifier = Modifier.padding(bottom = 8.dp)
            )
        }

        if (recentBookings.isEmpty()) {
            item {
                Text(
                    "Chưa có giao dịch nào.",
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Center,
                    color = Color.Gray
                )
            }
        } else {
            items(recentBookings) { booking ->
                BookingItemCard(booking)
            }
        }
    }
}

@Composable
fun OverviewSection(overview: OverviewStatisticsResponse) {
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        // Card Doanh thu (To full width)
        MetricCard(
            title = "Tổng Doanh Thu",
            value = formatCurrency(overview.revenue),
            icon = Icons.Default.MonetizationOn,
            iconColor = Color(0xFF10B981), // Xanh lá
            containerColor = Color.White,
            modifier = Modifier.fillMaxWidth()
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Card Booking thành công
            MetricCard(
                title = "Đơn thành công",
                value = "${overview.transactions.completed}",
                icon = Icons.Default.CheckCircle,
                iconColor = Color(0xFF3B82F6), // Xanh dương
                containerColor = Color.White,
                modifier = Modifier.weight(1f)
            )
            // Card Booking bị hủy
            MetricCard(
                title = "Đơn đã hủy",
                value = "${overview.transactions.failed}",
                icon = Icons.Default.Cancel,
                iconColor = Color(0xFFEF4444), // Đỏ
                containerColor = Color.White,
                modifier = Modifier.weight(1f)
            )
        }

        // Card Đơn chờ xử lý
        MetricCard(
            title = "Đơn chờ xử lý",
            value = "${overview.transactions.pending}",
            icon = Icons.Default.Info, // Standard Info Icon since Pending might not be available
            iconColor = Color(0xFFF59E0B), // Cam
            containerColor = Color.White,
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Composable
fun MetricCard(
    title: String,
    value: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    iconColor: Color,
    containerColor: Color,
    modifier: Modifier = Modifier
) {
    ElevatedCard(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.elevatedCardColors(containerColor = containerColor),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(iconColor.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, contentDescription = null, tint = iconColor, modifier = Modifier.size(24.dp))
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column {
                Text(text = title, fontSize = 14.sp, color = Color(0xFF64748B))
                Text(text = value, fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1E293B))
            }
        }
    }
}

@Composable
fun RevenueChartSection(chartData: List<RevenueChartItem>) {
    ElevatedCard(
        modifier = Modifier
            .fillMaxWidth()
            .height(250.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.elevatedCardColors(containerColor = Color.White),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "Biểu đồ doanh thu",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF1E293B),
                modifier = Modifier.padding(bottom = 16.dp)
            )

            if (chartData.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("Chưa có dữ liệu", color = Color.Gray)
                }
            } else {
                Canvas(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(top = 10.dp, bottom = 10.dp)
                ) {
                    val maxRevenue = chartData.maxOfOrNull { it.revenue } ?: 1.0
                    val widthPerPoint = size.width / (if (chartData.size > 1) chartData.size - 1 else 1)
                    val heightRatio = size.height / maxRevenue

                    val path = Path()
                    chartData.forEachIndexed { index, item ->
                        val x = index * widthPerPoint
                        val y = size.height - (item.revenue * heightRatio).toFloat()
                        
                        if (index == 0) {
                            path.moveTo(x, y)
                        } else {
                            path.lineTo(x, y)
                        }
                        
                        // Vẽ điểm (dot)
                        drawCircle(
                            color = Color(0xFF3B82F6),
                            radius = 6.dp.toPx(),
                            center = Offset(x, y)
                        )
                    }

                    // Vẽ đường line chart
                    drawPath(
                        path = path,
                        color = Color(0xFF3B82F6),
                        style = Stroke(width = 3.dp.toPx())
                    )
                }
            }
        }
    }
}

@Composable
fun BookingItemCard(booking: RecentBookingItem) {
    ElevatedCard(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 12.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.elevatedCardColors(containerColor = Color.White),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(Color(0xFFF1F5F9)),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.Receipt, contentDescription = null, tint = Color(0xFF64748B))
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Mã: ${booking.id.take(8).uppercase()}...",
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    color = Color(0xFF1E293B)
                )
                Text(
                    text = formatDate(booking.createdAt),
                    fontSize = 12.sp,
                    color = Color(0xFF64748B)
                )
            }
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = formatCurrency(booking.totalAmount),
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    color = Color(0xFF10B981)
                )
                // Status badge
                val (statusColor, statusBg, statusText) = getStatusUI(booking.status)
                Box(
                    modifier = Modifier
                        .padding(top = 4.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .background(statusBg)
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text(text = statusText, fontSize = 10.sp, color = statusColor, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
fun AccessDeniedView(onBack: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            Icons.Default.Lock,
            contentDescription = "Access Denied",
            tint = Color(0xFFEF4444),
            modifier = Modifier.size(80.dp)
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "Không có quyền truy cập",
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF1E293B)
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "Chỉ Admin/Quản lý mới có thể xem bảng điều khiển thống kê này.",
            textAlign = TextAlign.Center,
            color = Color(0xFF64748B)
        )
        Spacer(modifier = Modifier.height(24.dp))
        Button(
            onClick = onBack,
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF3B82F6))
        ) {
            Text("Quay lại Trang chủ")
        }
    }
}

@Composable
fun ErrorView(errorMsg: String, onRetry: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(Icons.Default.ErrorOutline, contentDescription = null, tint = Color.Red, modifier = Modifier.size(64.dp))
        Spacer(modifier = Modifier.height(16.dp))
        Text(errorMsg, color = Color.Red, textAlign = TextAlign.Center)
        Spacer(modifier = Modifier.height(16.dp))
        Button(onClick = onRetry) {
            Text("Thử lại")
        }
    }
}

@Composable
fun DashboardSkeleton() {
    val infiniteTransition = rememberInfiniteTransition(label = "skeleton")
    val alpha by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 0.7f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "alpha"
    )

    LazyColumn(
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(100.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color.LightGray.copy(alpha = alpha))
            )
        }
        item {
            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                Box(modifier = Modifier.weight(1f).height(80.dp).clip(RoundedCornerShape(16.dp)).background(Color.LightGray.copy(alpha = alpha)))
                Box(modifier = Modifier.weight(1f).height(80.dp).clip(RoundedCornerShape(16.dp)).background(Color.LightGray.copy(alpha = alpha)))
            }
        }
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color.LightGray.copy(alpha = alpha))
            )
        }
        items(3) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(70.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color.LightGray.copy(alpha = alpha))
            )
        }
    }
}

// --- Utils ---
fun formatCurrency(amount: Double): String {
    val format = NumberFormat.getCurrencyInstance(Locale("vi", "VN"))
    return format.format(amount)
}

fun formatDate(isoDate: String): String {
    return try {
        val parser = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault()).apply {
            timeZone = TimeZone.getTimeZone("UTC")
        }
        val date = parser.parse(isoDate) ?: return isoDate
        val formatter = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
        formatter.format(date)
    } catch (e: Exception) {
        isoDate // Trả về chuỗi gốc nếu lỗi parse
    }
}

fun getStatusUI(status: String): Triple<Color, Color, String> {
    return when (status.lowercase()) {
        "success", "completed" -> Triple(Color(0xFF047857), Color(0xFFD1FAE5), "Thành công")
        "canceled", "cancelled" -> Triple(Color(0xFFB91C1C), Color(0xFFFEE2E2), "Đã hủy")
        "pending" -> Triple(Color(0xFFB45309), Color(0xFFFEF3C7), "Chờ xử lý")
        else -> Triple(Color(0xFF475569), Color(0xFFF1F5F9), status.uppercase())
    }
}
