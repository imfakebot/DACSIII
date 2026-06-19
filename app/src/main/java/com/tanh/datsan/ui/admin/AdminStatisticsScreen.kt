package com.tanh.datsan.ui.admin

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.tanh.datsan.data.model.OverviewStatisticsResponse
import com.tanh.datsan.data.model.RecentBookingItem
import com.tanh.datsan.data.model.RevenueChartItem
import com.tanh.datsan.viewmodel.StatisticsViewModel
import com.tanh.datsan.viewmodel.TimeRange
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.*

// ── Sporty Premium Dark Palette ──────────────────────────────────────
private val DarkNavy = Color(0xFF0F1923)
private val CardDark = Color(0xFF1A2733)
private val AxisColor = Color(0xFF37474F)
private val SkeletonColor = Color(0xFF263238)
private val NeonGreen = Color(0xFF00E676)
private val TealAccent = Color(0xFF00BFA5)
private val TextPrimary = Color(0xFFFFFFFF)
private val TextSecondary = Color(0xFF90A4AE)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminStatisticsScreen(
    onBackClick: () -> Unit,
    viewModel: StatisticsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Dashboard Thống Kê",
                        fontWeight = FontWeight.Black,
                        color = TextPrimary
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Quay lại",
                            tint = TextPrimary
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFF141E2B),
                    titleContentColor = TextPrimary
                )
            )
        },
        containerColor = DarkNavy
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
                        recentBookings = uiState.recentBookings,
                        selectedTimeRange = uiState.selectedTimeRange,
                        onTimeRangeSelected = { viewModel.setTimeRange(it) }
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
    recentBookings: List<RecentBookingItem>,
    selectedTimeRange: TimeRange,
    onTimeRangeSelected: (TimeRange) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        item {
            TimeRangeFilter(
                selected = selectedTimeRange,
                onSelected = onTimeRangeSelected
            )
        }

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
                color = TextPrimary,
                modifier = Modifier.padding(bottom = 8.dp)
            )
        }

        if (recentBookings.isEmpty()) {
            item {
                Text(
                    "Chưa có giao dịch nào.",
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Center,
                    color = TextSecondary
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
        // Revenue card – gradient green background
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.Transparent)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        brush = Brush.linearGradient(
                            colors = listOf(NeonGreen, TealAccent)
                        ),
                        shape = RoundedCornerShape(16.dp)
                    )
            ) {
                Row(
                    modifier = Modifier.padding(20.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(52.dp)
                            .clip(CircleShape)
                            .background(Color.White.copy(alpha = 0.2f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Default.MonetizationOn,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(28.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(16.dp))
                    Column {
                        Text(
                            text = "Tổng Doanh Thu",
                            fontSize = 14.sp,
                            color = Color.White.copy(alpha = 0.85f)
                        )
                        Text(
                            text = formatCurrency(overview.revenue),
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Black,
                            color = Color.White
                        )
                    }
                }
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Completed bookings card
            MetricCard(
                title = "Đơn thành công",
                value = "${overview.transactions.completed}",
                icon = Icons.Default.CheckCircle,
                iconColor = Color(0xFF3B82F6),
                containerColor = CardDark,
                modifier = Modifier.weight(1f)
            )
            // Cancelled bookings card
            MetricCard(
                title = "Đơn đã hủy",
                value = "${overview.transactions.failed}",
                icon = Icons.Default.Cancel,
                iconColor = Color(0xFFEF4444),
                containerColor = CardDark,
                modifier = Modifier.weight(1f)
            )
        }

        // Pending bookings card
        MetricCard(
            title = "Đơn chờ xử lý",
            value = "${overview.transactions.pending}",
            icon = Icons.Default.Info,
            iconColor = Color(0xFFF59E0B),
            containerColor = CardDark,
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
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = containerColor)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(iconColor.copy(alpha = 0.2f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, contentDescription = null, tint = iconColor, modifier = Modifier.size(24.dp))
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column {
                Text(text = title, fontSize = 14.sp, color = TextSecondary)
                Text(
                    text = value,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Black,
                    color = TextPrimary
                )
            }
        }
    }
}

@Composable
fun RevenueChartSection(chartData: List<RevenueChartItem>) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(280.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = CardDark)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "Biểu đồ doanh thu",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimary,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            if (chartData.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("Chưa có dữ liệu", color = TextSecondary)
                }
            } else {
                val textMeasurer = rememberTextMeasurer()

                Canvas(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(top = 10.dp, bottom = 20.dp, start = 30.dp, end = 10.dp)
                ) {
                    val maxRevenue = chartData.maxOfOrNull { it.revenue } ?: 1.0
                    val widthPerPoint = size.width / (if (chartData.size > 1) chartData.size - 1 else 1)
                    val heightRatio = size.height / maxRevenue

                    // Draw Y and X axes
                    drawLine(
                        color = AxisColor,
                        start = Offset(0f, 0f),
                        end = Offset(0f, size.height),
                        strokeWidth = 2f
                    )
                    drawLine(
                        color = AxisColor,
                        start = Offset(0f, size.height),
                        end = Offset(size.width, size.height),
                        strokeWidth = 2f
                    )

                    // Draw max value label on Y axis
                    val maxRevText = formatCurrency(maxRevenue)
                    val measuredText = textMeasurer.measure(
                        maxRevText,
                        style = TextStyle(fontSize = 10.sp, color = TextSecondary)
                    )
                    drawText(
                        textLayoutResult = measuredText,
                        topLeft = Offset(-measuredText.size.width.toFloat() - 8f, 0f)
                    )

                    // Draw min value label (0) on Y axis
                    val minRevText = "0"
                    val measuredMinText = textMeasurer.measure(
                        minRevText,
                        style = TextStyle(fontSize = 10.sp, color = TextSecondary)
                    )
                    drawText(
                        textLayoutResult = measuredMinText,
                        topLeft = Offset(
                            -measuredMinText.size.width.toFloat() - 8f,
                            size.height - measuredMinText.size.height
                        )
                    )

                    // Build the line path
                    val path = Path()
                    chartData.forEachIndexed { index, item ->
                        val x = index * widthPerPoint
                        val y = size.height - (item.revenue * heightRatio).toFloat()

                        if (index == 0) {
                            path.moveTo(x, y)
                        } else {
                            path.lineTo(x, y)
                        }

                        // Draw dot
                        drawCircle(
                            color = TealAccent,
                            radius = 6.dp.toPx(),
                            center = Offset(x, y)
                        )
                    }

                    // Draw line chart stroke
                    drawPath(
                        path = path,
                        color = NeonGreen,
                        style = Stroke(width = 3.dp.toPx())
                    )

                    // Draw filled gradient under the line
                    val fillPath = Path()
                    chartData.forEachIndexed { index, item ->
                        val x = index * widthPerPoint
                        val y = size.height - (item.revenue * heightRatio).toFloat()
                        if (index == 0) fillPath.moveTo(x, y) else fillPath.lineTo(x, y)
                    }
                    fillPath.lineTo((chartData.size - 1) * widthPerPoint, size.height)
                    fillPath.lineTo(0f, size.height)
                    fillPath.close()
                    drawPath(
                        path = fillPath,
                        brush = Brush.verticalGradient(
                            colors = listOf(NeonGreen.copy(alpha = 0.3f), Color.Transparent),
                            startY = 0f,
                            endY = size.height
                        )
                    )
                }
            }
        }
    }
}

