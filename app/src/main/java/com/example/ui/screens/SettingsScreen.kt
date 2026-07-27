package com.example.ui.screens

import android.appwidget.AppWidgetManager
import android.content.ComponentName
import com.example.widget.VedraAppWidgetProvider
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.Hearing
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.Repeat
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.SmartToy
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.filled.Widgets
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.services.CustomPlugin
import com.example.services.CustomRoutine
import com.example.services.DatabaseService
import com.example.services.GoogleDriveService
import com.example.services.PermissionService
import com.example.services.PermissionStatus
import com.example.services.TranslationService
import com.example.services.VoiceService
import com.example.ui.components.CustomButton
import com.example.ui.components.CustomInput
import com.example.ui.components.CustomModal
import kotlinx.coroutines.launch
import org.json.JSONArray

data class SettingItemSpec(
    val id: String,
    val title: String,
    val subtitle: String,
    val icon: ImageVector,
    val iconTint: Color,
    val iconBg: Color
)

data class SettingCategorySpec(
    val categoryTitle: String,
    val items: List<SettingItemSpec>
)

@Composable
fun SettingsScreen(
    dbService: DatabaseService,
    voiceService: VoiceService? = null,
    onOpenDrawer: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    var selectedDetailScreen by remember { mutableStateOf<String?>(null) }

    if (selectedDetailScreen != null) {
        // FULL SCREEN DETAIL INTERFACE
        SettingDetailView(
            screenId = selectedDetailScreen!!,
            onBack = { selectedDetailScreen = null },
            dbService = dbService,
            voiceService = voiceService,
            modifier = modifier
        )
    } else {
        // MAIN SETTINGS LIST SCREEN (Matching Image 1 EXACTLY)
        MainSettingsListView(
            dbService = dbService,
            onSelectSetting = { id -> selectedDetailScreen = id },
            modifier = modifier
        )
    }
}

