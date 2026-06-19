package com.tanh.datsan.ui.admin

import android.annotation.SuppressLint
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.tanh.datsan.data.model.BookingResponse
import com.tanh.datsan.data.model.OverviewStatisticsResponse
import com.tanh.datsan.data.model.RevenueChartItem
import com.tanh.datsan.viewmodel.StatisticsViewModel
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.*
import androidx.compose.ui.platform.LocalLocale

// ─── Design tokens — aligned with ProfileScreen / DetailScreen ──────────────
private val DarkBg      = Color(0xFF0F172A)
private val DarkBg2     = Color(0xFF1E293B)
private val AccentBlue  = Color(0xFF3B82F6)
private val AccentGreen = Color(0xFF10B981)
private val AccentPurple= Color(0xFF8B5CF6)
private val AccentAmber = Color(0xFFF59E0B)
private val AccentRed   = Color(0xFFEF4444)
private val AppBg       = Color(0xFFF1F5F9)
private val CardWhite   = Color.White
private val TextPrimary = Color(0xFF0F172A)
private val TextSecond  = Color(0xFF64748B)
private val TextTertiary= Color(0xFF94A3B8)
private val Divider     = Color(0xFFF1F5F9)

// ─── Date filter ─────────────────────────────────────────────────────────────
enum class DateFilter(val label: String) {
    TODAY("Hôm nay"),
    THIS_WEEK("7 ngày"),
    THIS_MONTH("Tháng này"),
    LAST_MONTH("Tháng trước"),
    THIS_YEAR("Năm nay"),
    CUSTOM("Tùy chỉnh");

    fun toDateRange(): Pair<Date?, Date?> {
        val cal = Calendar.getInstance()
        return when (this) {
            TODAY -> {
                cal.set(Calendar.HOUR_OF_DAY, 0); cal.set(Calendar.MINUTE, 0)
                cal.set(Calendar.SECOND, 0); cal.set(Calendar.MILLISECOND, 0)
                val start = cal.time
                cal.set(Calendar.HOUR_OF_DAY, 23); cal.set(Calendar.MINUTE, 59); cal.set(Calendar.SECOND, 59)
                Pair(start, cal.time)
            }
            THIS_WEEK -> {
                cal.add(Calendar.DAY_OF_YEAR, -6)
                cal.set(Calendar.HOUR_OF_DAY, 0); cal.set(Calendar.MINUTE, 0)
                cal.set(Calendar.SECOND, 0); cal.set(Calendar.MILLISECOND, 0)
                Pair(cal.time, Date())
            }
            THIS_MONTH -> {
                cal.set(Calendar.DAY_OF_MONTH, 1)
                cal.set(Calendar.HOUR_OF_DAY, 0); cal.set(Calendar.MINUTE, 0)
                cal.set(Calendar.SECOND, 0); cal.set(Calendar.MILLISECOND, 0)
                Pair(cal.time, Date())
            }
            LAST_MONTH -> {
                cal.add(Calendar.MONTH, -1)
                cal.set(Calendar.DAY_OF_MONTH, 1)
                cal.set(Calendar.HOUR_OF_DAY, 0); cal.set(Calendar.MINUTE, 0)
                cal.set(Calendar.SECOND, 0); cal.set(Calendar.MILLISECOND, 0)
                val start = cal.time
                cal.set(Calendar.DAY_OF_MONTH, cal.getActualMaximum(Calendar.DAY_OF_MONTH))
                cal.set(Calendar.HOUR_OF_DAY, 23); cal.set(Calendar.MINUTE, 59); cal.set(Calendar.SECOND, 59)
                Pair(start, cal.time)
            }
            THIS_YEAR -> {
                cal.set(Calendar.DAY_OF_YEAR, 1)
                cal.set(Calendar.HOUR_OF_DAY, 0); cal.set(Calendar.MINUTE, 0)
                cal.set(Calendar.SECOND, 0); cal.set(Calendar.MILLISECOND, 0)
                Pair(cal.time, Date())
            }
            CUSTOM -> Pair(null, null)
        }
    }
}

