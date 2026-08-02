package com.example.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Functions
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.exp
import kotlin.math.sin

/**
 * Mathematically precise VED Logo renderer based on the multi-Gaussian modulated sine wave equations:
 * W(x) = 1.6 * sin(2.6x) * e^(-(x/2.2)^2) + 0.9 * sin(5.2x) * e^(-(x/1.8)^2) + 0.45 * sin(9.8x) * e^(-(x/1.4)^2)
 */
@Composable
fun VedMathLogoCanvas(
    modifier: Modifier = Modifier,
    animated: Boolean = true,
    amplitudeMultiplier: Float = 1.0f,
    a1: Float = 1.6f,
    w1: Float = 2.6f,
    s1: Float = 2.2f,
    a2: Float = 0.9f,
    w2: Float = 5.2f,
    s2: Float = 1.8f,
    a3: Float = 0.45f,
    w3: Float = 9.8f,
    s3: Float = 1.4f,
    showCenterBeam: Boolean = true,
    showFillGradient: Boolean = true
) {
    val infiniteTransition = rememberInfiniteTransition(label = "logo_math_phase")
    val timePhase by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = if (animated) (2f * Math.PI.toFloat()) else 0f,
        animationSpec = infiniteRepeatable(
            animation = tween(4000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "phase_angle"
    )

    Canvas(modifier = modifier) {
        val width = size.width
        val height = size.height
        val centerY = height / 2f

        val xMin = -6.2f
        val xMax = 6.2f
        val yMax = 2.5f

        val points = 250
        val dx = (xMax - xMin) / points

        // Calculate W(x) points
        val topPoints = ArrayList<Offset>(points + 1)
        val bottomPoints = ArrayList<Offset>(points + 1)

        for (i in 0..points) {
            val x = xMin + i * dx

            val g1 = exp(-((x / s1) * (x / s1)))
            val g2 = exp(-((x / s2) * (x / s2)))
            val g3 = exp(-((x / s3) * (x / s3)))

            val term1 = a1 * sin(w1 * x + timePhase) * g1
            val term2 = a2 * sin(w2 * x - timePhase * 0.6f) * g2
            val term3 = a3 * sin(w3 * x + timePhase * 1.2f) * g3

            val baseWx = (term1 + term2 + term3) * amplitudeMultiplier

            // Perlin/Harmonic noise displacement N(x) for holographic bloom enhancement W_final(x) = W(x) + 0.02 * N(x)
            val nx = sin(18.5f * x + timePhase * 2.2f) * kotlin.math.cos(7.3f * x - timePhase * 1.1f)
            val wxFinal = baseWx + 0.02f * nx

            val px = ((x - xMin) / (xMax - xMin)) * width
            val pyTop = centerY - (wxFinal / yMax) * (centerY * 0.82f)
            val pyBottom = centerY + (wxFinal / yMax) * (centerY * 0.82f)

            topPoints.add(Offset(px, pyTop))
            bottomPoints.add(Offset(px, pyBottom))
        }

        // Build Paths
        val topPath = Path().apply {
            if (topPoints.isNotEmpty()) {
                moveTo(topPoints[0].x, topPoints[0].y)
                for (i in 1 until topPoints.size) {
                    lineTo(topPoints[i].x, topPoints[i].y)
                }
            }
        }

        val bottomPath = Path().apply {
            if (bottomPoints.isNotEmpty()) {
                moveTo(bottomPoints[0].x, bottomPoints[0].y)
                for (i in 1 until bottomPoints.size) {
                    lineTo(bottomPoints[i].x, bottomPoints[i].y)
                }
            }
        }

        val filledPath = Path().apply {
            if (topPoints.isNotEmpty() && bottomPoints.isNotEmpty()) {
                moveTo(topPoints[0].x, topPoints[0].y)
                for (i in 1 until topPoints.size) {
                    lineTo(topPoints[i].x, topPoints[i].y)
                }
                for (i in bottomPoints.size - 1 downTo 0) {
                    lineTo(bottomPoints[i].x, bottomPoints[i].y)
                }
                close()
            }
        }

        // 1. Layer 1: Outer Glow Blur (#6D4CFF - 15% opacity)
        drawPath(
            path = topPath,
            color = Color(0xFF6D4CFF).copy(alpha = 0.20f),
            style = Stroke(width = size.width * 0.08f, cap = StrokeCap.Round, join = StrokeJoin.Round)
        )
        drawPath(
            path = bottomPath,
            color = Color(0xFF6D4CFF).copy(alpha = 0.20f),
            style = Stroke(width = size.width * 0.08f, cap = StrokeCap.Round, join = StrokeJoin.Round)
        )

        // 2. Layer 2: Inner Glow Blur (#A865FF - 40% opacity)
        drawPath(
            path = topPath,
            color = Color(0xFFA865FF).copy(alpha = 0.45f),
            style = Stroke(width = size.width * 0.04f, cap = StrokeCap.Round, join = StrokeJoin.Round)
        )
        drawPath(
            path = bottomPath,
            color = Color(0xFFA865FF).copy(alpha = 0.45f),
            style = Stroke(width = size.width * 0.04f, cap = StrokeCap.Round, join = StrokeJoin.Round)
        )

        // 3. Layer 3: Fill Gradient between upper & lower mirrored waves
        if (showFillGradient) {
            drawPath(
                path = filledPath,
                brush = Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF6A4DFF).copy(alpha = 0.30f),
                        Color(0xFF8A5BFF).copy(alpha = 0.65f),
                        Color(0xFFD8B4FE).copy(alpha = 0.85f),
                        Color(0xFF8A5BFF).copy(alpha = 0.65f),
                        Color(0xFF6A4DFF).copy(alpha = 0.30f)
                    )
                )
            )
        }

        // 4. Layer 4: Center Light Line y=0
        if (showCenterBeam) {
            drawLine(
                brush = Brush.horizontalGradient(
                    colors = listOf(
                        Color.Transparent,
                        Color(0xFF8A5BFF).copy(alpha = 0.5f),
                        Color.White,
                        Color(0xFFD8B4FE),
                        Color.White,
                        Color(0xFF8A5BFF).copy(alpha = 0.5f),
                        Color.Transparent
                    )
                ),
                start = Offset(0f, centerY),
                end = Offset(width, centerY),
                strokeWidth = size.height * 0.035f,
                cap = StrokeCap.Round
            )
        }

        // 5. Layer 5: Main Waveform Outline Lines (Gradient #6A4DFF -> #8A5BFF -> #D8B4FE -> White)
        val strokeGradient = Brush.horizontalGradient(
            colors = listOf(
                Color(0xFF6A4DFF),
                Color(0xFF8A5BFF),
                Color(0xFFD8B4FE),
                Color.White,
                Color(0xFFD8B4FE),
                Color(0xFF8A5BFF),
                Color(0xFF6A4DFF)
            )
        )

        drawPath(
            path = topPath,
            brush = strokeGradient,
            style = Stroke(width = size.width * 0.022f, cap = StrokeCap.Round, join = StrokeJoin.Round)
        )
        drawPath(
            path = bottomPath,
            brush = strokeGradient,
            style = Stroke(width = size.width * 0.022f, cap = StrokeCap.Round, join = StrokeJoin.Round)
        )

        // 6. Layer 6: Bright Highlight Core Stroke
        drawPath(
            path = topPath,
            color = Color.White.copy(alpha = 0.7f),
            style = Stroke(width = size.width * 0.008f, cap = StrokeCap.Round, join = StrokeJoin.Round)
        )
        drawPath(
            path = bottomPath,
            color = Color.White.copy(alpha = 0.7f),
            style = Stroke(width = size.width * 0.008f, cap = StrokeCap.Round, join = StrokeJoin.Round)
        )
    }
}

