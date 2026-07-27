package com.example.ui.screens

import android.widget.Toast
import androidx.compose.foundation.ExperimentalFoundationApi
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
import androidx.compose.material.icons.filled.Alarm
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.BatteryFull
import androidx.compose.material.icons.filled.Book
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.EditNote
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.Memory
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.MoreHoriz
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
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
import com.example.services.AppLauncher
import com.example.services.BatteryStatus
import com.example.services.DatabaseService
import com.example.services.StorageDetails
import com.example.services.StorageWeatherService
import com.example.services.UtilityService
import com.example.services.VoiceService
import com.example.ui.components.AppPickerAndLockModal
import com.example.ui.components.CustomInput
import com.example.ui.components.CustomModal

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
    var isQrScannerOpen by remember { mutableStateOf(false) }

    var selectedCardForLock by remember { mutableStateOf<Pair<String, String>?>(null) }
    var isAppLockModalOpen by remember { mutableStateOf(false) }

    fun refreshDashboardData() {
        battery = StorageWeatherService.getBatteryStatus(context)
        storage = StorageWeatherService.getStorageDetails(context)
    }

    LaunchedEffect(Unit) {
        refreshDashboardData()
    }

    fun handleBoxClick(cardKey: String, defaultAction: () -> Unit) {
        val lockedPkg = dbService.getSetting("locked_app_$cardKey", "")
        if (lockedPkg.isNotBlank()) {
            if (!AppLauncher.tryLaunchPackage(context, lockedPkg)) {
                defaultAction()
            }
        } else {
            defaultAction()
        }
    }

    fun handleBoxLongClick(cardKey: String, title: String) {
        selectedCardForLock = cardKey to title
        isAppLockModalOpen = true
    }

    fun getLockedLabel(cardKey: String): String? {
        val label = dbService.getSetting("locked_app_label_$cardKey", "")
        return if (label.isNotBlank()) "Locked: $label" else null
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFF090810))
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(top = 12.dp, bottom = 24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // TOP HEADER BAR: Drawer Icon, VEDRA Title, Bell, Avatar
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = { onOpenDrawer?.invoke() },
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Menu,
                        contentDescription = "Open Drawer",
                        tint = Color.White,
                        modifier = Modifier.size(22.dp)
                    )
                }

                Text(
                    text = "VEDRA",
                    color = Color(0xFFC4B5FD),
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 22.sp,
                    letterSpacing = 1.5.sp
                )

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF16132A)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Notifications,
                            contentDescription = "Notifications",
                            tint = Color.White,
                            modifier = Modifier.size(20.dp)
                        )
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(Color(0xFFFF4D4D))
                                .align(Alignment.TopEnd)
                        )
                    }

                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF2E1A47))
                            .border(1.5.dp, Color(0xFF9D6EFF), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = "User Profile",
                            tint = Color.White,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                }
            }
        }

        // GREETING & ONLINE STATUS
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column {
                    Text(
                        text = "Good Morning, Rahul 👋",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp
                    )
                    Spacer(modifier = Modifier.height(3.dp))
                    Text(
                        text = "I'm Vedra, your AI assistant",
                        color = Color(0xFF9CA3AF),
                        fontSize = 12.sp
                    )
                    Text(
                        text = "Long-press any action box to lock your preferred app!",
                        color = Color(0xFFC4B5FD),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(16.dp))
                        .background(Color(0xFF0F1A15))
                        .border(1.dp, Color(0xFF16382B), RoundedCornerShape(16.dp))
                        .padding(horizontal = 10.dp, vertical = 5.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(7.dp)
                                .clip(CircleShape)
                                .background(Color(0xFF10B981))
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Online",
                            color = Color(0xFF10B981),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }
        }

        // VEDRA SINGLE GLOBAL SEARCH BAR
        item {
            var homeSearchQuery by remember { mutableStateOf("") }
            CustomInput(
                value = homeSearchQuery,
                onValueChange = { homeSearchQuery = it },
                placeholder = "Search apps, files, play songs, or commands...",
                leadingIcon = Icons.Default.Search,
                trailingIcon = {
                    if (homeSearchQuery.isNotBlank()) {
                        IconButton(onClick = {
                            onExecuteQuickAction(homeSearchQuery)
                            homeSearchQuery = ""
                        }) {
                            Icon(
                                imageVector = Icons.Default.ArrowForward,
                                contentDescription = "Execute Search",
                                tint = Color(0xFFA78BFA)
                            )
                        }
                    }
                },
                onSend = {
                    if (homeSearchQuery.isNotBlank()) {
                        onExecuteQuickAction(homeSearchQuery)
                        homeSearchQuery = ""
                    }
                }
            )
        }

        // VEDRA SUGGESTIONS SECTION HEADER
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "VEDRA Suggestions for you",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp
                )
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.clickable { onNavigateTab(2) } // Actions Tab
                ) {
                    Text(
                        text = "View all",
                        color = Color(0xFF818CF8),
                        fontSize = 12.5.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                    Icon(
                        imageVector = Icons.Default.ChevronRight,
                        contentDescription = "View all",
                        tint = Color(0xFF818CF8),
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }

        // VEDRA SUGGESTIONS GRID (2 COLUMNS x 4 ROWS)
        item {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                // Row 1
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    SuggestionCardItem(
                        title = "Open WhatsApp",
                        subtitle = "Open directly",
                        icon = Icons.Default.Phone,
                        iconColor = Color(0xFF25D366),
                        arrowColor = Color(0xFF25D366),
                        lockedLabel = getLockedLabel("whatsapp"),
                        modifier = Modifier.weight(1f),
                        onLongClick = { handleBoxLongClick("whatsapp", "Open WhatsApp") },
                        onClick = { handleBoxClick("whatsapp") { onExecuteQuickAction("open whatsapp") } }
                    )
                    SuggestionCardItem(
                        title = "Study Planner",
                        subtitle = "Let's plan your study",
                        icon = Icons.Default.School,
                        iconColor = Color(0xFF60A5FA),
                        arrowColor = Color(0xFF60A5FA),
                        lockedLabel = getLockedLabel("study_planner"),
                        modifier = Modifier.weight(1f),
                        onLongClick = { handleBoxLongClick("study_planner", "Study Planner") },
                        onClick = { handleBoxClick("study_planner") { onNavigateTab(1) } }
                    )
                }

                // Row 2
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    SuggestionCardItem(
                        title = "Solve Question",
                        subtitle = "Get instant help",
                        icon = Icons.Default.Book,
                        iconColor = Color(0xFFA78BFA),
                        arrowColor = Color(0xFFA78BFA),
                        lockedLabel = getLockedLabel("solve_question"),
                        modifier = Modifier.weight(1f),
                        onLongClick = { handleBoxLongClick("solve_question", "Solve Question") },
                        onClick = { handleBoxClick("solve_question") { onNavigateTab(2) } }
                    )
                    SuggestionCardItem(
                        title = "Call Contact",
                        subtitle = "Quick call",
                        icon = Icons.Default.Phone,
                        iconColor = Color(0xFFF97316),
                        arrowColor = Color(0xFFF97316),
                        lockedLabel = getLockedLabel("call_contact"),
                        modifier = Modifier.weight(1f),
                        onLongClick = { handleBoxLongClick("call_contact", "Call Contact") },
                        onClick = { handleBoxClick("call_contact") { onExecuteQuickAction("call mom") } }
                    )
                }

                // Row 3
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    SuggestionCardItem(
                        title = "Create Reminder",
                        subtitle = "Set a reminder",
                        icon = Icons.Default.EditNote,
                        iconColor = Color(0xFFEC4899),
                        arrowColor = Color(0xFFEC4899),
                        lockedLabel = getLockedLabel("reminder"),
                        modifier = Modifier.weight(1f),
                        onLongClick = { handleBoxLongClick("reminder", "Create Reminder") },
                        onClick = { handleBoxClick("reminder") { onExecuteQuickAction("reminder") } }
                    )
                    SuggestionCardItem(
                        title = "Weather Update",
                        subtitle = "Check today's weather",
                        icon = Icons.Default.WbSunny,
                        iconColor = Color(0xFFFBBF24),
                        arrowColor = Color(0xFFFBBF24),
                        lockedLabel = getLockedLabel("weather"),
                        modifier = Modifier.weight(1f),
                        onLongClick = { handleBoxLongClick("weather", "Weather Update") },
                        onClick = { handleBoxClick("weather") { onExecuteQuickAction("weather") } }
                    )
                }

                // Row 4
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    SuggestionCardItem(
                        title = "Play Music",
                        subtitle = "Play your favorites",
                        icon = Icons.Default.MusicNote,
                        iconColor = Color(0xFF38BDF8),
                        arrowColor = Color(0xFF38BDF8),
                        lockedLabel = getLockedLabel("music"),
                        modifier = Modifier.weight(1f),
                        onLongClick = { handleBoxLongClick("music", "Play Music") },
                        onClick = { handleBoxClick("music") { onExecuteQuickAction("play music") } }
                    )
                    SuggestionCardItem(
                        title = "Open Calculator",
                        subtitle = "Perform calculations",
                        icon = Icons.Default.Calculate,
                        iconColor = Color(0xFF3B82F6),
                        arrowColor = Color(0xFF3B82F6),
                        lockedLabel = getLockedLabel("calculator"),
                        modifier = Modifier.weight(1f),
                        onLongClick = { handleBoxLongClick("calculator", "Open Calculator") },
                        onClick = { handleBoxClick("calculator") { onExecuteQuickAction("calculator") } }
                    )
                }
            }
        }

        // STUDY MODE BANNER CARD
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color(0xFF110F1C))
                    .border(1.dp, Color(0xFF261D3B), RoundedCornerShape(16.dp))
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
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(Color(0xFF25163A)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Psychology,
                                contentDescription = "Study Mode",
                                tint = Color(0xFFA78BFA),
                                modifier = Modifier.size(24.dp)
                            )
                        }

                        Spacer(modifier = Modifier.width(10.dp))

                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "Study Mode",
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(Color(0xFF2A1C40))
                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                ) {
                                    Text(
                                        text = "New",
                                        color = Color(0xFFC4B5FD),
                                        fontSize = 9.5.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "Your smart study companion for JEE preparation.",
                                color = Color(0xFF9CA3AF),
                                fontSize = 11.sp,
                                maxLines = 2
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(20.dp))
                            .background(Color(0xFF4C1D95))
                            .clickable { onNavigateTab(1) } // Study Hub
                            .padding(horizontal = 12.dp, vertical = 8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "Open Study Hub",
                                color = Color.White,
                                fontSize = 11.5.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.width(2.dp))
                            Icon(
                                imageVector = Icons.Default.ChevronRight,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(14.dp)
                            )
                        }
                    }
                }
            }
        }

        // QUICK STACK SECTION
        item {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Quick Stack",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp
                    )
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.clickable { /* Edit Quick Stack */ }
                    ) {
                        Text(
                            text = "Edit",
                            color = Color(0xFF6B7280),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium
                        )
                        Spacer(modifier = Modifier.width(2.dp))
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = "Edit",
                            tint = Color(0xFF6B7280),
                            modifier = Modifier.size(12.dp)
                        )
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    QuickStackItem("Apps", Icons.Default.GridView, Color(0xFF3B82F6)) {
                        onNavigateTab(2)
                    }
                    QuickStackItem("Flashlight", Icons.Default.FlashOn, Color(0xFFA78BFA)) {
                        onExecuteQuickAction("flashlight")
                    }
                    QuickStackItem("Camera", Icons.Default.PhotoCamera, Color(0xFFEC4899)) {
                        onExecuteQuickAction("camera")
                    }
                    QuickStackItem("Alarms", Icons.Default.Alarm, Color(0xFFF97316)) {
                        onExecuteQuickAction("alarm")
                    }
                    QuickStackItem("QR Scan", Icons.Default.QrCodeScanner, Color(0xFF10B981)) {
                        isQrScannerOpen = true
                    }
                    QuickStackItem("More", Icons.Default.MoreHoriz, Color(0xFF9CA3AF)) {
                        onNavigateTab(2)
                    }
                }
            }
        }

        // QUICK STATUS SECTION
        item {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = "Quick Status",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    QuickStatusCard(
                        title = "Battery",
                        value = "${battery.percentage}%",
                        status = battery.statusText,
                        icon = Icons.Default.BatteryFull,
                        iconColor = Color(0xFF10B981),
                        modifier = Modifier.weight(1f)
                    )
                    QuickStatusCard(
                        title = "Storage",
                        value = "64%",
                        status = "${storage.freeSpaceGB} GB free",
                        icon = Icons.Default.Storage,
                        iconColor = Color(0xFF3B82F6),
                        modifier = Modifier.weight(1f)
                    )
                    QuickStatusCard(
                        title = "Memory",
                        value = "3.2 GB",
                        status = "Free",
                        icon = Icons.Default.Memory,
                        iconColor = Color(0xFFA78BFA),
                        modifier = Modifier.weight(1f)
                    )
                    QuickStatusCard(
                        title = "Network",
                        value = "Airtel_5G",
                        status = "Connected",
                        icon = Icons.Default.Wifi,
                        iconColor = Color(0xFF10B981),
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }

    // App Picker & Lock Modal
    AppPickerAndLockModal(
        visible = isAppLockModalOpen,
        shortcutTitle = selectedCardForLock?.second ?: "Shortcut Box",
        onAppSelected = { app ->
            val cardKey = selectedCardForLock?.first
            if (cardKey != null) {
                dbService.setSetting("locked_app_$cardKey", app.packageName)
                dbService.setSetting("locked_app_label_$cardKey", app.label)
                Toast.makeText(context, "Locked '${app.label}' to this shortcut box! 🔒", Toast.LENGTH_SHORT).show()
            }
            isAppLockModalOpen = false
        },
        onDismissRequest = { isAppLockModalOpen = false }
    )

    // QR Scanner Modal Component
    CustomModal(
        visible = isQrScannerOpen,
        title = "QR & Barcode Scanner",
        onDismissRequest = { isQrScannerOpen = false }
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(180.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color.Black)
                    .border(2.dp, Color(0xFF10B981), RoundedCornerShape(16.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.QrCodeScanner,
                    contentDescription = "Scanner Frame",
                    tint = Color(0xFF10B981),
                    modifier = Modifier.size(56.dp)
                )
            }
            Text(
                text = "Point camera at QR code or Barcode",
                color = Color(0xFF9CA3AF),
                fontSize = 12.sp
            )
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun SuggestionCardItem(
    title: String,
    subtitle: String,
    icon: ImageVector,
    iconColor: Color,
    arrowColor: Color,
    modifier: Modifier = Modifier,
    lockedLabel: String? = null,
    onLongClick: (() -> Unit)? = null,
    onClick: () -> Unit
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(14.dp))
            .background(Color(0xFF110F1C))
            .border(1.dp, if (lockedLabel != null) Color(0xFF10B981) else Color(0xFF1E1A2E), RoundedCornerShape(14.dp))
            .combinedClickable(
                onClick = onClick,
                onLongClick = onLongClick
            )
            .padding(10.dp)
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
                        .size(28.dp)
                        .clip(CircleShape)
                        .background(iconColor.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = title,
                        tint = iconColor,
                        modifier = Modifier.size(16.dp)
                    )
                }

                Spacer(modifier = Modifier.width(8.dp))

                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = title,
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.5.sp,
                            maxLines = 1
                        )
                        if (lockedLabel != null) {
                            Spacer(modifier = Modifier.width(3.dp))
                            Text(text = "🔒", fontSize = 10.sp)
                        }
                    }
                    Spacer(modifier = Modifier.height(1.dp))
                    Text(
                        text = lockedLabel ?: subtitle,
                        color = if (lockedLabel != null) Color(0xFF10B981) else Color(0xFF6B7280),
                        fontSize = 9.5.sp,
                        maxLines = 1
                    )
                }
            }

            Spacer(modifier = Modifier.width(4.dp))

            Icon(
                imageVector = Icons.Default.ArrowForward,
                contentDescription = null,
                tint = arrowColor,
                modifier = Modifier.size(13.dp)
            )
        }
    }
}

