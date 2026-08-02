package com.example.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Canvas
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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.HelpOutline
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.RadioButtonUnchecked
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.ThumbDown
import androidx.compose.material.icons.filled.ThumbDownOffAlt
import androidx.compose.material.icons.filled.ThumbUp
import androidx.compose.material.icons.filled.ThumbUpOffAlt
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.services.DatabaseService
import com.example.services.GeminiService
import com.example.services.UtilityService
import com.example.services.VoiceService
import com.example.ui.components.VedMathLogoCanvas
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.cos
import kotlin.math.sin

// VED Sub-Screen View Modes
enum class VedViewMode {
    HOME,            // Panel 1: Main Home Screen (Greeting, Glowing Squircle, 2x2 Quick Actions)
    CHAT,            // Panel 2: Conversation View + LaTeX/Math Cards + Action Toolbar + Suggestion Chips
    THINKING,        // Panel 3: Step-by-Step Reasoning Progress Card
    HISTORY,         // Panel 4: Conversation History with Filter Chips
    VOICE_LISTENING, // Panel 5: Fullscreen Voice Listening Mode with Audio Waveform
    SETTINGS         // Panel 6: VED AI Settings Screen
}

// Chat Message Model
data class VedMessage(
    val id: String,
    val sender: String, // "user" or "ved"
    val content: String,
    val mathFormula1: String? = null,
    val mathFormula2: String? = null,
    val timestamp: String = "09:30 AM"
)

// History Item Model
data class VedHistoryItem(
    val id: String,
    val title: String,
    val subtitle: String,
    val time: String,
    val category: String, // "Chats", "Notes", "Images", "Voice"
    val dateGroup: String, // "Today", "Yesterday", "2 May 2025"
    val icon: ImageVector,
    val iconColor: Color
)

