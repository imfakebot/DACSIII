package com.tanh.datsan.ui.home.detail

import android.annotation.SuppressLint
import android.content.ContextWrapper
import androidx.activity.ComponentActivity
import android.content.Intent
import android.util.Log
import android.widget.Toast
import androidx.browser.customtabs.CustomTabsIntent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.rounded.LocationOn
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.util.Consumer
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
import java.util.Locale
import androidx.core.net.toUri
import kotlinx.coroutines.launch

@SuppressLint("RememberReturnType")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DetailScreen(
    fieldId: String,
    viewModel: DetailViewModel = hiltViewModel(),
    onBackClick: () -> Unit,
    onNavigateToReview: (String) -> Unit,
    onNavigateToLogin: () -> Unit,
    onNavigateToSuccess: (String) -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val isLoggedIn by viewModel.isLoggedIn.collectAsState()
    val bookingState by viewModel.bookingState


    val sheetState = rememberModalBottomSheetState()
    var showSheet by remember { mutableStateOf(false) }
    val uriHandler = LocalUriHandler.current

    val selectedVoucher by viewModel.selectedVoucher.collectAsState()
    val discountAmount by viewModel.discountAmount.collectAsState()
    val voucherList by viewModel.voucher.collectAsState()

    val currentNavigateSuccess by rememberUpdatedState(onNavigateToSuccess)

    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()

    // Gọi API lấy dữ liệu khi vào màn hình
    LaunchedEffect(fieldId) { viewModel.fetchFieldDetail(fieldId) }

    val context = LocalContext.current
    val activity = remember(context) {
        var ctx = context
        while (ctx is ContextWrapper) {
            if (ctx is ComponentActivity) break
            ctx = ctx.baseContext
        }
        ctx as? ComponentActivity
    }

    DisposableEffect(activity) {
        val intentListener = Consumer<Intent> { intent ->
            val uri = intent.data
            Log.d("DetailScreen", "Received intent with URI: $uri")

            if (uri != null && uri.scheme == "dacsii" && uri.host == "payment") {
                val path = uri.path
                val bookingId = uri.getQueryParameter("bookingId")
                val code = uri.getQueryParameter("code")
                if (path?.contains("payment-success") == true) {
                    onNavigateToSuccess(bookingId ?: "UNKNOWN")
                } else if (path?.contains("payment-failed") == true) {
                    Toast.makeText(context, "Thanh toán thất bại!", Toast.LENGTH_LONG).show()
                }
            }
        }
        // Đăng ký bắt link mới nếu App đang nằm ngầm
        activity?.addOnNewIntentListener(intentListener)
        // Bắt link ngay lập tức nếu App vừa được gọi dậy
        if (activity?.intent?.data != null) {
            intentListener.accept(activity.intent)
        }
        onDispose { activity?.removeOnNewIntentListener(intentListener) }
    }

    LaunchedEffect(bookingState) {
        if (bookingState is BookingUiState.Success) {
            val url = (bookingState as BookingUiState.Success).paymentUrl
            viewModel.resetBookingState()


            try {
                val builder = CustomTabsIntent.Builder()
                val customTabsIntent = builder.build()
                customTabsIntent.launchUrl(context, url.toUri())
            } catch (_: Exception) {
                val intent = Intent(Intent.ACTION_VIEW, url.toUri())
                context.startActivity(intent)
            }
        }
    }



    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
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
                    onNavigateToReview = onNavigateToReview,
                    onShowSnackbar = { message ->
                        coroutineScope.launch {
                            snackbarHostState.showSnackbar(message)
                        }
                    }
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
                selectedVoucherCode = selectedVoucher?.code,
                discountAmount = discountAmount,
                onOpenVoucherList = voucherList,
                onConfirm = { date, duration, time ->
                    showSheet = false
                    viewModel.createBooking(fieldId, "${date}T${time}:00+07:00", duration)
                }
            )
        }
    }
}

@Composable
fun DetailContent(
    field: FieldResponse,
    padding: PaddingValues,
    onBackClick: () -> Unit,
    onNavigateToReview: (String) -> Unit,
    onShowSnackbar: (String) -> Unit
) {
    val lazyListState = rememberLazyListState()
    val primaryColor = Color(0xFF2E7D32)

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF4F7F6))
            .padding(padding)
    ) {

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
                Box(
                    Modifier
                        .fillMaxSize()
                        .background(Color.LightGray)
                )
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
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp),
                    color = Color.White,
                    shadowElevation = 8.dp
                ) {
                    Column(Modifier.padding(24.dp)) {
                        FieldBadge(field.fieldType.name, primaryColor)

                        field.distance.let { dist ->
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Rounded.LocationOn,
                                    contentDescription = null,
                                    tint = primaryColor,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "${String.format(Locale.US, "%.1f", dist)} km",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = primaryColor,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        Text(
                            text = field.name,
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.ExtraBold,
                            modifier = Modifier.padding(vertical = 8.dp)
                        )

                        RatingAndLocation(
                            rating = field.averageRating ?: 0f,
                            reviewCount = field.reviewCount ?: 0,
                            address = "${field.branch.address?.street},${field.branch.address?.ward?.name}, ${field.branch.address?.city?.name}",
                            tint = primaryColor
                        )

                        if (field.branch.address?.latitude != null && field.branch.address.longitude != null) {
                            Spacer(Modifier.height(16.dp))
                            DirectionButton(
                                lat = field.branch.address.latitude,
                                lng = field.branch.address.longitude,
                                primaryColor = primaryColor,
                                onShowMessage = onShowSnackbar,
                                tenSan = field.name
                            )
                        }

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

