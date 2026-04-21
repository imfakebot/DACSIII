package com.tanh.datsan.ui.component

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.ImageLoader
import coil.compose.AsyncImage
import coil.decode.SvgDecoder
import coil.request.ImageRequest
import com.tanh.datsan.BuildConfig
import com.tanh.datsan.data.model.Utility

@Composable
fun UtilityItem(utility: Utility) {
    val context = LocalContext.current
    val imageLoader =
        remember { ImageLoader.Builder(context).components { add(SvgDecoder.Factory()) }.build() }
    val fullIconUrl = utility.iconUrl?.let { path ->
        if (path.startsWith("http")) path else "${BuildConfig.API_HOST}$path"
    }

    Surface(
        color = Color(0xFFF9FAFB),
        shape = RoundedCornerShape(12.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE5E7EB))
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
        ) {
            AsyncImage(
                model = ImageRequest.Builder(context).data(fullIconUrl).crossfade(true).build(),
                imageLoader = imageLoader,
                contentDescription = utility.name,
                modifier = Modifier.size(20.dp),
                error = painterResource(android.R.drawable.ic_menu_agenda)
            )
            Spacer(Modifier.width(8.dp))
            Text(text = utility.name, fontSize = 14.sp, fontWeight = FontWeight.Medium)
        }
    }
}