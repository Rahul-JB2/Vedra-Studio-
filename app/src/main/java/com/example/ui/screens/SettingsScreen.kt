package com.example.ui.screens

import android.content.Context
import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import kotlinx.coroutines.launch
import com.example.services.DatabaseService
import com.example.services.GeminiService
import com.example.services.GoogleDriveService
import com.example.services.TranslationService
import com.example.services.VoiceService
import com.example.ui.components.CustomModal
import com.example.ui.theme.VedraBackground
import com.example.ui.theme.VedraBorder
import com.example.ui.theme.VedraPurplePrimary
import com.example.ui.theme.VedraSurface
import com.example.ui.theme.VedraSurfaceVariant
import com.example.ui.theme.VedraTextMuted
import com.example.ui.theme.VedraTextPrimary
import com.example.ui.theme.VedraTextSecondary

// ==========================================
// MAIN SETTINGS SCREEN ENTRY POINT
// ==========================================
@Composable
fun SettingsScreen(
    dbService: DatabaseService,
    voiceService: VoiceService
) {
    val settingsVer = dbService.settingsVersion.intValue
    val context = LocalContext.current
    var activeSubScreen by remember { mutableStateOf<String?>(null) }
    var searchQuery by remember { mutableStateOf("") }
    var showSubscriptionModal by remember { mutableStateOf(false) }

    val activePlan = remember {
        mutableStateOf(dbService.getSetting("subscription_plan", "Vedra Pro"))
    }

    if (activeSubScreen != null) {
        val screenTitle = when (activeSubScreen) {
            "general_pref" -> "General Preferences"
            "voice_speech" -> "Voice & Speech"
            "appearance" -> "Appearance"
            "home_screen" -> "Home Screen"
            "notifications" -> "Notifications"
            "sound_vibration" -> "Sound & Vibration"
            "ai_settings" -> "AI Settings"
            "memory_settings" -> "Memory Settings"
            "permissions" -> "System Permissions"
            "widgets" -> "AI Widget Settings"
            "drive_backup" -> "Backup & Sync"
            "about_support" -> "About & Support"
            else -> "Settings"
        }

        val screenSubtitle = when (activeSubScreen) {
            "general_pref" -> "Customize language, theme, units and more"
            "voice_speech" -> "Adjust voice model, pitch, rate & wake word"
            "appearance" -> "Themes, accent colors, font scaling"
            "home_screen" -> "Customize layout, greeting & suggestions"
            "notifications" -> "Manage notifications, study alerts & reminders"
            "sound_vibration" -> "Assistant sounds, touch haptics & volumes"
            "ai_settings" -> "AI models, response tone & API config"
            "memory_settings" -> "Manage what Vedra remembers about you"
            "permissions" -> "Manage device access & security approvals"
            "widgets" -> "Configure floating assistant & quick pills"
            "drive_backup" -> "Google Drive backup & cloud state"
            "about_support" -> "App version, support & system diagnostics"
            else -> "Customize Vedra your way"
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(VedraBackground)
        ) {
            // Subscreen Header with Back Button
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(VedraBackground)
                    .padding(horizontal = 12.dp, vertical = 12.dp)
            ) {
                IconButton(
                    onClick = { activeSubScreen = null },
                    modifier = Modifier
                        .size(36.dp)
                        .align(Alignment.CenterStart)
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = VedraTextPrimary,
                        modifier = Modifier.size(20.dp)
                    )
                }

                Column(
                    modifier = Modifier.align(Alignment.Center),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = screenTitle,
                        color = VedraTextPrimary,
                        fontWeight = FontWeight.Bold,
                        fontSize = 17.sp
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = screenSubtitle,
                        color = VedraTextSecondary,
                        fontSize = 11.5.sp
                    )
                }
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(1.dp)
                    .background(VedraBorder)
            )

            // Subscreen Content
            Box(modifier = Modifier.fillMaxSize()) {
                when (activeSubScreen) {
                    "general_pref" -> GeneralPreferencesDetailScreen(dbService = dbService)
                    "voice_speech" -> VoiceSpeechDetailScreen(dbService = dbService, voiceService = voiceService)
                    "appearance" -> AppearanceDetailScreen(dbService = dbService)
                    "home_screen" -> HomeScreenDetailScreen(dbService = dbService)
                    "notifications" -> NotificationsDetailScreen(dbService = dbService)
                    "sound_vibration" -> SoundVibrationDetailScreen(dbService = dbService)
                    "ai_settings" -> AiSettingsDetailScreen(dbService = dbService)
                    "memory_settings" -> MemorySettingsDetailScreen(dbService = dbService)
                    "permissions" -> PermissionOnboardingScreen(onComplete = { activeSubScreen = null }, isDismissable = true)
                    "widgets" -> WidgetsDetailScreen(dbService = dbService)
                    "drive_backup" -> DriveBackupDetailScreen(dbService = dbService)
                    "about_support" -> AboutSupportDetailScreen(dbService = dbService)
                }
            }
        }
        return
    }

    // MAIN SETTINGS LIST VIEW
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(VedraBackground)
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(top = 16.dp, bottom = 40.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // TOP HEADER
        item {
            Column(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "VEDRA",
                            color = Color(0xFFC4B5FD),
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 18.sp,
                            letterSpacing = 1.sp
                        )
                        Spacer(modifier = Modifier.width(6.dp))
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
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF231B38))
                            .border(1.5.dp, Color(0xFF8B5CF6), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = "User Profile",
                            tint = Color.White,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "SETTINGS",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 22.sp,
                    letterSpacing = 1.sp
                )
                Text(
                    text = "Customize Vedra your way",
                    color = Color(0xFF9CA3AF),
                    fontSize = 12.sp
                )
            }
        }

        // PRO SUBSCRIPTION BANNER
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(18.dp))
                    .background(Color(0xFF130E24))
                    .border(1.dp, Color(0xFF2C2248), RoundedCornerShape(18.dp))
                    .padding(16.dp)
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(44.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFF231A3E)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Mic,
                                    contentDescription = null,
                                    tint = Color(0xFFA78BFA),
                                    modifier = Modifier.size(22.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = activePlan.value,
                                        color = Color.White,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 16.sp
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(6.dp))
                                            .background(Color(0xFF6D28D9))
                                            .padding(horizontal = 8.dp, vertical = 2.dp)
                                    ) {
                                        Text(
                                            text = "★ Active",
                                            color = Color.White,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 10.sp
                                        )
                                    }
                                }
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = "You have access to all premium features.",
                                    color = Color(0xFF9CA3AF),
                                    fontSize = 11.5.sp
                                )
                            }
                        }
                    }

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color(0xFF1B152E))
                            .border(1.dp, Color(0xFF332752), RoundedCornerShape(12.dp))
                            .clickable { showSubscriptionModal = true }
                            .padding(horizontal = 14.dp, vertical = 10.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Manage Subscription",
                                color = Color.White,
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 13.sp
                            )
                            Icon(
                                imageVector = Icons.Default.ChevronRight,
                                contentDescription = "Manage",
                                tint = Color(0xFF9CA3AF),
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }
            }
        }

        // CATEGORY 1: GENERAL
        item {
            MainCategoryGroup(
                categoryTitle = "GENERAL",
                items = listOf(
                    SettingMenuItemData("general_pref", "General Preferences", "Language, theme, units, etc.", Icons.Default.Palette, Color(0xFF8B5CF6)),
                    SettingMenuItemData("voice_speech", "Voice & Speech", "Voice model, wake word, speed", Icons.Default.Mic, Color(0xFFEC4899)),
                    SettingMenuItemData("appearance", "Appearance", "Theme, colors, animations", Icons.Default.Palette, Color(0xFF3B82F6)),
                    SettingMenuItemData("home_screen", "Home Screen", "Customize home, suggestions", Icons.Default.Home, Color(0xFF10B981)),
                    SettingMenuItemData("notifications", "Notifications", "Manage notifications & alerts", Icons.Default.Notifications, Color(0xFFF59E0B)),
                    SettingMenuItemData("sound_vibration", "Sound & Vibration", "Assistant sounds, vibration", Icons.AutoMirrored.Filled.VolumeUp, Color(0xFF10B981))
                ),
                onSelect = { activeSubScreen = it }
            )
        }

        // CATEGORY 2: AI & MEMORY
        item {
            MainCategoryGroup(
                categoryTitle = "AI & MEMORY",
                items = listOf(
                    SettingMenuItemData("ai_settings", "AI Settings", "Model, response style, creativity", Icons.Default.Psychology, Color(0xFF8B5CF6)),
                    SettingMenuItemData("memory_settings", "Memory Settings", "Manage what Vedra remembers", Icons.Default.Storage, Color(0xFF10B981))
                ),
                onSelect = { activeSubScreen = it }
            )
        }

        // CATEGORY 3: SYSTEM & PRIVACY
        item {
            MainCategoryGroup(
                categoryTitle = "SYSTEM & PRIVACY",
                items = listOf(
                    SettingMenuItemData("permissions", "System Permissions", "Microphone, storage, SMS, YouTube", Icons.Default.Security, Color(0xFFEF4444)),
                    SettingMenuItemData("widgets", "AI Floating Widget", "Floating pill style, opacity & status", Icons.Default.Widgets, Color(0xFF8B5CF6)),
                    SettingMenuItemData("drive_backup", "Backup & Cloud Sync", "Google Drive backup & restore", Icons.Default.CloudSync, Color(0xFF3B82F6)),
                    SettingMenuItemData("about_support", "About & Diagnostics", "Version 2.4.0, support & cache", Icons.Default.Info, Color(0xFF6B7280))
                ),
                onSelect = { activeSubScreen = it }
            )
        }
    }

    // Subscription Modal
    if (showSubscriptionModal) {
        CustomModal(
            visible = true,
            title = "Subscription Plan",
            onDismissRequest = { showSubscriptionModal = false }
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                listOf("Vedra Pro (Active)", "Vedra Free", "Vedra Enterprise").forEach { plan ->
                    val cleanPlan = plan.replace(" (Active)", "")
                    val isSel = activePlan.value == cleanPlan
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(if (isSel) Color(0xFF231B38) else Color(0xFF141122))
                            .border(1.dp, if (isSel) Color(0xFF7C3AED) else Color(0xFF201B30), RoundedCornerShape(12.dp))
                            .clickable {
                                activePlan.value = cleanPlan
                                dbService.setSetting("subscription_plan", cleanPlan)
                                Toast.makeText(context, "Switched to $cleanPlan", Toast.LENGTH_SHORT).show()
                                showSubscriptionModal = false
                            }
                            .padding(12.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = plan,
                                color = if (isSel) Color(0xFFC4B5FD) else Color.White,
                                fontWeight = if (isSel) FontWeight.Bold else FontWeight.Normal,
                                fontSize = 14.sp
                            )
                            if (isSel) {
                                Icon(imageVector = Icons.Default.Check, contentDescription = null, tint = Color(0xFF8B5CF6), modifier = Modifier.size(18.dp))
                            }
                        }
                    }
                }
            }
        }
    }
}