@Composable
fun MainSettingsListView(
    dbService: DatabaseService,
    onSelectSetting: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val categories = remember {
        listOf(
            SettingCategorySpec(
                categoryTitle = "GENERAL",
                items = listOf(
                    SettingItemSpec("general_pref", "General Preferences", "Language, theme, units, etc.", Icons.Default.Palette, Color(0xFF9D6EFF), Color(0xFF2C1D4D)),
                    SettingItemSpec("voice_speech", "Voice & Speech", "Voice model, wake word, speed", Icons.Default.Mic, Color(0xFFEC4899), Color(0xFF3D1B2D)),
                    SettingItemSpec("appearance", "Appearance", "Theme, colors, animations", Icons.Default.Palette, Color(0xFF3B82F6), Color(0xFF1D244D)),
                    SettingItemSpec("home_screen", "Home Screen", "Customize home, suggestions", Icons.Default.Home, Color(0xFF14B8A6), Color(0xFF1B3B3E)),
                    SettingItemSpec("notifications", "Notifications", "Manage notifications & alerts", Icons.Default.Notifications, Color(0xFFF59E0B), Color(0xFF3D331B)),
                    SettingItemSpec("sound_vibration", "Sound & Vibration", "Assistant sounds, vibration", Icons.Default.VolumeUp, Color(0xFF10B981), Color(0xFF1B3D2B))
                )
            ),
            SettingCategorySpec(
                categoryTitle = "AI & MEMORY",
                items = listOf(
                    SettingItemSpec("ai_settings", "AI Settings", "Model, response style, creativity", Icons.Default.Psychology, Color(0xFFA855F7), Color(0xFF2E1A47)),
                    SettingItemSpec("memory_settings", "Memory Settings", "Manage what Vedra remembers", Icons.Default.SmartToy, Color(0xFF10B981), Color(0xFF163A29)),
                    SettingItemSpec("personalization", "Personalization", "Your preferences, behavior, interests", Icons.Default.Person, Color(0xFFEC4899), Color(0xFF3E1B2D)),
                    SettingItemSpec("context_recall", "Context & Recall", "Context window, recall strength", Icons.Default.Hearing, Color(0xFF3B82F6), Color(0xFF1B2A4E))
                )
            ),
            SettingCategorySpec(
                categoryTitle = "AUTOMATION & ACTIONS",
                items = listOf(
                    SettingItemSpec("automations", "Automations", "Routines, triggers, smart actions", Icons.Default.SmartToy, Color(0xFFF97316), Color(0xFF3E2E1B)),
                    SettingItemSpec("custom_commands", "Custom Commands", "Manage custom voice commands", Icons.Default.Code, Color(0xFF14B8A6), Color(0xFF1B3D3B)),
                    SettingItemSpec("quick_actions", "Quick Actions", "Edit and reorder quick actions", Icons.Default.FlashOn, Color(0xFFA855F7), Color(0xFF311B4E))
                )
            ),
            SettingCategorySpec(
                categoryTitle = "PRIVACY & SECURITY",
                items = listOf(
                    SettingItemSpec("privacy", "Privacy", "Data, permissions, privacy settings", Icons.Default.Security, Color(0xFF10B981), Color(0xFF1B3D23)),
                    SettingItemSpec("security", "Security", "App lock, biometrics, backups", Icons.Default.Lock, Color(0xFF3B82F6), Color(0xFF1B2D4E)),
                    SettingItemSpec("permissions", "Permissions", "Manage app permissions", Icons.Default.Key, Color(0xFFF59E0B), Color(0xFF3E341B))
                )
            ),
            SettingCategorySpec(
                categoryTitle = "ADVANCED",
                items = listOf(
                    SettingItemSpec("offline_ai", "Offline AI", "Manage offline models & data", Icons.Default.SmartToy, Color(0xFFA855F7), Color(0xFF2D1B4E)),
                    SettingItemSpec("developer_options", "Developer Options", "Advanced settings for developers", Icons.Default.Code, Color(0xFF3B82F6), Color(0xFF1B2D4E)),
                    SettingItemSpec("about_vedra", "About Vedra", "Version, terms, help & feedback", Icons.Default.Info, Color(0xFF64748B), Color(0xFF1E283A))
                )
            )
        )
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFF090810))
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(top = 12.dp, bottom = 32.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // TOP HEADER BAR: VEDRA Logo, Title, Search & Avatar
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "VEDRA",
                        color = Color(0xFFC4B5FD),
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 18.sp,
                        letterSpacing = 1.sp
                    )
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(6.dp)
                                .clip(CircleShape)
                                .background(Color(0xFF10B981))
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Online",
                            color = Color(0xFF10B981),
                            fontSize = 10.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }

                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "SETTINGS",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        letterSpacing = 1.2.sp
                    )
                    Text(
                        text = "Customize Vedra your way",
                        color = Color(0xFF9CA3AF),
                        fontSize = 11.sp
                    )
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    IconButton(
                        onClick = { onSelectSetting("general_pref") },
                        modifier = Modifier.size(34.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = "Search Settings",
                            tint = Color.White,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    Box(
                        modifier = Modifier
                            .size(34.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF2E1A47))
                            .border(1.5.dp, Color(0xFF9D6EFF), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = "Profile",
                            tint = Color.White,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }
        }

        // VEDRA PRO CARD (Matching Image 1)
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color(0xFF110F1C))
                    .border(1.dp, Color(0xFF281B43), RoundedCornerShape(16.dp))
                    .padding(14.dp)
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            // Glowing Orb graphic preview
                            Box(
                                modifier = Modifier
                                    .size(54.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFF1F1235))
                                    .border(1.5.dp, Color(0xFF8B5CF6), CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Mic,
                                    contentDescription = "Vedra Pro",
                                    tint = Color(0xFFC4B5FD),
                                    modifier = Modifier.size(26.dp)
                                )
                            }

                            Spacer(modifier = Modifier.width(12.dp))

                            Column {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = "Vedra Pro",
                                        color = Color.White,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 16.sp
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(10.dp))
                                            .background(Color(0xFF3B1F69))
                                            .padding(horizontal = 8.dp, vertical = 3.dp)
                                    ) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Icon(
                                                imageVector = Icons.Default.Star,
                                                contentDescription = null,
                                                tint = Color(0xFFA78BFA),
                                                modifier = Modifier.size(10.dp)
                                            )
                                            Spacer(modifier = Modifier.width(3.dp))
                                            Text(
                                                text = "Active",
                                                color = Color(0xFFC4B5FD),
                                                fontSize = 10.sp,
                                                fontWeight = FontWeight.Bold
                                            )
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "You have access to all premium features.",
                                    color = Color(0xFF9CA3AF),
                                    fontSize = 11.5.sp
                                )
                            }
                        }
                    }

                    // Manage Subscription Row
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .background(Color(0xFF181528))
                            .clickable { onSelectSetting("manage_subscription") }
                            .padding(horizontal = 12.dp, vertical = 10.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Manage Subscription",
                                color = Color.White,
                                fontSize = 12.5.sp,
                                fontWeight = FontWeight.Medium
                            )
                            Icon(
                                imageVector = Icons.Default.ChevronRight,
                                contentDescription = null,
                                tint = Color(0xFF9CA3AF),
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }
            }
        }

        // CATEGORY GROUPS (GENERAL, AI & MEMORY, AUTOMATION & ACTIONS, PRIVACY & SECURITY, ADVANCED)
        items(categories) { category ->
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = category.categoryTitle,
                    color = Color(0xFFA78BFA),
                    fontWeight = FontWeight.Bold,
                    fontSize = 11.sp,
                    letterSpacing = 1.sp,
                    modifier = Modifier.padding(start = 4.dp, top = 4.dp)
                )

                // Grouped Card Container
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .background(Color(0xFF12101D))
                        .border(1.dp, Color(0xFF1E1B2E), RoundedCornerShape(14.dp))
                ) {
                    Column {
                        category.items.forEachIndexed { index, item ->
                            SettingRowItem(
                                item = item,
                                onClick = { onSelectSetting(item.title) }
                            )
                            if (index < category.items.size - 1) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(1.dp)
                                        .background(Color(0xFF1A1828))
                                )
                            }
                        }
                    }
                }
            }
        }

        // RESET ALL SETTINGS CARD (Danger Action)
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(Color(0xFF210D12))
                    .border(1.dp, Color(0xFF3B131A), RoundedCornerShape(14.dp))
                    .clickable { onSelectSetting("Reset All Settings") }
                    .padding(horizontal = 14.dp, vertical = 12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(Color(0xFF4E1B1B)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Delete,
                                contentDescription = null,
                                tint = Color(0xFFEF4444),
                                modifier = Modifier.size(18.dp)
                            )
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        Column {
                            Text(
                                text = "Reset All Settings",
                                color = Color(0xFFEF4444),
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.5.sp
                            )
                            Text(
                                text = "This will reset all settings to default",
                                color = Color(0xFF9CA3AF),
                                fontSize = 11.sp
                            )
                        }
                    }

                    Icon(
                        imageVector = Icons.Default.ChevronRight,
                        contentDescription = null,
                        tint = Color(0xFFEF4444),
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun SettingRowItem(
    item: SettingItemSpec,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(horizontal = 12.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.weight(1f)
        ) {
            Box(
                modifier = Modifier
                    .size(34.dp)
                    .clip(CircleShape)
                    .background(item.iconBg),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = item.icon,
                    contentDescription = null,
                    tint = item.iconTint,
                    modifier = Modifier.size(18.dp)
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column {
                Text(
                    text = item.title,
                    color = Color.White,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 13.sp
                )
                Text(
                    text = item.subtitle,
                    color = Color(0xFF9CA3AF),
                    fontSize = 11.sp
                )
            }
        }

        Icon(
            imageVector = Icons.Default.ChevronRight,
            contentDescription = null,
            tint = Color(0xFF6B7280),
            modifier = Modifier.size(16.dp)
        )
    }
}

@Composable
fun SettingDetailView(
    screenId: String,
    onBack: () -> Unit,
    dbService: DatabaseService,
    voiceService: VoiceService?,
    modifier: Modifier = Modifier
) {
    val screenTitle = when (screenId) {
        "voice_speech", "Voice & Speech" -> "VOICE & SPEECH"
        "general_pref", "General Preferences" -> "GENERAL PREFERENCES"
        "ai_settings", "AI Settings" -> "AI SETTINGS"
        "automations", "Automations" -> "AUTOMATIONS & ROUTINES"
        "privacy", "Privacy" -> "PRIVACY & SECURITY"
        "offline_ai", "Offline AI" -> "OFFLINE AI & DATA"
        "manage_subscription" -> "VEDRA PRO SUBSCRIPTION"
        "Reset All Settings" -> "RESET ALL SETTINGS"
        else -> screenId.uppercase()
    }

    val screenSubtitle = when (screenId) {
        "voice_speech", "Voice & Speech" -> "Customize Vedra's voice and speech settings"
        "general_pref", "General Preferences" -> "Configure default language, theme, and units"
        "ai_settings", "AI Settings" -> "Tune intelligence model, response style & API keys"
        "automations", "Automations" -> "Set up task chain routines & voice macros"
        "privacy", "Privacy" -> "Manage permissions, backups, and app security"
        "offline_ai", "Offline AI" -> "Inspect local database records & offline cache"
        "manage_subscription" -> "Your active Vedra Pro membership"
        else -> "Manage $screenId options"
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFF090810))
    ) {
        // TOP DETAIL HEADER BAR (Matching Image 2)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFF0B0A13))
                .padding(horizontal = 16.dp, vertical = 12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(
                        onClick = onBack,
                        modifier = Modifier.size(34.dp)
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = Color.White,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(4.dp))

                    Column {
                        Text(
                            text = "VEDRA",
                            color = Color(0xFFC4B5FD),
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 16.sp,
                            letterSpacing = 1.sp
                        )
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(5.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFF10B981))
                            )
                            Spacer(modifier = Modifier.width(3.dp))
                            Text(
                                text = "Online",
                                color = Color(0xFF10B981),
                                fontSize = 9.5.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }

                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = screenTitle,
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        letterSpacing = 1.sp
                    )
                    Text(
                        text = screenSubtitle,
                        color = Color(0xFF9CA3AF),
                        fontSize = 10.5.sp
                    )
                }

                Box(
                    modifier = Modifier
                        .size(34.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF2E1A47))
                        .border(1.5.dp, Color(0xFF9D6EFF), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = "Profile",
                        tint = Color.White,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(1.dp)
                .background(Color(0xFF1E1B2E))
        )

        // DETAIL SCREEN CONTENT
        when (screenId) {
            "voice_speech", "Voice & Speech" -> {
                VoiceAndSpeechDetailScreen(dbService = dbService, voiceService = voiceService)
            }
            "general_pref", "General Preferences", "Appearance", "Notifications", "Sound & Vibration" -> {
                GeneralPreferencesDetailScreen(dbService = dbService)
            }
            "home_screen", "Home Screen", "widget_settings" -> {
                WidgetSettingsDetailScreen(dbService = dbService)
            }
            "ai_settings", "AI Settings", "Personalization", "Context & Recall" -> {
                AISettingsDetailScreen(dbService = dbService)
            }
            "automations", "Automations", "Custom Commands", "Quick Actions" -> {
                AutomationsDetailScreen(dbService = dbService)
            }
            "privacy", "Privacy", "Security", "Permissions" -> {
                PrivacySecurityDetailScreen(dbService = dbService)
            }
            "offline_ai", "Offline AI", "Memory Settings", "Developer Options", "About Vedra" -> {
                OfflineAIDetailScreen(dbService = dbService)
            }
            "manage_subscription" -> {
                ManageSubscriptionDetailScreen()
            }
            "Reset All Settings" -> {
                ResetAllSettingsDetailScreen(dbService = dbService, onBack = onBack)
            }
            else -> {
                GeneralPreferencesDetailScreen(dbService = dbService)
            }
        }
    }
}

