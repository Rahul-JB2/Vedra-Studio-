package com.example.ui.screens

import android.widget.Toast
import androidx.compose.animation.animateColor
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
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
import com.example.services.BatteryStatus
import com.example.services.DatabaseService
import com.example.services.StorageDetails
import com.example.services.StorageWeatherService
import com.example.services.VoiceService
import com.example.services.OfflineService
import com.example.ui.components.CustomInput

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

    fun refreshDashboardData() {
        battery = StorageWeatherService.getBatteryStatus(context)
        storage = StorageWeatherService.getStorageDetails(context)
    }

    LaunchedEffect(Unit) {
        refreshDashboardData()
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFF07050E))
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(top = 12.dp, bottom = 24.dp),
    ) {
        // Top Bar
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (onOpenDrawer != null) {
                        IconButton(onClick = onOpenDrawer, modifier = Modifier.size(32.dp)) {
                            Icon(imageVector = Icons.Default.Menu, contentDescription = "Menu", tint = Color.White)
                        }
                    }
                    Text(
                        text = "VEDRA",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        modifier = Modifier.padding(start = 8.dp)
                    )
                }
                Row(
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(imageVector = Icons.Default.Notifications, contentDescription = "Notifications", tint = Color.White, modifier = Modifier.size(24.dp))
                    Icon(imageVector = Icons.Default.Settings, contentDescription = "Settings", tint = Color.White, modifier = Modifier.size(24.dp).clickable { onNavigateTab(3) })
                    Icon(imageVector = Icons.Default.Person, contentDescription = "Profile", tint = Color.White, modifier = Modifier.size(24.dp))
                }
            }
        }

        // Header Greeting
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 32.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Good Afternoon, Rahul 👋",
                    color = Color.White,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(24.dp))
                GlassyCard {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        com.example.ui.components.VedOrbView(
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
                            fontWeight = FontWeight.ExtraBold,
                            letterSpacing = 2.sp
                        )
                        Spacer(modifier = Modifier.width(24.dp))
                        VedraStatusIndicator(
                            isListening = voiceService.isListening.value,
                            isSpeaking = voiceService.isSpeaking.value,
                            isThinking = false
                        )
                    }
                }
            }
        }

        // Quick Actions (Horizontal scroll)
        item {
            Column(modifier = Modifier.fillMaxWidth().padding(top = 32.dp)) {
                Text(text = "Quick Actions", color = Color(0xFFA78BFA), fontSize = 15.sp, fontWeight = FontWeight.SemiBold, modifier = Modifier.padding(start = 4.dp))
                Spacer(modifier = Modifier.height(16.dp))
                LazyRow(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    val quickActions = listOf(
                        Triple("Chat", Icons.Default.ChatBubble, Color(0xFF3B82F6)),
                        Triple("Search", Icons.Default.Search, Color(0xFF10B981)),
                        Triple("PDF", Icons.Default.PictureAsPdf, Color(0xFFEF4444)),
                        Triple("Image", Icons.Default.Image, Color(0xFF8B5CF6)),
                        Triple("Study", Icons.Default.School, Color(0xFFF59E0B)),
                        Triple("VEDrive", Icons.Default.Folder, Color(0xFF6366F1)),
                        Triple("Voice", Icons.Default.Mic, Color(0xFFEC4899)),
                        Triple("More", Icons.Default.MoreHoriz, Color(0xFF6B7280))
                    )
                    items(quickActions.size) { index ->
                        val item = quickActions[index]
                        val name = item.first
                        val icon = item.second
                        val color = item.third
                        QuickActionGlassItem(name, icon, color) { 
                            if (name == "Study") onNavigateTab(1)
                            else if (name == "VEDrive") onNavigateTab(2)
                            else if (name == "Voice") onActivateVoice()
                            else onExecuteQuickAction(name.lowercase()) 
                        }
                    }
                }
            }
        }

        // Quick Command (Apps)
        item {
            Column(modifier = Modifier.fillMaxWidth().padding(top = 32.dp)) {
                Text(text = "Quick Command", color = Color(0xFFA78BFA), fontSize = 15.sp, fontWeight = FontWeight.SemiBold, modifier = Modifier.padding(start = 4.dp))
                Spacer(modifier = Modifier.height(16.dp))
                val commands = listOf(
                    Triple("WhatsApp", Icons.Default.Message, Color(0xFF25D366)),
                    Triple("Music", Icons.Default.MusicNote, Color(0xFFF43F5E)),
                    Triple("Calculator", Icons.Default.Calculate, Color(0xFFF97316)),
                    Triple("Weather", Icons.Default.WbSunny, Color(0xFFEAB308)),
                    Triple("Camera", Icons.Default.PhotoCamera, Color(0xFF3B82F6)),
                    Triple("Torch", Icons.Default.FlashlightOn, Color(0xFFFCD34D)),
                    Triple("YouTube", Icons.Default.PlayArrow, Color(0xFFFF0000)),
                    Triple("Call", Icons.Default.Phone, Color(0xFF10B981)),
                    Triple("Clock", Icons.Default.AccessTime, Color(0xFF6366F1)),
                    Triple("Scan", Icons.Default.QrCodeScanner, Color(0xFF8B5CF6))
                )
                // We'll place them in 2 rows using LazyRow or just wrap them.
                LazyRow(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    items(5) { i ->
                        Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                            QuickAppIcon(commands[i].first, commands[i].second, commands[i].third) { onExecuteQuickAction(commands[i].first) }
                            QuickAppIcon(commands[i + 5].first, commands[i + 5].second, commands[i + 5].third) { onExecuteQuickAction(commands[i + 5].first) }
                        }
                    }
                }
            }
        }

        // Continue Section
        item {
            Column(modifier = Modifier.fillMaxWidth().padding(top = 32.dp)) {
                Text(text = "Continue", color = Color(0xFFA78BFA), fontSize = 15.sp, fontWeight = FontWeight.SemiBold, modifier = Modifier.padding(start = 4.dp))
                Spacer(modifier = Modifier.height(12.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    ContinueGlassCard("Last Chat", Icons.Default.History, Modifier.weight(1f)) { onExecuteQuickAction("last chat") }
                    ContinueGlassCard("Resume Research", Icons.Default.Science, Modifier.weight(1f)) { onExecuteQuickAction("resume research") }
                }
            }
        }

        // Recent Files (Horizontal scroll)
        item {
            Column(modifier = Modifier.fillMaxWidth().padding(top = 32.dp)) {
                Text(text = "Recent Files", color = Color(0xFFA78BFA), fontSize = 15.sp, fontWeight = FontWeight.SemiBold, modifier = Modifier.padding(start = 4.dp))
                Spacer(modifier = Modifier.height(12.dp))
                LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    item { RecentFileGlassCard("Physics.pdf", Icons.Default.PictureAsPdf, Color(0xFFEF4444)) { onExecuteQuickAction("open physics.pdf") } }
                    item { RecentFileGlassCard("Notes.md", Icons.Default.EditNote, Color(0xFF10B981)) { onExecuteQuickAction("open notes.md") } }
                    item { RecentFileGlassCard("Diagram.png", Icons.Default.Image, Color(0xFF8B5CF6)) { onExecuteQuickAction("open diagram.png") } }
                }
            }
        }

        // Suggested
        item {
            Column(modifier = Modifier.fillMaxWidth().padding(top = 32.dp)) {
                Text(text = "Suggested", color = Color(0xFFA78BFA), fontSize = 15.sp, fontWeight = FontWeight.SemiBold, modifier = Modifier.padding(start = 4.dp))
                Spacer(modifier = Modifier.height(12.dp))
                GlassyCard {
                    Column(
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        SuggestedGlassItem("Summarize today's notes") { onExecuteQuickAction("Summarize today's notes") }
                        SuggestedGlassItem("Explain a concept") { onExecuteQuickAction("Explain a concept") }
                        SuggestedGlassItem("Practice JEE questions") { onExecuteQuickAction("Practice JEE questions") }
                    }
                }
            }
        }
        
        // Quick Status
        item {
            Column(modifier = Modifier.fillMaxWidth().padding(top = 32.dp)) {
                Text(text = "Quick Status", color = Color(0xFFA78BFA), fontSize = 15.sp, fontWeight = FontWeight.SemiBold, modifier = Modifier.padding(start = 4.dp))
                Spacer(modifier = Modifier.height(12.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    StatusGlassCard("Battery", "${battery.percentage}%", Icons.Default.BatteryChargingFull, if (battery.percentage > 20) Color(0xFF10B981) else Color(0xFFEF4444), Modifier.weight(1f))
                    StatusGlassCard("Storage", "${storage.freeSpaceGB}GB", Icons.Default.Storage, Color(0xFF3B82F6), Modifier.weight(1f))
                }
                Spacer(modifier = Modifier.height(12.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    StatusGlassCard("Memory", "1.4GB Free", Icons.Default.Memory, Color(0xFFF59E0B), Modifier.weight(1f))
                    StatusGlassCard("Network", if (isOnline) "Online" else "Offline", Icons.Default.Wifi, if (isOnline) Color(0xFF10B981) else Color(0xFFEF4444), Modifier.weight(1f))
                }
            }
        }

        // Input Box
        item {
            var inputText by remember { mutableStateOf("") }
            GlassyCard(modifier = Modifier.padding(top = 32.dp, bottom = 16.dp)) {
                CustomInput(
                    value = inputText,
                    onValueChange = { inputText = it },
                    placeholder = "Ask VED...",
                    leadingIcon = Icons.Default.Add,
                    trailingIcon = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            IconButton(onClick = onActivateVoice, modifier = Modifier.size(36.dp)) {
                                Icon(imageVector = Icons.Default.Mic, contentDescription = "Voice Input", tint = Color(0xFFC4B5FD))
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
                                Icon(imageVector = Icons.Default.Send, contentDescription = "Send", tint = Color(0xFFC4B5FD))
                            }
                        }
                    },
                    onSend = { 
                        if (inputText.isNotBlank()) {
                            onExecuteQuickAction(inputText)
                            inputText = ""
                        }
                    },
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 4.dp)
                )
            }
        }
    }
}

