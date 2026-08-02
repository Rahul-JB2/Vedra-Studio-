package com.example.ui.screens

import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
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
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Book
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.ChatBubble
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.DeveloperMode
import androidx.compose.material.icons.filled.Equalizer
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Help
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Memory
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.TextFields
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.services.DatabaseService
import com.example.services.VoiceService

// Sub-screen navigation Enum for Settings
enum class SettingsSubView {
    MAIN,
    AI_INTELLIGENCE,
    VOICE_ENGINE,
    VEDMT_KNOWLEDGE,
    VEDRIVE,
    DEVICE_AUTOMATION,
    CHAT_HISTORY,
    NOTIFICATIONS,
    APPEARANCE,
    PRIVACY_SECURITY,
    ADVANCED,
    HELP_SUPPORT
}

// Category item structure
data class SettingCategoryItem(
    val type: SettingsSubView,
    val title: String,
    val subtitle: String,
    val icon: ImageVector,
    val iconColor: Color
)

@Composable
fun SettingsScreen(
    dbService: DatabaseService,
    voiceService: VoiceService
) {
    val context = LocalContext.current
    var currentSubView by remember { mutableStateOf(SettingsSubView.MAIN) }
    var searchQuery by remember { mutableStateOf("") }
    var isSearchActive by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0B0A1A)) // Dark Violet Canvas
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Main Top Bar or Sub-View Navigation Bar
            TopBarHeader(
                currentSubView = currentSubView,
                onBack = { currentSubView = SettingsSubView.MAIN },
                searchQuery = searchQuery,
                onSearchChange = { searchQuery = it },
                isSearchActive = isSearchActive,
                onToggleSearch = { isSearchActive = !isSearchActive }
            )

            // Content view router
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            ) {
                when (currentSubView) {
                    SettingsSubView.MAIN -> {
                        MainSettingsView(
                            dbService = dbService,
                            searchQuery = searchQuery,
                            onNavigate = { subView -> currentSubView = subView }
                        )
                    }

                    SettingsSubView.AI_INTELLIGENCE -> {
                        AiAndIntelligenceScreen(dbService = dbService)
                    }

                    SettingsSubView.VOICE_ENGINE -> {
                        VoiceEngineScreen(dbService = dbService, voiceService = voiceService)
                    }

                    SettingsSubView.VEDMT_KNOWLEDGE -> {
                        VedmtKnowledgeScreen(dbService = dbService)
                    }

                    SettingsSubView.VEDRIVE -> {
                        VedriveSettingsScreen(dbService = dbService)
                    }

                    SettingsSubView.DEVICE_AUTOMATION -> {
                        DeviceAutomationScreen(dbService = dbService)
                    }

                    SettingsSubView.CHAT_HISTORY -> {
                        ChatHistoryScreen(dbService = dbService)
                    }

                    SettingsSubView.NOTIFICATIONS -> {
                        NotificationsSettingsScreen(dbService = dbService)
                    }

                    SettingsSubView.APPEARANCE -> {
                        AppearanceSettingsScreen(dbService = dbService)
                    }

                    SettingsSubView.PRIVACY_SECURITY -> {
                        PrivacySecurityScreen(dbService = dbService)
                    }

                    SettingsSubView.ADVANCED -> {
                        AdvancedSettingsScreen(dbService = dbService)
                    }

                    SettingsSubView.HELP_SUPPORT -> {
                        HelpSupportScreen(dbService = dbService)
                    }
                }
            }
        }
    }
}

// ================= TOP BAR HEADER =================
@Composable
private fun TopBarHeader(
    currentSubView: SettingsSubView,
    onBack: () -> Unit,
    searchQuery: String,
    onSearchChange: (String) -> Unit,
    isSearchActive: Boolean,
    onToggleSearch: () -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (currentSubView != SettingsSubView.MAIN) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(
                        onClick = onBack,
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF1B1A38))
                    ) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = Color.White)
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = when (currentSubView) {
                            SettingsSubView.AI_INTELLIGENCE -> "AI & Intelligence"
                            SettingsSubView.VOICE_ENGINE -> "Voice Engine"
                            SettingsSubView.VEDMT_KNOWLEDGE -> "VEDM-T Knowledge"
                            SettingsSubView.VEDRIVE -> "VEDrive"
                            SettingsSubView.DEVICE_AUTOMATION -> "Device & Automation"
                            SettingsSubView.CHAT_HISTORY -> "Chat & History"
                            SettingsSubView.NOTIFICATIONS -> "Notifications"
                            SettingsSubView.APPEARANCE -> "Appearance"
                            SettingsSubView.PRIVACY_SECURITY -> "Privacy & Security"
                            SettingsSubView.ADVANCED -> "Advanced"
                            SettingsSubView.HELP_SUPPORT -> "Help & Support"
                            else -> "Settings"
                        },
                        color = Color.White,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            } else {
                Text(
                    text = "Settings",
                    color = Color.White,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold
                )

                IconButton(onClick = onToggleSearch) {
                    Icon(
                        imageVector = if (isSearchActive) Icons.Default.Close else Icons.Default.Search,
                        contentDescription = "Search",
                        tint = Color.White
                    )
                }
            }
        }

        // Search Input Field
        AnimatedVisibility(visible = isSearchActive && currentSubView == SettingsSubView.MAIN) {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = onSearchChange,
                placeholder = { Text("Search settings...", color = Color(0xFF6B6893), fontSize = 12.5.sp) },
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
    }
}

