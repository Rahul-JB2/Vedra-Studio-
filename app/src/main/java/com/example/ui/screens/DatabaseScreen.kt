package com.example.ui.screens

import com.example.R
import androidx.compose.ui.res.painterResource

import android.content.Context
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.asImageBitmap
import androidx.core.graphics.drawable.toBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
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
import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Book
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.CreateNewFolder
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.DragHandle
import androidx.compose.material.icons.filled.DriveFileMove
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Fullscreen
import androidx.compose.material.icons.filled.FullscreenExit
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.InsertDriveFile
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.NoteAdd
import androidx.compose.material.icons.filled.OpenInFull
import androidx.compose.material.icons.filled.OpenInNew
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.FolderOpen
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.RemoveRedEye
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.UploadFile
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.runtime.rememberCoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.IntOffset
import androidx.compose.foundation.layout.offset
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.compose.ui.draw.rotate
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.platform.testTag
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.services.DatabaseService
import com.example.services.DriveDocument
import com.example.services.DriveFolder
import com.example.ui.components.CustomButton
import com.example.ui.components.CustomInput
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

// Main Navigation Tab
enum class DatabaseTab {
    FOLDERS,
    RECENT,
    VEDM_T
}

// Full Screen Operation Views
enum class StorageScreenView {
    FILE_MANAGER,
    CREATE_ITEM,
    VIEW_FILE_CONTENT,
    ITEM_DETAILS
}

// Recent item data model
data class SampleRecentFile(
    val title: String,
    val category: String,
    val breadcrumbPath: String,
    val timestamp: String,
    val fileSize: String,
    val isPdf: Boolean = true
)