// ─── Main screen ─────────────────────────────────────────────────────────────
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminStatisticsScreen(
    onBackClick: () -> Unit,
    viewModel: StatisticsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    var selectedFilter  by remember { mutableStateOf(DateFilter.THIS_MONTH) }
    var customStartDate by remember { mutableStateOf<Date?>(null) }
    var customEndDate   by remember { mutableStateOf<Date?>(null) }
    var selectedYear    by remember { mutableStateOf(Calendar.getInstance().get(Calendar.YEAR)) }
    var showDatePicker  by remember { mutableStateOf(false) }
    var dropdownExpanded by remember { mutableStateOf(false) }

    val locale = LocalLocale.current.platformLocale
    val dateLabel: String = remember(selectedFilter, customStartDate, customEndDate) {
        val fmt = SimpleDateFormat("dd/MM/yy", Locale.getDefault())
        if (selectedFilter == DateFilter.CUSTOM && customStartDate != null && customEndDate != null)
            "${fmt.format(customStartDate!!)} – ${fmt.format(customEndDate!!)}"
        else ""
    }

    fun applyPreset(filter: DateFilter) {
        selectedFilter = filter
        val (s, e) = filter.toDateRange()
        customStartDate = s; customEndDate = e
        val yr = Calendar.getInstance().also { c -> s?.let { c.time = it } }.get(Calendar.YEAR)
        selectedYear = yr
        viewModel.fetchStatistics(s, e, yr)
    }

    fun applyCustom(s: Date, e: Date) {
        selectedFilter = DateFilter.CUSTOM; customStartDate = s; customEndDate = e
        val yr = Calendar.getInstance().also { c -> c.time = s }.get(Calendar.YEAR)
        selectedYear = yr
        viewModel.fetchStatistics(s, e, yr)
    }

    LaunchedEffect(Unit) { applyPreset(DateFilter.THIS_MONTH) }

    if (showDatePicker) {
        DateRangePickerDialog(
            onDismiss = { showDatePicker = false },
            onConfirm = { s, e -> showDatePicker = false; applyCustom(s, e) }
        )
    }

    Scaffold(containerColor = AppBg) { padding ->
        Box(modifier = Modifier.fillMaxSize().padding(padding)) {
            when {
                uiState.isForbidden -> StatAccessDenied(onBackClick)
                uiState.error != null && !uiState.isLoading -> StatError(uiState.error!!) { applyPreset(selectedFilter) }
                else -> {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(bottom = 40.dp)
                    ) {
                        // ── Dark header with canvas blobs ──────────────────
                        item {
                            StatHeader(
                                selectedFilter   = selectedFilter,
                                dateLabel        = dateLabel,
                                dropdownExpanded = dropdownExpanded,
                                onDropdownToggle = { dropdownExpanded = !dropdownExpanded },
                                onDropdownDismiss= { dropdownExpanded = false },
                                onPresetSelect   = { applyPreset(it); dropdownExpanded = false },
                                onCustomClick    = { showDatePicker = true }
                            )
                        }

                        // ── Metric cards floating up from header ───────────
                        item {
                            if (uiState.isLoading) StatSkeleton()
                            else uiState.overview?.let { StatOverviewCards(it) }
                        }

                        // ── Bar chart card ────────────────────────────────
                        if (!uiState.isLoading) {
                            item {
                                StatBarChart(
                                    chartData    = uiState.chartData,
                                    selectedYear = selectedYear,
                                    onYearChange = { yr ->
                                        selectedYear = yr
                                        viewModel.fetchStatistics(customStartDate, customEndDate, yr)
                                    }
                                )
                            }

                            // ── Recent bookings ───────────────────────────
                            item {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(start = 20.dp, end = 20.dp, top = 8.dp, bottom = 4.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(4.dp, 20.dp)
                                            .background(AccentBlue, RoundedCornerShape(2.dp))
                                    )
                                    Spacer(Modifier.width(10.dp))
                                    Text(
                                        "Giao dịch gần đây",
                                        fontWeight = FontWeight.ExtraBold,
                                        fontSize = 17.sp,
                                        color = TextPrimary
                                    )
                                }
                            }

                            if (uiState.recentBookings.isEmpty()) {
                                item { StatEmptyBookings() }
                            } else {
                                items(uiState.recentBookings) { StatBookingCard(it) }
                            }
                        }
                    }
                }
            }
        }
    }
}

