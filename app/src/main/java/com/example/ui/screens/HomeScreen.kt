package com.example.ui.screens

import android.content.Context
import android.graphics.Bitmap
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColor
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Density
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import com.example.ui.theme.LocalGlassmorphismTint
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.graphics.drawable.toBitmap
import com.example.services.AppLauncher
import com.example.services.AppPackageManagerHelper
import com.example.services.BatteryStatus
import com.example.services.DatabaseService
import com.example.services.StorageDetails
import com.example.services.StorageWeatherService
import com.example.services.UtilityService
import com.example.services.VoiceService
import com.example.services.OfflineService
import com.example.ui.components.CustomInput
import com.example.ui.components.VedOrbView

data class QuickActionData(
    val id: String,
    val label: String,
    val icon: ImageVector,
    val color: Color,
    val packageName: String? = null
)

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun HomeScreen(
    dbService: DatabaseService,
    voiceService: VoiceService,
    onActivateVoice: () -> Unit,
    onNavigateTab: (Int) -> Unit,
    onExecuteQuickAction: (String) -> Unit,
    onOpenDrawer: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var battery by remember { mutableStateOf(BatteryStatus(85, "Discharging", false)) }
    var storage by remember { mutableStateOf(StorageDetails(3.2, 40.1, 64.0)) }
    val isOnline by OfflineService.isNetworkAvailable.collectAsState()

    var showNotificationsDialog by remember { mutableStateOf(false) }
    var showProfileDialog by remember { mutableStateOf(false) }
    var showAttachmentDialog by remember { mutableStateOf(false) }

    val userName = remember { dbService.getSetting("user_name", "Rahul") }

    // Dynamic Greeting based on time
    val greetingTime = remember {
        val hour = java.util.Calendar.getInstance().get(java.util.Calendar.HOUR_OF_DAY)
        when (hour) {
            in 5..11 -> "Good Morning"
            in 12..16 -> "Good Afternoon"
            in 17..21 -> "Good Evening"
            else -> "Good Night"
        }
    }

    // Dynamic AI Suggestions logic
    val allSuggestions = remember {
        listOf(
            "Summarize today's notes",
            "Explain Quantum Wave-Particle Duality",
            "Practice JEE Physics questions",
            "Generate daily study schedule",
            "Solve Math Integration problem",
            "Review Chemistry Periodic Table",
            "Optimize app memory and storage",
            "Create mind map for Biology"
        )
    }
    var currentSuggestionsOffset by remember { mutableStateOf(0) }
    val activeSuggestions = remember(currentSuggestionsOffset) {
        val start = currentSuggestionsOffset % allSuggestions.size
        val items = mutableListOf<String>()
        for (i in 0 until 4) {
            items.add(allSuggestions[(start + i) % allSuggestions.size])
        }
        items
    }

    // Pinning Quick Actions System
    val initialPinned = remember {
        val saved = dbService.getSetting("pinned_quick_actions", "Chat,Search,PDF")
        saved.split(",").map { it.trim() }.filter { it.isNotEmpty() }.toSet()
    }
    val pinnedActionIds = remember { mutableStateListOf<String>().apply { addAll(initialPinned) } }

    fun togglePinAction(id: String) {
        if (pinnedActionIds.contains(id)) {
            pinnedActionIds.remove(id)
            Toast.makeText(context, "Unpinned '$id'", Toast.LENGTH_SHORT).show()
        } else {
            pinnedActionIds.add(id)
            Toast.makeText(context, "📌 Pinned '$id' to start!", Toast.LENGTH_SHORT).show()
        }
        dbService.setSetting("pinned_quick_actions", pinnedActionIds.joinToString(","))
    }

    fun refreshDashboardData() {
        battery = StorageWeatherService.getBatteryStatus(context)
        storage = StorageWeatherService.getStorageDetails(context)
    }

    LaunchedEffect(Unit) {
        refreshDashboardData()
    }

    val currentDensity = LocalDensity.current
    val customDensity = remember(currentDensity) {
        Density(
            density = currentDensity.density * 0.88f,
            fontScale = currentDensity.fontScale * 0.88f
        )
    }

    CompositionLocalProvider(LocalDensity provides customDensity) {
        Box(
            modifier = modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color(0xFF090714),
                            Color(0xFF0F0B21),
                            Color(0xFF07050E)
                        )
                    )
                )
        ) {
        // Main Scrollable Dashboard Content
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            contentPadding = PaddingValues(top = 8.dp, bottom = 100.dp), // Space for fixed bottom search bar
        ) {
            // 1. Top Bar: ☰ VEDRA                      🔔 ⚙️  👤
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp, bottom = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        IconButton(
                            onClick = {
                                if (onOpenDrawer != null) {
                                    onOpenDrawer()
                                } else {
                                    onExecuteQuickAction("open drawer")
                                }
                            },
                            modifier = Modifier
                                .size(38.dp)
                                .clip(CircleShape)
                                .background(Color(0x20FFFFFF))
                                .border(1.dp, Color(0x30FFFFFF), CircleShape)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Menu,
                                contentDescription = "Menu",
                                tint = Color.White,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        com.example.ui.components.VedMathLogoIconCard(
                            size = 32.dp,
                            animated = true,
                            showBrandText = false
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "VEDRA",
                            color = Color.White,
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 20.sp,
                            letterSpacing = 1.sp
                        )
                    }

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(
                            onClick = { showNotificationsDialog = true },
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(Color(0x15FFFFFF))
                                .border(1.dp, Color(0x25FFFFFF), CircleShape)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Notifications,
                                contentDescription = "Notifications",
                                tint = Color(0xFFC4B5FD),
                                modifier = Modifier.size(18.dp)
                            )
                        }

                        IconButton(
                            onClick = { onNavigateTab(5) }, // Settings tab (index 5)
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(Color(0x15FFFFFF))
                                .border(1.dp, Color(0x25FFFFFF), CircleShape)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Settings,
                                contentDescription = "Settings",
                                tint = Color(0xFFC4B5FD),
                                modifier = Modifier.size(18.dp)
                            )
                        }

                        IconButton(
                            onClick = { showProfileDialog = true },
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(Color(0x308B5CF6))
                                .border(1.dp, Color(0x608B5CF6), CircleShape)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Person,
                                contentDescription = "Profile",
                                tint = Color.White,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }
            }

            // 2. Header Greeting: Good Afternoon, Rahul 👋 | Ambient Light Glass Tint Badge | VED Orb •••••
            item {
                val glassTint = LocalGlassmorphismTint.current
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp, bottom = 18.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "$greetingTime, $userName 👋",
                        color = Color.White,
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold
                    )
                    
                    // Ambient Light Glassmorphism Sensor Chip
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center,
                        modifier = Modifier
                            .padding(top = 6.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color(0x188B5CF6))
                            .border(1.dp, Color(0x35A78BFA), RoundedCornerShape(12.dp))
                            .clickable {
                                Toast.makeText(
                                    context,
                                    "Ambient Light Sensor Active: ${glassTint.ambientLux.toInt()} Lux | System Brightness: ${glassTint.systemBrightness}/255 | Tint Factor: ${String.format("%.2f", glassTint.tintIntensity)}x",
                                    Toast.LENGTH_LONG
                                ).show()
                            }
                            .padding(horizontal = 10.dp, vertical = 4.dp)
                    ) {
                        Icon(
                            imageVector = if (glassTint.ambientLux < 50f) Icons.Default.NightsStay else Icons.Default.WbSunny,
                            contentDescription = "Ambient Light Sensor",
                            tint = if (glassTint.ambientLux < 50f) Color(0xFFC4B5FD) else Color(0xFFFBBF24),
                            modifier = Modifier.size(13.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Ambient Glass Tint: ${glassTint.ambientLux.toInt()} Lux (${String.format("%.2f", glassTint.tintIntensity)}x)",
                            color = Color(0xFFE2E8F0),
                            fontSize = 11.5.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))
                    GlassyCard {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 20.dp, vertical = 14.dp)
                                .clickable { onActivateVoice() },
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            VedOrbView(
                                orbStyle = dbService.getSetting("ved_orb_style", "Ved Purple Energy Orb"),
                                size = 48.dp,
                                isListening = voiceService.isListening.value,
                                isSpeaking = voiceService.isSpeaking.value
                            )
                            Spacer(modifier = Modifier.width(16.dp))
                            Text(
                                text = "VED",
                                color = Color.White,
                                fontSize = 28.sp,
                                fontWeight = FontWeight.Black,
                                letterSpacing = 3.sp
                            )
                            Spacer(modifier = Modifier.width(20.dp))
                            VedraStatusIndicator(
                                isListening = voiceService.isListening.value,
                                isSpeaking = voiceService.isSpeaking.value,
                                isThinking = false
                            )
                        }
                    }
                }
            }

            // 3. Quick Actions (Horizontal scroll with Pin support)
            item {
                Column(modifier = Modifier.fillMaxWidth().padding(top = 16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(start = 4.dp, bottom = 10.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Quick Actions",
                            color = Color(0xFFA78BFA),
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Long-press to Pin 📌",
                            color = Color(0xFF6B7280),
                            fontSize = 10.5.sp
                        )
                    }

                    val allQuickActions = remember {
                        listOf(
                            QuickActionData("Chat", "Chat", Icons.Default.ChatBubble, Color(0xFF3B82F6), "com.whatsapp"),
                            QuickActionData("Search", "Search", Icons.Default.Search, Color(0xFF10B981), "com.android.chrome"),
                            QuickActionData("PDF", "PDF", Icons.Default.PictureAsPdf, Color(0xFFEF4444)),
                            QuickActionData("Image", "Image", Icons.Default.Palette, Color(0xFF8B5CF6)),
                            QuickActionData("Study", "Study", Icons.Default.Psychology, Color(0xFFF59E0B)),
                            QuickActionData("VEDrive", "VEDrive", Icons.Default.Folder, Color(0xFF6366F1), "com.google.android.apps.docs"),
                            QuickActionData("Voice", "Voice", Icons.Default.Mic, Color(0xFFEC4899)),
                            QuickActionData("More", "More", Icons.Default.ElectricBolt, Color(0xFF06B6D4))
                        )
                    }

                    // Sort actions: Pinned ones first!
                    val sortedQuickActions = remember(pinnedActionIds.toList()) {
                        allQuickActions.sortedByDescending { pinnedActionIds.contains(it.id) }
                    }

                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(14.dp),
                        contentPadding = PaddingValues(horizontal = 2.dp)
                    ) {
                        items(sortedQuickActions.size, key = { sortedQuickActions[it].id }) { index ->
                            val action = sortedQuickActions[index]
                            val isPinned = pinnedActionIds.contains(action.id)

                            QuickActionGlassItem(
                                label = action.label,
                                icon = action.icon,
                                color = action.color,
                                packageName = action.packageName,
                                isPinned = isPinned,
                                onLongClick = { togglePinAction(action.id) },
                                onClick = {
                                    when (action.id) {
                                        "Study", "VEHub" -> onNavigateTab(1) // VEHub
                                        "Voice", "Ved" -> onNavigateTab(2) // Ved AI tab
                                        "VEDrive" -> onNavigateTab(3) // VEDrive
                                        "More", "VETools" -> onNavigateTab(4) // VETools
                                        else -> onExecuteQuickAction(action.id.lowercase())
                                    }
                                }
                            )
                        }
                    }
                }
            }

            // 4. Quick Command (Actual Phone App Icons)
            item {
                Column(modifier = Modifier.fillMaxWidth().padding(top = 24.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(start = 4.dp, bottom = 12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Quick Command",
                            color = Color(0xFFA78BFA),
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Phone Apps",
                            color = Color(0xFF6B7280),
                            fontSize = 11.sp
                        )
                    }

                    val commands = listOf(
                        Triple("WhatsApp", Icons.Default.Message, "com.whatsapp"),
                        Triple("Music", Icons.Default.MusicNote, "com.spotify.music"),
                        Triple("Calculator", Icons.Default.Calculate, "com.google.android.calculator"),
                        Triple("Weather", Icons.Default.WbSunny, "com.google.android.apps.weather"),
                        Triple("Camera", Icons.Default.PhotoCamera, "com.google.android.GoogleCamera"),
                        Triple("Torch", Icons.Default.FlashlightOn, "torch"),
                        Triple("YouTube", Icons.Default.PlayCircle, "com.google.android.youtube"),
                        Triple("Call", Icons.Default.Phone, "com.google.android.dialer"),
                        Triple("Clock", Icons.Default.AccessTime, "com.google.android.deskclock"),
                        Triple("Scan", Icons.Default.QrCodeScanner, "scan")
                    )

                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        contentPadding = PaddingValues(horizontal = 2.dp)
                    ) {
                        items(commands.size) { index ->
                            val cmd = commands[index]
                            QuickAppIcon(
                                label = cmd.first,
                                fallbackIcon = cmd.second,
                                packageName = cmd.third
                            ) {
                                if (cmd.third == "torch") {
                                    val res = UtilityService.parseAndExecuteLocalCommand(context, dbService, "toggle torch")
                                    Toast.makeText(context, res.responseMessage, Toast.LENGTH_SHORT).show()
                                } else if (cmd.third == "scan") {
                                    onExecuteQuickAction("scan qr code")
                                } else {
                                    val launched = AppPackageManagerHelper.launchApp(context, cmd.third, cmd.first) || AppLauncher.tryLaunchPackage(context, cmd.third)
                                    if (!launched) {
                                        val res = UtilityService.parseAndExecuteLocalCommand(context, dbService, "open ${cmd.first.lowercase()}")
                                        Toast.makeText(context, res.responseMessage, Toast.LENGTH_SHORT).show()
                                    } else {
                                        Toast.makeText(context, "🚀 Opening ${cmd.first}...", Toast.LENGTH_SHORT).show()
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // 5. Continue Section (Without Resume Research)
            item {
                Column(modifier = Modifier.fillMaxWidth().padding(top = 24.dp)) {
                    Text(
                        text = "Continue",
                        color = Color(0xFFA78BFA),
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(start = 4.dp, bottom = 12.dp)
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        ContinueGlassCard(
                            title = "Last Chat",
                            subtitle = "Quantum AI & Physics Notes",
                            icon = Icons.Default.History,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            onExecuteQuickAction("open last chat")
                        }
                    }
                }
            }

            // 6. Recent Files (Horizontal scroll)
            item {
                Column(modifier = Modifier.fillMaxWidth().padding(top = 24.dp)) {
                    Text(
                        text = "Recent Files",
                        color = Color(0xFFA78BFA),
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(start = 4.dp, bottom = 12.dp)
                    )
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        contentPadding = PaddingValues(horizontal = 2.dp)
                    ) {
                        item {
                            RecentFileGlassCard("Physics.pdf", "2.4 MB", Icons.Default.PictureAsPdf, Color(0xFFEF4444)) {
                                onExecuteQuickAction("open Physics.pdf")
                            }
                        }
                        item {
                            RecentFileGlassCard("Notes.md", "18 KB", Icons.Default.EditNote, Color(0xFF10B981)) {
                                onExecuteQuickAction("open Notes.md")
                            }
                        }
                        item {
                            RecentFileGlassCard("Diagram.png", "1.2 MB", Icons.Default.Image, Color(0xFF8B5CF6)) {
                                onExecuteQuickAction("open Diagram.png")
                            }
                        }
                        item {
                            RecentFileGlassCard("Math_Formulas.pdf", "3.1 MB", Icons.Default.PictureAsPdf, Color(0xFF3B82F6)) {
                                onExecuteQuickAction("open Math_Formulas.pdf")
                            }
                        }
                    }
                }
            }

            // 7. Suggested (Matching Image 1)
            item {
                Column(modifier = Modifier.fillMaxWidth().padding(top = 24.dp)) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(20.dp))
                            .background(Color(0xFF070A18))
                            .border(1.dp, Color(0xFF1E233E), RoundedCornerShape(20.dp))
                            .padding(16.dp)
                    ) {
                        Column {
                            // Top Header
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(bottom = 14.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.AutoAwesome,
                                        contentDescription = "Suggested",
                                        tint = Color(0xFF818CF8),
                                        modifier = Modifier.size(22.dp)
                                    )
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Text(
                                        text = "Suggested",
                                        color = Color.White,
                                        fontSize = 18.sp,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                }

                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(8.dp))
                                        .clickable { currentSuggestionsOffset += 2 }
                                        .padding(horizontal = 6.dp, vertical = 4.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Refresh,
                                        contentDescription = "Refresh",
                                        tint = Color(0xFF818CF8),
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "Refresh",
                                        color = Color(0xFF818CF8),
                                        fontSize = 15.sp,
                                        fontWeight = FontWeight.Medium
                                    )
                                }
                            }

                            // Items Row
                            LazyRow(
                                horizontalArrangement = Arrangement.spacedBy(10.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                item {
                                    SuggestedPillCard(
                                        text = "Summarize today's notes",
                                        dotColor = Color(0xFF10B981)
                                    ) { onExecuteQuickAction("Summarize today's notes") }
                                }
                                item {
                                    SuggestedPillCard(
                                        text = "Explain a concept",
                                        dotColor = Color(0xFFA855F7)
                                    ) { onExecuteQuickAction("Explain a concept") }
                                }
                                item {
                                    SuggestedPillCard(
                                        text = "Practice JEE questions",
                                        dotColor = Color(0xFFF59E0B)
                                    ) { onExecuteQuickAction("Practice JEE questions") }
                                }
                                item {
                                    SuggestedPillCard(
                                        text = "Scan & Solve Physics",
                                        dotColor = Color(0xFF3B82F6)
                                    ) { onExecuteQuickAction("Scan & Solve Physics") }
                                }
                            }
                        }
                    }
                }
            }

            // 8. Quick Status (Matching Image 2)
            item {
                Column(modifier = Modifier.fillMaxWidth().padding(top = 24.dp)) {
                    Text(
                        text = "Quick Status",
                        color = Color.White,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(start = 4.dp, bottom = 14.dp)
                    )

                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        contentPadding = PaddingValues(horizontal = 2.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        // Card 1: Battery
                        item {
                            QuickStatusExactCard(
                                title = "Battery",
                                value = "${battery.percentage}%",
                                statusText = battery.statusText,
                                statusColor = if (battery.percentage > 20) Color(0xFF10B981) else Color(0xFFEF4444),
                                icon = Icons.Default.BatteryChargingFull,
                                iconColor = Color(0xFF10B981),
                                onClick = {
                                    refreshDashboardData()
                                    Toast.makeText(context, "Battery: ${battery.percentage}% (${battery.statusText})", Toast.LENGTH_SHORT).show()
                                }
                            )
                        }

                        // Card 2: Storage
                        item {
                            QuickStatusExactCard(
                                title = "Storage",
                                value = "64%",
                                statusText = "${storage.freeSpaceGB} GB free",
                                statusColor = Color(0xFF60A5FA),
                                icon = Icons.Default.Dns,
                                iconColor = Color(0xFF3B82F6),
                                onClick = {
                                    val msg = StorageWeatherService.clearAppCache(context)
                                    refreshDashboardData()
                                    Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                                }
                            )
                        }

                        // Card 3: Memory
                        item {
                            QuickStatusExactCard(
                                title = "Memory",
                                value = "3.2 GB",
                                statusText = "Free",
                                statusColor = Color(0xFFC084FC),
                                icon = Icons.Default.Memory,
                                iconColor = Color(0xFFA855F7),
                                onClick = {
                                    Toast.makeText(context, "Memory Cleaned! 3.2 GB Free RAM", Toast.LENGTH_SHORT).show()
                                }
                            )
                        }

                        // Card 4: Network
                        item {
                            QuickStatusExactCard(
                                title = "Network",
                                value = if (isOnline) "Airtel 5G" else "Offline",
                                statusText = if (isOnline) "Connected" else "Disconnected",
                                statusColor = if (isOnline) Color(0xFF10B981) else Color(0xFFEF4444),
                                icon = Icons.Default.Wifi,
                                iconColor = Color(0xFF14B8A6),
                                onClick = {
                                    Toast.makeText(context, if (isOnline) "Connected to Airtel 5G 📶" else "Offline Mode Active 📵", Toast.LENGTH_SHORT).show()
                                }
                            )
                        }
                    }
                }
            }
        }

        // 9. FIXED SEARCH BAR AT BOTTOM: ➕ Ask VED... 🎤 ➤
        var inputText by remember { mutableStateOf("") }
        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color.Transparent,
                            Color(0xEE090714),
                            Color(0xFF07050E)
                        )
                    )
                )
                .padding(horizontal = 16.dp, vertical = 10.dp)
        ) {
            GlassyCard(modifier = Modifier.fillMaxWidth()) {
                CustomInput(
                    value = inputText,
                    onValueChange = { inputText = it },
                    placeholder = "Ask VED...",
                    leadingIcon = Icons.Default.Add,
                    onLeadingIconClick = { showAttachmentDialog = true },
                    trailingIcon = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            IconButton(
                                onClick = onActivateVoice,
                                modifier = Modifier.size(36.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Mic,
                                    contentDescription = "Voice Input",
                                    tint = Color(0xFFC4B5FD)
                                )
                            }
                            IconButton(
                                onClick = {
                                    if (inputText.isNotBlank()) {
                                        onExecuteQuickAction(inputText)
                                        inputText = ""
                                    }
                                },
                                modifier = Modifier.size(36.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Send,
                                    contentDescription = "Send",
                                    tint = Color(0xFFC4B5FD)
                                )
                            }
                        }
                    },
                    onSend = {
                        if (inputText.isNotBlank()) {
                            onExecuteQuickAction(inputText)
                            inputText = ""
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 4.dp, vertical = 2.dp)
                )
            }
        }
    }
    }

    // Notifications Dialog
    if (showNotificationsDialog) {
        AlertDialog(
            onDismissRequest = { showNotificationsDialog = false },
            containerColor = Color(0xFF120E24),
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Notifications,
                        contentDescription = "Notifications",
                        tint = Color(0xFFA78BFA)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Notifications", color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                }
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    NotificationItem("VEDRA Voice Engine ready", "Voice features online.")
                    NotificationItem("Drive Sync Complete", "Physics.pdf saved to VEDrive.")
                    NotificationItem("JEE Study Session", "Today's goal: 20 Physics questions.")
                }
            },
            confirmButton = {
                TextButton(onClick = { showNotificationsDialog = false }) {
                    Text("Close", color = Color(0xFFA78BFA))
                }
            }
        )
    }

    // Profile Dialog
    if (showProfileDialog) {
        AlertDialog(
            onDismissRequest = { showProfileDialog = false },
            containerColor = Color(0xFF120E24),
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = "Profile",
                        tint = Color(0xFFA78BFA)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("User Profile", color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                }
            },
            text = {
                Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                    Box(
                        modifier = Modifier
                            .size(60.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF8B5CF6)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("R", color = Color.White, fontSize = 28.sp, fontWeight = FontWeight.Bold)
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(userName, color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                    Text("VEDRA AI Student Pro", color = Color(0xFFA78BFA), fontSize = 13.sp)
                    Spacer(modifier = Modifier.height(16.dp))
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .background(Color(0x20FFFFFF))
                            .padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceAround
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("Mode", color = Color.Gray, fontSize = 11.sp)
                            Text("PRO AI", color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                        }
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("Streak", color = Color.Gray, fontSize = 11.sp)
                            Text("14 Days 🔥", color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showProfileDialog = false }) {
                    Text("Close", color = Color(0xFFA78BFA))
                }
            }
        )
    }

    // Attachment Chooser Dialog
    if (showAttachmentDialog) {
        AlertDialog(
            onDismissRequest = { showAttachmentDialog = false },
            containerColor = Color(0xFF120E24),
            title = {
                Text("Add Attachment", color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold)
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    AttachmentOption("Upload PDF Document", Icons.Default.PictureAsPdf, Color(0xFFEF4444)) {
                        showAttachmentDialog = false
                        onExecuteQuickAction("upload pdf")
                    }
                    AttachmentOption("Scan Image / Document", Icons.Default.PhotoCamera, Color(0xFF3B82F6)) {
                        showAttachmentDialog = false
                        onExecuteQuickAction("scan document")
                    }
                    AttachmentOption("Record Audio Note", Icons.Default.Mic, Color(0xFFEC4899)) {
                        showAttachmentDialog = false
                        onActivateVoice()
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showAttachmentDialog = false }) {
                    Text("Cancel", color = Color.Gray)
                }
            }
        )
    }
}