// ==========================================
// VOICE & SPEECH DETAIL SCREEN (IMAGE 2)
// ==========================================
@Composable
fun VoiceAndSpeechDetailScreen(
    dbService: DatabaseService,
    voiceService: VoiceService?
) {
    val context = LocalContext.current
    var activeVoice by remember { mutableStateOf("Vedra (Natural)") }
    var selectedLanguage by remember { mutableStateOf("English (India)") }

    var speechSpeed by remember { mutableFloatStateOf(dbService.getSetting("speed", "1.0").toFloatOrNull() ?: 1.0f) }
    var speechPitch by remember { mutableFloatStateOf(dbService.getSetting("pitch", "1.0").toFloatOrNull() ?: 1.0f) }
    var speechVolume by remember { mutableFloatStateOf(0.8f) }
    var pauseDuration by remember { mutableFloatStateOf(1.2f) }

    var wakeWordEnabled by remember { mutableStateOf(true) }
    var voiceResponsesEnabled by remember { mutableStateOf(true) }
    var readNotificationsEnabled by remember { mutableStateOf(true) }
    var announceCallsEnabled by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF090810))
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(top = 16.dp, bottom = 32.dp),
        verticalArrangement = Arrangement.spacedBy(18.dp)
    ) {
        // SECTION 1: VOICE PREVIEW (Matching Image 2 top card)
        item {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = "VOICE PREVIEW",
                    color = Color(0xFFA78BFA),
                    fontWeight = FontWeight.Bold,
                    fontSize = 11.sp,
                    letterSpacing = 1.sp
                )

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(Color(0xFF110F1D))
                        .border(1.dp, Color(0xFF221A38), RoundedCornerShape(16.dp))
                        .padding(14.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            // Glowing Orb
                            Box(
                                modifier = Modifier
                                    .size(68.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFF1C1330))
                                    .border(2.dp, Color(0xFF8B5CF6), CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Mic,
                                    contentDescription = "Voice Waveform",
                                    tint = Color(0xFFC4B5FD),
                                    modifier = Modifier.size(30.dp)
                                )
                            }

                            Spacer(modifier = Modifier.width(12.dp))

                            Column {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = activeVoice,
                                        color = Color.White,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 14.5.sp
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(Color(0xFF3B1F69))
                                            .padding(horizontal = 6.dp, vertical = 2.dp)
                                    ) {
                                        Text(
                                            text = "Active",
                                            color = Color(0xFFC4B5FD),
                                            fontSize = 9.5.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "Hi Rahul! I'm Vedra.",
                                    color = Color(0xFFD1D5DB),
                                    fontSize = 11.5.sp
                                )
                                Text(
                                    text = "How can I help you today?",
                                    color = Color(0xFFD1D5DB),
                                    fontSize = 11.5.sp
                                )

                                Spacer(modifier = Modifier.height(8.dp))

                                // Play Sample Button
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(20.dp))
                                        .background(Color(0xFF6D28D9))
                                        .clickable {
                                            voiceService?.speak("Hi Rahul! I'm Vedra. How can I help you today?")
                                        }
                                        .padding(horizontal = 12.dp, vertical = 6.dp)
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            imageVector = Icons.Default.PlayArrow,
                                            contentDescription = "Play",
                                            tint = Color.White,
                                            modifier = Modifier.size(14.dp)
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(
                                            text = "Play Sample",
                                            color = Color.White,
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                            }
                        }

                        Icon(
                            imageVector = Icons.Default.ChevronRight,
                            contentDescription = null,
                            tint = Color(0xFF6B7280),
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }
        }

        // SECTION 2: VOICE SELECTION (Horizontal Cards)
        item {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = "VOICE SELECTION",
                    color = Color(0xFFA78BFA),
                    fontWeight = FontWeight.Bold,
                    fontSize = 11.sp,
                    letterSpacing = 1.sp
                )

                val voiceOptions = listOf(
                    Triple("Vedra (Natural)", "Female", true),
                    Triple("Vedra (Warm)", "Female", false),
                    Triple("Vedra (Deep)", "Male", false),
                    Triple("Vedra (Soft)", "Female", false),
                    Triple("Vedra (Energetic)", "Male", false)
                )

                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(voiceOptions) { (name, gender, isDefault) ->
                        val isSelected = activeVoice == name
                        Box(
                            modifier = Modifier
                                .width(120.dp)
                                .height(110.dp)
                                .clip(RoundedCornerShape(14.dp))
                                .background(if (isSelected) Color(0xFF1F1235) else Color(0xFF12101D))
                                .border(
                                    width = if (isSelected) 1.5.dp else 1.dp,
                                    color = if (isSelected) Color(0xFF8B5CF6) else Color(0xFF1E1B2E),
                                    shape = RoundedCornerShape(14.dp)
                                )
                                .clickable { activeVoice = name }
                                .padding(10.dp)
                        ) {
                            Column(
                                modifier = Modifier.fillMaxSize(),
                                verticalArrangement = Arrangement.SpaceBetween,
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.End
                                ) {
                                    if (isSelected) {
                                        Box(
                                            modifier = Modifier
                                                .size(16.dp)
                                                .clip(CircleShape)
                                                .background(Color(0xFF8B5CF6)),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Check,
                                                contentDescription = null,
                                                tint = Color.White,
                                                modifier = Modifier.size(10.dp)
                                            )
                                        }
                                    }
                                }

                                Icon(
                                    imageVector = Icons.Default.Mic,
                                    contentDescription = null,
                                    tint = if (isSelected) Color(0xFFC4B5FD) else Color(0xFF6B7280),
                                    modifier = Modifier.size(22.dp)
                                )

                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text(
                                        text = name,
                                        color = Color.White,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        maxLines = 1
                                    )
                                    Text(
                                        text = if (isDefault) "Default" else gender,
                                        color = Color(0xFF9CA3AF),
                                        fontSize = 9.5.sp
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // SECTION 3: LANGUAGE
        item {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = "LANGUAGE",
                    color = Color(0xFFA78BFA),
                    fontWeight = FontWeight.Bold,
                    fontSize = 11.sp,
                    letterSpacing = 1.sp
                )

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .background(Color(0xFF12101D))
                        .border(1.dp, Color(0xFF1E1B2E), RoundedCornerShape(14.dp))
                        .padding(12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(34.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFF2C1D4D)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Language,
                                    contentDescription = null,
                                    tint = Color(0xFF9D6EFF),
                                    modifier = Modifier.size(18.dp)
                                )
                            }

                            Spacer(modifier = Modifier.width(12.dp))

                            Column {
                                Text(
                                    text = "Assistant Language",
                                    color = Color.White,
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 13.sp
                                )
                                Text(
                                    text = "Choose the language Vedra speaks with you",
                                    color = Color(0xFF9CA3AF),
                                    fontSize = 11.sp
                                )
                            }
                        }

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = selectedLanguage,
                                color = Color.White,
                                fontSize = 11.5.sp,
                                fontWeight = FontWeight.Medium
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Icon(
                                imageVector = Icons.Default.ChevronRight,
                                contentDescription = null,
                                tint = Color(0xFF6B7280),
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }
            }
        }

        // SECTION 4: SPEECH SETTINGS (Sliders matching Image 2)
        item {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = "SPEECH SETTINGS",
                    color = Color(0xFFA78BFA),
                    fontWeight = FontWeight.Bold,
                    fontSize = 11.sp,
                    letterSpacing = 1.sp
                )

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .background(Color(0xFF12101D))
                        .border(1.dp, Color(0xFF1E1B2E), RoundedCornerShape(14.dp))
                        .padding(14.dp)
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                        // Speech Speed Slider
                        Column {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.Speed,
                                        contentDescription = null,
                                        tint = Color(0xFF9D6EFF),
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "Speech Speed",
                                        color = Color.White,
                                        fontSize = 12.5.sp,
                                        fontWeight = FontWeight.Medium
                                    )
                                }
                                Text(
                                    text = "${"%.1f".format(speechSpeed)}x",
                                    color = Color.White,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            Slider(
                                value = speechSpeed,
                                onValueChange = {
                                    speechSpeed = it
                                    voiceService?.setPitchAndRate(speechPitch, speechSpeed)
                                    dbService.setSetting("speed", speechSpeed.toString())
                                },
                                valueRange = 0.5f..2.0f,
                                colors = SliderDefaults.colors(
                                    thumbColor = Color(0xFF8B5CF6),
                                    activeTrackColor = Color(0xFF8B5CF6),
                                    inactiveTrackColor = Color(0xFF261D3B)
                                )
                            )
                        }

                        // Pitch Slider
                        Column {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.Mic,
                                        contentDescription = null,
                                        tint = Color(0xFF9D6EFF),
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "Pitch",
                                        color = Color.White,
                                        fontSize = 12.5.sp,
                                        fontWeight = FontWeight.Medium
                                    )
                                }
                                Text(
                                    text = "${((speechPitch - 1.0f) * 100).toInt()}%",
                                    color = Color.White,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            Slider(
                                value = speechPitch,
                                onValueChange = {
                                    speechPitch = it
                                    voiceService?.setPitchAndRate(speechPitch, speechSpeed)
                                    dbService.setSetting("pitch", speechPitch.toString())
                                },
                                valueRange = 0.5f..1.5f,
                                colors = SliderDefaults.colors(
                                    thumbColor = Color(0xFF8B5CF6),
                                    activeTrackColor = Color(0xFF8B5CF6),
                                    inactiveTrackColor = Color(0xFF261D3B)
                                )
                            )
                        }

                        // Speech Volume Slider
                        Column {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.VolumeUp,
                                        contentDescription = null,
                                        tint = Color(0xFF9D6EFF),
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "Speech Volume",
                                        color = Color.White,
                                        fontSize = 12.5.sp,
                                        fontWeight = FontWeight.Medium
                                    )
                                }
                                Text(
                                    text = "${(speechVolume * 100).toInt()}%",
                                    color = Color.White,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            Slider(
                                value = speechVolume,
                                onValueChange = { speechVolume = it },
                                valueRange = 0.0f..1.0f,
                                colors = SliderDefaults.colors(
                                    thumbColor = Color(0xFF8B5CF6),
                                    activeTrackColor = Color(0xFF8B5CF6),
                                    inactiveTrackColor = Color(0xFF261D3B)
                                )
                            )
                        }

                        // Pause Between Sentences Slider
                        Column {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.Pause,
                                        contentDescription = null,
                                        tint = Color(0xFF9D6EFF),
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "Pause Between Sentences",
                                        color = Color.White,
                                        fontSize = 12.5.sp,
                                        fontWeight = FontWeight.Medium
                                    )
                                }
                                Text(
                                    text = "${"%.1f".format(pauseDuration)}s",
                                    color = Color.White,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            Slider(
                                value = pauseDuration,
                                onValueChange = { pauseDuration = it },
                                valueRange = 0.5f..3.0f,
                                colors = SliderDefaults.colors(
                                    thumbColor = Color(0xFF8B5CF6),
                                    activeTrackColor = Color(0xFF8B5CF6),
                                    inactiveTrackColor = Color(0xFF261D3B)
                                )
                            )
                        }
                    }
                }
            }
        }

        // SECTION 5: WAKE WORD
        item {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = "WAKE WORD",
                    color = Color(0xFFA78BFA),
                    fontWeight = FontWeight.Bold,
                    fontSize = 11.sp,
                    letterSpacing = 1.sp
                )

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .background(Color(0xFF12101D))
                        .border(1.dp, Color(0xFF1E1B2E), RoundedCornerShape(14.dp))
                        .padding(14.dp)
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
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
                                        .size(34.dp)
                                        .clip(CircleShape)
                                        .background(Color(0xFF2C1D4D)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Mic,
                                        contentDescription = null,
                                        tint = Color(0xFF9D6EFF),
                                        modifier = Modifier.size(18.dp)
                                    )
                                }

                                Spacer(modifier = Modifier.width(12.dp))

                                Column {
                                    Text(
                                        text = "Wake Word",
                                        color = Color.White,
                                        fontWeight = FontWeight.SemiBold,
                                        fontSize = 13.sp
                                    )
                                    Text(
                                        text = "Say this word to wake Vedra",
                                        color = Color(0xFF9CA3AF),
                                        fontSize = 11.sp
                                    )
                                }
                            }

                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "\"Ved\"",
                                    color = Color.White,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Switch(
                                    checked = wakeWordEnabled,
                                    onCheckedChange = { wakeWordEnabled = it },
                                    colors = SwitchDefaults.colors(
                                        checkedThumbColor = Color.White,
                                        checkedTrackColor = Color(0xFF8B5CF6)
                                    )
                                )
                            }
                        }

                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(1.dp)
                                .background(Color(0xFF1E1B2E))
                        )

                        // Sensitivity row
                        Column {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(
                                        text = "Sensitivity",
                                        color = Color.White,
                                        fontSize = 12.5.sp,
                                        fontWeight = FontWeight.Medium
                                    )
                                    Text(
                                        text = "Adjust how easily Vedra listens for wake word",
                                        color = Color(0xFF9CA3AF),
                                        fontSize = 11.sp
                                    )
                                }

                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = "High",
                                        color = Color.White,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Medium
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Icon(
                                        imageVector = Icons.Default.ChevronRight,
                                        contentDescription = null,
                                        tint = Color(0xFF6B7280),
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            // Segmented bar
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                repeat(4) {
                                    Box(
                                        modifier = Modifier
                                            .weight(1f)
                                            .height(6.dp)
                                            .clip(RoundedCornerShape(3.dp))
                                            .background(Color(0xFF8B5CF6))
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // SECTION 6: VOICE RESPONSE (Toggles)
        item {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = "VOICE RESPONSE",
                    color = Color(0xFFA78BFA),
                    fontWeight = FontWeight.Bold,
                    fontSize = 11.sp,
                    letterSpacing = 1.sp
                )

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .background(Color(0xFF12101D))
                        .border(1.dp, Color(0xFF1E1B2E), RoundedCornerShape(14.dp))
                ) {
                    Column {
                        // Voice Responses
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.weight(1f)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(34.dp)
                                        .clip(CircleShape)
                                        .background(Color(0xFF2C1D4D)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Mic,
                                        contentDescription = null,
                                        tint = Color(0xFF9D6EFF),
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Text(text = "Voice Responses", color = Color.White, fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                                    Text(text = "Vedra will speak responses", color = Color(0xFF9CA3AF), fontSize = 11.sp)
                                }
                            }
                            Switch(
                                checked = voiceResponsesEnabled,
                                onCheckedChange = { voiceResponsesEnabled = it },
                                colors = SwitchDefaults.colors(checkedThumbColor = Color.White, checkedTrackColor = Color(0xFF8B5CF6))
                            )
                        }

                        Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(Color(0xFF1E1B2E)))

                        // Read Notifications Aloud
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.weight(1f)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(34.dp)
                                        .clip(CircleShape)
                                        .background(Color(0xFF2C1D4D)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Hearing,
                                        contentDescription = null,
                                        tint = Color(0xFF9D6EFF),
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Text(text = "Read Notifications Aloud", color = Color.White, fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                                    Text(text = "Vedra will read important notifications", color = Color(0xFF9CA3AF), fontSize = 11.sp)
                                }
                            }
                            Switch(
                                checked = readNotificationsEnabled,
                                onCheckedChange = { readNotificationsEnabled = it },
                                colors = SwitchDefaults.colors(checkedThumbColor = Color.White, checkedTrackColor = Color(0xFF8B5CF6))
                            )
                        }

                        Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(Color(0xFF1E1B2E)))

                        // Announce Incoming Calls
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.weight(1f)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(34.dp)
                                        .clip(CircleShape)
                                        .background(Color(0xFF2C1D4D)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Phone,
                                        contentDescription = null,
                                        tint = Color(0xFF9D6EFF),
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Text(text = "Announce Incoming Calls", color = Color.White, fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                                    Text(text = "Vedra will announce incoming calls", color = Color(0xFF9CA3AF), fontSize = 11.sp)
                                }
                            }
                            Switch(
                                checked = announceCallsEnabled,
                                onCheckedChange = { announceCallsEnabled = it },
                                colors = SwitchDefaults.colors(checkedThumbColor = Color.White, checkedTrackColor = Color(0xFF8B5CF6))
                            )
                        }
                    }
                }
            }
        }
    }
}