@Composable
fun VedAssistantScreen(
    dbService: DatabaseService,
    voiceService: VoiceService,
    onActivateVoice: () -> Unit,
    onOpenDrawer: () -> Unit,
    autoStartVoice: Boolean = false,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    // Active View Mode State
    var currentViewMode by remember { mutableStateOf(VedViewMode.HOME) }

    // User Input Query
    var inputQuery by remember { mutableStateOf("") }

    // Chat Messages State
    val chatMessages = remember {
        mutableStateListOf(
            VedMessage(
                id = "msg_1",
                sender = "user",
                content = "Explain Newton's Second Law with example.",
                timestamp = "09:30 AM"
            ),
            VedMessage(
                id = "msg_2",
                sender = "ved",
                content = "Newton's Second Law states that the rate of change of momentum of an object is directly proportional to the net force acting on it and occurs in the direction of the force.",
                mathFormula1 = "F = ma",
                mathFormula2 = "a = F/m = 10/2 = 5 m/s²",
                timestamp = "09:31 AM"
            )
        )
    }

    // Thinking State Progress
    var thinkingQuery by remember { mutableStateOf("Create a study plan for IIT JEE preparation.") }
    var thinkingStepIndex by remember { mutableIntStateOf(0) }

    // Voice Listening State
    var isVoiceActive by remember { mutableStateOf(false) }

    // Settings States
    var aiPersonality by remember { mutableStateOf("Friendly & Smart") }
    var memoryContext by remember { mutableStateOf("High") }
    var voiceLanguage by remember { mutableStateOf("English (India)") }
    var responseStyle by remember { mutableStateOf("Detailed") }
    var imageGenEnabled by remember { mutableStateOf(true) }
    var webSearchEnabled by remember { mutableStateOf(true) }
    var showClearHistoryDialog by remember { mutableStateOf(false) }

    // History Items List
    val historyItems = remember {
        mutableStateListOf(
            VedHistoryItem("h1", "Newton's Second Law", "Explain Newton's Second Law with example.", "09:30 AM", "Chats", "Today", Icons.Default.AutoAwesome, Color(0xFFA855F7)),
            VedHistoryItem("h2", "IIT JEE Study Plan", "Create a study plan for IIT JEE preparation.", "08:45 AM", "Notes", "Today", Icons.Default.School, Color(0xFF3B82F6)),
            VedHistoryItem("h3", "Photosynthesis Process", "Explain photosynthesis in detail.", "07:20 AM", "Chats", "Today", Icons.Default.Psychology, Color(0xFF10B981)),
            VedHistoryItem("h4", "Quadratic Equation", "Solve x² + 5x + 6 = 0", "09:10 PM", "Notes", "Yesterday", Icons.Default.Calculate, Color(0xFF3B82F6)),
            VedHistoryItem("h5", "Chemical Bonding", "Explain ionic and covalent bonding.", "06:30 PM", "Chats", "Yesterday", Icons.Default.AutoAwesome, Color(0xFFF97316)),
            VedHistoryItem("h6", "Motivation", "Give me some motivational quotes.", "05:15 PM", "Voice", "Yesterday", Icons.Default.GraphicEq, Color(0xFFEF4444)),
            VedHistoryItem("h7", "Thermodynamics Summary", "Explain 1st law of thermodynamics.", "04:10 PM", "Chats", "2 May 2025", Icons.Default.Description, Color(0xFF06B6D4))
        )
    }

    // Helper: Send Query and trigger thinking / answer flow
    fun sendQuery(text: String) {
        if (text.isBlank()) return
        inputQuery = ""

        // Add user message
        chatMessages.add(
            VedMessage(
                id = "msg_${System.currentTimeMillis()}",
                sender = "user",
                content = text,
                timestamp = "Just now"
            )
        )

        // Set query for thinking screen & switch to THINKING view
        thinkingQuery = text
        thinkingStepIndex = 0
        currentViewMode = VedViewMode.THINKING

        // Execute thinking steps & API call in coroutine
        scope.launch {
            // Step 0 -> 1 -> 2 -> 3
            delay(500)
            thinkingStepIndex = 1
            delay(600)
            thinkingStepIndex = 2
            delay(600)
            thinkingStepIndex = 3

            val localResult = UtilityService.parseAndExecuteLocalCommand(context, dbService, text)
            val responseText = if (localResult.isHandled && localResult.responseMessage.isNotBlank()) {
                localResult.responseMessage
            } else {
                val dbMatch = dbService.findLearnedResponse(text)
                if (dbMatch != null) {
                    dbMatch
                } else {
                    val geminiRes = GeminiService.generateResponse(
                        prompt = text,
                        contextSummary = "",
                        dbService = dbService,
                        context = context
                    )
                    if (geminiRes.isNotBlank()) geminiRes else "Newton's Second Law states that force equals mass times acceleration (F = ma)."
                }
            }

            dbService.saveChatHistory("VED Chat", text, responseText)

            // Add history record
            historyItems.add(
                0,
                VedHistoryItem(
                    id = "h_${System.currentTimeMillis()}",
                    title = if (text.length > 25) text.take(25) + "..." else text,
                    subtitle = text,
                    time = "Just now",
                    category = "Chats",
                    dateGroup = "Today",
                    icon = Icons.Default.AutoAwesome,
                    iconColor = Color(0xFFA855F7)
                )
            )

            // Formulate math formula boxes if physics/math query
            val f1 = if (text.contains("Newton", ignoreCase = true)) "F = ma" else if (text.contains("Quadratic", ignoreCase = true)) "x² + 5x + 6 = 0" else null
            val f2 = if (text.contains("Newton", ignoreCase = true)) "a = F/m = 10/2 = 5 m/s²" else null

            // Add VED response
            chatMessages.add(
                VedMessage(
                    id = "msg_${System.currentTimeMillis() + 1}",
                    sender = "ved",
                    content = responseText,
                    mathFormula1 = f1,
                    mathFormula2 = f2,
                    timestamp = "Just now"
                )
            )

            // Switch to CHAT view
            currentViewMode = VedViewMode.CHAT
            voiceService.speak(responseText)
        }
    }

    // Auto-start voice if requested
    LaunchedEffect(autoStartVoice) {
        if (autoStartVoice) {
            currentViewMode = VedViewMode.VOICE_LISTENING
            isVoiceActive = true
            voiceService.startListening(
                onResult = { result ->
                    isVoiceActive = false
                    if (result.isNotBlank()) {
                        sendQuery(result)
                    } else {
                        currentViewMode = VedViewMode.HOME
                    }
                },
                onError = {
                    isVoiceActive = false
                    currentViewMode = VedViewMode.HOME
                }
            )
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFF090814)) // Deep space purple canvas
    ) {
        when (currentViewMode) {
            VedViewMode.HOME -> {
                VedHomeScreen(
                    onOpenDrawer = onOpenDrawer,
                    onOpenSettings = { currentViewMode = VedViewMode.SETTINGS },
                    onOpenHistory = { currentViewMode = VedViewMode.HISTORY },
                    onOpenVoice = {
                        currentViewMode = VedViewMode.VOICE_LISTENING
                        isVoiceActive = true
                        voiceService.startListening(
                            onResult = { recognized ->
                                isVoiceActive = false
                                if (recognized.isNotBlank()) sendQuery(recognized) else currentViewMode = VedViewMode.HOME
                            },
                            onError = {
                                isVoiceActive = false
                                currentViewMode = VedViewMode.HOME
                            }
                        )
                    },
                    onQuickActionSelect = { prompt -> sendQuery(prompt) },
                    inputQuery = inputQuery,
                    onInputQueryChange = { inputQuery = it },
                    onSend = { sendQuery(inputQuery) }
                )
            }

            VedViewMode.CHAT -> {
                VedChatScreen(
                    messages = chatMessages,
                    onBack = { currentViewMode = VedViewMode.HOME },
                    onOpenVoice = {
                        currentViewMode = VedViewMode.VOICE_LISTENING
                        isVoiceActive = true
                        voiceService.startListening(
                            onResult = { recognized ->
                                isVoiceActive = false
                                if (recognized.isNotBlank()) sendQuery(recognized) else currentViewMode = VedViewMode.CHAT
                            },
                            onError = {
                                isVoiceActive = false
                                currentViewMode = VedViewMode.CHAT
                            }
                        )
                    },
                    onOpenHistory = { currentViewMode = VedViewMode.HISTORY },
                    onSuggestionSelect = { suggestion -> sendQuery(suggestion) },
                    inputQuery = inputQuery,
                    onInputQueryChange = { inputQuery = it },
                    onSend = { sendQuery(inputQuery) },
                    voiceService = voiceService
                )
            }

            VedViewMode.THINKING -> {
                VedThinkingScreen(
                    queryText = thinkingQuery,
                    stepIndex = thinkingStepIndex,
                    onBack = { currentViewMode = VedViewMode.HOME },
                    onOpenVoice = { currentViewMode = VedViewMode.VOICE_LISTENING },
                    inputQuery = inputQuery,
                    onInputQueryChange = { inputQuery = it },
                    onSend = { sendQuery(inputQuery) }
                )
            }

            VedViewMode.HISTORY -> {
                VedHistoryScreen(
                    historyItems = historyItems,
                    onBack = { currentViewMode = VedViewMode.HOME },
                    onSelectHistoryItem = { item -> sendQuery(item.subtitle) }
                )
            }

            VedViewMode.VOICE_LISTENING -> {
                VedVoiceListeningScreen(
                    onStop = {
                        voiceService.stopListening()
                        isVoiceActive = false
                        currentViewMode = VedViewMode.HOME
                    }
                )
            }

            VedViewMode.SETTINGS -> {
                VedSettingsScreen(
                    aiPersonality = aiPersonality,
                    onAiPersonalityChange = { aiPersonality = it },
                    memoryContext = memoryContext,
                    onMemoryContextChange = { memoryContext = it },
                    voiceLanguage = voiceLanguage,
                    onVoiceLanguageChange = { voiceLanguage = it },
                    responseStyle = responseStyle,
                    onResponseStyleChange = { responseStyle = it },
                    imageGenEnabled = imageGenEnabled,
                    onImageGenToggle = { imageGenEnabled = !imageGenEnabled },
                    webSearchEnabled = webSearchEnabled,
                    onWebSearchToggle = { webSearchEnabled = !webSearchEnabled },
                    onClearHistoryClick = { showClearHistoryDialog = true },
                    onBack = { currentViewMode = VedViewMode.HOME }
                )
            }
        }
    }

    // Clear History Dialog
    if (showClearHistoryDialog) {
        AlertDialog(
            onDismissRequest = { showClearHistoryDialog = false },
            title = { Text("Clear History?", color = Color.White, fontWeight = FontWeight.Bold) },
            text = { Text("Are you sure you want to clear all conversation history and VED context?", color = Color(0xFFA09EC0)) },
            confirmButton = {
                TextButton(
                    onClick = {
                        chatMessages.clear()
                        historyItems.clear()
                        dbService.clearChatHistory()
                        Toast.makeText(context, "Conversation history cleared", Toast.LENGTH_SHORT).show()
                        showClearHistoryDialog = false
                    }
                ) {
                    Text("Clear All", color = Color(0xFFEF4444), fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showClearHistoryDialog = false }) {
                    Text("Cancel", color = Color.White)
                }
            },
            containerColor = Color(0xFF181530)
        )
    }
}