/**
 * Standalone Rounded Icon Card component featuring the VED Mathematical Logo.
 * Designed for step 13 (Background #0B1020, Corner Radius 28%).
 */
@Composable
fun VedMathLogoIconCard(
    modifier: Modifier = Modifier,
    size: Dp = 96.dp,
    animated: Boolean = true,
    showBrandText: Boolean = false,
    onClick: (() -> Unit)? = null
) {
    val cornerRadius = size * 0.28f

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = if (onClick != null) modifier.clickable { onClick() } else modifier
    ) {
        Box(
            modifier = Modifier
                .size(size)
                .clip(RoundedCornerShape(cornerRadius))
                .background(Color(0xFF0B1020))
                .border(
                    1.2.dp,
                    Brush.linearGradient(
                        colors = listOf(
                            Color(0xFF8B5CF6).copy(alpha = 0.6f),
                            Color(0xFF1E2442),
                            Color(0xFF6D4CFF).copy(alpha = 0.4f)
                        )
                    ),
                    RoundedCornerShape(cornerRadius)
                )
                .padding(size * 0.12f),
            contentAlignment = Alignment.Center
        ) {
            VedMathLogoCanvas(
                modifier = Modifier.fillMaxSize(),
                animated = animated
            )
        }

        if (showBrandText) {
            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text = "VED",
                color = Color.White,
                fontSize = (size.value * 0.22f).sp,
                fontWeight = FontWeight.ExtraBold,
                letterSpacing = 4.sp
            )
            Text(
                text = "INTELLIGENCE • VOICE • EVOLUTION",
                color = Color(0xFFA78BFA),
                fontSize = (size.value * 0.09f).sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.2.sp
            )
        }
    }
}

/**
 * Interactive Equation Inspector Sheet / Card showing all 14 steps, formula parameters,
 * and live math wave parameter controls!
 */
