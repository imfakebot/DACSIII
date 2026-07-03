package com.tanh.datsan.ui.home.detail

import android.annotation.SuppressLint
import android.content.ContextWrapper
import android.content.Intent
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ChatBubble
import androidx.compose.material.icons.rounded.LocationOn
import androidx.compose.material.icons.rounded.Star
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.VerticalDivider
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.util.Consumer
import com.tanh.datsan.R
import com.tanh.datsan.data.model.CheckPriceResponseDto
import com.tanh.datsan.data.model.CreateBookingDto
import com.tanh.datsan.data.model.FieldResponse
import com.tanh.datsan.data.model.Voucher
import com.tanh.datsan.ui.component.ErrorStateScreen
import com.tanh.datsan.ui.component.FieldImageSlider
import com.tanh.datsan.ui.component.FullScreenImageViewer
import com.tanh.datsan.ui.component.LoadingStateScreen
import com.tanh.datsan.ui.component.UtilityItem
import com.tanh.datsan.ui.component.FullScreenImageViewer
import com.tanh.datsan.ui.component.UtilityItem
import com.tanh.datsan.utils.OpenVNPay
import com.tanh.datsan.utils.toFullImageUrl
import com.tanh.datsan.ui.home.detail.BookingUiState
import com.tanh.datsan.ui.home.detail.DetailUiState
import kotlinx.coroutines.launch
import java.util.Locale