// ================= 1. PANEL 1: VED HOME SCREEN =================
@Composable
private fun VedHomeScreen(
    onOpenDrawer: () -> Unit,
    onOpenSettings: () -> Unit,
    onOpenHistory: () -> Unit,
    onOpenVoice: () -> Unit,
    onQuickActionSelect: (String) -> Unit,
    inputQuery: String,
    onInputQueryChange: (String) -> Unit,
    onSend: () -> Unit
) {
    Column(modifier = Modifier.fillMaxSize()) {
        // Header Bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(
                    onClick = onOpenDrawer,
                    modifier = Modifier
                        .size(38.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF181530))
                ) {
                    Icon(Icons.Default.Menu, contentDescription = "Menu", tint = Color.White)
                }

                Spacer(modifier = Modifier.width(12.dp))

                // VED Title with glowing waveform icon
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(Color(0xFF231C42)),
                        contentAlignment = Alignment.Center
                    ) {
                        VedMathLogoCanvas(
                            modifier = Modifier.size(24.dp),
                            animated = true
                        )
                    }

                    Spacer(modifier = Modifier.width(10.dp))

                    Column {
                        Text("VED", color = Color.White, fontSize = 17.sp, fontWeight = FontWeight.Bold)
                        Text("Your AI Companion", color = Color(0xFFA09EC0), fontSize = 11.sp)
                    }
                }
            }

            // Right Action Icons (Speaker / History / Settings)
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onOpenVoice) {
                    Icon(Icons.Default.VolumeUp, contentDescription = "Voice", tint = Color.White)
                }
                IconButton(onClick = onOpenHistory) {
                    Icon(Icons.Default.History, contentDescription = "History", tint = Color.White)
                }
                IconButton(onClick = onOpenSettings) {
                    Icon(Icons.Default.Settings, contentDescription = "Settings", tint = Color.White)
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Main Content Area
        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(horizontal = 20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Main Greeting
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.Start
            ) {
                Text(
                    text = "Hi Rahul! 👋",
                    color = Color.White,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.ExtraBold
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "How can I help you today?",
                    color = Color(0xFFA09EC0),
                    fontSize = 14.sp
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Central Glowing Audio Spectrum Squircle Hero Box
            Box(
                modifier = Modifier
                    .size(175.dp)
                    .clip(RoundedCornerShape(36.dp))
                    .background(
                        Brush.radialGradient(
                            colors = listOf(
                                Color(0xFF281C58),
                                Color(0xFF140F30)
                            )
                        )
                    )
                    .border(
                        width = 1.5.dp,
                        brush = Brush.sweepGradient(
                            listOf(
                                Color(0xFF8B5CF6),
                                Color(0xFFD946EF),
                                Color(0xFF3B82F6),
                                Color(0xFF8B5CF6)
                            )
                        ),
                        shape = RoundedCornerShape(36.dp)
                    )
                    .clickable { onOpenVoice() },
                contentAlignment = Alignment.Center
            ) {
                // Waveform Canvas Visualizer inside
                VedMathLogoCanvas(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(20.dp),
                    animated = true
                )
            }

            Spacer(modifier = Modifier.height(28.dp))

            // 2x2 Quick Action Cards Grid
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    QuickActionGridCard(
                        icon = Icons.Default.AutoAwesome,
                        iconColor = Color(0xFFA855F7),
                        title = "Explain",
                        subtitle = "Quantum Physics",
                        modifier = Modifier.weight(1f),
                        onClick = { onQuickActionSelect("Explain Quantum Physics in detail.") }
                    )

                    QuickActionGridCard(
                        icon = Icons.Default.Description,
                        iconColor = Color(0xFFF97316),
                        title = "Summarize",
                        subtitle = "This PDF",
                        modifier = Modifier.weight(1f),
                        onClick = { onQuickActionSelect("Summarize my physics PDF notes.") }
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    QuickActionGridCard(
                        icon = Icons.Default.Edit,
                        iconColor = Color(0xFF3B82F6),
                        title = "Write",
                        subtitle = "Study Plan",
                        modifier = Modifier.weight(1f),
                        onClick = { onQuickActionSelect("Create a study plan for IIT JEE preparation.") }
                    )

                    QuickActionGridCard(
                        icon = Icons.Default.Calculate,
                        iconColor = Color(0xFF10B981),
                        title = "Solve",
                        subtitle = "Math Problem",
                        modifier = Modifier.weight(1f),
                        onClick = { onQuickActionSelect("Solve quadratic equation x² + 5x + 6 = 0") }
                    )
                }
            }
        }

        // Bottom Prompt Input Bar
        VedPromptInputBar(
            inputQuery = inputQuery,
            onInputQueryChange = onInputQueryChange,
            onMicClick = onOpenVoice,
            onSend = onSend
        )
    }
}

