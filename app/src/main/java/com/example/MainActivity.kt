package com.example

import android.os.Bundle
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import androidx.compose.ui.unit.sp
import com.example.services.DatabaseService
import com.example.services.PermissionService
import com.example.services.UtilityService
import com.example.services.VoiceService
import com.example.ui.components.VedraDriveModal
import com.example.ui.screens.ActionsScreen
import com.example.ui.screens.DatabaseScreen
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.MemoryScreen
import com.example.ui.screens.PermissionOnboardingScreen
import com.example.ui.screens.SettingsScreen
import com.example.ui.screens.StudyHubScreen
import com.example.ui.screens.VedAssistantScreen
import com.example.ui.screens.VoiceModeOverlay
import com.example.ui.theme.VedraBackground
import com.example.ui.theme.VedraPurplePrimary
import com.example.ui.theme.VedraSurface
import com.example.ui.theme.VedraTextMuted
import com.example.ui.theme.VedraTheme
import kotlinx.coroutines.delay

import com.example.services.BackgroundService
import com.example.services.NotificationService

import androidx.compose.foundation.border
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.ui.input.pointer.pointerInput
import android.widget.Toast
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import com.example.ui.components.SideDrawer

class MainActivity : ComponentActivity() {

    private lateinit var dbService: DatabaseService
    private lateinit var voiceService: VoiceService
    private val voiceTriggerState = androidx.compose.runtime.mutableStateOf(false)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        dbService = DatabaseService(this)
        voiceService = VoiceService(this)

        NotificationService.createNotificationChannel(this)
        BackgroundService.startBackgroundTasks(this, dbService)

        val launchVoice = intent?.getBooleanExtra("OPEN_VOICE_MODE", false) ?: false
        if (launchVoice) {
            voiceTriggerState.value = true
        }
        val launchTab = intent?.getIntExtra("OPEN_TAB", -1) ?: -1

        setContent {
            VedraTheme(dbService = dbService) {
                MainAppLayout(
                    dbService = dbService,
                    voiceService = voiceService,
                    externalVoiceTrigger = voiceTriggerState,
                    initialTab = if (launchTab in 0..4) launchTab else 3
                )
            }
        }
    }

    override fun onNewIntent(intent: android.content.Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        val launchVoice = intent.getBooleanExtra("OPEN_VOICE_MODE", false)
        if (launchVoice) {
            voiceTriggerState.value = true
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        voiceService.shutdown()
    }
}