@Composable
fun BookingItemCard(booking: RecentBookingItem) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 12.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = CardDark)
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
                    .background(Color(0xFF263238)),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.Receipt, contentDescription = null, tint = TextSecondary)
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Mã: ${booking.id.take(8).uppercase()}...",
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    color = TextPrimary
                )
                Text(
                    text = formatDate(booking.createdAt),
                    fontSize = 12.sp,
                    color = TextSecondary
                )
            }
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = formatCurrency(booking.totalAmount),
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    color = NeonGreen
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
                    Text(
                        text = statusText,
                        fontSize = 10.sp,
                        color = statusColor,
                        fontWeight = FontWeight.Bold
                    )
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
            color = TextPrimary
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "Chỉ Admin/Quản lý mới có thể xem bảng điều khiển thống kê này.",
            textAlign = TextAlign.Center,
            color = TextSecondary
        )
        Spacer(modifier = Modifier.height(24.dp))
        Button(
            onClick = onBack,
            colors = ButtonDefaults.buttonColors(containerColor = NeonGreen)
        ) {
            Text("Quay lại Trang chủ", color = DarkNavy, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
fun ErrorView(errorMsg: String, onRetry: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            Icons.Default.ErrorOutline,
            contentDescription = null,
            tint = Color(0xFFEF4444),
            modifier = Modifier.size(64.dp)
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(errorMsg, color = Color(0xFFEF4444), textAlign = TextAlign.Center)
        Spacer(modifier = Modifier.height(16.dp))
        Button(
            onClick = onRetry,
            colors = ButtonDefaults.buttonColors(containerColor = NeonGreen)
        ) {
            Text("Thử lại", color = DarkNavy, fontWeight = FontWeight.Bold)
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
                    .background(SkeletonColor.copy(alpha = alpha))
            )
        }
        item {
            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(80.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(SkeletonColor.copy(alpha = alpha))
                )
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(80.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(SkeletonColor.copy(alpha = alpha))
                )
            }
        }
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(SkeletonColor.copy(alpha = alpha))
            )
        }
        items(3) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(70.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(SkeletonColor.copy(alpha = alpha))
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
        isoDate
    }
}

fun getStatusUI(status: String): Triple<Color, Color, String> {
    return when (status.lowercase()) {
        "completed" -> Triple(Color(0xFF00E676), Color(0xFF1B3A2A), "Hoàn thành")
        "pending" -> Triple(Color(0xFFF59E0B), Color(0xFF3A2E1B), "Chờ duyệt")
        "cancelled" -> Triple(Color(0xFFEF4444), Color(0xFF3A1B1B), "Đã hủy")
        else -> Triple(Color(0xFF90A4AE), Color(0xFF263238), status)
    }
}

@Composable
fun TimeRangeFilter(selected: TimeRange, onSelected: (TimeRange) -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(CardDark)
            .padding(4.dp),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        val options = listOf(
            TimeRange.TODAY to "Hôm nay",
            TimeRange.WEEK to "Tuần này",
            TimeRange.MONTH to "Tháng này"
        )

        options.forEach { (range, label) ->
            val isSelected = selected == range
            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(6.dp))
                    .background(if (isSelected) NeonGreen else Color.Transparent)
                    .clickable { onSelected(range) }
                    .padding(vertical = 8.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = label,
                    fontSize = 14.sp,
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                    color = if (isSelected) DarkNavy else TextSecondary
                )
            }
        }
    }
}
