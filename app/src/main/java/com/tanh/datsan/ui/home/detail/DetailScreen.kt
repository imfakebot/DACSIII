package com.tanh.datsan.ui.home.detail

import android.annotation.SuppressLint
import android.content.ContextWrapper
import android.content.Intent
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ChatBubble
import androidx.compose.material.icons.filled.Directions
import androidx.compose.material.icons.rounded.LocationOn
import androidx.compose.material.icons.rounded.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
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
import com.tanh.datsan.data.model.CheckPriceResponseDto
import com.tanh.datsan.data.model.Voucher
import com.tanh.datsan.data.model.CreateBookingDto
import com.tanh.datsan.ui.component.*
import com.tanh.datsan.utils.OpenVNPay
import com.tanh.datsan.utils.toFullImageUrl
import com.tanh.datsan.viewmodel.*
import kotlinx.coroutines.launch
import java.util.Locale

@SuppressLint("RememberReturnType")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DetailScreen(
    fieldId: String,
    uiState: DetailUiState,
    bookingState: BookingUiState,
    priceState: CheckPriceResponseDto?,
    bookedSlots: List<String>,
    vouchers: List<Voucher>,
    selectedVoucher: Voucher?,
    isLoggedIn: Boolean,
    onFetchFieldDetail: (String) -> Unit,
    onFetchBookedSlots: (String, String) -> Unit,
    onCheckPrice: (String, String, Int) -> Unit,
    onFetchAvailableVouchers: (Double) -> Unit,
    onCreateBooking: (CreateBookingDto) -> Unit,
    onSelectVoucher: (Voucher?, Double) -> Unit,
    onBackClick: () -> Unit,
    onNavigateToReview: (String) -> Unit,
    onNavigateToLogin: () -> Unit,
    onNavigateToSuccess: (String) -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var showSheet by remember { mutableStateOf(false) }
    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()

    // Gọi API lấy dữ liệu khi vào màn hình
    LaunchedEffect(fieldId) { onFetchFieldDetail(fieldId) }

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
            if (uri != null && uri.scheme == "dacsii" && uri.host == "payment") {
                val path = uri.path
                val bookingId = uri.getQueryParameter("bookingId")
                if (path?.contains("payment-success") == true) {
                    onNavigateToSuccess(bookingId ?: "UNKNOWN")
                } else if (path?.contains("payment-failed") == true) {
                    Toast.makeText(context, "Thanh toán thất bại!", Toast.LENGTH_LONG).show()
                }
            }
        }
        activity?.addOnNewIntentListener(intentListener)
        if (activity?.intent?.data != null) {
            intentListener.accept(activity.intent)
        }
        onDispose { activity?.removeOnNewIntentListener(intentListener) }
    }

    LaunchedEffect(bookingState) {
        if (bookingState is BookingUiState.Success) {
            val url = (bookingState as BookingUiState.Success).paymentUrl
            OpenVNPay.openVnPay(context, url)
        }
    }

    LaunchedEffect(priceState) {
        priceState?.pricing?.totalPrice?.let {
            onFetchAvailableVouchers(it)
        }
    }

    Scaffold(
        containerColor = Color.White,
        snackbarHost = { SnackbarHost(snackbarHostState) },
        bottomBar = {
            AnimatedVisibility(
                visible = uiState is DetailUiState.Success,
                enter = slideInVertically(initialOffsetY = { it }),
                exit = slideOutVertically(targetOffsetY = { it })
            ) {
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

    if (showSheet && uiState is DetailUiState.Success) {
        val field = (uiState as DetailUiState.Success).field
        ModalBottomSheet(
            onDismissRequest = { showSheet = false },
            sheetState = sheetState,
            containerColor = Color.White,
            dragHandle = { BottomSheetDefaults.DragHandle(color = Color(0xFFE2E8F0)) }
        ) {
            val discountAmount = 0.0 // To do: calculate or pass down
            
            BookingBottomSheetContent(
                field = field,
                priceState = priceState,
                bookedSlots = bookedSlots,
                vouchers = vouchers,
                selectedVoucher = selectedVoucher,
                discountAmount = discountAmount,
                onFetchBookedSlots = { date -> onFetchBookedSlots(fieldId, date) },
                onCheckPrice = { startTime, duration -> onCheckPrice(fieldId, startTime, duration) },
                onSelectVoucher = onSelectVoucher,
                onConfirm = { date, duration, time ->
                    showSheet = false
                    val startTimeIso = "${date}T${time}:00+07:00"
                    onCreateBooking(
                        CreateBookingDto(
                            fieldId = fieldId,
                            startTime = startTimeIso,
                            durationMinutes = duration,
                            voucherCode = selectedVoucher?.code
                        )
                    )
                }
            )
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun DetailContent(
    field: FieldResponse,
    padding: PaddingValues,
    onBackClick: () -> Unit,
    onNavigateToReview: (String) -> Unit,
    onShowSnackbar: (String) -> Unit
) {
    val lazyListState = rememberLazyListState()
    val surfaceColor = Color.White
    val secondaryTextColor = Color(0xFF64748B)
    val accentColor = Color(0xFF3B82F6)

    var showImageViewer by remember { mutableStateOf(false) }
    var clickedImageIndex by remember { mutableIntStateOf(0) }

    Box(modifier = Modifier.fillMaxSize()) {
        val imageUrls = remember(field) {
            field.images?.map { it.imageUrl.toFullImageUrl() } ?: emptyList()
        }

        LazyColumn(
            state = lazyListState,
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(bottom = 100.dp)
        ) {
            // 1. IMMERSIVE IMAGE HEADER
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(360.dp)
                        .graphicsLayer {
                            val scrollOffset = if (lazyListState.firstVisibleItemIndex == 0) {
                                lazyListState.firstVisibleItemScrollOffset.toFloat()
                            } else 500f
                            translationY = scrollOffset * 0.5f
                            alpha = (1f - (scrollOffset / 800f)).coerceIn(0f, 1f)
                        }
                ) {
                    if (imageUrls.isNotEmpty()) {
                        FieldImageSlider(
                            images = imageUrls,
                            onImageClick = { index ->
                                clickedImageIndex = index
                                showImageViewer = true
                            }
                        )
                    }
                    
                    // Dark Gradient Overlay
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                Brush.verticalGradient(
                                    listOf(Color.Black.copy(0.3f), Color.Transparent, Color.Black.copy(0.5f))
                                )
                            )
                    )
                }
            }

            // 2. MAIN CONTENT CARD
            item {
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .offset(y = (-40).dp),
                    shape = RoundedCornerShape(topStart = 40.dp, topEnd = 40.dp),
                    color = surfaceColor,
                    shadowElevation = 0.dp 
                ) {
                    Column(Modifier.padding(horizontal = 24.dp, vertical = 32.dp)) {
                        
                        // Category & Distance Row
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Surface(
                                color = accentColor.copy(alpha = 0.1f),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Text(
                                    text = field.fieldType.name,
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                    color = accentColor,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp
                                )
                            }
                            
                            field.distance?.let { dist ->
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Rounded.LocationOn, null, tint = Color(0xFF10B981), modifier = Modifier.size(16.dp))
                                    Spacer(Modifier.width(4.dp))
                                    Text(
                                        text = "${String.format(Locale.US, "%.1f", dist)} km",
                                        color = Color(0xFF10B981),
                                        fontWeight = FontWeight.ExtraBold,
                                        fontSize = 15.sp
                                    )
                                }
                            }
                        }

                        Text(
                            text = field.name,
                            style = MaterialTheme.typography.headlineLarge,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color(0xFF0F172A),
                            modifier = Modifier.padding(top = 16.dp, bottom = 8.dp),
                            lineHeight = 38.sp
                        )

                        // Address Row
                        Row(verticalAlignment = Alignment.Top) {
                            Icon(Icons.Rounded.LocationOn, null, tint = secondaryTextColor, modifier = Modifier.size(18.dp).padding(top = 2.dp))
                            Spacer(Modifier.width(8.dp))
                            val ward = field.branch.address?.wardName ?: field.branch.address?.ward?.name ?: ""
                            val city = field.branch.address?.cityName ?: field.branch.address?.city?.name ?: ""
                            val street = field.branch.address?.street ?: ""
                            val fullAddress = listOf(street, ward, city).filter { it.isNotBlank() }.joinToString(", ")
                            Text(
                                text = fullAddress.ifBlank { "Địa chỉ không xác định" },
                                color = secondaryTextColor,
                                fontSize = 15.sp,
                                lineHeight = 22.sp
                            )
                        }

                        Spacer(Modifier.height(24.dp))

                        // Quick Info Surface
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .border(1.dp, Color(0xFFE2E8F0), RoundedCornerShape(24.dp)),
                            color = Color(0xFFF8FAFC),
                            shape = RoundedCornerShape(24.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(20.dp),
                                horizontalArrangement = Arrangement.SpaceAround,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                InfoItem(label = "Đánh giá", value = "${field.averageRating ?: 0.0}", icon = Icons.Rounded.Star, iconColor = Color(0xFFFFD700))
                                VerticalDivider(modifier = Modifier.height(30.dp), color = Color(0xFFE2E8F0))
                                InfoItem(label = "Nhận xét", value = "${field.reviewCount ?: 0}", icon = Icons.Default.ChatBubble, iconColor = accentColor)
                                if (field.branch.address?.latitude != null && field.branch.address.longitude != null) {
                                    VerticalDivider(modifier = Modifier.height(30.dp), color = Color(0xFFE2E8F0))
                                    DirectionIconButton(
                                        lat = field.branch.address.latitude,
                                        lng = field.branch.address.longitude,
                                        tenSan = field.name,
                                        onShowMessage = onShowSnackbar
                                    )
                                }
                            }
                        }

                        SectionDivider()

                        // Amenities Section
                        Text("Tiện ích của sân", fontWeight = FontWeight.ExtraBold, fontSize = 20.sp, color = Color(0xFF0F172A))
                        FlowRow(
                            modifier = Modifier.padding(vertical = 20.dp).fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            field.utilities?.forEach { UtilityItem(it) }
                        }

                        SectionDivider()

                        // Description Section
                        Text("Giới thiệu", fontWeight = FontWeight.ExtraBold, fontSize = 20.sp, color = Color(0xFF0F172A))
                        Text(
                            text = field.description ?: "Chưa có mô tả cho sân này.",
                            color = Color(0xFF475569),
                            modifier = Modifier.padding(vertical = 12.dp),
                            lineHeight = 24.sp,
                            fontSize = 16.sp
                        )

                        SectionDivider()

                        // Reviews Section
                        ReviewHeader(
                            fieldId = field.id,
                            reviewCount = field.reviewCount ?: 0,
                            rating = field.averageRating ?: 0f,
                            onNavigate = onNavigateToReview,
                            color = accentColor
                        )
                        ReviewList(field.reviews)
                    }
                }
            }
        }

        // Floating Back Button (Glassmorphism)
        Surface(
            modifier = Modifier
                .padding(top = 48.dp, start = 20.dp)
                .size(48.dp)
                .clickable { onBackClick() },
            color = Color.Black.copy(alpha = 0.3f),
            shape = CircleShape,
            border = BorderStroke(1.dp, Color.White.copy(alpha = 0.2f))
        ) {
            Icon(Icons.AutoMirrored.Filled.ArrowBack, null, tint = Color.White, modifier = Modifier.padding(12.dp))
        }

        if (showImageViewer) {
            FullScreenImageViewer(
                imageUrls = imageUrls,
                initialIndex = clickedImageIndex,
                onDismiss = { showImageViewer = false }
            )
        }
    }
}

