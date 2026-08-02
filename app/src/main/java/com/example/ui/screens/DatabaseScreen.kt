package com.example.ui.screens

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Book
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.CloudUpload
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.CreateNewFolder
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.DriveFileMove
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.FolderOpen
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.InsertDriveFile
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.OpenInNew
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.UploadFile
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.services.DatabaseService
import com.example.services.DriveDocument
import com.example.services.DriveFolder
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

// Navigation view modes inside VEDrive
enum class VedriveViewMode {
    ROOT_DRIVE,
    FOLDER_CONTENTS,
    FILE_DETAILS,
    FILE_VIEWER
}

// VEDrive Primary Tabs
enum class VedriveTab {
    FOLDERS,
    RECENT,
    VEDMT
}

// Recent file display item model
data class RecentFileItem(
    val title: String,
    val size: String,
    val dateOrTime: String,
    val fileType: String,
    val category: String, // Today, Yesterday, This Week
    val path: String = "VEDrive / Study Material"
)

// Main VEDrive Screen Entry Point
@Composable
fun DatabaseScreen(
    dbService: DatabaseService,
    onOpenDrawer: () -> Unit = {}
) {
    val context = LocalContext.current

    // Active View Mode (ROOT_DRIVE, FOLDER_CONTENTS, FILE_DETAILS, FILE_VIEWER)
    var currentViewMode by remember { mutableStateOf(VedriveViewMode.ROOT_DRIVE) }

    // Active Primary Tab (FOLDERS, RECENT, VEDMT)
    var selectedTab by remember { mutableStateOf(VedriveTab.FOLDERS) }

    // Grid vs List view toggle state
    var isGridView by remember { mutableStateOf(dbService.getSetting("drive_file_view", "List View") == "Grid View") }

    // Navigation Stack for Folders
    var activeFolderId by remember { mutableLongStateOf(0L) }
    var activeFolderName by remember { mutableStateOf("Study Material") }

    // Active file selected for Details or Viewing
    var activeDocument by remember { mutableStateOf<DriveDocument?>(null) }
    var activeRecentFile by remember { mutableStateOf<RecentFileItem?>(null) }

    // Search query state
    var searchQuery by remember { mutableStateOf("") }
    var isSearchActive by remember { mutableStateOf(false) }

    // Modal and Dialog States
    var showCreateFolderDialog by remember { mutableStateOf(false) }
    var showCreateFileDialog by remember { mutableStateOf(false) }
    var showItemActionMenu by remember { mutableStateOf<Any?>(null) }
    var itemToRename by remember { mutableStateOf<Any?>(null) }
    var itemToMove by remember { mutableStateOf<Any?>(null) }

    // System File Picker Launcher
    val filePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            val fileName = it.lastPathSegment?.substringAfterLast('/') ?: "Uploaded_Document.pdf"
            dbService.createDriveDocument(
                folderId = if (activeFolderId == 0L) 1L else activeFolderId,
                title = fileName,
                content = "Imported from local storage: $uri",
                fileType = when {
                    fileName.endsWith(".pdf", ignoreCase = true) -> "PDF"
                    fileName.endsWith(".png", ignoreCase = true) || fileName.endsWith(".jpg", ignoreCase = true) -> "IMG"
                    else -> "TXT"
                }
            )
            Toast.makeText(context, "Added $fileName to VEDrive!", Toast.LENGTH_SHORT).show()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0B0A1A)) // Dark violet space background
    ) {
        when (currentViewMode) {
            VedriveViewMode.ROOT_DRIVE -> {
                VedriveRootView(
                    dbService = dbService,
                    selectedTab = selectedTab,
                    onSelectTab = { selectedTab = it },
                    isGridView = isGridView,
                    onToggleGridView = { isGridView = !isGridView },
                    searchQuery = searchQuery,
                    onSearchChange = { searchQuery = it },
                    isSearchActive = isSearchActive,
                    onToggleSearch = { isSearchActive = !isSearchActive },
                    onOpenDrawer = onOpenDrawer,
                    onOpenFolder = { folderId, folderName ->
                        activeFolderId = folderId
                        activeFolderName = folderName
                        currentViewMode = VedriveViewMode.FOLDER_CONTENTS
                    },
                    onSelectFile = { doc ->
                        activeDocument = doc
                        activeRecentFile = null
                        currentViewMode = VedriveViewMode.FILE_DETAILS
                    },
                    onSelectRecentFile = { recent ->
                        activeRecentFile = recent
                        activeDocument = null
                        currentViewMode = VedriveViewMode.FILE_DETAILS
                    },
                    onOpenItemMenu = { item -> showItemActionMenu = item },
                    onUploadClick = { filePickerLauncher.launch("*/*") },
                    onCreateFolderClick = { showCreateFolderDialog = true }
                )
            }

            VedriveViewMode.FOLDER_CONTENTS -> {
                FolderContentsView(
                    dbService = dbService,
                    folderId = activeFolderId,
                    folderName = activeFolderName,
                    isGridView = isGridView,
                    onToggleGridView = { isGridView = !isGridView },
                    searchQuery = searchQuery,
                    onSearchChange = { searchQuery = it },
                    isSearchActive = isSearchActive,
                    onToggleSearch = { isSearchActive = !isSearchActive },
                    onBack = { currentViewMode = VedriveViewMode.ROOT_DRIVE },
                    onOpenSubFolder = { subId, subName ->
                        activeFolderId = subId
                        activeFolderName = subName
                    },
                    onSelectFile = { doc ->
                        activeDocument = doc
                        activeRecentFile = null
                        currentViewMode = VedriveViewMode.FILE_DETAILS
                    },
                    onOpenItemMenu = { item -> showItemActionMenu = item },
                    onUploadClick = { filePickerLauncher.launch("*/*") },
                    onCreateFolderClick = { showCreateFolderDialog = true }
                )
            }

            VedriveViewMode.FILE_DETAILS -> {
                FileDetailsScreen(
                    document = activeDocument,
                    recentFile = activeRecentFile,
                    onBack = {
                        currentViewMode = if (activeFolderId != 0L) VedriveViewMode.FOLDER_CONTENTS else VedriveViewMode.ROOT_DRIVE
                    },
                    onOpen = {
                        if (activeDocument != null) {
                            currentViewMode = VedriveViewMode.FILE_VIEWER
                        } else {
                            Toast.makeText(context, "Opening file in viewer...", Toast.LENGTH_SHORT).show()
                        }
                    },
                    onShare = {
                        val title = activeDocument?.title ?: activeRecentFile?.title ?: "Document"
                        val intent = Intent(Intent.ACTION_SEND).apply {
                            type = "text/plain"
                            putExtra(Intent.EXTRA_SUBJECT, title)
                            putExtra(Intent.EXTRA_TEXT, "📄 Shared via VEDrive AI Data Hub: $title")
                        }
                        context.startActivity(Intent.createChooser(intent, "Share File"))
                    },
                    onRename = { itemToRename = activeDocument ?: activeRecentFile },
                    onMove = { itemToMove = activeDocument ?: activeRecentFile },
                    onCopy = {
                        Toast.makeText(context, "Copied file to clipboard!", Toast.LENGTH_SHORT).show()
                    },
                    onAddToVedmT = {
                        val title = activeDocument?.title ?: activeRecentFile?.title ?: "Document.txt"
                        val vedmTFolderId = dbService.getOrCreateVedmTFolderId()
                        dbService.createDriveDocument(
                            folderId = vedmTFolderId,
                            title = title,
                            content = activeDocument?.content ?: "VEDM-T Indexed text content for $title",
                            fileType = "TXT"
                        )
                        Toast.makeText(context, "Added '$title' to VEDM-T Knowledge Base!", Toast.LENGTH_SHORT).show()
                    },
                    onDelete = {
                        if (activeDocument != null) {
                            dbService.deleteDriveDocument(activeDocument!!.id)
                        }
                        Toast.makeText(context, "File deleted from VEDrive", Toast.LENGTH_SHORT).show()
                        currentViewMode = VedriveViewMode.ROOT_DRIVE
                    }
                )
            }

            VedriveViewMode.FILE_VIEWER -> {
                FileContentViewerScreen(
                    document = activeDocument,
                    onBack = { currentViewMode = VedriveViewMode.FILE_DETAILS }
                )
            }
        }
    }

    // Modal Dialogs
    if (showCreateFolderDialog) {
        CreateFolderModalDialog(
            onDismiss = { showCreateFolderDialog = false },
            onCreate = { name ->
                dbService.createDriveFolder(name = name, parentId = activeFolderId)
                Toast.makeText(context, "Created folder '$name'", Toast.LENGTH_SHORT).show()
                showCreateFolderDialog = false
            }
        )
    }

    if (showCreateFileDialog) {
        CreateFileModalDialog(
            onDismiss = { showCreateFileDialog = false },
            onCreate = { title, content, type ->
                dbService.createDriveDocument(
                    folderId = if (activeFolderId == 0L) 1L else activeFolderId,
                    title = title,
                    content = content,
                    fileType = type
                )
                Toast.makeText(context, "Created file '$title'", Toast.LENGTH_SHORT).show()
                showCreateFileDialog = false
            }
        )
    }

    if (showItemActionMenu != null) {
        ItemActionBottomSheet(
            item = showItemActionMenu!!,
            onDismiss = { showItemActionMenu = null },
            onSelectDetails = {
                if (showItemActionMenu is DriveDocument) {
                    activeDocument = showItemActionMenu as DriveDocument
                    activeRecentFile = null
                    currentViewMode = VedriveViewMode.FILE_DETAILS
                } else if (showItemActionMenu is RecentFileItem) {
                    activeRecentFile = showItemActionMenu as RecentFileItem
                    activeDocument = null
                    currentViewMode = VedriveViewMode.FILE_DETAILS
                } else {
                    Toast.makeText(context, "Folder Details", Toast.LENGTH_SHORT).show()
                }
                showItemActionMenu = null
            },
            onRename = {
                itemToRename = showItemActionMenu
                showItemActionMenu = null
            },
            onDelete = {
                val item = showItemActionMenu
                if (item is DriveFolder) {
                    dbService.deleteDriveFolder(item.id)
                } else if (item is DriveDocument) {
                    dbService.deleteDriveDocument(item.id)
                }
                Toast.makeText(context, "Item deleted", Toast.LENGTH_SHORT).show()
                showItemActionMenu = null
            },
            onShare = {
                Toast.makeText(context, "Sharing item...", Toast.LENGTH_SHORT).show()
                showItemActionMenu = null
            }
        )
    }

    if (itemToRename != null) {
        RenameModalDialog(
            item = itemToRename!!,
            onDismiss = { itemToRename = null },
            onRename = { newName ->
                when (val item = itemToRename) {
                    is DriveFolder -> dbService.updateDriveFolder(item.id, newName)
                    is DriveDocument -> dbService.updateDriveDocument(item.id, newName, item.content)
                }
                Toast.makeText(context, "Renamed to '$newName'", Toast.LENGTH_SHORT).show()
                itemToRename = null
            }
        )
    }
}

