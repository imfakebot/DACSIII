package com.tanh.datsan.ui.home.booking

import android.annotation.SuppressLint
import android.content.Context
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CloudDownload
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.QrCode2
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.SportsSoccer
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.RoundRect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Outline
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.tanh.datsan.R
import com.tanh.datsan.ui.theme.PrimaryGreen
import com.tanh.datsan.utils.NotificationHelper
import com.tanh.datsan.viewmodel.BookingReceiptUiState

@SuppressLint("LocalContextGetResourceValueCall")
@Composable
fun BookingSuccessScreen(
    bookingId: String,
    uiState: BookingReceiptUiState,
    onFetchBookingReceipt: (String) -> Unit,
    onDownloadTicket: (Context, String, String) -> Unit,
    onNavigateHome: () -> Unit,
    onNavigateHistory: () -> Unit
) {
    val context = LocalContext.current
    val scrollState = rememberScrollState()
    val snackBarHostState = remember { SnackbarHostState() }

    LaunchedEffect(bookingId) {
        onFetchBookingReceipt(bookingId)
    }

    LaunchedEffect(uiState) {
        if (uiState is BookingReceiptUiState.Success) {
            val booking = uiState.booking
            NotificationHelper.showBookingSuccessNotification(
                context,
                bookingCode = booking.code ?: bookingId,
                fieldName = booking.field?.name ?: "Sân bóng"
            )
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackBarHostState) },
        containerColor = Color(0xFFF8FAFC)
    ) { paddingValue ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValue)
        ) {
            when (uiState) {
                is BookingReceiptUiState.Loading -> LoadingState()
                is BookingReceiptUiState.Error -> ErrorState(uiState.message, onNavigateHome)
                is BookingReceiptUiState.Success -> {
                    val data = uiState.booking
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(scrollState)
                            .padding(horizontal = 24.dp, vertical = 32.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        SuccessAnimatedHeader()

                        Spacer(modifier = Modifier.height(32.dp))

                        TicketContainer(
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(24.dp)) {
                                TicketHeaderSection(data.code ?: bookingId)

                                Spacer(modifier = Modifier.height(20.dp))
                                HorizontalDivider(color = Color(0xFFE2E8F0))
                                Spacer(modifier = Modifier.height(20.dp))

                                TicketDetailSection(
                                    fieldName = data.field?.name
                                        ?: stringResource(R.string.updating),
                                    time = "${data.startTime} - ${data.endTime}",
                                    customer = data.customerName
                                        ?: stringResource(R.string.customer)
                                )
                            }

                            TicketDashedDivider()

                            Column(modifier = Modifier.padding(24.dp)) {
                                TicketPriceSection(data.totalPrice.toString())

                                Spacer(modifier = Modifier.height(24.dp))

                                TicketQRCodeSection()
                            }
                        }

                        Spacer(modifier = Modifier.height(32.dp))

                        ActionButtons(
                            onDownload = {
                                onDownloadTicket(context, bookingId, data.code ?: bookingId)
                            },
                            onNavigateHome = onNavigateHome,
                            onNavigateHistory = onNavigateHistory
                        )

                        Spacer(modifier = Modifier.height(24.dp))

                        AttentionCard()

                        Spacer(modifier = Modifier.height(48.dp))
                    }
                }
            }
        }
    }
}

@Composable
fun SuccessAnimatedHeader() {
    val scale = remember { Animatable(0f) }
    LaunchedEffect(Unit) {
        scale.animateTo(
            targetValue = 1f,
            animationSpec = spring(
                dampingRatio = Spring.DampingRatioMediumBouncy,
                stiffness = Spring.StiffnessLow
            )
        )
    }

    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Box(
            modifier = Modifier
                .size(80.dp)
                .graphicsLayer(scaleX = scale.value, scaleY = scale.value)
                .background(PrimaryGreen.copy(alpha = 0.1f), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Filled.CheckCircle,
                contentDescription = null,
                modifier = Modifier.size(60.dp),
                tint = PrimaryGreen
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = stringResource(R.string.booking_success),
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.ExtraBold,
            color = Color(0xFF0F172A)
        )
        Text(
            text = stringResource(R.string.booking_success_ticket),
            style = MaterialTheme.typography.bodyMedium,
            color = Color(0xFF64748B),
            textAlign = TextAlign.Center
        )
    }
}