// ==========================================
// GENERAL PREFERENCES DETAIL SCREEN
// ==========================================
@Composable
fun GeneralPreferencesDetailScreen(dbService: DatabaseService) {
    var selectedLangCode by remember { mutableStateOf(TranslationService.getTargetLanguage().code) }
    var themeMode by remember { mutableStateOf("Dark Neon Purple") }

    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp),
        contentPadding = PaddingValues(top = 16.dp, bottom = 32.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Text(text = "APP LANGUAGE", color = Color(0xFFA78BFA), fontWeight = FontWeight.Bold, fontSize = 11.sp, letterSpacing = 1.sp)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(Color(0xFF12101D))
                    .border(1.dp, Color(0xFF1E1B2E), RoundedCornerShape(14.dp))
                    .padding(12.dp)
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    TranslationService.SUPPORTED_LANGUAGES.forEach { lang ->
                        val isSel = selectedLangCode == lang.code
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    selectedLangCode = lang.code
                                    TranslationService.setTargetLanguage(lang.code)
                                    dbService.setSetting("language", lang.code)
                                }
                                .padding(vertical = 6.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(text = lang.displayName, color = if (isSel) Color(0xFFC4B5FD) else Color.White, fontSize = 13.sp)
                            if (isSel) {
                                Icon(imageVector = Icons.Default.Check, contentDescription = null, tint = Color(0xFF8B5CF6), modifier = Modifier.size(16.dp))
                            }
                        }
                    }
                }
            }
        }

        item {
            Text(text = "THEME & APPEARANCE", color = Color(0xFFA78BFA), fontWeight = FontWeight.Bold, fontSize = 11.sp, letterSpacing = 1.sp)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(Color(0xFF12101D))
                    .border(1.dp, Color(0xFF1E1B2E), RoundedCornerShape(14.dp))
                    .padding(12.dp)
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf("Dark Neon Purple", "Deep AMOLED Black", "Cyan Glow").forEach { theme ->
                        val isSel = themeMode == theme
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { themeMode = theme }
                                .padding(vertical = 6.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(text = theme, color = if (isSel) Color(0xFFC4B5FD) else Color.White, fontSize = 13.sp)
                            if (isSel) {
                                Icon(imageVector = Icons.Default.Check, contentDescription = null, tint = Color(0xFF8B5CF6), modifier = Modifier.size(16.dp))
                            }
                        }
                    }
                }
            }
        }
    }
}