// ================= 1. ROOT VEDRIVE VIEW =================
@Composable
private fun VedriveRootView(
    dbService: DatabaseService,
    selectedTab: VedriveTab,
    onSelectTab: (VedriveTab) -> Unit,
    isGridView: Boolean,
    onToggleGridView: () -> Unit,
    searchQuery: String,
    onSearchChange: (String) -> Unit,
    isSearchActive: Boolean,
    onToggleSearch: () -> Unit,
    onOpenDrawer: () -> Unit,
    onOpenFolder: (Long, String) -> Unit,
    onSelectFile: (DriveDocument) -> Unit,
    onSelectRecentFile: (RecentFileItem) -> Unit,
    onOpenItemMenu: (Any) -> Unit,
    onUploadClick: () -> Unit,
    onCreateFolderClick: () -> Unit
) {
    val context = LocalContext.current

    Column(modifier = Modifier.fillMaxSize()) {
        // Top Bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(
                    onClick = onOpenDrawer,
                    modifier = Modifier
                        .size(38.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF1B1A38))
                ) {
                    Icon(Icons.Default.Menu, contentDescription = "Menu", tint = Color.White)
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text("VEDrive", color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                    Text("Your Personal Data Hub", color = Color(0xFFA09EC0), fontSize = 11.5.sp)
                }
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onToggleSearch) {
                    Icon(
                        if (isSearchActive) Icons.Default.Close else Icons.Default.Search,
                        contentDescription = "Search",
                        tint = Color.White
                    )
                }
                IconButton(onClick = onUploadClick) {
                    Icon(Icons.Default.CloudUpload, contentDescription = "Upload", tint = Color.White)
                }
                IconButton(onClick = { Toast.makeText(context, "VEDrive Options", Toast.LENGTH_SHORT).show() }) {
                    Icon(Icons.Default.MoreVert, contentDescription = "More Options", tint = Color.White)
                }
            }
        }

        // Search Input Field
        AnimatedVisibility(visible = isSearchActive) {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = onSearchChange,
                placeholder = { Text("Search files & folders in VEDrive...", color = Color(0xFF6B6893), fontSize = 12.5.sp) },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 4.dp),
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color(0xFF8B5CF6),
                    unfocusedBorderColor = Color(0xFF28264A),
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White
                )
            )
        }

        // Main Content Area with FAB
        Box(modifier = Modifier.weight(1f)) {
            Column(modifier = Modifier.fillMaxSize()) {
                // Top Storage Donut Overview Card (Only shown in ROOT view)
                StorageOverviewCard(dbService = dbService)

                Spacer(modifier = Modifier.height(12.dp))

                // Primary Tabs Row (Folders, Recent, VEDM-T)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(20.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        TabHeaderItem("Folders", selectedTab == VedriveTab.FOLDERS) {
                            onSelectTab(VedriveTab.FOLDERS)
                        }
                        TabHeaderItem("Recent", selectedTab == VedriveTab.RECENT) {
                            onSelectTab(VedriveTab.RECENT)
                        }
                        TabHeaderItem("VEDM-T", selectedTab == VedriveTab.VEDMT) {
                            onSelectTab(VedriveTab.VEDMT)
                        }
                    }

                    // Grid / List View Toggle Button
                    IconButton(
                        onClick = onToggleGridView,
                        modifier = Modifier
                            .size(34.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color(0xFF1B1A38))
                    ) {
                        Icon(
                            if (isGridView) Icons.AutoMirrored.Filled.List else Icons.Default.GridView,
                            contentDescription = "Toggle View",
                            tint = Color(0xFFA855F7),
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Tab Content Switcher
                Box(modifier = Modifier.weight(1f)) {
                    when (selectedTab) {
                        VedriveTab.FOLDERS -> {
                            FoldersTabContent(
                                dbService = dbService,
                                isGridView = isGridView,
                                searchQuery = searchQuery,
                                onOpenFolder = onOpenFolder,
                                onOpenItemMenu = onOpenItemMenu
                            )
                        }

                        VedriveTab.RECENT -> {
                            RecentTabContent(
                                dbService = dbService,
                                searchQuery = searchQuery,
                                onSelectRecentFile = onSelectRecentFile,
                                onOpenItemMenu = onOpenItemMenu
                            )
                        }

                        VedriveTab.VEDMT -> {
                            VedmtTabContent(
                                dbService = dbService,
                                searchQuery = searchQuery,
                                onSelectDocument = onSelectFile,
                                onOpenItemMenu = onOpenItemMenu
                            )
                        }
                    }
                }
            }

            // Floating Action Button (+)
            Box(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(20.dp)
                    .size(54.dp)
                    .clip(CircleShape)
                    .background(
                        Brush.linearGradient(
                            colors = listOf(Color(0xFF8B5CF6), Color(0xFFA855F7))
                        )
                    )
                    .clickable { onUploadClick() },
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add", tint = Color.White, modifier = Modifier.size(28.dp))
            }
        }
    }
}