@Composable
fun TicketContainer(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    Surface(
        modifier = modifier
            .shadow(
                elevation = 12.dp,
                shape = TicketShape(24.dp, 12.dp),
                ambientColor = Color.Black.copy(alpha = 0.1f),
                spotColor = Color.Black.copy(alpha = 0.2f)
            ),
        shape = TicketShape(24.dp, 12.dp),
        color = Color.White
    ) {
        Column { content() }
    }
}

@Composable
fun TicketHeaderSection(bookingCode: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Text(
                stringResource(R.string.booking_code).uppercase(),
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF94A3B8),
                letterSpacing = 1.sp
            )
            Text(
                text = bookingCode,
                fontSize = 18.sp,
                fontWeight = FontWeight.ExtraBold,
                color = Color(0xFF1E293B)
            )
        }

        Surface(
            color = Color(0xFFF1F5F9),
            shape = RoundedCornerShape(8.dp)
        ) {
            Text(
                stringResource(R.string.confirmed).uppercase(),
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                fontSize = 10.sp,
                fontWeight = FontWeight.Black,
                color = Color(0xFF475569)
            )
        }
    }
}

@Composable
fun TicketDetailSection(fieldName: String, time: String, customer: String) {
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        TicketInfoRow(Icons.Default.SportsSoccer, stringResource(R.string.field), fieldName)
        TicketInfoRow(Icons.Default.Schedule, stringResource(R.string.time), time)
        TicketInfoRow(Icons.Default.Person, stringResource(R.string.customer), customer)
    }
}

@Composable
fun TicketInfoRow(icon: ImageVector, label: String, value: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(32.dp)
                .background(Color(0xFFF1F5F9), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, null, modifier = Modifier.size(16.dp), tint = Color(0xFF64748B))
        }
        Spacer(modifier = Modifier.width(12.dp))
        Column {
            Text(label, fontSize = 11.sp, color = Color(0xFF94A3B8), fontWeight = FontWeight.Medium)
            Text(value, fontSize = 14.sp, color = Color(0xFF1E293B), fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
fun TicketDashedDivider() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxWidth()) {
            val path = Path().apply {
                moveTo(32f, size.height / 2)
                lineTo(size.width - 32f, size.height / 2)
            }
            drawPath(
                path = path,
                color = Color(0xFFE2E8F0),
                style = Stroke(
                    width = 2.dp.toPx(),
                    pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 10f), 0f)
                )
            )
        }
    }
}

@Composable
fun TicketPriceSection(price: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFFF8FAFC), RoundedCornerShape(12.dp))
            .padding(16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            stringResource(R.string.money_total),
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF64748B)
        )
        Text(
            "$price VNĐ",
            fontSize = 20.sp,
            fontWeight = FontWeight.Black,
            color = PrimaryGreen
        )
    }
}

@Composable
fun TicketQRCodeSection() {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Surface(
            modifier = Modifier
                .size(160.dp)
                .padding(8.dp),
            shape = RoundedCornerShape(16.dp),
            color = Color.White,
            border = BorderStroke(1.dp, Color(0xFFE2E8F0))
        ) {
            Icon(
                imageVector = Icons.Filled.QrCode2,
                contentDescription = null,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(12.dp),
                tint = Color(0xFF0F172A)
            )
        }
        Spacer(modifier = Modifier.height(12.dp))
        Text(
            stringResource(R.string.check_ticket),
            fontSize = 12.sp,
            color = Color(0xFF94A3B8),
            textAlign = TextAlign.Center
        )
    }
}

