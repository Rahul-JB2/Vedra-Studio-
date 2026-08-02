package com.example.ui.screens

import android.content.Context
import android.hardware.camera2.CameraManager
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
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
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.ChatBubble
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.EditNote
import androidx.compose.material.icons.filled.FlashlightOn
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.FormatQuote
import androidx.compose.material.icons.filled.HelpOutline
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MoreHoriz
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.NotificationsNone
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.PushPin
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material.icons.filled.Translate
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.ui.window.Dialog
import kotlinx.coroutines.launch
import com.example.services.GeminiService
import com.example.services.UtilityService
import com.example.services.DatabaseService
import com.example.services.OfflineService
import com.example.services.StorageWeatherService
import com.example.services.VoiceService

@Composable
fun HomeScreen(
    dbService: DatabaseService,
    voiceService: VoiceService,
    onActivateVoice: () -> Unit,
    onNavigateTab: (Int) -> Unit,
    onExecuteQuickAction: (String) -> Unit = {},
    onOpenDrawer: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val clipboardManager = LocalClipboardManager.current
    val isOnline by OfflineService.isNetworkAvailable.collectAsState()

    var showNotificationsDialog by remember { mutableStateOf(false) }
    var showProfileDialog by remember { mutableStateOf(false) }
    var showAskDialog by remember { mutableStateOf(false) }
    var askQueryText by remember { mutableStateOf("") }
    var isTorchOn by remember { mutableStateOf(false) }

    var showSummarizeModal by remember { mutableStateOf(false) }
    var showTranslateModal by remember { mutableStateOf(false) }
    var showExplainModal by remember { mutableStateOf(false) }
    var showWriterModal by remember { mutableStateOf(false) }
    var showScannerModal by remember { mutableStateOf(false) }
    var showCalculatorModal by remember { mutableStateOf(false) }
    var showQuickNotesModal by remember { mutableStateOf(false) }
    var showQuickToolsModal by remember { mutableStateOf(false) }

    var userName by remember { mutableStateOf(dbService.getSetting("user_name", "Rahul")) }

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

    // Toggle Torch / Flashlight Helper
    fun toggleTorch() {
        try {
            val cameraManager = context.getSystemService(Context.CAMERA_SERVICE) as? CameraManager
            val cameraId = cameraManager?.cameraIdList?.firstOrNull()
            if (cameraManager != null && cameraId != null) {
                isTorchOn = !isTorchOn
                cameraManager.setTorchMode(cameraId, isTorchOn)
                Toast.makeText(context, if (isTorchOn) "🔦 Torch ON" else "🔦 Torch OFF", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(context, "Flashlight unavailable on this device", Toast.LENGTH_SHORT).show()
            }
        } catch (e: Exception) {
            Toast.makeText(context, "Torch toggled", Toast.LENGTH_SHORT).show()
        }
    }

    val currentDensity = LocalDensity.current
    val customDensity = remember(currentDensity) {
        Density(
            density = currentDensity.density * 0.92f,
            fontScale = currentDensity.fontScale * 0.92f
        )
    }

    CompositionLocalProvider(LocalDensity provides customDensity) {
        Box(
            modifier = modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color(0xFF070512),
                            Color(0xFF0D0B1F),
                            Color(0xFF05040B)
                        )
                    )
                )
        ) {
            // Main Dashboard Content
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 14.dp),
                contentPadding = PaddingValues(top = 10.dp, bottom = 110.dp)
            ) {
                // 1. HEADER ROW: [☰ Menu] VEDRA AI | [🔍 Search] [👤 Profile]
                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            IconButton(
                                onClick = {
                                    if (onOpenDrawer != null) onOpenDrawer() else onExecuteQuickAction("open drawer")
                                },
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFF1B1530))
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Menu,
                                    contentDescription = "Drawer Menu",
                                    tint = Color.White,
                                    modifier = Modifier.size(20.dp)
                                )
                            }

                            Spacer(modifier = Modifier.width(10.dp))

                            Column {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = "VEDRA",
                                        color = Color.White,
                                        fontWeight = FontWeight.ExtraBold,
                                        fontSize = 18.sp,
                                        letterSpacing = 0.5.sp
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(6.dp))
                                            .background(Color(0xFF6D28D9))
                                            .padding(horizontal = 6.dp, vertical = 2.dp)
                                    ) {
                                        Text(
                                            text = "AI",
                                            color = Color.White,
                                            fontWeight = FontWeight.Black,
                                            fontSize = 10.5.sp
                                        )
                                    }
                                }
                                Text(
                                    text = "Your AI Companion ✨",
                                    color = Color(0xFFA29BFE),
                                    fontSize = 10.5.sp
                                )
                            }
                        }

                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            IconButton(
                                onClick = { showAskDialog = true },
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFF1B1530))
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Search,
                                    contentDescription = "Search",
                                    tint = Color(0xFFA29BFE),
                                    modifier = Modifier.size(18.dp)
                                )
                            }

                            IconButton(
                                onClick = { showProfileDialog = true },
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(
                                        Brush.radialGradient(
                                            listOf(Color(0xFF8B5CF6), Color(0xFF3B0764))
                                        )
                                    )
                                    .border(1.dp, Color(0xFFA855F7), CircleShape)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Person,
                                    contentDescription = "User Profile",
                                    tint = Color.White,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                    }
                }

                // 2. GREETING ROW + "✦ Ask VEDRA" BUTTON
                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 10.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "$greetingTime, $userName! 👋",
                                color = Color.White,
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "How can I help you today?",
                                color = Color(0xFF94A3B8),
                                fontSize = 12.sp
                            )
                        }

                        // Button: ✦ Ask VEDRA
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(20.dp))
                                .background(
                                    Brush.horizontalGradient(
                                        listOf(Color(0xFF6366F1), Color(0xFF8B5CF6))
                                    )
                                )
                                .clickable { onNavigateTab(2) } // Navigate to Ved AI Chat
                                .padding(horizontal = 14.dp, vertical = 10.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.AutoAwesome,
                                    contentDescription = "Ask VEDRA",
                                    tint = Color.White,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "Ask VEDRA",
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.5.sp
                                )
                            }
                        }
                    }
                }

                // 3. AI ENGINE STATUS CARD
                item {
                    Spacer(modifier = Modifier.height(8.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(20.dp))
                            .background(Color(0xFF130F2A))
                            .border(1.dp, Color(0xFF261E48), RoundedCornerShape(20.dp))
                            .padding(14.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Left Status
                            Column {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(
                                        modifier = Modifier
                                            .size(6.dp)
                                            .clip(CircleShape)
                                            .background(Color(0xFF10B981))
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "AI Status",
                                        color = Color(0xFFA29BFE),
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Medium
                                    )
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "Online",
                                    color = Color(0xFF10B981),
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = "Ready to assist you",
                                    color = Color(0xFF64748B),
                                    fontSize = 10.5.sp
                                )
                            }

                            // Center Pulse Orb Graphic
                            PulsingWaveformOrb(modifier = Modifier.size(64.dp))

                            // Right Stats
                            Column(horizontalAlignment = Alignment.End) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.clickable { onNavigateTab(4) }
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.AutoAwesome,
                                        contentDescription = "Active Engine",
                                        tint = Color(0xFF10B981),
                                        modifier = Modifier.size(13.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = "89%",
                                        color = Color.White,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 13.sp
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = "AI Engine Active",
                                        color = Color(0xFF94A3B8),
                                        fontSize = 10.sp
                                    )
                                    Icon(
                                        imageVector = Icons.Default.ChevronRight,
                                        contentDescription = "Details",
                                        tint = Color(0xFF64748B),
                                        modifier = Modifier.size(14.dp)
                                    )
                                }

                                Spacer(modifier = Modifier.height(8.dp))

                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.Storage,
                                        contentDescription = "Memory Used",
                                        tint = Color(0xFFA855F7),
                                        modifier = Modifier.size(13.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = "1.8 GB",
                                        color = Color.White,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 13.sp
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = "Memory Used",
                                        color = Color(0xFF94A3B8),
                                        fontSize = 10.sp
                                    )
                                }
                            }
                        }
                    }
                }

                // 4. QUICK ACTIONS
                item {
                    Spacer(modifier = Modifier.height(18.dp))
                    HomeSectionHeader(
                        title = "Quick Actions",
                        actionText = "Edit >",
                        onAction = { Toast.makeText(context, "Quick Actions Edit Mode", Toast.LENGTH_SHORT).show() }
                    )

                    // Row 1: 4 Large Feature Cards
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 8.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        LargeActionCard(
                            label = "New Chat",
                            icon = Icons.Default.ChatBubble,
                            backgroundColor = Color(0xFF432371),
                            modifier = Modifier.weight(1f)
                        ) { onNavigateTab(2) }

                        LargeActionCard(
                            label = "Voice Chat",
                            icon = Icons.Default.Mic,
                            backgroundColor = Color(0xFF1E3A8A),
                            modifier = Modifier.weight(1f)
                        ) { onActivateVoice() }

                        LargeActionCard(
                            label = "Scan & Ask",
                            icon = Icons.Default.CameraAlt,
                            backgroundColor = Color(0xFF064E3B),
                            modifier = Modifier.weight(1f)
                        ) { showScannerModal = true }

                        LargeActionCard(
                            label = "Study Hub",
                            icon = Icons.Default.School,
                            backgroundColor = Color(0xFF78350F),
                            modifier = Modifier.weight(1f)
                        ) { onNavigateTab(1) } // Navigate to VEHub tab
                    }

                    // Row 2: 5 Small Action Cards Below
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 10.dp),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        SmallActionCard("Summarize", Icons.Default.Description, Color(0xFFA855F7), Modifier.weight(1f)) {
                            showSummarizeModal = true
                        }
                        SmallActionCard("Translate", Icons.Default.Translate, Color(0xFF3B82F6), Modifier.weight(1f)) {
                            showTranslateModal = true
                        }
                        SmallActionCard("Explain", Icons.Default.Lightbulb, Color(0xFF10B981), Modifier.weight(1f)) {
                            showExplainModal = true
                        }
                        SmallActionCard("Write", Icons.Default.Edit, Color(0xFFF59E0B), Modifier.weight(1f)) {
                            showWriterModal = true
                        }
                        SmallActionCard("More", Icons.Default.MoreHoriz, Color(0xFF94A3B8), Modifier.weight(1f)) {
                            showQuickToolsModal = true
                        }
                    }
                }

                // 5. RECENT CHATS
                item {
                    Spacer(modifier = Modifier.height(20.dp))
                    HomeSectionHeader(
                        title = "Recent Chats",
                        actionText = "View All >",
                        onAction = { onNavigateTab(2) }
                    )

                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        RecentChatCardItem(
                            iconText = "⚛️",
                            title = "Quantum Mechanics Explanation",
                            time = "09:30 AM",
                            snippet = "Can you explain the uncertainty principle...",
                            isPinned = true
                        ) { onNavigateTab(2) }

                        RecentChatCardItem(
                            iconText = "🍃",
                            title = "Photosynthesis Process",
                            time = "Yesterday",
                            snippet = "Summarize this topic in simple words"
                        ) { onNavigateTab(2) }

                        RecentChatCardItem(
                            iconText = "💻",
                            title = "Python Code Help",
                            time = "Yesterday",
                            snippet = "How to reverse a linked list in Python?"
                        ) { onNavigateTab(2) }

                        RecentChatCardItem(
                            iconText = "😊",
                            title = "Motivation for Study",
                            time = "2 days ago",
                            snippet = "Give me some motivational lines"
                        ) { onNavigateTab(2) }
                    }
                }

                // 6. VEHUB - CONTINUE LEARNING
                item {
                    Spacer(modifier = Modifier.height(20.dp))
                    HomeSectionHeader(
                        title = "VEHub – Continue Learning",
                        actionText = "Go to Hub >",
                        onAction = { onNavigateTab(1) }
                    )

                    Spacer(modifier = Modifier.height(8.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(20.dp))
                            .background(
                                Brush.linearGradient(
                                    listOf(Color(0xFF1E103A), Color(0xFF0F0B24))
                                )
                            )
                            .border(1.dp, Color(0xFF382362), RoundedCornerShape(20.dp))
                            .padding(14.dp)
                    ) {
                        Column {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                // Left Book Art Graphic
                                Box(
                                    modifier = Modifier
                                        .size(68.dp, 84.dp)
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(
                                            Brush.verticalGradient(
                                                listOf(Color(0xFF4C1D95), Color(0xFF1E1B4B))
                                            )
                                        )
                                        .border(1.dp, Color(0xFF7C3AED), RoundedCornerShape(12.dp)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        Text("PHYSICS", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 9.sp)
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Icon(
                                            imageVector = Icons.Default.AutoAwesome,
                                            contentDescription = null,
                                            tint = Color(0xFFC4B5FD),
                                            modifier = Modifier.size(24.dp)
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.width(14.dp))

                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = "Electrostatics",
                                        color = Color.White,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 15.sp
                                    )
                                    Text(
                                        text = "Chapter 4 • Physics",
                                        color = Color(0xFFA29BFE),
                                        fontSize = 11.5.sp
                                    )

                                    Spacer(modifier = Modifier.height(8.dp))

                                    // Progress Bar
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Box(
                                            modifier = Modifier
                                                .weight(1f)
                                                .height(6.dp)
                                                .clip(RoundedCornerShape(3.dp))
                                                .background(Color(0xFF2E2050))
                                        ) {
                                            Box(
                                                modifier = Modifier
                                                    .fillMaxWidth(0.65f)
                                                    .fillMaxSize()
                                                    .background(Color(0xFF8B5CF6))
                                            )
                                        }
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            text = "65%",
                                            color = Color.White,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 11.sp
                                        )
                                    }

                                    Spacer(modifier = Modifier.height(10.dp))

                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(14.dp))
                                            .background(Color(0xFF6D28D9))
                                            .clickable { onNavigateTab(1) }
                                            .padding(horizontal = 12.dp, vertical = 6.dp)
                                    ) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Icon(
                                                imageVector = Icons.Default.PlayArrow,
                                                contentDescription = null,
                                                tint = Color.White,
                                                modifier = Modifier.size(14.dp)
                                            )
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text(
                                                text = "Continue Study",
                                                color = Color.White,
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 11.sp
                                            )
                                        }
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            // 3 Mini Cards inside VEHub
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                VEHubMiniCard(
                                    title = "VEDrive",
                                    subtitle = "128 Files • 12.4 GB",
                                    actionText = "Manage",
                                    icon = Icons.Default.Folder,
                                    modifier = Modifier.weight(1f)
                                ) { onNavigateTab(3) }

                                VEHubMiniCard(
                                    title = "VEDM-T",
                                    subtitle = "Knowledge Base",
                                    actionText = "Open",
                                    icon = Icons.Default.Psychology,
                                    modifier = Modifier.weight(1f)
                                ) { onNavigateTab(1) }

                                VEHubMiniCard(
                                    title = "Tools",
                                    subtitle = "12+ Utilities",
                                    actionText = "Open",
                                    icon = Icons.Default.AutoAwesome,
                                    modifier = Modifier.weight(1f)
                                ) { onNavigateTab(1) }
                            }
                        }
                    }
                }

                // 7. TODAY'S OVERVIEW
                item {
                    Spacer(modifier = Modifier.height(20.dp))
                    HomeSectionHeader(
                        title = "Today's Overview",
                        actionText = "🔄 Refresh",
                        onAction = { Toast.makeText(context, "Stats Refreshed!", Toast.LENGTH_SHORT).show() }
                    )

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 8.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        StatCardItem(
                            label = "Study Time",
                            value = "2h 45m",
                            trendText = "▲ 12% from yesterday",
                            trendColor = Color(0xFF10B981),
                            modifier = Modifier.weight(1f)
                        )

                        StatCardItem(
                            label = "Chats",
                            value = "18",
                            trendText = "▲ 8% from yesterday",
                            trendColor = Color(0xFF10B981),
                            modifier = Modifier.weight(1f)
                        )

                        StatCardItem(
                            label = "Questions Asked",
                            value = "23",
                            trendText = "▲ 15% from yesterday",
                            trendColor = Color(0xFF10B981),
                            modifier = Modifier.weight(1f)
                        )
                    }
                }

                // 8. TOP TOOLS
                item {
                    Spacer(modifier = Modifier.height(20.dp))
                    HomeSectionHeader(
                        title = "Top Tools",
                        actionText = "View All >",
                        onAction = { onNavigateTab(1) }
                    )

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 8.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        TopToolCard("Ask VEDRA", Icons.Default.AutoAwesome, Color(0xFF6366F1), Modifier.weight(1f)) { onNavigateTab(2) }
                        TopToolCard("Scan & Ask", Icons.Default.CameraAlt, Color(0xFF10B981), Modifier.weight(1f)) { Toast.makeText(context, "Camera Scanner Ready", Toast.LENGTH_SHORT).show() }
                        TopToolCard("Voice Chat", Icons.Default.Mic, Color(0xFF3B82F6), Modifier.weight(1f)) { onActivateVoice() }
                        TopToolCard("Study Hub", Icons.Default.School, Color(0xFFF59E0B), Modifier.weight(1f)) { onNavigateTab(1) }
                    }
                }

                // 9. AI SUGGESTIONS FOR YOU
                item {
                    Spacer(modifier = Modifier.height(20.dp))
                    HomeSectionHeader(
                        title = "AI Suggestions for You",
                        actionText = null,
                        onAction = {}
                    )

                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        AISuggestionItem(
                            icon = Icons.Default.AutoAwesome,
                            iconBg = Color(0xFF2563EB),
                            title = "Continue your study on Electrostatics",
                            subtitle = "Physics • 65% completed"
                        ) { onNavigateTab(1) }

                        AISuggestionItem(
                            icon = Icons.Default.EditNote,
                            iconBg = Color(0xFF9333EA),
                            title = "Practice 15 PYQs",
                            subtitle = "Improve your problem solving"
                        ) { onNavigateTab(1) }

                        AISuggestionItem(
                            icon = Icons.Default.Description,
                            iconBg = Color(0xFF16A34A),
                            title = "Revise Formula Sheet",
                            subtitle = "You last opened 2 days ago"
                        ) { onNavigateTab(3) }
                    }
                }

                // 10. DAILY QUOTE
                item {
                    Spacer(modifier = Modifier.height(20.dp))
                    HomeSectionHeader(title = "Daily Quote", actionText = null, onAction = {})

                    Spacer(modifier = Modifier.height(8.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(18.dp))
                            .background(Color(0xFF0F142A))
                            .border(1.dp, Color(0xFF1D284B), RoundedCornerShape(18.dp))
                            .padding(16.dp)
                    ) {
                        Row(verticalAlignment = Alignment.Top) {
                            Text(
                                text = "❝",
                                color = Color(0xFF3B82F6),
                                fontSize = 32.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(end = 10.dp)
                            )
                            Column {
                                Text(
                                    text = "The beautiful thing about learning is that no one can take it away from you.",
                                    color = Color.White,
                                    fontSize = 12.5.sp,
                                    fontWeight = FontWeight.Medium,
                                    lineHeight = 18.sp
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = "– B. B. King",
                                    color = Color(0xFF94A3B8),
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }

                // 11. SMART REMINDERS
                item {
                    Spacer(modifier = Modifier.height(20.dp))
                    HomeSectionHeader(
                        title = "Smart Reminders",
                        actionText = "View All >",
                        onAction = { Toast.makeText(context, "Opening All Reminders", Toast.LENGTH_SHORT).show() }
                    )

                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        SmartReminderItem("Physics Revision", "Today, 11:00 AM", Icons.Default.AccessTime, Color(0xFF8B5CF6))
                        SmartReminderItem("Chemistry Test", "Today, 03:00 PM", Icons.Default.AutoAwesome, Color(0xFF10B981))
                        SmartReminderItem("Maths Mock Test", "Tomorrow, 09:00 AM", Icons.Default.School, Color(0xFFF59E0B))
                    }
                }

                // 12. SYSTEM OVERVIEW
                item {
                    Spacer(modifier = Modifier.height(20.dp))
                    HomeSectionHeader(title = "System Overview", actionText = null, onAction = {})

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 8.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        SystemStatusChip("AI Mode", "Hybrid", Icons.Default.Psychology, Color(0xFF3B82F6), Modifier.weight(1f))
                        SystemStatusChip("Internet", if (isOnline) "Connected" else "Offline", Icons.Default.Wifi, if (isOnline) Color(0xFF10B981) else Color(0xFFEF4444), Modifier.weight(1f))
                        SystemStatusChip("Battery", "72%", Icons.Default.AutoAwesome, Color(0xFF10B981), Modifier.weight(1f))
                        SystemStatusChip("Storage", "56% Used", Icons.Default.Storage, Color(0xFF8B5CF6), Modifier.weight(1f))
                    }
                }

                // 13. ACTIVITY MONITOR
                item {
                    Spacer(modifier = Modifier.height(20.dp))
                    HomeSectionHeader(
                        title = "Activity Monitor",
                        actionText = "View All >",
                        onAction = { Toast.makeText(context, "Opening Analytics Dashboard", Toast.LENGTH_SHORT).show() }
                    )

                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        ActivityMonitorRow("AI Responses Generated", "124 Today", Color(0xFFA855F7), Icons.Default.AutoAwesome)
                        ActivityMonitorRow("Voice Interactions", "32 Today", Color(0xFF10B981), Icons.Default.Mic)
                        ActivityMonitorRow("Files Scanned", "14 Today", Color(0xFFF59E0B), Icons.Default.Folder)
                    }
                }

                // 14. SHORTCUTS
                item {
                    Spacer(modifier = Modifier.height(20.dp))
                    HomeSectionHeader(
                        title = "Shortcuts",
                        actionText = "Edit",
                        onAction = { Toast.makeText(context, "Shortcut Edit Mode", Toast.LENGTH_SHORT).show() }
                    )

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 8.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        ShortcutButtonItem("Torch", Icons.Default.FlashlightOn, Color(0xFFF59E0B), Modifier.weight(1f)) {
                            toggleTorch()
                        }
                        ShortcutButtonItem("Calculator", Icons.Default.Calculate, Color(0xFF3B82F6), Modifier.weight(1f)) {
                            showCalculatorModal = true
                        }
                        ShortcutButtonItem("Translate", Icons.Default.Translate, Color(0xFF10B981), Modifier.weight(1f)) {
                            showTranslateModal = true
                        }
                        ShortcutButtonItem("Notes", Icons.Default.Description, Color(0xFFA855F7), Modifier.weight(1f)) {
                            showQuickNotesModal = true
                        }
                        ShortcutButtonItem("More", Icons.Default.MoreHoriz, Color(0xFF94A3B8), Modifier.weight(1f)) {
                            showQuickToolsModal = true
                        }
                    }
                }
            }

            // 15. BOTTOM FIXED INPUT QUERY BAR ("Ask VEDRA anything...")
            Box(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .padding(12.dp)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(26.dp))
                        .background(Color(0xFF16122E))
                        .border(1.dp, Color(0xFF2E2452), RoundedCornerShape(26.dp))
                        .padding(horizontal = 14.dp, vertical = 6.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedTextField(
                            value = askQueryText,
                            onValueChange = { askQueryText = it },
                            placeholder = {
                                Text("Ask VEDRA anything...", color = Color(0xFF64748B), fontSize = 13.sp)
                            },
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White,
                                focusedBorderColor = Color.Transparent,
                                unfocusedBorderColor = Color.Transparent,
                                focusedContainerColor = Color.Transparent,
                                unfocusedContainerColor = Color.Transparent
                            ),
                            modifier = Modifier.weight(1f)
                        )

                        // Mic Button
                        IconButton(
                            onClick = { onActivateVoice() },
                            modifier = Modifier.size(36.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Mic,
                                contentDescription = "Voice Input",
                                tint = Color(0xFFA29BFE),
                                modifier = Modifier.size(20.dp)
                            )
                        }

                        Spacer(modifier = Modifier.width(4.dp))

                        // Purple Send Circle Button
                        IconButton(
                            onClick = {
                                if (askQueryText.isNotBlank()) {
                                    Toast.makeText(context, "Query sent to VEDRA!", Toast.LENGTH_SHORT).show()
                                    askQueryText = ""
                                    onNavigateTab(2)
                                } else {
                                    onNavigateTab(2)
                                }
                            },
                            modifier = Modifier
                                .size(38.dp)
                                .clip(CircleShape)
                                .background(Color(0xFF6D28D9))
                        ) {
                            Icon(
                                imageVector = Icons.Default.Send,
                                contentDescription = "Send Query",
                                tint = Color.White,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }
            }
        }

        // Search / Ask Dialog
        if (showAskDialog) {
            AlertDialog(
                onDismissRequest = { showAskDialog = false },
                containerColor = Color(0xFF141026),
                title = {
                    Text("Search & Ask VEDRA AI", color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                },
                text = {
                    OutlinedTextField(
                        value = askQueryText,
                        onValueChange = { askQueryText = it },
                        placeholder = { Text("Search topics, files, or ask anything...", color = Color.Gray) },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedBorderColor = Color(0xFF8B5CF6),
                            unfocusedBorderColor = Color(0xFF28204A)
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )
                },
                confirmButton = {
                    Button(
                        onClick = {
                            showAskDialog = false
                            onNavigateTab(2)
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF8B5CF6))
                    ) {
                        Text("Ask VEDRA", color = Color.White)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showAskDialog = false }) {
                        Text("Cancel", color = Color.Gray)
                    }
                }
            )
        }

        // Profile Dialog
        if (showProfileDialog) {
            var tempName by remember { mutableStateOf(userName) }
            AlertDialog(
                onDismissRequest = { showProfileDialog = false },
                containerColor = Color(0xFF141026),
                title = {
                    Text("User Profile & Customization", color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Text("Display Name:", color = Color(0xFFA855F7), fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                        OutlinedTextField(
                            value = tempName,
                            onValueChange = { tempName = it },
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White,
                                focusedBorderColor = Color(0xFFA855F7),
                                unfocusedBorderColor = Color(0xFF2E2452)
                            ),
                            modifier = Modifier.fillMaxWidth()
                        )
                        Text("Status: Premium VEDRA Member 👑", color = Color(0xFFD8B4FE), fontSize = 12.sp)
                        Text("AI Memory: 50 GB High Speed Storage", color = Color(0xFF94A3B8), fontSize = 11.5.sp)
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            if (tempName.isNotBlank()) {
                                dbService.setSetting("user_name", tempName)
                                userName = tempName
                                Toast.makeText(context, "Name updated to $tempName! ✨", Toast.LENGTH_SHORT).show()
                            }
                            showProfileDialog = false
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF8B5CF6))
                    ) {
                        Text("Save & Close", color = Color.White)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showProfileDialog = false }) {
                        Text("Cancel", color = Color.Gray)
                    }
                }
            )
        }

        // 1. Summarize Modal
        if (showSummarizeModal) {
            var inputText by remember { mutableStateOf("") }
            var resultText by remember { mutableStateOf("") }
            var isLoading by remember { mutableStateOf(false) }

            Dialog(onDismissRequest = { showSummarizeModal = false }) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(0.95f)
                        .clip(RoundedCornerShape(20.dp))
                        .background(Color(0xFF130F26))
                        .border(1.dp, Color(0xFF3B2863), RoundedCornerShape(20.dp))
                        .padding(18.dp)
                ) {
                    Column {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Description, contentDescription = null, tint = Color(0xFFA855F7), modifier = Modifier.size(20.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("⚡ VEDRA AI Summarizer", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                            }
                            IconButton(onClick = { showSummarizeModal = false }) {
                                Icon(Icons.Default.Close, contentDescription = "Close", tint = Color.Gray)
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))
                        Text("Paste or type text to generate a concise summary:", color = Color(0xFF94A3B8), fontSize = 11.5.sp)
                        Spacer(modifier = Modifier.height(8.dp))

                        OutlinedTextField(
                            value = inputText,
                            onValueChange = { inputText = it },
                            placeholder = { Text("Paste article, notes, or paragraph...", color = Color.Gray, fontSize = 12.sp) },
                            maxLines = 5,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White,
                                focusedBorderColor = Color(0xFFA855F7),
                                unfocusedBorderColor = Color(0xFF261D42)
                            ),
                            modifier = Modifier.fillMaxWidth().height(110.dp)
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        Button(
                            onClick = {
                                if (inputText.isBlank()) return@Button
                                isLoading = true
                                coroutineScope.launch {
                                    val res = GeminiService.generateResponse(
                                        prompt = "Summarize the following text clearly with key bullet points:\n$inputText",
                                        dbService = dbService,
                                        context = context
                                    )
                                    resultText = res
                                    isLoading = false
                                }
                            },
                            enabled = !isLoading && inputText.isNotBlank(),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFA855F7)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            if (isLoading) {
                                CircularProgressIndicator(modifier = Modifier.size(16.dp), color = Color.White, strokeWidth = 2.dp)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Summarizing...", color = Color.White)
                            } else {
                                Text("Summarize with VEDRA AI", color = Color.White, fontWeight = FontWeight.Bold)
                            }
                        }

                        if (resultText.isNotBlank()) {
                            Spacer(modifier = Modifier.height(12.dp))
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(Color(0xFF1C1635))
                                    .border(1.dp, Color(0xFF4C307E), RoundedCornerShape(12.dp))
                                    .padding(12.dp)
                            ) {
                                Column {
                                    Text("Summary:", color = Color(0xFFA855F7), fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(resultText, color = Color.White, fontSize = 12.sp, lineHeight = 17.sp)
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Row(horizontalArrangement = Arrangement.End, modifier = Modifier.fillMaxWidth()) {
                                        TextButton(onClick = {
                                            clipboardManager.setText(AnnotatedString(resultText))
                                            Toast.makeText(context, "Summary copied! 📋", Toast.LENGTH_SHORT).show()
                                        }) {
                                            Text("Copy 📋", color = Color(0xFFA855F7), fontSize = 11.sp)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // 2. Translate Modal
        if (showTranslateModal) {
            var inputText by remember { mutableStateOf("") }
            var selectedLang by remember { mutableStateOf("Hindi") }
            var resultText by remember { mutableStateOf("") }
            var isLoading by remember { mutableStateOf(false) }

            val languages = listOf("Hindi", "English", "Hinglish", "Spanish", "French", "German", "Japanese", "Marathi")

            Dialog(onDismissRequest = { showTranslateModal = false }) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(0.95f)
                        .clip(RoundedCornerShape(20.dp))
                        .background(Color(0xFF101328))
                        .border(1.dp, Color(0xFF1E2856), RoundedCornerShape(20.dp))
                        .padding(18.dp)
                ) {
                    Column {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Translate, contentDescription = null, tint = Color(0xFF3B82F6), modifier = Modifier.size(20.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("🌐 VEDRA AI Translator", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                            }
                            IconButton(onClick = { showTranslateModal = false }) {
                                Icon(Icons.Default.Close, contentDescription = "Close", tint = Color.Gray)
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))
                        Text("Select target language:", color = Color(0xFF94A3B8), fontSize = 11.sp)
                        Spacer(modifier = Modifier.height(6.dp))

                        LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            items(languages) { lang ->
                                val isSel = selectedLang == lang
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(if (isSel) Color(0xFF2563EB) else Color(0xFF1E2442))
                                        .clickable { selectedLang = lang }
                                        .padding(horizontal = 10.dp, vertical = 5.dp)
                                ) {
                                    Text(lang, color = Color.White, fontSize = 11.sp, fontWeight = if (isSel) FontWeight.Bold else FontWeight.Normal)
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        OutlinedTextField(
                            value = inputText,
                            onValueChange = { inputText = it },
                            placeholder = { Text("Enter text to translate...", color = Color.Gray, fontSize = 12.sp) },
                            maxLines = 4,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White,
                                focusedBorderColor = Color(0xFF3B82F6),
                                unfocusedBorderColor = Color(0xFF1E2442)
                            ),
                            modifier = Modifier.fillMaxWidth().height(90.dp)
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        Button(
                            onClick = {
                                if (inputText.isBlank()) return@Button
                                isLoading = true
                                coroutineScope.launch {
                                    val res = GeminiService.generateResponse(
                                        prompt = "Translate the following text accurately into $selectedLang:\n$inputText",
                                        dbService = dbService,
                                        context = context
                                    )
                                    resultText = res
                                    isLoading = false
                                }
                            },
                            enabled = !isLoading && inputText.isNotBlank(),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2563EB)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            if (isLoading) {
                                CircularProgressIndicator(modifier = Modifier.size(16.dp), color = Color.White, strokeWidth = 2.dp)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Translating...", color = Color.White)
                            } else {
                                Text("Translate to $selectedLang", color = Color.White, fontWeight = FontWeight.Bold)
                            }
                        }

                        if (resultText.isNotBlank()) {
                            Spacer(modifier = Modifier.height(12.dp))
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(Color(0xFF181F3B))
                                    .border(1.dp, Color(0xFF2B3A70), RoundedCornerShape(12.dp))
                                    .padding(12.dp)
                            ) {
                                Column {
                                    Text("Translation ($selectedLang):", color = Color(0xFF60A5FA), fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(resultText, color = Color.White, fontSize = 13.sp, lineHeight = 18.sp)
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                                        TextButton(onClick = { voiceService.speak(resultText) }) {
                                            Text("Speak 🔊", color = Color(0xFF60A5FA), fontSize = 11.sp)
                                        }
                                        TextButton(onClick = {
                                            clipboardManager.setText(AnnotatedString(resultText))
                                            Toast.makeText(context, "Translation copied! 📋", Toast.LENGTH_SHORT).show()
                                        }) {
                                            Text("Copy 📋", color = Color(0xFF60A5FA), fontSize = 11.sp)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // 3. Concept Explainer Modal
        if (showExplainModal) {
            var inputText by remember { mutableStateOf("") }
            var resultText by remember { mutableStateOf("") }
            var isLoading by remember { mutableStateOf(false) }

            Dialog(onDismissRequest = { showExplainModal = false }) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(0.95f)
                        .clip(RoundedCornerShape(20.dp))
                        .background(Color(0xFF0C1917))
                        .border(1.dp, Color(0xFF1A3D36), RoundedCornerShape(20.dp))
                        .padding(18.dp)
                ) {
                    Column {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Lightbulb, contentDescription = null, tint = Color(0xFF10B981), modifier = Modifier.size(20.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("💡 Concept Explainer", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                            }
                            IconButton(onClick = { showExplainModal = false }) {
                                Icon(Icons.Default.Close, contentDescription = "Close", tint = Color.Gray)
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))
                        Text("Enter any complex topic, formula, or law:", color = Color(0xFF94A3B8), fontSize = 11.5.sp)
                        Spacer(modifier = Modifier.height(8.dp))

                        OutlinedTextField(
                            value = inputText,
                            onValueChange = { inputText = it },
                            placeholder = { Text("e.g. Quantum Entanglement, Bernoulli's Principle...", color = Color.Gray, fontSize = 12.sp) },
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White,
                                focusedBorderColor = Color(0xFF10B981),
                                unfocusedBorderColor = Color(0xFF15332B)
                            ),
                            modifier = Modifier.fillMaxWidth()
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        Button(
                            onClick = {
                                if (inputText.isBlank()) return@Button
                                isLoading = true
                                coroutineScope.launch {
                                    val res = GeminiService.generateResponse(
                                        prompt = "Explain $inputText step-by-step with simple analogies, key formula, and 3 bullet points.",
                                        dbService = dbService,
                                        context = context
                                    )
                                    resultText = res
                                    isLoading = false
                                }
                            },
                            enabled = !isLoading && inputText.isNotBlank(),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF059669)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            if (isLoading) {
                                CircularProgressIndicator(modifier = Modifier.size(16.dp), color = Color.White, strokeWidth = 2.dp)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Explaining...", color = Color.White)
                            } else {
                                Text("Explain Step-by-Step", color = Color.White, fontWeight = FontWeight.Bold)
                            }
                        }

                        if (resultText.isNotBlank()) {
                            Spacer(modifier = Modifier.height(12.dp))
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(Color(0xFF112E27))
                                    .border(1.dp, Color(0xFF1F594C), RoundedCornerShape(12.dp))
                                    .padding(12.dp)
                            ) {
                                Column {
                                    Text("Explanation:", color = Color(0xFF34D399), fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(resultText, color = Color.White, fontSize = 12.sp, lineHeight = 17.sp)
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Row(horizontalArrangement = Arrangement.End, modifier = Modifier.fillMaxWidth()) {
                                        TextButton(onClick = {
                                            clipboardManager.setText(AnnotatedString(resultText))
                                            Toast.makeText(context, "Explanation copied! 📋", Toast.LENGTH_SHORT).show()
                                        }) {
                                            Text("Copy 📋", color = Color(0xFF34D399), fontSize = 11.sp)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // 4. Creative AI Writer Modal
        if (showWriterModal) {
            var inputText by remember { mutableStateOf("") }
            var resultText by remember { mutableStateOf("") }
            var isLoading by remember { mutableStateOf(false) }

            Dialog(onDismissRequest = { showWriterModal = false }) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(0.95f)
                        .clip(RoundedCornerShape(20.dp))
                        .background(Color(0xFF26180C))
                        .border(1.dp, Color(0xFF4D331A), RoundedCornerShape(20.dp))
                        .padding(18.dp)
                ) {
                    Column {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Edit, contentDescription = null, tint = Color(0xFFF59E0B), modifier = Modifier.size(20.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("✍️ VEDRA AI Content Writer", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                            }
                            IconButton(onClick = { showWriterModal = false }) {
                                Icon(Icons.Default.Close, contentDescription = "Close", tint = Color.Gray)
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))
                        Text("What would you like VEDRA to write?", color = Color(0xFF94A3B8), fontSize = 11.5.sp)
                        Spacer(modifier = Modifier.height(8.dp))

                        OutlinedTextField(
                            value = inputText,
                            onValueChange = { inputText = it },
                            placeholder = { Text("e.g. Leave application email, physics study summary...", color = Color.Gray, fontSize = 12.sp) },
                            maxLines = 4,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White,
                                focusedBorderColor = Color(0xFFF59E0B),
                                unfocusedBorderColor = Color(0xFF452D17)
                            ),
                            modifier = Modifier.fillMaxWidth().height(90.dp)
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        Button(
                            onClick = {
                                if (inputText.isBlank()) return@Button
                                isLoading = true
                                coroutineScope.launch {
                                    val res = GeminiService.generateResponse(
                                        prompt = "Write a well-crafted draft for:\n$inputText",
                                        dbService = dbService,
                                        context = context
                                    )
                                    resultText = res
                                    isLoading = false
                                }
                            },
                            enabled = !isLoading && inputText.isNotBlank(),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD97706)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            if (isLoading) {
                                CircularProgressIndicator(modifier = Modifier.size(16.dp), color = Color.White, strokeWidth = 2.dp)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Writing...", color = Color.White)
                            } else {
                                Text("Generate Content", color = Color.White, fontWeight = FontWeight.Bold)
                            }
                        }

                        if (resultText.isNotBlank()) {
                            Spacer(modifier = Modifier.height(12.dp))
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(Color(0xFF382312))
                                    .border(1.dp, Color(0xFF5E3C1E), RoundedCornerShape(12.dp))
                                    .padding(12.dp)
                            ) {
                                Column {
                                    Text("Generated Draft:", color = Color(0xFFFBBF24), fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(resultText, color = Color.White, fontSize = 12.sp, lineHeight = 17.sp)
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Row(horizontalArrangement = Arrangement.End, modifier = Modifier.fillMaxWidth()) {
                                        TextButton(onClick = {
                                            clipboardManager.setText(AnnotatedString(resultText))
                                            Toast.makeText(context, "Draft copied! 📋", Toast.LENGTH_SHORT).show()
                                        }) {
                                            Text("Copy 📋", color = Color(0xFFFBBF24), fontSize = 11.sp)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // 5. Visual Camera & Document Scanner Modal
        if (showScannerModal) {
            var scanStatus by remember { mutableStateOf("Ready to scan") }
            var isScanning by remember { mutableStateOf(false) }

            Dialog(onDismissRequest = { showScannerModal = false }) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(0.92f)
                        .clip(RoundedCornerShape(20.dp))
                        .background(Color(0xFF091F1A))
                        .border(1.dp, Color(0xFF124036), RoundedCornerShape(20.dp))
                        .padding(18.dp)
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.CameraAlt, contentDescription = null, tint = Color(0xFF10B981), modifier = Modifier.size(20.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("📷 Visual Scan & AI Solver", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                            }
                            IconButton(onClick = { showScannerModal = false }) {
                                Icon(Icons.Default.Close, contentDescription = "Close", tint = Color.Gray)
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(130.dp)
                                .clip(RoundedCornerShape(14.dp))
                                .background(Color(0xFF10332B))
                                .border(1.dp, Color(0xFF1F5C4F), RoundedCornerShape(14.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(Icons.Default.CameraAlt, contentDescription = null, tint = Color(0xFF34D399), modifier = Modifier.size(36.dp))
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(scanStatus, color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Medium)
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        Button(
                            onClick = {
                                isScanning = true
                                scanStatus = "Scanning document & extracting text..."
                                coroutineScope.launch {
                                    kotlinx.coroutines.delay(1200)
                                    isScanning = false
                                    scanStatus = "✅ Scan Complete! OCR Extracted formula: F = ma. Asking VEDRA AI..."
                                    kotlinx.coroutines.delay(800)
                                    showScannerModal = false
                                    onNavigateTab(2)
                                }
                            },
                            enabled = !isScanning,
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF059669)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Capture & Analyze with VEDRA AI", color = Color.White, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        // 6. Scientific Calculator Modal
        if (showCalculatorModal) {
            var display by remember { mutableStateOf("0") }
            var expression by remember { mutableStateOf("") }

            val calcButtons = listOf(
                listOf("C", "(", ")", "/"),
                listOf("7", "8", "9", "*"),
                listOf("4", "5", "6", "-"),
                listOf("1", "2", "3", "+"),
                listOf("0", ".", "sin", "=")
            )

            fun evalCalc(expr: String): String {
                return try {
                    val clean = expr.replace("sin", "Math.sin")
                    if (clean.contains("+")) {
                        val parts = clean.split("+")
                        (parts[0].trim().toDouble() + parts[1].trim().toDouble()).toString()
                    } else if (clean.contains("-")) {
                        val parts = clean.split("-")
                        (parts[0].trim().toDouble() - parts[1].trim().toDouble()).toString()
                    } else if (clean.contains("*")) {
                        val parts = clean.split("*")
                        (parts[0].trim().toDouble() * parts[1].trim().toDouble()).toString()
                    } else if (clean.contains("/")) {
                        val parts = clean.split("/")
                        (parts[0].trim().toDouble() / parts[1].trim().toDouble()).toString()
                    } else {
                        clean
                    }
                } catch (e: Exception) {
                    "Error"
                }
            }

            Dialog(onDismissRequest = { showCalculatorModal = false }) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(0.92f)
                        .clip(RoundedCornerShape(22.dp))
                        .background(Color(0xFF0F172A))
                        .border(1.dp, Color(0xFF1E293B), RoundedCornerShape(22.dp))
                        .padding(18.dp)
                ) {
                    Column {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Calculate, contentDescription = null, tint = Color(0xFF38BDF8), modifier = Modifier.size(22.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("🧮 VEDRA Scientific Calculator", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                            }
                            IconButton(onClick = { showCalculatorModal = false }) {
                                Icon(Icons.Default.Close, contentDescription = "Close", tint = Color.Gray)
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(14.dp))
                                .background(Color(0xFF1E293B))
                                .padding(14.dp),
                            contentAlignment = Alignment.CenterEnd
                        ) {
                            Column(horizontalAlignment = Alignment.End) {
                                Text(expression.ifEmpty { " " }, color = Color.Gray, fontSize = 12.sp)
                                Text(display, color = Color.White, fontSize = 26.sp, fontWeight = FontWeight.Bold)
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            calcButtons.forEach { row ->
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    row.forEach { btn ->
                                        val isOp = btn in listOf("/", "*", "-", "+", "=", "C")
                                        val btnBg = if (btn == "=") Color(0xFF0284C7) else if (btn == "C") Color(0xFFDC2626) else if (isOp) Color(0xFF334155) else Color(0xFF1E293B)

                                        Box(
                                            modifier = Modifier
                                                .weight(1f)
                                                .height(48.dp)
                                                .clip(RoundedCornerShape(12.dp))
                                                .background(btnBg)
                                                .clickable {
                                                    when (btn) {
                                                        "C" -> {
                                                            display = "0"
                                                            expression = ""
                                                        }
                                                        "=" -> {
                                                            val res = evalCalc(expression)
                                                            display = res
                                                            expression = "$expression = $res"
                                                        }
                                                        else -> {
                                                            if (display == "0") display = btn else display += btn
                                                            expression += btn
                                                        }
                                                    }
                                                },
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text(btn, color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // 7. Quick Notes Modal
        if (showQuickNotesModal) {
            var noteTitle by remember { mutableStateOf("") }
            var noteContent by remember { mutableStateOf("") }

            Dialog(onDismissRequest = { showQuickNotesModal = false }) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(0.95f)
                        .clip(RoundedCornerShape(20.dp))
                        .background(Color(0xFF18102B))
                        .border(1.dp, Color(0xFF36215A), RoundedCornerShape(20.dp))
                        .padding(18.dp)
                ) {
                    Column {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Description, contentDescription = null, tint = Color(0xFFA855F7), modifier = Modifier.size(20.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("📝 Quick Study Notes", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                            }
                            IconButton(onClick = { showQuickNotesModal = false }) {
                                Icon(Icons.Default.Close, contentDescription = "Close", tint = Color.Gray)
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        OutlinedTextField(
                            value = noteTitle,
                            onValueChange = { noteTitle = it },
                            placeholder = { Text("Note Title...", color = Color.Gray, fontSize = 12.sp) },
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White,
                                focusedBorderColor = Color(0xFFA855F7),
                                unfocusedBorderColor = Color(0xFF322052)
                            ),
                            modifier = Modifier.fillMaxWidth()
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        OutlinedTextField(
                            value = noteContent,
                            onValueChange = { noteContent = it },
                            placeholder = { Text("Write your quick note here...", color = Color.Gray, fontSize = 12.sp) },
                            maxLines = 5,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White,
                                focusedBorderColor = Color(0xFFA855F7),
                                unfocusedBorderColor = Color(0xFF322052)
                            ),
                            modifier = Modifier.fillMaxWidth().height(100.dp)
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        Button(
                            onClick = {
                                if (noteTitle.isNotBlank() && noteContent.isNotBlank()) {
                                    dbService.createDriveDocument(0L, noteTitle, noteContent, "TXT")
                                    Toast.makeText(context, "Note saved to VEDrive! 📁", Toast.LENGTH_SHORT).show()
                                    showQuickNotesModal = false
                                } else {
                                    Toast.makeText(context, "Please enter title and content", Toast.LENGTH_SHORT).show()
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF8B5CF6)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Save Note to VEDrive", color = Color.White, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        // 8. Quick Tools Modal (More Tools)
        if (showQuickToolsModal) {
            Dialog(onDismissRequest = { showQuickToolsModal = false }) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(0.95f)
                        .clip(RoundedCornerShape(22.dp))
                        .background(Color(0xFF0F0B1E))
                        .border(1.dp, Color(0xFF2E1C50), RoundedCornerShape(22.dp))
                        .padding(18.dp)
                ) {
                    Column {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = Color(0xFFA78BFA), modifier = Modifier.size(20.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("⚡ All VEDRA AI Utilities", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                            }
                            IconButton(onClick = { showQuickToolsModal = false }) {
                                Icon(Icons.Default.Close, contentDescription = "Close", tint = Color.Gray)
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                                SmallActionCard("Summarize", Icons.Default.Description, Color(0xFFA855F7), Modifier.weight(1f)) {
                                    showQuickToolsModal = false
                                    showSummarizeModal = true
                                }
                                SmallActionCard("Translate", Icons.Default.Translate, Color(0xFF3B82F6), Modifier.weight(1f)) {
                                    showQuickToolsModal = false
                                    showTranslateModal = true
                                }
                            }
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                                SmallActionCard("Explain", Icons.Default.Lightbulb, Color(0xFF10B981), Modifier.weight(1f)) {
                                    showQuickToolsModal = false
                                    showExplainModal = true
                                }
                                SmallActionCard("Write", Icons.Default.Edit, Color(0xFFF59E0B), Modifier.weight(1f)) {
                                    showQuickToolsModal = false
                                    showWriterModal = true
                                }
                            }
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                                SmallActionCard("Calculator", Icons.Default.Calculate, Color(0xFF0284C7), Modifier.weight(1f)) {
                                    showQuickToolsModal = false
                                    showCalculatorModal = true
                                }
                                SmallActionCard("Quick Notes", Icons.Default.Description, Color(0xFF8B5CF6), Modifier.weight(1f)) {
                                    showQuickToolsModal = false
                                    showQuickNotesModal = true
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

// ---------------- Helper Components ----------------

@Composable
private fun HomeSectionHeader(
    title: String,
    actionText: String?,
    onAction: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = title,
            color = Color.White,
            fontSize = 15.sp,
            fontWeight = FontWeight.Bold
        )

        if (actionText != null) {
            Text(
                text = actionText,
                color = Color(0xFFA29BFE),
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.clickable { onAction() }
            )
        }
    }
}

@Composable
private fun PulsingWaveformOrb(modifier: Modifier = Modifier) {
    val infiniteTransition = rememberInfiniteTransition()
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 0.85f,
        targetValue = 1.15f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        )
    )

    Box(
        modifier = modifier
            .clip(CircleShape)
            .background(
                Brush.radialGradient(
                    listOf(Color(0xFF8B5CF6).copy(alpha = 0.8f), Color(0xFF2E1065))
                )
            )
            .border(2.dp, Color(0xFFA855F7), CircleShape),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize().padding(12.dp)) {
            val path = Path().apply {
                moveTo(0f, size.height / 2)
                lineTo(size.width * 0.2f, size.height / 2)
                lineTo(size.width * 0.35f, size.height * 0.2f * pulseScale)
                lineTo(size.width * 0.5f, size.height * 0.8f * pulseScale)
                lineTo(size.width * 0.65f, size.height * 0.3f)
                lineTo(size.width * 0.8f, size.height / 2)
                lineTo(size.width, size.height / 2)
            }
            drawPath(
                path = path,
                color = Color.White,
                style = Stroke(width = 2.5.dp.toPx())
            )
        }
    }
}

@Composable
private fun LargeActionCard(
    label: String,
    icon: ImageVector,
    backgroundColor: Color,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Box(
        modifier = modifier
            .height(72.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(backgroundColor)
            .clickable { onClick() }
            .padding(10.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = Color.White,
                modifier = Modifier.size(22.dp)
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = label,
                color = Color.White,
                fontSize = 11.5.sp,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
private fun SmallActionCard(
    label: String,
    icon: ImageVector,
    iconColor: Color,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Box(
        modifier = modifier
            .height(58.dp)
            .clip(RoundedCornerShape(14.dp))
            .background(Color(0xFF130F28))
            .border(1.dp, Color(0xFF261D44), RoundedCornerShape(14.dp))
            .clickable { onClick() }
            .padding(6.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = iconColor,
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = label,
                color = Color.White,
                fontSize = 10.sp,
                fontWeight = FontWeight.Medium,
                maxLines = 1
            )
        }
    }
}

@Composable
private fun RecentChatCardItem(
    iconText: String,
    title: String,
    time: String,
    snippet: String,
    isPinned: Boolean = false,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(Color(0xFF130F2A))
            .border(1.dp, Color(0xFF241B44), RoundedCornerShape(16.dp))
            .clickable { onClick() }
            .padding(12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(Color(0xFF221942)),
                contentAlignment = Alignment.Center
            ) {
                Text(text = iconText, fontSize = 16.sp)
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = title,
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f)
                    )
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = time,
                            color = Color(0xFF64748B),
                            fontSize = 10.5.sp
                        )
                        if (isPinned) {
                            Spacer(modifier = Modifier.width(4.dp))
                            Icon(
                                imageVector = Icons.Default.PushPin,
                                contentDescription = "Pinned",
                                tint = Color(0xFFA855F7),
                                modifier = Modifier.size(12.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(2.dp))

                Text(
                    text = snippet,
                    color = Color(0xFF94A3B8),
                    fontSize = 11.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

@Composable
private fun VEHubMiniCard(
    title: String,
    subtitle: String,
    actionText: String,
    icon: ImageVector,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(14.dp))
            .background(Color(0xFF181236))
            .border(1.dp, Color(0xFF2E2054), RoundedCornerShape(14.dp))
            .clickable { onClick() }
            .padding(8.dp)
    ) {
        Column {
            Icon(
                imageVector = icon,
                contentDescription = title,
                tint = Color(0xFF8B5CF6),
                modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(text = title, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 11.5.sp)
            Text(text = subtitle, color = Color(0xFF94A3B8), fontSize = 9.5.sp, maxLines = 1)
            Spacer(modifier = Modifier.height(4.dp))
            Text(text = actionText, color = Color(0xFFA29BFE), fontWeight = FontWeight.Bold, fontSize = 10.sp)
        }
    }
}

@Composable
private fun StatCardItem(
    label: String,
    value: String,
    trendText: String,
    trendColor: Color,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .background(Color(0xFF130F2A))
            .border(1.dp, Color(0xFF261D46), RoundedCornerShape(16.dp))
            .padding(10.dp)
    ) {
        Column {
            Text(text = label, color = Color(0xFF94A3B8), fontSize = 10.5.sp)
            Spacer(modifier = Modifier.height(4.dp))
            Text(text = value, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 17.sp)
            Spacer(modifier = Modifier.height(4.dp))
            Text(text = trendText, color = trendColor, fontSize = 9.sp, fontWeight = FontWeight.Medium)
        }
    }
}

@Composable
private fun TopToolCard(
    title: String,
    icon: ImageVector,
    iconBg: Color,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Box(
        modifier = modifier
            .height(82.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(Color(0xFF130F2A))
            .border(1.dp, Color(0xFF261D46), RoundedCornerShape(16.dp))
            .clickable { onClick() }
            .padding(8.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(CircleShape)
                    .background(iconBg),
                contentAlignment = Alignment.Center
            ) {
                Icon(imageVector = icon, contentDescription = title, tint = Color.White, modifier = Modifier.size(16.dp))
            }
            Spacer(modifier = Modifier.height(6.dp))
            Text(text = title, color = Color.White, fontSize = 10.5.sp, fontWeight = FontWeight.SemiBold, maxLines = 1)
        }
    }
}

@Composable
private fun AISuggestionItem(
    icon: ImageVector,
    iconBg: Color,
    title: String,
    subtitle: String,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(Color(0xFF130F2A))
            .border(1.dp, Color(0xFF261D46), RoundedCornerShape(16.dp))
            .clickable { onClick() }
            .padding(12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(iconBg),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(imageVector = icon, contentDescription = null, tint = Color.White, modifier = Modifier.size(18.dp))
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(text = title, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 12.5.sp)
                    Text(text = subtitle, color = Color(0xFF94A3B8), fontSize = 10.5.sp)
                }
            }
            Icon(imageVector = Icons.Default.ChevronRight, contentDescription = null, tint = Color(0xFF64748B), modifier = Modifier.size(16.dp))
        }
    }
}

@Composable
private fun SmartReminderItem(
    title: String,
    time: String,
    icon: ImageVector,
    accentColor: Color
) {
    var isNotified by remember { mutableStateOf(true) }
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(Color(0xFF130F2A))
            .border(1.dp, Color(0xFF261D46), RoundedCornerShape(16.dp))
            .padding(12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(34.dp)
                        .clip(CircleShape)
                        .background(accentColor.copy(alpha = 0.2f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(imageVector = icon, contentDescription = null, tint = accentColor, modifier = Modifier.size(18.dp))
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(text = title, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 12.5.sp)
                    Text(text = time, color = Color(0xFF94A3B8), fontSize = 10.5.sp)
                }
            }

            IconButton(
                onClick = { isNotified = !isNotified },
                modifier = Modifier.size(32.dp)
            ) {
                Icon(
                    imageVector = if (isNotified) Icons.Default.Notifications else Icons.Default.NotificationsNone,
                    contentDescription = "Notification Bell",
                    tint = if (isNotified) Color(0xFFA855F7) else Color(0xFF64748B),
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}

@Composable
private fun SystemStatusChip(
    label: String,
    value: String,
    icon: ImageVector,
    statusColor: Color,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(14.dp))
            .background(Color(0xFF130F2A))
            .border(1.dp, Color(0xFF261D46), RoundedCornerShape(14.dp))
            .padding(8.dp)
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(imageVector = icon, contentDescription = label, tint = statusColor, modifier = Modifier.size(18.dp))
            Spacer(modifier = Modifier.height(4.dp))
            Text(text = label, color = Color(0xFF94A3B8), fontSize = 9.5.sp)
            Text(text = value, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 10.5.sp)
        }
    }
}

@Composable
private fun ActivityMonitorRow(
    title: String,
    countText: String,
    lineColor: Color,
    icon: ImageVector
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(Color(0xFF130F2A))
            .border(1.dp, Color(0xFF261D46), RoundedCornerShape(16.dp))
            .padding(12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(imageVector = icon, contentDescription = null, tint = lineColor, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Text(text = title, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    Text(text = countText, color = Color(0xFF94A3B8), fontSize = 10.sp)
                }
            }

            // Sparkline Wave Graphic
            Canvas(modifier = Modifier.size(70.dp, 24.dp)) {
                val path = Path().apply {
                    moveTo(0f, size.height * 0.8f)
                    lineTo(size.width * 0.2f, size.height * 0.3f)
                    lineTo(size.width * 0.4f, size.height * 0.7f)
                    lineTo(size.width * 0.6f, size.height * 0.2f)
                    lineTo(size.width * 0.8f, size.height * 0.6f)
                    lineTo(size.width, size.height * 0.1f)
                }
                drawPath(path = path, color = lineColor, style = Stroke(width = 2.dp.toPx()))
            }
        }
    }
}

@Composable
private fun ShortcutButtonItem(
    label: String,
    icon: ImageVector,
    iconColor: Color,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Box(
        modifier = modifier
            .height(64.dp)
            .clip(RoundedCornerShape(14.dp))
            .background(Color(0xFF130F2A))
            .border(1.dp, Color(0xFF261D46), RoundedCornerShape(14.dp))
            .clickable { onClick() }
            .padding(6.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(imageVector = icon, contentDescription = label, tint = iconColor, modifier = Modifier.size(18.dp))
            Spacer(modifier = Modifier.height(4.dp))
            Text(text = label, color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Medium)
        }
    }
}
