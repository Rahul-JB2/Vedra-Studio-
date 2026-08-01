package com.example.ui.components

import android.os.Build
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Reusable Compose Modifier implementing a high-performance blurred background effect
 * with adjustable opacity, gradient tinting, and adaptive border highlighting for VEDRA glassmorphism UI components.
 */
fun Modifier.glassmorphicBlur(
    blurRadius: Dp = 16.dp,
    opacity: Float = 0.16f,
    borderOpacity: Float = 0.28f,
    tintColor: Color = Color.White,
    borderColor: Color = Color.White,
    shape: Shape = RoundedCornerShape(18.dp)
): Modifier {
    val clampedOpacity = opacity.coerceIn(0.02f, 0.90f)
    val clampedBorderAlpha = borderOpacity.coerceIn(0.05f, 0.90f)

    val backgroundBrush = Brush.linearGradient(
        colors = listOf(
            tintColor.copy(alpha = (clampedOpacity * 1.1f).coerceIn(0.04f, 0.80f)),
            tintColor.copy(alpha = (clampedOpacity * 0.35f).coerceIn(0.01f, 0.50f))
        )
    )

    val borderBrush = Brush.linearGradient(
        colors = listOf(
            borderColor.copy(alpha = clampedBorderAlpha),
            borderColor.copy(alpha = (clampedBorderAlpha * 0.25f).coerceIn(0.02f, 0.50f))
        )
    )

    val baseModifier = this
        .clip(shape)
        .background(backgroundBrush)
        .border(1.dp, borderBrush, shape)

    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        baseModifier.blur(blurRadius)
    } else {
        baseModifier
    }
}

/**
 * Reusable Glassmorphic Blur Box Container that combines background blurring,
 * smooth animation transitions on opacity, and subtle glass borders.
 */
@Composable
fun GlassmorphicBlurBox(
    modifier: Modifier = Modifier,
    blurRadius: Dp = 16.dp,
    opacity: Float = 0.16f,
    borderOpacity: Float = 0.28f,
    tintColor: Color = Color(0xFF8B5CF6),
    borderColor: Color = Color(0xFFA78BFA),
    shape: Shape = RoundedCornerShape(18.dp),
    content: @Composable BoxScope.() -> Unit
) {
    val animOpacity by animateFloatAsState(
        targetValue = opacity,
        animationSpec = tween(500),
        label = "GlassOpacity"
    )
    val animBorderOpacity by animateFloatAsState(
        targetValue = borderOpacity,
        animationSpec = tween(500),
        label = "GlassBorderOpacity"
    )

    Box(
        modifier = modifier.glassmorphicBlur(
            blurRadius = blurRadius,
            opacity = animOpacity,
            borderOpacity = animBorderOpacity,
            tintColor = tintColor,
            borderColor = borderColor,
            shape = shape
        ),
        content = content
    )
}