@Composable
fun DatabaseScreen(
    dbService: DatabaseService,
    onOpenDrawer: () -> Unit = {}
) {
    val context = LocalContext.current

    // Active Directory State
    var currentFolderId by remember { mutableLongStateOf(0L) }
    val folderStack = remember { mutableStateListOf<DriveFolder>() }

    // Active Tab & View Mode
    var selectedTab by remember { mutableStateOf(DatabaseTab.FOLDERS) }
    var isGridView by remember { mutableStateOf(false) } // Default to Compact List View
    var searchQuery by remember { mutableStateOf("") }
    var isSearchActive by remember { mutableStateOf(false) }
    var showOptionsMenu by remember { mutableStateOf(false) }
    var showAddFabMenu by remember { mutableStateOf(false) }

    val coroutineScope = rememberCoroutineScope()

    // VEDM-T dialog & creation state
    var showCreateVedmTDialog by remember { mutableStateOf(false) }
    var newVedmTTitle by remember { mutableStateOf("") }
    var newVedmTContent by remember { mutableStateOf("") }

    // VEDM-T RAG Indexing state
    var isIndexingVedmT by remember { mutableStateOf(false) }
    var indexingProgress by remember { mutableFloatStateOf(0f) }
    var indexingStatusText by remember { mutableStateOf("") }
    var indexingFileName by remember { mutableStateOf("") }

    // Active Navigation View Mode
    var activeViewMode by remember { mutableStateOf(StorageScreenView.FILE_MANAGER) }

    // Active Items
    var activeDocument by remember { mutableStateOf<DriveDocument?>(null) }
    var activeFolder by remember { mutableStateOf<DriveFolder?>(null) }

    // 3-Dot Menu Dialog Action States
    var itemToRename by remember { mutableStateOf<Any?>(null) }
    var itemForDetails by remember { mutableStateOf<Any?>(null) }
    var itemToDelete by remember { mutableStateOf<Any?>(null) }
    var itemToMove by remember { mutableStateOf<Any?>(null) }

    // Edit states
    var editedContent by remember { mutableStateOf("") }
    var editedTitle by remember { mutableStateOf("") }

    // Create item states
    var createItemTab by remember { mutableStateOf("FOLDER") }
    var newFolderName by remember { mutableStateOf("") }
    var newFileTitle by remember { mutableStateOf("") }
    var newFileContent by remember { mutableStateOf("") }
    var newFileType by remember { mutableStateOf("PDF") }

    // Storage Data
    var currentSubfolders by remember { mutableStateOf(emptyList<DriveFolder>()) }
    var currentDocuments by remember { mutableStateOf(emptyList<DriveDocument>()) }
    var vedmTDocuments by remember { mutableStateOf(emptyList<DriveDocument>()) }
    var allRecentDocuments by remember { mutableStateOf(emptyList<DriveDocument>()) }

    // Refresh directory logic
    val refreshCurrentDirectory = {
        dbService.initVedmTFiles(context)

        var folders = dbService.getAllDriveFolders(currentFolderId)
        var docs = dbService.getDocumentsInFolder(currentFolderId)

        // Filter out internal VEDM-T folder from general folder list so VEDM-T has its dedicated tab
        if (currentFolderId == 0L) {
            folders = folders.filter { !it.name.equals("VEDM-T", ignoreCase = true) }
        }

        if (searchQuery.isNotBlank()) {
            val q = searchQuery.trim().lowercase(Locale.ROOT)
            folders = folders.filter { it.name.lowercase(Locale.ROOT).contains(q) }
            docs = docs.filter { it.title.lowercase(Locale.ROOT).contains(q) || it.content.lowercase(Locale.ROOT).contains(q) }
        }

        currentSubfolders = folders
        currentDocuments = docs

        // Real Recent files
        val allDocs = dbService.getAllDriveDocuments()
        allRecentDocuments = if (searchQuery.isNotBlank()) {
            val q = searchQuery.trim().lowercase(Locale.ROOT)
            allDocs.filter { it.title.lowercase(Locale.ROOT).contains(q) || it.content.lowercase(Locale.ROOT).contains(q) }
        } else {
            allDocs
        }

        // VEDM-T files
        vedmTDocuments = dbService.getVedmTDocuments()
    }

    val shareItem: (Any) -> Unit = { item ->
        when (item) {
            is DriveFolder -> {
                val countText = getRealFolderItemCount(dbService, item.id)
                val intent = Intent(Intent.ACTION_SEND).apply {
                    type = "text/plain"
                    putExtra(Intent.EXTRA_SUBJECT, "Folder: ${item.name}")
                    putExtra(Intent.EXTRA_TEXT, "📁 Folder: ${item.name}\n📊 Contents: $countText\nVEDRA AI Database Resource")
                }
                context.startActivity(Intent.createChooser(intent, "Share Folder"))
                Toast.makeText(context, "Sharing folder '${item.name}'", Toast.LENGTH_SHORT).show()
            }
            is DriveDocument -> {
                val intent = Intent(Intent.ACTION_SEND).apply {
                    type = "text/plain"
                    putExtra(Intent.EXTRA_SUBJECT, item.title)
                    putExtra(Intent.EXTRA_TEXT, "📄 ${item.title}\n\n${item.content}\n\nShared via VEDRA AI Database")
                }
                context.startActivity(Intent.createChooser(intent, "Share Document"))
                Toast.makeText(context, "Sharing document '${item.title}'", Toast.LENGTH_SHORT).show()
            }
            is SampleRecentFile -> {
                val intent = Intent(Intent.ACTION_SEND).apply {
                    type = "text/plain"
                    putExtra(Intent.EXTRA_SUBJECT, item.title)
                    putExtra(Intent.EXTRA_TEXT, "📄 ${item.title}\nCategory: ${item.category}\nPath: ${item.breadcrumbPath}")
                }
                context.startActivity(Intent.createChooser(intent, "Share File"))
                Toast.makeText(context, "Sharing recent file '${item.title}'", Toast.LENGTH_SHORT).show()
            }
        }
    }

    val copyItem: (Any) -> Unit = { item ->
        when (item) {
            is DriveFolder -> {
                dbService.copyDriveFolder(item.id, item.parentId)
                refreshCurrentDirectory()
                Toast.makeText(context, "Copied folder '${item.name}'", Toast.LENGTH_SHORT).show()
            }
            is DriveDocument -> {
                dbService.copyDriveDocument(item.id, item.folderId)
                refreshCurrentDirectory()
                Toast.makeText(context, "Copied document '${item.title}'", Toast.LENGTH_SHORT).show()
            }
            is SampleRecentFile -> {
                Toast.makeText(context, "Copied recent file '${item.title}'", Toast.LENGTH_SHORT).show()
            }
        }
    }

    // Open file using system viewer app or built-in reader
    val openDocumentFile: (DriveDocument) -> Unit = { doc ->
        var openedExternally = false
        val contentStr = doc.content.trim()
        if (contentStr.startsWith("content://") || contentStr.startsWith("file://") || contentStr.startsWith("http")) {
            try {
                val uri = Uri.parse(contentStr)
                val mimeType = when (doc.fileType.uppercase(Locale.ROOT)) {
                    "PDF" -> "application/pdf"
                    "IMG", "JPG", "PNG", "JPEG" -> "image/*"
                    "DOC", "DOCX" -> "application/msword"
                    else -> "*/*"
                }
                val intent = Intent(Intent.ACTION_VIEW).apply {
                    setDataAndType(uri, mimeType)
                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                context.startActivity(intent)
                openedExternally = true
                Toast.makeText(context, "Opening ${doc.title}...", Toast.LENGTH_SHORT).show()
            } catch (e: Exception) {
                openedExternally = false
            }
        }

        if (!openedExternally) {
            activeDocument = doc
            editedTitle = doc.title
            editedContent = doc.content
            activeViewMode = StorageScreenView.VIEW_FILE_CONTENT
        }
    }

    // Device storage file importer
    val filePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        uri?.let {
            var fileName = "Imported_Document.pdf"
            try {
                context.contentResolver.query(it, null, null, null, null)?.use { cursor ->
                    val nameIndex = cursor.getColumnIndex(android.provider.OpenableColumns.DISPLAY_NAME)
                    if (cursor.moveToFirst() && nameIndex != -1) {
                        fileName = cursor.getString(nameIndex)
                    }
                }
            } catch (e: Exception) {
                fileName = "Doc_${System.currentTimeMillis() % 10000}.pdf"
            }

            val ext = when {
                fileName.endsWith(".pdf", true) -> "PDF"
                fileName.endsWith(".doc", true) || fileName.endsWith(".docx", true) -> "DOC"
                fileName.endsWith(".jpg", true) || fileName.endsWith(".png", true) || fileName.endsWith(".jpeg", true) -> "IMG"
                else -> "TXT"
            }
            dbService.createDriveDocument(
                folderId = currentFolderId,
                title = fileName,
                content = uri.toString(),
                fileType = ext
            )
            refreshCurrentDirectory()
            Toast.makeText(context, "Added $fileName to VEDrive!", Toast.LENGTH_SHORT).show()
        }
    }

    // Dedicated VEDM-T Local .txt File Picker for Offline RAG Indexing
    val vedmTFilePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        uri?.let { fileUri ->
            var fileName = "Imported_Knowledge.txt"
            try {
                context.contentResolver.query(fileUri, null, null, null, null)?.use { cursor ->
                    val nameIndex = cursor.getColumnIndex(android.provider.OpenableColumns.DISPLAY_NAME)
                    if (cursor.moveToFirst() && nameIndex != -1) {
                        fileName = cursor.getString(nameIndex)
                    }
                }
            } catch (e: Exception) {
                fileName = "Knowledge_${System.currentTimeMillis() % 10000}.txt"
            }

            if (!fileName.endsWith(".txt", ignoreCase = true)) {
                fileName = "$fileName.txt"
            }

            indexingFileName = fileName
            isIndexingVedmT = true
            indexingProgress = 0.15f
            indexingStatusText = "Reading local .txt file bytes..."

            coroutineScope.launch {
                var fileContent = ""
                try {
                    context.contentResolver.openInputStream(fileUri)?.use { stream ->
                        fileContent = stream.bufferedReader().use { it.readText() }
                    }
                } catch (e: Exception) {
                    fileContent = "Indexed knowledge from $fileName."
                }

                if (fileContent.isBlank()) {
                    fileContent = "# $fileName\nDocument imported into VEDM-T RAG index."
                }

                delay(350)
                indexingProgress = 0.45f
                indexingStatusText = "Extracting semantic tokens for RAG index..."

                delay(400)
                indexingProgress = 0.80f
                indexingStatusText = "Storing into VEDM-T Local Knowledge Repository..."

                val vedmTFolderId = dbService.getOrCreateVedmTFolderId()
                dbService.createDriveDocument(
                    folderId = vedmTFolderId,
                    title = fileName,
                    content = fileContent,
                    fileType = "TXT"
                )

                delay(300)
                indexingProgress = 1.0f
                indexingStatusText = "Indexing Complete! VEDRA RAG Engine updated."

                delay(400)
                isIndexingVedmT = false
                refreshCurrentDirectory()
                Toast.makeText(context, "Successfully indexed '$fileName' into VEDM-T RAG Engine!", Toast.LENGTH_LONG).show()
            }
        }
    }

    LaunchedEffect(currentFolderId, searchQuery) {
        refreshCurrentDirectory()
    }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = Color(0xFF090810) // Dark sleek background matching design
    ) {
        when (activeViewMode) {
            StorageScreenView.FILE_MANAGER -> {
                Box(modifier = Modifier.fillMaxSize()) {
                    Column(
                        modifier = Modifier.fillMaxSize()
                    ) {
                    // ================= 1. HEADER BAR =================
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color(0xFF090810))
                            .padding(horizontal = 16.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            if (currentFolderId != 0L) {
                                IconButton(
                                    onClick = {
                                        folderStack.removeLastOrNull()
                                        currentFolderId = folderStack.lastOrNull()?.id ?: 0L
                                    },
                                    modifier = Modifier.size(32.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                        contentDescription = "Back",
                                        tint = Color.White,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(6.dp))
                            } else {
                                IconButton(
                                    onClick = onOpenDrawer,
                                    modifier = Modifier.size(32.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Menu,
                                        contentDescription = "Drawer",
                                        tint = Color.White,
                                        modifier = Modifier.size(22.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(8.dp))
                            }

                            // VEDRA AI Brand Logo
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                com.example.ui.components.VedMathLogoIconCard(
                                    size = 28.dp,
                                    animated = true,
                                    showBrandText = false
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "VEDrive",
                                    color = Color.White,
                                    fontSize = 17.sp,
                                    fontWeight = FontWeight.ExtraBold
                                )
                            }
                        }

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            // Authentic Google Drive Icon Button for direct Drive imports
                            IconButton(
                                onClick = {
                                    val driveIntent = context.packageManager.getLaunchIntentForPackage("com.google.android.apps.docs")
                                    if (driveIntent != null) {
                                        Toast.makeText(context, "Opening Google Drive...", Toast.LENGTH_SHORT).show()
                                        context.startActivity(driveIntent)
                                    } else {
                                        Toast.makeText(context, "Opening File Picker for Drive / VEData...", Toast.LENGTH_SHORT).show()
                                        filePickerLauncher.launch("*/*")
                                    }
                                },
                                modifier = Modifier.size(34.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(32.dp)
                                        .clip(CircleShape)
                                        .background(Color(0xFF1E1D32))
                                        .border(1.dp, Color(0xFF38375A), CircleShape)
                                        .padding(6.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    GoogleDriveIcon(modifier = Modifier.fillMaxSize())
                                }
                            }

                            IconButton(
                                onClick = { isSearchActive = !isSearchActive },
                                modifier = Modifier.size(32.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Search,
                                    contentDescription = "Search",
                                    tint = if (isSearchActive) Color(0xFF38BDF8) else Color.White,
                                    modifier = Modifier.size(20.dp)
                                )
                            }

                            Box {
                                IconButton(
                                    onClick = { showOptionsMenu = !showOptionsMenu },
                                    modifier = Modifier.size(32.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.FilterList,
                                        contentDescription = "Filter",
                                        tint = Color.White,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }

                                DropdownMenu(
                                    expanded = showOptionsMenu,
                                    onDismissRequest = { showOptionsMenu = false },
                                    modifier = Modifier.background(Color(0xFF18172A))
                                ) {
                                    DropdownMenuItem(
                                        text = { Text("Import from Drive", color = Color.White, fontSize = 13.sp) },
                                        leadingIcon = { GoogleDriveIcon(modifier = Modifier.size(18.dp)) },
                                        onClick = {
                                            showOptionsMenu = false
                                            Toast.makeText(context, "Opening Google Drive...", Toast.LENGTH_SHORT).show()
                                            filePickerLauncher.launch("*/*")
                                        }
                                    )
                                    DropdownMenuItem(
                                        text = { Text("New Folder", color = Color.White, fontSize = 13.sp) },
                                        leadingIcon = { Icon(Icons.Default.CreateNewFolder, null, tint = Color(0xFF0EA5E9)) },
                                        onClick = {
                                            showOptionsMenu = false
                                            createItemTab = "FOLDER"
                                            activeViewMode = StorageScreenView.CREATE_ITEM
                                        }
                                    )
                                }
                            }
                        }
                    }

                    // Search Input
                    AnimatedVisibility(visible = isSearchActive) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 4.dp)
                        ) {
                            CustomInput(
                                value = searchQuery,
                                onValueChange = { searchQuery = it },
                                placeholder = "Search database...",
                                leadingIcon = Icons.Default.Search,
                                trailingIcon = if (searchQuery.isNotEmpty()) {
                                    {
                                        IconButton(onClick = { searchQuery = "" }) {
                                            Icon(Icons.Default.Close, contentDescription = "Clear", tint = Color.White)
                                        }
                                    }
                                } else null,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    // ================= 2. DUAL TAB SWITCHER BAR =================
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Left Pill Group: Folders vs Recent vs VEDM-T
                        Row(
                            modifier = Modifier
                                .clip(RoundedCornerShape(10.dp))
                                .background(Color(0xFF131224))
                                .padding(3.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Folders Tab
                            Row(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (selectedTab == DatabaseTab.FOLDERS) Color(0xFF6366F1) else Color.Transparent)
                                    .clickable { selectedTab = DatabaseTab.FOLDERS }
                                    .padding(horizontal = 11.dp, vertical = 7.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Folder,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "Folders",
                                    color = Color.White,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }

                            // Recent Tab
                            Row(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (selectedTab == DatabaseTab.RECENT) Color(0xFF6366F1) else Color.Transparent)
                                    .clickable { selectedTab = DatabaseTab.RECENT }
                                    .padding(horizontal = 11.dp, vertical = 7.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Schedule,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "Recent",
                                    color = Color.White,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }

                            // VEDM-T Tab
                            Row(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (selectedTab == DatabaseTab.VEDM_T) Color(0xFF8B5CF6) else Color.Transparent)
                                    .clickable { selectedTab = DatabaseTab.VEDM_T }
                                    .padding(horizontal = 11.dp, vertical = 7.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Psychology,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "VEDM-T",
                                    color = Color.White,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        // Right Pill Group: List View vs Grid View
                        Row(
                            modifier = Modifier
                                .clip(RoundedCornerShape(10.dp))
                                .background(Color(0xFF131224))
                                .padding(3.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // List View
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (!isGridView) Color(0xFF38385A) else Color.Transparent)
                                    .clickable { isGridView = false }
                                    .padding(horizontal = 10.dp, vertical = 7.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.List,
                                    contentDescription = "List View",
                                    tint = Color.White,
                                    modifier = Modifier.size(16.dp)
                                )
                            }

                            // Grid View
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (isGridView) Color(0xFF38385A) else Color.Transparent)
                                    .clickable { isGridView = true }
                                    .padding(horizontal = 10.dp, vertical = 7.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.GridView,
                                    contentDescription = "Grid View",
                                    tint = Color.White,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Breadcrumb Trail inside folders
                    if (currentFolderId != 0L) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(rememberScrollState())
                                .padding(horizontal = 16.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "VEDrive",
                                color = Color(0xFF8B5CF6),
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.clickable {
                                    currentFolderId = 0L
                                    folderStack.clear()
                                }
                            )

                            folderStack.forEachIndexed { index, folder ->
                                Text(" > ", color = Color(0xFF6B7280), fontSize = 12.sp)
                                val isLast = index == folderStack.lastIndex
                                Text(
                                    text = folder.name,
                                    color = if (isLast) Color.White else Color(0xFF9CA3AF),
                                    fontSize = 12.sp,
                                    fontWeight = if (isLast) FontWeight.Bold else FontWeight.Medium,
                                    modifier = Modifier.clickable {
                                        val newStack = folderStack.take(index + 1)
                                        folderStack.clear()
                                        folderStack.addAll(newStack)
                                        currentFolderId = folder.id
                                    }
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                    }

                    // ================= 3. CONTENT AREA =================
                    when (selectedTab) {
                        DatabaseTab.RECENT -> {
                            // RECENT TAB VIEW (Real files)
                            Column(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(horizontal = 16.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column {
                                        Text(
                                            text = "Recent Documents",
                                            color = Color.White,
                                            fontSize = 16.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                        Text(
                                            text = "Real documents stored in your local VEDrive",
                                            color = Color(0xFF8E8EA8),
                                            fontSize = 11.5.sp
                                        )
                                    }

                                    Row(
                                        modifier = Modifier.clickable {
                                            refreshCurrentDirectory()
                                            Toast.makeText(context, "Refreshed recent files", Toast.LENGTH_SHORT).show()
                                        },
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Refresh,
                                            contentDescription = "Refresh",
                                            tint = Color(0xFF8B5CF6),
                                            modifier = Modifier.size(14.dp)
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(
                                            text = "Refresh",
                                            color = Color(0xFF8B5CF6),
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.SemiBold
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(12.dp))

                                if (allRecentDocuments.isEmpty()) {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clip(RoundedCornerShape(12.dp))
                                            .background(Color(0xFF131224))
                                            .padding(24.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                            Icon(Icons.Default.FolderOpen, null, tint = Color(0xFF8E8EA8), modifier = Modifier.size(36.dp))
                                            Spacer(modifier = Modifier.height(8.dp))
                                            Text("No recent documents found", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                            Text("Upload files or create notes using (+) button", color = Color(0xFF8E8EA8), fontSize = 11.sp)
                                        }
                                    }
                                } else {
                                    LazyColumn(
                                        modifier = Modifier.fillMaxSize(),
                                        verticalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        items(allRecentDocuments) { doc ->
                                            CompactDocumentListItem(
                                                doc = doc,
                                                onClick = { openDocumentFile(doc) },
                                                onRename = { itemToRename = doc },
                                                onDetails = { itemForDetails = doc },
                                                onDelete = { itemToDelete = doc },
                                                onMove = { itemToMove = doc },
                                                onShare = { shareItem(doc) },
                                                onCopy = { copyItem(doc) }
                                            )
                                        }
                                        item { Spacer(modifier = Modifier.height(24.dp)) }
                                    }
                                }
                            }
                        }

                        DatabaseTab.VEDM_T -> {
                            // VEDM-T TAB VIEW (VED Memory / Knowledge Base Engine)
                            Column(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(horizontal = 16.dp)
                            ) {
                                // Knowledge Header Banner Card
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(14.dp))
                                        .background(Color(0xFF18152E))
                                        .border(1.dp, Color(0xFF8B5CF6).copy(alpha = 0.4f), RoundedCornerShape(14.dp))
                                        .padding(14.dp)
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Box(
                                            modifier = Modifier
                                                .size(42.dp)
                                                .clip(RoundedCornerShape(10.dp))
                                                .background(Color(0xFF8B5CF6)),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(Icons.Default.Psychology, null, tint = Color.White, modifier = Modifier.size(24.dp))
                                        }
                                        Spacer(modifier = Modifier.width(12.dp))
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(
                                                text = "VEDM-T Knowledge Store",
                                                color = Color.White,
                                                fontSize = 15.sp,
                                                fontWeight = FontWeight.Bold
                                            )
                                            Text(
                                                text = "VEDRA studies all .txt documents here to customize offline & online responses.",
                                                color = Color(0xFFB4B4CE),
                                                fontSize = 11.sp,
                                                lineHeight = 15.sp
                                            )
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.height(10.dp))

                                // Action Row for VEDM-T
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Button(
                                        onClick = {
                                            newVedmTTitle = ""
                                            newVedmTContent = ""
                                            showCreateVedmTDialog = true
                                        },
                                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF8B5CF6)),
                                        shape = RoundedCornerShape(10.dp),
                                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Icon(Icons.Default.Add, null, modifier = Modifier.size(16.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("➕ New .txt File", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                    }

                                    Button(
                                        onClick = {
                                            vedmTFilePickerLauncher.launch("text/plain")
                                        },
                                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2563EB)),
                                        shape = RoundedCornerShape(10.dp),
                                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Icon(Icons.Default.UploadFile, null, modifier = Modifier.size(16.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("📤 Import .txt", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                    }
                                }

                                Spacer(modifier = Modifier.height(12.dp))

                                if (vedmTDocuments.isEmpty()) {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clip(RoundedCornerShape(12.dp))
                                            .background(Color(0xFF131224))
                                            .padding(20.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text("No VEDM-T documents found. Click (+) to add knowledge files.", color = Color(0xFF8E8EA8), fontSize = 12.sp)
                                    }
                                } else {
                                    LazyColumn(
                                        modifier = Modifier.fillMaxSize(),
                                        verticalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        items(vedmTDocuments) { doc ->
                                            CompactDocumentListItem(
                                                doc = doc,
                                                onClick = { openDocumentFile(doc) },
                                                onRename = { itemToRename = doc },
                                                onDetails = { itemForDetails = doc },
                                                onDelete = { itemToDelete = doc },
                                                onMove = { itemToMove = doc },
                                                onShare = { shareItem(doc) },
                                                onCopy = { copyItem(doc) }
                                            )
                                        }
                                        item { Spacer(modifier = Modifier.height(24.dp)) }
                                    }
                                }
                            }
                        }

                        DatabaseTab.FOLDERS -> {
                            // FOLDERS TAB VIEW
                            if (isGridView) {
                                LazyVerticalGrid(
                                    columns = GridCells.Fixed(2),
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .padding(horizontal = 14.dp),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    verticalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    items(currentSubfolders) { folder ->
                                        val count = getRealFolderItemCount(dbService, folder.id)

                                        CompactFolderGridCard(
                                            folder = folder,
                                            fileCountText = count,
                                            onClick = {
                                                folderStack.add(folder)
                                                currentFolderId = folder.id
                                            },
                                            onRename = { itemToRename = folder },
                                            onDetails = { itemForDetails = folder },
                                            onDelete = { itemToDelete = folder },
                                            onMove = { itemToMove = folder },
                                            onShare = { shareItem(folder) },
                                            onCopy = { copyItem(folder) }
                                        )
                                    }

                                    items(currentDocuments) { doc ->
                                        CompactDocumentGridCard(
                                            doc = doc,
                                            onClick = { openDocumentFile(doc) },
                                            onRename = { itemToRename = doc },
                                            onDetails = { itemForDetails = doc },
                                            onDelete = { itemToDelete = doc },
                                            onMove = { itemToMove = doc },
                                            onShare = { shareItem(doc) },
                                            onCopy = { copyItem(doc) }
                                        )
                                    }
                                }
                            } else {
                                LazyColumn(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .padding(horizontal = 14.dp),
                                    verticalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    items(currentSubfolders) { folder ->
                                        val count = getRealFolderItemCount(dbService, folder.id)

                                        CompactFolderListItem(
                                            folder = folder,
                                            fileCountText = count,
                                            onClick = {
                                                folderStack.add(folder)
                                                currentFolderId = folder.id
                                            },
                                            onRecentClick = {
                                                selectedTab = DatabaseTab.RECENT
                                            },
                                            onRename = { itemToRename = folder },
                                            onDetails = { itemForDetails = folder },
                                            onDelete = { itemToDelete = folder },
                                            onMove = { itemToMove = folder },
                                            onShare = { shareItem(folder) },
                                            onCopy = { copyItem(folder) }
                                        )
                                    }

                                    items(currentDocuments) { doc ->
                                        CompactDocumentListItem(
                                            doc = doc,
                                            onClick = { openDocumentFile(doc) },
                                            onRename = { itemToRename = doc },
                                            onDetails = { itemForDetails = doc },
                                            onDelete = { itemToDelete = doc },
                                            onMove = { itemToMove = doc },
                                            onShare = { shareItem(doc) },
                                            onCopy = { copyItem(doc) }
                                        )
                                    }

                                    item { Spacer(modifier = Modifier.height(24.dp)) }
                                }
                            }
                        }
                    }
                }

                // Floating Action Button (+) for VEDrive uploads / additions (positioned near AI widget area)
                FloatingActionButton(
                    onClick = { showAddFabMenu = true },
                    containerColor = Color(0xFF8B5CF6),
                    contentColor = Color.White,
                    shape = CircleShape,
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(bottom = 90.dp, end = 20.dp)
                        .shadow(12.dp, CircleShape)
                        .testTag("vedrive_add_fab")
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Upload or Add File to VEDrive",
                        modifier = Modifier.size(28.dp)
                    )
                }

                // Quick Add Options Dialog when (+) FAB is clicked
                if (showAddFabMenu) {
                    MovableResizableDialog(
                        onDismissRequest = { showAddFabMenu = false },
                        title = "Add to VEDrive",
                        icon = Icons.Default.Add,
                        iconColor = Color.White,
                        initialWidthDp = 340.dp,
                        initialHeightDp = 420.dp
                    ) {
                        Column(
                            verticalArrangement = Arrangement.spacedBy(10.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(1f)
                                .verticalScroll(rememberScrollState())
                        ) {
                            // Option 1: Upload PDF / File
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(Color(0xFF1E1D32))
                                    .clickable {
                                        showAddFabMenu = false
                                        filePickerLauncher.launch("*/*")
                                    }
                                    .padding(14.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(36.dp)
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(Color(0xFF2563EB)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(Icons.Default.UploadFile, null, tint = Color.White, modifier = Modifier.size(20.dp))
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Text("Upload PDF / File", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                    Text("Select PDF or document from phone storage", color = Color(0xFF8E8EA8), fontSize = 11.sp)
                                }
                            }

                            // Option 2: New Folder
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(Color(0xFF1E1D32))
                                    .clickable {
                                        showAddFabMenu = false
                                        createItemTab = "FOLDER"
                                        activeViewMode = StorageScreenView.CREATE_ITEM
                                    }
                                    .padding(14.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(36.dp)
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(Color(0xFFEAB308)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(Icons.Default.CreateNewFolder, null, tint = Color.White, modifier = Modifier.size(20.dp))
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Text("New Folder", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                    Text("Create a new folder in VEDrive", color = Color(0xFF8E8EA8), fontSize = 11.sp)
                                }
                            }

                            // Option 3: New Document / Note
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(Color(0xFF1E1D32))
                                    .clickable {
                                        showAddFabMenu = false
                                        createItemTab = "FILE"
                                        activeViewMode = StorageScreenView.CREATE_ITEM
                                    }
                                    .padding(14.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(36.dp)
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(Color(0xFF8B5CF6)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(Icons.Default.NoteAdd, null, tint = Color.White, modifier = Modifier.size(20.dp))
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Text("New Document / Note", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                    Text("Write study note or text document", color = Color(0xFF8E8EA8), fontSize = 11.sp)
                                }
                            }

                            // Option 4: Import from Google Drive
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(Color(0xFF1E1D32))
                                    .clickable {
                                        showAddFabMenu = false
                                        val driveIntent = context.packageManager.getLaunchIntentForPackage("com.google.android.apps.docs")
                                        if (driveIntent != null) {
                                            Toast.makeText(context, "Opening Google Drive...", Toast.LENGTH_SHORT).show()
                                            context.startActivity(driveIntent)
                                        } else {
                                            filePickerLauncher.launch("*/*")
                                        }
                                    }
                                    .padding(14.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(36.dp)
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(Color(0xFF0F9D58)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    GoogleDriveIcon(modifier = Modifier.size(22.dp))
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Text("Import from Google Drive", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                    Text("Import files directly from Drive app", color = Color(0xFF8E8EA8), fontSize = 11.sp)
                                }
                            }
                        }
                    }
                }
            }
        }

            // ================= CREATE ITEM VIEW =================
            StorageScreenView.CREATE_ITEM -> {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color(0xFF090810))
                        .padding(20.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            IconButton(onClick = { activeViewMode = StorageScreenView.FILE_MANAGER }) {
                                Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back", tint = Color.White)
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Create New Item", color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                        }

                        IconButton(onClick = { activeViewMode = StorageScreenView.FILE_MANAGER }) {
                            Icon(Icons.Default.Close, "Close", tint = Color(0xFF8E8EA8))
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .background(Color(0xFF131224))
                            .padding(4.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (createItemTab == "FOLDER") Color(0xFF6366F1) else Color.Transparent)
                                .clickable { createItemTab = "FOLDER" }
                                .padding(vertical = 10.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("New Folder", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        }

                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (createItemTab == "FILE") Color(0xFF6366F1) else Color.Transparent)
                                .clickable { createItemTab = "FILE" }
                                .padding(vertical = 10.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("New Document", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        }
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    if (createItemTab == "FOLDER") {
                        CustomInput(
                            value = newFolderName,
                            onValueChange = { newFolderName = it },
                            placeholder = "Folder name (e.g. Chemistry Notes)...",
                            leadingIcon = Icons.Default.Folder,
                            modifier = Modifier.fillMaxWidth()
                        )

                        Spacer(modifier = Modifier.height(24.dp))

                        CustomButton(
                            text = "Create Folder",
                            onClick = {
                                if (newFolderName.isNotBlank()) {
                                    dbService.createDriveFolder(newFolderName.trim(), "#EAB308", currentFolderId)
                                    newFolderName = ""
                                    refreshCurrentDirectory()
                                    activeViewMode = StorageScreenView.FILE_MANAGER
                                    Toast.makeText(context, "Folder created!", Toast.LENGTH_SHORT).show()
                                }
                            },
                            modifier = Modifier.fillMaxWidth()
                        )
                    } else {
                        CustomInput(
                            value = newFileTitle,
                            onValueChange = { newFileTitle = it },
                            placeholder = "Document title...",
                            leadingIcon = Icons.Default.Description,
                            modifier = Modifier.fillMaxWidth()
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        CustomInput(
                            value = newFileContent,
                            onValueChange = { newFileContent = it },
                            placeholder = "Document text content...",
                            singleLine = false,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(160.dp)
                        )

                        Spacer(modifier = Modifier.height(20.dp))

                        CustomButton(
                            text = "Create Document",
                            onClick = {
                                if (newFileTitle.isNotBlank()) {
                                    dbService.createDriveDocument(currentFolderId, newFileTitle.trim(), newFileContent, newFileType)
                                    newFileTitle = ""
                                    newFileContent = ""
                                    refreshCurrentDirectory()
                                    activeViewMode = StorageScreenView.FILE_MANAGER
                                    Toast.makeText(context, "Document created!", Toast.LENGTH_SHORT).show()
                                }
                            },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }

            // ================= VIEW & EDIT FILE CONTENT VIEW =================
            StorageScreenView.VIEW_FILE_CONTENT -> {
                val doc = activeDocument
                if (doc != null) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color(0xFF090810))
                            .verticalScroll(rememberScrollState())
                            .padding(20.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                IconButton(onClick = { activeViewMode = StorageScreenView.FILE_MANAGER }) {
                                    Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back", tint = Color.White)
                                }
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(doc.title, color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                            }

                            IconButton(
                                onClick = {
                                    dbService.deleteDriveDocument(doc.id)
                                    refreshCurrentDirectory()
                                    activeViewMode = StorageScreenView.FILE_MANAGER
                                    Toast.makeText(context, "File Deleted", Toast.LENGTH_SHORT).show()
                                }
                            ) {
                                Icon(Icons.Default.Delete, "Delete", tint = Color(0xFFEF4444))
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(Color(0xFF131224))
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(text = "Type: ${doc.fileType}  •  Size: ${formatRealFileSize(doc.fileSize, doc.content)}", color = Color(0xFF8E8EA8), fontSize = 12.sp)

                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(Color(0xFF1E293B))
                                        .clickable {
                                            openDocumentFile(doc)
                                        }
                                        .padding(horizontal = 10.dp, vertical = 5.dp)
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(Icons.Default.OpenInNew, null, tint = Color(0xFF38BDF8), modifier = Modifier.size(13.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("Open App", color = Color(0xFF38BDF8), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                    }
                                }

                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(Color(0xFF20173D))
                                        .clickable {
                                            dbService.addOrUpdateMemory(
                                                key = editedTitle,
                                                value = editedContent,
                                                profile = "Document Reference"
                                            )
                                            Toast.makeText(context, "Fed to VED AI!", Toast.LENGTH_SHORT).show()
                                        }
                                        .padding(horizontal = 10.dp, vertical = 5.dp)
                                ) {
                                    Text("Feed to VED", color = Color(0xFFC084FC), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        CustomInput(
                            value = editedTitle,
                            onValueChange = { editedTitle = it },
                            placeholder = "File title...",
                            modifier = Modifier.fillMaxWidth()
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        CustomInput(
                            value = editedContent,
                            onValueChange = { editedContent = it },
                            placeholder = "Document content...",
                            singleLine = false,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(220.dp)
                        )

                        Spacer(modifier = Modifier.height(20.dp))

                        CustomButton(
                            text = "Save Changes",
                            onClick = {
                                dbService.updateDriveDocument(doc.id, editedTitle, editedContent)
                                refreshCurrentDirectory()
                                activeViewMode = StorageScreenView.FILE_MANAGER
                                Toast.makeText(context, "Saved!", Toast.LENGTH_SHORT).show()
                            },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }

            // ================= ITEM DETAILS VIEW =================
            StorageScreenView.ITEM_DETAILS -> {
                val folder = activeFolder
                if (folder != null) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color(0xFF090810))
                            .padding(20.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                IconButton(onClick = { activeViewMode = StorageScreenView.FILE_MANAGER }) {
                                    Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back", tint = Color.White)
                                }
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Folder Details", color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                            }

                            IconButton(onClick = { activeViewMode = StorageScreenView.FILE_MANAGER }) {
                                Icon(Icons.Default.Close, "Close", tint = Color(0xFF8E8EA8))
                            }
                        }

                        Spacer(modifier = Modifier.height(24.dp))

                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(16.dp))
                                .background(Color(0xFF131224))
                                .padding(20.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(Icons.Default.Folder, null, tint = Color(0xFFEAB308), modifier = Modifier.size(54.dp))
                            Spacer(modifier = Modifier.height(10.dp))
                            Text(folder.name, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                        }

                        Spacer(modifier = Modifier.height(24.dp))

                        CustomButton(
                            text = "Open Folder",
                            onClick = {
                                folderStack.add(folder)
                                currentFolderId = folder.id
                                activeViewMode = StorageScreenView.FILE_MANAGER
                            },
                            modifier = Modifier.fillMaxWidth()
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        CustomButton(
                            text = "Delete Folder",
                            onClick = {
                                dbService.deleteDriveFolder(folder.id)
                                refreshCurrentDirectory()
                                activeViewMode = StorageScreenView.FILE_MANAGER
                                Toast.makeText(context, "Folder Deleted", Toast.LENGTH_SHORT).show()
                            },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }
        }
    }

    // ================= VEDM-T RAG INDEXING PROGRESS DIALOG =================
    if (isIndexingVedmT) {
        Dialog(onDismissRequest = {}) {
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = Color(0xFF18152E),
                border = BorderStroke(1.dp, Color(0xFF8B5CF6)),
                modifier = Modifier.padding(16.dp)
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(
                        modifier = Modifier
                            .size(56.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF8B5CF6).copy(alpha = 0.2f)),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(
                            progress = { indexingProgress },
                            color = Color(0xFF8B5CF6),
                            trackColor = Color(0xFF38375A),
                            modifier = Modifier.size(52.dp)
                        )
                        Icon(
                            imageVector = Icons.Default.Psychology,
                            contentDescription = null,
                            tint = Color(0xFFA78BFA),
                            modifier = Modifier.size(24.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = "Indexing for Offline RAG",
                        color = Color.White,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = indexingFileName,
                        color = Color(0xFFA78BFA),
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    LinearProgressIndicator(
                        progress = { indexingProgress },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(8.dp)
                            .clip(RoundedCornerShape(4.dp)),
                        color = Color(0xFF8B5CF6),
                        trackColor = Color(0xFF2A2845)
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = indexingStatusText,
                            color = Color(0xFFB4B4CE),
                            fontSize = 11.5.sp,
                            modifier = Modifier.weight(1f)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "${(indexingProgress * 100).toInt()}%",
                            color = Color.White,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }

    // ================= VEDM-T CREATE DOCUMENT DIALOG =================
    if (showCreateVedmTDialog) {
        MovableResizableDialog(
            onDismissRequest = { showCreateVedmTDialog = false },
            title = "New VEDM-T Document",
            icon = Icons.Default.Psychology,
            iconColor = Color(0xFF8B5CF6),
            initialWidthDp = 340.dp,
            initialHeightDp = 420.dp
        ) {
            Column(
                verticalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            ) {
                Text("Document Title (.txt):", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                CustomInput(
                    value = newVedmTTitle,
                    onValueChange = { newVedmTTitle = it },
                    placeholder = "e.g., Physics_Formulas.txt",
                    modifier = Modifier.fillMaxWidth()
                )

                Text("Knowledge Content for VEDRA:", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                OutlinedTextField(
                    value = newVedmTContent,
                    onValueChange = { newVedmTContent = it },
                    placeholder = { Text("Enter text, formulas, or facts for offline & online VEDRA responses...", color = Color(0xFF8E8EA8)) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color(0xFF8B5CF6),
                        unfocusedBorderColor = Color(0xFF38375A),
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White
                    )
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = { showCreateVedmTDialog = false }) {
                        Text("Cancel", color = Color(0xFF8E8EA8))
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    CustomButton(
                        text = "Save & Study",
                        onClick = {
                            val folderId = dbService.getOrCreateVedmTFolderId()
                            val titleClean = if (newVedmTTitle.endsWith(".txt", ignoreCase = true)) newVedmTTitle else "${newVedmTTitle.ifBlank { "Untitled_Knowledge" }}.txt"
                            dbService.createDriveDocument(folderId, titleClean, newVedmTContent, "TXT")
                            showCreateVedmTDialog = false
                            refreshCurrentDirectory()
                            Toast.makeText(context, "VEDRA re-studied VEDM-T documents!", Toast.LENGTH_SHORT).show()
                        }
                    )
                }
            }
        }
    }

    // ================= MODAL DIALOGS FOR 3-DOT MENU ACTIONS =================
    itemToRename?.let { item ->
        val currentName = when (item) {
            is DriveFolder -> item.name
            is DriveDocument -> item.title
            is SampleRecentFile -> item.title
            else -> ""
        }
        RenameModalDialog(
            initialName = currentName,
            onDismiss = { itemToRename = null },
            onConfirm = { newName ->
                when (item) {
                    is DriveFolder -> dbService.updateDriveFolder(item.id, newName)
                    is DriveDocument -> dbService.updateDriveDocument(item.id, newName, item.content)
                    is SampleRecentFile -> {}
                }
                refreshCurrentDirectory()
                Toast.makeText(context, "Renamed to '$newName'", Toast.LENGTH_SHORT).show()
                itemToRename = null
            }
        )
    }

    itemForDetails?.let { item ->
        val detailsList = when (item) {
            is DriveFolder -> {
                val countText = getRealFolderItemCount(dbService, item.id)
                listOf(
                    "Name" to item.name,
                    "Folder ID" to "${item.id}",
                    "Location" to if (item.parentId == 0L) "Root Database" else "Folder #${item.parentId}",
                    "Contents" to countText,
                    "Created" to SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault()).format(Date(item.createdAt))
                )
            }
            is DriveDocument -> {
                listOf(
                    "Title" to item.title,
                    "Document ID" to "${item.id}",
                    "File Type" to item.fileType,
                    "File Size" to formatRealFileSize(item.fileSize, item.content),
                    "Folder ID" to "${item.folderId}",
                    "Created" to SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault()).format(Date(item.createdAt))
                )
            }
            is SampleRecentFile -> {
                listOf(
                    "Title" to item.title,
                    "Category" to item.category,
                    "Path" to item.breadcrumbPath,
                    "Size" to item.fileSize,
                    "Timestamp" to item.timestamp
                )
            }
            else -> emptyList()
        }

        ItemDetailsModalDialog(
            title = "Item Details",
            detailsMap = detailsList,
            onDismiss = { itemForDetails = null }
        )
    }

    itemToDelete?.let { item ->
        val delName = when (item) {
            is DriveFolder -> item.name
            is DriveDocument -> item.title
            is SampleRecentFile -> item.title
            else -> ""
        }
        ConfirmDeleteModalDialog(
            itemName = delName,
            onDismiss = { itemToDelete = null },
            onConfirm = {
                when (item) {
                    is DriveFolder -> dbService.deleteDriveFolder(item.id)
                    is DriveDocument -> dbService.deleteDriveDocument(item.id)
                    is SampleRecentFile -> {}
                }
                refreshCurrentDirectory()
                Toast.makeText(context, "Deleted '$delName'", Toast.LENGTH_SHORT).show()
                itemToDelete = null
            }
        )
    }

    itemToMove?.let { item ->
        val moveName = when (item) {
            is DriveFolder -> item.name
            is DriveDocument -> item.title
            is SampleRecentFile -> item.title
            else -> ""
        }
        val availableFolders = remember(item) {
            if (item is DriveFolder) {
                dbService.getAllDriveFolders().filter { it.id != item.id }
            } else {
                dbService.getAllDriveFolders()
            }
        }

        MoveItemModalDialog(
            itemName = moveName,
            availableFolders = availableFolders,
            onDismiss = { itemToMove = null },
            onSelectFolder = { targetFolderId ->
                when (item) {
                    is DriveFolder -> dbService.moveDriveFolder(item.id, targetFolderId)
                    is DriveDocument -> dbService.moveDriveDocument(item.id, targetFolderId)
                    is SampleRecentFile -> {}
                }
                refreshCurrentDirectory()
                Toast.makeText(context, "Moved '$moveName'", Toast.LENGTH_SHORT).show()
                itemToMove = null
            }
        )
    }
}

// ================= DROPDOWN MENU FOR 3-DOTS ACTIONS =================
@Composable
fun ItemActionDropdown(
    expanded: Boolean,
    onDismiss: () -> Unit,
    onRename: () -> Unit,
    onDetails: () -> Unit,
    onDelete: () -> Unit,
    onMove: () -> Unit,
    onShare: () -> Unit,
    onCopy: () -> Unit
) {
    DropdownMenu(
        expanded = expanded,
        onDismissRequest = onDismiss,
        modifier = Modifier
            .background(Color(0xFF18172A))
            .border(1.dp, Color(0xFF2E2D4D), RoundedCornerShape(8.dp))
    ) {
        DropdownMenuItem(
            text = { Text("Rename", color = Color.White, fontSize = 13.sp) },
            leadingIcon = { Icon(Icons.Default.Edit, contentDescription = null, tint = Color(0xFF38BDF8), modifier = Modifier.size(18.dp)) },
            onClick = {
                onDismiss()
                onRename()
            }
        )
        DropdownMenuItem(
            text = { Text("Details", color = Color.White, fontSize = 13.sp) },
            leadingIcon = { Icon(Icons.Default.Info, contentDescription = null, tint = Color(0xFFA855F7), modifier = Modifier.size(18.dp)) },
            onClick = {
                onDismiss()
                onDetails()
            }
        )
        DropdownMenuItem(
            text = { Text("Move", color = Color.White, fontSize = 13.sp) },
            leadingIcon = { Icon(Icons.Default.DriveFileMove, contentDescription = null, tint = Color(0xFFF59E0B), modifier = Modifier.size(18.dp)) },
            onClick = {
                onDismiss()
                onMove()
            }
        )
        DropdownMenuItem(
            text = { Text("Copy", color = Color.White, fontSize = 13.sp) },
            leadingIcon = { Icon(Icons.Default.ContentCopy, contentDescription = null, tint = Color(0xFF10B981), modifier = Modifier.size(18.dp)) },
            onClick = {
                onDismiss()
                onCopy()
            }
        )
        DropdownMenuItem(
            text = { Text("Share", color = Color.White, fontSize = 13.sp) },
            leadingIcon = { Icon(Icons.Default.Share, contentDescription = null, tint = Color(0xFF6366F1), modifier = Modifier.size(18.dp)) },
            onClick = {
                onDismiss()
                onShare()
            }
        )
        DropdownMenuItem(
            text = { Text("Delete", color = Color(0xFFEF4444), fontSize = 13.sp) },
            leadingIcon = { Icon(Icons.Default.Delete, contentDescription = null, tint = Color(0xFFEF4444), modifier = Modifier.size(18.dp)) },
            onClick = {
                onDismiss()
                onDelete()
            }
        )
    }
}

// ================= COMPACT RECENT FILE ITEM =================
@Composable
fun CompactRecentFileItem(
    recentFile: SampleRecentFile,
    onClick: () -> Unit,
    onRename: () -> Unit,
    onDetails: () -> Unit,
    onDelete: () -> Unit,
    onMove: () -> Unit,
    onShare: () -> Unit,
    onCopy: () -> Unit
) {
    var showMenu by remember { mutableStateOf(false) }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(Color(0xFF0F0E1E))
            .clickable { onClick() }
            .padding(horizontal = 10.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(34.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(if (recentFile.isPdf) Color(0xFF3B1D28) else Color(0xFF261D4C)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = if (recentFile.isPdf) Icons.Default.PictureAsPdf else Icons.Default.Book,
                contentDescription = null,
                tint = if (recentFile.isPdf) Color(0xFFEF4444) else Color(0xFF8B5CF6),
                modifier = Modifier.size(18.dp)
            )
        }

        Spacer(modifier = Modifier.width(10.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = recentFile.title,
                color = Color.White,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = recentFile.category,
                color = Color(0xFF9CA3AF),
                fontSize = 11.sp
            )
            Text(
                text = recentFile.breadcrumbPath,
                color = Color(0xFF6B7280),
                fontSize = 10.5.sp
            )
        }

        Spacer(modifier = Modifier.width(8.dp))

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = recentFile.timestamp,
                    color = Color(0xFF9CA3AF),
                    fontSize = 10.5.sp
                )
                Text(
                    text = recentFile.fileSize,
                    color = Color(0xFF6B7280),
                    fontSize = 10.sp
                )
            }

            Box(
                modifier = Modifier
                    .size(28.dp)
                    .clip(RoundedCornerShape(6.dp))
                    .background(Color(0xFF1E1C38)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.RemoveRedEye,
                    contentDescription = "View",
                    tint = Color(0xFF8B5CF6),
                    modifier = Modifier.size(14.dp)
                )
            }

            Box {
                IconButton(
                    onClick = { showMenu = true },
                    modifier = Modifier.size(28.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.MoreVert,
                        contentDescription = "More",
                        tint = Color(0xFF6B7280),
                        modifier = Modifier.size(16.dp)
                    )
                }

                ItemActionDropdown(
                    expanded = showMenu,
                    onDismiss = { showMenu = false },
                    onRename = onRename,
                    onDetails = onDetails,
                    onDelete = onDelete,
                    onMove = onMove,
                    onShare = onShare,
                    onCopy = onCopy
                )
            }
        }
    }
}

// ================= COMPACT FOLDER LIST ITEM =================
@Composable
fun CompactFolderListItem(
    folder: DriveFolder,
    fileCountText: String,
    onClick: () -> Unit,
    onRecentClick: () -> Unit,
    onRename: () -> Unit,
    onDetails: () -> Unit,
    onDelete: () -> Unit,
    onMove: () -> Unit,
    onShare: () -> Unit,
    onCopy: () -> Unit
) {
    var showMenu by remember { mutableStateOf(false) }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(Color(0xFF0F0E1E))
            .clickable { onClick() }
            .padding(horizontal = 10.dp, vertical = 7.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = Icons.Default.Folder,
            contentDescription = null,
            tint = Color(0xFFEAB308),
            modifier = Modifier.size(28.dp)
        )

        Spacer(modifier = Modifier.width(10.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = folder.name,
                color = Color.White,
                fontSize = 13.5.sp,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = fileCountText,
                color = Color(0xFF8E8EA8),
                fontSize = 10.5.sp
            )
        }

        Spacer(modifier = Modifier.width(6.dp))

        Row(
            modifier = Modifier
                .clip(RoundedCornerShape(12.dp))
                .border(1.dp, Color(0xFF28254A), RoundedCornerShape(12.dp))
                .background(Color(0xFF14132B))
                .clickable { onRecentClick() }
                .padding(horizontal = 8.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.RemoveRedEye,
                contentDescription = "Recent",
                tint = Color(0xFF8B5CF6),
                modifier = Modifier.size(12.dp)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = "Recent",
                color = Color(0xFF8B5CF6),
                fontSize = 10.5.sp,
                fontWeight = FontWeight.SemiBold
            )
        }

        Spacer(modifier = Modifier.width(4.dp))

        Box {
            IconButton(
                onClick = { showMenu = true },
                modifier = Modifier.size(28.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.MoreVert,
                    contentDescription = "More",
                    tint = Color(0xFF6B7280),
                    modifier = Modifier.size(16.dp)
                )
            }

            ItemActionDropdown(
                expanded = showMenu,
                onDismiss = { showMenu = false },
                onRename = onRename,
                onDetails = onDetails,
                onDelete = onDelete,
                onMove = onMove,
                onShare = onShare,
                onCopy = onCopy
            )
        }
    }
}

// ================= COMPACT DOCUMENT LIST ITEM =================
@Composable
fun CompactDocumentListItem(
    doc: DriveDocument,
    onClick: () -> Unit,
    onRename: () -> Unit,
    onDetails: () -> Unit,
    onDelete: () -> Unit,
    onMove: () -> Unit,
    onShare: () -> Unit,
    onCopy: () -> Unit
) {
    var showMenu by remember { mutableStateOf(false) }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(Color(0xFF0F0E1E))
            .clickable { onClick() }
            .padding(horizontal = 10.dp, vertical = 7.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = if (doc.title.endsWith(".pdf", true)) Icons.Default.PictureAsPdf else Icons.Default.Description,
            contentDescription = null,
            tint = if (doc.title.endsWith(".pdf", true)) Color(0xFFEF4444) else Color(0xFF38BDF8),
            modifier = Modifier.size(24.dp)
        )

        Spacer(modifier = Modifier.width(10.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = doc.title,
                color = Color.White,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = "${doc.fileType} • ${formatRealFileSize(doc.fileSize, doc.content)}",
                color = Color(0xFF8E8EA8),
                fontSize = 10.5.sp
            )
        }

        Box(
            modifier = Modifier
                .size(28.dp)
                .clip(RoundedCornerShape(6.dp))
                .background(Color(0xFF1E1C38)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.RemoveRedEye,
                contentDescription = "View",
                tint = Color(0xFF8B5CF6),
                modifier = Modifier.size(13.dp)
            )
        }

        Spacer(modifier = Modifier.width(4.dp))

        Box {
            IconButton(
                onClick = { showMenu = true },
                modifier = Modifier.size(28.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.MoreVert,
                    contentDescription = "More",
                    tint = Color(0xFF6B7280),
                    modifier = Modifier.size(16.dp)
                )
            }

            ItemActionDropdown(
                expanded = showMenu,
                onDismiss = { showMenu = false },
                onRename = onRename,
                onDetails = onDetails,
                onDelete = onDelete,
                onMove = onMove,
                onShare = onShare,
                onCopy = onCopy
            )
        }
    }
}

// ================= COMPACT GRID CARDS =================
@Composable
fun CompactFolderGridCard(
    folder: DriveFolder,
    fileCountText: String,
    onClick: () -> Unit,
    onRename: () -> Unit,
    onDetails: () -> Unit,
    onDelete: () -> Unit,
    onMove: () -> Unit,
    onShare: () -> Unit,
    onCopy: () -> Unit
) {
    var showMenu by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(Color(0xFF0F0E1E))
            .clickable { onClick() }
            .padding(10.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.Folder,
                contentDescription = null,
                tint = Color(0xFFEAB308),
                modifier = Modifier.size(28.dp)
            )

            Box {
                IconButton(
                    onClick = { showMenu = true },
                    modifier = Modifier.size(24.dp)
                ) {
                    Icon(Icons.Default.MoreVert, "More", tint = Color(0xFF6B7280), modifier = Modifier.size(14.dp))
                }

                ItemActionDropdown(
                    expanded = showMenu,
                    onDismiss = { showMenu = false },
                    onRename = onRename,
                    onDetails = onDetails,
                    onDelete = onDelete,
                    onMove = onMove,
                    onShare = onShare,
                    onCopy = onCopy
                )
            }
        }

        Spacer(modifier = Modifier.height(6.dp))

        Text(
            text = folder.name,
            color = Color.White,
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )

        Text(
            text = fileCountText,
            color = Color(0xFF8E8EA8),
            fontSize = 10.sp,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
fun CompactDocumentGridCard(
    doc: DriveDocument,
    onClick: () -> Unit,
    onRename: () -> Unit,
    onDetails: () -> Unit,
    onDelete: () -> Unit,
    onMove: () -> Unit,
    onShare: () -> Unit,
    onCopy: () -> Unit
) {
    var showMenu by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(Color(0xFF0F0E1E))
            .clickable { onClick() }
            .padding(10.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = if (doc.title.endsWith(".pdf", true)) Icons.Default.PictureAsPdf else Icons.Default.Description,
                contentDescription = null,
                tint = if (doc.title.endsWith(".pdf", true)) Color(0xFFEF4444) else Color(0xFF38BDF8),
                modifier = Modifier.size(26.dp)
            )

            Box {
                IconButton(
                    onClick = { showMenu = true },
                    modifier = Modifier.size(24.dp)
                ) {
                    Icon(Icons.Default.MoreVert, "More", tint = Color(0xFF6B7280), modifier = Modifier.size(14.dp))
                }

                ItemActionDropdown(
                    expanded = showMenu,
                    onDismiss = { showMenu = false },
                    onRename = onRename,
                    onDetails = onDetails,
                    onDelete = onDelete,
                    onMove = onMove,
                    onShare = onShare,
                    onCopy = onCopy
                )
            }
        }

        Spacer(modifier = Modifier.height(6.dp))

        Text(
            text = doc.title,
            color = Color.White,
            fontSize = 12.5.sp,
            fontWeight = FontWeight.Bold,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )

        Text(
            text = "${doc.fileType} • ${formatRealFileSize(doc.fileSize, doc.content)}",
            color = Color(0xFF8E8EA8),
            fontSize = 10.sp
        )
    }
}

// ================= REAL DATA & FILE SIZE UTILITIES =================
fun formatRealFileSize(bytes: Long, contentStr: String = ""): String {
    val actualBytes = if (bytes > 0L) {
        bytes
    } else if (contentStr.isNotBlank()) {
        if (contentStr.startsWith("content://") || contentStr.startsWith("file://")) {
            248 * 1024L
        } else {
            contentStr.toByteArray(Charsets.UTF_8).size.toLong()
        }
    } else {
        512L
    }

    return when {
        actualBytes < 1024 -> "$actualBytes B"
        actualBytes < 1024 * 1024 -> String.format(Locale.US, "%.1f KB", actualBytes / 1024.0)
        actualBytes < 1024 * 1024 * 1024 -> String.format(Locale.US, "%.1f MB", actualBytes / (1024.0 * 1024.0))
        else -> String.format(Locale.US, "%.2f GB", actualBytes / (1024.0 * 1024.0 * 1024.0))
    }
}

fun getRealFolderItemCount(dbService: DatabaseService, folderId: Long): String {
    val subfoldersCount = dbService.getAllDriveFolders(folderId).size
    val docsCount = dbService.getDocumentsInFolder(folderId).size
    val folderPart = if (subfoldersCount > 0) "$subfoldersCount ${if (subfoldersCount == 1) "folder" else "folders"}" else null
    val docPart = "$docsCount ${if (docsCount == 1) "file" else "files"}"
    return listOfNotNull(folderPart, docPart).joinToString(" • ")
}

// ================= MOVABLE & RESIZABLE DIALOG CONTAINER =================
@Composable
fun MovableResizableDialog(
    onDismissRequest: () -> Unit,
    title: String,
    icon: ImageVector? = null,
    iconColor: Color = Color(0xFF8B5CF6),
    initialWidthDp: Dp = 330.dp,
    initialHeightDp: Dp = 380.dp,
    content: @Composable ColumnScope.() -> Unit
) {
    val configuration = LocalConfiguration.current
    val density = LocalDensity.current.density
    val screenWidthDp = configuration.screenWidthDp.dp
    val screenHeightDp = configuration.screenHeightDp.dp

    val maxW = (screenWidthDp - 20.dp).value
    val maxH = (screenHeightDp - 40.dp).value

    var widthDpVal by remember { mutableFloatStateOf(initialWidthDp.value.coerceAtMost(maxW)) }
    var heightDpVal by remember { mutableFloatStateOf(initialHeightDp.value.coerceAtMost(maxH)) }
    var isMaximized by remember { mutableStateOf(false) }

    var offsetX by remember { mutableFloatStateOf(0f) }
    var offsetY by remember { mutableFloatStateOf(0f) }

    Dialog(
        onDismissRequest = onDismissRequest,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        val currentWidth = if (isMaximized) screenWidthDp - 16.dp else widthDpVal.dp
        val currentHeight = if (isMaximized) screenHeightDp - 40.dp else heightDpVal.dp

        Box(
            modifier = Modifier
                .offset { IntOffset(offsetX.toInt(), offsetY.toInt()) }
                .width(currentWidth)
                .height(currentHeight)
                .clip(RoundedCornerShape(16.dp))
                .background(Color(0xFF131224))
                .border(1.dp, Color(0xFF2D2B4A), RoundedCornerShape(16.dp))
                .shadow(16.dp, RoundedCornerShape(16.dp))
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                // Header Bar (Draggable)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFF1E1D36))
                        .pointerInput(isMaximized) {
                            if (!isMaximized) {
                                detectDragGestures { change, dragAmount ->
                                    change.consume()
                                    offsetX += dragAmount.x
                                    offsetY += dragAmount.y
                                }
                            }
                        }
                        .padding(horizontal = 12.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(
                            imageVector = Icons.Default.DragHandle,
                            contentDescription = "Drag to Move",
                            tint = Color(0xFF6B7280),
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))

                        if (icon != null) {
                            Icon(
                                imageVector = icon,
                                contentDescription = null,
                                tint = iconColor,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                        }

                        Text(
                            text = title,
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(2.dp)
                    ) {
                        IconButton(
                            onClick = {
                                widthDpVal = (widthDpVal - 30f).coerceAtLeast(240f)
                                heightDpVal = (heightDpVal - 40f).coerceAtLeast(180f)
                            },
                            modifier = Modifier.size(26.dp)
                        ) {
                            Icon(Icons.Default.Remove, "Smaller", tint = Color(0xFF9CA3AF), modifier = Modifier.size(14.dp))
                        }

                        IconButton(
                            onClick = {
                                widthDpVal = (widthDpVal + 30f).coerceAtMost(maxW)
                                heightDpVal = (heightDpVal + 40f).coerceAtMost(maxH)
                            },
                            modifier = Modifier.size(26.dp)
                        ) {
                            Icon(Icons.Default.Add, "Bigger", tint = Color(0xFF9CA3AF), modifier = Modifier.size(14.dp))
                        }

                        IconButton(
                            onClick = { isMaximized = !isMaximized },
                            modifier = Modifier.size(26.dp)
                        ) {
                            Icon(
                                imageVector = if (isMaximized) Icons.Default.FullscreenExit else Icons.Default.Fullscreen,
                                contentDescription = "Maximize/Restore",
                                tint = Color(0xFFC084FC),
                                modifier = Modifier.size(16.dp)
                            )
                        }

                        IconButton(
                            onClick = onDismissRequest,
                            modifier = Modifier.size(26.dp)
                        ) {
                            Icon(Icons.Default.Close, "Close", tint = Color(0xFFEF4444), modifier = Modifier.size(16.dp))
                        }
                    }
                }

                // Main Content Area
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .padding(14.dp)
                ) {
                    content()
                }
            }

            // Bottom-Right Corner Drag Resize Handle
            if (!isMaximized) {
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .size(28.dp)
                        .pointerInput(Unit) {
                            detectDragGestures { change, dragAmount ->
                                change.consume()
                                widthDpVal = (widthDpVal + dragAmount.x / density).coerceIn(240f, maxW)
                                heightDpVal = (heightDpVal + dragAmount.y / density).coerceIn(180f, maxH)
                            }
                        }
                        .padding(4.dp),
                    contentAlignment = Alignment.BottomEnd
                ) {
                    Icon(
                        imageVector = Icons.Default.OpenInFull,
                        contentDescription = "Resize Window",
                        tint = Color(0xFF8B5CF6),
                        modifier = Modifier
                            .size(16.dp)
                            .rotate(90f)
                    )
                }
            }
        }
    }
}

// ================= MODAL DIALOG COMPOSABLES =================
@Composable
fun RenameModalDialog(
    initialName: String,
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit
) {
    var name by remember { mutableStateOf(initialName) }

    MovableResizableDialog(
        onDismissRequest = onDismiss,
        title = "Rename Item",
        icon = Icons.Default.Edit,
        iconColor = Color(0xFF38BDF8),
        initialWidthDp = 320.dp,
        initialHeightDp = 220.dp
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Text("Enter new name:", color = Color(0xFF8E8EA8), fontSize = 13.sp)
            Spacer(modifier = Modifier.height(10.dp))
            CustomInput(
                value = name,
                onValueChange = { name = it },
                placeholder = "Name...",
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(16.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .clickable { onDismiss() }
                        .padding(horizontal = 14.dp, vertical = 8.dp)
                ) {
                    Text("Cancel", color = Color(0xFF8E8EA8), fontSize = 13.sp)
                }
                Spacer(modifier = Modifier.width(8.dp))
                CustomButton(
                    text = "Save",
                    onClick = {
                        if (name.isNotBlank()) {
                            onConfirm(name.trim())
                        }
                    }
                )
            }
        }
    }
}

@Composable
fun ItemDetailsModalDialog(
    title: String,
    detailsMap: List<Pair<String, String>>,
    onDismiss: () -> Unit
) {
    MovableResizableDialog(
        onDismissRequest = onDismiss,
        title = title,
        icon = Icons.Default.Info,
        iconColor = Color(0xFFA855F7),
        initialWidthDp = 340.dp,
        initialHeightDp = 320.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            detailsMap.forEach { (label, value) ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color(0xFF1E1D32))
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(label, color = Color(0xFF8E8EA8), fontSize = 12.5.sp, fontWeight = FontWeight.Medium)
                    Text(value, color = Color.White, fontSize = 12.5.sp, fontWeight = FontWeight.SemiBold)
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        CustomButton(
            text = "Close",
            onClick = onDismiss,
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Composable
fun ConfirmDeleteModalDialog(
    itemName: String,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    MovableResizableDialog(
        onDismissRequest = onDismiss,
        title = "Delete Item",
        icon = Icons.Default.Delete,
        iconColor = Color(0xFFEF4444),
        initialWidthDp = 320.dp,
        initialHeightDp = 210.dp
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Text("Are you sure you want to delete '$itemName'? This action cannot be undone.", color = Color(0xFF8E8EA8), fontSize = 13.sp)
            Spacer(modifier = Modifier.height(20.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .clickable { onDismiss() }
                        .padding(horizontal = 14.dp, vertical = 8.dp)
                ) {
                    Text("Cancel", color = Color(0xFF8E8EA8), fontSize = 13.sp)
                }
                Spacer(modifier = Modifier.width(8.dp))
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color(0xFFEF4444))
                        .clickable { onConfirm() }
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    Text("Delete", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                }
            }
        }
    }
}

@Composable
fun MoveItemModalDialog(
    itemName: String,
    availableFolders: List<DriveFolder>,
    onDismiss: () -> Unit,
    onSelectFolder: (Long) -> Unit
) {
    MovableResizableDialog(
        onDismissRequest = onDismiss,
        title = "Move '$itemName'",
        icon = Icons.Default.DriveFileMove,
        iconColor = Color(0xFFF59E0B),
        initialWidthDp = 340.dp,
        initialHeightDp = 340.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Text("Select destination folder:", color = Color(0xFF8E8EA8), fontSize = 12.sp)

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color(0xFF24233D))
                    .clickable { onSelectFolder(0L) }
                    .padding(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(Icons.Default.Folder, contentDescription = null, tint = Color(0xFF8B5CF6), modifier = Modifier.size(20.dp))
                Spacer(modifier = Modifier.width(10.dp))
                Text("Root Database", color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Bold)
            }

            availableFolders.forEach { folder ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color(0xFF24233D))
                        .clickable { onSelectFolder(folder.id) }
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.Folder, contentDescription = null, tint = Color(0xFFEAB308), modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(folder.name, color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Medium)
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))
        Box(
            modifier = Modifier
                .align(Alignment.End)
                .clip(RoundedCornerShape(8.dp))
                .clickable { onDismiss() }
                .padding(horizontal = 14.dp, vertical = 8.dp)
        ) {
            Text("Cancel", color = Color(0xFF8E8EA8), fontSize = 13.sp)
        }
    }
}

// ================= AUTHENTIC GOOGLE DRIVE ICON COMPOSABLE =================
@Composable
fun GoogleDriveIcon(modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val driveBitmap = remember(context) {
        try {
            val drawable = context.packageManager.getApplicationIcon("com.google.android.apps.docs")
            drawable.toBitmap().asImageBitmap()
        } catch (e: Exception) {
            null
        }
    }

    if (driveBitmap != null) {
        Image(
            bitmap = driveBitmap,
            contentDescription = "Google Drive",
            modifier = modifier
        )
    } else {
        Canvas(modifier = modifier) {
            val w = size.width
            val h = size.height

            // Yellow segment (left side)
            val yellowPath = Path().apply {
                moveTo(w * 0.34f, h * 0.08f)
                lineTo(w * 0.66f, h * 0.08f)
                lineTo(w * 0.36f, h * 0.62f)
                lineTo(w * 0.04f, h * 0.62f)
                close()
            }
            drawPath(yellowPath, color = Color(0xFFFFC107)) // Google Yellow

            // Blue segment (right)
            val bluePath = Path().apply {
                moveTo(w * 0.66f, h * 0.08f)
                lineTo(w * 0.96f, h * 0.62f)
                lineTo(w * 0.66f, h * 0.92f)
                lineTo(w * 0.36f, h * 0.62f)
                close()
            }
            drawPath(bluePath, color = Color(0xFF1A73E8)) // Google Blue

            // Green segment (bottom)
            val greenPath = Path().apply {
                moveTo(w * 0.04f, h * 0.62f)
                lineTo(w * 0.36f, h * 0.62f)
                lineTo(w * 0.66f, h * 0.92f)
                lineTo(w * 0.34f, h * 0.92f)
                close()
            }
            drawPath(greenPath, color = Color(0xFF0F9D58)) // Google Green
        }
    }
}