// ─── Header ──────────────────────────────────────────────────────────────────
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StatHeader(
    selectedFilter: DateFilter,
    dateLabel: String,
    dropdownExpanded: Boolean,
    onDropdownToggle: () -> Unit,
    onDropdownDismiss: () -> Unit,
    onPresetSelect: (DateFilter) -> Unit,
    onCustomClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(200.dp)
            .background(Brush.verticalGradient(listOf(DarkBg, DarkBg2)))
    ) {
        // Decorative blobs — mirroring ProfileScreen's PremiumHeaderBackground
        Canvas(modifier = Modifier.fillMaxSize()) {
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(AccentBlue.copy(alpha = 0.18f), Color.Transparent),
                    center = Offset(size.width * 0.1f, size.height * 0.15f),
                    radius = size.width * 0.8f
                ),
                radius = size.width * 0.8f,
                center = Offset(size.width * 0.1f, size.height * 0.15f)
            )
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(AccentPurple.copy(alpha = 0.12f), Color.Transparent),
                    center = Offset(size.width * 0.92f, size.height * 0.85f),
                    radius = size.width * 0.6f
                ),
                radius = size.width * 0.6f,
                center = Offset(size.width * 0.92f, size.height * 0.85f)
            )
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp, vertical = 20.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Title
            Column {
                Text(
                    "Dashboard",
                    color = Color.White,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.ExtraBold,
                    letterSpacing = 0.5.sp
                )
                Text(
                    "Tổng quan hoạt động kinh doanh",
                    color = Color.White.copy(alpha = 0.6f),
                    fontSize = 13.sp,
                    modifier = Modifier.padding(top = 2.dp)
                )
            }

            // Filter row
            Row(
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Dropdown for preset filters
                ExposedDropdownMenuBox(
                    expanded = dropdownExpanded,
                    onExpandedChange = { onDropdownToggle() },
                    modifier = Modifier.weight(1f)
                ) {
                    Surface(
                        modifier = Modifier
                            .menuAnchor()
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(14.dp)),
                        color = Color.White.copy(alpha = 0.12f),
                        shape = RoundedCornerShape(14.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(alpha = 0.15f))
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                Icons.Default.CalendarMonth, null,
                                tint = Color.White.copy(alpha = 0.8f),
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(Modifier.width(8.dp))
                            Text(
                                text = if (selectedFilter == DateFilter.CUSTOM && dateLabel.isNotEmpty())
                                    dateLabel else selectedFilter.label,
                                color = Color.White,
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 13.sp,
                                modifier = Modifier.weight(1f),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Icon(
                                if (dropdownExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                                null,
                                tint = Color.White.copy(alpha = 0.7f),
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }

                    ExposedDropdownMenu(
                        expanded = dropdownExpanded,
                        onDismissRequest = onDropdownDismiss,
                        modifier = Modifier.background(CardWhite)
                    ) {
                        DateFilter.entries.filter { it != DateFilter.CUSTOM }.forEach { filter ->
                            val isSel = filter == selectedFilter
                            DropdownMenuItem(
                                text = {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        if (isSel) {
                                            Icon(Icons.Default.Check, null, tint = AccentBlue, modifier = Modifier.size(16.dp))
                                            Spacer(Modifier.width(8.dp))
                                        } else {
                                            Spacer(Modifier.width(24.dp))
                                        }
                                        Text(
                                            filter.label,
                                            fontWeight = if (isSel) FontWeight.Bold else FontWeight.Normal,
                                            color = if (isSel) AccentBlue else TextPrimary,
                                            fontSize = 14.sp
                                        )
                                    }
                                },
                                onClick = { onPresetSelect(filter) }
                            )
                        }
                    }
                }

                // Custom date button
                val isCustom = selectedFilter == DateFilter.CUSTOM
                Surface(
                    modifier = Modifier
                        .clip(RoundedCornerShape(14.dp))
                        .clickable { onCustomClick() },
                    color = if (isCustom) Color.White else Color.White.copy(alpha = 0.12f),
                    shape = RoundedCornerShape(14.dp),
                    border = androidx.compose.foundation.BorderStroke(
                        1.dp,
                        if (isCustom) Color.White else Color.White.copy(alpha = 0.15f)
                    )
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp)
                    ) {
                        Icon(
                            Icons.Default.DateRange, null,
                            tint = if (isCustom) AccentBlue else Color.White.copy(alpha = 0.8f),
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(Modifier.width(5.dp))
                        Text(
                            "Tùy chỉnh",
                            color = if (isCustom) AccentBlue else Color.White,
                            fontWeight = if (isCustom) FontWeight.Bold else FontWeight.Normal,
                            fontSize = 13.sp
                        )
                    }
                }
            }
        }
    }
}