// Quick Action Card Composable
@Composable
private fun QuickActionGridCard(
    icon: ImageVector,
    iconColor: Color,
    title: String,
    subtitle: String,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(18.dp))
            .background(Color(0xFF141228))
            .border(1.dp, Color(0xFF28264A), RoundedCornerShape(18.dp))
            .clickable { onClick() }
            .padding(horizontal = 14.dp, vertical = 14.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(38.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(iconColor.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, contentDescription = null, tint = iconColor, modifier = Modifier.size(20.dp))
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column {
                Text(title, color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(2.dp))
                Text(subtitle, color = Color(0xFFA09EC0), fontSize = 11.5.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
            }
        }
    }
}

// ================= 2. PANEL 2: VED CHAT SCREEN =================
@Composable
private fun VedChatScreen(
    messages: List<VedMessage>,
    onBack: () -> Unit,
    onOpenVoice: () -> Unit,
    onOpenHistory: () -> Unit,
    onSuggestionSelect: (String) -> Unit,
    inputQuery: String,
    onInputQueryChange: (String) -> Unit,
    onSend: () -> Unit,
    voiceService: VoiceService
) {
    val listState = rememberLazyListState()
    val context = LocalContext.current

    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) {
            listState.animateScrollToItem(messages.size - 1)
        }
    }

    Column(modifier = Modifier.fillMaxSize()) {
        // Chat Header Bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onBack) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
                }

                Spacer(modifier = Modifier.width(8.dp))

                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(Color(0xFF231C42)),
                    contentAlignment = Alignment.Center
                ) {
                    VedMathLogoCanvas(modifier = Modifier.size(24.dp), animated = true)
                }

                Spacer(modifier = Modifier.width(10.dp))

                Column {
                    Text("VED", color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(6.dp)
                                .clip(CircleShape)
                                .background(Color(0xFF10B981))
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Online", color = Color(0xFF10B981), fontSize = 11.sp, fontWeight = FontWeight.Medium)
                    }
                }
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onOpenVoice) {
                    Icon(Icons.Default.Call, contentDescription = "Voice Call", tint = Color.White)
                }
                IconButton(onClick = onOpenHistory) {
                    Icon(Icons.Default.MoreVert, contentDescription = "More", tint = Color.White)
                }
            }
        }

        // Chat Message Stream
        LazyColumn(
            state = listState,
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            items(messages) { msg ->
                val isUser = msg.sender == "user"
                if (isUser) {
                    // Right-aligned User Bubble
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        Box(
                            modifier = Modifier
                                .widthIn(max = 280.dp)
                                .clip(
                                    RoundedCornerShape(
                                        topStart = 18.dp,
                                        topEnd = 18.dp,
                                        bottomStart = 18.dp,
                                        bottomEnd = 4.dp
                                    )
                                )
                                .background(
                                    Brush.linearGradient(
                                        listOf(Color(0xFF6B21A8), Color(0xFF581C87))
                                    )
                                )
                                .padding(14.dp)
                        ) {
                            Text(msg.content, color = Color.White, fontSize = 13.5.sp, lineHeight = 19.sp)
                        }
                    }
                } else {
                    // Left-aligned VED AI Bubble
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Start
                    ) {
                        // VED Avatar
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(Color(0xFF231C42)),
                            contentAlignment = Alignment.Center
                        ) {
                            VedMathLogoCanvas(modifier = Modifier.size(22.dp), animated = true)
                        }

                        Spacer(modifier = Modifier.width(10.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(
                                        RoundedCornerShape(
                                            topStart = 4.dp,
                                            topEnd = 18.dp,
                                            bottomStart = 18.dp,
                                            bottomEnd = 18.dp
                                        )
                                    )
                                    .background(Color(0xFF141328))
                                    .border(1.dp, Color(0xFF28264A), RoundedCornerShape(18.dp))
                                    .padding(14.dp)
                            ) {
                                Column {
                                    Text(
                                        msg.content,
                                        color = Color.White,
                                        fontSize = 13.5.sp,
                                        lineHeight = 20.sp
                                    )

                                    // Render Math / Formula Cards if present
                                    if (msg.mathFormula1 != null) {
                                        Spacer(modifier = Modifier.height(10.dp))
                                        Box(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .clip(RoundedCornerShape(12.dp))
                                                .background(Color(0xFF1B1936))
                                                .border(1.dp, Color(0xFF3B3766), RoundedCornerShape(12.dp))
                                                .padding(horizontal = 16.dp, vertical = 10.dp),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text(
                                                msg.mathFormula1,
                                                color = Color(0xFFA855F7),
                                                fontSize = 16.sp,
                                                fontWeight = FontWeight.Bold
                                            )
                                        }
                                    }

                                    if (msg.mathFormula2 != null) {
                                        Spacer(modifier = Modifier.height(8.dp))
                                        Box(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .clip(RoundedCornerShape(12.dp))
                                                .background(Color(0xFF1B1936))
                                                .border(1.dp, Color(0xFF3B3766), RoundedCornerShape(12.dp))
                                                .padding(horizontal = 16.dp, vertical = 10.dp),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text(
                                                msg.mathFormula2,
                                                color = Color(0xFF34D399),
                                                fontSize = 14.5.sp,
                                                fontWeight = FontWeight.Bold
                                            )
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(12.dp))

                                    // Action Toolbar Row
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                                            IconButton(
                                                onClick = {
                                                    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                                    clipboard.setPrimaryClip(ClipData.newPlainText("VED Response", msg.content))
                                                    Toast.makeText(context, "Copied response to clipboard", Toast.LENGTH_SHORT).show()
                                                },
                                                modifier = Modifier.size(20.dp)
                                            ) {
                                                Icon(Icons.Default.ContentCopy, contentDescription = "Copy", tint = Color(0xFFA09EC0), modifier = Modifier.size(16.dp))
                                            }

                                            IconButton(
                                                onClick = { Toast.makeText(context, "Liked response", Toast.LENGTH_SHORT).show() },
                                                modifier = Modifier.size(20.dp)
                                            ) {
                                                Icon(Icons.Default.ThumbUpOffAlt, contentDescription = "Like", tint = Color(0xFFA09EC0), modifier = Modifier.size(16.dp))
                                            }

                                            IconButton(
                                                onClick = { Toast.makeText(context, "Feedback recorded", Toast.LENGTH_SHORT).show() },
                                                modifier = Modifier.size(20.dp)
                                            ) {
                                                Icon(Icons.Default.ThumbDownOffAlt, contentDescription = "Dislike", tint = Color(0xFFA09EC0), modifier = Modifier.size(16.dp))
                                            }

                                            IconButton(
                                                onClick = {
                                                    val shareIntent = Intent(Intent.ACTION_SEND).apply {
                                                        type = "text/plain"
                                                        putExtra(Intent.EXTRA_TEXT, msg.content)
                                                    }
                                                    context.startActivity(Intent.createChooser(shareIntent, "Share VED Answer"))
                                                },
                                                modifier = Modifier.size(20.dp)
                                            ) {
                                                Icon(Icons.Default.Share, contentDescription = "Share", tint = Color(0xFFA09EC0), modifier = Modifier.size(16.dp))
                                            }

                                            IconButton(
                                                onClick = { voiceService.speak(msg.content) },
                                                modifier = Modifier.size(20.dp)
                                            ) {
                                                Icon(Icons.Default.VolumeUp, contentDescription = "Read Aloud", tint = Color(0xFFA09EC0), modifier = Modifier.size(16.dp))
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // Quick Suggestion Chips Row
        LazyRow(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            val suggestions = listOf("Give more examples", "Real life applications", "Derivation")
            items(suggestions) { label ->
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(16.dp))
                        .background(Color(0xFF1A1832))
                        .border(1.dp, Color(0xFF332F5C), RoundedCornerShape(16.dp))
                        .clickable { onSuggestionSelect(label) }
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Text(label, color = Color(0xFFA09EC0), fontSize = 11.5.sp, fontWeight = FontWeight.Medium)
                }
            }
        }

        // Bottom Prompt Input Bar
        VedPromptInputBar(
            inputQuery = inputQuery,
            onInputQueryChange = onInputQueryChange,
            onMicClick = onOpenVoice,
            onSend = onSend
        )
    }
}

// ================= 3. PANEL 3: VED THINKING SCREEN =================
@Composable
private fun VedThinkingScreen(
    queryText: String,
    stepIndex: Int,
    onBack: () -> Unit,
    onOpenVoice: () -> Unit,
    inputQuery: String,
    onInputQueryChange: (String) -> Unit,
    onSend: () -> Unit
) {
    Column(modifier = Modifier.fillMaxSize()) {
        // Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onBack) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
                }

                Spacer(modifier = Modifier.width(8.dp))

                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(Color(0xFF231C42)),
                    contentAlignment = Alignment.Center
                ) {
                    VedMathLogoCanvas(modifier = Modifier.size(24.dp), animated = true)
                }

                Spacer(modifier = Modifier.width(10.dp))

                Column {
                    Text("VED", color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(6.dp)
                                .clip(CircleShape)
                                .background(Color(0xFF10B981))
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Online", color = Color(0xFF10B981), fontSize = 11.sp, fontWeight = FontWeight.Medium)
                    }
                }
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onOpenVoice) {
                    Icon(Icons.Default.Call, contentDescription = "Call", tint = Color.White)
                }
                IconButton(onClick = {}) {
                    Icon(Icons.Default.MoreVert, contentDescription = "More", tint = Color.White)
                }
            }
        }

        // User Query Bubble at Top
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.End
        ) {
            Box(
                modifier = Modifier
                    .widthIn(max = 280.dp)
                    .clip(RoundedCornerShape(18.dp))
                    .background(
                        Brush.linearGradient(
                            listOf(Color(0xFF6B21A8), Color(0xFF581C87))
                        )
                    )
                    .padding(14.dp)
            ) {
                Text(queryText, color = Color.White, fontSize = 13.5.sp, lineHeight = 19.sp)
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // VED Thinking Card Container
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .clip(RoundedCornerShape(24.dp))
                .background(Color(0xFF141228))
                .border(1.dp, Color(0xFF28264A), RoundedCornerShape(24.dp))
                .padding(20.dp)
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                // Header with animated dots
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("VED is thinking...", color = Color.White, fontSize = 15.sp, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.width(8.dp))
                    PulsingDotsAnimation()
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Center Squircle Waveform Graphic
                Box(
                    modifier = Modifier
                        .size(120.dp)
                        .clip(RoundedCornerShape(28.dp))
                        .background(Color(0xFF231C42))
                        .border(1.dp, Color(0xFFA855F7).copy(alpha = 0.5f), RoundedCornerShape(28.dp))
                        .padding(16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    VedMathLogoCanvas(modifier = Modifier.fillMaxSize(), animated = true)
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Checklist Progress Steps
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    ThinkingCheckStepItem("Analyzing your request", isCompleted = stepIndex >= 1, isActive = stepIndex == 0)
                    ThinkingCheckStepItem("Understanding your goals", isCompleted = stepIndex >= 2, isActive = stepIndex == 1)
                    ThinkingCheckStepItem("Building personalized plan", isCompleted = stepIndex >= 3, isActive = stepIndex == 2)
                    ThinkingCheckStepItem("Finalizing study plan", isCompleted = stepIndex >= 4, isActive = stepIndex == 3)
                }
            }
        }

        Spacer(modifier = Modifier.weight(1f))

        // Bottom Prompt Input Bar
        VedPromptInputBar(
            inputQuery = inputQuery,
            onInputQueryChange = onInputQueryChange,
            onMicClick = onOpenVoice,
            onSend = onSend
        )
    }
}

