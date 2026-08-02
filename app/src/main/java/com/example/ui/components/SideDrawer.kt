package com.example.ui.components

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
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
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.CardGiftcard
import androidx.compose.material.icons.filled.ChatBubble
import androidx.compose.material.icons.filled.ChatBubbleOutline
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.FolderOpen
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.HelpOutline
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material.icons.filled.NotificationsNone
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PushPin
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.StarOutline
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
import com.example.services.ChatHistoryItem
import com.example.services.DatabaseService

@Composable
fun SideDrawer(
    isOpen: Boolean,
    dbService: DatabaseService? = null,
    onClose: () -> Unit,
    onSelectMenuItem: (String) -> Unit,
    onSelectChatHistoryItem: ((ChatHistoryItem) -> Unit)? = null
) {
    val context = LocalContext.current

    var selectedHistoryOptionsItem by remember { mutableStateOf<ChatHistoryItem?>(null) }
    var historyToRename by remember { mutableStateOf<ChatHistoryItem?>(null) }
    var renameInputText by remember { mutableStateOf("") }
    var showEditProfileDialog by remember { mutableStateOf(false) }
    var showLogoutDialog by remember { mutableStateOf(false) }

    val userName = remember(isOpen, dbService?.settingsVersion?.intValue) {
        dbService?.getSetting("user_name", "Rahul Kumar") ?: "Rahul Kumar"
    }
    val userEmail = remember(isOpen, dbService?.settingsVersion?.intValue) {
        dbService?.getSetting("user_email", "rahul.kumar@email.com") ?: "rahul.kumar@email.com"
    }

    var editedNameInput by remember { mutableStateOf(userName) }

    if (isOpen) {
        Box(modifier = Modifier.fillMaxSize()) {
            // Semi-transparent backdrop overlay
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.65f))
                    .clickable { onClose() }
            )

            // Animated Slide-In Drawer Panel
            AnimatedVisibility(
                visible = isOpen,
                enter = slideInHorizontally(initialOffsetX = { -it }) + fadeIn(),
                exit = slideOutHorizontally(targetOffsetX = { -it }) + fadeOut(),
                modifier = Modifier.align(Alignment.CenterStart)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxHeight()
                        .widthIn(max = 310.dp)
                        .clip(RoundedCornerShape(topEnd = 24.dp, bottomEnd = 24.dp))
                        .background(Color(0xFF0D0B1A))
                        .border(1.dp, Color(0xFF221B38), RoundedCornerShape(topEnd = 24.dp, bottomEnd = 24.dp))
                        .padding(horizontal = 16.dp, vertical = 18.dp)
                ) {
                    // 1. TOP HEADER ROW: [Avatar Orb] VEDRA AI / Your AI Companion / Online  [X Close]
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            // Glowing Orb Logo
                            Box(
                                modifier = Modifier
                                    .size(42.dp)
                                    .clip(CircleShape)
                                    .background(
                                        Brush.radialGradient(
                                            listOf(Color(0xFF8B5CF6), Color(0xFF3B0764))
                                        )
                                    )
                                    .border(1.5.dp, Color(0xFFA855F7), CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                VedMathLogoCanvas(
                                    modifier = Modifier.size(28.dp),
                                    animated = true
                                )
                            }

                            Spacer(modifier = Modifier.width(10.dp))

                            Column {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = "VEDRA",
                                        color = Color.White,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 17.sp,
                                        letterSpacing = 0.5.sp
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(6.dp))
                                            .background(Color(0xFF6D28D9))
                                            .padding(horizontal = 6.dp, vertical = 2.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = "AI",
                                            color = Color.White,
                                            fontWeight = FontWeight.ExtraBold,
                                            fontSize = 11.sp
                                        )
                                    }
                                }

                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.padding(top = 2.dp)
                                ) {
                                    Text(
                                        text = "Your AI Companion",
                                        color = Color(0xFFA29BFE),
                                        fontSize = 11.5.sp
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Box(
                                        modifier = Modifier
                                            .size(5.dp)
                                            .clip(CircleShape)
                                            .background(Color(0xFF10B981))
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = "Online",
                                        color = Color(0xFF10B981),
                                        fontSize = 10.5.sp,
                                        fontWeight = FontWeight.Medium
                                    )
                                }
                            }
                        }

                        // Close 'X' Button
                        IconButton(
                            onClick = { onClose() },
                            modifier = Modifier
                                .size(32.dp)
                                .clip(CircleShape)
                                .background(Color(0xFF1B1532))
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Close Drawer",
                                tint = Color(0xFFA29BFE),
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }

                    // 2. USER PROFILE CARD (Rahul Kumar, email, Premium badge)
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(18.dp))
                            .background(Color(0xFF16122C))
                            .border(1.dp, Color(0xFF28204A), RoundedCornerShape(18.dp))
                            .clickable {
                                editedNameInput = userName
                                showEditProfileDialog = true
                            }
                            .padding(12.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                modifier = Modifier.weight(1f),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                // User Avatar Circle
                                Box(
                                    modifier = Modifier
                                        .size(38.dp)
                                        .clip(CircleShape)
                                        .background(Color(0xFF2A1F52)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Person,
                                        contentDescription = "User Avatar",
                                        tint = Color(0xFFA855F7),
                                        modifier = Modifier.size(22.dp)
                                    )
                                }

                                Spacer(modifier = Modifier.width(10.dp))

                                Column {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(
                                            text = userName,
                                            color = Color.White,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 13.5.sp,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                        Spacer(modifier = Modifier.width(5.dp))
                                        Icon(
                                            imageVector = Icons.Default.Edit,
                                            contentDescription = "Edit Name",
                                            tint = Color(0xFFA29BFE),
                                            modifier = Modifier.size(12.dp)
                                        )
                                    }

                                    Text(
                                        text = userEmail,
                                        color = Color(0xFF94A3B8),
                                        fontSize = 10.5.sp,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )

                                    Spacer(modifier = Modifier.height(4.dp))

                                    // "Premium" Badge
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(12.dp))
                                            .background(Color(0xFF231745))
                                            .border(1.dp, Color(0xFF4C2A8A), RoundedCornerShape(12.dp))
                                            .padding(horizontal = 8.dp, vertical = 2.dp)
                                    ) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Text(
                                                text = "👑",
                                                fontSize = 9.sp
                                            )
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text(
                                                text = "Premium",
                                                color = Color(0xFFD8B4FE),
                                                fontSize = 9.5.sp,
                                                fontWeight = FontWeight.SemiBold
                                            )
                                        }
                                    }
                                }
                            }

                            Icon(
                                imageVector = Icons.Default.ChevronRight,
                                contentDescription = "Profile Details",
                                tint = Color(0xFFA29BFE),
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // 3. MAIN ACTION ITEM: "+ New Chat"
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(16.dp))
                            .background(Color(0xFF1E173D))
                            .border(1.dp, Color(0xFF36286B), RoundedCornerShape(16.dp))
                            .clickable {
                                onSelectMenuItem("new_chat")
                                onClose()
                                Toast.makeText(context, "New Chat Started ✨", Toast.LENGTH_SHORT).show()
                            }
                            .padding(horizontal = 14.dp, vertical = 12.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.ChatBubbleOutline,
                                    contentDescription = "New Chat",
                                    tint = Color(0xFFA855F7),
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                Text(
                                    text = "New Chat",
                                    color = Color.White,
                                    fontSize = 13.5.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                            Icon(
                                imageVector = Icons.Default.ChevronRight,
                                contentDescription = "Start New Chat",
                                tint = Color(0xFFA855F7),
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // 4. SCROLLABLE MENU ITEMS
                    LazyColumn(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(2.dp)
                    ) {
                        // UNLABELED TOP MENU GROUP
                        item {
                            DrawerRowMenuItem(
                                title = "History",
                                icon = Icons.Default.History,
                                onClick = {
                                    onSelectMenuItem("history")
                                    onClose()
                                }
                            )
                        }

                        item {
                            DrawerRowMenuItem(
                                title = "Saved Chats",
                                icon = Icons.Default.BookmarkBorder,
                                onClick = {
                                    onSelectMenuItem("saved_chats")
                                    onClose()
                                }
                            )
                        }

                        item {
                            DrawerRowMenuItem(
                                title = "Notes",
                                icon = Icons.Default.Description,
                                onClick = {
                                    onSelectMenuItem("notes")
                                    onClose()
                                }
                            )
                        }

                        item {
                            DrawerRowMenuItem(
                                title = "Reminders",
                                icon = Icons.Default.NotificationsNone,
                                onClick = {
                                    onSelectMenuItem("reminders")
                                    onClose()
                                }
                            )
                        }

                        // SECTION: VEDRIVE
                        item {
                            DrawerSectionHeader(title = "VEDRIVE")
                        }

                        item {
                            DrawerRowMenuItem(
                                title = "My Folders",
                                icon = Icons.Default.FolderOpen,
                                onClick = {
                                    onSelectMenuItem("drive_folders")
                                    onClose()
                                }
                            )
                        }

                        item {
                            DrawerRowMenuItem(
                                title = "Recent Files",
                                icon = Icons.Default.AccessTime,
                                onClick = {
                                    onSelectMenuItem("drive_recent")
                                    onClose()
                                }
                            )
                        }

                        item {
                            DrawerRowMenuItem(
                                title = "Shared with Me",
                                icon = Icons.Default.People,
                                onClick = {
                                    onSelectMenuItem("drive_shared")
                                    onClose()
                                }
                            )
                        }

                        item {
                            DrawerRowMenuItem(
                                title = "Starred",
                                icon = Icons.Default.StarOutline,
                                onClick = {
                                    onSelectMenuItem("drive_starred")
                                    onClose()
                                }
                            )
                        }

                        item {
                            DrawerRowMenuItem(
                                title = "Recycle Bin",
                                icon = Icons.Default.DeleteOutline,
                                onClick = {
                                    onSelectMenuItem("drive_bin")
                                    onClose()
                                }
                            )
                        }

                        // SECTION: TOOLS & FEATURES
                        item {
                            DrawerSectionHeader(title = "TOOLS & FEATURES")
                        }

                        item {
                            DrawerRowMenuItem(
                                title = "AI Tools",
                                icon = Icons.Default.AutoAwesome,
                                badgeText = "New",
                                onClick = {
                                    onSelectMenuItem("ai_tools")
                                    onClose()
                                }
                            )
                        }

                        item {
                            DrawerRowMenuItem(
                                title = "Voice Engine",
                                icon = Icons.Default.GraphicEq,
                                onClick = {
                                    onSelectMenuItem("voice_engine")
                                    onClose()
                                }
                            )
                        }

                        item {
                            DrawerRowMenuItem(
                                title = "Image Generator",
                                icon = Icons.Default.Image,
                                onClick = {
                                    onSelectMenuItem("image_gen")
                                    onClose()
                                }
                            )
                        }

                        item {
                            DrawerRowMenuItem(
                                title = "Web Search",
                                icon = Icons.Default.Language,
                                onClick = {
                                    onSelectMenuItem("web_search")
                                    onClose()
                                }
                            )
                        }

                        item {
                            DrawerRowMenuItem(
                                title = "Code Assistant",
                                icon = Icons.Default.Code,
                                onClick = {
                                    onSelectMenuItem("code_assistant")
                                    onClose()
                                }
                            )
                        }

                        // SECTION: SETTINGS & MORE
                        item {
                            DrawerSectionHeader(title = "SETTINGS & MORE")
                        }

                        item {
                            DrawerRowMenuItem(
                                title = "Settings",
                                icon = Icons.Default.Settings,
                                onClick = {
                                    onSelectMenuItem("settings")
                                    onClose()
                                }
                            )
                        }

                        item {
                            DrawerRowMenuItem(
                                title = "Appearance",
                                icon = Icons.Default.Palette,
                                onClick = {
                                    onSelectMenuItem("appearance")
                                    onClose()
                                }
                            )
                        }

                        item {
                            DrawerRowMenuItem(
                                title = "Help & Support",
                                icon = Icons.Default.HelpOutline,
                                onClick = {
                                    onSelectMenuItem("help")
                                    onClose()
                                }
                            )
                        }

                        item {
                            DrawerRowMenuItem(
                                title = "What's New",
                                icon = Icons.Default.CardGiftcard,
                                onClick = {
                                    onSelectMenuItem("whats_new")
                                    onClose()
                                }
                            )
                        }

                        // LOG OUT ITEM
                        item {
                            Spacer(modifier = Modifier.height(8.dp))
                            DrawerRowMenuItem(
                                title = "Log Out",
                                icon = Icons.Default.Logout,
                                textColor = Color(0xFFEF4444),
                                iconColor = Color(0xFFEF4444),
                                hideChevron = true,
                                onClick = {
                                    showLogoutDialog = true
                                }
                            )
                        }
                    }
                }
            }
        }

        // Edit Profile Name Dialog
        if (showEditProfileDialog) {
            AlertDialog(
                onDismissRequest = { showEditProfileDialog = false },
                containerColor = Color(0xFF141026),
                title = {
                    Text("User Profile Settings", color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Text("Update your display name:", color = Color(0xFFA09EC0), fontSize = 12.sp)
                        OutlinedTextField(
                            value = editedNameInput,
                            onValueChange = { editedNameInput = it },
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White,
                                focusedBorderColor = Color(0xFF8B5CF6),
                                unfocusedBorderColor = Color(0xFF28204A),
                                focusedContainerColor = Color(0xFF1B1634),
                                unfocusedContainerColor = Color(0xFF1B1634)
                            ),
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            if (editedNameInput.isNotBlank()) {
                                dbService?.setSetting("user_name", editedNameInput.trim())
                                Toast.makeText(context, "Profile updated!", Toast.LENGTH_SHORT).show()
                                showEditProfileDialog = false
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF8B5CF6))
                    ) {
                        Text("Save", color = Color.White)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showEditProfileDialog = false }) {
                        Text("Cancel", color = Color.Gray)
                    }
                }
            )
        }

        // Logout Confirmation Dialog
        if (showLogoutDialog) {
            AlertDialog(
                onDismissRequest = { showLogoutDialog = false },
                containerColor = Color(0xFF141026),
                title = {
                    Text("Log Out", color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                },
                text = {
                    Text("Are you sure you want to log out of VEDRA AI?", color = Color(0xFFA09EC0), fontSize = 12.5.sp)
                },
                confirmButton = {
                    Button(
                        onClick = {
                            Toast.makeText(context, "Logged out successfully", Toast.LENGTH_SHORT).show()
                            showLogoutDialog = false
                            onClose()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEF4444))
                    ) {
                        Text("Log Out", color = Color.White, fontWeight = FontWeight.Bold)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showLogoutDialog = false }) {
                        Text("Cancel", color = Color.Gray)
                    }
                }
            )
        }
    }
}

