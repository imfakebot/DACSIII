package com.tanh.datsan.ui.home.booking

import androidx.compose.foundation.BorderStroke
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
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CloudDownload
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.QrCode
import androidx.compose.material.icons.filled.QrCode2
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.SportsSoccer
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
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
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.tanh.datsan.R
import com.tanh.datsan.ui.theme.BackgroundGray
import com.tanh.datsan.ui.theme.PrimaryGreen
import com.tanh.datsan.utils.DownloadHelper
import com.tanh.datsan.utils.NotificationHelper
import com.tanh.datsan.viewmodel.BookingReceiptUiState
import com.tanh.datsan.viewmodel.BookingSuccessViewModel
import kotlinx.coroutines.launch

@Composable
fun BookingSuccessScreen(
    bookingId: String,
    viewModel: BookingSuccessViewModel = hiltViewModel(),
    onNavigateHome: () -> Unit
) {
    val context = LocalContext.current
    val scrollState = rememberScrollState()

    val snackBarHostState = remember{ SnackbarHostState() }
    val scope = rememberCoroutineScope()

    val uiState by viewModel.uiState.collectAsState()
    val token by viewModel.tokenFlow.collectAsState()

    LaunchedEffect(bookingId) {
        viewModel.fetchBookingReceipt(bookingId)
    }

    LaunchedEffect(uiState) {
        if (uiState is BookingReceiptUiState.Success) {
            val booking = (uiState as BookingReceiptUiState.Success).booking
            NotificationHelper.showBookingSuccessNotification(
                context,
                bookingCode = booking.code ?: bookingId,
                fieldName = booking.field?.name ?: "Sân bóng"
            )
        }
    }


    Scaffold(
        snackbarHost = {
            SnackbarHost(snackBarHostState)
        }
    ) { paddingValue ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(BackgroundGray)
                .padding(paddingValue)
        ) {
            when (val state = uiState) {
                is BookingReceiptUiState.Loading -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            CircularProgressIndicator(color = PrimaryGreen)
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(text = stringResource(R.string.ticket_loading), color = Color.Gray)
                        }
                    }
                }

                is BookingReceiptUiState.Error -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                Icons.Filled.ErrorOutline,
                                contentDescription = stringResource(R.string.error)
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                stringResource(R.string.error_with_prefix, state.message),
                                color = Color.Red,
                                textAlign = TextAlign.Center
                            )
                            Spacer(modifier = Modifier.height(24.dp))
                            Button(
                                onClick = onNavigateHome,
                                colors = ButtonDefaults.buttonColors(containerColor = PrimaryGreen)
                            ) {
                                Text(stringResource(R.string.home))
                            }
                        }
                    }
                }

                is BookingReceiptUiState.Success -> {
                    val data = state.booking
                    Column(
                        modifier = Modifier.verticalScroll(scrollState),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Box(
                            modifier = Modifier
                                .size(90.dp)
                                .background(PrimaryGreen.copy(alpha = 0.1f), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Check,
                                contentDescription = null,
                                modifier = Modifier.size(56.dp)
                            )
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        Text(
                            text = stringResource(R.string.booking_success),
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.ExtraBold,
                            color = PrimaryGreen
                        )

                        Text(
                            text = stringResource(R.string.booking_success_ticket),
                            textAlign = TextAlign.Center,
                            color = Color.Gray,
                            modifier = Modifier.padding(16.dp, 8.dp),
                            fontSize = 14.sp
                        )

                        Spacer(Modifier.height(24.dp))

                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(20.dp),
                            colors = CardDefaults.cardColors(containerColor = Color.White),
                            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                        ) {
                            Column(modifier = Modifier.padding(20.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = stringResource(R.string.booking_in4),
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 16.sp
                                    )
                                    Surface(
                                        color = Color(0xFFE8F5E9),
                                        shape = RoundedCornerShape(50.dp)
                                    ) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                                        ) {
                                            Icon(
                                                Icons.Filled.CheckCircle,
                                                null,
                                                tint = PrimaryGreen,
                                                modifier = Modifier.size(14.dp)
                                            )
                                            Spacer(Modifier.width(4.dp))
                                            Text(
                                                stringResource(R.string.confirmed),
                                                color = PrimaryGreen,
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 11.sp
                                            )
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.height(16.dp))
                                HorizontalDivider(color = Color(0xFFF0F0F0))
                                Spacer(modifier = Modifier.height(16.dp))

                                SuccessInfoRow(
                                    icon = Icons.Filled.QrCode,
                                    label = stringResource(R.string.booking_code),
                                    value = data.code ?: stringResource(R.string.updating)
                                )
                                SuccessInfoRow(
                                    icon = Icons.Filled.SportsSoccer,
                                    label = stringResource(R.string.field),
                                    value = data.field?.name ?: stringResource(R.string.updating)
                                )
                                SuccessInfoRow(
                                    icon = Icons.Filled.Schedule,
                                    label = stringResource(R.string.time),
                                    value = "${data.startTime} - ${data.endTime}"
                                )
                                SuccessInfoRow(
                                    icon = Icons.Filled.Person,
                                    label = stringResource(R.string.customer),
                                    value = data.customerName ?: stringResource(R.string.customer)
                                )

                                Spacer(modifier = Modifier.height(8.dp))

                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .background(Color(0xFFF9FAFB), RoundedCornerShape(8.dp))
                                        .padding(12.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        stringResource(R.string.money_total),
                                        fontWeight = FontWeight.Medium,
                                        color = Color.Gray
                                    )
                                    Text(
                                        "${data.totalPrice} VNĐ",
                                        fontWeight = FontWeight.ExtraBold,
                                        color = Color(0xFFD32F2F),
                                        fontSize = 18.sp
                                    )
                                }

                                Spacer(modifier = Modifier.height(24.dp))

                                // QR Code
                                Column(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Text("Mã QR Check-in", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                    Spacer(Modifier.height(8.dp))
                                    Icon(
                                        imageVector = Icons.Filled.QrCode2,
                                        contentDescription = null,
                                        modifier = Modifier.size(140.dp),
                                        tint = Color.Black
                                    )
                                    Text(
                                        stringResource(R.string.check_ticket),
                                        fontSize = 12.sp,
                                        color = Color.Gray
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(24.dp))

                        val pdfLink = viewModel.getDownloadUrl(bookingId)

                        Button(
                            onClick = {
                                if (pdfLink.isNotBlank()) {
                                    if(token.isBlank()){
                                        scope.launch {
                                            snackBarHostState.showSnackbar("Phiên đăng nhập hết hạn")
                                        }
                                    }else{
                                        DownloadHelper.downLoadTicketPDF(context,url=pdfLink, bookingCode = data.code?:bookingId,token=token)
                                    }
                                } else {
                                    scope.launch {
                                        snackBarHostState.showSnackbar("Tính năng tải vé đang cập nhật!")
                                    }
                                }
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(52.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = PrimaryGreen),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(imageVector = Icons.Filled.CloudDownload, contentDescription = null)
                            Spacer(Modifier.width(8.dp))
                            Text(stringResource(R.string.download_pdf_ticket), fontWeight = FontWeight.Bold)
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        OutlinedButton(
                            onClick = onNavigateHome,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(52.dp),
                            shape = RoundedCornerShape(12.dp),
                            border = BorderStroke(1.dp, PrimaryGreen)
                        ) {
                            Text(
                                stringResource(R.string.home),
                                color = PrimaryGreen,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Spacer(modifier = Modifier.height(32.dp))

                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = Color(0xFFFFFBEB)),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        Icons.Filled.Info,
                                        null,
                                        tint = Color(0xFFB45309),
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Spacer(Modifier.width(8.dp))
                                    Text(
                                        stringResource(R.string.attention),
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFFB45309)
                                    )
                                }
                                Spacer(Modifier.height(8.dp))

                                val notes = stringResource(R.string.attention_content)
                                Row(modifier = Modifier.padding(vertical = 2.dp)) {
                                    Text(notes, color = Color(0xFFB45309), fontSize = 13.sp)
                                }

                            }
                        }
                        Spacer(modifier = Modifier.height(50.dp))
                    }

                }
            }
        }
    }
}


@Composable
fun SuccessInfoRow(icon: ImageVector, label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.Top
    ) {
        Icon(
            icon,
            contentDescription = null,
            tint = Color.LightGray,
            modifier = Modifier.size(18.dp)
        )
        Spacer(Modifier.width(12.dp))
        Column {
            Text(label, fontSize = 12.sp, color = Color.Gray)
            Text(value, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
        }
    }
}
