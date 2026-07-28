package com.example.ui.components

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
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Apps
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Article
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.CreateNewFolder
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.ui.theme.Spacing
import com.example.ui.theme.VedraBorder
import com.example.ui.theme.VedraPurplePrimary
import com.example.ui.theme.VedraPurpleSecondary
import com.example.ui.theme.VedraSurface
import com.example.ui.theme.VedraSurfaceVariant
import com.example.ui.theme.VedraTextMuted
import com.example.ui.theme.VedraTextPrimary
import com.example.ui.theme.VedraTextSecondary

/**
 * Reusable CustomButton with VEDRA gradient theme styling.
 */
@Composable
fun CustomButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    icon: ImageVector? = null,
    isLoading: Boolean = false,
    enabled: Boolean = true,
    isSecondary: Boolean = false,
    fontSize: TextUnit = 13.sp,
    testTag: String = "custom_button"
) {
    val backgroundBrush = if (isSecondary) {
        Brush.linearGradient(listOf(VedraSurfaceVariant, VedraSurfaceVariant))
    } else {
        Brush.linearGradient(listOf(VedraPurplePrimary, VedraPurpleSecondary))
    }

    val shape = RoundedCornerShape(Spacing.buttonCorner)

    Box(
        modifier = modifier
            .testTag(testTag)
            .clip(shape)
            .background(backgroundBrush)
            .border(
                width = if (isSecondary) 1.dp else 0.dp,
                color = if (isSecondary) VedraBorder else Color.Transparent,
                shape = shape
            )
            .clickable(enabled = enabled && !isLoading) { onClick() }
            .padding(horizontal = 10.dp, vertical = 4.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            if (isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(14.dp),
                    color = VedraTextPrimary,
                    strokeWidth = 2.dp
                )
                Spacer(modifier = Modifier.width(4.dp))
            } else if (icon != null) {
                Icon(
                    imageVector = icon,
                    contentDescription = text,
                    tint = VedraTextPrimary,
                    modifier = Modifier.size(15.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
            }
            Text(
                text = text,
                color = if (enabled) VedraTextPrimary else VedraTextMuted,
                fontWeight = FontWeight.SemiBold,
                fontSize = fontSize,
                maxLines = 1,
                overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
            )
        }
    }
}

/**
 * Reusable CustomCard with dark surface and subtle border glow.
 */
@Composable
fun CustomCard(
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    cornerShape: RoundedCornerShape = RoundedCornerShape(Spacing.cardCorner),
    borderColor: Color = VedraBorder,
    containerColor: Color = VedraSurface,
    testTag: String = "custom_card",
    content: @Composable () -> Unit
) {
    Card(
        modifier = modifier
            .testTag(testTag)
            .fillMaxWidth()
            .then(if (onClick != null) Modifier.clickable { onClick() } else Modifier),
        shape = cornerShape,
        colors = CardDefaults.cardColors(
            containerColor = containerColor
        ),
        border = androidx.compose.foundation.BorderStroke(1.dp, borderColor)
    ) {
        Box(modifier = Modifier.padding(Spacing.medium)) {
            content()
        }
    }
}

/**
 * Reusable CustomInput field for text entry across VEDRA screens.
 */
@Composable
fun CustomInput(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    placeholder: String = "Type a command...",
    leadingIcon: ImageVector? = null,
    trailingIcon: @Composable (() -> Unit)? = null,
    singleLine: Boolean = true,
    onSend: (() -> Unit)? = null,
    testTag: String = "custom_input"
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = modifier
            .testTag(testTag)
            .fillMaxWidth()
            .defaultMinSize(minHeight = 48.dp),
        placeholder = {
            Text(
                text = placeholder,
                color = VedraTextMuted,
                fontSize = 14.sp
            )
        },
        leadingIcon = if (leadingIcon != null) {
            {
                Icon(
                    imageVector = leadingIcon,
                    contentDescription = null,
                    tint = VedraTextSecondary
                )
            }
        } else null,
        trailingIcon = trailingIcon,
        shape = RoundedCornerShape(24.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedContainerColor = VedraSurface,
            unfocusedContainerColor = VedraSurface,
            focusedBorderColor = VedraPurplePrimary,
            unfocusedBorderColor = VedraBorder,
            focusedTextColor = VedraTextPrimary,
            unfocusedTextColor = VedraTextPrimary,
            cursorColor = VedraPurplePrimary
        ),
        singleLine = singleLine,
        keyboardOptions = KeyboardOptions(
            imeAction = if (onSend != null) ImeAction.Send else ImeAction.Done
        ),
        keyboardActions = KeyboardActions(
            onSend = { onSend?.invoke() }
        )
    )
}