// ==========================================
// AI SETTINGS DETAIL SCREEN
// ==========================================
@Composable
fun AISettingsDetailScreen(dbService: DatabaseService) {
    var selectedEngine by remember { mutableStateOf(dbService.getSetting("engine", "Hybrid Cloud AI")) }
    var selectedTone by remember { mutableStateOf(dbService.getSetting("tone", "Short & Direct")) }
    var apiKeyInput by remember { mutableStateOf(dbService.getSetting("api_key", "")) }

    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp),
        contentPadding = PaddingValues(top = 16.dp, bottom = 32.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Text(text = "AI MODEL ENGINE", color = Color(0xFFA78BFA), fontWeight = FontWeight.Bold, fontSize = 11.sp, letterSpacing = 1.sp)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(Color(0xFF12101D))
                    .border(1.dp, Color(0xFF1E1B2E), RoundedCornerShape(14.dp))
                    .padding(14.dp)
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    listOf("Hybrid Cloud AI", "Strict Offline ONNX", "Custom API Endpoint").forEach { engine ->
                        val isSel = selectedEngine == engine
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    selectedEngine = engine
                                    dbService.setSetting("engine", engine)
                                }
                                .padding(vertical = 6.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(text = engine, color = if (isSel) Color(0xFFC4B5FD) else Color.White, fontSize = 13.sp)
                            if (isSel) {
                                Icon(imageVector = Icons.Default.Check, contentDescription = null, tint = Color(0xFF8B5CF6), modifier = Modifier.size(16.dp))
                            }
                        }
                    }

                    if (selectedEngine == "Custom API Endpoint") {
                        CustomInput(
                            value = apiKeyInput,
                            onValueChange = {
                                apiKeyInput = it
                                dbService.setSetting("api_key", it)
                            },
                            placeholder = "Enter custom API key (e.g. sk-...)"
                        )
                    }
                }
            }
        }

        item {
            Text(text = "RESPONSE STYLE & TONE", color = Color(0xFFA78BFA), fontWeight = FontWeight.Bold, fontSize = 11.sp, letterSpacing = 1.sp)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(Color(0xFF12101D))
                    .border(1.dp, Color(0xFF1E1B2E), RoundedCornerShape(14.dp))
                    .padding(14.dp)
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    listOf("Short & Direct", "Detailed & Conversational", "Empathetic Coach").forEach { tone ->
                        val isSel = selectedTone == tone
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    selectedTone = tone
                                    dbService.setSetting("tone", tone)
                                }
                                .padding(vertical = 6.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(text = tone, color = if (isSel) Color(0xFFC4B5FD) else Color.White, fontSize = 13.sp)
                            if (isSel) {
                                Icon(imageVector = Icons.Default.Check, contentDescription = null, tint = Color(0xFF8B5CF6), modifier = Modifier.size(16.dp))
                            }
                        }
                    }
                }
            }
        }
    }
}

