package com.tanh.datsan.ui.component

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.tanh.datsan.utils.toFullImageUrl

@Composable
fun FieldImageSlider(
    images: List<String>,
    onImageClick: ((Int) -> Unit)? = null
) {
    val formattedUrl = remember(images) {
        images.map { url -> url.toFullImageUrl() }
    }

    // State điều khiển việc trượt (PagerState)
    val pagerState = rememberPagerState(pageCount = { formattedUrl.size })

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(320.dp)
    ) {
        HorizontalPager(
            state = pagerState,
            modifier = Modifier.fillMaxSize()
        ) { page ->
            AsyncImage(
                model = formattedUrl[page],
                contentDescription = "Ảnh sân bóng ${page + 1}",
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .fillMaxSize()
                    .clickable(
                        enabled = true,
                        onClick = {
                            onImageClick?.invoke(page)
                        }
                    )
            )
        }

        if (formattedUrl.size > 1) {
            Row(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 24.dp) // Cách đáy một khoảng để không bị lẹm vào phần bo góc của content
                    .background(
                        Color.Black.copy(alpha = 0.2f),
                        CircleShape
                    ) // Nền mờ cho dấu chấm dễ nhìn
                    .padding(horizontal = 8.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                repeat(pagerState.pageCount) { index ->
                    val isSelected = pagerState.currentPage == index
                    Box(
                        modifier = Modifier
                            .size(if (isSelected) 8.dp else 6.dp)
                            .clip(CircleShape)
                            .background(if (isSelected) Color.White else Color.Gray)

                    )
                }
            }
        }
    }

}