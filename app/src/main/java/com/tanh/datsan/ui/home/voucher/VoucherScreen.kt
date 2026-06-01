package com.tanh.datsan.ui.home.voucher

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.LocalOffer
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.tanh.datsan.data.model.Voucher
import com.tanh.datsan.ui.component.CustomRefreshLayout
import com.tanh.datsan.ui.component.VoucherItem
import com.tanh.datsan.viewmodel.VoucherViewModel
import java.text.DecimalFormat

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VoucherScreen(
    viewModel: VoucherViewModel = hiltViewModel()
) {
    val myVouchers by viewModel.myVouchers.collectAsState()
    val collectibleVouchers by viewModel.collectibleVouchers.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()

    var selectedTab by remember { mutableIntStateOf(0) }
    val tabs = listOf("Voucher mới", "Voucher của tôi")

    LaunchedEffect(Unit) {
        viewModel.fetchCollectibleVouchers()
        viewModel.fetchMyVouchers()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Kho Voucher", fontWeight = FontWeight.Bold) },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
            )
        },
        containerColor = Color(0xFFF5F7FA)
    ) { padding ->
        Column(modifier = Modifier.padding(padding)) {
            SecondaryTabRow(
                selectedTabIndex = selectedTab,
                containerColor = Color.White,
                contentColor = Color(0xFF007BFF),
            ) {
                tabs.forEachIndexed { index, title ->
                    Tab(
                        selected = selectedTab == index,
                        onClick = { selectedTab = index },
                        text = {
                            Text(
                                title,
                                fontWeight = if (selectedTab == index) FontWeight.Bold else FontWeight.Normal
                            )
                        }
                    )
                }
            }

            CustomRefreshLayout(
                onRefresh = {
                    if (selectedTab == 0) viewModel.fetchCollectibleVouchers()
                    else viewModel.fetchMyVouchers()
                }
            ) {
                Box(modifier = Modifier.fillMaxSize()) {
                    val currentList = if (selectedTab == 0) collectibleVouchers else myVouchers

                    if (currentList.isEmpty() && !isLoading) {
                        EmptyVoucherState(if (selectedTab == 0) "Hiện chưa có voucher mới nào." else "Bạn chưa có voucher nào.")
                    } else {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(16.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            items(currentList) { voucher ->
                                if (selectedTab == 0) {
                                    CollectibleVoucherItem(
                                        voucher = voucher,
                                        onCollect = { viewModel.collectVoucher(voucher.id) }
                                    )
                                } else {
                                    // Ở màn hình danh sách voucher chung, ko cần check minOrderValue nên để 0.0
                                    VoucherItem(
                                        voucher = voucher,
                                        isSelected = false,
                                        currentOrderValue = 100000000.0, // Để luôn sáng
                                        onClick = {}
                                    )
                                }
                            }
                        }
                    }

                    if (isLoading) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(Color.Black.copy(alpha = 0.05f)),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator(color = Color(0xFF007BFF))
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun CollectibleVoucherItem(
    voucher: Voucher,
    onCollect: () -> Unit
) {
    val formatter = DecimalFormat("#,###")
    val discountTitle = if (voucher.discountPercentage != null) {
        "Giảm ${voucher.discountPercentage}%"
    } else {
        "Giảm ${formatter.format(voucher.discountAmount ?: 0)}đ"
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Outlined.LocalOffer,
                contentDescription = null,
                tint = Color(0xFFE53935),
                modifier = Modifier.size(40.dp)
            )
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(voucher.code, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                Text(discountTitle, color = Color(0xFFE53935), fontWeight = FontWeight.SemiBold)
                Text(
                    "Đơn tối thiểu ${formatter.format(voucher.minOrderValue ?: 0)}đ",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.Gray
                )
            }
            Button(
                onClick = onCollect,
                shape = RoundedCornerShape(8.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF007BFF))
            ) {
                Text("Lưu", fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
fun EmptyVoucherState(message: String) {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            Icons.Outlined.LocalOffer,
            contentDescription = null,
            modifier = Modifier.size(64.dp),
            tint = Color.LightGray
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(message, color = Color.Gray)
    }
}
