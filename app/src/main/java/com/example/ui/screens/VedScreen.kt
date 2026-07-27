package com.example.ui.screens

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MicOff
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.example.services.DatabaseService
import com.example.services.DirectActionService
import com.example.services.GeminiService
import com.example.services.OfflineIntentParser
import com.example.services.PluginService
import com.example.services.UtilityService
import com.example.services.VoiceService
import com.example.ui.components.CustomButton
import com.example.ui.components.CustomCard
import com.example.ui.components.CustomInput
import com.example.ui.components.CustomList
import com.example.ui.theme.Spacing
import com.example.ui.theme.VedraBackground
import com.example.ui.theme.VedraBorder
import com.example.ui.theme.VedraCyanAccent
import com.example.ui.theme.VedraPurplePrimary
import com.example.ui.theme.VedraPurpleSecondary
import com.example.ui.theme.VedraSurface
import com.example.ui.theme.VedraSurfaceVariant
import com.example.ui.theme.VedraTextMuted
import com.example.ui.theme.VedraTextPrimary
import com.example.ui.theme.VedraTextSecondary
import kotlinx.coroutines.launch
import androidx.compose.material.icons.filled.WifiOff
import androidx.compose.runtime.collectAsState
import com.example.services.OfflineService

data class ChatMessage(
    val id: String = java.util.UUID.randomUUID().toString(),
    val sender: String, // "USER" or "VEDRA"
    val text: String,
    val time: String = "9:30 AM"
)