/**
 * Reusable CustomModal dialog component for modals/alerts/actions matching screenshot design.
 */
@Composable
fun CustomModal(
    visible: Boolean,
    title: String,
    onDismissRequest: () -> Unit,
    modifier: Modifier = Modifier,
    subtitle: String? = null,
    testTag: String = "custom_modal",
    content: @Composable () -> Unit
) {
    AnimatedVisibility(
        visible = visible,
        enter = fadeIn(),
        exit = fadeOut()
    ) {
        Dialog(onDismissRequest = onDismissRequest) {
            Surface(
                modifier = modifier
                    .testTag(testTag)
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(26.dp))
                    .border(
                        width = 1.5.dp,
                        brush = Brush.verticalGradient(
                            colors = listOf(Color(0xFF5B3BA8), Color(0xFF3B2D6B), Color(0xFF1E173D))
                        ),
                        shape = RoundedCornerShape(26.dp)
                    ),
                color = Color(0xFF0C0E1B)
            ) {
                Column(
                    modifier = Modifier.padding(20.dp)
                ) {
                    // Header Row with Shield Lock Badge, Title & Close Button
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.Top
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.weight(1f)
                        ) {
                            // Left Shield Lock Badge with Ring Glow
                            Box(
                                modifier = Modifier
                                    .size(52.dp)
                                    .clip(CircleShape)
                                    .background(
                                        Brush.linearGradient(
                                            listOf(Color(0xFF6366F1), Color(0xFF8B5CF6), Color(0xFFC084FC))
                                        )
                                    )
                                    .padding(2.5.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFF13152A)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Lock,
                                    contentDescription = null,
                                    tint = Color(0xFFC084FC),
                                    modifier = Modifier.size(24.dp)
                                )
                            }

                            Spacer(modifier = Modifier.width(14.dp))

                            // Title & Subtitle Column
                            Column {
                                Text(
                                    text = title,
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 18.sp
                                )
                                if (!subtitle.isNullOrBlank()) {
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = subtitle,
                                        color = Color(0xFF94A3B8),
                                        fontSize = 11.5.sp,
                                        lineHeight = 15.sp
                                    )
                                }
                            }
                        }

                        // Top Right Circular Close Button
                        Box(
                            modifier = Modifier
                                .size(34.dp)
                                .clip(CircleShape)
                                .background(Color(0xFF1A1D34))
                                .border(1.dp, Color(0xFF2D3252), CircleShape)
                                .clickable { onDismissRequest() },
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Close",
                                tint = Color.White,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    content()
                }
            }
        }
    }
}

/**
 * Reusable CustomList component for rendered vertical lists with empty state support.
 */
@Composable
fun <T> CustomList(
    items: List<T>,
    modifier: Modifier = Modifier,
    emptyText: String = "No items available",
    itemKey: ((T) -> Any)? = null,
    itemContent: @Composable (T) -> Unit
) {
    if (items.isEmpty()) {
        Box(
            modifier = modifier
                .fillMaxWidth()
                .padding(Spacing.large),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = emptyText,
                color = VedraTextMuted,
                fontSize = 14.sp
            )
        }
    } else {
        LazyColumn(
            modifier = modifier,
            contentPadding = PaddingValues(vertical = Spacing.small),
            verticalArrangement = Arrangement.spacedBy(Spacing.small)
        ) {
            items(
                items = items,
                key = itemKey
            ) { item ->
                itemContent(item)
            }
        }
    }
}

