package com.example.ui.screens

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.provider.Settings
import android.widget.Toast
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddAPhoto
import androidx.compose.material.icons.filled.Alarm
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.CloudUpload
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.EditNote
import androidx.compose.material.icons.filled.Event
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.NearMe
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material.icons.filled.RecordVoiceOver
import androidx.compose.material.icons.filled.RemoveRedEye
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat

data class PermissionCategoryData(
    val id: String,
    val name: String,
    val subtitle: String,
    val icon: ImageVector,
    val iconBgColor: Color,
    val iconTintColor: Color,
    val description: String,
    val systemPermissions: List<String>,
    val usesList: List<Pair<ImageVector, String>>,
    val recentAccessLabel: String,
    val recentAccessTime: String
)

@Composable
fun PermissionsManagementScreen(
    onBack: () -> Unit = {}
) {
    val context = LocalContext.current

    // Category Definitions matching screenshot
    val categories = remember {
        listOf(
            PermissionCategoryData(
                id = "camera",
                name = "Camera",
                subtitle = "For scanning, photos & uploads",
                icon = Icons.Default.CameraAlt,
                iconBgColor = Color(0xFF3B1E6D),
                iconTintColor = Color(0xFFC084FC),
                description = "Allows VEDRA AI to access your camera for scanning QR codes, taking photos and uploading images.",
                systemPermissions = listOf(android.Manifest.permission.CAMERA),
                usesList = listOf(
                    Icons.Default.QrCodeScanner to "Scan QR codes & documents",
                    Icons.Default.AddAPhoto to "Take photos for uploads",
                    Icons.Default.RemoveRedEye to "Live camera for AI vision"
                ),
                recentAccessLabel = "QR Code Scan",
                recentAccessTime = "Today, 10:45 AM"
            ),
            PermissionCategoryData(
                id = "microphone",
                name = "Microphone",
                subtitle = "For voice input & commands",
                icon = Icons.Default.Mic,
                iconBgColor = Color(0xFF1E3A8A),
                iconTintColor = Color(0xFF60A5FA),
                description = "Allows VEDRA AI to access your microphone for voice commands, voice chat and audio input.",
                systemPermissions = listOf(android.Manifest.permission.RECORD_AUDIO),
                usesList = listOf(
                    Icons.Default.Mic to "Voice commands & interaction",
                    Icons.Default.RecordVoiceOver to "Voice messages & replies",
                    Icons.Default.Description to "Voice note transcription"
                ),
                recentAccessLabel = "Voice Command",
                recentAccessTime = "Today, 09:30 AM"
            ),
            PermissionCategoryData(
                id = "storage",
                name = "Files & Storage",
                subtitle = "For reading & saving files",
                icon = Icons.Default.Folder,
                iconBgColor = Color(0xFF78350F),
                iconTintColor = Color(0xFFFBBF24),
                description = "Allows VEDRA AI to access your files and storage to read, save and manage documents.",
                systemPermissions = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    listOf(
                        android.Manifest.permission.READ_MEDIA_IMAGES,
                        android.Manifest.permission.READ_MEDIA_AUDIO,
                        android.Manifest.permission.READ_MEDIA_VIDEO
                    )
                } else {
                    listOf(android.Manifest.permission.READ_EXTERNAL_STORAGE)
                },
                usesList = listOf(
                    Icons.Default.Description to "Read documents & PDFs",
                    Icons.Default.Download to "Save files & downloads",
                    Icons.Default.CloudUpload to "Upload files to VEDrive"
                ),
                recentAccessLabel = "VEDrive File Access",
                recentAccessTime = "Yesterday, 04:15 PM"
            ),
            PermissionCategoryData(
                id = "location",
                name = "Location",
                subtitle = "For location based features",
                icon = Icons.Default.LocationOn,
                iconBgColor = Color(0xFF4C1D95),
                iconTintColor = Color(0xFFA78BFA),
                description = "Allows VEDRA AI to access your location for location based features and better suggestions.",
                systemPermissions = listOf(
                    android.Manifest.permission.ACCESS_FINE_LOCATION,
                    android.Manifest.permission.ACCESS_COARSE_LOCATION
                ),
                usesList = listOf(
                    Icons.Default.NearMe to "Find nearby services",
                    Icons.Default.Alarm to "Location based reminders",
                    Icons.Default.Lightbulb to "Improve local suggestions"
                ),
                recentAccessLabel = "Nearby Search",
                recentAccessTime = "Today, 08:12 AM"
            ),
            PermissionCategoryData(
                id = "contacts",
                name = "Contacts",
                subtitle = "For connecting with people",
                icon = Icons.Default.Person,
                iconBgColor = Color(0xFF064E3B),
                iconTintColor = Color(0xFF34D399),
                description = "Allows VEDRA AI to access your contacts to send messages, connect with people, and call.",
                systemPermissions = listOf(android.Manifest.permission.READ_CONTACTS),
                usesList = listOf(
                    Icons.Default.People to "Identify contact names in chats",
                    Icons.Default.Phone to "Voice call shortcuts",
                    Icons.Default.Send to "Send quick messages & invites"
                ),
                recentAccessLabel = "Contact Lookup",
                recentAccessTime = "2 days ago, 11:20 AM"
            ),
            PermissionCategoryData(
                id = "notifications",
                name = "Notifications",
                subtitle = "For alerts & reminders",
                icon = Icons.Default.Notifications,
                iconBgColor = Color(0xFF7F1D1D),
                iconTintColor = Color(0xFFF87171),
                description = "Allows VEDRA AI to post notifications for reminders, study alerts, and background status updates.",
                systemPermissions = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    listOf(android.Manifest.permission.POST_NOTIFICATIONS)
                } else emptyList(),
                usesList = listOf(
                    Icons.Default.Alarm to "Study timer alerts & reminders",
                    Icons.Default.AutoAwesome to "AI task updates",
                    Icons.Default.VolumeUp to "Background voice response alerts"
                ),
                recentAccessLabel = "Study Reminder Alert",
                recentAccessTime = "Today, 07:00 AM"
            ),
            PermissionCategoryData(
                id = "phone",
                name = "Phone & Calls",
                subtitle = "For voice calls & dialer shortcuts",
                icon = Icons.Default.Phone,
                iconBgColor = Color(0xFF155E75),
                iconTintColor = Color(0xFF38BDF8),
                description = "Allows VEDRA AI to place phone calls and handle direct hands-free dialer actions.",
                systemPermissions = listOf(android.Manifest.permission.CALL_PHONE),
                usesList = listOf(
                    Icons.Default.Phone to "Direct voice calling",
                    Icons.Default.Call to "Hands-free phone dialer",
                    Icons.Default.Warning to "Emergency quick calls"
                ),
                recentAccessLabel = "Dialer Action",
                recentAccessTime = "3 days ago, 02:40 PM"
            ),
            PermissionCategoryData(
                id = "calendar",
                name = "Calendar & Events",
                subtitle = "For study schedules & alarms",
                icon = Icons.Default.CalendarToday,
                iconBgColor = Color(0xFF7C2D12),
                iconTintColor = Color(0xFFFB923C),
                description = "Allows VEDRA AI to sync your study schedule, set calendar reminders, and manage events.",
                systemPermissions = listOf(
                    android.Manifest.permission.READ_CALENDAR,
                    android.Manifest.permission.WRITE_CALENDAR
                ),
                usesList = listOf(
                    Icons.Default.CalendarToday to "Sync study schedule",
                    Icons.Default.Event to "Exam & deadline reminders",
                    Icons.Default.EditNote to "Auto-create calendar events"
                ),
                recentAccessLabel = "Schedule Sync",
                recentAccessTime = "Today, 10:00 AM"
            )
        )
    }

    // Permission state map (true = Granted, false = Denied)
    val grantedMap = remember { mutableStateMapOf<String, Boolean>() }

    fun refreshPermissions() {
        categories.forEach { cat ->
            if (cat.systemPermissions.isEmpty()) {
                grantedMap[cat.id] = true
            } else {
                val isGranted = cat.systemPermissions.all { perm ->
                    ContextCompat.checkSelfPermission(context, perm) == PackageManager.PERMISSION_GRANTED
                }
                grantedMap[cat.id] = isGranted
            }
        }
    }

    LaunchedEffect(Unit) {
        refreshPermissions()
    }

    var selectedCategory by remember { mutableStateOf<PermissionCategoryData?>(null) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF090715)) // Deep dark purple background matching screenshot
    ) {
        if (selectedCategory == null) {
            // Main Permissions Overview Screen
            PermissionsMainOverviewScreen(
                categories = categories,
                grantedMap = grantedMap,
                onSelectCategory = { selectedCategory = it },
                onBack = onBack
            )
        } else {
            // Permission Details Sub-Screen
            PermissionDetailsSubScreen(
                category = selectedCategory!!,
                isGranted = grantedMap[selectedCategory!!.id] ?: false,
                onBack = {
                    refreshPermissions()
                    selectedCategory = null
                },
                onPermissionStateChanged = { newGranted ->
                    grantedMap[selectedCategory!!.id] = newGranted
                },
                onRefresh = { refreshPermissions() }
            )
        }
    }
}