// ─── Overview cards — floated with offset like DetailScreen ─────────────────
@Composable
fun StatOverviewCards(overview: OverviewStatisticsResponse) {
    val vnd = NumberFormat.getCurrencyInstance(Locale("vi", "VN"))
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .offset(y = (-20).dp)          // float up over header — same trick as DetailScreen
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Revenue — full width, prominent
        StatMetricCard(
            title = "Tổng Doanh Thu",
            value = vnd.format(overview.revenue),
            subLabel = "Trong khoảng thời gian đã chọn",
            icon = Icons.Default.MonetizationOn,
            iconBg = AccentGreen.copy(alpha = 0.12f),
            iconTint = AccentGreen,
            accentColor = AccentGreen,
            modifier = Modifier.fillMaxWidth()
        )

        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            StatMetricCard(
                title = "Thành công",
                value = "${overview.transactions.completed}",
                subLabel = "đơn",
                icon = Icons.Default.CheckCircle,
                iconBg = AccentBlue.copy(alpha = 0.1f),
                iconTint = AccentBlue,
                accentColor = AccentBlue,
                modifier = Modifier.weight(1f)
            )
            StatMetricCard(
                title = "Đã hủy",
                value = "${overview.transactions.failed}",
                subLabel = "đơn",
                icon = Icons.Default.Cancel,
                iconBg = AccentRed.copy(alpha = 0.1f),
                iconTint = AccentRed,
                accentColor = AccentRed,
                modifier = Modifier.weight(1f)
            )
        }

        StatMetricCard(
            title = "Chờ xử lý",
            value = "${overview.transactions.pending}",
            subLabel = "đơn đang chờ",
            icon = Icons.Default.HourglassTop,
            iconBg = AccentAmber.copy(alpha = 0.1f),
            iconTint = AccentAmber,
            accentColor = AccentAmber,
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Composable
fun StatMetricCard(
    title: String,
    value: String,
    subLabel: String,
    icon: ImageVector,
    iconBg: Color,
    iconTint: Color,
    accentColor: Color,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.shadow(4.dp, RoundedCornerShape(24.dp)),
        shape = RoundedCornerShape(24.dp),
        color = CardWhite,
        border = androidx.compose.foundation.BorderStroke(1.dp, Divider)
    ) {
        Row(
            modifier = Modifier.padding(18.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(50.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(iconBg),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, null, tint = iconTint, modifier = Modifier.size(26.dp))
            }
            Spacer(Modifier.width(14.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(title, color = TextSecond, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                Text(
                    value,
                    color = TextPrimary,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.ExtraBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(subLabel, color = TextTertiary, fontSize = 11.sp)
            }
            // Accent dot
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .clip(CircleShape)
                    .background(accentColor)
            )
        }
    }
}

// ─── Bar chart card ───────────────────────────────────────────────────────────
@Composable
fun StatBarChart(
    chartData: List<RevenueChartItem>,
    selectedYear: Int,
    onYearChange: (Int) -> Unit
) {
    val monthLabels = listOf("T1","T2","T3","T4","T5","T6","T7","T8","T9","T10","T11","T12")
    val filled = (1..12).map { m -> chartData.find { it.month == m }?.revenue ?: 0.0 }
    val maxVal  = filled.maxOrNull()?.takeIf { it > 0 } ?: 1.0
    val currentMonth = Calendar.getInstance().get(Calendar.MONTH) + 1

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        shape = RoundedCornerShape(24.dp),
        color = CardWhite,
        border = androidx.compose.foundation.BorderStroke(1.dp, Divider),
        shadowElevation = 2.dp
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            // Title row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        "Doanh thu theo tháng",
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 15.sp,
                        color = TextPrimary
                    )
                    val vnd = NumberFormat.getCurrencyInstance(Locale("vi", "VN"))
                    Text(
                        "Tổng: ${vnd.format(filled.sum())}",
                        color = AccentGreen,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
                // Year picker — same style as profile edit toggle
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = AppBg,
                    border = androidx.compose.foundation.BorderStroke(1.dp, Divider)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                    ) {
                        IconButton(onClick = { onYearChange(selectedYear - 1) }, modifier = Modifier.size(30.dp)) {
                            Icon(Icons.Default.ChevronLeft, null, tint = TextSecond, modifier = Modifier.size(18.dp))
                        }
                        Text(
                            "$selectedYear",
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary,
                            fontSize = 14.sp,
                            modifier = Modifier.padding(horizontal = 4.dp)
                        )
                        IconButton(onClick = { onYearChange(selectedYear + 1) }, modifier = Modifier.size(30.dp)) {
                            Icon(Icons.Default.ChevronRight, null, tint = TextSecond, modifier = Modifier.size(18.dp))
                        }
                    }
                }
            }

            Spacer(Modifier.height(20.dp))

            val barAreaHeight = 140.dp

            // Bar area only
            Row(
                modifier = Modifier.fillMaxWidth().height(barAreaHeight),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.Bottom
            ) {
                filled.forEachIndexed { index, revenue ->
                    val fraction = (revenue / maxVal).toFloat().coerceIn(0f, 1f)
                    val barHeightDp = (barAreaHeight.value * fraction).coerceAtLeast(3f)
                    val isCurrentMonth = (index + 1) == currentMonth
                    val isMaxMonth = revenue == filled.maxOrNull() && revenue > 0

                    Box(
                        modifier = Modifier.weight(1f).fillMaxHeight(),
                        contentAlignment = Alignment.BottomCenter
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth(0.7f)
                                .height(barHeightDp.dp)
                                .clip(RoundedCornerShape(topStart = 6.dp, topEnd = 6.dp))
                                .background(
                                    when {
                                        isMaxMonth -> Brush.verticalGradient(
                                            listOf(AccentGreen.copy(alpha = 0.9f), AccentGreen)
                                        )
                                        revenue > 0 -> Brush.verticalGradient(
                                            listOf(AccentBlue.copy(alpha = 0.7f), AccentBlue)
                                        )
                                        else -> Brush.verticalGradient(
                                            listOf(Color(0xFFE2E8F0), Color(0xFFCBD5E1))
                                        )
                                    }
                                )
                        )
                    }
                }
            }

            // Label row — separate to avoid clipping
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                monthLabels.forEachIndexed { index, label ->
                    val isCurrentMonth = (index + 1) == currentMonth
                    Text(
                        label,
                        modifier = Modifier.weight(1f).padding(top = 5.dp),
                        fontSize = 9.sp,
                        color = if (isCurrentMonth) AccentBlue else TextTertiary,
                        textAlign = TextAlign.Center,
                        fontWeight = if (isCurrentMonth) FontWeight.Bold else FontWeight.Normal
                    )
                }
            }

            // Legend
            Spacer(Modifier.height(12.dp))
            HorizontalDivider(color = Divider)
            Spacer(Modifier.height(10.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                LegendDot(AccentGreen, "Cao nhất")
                LegendDot(AccentBlue, "Có doanh thu")
                LegendDot(Color(0xFFCBD5E1), "Chưa có")
            }
        }
    }
}

