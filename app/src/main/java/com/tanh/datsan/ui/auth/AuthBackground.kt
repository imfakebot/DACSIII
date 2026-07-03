package com.tanh.datsan.ui.auth

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke

@Composable
fun AuthBackground(content: @Composable () -> Unit) {
    val infiniteTransition = rememberInfiniteTransition(label = "mesh_animation")
    
    val time by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(10000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "time"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF020617)) // Deep Dark Background
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val width = size.width
            val height = size.height

            // Dynamic Blobs (Simulating Mesh Gradient)
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(Color(0xFF3B82F6).copy(alpha = 0.3f), Color.Transparent),
                    center = Offset(
                        width * (0.2f + 0.1f * kotlin.math.sin(time * 2 * Math.PI.toFloat())),
                        height * (0.3f + 0.15f * kotlin.math.cos(time * 2 * Math.PI.toFloat()))
                    ),
                    radius = width * 0.8f
                ),
                radius = width * 0.8f,
                center = Offset(
                    width * (0.2f + 0.1f * kotlin.math.sin(time * 2 * Math.PI.toFloat())),
                    height * (0.3f + 0.15f * kotlin.math.cos(time * 2 * Math.PI.toFloat()))
                )
            )

            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(Color(0xFF8B5CF6).copy(alpha = 0.25f), Color.Transparent),
                    center = Offset(
                        width * (0.8f + 0.1f * kotlin.math.cos(time * 2 * Math.PI.toFloat())),
                        height * (0.7f + 0.1f * kotlin.math.sin(time * 2 * Math.PI.toFloat()))
                    ),
                    radius = width * 0.9f
                ),
                radius = width * 0.9f,
                center = Offset(
                    width * (0.8f + 0.1f * kotlin.math.cos(time * 2 * Math.PI.toFloat())),
                    height * (0.7f + 0.1f * kotlin.math.sin(time * 2 * Math.PI.toFloat()))
                )
            )

            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(Color(0xFFEC4899).copy(alpha = 0.15f), Color.Transparent),
                    center = Offset(
                        width * (0.5f + 0.2f * kotlin.math.sin(time * 2 * Math.PI.toFloat() + 1f)),
                        height * (0.2f + 0.1f * kotlin.math.cos(time * 2 * Math.PI.toFloat() + 1f))
                    ),
                    radius = width * 0.7f
                ),
                radius = width * 0.7f,
                center = Offset(
                    width * (0.5f + 0.2f * kotlin.math.sin(time * 2 * Math.PI.toFloat() + 1f)),
                    height * (0.2f + 0.1f * kotlin.math.cos(time * 2 * Math.PI.toFloat() + 1f))
                )
            )

            // Distant Stars
            repeat(30) { i ->
                val x = (width * ((i * 13) % 100) / 100f)
                val y = (height * ((i * 17) % 100) / 100f)
                val alpha = (0.1f + 0.4f * kotlin.math.sin(time * 2 * Math.PI.toFloat() + i)).coerceIn(0f, 1f)
                drawCircle(
                    color = Color.White.copy(alpha = alpha),
                    radius = 2f,
                    center = Offset(x, y)
                )
            }
            
            // Bubbles (Floating Over Mesh)
            drawBubble(
                center = Offset(width * 0.2f, height * 0.4f + 50 * kotlin.math.sin(time * 2 * Math.PI.toFloat())),
                radius = 60f,
                color = Color(0xFF60A5FA)
            )
            drawBubble(
                center = Offset(width * 0.8f, height * 0.2f + 40 * kotlin.math.cos(time * 2 * Math.PI.toFloat())),
                radius = 40f,
                color = Color(0xFFA78BFA)
            )
            drawBubble(
                center = Offset(width * 0.6f, height * 0.8f + 60 * kotlin.math.sin(time * 2 * Math.PI.toFloat() + 2)),
                radius = 80f,
                color = Color(0xFFF472B6)
            )
        }
        content()
    }
}

private fun androidx.compose.ui.graphics.drawscope.DrawScope.drawBubble(
    center: Offset,
    radius: Float,
    color: Color
) {
    drawCircle(
        color = color.copy(alpha = 0.4f),
        radius = radius,
        center = center,
        style = Stroke(width = 1.5f)
    )
    drawCircle(
        brush = Brush.radialGradient(
            colors = listOf(color.copy(alpha = 0.1f), Color.Transparent),
            center = center,
            radius = radius
        ),
        radius = radius,
        center = center
    )
    drawCircle(
        color = Color.White.copy(alpha = 0.3f),
        radius = radius * 0.2f,
        center = Offset(center.x - radius * 0.3f, center.y - radius * 0.3f)
    )
}
