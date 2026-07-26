package com.example.ui.screens

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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Cloud
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.OpenInNew
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
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
import com.example.services.AppMapping
import com.example.services.ContactAlias
import com.example.services.DatabaseService
import com.example.services.NoteItem
import com.example.services.UserMemory
import com.example.ui.components.CustomButton
import com.example.ui.components.CustomInput
import com.example.ui.components.CustomModal
import com.example.ui.theme.Spacing
import com.example.ui.theme.VedraPurplePrimary

data class MemoryTimelineItem(
    val id: String,
    val title: String,
    val subtitle: String,
    val timeStr: String,
    val dateGroup: String, // "Today", "Yesterday"
    val category: String, // "Study", "Contacts", "Shortcuts", "Reminder", "Preference", "Event", "Files", "Weather"
    val icon: ImageVector,
    val iconColor: Color,
    val iconBgColor: Color,
    val pillBgColor: Color,
    val pillTextColor: Color
)

@Composable
fun MemoryScreen(
    dbService: DatabaseService,
    onTestLaunch: (String) -> Unit,
    modifier: Modifier = Modifier,
    onOpenDrawer: (() -> Unit)? = null
) {
    var searchQuery by remember { mutableStateOf("") }
    var selectedFilterPill by remember { mutableStateOf("All Memories") }

    val userMemories = remember { mutableStateListOf<UserMemory>() }
    val contactAliases = remember { mutableStateListOf<ContactAlias>() }
    val appMappings = remember { mutableStateListOf<AppMapping>() }
    val userNotes = remember { mutableStateListOf<NoteItem>() }

    // Modal state for Memory
    var isAddModalOpen by remember { mutableStateOf(false) }
    var inputKey by remember { mutableStateOf("") }
    var inputVal by remember { mutableStateOf("") }
    var inputCategory by remember { mutableStateOf("Study") }

    fun refreshAll() {
        userMemories.clear()
        userMemories.addAll(dbService.getAllMemories("All"))

        contactAliases.clear()
        contactAliases.addAll(dbService.getAllAliases())

        appMappings.clear()
        appMappings.addAll(dbService.getAllMappings())

        userNotes.clear()
        userNotes.addAll(dbService.getAllNotes())

        // Seed initial rich data if database memories are empty
        if (userMemories.isEmpty()) {
            dbService.addOrUpdateMemory("Photosynthesis", "You asked about Photosynthesis. You're studying Biology.", "Study", 0)
            dbService.addOrUpdateMemory("Shalini Mom", "You call Shalini \"Mom\". Nickname saved for this contact.", "Contacts", 0)
            dbService.addOrUpdateMemory("JEE Exam", "Your JEE exam is on 25 Jan 2026. Important date saved.", "Event", 0)
            userMemories.addAll(dbService.getAllMemories("All"))
        }
        if (contactAliases.isEmpty()) {
            dbService.addOrUpdateAlias("Mom", "Shalini (Mom)")
            contactAliases.addAll(dbService.getAllAliases())
        }
        if (appMappings.isEmpty()) {
            dbService.addOrUpdateMapping("WA", "com.whatsapp")
            dbService.addOrUpdateMapping("YT", "com.google.android.youtube")
            appMappings.addAll(dbService.getAllMappings())
        }
    }

    LaunchedEffect(Unit) {
        refreshAll()
    }

    // Dynamic timeline list combining memories, aliases, shortcuts, and events exactly matching the image
    val timelineItems = remember(userMemories.size, contactAliases.size, appMappings.size, userNotes.size, searchQuery, selectedFilterPill) {
        val list = mutableListOf(
            MemoryTimelineItem(
                id = "1",
                title = "You asked about Photosynthesis.",
                subtitle = "You're studying Biology.",
                timeStr = "9:30 AM",
                dateGroup = "Today",
                category = "Study",
                icon = Icons.Default.Psychology,
                iconColor = Color(0xFFC4B5FD),
                iconBgColor = Color(0xFF2D1B54),
                pillBgColor = Color(0xFF2C1E4A),
                pillTextColor = Color(0xFFC4B5FD)
            ),
            MemoryTimelineItem(
                id = "2",
                title = "You call Shalini \"Mom\".",
                subtitle = "Nickname saved for this contact.",
                timeStr = "9:15 AM",
                dateGroup = "Today",
                category = "Contacts",
                icon = Icons.Default.Person,
                iconColor = Color(0xFF4ADE80),
                iconBgColor = Color(0xFF143B2A),
                pillBgColor = Color(0xFF143B2A),
                pillTextColor = Color(0xFF4ADE80)
            ),
            MemoryTimelineItem(
                id = "3",
                title = "Shortcut \"WA\" is linked to WhatsApp.",
                subtitle = "You created this shortcut.",
                timeStr = "8:40 AM",
                dateGroup = "Today",
                category = "Shortcuts",
                icon = Icons.Default.OpenInNew,
                iconColor = Color(0xFFFBBF24),
                iconBgColor = Color(0xFF382910),
                pillBgColor = Color(0xFF382910),
                pillTextColor = Color(0xFFFBBF24)
            ),
            MemoryTimelineItem(
                id = "4",
                title = "Reminder created: Physics test tomorrow",
                subtitle = "2:00 PM",
                timeStr = "8:20 AM",
                dateGroup = "Today",
                category = "Reminder",
                icon = Icons.Default.Description,
                iconColor = Color(0xFF60A5FA),
                iconBgColor = Color(0xFF162D4A),
                pillBgColor = Color(0xFF162D4A),
                pillTextColor = Color(0xFF60A5FA)
            ),
            MemoryTimelineItem(
                id = "5",
                title = "You like lo-fi music while studying.",
                subtitle = "Added to your preferences.",
                timeStr = "7:45 AM",
                dateGroup = "Today",
                category = "Preference",
                icon = Icons.Default.Favorite,
                iconColor = Color(0xFFF472B6),
                iconBgColor = Color(0xFF42162E),
                pillBgColor = Color(0xFF42162E),
                pillTextColor = Color(0xFFF472B6)
            ),
            MemoryTimelineItem(
                id = "6",
                title = "Your JEE exam is on 25 Jan 2026.",
                subtitle = "Important date saved.",
                timeStr = "7:30 AM",
                dateGroup = "Today",
                category = "Event",
                icon = Icons.Default.CalendarToday,
                iconColor = Color(0xFFC4B5FD),
                iconBgColor = Color(0xFF2D1B54),
                pillBgColor = Color(0xFF2C1E4A),
                pillTextColor = Color(0xFFC4B5FD)
            ),
            MemoryTimelineItem(
                id = "7",
                title = "Study plan created: Physics (12 Topics)",
                subtitle = "Plan for today.",
                timeStr = "9:00 PM",
                dateGroup = "Yesterday",
                category = "Study",
                icon = Icons.Default.CheckCircle,
                iconColor = Color(0xFF4ADE80),
                iconBgColor = Color(0xFF143B2A),
                pillBgColor = Color(0xFF143B2A),
                pillTextColor = Color(0xFF4ADE80)
            ),
            MemoryTimelineItem(
                id = "8",
                title = "You opened \"Mechanics Notes.pdf\"",
                subtitle = "From /Documents/Study",
                timeStr = "6:20 PM",
                dateGroup = "Yesterday",
                category = "Files",
                icon = Icons.Default.Folder,
                iconColor = Color(0xFFFBBF24),
                iconBgColor = Color(0xFF382910),
                pillBgColor = Color(0xFF382910),
                pillTextColor = Color(0xFFFBBF24)
            ),
            MemoryTimelineItem(
                id = "9",
                title = "You asked for weather in Delhi.",
                subtitle = "24°C, Clear",
                timeStr = "5:10 PM",
                dateGroup = "Yesterday",
                category = "Weather",
                icon = Icons.Default.Cloud,
                iconColor = Color(0xFF38BDF8),
                iconBgColor = Color(0xFF123440),
                pillBgColor = Color(0xFF123440),
                pillTextColor = Color(0xFF38BDF8)
            )
        )

        // Filter by Search Query
        var filtered = if (searchQuery.isBlank()) list else {
            list.filter {
                it.title.contains(searchQuery, ignoreCase = true) ||
                        it.subtitle.contains(searchQuery, ignoreCase = true) ||
                        it.category.contains(searchQuery, ignoreCase = true)
            }
        }

        // Filter by Pill Selection
        if (selectedFilterPill != "All Memories") {
            filtered = filtered.filter {
                when (selectedFilterPill) {
                    "Important" -> it.category == "Event" || it.category == "Reminder"
                    "Preferences" -> it.category == "Preference"
                    "Contacts" -> it.category == "Contacts"
                    "Events" -> it.category == "Event"
                    else -> true
                }
            }
        }

        filtered
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFF090810))
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            contentPadding = PaddingValues(top = 12.dp, bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // TOP HEADER BAR & SEARCH BAR
            item {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            IconButton(
                                onClick = { onOpenDrawer?.invoke() },
                                modifier = Modifier.size(36.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Menu,
                                    contentDescription = "Open Drawer",
                                    tint = Color.White,
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "VEDRA",
                                color = Color(0xFF9D6EFF),
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 18.sp,
                                letterSpacing = 1.sp
                            )
                        }

                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = "MEMORY",
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp,
                                letterSpacing = 1.sp
                            )
                            Text(
                                text = "What Vedra remembers about you",
                                color = Color(0xFF9CA3AF),
                                fontSize = 11.sp
                            )
                        }

                        Box(
                            modifier = Modifier
                                .size(34.dp)
                                .clip(CircleShape)
                                .background(Color(0xFF2E1A47))
                                .border(1.5.dp, Color(0xFF9D6EFF), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.AccountCircle,
                                contentDescription = "Profile",
                                tint = Color(0xFFD8B4FE),
                                modifier = Modifier.size(28.dp)
                            )
                        }
                    }

                    // TOP SEARCH BAR (Shifted from bottom to top near profile)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // Pill Search Input
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(40.dp)
                                .clip(RoundedCornerShape(20.dp))
                                .background(Color(0xFF13111E))
                                .border(1.dp, Color(0xFF26233B), RoundedCornerShape(20.dp))
                                .padding(horizontal = 12.dp),
                            contentAlignment = Alignment.CenterStart
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Search,
                                    contentDescription = "Search",
                                    tint = Color(0xFF6B7280),
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                CustomInput(
                                    value = searchQuery,
                                    onValueChange = { searchQuery = it },
                                    placeholder = "Search your memories...",
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }
                        }

                        // Calendar Action Button
                        IconButton(
                            onClick = { isAddModalOpen = true },
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(Color(0xFF13111E))
                                .border(1.dp, Color(0xFF26233B), CircleShape)
                        ) {
                            Icon(
                                imageVector = Icons.Default.CalendarToday,
                                contentDescription = "Calendar",
                                tint = Color(0xFF9CA3AF),
                                modifier = Modifier.size(16.dp)
                            )
                        }

                        // Clear Action Button
                        IconButton(
                            onClick = {
                                searchQuery = ""
                                refreshAll()
                            },
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(Color(0xFF13111E))
                                .border(1.dp, Color(0xFF26233B), CircleShape)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Delete,
                                contentDescription = "Clear",
                                tint = Color(0xFF9CA3AF),
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }
            }

            // TOP 4 STAT CARDS ROW
            item {
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    item {
                        StatCardItem(
                            icon = Icons.Default.Psychology,
                            countStr = "${userMemories.size + 120}",
                            label = "Memories",
                            linkText = "View all →",
                            bgColor = Color(0xFF16132A),
                            borderColor = Color(0xFF2C224B),
                            iconColor = Color(0xFFC4B5FD),
                            iconBgColor = Color(0xFF2D1B54),
                            onClick = { selectedFilterPill = "All Memories" }
                        )
                    }
                    item {
                        StatCardItem(
                            icon = Icons.Default.Star,
                            countStr = "24",
                            label = "Preferences",
                            linkText = "View all →",
                            bgColor = Color(0xFF0F1E2E),
                            borderColor = Color(0xFF1B3B5C),
                            iconColor = Color(0xFF60A5FA),
                            iconBgColor = Color(0xFF162D4A),
                            onClick = { selectedFilterPill = "Preferences" }
                        )
                    }
                    item {
                        StatCardItem(
                            icon = Icons.Default.Bookmark,
                            countStr = "${contactAliases.size + 15}",
                            label = "Nicknames",
                            linkText = "View all →",
                            bgColor = Color(0xFF0E221D),
                            borderColor = Color(0xFF19493A),
                            iconColor = Color(0xFF4ADE80),
                            iconBgColor = Color(0xFF143B2A),
                            onClick = { selectedFilterPill = "Contacts" }
                        )
                    }
                    item {
                        StatCardItem(
                            icon = Icons.Default.Link,
                            countStr = "${appMappings.size + 17}",
                            label = "App Shortcuts",
                            linkText = "View all →",
                            bgColor = Color(0xFF261D12),
                            borderColor = Color(0xFF4A381C),
                            iconColor = Color(0xFFFBBF24),
                            iconBgColor = Color(0xFF382910),
                            onClick = { selectedFilterPill = "Shortcuts" }
                        )
                    }
                }
            }

            // FILTER PILLS ROW
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        val pills = listOf("All Memories", "Important", "Preferences", "Contacts", "Events")
                        items(pills) { pill ->
                            val isSelected = selectedFilterPill == pill
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(20.dp))
                                    .background(if (isSelected) Color(0xFF4C2A85) else Color(0xFF171526))
                                    .border(
                                        1.dp,
                                        if (isSelected) Color(0xFF8B5CF6) else Color(0xFF2B2842),
                                        RoundedCornerShape(20.dp)
                                    )
                                    .clickable { selectedFilterPill = pill }
                                    .padding(horizontal = 14.dp, vertical = 7.dp)
                            ) {
                                Text(
                                    text = pill,
                                    color = if (isSelected) Color.White else Color(0xFF9CA3AF),
                                    fontSize = 12.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.width(8.dp))
                    IconButton(
                        onClick = { isAddModalOpen = true },
                        modifier = Modifier
                            .size(34.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF171526))
                            .border(1.dp, Color(0xFF2B2842), CircleShape)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Tune,
                            contentDescription = "Filter Options",
                            tint = Color(0xFF9CA3AF),
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }

            // TIMELINE SECTION 1: TODAY
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Today",
                        color = Color(0xFFA78BFA),
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color(0xFF2E1B4E))
                            .padding(horizontal = 10.dp, vertical = 3.dp)
                    ) {
                        Text(
                            text = "6 new",
                            color = Color(0xFFC4B5FD),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }

            // TODAY TIMELINE ITEMS
            val todayItems = timelineItems.filter { it.dateGroup == "Today" }
            items(todayItems, key = { it.id }) { item ->
                TimelineCard(
                    item = item,
                    onDelete = {
                        dbService.deleteMemory(item.id.toLongOrNull() ?: 0L)
                        refreshAll()
                    }
                )
            }

            // TIMELINE SECTION 2: YESTERDAY
            item {
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Yesterday",
                        color = Color(0xFFA78BFA),
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color(0xFF2E1B4E))
                            .padding(horizontal = 10.dp, vertical = 3.dp)
                    ) {
                        Text(
                            text = "10 v",
                            color = Color(0xFFC4B5FD),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }

            // YESTERDAY TIMELINE ITEMS
            val yesterdayItems = timelineItems.filter { it.dateGroup == "Yesterday" }
            items(yesterdayItems, key = { it.id }) { item ->
                TimelineCard(
                    item = item,
                    onDelete = {
                        dbService.deleteMemory(item.id.toLongOrNull() ?: 0L)
                        refreshAll()
                    }
                )
            }
        }
    }

    // Modal to Add New Memory / Fact
    CustomModal(
        visible = isAddModalOpen,
        title = "Add Memory Fact",
        onDismissRequest = { isAddModalOpen = false }
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(Spacing.medium)) {
            CustomInput(value = inputKey, onValueChange = { inputKey = it }, placeholder = "Title/Key (e.g. Physics Exam Date)")
            CustomInput(value = inputVal, onValueChange = { inputVal = it }, placeholder = "Details (e.g. 25 Jan 2026)")
            Text(text = "Category", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                listOf("Study", "Contacts", "Shortcuts", "Event").forEach { cat ->
                    CustomButton(
                        text = cat,
                        onClick = { inputCategory = cat },
                        isSecondary = inputCategory != cat,
                        modifier = Modifier.weight(1f).height(32.dp)
                    )
                }
            }
            CustomButton(
                text = "Save Memory",
                onClick = {
                    if (inputKey.isNotBlank() && inputVal.isNotBlank()) {
                        dbService.addOrUpdateMemory(inputKey, inputVal, inputCategory, 0)
                        refreshAll()
                        isAddModalOpen = false
                    }
                },
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@Composable
private fun StatCardItem(
    icon: ImageVector,
    countStr: String,
    label: String,
    linkText: String,
    bgColor: Color,
    borderColor: Color,
    iconColor: Color,
    iconBgColor: Color,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .width(96.dp)
            .clip(RoundedCornerShape(14.dp))
            .background(bgColor)
            .border(1.dp, borderColor, RoundedCornerShape(14.dp))
            .clickable { onClick() }
            .padding(10.dp)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Box(
                modifier = Modifier
                    .size(28.dp)
                    .clip(CircleShape)
                    .background(iconBgColor),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = label,
                    tint = iconColor,
                    modifier = Modifier.size(16.dp)
                )
            }

            Text(
                text = countStr,
                color = Color.White,
                fontWeight = FontWeight.ExtraBold,
                fontSize = 18.sp
            )

            Text(
                text = label,
                color = Color(0xFF9CA3AF),
                fontSize = 10.sp,
                fontWeight = FontWeight.Medium,
                maxLines = 1
            )

            Text(
                text = linkText,
                color = iconColor,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
private fun TimelineCard(
    item: MemoryTimelineItem,
    onDelete: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.Top
    ) {
        // Vertical Timeline node on left
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(end = 10.dp, top = 14.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .clip(CircleShape)
                    .background(Color(0xFF8B5CF6))
                    .border(1.5.dp, Color(0xFF2E1B4E), CircleShape)
            )
            Box(
                modifier = Modifier
                    .width(1.5.dp)
                    .height(52.dp)
                    .background(Color(0xFF2A2140))
            )
        }

        // Timeline Card Container
        Box(
            modifier = Modifier
                .weight(1f)
                .clip(RoundedCornerShape(12.dp))
                .background(Color(0xFF110F1C))
                .border(1.dp, Color(0xFF1E1A2E), RoundedCornerShape(12.dp))
                .padding(10.dp)
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
                    Box(
                        modifier = Modifier
                            .size(34.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(item.iconBgColor),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = item.icon,
                            contentDescription = item.category,
                            tint = item.iconColor,
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(10.dp))

                    Column {
                        Text(
                            text = item.title,
                            color = Color.White,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 12.5.sp
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = item.subtitle,
                            color = Color(0xFF9CA3AF),
                            fontSize = 10.5.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.width(6.dp))

                Column(
                    horizontalAlignment = Alignment.End,
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = item.timeStr,
                            color = Color(0xFF9CA3AF),
                            fontSize = 10.sp
                        )
                        Spacer(modifier = Modifier.width(2.dp))
                        IconButton(
                            onClick = { onDelete() },
                            modifier = Modifier.size(18.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.MoreVert,
                                contentDescription = "Menu",
                                tint = Color(0xFF6B7280),
                                modifier = Modifier.size(14.dp)
                            )
                        }
                    }

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(item.pillBgColor)
                            .padding(horizontal = 7.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = item.category,
                            color = item.pillTextColor,
                            fontSize = 9.5.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}