@Composable
fun ActionButtons(
    onDownload: () -> Unit,
    onNavigateHome: () -> Unit,
    onNavigateHistory: () -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Button(
            onClick = onDownload,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0F172A)),
            shape = RoundedCornerShape(16.dp)
        ) {
            Icon(Icons.Default.CloudDownload, null)
            Spacer(Modifier.width(8.dp))
            Text(stringResource(R.string.download_pdf_ticket), fontWeight = FontWeight.Bold)
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            OutlinedButton(
                onClick = onNavigateHome,
                modifier = Modifier
                    .weight(1f)
                    .height(56.dp),
                shape = RoundedCornerShape(16.dp),
                border = BorderStroke(1.dp, Color(0xFFE2E8F0))
            ) {
                Text(
                    stringResource(R.string.home),
                    color = Color(0xFF475569),
                    fontWeight = FontWeight.Bold
                )
            }
            OutlinedButton(
                onClick = onNavigateHistory,
                modifier = Modifier
                    .weight(1f)
                    .height(56.dp),
                shape = RoundedCornerShape(16.dp),
                border = BorderStroke(1.dp, Color(0xFFE2E8F0))
            ) {
                Text("Lịch sử", color = Color(0xFF475569), fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
fun AttentionCard() {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = Color(0xFFFFF7ED),
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.dp, Color(0xFFFED7AA))
    ) {
        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.Top) {
            Icon(
                Icons.Default.Info,
                null,
                tint = Color(0xFFEA580C),
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(
                    stringResource(R.string.attention),
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF9A3412),
                    fontSize = 14.sp
                )
                Text(
                    stringResource(R.string.attention_content),
                    color = Color(0xFFC2410C),
                    fontSize = 13.sp,
                    lineHeight = 18.sp
                )
            }
        }
    }
}

@Composable
fun LoadingState() {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            CircularProgressIndicator(color = PrimaryGreen)
            Spacer(modifier = Modifier.height(16.dp))
            Text(stringResource(R.string.ticket_loading), color = Color(0xFF64748B))
        }
    }
}

@Composable
fun ErrorState(message: String, onHome: () -> Unit) {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(32.dp)
        ) {
            Icon(
                Icons.Default.ErrorOutline,
                null,
                tint = Color.Red,
                modifier = Modifier.size(48.dp)
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text("Đã có lỗi xảy ra", fontWeight = FontWeight.Bold, fontSize = 18.sp)
            Text(message, textAlign = TextAlign.Center, color = Color.Gray)
            Spacer(modifier = Modifier.height(24.dp))
            Button(
                onClick = onHome,
                colors = ButtonDefaults.buttonColors(containerColor = PrimaryGreen)
            ) {
                Text(stringResource(R.string.home))
            }
        }
    }
}

class TicketShape(private val cornerRadius: Dp, private val holeRadius: Dp) : Shape {
    override fun createOutline(
        size: Size,
        layoutDirection: LayoutDirection,
        density: Density
    ): Outline {
        return Outline.Generic(Path().apply {
            val cornerRadiusPx = with(density) { cornerRadius.toPx() }
            val holeRadiusPx = with(density) { holeRadius.toPx() }

            addRoundRect(
                RoundRect(
                    rect = Rect(0f, 0f, size.width, size.height),
                    cornerRadius = CornerRadius(cornerRadiusPx)
                )
            )

            // Holes should align with the TicketDashedDivider which is roughly at a specific ratio
            // For a better match, we can calculate based on the content or use a fixed ratio.
            // In the implementation, TicketDashedDivider is between two Column sections.
            // Let's assume a ratio of ~0.55-0.6 based on typical content.
            val holeY = size.height * 0.54f

            addOval(
                Rect(
                    left = -holeRadiusPx,
                    top = holeY - holeRadiusPx,
                    right = holeRadiusPx,
                    bottom = holeY + holeRadiusPx
                )
            )

            addOval(
                Rect(
                    left = size.width - holeRadiusPx,
                    top = holeY - holeRadiusPx,
                    right = size.width + holeRadiusPx,
                    bottom = holeY + holeRadiusPx
                )
            )
        })
    }
}