/**
 * AppPickerAndLockModal matching the exact user screenshot design!
 */
@Composable
fun AppPickerAndLockModal(
    visible: Boolean,
    shortcutTitle: String,
    onAppSelected: (com.example.services.AppLauncher.AppInfoItem) -> Unit,
    onDismissRequest: () -> Unit
) {
    val context = androidx.compose.ui.platform.LocalContext.current
    var searchQuery by androidx.compose.runtime.remember { androidx.compose.runtime.mutableStateOf("") }
    var installedApps by androidx.compose.runtime.remember { androidx.compose.runtime.mutableStateOf<List<com.example.services.AppLauncher.AppInfoItem>>(emptyList()) }

    androidx.compose.runtime.LaunchedEffect(visible) {
        if (visible) {
            installedApps = com.example.services.AppLauncher.getInstalledAppsOnDevice(context)
        }
    }

    val filteredApps = androidx.compose.runtime.remember(searchQuery, installedApps) {
        if (searchQuery.isBlank()) installedApps
        else installedApps.filter {
            it.label.contains(searchQuery, ignoreCase = true) || it.packageName.contains(searchQuery, ignoreCase = true)
        }
    }

    CustomModal(
        visible = visible,
        title = "Choose an app",
        subtitle = "Select an installed app to assign & lock it.\nTap the app to open it directly.",
        onDismissRequest = onDismissRequest
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
            // Search Input Field
            CustomInput(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = "Search installed apps...",
                leadingIcon = Icons.Default.Search
            )

            // App List Scroll Container
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(300.dp)
            ) {
                if (filteredApps.isEmpty()) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text(
                            text = if (installedApps.isEmpty()) "Scanning device apps..." else "No matching app found.",
                            color = Color(0xFF94A3B8),
                            fontSize = 13.sp
                        )
                    }
                } else {
                    LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(filteredApps) { app ->
                            val appIconBitmap = androidx.compose.runtime.remember(app.icon) {
                                app.icon?.let { drawable ->
                                    try {
                                        if (drawable is android.graphics.drawable.BitmapDrawable && drawable.bitmap != null) {
                                            drawable.bitmap.asImageBitmap()
                                        } else {
                                            val w = if (drawable.intrinsicWidth > 0) drawable.intrinsicWidth else 96
                                            val h = if (drawable.intrinsicHeight > 0) drawable.intrinsicHeight else 96
                                            val bmp = android.graphics.Bitmap.createBitmap(w, h, android.graphics.Bitmap.Config.ARGB_8888)
                                            val canvas = android.graphics.Canvas(bmp)
                                            drawable.setBounds(0, 0, canvas.width, canvas.height)
                                            drawable.draw(canvas)
                                            bmp.asImageBitmap()
                                        }
                                    } catch (e: Exception) { null }
                                }
                            }

                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(16.dp))
                                    .background(Color(0xFF13162A))
                                    .border(1.dp, Color(0xFF222644), RoundedCornerShape(16.dp))
                                    .clickable { onAppSelected(app) }
                                    .padding(horizontal = 14.dp, vertical = 10.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.weight(1f)
                                ) {
                                    if (appIconBitmap != null) {
                                        androidx.compose.foundation.Image(
                                            bitmap = appIconBitmap,
                                            contentDescription = app.label,
                                            modifier = Modifier
                                                .size(38.dp)
                                                .clip(RoundedCornerShape(10.dp))
                                        )
                                        Spacer(modifier = Modifier.width(12.dp))
                                    } else {
                                        Box(
                                            modifier = Modifier
                                                .size(38.dp)
                                                .clip(RoundedCornerShape(10.dp))
                                                .background(Color(0xFF231C4A)),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Apps,
                                                contentDescription = null,
                                                tint = Color(0xFFC084FC),
                                                modifier = Modifier.size(20.dp)
                                            )
                                        }
                                        Spacer(modifier = Modifier.width(12.dp))
                                    }

                                    // Display ONLY app label, NO package name shown
                                    Text(
                                        text = app.label,
                                        color = Color.White,
                                        fontWeight = FontWeight.SemiBold,
                                        fontSize = 15.sp,
                                        maxLines = 1,
                                        overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                                    )
                                }

                                Spacer(modifier = Modifier.width(10.dp))

                                // Select Button exactly matching screenshot!
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(20.dp))
                                        .background(
                                            Brush.horizontalGradient(
                                                listOf(Color(0xFF4F46E5), Color(0xFF7C3AED), Color(0xFF9333EA))
                                            )
                                        )
                                        .clickable { onAppSelected(app) }
                                        .padding(horizontal = 18.dp, vertical = 6.dp)
                                ) {
                                    Text(
                                        text = "Select",
                                        color = Color.White,
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Privacy Banner at bottom of modal matching screenshot
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(18.dp))
                    .background(Color(0xFF121528))
                    .border(1.dp, Color(0xFF222644), RoundedCornerShape(18.dp))
                    .padding(12.dp)
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
                                .size(32.dp)
                                .clip(CircleShape)
                                .background(Color(0xFF2A1C52)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Lock,
                                contentDescription = null,
                                tint = Color(0xFFC084FC),
                                modifier = Modifier.size(16.dp)
                            )
                        }

                        Spacer(modifier = Modifier.width(10.dp))

                        Column {
                            Text(
                                text = "Your privacy is important.",
                                color = Color.White,
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 12.sp
                            )
                            Text(
                                text = "Only you can change this setting.",
                                color = Color(0xFF94A3B8),
                                fontSize = 11.sp
                            )
                        }
                    }

                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(
                                Brush.radialGradient(
                                    listOf(Color(0xFF7C3AED), Color(0xFF2E1065))
                                )
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Lock,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }
        }
    }
}

