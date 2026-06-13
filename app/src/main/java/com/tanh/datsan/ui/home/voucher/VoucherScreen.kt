package com.tanh.datsan.ui.home.voucher

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import com.tanh.datsan.data.model.Voucher
import com.tanh.datsan.ui.component.CustomRefreshLayout
import com.tanh.datsan.ui.component.VoucherItem

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VoucherScreen(
    myVouchers: List<Voucher>,
    collectibleVouchers: List<Voucher>,
    isLoading: Boolean,
    onFetchCollectibleVouchers: () -> Unit,
    onFetchMyVouchers: () -> Unit,
    onCollectVoucher: (String) -> Unit
) {
    var selectedTab by remember { mutableIntStateOf(0) }
    val tabs = listOf("Voucher mới", "Voucher của tôi")

    LaunchedEffect(Unit) {
        onFetchCollectibleVouchers()
        onFetchMyVouchers()
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
                            Text(title, fontWeight = if (selectedTab == index) FontWeight.Bold else FontWeight.Normal)
                        }
                    )
                }
            }

            CustomRefreshLayout(
                onRefresh = {
                    if (selectedTab == 0) onFetchCollectibleVouchers()
                    else onFetchMyVouchers()
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
                                        onCollect = { onCollectVoucher(voucher.id) }
                                    )
                                } else {
                                    VoucherItem(
                                        voucher = voucher,
                                        isSelected = false,
                                        currentOrderValue = 100000000.0,
                                        onClick = {}
                                    )
                                }
                            }
                        }
                    }

                    if (isLoading && currentList.isEmpty()) {
                        CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                    }
                }
            }
        }
    }
}

@Composable
fun CollectibleVoucherItem(voucher: Voucher, onCollect: () -> Unit) {
    VoucherItem(
        voucher = voucher,
        isSelected = false,
        currentOrderValue = 100000000.0,
        onClick = onCollect
    )
}

@Composable
fun EmptyVoucherState(message: String) {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(Icons.Outlined.LocalOffer, contentDescription = null, modifier = Modifier.size(64.dp), tint = Color.Gray)
            Spacer(modifier = Modifier.height(16.dp))
            Text(text = message, color = Color.Gray, fontSize = 16.sp)
        }
    }
}
