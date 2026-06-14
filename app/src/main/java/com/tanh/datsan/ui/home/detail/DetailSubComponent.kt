package com.tanh.datsan.ui.home.detail

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.rounded.Star
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.tanh.datsan.R
import com.tanh.datsan.data.model.Review
import com.tanh.datsan.ui.component.ReviewItem
import com.tanh.datsan.utils.DateUtil.formatReviewTime
import java.util.Locale

@Composable
fun SectionDivider() = HorizontalDivider(
    Modifier.padding(vertical = 24.dp), color = Color(0xFFE2E8F0), thickness = 1.dp
)

@Composable
fun LoadingState() = Box(
    Modifier
        .fillMaxSize()
        .background(Color.White), 
    Alignment.Center
) {
    CircularProgressIndicator(
        color = Color(0xFF1E293B),
        strokeWidth = 4.dp
    )
}

@Composable
fun ErrorState(msg: String?) =
    Box(Modifier.fillMaxSize(), Alignment.Center) { 
        Text("Lỗi: $msg", color = Color(0xFFEF4444), fontWeight = FontWeight.Bold) 
    }

@Composable
fun BookingBottomBar(onClick: () -> Unit) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shadowElevation = 24.dp,
        color = Color.White
    ) {
        Button(
            onClick = onClick,
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp)
                .height(60.dp),
            shape = RoundedCornerShape(20.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFF0F172A),
                contentColor = Color.White
            ),
            elevation = ButtonDefaults.buttonElevation(defaultElevation = 8.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Text(
                    stringResource(R.string.booking_select_time_title).uppercase(),
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 16.sp,
                    letterSpacing = 1.5.sp
                )
                Spacer(Modifier.width(12.dp))
                Icon(Icons.AutoMirrored.Filled.ArrowForward, null, modifier = Modifier.size(20.dp))
            }
        }
    }
}

@Composable
fun ReviewHeader(
    fieldId: String,
    reviewCount: Int,
    rating: Float,
    onNavigate: (String) -> Unit,
    color: Color
) {
    Row(
        Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.Bottom
    ) {
        Column {
            Text(
                stringResource(R.string.detail_tab_reviews),
                fontWeight = FontWeight.ExtraBold,
                fontSize = 22.sp,
                color = Color(0xFF0F172A)
            )

            if (reviewCount > 0) {
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(top = 4.dp)) {
                    Icon(
                        imageVector = Icons.Rounded.Star,
                        contentDescription = null,
                        tint = Color(0xFFFFD700),
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = String.format(Locale.US, "%.1f", rating),
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = Color(0xFF1E293B)
                    )
                    Text(
                        text = " ($reviewCount nhận xét)",
                        fontSize = 14.sp,
                        color = Color(0xFF64748B),
                        modifier = Modifier.padding(start = 4.dp)
                    )
                }
            }
        }

        TextButton(onClick = { onNavigate(fieldId) }) {
            Text(
                stringResource(R.string.btn_view_all), 
                color = Color(0xFF3B82F6), 
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp
            )
        }
    }
}

@Composable
fun ReviewList(reviews: List<Review>?) {
    if (reviews.isNullOrEmpty()) {
        Surface(
            modifier = Modifier.fillMaxWidth().padding(vertical = 16.dp),
            color = Color(0xFFF8FAFC),
            shape = RoundedCornerShape(16.dp)
        ) {
            Box(Modifier.padding(24.dp), contentAlignment = Alignment.Center) {
                Text(stringResource(R.string.review_empty_msg), color = Color(0xFF94A3B8), fontWeight = FontWeight.Medium)
            }
        }
    } else {
        Column(verticalArrangement = Arrangement.spacedBy(20.dp), modifier = Modifier.padding(vertical = 16.dp)) {
            reviews.take(3).forEach { review ->
                ReviewItem(
                    userName = review.user?.fullName ?: "Khách hàng",
                    rating = review.rating,
                    date = formatReviewTime(review.createdAt),
                    comment = review.comment ?: "",
                    avatarUrl = review.user?.avatarUrl
                )
            }
        }
    }
}