@Composable
fun GlassyCard(modifier: Modifier = Modifier, content: @Composable () -> Unit) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(Brush.linearGradient(listOf(Color(0x20FFFFFF), Color(0x05FFFFFF))))
            .border(1.dp, Brush.linearGradient(listOf(Color(0x30FFFFFF), Color(0x05FFFFFF))), RoundedCornerShape(16.dp))
    ) {
        content()
    }
}

@Composable
fun QuickActionGlassItem(label: String, icon: ImageVector, color: Color, onClick: () -> Unit) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.clickable { onClick() }.width(64.dp)
    ) {
        Box(
            modifier = Modifier
                .size(52.dp)
                .clip(CircleShape)
                .background(Brush.linearGradient(listOf(color.copy(alpha = 0.3f), color.copy(alpha = 0.1f))))
                .border(1.dp, color.copy(alpha = 0.5f), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(imageVector = icon, contentDescription = label, tint = color.copy(alpha = 0.9f), modifier = Modifier.size(24.dp))
        }
        Spacer(modifier = Modifier.height(10.dp))
        Text(text = label, color = Color(0xFFE5E7EB), fontSize = 11.5.sp, maxLines = 1, fontWeight = FontWeight.Medium)
    }
}

@Composable
fun QuickAppIcon(label: String, icon: ImageVector, color: Color, onClick: () -> Unit) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.clickable { onClick() }.width(64.dp)
    ) {
        Box(
            modifier = Modifier
                .size(48.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(Brush.linearGradient(listOf(Color(0x25FFFFFF), Color(0x0AFFFFFF))))
                .border(1.dp, Brush.linearGradient(listOf(Color(0x40FFFFFF), Color(0x10FFFFFF))), RoundedCornerShape(12.dp)),
            contentAlignment = Alignment.Center
        ) {
            Icon(imageVector = icon, contentDescription = label, tint = color, modifier = Modifier.size(24.dp))
        }
        Spacer(modifier = Modifier.height(6.dp))
        Text(text = label, color = Color(0xFFD1D5DB), fontSize = 11.sp, maxLines = 1, fontWeight = FontWeight.Medium)
    }
}

