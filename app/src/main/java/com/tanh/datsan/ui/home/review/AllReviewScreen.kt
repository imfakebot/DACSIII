package com.tanh.datsan.ui.home.review

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ChatBubble
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.tanh.datsan.R
import com.tanh.datsan.data.model.Review
import com.tanh.datsan.ui.component.CustomRefreshLayout
import com.tanh.datsan.ui.component.ReviewItem
import com.tanh.datsan.utils.DateUtil.formatReviewTime
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AllReviewScreen(
    fieldId: String,
    reviews: List<Review>,
    isLoading: Boolean,
    errorMessage: String?,
    onFetchReview: (String) -> Unit,
    onClearError: () -> Unit,
    onBackClick: () -> Unit
) {
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    LaunchedEffect(fieldId) { onFetchReview(fieldId) }

    LaunchedEffect(errorMessage) {
        errorMessage?.let { msg ->
            scope.launch { snackbarHostState.showSnackbar(message = msg) }
            onClearError()
        }
    }

    Scaffold(
        containerColor = Color(0xFFF9FAFB),
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.review_all_count, reviews.size), fontWeight = FontWeight.ExtraBold, fontSize = 20.sp) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.btn_back))
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
            onRefresh = { onFetchReview(fieldId) },
            modifier = Modifier.padding(padding)
        ) {
            if (reviews.isEmpty() && !isLoading) {
                EmptyReviewsPlaceholder()
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(reviews) { review ->
                        ReviewItem(
                            userName = review.user?.fullName ?: "Khách hàng ẩn danh",
                            rating = review.rating,
                            date = formatReviewTime(review.createdAt),
                            comment = review.comment ?: "",
                            avatarUrl = review.user?.avatarUrl
                        )
                    }
                    
                    // Thêm khoảng trống ở cuối list
                    item { Spacer(Modifier.height(16.dp)) }
                }
            }

            if (isLoading && reviews.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = Color(0xFF3B82F6))
                }
            }
        }
    }
}

@Composable
fun EmptyReviewsPlaceholder() {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(
                Icons.Default.ChatBubble, 
                contentDescription = null, 
                modifier = Modifier.size(64.dp), 
                tint = Color(0xFFE2E8F0)
            )
            Spacer(Modifier.height(16.dp))
            Text(
                text = "Chưa có đánh giá nào", 
                color = Color(0xFF64748B), 
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium
            )
        }
    }
}