// ==================== 1. MAIN OVERVIEW SCREEN ====================
@Composable
private fun PermissionsMainOverviewScreen(
    categories: List<PermissionCategoryData>,
    grantedMap: Map<String, Boolean>,
    onSelectCategory: (PermissionCategoryData) -> Unit,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val totalCount = 18
    val allowedCount = categories.count { grantedMap[it.id] == true } * 2 + 2
    val deniedCount = totalCount - allowedCount

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(top = 12.dp, bottom = 32.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Top App Bar
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = onBack, modifier = Modifier.size(36.dp)) {
                        Icon(
                            imageVector = Icons.Default.Menu,
                            contentDescription = "Menu",
                            tint = Color.White
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "VEDRA",
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 18.sp
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(
                                        Brush.horizontalGradient(
                                            listOf(Color(0xFF8B5CF6), Color(0xFF6366F1))
                                        )
                                    )
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = "AI",
                                    color = Color.White,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                        Text(
                            text = "Your AI Companion",
                            color = Color(0xFF94A3B8),
                            fontSize = 11.sp
                        )
                    }
                }

                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    IconButton(
                        onClick = { Toast.makeText(context, "Search permissions...", Toast.LENGTH_SHORT).show() },
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(Icons.Default.Search, contentDescription = "Search", tint = Color.White)
                    }
                    IconButton(
                        onClick = { Toast.makeText(context, "Permissions notifications active", Toast.LENGTH_SHORT).show() },
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(Icons.Default.Notifications, contentDescription = "Notifications", tint = Color.White)
                    }
                }
            }
        }

        // Header Title with Shield Graphic (Matching top right image)
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Permissions",
                        color = Color.White,
                        fontSize = 26.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Manage what VEDRA AI can access",
                        color = Color(0xFF94A3B8),
                        fontSize = 13.sp
                    )
                }

                // Glowing Shield Orb Graphic
                Box(
                    modifier = Modifier
                        .size(72.dp)
                        .clip(CircleShape)
                        .background(
                            Brush.radialGradient(
                                colors = listOf(Color(0xFF6D28D9).copy(alpha = 0.6f), Color.Transparent)
                            )
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Box(
                        modifier = Modifier
                            .size(52.dp)
                            .clip(RoundedCornerShape(18.dp))
                            .background(
                                Brush.linearGradient(
                                    colors = listOf(Color(0xFF2E1A47), Color(0xFF190F2C))
                                )
                            )
                            .border(1.5.dp, Color(0xFF8B5CF6), RoundedCornerShape(18.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Lock,
                            contentDescription = "Lock",
                            tint = Color(0xFFD8B4FE),
                            modifier = Modifier.size(26.dp)
                        )
                    }
                }
            }
        }

        // Summary Stats Cards Row (Matching 18 All, 14 Allowed, 4 Denied)
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Card 1: Total
                SummaryCardItem(
                    label = "Total Permissions",
                    countStr = "$totalCount",
                    badge = "All",
                    icon = Icons.Default.Shield,
                    iconBg = Color(0xFF2D1B54),
                    iconTint = Color(0xFFA78BFA),
                    countColor = Color.White,
                    modifier = Modifier.weight(1f)
                )

                // Card 2: Allowed
                SummaryCardItem(
                    label = "Allowed",
                    countStr = "$allowedCount",
                    badge = null,
                    icon = Icons.Default.Check,
                    iconBg = Color(0xFF064E3B),
                    iconTint = Color(0xFF10B981),
                    countColor = Color(0xFF10B981),
                    modifier = Modifier.weight(1f)
                )

                // Card 3: Denied
                SummaryCardItem(
                    label = "Denied",
                    countStr = "$deniedCount",
                    badge = null,
                    icon = Icons.Default.Close,
                    iconBg = Color(0xFF450A0A),
                    iconTint = Color(0xFFEF4444),
                    countColor = Color(0xFFEF4444),
                    modifier = Modifier.weight(1f)
                )
            }
        }

        // Section Title: Permission Categories
        item {
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Permission Categories",
                color = Color.White,
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold
            )
        }

        // Permission Categories List Items
        items(categories) { cat ->
            val isAllowed = grantedMap[cat.id] ?: false
            PermissionCategoryRow(
                category = cat,
                isAllowed = isAllowed,
                onClick = { onSelectCategory(cat) }
            )
        }
    }
}

