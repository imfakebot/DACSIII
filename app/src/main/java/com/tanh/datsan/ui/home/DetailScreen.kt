package com.tanh.datsan.ui.home

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.rounded.Star
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.style.TextAlign
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
import com.tanh.datsan.utils.FormatReviewTime
import java.time.Instant
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DetailScreen(
    fieldId: String,
    viewModel: DetailViewModel = viewModel(),
    onBackClick: () -> Unit,
    onNavigateToReview: (String) -> Unit,
    onNavigateToLogin: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val sheetState = rememberModalBottomSheetState()
    var showSheet by remember { mutableStateOf(false) }

    val isLoggedIn by viewModel.isLoggedIn.collectAsState()

    val uriHandler = LocalUriHandler.current
    val bookingState by viewModel.bookingState

    // Fetch dữ liệu từ NestJS
    LaunchedEffect(fieldId) { viewModel.fetchFieldDetail(fieldId) }

    Scaffold(
        bottomBar = {
            if (uiState is DetailUiState.Success) {
                Surface(shadowElevation = 8.dp) {
                    Button(
                        onClick = {
                            if (isLoggedIn) {
                                showSheet = true
                            }else{
                                onNavigateToLogin()
                            }
                        },
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
                DetailContent(state.field, padding, onBackClick, onNavigateToReview)
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

            LaunchedEffect(bookingState) {
                if (bookingState is BookingUiState.Success) {
                    val url = (bookingState as BookingUiState.Success).paymentUrl
                    uriHandler.openUri(url)
                    viewModel.resetBookingState()
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun DetailContent(
    field: FieldResponse,
    padding: PaddingValues,
    onBackClick: () -> Unit,
    onNavigateToReview: (String) -> Unit
) {
    // Trạng thái cuộn để làm hiệu ứng Parallax (3D)
    val lazyListState = rememberLazyListState()

    // Màu chủ đạo thể thao
    val primarySportColor = Color(0xFF2E7D32) // Xanh lục đậm
    val surfaceBg = Color(0xFFF4F7F6) // Xám nhạt pha xanh

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(surfaceBg)
            .padding(padding)
    ) {
        // 1. ẢNH BÌA PARALLAX (Nằm dưới cùng)
        var imageUrl =
            field.images?.find { it.isCover }?.imageUrl ?: field.images?.firstOrNull()?.imageUrl
        imageUrl = imageUrl?.replace("localhost", BuildConfig.API_HOST) ?: ""

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(320.dp) // Cho ảnh cao lên để làm Parallax
                .graphicsLayer {
                    // PHÉP THUẬT PARALLAX: Kéo ảnh chạy chậm hơn nội dung 50%
                    translationY = if (lazyListState.firstVisibleItemIndex == 0) {
                        lazyListState.firstVisibleItemScrollOffset * 0.5f
                    } else 0f
                }
        ) {
            AsyncImage(
                model = imageUrl,
                contentDescription = "Ảnh bìa sân bóng",
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )

            // Lớp sương mù Gradient từ dưới lên để làm dịu phần chuyển giao
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        brush = Brush.verticalGradient(
                            colors = listOf(
                                Color.Black.copy(alpha = 0.4f), // Đen mờ ở trên cho Nút Back nổi bật
                                Color.Transparent,
                                Color.Black.copy(alpha = 0.6f)  // Đen đậm ở dưới để tôn phần viền bo tròn
                            )
                        )
                    )
            )
        }

        // 2. NỘI DUNG CUỘN (Nằm đè lên trên)
        LazyColumn(
            state = lazyListState,
            modifier = Modifier.fillMaxSize()
        ) {
            // Spacer trong suốt để đẩy phần nội dung xuống dưới, hở ảnh bìa ra
            item {
                Spacer(modifier = Modifier.height(260.dp))
            }

            // Khối nội dung chính (Màu trắng, bo góc to)
            item {
                Surface(
                    modifier = Modifier.fillParentMaxHeight(), // Trải dài phần còn lại của màn hình
                    shape = RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp),
                    color = Color.White,
                    shadowElevation = 8.dp
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(24.dp)
                    ) {
                        // Badge Loại sân
                        Surface(
                            shape = CircleShape,
                            color = primarySportColor.copy(alpha = 0.1f)
                        ) {
                            Text(
                                text = field.fieldType.name.uppercase(),
                                color = primarySportColor,
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp,
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                            )
                        }

                        Spacer(Modifier.height(12.dp))

                        // Tên sân
                        Text(
                            text = field.name,
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color(0xFF111827), // Đen than vjp
                            lineHeight = 36.sp
                        )

                        Spacer(Modifier.height(12.dp))

                        // Review & Địa chỉ
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                Icons.Rounded.Star,
                                null,
                                tint = Color(0xFFFFC107),
                                modifier = Modifier.size(22.dp)
                            )
                            Spacer(Modifier.width(4.dp))
                            Text(
                                text = "${field.averageRating ?: 0.0}",
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp,
                                color = Color(0xFF111827)
                            )
                            Text(
                                text = " (${field.reviewCount ?: 0} đánh giá)",
                                fontSize = 14.sp,
                                color = Color.Gray
                            )
                        }

                        Spacer(Modifier.height(8.dp))

                        Row(verticalAlignment = Alignment.Top) {
                            Icon(
                                Icons.Default.LocationOn,
                                null,
                                tint = primarySportColor,
                                modifier = Modifier
                                    .size(20.dp)
                                    .offset(y = 2.dp)
                            )
                            Spacer(Modifier.width(8.dp))
                            field.branch.address?.let { addr ->
                                Text(
                                    text = "${addr.street}, ${addr.ward?.name}, ${addr.city?.name}",
                                    color = Color(0xFF4B5563),
                                    fontSize = 15.sp,
                                    lineHeight = 22.sp
                                )
                            }
                        }

                        Spacer(Modifier.height(24.dp))
                        HorizontalDivider(color = surfaceBg, thickness = 2.dp)
                        Spacer(Modifier.height(24.dp))

                        // Tiện ích
                        Text(
                            "Tiện ích tại sân",
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp,
                            color = Color(0xFF111827)
                        )
                        Spacer(Modifier.height(16.dp))
                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            field.utilities?.forEach { utility ->
                                UtilityItem(utility)
                            }
                        }

                        Spacer(Modifier.height(24.dp))
                        HorizontalDivider(color = surfaceBg, thickness = 2.dp)
                        Spacer(Modifier.height(24.dp))

                        // Giới thiệu
                        Text(
                            "Giới thiệu",
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp,
                            color = Color(0xFF111827)
                        )
                        Spacer(Modifier.height(12.dp))
                        Text(
                            text = field.description ?: "Chưa có thông tin mô tả cho sân bóng này.",
                            color = Color(0xFF4B5563),
                            fontSize = 15.sp,
                            lineHeight = 24.sp
                        )

                        Spacer(Modifier.height(24.dp))
                        HorizontalDivider(color = surfaceBg, thickness = 2.dp)
                        Spacer(Modifier.height(24.dp))

                        // 5. Đánh giá & Bình luận
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                "Đánh giá & Bình luận",
                                fontWeight = FontWeight.Bold,
                                fontSize = 18.sp,
                                color = Color(0xFF111827)
                            )
                            TextButton(
                                onClick = {
                                    onNavigateToReview(field.id)
                                }
                            ) {
                                Text(
                                    "Xem tất cả",
                                    color = primarySportColor,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        Spacer(Modifier.height(12.dp))

                        if (field.reviews.isNullOrEmpty()) {
                            // Trạng thái: Chưa có đánh giá
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(Color(0xFFF9FAFB), RoundedCornerShape(12.dp))
                                    .padding(32.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Icon(
                                        Icons.Rounded.Star,
                                        contentDescription = null,
                                        tint = Color.LightGray,
                                        modifier = Modifier.size(48.dp)
                                    )
                                    Spacer(Modifier.height(8.dp))
                                    Text(
                                        text = "Chưa có đánh giá nào.\nHãy là người đầu tiên trải nghiệm!",
                                        color = Color.Gray,
                                        fontSize = 14.sp,
                                        textAlign = TextAlign.Center
                                    )
                                }
                            }
                        } else {
                            // Trạng thái: Đã có đánh giá -> Rút data từ API ra xài
                            Column(
                                modifier = Modifier.fillMaxWidth(),
                                verticalArrangement = Arrangement.spacedBy(16.dp)
                            ) {
                                // Chỉ lấy tối đa 3 comment mới nhất để show ở màn hình này thôi
                                field.reviews.take(3).forEachIndexed { index, review ->
                                    ReviewItem(
                                        userName = review.user?.fullName ?: "Khách hàng",
                                        rating = review.rating,
                                        date = FormatReviewTime(review.createdAt),
                                        comment = review.comment ?: "",
                                        avatarUrl = review.user?.avatarUrl
                                    )

                                    // Kẻ chỉ phân cách giữa các bình luận
                                    if (index < field.reviews.take(3).size - 1) {
                                        HorizontalDivider(
                                            color = Color(0xFFF3F4F6),
                                            thickness = 1.dp
                                        )
                                    }
                                }
                            }
                        }
                    }

                    Spacer(Modifier.height(100.dp)) // Hở chỗ cho BottomSheet / Nút đặt sân
                }
            }
        }
    }

    // 3. NÚT QUAY LẠI (Luôn nổi ở trên cùng góc trái)
    IconButton(
        onClick = onBackClick,
        modifier = Modifier
            .padding(top = 40.dp, start = 16.dp)
            .size(48.dp)
            .background(Color.White.copy(alpha = 0.25f), CircleShape) // Nền kính mờ
            .clip(CircleShape)
    ) {
        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
    }
}

