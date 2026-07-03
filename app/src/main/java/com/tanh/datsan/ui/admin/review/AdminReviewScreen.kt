package com.tanh.datsan.ui.admin.review

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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ChatBubble
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.automirrored.rounded.Reply
import androidx.compose.material.icons.rounded.Star
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.res.stringResource
import com.tanh.datsan.data.model.Review
import com.tanh.datsan.ui.component.CustomRefreshLayout
import com.tanh.datsan.ui.component.ReviewItem
import com.tanh.datsan.utils.DateUtil.formatReviewTime
import com.tanh.datsan.ui.admin.review.AdminReviewUiState
import com.tanh.datsan.R
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminReviewScreen(
    reviews: List<Review>,
    uiState: AdminReviewUiState,
    filterRating: Int?,
    onFetchReviews: (branchId: String?, rating: Int?) -> Unit,
    onDeleteReview: (String) -> Unit,
    onReplyReview: (String, String) -> Unit,
    onResetUiState: () -> Unit,
    onBackClick: () -> Unit
) {
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    // Dialog states
    var reviewToDelete by remember { mutableStateOf<Review?>(null) }
    var reviewToReply by remember { mutableStateOf<Review?>(null) }
    var replyText by remember { mutableStateOf("") }

    LaunchedEffect(Unit) { onFetchReviews(null, null) }

    LaunchedEffect(uiState) {
        when (uiState) {
            is AdminReviewUiState.Success -> {
                scope.launch {
                    snackbarHostState.showSnackbar(uiState.message.ifBlank { context.getString(R.string.review_success) })
                    onResetUiState()
                }
            }
            is AdminReviewUiState.Error -> {
                scope.launch {
                    snackbarHostState.showSnackbar(uiState.message)
                    onResetUiState()
                }
            }
            else -> Unit
        }
    }

    Scaffold(
        containerColor = Color(0xFFF9FAFB),
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        stringResource(id = R.string.review_management),
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(id = R.string.review_back))
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFFF9FAFB),
                    titleContentColor = Color(0xFF111827)
                )
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // Rating filter chips
            RatingFilterRow(
                selectedRating = filterRating,
                onSelectRating = { rating ->
                    onFetchReviews(null, if (filterRating == rating) null else rating)
                }
            )

            CustomRefreshLayout(
                onRefresh = { onFetchReviews(null, filterRating) }
            ) {
                when {
                    uiState is AdminReviewUiState.Loading && reviews.isEmpty() -> {
                        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            CircularProgressIndicator(color = Color(0xFF3B82F6))
                        }
                    }

                    reviews.isEmpty() -> {
                        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(
                                    Icons.Default.ChatBubble,
                                    null,
                                    modifier = Modifier.size(64.dp),
                                    tint = Color(0xFFE2E8F0)
                                )
                                Spacer(Modifier.height(16.dp))
                                Text(
                                    stringResource(id = R.string.review_empty),
                                    color = Color(0xFF64748B),
                                    fontWeight = FontWeight.Medium,
                                    fontSize = 16.sp
                                )
                            }
                        }
                    }

                    else -> {
                        LazyColumn(
                            contentPadding = PaddingValues(16.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp),
                            modifier = Modifier.fillMaxSize()
                        ) {
                            items(reviews) { review ->
                                AdminReviewCard(
                                    review = review,
                                    onDeleteClick = { reviewToDelete = review },
                                    onReplyClick = {
                                        replyText = review.adminReply ?: ""
                                        reviewToReply = review
                                    }
                                )
                            }
                            item { Spacer(Modifier.height(16.dp)) }
                        }
                    }
                }
            }
        }
    }
    reviewToDelete?.let { review ->
        AlertDialog(
            onDismissRequest = { reviewToDelete = null },
            title = { Text(stringResource(id = R.string.review_delete_title), fontWeight = FontWeight.Bold) },
            text = {
                Text(stringResource(id = R.string.review_delete_confirm))
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        onDeleteReview(review.id)
                        reviewToDelete = null
                    },
                    colors = ButtonDefaults.textButtonColors(contentColor = Color(0xFFEF4444))
                ) {
                    Text(stringResource(id = R.string.review_delete_btn), fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { reviewToDelete = null }) { Text(stringResource(id = R.string.review_cancel_btn)) }
            }
        )
    }
    reviewToReply?.let { review ->
        AlertDialog(
            onDismissRequest = { reviewToReply = null },
            title = {
                Text(stringResource(id = R.string.review_reply_title), fontWeight = FontWeight.Bold)
            },
            text = {
                Column {
                    // Show original review snippet
                    Text(
                        text = "\"${review.comment?.take(80) ?: ""}${if ((review.comment?.length ?: 0) > 80) "..." else ""}\"",
                        fontSize = 13.sp,
                        fontStyle = FontStyle.Italic,
                        color = Color(0xFF64748B),
                        modifier = Modifier.padding(bottom = 12.dp)
                    )
                    OutlinedTextField(
                        value = replyText,
                        onValueChange = { replyText = it },
                        label = { Text(stringResource(id = R.string.review_reply_hint)) },
                        shape = RoundedCornerShape(10.dp),
                        maxLines = 5,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(140.dp)
                    )
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        if (replyText.isNotBlank()) {
                            onReplyReview(review.id, replyText.trim())
                        }
                        reviewToReply = null
                        replyText = ""
                    },
                    colors = ButtonDefaults.textButtonColors(contentColor = Color(0xFF3B82F6))
                ) {
                    Text(stringResource(id = R.string.review_reply_btn), fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = {
                    reviewToReply = null
                    replyText = ""
                }) { Text(stringResource(id = R.string.review_cancel_btn)) }
            }
        )
    }
}