// ================= 1. MAIN SETTINGS VIEW =================
@Composable
private fun MainSettingsView(
    dbService: DatabaseService,
    searchQuery: String,
    onNavigate: (SettingsSubView) -> Unit
) {
    val categories = remember {
        listOf(
            SettingCategoryItem(SettingsSubView.AI_INTELLIGENCE, "AI & Intelligence", "AI model, memory, learning & more", Icons.Default.Psychology, Color(0xFF8B5CF6)),
            SettingCategoryItem(SettingsSubView.VOICE_ENGINE, "Voice Engine", "Voice assistant, TTS, STT & more", Icons.Default.Mic, Color(0xFF3B82F6)),
            SettingCategoryItem(SettingsSubView.VEDMT_KNOWLEDGE, "VEDM-T Knowledge", "Manage offline AI, documents & indexing", Icons.Default.Book, Color(0xFF10B981)),
            SettingCategoryItem(SettingsSubView.VEDRIVE, "VEDrive", "File manager, backup & storage", Icons.Default.Folder, Color(0xFF06B6D4)),
            SettingCategoryItem(SettingsSubView.DEVICE_AUTOMATION, "Device & Automation", "Device controls, voice commands & more", Icons.Default.DeveloperMode, Color(0xFFF97316)),
            SettingCategoryItem(SettingsSubView.CHAT_HISTORY, "Chat & History", "Chat settings, history & data management", Icons.Default.ChatBubble, Color(0xFFEC4899)),
            SettingCategoryItem(SettingsSubView.NOTIFICATIONS, "Notifications", "Alerts, reminders & notification preferences", Icons.Default.Notifications, Color(0xFF8B5CF6)),
            SettingCategoryItem(SettingsSubView.APPEARANCE, "Appearance", "Theme, colors, font & visual preferences", Icons.Default.Palette, Color(0xFFEAB308)),
            SettingCategoryItem(SettingsSubView.PRIVACY_SECURITY, "Privacy & Security", "App lock, encryption, permissions & more", Icons.Default.Security, Color(0xFF059669)),
            SettingCategoryItem(SettingsSubView.ADVANCED, "Advanced", "Developer options, logs & performance", Icons.Default.Tune, Color(0xFF64748B)),
            SettingCategoryItem(SettingsSubView.HELP_SUPPORT, "Help & Support", "Help center, feedback & about VEDRA", Icons.Default.Help, Color(0xFF6366F1))
        )
    }

    val filteredCategories = categories.filter {
        searchQuery.isBlank() ||
                it.title.contains(searchQuery, ignoreCase = true) ||
                it.subtitle.contains(searchQuery, ignoreCase = true)
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(bottom = 32.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // VEDRA AI Header Card (Exact match to top left screenshot)
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(20.dp))
                    .background(
                        Brush.linearGradient(
                            colors = listOf(Color(0xFF18153B), Color(0xFF14102E))
                        )
                    )
                    .border(1.dp, Color(0xFF3B2E6E), RoundedCornerShape(20.dp))
                    .padding(16.dp)
            ) {
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(42.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFF8B5CF6).copy(alpha = 0.2f))
                                    .border(1.dp, Color(0xFF8B5CF6).copy(alpha = 0.5f), CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Default.Equalizer, contentDescription = null, tint = Color(0xFFA855F7), modifier = Modifier.size(22.dp))
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = "VEDRA AI",
                                        color = Color.White,
                                        fontSize = 17.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Box(
                                            modifier = Modifier
                                                .size(7.dp)
                                                .clip(CircleShape)
                                                .background(Color(0xFF10B981))
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("Online", color = Color(0xFF10B981), fontSize = 11.5.sp, fontWeight = FontWeight.SemiBold)
                                    }
                                }
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = "Your personal AI assistant",
                                    color = Color(0xFFA09EC0),
                                    fontSize = 12.sp
                                )
                            }
                        }

                        // Premium Badge
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .background(
                                    Brush.horizontalGradient(
                                        colors = listOf(Color(0xFF8B5CF6), Color(0xFFEC4899))
                                    )
                                )
                                .padding(horizontal = 10.dp, vertical = 4.dp)
                        ) {
                            Text("Premium", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Pill stats row (AI Mode, Memory, Storage)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // Stat 1: AI Mode
                        StatPillItem(
                            modifier = Modifier.weight(1f),
                            icon = Icons.Default.AutoAwesome,
                            label = "AI Mode",
                            value = "Hybrid"
                        )
                        // Stat 2: Memory
                        StatPillItem(
                            modifier = Modifier.weight(1f),
                            icon = Icons.Default.Memory,
                            label = "Memory",
                            value = "1.8 GB"
                        )
                        // Stat 3: Storage
                        StatPillItem(
                            modifier = Modifier.weight(1f),
                            icon = Icons.Default.Folder,
                            label = "Storage",
                            value = "56 GB"
                        )
                    }
                }
            }
        }

        // Settings Categories List
        items(filteredCategories) { cat ->
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color(0xFF14132B))
                    .border(1.dp, Color(0xFF28264A), RoundedCornerShape(16.dp))
                    .clickable { onNavigate(cat.type) }
                    .padding(horizontal = 14.dp, vertical = 12.dp)
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
                                .background(cat.iconColor.copy(alpha = 0.15f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(cat.icon, contentDescription = null, tint = cat.iconColor, modifier = Modifier.size(20.dp))
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = cat.title,
                                color = Color.White,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                            Spacer(modifier = Modifier.height(1.dp))
                            Text(
                                text = cat.subtitle,
                                color = Color(0xFFA09EC0),
                                fontSize = 11.5.sp,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }

                    Icon(
                        Icons.Default.ChevronRight,
                        contentDescription = null,
                        tint = Color(0xFF6B6893),
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }

        // Profile Footer Card
        item {
            Spacer(modifier = Modifier.height(6.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color(0xFF14132B))
                    .border(1.dp, Color(0xFF28264A), RoundedCornerShape(16.dp))
                    .padding(14.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(
                                    Brush.linearGradient(
                                        colors = listOf(Color(0xFF8B5CF6), Color(0xFF3B82F6))
                                    )
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("R", color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text("Rahul Kumar", color = Color.White, fontSize = 14.5.sp, fontWeight = FontWeight.Bold)
                            Text("Premium User", color = Color(0xFF8B5CF6), fontSize = 11.5.sp, fontWeight = FontWeight.Medium)
                        }
                    }

                    Column(horizontalAlignment = Alignment.End) {
                        Text("Version 1.0.0", color = Color(0xFF6B6893), fontSize = 11.sp)
                        Text("© VEDRA AI", color = Color(0xFF6B6893), fontSize = 10.sp)
                    }
                }
            }
        }
    }
}

