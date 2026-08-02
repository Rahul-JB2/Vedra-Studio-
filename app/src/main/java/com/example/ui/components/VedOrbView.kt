package com.example.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

@Composable
fun VedOrbView(
    orbStyle: String,
    modifier: Modifier = Modifier,
    size: Dp = 64.dp,
    isListening: Boolean = false,
    isSpeaking: Boolean = false,
    onClick: (() -> Unit)? = null
) {
    val infiniteTransition = rememberInfiniteTransition(label = "ved_orb_anim")

    val pulseScale by infiniteTransition.animateFloat(
        initialValue = if (isListening || isSpeaking) 0.92f else 0.96f,
        targetValue = if (isListening || isSpeaking) 1.18f else 1.04f,
        animationSpec = infiniteRepeatable(
            animation = tween(if (isListening || isSpeaking) 600 else 1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse_scale"
    )

    val rotationAngle by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(4000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "orb_rotation"
    )

    val waveHeight1 by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(450, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "wave1"
    )

    val waveHeight2 by infiniteTransition.animateFloat(
        initialValue = 0.6f,
        targetValue = 0.2f,
        animationSpec = infiniteRepeatable(
            animation = tween(550, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "wave2"
    )

    val baseModifier = modifier
        .size(size)
        .then(if (onClick != null) Modifier.clickable { onClick() } else Modifier)

    when (orbStyle) {
        "Mathematical Waveform Logo (VED)" -> {
            VedMathLogoIconCard(
                modifier = baseModifier,
                size = size,
                animated = true,
                showBrandText = false
            )
        }

        "Google Voice Waveform Orb" -> {
            // Google Voice / Assistant App Style: Vibrant 4-color dots & waveform
            val googleBlue = Color(0xFF4285F4)
            val googleRed = Color(0xFFEA4335)
            val googleYellow = Color(0xFFFBBC05)
            val googleGreen = Color(0xFF34A853)

            Box(
                modifier = baseModifier,
                contentAlignment = Alignment.Center
            ) {
                // Pulsing outer ripple background
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .scale(pulseScale)
                        .clip(CircleShape)
                        .background(
                            Brush.radialGradient(
                                listOf(googleBlue.copy(alpha = 0.35f), googleRed.copy(alpha = 0.25f), Color.Transparent)
                            )
                        )
                )

                // Central White Core Container
                Box(
                    modifier = Modifier
                        .size(size * 0.85f)
                        .clip(CircleShape)
                        .background(Color(0xFF131124))
                        .border(
                            1.5.dp,
                            Brush.sweepGradient(listOf(googleBlue, googleRed, googleYellow, googleGreen, googleBlue)),
                            CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    // 4 Animated Google Voice Color Waveform Bars
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(size * 0.06f),
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(horizontal = 4.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(width = size * 0.1f, height = size * 0.45f * waveHeight1)
                                .clip(CircleShape)
                                .background(googleBlue)
                        )
                        Box(
                            modifier = Modifier
                                .size(width = size * 0.1f, height = size * 0.45f * waveHeight2)
                                .clip(CircleShape)
                                .background(googleRed)
                        )
                        Box(
                            modifier = Modifier
                                .size(width = size * 0.1f, height = size * 0.45f * waveHeight1)
                                .clip(CircleShape)
                                .background(googleYellow)
                        )
                        Box(
                            modifier = Modifier
                                .size(width = size * 0.1f, height = size * 0.45f * waveHeight2)
                                .clip(CircleShape)
                                .background(googleGreen)
                        )
                    }
                }
            }
        }

        "Quantum Hologram Orb" -> {
            // Quantum Hologram Cyberpunk Cyan
            val cyanGlow = Color(0xFF00F0FF)
            val deepCyan = Color(0xFF06B6D4)

            Box(
                modifier = baseModifier,
                contentAlignment = Alignment.Center
            ) {
                // Outer Cyan Hologram Rotating Ring
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .scale(pulseScale)
                        .rotate(rotationAngle)
                        .clip(CircleShape)
                        .border(
                            2.dp,
                            Brush.sweepGradient(listOf(cyanGlow, Color.Transparent, deepCyan, cyanGlow)),
                            CircleShape
                        )
                )

                Box(
                    modifier = Modifier
                        .size(size * 0.82f)
                        .clip(CircleShape)
                        .background(Brush.radialGradient(listOf(deepCyan, Color(0xFF081B2B)))),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.AutoAwesome,
                        contentDescription = "Quantum Orb",
                        tint = cyanGlow,
                        modifier = Modifier.size(size * 0.4f)
                    )
                }
            }
        }

        "VED Purple Energy Orb" -> {
            // Classic VED Deep Purple Energy Core
            val purplePrimary = Color(0xFF8B5CF6)
            val purpleAccent = Color(0xFFA78BFA)

            Box(
                modifier = baseModifier,
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .scale(pulseScale)
                        .clip(CircleShape)
                        .background(
                            Brush.radialGradient(listOf(purplePrimary.copy(alpha = 0.4f), Color.Transparent))
                        )
                )

                Box(
                    modifier = Modifier
                        .size(size * 0.82f)
                        .clip(CircleShape)
                        .background(Brush.linearGradient(listOf(Color(0xFF2E1A47), Color(0xFF130B22))))
                        .border(1.5.dp, purpleAccent, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Mic,
                        contentDescription = "VED Purple Orb",
                        tint = Color.White,
                        modifier = Modifier.size(size * 0.42f)
                    )
                }
            }
        }

        else -> {
            // Default & "Gemini Neon Glow Orb" - Inspired by Gemini Live (Violet, Cyan, Pink, Amber)
            val geminiPurple = Color(0xFF9333EA)
            val geminiCyan = Color(0xFF06B6D4)
            val geminiPink = Color(0xFFEC4899)
            val geminiAmber = Color(0xFFF59E0B)

            Box(
                modifier = baseModifier,
                contentAlignment = Alignment.Center
            ) {
                // Outer Pulsing Gemini Multi-Color Glow
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .scale(pulseScale)
                        .rotate(rotationAngle)
                        .clip(CircleShape)
                        .background(
                            Brush.sweepGradient(
                                listOf(
                                    geminiPurple.copy(alpha = 0.5f),
                                    geminiCyan.copy(alpha = 0.5f),
                                    geminiPink.copy(alpha = 0.5f),
                                    geminiAmber.copy(alpha = 0.5f),
                                    geminiPurple.copy(alpha = 0.5f)
                                )
                            )
                        )
                )

                // Inner Glassy Core with Gradient Border
                Box(
                    modifier = Modifier
                        .size(size * 0.82f)
                        .clip(CircleShape)
                        .background(Color(0xFF0F0B1E))
                        .border(
                            1.8.dp,
                            Brush.rotateGradient(listOf(geminiCyan, geminiPink, geminiPurple)),
                            CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.AutoAwesome,
                        contentDescription = "Gemini Live Orb",
                        tint = Color.White,
                        modifier = Modifier.size(size * 0.42f)
                    )
                }
            }
        }
    }
}

// Helper Extension for Rotating Gradient
private fun Brush.Companion.rotateGradient(colors: List<Color>): Brush {
    return sweepGradient(colors)
}
