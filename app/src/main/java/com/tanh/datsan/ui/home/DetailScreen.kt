package com.tanh.datsan.ui.home

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.runtime.getValue
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.ImageLoader
import coil.compose.AsyncImage
import coil.decode.SvgDecoder
import coil.request.ImageRequest
import com.tanh.datsan.BuildConfig
import com.tanh.datsan.data.model.FieldResponse
import com.tanh.datsan.data.model.Utility
import com.tanh.datsan.viewmodel.BookingUiState
import com.tanh.datsan.viewmodel.DetailUiState
import com.tanh.datsan.viewmodel.DetailViewModel
import java.time.Instant
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DetailScreen(
    fieldId: String,
    modifier: Modifier,
    viewModel: DetailViewModel = viewModel(),
    onBackClick: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val sheetState = rememberModalBottomSheetState()
    var showSheet by remember { mutableStateOf(false) }

    // Fetch dữ liệu từ NestJS
    LaunchedEffect(fieldId) { viewModel.fetchFieldDetail(fieldId) }

    Scaffold(
        bottomBar = {
            if (uiState is DetailUiState.Success) {
                Surface(shadowElevation = 8.dp) {
                    Button(
                        onClick = { showSheet = true },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                            .height(56.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2E7D32))
                    ) {
                        Text("CHỌN GIỜ ĐẶT SÂN", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    }
                }
            }
        }) { padding ->
        when (val state = uiState) {
            is DetailUiState.Loading -> {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = Color(0xFF2E7D32))
                }
            }

            is DetailUiState.Success -> {
                DetailContent(state.field, padding, onBackClick)
            }

            is DetailUiState.Error -> {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("Lỗi: ${state.message}", color = Color.Red)
                }
            }
        }
    }

    // BOTTOM SHEET: Xử lý Đặt sân theo thời gian thực
    if (showSheet && uiState is DetailUiState.Success) {
        val field = (uiState as DetailUiState.Success).field
        ModalBottomSheet(
            onDismissRequest = { showSheet = false },
            sheetState = sheetState
        ) {
            val uriHandler = LocalUriHandler.current
            BookingBottomSheetContent(
                field = field,
                onConfirm = { date, duration, time ->
                    showSheet = false

                    val isoStartTime = "${date}T${time}:00.000Z"

                    viewModel.createBooking(
                        fieldId = fieldId,
                        startTime = isoStartTime,
                        durationMinutes = duration
                    )
                }
            )

            val bookingState by viewModel.bookingState
            LaunchedEffect(bookingState) {
               if(bookingState is BookingUiState.Success){
                   val url = (bookingState as BookingUiState.Success).paymentUrl
                   uriHandler.openUri(url)
                   viewModel.resetBookingState()
               }
            }
        }
    }
}

