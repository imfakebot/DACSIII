package com.tanh.datsan.ui.home.detail

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Star
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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
    Modifier.padding(vertical = 24.dp), color = Color(0xFFF4F7F6), thickness = 2.dp
)

@Composable
fun LoadingState() = Box(Modifier.fillMaxSize(), Alignment.Center) {
    CircularProgressIndicator(
        color = Color(0xFF2E7D32)
    )
}

@Composable
fun ErrorState(msg: String?) =
    Box(Modifier.fillMaxSize(), Alignment.Center) { Text("Lỗi: $msg", color = Color.Red) }

@Composable
fun BookingBottomBar(onClick: () -> Unit) {
    Surface(shadowElevation = 8.dp) {
        Button(
            onClick = onClick,
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .height(56.dp),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2E7D32))
        ) {
            Text(
                stringResource(R.string.booking_select_time_title),
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp
            )
        }
    }
}

@Composable
fun ReviewHeader(
    fieldId: String,
    reviewCount:Int,
    rating:Float,
    onNavigate: (String) -> Unit,
    color: Color
) {
    Row(
        Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                stringResource(R.string.detail_tab_reviews),
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp
            )

            if(reviewCount>0){
                Text(
                    text = " ($reviewCount)",
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    color = Color.Gray
                )

                Spacer(modifier = Modifier.width(8.dp))

                Icon(
                    imageVector = Icons.Rounded.Star,
                    contentDescription = null,
                    tint = Color(0xFFFFB300),
                    modifier = Modifier.size(20.dp)
                )

                Spacer(modifier = Modifier.width(2.dp))

                Text(
                    text = String.format(Locale.US, "%.1f", rating),
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
            }
        }

        TextButton(onClick = { onNavigate(fieldId) }) {
            Text(stringResource(R.string.btn_view_all), color = color, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
fun ReviewList(reviews: List<Review>?) {
    if (reviews.isNullOrEmpty()) {
        Box(Modifier
            .fillMaxWidth()
            .padding(24.dp), contentAlignment = Alignment.Center) {
            Text(stringResource(R.string.review_empty_msg), color = Color.Gray)
        }
    } else {
        Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
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