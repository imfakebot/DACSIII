package com.tanh.datsan.ui.home.history

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Star
import androidx.compose.material3.*
import androidx.compose.material3.SecondaryScrollableTabRow
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.res.stringResource
import com.tanh.datsan.data.model.BookingResponse
import com.tanh.datsan.ui.state.BookingHistoryUiState
import com.tanh.datsan.R
import com.tanh.datsan.utils.DateUtil.formatDateDash
import java.text.NumberFormat
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BookingHistoryScreen(
    bookings: List<BookingResponse>,
    uiState: BookingHistoryUiState,
    currentStatus: String?,
    onFetchBookings: (String?) -> Unit,
    onCancelBooking: (String) -> Unit,
    onWriteReview: (bookingId: String, fieldId: String, fieldName: String) -> Unit = { _, _, _ -> }
) {
    val tabs = listOf(
        Pair(null, stringResource(id = R.string.history_tab_all)),
        Pair("pending", stringResource(id = R.string.history_tab_pending)),
        Pair("confirmed", stringResource(id = R.string.history_tab_confirmed)),
        Pair("checked_in", stringResource(id = R.string.history_tab_checked_in)),
        Pair("finished", stringResource(id = R.string.history_tab_finished)),
        Pair("cancelled", stringResource(id = R.string.history_tab_cancelled))
    )

    val selectedTabIndex = tabs.indexOfFirst { it.first == currentStatus }.takeIf { it >= 0 } ?: 0

    LaunchedEffect(currentStatus) {
        onFetchBookings(currentStatus)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(id = R.string.history_title)) },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            SecondaryScrollableTabRow(
                selectedTabIndex = selectedTabIndex,
                modifier = Modifier.fillMaxWidth(),
                scrollState = rememberScrollState(),
                tabs = {
                    tabs.forEachIndexed { index, tab ->
                        Tab(
                            selected = selectedTabIndex == index,
                            onClick = { onFetchBookings(tab.first) },
                            text = { Text(tab.second) }
                        )
                    }
                }
            )

            Box(modifier = Modifier.fillMaxSize()) {
                if (uiState is BookingHistoryUiState.Loading && bookings.isEmpty()) {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                } else if (bookings.isEmpty()) {
                    Text(
                        text = stringResource(id = R.string.history_empty),
                        modifier = Modifier.align(Alignment.Center)
                    )
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(bookings) { booking ->
                            BookingItem(
                                booking = booking,
                                onCancelBooking = { booking.id?.let { onCancelBooking(it) } },
                                onWriteReview = {
                                    val bId = booking.id ?: return@BookingItem
                                    val fId = booking.field?.id ?: return@BookingItem
                                    val fName = booking.field.name
                                    onWriteReview(bId, fId, fName)
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun BookingItem(
    booking: BookingResponse,
    onCancelBooking: () -> Unit = {},
    onWriteReview: () -> Unit = {}
) {
    val statusText = when (booking.status?.lowercase()) {
        "pending" -> stringResource(id = R.string.history_tab_pending)
        "confirmed" -> stringResource(id = R.string.history_tab_confirmed)
        "checked_in" -> stringResource(id = R.string.history_tab_checked_in)
        "finished" -> stringResource(id = R.string.history_tab_finished)
        "cancelled" -> stringResource(id = R.string.history_tab_cancelled)
        else -> booking.status ?: stringResource(id = R.string.history_status_unknown)
    }

    val statusColor = when (booking.status?.lowercase()) {
        "pending" -> MaterialTheme.colorScheme.tertiary
        "confirmed", "checked_in", "finished" -> MaterialTheme.colorScheme.primary
        "cancelled" -> MaterialTheme.colorScheme.error
        else -> MaterialTheme.colorScheme.onSurface
    }

    val currencyFormatter = NumberFormat.getCurrencyInstance(Locale.forLanguageTag("vi-VN"))

    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = stringResource(id = R.string.history_item_code, booking.code ?: stringResource(id = R.string.history_item_na)),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = statusText,
                    style = MaterialTheme.typography.labelLarge,
                    color = statusColor,
                    fontWeight = FontWeight.SemiBold
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = stringResource(id = R.string.history_item_field, booking.field?.name ?: stringResource(id = R.string.history_item_na), booking.field?.fieldType?.name ?: ""),
                style = MaterialTheme.typography.bodyMedium
            )
            Text(
                text = stringResource(id = R.string.history_item_branch, booking.field?.branch?.name ?: stringResource(id = R.string.history_item_na)),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.secondary
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = stringResource(id = R.string.history_item_date, booking.bookingDate ?: stringResource(id = R.string.history_item_na)),
                style = MaterialTheme.typography.bodyMedium
            )
            Text(
                text = stringResource(id = R.string.history_item_time, formatDateDash(booking.startTime ?: ""), formatDateDash(booking.endTime ?: "")),
                style = MaterialTheme.typography.bodyMedium
            )
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                val status = booking.status?.lowercase()
                when (status) {
                    "pending", "approved", "confirmed" -> {
                        TextButton(
                            onClick = onCancelBooking,
                            colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)
                        ) {
                            Text(stringResource(id = R.string.history_action_cancel), fontWeight = FontWeight.Bold)
                        }
                    }
                    "finished", "completed" -> {
                        TextButton(
                            onClick = onWriteReview,
                            colors = ButtonDefaults.textButtonColors(
                                contentColor = androidx.compose.ui.graphics.Color(0xFFEA580C)
                            )
                        ) {
                            Icon(
                                Icons.Rounded.Star,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                            Text(stringResource(id = R.string.history_action_review), fontWeight = FontWeight.Bold)
                        }
                    }
                    else -> Spacer(modifier = Modifier.width(1.dp))
                }

                Text(
                    text = stringResource(id = R.string.history_item_total, currencyFormatter.format(booking.totalPrice ?: 0)),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}