@Composable
private fun LegendDot(color: Color, label: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(color))
        Spacer(Modifier.width(5.dp))
        Text(label, fontSize = 10.sp, color = TextTertiary)
    }
}

// ─── Recent booking card ──────────────────────────────────────────────────────
@Composable
fun StatBookingCard(booking: BookingResponse) {
    val vnd = NumberFormat.getCurrencyInstance(Locale("vi", "VN"))
    val status = booking.status ?: ""
    val (statusColor, statusBg, statusText) = when (status.lowercase()) {
        "completed","finished","success" -> Triple(Color(0xFF047857), Color(0xFFD1FAE5), "Thành công")
        "cancelled","canceled"          -> Triple(Color(0xFFB91C1C), Color(0xFFFEE2E2), "Đã hủy")
        "pending"                       -> Triple(Color(0xFFB45309), Color(0xFFFEF3C7), "Chờ xử lý")
        "confirmed"                     -> Triple(AccentBlue, Color(0xFFDBEAFE), "Đã xác nhận")
        else -> Triple(TextSecond, AppBg, status.uppercase())
    }

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 5.dp),
        shape = RoundedCornerShape(20.dp),
        color = CardWhite,
        border = androidx.compose.foundation.BorderStroke(1.dp, Divider),
        shadowElevation = 1.dp
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(46.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(AppBg),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.Receipt, null, tint = AccentBlue, modifier = Modifier.size(24.dp))
            }
            Spacer(Modifier.width(14.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    "Mã: ${(booking.code ?: booking.id ?: "").take(8).uppercase()}",
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp,
                    color = TextPrimary
                )
                Text(
                    formatDateDash(booking.createdAt ?: ""),
                    fontSize = 11.sp,
                    color = TextTertiary,
                    modifier = Modifier.padding(top = 2.dp)
                )
            }
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    vnd.format(booking.totalPrice ?: 0),
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 13.sp,
                    color = AccentGreen
                )
                Spacer(Modifier.height(5.dp))
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(statusBg)
                        .padding(horizontal = 8.dp, vertical = 3.dp)
                ) {
                    Text(statusText, fontSize = 10.sp, color = statusColor, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

// ─── DateRangePicker dialog ───────────────────────────────────────────────────
@SuppressLint("NonObservableLocale")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DateRangePickerDialog(
    onDismiss: () -> Unit,
    onConfirm: (Date, Date) -> Unit
) {
    val state = rememberDateRangePickerState()
    Dialog(onDismissRequest = onDismiss, properties = DialogProperties(usePlatformDefaultWidth = false)) {
        Surface(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp),
            shape = RoundedCornerShape(24.dp),
            color = CardWhite,
            tonalElevation = 4.dp
        ) {
            Column {
                DateRangePicker(
                    state = state,
                    title = {
                        Text(
                            "Chọn khoảng thời gian",
                            modifier = Modifier.padding(start = 20.dp, top = 16.dp),
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 16.sp,
                            color = TextPrimary
                        )
                    },
                    headline = {
                        val fmt = SimpleDateFormat("dd/MM/yyyy", LocalLocale.current.platformLocale)
                        val s = state.selectedStartDateMillis
                        val e = state.selectedEndDateMillis
                        val txt = when {
                            s != null && e != null -> "${fmt.format(Date(s))}  →  ${fmt.format(Date(e))}"
                            s != null -> "${fmt.format(Date(s))}  →  ?"
                            else -> "Chọn ngày bắt đầu"
                        }
                        Text(
                            txt,
                            modifier = Modifier.padding(start = 20.dp, bottom = 8.dp),
                            fontSize = 14.sp,
                            color = AccentBlue,
                            fontWeight = FontWeight.SemiBold
                        )
                    },
                    showModeToggle = true,
                    modifier = Modifier.weight(1f, fill = false)
                )

                Row(
                    modifier = Modifier.fillMaxWidth().padding(16.dp),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) { Text("Hủy", color = TextSecond) }
                    Spacer(Modifier.width(8.dp))
                    Button(
                        onClick = {
                            val s = state.selectedStartDateMillis
                            val e = state.selectedEndDateMillis
                            if (s != null && e != null) {
                                val endCal = Calendar.getInstance().apply {
                                    timeInMillis = e
                                    set(Calendar.HOUR_OF_DAY, 23)
                                    set(Calendar.MINUTE, 59)
                                    set(Calendar.SECOND, 59)
                                }
                                onConfirm(Date(s), endCal.time)
                            }
                        },
                        enabled = state.selectedStartDateMillis != null && state.selectedEndDateMillis != null,
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = AccentBlue)
                    ) { Text("Áp dụng", fontWeight = FontWeight.Bold) }
                }
            }
        }
    }
}