// Helper Pill for Header Card
@Composable
private fun StatPillItem(
    modifier: Modifier,
    icon: ImageVector,
    label: String,
    value: String
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(Color(0xFF0F0E22))
            .border(1.dp, Color(0xFF252345), RoundedCornerShape(12.dp))
            .padding(vertical = 8.dp, horizontal = 6.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(icon, contentDescription = null, tint = Color(0xFF8B5CF6), modifier = Modifier.size(14.dp))
            Spacer(modifier = Modifier.width(6.dp))
            Column {
                Text(label, color = Color(0xFF6B6893), fontSize = 9.5.sp)
                Text(value, color = Color.White, fontSize = 11.5.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}

// ================= 2. AI & INTELLIGENCE SCREEN =================
@Composable
private fun AiAndIntelligenceScreen(dbService: DatabaseService) {
    val context = LocalContext.current
    var aiMode by remember { mutableStateOf(dbService.getSetting("ai_mode", "Hybrid (Recommended)")) }
    var onlineModel by remember { mutableStateOf(dbService.getSetting("ai_online_model", "Gemini 1.5 Pro")) }
    var offlineEngine by remember { mutableStateOf(dbService.getSetting("ai_offline_engine", "VEDM-T")) }
    var adaptiveLearning by remember { mutableStateOf(dbService.getSetting("ai_adaptive_learning", "true") == "true") }
    var memoryContext by remember { mutableStateOf(dbService.getSetting("ai_memory_context", "Smart Memory")) }
    var responseStyle by remember { mutableStateOf(dbService.getSetting("ai_response_style", "Balanced")) }
    var defaultLanguage by remember { mutableStateOf(dbService.getSetting("ai_default_language", "English")) }

    var showModeDialog by remember { mutableStateOf(false) }
    var showCapabilityModal by remember { mutableStateOf<String?>(null) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(bottom = 32.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // AI Mode Banner Hero Card
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(18.dp))
                    .background(Color(0xFF14132B))
                    .border(1.dp, Color(0xFF28264A), RoundedCornerShape(18.dp))
                    .padding(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(52.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF8B5CF6).copy(alpha = 0.2f))
                            .border(1.dp, Color(0xFF8B5CF6).copy(alpha = 0.4f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.Psychology, contentDescription = null, tint = Color(0xFFA855F7), modifier = Modifier.size(28.dp))
                    }

                    Spacer(modifier = Modifier.width(14.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text("AI Mode", color = Color(0xFFA09EC0), fontSize = 12.sp)
                        Text(aiMode, color = Color.White, fontSize = 15.sp, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            "Get the best of both worlds with Gemini online and VEDRA offline.",
                            color = Color(0xFF6B6893),
                            fontSize = 11.sp,
                            lineHeight = 15.sp
                        )
                        Spacer(modifier = Modifier.height(8.dp))

                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .background(Color(0xFF8B5CF6))
                                .clickable { showModeDialog = true }
                                .padding(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            Text("Change Mode", color = Color.White, fontSize = 11.5.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        // Section Title: AI Settings
        item {
            Text("AI Settings", color = Color(0xFFA855F7), fontSize = 12.sp, fontWeight = FontWeight.Bold)
        }

        // Settings items list
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color(0xFF14132B))
                    .border(1.dp, Color(0xFF28264A), RoundedCornerShape(16.dp))
            ) {
                SettingRowValue("AI Model (Online)", onlineModel) {
                    onlineModel = if (onlineModel.contains("Pro")) "Gemini 1.5 Flash" else "Gemini 1.5 Pro"
                    dbService.setSetting("ai_online_model", onlineModel)
                }
                SettingDivider()
                SettingRowValue("Offline AI Engine", offlineEngine) {
                    Toast.makeText(context, "Offline Engine: VEDM-T Active", Toast.LENGTH_SHORT).show()
                }
                SettingDivider()
                SettingRowToggle("Adaptive Learning", "Enabled", adaptiveLearning) {
                    adaptiveLearning = it
                    dbService.setSetting("ai_adaptive_learning", it.toString())
                }
                SettingDivider()
                SettingRowValue("Memory & Context", memoryContext) {
                    memoryContext = if (memoryContext == "Smart Memory") "Extended Context" else "Smart Memory"
                    dbService.setSetting("ai_memory_context", memoryContext)
                }
                SettingDivider()
                SettingRowValue("Response Style", responseStyle) {
                    responseStyle = when (responseStyle) {
                        "Balanced" -> "Precise"
                        "Precise" -> "Creative"
                        else -> "Balanced"
                    }
                    dbService.setSetting("ai_response_style", responseStyle)
                }
                SettingDivider()
                SettingRowValue("Default Language", defaultLanguage) {
                    defaultLanguage = if (defaultLanguage == "English") "Hindi" else "English"
                    dbService.setSetting("ai_default_language", defaultLanguage)
                }
                SettingDivider()
                SettingRowValue("Custom Rules", "12 rules added") {
                    Toast.makeText(context, "Managing 12 Custom System Rules", Toast.LENGTH_SHORT).show()
                }
                SettingDivider()
                SettingRowAction("Reset AI Memory", "Clear learned data", isDanger = true) {
                    Toast.makeText(context, "AI Memory cleared!", Toast.LENGTH_SHORT).show()
                }
            }
        }

        // Section Title: AI Capabilities
        item {
            Text("AI Capabilities", color = Color(0xFFA855F7), fontSize = 12.sp, fontWeight = FontWeight.Bold)
        }

        item {
            val caps = listOf(
                Pair("Text Generation", Icons.Default.TextFields),
                Pair("Document Q&A", Icons.Default.Description),
                Pair("Web Search", Icons.Default.Language),
                Pair("Code Assistant", Icons.Default.Code),
                Pair("Math Solver", Icons.Default.Calculate),
                Pair("Image Analysis", Icons.Default.Image)
            )

            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                caps.chunked(2).forEach { row ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        row.forEach { (title, icon) ->
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(Color(0xFF14132B))
                                    .border(1.dp, Color(0xFF28264A), RoundedCornerShape(12.dp))
                                    .clickable { showCapabilityModal = title }
                                    .padding(12.dp)
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(icon, contentDescription = null, tint = Color(0xFF8B5CF6), modifier = Modifier.size(18.dp))
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(title, color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Medium)
                                }
                            }
                        }
                    }
                }
            }
        }

        // Section Title: AI Usage This Week
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color(0xFF14132B))
                    .border(1.dp, Color(0xFF28264A), RoundedCornerShape(16.dp))
                    .padding(14.dp)
            ) {
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("AI Usage This Week", color = Color.White, fontSize = 13.5.sp, fontWeight = FontWeight.Bold)
                        Text("12,450 tokens", color = Color(0xFFA09EC0), fontSize = 11.5.sp)
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Token Bar Chart Graph
                    val days = listOf("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun")
                    val heights = listOf(0.4f, 0.6f, 0.3f, 0.9f, 0.5f, 0.8f, 0.45f)

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(80.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.Bottom
                    ) {
                        days.forEachIndexed { i, day ->
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Box(
                                    modifier = Modifier
                                        .width(16.dp)
                                        .fillMaxHeight(heights[i])
                                        .clip(RoundedCornerShape(topStart = 4.dp, topEnd = 4.dp))
                                        .background(
                                            Brush.verticalGradient(
                                                colors = listOf(Color(0xFF8B5CF6), Color(0xFF3B82F6))
                                            )
                                        )
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(day, color = Color(0xFF6B6893), fontSize = 10.sp)
                            }
                        }
                    }
                }
            }
        }
    }

    // Modal Dialogs
    if (showModeDialog) {
        Dialog(onDismissRequest = { showModeDialog = false }) {
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = Color(0xFF14132B),
                border = BorderStroke(1.dp, Color(0xFF8B5CF6)),
                modifier = Modifier.padding(16.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Select AI Mode", color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(12.dp))
                    listOf("Hybrid (Recommended)", "Online Only (Gemini 1.5 Pro)", "Offline Only (VEDM-T)").forEach { option ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    aiMode = option
                                    dbService.setSetting("ai_mode", option)
                                    showModeDialog = false
                                }
                                .padding(vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(option, color = if (aiMode == option) Color(0xFF8B5CF6) else Color.White, fontSize = 13.sp)
                        }
                    }
                }
            }
        }
    }

    if (showCapabilityModal != null) {
        Dialog(onDismissRequest = { showCapabilityModal = null }) {
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = Color(0xFF14132B),
                border = BorderStroke(1.dp, Color(0xFF8B5CF6)),
                modifier = Modifier.padding(16.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("AI Feature: ${showCapabilityModal}", color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("This capability is fully active and optimized in VEDRA AI engine.", color = Color(0xFFA09EC0), fontSize = 12.sp)
                    Spacer(modifier = Modifier.height(12.dp))
                    Box(
                        modifier = Modifier
                            .align(Alignment.End)
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color(0xFF8B5CF6))
                            .clickable { showCapabilityModal = null }
                            .padding(horizontal = 14.dp, vertical = 6.dp)
                    ) {
                        Text("Close", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

// ================= 3. VOICE ENGINE SCREEN =================
@Composable
private fun VoiceEngineScreen(dbService: DatabaseService, voiceService: VoiceService) {
    val context = LocalContext.current
    var wakeWord by remember { mutableStateOf(dbService.getSetting("voice_wake_word", "Hey VEDRA")) }
    var voiceLang by remember { mutableStateOf(dbService.getSetting("voice_input_lang", "English (India)")) }
    var speechRecognition by remember { mutableStateOf(dbService.getSetting("voice_speech_rec", "true") == "true") }
    var ttsVoice by remember { mutableStateOf(dbService.getSetting("voice_tts", "VEDRA Neural Voice")) }

    var voiceSpeed by remember { mutableFloatStateOf(dbService.getSetting("voice_speed", "1.0").toFloatOrNull() ?: 1.0f) }
    var voicePitch by remember { mutableFloatStateOf(dbService.getSetting("voice_pitch", "0.0").toFloatOrNull() ?: 0.0f) }
    var voiceVolume by remember { mutableFloatStateOf(dbService.getSetting("voice_volume", "0.8").toFloatOrNull() ?: 0.8f) }

    var autoSpeak by remember { mutableStateOf(dbService.getSetting("voice_auto_speak", "true") == "true") }
    var handsFree by remember { mutableStateOf(dbService.getSetting("voice_hands_free", "false") == "true") }
    var bgListening by remember { mutableStateOf(dbService.getSetting("voice_bg_listening", "true") == "true") }
    var commandHints by remember { mutableStateOf(dbService.getSetting("voice_cmd_hints", "true") == "true") }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(bottom = 32.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Voice Assistant Banner
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(18.dp))
                    .background(Color(0xFF14132B))
                    .border(1.dp, Color(0xFF28264A), RoundedCornerShape(18.dp))
                    .padding(16.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(52.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF3B82F6).copy(alpha = 0.2f))
                            .border(1.dp, Color(0xFF3B82F6).copy(alpha = 0.4f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.Mic, contentDescription = null, tint = Color(0xFF3B82F6), modifier = Modifier.size(28.dp))
                    }
                    Spacer(modifier = Modifier.width(14.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Voice Assistant", color = Color.White, fontSize = 15.sp, fontWeight = FontWeight.Bold)
                        Text("Talk to VEDRA naturally and get instant voice responses.", color = Color(0xFFA09EC0), fontSize = 11.5.sp)
                        Spacer(modifier = Modifier.height(8.dp))
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .background(Color(0xFF3B82F6))
                                .clickable {
                                    voiceService.speak("Hello Rahul! Voice engine is functioning perfectly.")
                                }
                                .padding(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            Text("► Test Voice", color = Color.White, fontSize = 11.5.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        item {
            Text("Voice Preferences", color = Color(0xFFA855F7), fontSize = 12.sp, fontWeight = FontWeight.Bold)
        }

        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color(0xFF14132B))
                    .border(1.dp, Color(0xFF28264A), RoundedCornerShape(16.dp))
            ) {
                SettingRowValue("Wake Word", wakeWord) {
                    wakeWord = if (wakeWord == "Hey VEDRA") "OK VEDRA" else "Hey VEDRA"
                    dbService.setSetting("voice_wake_word", wakeWord)
                }
                SettingDivider()
                SettingRowValue("Voice Input Language", voiceLang) {
                    voiceLang = if (voiceLang.contains("India")) "English (US)" else "English (India)"
                    dbService.setSetting("voice_input_lang", voiceLang)
                }
                SettingDivider()
                SettingRowToggle("Speech Recognition", "High Accuracy", speechRecognition) {
                    speechRecognition = it
                    dbService.setSetting("voice_speech_rec", it.toString())
                }
                SettingDivider()
                SettingRowValue("Text-to-Speech (TTS)", ttsVoice) {
                    ttsVoice = if (ttsVoice.contains("Neural")) "VEDRA Expressive Voice" else "VEDRA Neural Voice"
                    dbService.setSetting("voice_tts", ttsVoice)
                }
                SettingDivider()

                // Sliders
                SettingSliderRow("Voice Speed", "${String.format("%.1f", voiceSpeed)}x", voiceSpeed, 0.5f, 2.0f) {
                    voiceSpeed = it
                    dbService.setSetting("voice_speed", it.toString())
                }
                SettingDivider()
                SettingSliderRow("Voice Pitch", if (voicePitch == 0f) "0 (Normal)" else String.format("%.1f", voicePitch), voicePitch, -5f, 5f) {
                    voicePitch = it
                    dbService.setSetting("voice_pitch", it.toString())
                }
                SettingDivider()
                SettingSliderRow("Voice Volume", "${(voiceVolume * 100).toInt()}%", voiceVolume, 0f, 1f) {
                    voiceVolume = it
                    dbService.setSetting("voice_volume", it.toString())
                }
                SettingDivider()

                SettingRowToggle("Auto Speak Responses", null, autoSpeak) {
                    autoSpeak = it
                    dbService.setSetting("voice_auto_speak", it.toString())
                }
                SettingDivider()
                SettingRowToggle("Hands-free Mode", "Disabled", handsFree) {
                    handsFree = it
                    dbService.setSetting("voice_hands_free", it.toString())
                }
                SettingDivider()
                SettingRowToggle("Background Listening", "Enabled", bgListening) {
                    bgListening = it
                    dbService.setSetting("voice_bg_listening", it.toString())
                }
                SettingDivider()
                SettingRowToggle("Voice Command Hints", "Enabled", commandHints) {
                    commandHints = it
                    dbService.setSetting("voice_cmd_hints", it.toString())
                }
            }
        }

        // Voice Activity Chart Card
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color(0xFF14132B))
                    .border(1.dp, Color(0xFF28264A), RoundedCornerShape(16.dp))
                    .padding(14.dp)
            ) {
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Voice Activity", color = Color.White, fontSize = 13.5.sp, fontWeight = FontWeight.Bold)
                        Text("This Week v", color = Color(0xFFA09EC0), fontSize = 11.5.sp)
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("Total Commands", color = Color(0xFF6B6893), fontSize = 10.5.sp)
                            Text("128", color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                            Spacer(modifier = Modifier.height(6.dp))
                            Text("Success Rate", color = Color(0xFF6B6893), fontSize = 10.5.sp)
                            Text("96%", color = Color(0xFF10B981), fontSize = 16.sp, fontWeight = FontWeight.Bold)
                        }

                        // Circular Donut Progress Indicator
                        Box(
                            modifier = Modifier.size(70.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator(
                                progress = { 0.96f },
                                modifier = Modifier.fillMaxSize(),
                                color = Color(0xFF8B5CF6),
                                strokeWidth = 7.dp,
                                trackColor = Color(0xFF28264A)
                            )
                            Text("96%", color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}

// ================= 4. VEDM-T KNOWLEDGE BASE SCREEN =================
@Composable
private fun VedmtKnowledgeScreen(dbService: DatabaseService) {
    val context = LocalContext.current
    var searchDepth by remember { mutableStateOf(dbService.getSetting("rag_search_depth", "High")) }
    var topKResults by remember { mutableStateOf(dbService.getSetting("rag_top_k", "5")) }
    var similarityThreshold by remember { mutableFloatStateOf(dbService.getSetting("rag_similarity", "0.65").toFloatOrNull() ?: 0.65f) }
    var useMetadata by remember { mutableStateOf(dbService.getSetting("rag_use_metadata", "true") == "true") }
    var autoUpdateIndex by remember { mutableStateOf(dbService.getSetting("rag_auto_update", "true") == "true") }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(bottom = 32.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Hero Card
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(18.dp))
                    .background(Color(0xFF14132B))
                    .border(1.dp, Color(0xFF28264A), RoundedCornerShape(18.dp))
                    .padding(16.dp)
            ) {
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(50.dp)
                                .clip(CircleShape)
                                .background(Color(0xFF10B981).copy(alpha = 0.2f))
                                .border(1.dp, Color(0xFF10B981).copy(alpha = 0.4f), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.Book, contentDescription = null, tint = Color(0xFF10B981), modifier = Modifier.size(26.dp))
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text("VEDM-T Knowledge Base", color = Color.White, fontSize = 15.sp, fontWeight = FontWeight.Bold)
                            Text("Your offline AI brain. Import .txt files and get customized answers.", color = Color(0xFFA09EC0), fontSize = 11.5.sp)
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(10.dp))
                                .background(Color(0xFF0F0E22))
                                .padding(8.dp)
                        ) {
                            Column {
                                Text("Indexed Files", color = Color(0xFF6B6893), fontSize = 9.5.sp)
                                Text("128", color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(10.dp))
                                .background(Color(0xFF0F0E22))
                                .padding(8.dp)
                        ) {
                            Column {
                                Text("Total Size", color = Color(0xFF6B6893), fontSize = 9.5.sp)
                                Text("256 MB", color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }

        item {
            Text("Knowledge Settings", color = Color(0xFFA855F7), fontSize = 12.sp, fontWeight = FontWeight.Bold)
        }

        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color(0xFF14132B))
                    .border(1.dp, Color(0xFF28264A), RoundedCornerShape(16.dp))
            ) {
                SettingRowDetail("Import .txt Files", "Add new documents to knowledge base") {
                    Toast.makeText(context, "Opening document importer...", Toast.LENGTH_SHORT).show()
                }
                SettingDivider()
                SettingRowDetail("Indexed Documents", "View and manage indexed files") {
                    Toast.makeText(context, "128 Documents Indexed", Toast.LENGTH_SHORT).show()
                }
                SettingDivider()
                SettingRowProgress("Training Progress", "Last updated 2 hours ago", 0.75f, "75%")
                SettingDivider()
                SettingRowDetail("Rebuild Database", "Re-index all documents") {
                    Toast.makeText(context, "Rebuilding database index...", Toast.LENGTH_SHORT).show()
                }
                SettingDivider()
                SettingRowDetail("Export Database", "Backup knowledge base") {
                    Toast.makeText(context, "Exporting knowledge database...", Toast.LENGTH_SHORT).show()
                }
                SettingDivider()
                SettingRowAction("Clear Knowledge", "Remove all indexed data", isDanger = true) {
                    Toast.makeText(context, "Knowledge data cleared!", Toast.LENGTH_SHORT).show()
                }
            }
        }

        item {
            Text("RAG Preferences", color = Color(0xFFA855F7), fontSize = 12.sp, fontWeight = FontWeight.Bold)
        }

        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color(0xFF14132B))
                    .border(1.dp, Color(0xFF28264A), RoundedCornerShape(16.dp))
            ) {
                SettingRowValue("Search Depth", searchDepth) {
                    searchDepth = if (searchDepth == "High") "Deep RAG" else "High"
                    dbService.setSetting("rag_search_depth", searchDepth)
                }
                SettingDivider()
                SettingRowValue("Top-K Results", topKResults) {
                    topKResults = if (topKResults == "5") "10" else "5"
                    dbService.setSetting("rag_top_k", topKResults)
                }
                SettingDivider()
                SettingSliderRow("Similarity Threshold", String.format("%.2f", similarityThreshold), similarityThreshold, 0f, 1f) {
                    similarityThreshold = it
                    dbService.setSetting("rag_similarity", it.toString())
                }
                SettingDivider()
                SettingRowToggle("Use File Metadata", null, useMetadata) {
                    useMetadata = it
                    dbService.setSetting("rag_use_metadata", it.toString())
                }
                SettingDivider()
                SettingRowToggle("Auto Update Index", null, autoUpdateIndex) {
                    autoUpdateIndex = it
                    dbService.setSetting("rag_auto_update", it.toString())
                }
            }
        }

        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color(0xFF14132B))
                    .border(1.dp, Color(0xFF28264A), RoundedCornerShape(16.dp))
                    .padding(14.dp)
            ) {
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Storage Used", color = Color(0xFF6B6893), fontSize = 11.sp)
                        Text("256 MB / 2 GB", color = Color.White, fontSize = 11.5.sp, fontWeight = FontWeight.Bold)
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(6.dp)
                            .clip(RoundedCornerShape(3.dp))
                            .background(Color(0xFF28264A))
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxHeight()
                                .fillMaxWidth(0.12f)
                                .clip(RoundedCornerShape(3.dp))
                                .background(Color(0xFF06B6D4))
                        )
                    }
                }
            }
        }
    }
}

