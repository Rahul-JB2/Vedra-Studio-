package com.example.ui.screens

import android.content.Context
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.material.icons.filled.CreateNewFolder
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.InsertDriveFile
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.RemoveRedEye
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
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
    RECENT
}

// Full Screen Operation Views
enum class StorageScreenView {
    FILE_MANAGER,
    CREATE_ITEM,
    VIEW_FILE_CONTENT,
    ITEM_DETAILS
}

// Sample recent item data model
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
    var isGridView by remember { mutableStateOf(false) } // Default to Compact List View as requested
    var searchQuery by remember { mutableStateOf("") }
    var isSearchActive by remember { mutableStateOf(false) }
    var showOptionsMenu by remember { mutableStateOf(false) }

    // Navigation View
    var activeViewMode by remember { mutableStateOf(StorageScreenView.FILE_MANAGER) }

    // Active Items
    var activeDocument by remember { mutableStateOf<DriveDocument?>(null) }
    var activeFolder by remember { mutableStateOf<DriveFolder?>(null) }

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

    // Sample Recents List matching Image 2
    val recentFilesList = remember {
        listOf(
            SampleRecentFile("HC Verma - Volume 1.pdf", "Physics", "books > JEE", "10:30 AM", "18.4 MB", true),
            SampleRecentFile("Allen Physics Module.pdf", "Physics", "modules > Allen", "9:15 AM", "25.7 MB", true),
            SampleRecentFile("JEE Main 2024 Paper.pdf", "Previous Year Papers", "previous_year_papers > JEE Main", "Yesterday 5:10 PM", "12.8 MB", true),
            SampleRecentFile("Electrostatics Notes.pdf", "Class Notes", "notes", "Yesterday 6:20 PM", "6.3 MB", true),
            SampleRecentFile("Organic Chemistry.pdf", "Chemistry", "pdfs", "Yesterday 1:20 PM", "45.8 MB", true),
            SampleRecentFile("Physics Formula Notebook", "Notebook", "workspace > notebooks", "Today 11:05 AM", "2.1 MB", false),
            SampleRecentFile("Full Syllabus Test 01.pdf", "Mock Test", "mock_tests", "Yesterday 3:45 PM", "8.9 MB", true)
        )
    }

    // Refresh directory logic & seeding initial structure
    val refreshCurrentDirectory = {
        var folders = dbService.getAllDriveFolders(currentFolderId)
        var docs = dbService.getDocumentsInFolder(currentFolderId)

        if (currentFolderId == 0L && folders.isEmpty() && docs.isEmpty()) {
            // Seed Root Folders matching Image 1
            val booksId = dbService.createDriveFolder("books", "#EAB308", 0L)
            val modulesId = dbService.createDriveFolder("modules", "#EAB308", 0L)
            val handbooksId = dbService.createDriveFolder("handbooks", "#EAB308", 0L)
            val notesId = dbService.createDriveFolder("notes", "#EAB308", 0L)
            val pypId = dbService.createDriveFolder("previous_year_papers", "#EAB308", 0L)
            val mockId = dbService.createDriveFolder("mock_tests", "#EAB308", 0L)
            val pdfsId = dbService.createDriveFolder("pdfs", "#EAB308", 0L)
            val workspaceId = dbService.createDriveFolder("workspace", "#EAB308", 0L)

            // Seed Subfolders inside "books"
            dbService.createDriveFolder("JEE", "#EAB308", booksId)
            dbService.createDriveFolder("NCERT", "#EAB308", booksId)
            dbService.createDriveFolder("Reference", "#EAB308", booksId)
            dbService.createDriveFolder("Others", "#EAB308", booksId)

            // Seed Subfolders inside "modules"
            dbService.createDriveFolder("Allen", "#EAB308", modulesId)
            dbService.createDriveFolder("PW", "#EAB308", modulesId)
            dbService.createDriveFolder("Others", "#EAB308", modulesId)

            // Seed Subfolders inside "previous_year_papers"
            dbService.createDriveFolder("JEE Main", "#EAB308", pypId)
            dbService.createDriveFolder("JEE Advanced", "#EAB308", pypId)

            // Seed Subfolders inside "workspace"
            dbService.createDriveFolder("notebooks", "#EAB308", workspaceId)
            dbService.createDriveFolder("uploaded_files", "#EAB308", workspaceId)
            dbService.createDriveFolder("favourites", "#EAB308", workspaceId)
            dbService.createDriveFolder("shared", "#EAB308", workspaceId)

            folders = dbService.getAllDriveFolders(0L)
            docs = dbService.getDocumentsInFolder(0L)
        }

        if (searchQuery.isNotBlank()) {
            val q = searchQuery.trim().lowercase(Locale.ROOT)
            folders = folders.filter { it.name.lowercase(Locale.ROOT).contains(q) }
            docs = docs.filter { it.title.lowercase(Locale.ROOT).contains(q) || it.content.lowercase(Locale.ROOT).contains(q) }
        }

        currentSubfolders = folders
        currentDocuments = docs
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

            val ext = if (fileName.endsWith(".pdf", true)) "PDF" else "TXT"
            dbService.createDriveDocument(
                folderId = currentFolderId,
                title = fileName,
                content = "Imported File path: $it",
                fileType = ext
            )
            refreshCurrentDirectory()
            Toast.makeText(context, "Added $fileName!", Toast.LENGTH_SHORT).show()
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
                                Box(
                                    modifier = Modifier
                                        .size(20.dp)
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(Color(0xFF8B5CF6)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = "V",
                                        color = Color.White,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 13.sp
                                    )
                                }
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "VEDRA AI",
                                    color = Color.White,
                                    fontSize = 17.sp,
                                    fontWeight = FontWeight.ExtraBold
                                )
                            }
                        }

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
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
                                        text = { Text("Import Files", color = Color.White, fontSize = 13.sp) },
                                        leadingIcon = { Icon(Icons.Default.InsertDriveFile, null, tint = Color(0xFF38BDF8)) },
                                        onClick = {
                                            showOptionsMenu = false
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

                    // Title Header Section
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = if (currentFolderId == 0L) "Database" else folderStack.lastOrNull()?.name ?: "Folder",
                            color = Color.White,
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "Organize and access all your study resources",
                            color = Color(0xFF8E8EA8),
                            fontSize = 11.5.sp
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // ================= 2. DUAL TAB SWITCHER BAR =================
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Left Pill Group: Folders vs Recent
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
                                    .padding(horizontal = 14.dp, vertical = 7.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Folder,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(15.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "Folders",
                                    color = Color.White,
                                    fontSize = 12.5.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }

                            // Recent Tab
                            Row(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (selectedTab == DatabaseTab.RECENT) Color(0xFF6366F1) else Color.Transparent)
                                    .clickable { selectedTab = DatabaseTab.RECENT }
                                    .padding(horizontal = 14.dp, vertical = 7.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Schedule,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(15.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "Recent",
                                    color = Color.White,
                                    fontSize = 12.5.sp,
                                    fontWeight = FontWeight.SemiBold
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
                                text = "Database",
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
                    if (selectedTab == DatabaseTab.RECENT) {
                        // RECENT TAB VIEW (Matching Image 2)
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
                                        text = "Recent Files",
                                        color = Color.White,
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = "Your recently opened or added files",
                                        color = Color(0xFF8E8EA8),
                                        fontSize = 11.5.sp
                                    )
                                }

                                Row(
                                    modifier = Modifier.clickable {
                                        Toast.makeText(context, "Cleared recent list", Toast.LENGTH_SHORT).show()
                                    },
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Delete,
                                        contentDescription = "Clear All",
                                        tint = Color(0xFF8B5CF6),
                                        modifier = Modifier.size(14.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = "Clear All",
                                        color = Color(0xFF8B5CF6),
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            LazyColumn(
                                modifier = Modifier.fillMaxSize(),
                                verticalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                items(recentFilesList) { recentFile ->
                                    CompactRecentFileItem(
                                        recentFile = recentFile,
                                        onClick = {
                                            Toast.makeText(context, "Opening ${recentFile.title}", Toast.LENGTH_SHORT).show()
                                        }
                                    )
                                }
                                item { Spacer(modifier = Modifier.height(24.dp)) }
                            }
                        }
                    } else {
                        // FOLDERS TAB VIEW (Matching Image 1 - COMPACT HALF SIZE AS REQUESTED)
                        if (isGridView) {
                            // COMPACT GRID VIEW
                            LazyVerticalGrid(
                                columns = GridCells.Fixed(2),
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(horizontal = 14.dp),
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                items(currentSubfolders) { folder ->
                                    val count = when (folder.name) {
                                        "books" -> "4 folders • 1,245 files"
                                        "modules" -> "3 folders • 876 files"
                                        "handbooks" -> "245 files"
                                        "notes" -> "1,326 files"
                                        "previous_year_papers" -> "2 folders • 2,341 files"
                                        "mock_tests" -> "1,105 files"
                                        "pdfs" -> "3,652 files"
                                        "workspace" -> "6 folders • 2,134 files"
                                        else -> "${dbService.getDocumentsInFolder(folder.id).size} files"
                                    }

                                    CompactFolderGridCard(
                                        folder = folder,
                                        fileCountText = count,
                                        onClick = {
                                            folderStack.add(folder)
                                            currentFolderId = folder.id
                                        },
                                        onMoreClick = {
                                            activeFolder = folder
                                            activeViewMode = StorageScreenView.ITEM_DETAILS
                                        }
                                    )
                                }

                                items(currentDocuments) { doc ->
                                    CompactDocumentGridCard(
                                        doc = doc,
                                        onClick = {
                                            activeDocument = doc
                                            editedTitle = doc.title
                                            editedContent = doc.content
                                            activeViewMode = StorageScreenView.VIEW_FILE_CONTENT
                                        }
                                    )
                                }
                            }
                        } else {
                            // COMPACT LIST VIEW (Exact match to Image 1 with HALF SIZE padding)
                            LazyColumn(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(horizontal = 14.dp),
                                verticalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                items(currentSubfolders) { folder ->
                                    val count = when (folder.name) {
                                        "books" -> "4 folders • 1,245 files"
                                        "modules" -> "3 folders • 876 files"
                                        "handbooks" -> "245 files"
                                        "notes" -> "1,326 files"
                                        "previous_year_papers" -> "2 folders • 2,341 files"
                                        "mock_tests" -> "1,105 files"
                                        "pdfs" -> "3,652 files"
                                        "workspace" -> "6 folders • 2,134 files"
                                        else -> "${dbService.getDocumentsInFolder(folder.id).size} files"
                                    }

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
                                        onMoreClick = {
                                            activeFolder = folder
                                            activeViewMode = StorageScreenView.ITEM_DETAILS
                                        }
                                    )
                                }

                                items(currentDocuments) { doc ->
                                    CompactDocumentListItem(
                                        doc = doc,
                                        onClick = {
                                            activeDocument = doc
                                            editedTitle = doc.title
                                            editedContent = doc.content
                                            activeViewMode = StorageScreenView.VIEW_FILE_CONTENT
                                        }
                                    )
                                }

                                item { Spacer(modifier = Modifier.height(24.dp)) }
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
                            Text(text = "Type: ${doc.fileType}  •  Size: ${doc.fileSize} B", color = Color(0xFF8E8EA8), fontSize = 12.sp)

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
}

// ================= COMPACT RECENT FILE ITEM (Matching Image 2) =================
@Composable
fun CompactRecentFileItem(
    recentFile: SampleRecentFile,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(Color(0xFF0F0E1E))
            .clickable { onClick() }
            .padding(horizontal = 10.dp, vertical = 8.dp), // COMPACT HALF SIZE PADDING
        verticalAlignment = Alignment.CenterVertically
    ) {
        // File Icon Box
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

        // Middle Text Info
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

        // Right side info (Timestamp, size, Eye button, 3 dots)
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

            // Eye Button
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

            Icon(
                imageVector = Icons.Default.MoreVert,
                contentDescription = "More",
                tint = Color(0xFF6B7280),
                modifier = Modifier.size(16.dp)
            )
        }
    }
}

// ================= COMPACT FOLDER LIST ITEM (Matching Image 1 - HALF SIZE) =================
@Composable
fun CompactFolderListItem(
    folder: DriveFolder,
    fileCountText: String,
    onClick: () -> Unit,
    onRecentClick: () -> Unit,
    onMoreClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(Color(0xFF0F0E1E))
            .clickable { onClick() }
            .padding(horizontal = 10.dp, vertical = 7.dp), // HALF SIZE PADDING
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Yellow Folder Icon
        Icon(
            imageVector = Icons.Default.Folder,
            contentDescription = null,
            tint = Color(0xFFEAB308), // Yellow folder color matching Image 1
            modifier = Modifier.size(28.dp)
        )

        Spacer(modifier = Modifier.width(10.dp))

        // Name & Subtext
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

        // [ 👁 Recent ] Button
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

        // 3 Dots Menu Button
        IconButton(
            onClick = onMoreClick,
            modifier = Modifier.size(28.dp)
        ) {
            Icon(
                imageVector = Icons.Default.MoreVert,
                contentDescription = "More",
                tint = Color(0xFF6B7280),
                modifier = Modifier.size(16.dp)
            )
        }
    }
}

// ================= COMPACT DOCUMENT LIST ITEM =================
@Composable
fun CompactDocumentListItem(
    doc: DriveDocument,
    onClick: () -> Unit
) {
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
                text = "${doc.fileType} • ${doc.fileSize} B",
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
    }
}

// ================= COMPACT GRID CARDS =================
@Composable
fun CompactFolderGridCard(
    folder: DriveFolder,
    fileCountText: String,
    onClick: () -> Unit,
    onMoreClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(Color(0xFF0F0E1E))
            .clickable { onClick() }
            .padding(10.dp) // COMPACT PADDING
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

            IconButton(
                onClick = onMoreClick,
                modifier = Modifier.size(24.dp)
            ) {
                Icon(Icons.Default.MoreVert, "More", tint = Color(0xFF6B7280), modifier = Modifier.size(14.dp))
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
    onClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(Color(0xFF0F0E1E))
            .clickable { onClick() }
            .padding(10.dp)
    ) {
        Icon(
            imageVector = if (doc.title.endsWith(".pdf", true)) Icons.Default.PictureAsPdf else Icons.Default.Description,
            contentDescription = null,
            tint = if (doc.title.endsWith(".pdf", true)) Color(0xFFEF4444) else Color(0xFF38BDF8),
            modifier = Modifier.size(26.dp)
        )

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
            text = "${doc.fileType} • ${doc.fileSize} B",
            color = Color(0xFF8E8EA8),
            fontSize = 10.sp
        )
    }
}
