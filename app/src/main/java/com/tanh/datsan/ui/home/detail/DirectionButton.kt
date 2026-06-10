package com.tanh.datsan.ui.home.detail

import android.content.Intent
import android.net.Uri
import android.util.Log
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Map
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.net.toUri

@Composable
fun DirectionButton(
    lat: Double,
    lng: Double,
    tenSan: String,
    primaryColor: Color,
    onShowMessage: (String) -> Unit
) {
    val context = LocalContext.current

    Button(
        onClick = {
            val encodedName = Uri.encode(tenSan)
            val uri = "geo:$lat,$lng?q=$lat,$lng($encodedName)".toUri()
            Log.d("DirectButton","uri: $uri")
            val intent = Intent(Intent.ACTION_VIEW, uri)
//            intent.setPackage("com.google.android.apps.maps")

            try {
                context.startActivity(intent)
            } catch (e: Exception) {
                Log.e("DirectButton","${e.message}")
                onShowMessage("Vui lòng cài đặt ứng dụng Google Maps!")
            }
        },
        modifier = Modifier
            .fillMaxWidth()
            .height(50.dp),
        shape = RoundedCornerShape(12.dp),
        colors = ButtonDefaults.buttonColors(primaryColor.copy(0.1f)),
        elevation = ButtonDefaults.buttonElevation(0.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = Icons.Rounded.Map,
                contentDescription = "Bản đồ",
                tint = primaryColor,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "Xem đường đi trên Google Maps",
                color = primaryColor,
                fontWeight = FontWeight.Bold,
                fontSize = 15.sp
            )
        }
    }
}