// ================= 5. VEDRIVE SETTINGS SCREEN =================
@Composable
private fun VedriveSettingsScreen(dbService: DatabaseService) {
    val context = LocalContext.current
    var autoBackup by remember { mutableStateOf(dbService.getSetting("drive_auto_backup", "Daily")) }
    var fileView by remember { mutableStateOf(dbService.getSetting("drive_file_view", "Grid View")) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(bottom = 32.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(18.dp))
                    .background(Color(0xFF14132B))
                    .border(1.dp, Color(0xFF28264A), RoundedCornerShape(18.dp))
                    .padding(16.dp)
            ) {
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(50.dp)
                                .clip(CircleShape)
                                .background(Color(0xFF06B6D4).copy(alpha = 0.2f))
                                .border(1.dp, Color(0xFF06B6D4).copy(alpha = 0.4f), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.Folder, contentDescription = null, tint = Color(0xFF06B6D4), modifier = Modifier.size(26.dp))
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text("VEDrive", color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                            Text("Manage, sync and backup your files across devices and accounts.", color = Color(0xFFA09EC0), fontSize = 11.5.sp)
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Storage Used", color = Color(0xFF6B6893), fontSize = 11.sp)
                        Text("12.4 GB / 100 GB", color = Color.White, fontSize = 11.5.sp, fontWeight = FontWeight.Bold)
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(6.dp)
                            .clip(RoundedCornerShape(3.dp))
                            .background(Color(0xFF28264A))
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxHeight()
                                .fillMaxWidth(0.124f)
                                .clip(RoundedCornerShape(3.dp))
                                .background(Color(0xFF3B82F6))
                        )
                    }
                }
            }
        }

        item {
            Text("Drive Settings", color = Color(0xFFA855F7), fontSize = 12.sp, fontWeight = FontWeight.Bold)
        }

        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color(0xFF14132B))
                    .border(1.dp, Color(0xFF28264A), RoundedCornerShape(16.dp))
            ) {
                SettingRowValue("Google Drive Sync", "Connected", isSuccess = true) {
                    Toast.makeText(context, "Google Drive Account Synced", Toast.LENGTH_SHORT).show()
                }
                SettingDivider()
                SettingRowDetail("Backup & Restore", "Backup and manage your data") {
                    Toast.makeText(context, "Initiating Cloud Backup...", Toast.LENGTH_SHORT).show()
                }
                SettingDivider()
                SettingRowValue("Auto Backup", autoBackup) {
                    autoBackup = if (autoBackup == "Daily") "Weekly" else "Daily"
                    dbService.setSetting("drive_auto_backup", autoBackup)
                }
                SettingDivider()
                SettingRowValue("Default Save Location", "/VEDrive") {
                    Toast.makeText(context, "Default path: /VEDrive", Toast.LENGTH_SHORT).show()
                }
                SettingDivider()
                SettingRowDetail("Manage Storage", "Clean cache and manage space") {
                    Toast.makeText(context, "Cleaning Drive Cache...", Toast.LENGTH_SHORT).show()
                }
                SettingDivider()
                SettingRowValue("File View", fileView) {
                    fileView = if (fileView == "Grid View") "List View" else "Grid View"
                    dbService.setSetting("drive_file_view", fileView)
                }
                SettingDivider()
                SettingRowDetail("File Permissions", "Manage access and visibility") {
                    Toast.makeText(context, "Permissions configured", Toast.LENGTH_SHORT).show()
                }
            }
        }

        item {
            Text("Recent Backups", color = Color(0xFFA855F7), fontSize = 12.sp, fontWeight = FontWeight.Bold)
        }

        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color(0xFF14132B))
                    .border(1.dp, Color(0xFF28264A), RoundedCornerShape(16.dp))
            ) {
                BackupHistoryRow("2 May 2025, 10:30 AM", "1.24 GB")
                SettingDivider()
                BackupHistoryRow("1 May 2025, 10:30 AM", "1.18 GB")
                SettingDivider()
                BackupHistoryRow("30 Apr 2025, 10:30 AM", "983 MB")
            }
        }

        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(Color(0xFF14132B))
                    .border(1.dp, Color(0xFF10B981).copy(alpha = 0.4f), RoundedCornerShape(14.dp))
                    .padding(12.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.CheckCircle, contentDescription = null, tint = Color(0xFF10B981), modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Sync Status: All files are up to date", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Medium)
                }
            }
        }
    }
}

