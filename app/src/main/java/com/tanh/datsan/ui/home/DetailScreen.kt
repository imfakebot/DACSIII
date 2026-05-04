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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
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
import com.tanh.datsan.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DetailScreen(
    fieldId: String,
    viewModel: DetailViewModel = hiltViewModel(),
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
                        Text(text = stringResource(R.string.booking_select_time_title), fontWeight = FontWeight.Bold, fontSize = 16.sp)
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
                    state.message?.let { Text(text= stringResource(R.string.error_with_prefix, it), color = Color.Red) }
                }
            }
        }
    }

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
    val lazyListState = rememberLazyListState()
    val primarySportColor = Color(0xFF2E7D32)
    val surfaceBg = Color(0xFFF4F7F6)

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(surfaceBg)
            .padding(padding)
    ) {
        // Sửa lỗi Nullable
        var imageUrl = field.images?.find { it.isCover }?.imageUrl ?: field.images?.firstOrNull()?.imageUrl ?: ""
        val host = BuildConfig.API_BASE_URL.removePrefix("http://").removeSuffix("/")
        imageUrl = imageUrl.replace("localhost", host)

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(320.dp)
                .graphicsLayer {
                    translationY = if (lazyListState.firstVisibleItemIndex == 0) {
                        lazyListState.firstVisibleItemScrollOffset * 0.5f
                    } else 0f
                }
        ) {
            AsyncImage(
                model = imageUrl,
                contentDescription = stringResource(R.string.cd_field_cover),
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        brush = Brush.verticalGradient(
                            colors = listOf(
                                Color.Black.copy(alpha = 0.4f),
                                Color.Transparent,
                                Color.Black.copy(alpha = 0.6f)
                            )
                        )
                    )
            )
        }

        LazyColumn(
            state = lazyListState,
            modifier = Modifier.fillMaxSize()
        ) {
            item { Spacer(modifier = Modifier.height(260.dp)) }

            item {
                Surface(
                    modifier = Modifier.fillParentMaxHeight(),
                    shape = RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp),
                    color = Color.White,
                    shadowElevation = 8.dp
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(24.dp)
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = primarySportColor.copy(alpha = 0.1f)
                        ) {
                            // Sửa lỗi Nullable
                            Text(
                                text = field.fieldType?.name?.uppercase() ?: "KHÔNG XÁC ĐỊNH",
                                color = primarySportColor,
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp,
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                            )
                        }

                        Spacer(Modifier.height(12.dp))

                        // Sửa lỗi Nullable
                        Text(
                            text = field.name ?: "Chưa có tên sân",
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color(0xFF111827),
                            lineHeight = 36.sp
                        )

                        Spacer(Modifier.height(12.dp))

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
                                text = stringResource(R.string.review_count_suffix, field.reviewCount ?: 0),
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

                            // Sửa lỗi Nullable calls
                            field.branch?.address?.let { addr ->
                                Text(
                                    text = "${addr.street ?: ""}, ${addr.ward?.name ?: ""}, ${addr.city?.name ?: ""}",
                                    color = Color(0xFF4B5563),
                                    fontSize = 15.sp,
                                    lineHeight = 22.sp
                                )
                            }
                        }

                        Spacer(Modifier.height(24.dp))
                        HorizontalDivider(color = surfaceBg, thickness = 2.dp)
                        Spacer(Modifier.height(24.dp))

                        Text(
                            stringResource(R.string.field_amenities_title),
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

                        Text(
                            stringResource(R.string.detail_tab_intro),
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp,
                            color = Color(0xFF111827)
                        )
                        Spacer(Modifier.height(12.dp))
                        Text(
                            text = field.description ?: stringResource(R.string.detail_no_description),
                            color = Color(0xFF4B5563),
                            fontSize = 15.sp,
                            lineHeight = 24.sp
                        )

                        Spacer(Modifier.height(24.dp))
                        HorizontalDivider(color = surfaceBg, thickness = 2.dp)
                        Spacer(Modifier.height(24.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                stringResource(R.string.detail_tab_reviews),
                                fontWeight = FontWeight.Bold,
                                fontSize = 18.sp,
                                color = Color(0xFF111827)
                            )
                            TextButton(
                                onClick = {
                                    // Sửa lỗi gọi thuộc tính null
                                    field.id?.let { onNavigateToReview(it) }
                                }
                            ) {
                                Text(
                                    stringResource(R.string.btn_view_all),
                                    color = primarySportColor,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        Spacer(Modifier.height(12.dp))

                        if (field.reviews.isNullOrEmpty()) {
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
                                        text = stringResource(R.string.review_empty_msg),
                                        color = Color.Gray,
                                        fontSize = 14.sp,
                                        textAlign = TextAlign.Center
                                    )
                                }
                            }
                        } else {
                            Column(
                                modifier = Modifier.fillMaxWidth(),
                                verticalArrangement = Arrangement.spacedBy(16.dp)
                            ) {
                                field.reviews.take(3).forEachIndexed { index, review ->
                                    ReviewItem(
                                        userName = review.user?.fullName ?: stringResource(R.string.review_user_fallback),
                                        rating = review.rating,
                                        date = FormatReviewTime(review.createdAt),
                                        comment = review.comment ?: "",
                                        avatarUrl = review.user?.avatarUrl
                                    )

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

                    Spacer(Modifier.height(100.dp))
                }
            }
        }
    }

    IconButton(
        onClick = onBackClick,
        modifier = Modifier
            .padding(top = 40.dp, start = 16.dp)
            .size(48.dp)
            .background(Color.White.copy(alpha = 0.25f), CircleShape)
            .clip(CircleShape)
    ) {
        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.btn_back), tint = Color.White)
    }
}