// ==========================================
// AUTOMATIONS & ROUTINES DETAIL SCREEN
// ==========================================
@Composable
fun AutomationsDetailScreen(dbService: DatabaseService) {
    val routines = remember { mutableStateListOf<CustomRoutine>() }
    var isAddModalOpen by remember { mutableStateOf(false) }
    var triggerInput by remember { mutableStateOf("") }
    var actionsInput by remember { mutableStateOf("") }

    fun refresh() {
        routines.clear()
        routines.addAll(dbService.getAllRoutines())
    }

    LaunchedEffect(Unit) { refresh() }

    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp),
        contentPadding = PaddingValues(top = 16.dp, bottom = 32.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(text = "CUSTOM TASK CHAINS", color = Color(0xFFA78BFA), fontWeight = FontWeight.Bold, fontSize = 11.sp, letterSpacing = 1.sp)
                CustomButton(
                    text = "Add Routine",
                    icon = Icons.Default.Add,
                    onClick = {
                        triggerInput = ""
                        actionsInput = ""
                        isAddModalOpen = true
                    },
                    modifier = Modifier.height(30.dp)
                )
            }
        }

        if (routines.isEmpty()) {
            item {
                Text(text = "No routines configured. Tap 'Add Routine' to create voice triggers.", color = Color(0xFF9CA3AF), fontSize = 12.sp)
            }
        } else {
            items(routines, key = { it.id }) { routine ->
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .background(Color(0xFF12101D))
                        .border(1.dp, Color(0xFF1E1B2E), RoundedCornerShape(14.dp))
                        .padding(12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(text = "Trigger: \"${routine.triggerPhrase}\"", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            Text(text = "Actions: ${routine.actionChainJson}", color = Color(0xFF9CA3AF), fontSize = 11.sp)
                        }
                        IconButton(
                            onClick = {
                                dbService.deleteRoutine(routine.id)
                                refresh()
                            },
                            modifier = Modifier.size(28.dp)
                        ) {
                            Icon(imageVector = Icons.Default.Delete, contentDescription = "Delete", tint = Color(0xFFEF4444), modifier = Modifier.size(16.dp))
                        }
                    }
                }
            }
        }
    }

    CustomModal(
        visible = isAddModalOpen,
        title = "Create Voice Routine",
        onDismissRequest = { isAddModalOpen = false }
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            CustomInput(value = triggerInput, onValueChange = { triggerInput = it }, placeholder = "Trigger Phrase (e.g. good morning)")
            CustomInput(value = actionsInput, onValueChange = { actionsInput = it }, placeholder = "Actions (e.g. Read Weather, Read Battery)")
            CustomButton(
                text = "Save Routine",
                onClick = {
                    if (triggerInput.isNotBlank() && actionsInput.isNotBlank()) {
                        val actionsList = actionsInput.split(",").map { it.trim() }
                        val jsonArr = JSONArray()
                        actionsList.forEach { jsonArr.put(it) }
                        dbService.addOrUpdateRoutine(triggerInput, jsonArr.toString())
                        refresh()
                        isAddModalOpen = false
                    }
                },
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

// ==========================================
// PRIVACY & SECURITY DETAIL SCREEN
// ==========================================
@Composable
fun PrivacySecurityDetailScreen(dbService: DatabaseService) {
    val context = LocalContext.current
    var permissionsList by remember { mutableStateOf<List<PermissionStatus>>(emptyList()) }
    val permLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) {
        permissionsList = PermissionService.checkAllPermissions(context)
    }

    LaunchedEffect(Unit) {
        permissionsList = PermissionService.checkAllPermissions(context)
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp),
        contentPadding = PaddingValues(top = 16.dp, bottom = 32.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Text(text = "SYSTEM PERMISSIONS", color = Color(0xFFA78BFA), fontWeight = FontWeight.Bold, fontSize = 11.sp, letterSpacing = 1.sp)
        }

        items(permissionsList) { perm ->
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(Color(0xFF12101D))
                    .border(1.dp, Color(0xFF1E1B2E), RoundedCornerShape(14.dp))
                    .padding(12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = if (perm.isGranted) Icons.Default.CheckCircle else Icons.Default.Warning,
                            contentDescription = null,
                            tint = if (perm.isGranted) Color(0xFF10B981) else Color(0xFFEF4444),
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            val shortName = perm.permissionName.substringAfterLast(".")
                            Text(text = shortName, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            Text(text = perm.requiredForFeature, color = Color(0xFF9CA3AF), fontSize = 11.sp)
                        }
                    }

                    CustomButton(
                        text = if (perm.isGranted) "Granted" else "Grant",
                        onClick = { permLauncher.launch(arrayOf(perm.permissionName)) },
                        isSecondary = perm.isGranted,
                        modifier = Modifier.height(28.dp)
                    )
                }
            }
        }
    }
}

// ==========================================
// OFFLINE AI & STORAGE DETAIL SCREEN
// ==========================================
@Composable
fun OfflineAIDetailScreen(dbService: DatabaseService) {
    val stats = remember { dbService.getOfflineStorageStats() }
    val totalSaved = stats.values.sum()

    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp),
        contentPadding = PaddingValues(top = 16.dp, bottom = 32.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Text(text = "SQLITE DATABASE RECORDS", color = Color(0xFFA78BFA), fontWeight = FontWeight.Bold, fontSize = 11.sp, letterSpacing = 1.sp)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(Color(0xFF12101D))
                    .border(1.dp, Color(0xFF1E1B2E), RoundedCornerShape(14.dp))
                    .padding(14.dp)
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(text = "Total Records Stored Locally: $totalSaved", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.5.sp)
                    Spacer(modifier = Modifier.height(4.dp))
                    stats.forEach { (table, count) ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(text = table, color = Color(0xFF9CA3AF), fontSize = 12.sp)
                            Text(text = "$count entries", color = Color(0xFFC4B5FD), fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        }
                    }
                }
            }
        }
    }
}