@Composable
fun VedMathLogoInspectorCard(
    modifier: Modifier = Modifier
) {
    var a1 by remember { mutableFloatStateOf(1.6f) }
    var w1 by remember { mutableFloatStateOf(2.6f) }
    var s1 by remember { mutableFloatStateOf(2.2f) }

    var a2 by remember { mutableFloatStateOf(0.9f) }
    var w2 by remember { mutableFloatStateOf(5.2f) }
    var s2 by remember { mutableFloatStateOf(1.8f) }

    var a3 by remember { mutableFloatStateOf(0.45f) }
    var w3 by remember { mutableFloatStateOf(9.8f) }
    var s3 by remember { mutableFloatStateOf(1.4f) }

    var isLiveAnimated by remember { mutableStateOf(true) }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(22.dp))
            .background(Color(0xFF0B1020))
            .border(1.dp, Color(0xFF1F2647), RoundedCornerShape(22.dp))
            .padding(18.dp)
            .testTag("ved_math_logo_inspector_card")
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
            // Header Title
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(Color(0xFF8B5CF6).copy(alpha = 0.2f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Functions,
                            contentDescription = "Math Logo",
                            tint = Color(0xFFC4B5FD),
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "VED Mathematical Logo",
                            color = Color.White,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Gaussian Modulated Waveform Function",
                            color = Color(0xFF9CA3AF),
                            fontSize = 11.sp
                        )
                    }
                }

                IconButton(
                    onClick = { isLiveAnimated = !isLiveAnimated },
                    modifier = Modifier
                        .size(36.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(Color(0xFF1F2647))
                ) {
                    Icon(
                        imageVector = Icons.Default.AutoAwesome,
                        contentDescription = "Toggle Animation",
                        tint = if (isLiveAnimated) Color(0xFF34D399) else Color.Gray,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            // Central Mathematical Preview Window
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp)
                    .clip(RoundedCornerShape(18.dp))
                    .background(Color(0xFF070A14))
                    .border(1.dp, Color(0xFF1E2442), RoundedCornerShape(18.dp))
                    .padding(16.dp),
                contentAlignment = Alignment.Center
            ) {
                VedMathLogoCanvas(
                    modifier = Modifier.fillMaxSize(),
                    animated = isLiveAnimated,
                    a1 = a1, w1 = w1, s1 = s1,
                    a2 = a2, w2 = w2, s2 = s2,
                    a3 = a3, w3 = w3, s3 = s3
                )
            }

            // Mathematical Formula Display
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(0xFF13182E))
                    .padding(12.dp)
            ) {
                Column {
                    Text(
                        text = "FORMULA:",
                        color = Color(0xFFA78BFA),
                        fontSize = 10.sp,
                        fontWeight = FontWeight.ExtraBold,
                        letterSpacing = 1.sp
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "W(x) = ${String.format("%.2f", a1)}·sin(${String.format("%.1f", w1)}x)·e^(-(x/${String.format("%.1f", s1)})²) + " +
                                "${String.format("%.2f", a2)}·sin(${String.format("%.1f", w2)}x)·e^(-(x/${String.format("%.1f", s2)})²) + " +
                                "${String.format("%.2f", a3)}·sin(${String.format("%.1f", w3)}x)·e^(-(x/${String.format("%.1f", s3)})²)",
                        color = Color.White,
                        fontSize = 11.5.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            // Parameter Adjustment Controls
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = "INTERACTIVE WAVE PARAMETERS",
                    color = Color(0xFF9CA3AF),
                    fontSize = 10.5.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )

                // Sine Wave 1 Controls
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Peak 1 (A1): ${String.format("%.2f", a1)}", color = Color(0xFFC4B5FD), fontSize = 11.sp, modifier = Modifier.weight(1f))
                    Slider(
                        value = a1,
                        onValueChange = { a1 = it },
                        valueRange = 0.5f..3.0f,
                        modifier = Modifier.weight(2f)
                    )
                }

                // Sine Wave 2 Controls
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Peak 2 (A2): ${String.format("%.2f", a2)}", color = Color(0xFFC4B5FD), fontSize = 11.sp, modifier = Modifier.weight(1f))
                    Slider(
                        value = a2,
                        onValueChange = { a2 = it },
                        valueRange = 0.2f..2.0f,
                        modifier = Modifier.weight(2f)
                    )
                }

                // Reset Button
                Button(
                    onClick = {
                        a1 = 1.6f; w1 = 2.6f; s1 = 2.2f
                        a2 = 0.9f; w2 = 5.2f; s2 = 1.8f
                        a3 = 0.45f; w3 = 9.8f; s3 = 1.4f
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1F2647)),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Default.Tune, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Reset Formula to Exact Logo Specification", fontSize = 11.5.sp)
                }
            }
        }
    }
}
