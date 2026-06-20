package com.tanh.datsan.ui.home.review

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ChatBubbleOutline
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.tanh.datsan.data.model.Review
import com.tanh.datsan.ui.component.CustomRefreshLayout
import com.tanh.datsan.ui.component.ReviewItem
import com.tanh.datsan.utils.DateUtil.formatReviewTime
import com.tanh.datsan.viewmodel.ReviewUiState
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MyReviewsScreen(
    myReviews: List<Review>,
    isLoading: Boolean,
    uiState: ReviewUiState,
    onFetchMyReviews: () -> Unit,
    onResetUiState: () -> Unit,
    onBackClick: () -> Unit
) {
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    LaunchedEffect(Unit) { onFetchMyReviews() }

    LaunchedEffect(uiState) {
        if (uiState is ReviewUiState.Error) {
            scope.launch {
                snackbarHostState.showSnackbar(uiState.message)
                onResetUiState()
            }
        }
    }

    Scaffold(
        containerColor = Color(0xFFF9FAFB),
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Đánh giá của tôi",
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Quay lại")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFFF9FAFB),
                    titleContentColor = Color(0xFF111827)
                )
            )
        }
    ) { padding ->
        CustomRefreshLayout(
            onRefresh = onFetchMyReviews,
            modifier = Modifier.padding(padding)
        ) {
            Box(modifier = Modifier.fillMaxSize()) {
                when {
                    isLoading && myReviews.isEmpty() -> {
                        CircularProgressIndicator(
                            color = Color(0xFF3B82F6),
                            modifier = Modifier.align(Alignment.Center)
                        )
                    }

                    myReviews.isEmpty() -> {
                        EmptyMyReviewsPlaceholder(modifier = Modifier.align(Alignment.Center))
                    }

                    else -> {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(16.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            items(myReviews) { review ->
                                MyReviewCard(review = review)
                            }
                            item { Spacer(Modifier.height(16.dp)) }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun MyReviewCard(review: Review) {
    // Show field name as header above the ReviewItem
    Column {
        review.field?.let { field ->
            Text(
                text = "🏟 ${field.name}" + (field.branch?.name?.let { " · $it" } ?: ""),
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color(0xFF3B82F6),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 4.dp, vertical = 2.dp)
            )
        }
        ReviewItem(
            userName = "Bạn",
            rating = review.rating,
            date = formatReviewTime(review.createdAt),
            comment = review.comment ?: "",
            adminReply = review.adminReply
        )
    }
}

@Composable
fun EmptyMyReviewsPlaceholder(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            Icons.Default.ChatBubbleOutline,
            contentDescription = null,
            modifier = Modifier.size(64.dp),
            tint = Color(0xFFE2E8F0)
        )
        Spacer(Modifier.height(16.dp))
        Text(
            text = "Bạn chưa viết đánh giá nào",
            color = Color(0xFF64748B),
            fontSize = 16.sp,
            fontWeight = FontWeight.Medium
        )
        Text(
            text = "Hãy đặt sân và chia sẻ trải nghiệm nhé!",
            color = Color(0xFF94A3B8),
            fontSize = 13.sp,
            modifier = Modifier.padding(top = 4.dp)
        )
    }
}