// ==========================================
// VEDRA PRO SUBSCRIPTION DETAIL SCREEN
// ==========================================
@Composable
fun ManageSubscriptionDetailScreen() {
    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp),
        contentPadding = PaddingValues(top = 16.dp, bottom = 32.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color(0xFF19102B))
                    .border(1.5.dp, Color(0xFF8B5CF6), RoundedCornerShape(16.dp))
                    .padding(16.dp)
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(imageVector = Icons.Default.Star, contentDescription = null, tint = Color(0xFFA78BFA), modifier = Modifier.size(22.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(text = "Vedra Pro Active Membership", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    }

                    Text(
                        text = "• Offline ONNX AI Intelligence\n• Custom Voice & Speech Customizer\n• Google Drive Memory Sync & Backups\n• JEE Study Hub Planner & Flashcards",
                        color = Color(0xFFD1D5DB),
                        fontSize = 12.5.sp,
                        lineHeight = 18.sp
                    )

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(20.dp))
                            .background(Color(0xFF8B5CF6))
                            .padding(vertical = 10.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(text = "Active • Renews Annually", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 12.5.sp)
                    }
                }
            }
        }
    }
}

// ==========================================
// RESET ALL SETTINGS DETAIL SCREEN
// ==========================================
@Composable
fun ResetAllSettingsDetailScreen(dbService: DatabaseService, onBack: () -> Unit) {
    var isResetDone by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(64.dp)
                .clip(CircleShape)
                .background(Color(0xFF4E1B1B)),
            contentAlignment = Alignment.Center
        ) {
            Icon(imageVector = Icons.Default.Delete, contentDescription = null, tint = Color(0xFFEF4444), modifier = Modifier.size(32.dp))
        }

        Text(text = "Reset All Settings", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 18.sp)

        Text(
            text = "Are you sure you want to reset all Vedra preferences, voice models, AI parameters, and shortcuts to their default state?",
            color = Color(0xFF9CA3AF),
            fontSize = 13.sp,
            modifier = Modifier.padding(horizontal = 16.dp)
        )

        if (isResetDone) {
            Text(text = "Settings successfully reset! ✅", color = Color(0xFF10B981), fontWeight = FontWeight.Bold, fontSize = 14.sp)
        } else {
            CustomButton(
                text = "Confirm Reset All Settings",
                onClick = {
                    dbService.setSetting("speed", "1.0")
                    dbService.setSetting("pitch", "1.0")
                    dbService.setSetting("engine", "Hybrid Cloud AI")
                    dbService.setSetting("tone", "Short & Direct")
                    isResetDone = true
                },
                modifier = Modifier.fillMaxWidth().height(44.dp)
            )
        }
    }
}