data class TabItem(
    val title: String,
    val icon: ImageVector,
    val isCenterPill: Boolean = false
)

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun MainAppLayout(
    dbService: DatabaseService,
    voiceService: VoiceService,
    externalVoiceTrigger: androidx.compose.runtime.MutableState<Boolean> = remember { mutableStateOf(false) },
    initialTab: Int = 3
) {
    val context = LocalContext.current
    var showPermissionOnboarding by remember {
        mutableStateOf(!PermissionService.isPermissionsOnboarded(context))
    }
    var isVoiceModeActive by remember { mutableStateOf(externalVoiceTrigger.value) }

    LaunchedEffect(externalVoiceTrigger.value) {
        if (externalVoiceTrigger.value) {
            isVoiceModeActive = true
            externalVoiceTrigger.value = false
        }
    }

    var activeTab by remember { mutableIntStateOf(initialTab) }
    var hasUserInteracted by remember { mutableStateOf(false) }
    var isDrawerOpen by remember { mutableStateOf(false) }
    var isDriveModalOpen by remember { mutableStateOf(false) }
    var isAppLauncherModalOpen by remember { mutableStateOf(false) }
    var isInstalledAppsScreenOpen by remember { mutableStateOf(false) }
    var selectedChatHistoryItem by remember { mutableStateOf<com.example.services.ChatHistoryItem?>(null) }

    if (showPermissionOnboarding) {
        PermissionOnboardingScreen(
            onComplete = {
                showPermissionOnboarding = false
            }
        )
        return
    }

    // Initial launch setup
    LaunchedEffect(initialTab) {
        if (initialTab in 0..5) {
            activeTab = initialTab
        }
    }

    // 6 Main Tabs: Home, VEHub, Ved (Center Pill), VEDrive, VETools, Settings
    val tabs = listOf(
        TabItem("Home", Icons.Default.Home),
        TabItem("VEHub", Icons.Default.School),
        TabItem("Ved", Icons.Default.AutoAwesome, isCenterPill = true),
        TabItem("VEDrive", Icons.Default.Folder),
        TabItem("VETools", Icons.Default.Build),
        TabItem("Settings", Icons.Default.Settings)
    )

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        bottomBar = {
            Row(
                modifier = Modifier
                    .testTag("bottom_navigation_bar")
                    .fillMaxWidth()
                    .background(Color(0xFF090810))
                    .navigationBarsPadding()
                    .padding(vertical = 6.dp, horizontal = 2.dp),
                horizontalArrangement = Arrangement.SpaceAround,
                verticalAlignment = Alignment.CenterVertically
            ) {
                tabs.forEachIndexed { index, tab ->
                    val isSelected = activeTab == index
                    val tint = if (isSelected) Color(0xFFC4B5FD) else Color(0xFF6B7280)

                    if (tab.isCenterPill) {
                        // Center VED Capsule/Pill Button with special focus & glowing design
                        Box(
                            modifier = Modifier
                                .testTag("tab_${tab.title.lowercase()}")
                                .weight(1.1f)
                                .combinedClickable(
                                    onClick = {
                                        hasUserInteracted = true
                                        activeTab = index
                                    },
                                    onLongClick = {
                                        hasUserInteracted = true
                                        isVoiceModeActive = true
                                    }
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(20.dp))
                                    .background(
                                        if (isSelected) androidx.compose.ui.graphics.Brush.horizontalGradient(
                                            listOf(Color(0xFF7C3AED), Color(0xFF06B6D4))
                                        ) else androidx.compose.ui.graphics.Brush.horizontalGradient(
                                            listOf(Color(0xFF2E1A47), Color(0xFF1E1035))
                                        )
                                    )
                                    .border(1.5.dp, if (isSelected) Color(0xFFA78BFA) else Color(0xFF06B6D4), RoundedCornerShape(20.dp))
                                    .padding(horizontal = 10.dp, vertical = 6.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.AutoAwesome,
                                        contentDescription = "Ved AI",
                                        tint = if (isSelected) Color.White else Color(0xFF06B6D4),
                                        modifier = Modifier.size(14.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = "Ved",
                                        color = Color.White,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        maxLines = 1,
                                        softWrap = false
                                    )
                                }
                            }
                        }
                    } else {
                        Box(
                            modifier = Modifier
                                .testTag("tab_${tab.title.lowercase()}")
                                .weight(1f)
                                .combinedClickable(
                                    onClick = {
                                        hasUserInteracted = true
                                        activeTab = index
                                    },
                                    onLongClick = {
                                        hasUserInteracted = true
                                        if (tab.title == "VEDrive") {
                                            isDriveModalOpen = true
                                        }
                                    }
                                )
                                .padding(vertical = 4.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(
                                    imageVector = tab.icon,
                                    contentDescription = tab.title,
                                    tint = tint,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.height(3.dp))
                                Text(
                                    text = tab.title,
                                    color = tint,
                                    fontSize = 10.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                    maxLines = 1,
                                    softWrap = false,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }
                    }
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(Color(0xFF090810))
                .pointerInput(Unit) {
                    detectHorizontalDragGestures { change, dragAmount ->
                        if (change.position.x < 140.dp.toPx() && dragAmount > 15f) {
                            isDrawerOpen = true
                        }
                    }
                }
        ) {
            androidx.compose.animation.AnimatedContent(
                targetState = activeTab,
                transitionSpec = {
                    if (targetState > initialState) {
                        (slideInHorizontally { width -> width } + fadeIn(animationSpec = tween(280)))
                            .togetherWith(slideOutHorizontally { width -> -width } + fadeOut(animationSpec = tween(280)))
                    } else {
                        (slideInHorizontally { width -> -width } + fadeIn(animationSpec = tween(280)))
                            .togetherWith(slideOutHorizontally { width -> width } + fadeOut(animationSpec = tween(280)))
                    }
                },
                label = "SmoothTabTransition"
            ) { targetTab ->
                when (targetTab) {
                    0 -> SafeTabBoundary("Home") {
                        HomeScreen(
                            dbService = dbService,
                            voiceService = voiceService,
                            onActivateVoice = {
                                hasUserInteracted = true
                                isVoiceModeActive = true
                            },
                            onNavigateTab = { tab ->
                                hasUserInteracted = true
                                activeTab = tab
                            },
                            onExecuteQuickAction = { actionText ->
                                hasUserInteracted = true
                                UtilityService.parseAndExecuteLocalCommand(context, dbService, actionText)
                            },
                            onOpenDrawer = {
                                isDrawerOpen = true
                            }
                        )
                    }
                    1 -> SafeTabBoundary("VEHub") {
                        StudyHubScreen(
                            dbService = dbService
                        )
                    }
                    2 -> SafeTabBoundary("Ved") {
                        VedAssistantScreen(
                            dbService = dbService,
                            voiceService = voiceService,
                            onActivateVoice = {
                                hasUserInteracted = true
                                isVoiceModeActive = true
                            },
                            onOpenDrawer = {
                                isDrawerOpen = true
                            }
                        )
                    }
                    3 -> SafeTabBoundary("VEDrive") {
                        DatabaseScreen(
                            dbService = dbService,
                            onOpenDrawer = {
                                isDrawerOpen = true
                            }
                        )
                    }
                    4 -> SafeTabBoundary("VETools") {
                        ActionsScreen(
                            onExecuteAction = { command ->
                                hasUserInteracted = true
                                UtilityService.parseAndExecuteLocalCommand(context, dbService, command)
                            }
                        )
                    }
                    5 -> SafeTabBoundary("Settings") {
                        SettingsScreen(
                            dbService = dbService,
                            voiceService = voiceService
                        )
                    }
                }
            }

            // Global VoiceMode Overlay
            if (isVoiceModeActive) {
                VoiceModeOverlay(
                    voiceService = voiceService,
                    onClose = {
                        hasUserInteracted = true
                        isVoiceModeActive = false
                    }
                )
            } else {
                com.example.ui.components.FloatingAssistantWidget(
                    voiceService = voiceService,
                    dbService = dbService,
                    onActivateVoiceMode = {
                        hasUserInteracted = true
                        isVoiceModeActive = true
                    },
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(bottom = 24.dp, end = 16.dp)
                )
            }

            // Side Drawer (Top-Left 3 horizontal lines menu & swipe gesture)
            SideDrawer(
                isOpen = isDrawerOpen,
                dbService = dbService,
                onClose = { isDrawerOpen = false },
                onSelectMenuItem = { actionKey ->
                    hasUserInteracted = true
                    when (actionKey) {
                        "ved" -> activeTab = 2 // Navigate to Ved tab
                        "app_launcher" -> isInstalledAppsScreenOpen = true // Open Installed Apps List Screen
                        "database", "drive" -> activeTab = 3 // Navigate to VEDrive tab
                        "workspace", "vehub" -> activeTab = 1 // Navigate to VEHub tab
                        "automation", "vetools" -> activeTab = 4 // Navigate to VETools tab
                        "search" -> activeTab = 3 // Navigate to Search in VEDrive
                        "settings" -> activeTab = 5 // Navigate to Settings tab
                        "pro_upgrade" -> Toast.makeText(context, "⚡ Upgraded to VEDRA PRO!", Toast.LENGTH_SHORT).show()
                    }
                },
                onSelectChatHistoryItem = { chat ->
                    selectedChatHistoryItem = chat
                    activeTab = 2
                }
            )

            // VEDRA AI Knowledge Base / Drive Modal (triggered via Memory long-press)
            VedraDriveModal(
                visible = isDriveModalOpen,
                dbService = dbService,
                onDismissRequest = { isDriveModalOpen = false }
            )

            // Installed Applications Launcher Modal
            com.example.ui.components.AppPickerAndLockModal(
                visible = isAppLauncherModalOpen,
                shortcutTitle = "App Launcher",
                onDismissRequest = { isAppLauncherModalOpen = false },
                onAppSelected = { app ->
                    if (com.example.services.AppLauncher.tryLaunchPackage(context, app.packageName)) {
                        Toast.makeText(context, "Opening ${app.label}...", Toast.LENGTH_SHORT).show()
                    } else {
                        Toast.makeText(context, "Unable to open ${app.label}", Toast.LENGTH_SHORT).show()
                    }
                    isAppLauncherModalOpen = false
                }
            )

            // Installed Apps Query List Screen Overlay
            if (isInstalledAppsScreenOpen) {
                androidx.compose.foundation.layout.Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(androidx.compose.material3.MaterialTheme.colorScheme.background)
                        .zIndex(100f)
                ) {
                    com.example.ui.components.InstalledAppsListScreen(
                        dbService = dbService,
                        onExecuteVoiceCommand = { cmd ->
                            isInstalledAppsScreenOpen = false
                            com.example.services.UtilityService.parseAndExecuteLocalCommand(context, dbService, cmd)
                        },
                        onBack = { isInstalledAppsScreenOpen = false }
                    )
                }
            }
        }
    }
}

@Composable
fun SafeTabBoundary(
    tabName: String,
    content: @Composable () -> Unit
) {
    var hasError by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf("") }

    if (hasError) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            contentAlignment = Alignment.Center
        ) {
            com.example.ui.components.CustomCard(borderColor = com.example.ui.theme.VedraPinkAccent) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = "⚡ $tabName Screen Error",
                        color = com.example.ui.theme.VedraPinkAccent,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = if (errorMessage.isNotBlank()) errorMessage else "An unexpected error occurred in $tabName.",
                        color = com.example.ui.theme.VedraTextSecondary,
                        fontSize = 12.sp
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    com.example.ui.components.CustomButton(
                        text = "Reload Screen",
                        onClick = {
                            hasError = false
                            errorMessage = ""
                        }
                    )
                }
            }
        }
    } else {
        content()
    }
}