// Thinking Step Row Item
@Composable
private fun ThinkingCheckStepItem(
    title: String,
    isCompleted: Boolean,
    isActive: Boolean
) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        if (isCompleted) {
            Box(
                modifier = Modifier
                    .size(20.dp)
                    .clip(CircleShape)
                    .background(Color(0xFF10B981)),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.Check, contentDescription = null, tint = Color.White, modifier = Modifier.size(13.dp))
            }
        } else if (isActive) {
            Box(
                modifier = Modifier
                    .size(20.dp)
                    .clip(CircleShape)
                    .background(Color(0xFFA855F7)),
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .clip(CircleShape)
                        .background(Color.White)
                )
            }
        } else {
            Box(
                modifier = Modifier
                    .size(20.dp)
                    .clip(CircleShape)
                    .border(1.5.dp, Color(0xFF4B4870), CircleShape)
            )
        }

        Spacer(modifier = Modifier.width(12.dp))

        Text(
            text = title,
            color = if (isCompleted || isActive) Color.White else Color(0xFF6B6893),
            fontSize = 13.5.sp,
            fontWeight = if (isActive) FontWeight.Bold else FontWeight.Medium
        )
    }
}

// Pulsing dots animation composable
@Composable
private fun PulsingDotsAnimation() {
    val infiniteTransition = rememberInfiniteTransition(label = "dots")
    val alpha1 by infiniteTransition.animateFloat(
        initialValue = 0.2f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(600, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "d1"
    )

    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
        Box(modifier = Modifier.size(5.dp).clip(CircleShape).background(Color(0xFFA855F7).copy(alpha = alpha1)))
        Box(modifier = Modifier.size(5.dp).clip(CircleShape).background(Color(0xFFA855F7).copy(alpha = alpha1)))
        Box(modifier = Modifier.size(5.dp).clip(CircleShape).background(Color(0xFFA855F7).copy(alpha = alpha1)))
    }
}

