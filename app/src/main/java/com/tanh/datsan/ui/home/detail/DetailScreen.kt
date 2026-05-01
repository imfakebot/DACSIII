package com.tanh.datsan.ui.home.detail

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.tanh.datsan.R
import com.tanh.datsan.data.model.FieldResponse
import com.tanh.datsan.ui.component.FieldImageSlider
import com.tanh.datsan.ui.component.RatingAndLocation
import com.tanh.datsan.ui.component.UtilityItem
import com.tanh.datsan.utils.toFullImageUrl
import com.tanh.datsan.viewmodel.BookingUiState
import com.tanh.datsan.viewmodel.DetailUiState
import com.tanh.datsan.viewmodel.DetailViewModel

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
    val isLoggedIn by viewModel.isLoggedIn.collectAsState()
    val bookingState by viewModel.bookingState

    val sheetState = rememberModalBottomSheetState()
    var showSheet by remember { mutableStateOf(false) }
    val uriHandler = LocalUriHandler.current

    // Gọi API lấy dữ liệu khi vào màn hình
    LaunchedEffect(fieldId) { viewModel.fetchFieldDetail(fieldId) }

    Scaffold(
        bottomBar = {
            if (uiState is DetailUiState.Success) {
                BookingBottomBar(
                    onClick = { if (isLoggedIn) showSheet = true else onNavigateToLogin() }
                )
            }
        }
    ) { padding ->
        when (val state = uiState) {
            is DetailUiState.Loading -> LoadingState()
            is DetailUiState.Error -> ErrorState(state.message)
            is DetailUiState.Success -> {
                DetailContent(
                    field = state.field,
                    padding = padding,
                    onBackClick = onBackClick,
                    onNavigateToReview = onNavigateToReview
                )
            }
        }
    }

    // Modal chọn giờ đặt sân
    if (showSheet && uiState is DetailUiState.Success) {
        val field = (uiState as DetailUiState.Success).field
        ModalBottomSheet(
            onDismissRequest = { showSheet = false },
            sheetState = sheetState
        ) {
            BookingBottomSheetContent(
                field = field,
                viewModel = viewModel,
                onConfirm = { date, duration, time ->
                    showSheet = false
                    viewModel.createBooking(fieldId, "${date}T${time}:00+07:00", duration)
                }
            )
        }
    }

    // Xử lý mở link thanh toán VNPAY sau khi tạo booking thành công
    LaunchedEffect(bookingState) {
        if (bookingState is BookingUiState.Success) {
            val url = (bookingState as BookingUiState.Success).paymentUrl
            uriHandler.openUri(url)
            viewModel.resetBookingState()
        }
    }
}

@Composable
fun DetailContent(
    field: FieldResponse,
    padding: PaddingValues,
    onBackClick: () -> Unit,
    onNavigateToReview: (String) -> Unit
) {
    val lazyListState = rememberLazyListState()
    val primaryColor = Color(0xFF2E7D32)

    Box(modifier = Modifier
        .fillMaxSize()
        .background(Color(0xFFF4F7F6))
        .padding(padding)) {

        //  ẢNH SLIDER + HIỆU ỨNG PARALLAX
        val imageUrls = remember(field) {
            field.images?.map { it.imageUrl.toFullImageUrl() } ?: emptyList()
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(320.dp)
                .graphicsLayer {
                    // Parallax: ảnh chạy chậm hơn nội dung cuộn
                    translationY = if (lazyListState.firstVisibleItemIndex == 0) {
                        lazyListState.firstVisibleItemScrollOffset * 0.5f
                    } else 0f
                }
        ) {
            if (imageUrls.isNotEmpty()) {
                FieldImageSlider(images = imageUrls)
            } else {
                Box(Modifier
                    .fillMaxSize()
                    .background(Color.LightGray))
            }

            // Gradient tối để nút quay lại và text nổi bật hơn
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            listOf(
                                Color.Black.copy(0.4f),
                                Color.Transparent,
                                Color.Black.copy(0.6f)
                            )
                        )
                    )
            )
        }

        //  NỘI DUNG CHI TIẾT SÂN
        LazyColumn(state = lazyListState, modifier = Modifier.fillMaxSize()) {
            item { Spacer(Modifier.height(280.dp)) } // Tạo khoảng trống để thấy ảnh

            item {
                Surface(
                    modifier = Modifier.fillParentMaxHeight(),
                    shape = RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp),
                    color = Color.White,
                    shadowElevation = 8.dp
                ) {
                    Column(Modifier.padding(24.dp)) {
                        FieldBadge(field.fieldType.name, primaryColor)

                        Text(
                            text = field.name,
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.ExtraBold,
                            modifier = Modifier.padding(vertical = 8.dp)
                        )

                        RatingAndLocation(
                            rating = field.averageRating?:0f,
                            reviewCount = field.reviewCount?:0,
                            address= "${field.branch.address?.street},${field.branch.address?.ward?.name}, ${field.branch.address?.city?.name}",
                            tint = primaryColor
                        )

                        SectionDivider()

                        // Danh sách tiện ích (Wifi, Gửi xe...)
                        Text(
                            stringResource(R.string.field_amenities_title),
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp
                        )
                        FlowRow(
                            modifier = Modifier
                                .padding(vertical = 16.dp)
                                .fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            field.utilities?.forEach { UtilityItem(it) }
                        }

                        SectionDivider()

                        // Mô tả chi tiết
                        Text(
                            stringResource(R.string.detail_tab_intro),
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp
                        )
                        Text(
                            text = field.description
                                ?: stringResource(R.string.detail_no_description),
                            color = Color(0xFF4B5563),
                            modifier = Modifier.padding(vertical = 12.dp),
                            lineHeight = 24.sp
                        )

                        SectionDivider()

                        // Phần đánh giá khách hàng
                        ReviewHeader(field.id, onNavigateToReview, primaryColor)
                        ReviewList(field.reviews)

                        Spacer(Modifier.height(100.dp)) // Padding cho BottomBar
                    }
                }
            }
        }

        //  NÚT QUAY LẠI (Nổi trên cùng)
        IconButton(
            onClick = onBackClick,
            modifier = Modifier
                .padding(top = 40.dp, start = 16.dp)
                .size(48.dp)
                .background(Color.White.copy(0.25f), CircleShape)
                .clip(CircleShape)
        ) {
            Icon(Icons.AutoMirrored.Filled.ArrowBack, null, tint = Color.White)
        }
    }
}