/**
 * VEDRA AI Knowledge Drive & Database Modal
 * Allows creating, editing, deleting, and uploading documents across organized folders.
 * All document content is auto-saved into SQLite DB and fed into VEDRA AI context!
 */
@Composable
fun VedraDriveModal(
    visible: Boolean,
    dbService: com.example.services.DatabaseService,
    onDismissRequest: () -> Unit
) {
    if (!visible) return

    var folders by androidx.compose.runtime.remember { androidx.compose.runtime.mutableStateOf(dbService.getAllDriveFolders()) }
    var selectedFolder by androidx.compose.runtime.remember { androidx.compose.runtime.mutableStateOf<com.example.services.DriveFolder?>(null) }
    var documents by androidx.compose.runtime.remember { androidx.compose.runtime.mutableStateOf<List<com.example.services.DriveDocument>>(emptyList()) }
    var searchQuery by androidx.compose.runtime.remember { androidx.compose.runtime.mutableStateOf("") }

    // Dialog state for folder creation
    var isCreateFolderOpen by androidx.compose.runtime.remember { androidx.compose.runtime.mutableStateOf(false) }
    var newFolderName by androidx.compose.runtime.remember { androidx.compose.runtime.mutableStateOf("") }

    // Dialog state for document creation
    var isCreateDocOpen by androidx.compose.runtime.remember { androidx.compose.runtime.mutableStateOf(false) }
    var newDocTitle by androidx.compose.runtime.remember { androidx.compose.runtime.mutableStateOf("") }
    var newDocContent by androidx.compose.runtime.remember { androidx.compose.runtime.mutableStateOf("") }
    var newDocType by androidx.compose.runtime.remember { androidx.compose.runtime.mutableStateOf("TXT") }

    // Viewing document state
    var selectedDocToView by androidx.compose.runtime.remember { androidx.compose.runtime.mutableStateOf<com.example.services.DriveDocument?>(null) }

    // Editing Folder state
    var editingFolder by androidx.compose.runtime.remember { androidx.compose.runtime.mutableStateOf<com.example.services.DriveFolder?>(null) }
    var editingFolderName by androidx.compose.runtime.remember { androidx.compose.runtime.mutableStateOf("") }

    val refreshAll = {
        folders = dbService.getAllDriveFolders()
        if (selectedFolder != null) {
            documents = dbService.getDocumentsInFolder(selectedFolder!!.id)
        } else {
            documents = emptyList()
        }
    }

    androidx.compose.runtime.LaunchedEffect(visible, selectedFolder) {
        refreshAll()
    }

    CustomModal(
        visible = visible,
        title = if (selectedFolder == null) "VEDRA AI Knowledge Drive" else "📁 ${selectedFolder!!.name}",
        subtitle = if (selectedFolder == null) "Organized document database connected to VEDRA AI." else "Documents stored in folder auto-saved for AI responses.",
        onDismissRequest = onDismissRequest
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
            // TOP BAR: Navigation Breadcrumb + Action Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (selectedFolder != null) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color(0xFF1F1A3A))
                            .clickable { selectedFolder = null }
                            .padding(horizontal = 10.dp, vertical = 6.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.ArrowBack,
                                contentDescription = "Back",
                                tint = Color(0xFFC084FC),
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "All Folders",
                                color = Color(0xFFC084FC),
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                } else {
                    Text(
                        text = "📁 ${folders.size} Folders Connected",
                        color = Color(0xFF94A3B8),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                // Add Folder / Add Doc button
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(18.dp))
                        .background(
                            Brush.horizontalGradient(
                                listOf(Color(0xFF6366F1), Color(0xFF8B5CF6))
                            )
                        )
                        .clickable {
                            if (selectedFolder == null) {
                                newFolderName = ""
                                isCreateFolderOpen = true
                            } else {
                                newDocTitle = ""
                                newDocContent = ""
                                newDocType = "TXT"
                                isCreateDocOpen = true
                            }
                        }
                        .padding(horizontal = 14.dp, vertical = 6.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = if (selectedFolder == null) Icons.Default.CreateNewFolder else Icons.Default.Add,
                            contentDescription = "New",
                            tint = Color.White,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = if (selectedFolder == null) "+ New Folder" else "+ Upload Doc",
                            color = Color.White,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            // Search Bar for Drive Documents & Folders
            CustomInput(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = if (selectedFolder == null) "Search folders & documents..." else "Search docs in ${selectedFolder!!.name}...",
                leadingIcon = Icons.Default.Search,
                trailingIcon = if (searchQuery.isNotBlank()) {
                    {
                        IconButton(onClick = { searchQuery = "" }, modifier = Modifier.size(24.dp)) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Clear",
                                tint = Color(0xFF9CA3AF),
                                modifier = Modifier.size(14.dp)
                            )
                        }
                    }
                } else null
            )

            // MAIN CONTENT BOX
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(300.dp)
            ) {
                if (selectedFolder == null) {
                    // ALL FOLDERS VIEW
                    val filteredFolders = folders.filter {
                        searchQuery.isBlank() || it.name.contains(searchQuery, ignoreCase = true)
                    }

                    if (filteredFolders.isEmpty()) {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Text(
                                text = "No folders found. Tap '+ New Folder' to create one!",
                                color = Color(0xFF94A3B8),
                                fontSize = 13.sp
                            )
                        }
                    } else {
                        LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            items(filteredFolders) { folder ->
                                val docCount = dbService.getDocumentsInFolder(folder.id).size
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(16.dp))
                                        .background(Color(0xFF13162A))
                                        .border(1.dp, Color(0xFF222644), RoundedCornerShape(16.dp))
                                        .clickable { selectedFolder = folder }
                                        .padding(horizontal = 14.dp, vertical = 12.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(38.dp)
                                                .clip(RoundedCornerShape(10.dp))
                                                .background(Color(0xFF231C4A)),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Folder,
                                                contentDescription = null,
                                                tint = Color(0xFFC084FC),
                                                modifier = Modifier.size(20.dp)
                                            )
                                        }

                                        Spacer(modifier = Modifier.width(12.dp))

                                        Column {
                                            Text(
                                                text = folder.name,
                                                color = Color.White,
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 15.sp
                                            )
                                            Text(
                                                text = "$docCount documents stored • Auto-saved DB",
                                                color = Color(0xFF94A3B8),
                                                fontSize = 11.5.sp
                                            )
                                        }
                                    }

                                    // Action Buttons: Edit / Delete Folder
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        IconButton(
                                            onClick = {
                                                editingFolder = folder
                                                editingFolderName = folder.name
                                            },
                                            modifier = Modifier.size(32.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Edit,
                                                contentDescription = "Rename",
                                                tint = Color(0xFF9CA3AF),
                                                modifier = Modifier.size(16.dp)
                                            )
                                        }

                                        IconButton(
                                            onClick = {
                                                dbService.deleteDriveFolder(folder.id)
                                                refreshAll()
                                            },
                                            modifier = Modifier.size(32.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Delete,
                                                contentDescription = "Delete",
                                                tint = Color(0xFFEF4444),
                                                modifier = Modifier.size(16.dp)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                } else {
                    // DOCUMENTS INSIDE FOLDER VIEW
                    val filteredDocs = documents.filter {
                        searchQuery.isBlank() || it.title.contains(searchQuery, ignoreCase = true) || it.content.contains(searchQuery, ignoreCase = true)
                    }

                    if (filteredDocs.isEmpty()) {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Text(
                                text = "No documents in this folder. Tap '+ Upload Doc' to add!",
                                color = Color(0xFF94A3B8),
                                fontSize = 13.sp
                            )
                        }
                    } else {
                        LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            items(filteredDocs) { doc ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(16.dp))
                                        .background(Color(0xFF13162A))
                                        .border(1.dp, Color(0xFF222644), RoundedCornerShape(16.dp))
                                        .clickable { selectedDocToView = doc }
                                        .padding(horizontal = 14.dp, vertical = 10.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(36.dp)
                                                .clip(RoundedCornerShape(8.dp))
                                                .background(Color(0xFF1E1B38)),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text(
                                                text = doc.fileType,
                                                color = Color(0xFFC084FC),
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 10.sp
                                            )
                                        }

                                        Spacer(modifier = Modifier.width(10.dp))

                                        Column {
                                            Text(
                                                text = doc.title,
                                                color = Color.White,
                                                fontWeight = FontWeight.SemiBold,
                                                fontSize = 14.sp,
                                                maxLines = 1,
                                                overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                                            )
                                            Text(
                                                text = doc.content,
                                                color = Color(0xFF94A3B8),
                                                fontSize = 11.sp,
                                                maxLines = 1,
                                                overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                                            )
                                        }
                                    }

                                    IconButton(
                                        onClick = {
                                            dbService.deleteDriveDocument(doc.id)
                                            refreshAll()
                                        },
                                        modifier = Modifier.size(32.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Delete,
                                            contentDescription = "Delete Doc",
                                            tint = Color(0xFFEF4444),
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // BOTTOM AI INTEGRATION BANNER
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color(0xFF121528))
                    .border(1.dp, Color(0xFF222644), RoundedCornerShape(16.dp))
                    .padding(10.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "🤖 AI Knowledge Connected",
                            color = Color(0xFFC084FC),
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.5.sp
                        )
                    }
                    Text(
                        text = "Auto-saved in SQLite DB",
                        color = Color(0xFF94A3B8),
                        fontSize = 11.sp
                    )
                }
            }
        }
    }

    // MODAL FOR CREATING FOLDER
    if (isCreateFolderOpen) {
        CustomModal(
            visible = true,
            title = "New Drive Folder",
            onDismissRequest = { isCreateFolderOpen = false }
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                CustomInput(
                    value = newFolderName,
                    onValueChange = { newFolderName = it },
                    placeholder = "Folder name (e.g. Science Docs, Notes...)"
                )
                CustomButton(
                    text = "Create Folder 📁",
                    onClick = {
                        if (newFolderName.isNotBlank()) {
                            dbService.createDriveFolder(newFolderName.trim())
                            isCreateFolderOpen = false
                            refreshAll()
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }

    // MODAL FOR EDITING FOLDER
    if (editingFolder != null) {
        CustomModal(
            visible = true,
            title = "Rename Folder",
            onDismissRequest = { editingFolder = null }
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                CustomInput(
                    value = editingFolderName,
                    onValueChange = { editingFolderName = it },
                    placeholder = "New folder name..."
                )
                CustomButton(
                    text = "Save Rename ✏️",
                    onClick = {
                        if (editingFolderName.isNotBlank()) {
                            dbService.updateDriveFolder(editingFolder!!.id, editingFolderName.trim())
                            editingFolder = null
                            refreshAll()
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }

    // MODAL FOR UPLOADING / CREATING DOCUMENT
    if (isCreateDocOpen && selectedFolder != null) {
        CustomModal(
            visible = true,
            title = "Upload / Add Document",
            subtitle = "Add notes, research papers, or syllabus into '${selectedFolder!!.name}'",
            onDismissRequest = { isCreateDocOpen = false }
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                CustomInput(
                    value = newDocTitle,
                    onValueChange = { newDocTitle = it },
                    placeholder = "Document Title (e.g., Biology Chapter 4 Notes)"
                )

                // Select File Type
                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    listOf("TXT", "PDF", "MD", "DOC", "CODE").forEach { type ->
                        val isSel = newDocType == type
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .background(if (isSel) Color(0xFF7C3AED) else Color(0xFF1B1830))
                                .clickable { newDocType = type }
                                .padding(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            Text(
                                text = type,
                                color = if (isSel) Color.White else Color(0xFF94A3B8),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                CustomInput(
                    value = newDocContent,
                    onValueChange = { newDocContent = it },
                    placeholder = "Paste or type document content here...",
                    modifier = Modifier.height(100.dp)
                )

                CustomButton(
                    text = "Save & Connect to VEDRA AI 🚀",
                    onClick = {
                        if (newDocTitle.isNotBlank() && newDocContent.isNotBlank()) {
                            dbService.createDriveDocument(
                                folderId = selectedFolder!!.id,
                                title = newDocTitle.trim(),
                                content = newDocContent.trim(),
                                fileType = newDocType
                            )
                            isCreateDocOpen = false
                            refreshAll()
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }

    // MODAL FOR VIEWING DOCUMENT CONTENT
    if (selectedDocToView != null) {
        CustomModal(
            visible = true,
            title = "📄 ${selectedDocToView!!.title}",
            subtitle = "Type: ${selectedDocToView!!.fileType} • Stored in Database for AI Context",
            onDismissRequest = { selectedDocToView = null }
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(0xFF110E20))
                        .border(1.dp, Color(0xFF282348), RoundedCornerShape(12.dp))
                        .padding(12.dp)
                ) {
                    LazyColumn {
                        item {
                            Text(
                                text = selectedDocToView!!.content,
                                color = Color.White,
                                fontSize = 13.sp,
                                lineHeight = 18.sp
                            )
                        }
                    }
                }

                CustomButton(
                    text = "Close Document",
                    onClick = { selectedDocToView = null },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}