// ================= 4. PANEL 4: VED HISTORY SCREEN =================
@Composable
private fun VedHistoryScreen(
    historyItems: List<VedHistoryItem>,
    onBack: () -> Unit,
    onSelectHistoryItem: (VedHistoryItem) -> Unit
) {
    var selectedCategory by remember { mutableStateOf("All") }
    var searchQuery by remember { mutableStateOf("") }
    var isSearchActive by remember { mutableStateOf(false) }

    val categories = listOf("All", "Chats", "Notes", "Images", "Voice")

    val filtered = historyItems.filter {
        (selectedCategory == "All" || it.category == selectedCategory) &&
                (searchQuery.isBlank() || it.title.contains(searchQuery, ignoreCase = true) || it.subtitle.contains(searchQuery, ignoreCase = true))
    }

    val grouped = filtered.groupBy { it.dateGroup }

    Column(modifier = Modifier.fillMaxSize()) {
        // Top Bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onBack) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
                }
                Spacer(modifier = Modifier.width(8.dp))
                Text("History", color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold)
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = { isSearchActive = !isSearchActive }) {
                    Icon(if (isSearchActive) Icons.Default.Close else Icons.Default.Search, contentDescription = "Search", tint = Color.White)
                }
                IconButton(onClick = {}) {
                    Icon(Icons.Default.MoreVert, contentDescription = "More", tint = Color.White)
                }
            }
        }

        // Search Field
        AnimatedVisibility(visible = isSearchActive) {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = { Text("Search history...", color = Color(0xFF6B6893), fontSize = 12.5.sp) },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 4.dp),
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color(0xFF8B5CF6),
                    unfocusedBorderColor = Color(0xFF28264A),
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White
                )
            )
        }

        // Filter Category Chips Row
        LazyRow(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            items(categories) { cat ->
                val isSelected = cat == selectedCategory
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(20.dp))
                        .background(if (isSelected) Color(0xFF6B21A8) else Color(0xFF141228))
                        .border(1.dp, if (isSelected) Color(0xFFA855F7) else Color(0xFF28264A), RoundedCornerShape(20.dp))
                        .clickable { selectedCategory = cat }
                        .padding(horizontal = 18.dp, vertical = 8.dp)
                ) {
                    Text(
                        text = cat,
                        color = if (isSelected) Color.White else Color(0xFFA09EC0),
                        fontSize = 13.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(6.dp))

        // History List Grouped by Date
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            grouped.forEach { (dateGroup, items) ->
                item {
                    Text(
                        text = dateGroup,
                        color = Color(0xFFA09EC0),
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(top = 8.dp, bottom = 4.dp)
                    )
                }

                items(items) { item ->
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(16.dp))
                            .background(Color(0xFF141228))
                            .border(1.dp, Color(0xFF28264A), RoundedCornerShape(16.dp))
                            .clickable { onSelectHistoryItem(item) }
                            .padding(14.dp)
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
                                Box(
                                    modifier = Modifier
                                        .size(38.dp)
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(item.iconColor.copy(alpha = 0.15f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(item.icon, contentDescription = null, tint = item.iconColor, modifier = Modifier.size(20.dp))
                                }

                                Spacer(modifier = Modifier.width(12.dp))

                                Column {
                                    Text(item.title, color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(item.subtitle, color = Color(0xFFA09EC0), fontSize = 11.5.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                }
                            }

                            Spacer(modifier = Modifier.width(8.dp))

                            Text(item.time, color = Color(0xFF6B6893), fontSize = 11.sp)
                        }
                    }
                }
            }
        }
    }
}

