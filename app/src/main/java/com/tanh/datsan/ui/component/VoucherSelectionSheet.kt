package com.tanh.datsan.ui.component

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.outlined.LocalOffer
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.tanh.datsan.R
import com.tanh.datsan.data.model.Voucher
import java.text.DecimalFormat

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VoucherSelectionSheet(
    vouchers: List<Voucher>,
    selectedVoucherCode: String?,
    onSelect: (Voucher?) -> Unit,
    onDismiss: () -> Unit,
    totalPrice: Double
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surface
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .padding(bottom = 32.dp)
        ) {
            Text(
                text = stringResource(id = R.string.voucher_sheet_title),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                item {
                    OutlinedButton(
                        onClick = { onSelect(null) },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(stringResource(id = R.string.voucher_sheet_none))
                    }
                }

                if (vouchers.isEmpty()) {
                    item {
                        Text(
                            text = stringResource(id = R.string.voucher_sheet_empty),
                            color = Color.Gray,
                            modifier = Modifier.padding(vertical = 24.dp)
                        )
                    }
                } else {
                    items(vouchers) { voucher ->
                        VoucherItem(
                            voucher = voucher,
                            isSelected = voucher.code == selectedVoucherCode,
                            onClick = { onSelect(voucher) },
                            currentOrderValue = totalPrice
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun VoucherItem(
    voucher: Voucher,
    isSelected: Boolean,
    currentOrderValue: Double,
    onClick: () -> Unit
) {
    val formatter = DecimalFormat("#,###")

    val isEnabled = currentOrderValue >= (voucher.minOrderValue ?: 0.0)

    val discountTitle = if (voucher.discountPercentage != null) {
        stringResource(id = R.string.voucher_sheet_discount_pct, voucher.discountPercentage.toString(), formatter.format(voucher.maxDiscountAmount ?: 0))
    } else {
        stringResource(id = R.string.voucher_sheet_discount_flat, formatter.format(voucher.discountAmount ?: 0))
    }
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp)
            .clickable(enabled = isEnabled) {
                onClick()
            },
        colors = CardDefaults.cardColors(
            containerColor = when {
                isSelected -> MaterialTheme.colorScheme.primaryContainer
                !isEnabled -> Color(0xFFF3F4F6)
                else -> Color(0xFFFAFAFA)
            }
        ),
        border = if (isSelected) BorderStroke(1.5.dp, MaterialTheme.colorScheme.primary)
        else BorderStroke(1.dp, Color(0xFFE5E7EB)),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        val contentAlpha = if (isEnabled) 1f else 0.5f
        Row(
            modifier = Modifier
                .padding(16.dp)
                .alpha(contentAlpha),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Outlined.LocalOffer,
                contentDescription = null,
                tint = if (isSelected) MaterialTheme.colorScheme.primary else Color.Gray,
                modifier = Modifier.size(32.dp)
            )
            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = voucher.code,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = if (isSelected) MaterialTheme.colorScheme.primary else Color.Black
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = discountTitle,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = if (isEnabled) Color(0xFFE53935) else Color.Gray
                )

                Text(
                    text = if (isEnabled) {
                        stringResource(id = R.string.voucher_sheet_min_order, formatter.format(voucher.minOrderValue))
                    } else {
                        stringResource(id = R.string.voucher_sheet_not_eligible, formatter.format((voucher.minOrderValue ?: 0.0) - currentOrderValue))
                    },
                    style = MaterialTheme.typography.bodySmall,
                    color = if (isEnabled) Color.Gray else MaterialTheme.colorScheme.error
                )
            }

            if (isSelected) {
                Icon(
                    imageVector = Icons.Default.CheckCircle,
                    contentDescription = "Selected",
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}