@Composable
fun RatingFilterRow(
    selectedRating: Int?,
    onSelectRating: (Int) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.White)
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Icon(
            Icons.Default.FilterList,
            contentDescription = null,
            tint = Color(0xFF64748B),
            modifier = Modifier.size(18.dp)
        )
        Text(stringResource(id = R.string.review_filter_stars), fontSize = 13.sp, color = Color(0xFF64748B))
        LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            items((1..5).toList()) { star ->
                FilterChip(
                    selected = selectedRating == star,
                    onClick = { onSelectRating(star) },
                    label = {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(2.dp)
                        ) {
                            Text("$star", fontSize = 13.sp)
                            Icon(
                                Icons.Rounded.Star,
                                null,
                                modifier = Modifier.size(12.dp)
                            )
                        }
                    },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = Color(0xFFFEF9C3),
                        selectedLabelColor = Color(0xFFB45309)
                    )
                )
            }
        }
    }
}

@Composable
fun AdminReviewCard(
    review: Review,
    onDeleteClick: () -> Unit,
    onReplyClick: () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        color = Color.White,
        shadowElevation = 1.dp
    ) {
        Column {
            // Field info header
            review.field?.let { field ->
                Text(
                    text = "🏟 ${field.name}" + (field.branch?.name?.let { " · $it" } ?: ""),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color(0xFF3B82F6),
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                )
            }

            ReviewItem(
                userName = review.user?.fullName ?: stringResource(id = R.string.review_anonymous),
                rating = review.rating,
                date = formatReviewTime(review.createdAt),
                comment = review.comment ?: "",
                avatarUrl = review.user?.avatarUrl,
                adminReply = review.adminReply
            )

            // Admin action buttons
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                TextButton(
                    onClick = onReplyClick,
                    colors = ButtonDefaults.textButtonColors(contentColor = Color(0xFF3B82F6))
                ) {
                    Icon(
                        Icons.AutoMirrored.Rounded.Reply,
                        null,
                        modifier = Modifier.size(16.dp)
                    )
                    Text(
                        text = if (review.adminReply.isNullOrBlank()) stringResource(id = R.string.review_action_reply) else stringResource(id = R.string.review_action_edit_reply),
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
                TextButton(
                    onClick = onDeleteClick,
                    colors = ButtonDefaults.textButtonColors(contentColor = Color(0xFFEF4444))
                ) {
                    Icon(Icons.Default.Delete, null, modifier = Modifier.size(16.dp))
                    Text(" " + stringResource(id = R.string.review_delete_btn), fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                }
            }
        }
    }
}