@Composable
fun UtilityItem(utility: Utility) {
    val context = LocalContext.current
    val imageLoader = remember {
        ImageLoader.Builder(context).components { add(SvgDecoder.Factory()) }.build()
    }

    val host = BuildConfig.API_BASE_URL.removePrefix("http://").removeSuffix("/")
    val fullIconUrl = utility.iconUrl?.let { path ->
        if (path.startsWith("http")) path else "http://$host$path"
    }

    Surface(
        color = Color(0xFFF9FAFB),
        shape = RoundedCornerShape(12.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE5E7EB))
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
                error = painterResource(id = android.R.drawable.ic_menu_agenda),
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
    val todayLabel = stringResource(R.string.date_today)
    val quickDates = remember { getUpcomingDates(todayLabel) }
    val durations = listOf(60, 90, 120)

    var selectedDate by remember { mutableStateOf(quickDates[0]) }
    var selectedDuration by remember { mutableIntStateOf(durations[0]) }
    var selectedTime by remember { mutableStateOf<String?>(null) }
    var showDatePicker by remember { mutableStateOf(false) }
    val datePickerState = rememberDatePickerState()

    // Cập nhật lại thời gian theo cấu trúc Branch mới
    val timeSlots = remember(selectedDuration, selectedDate) {
        val open = field.branch?.openTime ?: "00:00"
        val close = field.branch?.closeTime ?: "23:59"
        generateSlots(open, close, selectedDuration)
    }

    Column(
        Modifier
            .padding(16.dp)
            .navigationBarsPadding()
            .verticalScroll(rememberScrollState())
    ) {
        Text(
            stringResource(R.string.booking_customize_title),
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold
        )

        Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween, Alignment.CenterVertically) {
            Text(stringResource(R.string.booking_date_label), fontWeight = FontWeight.Bold)
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

        Text(stringResource(R.string.booking_duration_label), Modifier.padding(top = 16.dp), fontWeight = FontWeight.Bold)
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            durations.forEach { dur ->
                FilterChip(
                    selected = selectedDuration == dur,
                    onClick = { selectedDuration = dur; selectedTime = null },
                    label = { Text("$dur") })
            }
        }

        Text(stringResource(R.string.booking_start_time_label), Modifier.padding(top = 16.dp), fontWeight = FontWeight.Bold)
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
            Text(stringResource(R.string.btn_confirm_caps), fontWeight = FontWeight.Bold)
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
            }) { Text(stringResource(R.string.btn_select_caps)) }
        }) { DatePicker(state = datePickerState) }
    }
}

fun getUpcomingDates(todayLabel: String): List<Pair<String, String>> {
    val today = LocalDate.now()
    return (0..6).map {
        val d = today.plusDays(it.toLong())
        val label = if (it == 0) todayLabel else d.format(DateTimeFormatter.ofPattern("dd/MM"))
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
        Box(
            modifier = Modifier
                .size(44.dp)
                .clip(CircleShape)
                .background(Color(0xFFE5E7EB)),
            contentAlignment = Alignment.Center
        ) {
            if (!avatarUrl.isNullOrEmpty()) {
                val host = BuildConfig.API_BASE_URL.removePrefix("http://").removeSuffix("/")
                val fullAvatarUrl =
                    if (avatarUrl.startsWith("http")) avatarUrl else "http://$host$avatarUrl"
                AsyncImage(
                    model = ImageRequest.Builder(context).data(fullAvatarUrl).crossfade(true)
                        .build(),
                    contentDescription = stringResource(R.string.cd_user_avatar),
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

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = userName,
                fontWeight = FontWeight.Bold,
                fontSize = 15.sp,
                color = Color(0xFF111827)
            )

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