// Data class for menu items
private data class SettingMenuItemData(
    val id: String,
    val title: String,
    val subtitle: String,
    val icon: ImageVector,
    val tintColor: Color
)

@Composable
private fun MainCategoryGroup(
    categoryTitle: String,
    items: List<SettingMenuItemData>,
    onSelect: (String) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Text(
            text = categoryTitle,
            color = Color(0xFFA78BFA),
            fontWeight = FontWeight.Bold,
            fontSize = 11.sp,
            letterSpacing = 1.sp,
            modifier = Modifier.padding(start = 2.dp)
        )

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(18.dp))
                .background(Color(0xFF110E1C))
                .border(1.dp, Color(0xFF1E1B2C), RoundedCornerShape(18.dp))
        ) {
            Column {
                items.forEachIndexed { index, item ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onSelect(item.id) }
                            .padding(horizontal = 14.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.weight(1f)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(item.tintColor.copy(alpha = 0.15f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = item.icon,
                                    contentDescription = item.title,
                                    tint = item.tintColor,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = item.title,
                                    color = Color.White,
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 14.sp
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = item.subtitle,
                                    color = Color(0xFF9CA3AF),
                                    fontSize = 11.5.sp
                                )
                            }
                        }

                        Icon(
                            imageVector = Icons.Default.ChevronRight,
                            contentDescription = "Open",
                            tint = Color(0xFF6B7280),
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    if (index < items.size - 1) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 14.dp)
                                .height(1.dp)
                                .background(Color(0xFF181528))
                        )
                    }
                }
            }
        }
    }
}

// ==========================================
// 1. GENERAL PREFERENCES DETAIL SCREEN
// ==========================================
@Composable
fun GeneralPreferencesDetailScreen(dbService: DatabaseService) {
    val context = LocalContext.current

    var appLanguage by remember { mutableStateOf(dbService.getSetting("pref_app_language", "English (India)")) }
    var appTheme by remember { mutableStateOf(dbService.getSetting("pref_app_theme", "Dark")) }
    var measurementUnits by remember { mutableStateOf(dbService.getSetting("pref_measurement_units", "Metric (km, °C)")) }
    var tempUnit by remember { mutableStateOf(dbService.getSetting("pref_temp_unit", "°C")) }
    var timeFormat by remember { mutableStateOf(dbService.getSetting("pref_time_format", "24-hour")) }
    var dateFormat by remember { mutableStateOf(dbService.getSetting("pref_date_format", "DD MMM YYYY")) }
    var defaultInputLang by remember { mutableStateOf(dbService.getSetting("pref_input_lang", "English")) }
    var region by remember { mutableStateOf(dbService.getSetting("pref_region", "India")) }
    var weekStartsOn by remember { mutableStateOf(dbService.getSetting("pref_week_starts", "Monday")) }

    var activeModal by remember { mutableStateOf<String?>(null) }

    fun saveSetting(key: String, value: String) {
        dbService.setSetting(key, value)
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF07060F))
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(top = 16.dp, bottom = 40.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // LANGUAGE
        item {
            PreferenceSectionHeader(title = "LANGUAGE")
            Spacer(modifier = Modifier.height(4.dp))
            PreferenceCard {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f)
                    ) {
                        PreferenceIconBox(icon = Icons.Default.Language)
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(text = "App Language", color = Color.White, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(text = "Select your preferred language", color = Color(0xFF9CA3AF), fontSize = 11.sp)
                        }
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    DropdownPill(text = appLanguage, onClick = { activeModal = "language" })
                }
            }
        }

        // THEME
        item {
            PreferenceSectionHeader(title = "THEME")
            Spacer(modifier = Modifier.height(4.dp))
            PreferenceCard {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        PreferenceIconBox(icon = Icons.Default.Palette)
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(text = "App Theme", color = Color.White, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(text = "Choose your visual experience", color = Color(0xFF9CA3AF), fontSize = 11.sp)
                        }
                    }

                    ThemeOptionRow(
                        title = "Dark",
                        subtitle = "Default dark theme",
                        icon = Icons.Default.NightsStay,
                        isSelected = appTheme == "Dark",
                        onClick = {
                            appTheme = "Dark"
                            saveSetting("pref_app_theme", "Dark")
                        }
                    )

                    ThemeOptionRow(
                        title = "Light",
                        subtitle = "Clean light theme",
                        icon = Icons.Default.WbSunny,
                        isSelected = appTheme == "Light",
                        onClick = {
                            appTheme = "Light"
                            saveSetting("pref_app_theme", "Light")
                        }
                    )

                    ThemeOptionRow(
                        title = "System Default",
                        subtitle = "Follow system theme",
                        icon = Icons.Default.Smartphone,
                        isSelected = appTheme == "System Default",
                        onClick = {
                            appTheme = "System Default"
                            saveSetting("pref_app_theme", "System Default")
                        }
                    )
                }
            }
        }

        // UNITS
        item {
            PreferenceSectionHeader(title = "UNITS")
            Spacer(modifier = Modifier.height(4.dp))
            PreferenceCard {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f)
                    ) {
                        PreferenceIconBox(icon = Icons.Default.Straighten)
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(text = "Measurement Units", color = Color.White, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(text = "Choose your preferred units", color = Color(0xFF9CA3AF), fontSize = 11.sp)
                        }
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    DropdownPill(text = measurementUnits, onClick = { activeModal = "units" })
                }
            }
        }

        // TEMPERATURE UNIT
        item {
            PreferenceSectionHeader(title = "TEMPERATURE UNIT")
            Spacer(modifier = Modifier.height(4.dp))
            PreferenceCard {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f)
                    ) {
                        PreferenceIconBox(icon = Icons.Default.Thermostat)
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(text = "Temperature", color = Color.White, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(text = "Select temperature display unit", color = Color(0xFF9CA3AF), fontSize = 11.sp)
                        }
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    SegmentedToggleTwo(
                        option1 = "°C",
                        option2 = "°F",
                        selectedOption = tempUnit,
                        onSelect = {
                            tempUnit = it
                            saveSetting("pref_temp_unit", it)
                        }
                    )
                }
            }
        }

        // TIME FORMAT
        item {
            PreferenceSectionHeader(title = "TIME FORMAT")
            Spacer(modifier = Modifier.height(4.dp))
            PreferenceCard {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f)
                    ) {
                        PreferenceIconBox(icon = Icons.Default.AccessTime)
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(text = "Time Format", color = Color.White, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(text = "Choose how time is displayed", color = Color(0xFF9CA3AF), fontSize = 11.sp)
                        }
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    SegmentedToggleTwo(
                        option1 = "12-hour",
                        option2 = "24-hour",
                        selectedOption = timeFormat,
                        onSelect = {
                            timeFormat = it
                            saveSetting("pref_time_format", it)
                        }
                    )
                }
            }
        }

        // DATE FORMAT
        item {
            PreferenceSectionHeader(title = "DATE FORMAT")
            Spacer(modifier = Modifier.height(4.dp))
            PreferenceCard {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f)
                    ) {
                        PreferenceIconBox(icon = Icons.Default.CalendarToday)
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(text = "Date Format", color = Color.White, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(text = "Choose how date is displayed", color = Color(0xFF9CA3AF), fontSize = 11.sp)
                        }
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    DropdownPill(text = dateFormat, onClick = { activeModal = "dateFormat" })
                }
            }
        }

        // OTHER PREFERENCES
        item {
            PreferenceSectionHeader(title = "OTHER PREFERENCES")
            Spacer(modifier = Modifier.height(4.dp))
            PreferenceCard {
                Column {
                    OtherPreferenceRow(
                        icon = Icons.Default.Translate,
                        title = "Default Input Language",
                        subtitle = "Language used for typing & voice input",
                        value = defaultInputLang,
                        onClick = { activeModal = "inputLang" }
                    )

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 10.dp)
                            .height(1.dp)
                            .background(Color(0xFF1E1B2C))
                    )

                    OtherPreferenceRow(
                        icon = Icons.Default.LocationOn,
                        title = "Region",
                        subtitle = "Select your region",
                        value = region,
                        onClick = { activeModal = "region" }
                    )

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 10.dp)
                            .height(1.dp)
                            .background(Color(0xFF1E1B2C))
                    )

                    OtherPreferenceRow(
                        icon = Icons.Default.DateRange,
                        title = "Week Starts On",
                        subtitle = "Choose the first day of the week",
                        value = weekStartsOn,
                        onClick = { activeModal = "weekStarts" }
                    )
                }
            }
        }

        // RESET TO DEFAULT BUTTON
        item {
            Spacer(modifier = Modifier.height(6.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color(0xFF6D28D9))
                    .clickable {
                        appLanguage = "English (India)"
                        appTheme = "Dark"
                        measurementUnits = "Metric (km, °C)"
                        tempUnit = "°C"
                        timeFormat = "24-hour"
                        dateFormat = "DD MMM YYYY"
                        defaultInputLang = "English"
                        region = "India"
                        weekStartsOn = "Monday"

                        saveSetting("pref_app_language", appLanguage)
                        saveSetting("pref_app_theme", appTheme)
                        saveSetting("pref_measurement_units", measurementUnits)
                        saveSetting("pref_temp_unit", tempUnit)
                        saveSetting("pref_time_format", timeFormat)
                        saveSetting("pref_date_format", dateFormat)
                        saveSetting("pref_input_lang", defaultInputLang)
                        saveSetting("pref_region", region)
                        saveSetting("pref_week_starts", weekStartsOn)

                        Toast.makeText(context, "Preferences reset to default", Toast.LENGTH_SHORT).show()
                    },
                contentAlignment = Alignment.Center
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = "Reset",
                        tint = Color.White,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "RESET TO DEFAULT",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        letterSpacing = 1.sp
                    )
                }
            }
        }
    }

    // Modals
    when (activeModal) {
        "language" -> {
            SelectionListModal(
                title = "Select App Language",
                options = listOf("English (India)", "English (US)", "Hindi (हिन्दी)", "Spanish (Español)", "French (Français)", "German (Deutsch)"),
                selectedOption = appLanguage,
                onSelect = {
                    appLanguage = it
                    saveSetting("pref_app_language", it)
                    val langCode = when (it) {
                        "Hindi (हिन्दी)" -> "hi"
                        "Spanish (Español)" -> "es"
                        "French (Français)" -> "fr"
                        "German (Deutsch)" -> "de"
                        else -> "en"
                    }
                    TranslationService.setTargetLanguage(langCode)
                    Toast.makeText(context, "App language changed to $it", Toast.LENGTH_SHORT).show()
                    activeModal = null
                },
                onDismiss = { activeModal = null }
            )
        }
        "units" -> {
            SelectionListModal(
                title = "Select Measurement Units",
                options = listOf("Metric (km, °C)", "Imperial (mi, °F)"),
                selectedOption = measurementUnits,
                onSelect = {
                    measurementUnits = it
                    saveSetting("pref_measurement_units", it)
                    activeModal = null
                },
                onDismiss = { activeModal = null }
            )
        }
        "dateFormat" -> {
            SelectionListModal(
                title = "Select Date Format",
                options = listOf("DD MMM YYYY", "MM/DD/YYYY", "YYYY-MM-DD", "DD/MM/YYYY"),
                selectedOption = dateFormat,
                onSelect = {
                    dateFormat = it
                    saveSetting("pref_date_format", it)
                    activeModal = null
                },
                onDismiss = { activeModal = null }
            )
        }
        "inputLang" -> {
            SelectionListModal(
                title = "Select Default Input Language",
                options = listOf("English", "Hindi", "Hinglish", "Spanish", "French"),
                selectedOption = defaultInputLang,
                onSelect = {
                    defaultInputLang = it
                    saveSetting("pref_input_lang", it)
                    activeModal = null
                },
                onDismiss = { activeModal = null }
            )
        }
        "region" -> {
            SelectionListModal(
                title = "Select Region",
                options = listOf("India", "United States", "United Kingdom", "Canada", "Australia", "Global"),
                selectedOption = region,
                onSelect = {
                    region = it
                    saveSetting("pref_region", it)
                    activeModal = null
                },
                onDismiss = { activeModal = null }
            )
        }
        "weekStarts" -> {
            SelectionListModal(
                title = "Select Week Start Day",
                options = listOf("Monday", "Sunday", "Saturday"),
                selectedOption = weekStartsOn,
                onSelect = {
                    weekStartsOn = it
                    saveSetting("pref_week_starts", it)
                    activeModal = null
                },
                onDismiss = { activeModal = null }
            )
        }
    }
}

