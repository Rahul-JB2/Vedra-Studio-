package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChatBubble
import androidx.compose.material.icons.filled.ChatBubbleOutline
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.HelpOutline
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.PushPin
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.VpnKey
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.services.ChatHistoryItem
import com.example.services.DatabaseService

data class DefaultChatHistory(
    val title: String,
    val time: String,
    val response: String = ""
)

@Composable
fun SideDrawer(
    isOpen: Boolean,
    dbService: DatabaseService? = null,
    onClose: () -> Unit,
    onSelectMenuItem: (String) -> Unit,
    onSelectChatHistoryItem: ((ChatHistoryItem) -> Unit)? = null
) {
    var activeKey by remember { mutableStateOf("ved") }

    val defaultHistories = remember {
        listOf(
            DefaultChatHistory("Explain photosynthesis", "9:30 AM", "Photosynthesis is the process by which plants..."),
            DefaultChatHistory("Create Flashcard", "Yesterday", "Flashcard set generated for Biology chapter 4"),
            DefaultChatHistory("Solve integral problems", "2 days ago", "Step-by-step calculus solution for ∫x² dx"),
            DefaultChatHistory("Quantum mechanics basics", "3 days ago", "Introduction to wave-particle duality and Schrödinger equation"),
            DefaultChatHistory("Study plan for JEE", "4 days ago", "Customized 30-day preparation strategy for physics and math")
        )
    }

    val realChatHistory = remember(isOpen) {
        dbService?.getAllChatHistory() ?: emptyList()
    }

    if (isOpen) {
        Box(modifier = Modifier.fillMaxSize()) {
            // Semi-transparent backdrop overlay
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.70f))
                    .clickable { onClose() }
            )

            // Animated Slide-In Drawer Panel (Scaled to ~75% size: 235dp width)
            AnimatedVisibility(
                visible = isOpen,
                enter = slideInHorizontally(initialOffsetX = { -it }) + fadeIn(),
                exit = slideOutHorizontally(targetOffsetX = { -it }) + fadeOut(),
                modifier = Modifier.align(Alignment.CenterStart)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxHeight()
                        .width(235.dp)
                        .clip(RoundedCornerShape(topEnd = 18.dp, bottomEnd = 18.dp))
                        .background(Color(0xFF0C0915))
                        .border(1.dp, Color(0xFF1E1735), RoundedCornerShape(topEnd = 18.dp, bottomEnd = 18.dp))
                        .padding(horizontal = 12.dp, vertical = 15.dp)
                ) {
                    // 1. TOP HEADER: LOGO, BRANDING & STATUS
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 9.dp, bottom = 13.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            // Waveform Logo Icon Box
                            Box(
                                modifier = Modifier
                                    .size(32.dp)
                                    .clip(RoundedCornerShape(9.dp))
                                    .background(
                                        Brush.linearGradient(
                                            listOf(Color(0xFF1B1235), Color(0xFF2E1A52))
                                        )
                                    )
                                    .border(1.dp, Color(0xFF3B236E), RoundedCornerShape(9.dp)),
                                contentAlignment = Alignment.Center
                            ) {
                                VedMathLogoIconCard(
                                    size = 24.dp,
                                    animated = true,
                                    showBrandText = false
                                )
                            }
                            Spacer(modifier = Modifier.width(9.dp))
                            Column {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = "VEDR",
                                        color = Color.White,
                                        fontWeight = FontWeight.ExtraBold,
                                        fontSize = 15.sp,
                                        letterSpacing = 0.4.sp
                                    )
                                    Text(
                                        text = "A",
                                        color = Color(0xFFA78BFA),
                                        fontWeight = FontWeight.ExtraBold,
                                        fontSize = 15.sp,
                                        letterSpacing = 0.4.sp
                                    )
                                }
                                Text(
                                    text = "Your Smart Assistant ✨",
                                    color = Color(0xFF94A3B8),
                                    fontSize = 8.5.sp,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }

                        // Online Status Pill
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(15.dp))
                                .background(Color(0xFF092918))
                                .border(1.dp, Color(0xFF059669), RoundedCornerShape(15.dp))
                                .padding(horizontal = 7.dp, vertical = 3.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(4.5.dp)
                                        .clip(CircleShape)
                                        .background(Color(0xFF10B981))
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "Online",
                                    color = Color(0xFF34D399),
                                    fontSize = 8.5.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }
                    }

                    // 2. SCROLLABLE DRAWER CONTENT
                    LazyColumn(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(7.dp)
                    ) {
                        // PRIMARY NAVIGATION ITEMS
                        item {
                            DrawerMainItem(
                                title = "Ved Chat",
                                icon = Icons.Default.ChatBubble,
                                isSelected = activeKey == "ved",
                                onClick = {
                                    activeKey = "ved"
                                    onSelectMenuItem("ved")
                                    onClose()
                                }
                            )
                        }

                        item {
                            DrawerMainItem(
                                title = "VEHub",
                                icon = Icons.Default.School,
                                isSelected = activeKey == "vehub",
                                tagText = "formerly Study hub",
                                onClick = {
                                    activeKey = "vehub"
                                    onSelectMenuItem("vehub")
                                    onClose()
                                }
                            )
                        }

                        item {
                            DrawerMainItem(
                                title = "VEDrive",
                                icon = Icons.Default.Folder,
                                isSelected = activeKey == "drive",
                                onClick = {
                                    activeKey = "drive"
                                    onSelectMenuItem("drive")
                                    onClose()
                                }
                            )
                        }

                        item {
                            DrawerMainItem(
                                title = "Automation",
                                icon = Icons.Default.FlashOn,
                                isSelected = activeKey == "automation",
                                onClick = {
                                    activeKey = "automation"
                                    onSelectMenuItem("automation")
                                    onClose()
                                }
                            )
                        }

                        item {
                            DrawerMainItem(
                                title = "Notification",
                                icon = Icons.Default.Notifications,
                                isSelected = activeKey == "notification",
                                badgeText = "3",
                                onClick = {
                                    activeKey = "notification"
                                    onSelectMenuItem("notification")
                                    onClose()
                                }
                            )
                        }

                        // SECTION: RECENT CHAT
                        item {
                            Spacer(modifier = Modifier.height(7.dp))
                            Text(
                                text = "Recent Chat",
                                color = Color(0xFFA78BFA),
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(start = 3.dp, bottom = 3.dp)
                            )
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(9.dp))
                                    .background(Color(0xFF140F24))
                                    .border(1.dp, Color(0xFF261D42), RoundedCornerShape(9.dp))
                                    .clickable {
                                        activeKey = "ved"
                                        onSelectMenuItem("ved")
                                        onClose()
                                    }
                                    .padding(horizontal = 10.dp, vertical = 8.dp)
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(text = "📌", fontSize = 11.5.sp)
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "chat",
                                        color = Color.White,
                                        fontSize = 10.5.sp,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                }
                            }
                        }

                        // SECTION: CHAT HISTORY
                        item {
                            Spacer(modifier = Modifier.height(7.dp))
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 3.dp, vertical = 2.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Chat History",
                                    color = Color(0xFF94A3B8),
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "View all >",
                                    color = Color(0xFFA78BFA),
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.clickable {
                                        activeKey = "ved"
                                        onSelectMenuItem("ved")
                                        onClose()
                                    }
                                )
                            }
                        }

                        // Chat History List Items
                        if (realChatHistory.isNotEmpty()) {
                            items(realChatHistory.take(5)) { item ->
                                ChatHistoryRow(
                                    title = item.sessionTitle,
                                    time = "Today",
                                    onClick = {
                                        activeKey = "ved"
                                        onSelectChatHistoryItem?.invoke(item)
                                        onSelectMenuItem("ved")
                                        onClose()
                                    }
                                )
                            }
                        } else {
                            items(defaultHistories) { item ->
                                ChatHistoryRow(
                                    title = item.title,
                                    time = item.time,
                                    onClick = {
                                        activeKey = "ved"
                                        onSelectChatHistoryItem?.invoke(
                                            ChatHistoryItem(
                                                id = System.currentTimeMillis(),
                                                sessionTitle = item.title,
                                                userText = item.title,
                                                vedResponse = item.response,
                                                timestamp = System.currentTimeMillis()
                                            )
                                        )
                                        onSelectMenuItem("ved")
                                        onClose()
                                    }
                                )
                            }
                        }

                        // SECTION: SYSTEM SETTINGS & UTILITIES
                        item {
                            Spacer(modifier = Modifier.height(10.dp))
                            Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
                                DrawerSystemLink(
                                    title = "Settings",
                                    icon = Icons.Default.Settings,
                                    onClick = {
                                        activeKey = "settings"
                                        onSelectMenuItem("settings")
                                        onClose()
                                    }
                                )
                                DrawerSystemLink(
                                    title = "Privacy & Security",
                                    icon = Icons.Default.Security,
                                    onClick = {
                                        onSelectMenuItem("privacy")
                                        onClose()
                                    }
                                )
                                DrawerSystemLink(
                                    title = "Permissions",
                                    icon = Icons.Default.VpnKey,
                                    onClick = {
                                        onSelectMenuItem("permissions")
                                        onClose()
                                    }
                                )
                                DrawerSystemLink(
                                    title = "Help & Support",
                                    icon = Icons.Default.HelpOutline,
                                    onClick = {
                                        onSelectMenuItem("help")
                                        onClose()
                                    }
                                )
                                DrawerSystemLink(
                                    title = "Feedback",
                                    icon = Icons.Default.Edit,
                                    onClick = {
                                        onSelectMenuItem("feedback")
                                        onClose()
                                    }
                                )
                            }
                        }

                        // BOTTOM PROFILE CARD & FOOTER
                        item {
                            Spacer(modifier = Modifier.height(12.dp))
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(Color(0xFF130E24))
                                    .border(1.dp, Color(0xFF231A3B), RoundedCornerShape(12.dp))
                                    .clickable {
                                        onSelectMenuItem("settings")
                                        onClose()
                                    }
                                    .padding(10.dp)
                            ) {
                                Column {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(31.dp)
                                                .clip(CircleShape)
                                                .background(
                                                    Brush.linearGradient(
                                                        listOf(Color(0xFF8B5CF6), Color(0xFFEC4899))
                                                    )
                                                ),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text(
                                                text = "A",
                                                color = Color.White,
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 13.5.sp
                                            )
                                        }
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Column {
                                            Text(
                                                text = "Arjun Kumar",
                                                color = Color.White,
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 11.sp
                                            )
                                            Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                modifier = Modifier.padding(top = 1.dp)
                                            ) {
                                                Text(
                                                    text = "👑 Premium",
                                                    color = Color(0xFFF59E0B),
                                                    fontSize = 8.5.sp,
                                                    fontWeight = FontWeight.SemiBold
                                                )
                                            }
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(8.dp))

                                    Column(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalAlignment = Alignment.Start
                                    ) {
                                        Text(
                                            text = "Version 1.0.0",
                                            color = Color(0xFF64748B),
                                            fontSize = 8.5.sp,
                                            fontWeight = FontWeight.Medium
                                        )
                                        Text(
                                            text = "©VEDRA.AI",
                                            color = Color(0xFF64748B),
                                            fontSize = 7.5.sp,
                                            fontWeight = FontWeight.Normal
                                        )
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

@Composable
private fun DrawerMainItem(
    title: String,
    icon: ImageVector,
    isSelected: Boolean,
    tagText: String? = null,
    badgeText: String? = null,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(if (isSelected) Color(0xFF231642) else Color(0xFF130E22))
            .border(
                1.dp,
                if (isSelected) Color(0xFF8B5CF6) else Color(0xFF211838),
                RoundedCornerShape(10.dp)
            )
            .clickable { onClick() }
            .padding(horizontal = 10.dp, vertical = 8.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = icon,
                    contentDescription = title,
                    tint = Color(0xFFA78BFA),
                    modifier = Modifier.size(15.dp)
                )
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = title,
                    color = Color.White,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold
                )

                if (tagText != null) {
                    Spacer(modifier = Modifier.width(6.dp))
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(9.dp))
                            .background(Color(0xFF2E1A47))
                            .border(1.dp, Color(0xFF4C2A8A), RoundedCornerShape(9.dp))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = tagText,
                            color = Color(0xFFC084FC),
                            fontSize = 8.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }

            if (badgeText != null) {
                Box(
                    modifier = Modifier
                        .size(16.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF7C3AED)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = badgeText,
                        color = Color.White,
                        fontSize = 8.5.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@Composable
private fun ChatHistoryRow(
    title: String,
    time: String,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .clickable { onClick() }
            .padding(horizontal = 4.dp, vertical = 5.dp)
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
                    imageVector = Icons.Default.ChatBubbleOutline,
                    contentDescription = null,
                    tint = Color(0xFFA78BFA),
                    modifier = Modifier.size(12.dp)
                )
                Spacer(modifier = Modifier.width(7.dp))
                Text(
                    text = title,
                    color = Color.White,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Medium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = time,
                color = Color(0xFF94A3B8),
                fontSize = 8.5.sp,
                fontWeight = FontWeight.Normal
            )
        }
    }
}

@Composable
private fun DrawerSystemLink(
    title: String,
    icon: ImageVector,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .clickable { onClick() }
            .padding(horizontal = 4.dp, vertical = 7.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = icon,
                    contentDescription = title,
                    tint = Color(0xFFA78BFA),
                    modifier = Modifier.size(13.5.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = title,
                    color = Color.White,
                    fontSize = 10.5.sp,
                    fontWeight = FontWeight.Medium
                )
            }
            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = null,
                tint = Color(0xFF64748B),
                modifier = Modifier.size(12.dp)
            )
        }
    }
}