// ================= 5. PANEL 5: FULLSCREEN VOICE LISTENING SCREEN =================
@Composable
private fun VedVoiceListeningScreen(
    onStop: () -> Unit
) {
    val infiniteTransition = rememberInfiniteTransition(label = "listening_wave")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 0.95f,
        targetValue = 1.15f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse"
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF090814)),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Top Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onStop) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
                }

                Spacer(modifier = Modifier.width(8.dp))

                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(Color(0xFF231C42)),
                    contentAlignment = Alignment.Center
                ) {
                    VedMathLogoCanvas(modifier = Modifier.size(24.dp), animated = true)
                }

                Spacer(modifier = Modifier.width(10.dp))

                Column {
                    Text("VED", color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                    Text("Online", color = Color(0xFF10B981), fontSize = 11.sp)
                }
            }

            Row {
                IconButton(onClick = {}) {
                    Icon(Icons.Default.Call, contentDescription = "Call", tint = Color.White)
                }
                IconButton(onClick = {}) {
                    Icon(Icons.Default.MoreVert, contentDescription = "More", tint = Color.White)
                }
            }
        }

        Spacer(modifier = Modifier.height(40.dp))

        // Listening Title
        Text(
            text = "I'm listening...",
            color = Color.White,
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.weight(1f))

        // Center Voice Hero Visualizer with Horizontal Equalizer Lines
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(220.dp),
            contentAlignment = Alignment.Center
        ) {
            // Horizontal Equalizer Waves Background
            HorizontalAudioEqualizerLines(modifier = Modifier.fillMaxSize())

            // Central Glowing Squircle Card
            Box(
                modifier = Modifier
                    .size(160.dp)
                    .scale(pulseScale)
                    .clip(RoundedCornerShape(36.dp))
                    .background(Color(0xFF1D173D))
                    .border(1.5.dp, Color(0xFFA855F7), RoundedCornerShape(36.dp))
                    .padding(20.dp),
                contentAlignment = Alignment.Center
            ) {
                VedMathLogoCanvas(modifier = Modifier.fillMaxSize(), animated = true)
            }
        }

        Spacer(modifier = Modifier.weight(1f))

        // Stop Action Button ('X')
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(bottom = 48.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .clip(CircleShape)
                    .background(Color(0xFF181530))
                    .border(1.dp, Color(0xFF38345C), CircleShape)
                    .clickable { onStop() },
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.Close, contentDescription = "Stop", tint = Color.White, modifier = Modifier.size(26.dp))
            }

            Spacer(modifier = Modifier.height(10.dp))

            Text("Tap to stop", color = Color(0xFFA09EC0), fontSize = 12.sp)
        }
    }
}

// Horizontal Equalizer Spectrum Lines Composable
@Composable
private fun HorizontalAudioEqualizerLines(modifier: Modifier = Modifier) {
    val infiniteTransition = rememberInfiniteTransition(label = "equalizer")
    val phase by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 2f * Math.PI.toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "phase"
    )

    Canvas(modifier = modifier) {
        val width = size.width
        val height = size.height
        val centerY = height / 2f

        val barCount = 45
        val barGap = width / barCount

        for (i in 0 until barCount) {
            val x = i * barGap
            val distFromCenter = Math.abs(x - width / 2f) / (width / 2f)
            val amplitude = (1.0f - distFromCenter * 0.7f) * 60.dp.toPx()

            val h = (sin(i * 0.4f + phase) * 0.5f + 0.5f) * amplitude + 10.dp.toPx()

            drawLine(
                color = Color(0xFFA855F7).copy(alpha = (1.0f - distFromCenter * 0.6f).coerceIn(0.2f, 0.9f)),
                start = Offset(x, centerY - h / 2f),
                end = Offset(x, centerY + h / 2f),
                strokeWidth = 3.dp.toPx(),
                cap = StrokeCap.Round
            )
        }
    }
}