// Storage Donut Overview Card (Exact match to Top Left screenshot with Google Drive Sync)
@Composable
private fun StorageOverviewCard(dbService: DatabaseService) {
    val stats = remember(dbService.getAllDriveDocuments().size, dbService.getAllDriveFolders().size) {
        com.example.services.GoogleDriveService.getStorageStats(dbService)
    }
    val totalFilesCount = remember(dbService.getAllDriveDocuments().size) { dbService.getAllDriveDocuments().size }
    val totalFoldersCount = remember(dbService.getAllDriveFolders().size) { dbService.getAllDriveFolders().size }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .clip(RoundedCornerShape(20.dp))
            .background(
                Brush.linearGradient(
                    colors = listOf(Color(0xFF18153B), Color(0xFF14102E))
                )
            )
            .border(1.dp, Color(0xFF3B2E6E), RoundedCornerShape(20.dp))
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // Left Donut Ring Chart
            Row(verticalAlignment = Alignment.CenterVertically) {
                StorageDonutChart(percentage = stats.percentageUsed)

                Spacer(modifier = Modifier.width(16.dp))

                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("Google Drive Storage", color = Color(0xFFA09EC0), fontSize = 11.sp)
                        Spacer(modifier = Modifier.width(4.dp))
                        Box(
                            modifier = Modifier
                                .size(6.dp)
                                .clip(CircleShape)
                                .background(Color(0xFF10B981))
                        )
                    }
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("${stats.leftGigaBytes} GB", color = Color.White, fontSize = 17.sp, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Left", color = Color(0xFF10B981), fontSize = 12.5.sp, fontWeight = FontWeight.Bold)
                    }
                    Spacer(modifier = Modifier.height(2.dp))
                    Text("${stats.usedGigaBytes} GB Used / ${stats.totalGigaBytes.toInt()} GB Total", color = Color(0xFF6B6893), fontSize = 10.5.sp)
                    Text("Linked: ${stats.connectedEmail}", color = Color(0xFF34D399), fontSize = 9.5.sp, fontWeight = FontWeight.Medium)
                }
            }

            // Right Stats (Total Files, Total Folders)
            Row(horizontalArrangement = Arrangement.spacedBy(14.dp)) {
                Column {
                    Text("Total Files", color = Color(0xFF6B6893), fontSize = 10.5.sp)
                    Text("$totalFilesCount", color = Color.White, fontSize = 15.sp, fontWeight = FontWeight.Bold)
                    Text("items", color = Color(0xFF6B6893), fontSize = 10.sp)
                }

                Column {
                    Text("Total Folders", color = Color(0xFF6B6893), fontSize = 10.5.sp)
                    Text("$totalFoldersCount", color = Color.White, fontSize = 15.sp, fontWeight = FontWeight.Bold)
                    Text("folders", color = Color(0xFF6B6893), fontSize = 10.sp)
                }
            }
        }
    }
}

// Donut Chart Composable
@Composable
private fun StorageDonutChart(percentage: Float = 0.68f) {
    Box(
        modifier = Modifier.size(62.dp),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val strokeWidth = 7.dp.toPx()

            // Background Circle Track
            drawArc(
                color = Color(0xFF25224A),
                startAngle = -90f,
                sweepAngle = 360f,
                useCenter = false,
                style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
            )

            // Active Progress Arc
            drawArc(
                color = Color(0xFF8B5CF6),
                startAngle = -90f,
                sweepAngle = 360f * percentage,
                useCenter = false,
                style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
            )
        }

        Text(
            text = "${(percentage * 100).toInt()}%",
            color = Color.White,
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold
        )
    }
}