// ================= 6. DEVICE & AUTOMATION SCREEN =================
@Composable
private fun DeviceAutomationScreen(dbService: DatabaseService) {
    val context = LocalContext.current

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(bottom = 32.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(18.dp))
                    .background(Color(0xFF14132B))
                    .border(1.dp, Color(0xFF28264A), RoundedCornerShape(18.dp))
                    .padding(16.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(50.dp)
                            .clip(CircleShape)
                            .background(Color(0xFFF97316).copy(alpha = 0.2f))
                            .border(1.dp, Color(0xFFF97316).copy(alpha = 0.4f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.DeveloperMode, contentDescription = null, tint = Color(0xFFF97316), modifier = Modifier.size(26.dp))
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text("Device & Automation", color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                        Text("Control your device and automate tasks using voice.", color = Color(0xFFA09EC0), fontSize = 11.5.sp)
                    }
                }
            }
        }

        item {
            Text("Device Controls", color = Color(0xFFA855F7), fontSize = 12.sp, fontWeight = FontWeight.Bold)
        }

        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color(0xFF14132B))
                    .border(1.dp, Color(0xFF28264A), RoundedCornerShape(16.dp))
            ) {
                SettingRowDetail("App Launcher", "Open apps by voice or text") {
                    Toast.makeText(context, "App Launcher Active", Toast.LENGTH_SHORT).show()
                }
                SettingDivider()
                SettingRowSimple("Torch / Flashlight") {
                    Toast.makeText(context, "Torch toggled", Toast.LENGTH_SHORT).show()
                }
                SettingDivider()
                SettingRowSimple("Volume Control") {
                    Toast.makeText(context, "Volume settings opened", Toast.LENGTH_SHORT).show()
                }
                SettingDivider()
                SettingRowSimple("Brightness Control") {
                    Toast.makeText(context, "Brightness adjusted", Toast.LENGTH_SHORT).show()
                }
                SettingDivider()
                SettingRowDetail("Do Not Disturb", "Manage DND and focus mode") {
                    Toast.makeText(context, "Focus Mode Enabled", Toast.LENGTH_SHORT).show()
                }
                SettingDivider()
                SettingRowDetail("Battery Optimization", "Optimize battery for VEDRA") {
                    Toast.makeText(context, "Battery Optimized", Toast.LENGTH_SHORT).show()
                }
            }
        }

        item {
            Text("Automation", color = Color(0xFFA855F7), fontSize = 12.sp, fontWeight = FontWeight.Bold)
        }

        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color(0xFF14132B))
                    .border(1.dp, Color(0xFF28264A), RoundedCornerShape(16.dp))
            ) {
                SettingRowDetail("Smart Actions", "Automate your daily tasks") {
                    Toast.makeText(context, "Creating Smart Action...", Toast.LENGTH_SHORT).show()
                }
                SettingDivider()
                SettingRowDetail("Scheduled Tasks", "Run tasks automatically") {
                    Toast.makeText(context, "Scheduled Tasks list", Toast.LENGTH_SHORT).show()
                }
                SettingDivider()
                SettingRowDetail("Routines", "Create custom routines") {
                    Toast.makeText(context, "Routines configured", Toast.LENGTH_SHORT).show()
                }
                SettingDivider()
                SettingRowDetail("Background Tasks", "Manage background operations") {
                    Toast.makeText(context, "Background operations normal", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
}

// ================= 7. CHAT & HISTORY SCREEN =================
@Composable
private fun ChatHistoryScreen(dbService: DatabaseService) {
    val context = LocalContext.current
    var autoTitle by remember { mutableStateOf(dbService.getSetting("chat_auto_title", "true") == "true") }
    var pinChats by remember { mutableStateOf(dbService.getSetting("chat_pin_chats", "true") == "true") }
    var autoDelete by remember { mutableStateOf(dbService.getSetting("chat_auto_delete", "After 30 days")) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(bottom = 32.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(18.dp))
                    .background(Color(0xFF14132B))
                    .border(1.dp, Color(0xFF28264A), RoundedCornerShape(18.dp))
                    .padding(16.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(50.dp)
                            .clip(CircleShape)
                            .background(Color(0xFFEC4899).copy(alpha = 0.2f))
                            .border(1.dp, Color(0xFFEC4899).copy(alpha = 0.4f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.ChatBubble, contentDescription = null, tint = Color(0xFFEC4899), modifier = Modifier.size(26.dp))
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text("Chat & History", color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                        Text("Manage your conversations and history.", color = Color(0xFFA09EC0), fontSize = 11.5.sp)
                    }
                }
            }
        }

        item {
            Text("Chat Settings", color = Color(0xFFA855F7), fontSize = 12.sp, fontWeight = FontWeight.Bold)
        }

        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color(0xFF14132B))
                    .border(1.dp, Color(0xFF28264A), RoundedCornerShape(16.dp))
            ) {
                SettingRowToggle("Auto Title", "Generate titles automatically", autoTitle) {
                    autoTitle = it
                    dbService.setSetting("chat_auto_title", it.toString())
                }
                SettingDivider()
                SettingRowToggle("Pin Chats", "Pin important chats to top", pinChats) {
                    pinChats = it
                    dbService.setSetting("chat_pin_chats", it.toString())
                }
                SettingDivider()
                SettingRowDetail("Rename Chats", "Custom any chat") {
                    Toast.makeText(context, "Rename chat dialog", Toast.LENGTH_SHORT).show()
                }
                SettingDivider()
                SettingRowDetail("Export Chats", "Export chats as .txt or .pdf") {
                    Toast.makeText(context, "Exporting chat history...", Toast.LENGTH_SHORT).show()
                }
                SettingDivider()
                SettingRowValue("Auto Delete", autoDelete) {
                    autoDelete = if (autoDelete.contains("30")) "Never" else "After 30 days"
                    dbService.setSetting("chat_auto_delete", autoDelete)
                }
                SettingDivider()
                SettingRowAction("Clear All Chats", "Delete all conversations", isDanger = true) {
                    Toast.makeText(context, "All chats cleared!", Toast.LENGTH_SHORT).show()
                }
            }
        }

        item {
            Text("History Management", color = Color(0xFFA855F7), fontSize = 12.sp, fontWeight = FontWeight.Bold)
        }

        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color(0xFF14132B))
                    .border(1.dp, Color(0xFF28264A), RoundedCornerShape(16.dp))
            ) {
                SettingRowDetail("Search in Chats", "Search messages and chats") {
                    Toast.makeText(context, "Search bar focused", Toast.LENGTH_SHORT).show()
                }
                SettingDivider()
                SettingRowDetail("Chat History Backup", "Backup your chat history") {
                    Toast.makeText(context, "Chat backup created!", Toast.LENGTH_SHORT).show()
                }
                SettingDivider()
                SettingRowDetail("Import Chat History", "Import from backup file") {
                    Toast.makeText(context, "Selecting backup file...", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
}

// ================= 8. APPEARANCE SCREEN =================
@Composable
private fun AppearanceSettingsScreen(dbService: DatabaseService) {
    val context = LocalContext.current
    var selectedTheme by remember { mutableStateOf(dbService.getSetting("app_theme_mode", "Dark")) }
    var selectedColor by remember { mutableStateOf(dbService.getSetting("app_accent_color", "#8B5CF6")) }
    var fontSize by remember { mutableStateOf(dbService.getSetting("app_font_size", "Medium")) }
    var bubbleStyle by remember { mutableStateOf(dbService.getSetting("app_bubble_style", "Modern")) }
    var animations by remember { mutableStateOf(dbService.getSetting("app_animations", "true") == "true") }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(bottom = 32.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(18.dp))
                    .background(Color(0xFF14132B))
                    .border(1.dp, Color(0xFF28264A), RoundedCornerShape(18.dp))
                    .padding(16.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(50.dp)
                            .clip(CircleShape)
                            .background(Color(0xFFEAB308).copy(alpha = 0.2f))
                            .border(1.dp, Color(0xFFEAB308).copy(alpha = 0.4f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.Palette, contentDescription = null, tint = Color(0xFFEAB308), modifier = Modifier.size(26.dp))
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text("Appearance", color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                        Text("Customize the look and feel of VEDRA.", color = Color(0xFFA09EC0), fontSize = 11.5.sp)
                    }
                }
            }
        }

        item {
            Text("Theme", color = Color(0xFFA855F7), fontSize = 12.sp, fontWeight = FontWeight.Bold)
        }

        // Theme selector buttons
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                listOf("Dark", "Light", "System").forEach { theme ->
                    val isSelected = selectedTheme == theme
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(12.dp))
                            .background(if (isSelected) Color(0xFF1F1B47) else Color(0xFF14132B))
                            .border(
                                1.dp,
                                if (isSelected) Color(0xFF8B5CF6) else Color(0xFF28264A),
                                RoundedCornerShape(12.dp)
                            )
                            .clickable {
                                selectedTheme = theme
                                dbService.setSetting("app_theme_mode", theme)
                            }
                            .padding(vertical = 12.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            if (isSelected) {
                                Icon(Icons.Default.Check, contentDescription = null, tint = Color(0xFF8B5CF6), modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                            }
                            Text(theme, color = Color.White, fontSize = 12.5.sp, fontWeight = FontWeight.SemiBold)
                        }
                    }
                }
            }
        }

        item {
            Text("Accent Color", color = Color(0xFFA855F7), fontSize = 12.sp, fontWeight = FontWeight.Bold)
        }

        // Accent Color Swatches
        item {
            val colors = listOf(
                Pair("#8B5CF6", Color(0xFF8B5CF6)),
                Pair("#3B82F6", Color(0xFF3B82F6)),
                Pair("#10B981", Color(0xFF10B981)),
                Pair("#F97316", Color(0xFFF97316)),
                Pair("#EC4899", Color(0xFFEC4899)),
                Pair("#EF4444", Color(0xFFEF4444))
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                colors.forEach { (hex, col) ->
                    val isSel = selectedColor == hex
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(col)
                            .border(
                                width = if (isSel) 3.dp else 0.dp,
                                color = Color.White,
                                shape = CircleShape
                            )
                            .clickable {
                                selectedColor = hex
                                dbService.setSetting("app_accent_color", hex)
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        if (isSel) {
                            Icon(Icons.Default.Check, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                        }
                    }
                }
            }
        }

        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color(0xFF14132B))
                    .border(1.dp, Color(0xFF28264A), RoundedCornerShape(16.dp))
            ) {
                SettingRowValue("Font Size", fontSize) {
                    fontSize = if (fontSize == "Medium") "Large" else "Medium"
                    dbService.setSetting("app_font_size", fontSize)
                }
                SettingDivider()
                SettingRowValue("Chat Bubble Style", bubbleStyle) {
                    bubbleStyle = if (bubbleStyle == "Modern") "Classic" else "Modern"
                    dbService.setSetting("app_bubble_style", bubbleStyle)
                }
                SettingDivider()
                SettingRowToggle("Animations", null, animations) {
                    animations = it
                    dbService.setSetting("app_animations", it.toString())
                }
                SettingDivider()
                SettingRowValue("Wallpapers", "Default") {
                    Toast.makeText(context, "Wallpaper picker", Toast.LENGTH_SHORT).show()
                }
                SettingDivider()
                SettingRowValue("Icon Pack", "Default") {
                    Toast.makeText(context, "Icon Pack settings", Toast.LENGTH_SHORT).show()
                }
            }
        }

        // Live Chat Bubble Preview Card
        item {
            Text("Preview", color = Color(0xFFA855F7), fontSize = 12.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(4.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color(0xFF14132B))
                    .border(1.dp, Color(0xFF28264A), RoundedCornerShape(16.dp))
                    .padding(14.dp)
            ) {
                Row(verticalAlignment = Alignment.Top) {
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF8B5CF6)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.Equalizer, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(14.dp))
                            .background(Color(0xFF1F1B47))
                            .padding(12.dp)
                    ) {
                        Column {
                            Text("VEDRA", color = Color(0xFF8B5CF6), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            Text("Hi Rahul! How can I help you today?", color = Color.White, fontSize = 12.5.sp)
                        }
                    }
                }
            }
        }
    }
}