@Composable
fun InfoItem(label: String, value: String, icon: androidx.compose.ui.graphics.vector.ImageVector, iconColor: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(icon, null, tint = iconColor, modifier = Modifier.size(18.dp))
            Spacer(Modifier.width(6.dp))
            Text(text = value, fontWeight = FontWeight.Bold, fontSize = 17.sp, color = Color(0xFF0F172A))
        }
        Text(text = label, fontSize = 12.sp, color = Color(0xFF64748B), fontWeight = FontWeight.Medium)
    }
}

@Composable
fun DirectionIconButton(lat: Double, lng: Double, tenSan: String, onShowMessage: (String) -> Unit) {
    val context = LocalContext.current
    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.clickable {
        try {
            val uri = "google.navigation:q=$lat,$lng"
            val intent = Intent(Intent.ACTION_VIEW, android.net.Uri.parse(uri))
            intent.setPackage("com.google.android.apps.maps")
            context.startActivity(intent)
        } catch (e: Exception) {
            onShowMessage("Vui lòng cài đặt Google Maps")
        }
    }) {
        Icon(Icons.Default.Directions, null, tint = Color(0xFF3B82F6), modifier = Modifier.size(24.dp))
        Text(text = "Chỉ đường", fontSize = 12.sp, color = Color(0xFF3B82F6), fontWeight = FontWeight.Bold)
    }
}
