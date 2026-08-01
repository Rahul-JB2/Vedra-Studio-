#!/bin/bash
cat << 'KOTLIN' > app/src/main/java/com/example/ui/screens/SettingsScreen.kt
package com.example.ui.screens

import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
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
import com.example.services.DatabaseService
import com.example.services.VoiceService
import com.example.ui.screens.settings.*
import com.example.ui.theme.*

@Composable
fun SettingsScreen(dbService: DatabaseService, voiceService: VoiceService) {
    var activeSubScreen by remember { mutableStateOf<String?>(null) }

    BackHandler(enabled = activeSubScreen != null) {
        activeSubScreen = null
    }

    if (activeSubScreen == null) {
        SettingsMainScreen(onNavigate = { activeSubScreen = it })
    } else {
        Column(modifier = Modifier.fillMaxSize().background(VedraBackground)) {
            // Header for sub-screen
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = { activeSubScreen = null }) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
                }
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = activeSubScreen ?: "",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                )
            }
            // Content
            Box(modifier = Modifier.weight(1f)) {
                when (activeSubScreen) {
                    "General Preferences" -> GeneralPreferencesDetailScreen(dbService = dbService)
                    "Voice & Speech" -> VoiceSpeechDetailScreen(dbService = dbService, voiceService = voiceService)
                    "Appearance" -> AppearanceDetailScreen(dbService = dbService)
                    "Home Screen" -> HomeScreenDetailScreen(dbService = dbService)
                    "Notifications" -> NotificationsDetailScreen(dbService = dbService)
                    "Sound & Vibration" -> SoundVibrationDetailScreen(dbService = dbService)
                    "AI Settings" -> AiSettingsDetailScreen(dbService = dbService)
                    "Memory & Knowledge" -> MemorySettingsDetailScreen(dbService = dbService)
                    "Widgets & Overlays" -> WidgetsDetailScreen(dbService = dbService)
                    "Data & Storage" -> DriveBackupDetailScreen(dbService = dbService)
                    "About VEDRA" -> AboutSupportDetailScreen(dbService = dbService)
                    else -> Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text("Screen not implemented yet", color = Color.Gray)
                    }
                }
            }
        }
    }
}

@Composable
fun SettingsMainScreen(onNavigate: (String) -> Unit) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(VedraBackground)
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(top = 16.dp, bottom = 100.dp)
    ) {
        // VEDRA Pro Banner
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color(0xFF141122))
                    .border(1.dp, Color(0xFF2E1B4E), RoundedCornerShape(16.dp))
                    .padding(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier.size(40.dp).clip(CircleShape).border(1.dp, Color(0xFF6B21A8), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.GraphicEq, contentDescription = null, tint = VedraPurplePrimary)
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text("Vedra Pro", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                                Spacer(modifier = Modifier.width(8.dp))
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(4.dp))
                                        .background(VedraPurplePrimary)
                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                ) {
                                    Text("★ Active", color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                            Spacer(modifier = Modifier.height(2.dp))
                            Text("You have access to all premium features.", color = VedraTextMuted, fontSize = 12.sp)
                        }
                    }
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(20.dp))
                            .background(Color(0xFF231B38))
                            .padding(horizontal = 12.dp, vertical = 6.dp)
                            .clickable { /* Manage Sub */ }
                    ) {
                        Text("Manage Subscription >", color = Color.White, fontSize = 10.sp)
                    }
                }
            }
            Spacer(modifier = Modifier.height(24.dp))
        }

        // PREFERENCES
        item {
            SettingsCategoryGroup(
                title = "PREFERENCES",
                items = listOf(
                    SettingsItemData("General Preferences", "Language, theme, units, font size and more", Icons.Default.Palette, Color(0xFF6366F1)),
                    SettingsItemData("Voice & Speech", "Voice model, speed, wake word, speaking style", Icons.Default.Mic, Color(0xFFEC4899)),
                    SettingsItemData("Appearance", "Theme, colors, animations, chat bubbles", Icons.Default.Brush, Color(0xFF3B82F6)),
                    SettingsItemData("Home Screen", "Customize home layout, suggestions & quick actions", Icons.Default.Home, Color(0xFF10B981)),
                    SettingsItemData("Notifications", "Manage notifications, alerts and reminders", Icons.Default.Notifications, Color(0xFFF59E0B)),
                    SettingsItemData("Sound & Vibration", "Assistant sounds, vibration and silent mode", Icons.Default.VolumeUp, Color(0xFF14B8A6))
                ),
                onNavigate = onNavigate
            )
            Spacer(modifier = Modifier.height(24.dp))
        }

        // AI & DATA
        item {
            SettingsCategoryGroup(
                title = "AI & DATA",
                items = listOf(
                    SettingsItemData("AI Settings", "Model, response style, creativity level", Icons.Default.Psychology, Color(0xFF8B5CF6)),
                    SettingsItemData("Memory & Knowledge", "Manage memory, data usage and context", Icons.Default.Storage, Color(0xFF0EA5E9)),
                    SettingsItemData("Data & Storage", "Cache, local files, storage usage", Icons.Default.CloudSync, Color(0xFF10B981)),
                    SettingsItemData("Privacy & Security", "App lock, data privacy and permissions", Icons.Default.Security, Color(0xFF8B5CF6))
                ),
                onNavigate = onNavigate
            )
            Spacer(modifier = Modifier.height(24.dp))
        }

        // ACCOUNT
        item {
            SettingsCategoryGroup(
                title = "ACCOUNT",
                items = listOf(
                    SettingsItemData("Account Profile", "Personal information, avatar, account details", Icons.Default.Person, Color(0xFF8B5CF6)),
                    SettingsItemData("Billing & Subscription", "Payment methods, history and invoices", Icons.Default.CreditCard, Color(0xFF3B82F6)),
                    SettingsItemData("About VEDRA", "Version 2.3.1 • Built with ❤️ for students", Icons.Default.GraphicEq, Color(0xFF8B5CF6))
                ),
                onNavigate = onNavigate
            )
        }
    }
}

data class SettingsItemData(val title: String, val subtitle: String, val icon: ImageVector, val iconTint: Color)

@Composable
fun SettingsCategoryGroup(title: String, items: List<SettingsItemData>, onNavigate: (String) -> Unit) {
    Column {
        Text(
            text = title,
            color = VedraPurplePrimary,
            fontWeight = FontWeight.Bold,
            fontSize = 11.sp,
            letterSpacing = 1.sp,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(Color(0xFF141122))
        ) {
            Column {
                items.forEachIndexed { index, item ->
                    SettingsItemRow(item, onClick = { onNavigate(item.title) })
                    if (index < items.size - 1) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp)
                                .height(1.dp)
                                .background(Color(0xFF1E1B2C))
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun SettingsItemRow(item: SettingsItemData, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(CircleShape)
                .background(item.iconTint.copy(alpha = 0.15f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(item.icon, contentDescription = null, tint = item.iconTint, modifier = Modifier.size(20.dp))
        }
        Spacer(modifier = Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(item.title, color = Color.White, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
            Spacer(modifier = Modifier.height(2.dp))
            Text(item.subtitle, color = VedraTextMuted, fontSize = 11.sp)
        }
        Icon(Icons.Default.ChevronRight, contentDescription = null, tint = VedraTextMuted, modifier = Modifier.size(20.dp))
    }
}
KOTLIN