// ================= 9. PRIVACY & SECURITY SCREEN =================
@Composable
private fun PrivacySecurityScreen(dbService: DatabaseService) {
    val context = LocalContext.current
    var appLock by remember { mutableStateOf(dbService.getSetting("sec_app_lock", "true") == "true") }
    var fingerprintLock by remember { mutableStateOf(dbService.getSetting("sec_fingerprint", "true") == "true") }
    var faceUnlock by remember { mutableStateOf(dbService.getSetting("sec_face_unlock", "false") == "true") }
    var autoLock by remember { mutableStateOf(dbService.getSetting("sec_auto_lock", "After 5 minutes")) }
    var dataEncryption by remember { mutableStateOf(dbService.getSetting("sec_encryption", "true") == "true") }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(bottom = 32.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(18.dp))
                    .background(Color(0xFF14132B))
                    .border(1.dp, Color(0xFF28264A), RoundedCornerShape(18.dp))
                    .padding(16.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(50.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF059669).copy(alpha = 0.2f))
                            .border(1.dp, Color(0xFF059669).copy(alpha = 0.4f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.Security, contentDescription = null, tint = Color(0xFF059669), modifier = Modifier.size(26.dp))
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text("Privacy & Security", color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                        Text("Keep your data safe and secure.", color = Color(0xFFA09EC0), fontSize = 11.5.sp)
                    }
                }
            }
        }

        item {
            Text("Security", color = Color(0xFFA855F7), fontSize = 12.sp, fontWeight = FontWeight.Bold)
        }

        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color(0xFF14132B))
                    .border(1.dp, Color(0xFF28264A), RoundedCornerShape(16.dp))
            ) {
                SettingRowToggle("App Lock", "Protect app with PIN / Pattern", appLock) {
                    appLock = it
                    dbService.setSetting("sec_app_lock", it.toString())
                }
                SettingDivider()
                SettingRowToggle("Fingerprint Lock", "Use fingerprint to unlock", fingerprintLock) {
                    fingerprintLock = it
                    dbService.setSetting("sec_fingerprint", it.toString())
                }
                SettingDivider()
                SettingRowToggle("Face Unlock", "Use face to unlock", faceUnlock) {
                    faceUnlock = it
                    dbService.setSetting("sec_face_unlock", it.toString())
                }
                SettingDivider()
                SettingRowValue("Auto Lock", autoLock) {
                    autoLock = if (autoLock.contains("5")) "Immediately" else "After 5 minutes"
                    dbService.setSetting("sec_auto_lock", autoLock)
                }
                SettingDivider()
                SettingRowToggle("Data Encryption", "All data is encrypted", dataEncryption) {
                    dataEncryption = it
                    dbService.setSetting("sec_encryption", it.toString())
                }
            }
        }

        item {
            Text("Privacy", color = Color(0xFFA855F7), fontSize = 12.sp, fontWeight = FontWeight.Bold)
        }

        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color(0xFF14132B))
                    .border(1.dp, Color(0xFF28264A), RoundedCornerShape(16.dp))
            ) {
                SettingRowDetail("Permissions", "Manage app permissions") {
                    Toast.makeText(context, "Permissions settings", Toast.LENGTH_SHORT).show()
                }
                SettingDivider()
                SettingRowDetail("Privacy Dashboard", "View data usage and activity") {
                    Toast.makeText(context, "Privacy Dashboard opened", Toast.LENGTH_SHORT).show()
                }
                SettingDivider()
                SettingRowAction("Clear Local Data", "Clear cache and local data", isDanger = true) {
                    Toast.makeText(context, "Local cache cleared!", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
}

// ================= 10. NOTIFICATIONS SCREEN =================
@Composable
private fun NotificationsSettingsScreen(dbService: DatabaseService) {
    val context = LocalContext.current
    var aiNotif by remember { mutableStateOf(dbService.getSetting("notif_ai", "true") == "true") }
    var studyReminders by remember { mutableStateOf(dbService.getSetting("notif_study", "true") == "true") }
    var dailySummary by remember { mutableStateOf(dbService.getSetting("notif_summary", "true") == "true") }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(bottom = 32.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(18.dp))
                    .background(Color(0xFF14132B))
                    .border(1.dp, Color(0xFF28264A), RoundedCornerShape(18.dp))
                    .padding(16.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(50.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF8B5CF6).copy(alpha = 0.2f))
                            .border(1.dp, Color(0xFF8B5CF6).copy(alpha = 0.4f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.Notifications, contentDescription = null, tint = Color(0xFF8B5CF6), modifier = Modifier.size(26.dp))
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text("Notifications", color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                        Text("Manage alerts, reminders and notifications.", color = Color(0xFFA09EC0), fontSize = 11.5.sp)
                    }
                }
            }
        }

        item {
            Text("App Notifications", color = Color(0xFFA855F7), fontSize = 12.sp, fontWeight = FontWeight.Bold)
        }

        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color(0xFF14132B))
                    .border(1.dp, Color(0xFF28264A), RoundedCornerShape(16.dp))
            ) {
                SettingRowToggle("AI Notifications", "Get notified about AI updates", aiNotif) {
                    aiNotif = it
                    dbService.setSetting("notif_ai", it.toString())
                }
                SettingDivider()
                SettingRowToggle("Study Reminders", "Remind me to study", studyReminders) {
                    studyReminders = it
                    dbService.setSetting("notif_study", it.toString())
                }
                SettingDivider()
                SettingRowToggle("Daily Summary", "Get daily study summary", dailySummary) {
                    dailySummary = it
                    dbService.setSetting("notif_summary", it.toString())
                }
                SettingDivider()
                SettingRowDetail("Reminder Alerts", "Custom reminders and alerts") {
                    Toast.makeText(context, "Reminders list", Toast.LENGTH_SHORT).show()
                }
            }
        }

        item {
            Text("Notification Preferences", color = Color(0xFFA855F7), fontSize = 12.sp, fontWeight = FontWeight.Bold)
        }

        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color(0xFF14132B))
                    .border(1.dp, Color(0xFF28264A), RoundedCornerShape(16.dp))
            ) {
                SettingRowValue("Sound", "Default") {
                    Toast.makeText(context, "Notification sound selector", Toast.LENGTH_SHORT).show()
                }
                SettingDivider()
                SettingRowValue("Vibration", "Banners") {
                    Toast.makeText(context, "Vibration pattern selector", Toast.LENGTH_SHORT).show()
                }
                SettingDivider()
                SettingRowValue("Pop-up Style", "Banners") {
                    Toast.makeText(context, "Banner style selector", Toast.LENGTH_SHORT).show()
                }
                SettingDivider()
                SettingRowValue("Do Not Disturb", "11:00 PM - 7:00 AM") {
                    Toast.makeText(context, "Quiet Hours configured", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
}

// ================= 11. ADVANCED SCREEN =================
@Composable
private fun AdvancedSettingsScreen(dbService: DatabaseService) {
    val context = LocalContext.current

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(bottom = 32.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(18.dp))
                    .background(Color(0xFF14132B))
                    .border(1.dp, Color(0xFF28264A), RoundedCornerShape(18.dp))
                    .padding(16.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(50.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF64748B).copy(alpha = 0.2f))
                            .border(1.dp, Color(0xFF64748B).copy(alpha = 0.4f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.Tune, contentDescription = null, tint = Color(0xFF64748B), modifier = Modifier.size(26.dp))
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text("Advanced Settings", color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                        Text("Developer options, logs and system configurations.", color = Color(0xFFA09EC0), fontSize = 11.5.sp)
                    }
                }
            }
        }

        item {
            Text("System & Performance", color = Color(0xFFA855F7), fontSize = 12.sp, fontWeight = FontWeight.Bold)
        }

        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color(0xFF14132B))
                    .border(1.dp, Color(0xFF28264A), RoundedCornerShape(16.dp))
            ) {
                SettingRowDetail("Developer Mode", "Enable experimental features") {
                    Toast.makeText(context, "Developer Mode Active", Toast.LENGTH_SHORT).show()
                }
                SettingDivider()
                SettingRowDetail("View System Logs", "Inspect live event logs") {
                    Toast.makeText(context, "Logs viewer", Toast.LENGTH_SHORT).show()
                }
                SettingDivider()
                SettingRowDetail("API Key Configuration", "Manage custom Gemini API keys") {
                    Toast.makeText(context, "API Key manager", Toast.LENGTH_SHORT).show()
                }
                SettingDivider()
                SettingRowAction("Clear System Cache", "Free up temporary storage", isDanger = false) {
                    Toast.makeText(context, "System cache purged!", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
}

// ================= 12. HELP & SUPPORT SCREEN =================
@Composable
private fun HelpSupportScreen(dbService: DatabaseService) {
    val context = LocalContext.current

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(bottom = 32.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(18.dp))
                    .background(Color(0xFF14132B))
                    .border(1.dp, Color(0xFF28264A), RoundedCornerShape(18.dp))
                    .padding(16.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(50.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF6366F1).copy(alpha = 0.2f))
                            .border(1.dp, Color(0xFF6366F1).copy(alpha = 0.4f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.Help, contentDescription = null, tint = Color(0xFF6366F1), modifier = Modifier.size(26.dp))
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text("Help & Support", color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                        Text("Get assistance, submit feedback and view FAQs.", color = Color(0xFFA09EC0), fontSize = 11.5.sp)
                    }
                }
            }
        }

        item {
            Text("Support Options", color = Color(0xFFA855F7), fontSize = 12.sp, fontWeight = FontWeight.Bold)
        }

        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color(0xFF14132B))
                    .border(1.dp, Color(0xFF28264A), RoundedCornerShape(16.dp))
            ) {
                SettingRowDetail("Help Center & FAQs", "Browse guides and articles") {
                    Toast.makeText(context, "Opening Help Center...", Toast.LENGTH_SHORT).show()
                }
                SettingDivider()
                SettingRowDetail("Submit Feedback", "Send your suggestions to VEDRA team") {
                    Toast.makeText(context, "Opening feedback form...", Toast.LENGTH_SHORT).show()
                }
                SettingDivider()
                SettingRowDetail("Contact Support", "Get 24/7 assistant support") {
                    Toast.makeText(context, "Contacting support...", Toast.LENGTH_SHORT).show()
                }
                SettingDivider()
                SettingRowDetail("About VEDRA AI", "Version 1.0.0 • Terms & Privacy") {
                    Toast.makeText(context, "VEDRA AI v1.0.0", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
}

// ================= REUSABLE SETTING COMPONENT ROWS =================
@Composable
private fun SettingDivider() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(1.dp)
            .background(Color(0xFF28264A))
    )
}

