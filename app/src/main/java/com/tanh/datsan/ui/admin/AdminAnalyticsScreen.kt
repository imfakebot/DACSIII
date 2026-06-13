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
import androidx.hilt.navigation.compose.hiltViewModel
import com.tanh.datsan.data.model.RevenueChartItem
import com.tanh.datsan.data.model.StatsOverviewResponse
import com.tanh.datsan.viewmodel.AdminAnalyticsViewModel
import java.text.NumberFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminAnalyticsScreen(
    onBackClick: () -> Unit,
    viewModel: AdminAnalyticsViewModel = hiltViewModel()
) {
    val overviewStats by viewModel.overviewStats.collectAsState()
    val chartData by viewModel.chartData.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val error by viewModel.error.collectAsState()
    
    val totalBookings by viewModel.totalBookings.collectAsState()
    val cancellationRate by viewModel.cancellationRate.collectAsState()

    var isBranchMenuExpanded by remember { mutableStateOf(false) }
    // Danh sách chi nhánh mẫu (có thể thay thế bằng dữ liệu từ API nếu có)
    val branches = listOf(
        Pair(null, "Toàn hệ thống (Global)"),
        Pair("branch_1", "Chi nhánh Trung tâm"),
        Pair("branch_2", "Chi nhánh Phía Nam")
    )
    var selectedBranch by remember { mutableStateOf(branches[0]) }

    LaunchedEffect(selectedBranch) {
        // Gọi lại API khi thay đổi chi nhánh lọc
        viewModel.fetchAnalytics(null, null, selectedBranch.first)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Thống kê kinh doanh") },
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
                    // Dropdown Lọc theo Chi nhánh
                    Box(modifier = Modifier.padding(bottom = 16.dp)) {
                        OutlinedButton(
                            onClick = { isBranchMenuExpanded = true },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("Lọc: \${selectedBranch.second}", color = MaterialTheme.colorScheme.onSurface)
                                Text("▼")
                            }
                        }
                        DropdownMenu(
                            expanded = isBranchMenuExpanded,
                            onDismissRequest = { isBranchMenuExpanded = false }
                        ) {
                            branches.forEach { branch ->
                                DropdownMenuItem(
                                    text = { Text(branch.second) },
                                    onClick = {
                                        selectedBranch = branch
                                        isBranchMenuExpanded = false
                                    }
                                )
                            }
                        }
                    }

                    overviewStats?.let { stats ->
                        OverviewCards(
                            stats = stats,
                            totalBookings = totalBookings,
                            cancellationRate = cancellationRate
                        )
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    if (chartData.isNotEmpty()) {
                        Text(
                            text = "Biểu đồ tăng trưởng doanh thu",
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
fun OverviewCards(stats: StatsOverviewResponse, totalBookings: Int, cancellationRate: Double) {
    val formatter = NumberFormat.getCurrencyInstance(Locale.Builder().setLanguage("vi").setRegion("VN").build())
    
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
        TransactionCard(
            title = "Tổng lượt đặt",
            count = totalBookings.toString(),
            color = Color(0xFF2196F3),
            modifier = Modifier.weight(1f)
        )
        TransactionCard(
            title = "Tỷ lệ hủy",
            count = String.format(Locale.US, "%.1f%%", cancellationRate),
            color = Color(0xFFF44336),
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
fun TransactionCard(title: String, count: String, color: Color, modifier: Modifier = Modifier) {
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
                text = count,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = color
            )
        }
    }
}

@Composable
fun RevenueBarChart(chartData: List<RevenueChartItem>) {
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
