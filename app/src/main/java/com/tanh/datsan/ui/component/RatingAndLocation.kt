package com.tanh.datsan.ui.component

import androidx.compose.ui.graphics.Color
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.rounded.Star
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.tanh.datsan.R

@Composable
fun RatingAndLocation(
    rating: Float,
    reviewCount: Int,
    address: String,
    tint: Color = Color(0xFF2E7D32), // Cho mặc định màu xanh luôn
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // Hàng Sao & Review
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = Icons.Rounded.Star,
                contentDescription = null,
                tint = Color(0xFFFFC107),
                modifier = Modifier.size(20.dp)
            )
            Text(" $rating ", fontWeight = FontWeight.Bold)
            Text(
                stringResource(R.string.review_count_suffix, reviewCount),
                color = Color.Gray,
                fontSize = 14.sp
            )
        }

        // Hàng Địa chỉ
        Row(verticalAlignment = Alignment.Top) {
            Icon(
                Icons.Default.LocationOn,
                null,
                tint = tint,
                modifier = Modifier.size(20.dp)
            )
            Spacer(Modifier.width(4.dp))
            Text(
                text = address,
                color = Color(0xFF4B5563),
                fontSize = 14.sp,
                lineHeight = 20.sp
            )
        }
    }
}