// Tab Header Item with Active Indicator
@Composable
private fun TabHeaderItem(
    title: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.clickable { onClick() }
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            if (title == "Folders") Icon(Icons.Default.Folder, contentDescription = null, tint = if (isSelected) Color(0xFFA855F7) else Color(0xFF6B6893), modifier = Modifier.size(15.dp))
            if (title == "Recent") Icon(Icons.Default.Description, contentDescription = null, tint = if (isSelected) Color(0xFFA855F7) else Color(0xFF6B6893), modifier = Modifier.size(15.dp))
            if (title == "VEDM-T") Icon(Icons.Default.Psychology, contentDescription = null, tint = if (isSelected) Color(0xFFA855F7) else Color(0xFF6B6893), modifier = Modifier.size(15.dp))

            Spacer(modifier = Modifier.width(6.dp))

            Text(
                text = title,
                color = if (isSelected) Color.White else Color(0xFF6B6893),
                fontSize = 14.5.sp,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
            )
        }

        Spacer(modifier = Modifier.height(4.dp))

        if (isSelected) {
            Box(
                modifier = Modifier
                    .width(28.dp)
                    .height(2.5.dp)
                    .clip(CircleShape)
                    .background(Color(0xFF8B5CF6))
            )
        } else {
            Spacer(modifier = Modifier.height(2.5.dp))
        }
    }
}

// ================= 2. FOLDERS TAB CONTENT =================
@Composable
private fun FoldersTabContent(
    dbService: DatabaseService,
    isGridView: Boolean,
    searchQuery: String,
    onOpenFolder: (Long, String) -> Unit,
    onOpenItemMenu: (Any) -> Unit
) {
    val chatsCount = remember(dbService.getAllChatHistory().size) { dbService.getAllChatHistory().size }
    val customDbFolders = remember(dbService.getAllDriveFolders().size) { dbService.getAllDriveFolders() }

    // Root Folders including VEChat History & Custom User Folders
    val rootFolders = remember(chatsCount, customDbFolders) {
        val list = mutableListOf(
            Triple("VEChat History", "$chatsCount sessions (Synced to Drive)", "Auto Backup"),
            Triple("Study Material", "842 items", "2 May 2025"),
            Triple("Books", "156 items", "1 May 2025"),
            Triple("Notes", "532 items", "30 Apr 2025"),
            Triple("PYQs", "432 items", "29 Apr 2025"),
            Triple("Mock Tests", "215 items", "28 Apr 2025"),
            Triple("Projects", "120 items", "27 Apr 2025")
        )
        customDbFolders.forEach { f ->
            list.add(Triple(f.name, "Custom Folder", "Just Now"))
        }
        list
    }

    val filtered = rootFolders.filter {
        searchQuery.isBlank() || it.first.contains(searchQuery, ignoreCase = true)
    }

    Column(modifier = Modifier.fillMaxSize()) {
        // Folders Header Row
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Folders", color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold)

            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("Name ↑", color = Color(0xFFA09EC0), fontSize = 12.sp)
                Spacer(modifier = Modifier.width(12.dp))
                Icon(Icons.Default.GridView, contentDescription = null, tint = Color(0xFF6B6893), modifier = Modifier.size(16.dp))
            }
        }

        if (isGridView) {
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(filtered) { (name, count, date) ->
                    GridFolderItemCard(
                        name = name,
                        count = count,
                        date = date,
                        onClick = { onOpenFolder(1L, name) },
                        onMenuClick = { onOpenItemMenu(name) }
                    )
                }
            }
        } else {
            LazyColumn(
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(filtered) { (name, count, date) ->
                    ListFolderRowItem(
                        name = name,
                        count = count,
                        date = date,
                        onClick = { onOpenFolder(1L, name) },
                        onMenuClick = { onOpenItemMenu(name) }
                    )
                }
            }
        }
    }
}

// List Folder Row Item Composable
@Composable
private fun ListFolderRowItem(
    name: String,
    count: String,
    date: String,
    onClick: () -> Unit,
    onMenuClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(Color(0xFF14132B))
            .border(1.dp, Color(0xFF28264A), RoundedCornerShape(14.dp))
            .clickable { onClick() }
            .padding(horizontal = 14.dp, vertical = 12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                // Blue Folder Icon Container
                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(0xFF1E2952)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.Folder, contentDescription = null, tint = Color(0xFF3B82F6), modifier = Modifier.size(24.dp))
                }

                Spacer(modifier = Modifier.width(14.dp))

                Column {
                    Text(name, color = Color.White, fontSize = 14.5.sp, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(count, color = Color(0xFFA09EC0), fontSize = 11.5.sp)
                }
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(date, color = Color(0xFF6B6893), fontSize = 11.5.sp)
                Spacer(modifier = Modifier.width(8.dp))
                IconButton(onClick = onMenuClick, modifier = Modifier.size(24.dp)) {
                    Icon(Icons.Default.MoreVert, contentDescription = "Menu", tint = Color(0xFF6B6893), modifier = Modifier.size(16.dp))
                }
            }
        }
    }
}

// Grid Folder Item Card Composable
@Composable
private fun GridFolderItemCard(
    name: String,
    count: String,
    date: String,
    onClick: () -> Unit,
    onMenuClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(Color(0xFF14132B))
            .border(1.dp, Color(0xFF28264A), RoundedCornerShape(16.dp))
            .clickable { onClick() }
            .padding(14.dp)
    ) {
        Column {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(0xFF1E2952)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.Folder, contentDescription = null, tint = Color(0xFF3B82F6), modifier = Modifier.size(26.dp))
                }

                IconButton(onClick = onMenuClick, modifier = Modifier.size(24.dp)) {
                    Icon(Icons.Default.MoreVert, contentDescription = "Menu", tint = Color(0xFF6B6893), modifier = Modifier.size(16.dp))
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            Text(name, color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis)
            Spacer(modifier = Modifier.height(2.dp))
            Text(count, color = Color(0xFFA09EC0), fontSize = 11.5.sp)
            Spacer(modifier = Modifier.height(1.dp))
            Text(date, color = Color(0xFF6B6893), fontSize = 10.5.sp)
        }
    }
}