@Composable
private fun SettingRowValue(
    title: String,
    value: String,
    isSuccess: Boolean = false,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(horizontal = 14.dp, vertical = 13.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(title, color = Color.White, fontSize = 13.5.sp, fontWeight = FontWeight.Medium)
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = value,
                    color = if (isSuccess) Color(0xFF10B981) else Color(0xFFA09EC0),
                    fontSize = 12.5.sp,
                    fontWeight = FontWeight.Medium
                )
                Spacer(modifier = Modifier.width(6.dp))
                Icon(Icons.Default.ChevronRight, contentDescription = null, tint = Color(0xFF6B6893), modifier = Modifier.size(16.dp))
            }
        }
    }
}

@Composable
private fun SettingRowToggle(
    title: String,
    subtitle: String?,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 14.dp, vertical = 10.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(title, color = Color.White, fontSize = 13.5.sp, fontWeight = FontWeight.Medium)
                if (!subtitle.isNullOrBlank()) {
                    Text(subtitle, color = Color(0xFF6B6893), fontSize = 11.sp)
                }
            }
            Switch(
                checked = checked,
                onCheckedChange = onCheckedChange,
                colors = SwitchDefaults.colors(
                    checkedThumbColor = Color.White,
                    checkedTrackColor = Color(0xFF8B5CF6),
                    uncheckedThumbColor = Color(0xFFA09EC0),
                    uncheckedTrackColor = Color(0xFF28264A)
                )
            )
        }
    }
}