@SuppressLint("RememberReturnType", "LocalContextGetResourceValueCall")
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
    discountAmount: Double,
    isVoucherLoading: Boolean,
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
                    Toast.makeText(
                        context,
                        context.getString(R.string.detail_payment_failed),
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
        }
        activity?.addOnNewIntentListener(intentListener)
        activity?.intent?.let { intent ->
            if (intent.data != null) {
                intentListener.accept(intent)
            }
        }
        onDispose { activity?.removeOnNewIntentListener(intentListener) }
    }

    LaunchedEffect(bookingState) {
        if (bookingState is BookingUiState.Success) {
            val url = bookingState.paymentUrl
            OpenVNPay.openVnPay(context, url)
        } else if (bookingState is BookingUiState.Error) {
            coroutineScope.launch {
                snackbarHostState.showSnackbar(bookingState.message ?: context.getString(R.string.error_unknown))
            }
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
    ) { paddingValues ->
        when (val state = uiState) {
            is DetailUiState.Loading -> LoadingStateScreen()
            is DetailUiState.Error -> ErrorStateScreen(state.message ?: stringResource(R.string.error_unknown), onBackClick)
            is DetailUiState.Success -> {
                DetailContent(
                    field = state.field,
                    padding = paddingValues,
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
        val field = uiState.field
        ModalBottomSheet(
            onDismissRequest = { showSheet = false },
            sheetState = sheetState,
            containerColor = Color.White,
            dragHandle = { BottomSheetDefaults.DragHandle(color = Color(0xFFE2E8F0)) }
        ) {
            BookingBottomSheetContent(
                field = field,
                priceState = priceState,
                bookedSlots = bookedSlots,
                vouchers = vouchers,
                selectedVoucher = selectedVoucher,
                discountAmount = discountAmount,
                isVoucherLoading = isVoucherLoading,
                onFetchBookedSlots = { date -> onFetchBookedSlots(fieldId, date) },
                onCheckPrice = { startTime, duration ->
                    onCheckPrice(
                        fieldId,
                        startTime,
                        duration
                    )
                },
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
            contentPadding = PaddingValues(bottom = padding.calculateBottomPadding() + 24.dp)
        ) {
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

                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                Brush.verticalGradient(
                                    listOf(
                                        Color.Black.copy(0.3f),
                                        Color.Transparent,
                                        Color.Black.copy(0.5f)
                                    )
                                )
                            )
                    )
                }
            }

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
                                    modifier = Modifier.padding(
                                        horizontal = 12.dp,
                                        vertical = 6.dp
                                    ),
                                    color = accentColor,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp
                                )
                            }

                            field.distance?.let { dist ->
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        Icons.Rounded.LocationOn,
                                        null,
                                        tint = Color(0xFF10B981),
                                        modifier = Modifier.size(16.dp)
                                    )
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

                        Row(verticalAlignment = Alignment.Top) {
                            Icon(
                                Icons.Rounded.LocationOn,
                                null,
                                tint = secondaryTextColor,
                                modifier = Modifier
                                    .size(18.dp)
                                    .padding(top = 2.dp)
                                )
                            Spacer(Modifier.width(8.dp))
                            val ward = field.branch.address?.wardName
                                ?: field.branch.address?.ward?.name ?: ""
                            val city = field.branch.address?.cityName
                                ?: field.branch.address?.city?.name ?: ""
                            val street = field.branch.address?.street ?: ""
                            val fullAddress =
                                listOf(street, ward, city).filter { it.isNotBlank() }
                                    .joinToString(", ")
                            Text(
                                text = fullAddress.ifBlank { stringResource(R.string.error_unknown_address) },
                                color = secondaryTextColor,
                                fontSize = 15.sp,
                                lineHeight = 22.sp
                            )
                        }

                        Spacer(Modifier.height(24.dp))

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
                                InfoItem(
                                    label = stringResource(R.string.detail_label_rating),
                                    value = "${field.averageRating ?: 0.0}",
                                    icon = Icons.Rounded.Star,
                                    iconColor = Color(0xFFFFD700)
                                )
                                VerticalDivider(
                                    modifier = Modifier.height(30.dp),
                                    color = Color(0xFFE2E8F0)
                                )
                                InfoItem(
                                    label = stringResource(R.string.detail_label_reviews),
                                    value = "${field.reviewCount ?: 0}",
                                    icon = Icons.Default.ChatBubble,
                                    iconColor = accentColor
                                )
                            }
                        }

                        if (field.branch.address?.latitude != null && field.branch.address.longitude != null) {
                            Spacer(Modifier.height(16.dp))
                            DirectionButton(
                                lat = field.branch.address.latitude,
                                lng = field.branch.address.longitude,
                                tenSan = field.name,
                                primaryColor = accentColor,
                                onShowMessage = onShowSnackbar
                            )
                        }

                        SectionDivider()

                        Text(
                            stringResource(R.string.detail_section_amenities),
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 20.sp,
                            color = Color(0xFF0F172A)
                        )
                        FlowRow(
                            modifier = Modifier
                                .padding(vertical = 20.dp)
                                .fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            field.utilities?.forEach { UtilityItem(it) }
                        }

                        SectionDivider()

                        Text(
                            stringResource(R.string.detail_section_description),
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 20.sp,
                            color = Color(0xFF0F172A)
                        )
                        Text(
                            text = field.description
                                ?: stringResource(R.string.detail_default_description),
                            color = Color(0xFF475569),
                            modifier = Modifier.padding(vertical = 12.dp),
                            lineHeight = 24.sp,
                            fontSize = 16.sp
                        )

                        SectionDivider()

                        ReviewHeader(
                            fieldId = field.id,
                            reviewCount = field.reviewCount ?: 0,
                            rating = field.averageRating ?: 0f,
                            onNavigate = onNavigateToReview,
                            color = accentColor
                        )
                        ReviewList(reviews = field.reviews)
                    }
                }
            }
        }

        Surface(
            modifier = Modifier
                .padding(top = 12.dp, start = 20.dp)
                .size(48.dp)
                .clickable { onBackClick() },
            color = Color.Black.copy(alpha = 0.3f),
            shape = CircleShape,
            border = BorderStroke(1.dp, Color.White.copy(alpha = 0.2f))
        ) {
            Icon(
                Icons.AutoMirrored.Filled.ArrowBack,
                null,
                tint = Color.White,
                modifier = Modifier.padding(12.dp)
            )
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
fun InfoItem(
    label: String,
    value: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    iconColor: Color
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(icon, null, tint = iconColor, modifier = Modifier.size(18.dp))
            Spacer(Modifier.width(6.dp))
            Text(
                text = value,
                fontWeight = FontWeight.Bold,
                fontSize = 17.sp,
                color = Color(0xFF0F172A)
            )
        }
        Text(
            text = label,
            fontSize = 12.sp,
            color = Color(0xFF64748B),
            fontWeight = FontWeight.Medium
        )
    }
}