// ==========================================
// HOME SCREEN & WIDGET SETTINGS DETAIL SCREEN
// ==========================================
@Composable
fun WidgetSettingsDetailScreen(dbService: DatabaseService) {
    val context = LocalContext.current
    var widgetTheme by remember { mutableStateOf(dbService.getSetting("widget_theme", "Vedra Dark Purple")) }
    var widgetLayout by remember { mutableStateOf(dbService.getSetting("widget_layout", "Compact Assistant Pill")) }
    var backgroundOpacity by remember { mutableFloatStateOf(dbService.getSetting("widget_opacity", "0.9").toFloatOrNull() ?: 0.9f) }
    var showLiveStatus by remember { mutableStateOf(dbService.getSetting("widget_show_status", "true") == "true") }
    var showQuickPrompts by remember { mutableStateOf(dbService.getSetting("widget_show_prompts", "true") == "true") }
    var isPinnedMessageShown by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF090810))
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(top = 16.dp, bottom = 32.dp),
        verticalArrangement = Arrangement.spacedBy(18.dp)
    ) {
        // SECTION 1: LIVE WIDGET PREVIEW CARD
        item {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = "LIVE WIDGET PREVIEW (PHONE HOME SCREEN)",
                    color = Color(0xFFA78BFA),
                    fontWeight = FontWeight.Bold,
                    fontSize = 11.sp,
                    letterSpacing = 1.sp
                )

                val bgColor = when (widgetTheme) {
                    "Cyber Neon" -> Color(0xFF0F172A).copy(alpha = backgroundOpacity)
                    "Pitch Black OLED" -> Color(0xFF000000).copy(alpha = backgroundOpacity)
                    "Frosted Glass" -> Color(0xFF1E1B2E).copy(alpha = backgroundOpacity)
                    else -> Color(0xFF12101D).copy(alpha = backgroundOpacity)
                }

                val borderColor = when (widgetTheme) {
                    "Cyber Neon" -> Color(0xFF06B6D4)
                    "Pitch Black OLED" -> Color(0xFF334155)
                    "Frosted Glass" -> Color(0xFF818CF8)
                    else -> Color(0xFF2E1A47)
                }

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(18.dp))
                        .background(bgColor)
                        .border(1.5.dp, borderColor, RoundedCornerShape(18.dp))
                        .padding(16.dp)
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = "VEDRA",
                                    color = Color(0xFFC4B5FD),
                                    fontWeight = FontWeight.ExtraBold,
                                    fontSize = 17.sp,
                                    letterSpacing = 1.2.sp
                                )
                                if (showLiveStatus) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Box(
                                            modifier = Modifier
                                                .size(6.dp)
                                                .clip(CircleShape)
                                                .background(Color(0xFF10B981))
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(
                                            text = "Active • Phone Home Screen",
                                            color = Color(0xFF10B981),
                                            fontSize = 10.5.sp,
                                            fontWeight = FontWeight.SemiBold
                                        )
                                    }
                                }
                            }

                            Box(
                                modifier = Modifier
                                    .size(44.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFF3B1F69))
                                    .border(1.5.dp, Color(0xFF8B5CF6), CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Mic,
                                    contentDescription = "Voice Mode",
                                    tint = Color(0xFFC4B5FD),
                                    modifier = Modifier.size(22.dp)
                                )
                            }
                        }

                        if (showQuickPrompts) {
                            Text(
                                text = "How can Vedra help you on your home screen today?",
                                color = Color(0xFF9CA3AF),
                                fontSize = 12.sp
                            )
                        }

                        if (widgetLayout == "Expanded Stats & Shortcuts Grid") {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(Color(0xFF1C182B))
                                        .padding(8.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text("⚡ Flashlight", color = Color.White, fontSize = 10.5.sp)
                                }
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(Color(0xFF1C182B))
                                        .padding(8.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text("🧠 Memory", color = Color.White, fontSize = 10.5.sp)
                                }
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(Color(0xFF1C182B))
                                        .padding(8.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text("🌤️ Weather", color = Color.White, fontSize = 10.5.sp)
                                }
                            }
                        }
                    }
                }
            }
        }

        // SECTION 2: PIN WIDGET TO PHONE HOME SCREEN BUTTON
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(Color(0xFF1F1235))
                    .border(1.5.dp, Color(0xFF8B5CF6), RoundedCornerShape(14.dp))
                    .clickable {
                        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                            val appWidgetManager = AppWidgetManager.getInstance(context)
                            val myProvider = ComponentName(context, VedraAppWidgetProvider::class.java)
                            if (appWidgetManager.isRequestPinAppWidgetSupported) {
                                appWidgetManager.requestPinAppWidget(myProvider, null, null)
                                isPinnedMessageShown = true
                            } else {
                                isPinnedMessageShown = true
                            }
                        } else {
                            isPinnedMessageShown = true
                        }
                    }
                    .padding(14.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(38.dp)
                                .clip(CircleShape)
                                .background(Color(0xFF3B1F69)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Widgets,
                                contentDescription = null,
                                tint = Color(0xFFC4B5FD),
                                modifier = Modifier.size(20.dp)
                            )
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        Column {
                            Text(
                                text = "Add Widget to Phone Home Screen",
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.5.sp
                            )
                            Text(
                                text = "Tap to pin Vedra Widget directly to launcher",
                                color = Color(0xFF9CA3AF),
                                fontSize = 11.sp
                            )
                        }
                    }

                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = null,
                        tint = Color(0xFFA78BFA),
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            if (isPinnedMessageShown) {
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "Request sent to launcher! Widget can be placed on your Phone Home Screen.",
                    color = Color(0xFF10B981),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.padding(start = 4.dp)
                )
            }
        }

        // SECTION 3: WIDGET LAYOUT STYLE
        item {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = "WIDGET LAYOUT STYLE",
                    color = Color(0xFFA78BFA),
                    fontWeight = FontWeight.Bold,
                    fontSize = 11.sp,
                    letterSpacing = 1.sp
                )

                val layouts = listOf(
                    "Compact Assistant Pill",
                    "Standard Voice & Prompts",
                    "Expanded Stats & Shortcuts Grid"
                )

                layouts.forEach { option ->
                    val isSelected = widgetLayout == option
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(if (isSelected) Color(0xFF1F1235) else Color(0xFF12101D))
                            .border(
                                width = if (isSelected) 1.5.dp else 1.dp,
                                color = if (isSelected) Color(0xFF8B5CF6) else Color(0xFF1E1B2E),
                                shape = RoundedCornerShape(12.dp)
                            )
                            .clickable {
                                widgetLayout = option
                                dbService.setSetting("widget_layout", option)
                            }
                            .padding(horizontal = 14.dp, vertical = 10.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = option,
                                color = Color.White,
                                fontSize = 13.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                            )
                            if (isSelected) {
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = null,
                                    tint = Color(0xFFA78BFA),
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    }
                }
            }
        }

        // SECTION 4: WIDGET THEME
        item {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = "WIDGET COLOR THEME",
                    color = Color(0xFFA78BFA),
                    fontWeight = FontWeight.Bold,
                    fontSize = 11.sp,
                    letterSpacing = 1.sp
                )

                val themes = listOf(
                    "Vedra Dark Purple",
                    "Cyber Neon",
                    "Pitch Black OLED",
                    "Frosted Glass"
                )

                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(themes) { t ->
                        val isSelected = widgetTheme == t
                        Box(
                            modifier = Modifier
                                .width(130.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(if (isSelected) Color(0xFF1F1235) else Color(0xFF12101D))
                                .border(
                                    width = if (isSelected) 1.5.dp else 1.dp,
                                    color = if (isSelected) Color(0xFF8B5CF6) else Color(0xFF1E1B2E),
                                    shape = RoundedCornerShape(12.dp)
                                )
                                .clickable {
                                    widgetTheme = t
                                    dbService.setSetting("widget_theme", t)
                                }
                                .padding(12.dp)
                        ) {
                            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(14.dp)
                                            .clip(CircleShape)
                                            .background(
                                                when (t) {
                                                    "Cyber Neon" -> Color(0xFF06B6D4)
                                                    "Pitch Black OLED" -> Color.Black
                                                    "Frosted Glass" -> Color(0xFF818CF8)
                                                    else -> Color(0xFF8B5CF6)
                                                }
                                            )
                                    )
                                    if (isSelected) {
                                        Icon(
                                            imageVector = Icons.Default.Check,
                                            contentDescription = null,
                                            tint = Color(0xFFA78BFA),
                                            modifier = Modifier.size(14.dp)
                                        )
                                    }
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = t,
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

        // SECTION 5: OPACITY & TOGGLES
        item {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    text = "VISUALS & BEHAVIOR",
                    color = Color(0xFFA78BFA),
                    fontWeight = FontWeight.Bold,
                    fontSize = 11.sp,
                    letterSpacing = 1.sp
                )

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .background(Color(0xFF12101D))
                        .border(1.dp, Color(0xFF1E1B2E), RoundedCornerShape(14.dp))
                        .padding(14.dp)
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                        // Opacity Slider
                        Column {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(text = "Widget Background Opacity", color = Color.White, fontSize = 12.5.sp, fontWeight = FontWeight.Medium)
                                Text(text = "${(backgroundOpacity * 100).toInt()}%", color = Color(0xFFA78BFA), fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }
                            Slider(
                                value = backgroundOpacity,
                                onValueChange = {
                                    backgroundOpacity = it
                                    dbService.setSetting("widget_opacity", it.toString())
                                },
                                valueRange = 0.2f..1.0f,
                                colors = SliderDefaults.colors(
                                    thumbColor = Color(0xFF8B5CF6),
                                    activeTrackColor = Color(0xFF8B5CF6),
                                    inactiveTrackColor = Color(0xFF1E1B2E)
                                )
                            )
                        }

                        // Live Status Toggle
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(text = "Show Live Online Status", color = Color.White, fontSize = 12.5.sp, fontWeight = FontWeight.Medium)
                                Text(text = "Display green dot and ready badge", color = Color(0xFF9CA3AF), fontSize = 10.5.sp)
                            }
                            Switch(
                                checked = showLiveStatus,
                                onCheckedChange = {
                                    showLiveStatus = it
                                    dbService.setSetting("widget_show_status", it.toString())
                                },
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = Color.White,
                                    checkedTrackColor = Color(0xFF8B5CF6),
                                    uncheckedThumbColor = Color(0xFF6B7280),
                                    uncheckedTrackColor = Color(0xFF1F1235)
                                )
                            )
                        }

                        // Prompts Toggle
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(text = "Show Quick Prompt Subtitle", color = Color.White, fontSize = 12.5.sp, fontWeight = FontWeight.Medium)
                                Text(text = "Display greeting message on widget", color = Color(0xFF9CA3AF), fontSize = 10.5.sp)
                            }
                            Switch(
                                checked = showQuickPrompts,
                                onCheckedChange = {
                                    showQuickPrompts = it
                                    dbService.setSetting("widget_show_prompts", it.toString())
                                },
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = Color.White,
                                    checkedTrackColor = Color(0xFF8B5CF6),
                                    uncheckedThumbColor = Color(0xFF6B7280),
                                    uncheckedTrackColor = Color(0xFF1F1235)
                                )
                            )
                        }
                    }
                }
            }
        }
    }
}