@Composable
fun DetailContent(field: FieldResponse, padding: PaddingValues, onBackClick: () -> Unit) {
    LazyColumn(
        modifier = Modifier
            .padding(padding)
            .fillMaxSize()
    ) {
        // 1. Header: Ảnh bìa & Nút Back
        item {
            Box(
                Modifier
                    .fillMaxWidth()
                    .height(260.dp)
            ) {
                AsyncImage(
                    model = field.images?.find { it.isCover }?.imageUrl?.replace(
                        "localhost", BuildConfig.API_HOST
                    ) ?: field.images?.firstOrNull()?.imageUrl?.replace(
                        "localhost", BuildConfig.API_HOST
                    ),
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
                IconButton(
                    onClick = onBackClick,
                    modifier = Modifier
                        .padding(16.dp)
                        .background(Color.Black.copy(0.4f), CircleShape)
                ) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, null, tint = Color.White)
                }
            }
        }

        // 2. Thông tin cơ bản: Tên, Loại sân, Địa chỉ
        item {
            Column(Modifier.padding(16.dp)) {
                Text(
                    field.name,
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold
                )
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(top = 4.dp)
                ) {
                    SuggestionChip(onClick = {}, label = { Text(field.fieldType.name) })
                    Spacer(Modifier.width(8.dp))
                    Icon(Icons.Default.Star, null, Modifier.size(16.dp), tint = Color(0xFFFFB300))
                    Text(
                        "${field.averageRating ?: 0.0} (${field.reviewCount ?: 0} đánh giá)",
                        fontSize = 14.sp
                    )
                }
                field.branch.address?.let { addr ->
                    Text(
                        text = "${addr.street}, ${addr.ward?.name}, ${addr.city?.name}",
                        color = Color.Gray,
                        fontSize = 14.sp,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                }
            }
        }

        // 3. Tiện ích (Utilities - Lấy từ Database)
        item {
            Column(Modifier.padding(horizontal = 16.dp)) {
                Text("Tiện ích tại sân", fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(8.dp))
                FlowRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    field.utilities?.forEach { utility ->
                        UtilityItem(utility)
                    }
                }
            }
        }

        // 4. Mô tả
        item {
            Column(Modifier.padding(16.dp)) {
                Text("Giới thiệu", fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(8.dp))
                Text(
                    field.description ?: "Sân bóng chất lượng cao, phục vụ nhiệt tình.",
                    color = Color.DarkGray,
                    lineHeight = 22.sp
                )
                Spacer(Modifier.height(100.dp))
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun BookingBottomSheetContent(
    field: FieldResponse, onConfirm: (date: String, duration: Int, time: String) -> Unit
) {
    val quickDates = remember { getUpcomingDates() }
    val durations = listOf(60, 90, 120)

    var selectedDate by remember { mutableStateOf(quickDates[0]) }
    var selectedDuration by remember { mutableIntStateOf(durations[0]) }
    var selectedTime by remember { mutableStateOf<String?>(null) }
    var showDatePicker by remember { mutableStateOf(false) }
    val datePickerState = rememberDatePickerState()

    val timeSlots = remember(selectedDuration, selectedDate) {
        generateSlots(field.branch.openTime, field.branch.closeTime, selectedDuration)
    }

    Column(
        Modifier
            .padding(16.dp)
            .navigationBarsPadding()
            .verticalScroll(rememberScrollState())
    ) {
        Text(
            "Tùy chỉnh đặt sân",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold
        )

        // CHỌN NGÀY
        Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween, Alignment.CenterVertically) {
            Text("Ngày đá", fontWeight = FontWeight.Bold)
            IconButton(onClick = { showDatePicker = true }) {
                Icon(Icons.Default.DateRange, null, tint = Color(0xFF2E7D32))
            }
        }
        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            items(quickDates) { date ->
                FilterChip(
                    selected = selectedDate == date,
                    onClick = { selectedDate = date; selectedTime = null },
                    label = { Text(date.first) })
            }
        }

        // CHỌN THỜI LƯỢNG
        Text("Thời lượng (phút)", Modifier.padding(top = 16.dp), fontWeight = FontWeight.Bold)
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            durations.forEach { dur ->
                FilterChip(
                    selected = selectedDuration == dur,
                    onClick = { selectedDuration = dur; selectedTime = null },
                    label = { Text("$dur") })
            }
        }

        // CHỌN GIỜ
        Text("Giờ bắt đầu", Modifier.padding(top = 16.dp), fontWeight = FontWeight.Bold)
        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            timeSlots.forEach { time ->
                FilterChip(
                    selected = selectedTime == time,
                    onClick = { selectedTime = time },
                    label = { Text(time) })
            }
        }

        Spacer(Modifier.height(24.dp))
        Button(
            onClick = {
                selectedTime?.let {
                    onConfirm(
                        selectedDate.second, selectedDuration, it
                    )
                }
            },
            enabled = selectedTime != null,
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2E7D32))
        ) {
            Text("XÁC NHẬN", fontWeight = FontWeight.Bold)
        }
    }

    if (showDatePicker) {
        DatePickerDialog(onDismissRequest = { showDatePicker = false }, confirmButton = {
            TextButton(onClick = {
                datePickerState.selectedDateMillis?.let { millis ->
                    val date = Instant.ofEpochMilli(millis).atZone(ZoneId.of("UTC")).toLocalDate()
                    selectedDate = date.format(DateTimeFormatter.ofPattern("dd/MM")) to date.format(
                        DateTimeFormatter.ofPattern("yyyy-MM-dd")
                    )
                }
                showDatePicker = false
            }) { Text("CHỌN") }
        }) { DatePicker(state = datePickerState) }
    }
}

@Composable
fun UtilityItem(utility: Utility) {
    val context = LocalContext.current


    val imageLoader = remember {
        ImageLoader.Builder(context).components {
            add(SvgDecoder.Factory())
        }.build()
    }


    val fullIconUrl = utility.iconUrl?.let { path ->
        if (path.startsWith("http")) path
        else "${BuildConfig.API_HOST}$path"
    }

    Row(
        verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(vertical = 4.dp)
    ) {
        // 3. Hiển thị Icon dùng AsyncImage
        AsyncImage(
            model = ImageRequest.Builder(context).data(fullIconUrl).crossfade(true).build(),
            imageLoader = imageLoader, // Truyền cái loader đã cấu hình vào
            contentDescription = utility.name,
            modifier = Modifier.size(20.dp),
            // Ảnh hiện ra nếu link lỗi hoặc đang load
            error = painterResource(id = android.R.drawable.ic_menu_help),
            placeholder = painterResource(id = android.R.drawable.ic_menu_gallery)
        )

        Spacer(Modifier.width(8.dp))

        Text(
            text = utility.name, fontSize = 14.sp, color = Color.DarkGray
        )
    }
}

// --- HELPER FUNCTIONS ---
fun getUpcomingDates(): List<Pair<String, String>> {
    val today = LocalDate.now()
    return (0..6).map {
        val d = today.plusDays(it.toLong())
        val label = if (it == 0) "Hôm nay" else d.format(DateTimeFormatter.ofPattern("dd/MM"))
        label to d.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))
    }
}

fun generateSlots(open: String, close: String, dur: Int): List<String> {
    val slots = mutableListOf<String>()
    try {
        var curr = LocalTime.parse(open)
        val end = LocalTime.parse(close)
        while (curr.plusMinutes(dur.toLong())
                .isBefore(end) || curr.plusMinutes(dur.toLong()) == end
        ) {
            slots.add(curr.format(DateTimeFormatter.ofPattern("HH:mm")))
            curr = curr.plusMinutes(30)
        }
    } catch (e: Exception) {
        e.printStackTrace()
    }
    return slots
}