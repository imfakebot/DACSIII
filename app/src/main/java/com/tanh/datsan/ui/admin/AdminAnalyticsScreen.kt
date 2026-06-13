package com.tanh.datsan.ui.admin

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.tanh.datsan.data.model.RevenueChartItemDto
import com.tanh.datsan.data.model.StatsResponseDto
import com.tanh.datsan.viewmodel.AdminAnalyticsViewModel
import java.text.NumberFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminAnalyticsScreen(
    onBackClick: () -> Unit,
    viewModel: AdminAnalyticsViewModel = viewModel()
) {
    val overviewStats by viewModel.overviewStats.collectAsState()
    val chartData by viewModel.chartData.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val error by viewModel.error.collectAsState()

    LaunchedEffect(Unit) {
        // Fetch all data for the current month or default range
        viewModel.fetchAnalytics(null, null)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Thống kê doanh thu") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            if (isLoading) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            } else if (error != null) {
                Text(
                    text = error ?: "Đã có lỗi xảy ra",
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.align(Alignment.Center)
                )
            } else {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(16.dp)
                ) {
                    overviewStats?.let { stats ->
                        OverviewCards(stats)
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    if (chartData.isNotEmpty()) {
                        Text(
                            text = "Biểu đồ doanh thu hàng tháng",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(bottom = 16.dp)
                        )
                        RevenueBarChart(chartData = chartData)
                    } else {
                        Text("Chưa có dữ liệu biểu đồ.")
                    }
                }
            }
        }
    }
}

@Composable
fun OverviewCards(stats: StatsResponseDto) {
    val formatter = NumberFormat.getCurrencyInstance(Locale("vi", "VN"))
    
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("Tổng doanh thu", fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text(
                text = formatter.format(stats.revenue),
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
        }
    }

    Spacer(modifier = Modifier.height(16.dp))

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        val completed = stats.transactions["completed"] ?: 0
        val pending = stats.transactions["pending"] ?: 0
        val failed = stats.transactions["failed"] ?: 0

        TransactionCard(
            title = "Thành công",
            count = completed,
            color = Color(0xFF4CAF50),
            modifier = Modifier.weight(1f)
        )
        TransactionCard(
            title = "Đang chờ",
            count = pending,
            color = Color(0xFFFFC107),
            modifier = Modifier.weight(1f)
        )
        TransactionCard(
            title = "Thất bại",
            count = failed,
            color = Color(0xFFF44336),
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
fun TransactionCard(title: String, count: Int, color: Color, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = color.copy(alpha = 0.1f))
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(text = title, fontSize = 12.sp, color = color)
            Text(
                text = count.toString(),
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = color
            )
        }
    }
}

@Composable
fun RevenueBarChart(chartData: List<RevenueChartItemDto>) {
    val maxRevenue = chartData.maxOfOrNull { it.revenue }?.toFloat() ?: 1f
    val primaryColor = MaterialTheme.colorScheme.primary

    Canvas(
        modifier = Modifier
            .fillMaxWidth()
            .height(250.dp)
            .padding(top = 16.dp)
    ) {
        val barWidth = size.width / (chartData.size * 2f)
        val space = barWidth

        chartData.forEachIndexed { index, item ->
            val barHeight = if (maxRevenue > 0) (item.revenue.toFloat() / maxRevenue) * size.height else 0f
            val x = index * (barWidth + space) + space / 2
            val y = size.height - barHeight

            drawRoundRect(
                color = primaryColor,
                topLeft = Offset(x, y),
                size = Size(barWidth, barHeight),
                cornerRadius = CornerRadius(4.dp.toPx(), 4.dp.toPx())
            )

            // Draw month labels
            val textPaint = android.graphics.Paint().apply {
                color = android.graphics.Color.GRAY
                textSize = 30f
                textAlign = android.graphics.Paint.Align.CENTER
            }
            drawContext.canvas.nativeCanvas.drawText(
                "T\${item.month}",
                x + barWidth / 2,
                size.height + 40f,
                textPaint
            )
        }
    }
}