@Composable
private fun SettingRowDetail(
    title: String,
    subtitle: String,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(horizontal = 14.dp, vertical = 12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(title, color = Color.White, fontSize = 13.5.sp, fontWeight = FontWeight.Medium)
                Text(subtitle, color = Color(0xFF6B6893), fontSize = 11.sp)
            }
            Icon(Icons.Default.ChevronRight, contentDescription = null, tint = Color(0xFF6B6893), modifier = Modifier.size(16.dp))
        }
    }
}

@Composable
private fun SettingRowSimple(
    title: String,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(horizontal = 14.dp, vertical = 13.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(title, color = Color.White, fontSize = 13.5.sp, fontWeight = FontWeight.Medium)
            Icon(Icons.Default.ChevronRight, contentDescription = null, tint = Color(0xFF6B6893), modifier = Modifier.size(16.dp))
        }
    }
}

@Composable
private fun SettingRowAction(
    title: String,
    subtitle: String,
    isDanger: Boolean,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(horizontal = 14.dp, vertical = 12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(title, color = if (isDanger) Color(0xFFEF4444) else Color.White, fontSize = 13.5.sp, fontWeight = FontWeight.Bold)
                Text(subtitle, color = Color(0xFF6B6893), fontSize = 11.sp)
            }
            Icon(Icons.Default.ChevronRight, contentDescription = null, tint = if (isDanger) Color(0xFFEF4444) else Color(0xFF6B6893), modifier = Modifier.size(16.dp))
        }
    }
}

@Composable
private fun SettingRowProgress(
    title: String,
    subtitle: String,
    progress: Float,
    percentText: String
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 14.dp, vertical = 12.dp)
    ) {
        Column {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(title, color = Color.White, fontSize = 13.5.sp, fontWeight = FontWeight.Medium)
                    Text(subtitle, color = Color(0xFF6B6893), fontSize = 11.sp)
                }
                Text(percentText, color = Color(0xFF10B981), fontSize = 12.sp, fontWeight = FontWeight.Bold)
            }
            Spacer(modifier = Modifier.height(6.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .clip(RoundedCornerShape(3.dp))
                    .background(Color(0xFF28264A))
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxHeight()
                        .fillMaxWidth(progress)
                        .clip(RoundedCornerShape(3.dp))
                        .background(Color(0xFF10B981))
                )
            }
        }
    }
}

@Composable
private fun SettingSliderRow(
    title: String,
    valueText: String,
    value: Float,
    rangeStart: Float,
    rangeEnd: Float,
    onValueChange: (Float) -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 14.dp, vertical = 8.dp)
    ) {
        Column {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(title, color = Color.White, fontSize = 13.5.sp, fontWeight = FontWeight.Medium)
                Text(valueText, color = Color(0xFFA09EC0), fontSize = 12.sp)
            }
            Slider(
                value = value,
                onValueChange = onValueChange,
                valueRange = rangeStart..rangeEnd,
                colors = SliderDefaults.colors(
                    thumbColor = Color(0xFF8B5CF6),
                    activeTrackColor = Color(0xFF8B5CF6),
                    inactiveTrackColor = Color(0xFF28264A)
                )
            )
        }
    }
}

@Composable
private fun BackupHistoryRow(
    timestamp: String,
    size: String
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 14.dp, vertical = 12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(timestamp, color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Medium)
                Text(size, color = Color(0xFF6B6893), fontSize = 11.sp)
            }
            Icon(Icons.Default.ChevronRight, contentDescription = null, tint = Color(0xFF6B6893), modifier = Modifier.size(16.dp))
        }
    }
}
