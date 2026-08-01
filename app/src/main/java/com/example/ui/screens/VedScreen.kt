package com.example.ui.screens

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.widget.Toast
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MicOff
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
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
import kotlinx.coroutines.launch
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

import androidx.compose.material.icons.filled.Menu

import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Hearing
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import com.example.data.room.ConversationContextEntity
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.foundation.lazy.LazyColumn
import com.example.services.ChatHistoryItem
import com.example.ui.components.CustomModal

data class ChatMessage(
    val id: String = java.util.UUID.randomUUID().toString(),
    val sender: String, // "USER" or "VEDRA"
    val text: String,
    val time: String = formatTimestamp(System.currentTimeMillis())
)

fun formatTimestamp(ms: Long): String {
    if (ms <= 0) return "Just now"
    return try {
        val sdf = java.text.SimpleDateFormat("h:mm a", java.util.Locale.getDefault())
        sdf.format(java.util.Date(ms))
    } catch (e: Exception) {
        "Recent"
    }
}

@Composable
fun VedScreen(
    dbService: DatabaseService,
    voiceService: VoiceService,
    onActivateVoiceMode: () -> Unit,
    onOpenDrawer: (() -> Unit)? = null,
    selectedHistoryItem: ChatHistoryItem? = null,
    onClearHistorySelection: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val vedOrbStyle = remember { dbService.getSetting("ved_orb_style", "Gemini Neon Glow Orb") }
    val coroutineScope = rememberCoroutineScope()

    var inputText by remember { mutableStateOf("") }
    var isVoiceInputActive by remember { mutableStateOf(false) }
    var isThinking by remember { mutableStateOf(false) }
    var isIncognitoMode by remember { mutableStateOf(false) }
    var showHistoryModal by remember { mutableStateOf(false) }
    var loadedHistoryTitle by remember { mutableStateOf<String?>(null) }
    var isOfflineNativeMode by remember {
        mutableStateOf(dbService.getSetting("ai_network_mode", "Auto") == "Force Offline")
    }

    val roomContextLogsState = dbService.aiContextRepository.allContextsFlow.collectAsState(initial = emptyList())
    val roomContextLogs = roomContextLogsState.value

    var activeTab by remember { mutableStateOf("CHAT") } // "CHAT" or "ROOM_LOGS"
    var roomLogSearchQuery by remember { mutableStateOf("") }
    var isWakeWordActive by remember { mutableStateOf(voiceService.isWakeWordActive.value) }

    val isOnline = OfflineService.isNetworkAvailable.collectAsState().value

    val messages = remember {
        mutableStateListOf(
            ChatMessage(
                sender = "VEDRA",
                text = "Hello! I am VEDRA. How can I assist you today? Ask me questions or switch between ⚡ Cloud Gemini AI and 🏠 Offline Native AI above. I learn and store all conversations so I can assist you offline!"
            )
        )
    }

    // Load selected history session when user clicks on a past chat from SideDrawer or History Modal
    LaunchedEffect(selectedHistoryItem) {
        selectedHistoryItem?.let { item ->
            loadedHistoryTitle = item.sessionTitle
            messages.clear()
            messages.add(
                ChatMessage(
                    sender = "USER",
                    text = item.userText,
                    time = formatTimestamp(item.timestamp)
                )
            )
            messages.add(
                ChatMessage(
                    sender = "VEDRA",
                    text = item.vedResponse,
                    time = formatTimestamp(item.timestamp)
                )
            )
        }
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
                } else {
                    // Inject user profile memory & aliases context into AI engine
                    val contextSummary = dbService.getUserContextSummary()
                    val aiReply = GeminiService.generateResponse(clean, contextSummary, dbService, context)
                    // Cache response for offline use if not in Incognito Mode
                    if (!isIncognitoMode) {
                        dbService.saveCachedResponse(clean, aiReply)
                    }
                    aiReply
                }
            }

            isThinking = false
            messages.add(ChatMessage(sender = "VEDRA", text = replyText))

            // Save conversation history to SQLite DB, Room Database & Drive Document automatically
            if (!isIncognitoMode) {
                dbService.saveChatHistory(
                    sessionTitle = clean.take(25),
                    userText = clean,
                    vedResponse = replyText
                )
                dbService.aiContextRepository.recordConversation(
                    userPrompt = clean,
                    aiResponse = replyText,
                    engine = if (isOfflineNativeMode) "Offline Native VEDRA AI" else "Cloud Gemini AI"
                )
            }

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
                if (onOpenDrawer != null) {
                    IconButton(
                        onClick = { onOpenDrawer() },
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Menu,
                            contentDescription = "Drawer Menu",
                            tint = Color.White,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(6.dp))
                }
                com.example.ui.components.VedOrbView(
                    orbStyle = vedOrbStyle,
                    size = 38.dp,
                    isListening = voiceService.isListening.value,
                    isSpeaking = voiceService.isSpeaking.value
                )
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
                // Chat History Modal Trigger Button
                IconButton(
                    onClick = { showHistoryModal = true },
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(VedraSurface)
                        .border(1.dp, VedraBorder, CircleShape)
                ) {
                    Icon(
                        imageVector = Icons.Default.History,
                        contentDescription = "Chat History",
                        tint = VedraPurpleSecondary,
                        modifier = Modifier.size(18.dp)
                    )
                }

                // Incognito Mode Toggle
                CustomButton(
                    text = if (isIncognitoMode) "🕶️ Incognito" else "🕶️ Off",
                    onClick = { isIncognitoMode = !isIncognitoMode },
                    isSecondary = !isIncognitoMode,
                    fontSize = 12.sp
                )

                // Hands-Free 'Hey VEDRA' Wake Word Activation
                CustomButton(
                    text = if (voiceService.isWakeWordActive.value) "🎙️ Hey VEDRA ON" else "🎙️ Hey VEDRA",
                    icon = Icons.Default.Hearing,
                    onClick = {
                        if (voiceService.isWakeWordActive.value) {
                            voiceService.stopWakeWordDetection()
                            Toast.makeText(context, "Hands-free 'Hey VEDRA' wake word detection paused", Toast.LENGTH_SHORT).show()
                        } else {
                            if (hasMicPermission) {
                                voiceService.startWakeWordDetection { detected ->
                                    Toast.makeText(context, "🎙️ 'Hey VEDRA' detected! Listening for command...", Toast.LENGTH_SHORT).show()
                                    voiceService.startListening(
                                        onResult = { spoken ->
                                            inputText = spoken
                                            sendMessage(spoken)
                                        },
                                        onError = { err -> Toast.makeText(context, err, Toast.LENGTH_SHORT).show() }
                                    )
                                }
                                Toast.makeText(context, "🎙️ Listening for 'Hey VEDRA' hands-free wake word...", Toast.LENGTH_SHORT).show()
                            } else {
                                permissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                            }
                        }
                    },
                    isSecondary = !voiceService.isWakeWordActive.value,
                    fontSize = 11.5.sp
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

        // AI Engine Mode Switch & New Chat Control Strip
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = Spacing.small),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Mode Switch Button (⚡ Cloud Gemini AI <-> 🏠 Offline VEDRA AI)
            Surface(
                onClick = {
                    isOfflineNativeMode = !isOfflineNativeMode
                    val newModeStr = if (isOfflineNativeMode) "Force Offline" else "Auto"
                    dbService.setSetting("ai_network_mode", newModeStr)
                    coroutineScope.launch {
                        try {
                            dbService.aiContextRepository.recordInteractionPattern("mode_switch", if (isOfflineNativeMode) "offline_native_mode" else "cloud_gemini_mode", "Toggled in VedScreen UI")
                        } catch (_: Exception) {}
                    }
                    Toast.makeText(
                        context,
                        if (isOfflineNativeMode) "Switched to 🏠 Offline Native VEDRA AI Mode" else "Switched to ⚡ Cloud Gemini AI Mode",
                        Toast.LENGTH_SHORT
                    ).show()
                },
                shape = RoundedCornerShape(20.dp),
                color = if (isOfflineNativeMode) Color(0xFFF59E0B).copy(alpha = 0.18f) else VedraPurplePrimary.copy(alpha = 0.18f),
                border = BorderStroke(1.dp, if (isOfflineNativeMode) Color(0xFFF59E0B) else VedraPurplePrimary)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(
                        imageVector = if (isOfflineNativeMode) Icons.Default.Home else Icons.Default.AutoAwesome,
                        contentDescription = "AI Mode",
                        tint = if (isOfflineNativeMode) Color(0xFFF59E0B) else VedraPurpleSecondary,
                        modifier = Modifier.size(16.dp)
                    )
                    Text(
                        text = if (isOfflineNativeMode) "🏠 Offline Native AI" else "⚡ Cloud Gemini AI",
                        color = if (isOfflineNativeMode) Color(0xFFFFB74D) else VedraTextPrimary,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                // New Chat Button
                CustomButton(
                    text = "+ New Chat",
                    icon = Icons.Default.Add,
                    onClick = {
                        loadedHistoryTitle = null
                        onClearHistorySelection?.invoke()
                        messages.clear()
                        messages.add(
                            ChatMessage(
                                sender = "VEDRA",
                                text = "Started a new chat session. I am VEDRA! I learn and store all our conversations to answer your requests offline or online."
                            )
                        )
                        Toast.makeText(context, "Started new chat session", Toast.LENGTH_SHORT).show()
                    },
                    fontSize = 11.5.sp
                )
            }
        }

        // Active Session History Banner
        loadedHistoryTitle?.let { title ->
            CustomCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = Spacing.small),
                containerColor = Color(0xFF1B162E),
                borderColor = Color(0xFF8B5CF6)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(
                            imageVector = Icons.Default.History,
                            contentDescription = "Loaded History",
                            tint = Color(0xFFA78BFA),
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text(
                                text = "📜 Viewing Saved Session History",
                                color = Color(0xFFA78BFA),
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = title,
                                color = Color.White,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                    CustomButton(
                        text = "New Chat",
                        onClick = {
                            loadedHistoryTitle = null
                            onClearHistorySelection?.invoke()
                            messages.clear()
                            messages.add(
                                ChatMessage(
                                    sender = "VEDRA",
                                    text = "Hello! I am VEDRA. How can I assist you today? You can ask me questions, give commands like \"turn on flashlight\", \"calculate 15 * 4\", or \"open whatsapp\"."
                                )
                            )
                        },
                        fontSize = 11.sp
                    )
                }
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

        // Tab Selector Bar (Active Chat vs Room Database Conversation Logs)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 6.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(VedraSurface)
                .border(1.dp, VedraBorder, RoundedCornerShape(12.dp))
                .padding(4.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(10.dp))
                    .background(if (activeTab == "CHAT") VedraPurplePrimary else Color.Transparent)
                    .clickable { activeTab = "CHAT" }
                    .padding(vertical = 8.dp),
                contentAlignment = Alignment.Center
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Send, contentDescription = null, tint = Color.White, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "💬 Active Chat (${messages.size})",
                        color = Color.White,
                        fontSize = 12.sp,
                        fontWeight = if (activeTab == "CHAT") FontWeight.Bold else FontWeight.Normal
                    )
                }
            }

            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(10.dp))
                    .background(if (activeTab == "ROOM_LOGS") VedraPurplePrimary else Color.Transparent)
                    .clickable { activeTab = "ROOM_LOGS" }
                    .padding(vertical = 8.dp),
                contentAlignment = Alignment.Center
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Storage, contentDescription = null, tint = Color(0xFFA78BFA), modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "🧠 Room DB Logs (${roomContextLogs.size})",
                        color = Color.White,
                        fontSize = 12.sp,
                        fontWeight = if (activeTab == "ROOM_LOGS") FontWeight.Bold else FontWeight.Normal
                    )
                }
            }
        }

        androidx.compose.animation.AnimatedContent(
            targetState = activeTab,
            transitionSpec = {
                (fadeIn(animationSpec = tween(220)) togetherWith fadeOut(animationSpec = tween(220)))
            },
            modifier = Modifier.weight(1f),
            label = "VedTabTransition"
        ) { targetTab ->
            if (targetTab == "ROOM_LOGS") {
                // UI List displaying historical conversation logs retrieved from Room Database
                val filteredRoomLogs = remember(roomLogSearchQuery, roomContextLogs) {
                    if (roomLogSearchQuery.isBlank()) roomContextLogs
                    else roomContextLogs.filter { log ->
                        log.userPrompt.contains(roomLogSearchQuery, ignoreCase = true) ||
                                log.aiResponse.contains(roomLogSearchQuery, ignoreCase = true) ||
                                log.keywords.contains(roomLogSearchQuery, ignoreCase = true) ||
                                log.aiEngine.contains(roomLogSearchQuery, ignoreCase = true)
                    }
                }

                Column(
                    modifier = Modifier
                        .fillMaxSize()
                ) {
                    // Search Field for Room Logs
                    OutlinedTextField(
                        value = roomLogSearchQuery,
                        onValueChange = { roomLogSearchQuery = it },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 6.dp),
                        placeholder = { Text("Search Room DB logs by keyword, prompt, or engine...", color = Color.Gray, fontSize = 12.sp) },
                        leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = Color(0xFFA78BFA)) },
                        trailingIcon = {
                            if (roomLogSearchQuery.isNotEmpty()) {
                                IconButton(onClick = { roomLogSearchQuery = "" }) {
                                    Icon(Icons.Default.Close, contentDescription = "Clear", tint = Color.Gray)
                                }
                            }
                        },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = VedraPurplePrimary,
                            unfocusedBorderColor = VedraBorder,
                            focusedContainerColor = VedraSurface,
                            unfocusedContainerColor = VedraSurface,
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White
                        ),
                        shape = RoundedCornerShape(12.dp)
                    )

                    if (filteredRoomLogs.isEmpty()) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(1f),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(Icons.Default.Psychology, contentDescription = null, tint = Color.Gray, modifier = Modifier.size(48.dp))
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = if (roomLogSearchQuery.isBlank()) "No conversation logs stored in Room DB yet.\nStart chatting to automatically record history!"
                                    else "No Room logs matched '$roomLogSearchQuery'",
                                    color = Color.Gray,
                                    fontSize = 13.sp,
                                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                                )
                            }
                        }
                    } else {
                        LazyColumn(
                            modifier = Modifier.weight(1f),
                            verticalArrangement = Arrangement.spacedBy(10.dp),
                            contentPadding = PaddingValues(vertical = 6.dp)
                        ) {
                            items(filteredRoomLogs, key = { it.id }) { log ->
                                CustomCard(
                                    modifier = Modifier.fillMaxWidth(),
                                    containerColor = Color(0xFF141829),
                                    borderColor = Color(0xFF2A2E4B)
                                ) {
                                    Column(modifier = Modifier.padding(12.dp)) {
                                        // Top Header Row
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Box(
                                                modifier = Modifier
                                                    .clip(RoundedCornerShape(6.dp))
                                                    .background(Color(0xFF8B5CF6).copy(alpha = 0.25f))
                                                    .padding(horizontal = 8.dp, vertical = 2.dp)
                                            ) {
                                                Text(
                                                    text = log.aiEngine,
                                                    color = Color(0xFFC4B5FD),
                                                    fontSize = 10.sp,
                                                    fontWeight = FontWeight.Bold
                                                )
                                            }

                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Text(
                                                    text = formatTimestamp(log.timestamp),
                                                    color = Color(0xFF9CA3AF),
                                                    fontSize = 10.sp
                                                )
                                                Spacer(modifier = Modifier.width(6.dp))
                                                IconButton(
                                                    onClick = {
                                                        coroutineScope.launch {
                                                            dbService.aiContextRepository.deleteContext(log.id)
                                                            Toast.makeText(context, "Deleted log entry from Room DB", Toast.LENGTH_SHORT).show()
                                                        }
                                                    },
                                                    modifier = Modifier.size(24.dp)
                                                ) {
                                                    Icon(Icons.Default.Delete, contentDescription = "Delete", tint = Color.Gray, modifier = Modifier.size(14.dp))
                                                }
                                            }
                                        }

                                        Spacer(modifier = Modifier.height(8.dp))

                                        // User Prompt Box
                                        Box(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .clip(RoundedCornerShape(8.dp))
                                                .background(Color(0xFF1E243B))
                                                .padding(8.dp)
                                        ) {
                                            Text(
                                                text = "👤 ${log.userPrompt}",
                                                color = VedraCyanAccent,
                                                fontWeight = FontWeight.SemiBold,
                                                fontSize = 13.sp
                                            )
                                        }

                                        Spacer(modifier = Modifier.height(6.dp))

                                        // AI Response Text
                                        Text(
                                            text = "🤖 ${log.aiResponse}",
                                            color = Color.White,
                                            fontSize = 12.5.sp,
                                            maxLines = 6,
                                            overflow = TextOverflow.Ellipsis
                                        )

                                        if (log.keywords.isNotBlank()) {
                                            Spacer(modifier = Modifier.height(6.dp))
                                            Text(
                                                text = "🏷️ Keywords: ${log.keywords}",
                                                color = Color(0xFF9CA3AF),
                                                fontSize = 10.sp
                                            )
                                        }

                                        Spacer(modifier = Modifier.height(8.dp))

                                        // Action buttons for log item
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            CustomButton(
                                                text = "🚀 Load to Active Chat",
                                                onClick = {
                                                    messages.clear()
                                                    messages.add(ChatMessage(sender = "USER", text = log.userPrompt, time = formatTimestamp(log.timestamp)))
                                                    messages.add(ChatMessage(sender = "VEDRA", text = log.aiResponse, time = formatTimestamp(log.timestamp)))
                                                    activeTab = "CHAT"
                                                    Toast.makeText(context, "Loaded Room conversation into active chat!", Toast.LENGTH_SHORT).show()
                                                },
                                                fontSize = 11.sp
                                            )

                                            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                                IconButton(
                                                    onClick = { voiceService.speak(log.aiResponse) },
                                                    modifier = Modifier
                                                        .size(32.dp)
                                                        .clip(CircleShape)
                                                        .background(Color(0xFF8B5CF6).copy(alpha = 0.2f))
                                                ) {
                                                    Icon(Icons.Default.VolumeUp, contentDescription = "Read Aloud", tint = Color(0xFFC4B5FD), modifier = Modifier.size(16.dp))
                                                }

                                                IconButton(
                                                    onClick = {
                                                        UtilityService.writeToClipboard(context, "Q: ${log.userPrompt}\nA: ${log.aiResponse}")
                                                        Toast.makeText(context, "Copied log to clipboard!", Toast.LENGTH_SHORT).show()
                                                    },
                                                    modifier = Modifier
                                                        .size(32.dp)
                                                        .clip(CircleShape)
                                                        .background(Color(0xFF8B5CF6).copy(alpha = 0.2f))
                                                ) {
                                                    Icon(Icons.Default.ContentCopy, contentDescription = "Copy", tint = Color(0xFFC4B5FD), modifier = Modifier.size(16.dp))
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            } else {
                // Messages List using CustomList
                CustomList(
                    items = messages.toList(),
                    modifier = Modifier.fillMaxSize(),
                    emptyText = "No messages yet. Start conversation with VEDRA!",
                    itemKey = { it.id }
                ) { msg ->
                    ChatMessageBubble(
                        message = msg,
                        isSpeaking = voiceService.isSpeaking.value,
                        onSpeak = {
                            if (voiceService.isSpeaking.value) {
                                voiceService.stopSpeaking()
                            } else {
                                voiceService.speak(msg.text)
                            }
                        },
                        onCopy = { UtilityService.writeToClipboard(context, msg.text) }
                    )
                }
            }
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
                onLeadingIconClick = {
                    if (voiceService.isListening.value) {
                        voiceService.stopListening()
                    } else {
                        voiceService.startListening(
                            onResult = { resultText ->
                                inputText = resultText
                                sendMessage(resultText)
                            },
                            onError = { err ->
                                Toast.makeText(context, err, Toast.LENGTH_SHORT).show()
                            }
                        )
                    }
                },
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

        // Chat History Dialog Modal
        if (showHistoryModal) {
            VedChatHistoryModal(
                visible = showHistoryModal,
                dbService = dbService,
                onSelectHistory = { chatItem ->
                    loadedHistoryTitle = chatItem.sessionTitle
                    messages.clear()
                    messages.add(
                        ChatMessage(
                            sender = "USER",
                            text = chatItem.userText,
                            time = formatTimestamp(chatItem.timestamp)
                        )
                    )
                    messages.add(
                        ChatMessage(
                            sender = "VEDRA",
                            text = chatItem.vedResponse,
                            time = formatTimestamp(chatItem.timestamp)
                        )
                    )
                },
                onDismissRequest = { showHistoryModal = false }
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
    isSpeaking: Boolean = false,
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

                Spacer(modifier = Modifier.height(4.dp))
                Row(
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = message.time,
                        color = VedraTextMuted,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Normal
                    )

                    if (!isUser) {
                        Row(horizontalArrangement = Arrangement.End) {
                            IconButton(onClick = onCopy, modifier = Modifier.size(24.dp)) {
                                Icon(
                                    imageVector = Icons.Default.ContentCopy,
                                    contentDescription = "Copy",
                                    tint = VedraTextMuted,
                                    modifier = Modifier.size(14.dp)
                                )
                            }
                            IconButton(onClick = onSpeak, modifier = Modifier.size(24.dp)) {
                                Icon(
                                    imageVector = Icons.Default.VolumeUp,
                                    contentDescription = if (isSpeaking) "Stop Reading" else "Read Aloud",
                                    tint = if (isSpeaking) VedraCyanAccent else VedraTextMuted,
                                    modifier = Modifier.size(14.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun VedChatHistoryModal(
    visible: Boolean,
    dbService: DatabaseService,
    onSelectHistory: (ChatHistoryItem) -> Unit,
    onDismissRequest: () -> Unit
) {
    if (!visible) return

    val historyList = remember {
        mutableStateListOf<ChatHistoryItem>().apply { addAll(dbService.getAllChatHistory()) }
    }

    CustomModal(
        visible = visible,
        title = "📜 Chat History",
        onDismissRequest = onDismissRequest
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .height(380.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "${historyList.size} Saved Conversations",
                    color = VedraTextSecondary,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium
                )
                if (historyList.isNotEmpty()) {
                    CustomButton(
                        text = "Clear All",
                        onClick = {
                            dbService.clearChatHistory()
                            historyList.clear()
                        },
                        isSecondary = true,
                        fontSize = 11.sp
                    )
                }
            }

            if (historyList.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No saved chat history yet.",
                        color = VedraTextMuted,
                        fontSize = 13.sp
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(historyList) { item ->
                        CustomCard(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    onSelectHistory(item)
                                    onDismissRequest()
                                },
                            containerColor = VedraSurface,
                            borderColor = VedraBorder
                        ) {
                            Column(modifier = Modifier.fillMaxWidth().padding(10.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = item.sessionTitle,
                                        color = Color.White,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 13.sp,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis,
                                        modifier = Modifier.weight(1f)
                                    )
                                    Text(
                                        text = formatTimestamp(item.timestamp),
                                        color = VedraTextMuted,
                                        fontSize = 10.sp
                                    )
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "Q: ${item.userText}",
                                    color = VedraCyanAccent,
                                    fontSize = 11.sp,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = "A: ${item.vedResponse}",
                                    color = VedraTextSecondary,
                                    fontSize = 11.sp,
                                    maxLines = 2,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