@Composable
fun VedScreen(
    dbService: DatabaseService,
    voiceService: VoiceService,
    onActivateVoiceMode: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    var inputText by remember { mutableStateOf("") }
    var isVoiceInputActive by remember { mutableStateOf(false) }
    var isThinking by remember { mutableStateOf(false) }
    var isIncognitoMode by remember { mutableStateOf(false) }

    val isOnline = OfflineService.isNetworkAvailable.collectAsState().value

    val messages = remember {
        mutableStateListOf(
            ChatMessage(
                sender = "VEDRA",
                text = "Hello! I am VEDRA. How can I assist you today? You can ask me questions, give commands like \"turn on flashlight\", \"calculate 15 * 4\", or \"open whatsapp\"."
            )
        )
    }

    // Permission launcher for microphone
    var hasMicPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED
        )
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        hasMicPermission = isGranted
    }

    // Request permission on mount & start network monitor
    LaunchedEffect(Unit) {
        OfflineService.startMonitoring(context)
        if (!hasMicPermission) {
            permissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
        }
    }

    // Function to handle sending a message
    fun sendMessage(msgText: String) {
        val clean = msgText.trim()
        if (clean.isEmpty()) return

        // Turn-Taking VAD Cue: Instantly stop ongoing TTS playback when user interrupts
        voiceService.stopSpeaking()

        inputText = ""
        messages.add(ChatMessage(sender = "USER", text = clean))
        
        // Check & summarize chat history if exceeds 20 messages
        if (messages.size > 20) {
            val countToSummarize = messages.size - 6
            val summaryText = "⚡ Context Condensed: Summarized $countToSummarize earlier messages to optimize token memory."
            val preserved = messages.drop(countToSummarize)
            messages.clear()
            messages.add(ChatMessage(sender = "SYSTEM_SUMMARY", text = summaryText))
            messages.addAll(preserved)
        }

        isThinking = true

        coroutineScope.launch {
            val matchedPlugin = dbService.getPluginByTrigger(clean)

            val replyText = if (matchedPlugin != null) {
                PluginService.executePlugin(matchedPlugin)
            } else {
                // Direct Voice Action & Local NLP Parser (calls, sms, alarms, timers, notes, volume, flashlight, habits, etc.)
                val directAction = DirectActionService.processDirectVoiceAction(context, dbService, clean)

                if (directAction != null && directAction.isHandled) {
                    directAction.responseMessage
                } else if (!isOnline) {
                    // Network unavailable -> Query local SQLite cache or local search
                    val cached = dbService.searchCachedResponse(clean) ?: dbService.searchOfflineContent(clean)
                    cached ?: "Offline Mode: I couldn't find '$clean' in local memory or cached responses. Reconnect to internet for full Gemini AI features."
                } else {
                    // Inject user profile memory & aliases context into Gemini
                    val contextSummary = dbService.getUserContextSummary()
                    val geminiReply = GeminiService.generateResponse(clean, contextSummary)
                    // Cache response for offline use if not in Incognito Mode
                    if (!isIncognitoMode) {
                        dbService.saveCachedResponse(clean, geminiReply)
                    }
                    geminiReply
                }
            }

            isThinking = false
            messages.add(ChatMessage(sender = "VEDRA", text = replyText))

            // Continuous Voice Conversation Loop: read response aloud and auto-listen for follow-up
            if (isVoiceInputActive) {
                voiceService.speak(replyText) {
                    if (isVoiceInputActive && hasMicPermission) {
                        voiceService.startListening(
                            onResult = { spoken ->
                                inputText = spoken
                                sendMessage(spoken)
                            },
                            onError = { /* pause loop gracefully */ }
                        )
                    }
                }
            }
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(VedraBackground)
            .padding(horizontal = Spacing.medium)
    ) {
        // Chat Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = Spacing.medium),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(VedraPurplePrimary),
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = "VED", color = VedraTextPrimary, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                }
                Spacer(modifier = Modifier.width(Spacing.small))
                Column {
                    Text(text = "VEDRA AI", color = VedraTextPrimary, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    Text(
                        text = if (isOnline) "Online • Smart Assistant" else "Offline • Local Response Engine",
                        color = if (isOnline) VedraTextSecondary else Color(0xFFFFB74D),
                        fontSize = 11.sp
                    )
                }
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                // Incognito Mode Toggle
                CustomButton(
                    text = if (isIncognitoMode) "🕶️ Incognito" else "🕶️ Off",
                    onClick = { isIncognitoMode = !isIncognitoMode },
                    isSecondary = !isIncognitoMode,
                    fontSize = 12.sp
                )

                // Switch between Text & Voice Mode button
                CustomButton(
                    text = if (isVoiceInputActive) "Voice ON" else "Voice OFF",
                    icon = if (isVoiceInputActive) Icons.Default.Mic else Icons.Default.MicOff,
                    onClick = {
                        voiceService.stopSpeaking()
                        isVoiceInputActive = !isVoiceInputActive
                        if (isVoiceInputActive && hasMicPermission) {
                            voiceService.startListening(
                                onResult = { spoken ->
                                    inputText = spoken
                                    sendMessage(spoken)
                                },
                                onError = { /* handle */ }
                            )
                        } else {
                            voiceService.stopListening()
                        }
                    },
                    isSecondary = !isVoiceInputActive,
                    fontSize = 12.sp
                )
            }
        }

        // Incognito Banner
        if (isIncognitoMode) {
            CustomCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = Spacing.small),
                containerColor = Color(0xFF1E1A29),
                borderColor = VedraCyanAccent
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "🕶️ INCOGNITO ACTIVE — Session is stateless. No chat logs or memories are saved to database.",
                        color = VedraCyanAccent,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        // Offline Banner Card
        if (!isOnline) {
            CustomCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = Spacing.small),
                containerColor = Color(0xFF2A1C10),
                borderColor = Color(0xFFFF9800)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.WifiOff,
                        contentDescription = "Offline Mode",
                        tint = Color(0xFFFF9800),
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(Spacing.small))
                    Text(
                        text = "Offline Mode: Querying SQLite memory, routines & formula cache.",
                        color = Color(0xFFFFCC80),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }

        // Quick Suggestion Pills
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(Spacing.small),
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = Spacing.small)
        ) {
            val suggestions = listOf(
                "Explain photosynthesis",
                "What is formula for glucose?",
                "Turn on flashlight",
                "Calculate 15 * 4",
                "Convert 70 kg to lbs",
                "Copy to clipboard VEDRA 2026",
                "Open WhatsApp"
            )
            items(suggestions) { pill ->
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(16.dp))
                        .background(VedraSurface)
                        .border(1.dp, VedraBorder, RoundedCornerShape(16.dp))
                        .clickable { sendMessage(pill) }
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Text(text = pill, color = VedraTextSecondary, fontSize = 12.sp)
                }
            }
        }

        // Messages List using CustomList
        CustomList(
            items = messages.toList(),
            modifier = Modifier.weight(1f),
            emptyText = "No messages yet. Start conversation with VEDRA!",
            itemKey = { it.id }
        ) { msg ->
            ChatMessageBubble(
                message = msg,
                onSpeak = { voiceService.speak(msg.text) },
                onCopy = { UtilityService.writeToClipboard(context, msg.text) }
            )
        }

        if (isThinking) {
            Text(
                text = "VEDRA is thinking...",
                color = VedraPurpleSecondary,
                fontSize = 12.sp,
                modifier = Modifier.padding(start = 8.dp, bottom = 4.dp)
            )
        }

        if (voiceService.isListening.value) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(VedraPurplePrimary.copy(alpha = 0.2f))
                    .padding(horizontal = 12.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Mic,
                    contentDescription = null,
                    tint = VedraPurplePrimary,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(Spacing.small))
                Text(
                    text = "Listening... Speak your query or press stop",
                    color = VedraPurplePrimary,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        // Dynamic Quick Reply Chips
        val lastVedraMsg = messages.lastOrNull { it.sender == "VEDRA" }?.text
        val dynamicReplies = remember(lastVedraMsg) { computeDynamicQuickReplies(lastVedraMsg) }

        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(Spacing.small),
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp)
        ) {
            items(dynamicReplies) { reply ->
                CustomButton(
                    text = reply,
                    onClick = { sendMessage(reply) },
                    isSecondary = true,
                    fontSize = 12.sp
                )
            }
        }

        // Input Field using CustomInput
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = Spacing.medium),
            verticalAlignment = Alignment.CenterVertically
        ) {
            CustomInput(
                value = inputText,
                onValueChange = { inputText = it },
                placeholder = if (voiceService.isListening.value) "Listening..." else "Ask anything...",
                leadingIcon = Icons.Default.Mic,
                trailingIcon = {
                    IconButton(
                        onClick = { sendMessage(inputText) },
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Send,
                            contentDescription = "Send",
                            tint = VedraPurplePrimary
                        )
                    }
                },
                onSend = { sendMessage(inputText) },
                modifier = Modifier.weight(1f)
            )
        }
    }
}

