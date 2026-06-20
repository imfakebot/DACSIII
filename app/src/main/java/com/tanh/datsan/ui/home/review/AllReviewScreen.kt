package com.tanh.datsan.ui.home.review

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ChatBubble
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.rounded.Star
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
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
import com.tanh.datsan.data.model.ReviewMeta
import com.tanh.datsan.ui.component.CustomRefreshLayout
import com.tanh.datsan.ui.component.ReviewItem
import com.tanh.datsan.utils.DateUtil.formatReviewTime
import com.tanh.datsan.utils.toFullImageUrl
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AllReviewScreen(
    fieldId: String,
    reviews: List<Review>,
    reviewMeta: ReviewMeta?,
    isLoading: Boolean,
    errorMessage: String?,
    isLoggedIn: Boolean,
    onFetchReview: (String) -> Unit,
    onClearError: () -> Unit,
    onBackClick: () -> Unit,
    onWriteReviewClick: () -> Unit = {}
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
                title = {
                    Text(
                        stringResource(R.string.review_all_count, reviews.size),
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 20.sp
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.btn_back)
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFFF9FAFB),
                    titleContentColor = Color(0xFF111827)
                )
            )
        },
        floatingActionButton = {
            if (isLoggedIn) {
                ExtendedFloatingActionButton(
                    onClick = onWriteReviewClick,
                    icon = { Icon(Icons.Default.Edit, contentDescription = null) },
                    text = { Text("Viết đánh giá") },
                    containerColor = Color(0xFF3B82F6),
                    contentColor = Color.White
                )
            }
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
                    // Average rating header
                    reviewMeta?.averageRating?.let { avg ->
                        item {
                            AverageRatingHeader(
                                averageRating = avg,
                                totalCount = reviewMeta.total
                            )
                        }
                    }

                    items(reviews) { review ->
                        Log.d("Allreview","avtar url ${review.user?.avatarUrl.toFullImageUrl()} ")
                        ReviewItem(
                            userName = review.user?.fullName ?: "Khách hàng ẩn danh",
                            rating = review.rating,
                            date = formatReviewTime(review.createdAt),
                            comment = review.comment ?: "",
                            avatarUrl = review.user?.avatarUrl.toFullImageUrl(),
                            adminReply = review.adminReply
                        )
                    }

                    item { Spacer(Modifier.height(80.dp)) } // FAB clearance
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
fun AverageRatingHeader(averageRating: Float, totalCount: Int) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.White, RoundedCornerShape(16.dp))
            .padding(20.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = "%.1f".format(averageRating),
                fontSize = 48.sp,
                fontWeight = FontWeight.ExtraBold,
                color = Color(0xFF0F172A)
            )
            Row(verticalAlignment = Alignment.CenterVertically) {
                repeat(5) { i ->
                    Icon(
                        Icons.Rounded.Star,
                        contentDescription = null,
                        tint = if (i < averageRating) Color(0xFFFFD700) else Color(0xFFE2E8F0),
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
            Text(
                text = "$totalCount đánh giá",
                fontSize = 12.sp,
                color = Color(0xFF94A3B8),
                modifier = Modifier.padding(top = 4.dp)
            )
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