// ================= 3. RECENT TAB CONTENT =================
@Composable
private fun RecentTabContent(
    dbService: DatabaseService,
    searchQuery: String,
    onSelectRecentFile: (RecentFileItem) -> Unit,
    onOpenItemMenu: (Any) -> Unit
) {
    val recents = remember {
        listOf(
            // Today
            RecentFileItem("Electrostatics Formula Sheet.txt", "2.4 MB", "10:30 AM", "TXT", "Today"),
            RecentFileItem("Physics Notes.pdf", "12.6 MB", "09:15 AM", "PDF", "Today"),
            RecentFileItem("Organic Chemistry Notes.pdf", "8.7 MB", "08:45 AM", "PDF", "Today"),
            RecentFileItem("Maths Mock Test 12.pdf", "25.3 MB", "08:20 AM", "PDF", "Today"),
            // Yesterday
            RecentFileItem("Modern Physics Notes.txt", "3.1 MB", "Yesterday, 09:40 PM", "TXT", "Yesterday"),
            RecentFileItem("JEE PYQ Chapter 5.pdf", "15.2 MB", "Yesterday, 07:30 PM", "PDF", "Yesterday"),
            RecentFileItem("Wave Optics Mind Map.png", "1.8 MB", "Yesterday, 06:10 PM", "IMG", "Yesterday"),
            // This Week
            RecentFileItem("Thermodynamics Summary.txt", "2.2 MB", "2 May 2025", "TXT", "This Week"),
            RecentFileItem("Hydrocarbons Notes.pdf", "9.4 MB", "2 May 2025", "PDF", "This Week")
        )
    }

    val grouped = recents.groupBy { it.category }

    LazyColumn(
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        grouped.forEach { (category, files) ->
            item {
                Text(category, color = Color.White, fontSize = 14.5.sp, fontWeight = FontWeight.Bold)
            }

            items(files) { file ->
                RecentFileRowItem(
                    file = file,
                    onClick = { onSelectRecentFile(file) },
                    onMenuClick = { onOpenItemMenu(file) }
                )
            }
        }
    }
}

// Recent File Row Item Composable
@Composable
private fun RecentFileRowItem(
    file: RecentFileItem,
    onClick: () -> Unit,
    onMenuClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(Color(0xFF14132B))
            .border(1.dp, Color(0xFF28264A), RoundedCornerShape(14.dp))
            .clickable { onClick() }
            .padding(horizontal = 14.dp, vertical = 12.dp)
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
                // File Type Badge Container
                FileTypeBadgeIcon(fileType = file.fileType)

                Spacer(modifier = Modifier.width(14.dp))

                Column {
                    Text(file.title, color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                    Spacer(modifier = Modifier.height(2.dp))
                    Text("${file.size}  •  ${file.dateOrTime}", color = Color(0xFFA09EC0), fontSize = 11.5.sp)
                }
            }

            IconButton(onClick = onMenuClick, modifier = Modifier.size(24.dp)) {
                Icon(Icons.Default.MoreVert, contentDescription = "Menu", tint = Color(0xFF6B6893), modifier = Modifier.size(16.dp))
            }
        }
    }
}

// ================= 4. VEDM-T TAB CONTENT =================
@Composable
private fun VedmtTabContent(
    dbService: DatabaseService,
    searchQuery: String,
    onSelectDocument: (DriveDocument) -> Unit,
    onOpenItemMenu: (Any) -> Unit
) {
    val vedmtDocs = remember {
        listOf(
            Pair("Physics Formulas.txt", "2.4 MB • Indexed on 2 May 2025"),
            Pair("Chemistry Organic Reactions.txt", "3.1 MB • Indexed on 1 May 2025"),
            Pair("Maths Important Formulas.txt", "1.8 MB • Indexed on 30 Apr 2025"),
            Pair("JEE Short Notes.txt", "4.2 MB • Indexed on 29 Apr 2025"),
            Pair("Constants & Units.txt", "0.9 MB • Indexed on 28 Apr 2025")
        )
    }

    LazyColumn(
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Hero Banner Card for VEDM-T Knowledge Base
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(18.dp))
                    .background(
                        Brush.linearGradient(
                            colors = listOf(Color(0xFF18153B), Color(0xFF121A3B))
                        )
                    )
                    .border(1.dp, Color(0xFF28264A), RoundedCornerShape(18.dp))
                    .padding(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("VEDM-T Knowledge Base", color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            "Your offline AI brain. Import .txt files and get customized answers.",
                            color = Color(0xFFA09EC0),
                            fontSize = 11.5.sp,
                            lineHeight = 16.sp
                        )

                        Spacer(modifier = Modifier.height(14.dp))

                        // Stats Pill Row
                        Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                            Column {
                                Text("Indexed Files", color = Color(0xFF6B6893), fontSize = 10.sp)
                                Text("256 files", color = Color.White, fontSize = 12.5.sp, fontWeight = FontWeight.Bold)
                            }
                            Column {
                                Text("Total Size", color = Color(0xFF6B6893), fontSize = 10.sp)
                                Text("512 MB", color = Color.White, fontSize = 12.5.sp, fontWeight = FontWeight.Bold)
                            }
                            Column {
                                Text("Index Status", color = Color(0xFF6B6893), fontSize = 10.sp)
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text("Up to date", color = Color(0xFF10B981), fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                    Spacer(modifier = Modifier.width(3.dp))
                                    Icon(Icons.Default.Check, contentDescription = null, tint = Color(0xFF10B981), modifier = Modifier.size(13.dp))
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    // Glowing Book Graphic
                    Box(
                        modifier = Modifier
                            .size(52.dp)
                            .clip(RoundedCornerShape(14.dp))
                            .background(Color(0xFF3B82F6).copy(alpha = 0.2f))
                            .border(1.dp, Color(0xFF3B82F6).copy(alpha = 0.5f), RoundedCornerShape(14.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.Book, contentDescription = null, tint = Color(0xFF3B82F6), modifier = Modifier.size(28.dp))
                    }
                }
            }
        }

        // Header: Indexed Documents
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Indexed Documents", color = Color.White, fontSize = 15.sp, fontWeight = FontWeight.Bold)

                Row {
                    IconButton(onClick = {}, modifier = Modifier.size(28.dp)) {
                        Icon(Icons.Default.FilterList, contentDescription = null, tint = Color(0xFF6B6893), modifier = Modifier.size(18.dp))
                    }
                    IconButton(onClick = {}, modifier = Modifier.size(28.dp)) {
                        Icon(Icons.Default.GridView, contentDescription = null, tint = Color(0xFF6B6893), modifier = Modifier.size(18.dp))
                    }
                }
            }
        }

        // Indexed Documents List
        items(vedmtDocs) { (title, subtitle) ->
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(Color(0xFF14132B))
                    .border(1.dp, Color(0xFF28264A), RoundedCornerShape(14.dp))
                    .clickable {
                        onSelectDocument(
                            DriveDocument(id = 1, folderId = 1, title = title, content = subtitle, fileType = "TXT")
                        )
                    }
                    .padding(horizontal = 14.dp, vertical = 12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        // Green Document Icon
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(Color(0xFF12322B)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.Description, contentDescription = null, tint = Color(0xFF10B981), modifier = Modifier.size(22.dp))
                        }

                        Spacer(modifier = Modifier.width(14.dp))

                        Column {
                            Text(title, color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(subtitle, color = Color(0xFFA09EC0), fontSize = 11.5.sp)
                        }
                    }

                    IconButton(onClick = { onOpenItemMenu(title) }, modifier = Modifier.size(24.dp)) {
                        Icon(Icons.Default.MoreVert, contentDescription = "Menu", tint = Color(0xFF6B6893), modifier = Modifier.size(16.dp))
                    }
                }
            }
        }
    }
}