@Composable
fun ContinueGlassCard(title: String, icon: ImageVector, modifier: Modifier = Modifier, onClick: () -> Unit) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(14.dp))
            .background(Brush.linearGradient(listOf(Color(0x208B5CF6), Color(0x058B5CF6))))
            .border(1.dp, Color(0x408B5CF6), RoundedCornerShape(14.dp))
            .clickable { onClick() }
            .padding(16.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(modifier = Modifier.size(32.dp).clip(CircleShape).background(Color(0x408B5CF6)), contentAlignment = Alignment.Center) {
                Icon(imageVector = icon, contentDescription = title, tint = Color(0xFFC4B5FD), modifier = Modifier.size(18.dp))
            }
            Spacer(modifier = Modifier.width(12.dp))
            Text(text = title, color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
        }
    }
}

@Composable
fun RecentFileGlassCard(title: String, icon: ImageVector, color: Color, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .width(140.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(Brush.linearGradient(listOf(Color(0x15FFFFFF), Color(0x05FFFFFF))))
            .border(1.dp, Color(0x20FFFFFF), RoundedCornerShape(12.dp))
            .clickable { onClick() }
            .padding(12.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(imageVector = icon, contentDescription = title, tint = color, modifier = Modifier.size(18.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text(text = title, color = Color.White, fontSize = 12.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
        }
    }
}

@Composable
fun SuggestedGlassItem(text: String, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .clickable { onClick() }
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(modifier = Modifier.size(6.dp).clip(CircleShape).background(Color(0xFFA78BFA)))
        Spacer(modifier = Modifier.width(16.dp))
        Text(text = text, color = Color(0xFFE5E7EB), fontSize = 13.5.sp, fontWeight = FontWeight.Medium)
    }
}

@Composable
fun StatusGlassCard(title: String, value: String, icon: ImageVector, color: Color, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(14.dp))
            .background(Brush.linearGradient(listOf(Color(0x15FFFFFF), Color(0x05FFFFFF))))
            .border(1.dp, Color(0x20FFFFFF), RoundedCornerShape(14.dp))
            .padding(16.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(color.copy(alpha = 0.2f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(imageVector = icon, contentDescription = title, tint = color, modifier = Modifier.size(18.dp))
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(text = title, color = Color(0xFF9CA3AF), fontSize = 11.sp)
                Text(text = value, color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
fun VedraStatusIndicator(isListening: Boolean, isSpeaking: Boolean, isThinking: Boolean = false) {
    val infiniteTransition = rememberInfiniteTransition()
    
    val pulseAlpha by infiniteTransition.animateColor(
        initialValue = if (isListening) Color(0xFF34D399) else if (isSpeaking) Color(0xFF67E8F9) else if (isThinking) Color(0xFFC4B5FD) else Color(0xFF9CA3AF),
        targetValue = if (isListening) Color(0xFF059669) else if (isSpeaking) Color(0xFF06B6D4) else if (isThinking) Color(0xFF8B5CF6) else Color(0xFF6B7280),
        animationSpec = infiniteRepeatable(
            animation = tween(1000),
            repeatMode = RepeatMode.Reverse
        )
    )

    Row(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
        repeat(5) {
            Box(modifier = Modifier.size(6.dp).clip(CircleShape).background(pulseAlpha))
        }
    }
}