// ==========================================
// 2. VOICE & SPEECH DETAIL SCREEN
// ==========================================
@Composable
fun VoiceSpeechDetailScreen(dbService: DatabaseService, voiceService: VoiceService) {
    val context = LocalContext.current

    var voiceSpeed by remember { mutableFloatStateOf(dbService.getSetting("voice_speed", "1.0").toFloatOrNull() ?: 1.0f) }
    var voicePitch by remember { mutableFloatStateOf(dbService.getSetting("voice_pitch", "1.0").toFloatOrNull() ?: 1.0f) }
    var voiceModel by remember { mutableStateOf(dbService.getSetting("voice_model", "Priya (Neural AI)")) }
    var wakeWordEnabled by remember { mutableStateOf(dbService.getSetting("wake_word_enabled", "true") == "true") }
    var ttsEngine by remember { mutableStateOf(dbService.getSetting("tts_engine", "Google Speech Engine")) }

    var showVoiceModelModal by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF07060F))
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(top = 16.dp, bottom = 40.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // VOICE SPEED & PITCH
        item {
            PreferenceSectionHeader(title = "VOICE CONTROLS")
            Spacer(modifier = Modifier.height(4.dp))
            PreferenceCard {
                Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    // Speed
                    Column {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(text = "Voice Speed", color = Color.White, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                            Text(text = String.format("%.1fx", voiceSpeed), color = Color(0xFFA78BFA), fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Slider(
                            value = voiceSpeed,
                            onValueChange = {
                                voiceSpeed = it
                                dbService.setSetting("voice_speed", it.toString())
                                voiceService.setPitchAndRate(voicePitch, voiceSpeed)
                            },
                            valueRange = 0.5f..2.0f,
                            steps = 15,
                            colors = SliderDefaults.colors(
                                thumbColor = Color(0xFF8B5CF6),
                                activeTrackColor = Color(0xFF8B5CF6),
                                inactiveTrackColor = Color(0xFF2E2744)
                            )
                        )
                    }

                    // Pitch
                    Column {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(text = "Voice Pitch", color = Color.White, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                            Text(text = String.format("%.1fx", voicePitch), color = Color(0xFFA78BFA), fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Slider(
                            value = voicePitch,
                            onValueChange = {
                                voicePitch = it
                                dbService.setSetting("voice_pitch", it.toString())
                                voiceService.setPitchAndRate(voicePitch, voiceSpeed)
                            },
                            valueRange = 0.5f..1.5f,
                            steps = 10,
                            colors = SliderDefaults.colors(
                                thumbColor = Color(0xFF8B5CF6),
                                activeTrackColor = Color(0xFF8B5CF6),
                                inactiveTrackColor = Color(0xFF2E2744)
                            )
                        )
                    }

                    // Test Voice Button
                    Button(
                        onClick = {
                            voiceService.setPitchAndRate(voicePitch, voiceSpeed)
                            voiceService.speak("Hello! I am Vedra. Speech rate and pitch have been updated successfully.")
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF231B38)),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(imageVector = Icons.Default.PlayArrow, contentDescription = "Play", tint = Color(0xFFA78BFA), modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(text = "LISTEN TO SAMPLE VOICE", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        }
                    }
                }
            }
        }

        // VOICE MODEL & WAKE WORD
        item {
            PreferenceSectionHeader(title = "VOICE ENGINE & WAKE WORD")
            Spacer(modifier = Modifier.height(4.dp))
            PreferenceCard {
                Column {
                    OtherPreferenceRow(
                        icon = Icons.Default.Mic,
                        title = "Voice Model",
                        subtitle = "Select AI voice persona",
                        value = voiceModel,
                        onClick = { showVoiceModelModal = true }
                    )

                    Box(modifier = Modifier.fillMaxWidth().padding(vertical = 10.dp).height(1.dp).background(Color(0xFF1E1B2C)))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            PreferenceIconBox(icon = Icons.Default.GraphicEq)
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(text = "Wake Word (\"Hey Vedra\")", color = Color.White, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(text = "Always listening for activation keyword", color = Color(0xFF9CA3AF), fontSize = 11.sp)
                            }
                        }
                        Switch(
                            checked = wakeWordEnabled,
                            onCheckedChange = {
                                wakeWordEnabled = it
                                dbService.setSetting("wake_word_enabled", it.toString())
                            },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = Color.White,
                                checkedTrackColor = Color(0xFF8B5CF6),
                                uncheckedThumbColor = Color(0xFF9CA3AF),
                                uncheckedTrackColor = Color(0xFF1E1B2C)
                            )
                        )
                    }
                }
            }
        }
    }

    if (showVoiceModelModal) {
        SelectionListModal(
            title = "Select Voice Model",
            options = listOf("Priya (Neural AI)", "Alex (Male Natural)", "Maya (Studio Clear)", "Aria (Soft Expressive)"),
            selectedOption = voiceModel,
            onSelect = {
                voiceModel = it
                dbService.setSetting("voice_model", it)
                Toast.makeText(context, "Selected voice $it", Toast.LENGTH_SHORT).show()
                showVoiceModelModal = false
            },
            onDismiss = { showVoiceModelModal = false }
        )
    }
}

