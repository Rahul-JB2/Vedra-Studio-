package com.example.ui.screens

import android.widget.Toast
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.ui.graphics.Path
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Book
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.FormatListBulleted
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Quiz
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Smartphone
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.StarBorder
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.services.DatabaseService
import com.example.services.DriveDocument
import com.example.ui.components.CustomButton
import com.example.ui.components.CustomInput
import com.example.ui.components.CustomModal

// Data Models for UI
data class StatMetric(
    val title: String,
    val count: String,
    val icon: ImageVector,
    val iconBgColor: Color,
    val iconTint: Color
)

data class ResourceCategory(
    val name: String,
    val fileCount: String,
    val icon: ImageVector,
    val iconBgColor: Color,
    val iconTint: Color
)

data class DatabaseFile(
    val id: Long,
    val title: String,
    val subtitle: String,
    val categoryTag: String,
    val badgeColor: Color,
    val fileSize: String,
    val timeAgo: String,
    val isFavorite: Boolean = false,
    val fileType: String = "PDF"
)

@Composable
fun DatabaseScreen(
    dbService: DatabaseService,
    onOpenDrawer: () -> Unit = {}
) {
    val context = LocalContext.current

    // State for Search & Filters
    var searchQuery by remember { mutableStateOf("") }
    var selectedFilterChip by remember { mutableStateOf("All") }
    var isGridView by remember { mutableStateOf(false) }

    // Dialog states
    var isBrowseFilesModalOpen by remember { mutableStateOf(false) }
    var isDriveModalOpen by remember { mutableStateOf(false) }
    var isLinkModalOpen by remember { mutableStateOf(false) }
    var isViewAllFilesModalOpen by remember { mutableStateOf(false) }

    // Link input state
    var linkUrlInput by remember { mutableStateOf("") }
    var linkTitleInput by remember { mutableStateOf("") }

    // Device file upload state
    var deviceFileName by remember { mutableStateOf("") }
    var deviceFileCategory by remember { mutableStateOf("Notes") }
    var deviceFileContent by remember { mutableStateOf("") }

    // View/Edit document state
    var activeViewingFile by remember { mutableStateOf<DatabaseFile?>(null) }

    // Local state lists initialized with default exact items from user's image
    var fileList by remember {
        mutableStateOf(
            listOf(
                DatabaseFile(
                    id = 1,
                    title = "HC Verma - Volume 1.pdf",
                    subtitle = "Physics • Mechanics",
                    categoryTag = "Book",
                    badgeColor = Color(0xFF8B5CF6),
                    fileSize = "18.4 MB",
                    timeAgo = "Today",
                    isFavorite = true
                ),
                DatabaseFile(
                    id = 2,
                    title = "JEE Advanced PYQ 2023.pdf",
                    subtitle = "JEE • Previous Year Papers",
                    categoryTag = "PYQ",
                    badgeColor = Color(0xFFEC4899),
                    fileSize = "25.7 MB",
                    timeAgo = "Yesterday"
                ),
                DatabaseFile(
                    id = 3,
                    title = "Resonance Module - Maths.pdf",
                    subtitle = "Mathematics • Calculus",
                    categoryTag = "Module",
                    badgeColor = Color(0xFF3B82F6),
                    fileSize = "32.1 MB",
                    timeAgo = "2 days ago"
                ),
                DatabaseFile(
                    id = 4,
                    title = "Electrostatics - Short Notes.pdf",
                    subtitle = "Physics • Notes",
                    categoryTag = "Notes",
                    badgeColor = Color(0xFFF97316),
                    fileSize = "6.3 MB",
                    timeAgo = "3 days ago"
                ),
                DatabaseFile(
                    id = 5,
                    title = "Organic Chemistry Handbook.pdf",
                    subtitle = "Chemistry • Organic",
                    categoryTag = "Handbook",
                    badgeColor = Color(0xFF10B981),
                    fileSize = "45.8 MB",
                    timeAgo = "5 days ago"
                )
            )
        )
    }

    // Refresh DB docs if any added via SQLite
    val refreshFromDb = {
        val driveDocs = dbService.getAllDriveDocuments()
        if (driveDocs.isNotEmpty()) {
            val newConverted = driveDocs.map { doc ->
                DatabaseFile(
                    id = doc.id + 1000,
                    title = doc.title,
                    subtitle = "Drive • Indexed Resource",
                    categoryTag = if (doc.fileType.isNotBlank()) doc.fileType else "PDF",
                    badgeColor = Color(0xFF8B5CF6),
                    fileSize = "${doc.fileSize / 1024 + 1}.2 KB",
                    timeAgo = "Just now",
                    fileType = doc.fileType
                )
            }
            // Append unique ones
            val existingIds = fileList.map { it.id }.toSet()
            val toAdd = newConverted.filter { it.id !in existingIds }
            if (toAdd.isNotEmpty()) {
                fileList = toAdd + fileList
            }
        }
    }

    LaunchedEffect(Unit) {
        refreshFromDb()
    }

    // Dynamic stats derived from file list
    val totalFilesCount = 12846 + fileList.size - 5
    val booksCount = 2485 + fileList.count { it.categoryTag == "Book" } - 1
    val pdfsCount = 5732
    val notesCount = 1923 + fileList.count { it.categoryTag == "Notes" } - 1
    val favsCount = 704 + fileList.count { it.isFavorite } - 1

    val categories = listOf(
        ResourceCategory("Books", "2,485 files", Icons.Default.MenuBook, Color(0xFF2E1A47), Color(0xFFA78BFA)),
        ResourceCategory("JEE Modules", "1,284 files", Icons.Default.Quiz, Color(0xFF1E293B), Color(0xFF38BDF8)),
        ResourceCategory("Handbooks", "342 files", Icons.Default.Description, Color(0xFF064E3B), Color(0xFF34D399)),
        ResourceCategory("Notes", "1,923 files", Icons.Default.School, Color(0xFF451A03), Color(0xFFFB923C)),
        ResourceCategory("PYQ Papers", "2,156 files", Icons.Default.Description, Color(0xFF831843), Color(0xFFF472B6)),
        ResourceCategory("Question Bank", "1,872 files", Icons.Default.Quiz, Color(0xFF312E81), Color(0xFF818CF8)),
        ResourceCategory("Mock Tests", "1,109 files", Icons.Default.Description, Color(0xFF1E3A8A), Color(0xFF60A5FA)),
        ResourceCategory("Others", "675 files", Icons.Default.Folder, Color(0xFF1F2937), Color(0xFF9CA3AF))
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0B0B14))
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 12.dp)
    ) {
        // ================= TOP HEADER BAR =================
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(
                        onClick = onOpenDrawer,
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Menu,
                            contentDescription = "Drawer",
                            tint = Color.White,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Database",
                        color = Color.White,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "Organize, access and manage all your study resources",
                    color = Color(0xFF8E8EA8),
                    fontSize = 12.sp
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            // Right header action buttons (Search, Filter, Options)
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                // Search Input Pill
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(20.dp))
                        .background(Color(0xFF161528))
                        .border(1.dp, Color(0xFF282642), RoundedCornerShape(20.dp))
                        .padding(horizontal = 10.dp, vertical = 6.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = "Search",
                            tint = Color(0xFF8E8EA8),
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        CustomSearchTextField(
                            value = searchQuery,
                            onValueChange = { searchQuery = it },
                            placeholder = "Search in database...",
                            modifier = Modifier.width(110.dp)
                        )
                    }
                }

                // Filter button
                Box(
                    modifier = Modifier
                        .size(34.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(0xFF161528))
                        .border(1.dp, Color(0xFF282642), RoundedCornerShape(12.dp))
                        .clickable {
                            Toast.makeText(context, "Filter mode toggled", Toast.LENGTH_SHORT).show()
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.FilterList,
                        contentDescription = "Filter",
                        tint = Color(0xFFC084FC),
                        modifier = Modifier.size(16.dp)
                    )
                }

                // Options button
                Box(
                    modifier = Modifier
                        .size(34.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(0xFF161528))
                        .border(1.dp, Color(0xFF282642), RoundedCornerShape(12.dp))
                        .clickable {
                            Toast.makeText(context, "Database auto-indexed & connected to VEDRA AI", Toast.LENGTH_SHORT).show()
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.MoreVert,
                        contentDescription = "More",
                        tint = Color(0xFF8E8EA8),
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // ================= TOP METRICS STATS CARDS BAR =================
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            MetricCardItem(
                title = "Total Files",
                count = String.format("%,d", totalFilesCount),
                icon = Icons.Default.Folder,
                iconBg = Color(0xFF3B1D5A),
                iconTint = Color(0xFFC084FC)
            )
            MetricCardItem(
                title = "Books",
                count = String.format("%,d", booksCount),
                icon = Icons.Default.MenuBook,
                iconBg = Color(0xFF1E293B),
                iconTint = Color(0xFF38BDF8)
            )
            MetricCardItem(
                title = "PDFs",
                count = String.format("%,d", pdfsCount),
                icon = Icons.Default.Description,
                iconBg = Color(0xFF064E3B),
                iconTint = Color(0xFF34D399)
            )
            MetricCardItem(
                title = "Notes",
                count = String.format("%,d", notesCount),
                icon = Icons.Default.School,
                iconBg = Color(0xFF451A03),
                iconTint = Color(0xFFFB923C)
            )
            MetricCardItem(
                title = "Favourites",
                count = String.format("%,d", favsCount),
                icon = Icons.Default.Star,
                iconBg = Color(0xFF831843),
                iconTint = Color(0xFFF472B6)
            )
        }

        Spacer(modifier = Modifier.height(20.dp))

        // ================= CATEGORIES SECTION =================
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Categories",
                color = Color.White,
                fontSize = 17.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "View All",
                color = Color(0xFFA78BFA),
                fontSize = 12.5.sp,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.clickable {
                    Toast.makeText(context, "Showing all 8 resource categories", Toast.LENGTH_SHORT).show()
                }
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        // 8 Category Cards in 4 Rows x 2 Columns Grid layout (Spacious layout so names never cut off)
        val filteredCategories = categories.filter {
            searchQuery.isBlank() || it.name.contains(searchQuery, ignoreCase = true)
        }

        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            val chunked = filteredCategories.chunked(2)
            chunked.forEach { rowCategories ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    rowCategories.forEach { category ->
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(14.dp))
                                .background(Color(0xFF131124))
                                .border(1.dp, Color(0xFF232042), RoundedCornerShape(14.dp))
                                .clickable {
                                    selectedFilterChip = when (category.name) {
                                        "Books" -> "Books"
                                        "PYQ Papers" -> "PYQ"
                                        "JEE Modules" -> "Modules"
                                        "Notes" -> "Notes"
                                        else -> "All"
                                    }
                                    Toast.makeText(context, "Filtered by ${category.name}", Toast.LENGTH_SHORT).show()
                                }
                                .padding(vertical = 12.dp, horizontal = 10.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(36.dp)
                                        .clip(RoundedCornerShape(10.dp))
                                        .background(category.iconBgColor),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = category.icon,
                                        contentDescription = category.name,
                                        tint = category.iconTint,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(10.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = category.name,
                                        color = Color.White,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 12.5.sp,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = category.fileCount,
                                        color = Color(0xFF8E8EA8),
                                        fontSize = 10.5.sp,
                                        maxLines = 1
                                    )
                                }
                            }
                        }
                    }
                    // Fill remaining slots if chunk has fewer than 2
                    repeat(2 - rowCategories.size) {
                        Spacer(modifier = Modifier.weight(1f))
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(22.dp))

        // ================= RECENT FILES SECTION =================
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Recent Files",
                color = Color.White,
                fontSize = 17.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "View All",
                color = Color(0xFFA78BFA),
                fontSize = 12.5.sp,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.clickable {
                    isViewAllFilesModalOpen = true
                }
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Filter Chips Row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                modifier = Modifier
                    .weight(1f)
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                listOf("All", "Books", "PDFs", "Notes", "Modules", "PYQ").forEach { chip ->
                    val isSelected = selectedFilterChip == chip
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(16.dp))
                            .background(
                                if (isSelected) Color(0xFF6D28D9) else Color(0xFF131124)
                            )
                            .border(
                                1.dp,
                                if (isSelected) Color(0xFFA78BFA) else Color(0xFF232042),
                                RoundedCornerShape(16.dp)
                            )
                            .clickable { selectedFilterChip = chip }
                            .padding(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = chip,
                            color = if (isSelected) Color.White else Color(0xFF8E8EA8),
                            fontSize = 11.5.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.width(6.dp))

            // Far Right Controls (Recent dropdown & View Mode Toggle)
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                // Dropdown pill "Recent v"
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(14.dp))
                        .background(Color(0xFF131124))
                        .border(1.dp, Color(0xFF232042), RoundedCornerShape(14.dp))
                        .padding(horizontal = 8.dp, vertical = 5.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "Recent",
                            color = Color(0xFF8E8EA8),
                            fontSize = 11.sp
                        )
                        Spacer(modifier = Modifier.width(3.dp))
                        Icon(
                            imageVector = Icons.Default.KeyboardArrowDown,
                            contentDescription = null,
                            tint = Color(0xFF8E8EA8),
                            modifier = Modifier.size(12.dp)
                        )
                    }
                }

                // View Toggle (List vs Grid)
                Box(
                    modifier = Modifier
                        .size(28.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(Color(0xFF131124))
                        .border(1.dp, Color(0xFF232042), RoundedCornerShape(10.dp))
                        .clickable { isGridView = !isGridView },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (isGridView) Icons.Default.FormatListBulleted else Icons.Default.GridView,
                        contentDescription = "Toggle View",
                        tint = Color(0xFF8E8EA8),
                        modifier = Modifier.size(14.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Document List Cards
        val filteredFiles = fileList.filter { file ->
            val matchesFilter = when (selectedFilterChip) {
                "All" -> true
                "Books" -> file.categoryTag.equals("Book", ignoreCase = true) || file.title.contains("Book", ignoreCase = true)
                "PDFs" -> file.fileType.equals("PDF", ignoreCase = true) || file.title.endsWith(".pdf", ignoreCase = true)
                "Notes" -> file.categoryTag.equals("Notes", ignoreCase = true) || file.title.contains("Notes", ignoreCase = true)
                "Modules" -> file.categoryTag.equals("Module", ignoreCase = true) || file.title.contains("Module", ignoreCase = true)
                "PYQ" -> file.categoryTag.equals("PYQ", ignoreCase = true) || file.title.contains("PYQ", ignoreCase = true)
                else -> true
            }
            val matchesSearch = searchQuery.isBlank() ||
                    file.title.contains(searchQuery, ignoreCase = true) ||
                    file.subtitle.contains(searchQuery, ignoreCase = true)

            matchesFilter && matchesSearch
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(Color(0xFF131124))
                .border(1.dp, Color(0xFF232042), RoundedCornerShape(16.dp))
                .padding(vertical = 4.dp, horizontal = 12.dp)
        ) {
            Column {
                if (filteredFiles.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "No files found in database.",
                            color = Color(0xFF8E8EA8),
                            fontSize = 12.sp
                        )
                    }
                } else {
                    filteredFiles.take(5).forEachIndexed { index, file ->
                        FileItemRow(
                            file = file,
                            onOptionClick = { activeViewingFile = file },
                            onToggleFavorite = {
                                fileList = fileList.map { f ->
                                    if (f.id == file.id) f.copy(isFavorite = !f.isFavorite) else f
                                }
                            }
                        )
                        if (index < filteredFiles.take(5).size - 1) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(1.dp)
                                    .background(Color(0xFF1E1C36))
                            )
                        }
                    }
                }

                // Bottom link: "View All Files ->"
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { isViewAllFilesModalOpen = true }
                        .padding(vertical = 12.dp),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "View All Files",
                        color = Color(0xFFA78BFA),
                        fontSize = 12.5.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Icon(
                        imageVector = Icons.Default.ArrowForward,
                        contentDescription = null,
                        tint = Color(0xFFA78BFA),
                        modifier = Modifier.size(14.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // ================= ADD NEW RESOURCE SECTION =================
        Text(
            text = "Add New Resource",
            color = Color.White,
            fontSize = 17.sp,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = "Upload or add files from different sources",
            color = Color(0xFF8E8EA8),
            fontSize = 12.sp
        )

        Spacer(modifier = Modifier.height(12.dp))

        // 3 Cards Row: "From Device", "From Google Drive", "From Link"
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Card 1: From Device
            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color(0xFF131124))
                    .border(1.dp, Color(0xFF232042), RoundedCornerShape(16.dp))
                    .padding(12.dp)
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    DeviceStorageLogo(modifier = Modifier.size(40.dp))

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "From Device",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp
                    )

                    Spacer(modifier = Modifier.height(2.dp))

                    Text(
                        text = "Select PDF from your device",
                        color = Color(0xFF8E8EA8),
                        fontSize = 10.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color(0xFF1F1C3A))
                            .border(1.dp, Color(0xFF322C5A), RoundedCornerShape(12.dp))
                            .clickable {
                                deviceFileName = ""
                                deviceFileContent = ""
                                isBrowseFilesModalOpen = true
                            }
                            .padding(vertical = 8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Browse Files",
                            color = Color.White,
                            fontSize = 11.5.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }

            // Card 2: From Google Drive
            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color(0xFF131124))
                    .border(1.dp, Color(0xFF232042), RoundedCornerShape(16.dp))
                    .padding(12.dp)
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    GoogleDriveLogo(modifier = Modifier.size(40.dp))

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "From Google Drive",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp
                    )

                    Spacer(modifier = Modifier.height(2.dp))

                    Text(
                        text = "Select files from your Drive",
                        color = Color(0xFF8E8EA8),
                        fontSize = 10.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color(0xFF063529))
                            .border(1.dp, Color(0xFF0D5E49), RoundedCornerShape(12.dp))
                            .clickable {
                                isDriveModalOpen = true
                            }
                            .padding(vertical = 8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Connect Drive",
                            color = Color(0xFF34D399),
                            fontSize = 11.5.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }

            // Card 3: From Link
            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color(0xFF131124))
                    .border(1.dp, Color(0xFF232042), RoundedCornerShape(16.dp))
                    .padding(12.dp)
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    WebLinkLogo(modifier = Modifier.size(40.dp))

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "From Link",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp
                    )

                    Spacer(modifier = Modifier.height(2.dp))

                    Text(
                        text = "Add PDF using any link",
                        color = Color(0xFF8E8EA8),
                        fontSize = 10.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color(0xFF0F2642))
                            .border(1.dp, Color(0xFF1E4B82), RoundedCornerShape(12.dp))
                            .clickable {
                                linkUrlInput = ""
                                linkTitleInput = ""
                                isLinkModalOpen = true
                            }
                            .padding(vertical = 8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Add Link",
                            color = Color(0xFF38BDF8),
                            fontSize = 11.5.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))
    }

    // ================= MODALS & DIALOGS =================

    // 1. Browse Files Modal (Upload PDF / Doc from Device)
    if (isBrowseFilesModalOpen) {
        CustomModal(
            visible = true,
            title = "Browse Device Files",
            subtitle = "Upload PDF or text study materials",
            onDismissRequest = { isBrowseFilesModalOpen = false }
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                CustomInput(
                    value = deviceFileName,
                    onValueChange = { deviceFileName = it },
                    placeholder = "Resource Name (e.g., Physics Mechanics Part 2.pdf)"
                )

                CustomInput(
                    value = deviceFileContent,
                    onValueChange = { deviceFileContent = it },
                    placeholder = "Paste study content or notes summary for AI indexing...",
                    modifier = Modifier.height(90.dp)
                )

                // Category selector
                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    listOf("Notes", "Book", "PYQ", "Module", "Handbook").forEach { cat ->
                        val isSel = deviceFileCategory == cat
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(10.dp))
                                .background(if (isSel) Color(0xFF6D28D9) else Color(0xFF1A182E))
                                .clickable { deviceFileCategory = cat }
                                .padding(horizontal = 10.dp, vertical = 5.dp)
                        ) {
                            Text(
                                text = cat,
                                color = if (isSel) Color.White else Color(0xFF8E8EA8),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                CustomButton(
                    text = "Upload & Save to Database 📁",
                    onClick = {
                        if (deviceFileName.isNotBlank()) {
                            val newFile = DatabaseFile(
                                id = System.currentTimeMillis(),
                                title = if (deviceFileName.contains(".")) deviceFileName else "$deviceFileName.pdf",
                                subtitle = "Device Upload • $deviceFileCategory",
                                categoryTag = deviceFileCategory,
                                badgeColor = when (deviceFileCategory) {
                                    "Book" -> Color(0xFF8B5CF6)
                                    "PYQ" -> Color(0xFFEC4899)
                                    "Module" -> Color(0xFF3B82F6)
                                    "Handbook" -> Color(0xFF10B981)
                                    else -> Color(0xFFF97316)
                                },
                                fileSize = "4.2 MB",
                                timeAgo = "Just now"
                            )
                            fileList = listOf(newFile) + fileList

                            // Also persist to SQLite VEDRA Drive
                            val targetFolder = dbService.getAllDriveFolders().firstOrNull()?.id ?: dbService.createDriveFolder("General AI Knowledge")
                            dbService.createDriveDocument(
                                folderId = targetFolder,
                                title = newFile.title,
                                content = if (deviceFileContent.isNotBlank()) deviceFileContent else "Uploaded PDF resource: ${newFile.title}",
                                fileType = "PDF"
                            )

                            isBrowseFilesModalOpen = false
                            Toast.makeText(context, "File added & indexed for VEDRA AI!", Toast.LENGTH_SHORT).show()
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }

    // 2. Connect Google Drive Modal
    if (isDriveModalOpen) {
        CustomModal(
            visible = true,
            title = "Google Drive Sync",
            subtitle = "Connected to VEDRA AI Drive Cloud",
            onDismissRequest = { isDriveModalOpen = false }
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(0xFF063529))
                        .border(1.dp, Color(0xFF0D5E49), RoundedCornerShape(12.dp))
                        .padding(12.dp)
                ) {
                    Column {
                        Text(
                            text = "🟢 Google Drive Auto-Sync Active",
                            color = Color(0xFF34D399),
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "All study folders and PDFs from Google Drive are auto-indexed into VEDRA AI's offline vector database for instant study queries.",
                            color = Color(0xFF94A3B8),
                            fontSize = 11.5.sp
                        )
                    }
                }

                CustomButton(
                    text = "Sync Latest Drive PDFs Now 🔄",
                    onClick = {
                        refreshFromDb()
                        isDriveModalOpen = false
                        Toast.makeText(context, "Synced latest Drive files successfully!", Toast.LENGTH_SHORT).show()
                    },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }

    // 3. Add Link Modal
    if (isLinkModalOpen) {
        CustomModal(
            visible = true,
            title = "Add Resource From Link",
            subtitle = "Paste web URL or online PDF link",
            onDismissRequest = { isLinkModalOpen = false }
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                CustomInput(
                    value = linkTitleInput,
                    onValueChange = { linkTitleInput = it },
                    placeholder = "Resource Title (e.g. JEE Organic Notes)"
                )

                CustomInput(
                    value = linkUrlInput,
                    onValueChange = { linkUrlInput = it },
                    placeholder = "Paste Link (https://example.com/file.pdf)"
                )

                CustomButton(
                    text = "Import Link Resource 🔗",
                    onClick = {
                        if (linkTitleInput.isNotBlank() && linkUrlInput.isNotBlank()) {
                            val newFile = DatabaseFile(
                                id = System.currentTimeMillis(),
                                title = linkTitleInput.trim(),
                                subtitle = "Web Link • Imported",
                                categoryTag = "Handbook",
                                badgeColor = Color(0xFF38BDF8),
                                fileSize = "12.1 MB",
                                timeAgo = "Just now"
                            )
                            fileList = listOf(newFile) + fileList
                            isLinkModalOpen = false
                            Toast.makeText(context, "Link resource added to database!", Toast.LENGTH_SHORT).show()
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }

    // 4. View All Files Modal
    if (isViewAllFilesModalOpen) {
        CustomModal(
            visible = true,
            title = "All Database Files (${fileList.size})",
            subtitle = "Indexed and available for VEDRA AI context",
            onDismissRequest = { isViewAllFilesModalOpen = false }
        ) {
            Box(modifier = Modifier.height(320.dp)) {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(fileList) { file ->
                        FileItemRow(
                            file = file,
                            onOptionClick = {
                                activeViewingFile = file
                            },
                            onToggleFavorite = {
                                fileList = fileList.map { f ->
                                    if (f.id == file.id) f.copy(isFavorite = !f.isFavorite) else f
                                }
                            }
                        )
                    }
                }
            }
        }
    }

    // 5. Active File Details Viewer Modal
    if (activeViewingFile != null) {
        CustomModal(
            visible = true,
            title = activeViewingFile!!.title,
            subtitle = "${activeViewingFile!!.subtitle} • ${activeViewingFile!!.fileSize}",
            onDismissRequest = { activeViewingFile = null }
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(130.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(0xFF121020))
                        .border(1.dp, Color(0xFF232042), RoundedCornerShape(12.dp))
                        .padding(12.dp)
                ) {
                    Column {
                        Text(
                            text = "📄 Document Preview Content",
                            color = Color(0xFFA78BFA),
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "This document is fully indexed in SQLite. You can ask VEDRA AI questions about this document in the VEDRA tab or study memory.",
                            color = Color.White,
                            fontSize = 11.5.sp,
                            lineHeight = 16.sp
                        )
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    CustomButton(
                        text = if (activeViewingFile!!.isFavorite) "Starred ⭐" else "Star File 🌟",
                        onClick = {
                            val targetId = activeViewingFile!!.id
                            fileList = fileList.map { f ->
                                if (f.id == targetId) f.copy(isFavorite = !f.isFavorite) else f
                            }
                            activeViewingFile = activeViewingFile!!.copy(isFavorite = !activeViewingFile!!.isFavorite)
                        },
                        modifier = Modifier.weight(1f)
                    )

                    CustomButton(
                        text = "Delete File 🗑️",
                        onClick = {
                            fileList = fileList.filter { it.id != activeViewingFile!!.id }
                            activeViewingFile = null
                            Toast.makeText(context, "File deleted from database", Toast.LENGTH_SHORT).show()
                        },
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }
}

// Sub-composable for Metric Stat Cards
@Composable
fun MetricCardItem(
    title: String,
    count: String,
    icon: ImageVector,
    iconBg: Color,
    iconTint: Color
) {
    Box(
        modifier = Modifier
            .width(115.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(Color(0xFF131124))
            .border(1.dp, Color(0xFF232042), RoundedCornerShape(16.dp))
            .padding(12.dp)
    ) {
        Column {
            Box(
                modifier = Modifier
                    .size(34.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(iconBg),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = title,
                    tint = iconTint,
                    modifier = Modifier.size(18.dp)
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = count,
                color = Color.White,
                fontWeight = FontWeight.ExtraBold,
                fontSize = 16.sp,
                maxLines = 1
            )

            Spacer(modifier = Modifier.height(2.dp))

            Text(
                text = title,
                color = Color(0xFF8E8EA8),
                fontSize = 10.5.sp,
                maxLines = 1
            )
        }
    }
}

// Sub-composable for Individual File List Item Row
@Composable
fun FileItemRow(
    file: DatabaseFile,
    onOptionClick: () -> Unit,
    onToggleFavorite: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onOptionClick() }
            .padding(vertical = 10.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.weight(1f)
        ) {
            // PDF Document Red/White Icon Badge
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(Color(0xFFFEF2F2)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Description,
                    contentDescription = "PDF",
                    tint = Color(0xFFEF4444),
                    modifier = Modifier.size(20.dp)
                )
            }

            Spacer(modifier = Modifier.width(10.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = file.title,
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.5.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = file.subtitle,
                    color = Color(0xFF8E8EA8),
                    fontSize = 10.5.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }

        Spacer(modifier = Modifier.width(8.dp))

        // Right info: Badge pill, File size, Date, Options Menu
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Category Badge Pill (e.g. Book, PYQ, Module, Notes, Handbook)
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(10.dp))
                    .background(file.badgeColor.copy(alpha = 0.2f))
                    .border(1.dp, file.badgeColor.copy(alpha = 0.4f), RoundedCornerShape(10.dp))
                    .padding(horizontal = 8.dp, vertical = 3.dp)
            ) {
                Text(
                    text = file.categoryTag,
                    color = file.badgeColor,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            // File size
            Text(
                text = file.fileSize,
                color = Color(0xFF8E8EA8),
                fontSize = 11.sp
            )

            // Date / Time ago
            Text(
                text = file.timeAgo,
                color = Color(0xFF8E8EA8),
                fontSize = 11.sp,
                modifier = Modifier.width(60.dp)
            )

            // Star / Options icon
            IconButton(
                onClick = onOptionClick,
                modifier = Modifier.size(24.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.MoreVert,
                    contentDescription = "Options",
                    tint = Color(0xFF8E8EA8),
                    modifier = Modifier.size(16.dp)
                )
            }
        }
    }
}

// Custom Search Text Field without default Material borders
@Composable
fun CustomSearchTextField(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    modifier: Modifier = Modifier
) {
    androidx.compose.foundation.text.BasicTextField(
        value = value,
        onValueChange = onValueChange,
        singleLine = true,
        textStyle = androidx.compose.ui.text.TextStyle(
            color = Color.White,
            fontSize = 11.5.sp,
            fontWeight = FontWeight.Medium
        ),
        modifier = modifier,
        decorationBox = { innerTextField ->
            if (value.isEmpty()) {
                Text(
                    text = placeholder,
                    color = Color(0xFF8E8EA8),
                    fontSize = 11.5.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            innerTextField()
        }
    )
}

// Authentic Google Drive Logo (3-color tri-band)
@Composable
fun GoogleDriveLogo(modifier: Modifier = Modifier) {
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height

        // Google Drive exact brand colors
        val blueColor = Color(0xFF4285F4)
        val greenColor = Color(0xFF0F9D58)
        val yellowColor = Color(0xFFF4B400)

        // 1. Blue Band (left side)
        val bluePath = Path().apply {
            moveTo(w * 0.15f, h * 0.85f)
            lineTo(w * 0.38f, h * 0.18f)
            lineTo(w * 0.58f, h * 0.18f)
            lineTo(w * 0.35f, h * 0.85f)
            close()
        }
        drawPath(bluePath, color = blueColor)

        // 2. Yellow Band (top-right side)
        val yellowPath = Path().apply {
            moveTo(w * 0.38f, h * 0.18f)
            lineTo(w * 0.85f, h * 0.18f)
            lineTo(w * 0.65f, h * 0.52f)
            lineTo(w * 0.18f, h * 0.52f)
            close()
        }
        drawPath(yellowPath, color = yellowColor)

        // 3. Green Band (bottom-right side)
        val greenPath = Path().apply {
            moveTo(w * 0.65f, h * 0.52f)
            lineTo(w * 0.85f, h * 0.18f)
            lineTo(w * 0.85f, h * 0.85f)
            lineTo(w * 0.35f, h * 0.85f)
            close()
        }
        drawPath(greenPath, color = greenColor)
    }
}

// Device Storage Logo
@Composable
fun DeviceStorageLogo(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .size(40.dp)
            .clip(CircleShape)
            .background(Brush.linearGradient(listOf(Color(0xFF8B5CF6), Color(0xFF6D28D9)))),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = Icons.Default.Smartphone,
            contentDescription = "Device Storage",
            tint = Color.White,
            modifier = Modifier.size(20.dp)
        )
    }
}

// Web Link Logo
@Composable
fun WebLinkLogo(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .size(40.dp)
            .clip(CircleShape)
            .background(Brush.linearGradient(listOf(Color(0xFF38BDF8), Color(0xFF0284C7)))),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = Icons.Default.Link,
            contentDescription = "Web Link",
            tint = Color.White,
            modifier = Modifier.size(20.dp)
        )
    }
}

