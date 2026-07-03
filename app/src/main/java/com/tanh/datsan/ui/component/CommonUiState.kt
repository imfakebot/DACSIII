package com.tanh.datsan.ui.component

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.tanh.datsan.R
import com.tanh.datsan.ui.theme.PrimaryGreen

/**
 * Shared full-screen loading state dùng chung cho nhiều màn hình.
 */
@Composable
fun LoadingStateScreen(
    modifier: Modifier = Modifier,
    message: String = stringResource(R.string.ticket_loading),
    color: Color = PrimaryGreen
) {
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            CircularProgressIndicator(color = color)
            Spacer(modifier = Modifier.height(16.dp))
            Text(message, color = Color(0xFF64748B))
        }
    }
}

/**
 * Shared full-screen error state dùng chung cho nhiều màn hình.
 */
@Composable
fun ErrorStateScreen(
    message: String,
    onHome: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(32.dp)
        ) {
            Icon(
                Icons.Default.ErrorOutline,
                contentDescription = null,
                tint = Color.Red,
                modifier = Modifier.size(48.dp)
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                stringResource(R.string.error_unknown),
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp
            )
            Text(message, textAlign = TextAlign.Center, color = Color.Gray)
            Spacer(modifier = Modifier.height(24.dp))
            Button(
                onClick = onHome,
                colors = ButtonDefaults.buttonColors(containerColor = PrimaryGreen)
            ) {
                Text(stringResource(R.string.home))
            }
        }
    }
}