// ─── Empty / Error / Forbidden / Skeleton ────────────────────────────────────
@Composable
fun StatEmptyBookings() {
    Column(
        modifier = Modifier.fillMaxWidth().padding(40.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier.size(72.dp).clip(RoundedCornerShape(20.dp)).background(AppBg),
            contentAlignment = Alignment.Center
        ) {
            Icon(Icons.Default.Inbox, null, tint = Color(0xFFCBD5E1), modifier = Modifier.size(36.dp))
        }
        Spacer(Modifier.height(12.dp))
        Text("Chưa có giao dịch", fontWeight = FontWeight.Bold, color = TextSecond)
        Text("trong khoảng thời gian này", color = TextTertiary, fontSize = 13.sp)
    }
}

@Composable
fun StatAccessDenied(onBack: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize().padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier.size(88.dp).clip(RoundedCornerShape(24.dp)).background(AccentRed.copy(alpha = 0.1f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(Icons.Default.Lock, null, tint = AccentRed, modifier = Modifier.size(44.dp))
        }
        Spacer(Modifier.height(20.dp))
        Text("Không có quyền truy cập", fontSize = 18.sp, fontWeight = FontWeight.ExtraBold, color = TextPrimary)
        Spacer(Modifier.height(8.dp))
        Text("Chỉ Admin/Quản lý mới xem được.", textAlign = TextAlign.Center, color = TextSecond, fontSize = 14.sp)
        Spacer(Modifier.height(28.dp))
        Button(
            onClick = onBack,
            shape = RoundedCornerShape(14.dp),
            colors = ButtonDefaults.buttonColors(containerColor = DarkBg)
        ) { Text("Quay lại", fontWeight = FontWeight.Bold) }
    }
}