@Composable
fun GlassyCard(modifier: Modifier = Modifier, content: @Composable () -> Unit) {
    val glassTint = LocalGlassmorphismTint.current
    val accent = glassTint.accentColor
    val bgAlpha = glassTint.bgAlpha
    val borderAlpha = glassTint.borderAlpha
    val intensity = glassTint.tintIntensity

    val topBgColor = Color.White.copy(alpha = (bgAlpha * 1.1f).coerceIn(0.06f, 0.40f))
    val bottomBgColor = accent.copy(alpha = (bgAlpha * 0.40f * intensity).coerceIn(0.02f, 0.45f))

    val topBorderColor = Color.White.copy(alpha = borderAlpha.coerceIn(0.15f, 0.65f))
    val bottomBorderColor = accent.copy(alpha = (borderAlpha * 0.60f * intensity).coerceIn(0.10f, 0.65f))

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(18.dp))
            .background(
                Brush.linearGradient(
                    colors = listOf(
                        topBgColor,
                        bottomBgColor
                    )
                )
            )
            .border(
                1.dp,
                Brush.linearGradient(
                    colors = listOf(
                        topBorderColor,
                        bottomBorderColor
                    )
                ),
                RoundedCornerShape(18.dp)
            )
    ) {
        content()
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun QuickActionGlassItem(
    label: String,
    icon: ImageVector,
    color: Color,
    packageName: String? = null,
    isPinned: Boolean = false,
    onLongClick: () -> Unit = {},
    onClick: () -> Unit
) {
    val context = LocalContext.current
    val appBitmap = remember(packageName, label) {
        AppPackageManagerHelper.getAppIconBitmap(context, packageName, label)
    }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.width(68.dp)
    ) {
        Box(
            modifier = Modifier
                .size(56.dp)
                .clip(CircleShape)
                .background(
                    Brush.linearGradient(
                        colors = listOf(
                            if (isPinned) Color(0x608B5CF6) else color.copy(alpha = 0.35f),
                            color.copy(alpha = 0.12f)
                        )
                    )
                )
                .border(
                    if (isPinned) 1.5.dp else 1.dp,
                    if (isPinned) Color(0xFFA78BFA) else color.copy(alpha = 0.6f),
                    CircleShape
                )
                .combinedClickable(
                    onClick = onClick,
                    onLongClick = onLongClick
                ),
            contentAlignment = Alignment.Center
        ) {
            if (appBitmap != null) {
                Image(
                    bitmap = appBitmap,
                    contentDescription = label,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(CircleShape)
                )
            } else {
                Icon(
                    imageVector = icon,
                    contentDescription = label,
                    tint = color,
                    modifier = Modifier.size(28.dp)
                )
            }

            if (isPinned) {
                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .size(14.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF8B5CF6)),
                    contentAlignment = Alignment.Center
                ) {
                    Text("📌", fontSize = 8.sp)
                }
            }
        }
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = label,
            color = Color(0xFFE5E7EB),
            fontSize = 11.5.sp,
            maxLines = 1,
            fontWeight = if (isPinned) FontWeight.Bold else FontWeight.SemiBold,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
