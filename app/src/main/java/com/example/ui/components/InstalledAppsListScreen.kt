package com.example.ui.components

import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.graphics.drawable.toBitmap
import com.example.data.room.CustomTextCommandEntity
import com.example.services.AppLauncher
import com.example.services.AppLauncher.AppInfoItem
import com.example.services.DatabaseService
import com.example.services.UtilityService
import kotlinx.coroutines.launch

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun InstalledAppsListScreen(
    dbService: DatabaseService,
    onExecuteVoiceCommand: ((String) -> Unit)? = null,
    onBack: (() -> Unit)? = null
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var searchQuery by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf("All") }
    var installedApps by remember { mutableStateOf<List<AppInfoItem>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var isGridView by remember { mutableStateOf(true) }

    val persistentShortcutsState = dbService.aiContextRepository.allCustomCommandsFlow?.collectAsState(initial = emptyList())
    val persistentShortcuts = persistentShortcutsState?.value ?: emptyList()

    val categories = listOf("All", "Productivity & Tools 🛠️", "Social & Communication 💬", "Media & Video 🎵", "System Apps ⚙️")

    LaunchedEffect(Unit) {
        isLoading = true
        installedApps = AppLauncher.getInstalledAppsOnDevice(context)
        isLoading = false
    }

    fun getCategory(app: AppInfoItem): String {
        val pkg = app.packageName.lowercase()
        val label = app.label.lowercase()
        return when {
            pkg.contains("whatsapp") || pkg.contains("telegram") || pkg.contains("gmail") ||
                    pkg.contains("mail") || pkg.contains("message") || pkg.contains("contact") ||
                    pkg.contains("facebook") || pkg.contains("instagram") || pkg.contains("twitter") -> "Social & Communication 💬"

            pkg.contains("chrome") || pkg.contains("browser") || pkg.contains("drive") ||
                    pkg.contains("office") || pkg.contains("doc") || pkg.contains("notes") ||
                    pkg.contains("calculator") || pkg.contains("clock") || pkg.contains("tool") -> "Productivity & Tools 🛠️"

            pkg.contains("youtube") || pkg.contains("spotify") || pkg.contains("gallery") ||
                    pkg.contains("photos") || pkg.contains("camera") || pkg.contains("video") ||
                    pkg.contains("music") || pkg.contains("player") -> "Media & Video 🎵"

            else -> "System Apps ⚙️"
        }
    }

    val filteredApps = remember(searchQuery, selectedCategory, installedApps) {
        installedApps.filter { app ->
            val matchesSearch = searchQuery.isBlank() ||
                    app.label.contains(searchQuery, ignoreCase = true) ||
                    app.packageName.contains(searchQuery, ignoreCase = true)
            val appCat = getCategory(app)
            val matchesCat = selectedCategory == "All" || appCat == selectedCategory
            matchesSearch && matchesCat
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(16.dp)
            .testTag("installed_apps_list_screen")
    ) {
        // Top Bar Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                if (onBack != null) {
                    IconButton(
                        onClick = onBack,
                        modifier = Modifier.testTag("installed_apps_back_button")
                    ) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = Color.White)
                    }
                    Spacer(modifier = Modifier.width(4.dp))
                }
                Column {
                    Text(
                        text = "📱 Installed Device Apps",
                        color = Color.White,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Long-press any app to create a persistent VEDRA shortcut 📌",
                        color = Color(0xFF9CA3AF),
                        fontSize = 11.sp
                    )
                }
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .clip(CircleShape)
                        .background(Color(0xFF8B5CF6).copy(alpha = 0.2f))
                        .padding(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = "${filteredApps.size} apps",
                        color = Color(0xFFC4B5FD),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(modifier = Modifier.width(6.dp))

                IconButton(
                    onClick = { isGridView = !isGridView },
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF1F2647))
                ) {
                    Icon(
                        imageVector = if (isGridView) Icons.Default.List else Icons.Default.GridView,
                        contentDescription = "Toggle Grid/List View",
                        tint = Color(0xFFC4B5FD),
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Persistent Quick Launch Shortcuts Bar (if any created)
        if (persistentShortcuts.isNotEmpty()) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 10.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(0xFF1B162E))
                    .border(1.dp, Color(0xFF8B5CF6), RoundedCornerShape(12.dp))
                    .padding(10.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "📌 Persistent Quick-Launch Shortcuts (${persistentShortcuts.size})",
                        color = Color(0xFFC4B5FD),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Tap to Launch • Long-press to Remove",
                        color = Color(0xFF9CA3AF),
                        fontSize = 10.sp
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(persistentShortcuts, key = { it.id }) { shortcut ->
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(20.dp))
                                .background(Color(0xFF2E1A47))
                                .border(1.dp, Color(0xFFA78BFA), RoundedCornerShape(20.dp))
                                .combinedClickable(
                                    onClick = {
                                        if (shortcut.actionType == "LAUNCH_APP") {
                                            AppLauncher.tryLaunchPackage(context, shortcut.targetPayload)
                                            Toast.makeText(context, "🚀 Launching ${shortcut.commandText}...", Toast.LENGTH_SHORT).show()
                                        } else {
                                            UtilityService.parseAndExecuteLocalCommand(context, dbService, shortcut.commandText)
                                        }
                                    },
                                    onLongClick = {
                                        scope.launch {
                                            dbService.aiContextRepository.deleteCustomCommand(shortcut.id)
                                            Toast.makeText(context, "🗑️ Removed shortcut '${shortcut.commandText}'", Toast.LENGTH_SHORT).show()
                                        }
                                    }
                                )
                                .padding(horizontal = 10.dp, vertical = 6.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.PushPin, contentDescription = null, tint = Color(0xFFF59E0B), modifier = Modifier.size(12.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = shortcut.commandText.replaceFirstChar { it.uppercase() },
                                    color = Color.White,
                                    fontSize = 11.5.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }
                    }
                }
            }
        }

        // Search Bar
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            modifier = Modifier
                .fillMaxWidth()
                .testTag("installed_apps_search_input"),
            placeholder = { Text("Search installed apps or packages...", color = Color.Gray, fontSize = 13.sp) },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = Color(0xFFA78BFA)) },
            trailingIcon = {
                if (searchQuery.isNotEmpty()) {
                    IconButton(onClick = { searchQuery = "" }) {
                        Icon(Icons.Default.Close, contentDescription = "Clear", tint = Color.Gray)
                    }
                }
            },
            singleLine = true,
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = Color(0xFF8B5CF6),
                unfocusedBorderColor = Color(0xFF2A2E47),
                focusedContainerColor = Color(0xFF121524),
                unfocusedContainerColor = Color(0xFF121524),
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.White
            ),
            shape = RoundedCornerShape(14.dp)
        )

        Spacer(modifier = Modifier.height(10.dp))

        // Filter Categories
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            items(categories) { cat ->
                val isSelected = selectedCategory == cat
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(20.dp))
                        .background(if (isSelected) Color(0xFF8B5CF6) else Color(0xFF1B1F33))
                        .clickable { selectedCategory = cat }
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = cat,
                        color = if (isSelected) Color.White else Color(0xFF9CA3AF),
                        fontSize = 11.5.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        if (isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    CircularProgressIndicator(color = Color(0xFF8B5CF6))
                    Spacer(modifier = Modifier.height(10.dp))
                    Text("Querying installed applications on device...", color = Color.Gray, fontSize = 12.sp)
                }
            }
        } else if (filteredApps.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "No installed applications matched '$searchQuery'",
                    color = Color.Gray,
                    fontSize = 13.sp
                )
            }
        } else if (isGridView) {
            LazyVerticalGrid(
                columns = GridCells.Adaptive(minSize = 92.dp),
                modifier = Modifier.weight(1f),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(filteredApps, key = { it.packageName }) { app ->
                    InstalledAppGridCardItem(
                        app = app,
                        dbService = dbService,
                        onLaunch = {
                            if (AppLauncher.tryLaunchPackage(context, app.packageName)) {
                                Toast.makeText(context, "🚀 Launching ${app.label}...", Toast.LENGTH_SHORT).show()
                            } else {
                                Toast.makeText(context, "Unable to launch ${app.label}", Toast.LENGTH_SHORT).show()
                            }
                        }
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(filteredApps, key = { it.packageName }) { app ->
                    InstalledAppCardItem(
                        app = app,
                        dbService = dbService,
                        onLaunch = {
                            if (AppLauncher.tryLaunchPackage(context, app.packageName)) {
                                Toast.makeText(context, "🚀 Launching ${app.label}...", Toast.LENGTH_SHORT).show()
                            } else {
                                Toast.makeText(context, "Unable to launch ${app.label}", Toast.LENGTH_SHORT).show()
                            }
                        },
                        onVoiceCommand = {
                            val cmd = "open ${app.label}"
                            if (onExecuteVoiceCommand != null) {
                                onExecuteVoiceCommand(cmd)
                            } else {
                                UtilityService.parseAndExecuteLocalCommand(context, dbService, cmd)
                                Toast.makeText(context, "🎙️ Executing VEDRA command: '$cmd'", Toast.LENGTH_SHORT).show()
                            }
                        }
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun InstalledAppCardItem(
    app: AppInfoItem,
    dbService: DatabaseService,
    onLaunch: () -> Unit,
    onVoiceCommand: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    fun createShortcut() {
        scope.launch {
            dbService.aiContextRepository.saveCustomCommand(
                commandText = app.label,
                actionType = "LAUNCH_APP",
                targetPayload = app.packageName,
                description = "Persistent quick-launch shortcut for ${app.label}"
            )
            Toast.makeText(context, "📌 Created persistent quick-launch shortcut for '${app.label}'!", Toast.LENGTH_SHORT).show()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(Color(0xFF121524))
            .border(1.dp, Color(0xFF1E2238), RoundedCornerShape(14.dp))
            .combinedClickable(
                onClick = onLaunch,
                onLongClick = { createShortcut() }
            )
            .padding(12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // App Icon
            val iconBitmap = remember(app.icon) {
                app.icon?.toBitmap(64, 64)?.asImageBitmap()
            }
            if (iconBitmap != null) {
                Image(
                    bitmap = iconBitmap,
                    contentDescription = app.label,
                    modifier = Modifier
                        .size(42.dp)
                        .clip(RoundedCornerShape(10.dp))
                )
            } else {
                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(Color(0xFF2E1A47)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.Android, contentDescription = null, tint = Color(0xFFA78BFA))
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            // Info Column
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = app.label,
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        maxLines = 1
                    )
                    if (app.isSystemApp) {
                        Spacer(modifier = Modifier.width(6.dp))
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .background(Color(0xFF374151))
                                .padding(horizontal = 4.dp, vertical = 1.dp)
                        ) {
                            Text("SYS", color = Color(0xFF9CA3AF), fontSize = 9.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "Long-press to pin shortcut • ${app.packageName}",
                    color = Color(0xFF9CA3AF),
                    fontSize = 10.5.sp,
                    maxLines = 1
                )
            }

            Spacer(modifier = Modifier.width(6.dp))

            // Action Buttons
            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                // Pin Shortcut Button
                IconButton(
                    onClick = { createShortcut() },
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(Color(0xFFF59E0B).copy(alpha = 0.2f))
                ) {
                    Icon(Icons.Default.PushPin, contentDescription = "Pin Quick Shortcut", tint = Color(0xFFFBBF24), modifier = Modifier.size(18.dp))
                }

                // Voice Command Button
                IconButton(
                    onClick = onVoiceCommand,
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF8B5CF6).copy(alpha = 0.2f))
                ) {
                    Icon(Icons.Default.Mic, contentDescription = "Voice Command", tint = Color(0xFFC4B5FD), modifier = Modifier.size(18.dp))
                }

                // Launch Button
                Button(
                    onClick = onLaunch,
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF8B5CF6)),
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.height(36.dp)
                ) {
                    Icon(Icons.Default.PlayArrow, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(2.dp))
                    Text("Open", fontSize = 11.5.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun InstalledAppGridCardItem(
    app: AppInfoItem,
    dbService: DatabaseService,
    onLaunch: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    val iconBitmap = remember(app.icon) {
        app.icon?.toBitmap(72, 72)?.asImageBitmap()
    }

    Box(
        modifier = Modifier
            .aspectRatio(1f)
            .clip(RoundedCornerShape(16.dp))
            .background(Color(0xFF13182E))
            .border(1.dp, Color(0xFF1F2647), RoundedCornerShape(16.dp))
            .combinedClickable(
                onClick = onLaunch,
                onLongClick = {
                    scope.launch {
                        dbService.aiContextRepository.saveCustomCommand(
                            commandText = app.label,
                            actionType = "LAUNCH_APP",
                            targetPayload = app.packageName,
                            description = "Shortcut for ${app.label}"
                        )
                        Toast.makeText(context, "📌 Shortcut pinned for '${app.label}'", Toast.LENGTH_SHORT).show()
                    }
                }
            )
            .padding(10.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            if (iconBitmap != null) {
                Image(
                    bitmap = iconBitmap,
                    contentDescription = app.label,
                    modifier = Modifier
                        .size(44.dp)
                        .clip(RoundedCornerShape(12.dp))
                )
            } else {
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(0xFF2E1A47)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Android,
                        contentDescription = null,
                        tint = Color(0xFFA78BFA),
                        modifier = Modifier.size(24.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = app.label,
                color = Color.White,
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium,
                maxLines = 1,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
        }
    }
}