// ================= 5. FOLDER CONTENTS VIEW (e.g. "Study Material") =================
@Composable
private fun FolderContentsView(
    dbService: DatabaseService,
    folderId: Long,
    folderName: String,
    isGridView: Boolean,
    onToggleGridView: () -> Unit,
    searchQuery: String,
    onSearchChange: (String) -> Unit,
    isSearchActive: Boolean,
    onToggleSearch: () -> Unit,
    onBack: () -> Unit,
    onOpenSubFolder: (Long, String) -> Unit,
    onSelectFile: (DriveDocument) -> Unit,
    onOpenItemMenu: (Any) -> Unit,
    onUploadClick: () -> Unit,
    onCreateFolderClick: () -> Unit
) {
    val subfolders = remember {
        listOf(
            Triple("Physics", "128 items", "2 May 2025"),
            Triple("Chemistry", "98 items", "1 May 2025"),
            Triple("Mathematics", "156 items", "30 Apr 2025"),
            Triple("NCERT", "42 items", "29 Apr 2025")
        )
    }

    val folderFiles = remember {
        listOf(
            DriveDocument(id = 101, folderId = folderId, title = "Important Formulas.txt", content = "Physics & Chem formulas...", fileType = "TXT", fileSize = 2400000L),
            DriveDocument(id = 102, folderId = folderId, title = "Revision Notes.pdf", content = "PDF Summary...", fileType = "PDF", fileSize = 8700000L),
            DriveDocument(id = 103, folderId = folderId, title = "Summary.pdf", content = "Comprehensive Summary...", fileType = "PDF", fileSize = 4200000L),
            DriveDocument(id = 104, folderId = folderId, title = "Mind Map.png", content = "Visual Mind Map...", fileType = "IMG", fileSize = 1800000L)
        )
    }

    Column(modifier = Modifier.fillMaxSize()) {
        // Top Navigation Bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(
                    onClick = onBack,
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF1B1A38))
                ) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
                }

                Spacer(modifier = Modifier.width(12.dp))

                Text(folderName, color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold)
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onToggleSearch) {
                    Icon(if (isSearchActive) Icons.Default.Close else Icons.Default.Search, contentDescription = "Search", tint = Color.White)
                }
                IconButton(onClick = {}) {
                    Icon(Icons.Default.FilterList, contentDescription = "Sort", tint = Color.White)
                }
                IconButton(onClick = {}) {
                    Icon(Icons.Default.MoreVert, contentDescription = "Menu", tint = Color.White)
                }
            }
        }

        // Breadcrumb Trail Row
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("VEDrive", color = Color(0xFFA09EC0), fontSize = 12.sp, modifier = Modifier.clickable { onBack() })
            Text("  >  ", color = Color(0xFF6B6893), fontSize = 12.sp)
            Text(folderName, color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)

            Spacer(modifier = Modifier.weight(1f))

            IconButton(onClick = onToggleGridView, modifier = Modifier.size(28.dp)) {
                Icon(
                    if (isGridView) Icons.AutoMirrored.Filled.List else Icons.Default.GridView,
                    contentDescription = null,
                    tint = Color(0xFFA855F7),
                    modifier = Modifier.size(18.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        if (isGridView) {
            // GRID VIEW (Exact match to Bottom Middle Screenshot)
            LazyVerticalGrid(
                columns = GridCells.Fixed(3),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(subfolders) { (subName, count, date) ->
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(14.dp))
                            .background(Color(0xFF14132B))
                            .border(1.dp, Color(0xFF28264A), RoundedCornerShape(14.dp))
                            .clickable { onOpenSubFolder(2L, subName) }
                            .padding(12.dp)
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(Color(0xFF1E2952)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Default.Folder, contentDescription = null, tint = Color(0xFF3B82F6), modifier = Modifier.size(24.dp))
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(subName, color = Color.White, fontSize = 12.5.sp, fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                            Text(count, color = Color(0xFF6B6893), fontSize = 10.sp)
                        }
                    }
                }

                items(folderFiles) { doc ->
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(14.dp))
                            .background(Color(0xFF14132B))
                            .border(1.dp, Color(0xFF28264A), RoundedCornerShape(14.dp))
                            .clickable { onSelectFile(doc) }
                            .padding(12.dp)
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            FileTypeBadgeIcon(fileType = doc.fileType)
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(doc.title, color = Color.White, fontSize = 11.5.sp, fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                            Text("${doc.fileSize / 1000000.0} MB", color = Color(0xFF6B6893), fontSize = 10.sp)
                        }
                    }
                }

                // Dotted Upload Files Card
                item {
                    DottedUploadCard(onUploadClick = onUploadClick)
                }
            }
        } else {
            // LIST VIEW (Exact match to Bottom Left Screenshot)
            LazyColumn(
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Headers Row
                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 8.dp, vertical = 4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Name ↑", color = Color(0xFF6B6893), fontSize = 11.5.sp, modifier = Modifier.weight(1.5f))
                        Text("Date Modified", color = Color(0xFF6B6893), fontSize = 11.5.sp, modifier = Modifier.weight(1f), textAlign = TextAlign.Center)
                        Text("Size", color = Color(0xFF6B6893), fontSize = 11.5.sp, modifier = Modifier.weight(0.8f), textAlign = TextAlign.End)
                    }
                }

                items(subfolders) { (subName, count, date) ->
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(14.dp))
                            .background(Color(0xFF14132B))
                            .border(1.dp, Color(0xFF28264A), RoundedCornerShape(14.dp))
                            .clickable { onOpenSubFolder(2L, subName) }
                            .padding(horizontal = 12.dp, vertical = 10.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.weight(1.5f)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(34.dp)
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(Color(0xFF1E2952)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(Icons.Default.Folder, contentDescription = null, tint = Color(0xFF3B82F6), modifier = Modifier.size(20.dp))
                                }
                                Spacer(modifier = Modifier.width(10.dp))
                                Text(subName, color = Color.White, fontSize = 13.5.sp, fontWeight = FontWeight.Bold, maxLines = 1)
                            }

                            Text(date, color = Color(0xFFA09EC0), fontSize = 11.5.sp, modifier = Modifier.weight(1f), textAlign = TextAlign.Center)
                            Text(count, color = Color(0xFFA09EC0), fontSize = 11.5.sp, modifier = Modifier.weight(0.8f), textAlign = TextAlign.End)
                        }
                    }
                }

                items(folderFiles) { doc ->
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(14.dp))
                            .background(Color(0xFF14132B))
                            .border(1.dp, Color(0xFF28264A), RoundedCornerShape(14.dp))
                            .clickable { onSelectFile(doc) }
                            .padding(horizontal = 12.dp, vertical = 10.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.weight(1.5f)
                            ) {
                                FileTypeBadgeIcon(fileType = doc.fileType, size = 34)
                                Spacer(modifier = Modifier.width(10.dp))
                                Text(doc.title, color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                            }

                            Text("2 May 2025", color = Color(0xFFA09EC0), fontSize = 11.5.sp, modifier = Modifier.weight(1f), textAlign = TextAlign.Center)
                            Text("${String.format("%.1f", doc.fileSize / 1000000.0)} MB", color = Color(0xFFA09EC0), fontSize = 11.5.sp, modifier = Modifier.weight(0.8f), textAlign = TextAlign.End)
                        }
                    }
                }
            }
        }
    }
}