// Section Header Component (e.g. VEDRIVE, TOOLS & FEATURES, SETTINGS & MORE)
@Composable
private fun DrawerSectionHeader(title: String) {
    Text(
        text = title,
        color = Color(0xFF6455A4),
        fontSize = 10.5.sp,
        fontWeight = FontWeight.Bold,
        letterSpacing = 0.6.sp,
        modifier = Modifier.padding(start = 6.dp, top = 14.dp, bottom = 4.dp)
    )
}

// Menu Row Item Component
@Composable
private fun DrawerRowMenuItem(
    title: String,
    icon: ImageVector,
    textColor: Color = Color.White,
    iconColor: Color = Color(0xFFA855F7),
    badgeText: String? = null,
    hideChevron: Boolean = false,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .clickable { onClick() }
            .padding(horizontal = 8.dp, vertical = 10.dp)
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
                    tint = iconColor,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = title,
                    color = textColor,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium
                )
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                if (badgeText != null) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(10.dp))
                            .background(Color(0xFF3B2875))
                            .padding(horizontal = 7.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = badgeText,
                            color = Color(0xFFD8B4FE),
                            fontSize = 9.5.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Spacer(modifier = Modifier.width(6.dp))
                }

                if (!hideChevron) {
                    Icon(
                        imageVector = Icons.Default.ChevronRight,
                        contentDescription = null,
                        tint = Color(0xFF53487A),
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }
    }
}