@Composable
fun StatError(msg: String, onRetry: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize().padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier.size(88.dp).clip(RoundedCornerShape(24.dp)).background(AccentRed.copy(alpha = 0.1f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(Icons.Default.ErrorOutline, null, tint = AccentRed, modifier = Modifier.size(44.dp))
        }
        Spacer(Modifier.height(16.dp))
        Text(msg, color = TextSecond, textAlign = TextAlign.Center, fontSize = 14.sp)
        Spacer(Modifier.height(20.dp))
        Button(
            onClick = onRetry,
            shape = RoundedCornerShape(14.dp),
            colors = ButtonDefaults.buttonColors(containerColor = AccentBlue)
        ) { Text("Thử lại", fontWeight = FontWeight.Bold) }
    }
}

@Composable
fun StatSkeleton() {
    val anim = rememberInfiniteTransition(label = "sk")
    val alpha by anim.animateFloat(
        initialValue = 0.3f, targetValue = 0.8f,
        animationSpec = infiniteRepeatable(tween(900), RepeatMode.Reverse), label = "a"
    )
    val skColor = Color(0xFFE2E8F0).copy(alpha = alpha)
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .offset(y = (-20).dp)
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Box(modifier = Modifier.fillMaxWidth().height(84.dp).clip(RoundedCornerShape(24.dp)).background(skColor))
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Box(modifier = Modifier.weight(1f).height(84.dp).clip(RoundedCornerShape(24.dp)).background(skColor))
            Box(modifier = Modifier.weight(1f).height(84.dp).clip(RoundedCornerShape(24.dp)).background(skColor))
        }
        Box(modifier = Modifier.fillMaxWidth().height(84.dp).clip(RoundedCornerShape(24.dp)).background(skColor))
        Box(modifier = Modifier.fillMaxWidth().height(220.dp).clip(RoundedCornerShape(24.dp)).background(skColor))
    }
}

// ─── Utils ────────────────────────────────────────────────────────────────────
fun formatDateDash(isoDate: String): String {
    return try {
        val parser = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault()).apply {
            timeZone = TimeZone.getTimeZone("UTC")
        }
        val date = parser.parse(isoDate) ?: return isoDate
        SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).format(date)
    } catch (e: Exception) { isoDate }
}