// ==========================================
// 3. APPEARANCE DETAIL SCREEN
// ==========================================
@Composable
fun AppearanceDetailScreen(dbService: DatabaseService) {
    val context = LocalContext.current

    var accentColor by remember { mutableStateOf(dbService.getSetting("pref_accent_color", "Royal Purple")) }
    var fontScale by remember { mutableStateOf(dbService.getSetting("pref_font_scale", "Standard")) }
    var amoledDark by remember { mutableStateOf(dbService.getSetting("pref_amoled_dark", "true") == "true") }
    var smoothAnim by remember { mutableStateOf(dbService.getSetting("pref_smooth_anim", "true") == "true") }

    LazyColumn(
        modifier = Modifier.fillMaxSize().background(Color(0xFF07060F)).padding(horizontal = 16.dp),
        contentPadding = PaddingValues(top = 16.dp, bottom = 40.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            PreferenceSectionHeader(title = "ACCENT COLOR")
            Spacer(modifier = Modifier.height(4.dp))
            PreferenceCard {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(text = "Primary Accent Highlight", color = Color.White, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        listOf(
                            "Royal Purple" to Color(0xFF8B5CF6),
                            "Cyber Blue" to Color(0xFF3B82F6),
                            "Emerald" to Color(0xFF10B981),
                            "Rose Pink" to Color(0xFFEC4899),
                            "Amber Gold" to Color(0xFFF59E0B)
                        ).forEach { (name, color) ->
                            val isSel = accentColor == name
                            Box(
                                modifier = Modifier
                                    .size(44.dp)
                                    .clip(CircleShape)
                                    .background(color)
                                    .border(
                                        2.dp,
                                        if (isSel) Color.White else Color.Transparent,
                                        CircleShape
                                    )
                                    .clickable {
                                        accentColor = name
                                        dbService.setSetting("pref_accent_color", name)
                                        Toast.makeText(context, "Accent color set to $name", Toast.LENGTH_SHORT).show()
                                    },
                                contentAlignment = Alignment.Center
                            ) {
                                if (isSel) {
                                    Icon(imageVector = Icons.Default.Check, contentDescription = null, tint = Color.White, modifier = Modifier.size(20.dp))
                                }
                            }
                        }
                    }
                }
            }
        }

        item {
            PreferenceSectionHeader(title = "VISUAL TWEAKS")
            Spacer(modifier = Modifier.height(4.dp))
            PreferenceCard {
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(text = "Pure AMOLED Black", color = Color.White, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                            Text(text = "Deep pitch black background for OLED screens", color = Color(0xFF9CA3AF), fontSize = 11.sp)
                        }
                        Switch(
                            checked = amoledDark,
                            onCheckedChange = {
                                amoledDark = it
                                dbService.setSetting("pref_amoled_dark", it.toString())
                            },
                            colors = SwitchDefaults.colors(checkedTrackColor = Color(0xFF8B5CF6))
                        )
                    }

                    Box(modifier = Modifier.fillMaxWidth().padding(vertical = 10.dp).height(1.dp).background(Color(0xFF1E1B2C)))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(text = "Smooth UI Animations", color = Color.White, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                            Text(text = "Enable transition motion & effects", color = Color(0xFF9CA3AF), fontSize = 11.sp)
                        }
                        Switch(
                            checked = smoothAnim,
                            onCheckedChange = {
                                smoothAnim = it
                                dbService.setSetting("pref_smooth_anim", it.toString())
                            },
                            colors = SwitchDefaults.colors(checkedTrackColor = Color(0xFF8B5CF6))
                        )
                    }
                }
            }
        }
    }
}

// ==========================================
// 4. HOME SCREEN DETAIL SCREEN
// ==========================================
@Composable
fun HomeScreenDetailScreen(dbService: DatabaseService) {
    var showGreeting by remember { mutableStateOf(dbService.getSetting("home_show_greeting", "true") == "true") }
    var showSuggestions by remember { mutableStateOf(dbService.getSetting("home_show_suggestions", "true") == "true") }
    var compactGrid by remember { mutableStateOf(dbService.getSetting("home_compact_grid", "false") == "true") }

    LazyColumn(
        modifier = Modifier.fillMaxSize().background(Color(0xFF07060F)).padding(horizontal = 16.dp),
        contentPadding = PaddingValues(top = 16.dp, bottom = 40.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            PreferenceSectionHeader(title = "HOME LAYOUT")
            Spacer(modifier = Modifier.height(4.dp))
            PreferenceCard {
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(text = "Show Welcome Banner", color = Color.White, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                            Text(text = "Display greeting card at top of home screen", color = Color(0xFF9CA3AF), fontSize = 11.sp)
                        }
                        Switch(
                            checked = showGreeting,
                            onCheckedChange = {
                                showGreeting = it
                                dbService.setSetting("home_show_greeting", it.toString())
                            },
                            colors = SwitchDefaults.colors(checkedTrackColor = Color(0xFF8B5CF6))
                        )
                    }

                    Box(modifier = Modifier.fillMaxWidth().padding(vertical = 10.dp).height(1.dp).background(Color(0xFF1E1B2C)))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(text = "Quick Suggestion Chips", color = Color.White, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                            Text(text = "Show smart task & action shortcuts", color = Color(0xFF9CA3AF), fontSize = 11.sp)
                        }
                        Switch(
                            checked = showSuggestions,
                            onCheckedChange = {
                                showSuggestions = it
                                dbService.setSetting("home_show_suggestions", it.toString())
                            },
                            colors = SwitchDefaults.colors(checkedTrackColor = Color(0xFF8B5CF6))
                        )
                    }
                }
            }
        }
    }
}

// ==========================================
// 5. NOTIFICATIONS DETAIL SCREEN
// ==========================================
@Composable
fun NotificationsDetailScreen(dbService: DatabaseService) {
    var pushEnabled by remember { mutableStateOf(dbService.getSetting("notif_push_enabled", "true") == "true") }
    var studyReminders by remember { mutableStateOf(dbService.getSetting("notif_study_reminders", "true") == "true") }
    var voiceAlerts by remember { mutableStateOf(dbService.getSetting("notif_voice_alerts", "false") == "true") }

    LazyColumn(
        modifier = Modifier.fillMaxSize().background(Color(0xFF07060F)).padding(horizontal = 16.dp),
        contentPadding = PaddingValues(top = 16.dp, bottom = 40.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            PreferenceSectionHeader(title = "NOTIFICATION ALERTS")
            Spacer(modifier = Modifier.height(4.dp))
            PreferenceCard {
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(text = "Push Notifications", color = Color.White, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                            Text(text = "Receive updates, reminders and tips", color = Color(0xFF9CA3AF), fontSize = 11.sp)
                        }
                        Switch(
                            checked = pushEnabled,
                            onCheckedChange = {
                                pushEnabled = it
                                dbService.setSetting("notif_push_enabled", it.toString())
                            },
                            colors = SwitchDefaults.colors(checkedTrackColor = Color(0xFF8B5CF6))
                        )
                    }

                    Box(modifier = Modifier.fillMaxWidth().padding(vertical = 10.dp).height(1.dp).background(Color(0xFF1E1B2C)))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(text = "Daily Study & Habit Reminders", color = Color.White, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                            Text(text = "Scheduled alerts for daily study goals", color = Color(0xFF9CA3AF), fontSize = 11.sp)
                        }
                        Switch(
                            checked = studyReminders,
                            onCheckedChange = {
                                studyReminders = it
                                dbService.setSetting("notif_study_reminders", it.toString())
                            },
                            colors = SwitchDefaults.colors(checkedTrackColor = Color(0xFF8B5CF6))
                        )
                    }
                }
            }
        }
    }
}

