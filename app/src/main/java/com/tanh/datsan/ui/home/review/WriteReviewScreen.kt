package com.tanh.datsan.ui.home.review

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.rounded.Star
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.tanh.datsan.ui.home.review.ReviewUiState
import kotlinx.coroutines.launch
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.stringArrayResource
import com.tanh.datsan.R


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WriteReviewScreen(
    bookingId: String,
    fieldName: String,
    uiState: ReviewUiState,
    onSubmit: (bookingId: String, rating: Int, comment: String?) -> Unit,
    onResetUiState: () -> Unit,
    onBackClick: () -> Unit,
    onSuccess: () -> Unit
) {
    var selectedRating by remember { mutableIntStateOf(0) }
    var comment by remember { mutableStateOf("") }
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    // Handle UiState side-effects
    LaunchedEffect(uiState) {
        when (uiState) {
            is ReviewUiState.Success -> {
                snackbarHostState.showSnackbar(
                    uiState.message.ifBlank { context.getString(R.string.review_write_success) }
                )
                onResetUiState()
                onSuccess()
            }
            is ReviewUiState.Error -> {
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
                    Text(stringResource(id = R.string.review_write_action), fontWeight = FontWeight.Bold, fontSize = 20.sp)
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
                .verticalScroll(rememberScrollState())
                .imePadding()
                .padding(horizontal = 24.dp, vertical = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Field name
            Text(
                text = fieldName,
                fontSize = 22.sp,
                fontWeight = FontWeight.ExtraBold,
                color = Color(0xFF0F172A),
                textAlign = TextAlign.Center
            )
            Text(
                text = stringResource(id = R.string.review_write_desc),
                fontSize = 14.sp,
                color = Color(0xFF94A3B8),
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(top = 4.dp, bottom = 24.dp)
            )

            // Rating label
            val ratingLabels = stringArrayResource(id = R.array.review_rating_labels)
            Text(
                text = if (selectedRating > 0) ratingLabels[selectedRating - 1] else stringResource(id = R.string.review_write_select_star),
                fontSize = 18.sp,
                fontWeight = FontWeight.SemiBold,
                color = if (selectedRating > 0) Color(0xFFFFD700) else Color(0xFF94A3B8),
                modifier = Modifier.padding(bottom = 12.dp)
            )

            // Star selector
            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.padding(bottom = 32.dp)
            ) {
                (1..5).forEach { star ->
                    val starColor by animateColorAsState(
                        targetValue = if (star <= selectedRating) Color(0xFFFFD700) else Color(0xFFE2E8F0),
                        animationSpec = spring(),
                        label = "star_color"
                    )
                    Icon(
                        imageVector = Icons.Rounded.Star,
                        contentDescription = stringResource(id = R.string.review_write_star_format, star),
                        tint = starColor,
                        modifier = Modifier
                            .size(52.dp)
                            .clickable { selectedRating = star }
                    )
                }
            }

            // Comment field
            OutlinedTextField(
                value = comment,
                onValueChange = { comment = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(160.dp),
                placeholder = {
                    Text(
                        stringResource(id = R.string.review_write_hint),
                        color = Color(0xFFCBD5E1)
                    )
                },
                shape = RoundedCornerShape(12.dp),
                maxLines = 8
            )

            Spacer(Modifier.height(32.dp))

            // Submit button
            val isLoading = uiState is ReviewUiState.Loading
            Button(
                onClick = {
                    if (selectedRating == 0) {
                        scope.launch { snackbarHostState.showSnackbar(context.getString(R.string.review_write_error_star)) }
                        return@Button
                    }
                    onSubmit(bookingId, selectedRating, comment.ifBlank { null })
                },
                enabled = !isLoading,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF3B82F6),
                    disabledContainerColor = Color(0xFFBFDBFE)
                )
            ) {
                if (isLoading) {
                    CircularProgressIndicator(color = Color.White, modifier = Modifier.size(20.dp))
                } else {
                    Text(stringResource(id = R.string.review_write_submit_btn), fontWeight = FontWeight.Bold, fontSize = 16.sp)
                }
            }
        }
    }
}