// ================= 6. PANEL 6: VED SETTINGS SCREEN =================
@Composable
private fun VedSettingsScreen(
    aiPersonality: String,
    onAiPersonalityChange: (String) -> Unit,
    memoryContext: String,
    onMemoryContextChange: (String) -> Unit,
    voiceLanguage: String,
    onVoiceLanguageChange: (String) -> Unit,
    responseStyle: String,
    onResponseStyleChange: (String) -> Unit,
    imageGenEnabled: Boolean,
    onImageGenToggle: () -> Unit,
    webSearchEnabled: Boolean,
    onWebSearchToggle: () -> Unit,
    onClearHistoryClick: () -> Unit,
    onBack: () -> Unit
) {
    val context = LocalContext.current

    Column(modifier = Modifier.fillMaxSize()) {
        // Header Bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
            }
            Spacer(modifier = Modifier.width(8.dp))
            Text("VED Settings", color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold)
        }

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Profile Header Card
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(20.dp))
                        .background(Color(0xFF141228))
                        .border(1.dp, Color(0xFF28264A), RoundedCornerShape(20.dp))
                        .padding(16.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(48.dp)
                                    .clip(RoundedCornerShape(14.dp))
                                    .background(Color(0xFF231C42)),
                                contentAlignment = Alignment.Center
                            ) {
                                VedMathLogoCanvas(modifier = Modifier.size(34.dp), animated = true)
                            }

                            Spacer(modifier = Modifier.width(14.dp))

                            Column {
                                Text("VED AI", color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                                Spacer(modifier = Modifier.height(2.dp))
                                Text("Version 1.0.0", color = Color(0xFFA09EC0), fontSize = 11.5.sp)
                                Text("Your personal AI companion", color = Color(0xFF6B6893), fontSize = 11.sp)
                            }
                        }

                        // Premium Badge Pill
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .background(Color(0xFF381452))
                                .border(1.dp, Color(0xFFA855F7), RoundedCornerShape(12.dp))
                                .padding(horizontal = 10.dp, vertical = 5.dp)
                        ) {
                            Text("Premium", color = Color(0xFFA855F7), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            // Section 1: AI Parameters
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(20.dp))
                        .background(Color(0xFF141228))
                        .border(1.dp, Color(0xFF28264A), RoundedCornerShape(20.dp))
                ) {
                    VedSettingRowItem(
                        icon = Icons.Default.AutoAwesome,
                        title = "AI Personality",
                        value = aiPersonality,
                        onClick = {
                            val next = if (aiPersonality.contains("Friendly")) "Strict & Precise" else "Friendly & Smart"
                            onAiPersonalityChange(next)
                        }
                    )
                    VedSettingDivider()

                    VedSettingRowItem(
                        icon = Icons.Default.Psychology,
                        title = "Memory & Context",
                        value = memoryContext,
                        onClick = {
                            val next = if (memoryContext == "High") "Maximum" else "High"
                            onMemoryContextChange(next)
                        }
                    )
                    VedSettingDivider()

                    VedSettingRowItem(
                        icon = Icons.Default.Mic,
                        title = "Voice & Language",
                        value = voiceLanguage,
                        onClick = {
                            val next = if (voiceLanguage.contains("India")) "English (US)" else "English (India)"
                            onVoiceLanguageChange(next)
                        }
                    )
                    VedSettingDivider()

                    VedSettingRowItem(
                        icon = Icons.Default.Edit,
                        title = "Response Style",
                        value = responseStyle,
                        onClick = {
                            val next = if (responseStyle == "Detailed") "Concise" else "Detailed"
                            onResponseStyleChange(next)
                        }
                    )
                    VedSettingDivider()

                    VedSettingRowItem(
                        icon = Icons.Default.Description,
                        title = "Image Generation",
                        value = if (imageGenEnabled) "Enabled" else "Disabled",
                        onClick = onImageGenToggle
                    )
                    VedSettingDivider()

                    VedSettingRowItem(
                        icon = Icons.Default.Search,
                        title = "Web Search",
                        value = if (webSearchEnabled) "Enabled" else "Disabled",
                        onClick = onWebSearchToggle
                    )
                }
            }

            // Section 2: Account & Privacy
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(20.dp))
                        .background(Color(0xFF141228))
                        .border(1.dp, Color(0xFF28264A), RoundedCornerShape(20.dp))
                ) {
                    VedSettingRowItem(
                        icon = Icons.Default.CheckCircle,
                        title = "Data & Privacy",
                        value = "Manage your data",
                        onClick = { Toast.makeText(context, "Data privacy managed locally", Toast.LENGTH_SHORT).show() }
                    )
                    VedSettingDivider()

                    VedSettingRowItem(
                        icon = Icons.AutoMirrored.Filled.HelpOutline,
                        title = "About VED",
                        value = "Learn more about VED",
                        onClick = { Toast.makeText(context, "VED AI v1.0.0 - Personal Intelligence", Toast.LENGTH_SHORT).show() }
                    )
                }
            }

            // Section 3: Clear History Button
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(Color(0xFF231018))
                        .border(1.dp, Color(0xFF5C1D24), RoundedCornerShape(16.dp))
                        .clickable { onClearHistoryClick() }
                        .padding(vertical = 14.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Delete, contentDescription = null, tint = Color(0xFFEF4444), modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Clear Conversation History", color = Color(0xFFEF4444), fontSize = 14.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
}

// Single Settings Row Item
@Composable
private fun VedSettingRowItem(
    icon: ImageVector,
    title: String,
    value: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(horizontal = 16.dp, vertical = 14.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(icon, contentDescription = null, tint = Color(0xFFA09EC0), modifier = Modifier.size(20.dp))
            Spacer(modifier = Modifier.width(12.dp))
            Text(title, color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Medium)
        }

        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(value, color = Color(0xFF6B6893), fontSize = 12.sp)
            Spacer(modifier = Modifier.width(6.dp))
            Icon(Icons.Default.ChevronRight, contentDescription = null, tint = Color(0xFF6B6893), modifier = Modifier.size(16.dp))
        }
    }
}

@Composable
private fun VedSettingDivider() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(1.dp)
            .background(Color(0xFF1E1C38))
    )
}

// Common Bottom Prompt Input Bar
@Composable
private fun VedPromptInputBar(
    inputQuery: String,
    onInputQueryChange: (String) -> Unit,
    onMicClick: () -> Unit,
    onSend: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        OutlinedTextField(
            value = inputQuery,
            onValueChange = onInputQueryChange,
            placeholder = { Text("Ask VED anything...", color = Color(0xFF6B6893), fontSize = 13.5.sp) },
            modifier = Modifier
                .weight(1f)
                .testTag("ved_prompt_input_field"),
            shape = RoundedCornerShape(26.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = Color(0xFF141228),
                unfocusedContainerColor = Color(0xFF141228),
                focusedBorderColor = Color(0xFF8B5CF6),
                unfocusedBorderColor = Color(0xFF28264A),
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.White
            ),
            singleLine = true,
            trailingIcon = {
                IconButton(onClick = onMicClick) {
                    Icon(Icons.Default.Mic, contentDescription = "Mic", tint = Color(0xFFA09EC0), modifier = Modifier.size(20.dp))
                }
            }
        )

        Spacer(modifier = Modifier.width(10.dp))

        // Glowing Purple Send / Voice Action Button
        Box(
            modifier = Modifier
                .size(48.dp)
                .clip(CircleShape)
                .background(
                    Brush.linearGradient(
                        colors = listOf(Color(0xFF8B5CF6), Color(0xFFA855F7))
                    )
                )
                .clickable {
                    if (inputQuery.isNotBlank()) onSend() else onMicClick()
                }
                .testTag("ved_prompt_send_button"),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                if (inputQuery.isNotBlank()) Icons.Default.Send else Icons.Default.GraphicEq,
                contentDescription = "Send",
                tint = Color.White,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}