@Composable
fun QuickStackItem(
    label: String,
    icon: ImageVector,
    iconColor: Color,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.clickable { onClick() }
    ) {
        Box(
            modifier = Modifier
                .size(44.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(Color(0xFF110F1C))
                .border(1.dp, Color(0xFF1E1A2E), RoundedCornerShape(12.dp)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = iconColor,
                modifier = Modifier.size(20.dp)
            )
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = label,
            color = Color(0xFF9CA3AF),
            fontSize = 10.5.sp,
            fontWeight = FontWeight.Normal
        )
    }
}

@Composable
fun QuickStatusCard(
    title: String,
    value: String,
    status: String,
    icon: ImageVector,
    iconColor: Color,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(Color(0xFF110F1C))
            .border(1.dp, Color(0xFF1E1A2E), RoundedCornerShape(12.dp))
            .padding(8.dp)
    ) {
        Column {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = icon,
                    contentDescription = title,
                    tint = iconColor,
                    modifier = Modifier.size(14.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = title,
                    color = Color(0xFF9CA3AF),
                    fontSize = 10.sp
                )
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = value,
                color = Color.White,
                fontWeight = FontWeight.Bold,
                fontSize = 13.sp,
                maxLines = 1
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = status,
                color = iconColor,
                fontSize = 9.5.sp,
                maxLines = 1
            )
        }
    }
}