// Summary Card Row Item Component
@Composable
private fun SummaryCardItem(
    label: String,
    countStr: String,
    badge: String?,
    icon: ImageVector,
    iconBg: Color,
    iconTint: Color,
    countColor: Color,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .background(Color(0xFF120E26))
            .border(1.dp, Color(0xFF271B48), RoundedCornerShape(16.dp))
            .padding(12.dp)
    ) {
        Column {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(26.dp)
                        .clip(CircleShape)
                        .background(iconBg),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = iconTint,
                        modifier = Modifier.size(14.dp)
                    )
                }
            }
            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text = label,
                color = Color(0xFF94A3B8),
                fontSize = 10.sp,
                fontWeight = FontWeight.Medium
            )
            Spacer(modifier = Modifier.height(2.dp))
            Row(verticalAlignment = Alignment.Bottom) {
                Text(
                    text = countStr,
                    color = countColor,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
                if (badge != null) {
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = badge,
                        color = Color(0xFF64748B),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Normal,
                        modifier = Modifier.padding(bottom = 2.dp)
                    )
                }
            }
        }
    }
}

// Permission Category List Row Component
@Composable
private fun PermissionCategoryRow(
    category: PermissionCategoryData,
    isAllowed: Boolean,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(Color(0xFF130E26))
            .border(1.dp, Color(0xFF271C4A), RoundedCornerShape(16.dp))
            .clickable { onClick() }
            .padding(14.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Left Icon Container
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(category.iconBgColor),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = category.icon,
                    contentDescription = category.name,
                    tint = category.iconTintColor,
                    modifier = Modifier.size(22.dp)
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            // Title & Subtitle
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = category.name,
                    color = Color.White,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 14.5.sp
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = category.subtitle,
                    color = Color(0xFF94A3B8),
                    fontSize = 11.5.sp
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            // Allowed / Denied Pill Tag
            if (isAllowed) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(10.dp))
                        .background(Color(0xFF052E16))
                        .border(1.dp, Color(0xFF14532D), RoundedCornerShape(10.dp))
                        .padding(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = "Allowed",
                        color = Color(0xFF22C55E),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            } else {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(10.dp))
                        .background(Color(0xFF450A0A))
                        .border(1.dp, Color(0xFF7F1D1D), RoundedCornerShape(10.dp))
                        .padding(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = "Denied",
                        color = Color(0xFFEF4444),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.width(6.dp))

            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = "Details",
                tint = Color(0xFF64748B),
                modifier = Modifier.size(18.dp)
            )
        }
    }
}