// Dotted Upload Files Card Component
@Composable
private fun DottedUploadCard(onUploadClick: () -> Unit) {
    val pathEffect = remember { PathEffect.dashPathEffect(floatArrayOf(10f, 10f), 0f) }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(100.dp)
            .clip(RoundedCornerShape(14.dp))
            .background(Color(0xFF0F0E24))
            .drawWithContent {
                drawContent()
                drawRoundRect(
                    color = Color(0xFF3B2E6E),
                    style = Stroke(width = 2.dp.toPx(), pathEffect = pathEffect),
                    cornerRadius = androidx.compose.ui.geometry.CornerRadius(14.dp.toPx())
                )
            }
            .clickable { onUploadClick() },
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(Icons.Default.CloudUpload, contentDescription = null, tint = Color(0xFF8B5CF6), modifier = Modifier.size(24.dp))
            Spacer(modifier = Modifier.height(4.dp))
            Text("Upload Files", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
        }
    }
}

// ================= 6. FILE DETAILS SCREEN (Exact match to Bottom Right Screenshot) =================
@Composable
private fun FileDetailsScreen(
    document: DriveDocument?,
    recentFile: RecentFileItem?,
    onBack: () -> Unit,
    onOpen: () -> Unit,
    onShare: () -> Unit,
    onRename: () -> Unit,
    onMove: () -> Unit,
    onCopy: () -> Unit,
    onAddToVedmT: () -> Unit,
    onDelete: () -> Unit
) {
    val title = document?.title ?: recentFile?.title ?: "Physics Notes.pdf"
    val size = document?.let { "${it.fileSize / 1000000.0} MB" } ?: recentFile?.size ?: "12.6 MB"
    val fileType = document?.fileType ?: recentFile?.fileType ?: "PDF"
    val path = recentFile?.path ?: "VEDrive / Study Material / Physics"

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
    ) {
        // Top Bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(
                    onClick = onBack,
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF1B1A38))
                ) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
                }

                Spacer(modifier = Modifier.width(12.dp))

                Text("File Details", color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold)
            }

            IconButton(onClick = {}) {
                Icon(Icons.Default.MoreVert, contentDescription = "Menu", tint = Color.White)
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Main File Header Card
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(18.dp))
                .background(Color(0xFF14132B))
                .border(1.dp, Color(0xFF28264A), RoundedCornerShape(18.dp))
                .padding(16.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                // File Big Badge Icon
                FileTypeBadgeIcon(fileType = fileType, size = 56)

                Spacer(modifier = Modifier.width(16.dp))

                Column {
                    Text(title, color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(path, color = Color(0xFFA09EC0), fontSize = 11.5.sp)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text("$fileType Document    $size", color = Color(0xFF6B6893), fontSize = 11.sp)
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Metadata Properties Card
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(18.dp))
                .background(Color(0xFF14132B))
                .border(1.dp, Color(0xFF28264A), RoundedCornerShape(18.dp))
                .padding(16.dp)
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                MetadataPropertyRow("Date Modified", "2 May 2025, 09:15 AM")
                MetadataPropertyRow("Date Created", "28 Apr 2025, 11:20 AM")
                MetadataPropertyRow("Contains", "42 pages")
                MetadataPropertyRow("Location", "Internal Storage")
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Status", color = Color(0xFFA09EC0), fontSize = 12.5.sp)
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("Synced", color = Color.White, fontSize = 12.5.sp, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.width(6.dp))
                        Icon(Icons.Default.CheckCircle, contentDescription = null, tint = Color(0xFF10B981), modifier = Modifier.size(16.dp))
                    }
                }
            }
        }

        Spacer(modifier = Modifier.weight(1f))

        // 8 Quick Action Buttons Grid (2 rows x 4 columns)
        val actions = listOf(
            Triple("Open", Icons.Default.OpenInNew, onOpen),
            Triple("Share", Icons.Default.Share, onShare),
            Triple("Rename", Icons.Default.Edit, onRename),
            Triple("Move", Icons.Default.DriveFileMove, onMove),
            Triple("Copy", Icons.Default.ContentCopy, onCopy),
            Triple("Details", Icons.Default.Info, {}),
            Triple("Add to VEDM-T", Icons.Default.AutoAwesome, onAddToVedmT),
            Triple("Delete", Icons.Default.Delete, onDelete)
        )

        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            actions.chunked(4).forEach { rowActions ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    rowActions.forEach { (label, icon, onClick) ->
                        val isDelete = label == "Delete"
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(14.dp))
                                .background(Color(0xFF14132B))
                                .border(1.dp, if (isDelete) Color(0xFF7F1D1D) else Color(0xFF28264A), RoundedCornerShape(14.dp))
                                .clickable { onClick() }
                                .padding(vertical = 14.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(
                                    icon,
                                    contentDescription = label,
                                    tint = if (isDelete) Color(0xFFEF4444) else Color.White,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    label,
                                    color = if (isDelete) Color(0xFFEF4444) else Color.White,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Medium,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))
    }
}

// Property Metadata Row Helper
@Composable
private fun MetadataPropertyRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(label, color = Color(0xFFA09EC0), fontSize = 12.5.sp)
        Text(value, color = Color.White, fontSize = 12.5.sp, fontWeight = FontWeight.Bold)
    }
}