@Composable
fun UtilityItem(utility: Utility) {
    val context = LocalContext.current
    val imageLoader = remember {
        ImageLoader.Builder(context).components { add(SvgDecoder.Factory()) }.build()
    }

    val fullIconUrl = utility.iconUrl?.let { path ->
        if (path.startsWith("http")) path else "${BuildConfig.API_HOST}$path"
    }

    Surface(
        color = Color(0xFFF9FAFB), // Xám cực nhạt
        shape = RoundedCornerShape(12.dp),
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            Color(0xFFE5E7EB)
        ) // Viền mỏng tinh tế
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp)
        ) {
            AsyncImage(
                model = ImageRequest.Builder(context).data(fullIconUrl).crossfade(true).build(),
                imageLoader = imageLoader,
                contentDescription = utility.name,
                modifier = Modifier.size(20.dp),
                error = painterResource(id = android.R.drawable.ic_menu_agenda), // Icon thay thế mặc định nhìn đỡ lỗi hơn
                placeholder = painterResource(id = android.R.drawable.ic_menu_gallery)
            )
            Spacer(Modifier.width(8.dp))
            Text(
                text = utility.name,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                color = Color(0xFF374151)
            )
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

@Composable
fun ReviewItem(
    userName: String,
    rating: Int,
    date: String,
    comment: String,
    avatarUrl: String? = null
) {
    val context = LocalContext.current

    Row(modifier = Modifier.fillMaxWidth()) {
        // 1. Avatar (Có ảnh thì load, không có thì lấy chữ cái đầu)
        Box(
            modifier = Modifier
                .size(44.dp)
                .clip(CircleShape)
                .background(Color(0xFFE5E7EB)),
            contentAlignment = Alignment.Center
        ) {
            if (!avatarUrl.isNullOrEmpty()) {
                val fullAvatarUrl =
                    if (avatarUrl.startsWith("http")) avatarUrl else "${BuildConfig.API_HOST}$avatarUrl"
                AsyncImage(
                    model = ImageRequest.Builder(context).data(fullAvatarUrl).crossfade(true)
                        .build(),
                    contentDescription = "Avatar",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
            } else {
                Text(
                    text = userName.take(1).uppercase(),
                    color = Color(0xFF6B7280),
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                )
            }
        }

        Spacer(modifier = Modifier.width(12.dp))

        // 2. Nội dung Comment
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = userName,
                fontWeight = FontWeight.Bold,
                fontSize = 15.sp,
                color = Color(0xFF111827)
            )

            // Vẽ số ngôi sao
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(vertical = 4.dp)
            ) {
                repeat(5) { index ->
                    Icon(
                        Icons.Rounded.Star,
                        contentDescription = null,
                        tint = if (index < rating) Color(0xFFFFC107) else Color(0xFFE5E7EB),
                        modifier = Modifier.size(16.dp)
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
                Text(text = date, fontSize = 12.sp, color = Color.Gray)
            }

            Text(
                text = comment,
                fontSize = 14.sp,
                color = Color(0xFF4B5563),
                lineHeight = 22.sp
            )
        }
    }
}