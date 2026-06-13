package com.tanh.datsan.ui.home.review

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
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
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.review_all_count, reviews.size), fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.btn_back))
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White, titleContentColor = Color(0xFF111827))
            )
        }
    ) { padding ->
        CustomRefreshLayout(
            onRefresh = { onFetchReview(fieldId) },
            modifier = Modifier.padding(padding)
        ) {
            Box(modifier = Modifier.fillMaxSize()) {
                if (reviews.isEmpty() && !isLoading) {
                    EmptyReviewsPlaceholder()
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize().background(Color(0xFFF9FAFB)),
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
                    }
                }

                if (isLoading && reviews.isEmpty()) {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                }
            }
        }
    }
}

@Composable
fun EmptyReviewsPlaceholder() {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(text = "Chưa có đánh giá nào", color = Color(0xFF6B7280), fontWeight = FontWeight.Medium)
        }
    }
}