// ==================== 2. PERMISSION DETAILS SUB-SCREEN ====================
@Composable
private fun PermissionDetailsSubScreen(
    category: PermissionCategoryData,
    isGranted: Boolean,
    onBack: () -> Unit,
    onPermissionStateChanged: (Boolean) -> Unit,
    onRefresh: () -> Unit
) {
    val context = LocalContext.current
    var currentGrantedState by remember { mutableStateOf(isGranted) }
    var selectedAccessLevel by remember { mutableStateOf(if (isGranted) 0 else 2) }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { result ->
        val allGranted = result.values.all { it }
        currentGrantedState = allGranted
        onPermissionStateChanged(allGranted)
        if (allGranted) {
            selectedAccessLevel = 0
            Toast.makeText(context, "${category.name} permission granted! ✅", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(context, "${category.name} permission was not granted.", Toast.LENGTH_SHORT).show()
        }
        onRefresh()
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(top = 12.dp, bottom = 32.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Top Sub-screen Bar
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBack, modifier = Modifier.size(36.dp)) {
                    Icon(
                        imageVector = Icons.Default.ArrowBack,
                        contentDescription = "Back",
                        tint = Color.White
                    )
                }

                Text(
                    text = "Permission Details",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )

                Icon(
                    imageVector = Icons.Default.Shield,
                    contentDescription = "Security",
                    tint = Color(0xFFA78BFA),
                    modifier = Modifier.size(20.dp)
                )
            }
        }

        // Header Card (Large Colored Icon + Permission Title + Description)
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(18.dp))
                    .background(Color(0xFF120E28))
                    .border(1.dp, Color(0xFF2A1E52), RoundedCornerShape(18.dp))
                    .padding(16.dp)
            ) {
                Row(modifier = Modifier.fillMaxWidth()) {
                    // Large Rounded Icon
                    Box(
                        modifier = Modifier
                            .size(56.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .background(category.iconBgColor),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = category.icon,
                            contentDescription = category.name,
                            tint = category.iconTintColor,
                            modifier = Modifier.size(30.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(14.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "${category.name} Permission",
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp
                        )

                        Spacer(modifier = Modifier.height(4.dp))

                        if (currentGrantedState) {
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(Color(0xFF052E16))
                                    .border(1.dp, Color(0xFF14532D), RoundedCornerShape(8.dp))
                                    .padding(horizontal = 8.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = "Allowed",
                                    color = Color(0xFF22C55E),
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        } else {
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(Color(0xFF450A0A))
                                    .border(1.dp, Color(0xFF7F1D1D), RoundedCornerShape(8.dp))
                                    .padding(horizontal = 8.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = "Denied",
                                    color = Color(0xFFEF4444),
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = category.description,
                            color = Color(0xFF94A3B8),
                            fontSize = 12.sp,
                            lineHeight = 17.sp
                        )
                    }
                }
            }
        }

        // Section 1: Access Level (Matching Radio Buttons in Images 2, 3, 4, 5)
        item {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    text = "Access Level",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                )

                // Radio Option 1: Allow while using the app / Allow
                AccessLevelRadioOption(
                    title = if (category.id == "storage") "Allow" else "Allow while using the app",
                    subtitle = if (category.id == "storage") "Full access to files & storage" else "Recommended",
                    isSelected = selectedAccessLevel == 0,
                    isRecommended = true,
                    onClick = {
                        selectedAccessLevel = 0
                        if (category.systemPermissions.isNotEmpty()) {
                            permissionLauncher.launch(category.systemPermissions.toTypedArray())
                        } else {
                            currentGrantedState = true
                            onPermissionStateChanged(true)
                            Toast.makeText(context, "${category.name} allowed", Toast.LENGTH_SHORT).show()
                        }
                    }
                )

                // Radio Option 2: Ask every time
                AccessLevelRadioOption(
                    title = "Ask every time",
                    subtitle = if (category.id == "storage") "Select files & folders" else "Ask when feature is launched",
                    isSelected = selectedAccessLevel == 1,
                    isRecommended = false,
                    onClick = {
                        selectedAccessLevel = 1
                        Toast.makeText(context, "VEDRA AI will ask before using ${category.name}", Toast.LENGTH_SHORT).show()
                    }
                )

                // Radio Option 3: Don't allow
                AccessLevelRadioOption(
                    title = "Don't allow",
                    subtitle = "${category.name} access is blocked",
                    isSelected = selectedAccessLevel == 2,
                    isRecommended = false,
                    onClick = {
                        selectedAccessLevel = 2
                        currentGrantedState = false
                        onPermissionStateChanged(false)
                        try {
                            val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                                data = Uri.fromParts("package", context.packageName, null)
                            }
                            context.startActivity(intent)
                        } catch (e: Exception) {
                            Toast.makeText(context, "${category.name} set to Don't Allow", Toast.LENGTH_SHORT).show()
                        }
                    }
                )
            }
        }

        // Section 2: What VEDRA AI uses this for
        item {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    text = "What VEDRA AI uses this for",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                )

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(Color(0xFF130E26))
                        .border(1.dp, Color(0xFF271C4A), RoundedCornerShape(16.dp))
                ) {
                    Column {
                        category.usesList.forEachIndexed { index, (useIcon, useText) ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        Toast.makeText(context, "$useText active", Toast.LENGTH_SHORT).show()
                                    }
                                    .padding(horizontal = 14.dp, vertical = 12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(32.dp)
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(Color(0xFF1E173B)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = useIcon,
                                        contentDescription = null,
                                        tint = Color(0xFFA78BFA),
                                        modifier = Modifier.size(16.dp)
                                    )
                                }

                                Spacer(modifier = Modifier.width(12.dp))

                                Text(
                                    text = useText,
                                    color = Color.White,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Medium,
                                    modifier = Modifier.weight(1f)
                                )

                                Icon(
                                    imageVector = Icons.Default.ChevronRight,
                                    contentDescription = "Next",
                                    tint = Color(0xFF64748B),
                                    modifier = Modifier.size(18.dp)
                                )
                            }

                            if (index < category.usesList.size - 1) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(1.dp)
                                        .background(Color(0xFF221740))
                                )
                            }
                        }
                    }
                }
            }
        }

        // Section 3: Recent Access (Matching Clock icon + Timestamp + Success)
        item {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    text = "Recent Access",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                )

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(Color(0xFF130E26))
                        .border(1.dp, Color(0xFF271C4A), RoundedCornerShape(16.dp))
                        .padding(14.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(34.dp)
                                .clip(CircleShape)
                                .background(Color(0xFF1C1638)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.History,
                                contentDescription = "History",
                                tint = Color(0xFF94A3B8),
                                modifier = Modifier.size(18.dp)
                            )
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = category.recentAccessTime,
                                color = Color.White,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = category.recentAccessLabel,
                                color = Color(0xFF94A3B8),
                                fontSize = 11.5.sp
                            )
                        }

                        Text(
                            text = "Success",
                            color = Color(0xFF10B981),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }

        // Section 4: Learn more about this permission
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color(0xFF130E26))
                    .border(1.dp, Color(0xFF271C4A), RoundedCornerShape(16.dp))
                    .clickable {
                        Toast.makeText(context, "VEDRA AI Privacy & Security documentation", Toast.LENGTH_SHORT).show()
                    }
                    .padding(14.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(30.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF1E293B)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = "Info",
                            tint = Color(0xFF3B82F6),
                            modifier = Modifier.size(16.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Text(
                        text = "Learn more about this permission",
                        color = Color.White,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.weight(1f)
                    )

                    Icon(
                        imageVector = Icons.Default.ChevronRight,
                        contentDescription = "More",
                        tint = Color(0xFF64748B),
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }
}

// Access Level Radio Option Composable Component
@Composable
private fun AccessLevelRadioOption(
    title: String,
    subtitle: String,
    isSelected: Boolean,
    isRecommended: Boolean,
    onClick: () -> Unit
) {
    val bg = if (isSelected) Color(0xFF1F1745) else Color(0xFF120D24)
    val borderCol = if (isSelected) Color(0xFF6366F1) else Color(0xFF251A45)

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(bg)
            .border(if (isSelected) 1.5.dp else 1.dp, borderCol, RoundedCornerShape(14.dp))
            .clickable { onClick() }
            .padding(14.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (isRecommended && isSelected) {
                        Box(
                            modifier = Modifier
                                .size(6.dp)
                                .clip(CircleShape)
                                .background(Color(0xFFA78BFA))
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                    }
                    Text(
                        text = title,
                        color = Color.White,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 13.5.sp
                    )
                }

                Spacer(modifier = Modifier.height(2.dp))

                Text(
                    text = subtitle,
                    color = if (isSelected) Color(0xFFC4B5FD) else Color(0xFF94A3B8),
                    fontSize = 11.5.sp
                )
            }

            // Radio Indicator
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
                        .border(1.5.dp, Color(0xFF475569), CircleShape)
                )
            }
        }
    }
}
