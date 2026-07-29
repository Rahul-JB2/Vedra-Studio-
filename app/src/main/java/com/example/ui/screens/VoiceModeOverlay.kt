package com.example.ui.screens

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MicOff
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.services.VoiceService
import com.example.ui.components.CustomButton
import com.example.ui.components.CustomCard
import com.example.ui.theme.Spacing
import com.example.ui.theme.VedraBackground
import com.example.ui.theme.VedraBorder
import com.example.ui.theme.VedraCyanAccent
import com.example.ui.theme.VedraPinkAccent
import com.example.ui.theme.VedraPurplePrimary
import com.example.ui.theme.VedraPurpleSecondary
import com.example.ui.theme.VedraSurface
import com.example.ui.theme.VedraTextMuted
import com.example.ui.theme.VedraTextPrimary
import com.example.ui.theme.VedraTextSecondary

@Composable
fun VoiceModeOverlay(
    voiceService: VoiceService,
    onClose: () -> Unit,
    modifier: Modifier = Modifier
) {
    var isMuted by remember { mutableStateOf(false) }
    var isPaused by remember { mutableStateOf(false) }

    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 0.92f,
        targetValue = 1.15f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "scale"
    )

    Box(
        modifier = modifier
            .testTag("voice_mode_overlay")
            .fillMaxSize()
            .background(VedraBackground.copy(alpha = 0.98f))
            .padding(Spacing.medium)
    ) {
        // Top Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = Spacing.medium),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "VOICE MODE",
                    color = VedraTextPrimary,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                )
                Text(
                    text = "Talk with VEDRA",
                    color = VedraTextSecondary,
                    fontSize = 12.sp
                )
            }

            IconButton(onClick = onClose) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Close Voice Mode",
                    tint = VedraTextPrimary
                )
            }
        }

        // Center Content
        Column(
            modifier = Modifier
                .align(Alignment.Center)
                .padding(horizontal = Spacing.medium),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Glowing Orb
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.size(200.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(180.dp)
                        .scale(if (voiceService.isListening.value || voiceService.isSpeaking.value) pulseScale else 1f)
                        .clip(CircleShape)
                        .background(
                            Brush.radialGradient(
                                colors = listOf(
                                    VedraPurplePrimary.copy(alpha = 0.6f),
                                    VedraCyanAccent.copy(alpha = 0.3f),
                                    Color.Transparent
                                )
                            )
                        )
                )

                Box(
                    modifier = Modifier
                        .size(130.dp)
                        .clip(CircleShape)
                        .background(
                            Brush.linearGradient(
                                listOf(VedraPurplePrimary, VedraPurpleSecondary, VedraCyanAccent)
                            )
                        )
                        .border(3.dp, VedraTextPrimary.copy(alpha = 0.8f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (isMuted) Icons.Default.MicOff else Icons.Default.Mic,
                        contentDescription = null,
                        tint = VedraTextPrimary,
                        modifier = Modifier.size(56.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(Spacing.large))

            Text(
                text = when {
                    isMuted -> "Muted"
                    isPaused -> "Paused"
                    voiceService.isListening.value -> "Listening..."
                    voiceService.isSpeaking.value -> "VEDRA Speaking..."
                    else -> "Listening... Say \"Ved\" to wake me up"
                },
                color = VedraTextPrimary,
                fontWeight = FontWeight.Bold,
                fontSize = 20.sp,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(Spacing.small))

            if (voiceService.lastRecognizedText.value.isNotBlank()) {
                Text(
                    text = "\"${voiceService.lastRecognizedText.value}\"",
                    color = VedraCyanAccent,
                    fontSize = 14.sp,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(horizontal = Spacing.medium)
                )
            } else {
                Text(
                    text = "Say \"turn on flashlight\" or ask any question",
                    color = VedraTextMuted,
                    fontSize = 12.sp,
                    textAlign = TextAlign.Center
                )
            }

            Spacer(modifier = Modifier.height(Spacing.extraLarge))

            // Control Buttons Row
            Row(
                horizontalArrangement = Arrangement.spacedBy(Spacing.medium),
                verticalAlignment = Alignment.CenterVertically
            ) {
                CustomButton(
                    text = if (voiceService.isMuted.value) "Unmute TTS" else "Mute TTS",
                    icon = if (voiceService.isMuted.value) Icons.Default.MicOff else Icons.Default.Mic,
                    onClick = {
                        voiceService.toggleMute()
                    },
                    isSecondary = !voiceService.isMuted.value
                )

                CustomButton(
                    text = if (isPaused) "Resume" else "Pause",
                    icon = Icons.Default.Pause,
                    onClick = {
                        isPaused = !isPaused
                        if (isPaused) {
                            voiceService.stopListening()
                            voiceService.stopSpeaking()
                        }
                    },
                    isSecondary = true
                )

                CustomButton(
                    text = "Stop",
                    icon = Icons.Default.Stop,
                    onClick = {
                        voiceService.stopListening()
                        voiceService.stopSpeaking()
                        onClose()
                    }
                )
            }
        }

        // Bottom Voice Tip
        CustomCard(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = Spacing.medium)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "💡 VOICE TIP: You can give natural voice commands or ask any study & math question in voice mode.",
                    color = VedraTextSecondary,
                    fontSize = 12.sp
                )
            }
        }
    }
}
