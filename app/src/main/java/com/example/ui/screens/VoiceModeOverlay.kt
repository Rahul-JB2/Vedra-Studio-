package com.example.ui.screens

import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Alarm
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.Keyboard
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MicOff
import androidx.compose.material.icons.filled.NoteAdd
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.VolumeOff
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.services.DatabaseService
import com.example.services.UtilityService
import com.example.services.VoiceService
import com.example.ui.theme.VedraCyanAccent
import com.example.ui.theme.VedraPinkAccent
import com.example.ui.theme.VedraPurplePrimary
import com.example.ui.theme.VedraTextMuted
import com.example.ui.theme.VedraTextPrimary
import com.example.ui.theme.VedraTextSecondary
import kotlinx.coroutines.launch

@Composable
fun VoiceModeOverlay(
    voiceService: VoiceService,
    onClose: () -> Unit,
    modifier: Modifier = Modifier
) {
    var isMuted by remember { mutableStateOf(false) }
    var isPaused by remember { mutableStateOf(false) }
    var showTextInput by remember { mutableStateOf(false) }
    var textInputQuery by remember { mutableStateOf("") }
    var activeTab by remember { mutableStateOf("voice") } // "voice" or "logs"
    var lastAiResponse by remember { mutableStateOf("") }

    val context = LocalContext.current
    val dbService = remember { DatabaseService(context) }
    val vedOrbStyle = remember(dbService.settingsVersion.intValue) { dbService.getSetting("ved_orb_style", "Gemini Neon Glow Orb") }
    val coroutineScope = androidx.compose.runtime.rememberCoroutineScope()

    var recentLogs by remember {
        mutableStateOf(dbService.getAllChatHistory().take(10))
    }

    fun handleVoiceResult(query: String) {
        if (query.isBlank()) return
        val res = UtilityService.parseAndExecuteLocalCommand(context, dbService, query)
        lastAiResponse = res.responseMessage
        dbService.saveChatHistory("Voice Query", query, res.responseMessage)
        coroutineScope.launch {
            dbService.aiContextRepository.recordConversation(query, res.responseMessage)
            recentLogs = dbService.getAllChatHistory().take(10)
        }
        voiceService.speak(res.responseMessage)
    }

    LaunchedEffect(Unit) {
        if (!voiceService.isListening.value && !voiceService.isSpeaking.value) {
            voiceService.startListening(
                onResult = { query -> handleVoiceResult(query) },
                onError = { err -> }
            )
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            voiceService.stopListening()
            voiceService.stopSpeaking()
        }
    }

    // Dynamic Soundwave / Pulse Animations
    val infiniteTransition = rememberInfiniteTransition(label = "vedra_pulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 0.96f,
        targetValue = 1.12f,
        animationSpec = infiniteRepeatable(
            animation = tween(850, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "scale"
    )

    val bar1Height by infiniteTransition.animateFloat(
        initialValue = 10f, targetValue = 28f,
        animationSpec = infiniteRepeatable(tween(400, easing = FastOutSlowInEasing), RepeatMode.Reverse), label = "b1"
    )
    val bar2Height by infiniteTransition.animateFloat(
        initialValue = 18f, targetValue = 36f,
        animationSpec = infiniteRepeatable(tween(550, easing = FastOutSlowInEasing), RepeatMode.Reverse), label = "b2"
    )
    val bar3Height by infiniteTransition.animateFloat(
        initialValue = 12f, targetValue = 32f,
        animationSpec = infiniteRepeatable(tween(480, easing = FastOutSlowInEasing), RepeatMode.Reverse), label = "b3"
    )

    // Semi-transparent dim background overlay (clicking outside closes popup)
    Box(
        modifier = modifier
            .testTag("voice_mode_overlay")
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.65f))
            .clickable { onClose() },
        contentAlignment = Alignment.Center
    ) {
        // VEDRA Voice Assistant Pop-Up Card
        Card(
            modifier = Modifier
                .widthIn(max = 330.dp)
                .fillMaxWidth(0.88f)
                .clickable(enabled = false) {}, // absorb clicks inside card
            shape = RoundedCornerShape(26.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF120E27)),
            border = androidx.compose.foundation.BorderStroke(
                1.5.dp,
                Brush.linearGradient(
                    listOf(
                        Color(0xFFA78BFA),
                        Color(0xFF06B6D4),
                        Color(0xFFEC4899)
                    )
                )
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 16.dp)
        ) {
            Column(
                modifier = Modifier
                    .padding(horizontal = 16.dp, vertical = 14.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Header Row: VEDRA Logo, Assistant Title, Status Dot & Close Button
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(24.dp)
                                .clip(CircleShape)
                                .background(Brush.radialGradient(listOf(Color(0xFF8B5CF6), Color(0xFF06B6D4)))),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.AutoAwesome,
                                contentDescription = "VEDRA AI",
                                tint = Color.White,
                                modifier = Modifier.size(14.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text(
                                text = "VEDRA Voice Assistant ✦",
                                color = Color.White,
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 13.sp
                            )
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(6.dp)
                                        .clip(CircleShape)
                                        .background(
                                            when {
                                                voiceService.isListening.value -> Color(0xFF10B981)
                                                voiceService.isSpeaking.value -> Color(0xFF06B6D4)
                                                else -> Color.Gray
                                            }
                                        )
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = when {
                                        voiceService.isListening.value -> "Listening..."
                                        voiceService.isSpeaking.value -> "VEDRA Speaking..."
                                        else -> "VED Live Ready"
                                    },
                                    color = VedraCyanAccent,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                    }

                    IconButton(
                        onClick = onClose,
                        modifier = Modifier.size(28.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close VEDRA Assistant",
                            tint = Color.White.copy(alpha = 0.8f),
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Tab Switcher Row (Voice Mode vs Recent Logs)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(0xFF1C1636))
                        .padding(2.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(10.dp))
                            .background(if (activeTab == "voice") VedraPurplePrimary else Color.Transparent)
                            .clickable { activeTab = "voice" }
                            .padding(vertical = 5.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "🎙️ Voice Mode",
                            color = Color.White,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(10.dp))
                            .background(if (activeTab == "logs") VedraPurplePrimary else Color.Transparent)
                            .clickable {
                                activeTab = "logs"
                                recentLogs = dbService.getAllChatHistory().take(10)
                            }
                            .padding(vertical = 5.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "📜 Recent Logs (${recentLogs.size})",
                            color = Color.White,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                if (activeTab == "voice") {
                    // Centered VED Animated Orb (Gemini / Google Voice style)
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        com.example.ui.components.VedOrbView(
                            orbStyle = vedOrbStyle,
                            size = 72.dp,
                            isListening = voiceService.isListening.value,
                            isSpeaking = voiceService.isSpeaking.value
                        )
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    // VEDRA Soundwave Visualizer Bar
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(42.dp)
                            .clip(RoundedCornerShape(14.dp))
                            .background(Color(0xFF17122E))
                            .padding(horizontal = 12.dp),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        val isWaveActive = voiceService.isListening.value || voiceService.isSpeaking.value
                        listOf(bar1Height, bar2Height, bar3Height, bar2Height, bar1Height).forEach { h ->
                            Box(
                                modifier = Modifier
                                    .padding(horizontal = 3.dp)
                                    .width(4.dp)
                                    .height(if (isWaveActive) h.dp else 8.dp)
                                    .clip(RoundedCornerShape(2.dp))
                                    .background(
                                        if (isWaveActive)
                                            Brush.verticalGradient(listOf(Color(0xFFA78BFA), Color(0xFF06B6D4)))
                                        else
                                            Brush.verticalGradient(listOf(Color.Gray.copy(alpha = 0.5f), Color.Gray.copy(alpha = 0.3f)))
                                    )
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Live Speech Transcription / AI Answer Box
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(14.dp))
                            .background(Color(0xFF181333))
                            .border(1.dp, Color(0xFF2C2352), RoundedCornerShape(14.dp))
                            .padding(10.dp)
                    ) {
                        Column {
                            if (voiceService.lastRecognizedText.value.isNotBlank()) {
                                Text(
                                    text = "You: \"${voiceService.lastRecognizedText.value}\"",
                                    color = VedraCyanAccent,
                                    fontSize = 11.5.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    maxLines = 2
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                            }

                            if (lastAiResponse.isNotBlank()) {
                                Text(
                                    text = lastAiResponse,
                                    color = Color.White,
                                    fontSize = 12.sp,
                                    lineHeight = 16.sp,
                                    maxLines = 3
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.End,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    IconButton(
                                        onClick = {
                                            val clipboard = context.getSystemService(android.content.Context.CLIPBOARD_SERVICE) as android.content.ClipboardManager
                                            val clip = android.content.ClipData.newPlainText("VEDRA Response", lastAiResponse)
                                            clipboard.setPrimaryClip(clip)
                                            Toast.makeText(context, "Response copied to clipboard!", Toast.LENGTH_SHORT).show()
                                        },
                                        modifier = Modifier.size(24.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.ContentCopy,
                                            contentDescription = "Copy Response",
                                            tint = VedraTextSecondary,
                                            modifier = Modifier.size(14.dp)
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(6.dp))
                                    IconButton(
                                        onClick = {
                                            if (voiceService.isSpeaking.value) {
                                                voiceService.stopSpeaking()
                                            } else {
                                                voiceService.speak(lastAiResponse)
                                            }
                                        },
                                        modifier = Modifier.size(24.dp)
                                    ) {
                                        Icon(
                                            imageVector = if (voiceService.isSpeaking.value) Icons.Default.VolumeOff else Icons.Default.VolumeUp,
                                            contentDescription = "Speak Response",
                                            tint = VedraPinkAccent,
                                            modifier = Modifier.size(15.dp)
                                        )
                                    }
                                }
                            } else {
                                Text(
                                    text = if (voiceService.isListening.value) "Speak command now... (e.g., 'turn on flashlight', 'set alarm')" else "Tap mic below or select quick prompt",
                                    color = VedraTextMuted,
                                    fontSize = 11.sp,
                                    textAlign = TextAlign.Center,
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Quick VED Shortcut Action Pills (Scrollable)
                    LazyRow(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        item {
                            QuickVedPill(icon = Icons.Default.FlashOn, label = "Flashlight") {
                                handleVoiceResult("turn on flashlight")
                            }
                        }
                        item {
                            QuickVedPill(icon = Icons.Default.Alarm, label = "Alarm 7 AM") {
                                handleVoiceResult("set alarm for 7 AM")
                            }
                        }
                        item {
                            QuickVedPill(icon = Icons.Default.NoteAdd, label = "Voice Note") {
                                handleVoiceResult("take note Buy groceries and study")
                            }
                        }
                        item {
                            QuickVedPill(icon = Icons.Default.Call, label = "Call Contact") {
                                handleVoiceResult("call mom")
                            }
                        }
                        item {
                            QuickVedPill(icon = Icons.Default.AutoAwesome, label = "What can you do?") {
                                handleVoiceResult("what can you do")
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Text Input Bar Toggleable View
                    AnimatedVisibility(visible = showTextInput) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            OutlinedTextField(
                                value = textInputQuery,
                                onValueChange = { textInputQuery = it },
                                placeholder = { Text("Type prompt...", color = VedraTextMuted, fontSize = 11.5.sp) },
                                modifier = Modifier.weight(1f),
                                singleLine = true,
                                shape = RoundedCornerShape(12.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedContainerColor = Color(0xFF1E173B),
                                    unfocusedContainerColor = Color(0xFF181230),
                                    focusedBorderColor = VedraPurplePrimary,
                                    unfocusedBorderColor = Color(0xFF332959),
                                    focusedTextColor = Color.White,
                                    unfocusedTextColor = Color.White
                                )
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            IconButton(
                                onClick = {
                                    if (textInputQuery.isNotBlank()) {
                                        handleVoiceResult(textInputQuery)
                                        textInputQuery = ""
                                        showTextInput = false
                                    }
                                },
                                modifier = Modifier
                                    .size(42.dp)
                                    .clip(CircleShape)
                                    .background(VedraPurplePrimary)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Send,
                                    contentDescription = "Send",
                                    tint = Color.White,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    }

                    // Main Action Row: Large Voice Microphone Button & Controls
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Keyboard Input Toggle Button
                        IconButton(
                            onClick = { showTextInput = !showTextInput },
                            modifier = Modifier
                                .size(38.dp)
                                .clip(CircleShape)
                                .background(if (showTextInput) VedraPurplePrimary else Color(0xFF261E47))
                        ) {
                            Icon(
                                imageVector = Icons.Default.Keyboard,
                                contentDescription = "Type Command",
                                tint = Color.White,
                                modifier = Modifier.size(18.dp)
                            )
                        }

                        // Central Large Voice Mic Button with Pulsing Halo
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier
                                .size(56.dp)
                                .scale(if (voiceService.isListening.value || voiceService.isSpeaking.value) pulseScale else 1f)
                                .clip(CircleShape)
                                .background(
                                    if (voiceService.isListening.value)
                                        Brush.radialGradient(listOf(Color(0xFF10B981), Color(0xFF059669)))
                                    else
                                        Brush.radialGradient(listOf(Color(0xFF8B5CF6), Color(0xFF6D28D9)))
                                )
                                .border(
                                    2.dp,
                                    if (voiceService.isListening.value) Color(0xFF34D399) else Color(0xFFA78BFA),
                                    CircleShape
                                )
                                .clickable {
                                    if (voiceService.isListening.value) {
                                        voiceService.stopListening()
                                    } else {
                                        voiceService.startListening(
                                            onResult = { query -> handleVoiceResult(query) },
                                            onError = { err -> }
                                        )
                                    }
                                }
                        ) {
                            Icon(
                                imageVector = if (voiceService.isMuted.value) Icons.Default.MicOff else Icons.Default.Mic,
                                contentDescription = "Voice Microphone",
                                tint = Color.White,
                                modifier = Modifier.size(28.dp)
                            )
                        }

                        // Mute / Unmute Button
                        IconButton(
                            onClick = { voiceService.toggleMute() },
                            modifier = Modifier
                                .size(38.dp)
                                .clip(CircleShape)
                                .background(if (voiceService.isMuted.value) Color(0xFFEF4444) else Color(0xFF261E47))
                        ) {
                            Icon(
                                imageVector = if (voiceService.isMuted.value) Icons.Default.MicOff else Icons.Default.Mic,
                                contentDescription = "Toggle Mute",
                                tint = Color.White,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Secondary Control Buttons (Pause/Resume & Close Done)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(10.dp))
                                .background(Color(0xFF261E47))
                                .clickable {
                                    isPaused = !isPaused
                                    if (isPaused) {
                                        voiceService.stopListening()
                                        voiceService.stopSpeaking()
                                    }
                                }
                                .padding(horizontal = 12.dp, vertical = 6.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = if (isPaused) Icons.Default.PlayArrow else Icons.Default.Pause,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = if (isPaused) "Resume" else "Pause",
                                    color = Color.White,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }

                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(10.dp))
                                .background(Brush.horizontalGradient(listOf(VedraPurplePrimary, Color(0xFF9333EA))))
                                .clickable {
                                    voiceService.stopListening()
                                    voiceService.stopSpeaking()
                                    onClose()
                                }
                                .padding(horizontal = 14.dp, vertical = 6.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "Done",
                                color = Color.White,
                                fontSize = 11.5.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                } else {
                    // RECENT LOGS TAB
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(max = 250.dp)
                    ) {
                        if (recentLogs.isEmpty()) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 28.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "No AI interaction logs found",
                                    color = VedraTextMuted,
                                    fontSize = 12.sp
                                )
                            }
                        } else {
                            LazyColumn(
                                verticalArrangement = Arrangement.spacedBy(6.dp),
                                modifier = Modifier.weight(1f)
                            ) {
                                items(recentLogs) { item ->
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clip(RoundedCornerShape(10.dp))
                                            .background(Color(0xFF1B1633))
                                            .border(1.dp, Color(0xFF2D2550), RoundedCornerShape(10.dp))
                                            .clickable {
                                                lastAiResponse = item.vedResponse
                                                voiceService.speak(item.vedResponse)
                                            }
                                            .padding(10.dp)
                                    ) {
                                        Column {
                                            Text(
                                                text = "Q: ${item.userText}",
                                                color = VedraCyanAccent,
                                                fontSize = 11.5.sp,
                                                fontWeight = FontWeight.Bold,
                                                maxLines = 1
                                            )
                                            Spacer(modifier = Modifier.height(3.dp))
                                            Text(
                                                text = item.vedResponse,
                                                color = Color.White.copy(alpha = 0.9f),
                                                fontSize = 11.sp,
                                                maxLines = 2
                                            )
                                        }
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(10.dp))
                                .background(Color(0xFF261E47))
                                .clickable { activeTab = "voice" }
                                .padding(vertical = 7.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "← Back to Voice Mode",
                                color = Color.White,
                                fontSize = 11.5.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun QuickVedPill(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(16.dp))
            .background(Color(0xFF231B42))
            .border(1.dp, Color(0xFF392E66), RoundedCornerShape(16.dp))
            .clickable { onClick() }
            .padding(horizontal = 10.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            tint = VedraCyanAccent,
            modifier = Modifier.size(13.dp)
        )
        Spacer(modifier = Modifier.width(4.dp))
        Text(
            text = label,
            color = VedraTextPrimary,
            fontSize = 10.5.sp,
            fontWeight = FontWeight.Medium
        )
    }
}