// File Type Badge Icon helper
@Composable
private fun FileTypeBadgeIcon(fileType: String, size: Int = 42) {
    val (bgColor, iconColor, textLabel) = when (fileType.uppercase(Locale.ROOT)) {
        "PDF" -> Triple(Color(0xFF3B1A24), Color(0xFFEF4444), "PDF")
        "IMG", "PNG", "JPG" -> Triple(Color(0xFF12322B), Color(0xFF10B981), "IMG")
        else -> Triple(Color(0xFF1A2B42), Color(0xFF3B82F6), "TXT")
    }

    Box(
        modifier = Modifier
            .size(size.dp)
            .clip(RoundedCornerShape((size / 3.5).dp))
            .background(bgColor),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(
                when (fileType.uppercase(Locale.ROOT)) {
                    "PDF" -> Icons.Default.Description
                    "IMG", "PNG", "JPG" -> Icons.Default.Description
                    else -> Icons.Default.InsertDriveFile
                },
                contentDescription = null,
                tint = iconColor,
                modifier = Modifier.size((size / 2).dp)
            )
            Text(textLabel, color = iconColor, fontSize = (size / 5.5).sp, fontWeight = FontWeight.Bold)
        }
    }
}

// ================= FILE CONTENT VIEWER SCREEN =================
@Composable
private fun FileContentViewerScreen(
    document: DriveDocument?,
    onBack: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onBack) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
                }
                Spacer(modifier = Modifier.width(8.dp))
                Text(document?.title ?: "Document Viewer", color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold)
            }
        }

        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(Color(0xFF14132B))
                .border(1.dp, Color(0xFF28264A), RoundedCornerShape(16.dp))
                .padding(16.dp)
                .verticalScroll(rememberScrollState())
        ) {
            Text(
                text = document?.content ?: "Sample Physics Notes Content:\n\n1. Electrostatics: Force F = k * (q1 * q2) / r^2.\n2. Electric Field E = F / q.\n3. Electric Potential V = k * q / r.\n4. Gauss's Law: Net flux through any closed surface is Q / Epsilon_0.",
                color = Color.White,
                fontSize = 14.sp,
                lineHeight = 22.sp
            )
        }

        Spacer(modifier = Modifier.height(16.dp))
    }
}

// ================= MODALS & DIALOGS =================
@Composable
private fun CreateFolderModalDialog(
    onDismiss: () -> Unit,
    onCreate: (String) -> Unit
) {
    var name by remember { mutableStateOf("") }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = Color(0xFF14132B),
            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF8B5CF6)),
            modifier = Modifier.padding(16.dp)
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Text("Create New Folder", color = Color.White, fontSize = 17.sp, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    placeholder = { Text("Folder name...", color = Color(0xFF6B6893)) },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color(0xFF8B5CF6),
                        unfocusedBorderColor = Color(0xFF28264A),
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White
                    )
                )

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    Text(
                        "Cancel",
                        color = Color(0xFFA09EC0),
                        modifier = Modifier
                            .clickable { onDismiss() }
                            .padding(8.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(10.dp))
                            .background(Color(0xFF8B5CF6))
                            .clickable { if (name.isNotBlank()) onCreate(name) }
                            .padding(horizontal = 16.dp, vertical = 8.dp)
                    ) {
                        Text("Create", color = Color.White, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
private fun CreateFileModalDialog(
    onDismiss: () -> Unit,
    onCreate: (String, String, String) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var content by remember { mutableStateOf("") }
    var type by remember { mutableStateOf("TXT") }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = Color(0xFF14132B),
            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF8B5CF6)),
            modifier = Modifier.padding(16.dp)
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Text("Create New Document", color = Color.White, fontSize = 17.sp, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    placeholder = { Text("File title...", color = Color(0xFF6B6893)) },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color(0xFF8B5CF6),
                        unfocusedBorderColor = Color(0xFF28264A),
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White
                    )
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = content,
                    onValueChange = { content = it },
                    placeholder = { Text("File content...", color = Color(0xFF6B6893)) },
                    modifier = Modifier.height(100.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color(0xFF8B5CF6),
                        unfocusedBorderColor = Color(0xFF28264A),
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White
                    )
                )

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    Text(
                        "Cancel",
                        color = Color(0xFFA09EC0),
                        modifier = Modifier
                            .clickable { onDismiss() }
                            .padding(8.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(10.dp))
                            .background(Color(0xFF8B5CF6))
                            .clickable { if (title.isNotBlank()) onCreate(title, content, type) }
                            .padding(horizontal = 16.dp, vertical = 8.dp)
                    ) {
                        Text("Create File", color = Color.White, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
private fun RenameModalDialog(
    item: Any,
    onDismiss: () -> Unit,
    onRename: (String) -> Unit
) {
    var newName by remember {
        mutableStateOf(
            when (item) {
                is DriveFolder -> item.name
                is DriveDocument -> item.title
                is RecentFileItem -> item.title
                else -> item.toString()
            }
        )
    }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = Color(0xFF14132B),
            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF8B5CF6)),
            modifier = Modifier.padding(16.dp)
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Text("Rename Item", color = Color.White, fontSize = 17.sp, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = newName,
                    onValueChange = { newName = it },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color(0xFF8B5CF6),
                        unfocusedBorderColor = Color(0xFF28264A),
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White
                    )
                )

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    Text(
                        "Cancel",
                        color = Color(0xFFA09EC0),
                        modifier = Modifier
                            .clickable { onDismiss() }
                            .padding(8.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(10.dp))
                            .background(Color(0xFF8B5CF6))
                            .clickable { if (newName.isNotBlank()) onRename(newName) }
                            .padding(horizontal = 16.dp, vertical = 8.dp)
                    ) {
                        Text("Save", color = Color.White, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
private fun ItemActionBottomSheet(
    item: Any,
    onDismiss: () -> Unit,
    onSelectDetails: () -> Unit,
    onRename: () -> Unit,
    onDelete: () -> Unit,
    onShare: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = Color(0xFF14132B),
            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF28264A)),
            modifier = Modifier.padding(16.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = when (item) {
                        is DriveFolder -> item.name
                        is DriveDocument -> item.title
                        is RecentFileItem -> item.title
                        else -> item.toString()
                    },
                    color = Color.White,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(14.dp))

                ActionMenuItem(Icons.Default.Info, "View Details") { onSelectDetails() }
                ActionMenuItem(Icons.Default.Edit, "Rename") { onRename() }
                ActionMenuItem(Icons.Default.Share, "Share") { onShare() }
                ActionMenuItem(Icons.Default.Delete, "Delete", isDanger = true) { onDelete() }
            }
        }
    }
}

@Composable
private fun ActionMenuItem(
    icon: ImageVector,
    label: String,
    isDanger: Boolean = false,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icon, contentDescription = null, tint = if (isDanger) Color(0xFFEF4444) else Color.White, modifier = Modifier.size(18.dp))
        Spacer(modifier = Modifier.width(12.dp))
        Text(label, color = if (isDanger) Color(0xFFEF4444) else Color.White, fontSize = 13.5.sp)
    }
}