fun computeDynamicQuickReplies(lastVedraMsgText: String?): List<String> {
    if (lastVedraMsgText.isNullOrBlank()) {
        return listOf("Tell me more", "Explain photosynthesis", "Turn on flashlight", "Daily Briefing")
    }
    val lower = lastVedraMsgText.lowercase()
    return when {
        lower.contains("?") || lower.contains("would you like") || lower.contains("do you want") ->
            listOf("Yes, please", "No, thanks", "Tell me more", "Why?")
        lower.contains("study") || lower.contains("exam") || lower.contains("physics") || lower.contains("topic") ->
            listOf("Add Study Task", "Create Flashcard", "Explain further", "Give an example")
        lower.contains("weather") ->
            listOf("Show forecast", "Daily Briefing", "Thank you")
        lower.contains("offline") ->
            listOf("Check saved profile", "Show routines", "Turn on flashlight")
        else ->
            listOf("Tell me more", "Summarize this", "Give an example", "What else can you do?")
    }
}

@Composable
fun ChatMessageBubble(
    message: ChatMessage,
    onSpeak: () -> Unit,
    onCopy: () -> Unit
) {
    if (message.sender == "SYSTEM_SUMMARY") {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            contentAlignment = Alignment.Center
        ) {
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(0xFF3E2723))
                    .border(1.dp, Color(0xFFFFB74D), RoundedCornerShape(12.dp))
                    .padding(horizontal = 12.dp, vertical = 6.dp)
            ) {
                Text(
                    text = message.text,
                    color = Color(0xFFFFE0B2),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        }
        return
    }

    val isUser = message.sender == "USER"

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalAlignment = if (isUser) Alignment.End else Alignment.Start
    ) {
        CustomCard(
            modifier = Modifier.fillMaxWidth(0.85f),
            borderColor = if (isUser) VedraPurplePrimary else VedraBorder,
            cornerShape = RoundedCornerShape(
                topStart = 16.dp,
                topEnd = 16.dp,
                bottomStart = if (isUser) 16.dp else 4.dp,
                bottomEnd = if (isUser) 4.dp else 16.dp
            )
        ) {
            Column {
                Text(
                    text = message.text,
                    color = VedraTextPrimary,
                    fontSize = 14.sp,
                    lineHeight = 20.sp
                )

                if (!isUser) {
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(
                        horizontalArrangement = Arrangement.End,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        IconButton(onClick = onCopy, modifier = Modifier.size(28.dp)) {
                            Icon(
                                imageVector = Icons.Default.ContentCopy,
                                contentDescription = "Copy",
                                tint = VedraTextMuted,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                        IconButton(onClick = onSpeak, modifier = Modifier.size(28.dp)) {
                            Icon(
                                imageVector = Icons.Default.VolumeUp,
                                contentDescription = "Read Aloud",
                                tint = VedraTextMuted,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}