fun QuickAppIcon(
    label: String,
    fallbackIcon: ImageVector,
    packageName: String,
    onClick: () -> Unit
) {
    val context = LocalContext.current
    val appBitmap = remember(packageName, label) {
        if (packageName != "torch" && packageName != "scan") {
            AppPackageManagerHelper.getAppIconBitmap(context, packageName, label)
        } else null
    }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .clickable { onClick() }
            .width(66.dp)
    ) {
        Box(
            modifier = Modifier
                .size(52.dp)
                .clip(CircleShape)
                .background(
                    Brush.linearGradient(
                        colors = listOf(
                            Color(0x28FFFFFF),
                            Color(0x0CFFFFFF)
                        )
                    )
                )
                .border(
                    1.dp,
                    Brush.linearGradient(
                        colors = listOf(
                            Color(0x40FFFFFF),
                            Color(0x10FFFFFF)
                        )
                    ),
                    CircleShape
                ),
            contentAlignment = Alignment.Center
        ) {
            if (appBitmap != null) {
                Image(
                    bitmap = appBitmap,
                    contentDescription = label,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(CircleShape)
                )
            } else {
                Icon(
                    imageVector = fallbackIcon,
                    contentDescription = label,
                    tint = when (label) {
                        "WhatsApp" -> Color(0xFF25D366)
                        "Music" -> Color(0xFFF43F5E)
                        "Calculator" -> Color(0xFFF97316)
                        "Weather" -> Color(0xFFEAB308)
                        "Camera" -> Color(0xFF3B82F6)
                        "Torch" -> Color(0xFFFCD34D)
                        "YouTube" -> Color(0xFFFF0000)
                        "Call" -> Color(0xFF10B981)
                        "Clock" -> Color(0xFF6366F1)
                        else -> Color(0xFF8B5CF6)
                    },
                    modifier = Modifier.size(24.dp)
                )
            }
        }
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = label,
            color = Color(0xFFD1D5DB),
            fontSize = 11.sp,
            maxLines = 1,
            fontWeight = FontWeight.Medium,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
fun ContinueGlassCard(
    title: String,
    subtitle: String,
    icon: ImageVector,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .background(
                Brush.linearGradient(
                    colors = listOf(
                        Color(0x258B5CF6),
                        Color(0x088B5CF6)
                    )
                )
            )
            .border(1.dp, Color(0x458B5CF6), RoundedCornerShape(16.dp))
            .clickable { onClick() }
            .padding(14.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(38.dp)
                    .clip(CircleShape)
                    .background(Color(0x408B5CF6)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = title,
                    tint = Color(0xFFC4B5FD),
                    modifier = Modifier.size(20.dp)
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(
                    text = title,
                    color = Color.White,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = subtitle,
                    color = Color(0xFF9CA3AF),
                    fontSize = 11.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

@Composable
fun RecentFileGlassCard(
    title: String,
    size: String,
    icon: ImageVector,
    color: Color,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .width(150.dp)
            .clip(RoundedCornerShape(14.dp))
            .background(
                Brush.linearGradient(
                    colors = listOf(
                        Color(0x20FFFFFF),
                        Color(0x08FFFFFF)
                    )
                )
            )
            .border(1.dp, Color(0x25FFFFFF), RoundedCornerShape(14.dp))
            .clickable { onClick() }
            .padding(12.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(color.copy(alpha = 0.2f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = title,
                    tint = color,
                    modifier = Modifier.size(18.dp)
                )
            }
            Spacer(modifier = Modifier.width(10.dp))
            Column {
                Text(
                    text = title,
                    color = Color.White,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = size,
                    color = Color.Gray,
                    fontSize = 10.sp
                )
            }
        }
    }
}

@Composable
fun SuggestedPillCard(
    text: String,
    dotColor: Color,
    onClick: () -> Unit
) {
    val glassTint = LocalGlassmorphismTint.current
    val borderAlpha = glassTint.borderAlpha
    val bgAlpha = glassTint.bgAlpha

    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(14.dp))
            .background(Color(0xFF0E1122).copy(alpha = (0.85f + bgAlpha * 0.4f).coerceIn(0.80f, 0.98f)))
            .border(1.dp, Color(0xFF1D223B).copy(alpha = (0.75f + borderAlpha * 0.4f).coerceIn(0.70f, 1.0f)), RoundedCornerShape(14.dp))
            .clickable { onClick() }
            .padding(horizontal = 14.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(8.dp)
                .clip(CircleShape)
                .background(dotColor)
        )
        Spacer(modifier = Modifier.width(10.dp))
        Text(
            text = text,
            color = Color(0xFFE2E8F0),
            fontSize = 13.5.sp,
            fontWeight = FontWeight.Medium
        )
        Spacer(modifier = Modifier.width(12.dp))
        Icon(
            imageVector = Icons.Default.ChevronRight,
            contentDescription = "Navigate",
            tint = Color(0xFF64748B),
            modifier = Modifier.size(18.dp)
        )
    }
}

@Composable
fun QuickStatusExactCard(
    title: String,
    value: String,
    statusText: String,
    statusColor: Color,
    icon: ImageVector,
    iconColor: Color,
    onClick: () -> Unit
) {
    val glassTint = LocalGlassmorphismTint.current
    val borderAlpha = glassTint.borderAlpha
    val intensity = glassTint.tintIntensity

    val topBorder = Color(0xFF1B2038).copy(alpha = (0.70f + borderAlpha * 0.5f).coerceIn(0.65f, 1.0f))
    val bgGradStart = Color(0xFF080B18)
    val bgGradEnd = iconColor.copy(alpha = (0.06f * intensity).coerceIn(0.02f, 0.22f))

    Box(
        modifier = Modifier
            .width(132.dp)
            .height(120.dp)
            .clip(RoundedCornerShape(18.dp))
            .background(
                Brush.verticalGradient(
                    colors = listOf(bgGradStart, bgGradEnd)
                )
            )
            .border(1.dp, topBorder, RoundedCornerShape(18.dp))
            .clickable { onClick() }
            .padding(14.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = title,
                    tint = iconColor,
                    modifier = Modifier.size(18.dp)
                )
                Text(
                    text = title,
                    color = Color(0xFF94A3B8),
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium
                )
            }

            Text(
                text = value,
                color = Color.White,
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Text(
                text = statusText,
                color = statusColor,
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
fun SuggestedGlassItem(text: String, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .clickable { onClick() }
            .padding(vertical = 8.dp, horizontal = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(7.dp)
                .clip(CircleShape)
                .background(Color(0xFFA78BFA))
        )
        Spacer(modifier = Modifier.width(14.dp))
        Text(
            text = text,
            color = Color(0xFFE5E7EB),
            fontSize = 13.5.sp,
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
fun StatusGlassCard(
    title: String,
    value: String,
    subtitle: String,
    icon: ImageVector,
    color: Color,
    modifier: Modifier = Modifier,
    onClick: () -> Unit = {}
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .background(
                Brush.linearGradient(
                    colors = listOf(
                        Color(0x20FFFFFF),
                        Color(0x06FFFFFF)
                    )
                )
            )
            .border(1.dp, Color(0x25FFFFFF), RoundedCornerShape(16.dp))
            .clickable { onClick() }
            .padding(14.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(38.dp)
                    .clip(CircleShape)
                    .background(color.copy(alpha = 0.2f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = title,
                    tint = color,
                    modifier = Modifier.size(20.dp)
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(
                    text = title,
                    color = Color(0xFF9CA3AF),
                    fontSize = 11.sp
                )
                Text(
                    text = value,
                    color = Color.White,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = subtitle,
                    color = Color.Gray,
                    fontSize = 9.5.sp,
                    maxLines = 1
                )
            }
        }
    }
}

@Composable
fun VedraStatusIndicator(
    isListening: Boolean,
    isSpeaking: Boolean,
    isThinking: Boolean = false
) {
    val infiniteTransition = rememberInfiniteTransition(label = "status_pulse")
    
    val pulseColor by infiniteTransition.animateColor(
        initialValue = if (isListening) Color(0xFF34D399) else if (isSpeaking) Color(0xFF67E8F9) else if (isThinking) Color(0xFFC4B5FD) else Color(0xFFA78BFA),
        targetValue = if (isListening) Color(0xFF059669) else if (isSpeaking) Color(0xFF06B6D4) else if (isThinking) Color(0xFF8B5CF6) else Color(0xFF6B7280),
        animationSpec = infiniteRepeatable(
            animation = tween(1000),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse_color"
    )

    Row(
        horizontalArrangement = Arrangement.spacedBy(5.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        repeat(5) {
            Box(
                modifier = Modifier
                    .size(6.dp)
                    .clip(CircleShape)
                    .background(pulseColor)
            )
        }
    }
}

@Composable
private fun NotificationItem(title: String, desc: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(Color(0x15FFFFFF))
            .padding(10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(8.dp)
                .clip(CircleShape)
                .background(Color(0xFFA78BFA))
        )
        Spacer(modifier = Modifier.width(10.dp))
        Column {
            Text(title, color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Bold)
            Text(desc, color = Color.Gray, fontSize = 11.sp)
        }
    }
}

@Composable
private fun AttachmentOption(label: String, icon: ImageVector, color: Color, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(Color(0x15FFFFFF))
            .clickable { onClick() }
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(imageVector = icon, contentDescription = label, tint = color, modifier = Modifier.size(22.dp))
        Spacer(modifier = Modifier.width(12.dp))
        Text(label, color = Color.White, fontSize = 13.5.sp, fontWeight = FontWeight.Medium)
    }
}