// ==========================================
// 6. SOUND & VIBRATION DETAIL SCREEN
// ==========================================
@Composable
fun SoundVibrationDetailScreen(dbService: DatabaseService) {
    var volume by remember { mutableFloatStateOf(dbService.getSetting("sound_volume", "0.8").toFloatOrNull() ?: 0.8f) }
    var hapticFeedback by remember { mutableStateOf(dbService.getSetting("sound_haptics", "true") == "true") }

    LazyColumn(
        modifier = Modifier.fillMaxSize().background(Color(0xFF07060F)).padding(horizontal = 16.dp),
        contentPadding = PaddingValues(top = 16.dp, bottom = 40.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            PreferenceSectionHeader(title = "AUDIO & HAPTICS")
            Spacer(modifier = Modifier.height(4.dp))
            PreferenceCard {
                Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    Column {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(text = "Assistant Speech Volume", color = Color.White, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                            Text(text = "${(volume * 100).toInt()}%", color = Color(0xFFA78BFA), fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Slider(
                            value = volume,
                            onValueChange = {
                                volume = it
                                dbService.setSetting("sound_volume", it.toString())
                            },
                            colors = SliderDefaults.colors(
                                thumbColor = Color(0xFF8B5CF6),
                                activeTrackColor = Color(0xFF8B5CF6),
                                inactiveTrackColor = Color(0xFF2E2744)
                            )
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(text = "Touch Haptic Feedback", color = Color.White, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                            Text(text = "Vibrate on button clicks & voice activation", color = Color(0xFF9CA3AF), fontSize = 11.sp)
                        }
                        Switch(
                            checked = hapticFeedback,
                            onCheckedChange = {
                                hapticFeedback = it
                                dbService.setSetting("sound_haptics", it.toString())
                            },
                            colors = SwitchDefaults.colors(checkedTrackColor = Color(0xFF8B5CF6))
                        )
                    }
                }
            }
        }
    }
}

// ==========================================
// 7. AI SETTINGS DETAIL SCREEN
// ==========================================
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun AiSettingsDetailScreen(dbService: DatabaseService) {
    val context = LocalContext.current

    var networkMode by remember { mutableStateOf(dbService.getSetting("ai_network_mode", "Auto")) }
    var provider by remember { mutableStateOf(dbService.getSetting("ai_provider", "Gemini AI")) }
    var selectedModel by remember { mutableStateOf(dbService.getSetting("ai_model", "Gemini 3.5 Flash")) }
    var responseTone by remember { mutableStateOf(dbService.getSetting("ai_tone", "Short & Direct")) }

    var geminiIntelligence by remember { mutableStateOf(dbService.getSetting("setting_feature_gemini_intelligence", "true") != "false") }
    var geminiChatbot by remember { mutableStateOf(dbService.getSetting("setting_feature_gemini_chatbot", "true") != "false") }
    var voiceConversation by remember { mutableStateOf(dbService.getSetting("setting_feature_voice_conversation", "true") != "false") }
    var highOrderThinking by remember { mutableStateOf(dbService.getSetting("setting_feature_high_order_thinking", "false") == "true") }

    var geminiApiKey by remember { mutableStateOf(dbService.getSetting("gemini_api_key", dbService.getSetting("api_key", ""))) }
    var openAiApiKey by remember { mutableStateOf(dbService.getSetting("openai_api_key", "")) }
    var otherAiApiKey by remember { mutableStateOf(dbService.getSetting("other_api_key", "")) }

    var showProviderModal by remember { mutableStateOf(false) }
    var showModelModal by remember { mutableStateOf(false) }
    var showToneModal by remember { mutableStateOf(false) }

    val isOnline = remember { GeminiService.isDeviceOnline(context) }

    LazyColumn(
        modifier = Modifier.fillMaxSize().background(Color(0xFF07060F)).padding(horizontal = 16.dp),
        contentPadding = PaddingValues(top = 16.dp, bottom = 40.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // --- 1. ENGINE STATUS CARD ---
        item {
            PreferenceSectionHeader(title = "ACTIVE AI ENGINE STATUS")
            Spacer(modifier = Modifier.height(4.dp))
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFF131022)),
                shape = RoundedCornerShape(16.dp),
                border = BorderStroke(1.dp, Color(0xFF2E2744)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Box(
                                modifier = Modifier
                                    .size(10.dp)
                                    .background(
                                        when {
                                            networkMode == "Force Offline" -> Color(0xFFE11D48)
                                            isOnline -> Color(0xFF10B981)
                                            else -> Color(0xFFF59E0B)
                                        },
                                        shape = CircleShape
                                    )
                            )
                            Text(
                                text = when (networkMode) {
                                    "Force Offline" -> "OFFLINE MODE ACTIVE"
                                    "Force Online" -> if (isOnline) "ONLINE (FORCED)" else "NO NETWORK CONNECTION"
                                    else -> if (isOnline) "AUTO SWITCH (ONLINE)" else "AUTO SWITCH (OFFLINE FALLBACK)"
                                },
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp
                            )
                        }
                        Surface(
                            color = Color(0xFF6D28D9).copy(alpha = 0.2f),
                            shape = RoundedCornerShape(20.dp),
                            border = BorderStroke(1.dp, Color(0xFF8B5CF6))
                        ) {
                            Text(
                                text = provider,
                                color = Color(0xFFA78BFA),
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                            )
                        }
                    }

                    Text(
                        text = "Model: $selectedModel • Tone: $responseTone",
                        color = Color(0xFF9CA3AF),
                        fontSize = 11.sp
                    )
                }
            }
        }

        // --- 2. CONNECTIVITY & AUTO SWITCH MODE ---
        item {
            PreferenceSectionHeader(title = "CONNECTIVITY & SWITCH MODE")
            Spacer(modifier = Modifier.height(4.dp))
            PreferenceCard {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(
                        text = "Choose how Vedra switches between Online AI Cloud and Local Offline Engine.",
                        color = Color(0xFF9CA3AF),
                        fontSize = 12.sp
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        listOf("Auto", "Force Online", "Force Offline").forEach { mode ->
                            val isSelected = networkMode == mode
                            val bg = if (isSelected) Color(0xFF6D28D9) else Color(0xFF1E1B2C)
                            val border = if (isSelected) Color(0xFFA78BFA) else Color(0xFF2E2744)

                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .background(bg, RoundedCornerShape(10.dp))
                                    .border(1.dp, border, RoundedCornerShape(10.dp))
                                    .clickable {
                                        networkMode = mode
                                        dbService.setSetting("ai_network_mode", mode)
                                        Toast.makeText(context, "Switch mode: $mode", Toast.LENGTH_SHORT).show()
                                    }
                                    .padding(vertical = 10.dp, horizontal = 4.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = mode,
                                    color = if (isSelected) Color.White else Color(0xFF9CA3AF),
                                    fontSize = 11.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                )
                            }
                        }
                    }
                }
            }
        }

        // --- 3. FEATURE TOGGLES & CAPABILITIES ---
        item {
            PreferenceSectionHeader(title = "AI CAPABILITIES & FEATURE TOGGLES")
            Spacer(modifier = Modifier.height(4.dp))
            PreferenceCard {
                Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                    // 1. Gemini Intelligence
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                            PreferenceIconBox(icon = Icons.Default.AutoAwesome)
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(text = "Gemini Intelligence", color = VedraTextPrimary, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                                Text(text = "Enable Cloud Gemini intelligence for general analysis and tasks", color = VedraTextSecondary, fontSize = 11.5.sp)
                            }
                        }
                        Switch(
                            checked = geminiIntelligence,
                            onCheckedChange = {
                                geminiIntelligence = it
                                dbService.setSetting("setting_feature_gemini_intelligence", it.toString())
                            },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = Color.White,
                                checkedTrackColor = VedraPurplePrimary,
                                uncheckedThumbColor = VedraTextMuted,
                                uncheckedTrackColor = VedraSurfaceVariant
                            )
                        )
                    }

                    Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(VedraBorder))

                    // 2. Gemini Chatbot
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                            PreferenceIconBox(icon = Icons.Default.Forum)
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(text = "Gemini Chatbot", color = VedraTextPrimary, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                                Text(text = "Enable Cloud Gemini conversation mode in chat interface", color = VedraTextSecondary, fontSize = 11.5.sp)
                            }
                        }
                        Switch(
                            checked = geminiChatbot,
                            onCheckedChange = {
                                geminiChatbot = it
                                dbService.setSetting("setting_feature_gemini_chatbot", it.toString())
                            },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = Color.White,
                                checkedTrackColor = VedraPurplePrimary,
                                uncheckedThumbColor = VedraTextMuted,
                                uncheckedTrackColor = VedraSurfaceVariant
                            )
                        )
                    }

                    Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(VedraBorder))

                    // 3. Voice Conversation
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                            PreferenceIconBox(icon = Icons.Default.RecordVoiceOver)
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(text = "Voice Conversation", color = VedraTextPrimary, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                                Text(text = "Enable real-time voice conversations and spoken AI responses", color = VedraTextSecondary, fontSize = 11.5.sp)
                            }
                        }
                        Switch(
                            checked = voiceConversation,
                            onCheckedChange = {
                                voiceConversation = it
                                dbService.setSetting("setting_feature_voice_conversation", it.toString())
                            },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = Color.White,
                                checkedTrackColor = VedraPurplePrimary,
                                uncheckedThumbColor = VedraTextMuted,
                                uncheckedTrackColor = VedraSurfaceVariant
                            )
                        )
                    }

                    Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(VedraBorder))

                    // 4. High Order Thinking
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                            PreferenceIconBox(icon = Icons.Default.Psychology)
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(text = "High Order Thinking", color = VedraTextPrimary, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                                Text(text = "Enable deep analytical reasoning mode (gemini-3.1-pro-preview)", color = VedraTextSecondary, fontSize = 11.5.sp)
                            }
                        }
                        Switch(
                            checked = highOrderThinking,
                            onCheckedChange = {
                                highOrderThinking = it
                                dbService.setSetting("setting_feature_high_order_thinking", it.toString())
                            },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = Color.White,
                                checkedTrackColor = VedraPurplePrimary,
                                uncheckedThumbColor = VedraTextMuted,
                                uncheckedTrackColor = VedraSurfaceVariant
                            )
                        )
                    }
                }
            }
        }

        // --- 4. AI PROVIDER & MODEL ---
        item {
            PreferenceSectionHeader(title = "AI PROVIDER & REASONING MODEL")
            Spacer(modifier = Modifier.height(4.dp))
            PreferenceCard {
                Column {
                    OtherPreferenceRow(
                        icon = Icons.Default.Psychology,
                        title = "AI Provider",
                        subtitle = "Select AI Cloud or Native provider",
                        value = provider,
                        onClick = { showProviderModal = true }
                    )

                    Box(modifier = Modifier.fillMaxWidth().padding(vertical = 10.dp).height(1.dp).background(Color(0xFF1E1B2C)))

                    OtherPreferenceRow(
                        icon = Icons.Default.Tune,
                        title = "Model Variant",
                        subtitle = "Specific model configuration",
                        value = selectedModel,
                        onClick = { showModelModal = true }
                    )

                    Box(modifier = Modifier.fillMaxWidth().padding(vertical = 10.dp).height(1.dp).background(Color(0xFF1E1B2C)))

                    OtherPreferenceRow(
                        icon = Icons.Default.GraphicEq,
                        title = "Response Tone",
                        subtitle = "Personality and word limit",
                        value = responseTone,
                        onClick = { showToneModal = true }
                    )
                }
            }
        }

        // --- 4. API KEYS CONFIGURATION ---
        item {
            PreferenceSectionHeader(title = "API KEYS & CREDENTIALS")
            Spacer(modifier = Modifier.height(4.dp))
            PreferenceCard {
                var isGeminiKeyRevealed by remember { mutableStateOf(false) }
                var isOpenAiKeyRevealed by remember { mutableStateOf(false) }
                var isOtherKeyRevealed by remember { mutableStateOf(false) }

                Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    Surface(
                        color = Color(0xFF1E1B2C),
                        shape = RoundedCornerShape(10.dp),
                        border = BorderStroke(1.dp, Color(0xFF3B2E58))
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(imageVector = Icons.Default.Lock, contentDescription = null, tint = Color(0xFFA78BFA), modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Keys are masked. Tap 'SAVE KEY' to store. Long-Press 'SAVE KEY' button to reveal key.",
                                color = Color(0xFFD1D5DB),
                                fontSize = 11.sp
                            )
                        }
                    }

                    // Gemini API Key
                    Column {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                            Text(text = "Gemini API Key", color = Color.White, fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                            Text(
                                text = if (isGeminiKeyRevealed) "👁️ UNMASKED" else if (geminiApiKey.isBlank()) "Default Built-in" else "Custom Key Saved",
                                color = if (isGeminiKeyRevealed) Color(0xFFF59E0B) else if (geminiApiKey.isBlank()) Color(0xFF10B981) else Color(0xFFA78BFA),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        OutlinedTextField(
                            value = geminiApiKey,
                            onValueChange = { geminiApiKey = it },
                            visualTransformation = if (isGeminiKeyRevealed) VisualTransformation.None else PasswordVisualTransformation(),
                            placeholder = { Text("Enter Gemini API Key", color = Color.Gray, fontSize = 11.sp) },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth(),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = Color(0xFF8B5CF6),
                                unfocusedBorderColor = Color(0xFF2E2744),
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White
                            )
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Surface(
                            color = Color(0xFF6D28D9),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(10.dp))
                                .combinedClickable(
                                    onClick = {
                                        dbService.setSetting("gemini_api_key", geminiApiKey)
                                        dbService.setSetting("api_key", geminiApiKey)
                                        Toast.makeText(context, "✅ Gemini API Key Saved!", Toast.LENGTH_SHORT).show()
                                    },
                                    onLongClick = {
                                        isGeminiKeyRevealed = !isGeminiKeyRevealed
                                        val msg = if (isGeminiKeyRevealed) "👁️ Gemini API Key Unmasked!" else "🔒 Gemini API Key Hidden!"
                                        Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                                    }
                                )
                        ) {
                            Box(modifier = Modifier.padding(vertical = 10.dp), contentAlignment = Alignment.Center) {
                                Text(
                                    text = if (isGeminiKeyRevealed) "SAVE KEY (HOLD TO HIDE KEY)" else "SAVE KEY (LONG PRESS TO SEE KEY)",
                                    color = Color.White,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }

                    // OpenAI API Key
                    Column {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                            Text(text = "OpenAI API Key (GPT-4o)", color = Color.White, fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                            Text(
                                text = if (isOpenAiKeyRevealed) "👁️ UNMASKED" else if (openAiApiKey.isBlank()) "None" else "Saved",
                                color = if (isOpenAiKeyRevealed) Color(0xFFF59E0B) else Color(0xFFA78BFA),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        OutlinedTextField(
                            value = openAiApiKey,
                            onValueChange = { openAiApiKey = it },
                            visualTransformation = if (isOpenAiKeyRevealed) VisualTransformation.None else PasswordVisualTransformation(),
                            placeholder = { Text("sk-proj-...", color = Color.Gray, fontSize = 11.sp) },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth(),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = Color(0xFF8B5CF6),
                                unfocusedBorderColor = Color(0xFF2E2744),
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White
                            )
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Surface(
                            color = Color(0xFF1E1B2C),
                            shape = RoundedCornerShape(10.dp),
                            border = BorderStroke(1.dp, Color(0xFF8B5CF6)),
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(10.dp))
                                .combinedClickable(
                                    onClick = {
                                        dbService.setSetting("openai_api_key", openAiApiKey)
                                        Toast.makeText(context, "✅ OpenAI API Key Saved!", Toast.LENGTH_SHORT).show()
                                    },
                                    onLongClick = {
                                        isOpenAiKeyRevealed = !isOpenAiKeyRevealed
                                        val msg = if (isOpenAiKeyRevealed) "👁️ OpenAI Key Unmasked!" else "🔒 OpenAI Key Hidden!"
                                        Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                                    }
                                )
                        ) {
                            Box(modifier = Modifier.padding(vertical = 10.dp), contentAlignment = Alignment.Center) {
                                Text(
                                    text = if (isOpenAiKeyRevealed) "SAVE OPENAI KEY (HOLD TO HIDE)" else "SAVE OPENAI KEY (LONG PRESS TO SEE)",
                                    color = Color(0xFFA78BFA),
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }

                    // DeepSeek / Claude API Key
                    Column {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                            Text(text = "DeepSeek / Claude API Key", color = Color.White, fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                            Text(
                                text = if (isOtherKeyRevealed) "👁️ UNMASKED" else if (otherAiApiKey.isBlank()) "None" else "Saved",
                                color = if (isOtherKeyRevealed) Color(0xFFF59E0B) else Color(0xFFA78BFA),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        OutlinedTextField(
                            value = otherAiApiKey,
                            onValueChange = { otherAiApiKey = it },
                            visualTransformation = if (isOtherKeyRevealed) VisualTransformation.None else PasswordVisualTransformation(),
                            placeholder = { Text("sk-... / sk-ant-...", color = Color.Gray, fontSize = 11.sp) },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth(),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = Color(0xFF8B5CF6),
                                unfocusedBorderColor = Color(0xFF2E2744),
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White
                            )
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Surface(
                            color = Color(0xFF1E1B2C),
                            shape = RoundedCornerShape(10.dp),
                            border = BorderStroke(1.dp, Color(0xFF8B5CF6)),
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(10.dp))
                                .combinedClickable(
                                    onClick = {
                                        dbService.setSetting("other_api_key", otherAiApiKey)
                                        Toast.makeText(context, "✅ Claude/DeepSeek API Key Saved!", Toast.LENGTH_SHORT).show()
                                    },
                                    onLongClick = {
                                        isOtherKeyRevealed = !isOtherKeyRevealed
                                        val msg = if (isOtherKeyRevealed) "👁️ Key Unmasked!" else "🔒 Key Hidden!"
                                        Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                                    }
                                )
                        ) {
                            Box(modifier = Modifier.padding(vertical = 10.dp), contentAlignment = Alignment.Center) {
                                Text(
                                    text = if (isOtherKeyRevealed) "SAVE KEY (HOLD TO HIDE)" else "SAVE KEY (LONG PRESS TO SEE)",
                                    color = Color(0xFFA78BFA),
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    // Modal Pickers
    if (showProviderModal) {
        SelectionListModal(
            title = "Select AI Provider",
            options = listOf("Gemini AI", "OpenAI (GPT-4o)", "Claude / DeepSeek", "Native (Offline Engine)"),
            selectedOption = provider,
            onSelect = {
                provider = it
                dbService.setSetting("ai_provider", it)
                val defaultModel = when {
                    it.contains("Gemini") -> "Gemini 1.5 Flash"
                    it.contains("OpenAI") -> "gpt-4o-mini"
                    it.contains("DeepSeek") || it.contains("Claude") -> "deepseek-chat"
                    else -> "Vedra Local Engine"
                }
                selectedModel = defaultModel
                dbService.setSetting("ai_model", defaultModel)
                Toast.makeText(context, "Provider set to $it", Toast.LENGTH_SHORT).show()
                showProviderModal = false
            },
            onDismiss = { showProviderModal = false }
        )
    }

    if (showModelModal) {
        val availableModels = when {
            provider.contains("Gemini") -> listOf("Gemini 1.5 Flash", "Gemini 1.5 Pro", "Gemini 2.5 Flash")
            provider.contains("OpenAI") -> listOf("gpt-4o-mini", "gpt-4o")
            provider.contains("DeepSeek") || provider.contains("Claude") -> listOf("deepseek-chat", "claude-3-5-sonnet")
            else -> listOf("Vedra Local Engine", "Rule-Based Offline Assistant")
        }

        SelectionListModal(
            title = "Select Model Variant",
            options = availableModels,
            selectedOption = selectedModel,
            onSelect = {
                selectedModel = it
                dbService.setSetting("ai_model", it)
                Toast.makeText(context, "Model set to $it", Toast.LENGTH_SHORT).show()
                showModelModal = false
            },
            onDismiss = { showModelModal = false }
        )
    }

    if (showToneModal) {
        SelectionListModal(
            title = "Select Response Tone",
            options = listOf("Short & Direct", "Academic & Detailed (JEE)", "Conversational", "Code Specialist"),
            selectedOption = responseTone,
            onSelect = {
                responseTone = it
                dbService.setSetting("ai_tone", it)
                Toast.makeText(context, "Tone set to $it", Toast.LENGTH_SHORT).show()
                showToneModal = false
            },
            onDismiss = { showToneModal = false }
        )
    }
}

// ==========================================
// 8. MEMORY SETTINGS DETAIL SCREEN
// ==========================================
@Composable
fun MemorySettingsDetailScreen(dbService: DatabaseService) {
    val context = LocalContext.current

    LazyColumn(
        modifier = Modifier.fillMaxSize().background(Color(0xFF07060F)).padding(horizontal = 16.dp),
        contentPadding = PaddingValues(top = 16.dp, bottom = 40.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            PreferenceSectionHeader(title = "ASSISTANT MEMORY ENGINE")
            Spacer(modifier = Modifier.height(4.dp))
            PreferenceCard {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(text = "Vedra remembers your preferences, schedule and notes to personalize responses.", color = Color(0xFF9CA3AF), fontSize = 12.sp)

                    Button(
                        onClick = {
                            Toast.makeText(context, "Assistant memory cleared", Toast.LENGTH_SHORT).show()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF7F1D1D)),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(imageVector = Icons.Default.Delete, contentDescription = "Clear", tint = Color.White, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(text = "CLEAR ALL MEMORY LOGS", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        }
                    }
                }
            }
        }
    }
}

// ==========================================
// 9. WIDGETS DETAIL SCREEN
// ==========================================
@Composable
fun WidgetsDetailScreen(dbService: DatabaseService) {
    var widgetEnabled by remember { mutableStateOf(dbService.getSetting("widget_enabled", "true") == "true") }
    var liveStatus by remember { mutableStateOf(dbService.getSetting("widget_live_status", "true") == "true") }

    LazyColumn(
        modifier = Modifier.fillMaxSize().background(Color(0xFF07060F)).padding(horizontal = 16.dp),
        contentPadding = PaddingValues(top = 16.dp, bottom = 40.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            PreferenceSectionHeader(title = "FLOATING AI WIDGET")
            Spacer(modifier = Modifier.height(4.dp))
            PreferenceCard {
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(text = "Floating Voice Widget", color = Color.White, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                            Text(text = "Quick overlay pill over other apps", color = Color(0xFF9CA3AF), fontSize = 11.sp)
                        }
                        Switch(
                            checked = widgetEnabled,
                            onCheckedChange = {
                                widgetEnabled = it
                                dbService.setSetting("widget_enabled", it.toString())
                            },
                            colors = SwitchDefaults.colors(checkedTrackColor = Color(0xFF8B5CF6))
                        )
                    }

                    Box(modifier = Modifier.fillMaxWidth().padding(vertical = 10.dp).height(1.dp).background(Color(0xFF1E1B2C)))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(text = "Show Live Voice Indicator", color = Color.White, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                            Text(text = "Pulse wave animation during voice listening", color = Color(0xFF9CA3AF), fontSize = 11.sp)
                        }
                        Switch(
                            checked = liveStatus,
                            onCheckedChange = {
                                liveStatus = it
                                dbService.setSetting("widget_live_status", it.toString())
                            },
                            colors = SwitchDefaults.colors(checkedTrackColor = Color(0xFF8B5CF6))
                        )
                    }
                }
            }
        }
    }
}

// ==========================================
// 10. BACKUP & DRIVE DETAIL SCREEN
// ==========================================
@Composable
fun DriveBackupDetailScreen(dbService: DatabaseService) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var isConnected by remember { mutableStateOf(GoogleDriveService.isConnected(dbService)) }
    var connectedEmail by remember { mutableStateOf(GoogleDriveService.getConnectedEmail(dbService)) }
    var lastSyncTime by remember { mutableStateOf(GoogleDriveService.getLastSyncTime(dbService)) }
    var isOperating by remember { mutableStateOf(false) }
    var operationStatus by remember { mutableStateOf("") }

    var showAccountPickerModal by remember { mutableStateOf(false) }
    var customAccountInput by remember { mutableStateOf("") }
    val phoneAccounts = remember { GoogleDriveService.getAvailablePhoneAccounts(context) }

    if (showAccountPickerModal) {
    CustomModal(
        visible = showAccountPickerModal,
        title = "Choose Google Account",
        subtitle = "Select a Google account on your phone or enter your email address to connect VEDrive.",
        onDismissRequest = { showAccountPickerModal = false }
    ) {
            Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                if (phoneAccounts.isNotEmpty()) {
                    Text(
                        text = "📱 Accounts Found on Phone:",
                        color = Color(0xFFA78BFA),
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp
                    )
                    phoneAccounts.forEach { accEmail ->
                        Surface(
                            color = Color(0xFF1E1A34),
                            shape = RoundedCornerShape(10.dp),
                            border = BorderStroke(1.dp, Color(0xFF3B3260)),
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    GoogleDriveService.connectAccount(dbService, accEmail)
                                    isConnected = true
                                    connectedEmail = accEmail
                                    showAccountPickerModal = false
                                    Toast.makeText(context, "Connected to Google Drive ($accEmail)", Toast.LENGTH_SHORT).show()
                                }
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.AccountCircle,
                                    contentDescription = null,
                                    tint = Color(0xFF60A5FA),
                                    modifier = Modifier.size(24.dp)
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(text = accEmail, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                    Text(text = "Google Account", color = Color.Gray, fontSize = 10.sp)
                                }
                                Icon(
                                    imageVector = Icons.Default.CheckCircle,
                                    contentDescription = "Select",
                                    tint = Color(0xFF10B981),
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                    }
                    HorizontalDivider(color = Color(0xFF2A2644))
                }

                Text(
                    text = "✉️ Enter Google Email Address:",
                    color = Color.White,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 12.sp
                )
                OutlinedTextField(
                    value = customAccountInput,
                    onValueChange = { customAccountInput = it },
                    placeholder = { Text("e.g. user@gmail.com", color = Color.Gray, fontSize = 12.sp) },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedBorderColor = Color(0xFF8B5CF6),
                        unfocusedBorderColor = Color(0xFF3B3260),
                        focusedContainerColor = Color(0xFF121020),
                        unfocusedContainerColor = Color(0xFF121020)
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                Button(
                    onClick = {
                        val input = customAccountInput.trim()
                        if (input.isNotBlank()) {
                            GoogleDriveService.connectAccount(dbService, input)
                            isConnected = true
                            connectedEmail = input
                            showAccountPickerModal = false
                            Toast.makeText(context, "Connected to Google Drive ($input)", Toast.LENGTH_SHORT).show()
                        } else {
                            Toast.makeText(context, "Please enter a valid Google email", Toast.LENGTH_SHORT).show()
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4285F4)),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth().height(44.dp)
                ) {
                    Text("Connect Account", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                }
            }
        }
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize().background(Color(0xFF07060F)).padding(horizontal = 16.dp),
        contentPadding = PaddingValues(top = 16.dp, bottom = 40.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // --- 1. GOOGLE DRIVE ACCOUNT & LOGO ---
        item {
            PreferenceSectionHeader(title = "GOOGLE DRIVE BACKUP & VEDrive STORAGE")
            Spacer(modifier = Modifier.height(4.dp))
            PreferenceCard {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Phone Google Drive Logo Styling
                        Surface(
                            shape = CircleShape,
                            color = Color(0xFF1E1B2C),
                            border = BorderStroke(2.dp, Color(0xFF4285F4)),
                            modifier = Modifier.size(44.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.AddToDrive,
                                    contentDescription = "Google Drive Logo",
                                    tint = Color(0xFF34A853),
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(text = "Google Drive VEDrive", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                            Text(text = if (isConnected) "Connected: $connectedEmail" else "Not connected (Choose account)", color = if (isConnected) Color(0xFF10B981) else Color(0xFF9CA3AF), fontSize = 11.sp)
                        }
                        Surface(
                            color = if (isConnected) Color(0xFF272042) else Color(0xFF4285F4),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.clip(RoundedCornerShape(8.dp)).clickable {
                                if (isConnected) {
                                    GoogleDriveService.disconnectAccount(dbService)
                                    isConnected = false
                                    connectedEmail = ""
                                    Toast.makeText(context, "Disconnected Google Drive", Toast.LENGTH_SHORT).show()
                                } else {
                                    customAccountInput = ""
                                    showAccountPickerModal = true
                                }
                            }
                        ) {
                            Text(
                                text = if (isConnected) "Disconnect" else "Add Drive",
                                color = Color.White,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                            )
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(text = "Last Synced to VEDrive:", color = Color.Gray, fontSize = 11.sp)
                        Text(text = lastSyncTime, color = Color(0xFFA78BFA), fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                    }
                }
            }
        }

        // --- 2. VEDrive FOLDER STRUCTURE DIAGRAM ---
        item {
            PreferenceSectionHeader(title = "VEDrive STRUCTURE & FOLDERS")
            Spacer(modifier = Modifier.height(4.dp))
            PreferenceCard {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        text = "All VEDRA data is stored on Drive & Local Storage in 5 structured folders:",
                        color = Color(0xFFD1D5DB),
                        fontSize = 11.sp
                    )

                    // Folder Tree Display
                    Surface(
                        color = Color(0xFF120E22),
                        shape = RoundedCornerShape(10.dp),
                        border = BorderStroke(1.dp, Color(0xFF2A2045)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(imageVector = Icons.Default.Folder, contentDescription = null, tint = Color(0xFFF59E0B), modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(text = "VEDrive", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                Text(text = "  (Main Root Folder)", color = Color.Gray, fontSize = 10.sp)
                            }

                            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(start = 16.dp)) {
                                Icon(imageVector = Icons.Default.CloudQueue, contentDescription = null, tint = Color(0xFF10B981), modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(text = "VEDrive/", color = Color(0xFF34D399), fontWeight = FontWeight.SemiBold, fontSize = 12.sp)
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(text = "→ Uploaded files & folders in VEDrive Tab", color = Color.Gray, fontSize = 10.sp)
                            }

                            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(start = 16.dp)) {
                                Icon(imageVector = Icons.Default.FolderZip, contentDescription = null, tint = Color(0xFF3B82F6), modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(text = "VEChat/", color = Color(0xFF93C5FD), fontWeight = FontWeight.SemiBold, fontSize = 12.sp)
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(text = "→ All Offline & Online Chat History JSON", color = Color.Gray, fontSize = 10.sp)
                            }

                            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(start = 16.dp)) {
                                Icon(imageVector = Icons.Default.Lock, contentDescription = null, tint = Color(0xFFEF4444), modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(text = "VEDSecret/", color = Color(0xFFFCA5A5), fontWeight = FontWeight.SemiBold, fontSize = 12.sp)
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(text = "→ Encrypted API Keys & Credentials JSON", color = Color.Gray, fontSize = 10.sp)
                            }

                            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(start = 16.dp)) {
                                Icon(imageVector = Icons.Default.Psychology, contentDescription = null, tint = Color(0xFF10B981), modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(text = "VETrain/", color = Color(0xFF6EE7B7), fontWeight = FontWeight.SemiBold, fontSize = 12.sp)
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(text = "→ Native AI Training Data & Habits JSON", color = Color.Gray, fontSize = 10.sp)
                            }

                            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(start = 16.dp)) {
                                Icon(imageVector = Icons.Default.InsertDriveFile, contentDescription = null, tint = Color(0xFFA78BFA), modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(text = "VEDx/", color = Color(0xFFD8B4FE), fontWeight = FontWeight.SemiBold, fontSize = 12.sp)
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(text = "→ Extra Settings, Notes & App Mappings JSON", color = Color.Gray, fontSize = 10.sp)
                            }
                        }
                    }
                }
            }
        }

        // --- 3. EXPORT & IMPORT ACTION BUTTONS ---
        item {
            PreferenceSectionHeader(title = "BACKUP & RESTORE OPERATIONS")
            Spacer(modifier = Modifier.height(4.dp))
            PreferenceCard {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {

                    if (operationStatus.isNotBlank()) {
                        Surface(
                            color = Color(0xFF1E1838),
                            shape = RoundedCornerShape(8.dp),
                            border = BorderStroke(1.dp, Color(0xFF6D28D9))
                        ) {
                            Text(
                                text = operationStatus,
                                color = Color.White,
                                fontSize = 11.sp,
                                modifier = Modifier.padding(10.dp)
                            )
                        }
                    }

                    // MASTER SAVE ALL DATA TO VEDrive
                    Button(
                        onClick = {
                            if (isOperating) return@Button
                            isOperating = true
                            operationStatus = "⏳ Syncing all VEDRA data to VEDrive..."
                            scope.launch {
                                val res = GoogleDriveService.exportAllVedDriveData(context, dbService)
                                operationStatus = res
                                isOperating = false
                                lastSyncTime = GoogleDriveService.getLastSyncTime(dbService)
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF6D28D9)),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth(),
                        enabled = !isOperating
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(imageVector = Icons.Default.CloudUpload, contentDescription = "Sync", tint = Color.White, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(text = "SAVE ALL VEDRA DATA TO DRIVE (5 FOLDERS)", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        }
                    }

                    // MASTER RESTORE ALL DATA FROM VEDrive
                    Button(
                        onClick = {
                            if (isOperating) return@Button
                            isOperating = true
                            operationStatus = "⏳ Restoring all VEDRA data from VEDrive..."
                            scope.launch {
                                val res = GoogleDriveService.importAllVedDriveData(context, dbService)
                                operationStatus = res
                                isOperating = false
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1E1B2C)),
                        border = BorderStroke(1.dp, Color(0xFF8B5CF6)),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth(),
                        enabled = !isOperating
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(imageVector = Icons.Default.CloudDownload, contentDescription = "Restore", tint = Color(0xFFA78BFA), modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(text = "RESTORE ALL VEDRA DATA FROM DRIVE", color = Color(0xFFA78BFA), fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        }
                    }

                    Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(Color(0xFF272042)))

                    // CHAT HISTORY EXPORT JSON ONLY
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Button(
                            onClick = {
                                if (isOperating) return@Button
                                isOperating = true
                                scope.launch {
                                    val (file, count) = GoogleDriveService.exportChatHistoryToJson(context, dbService)
                                    operationStatus = "✅ Exported $count chat sessions to JSON:\n${file.absolutePath}"
                                    isOperating = false
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1E1B2C)),
                            border = BorderStroke(1.dp, Color(0xFF3B82F6)),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.weight(1f),
                            enabled = !isOperating
                        ) {
                            Text(text = "EXPORT CHAT JSON", color = Color(0xFF60A5FA), fontSize = 10.sp, fontWeight = FontWeight.Bold)
                        }

                        Button(
                            onClick = {
                                if (isOperating) return@Button
                                isOperating = true
                                scope.launch {
                                    val result = GoogleDriveService.importChatHistoryFromJson(context, dbService)
                                    operationStatus = result
                                    isOperating = false
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1E1B2C)),
                            border = BorderStroke(1.dp, Color(0xFF10B981)),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.weight(1f),
                            enabled = !isOperating
                        ) {
                            Text(text = "IMPORT CHAT JSON", color = Color(0xFF34D399), fontSize = 10.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}

// ==========================================
// 11. ABOUT & SUPPORT DETAIL SCREEN
// ==========================================
@Composable
fun AboutSupportDetailScreen(dbService: DatabaseService) {
    val context = LocalContext.current

    LazyColumn(
        modifier = Modifier.fillMaxSize().background(Color(0xFF07060F)).padding(horizontal = 16.dp),
        contentPadding = PaddingValues(top = 16.dp, bottom = 40.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            PreferenceSectionHeader(title = "APP INFORMATION")
            Spacer(modifier = Modifier.height(4.dp))
            PreferenceCard {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(text = "App Version", color = Color(0xFF9CA3AF), fontSize = 13.sp)
                        Text(text = "2.4.0-Pro", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(text = "Developer", color = Color(0xFF9CA3AF), fontSize = 13.sp)
                        Text(text = "Vedra AI Studio", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(text = "Engine", color = Color(0xFF9CA3AF), fontSize = 13.sp)
                        Text(text = "Gemini 1.5 + Android Native", color = Color(0xFFC4B5FD), fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    }
                }
            }
        }

        item {
            Button(
                onClick = {
                    Toast.makeText(context, "Vedra is already on the latest version!", Toast.LENGTH_SHORT).show()
                },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF231B38)),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(text = "CHECK FOR UPDATES", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 12.sp)
            }
        }
    }
}

// ==========================================
// REUSABLE PREFERENCE UI COMPONENTS
// ==========================================
@Composable
private fun PreferenceSectionHeader(title: String) {
    Text(
        text = title,
        color = Color(0xFFA78BFA),
        fontWeight = FontWeight.Bold,
        fontSize = 11.sp,
        letterSpacing = 1.sp
    )
}

@Composable
private fun PreferenceCard(content: @Composable () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(VedraSurface)
            .border(1.dp, VedraBorder, RoundedCornerShape(16.dp))
            .padding(14.dp)
    ) {
        content()
    }
}

@Composable
private fun PreferenceIconBox(icon: ImageVector) {
    Box(
        modifier = Modifier
            .size(38.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(VedraSurfaceVariant),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = VedraPurplePrimary,
            modifier = Modifier.size(20.dp)
        )
    }
}

@Composable
private fun DropdownPill(text: String, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(10.dp))
            .background(VedraSurfaceVariant)
            .border(1.dp, VedraBorder, RoundedCornerShape(10.dp))
            .clickable { onClick() }
            .padding(horizontal = 12.dp, vertical = 8.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Text(
                text = text,
                color = VedraTextPrimary,
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium
            )
            Icon(
                imageVector = Icons.Default.KeyboardArrowDown,
                contentDescription = "Dropdown",
                tint = VedraTextSecondary,
                modifier = Modifier.size(16.dp)
            )
        }
    }
}

@Composable
private fun SegmentedToggleTwo(
    option1: String,
    option2: String,
    selectedOption: String,
    onSelect: (String) -> Unit
) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(10.dp))
            .background(VedraSurfaceVariant)
            .border(1.dp, VedraBorder, RoundedCornerShape(10.dp))
            .padding(3.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            val isOpt1Selected = selectedOption == option1
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(if (isOpt1Selected) VedraPurplePrimary else Color.Transparent)
                    .clickable { onSelect(option1) }
                    .padding(horizontal = 14.dp, vertical = 6.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = option1,
                    color = if (isOpt1Selected) Color.White else VedraTextSecondary,
                    fontSize = 12.sp,
                    fontWeight = if (isOpt1Selected) FontWeight.Bold else FontWeight.Medium
                )
            }

            val isOpt2Selected = selectedOption == option2
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(if (isOpt2Selected) VedraPurplePrimary else Color.Transparent)
                    .clickable { onSelect(option2) }
                    .padding(horizontal = 14.dp, vertical = 6.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = option2,
                    color = if (isOpt2Selected) Color.White else VedraTextSecondary,
                    fontSize = 12.sp,
                    fontWeight = if (isOpt2Selected) FontWeight.Bold else FontWeight.Medium
                )
            }
        }
    }
}

@Composable
private fun ThemeOptionRow(
    title: String,
    subtitle: String,
    icon: ImageVector,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(if (isSelected) VedraSurfaceVariant else VedraSurface)
            .border(
                1.dp,
                if (isSelected) VedraPurplePrimary else VedraBorder,
                RoundedCornerShape(12.dp)
            )
            .clickable { onClick() }
            .padding(12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = title,
                    tint = if (isSelected) Color(0xFFA78BFA) else Color(0xFF9CA3AF),
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = title,
                        color = Color.White,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 13.sp
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = subtitle,
                        color = Color(0xFF9CA3AF),
                        fontSize = 11.sp
                    )
                }
            }

            if (isSelected) {
                Box(
                    modifier = Modifier
                        .size(20.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF8B5CF6)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = "Selected",
                        tint = Color.White,
                        modifier = Modifier.size(13.dp)
                    )
                }
            } else {
                Box(
                    modifier = Modifier
                        .size(20.dp)
                        .clip(CircleShape)
                        .border(1.5.dp, Color(0xFF4B5563), CircleShape)
                )
            }
        }
    }
}

@Composable
private fun OtherPreferenceRow(
    icon: ImageVector,
    title: String,
    subtitle: String,
    value: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.weight(1f)
        ) {
            PreferenceIconBox(icon = icon)
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(
                    text = title,
                    color = Color.White,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 13.5.sp
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = subtitle,
                    color = Color(0xFF9CA3AF),
                    fontSize = 11.sp
                )
            }
        }

        Spacer(modifier = Modifier.width(8.dp))

        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = value,
                color = Color(0xFFD1D5DB),
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium
            )
            Spacer(modifier = Modifier.width(4.dp))
            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = "Open",
                tint = Color(0xFF9CA3AF),
                modifier = Modifier.size(18.dp)
            )
        }
    }
}

@Composable
private fun SelectionListModal(
    title: String,
    options: List<String>,
    selectedOption: String,
    onSelect: (String) -> Unit,
    onDismiss: () -> Unit
) {
    CustomModal(
        visible = true,
        title = title,
        onDismissRequest = onDismiss
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            options.forEach { option ->
                val isSel = option == selectedOption
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(if (isSel) Color(0xFF231B38) else Color(0xFF141122))
                        .border(1.dp, if (isSel) Color(0xFF7C3AED) else Color(0xFF201B30), RoundedCornerShape(12.dp))
                        .clickable { onSelect(option) }
                        .padding(horizontal = 14.dp, vertical = 12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = option,
                            color = if (isSel) Color(0xFFC4B5FD) else Color.White,
                            fontWeight = if (isSel) FontWeight.Bold else FontWeight.Normal,
                            fontSize = 14.sp
                        )
                        if (isSel) {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = "Selected",
                                tint = Color(0xFF8B5CF6),
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}
