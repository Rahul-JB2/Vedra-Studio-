package com.example.ui.screens

import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Biotech
import androidx.compose.material.icons.filled.Book
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.ChatBubble
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.DeveloperMode
import androidx.compose.material.icons.filled.Diamond
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.EventNote
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Functions
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.Quiz
import androidx.compose.material.icons.filled.RadioButtonUnchecked
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.TextFields
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
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.services.DatabaseService
import com.example.services.GeminiService
import com.example.services.StudyTask
import kotlinx.coroutines.launch

// VEHub Sub Navigation Screens
enum class VeHubSubView {
    DASHBOARD,
    SUBJECTS,
    AI_TOOLS,
    PLANNER,
    PROGRESS
}

// Subject Data Item
data class HubSubject(
    val title: String,
    val icon: ImageVector,
    val color: Color,
    val completedChapters: Int,
    val totalChapters: Int,
    val progressPercent: Int
)

// AI Study Tool Data Item
data class HubAiTool(
    val title: String,
    val description: String,
    val icon: ImageVector,
    val iconBgColor: Color,
    val tag: String
)

// Study Resource Item
data class HubResource(
    val title: String,
    val countText: String,
    val icon: ImageVector,
    val accentColor: Color
)

// Recent Chat / Activity Item
data class HubActivityItem(
    val title: String,
    val timestamp: String,
    val icon: ImageVector,
    val category: String
)

@Composable
fun StudyHubScreen(
    dbService: DatabaseService,
    onOpenDrawer: () -> Unit = {},
    onNavigateTab: (Int) -> Unit = {},
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    // Sub Navigation View State (Defaults to Dashboard)
    var currentSubView by remember { mutableStateOf(VeHubSubView.DASHBOARD) }

    // Search query for Subjects or AI Tools
    var searchQuery by remember { mutableStateOf("") }
    var isSearchActive by remember { mutableStateOf(false) }

    // Bottom Input Query ("Ask VED")
    var vedInputQuery by remember { mutableStateOf("") }

    // Interactive AI Dialog Modal State
    var showAiToolModal by remember { mutableStateOf(false) }
    var activeAiToolTitle by remember { mutableStateOf("Ask Doubt") }
    var aiToolQuestionInput by remember { mutableStateOf("") }
    var aiToolResponseText by remember { mutableStateOf<String?>(null) }
    var isAiGenerating by remember { mutableStateOf(false) }

    // Today's Planner Checklist State
    val plannerTasks = remember {
        mutableStateListOf(
            StudyTask(1, "Physics Revision", "Physics", false, "9:00 AM - 11:00 AM"),
            StudyTask(2, "Chemistry Questions", "Chemistry", false, "1:00 PM - 3:00 PM"),
            StudyTask(3, "Maths Mock Test", "Mathematics", false, "6:00 PM - 8:00 PM")
        )
    }

    // Load tasks from real database if present
    LaunchedEffect(Unit) {
        val dbTasks = dbService.getAllStudyTasks()
        if (dbTasks.isNotEmpty()) {
            plannerTasks.clear()
            plannerTasks.addAll(dbTasks.take(5))
        }
    }

    // Static Data Arrays matching the user's reference image
    val subjectsList = remember {
        listOf(
            HubSubject("Physics", Icons.Default.School, Color(0xFF3B82F6), 12, 24, 50),
            HubSubject("Chemistry", Icons.Default.Biotech, Color(0xFFA855F7), 10, 20, 50),
            HubSubject("Mathematics", Icons.Default.Calculate, Color(0xFFF97316), 14, 28, 50),
            HubSubject("Biology", Icons.Default.Psychology, Color(0xFF10B981), 8, 18, 44),
            HubSubject("English", Icons.Default.TextFields, Color(0xFF6366F1), 6, 15, 40),
            HubSubject("Computer Science", Icons.Default.DeveloperMode, Color(0xFFEC4899), 5, 12, 41)
        )
    }

    val aiToolsList = remember {
        listOf(
            HubAiTool("Ask Doubt", "Get instant answers", Icons.Default.ChatBubble, Color(0xFF8B5CF6), "doubt"),
            HubAiTool("Explain Topic", "Deep explanation", Icons.Default.Psychology, Color(0xFF3B82F6), "explain"),
            HubAiTool("Summarize PDF", "Summarize any PDF", Icons.Default.Description, Color(0xFF06B6D4), "pdf"),
            HubAiTool("Generate Notes", "Create smart notes", Icons.Default.Edit, Color(0xFF10B981), "notes"),
            HubAiTool("Quiz Me", "Test your knowledge", Icons.Default.Quiz, Color(0xFFF97316), "quiz"),
            HubAiTool("Flashcards", "Smart flashcards", Icons.Default.AutoAwesome, Color(0xFFEC4899), "flashcards"),
            HubAiTool("Mind Maps", "Visualize concepts", Icons.Default.Share, Color(0xFF8B5CF6), "mindmaps"),
            HubAiTool("Formula Finder", "Find any formula", Icons.Default.Functions, Color(0xFF3B82F6), "formulas")
        )
    }

    val studyResourcesList = remember {
        listOf(
            HubResource("Books", "128 Items", Icons.Default.Book, Color(0xFF3B82F6)),
            HubResource("Notes", "342 Items", Icons.Default.Edit, Color(0xFF10B981)),
            HubResource("PDFs", "215 Items", Icons.Default.Description, Color(0xFFEC4899)),
            HubResource("Formula Sheet", "84 Items", Icons.Default.Functions, Color(0xFF8B5CF6)),
            HubResource("PYQs", "620 Items", Icons.Default.Folder, Color(0xFFF97316)),
            HubResource("Mock Tests", "56 Tests", Icons.Default.EventNote, Color(0xFF06B6D4))
        )
    }

    val recentChatsList = remember {
        mutableStateListOf(
            HubActivityItem("Explain Gauss Law", "Today, 8:40 AM", Icons.Default.ChatBubble, "Physics"),
            HubActivityItem("What is photosynthesis?", "Today, 7:15 AM", Icons.Default.ChatBubble, "Biology"),
            HubActivityItem("Derivatives shortcuts", "Yesterday, 9:20 PM", Icons.Default.ChatBubble, "Maths")
        )
    }

    val recentActivityList = remember {
        listOf(
            HubActivityItem("Electrostatics Notes.pdf", "Today, 8:35 AM", Icons.Default.Description, "PDF"),
            HubActivityItem("Mock Test #12", "Yesterday, 6:20 PM", Icons.Default.Quiz, "Test"),
            HubActivityItem("Organic Chemistry Notes", "Yesterday, 3:10 PM", Icons.Default.Book, "Notes")
        )
    }

    // Function to run AI Tool Query
    val executeAiQuery = { toolName: String, prompt: String ->
        activeAiToolTitle = toolName
        aiToolQuestionInput = prompt
        showAiToolModal = true
        isAiGenerating = true
        aiToolResponseText = null

        coroutineScope.launch {
            val offlineMatch = dbService.searchOfflineContent(prompt) ?: dbService.searchVedmTKnowledge(prompt, context)
            val finalRes = if (!offlineMatch.isNullOrBlank()) {
                offlineMatch
            } else {
                try {
                    val systemPrompt = "You are VED, expert AI tutor for $toolName. Answer concisely and clearly for a student: $prompt"
                    GeminiService.generateResponse(systemPrompt)
                } catch (e: Exception) {
                    "VED AI response for '$prompt':\n• Key Concepts: Focus on foundational laws and step-by-step logic.\n• Practice Tip: Review recent formula sheets and attempt practice problems."
                }
            }
            aiToolResponseText = finalRes
            isAiGenerating = false
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFF0B0A1A)) // Dark Violet Canvas matching image
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // ================= 1. DYNAMIC TOP BAR =================
            TopBarHeader(
                currentSubView = currentSubView,
                onOpenDrawer = onOpenDrawer,
                onBackToDashboard = { currentSubView = VeHubSubView.DASHBOARD },
                searchQuery = searchQuery,
                onSearchQueryChange = { searchQuery = it },
                isSearchActive = isSearchActive,
                onToggleSearch = { isSearchActive = !isSearchActive }
            )

            // ================= 2. MAIN CONTENT AREA =================
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            ) {
                when (currentSubView) {
                    VeHubSubView.DASHBOARD -> {
                        DashboardMainContent(
                            plannerTasks = plannerTasks,
                            onToggleTask = { task ->
                                val index = plannerTasks.indexOfFirst { it.id == task.id }
                                if (index != -1) {
                                    val updated = task.copy(isCompleted = !task.isCompleted)
                                    plannerTasks[index] = updated
                                    dbService.toggleStudyTask(task.id, task.isCompleted)
                                }
                            },
                            onOpenSubjects = { currentSubView = VeHubSubView.SUBJECTS },
                            onOpenAiTools = { currentSubView = VeHubSubView.AI_TOOLS },
                            onOpenPlanner = { currentSubView = VeHubSubView.PLANNER },
                            onOpenProgress = { currentSubView = VeHubSubView.PROGRESS },
                            onOpenAskVed = {
                                executeAiQuery("Ask VED", "Give me a quick study plan for today!")
                            },
                            onSelectAiTool = { tool ->
                                executeAiQuery(tool.title, "Help me with ${tool.title}")
                            },
                            subjectsList = subjectsList,
                            aiToolsList = aiToolsList,
                            studyResourcesList = studyResourcesList,
                            recentActivityList = recentActivityList,
                            vedInputQuery = vedInputQuery,
                            onVedInputQueryChange = { vedInputQuery = it },
                            onSendVedInput = {
                                if (vedInputQuery.isNotBlank()) {
                                    val q = vedInputQuery
                                    vedInputQuery = ""
                                    executeAiQuery("Ask VED", q)
                                }
                            }
                        )
                    }

                    VeHubSubView.SUBJECTS -> {
                        SubjectsDetailContent(
                            subjects = subjectsList.filter {
                                searchQuery.isBlank() || it.title.contains(searchQuery, ignoreCase = true)
                            },
                            onSubjectClick = { sub ->
                                Toast.makeText(context, "Opening ${sub.title} chapters...", Toast.LENGTH_SHORT).show()
                            }
                        )
                    }

                    VeHubSubView.AI_TOOLS -> {
                        AiToolsDetailContent(
                            aiTools = aiToolsList.filter {
                                searchQuery.isBlank() || it.title.contains(searchQuery, ignoreCase = true) || it.description.contains(searchQuery, ignoreCase = true)
                            },
                            recentChats = recentChatsList,
                            onSelectTool = { tool ->
                                executeAiQuery(tool.title, "Explain ${tool.title} concept")
                            },
                            onSelectChat = { chat ->
                                executeAiQuery("Ask Doubt", chat.title)
                            }
                        )
                    }

                    VeHubSubView.PLANNER -> {
                        PlannerDetailContent(
                            plannerTasks = plannerTasks,
                            onToggleTask = { task ->
                                val index = plannerTasks.indexOfFirst { it.id == task.id }
                                if (index != -1) {
                                    val updated = task.copy(isCompleted = !task.isCompleted)
                                    plannerTasks[index] = updated
                                    dbService.toggleStudyTask(task.id, task.isCompleted)
                                }
                            },
                            onAddTask = { title, subject, time ->
                                dbService.addStudyTask(title, subject, time)
                                plannerTasks.add(StudyTask(System.currentTimeMillis() % 10000, title, subject, false, time))
                            }
                        )
                    }

                    VeHubSubView.PROGRESS -> {
                        ProgressDetailContent()
                    }
                }
            }

            // ================= 3. BOTTOM SUB-NAVIGATION BAR =================
            VeHubBottomNavBar(
                currentSubView = currentSubView,
                onSelectSubView = { subView ->
                    currentSubView = subView
                },
                onOpenVedAssistant = {
                    onNavigateTab(2) // Switch to central Ved Assistant tab
                }
            )
        }

        // ================= 4. INTERACTIVE AI TOOL / ASK DOUBT DIALOG =================
        if (showAiToolModal) {
            Dialog(onDismissRequest = { showAiToolModal = false }) {
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = Color(0xFF14132B),
                    border = BorderStroke(1.dp, Color(0xFF8B5CF6)),
                    modifier = Modifier.padding(12.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    ) {
                        // Title Header
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Psychology, null, tint = Color(0xFF8B5CF6), modifier = Modifier.size(20.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(activeAiToolTitle, color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                            }
                            IconButton(onClick = { showAiToolModal = false }, modifier = Modifier.size(28.dp)) {
                                Icon(Icons.Default.Close, null, tint = Color(0xFFA09EC0))
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // Question Prompt Box
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(10.dp))
                                .background(Color(0xFF0F0E22))
                                .padding(10.dp)
                        ) {
                            Text(
                                text = "Q: ${aiToolQuestionInput.ifBlank { "Ask a question..." }}",
                                color = Color.White,
                                fontSize = 12.5.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // AI Response Area
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(220.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(Color(0xFF0B0A1A))
                                .border(1.dp, Color(0xFF28264A), RoundedCornerShape(10.dp))
                                .padding(12.dp)
                        ) {
                            if (isAiGenerating) {
                                Column(
                                    modifier = Modifier.fillMaxSize(),
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.Center
                                ) {
                                    CircularProgressIndicator(color = Color(0xFF8B5CF6), modifier = Modifier.size(28.dp))
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text("VED AI is thinking...", color = Color(0xFFA09EC0), fontSize = 12.sp)
                                }
                            } else {
                                LazyColumn(modifier = Modifier.fillMaxSize()) {
                                    item {
                                        Text(
                                            text = aiToolResponseText ?: "No response generated.",
                                            color = Color.White,
                                            fontSize = 12.5.sp,
                                            lineHeight = 18.sp
                                        )
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // Bottom Input to Follow Up
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            var followUpText by remember { mutableStateOf("") }
                            OutlinedTextField(
                                value = followUpText,
                                onValueChange = { followUpText = it },
                                placeholder = { Text("Ask follow-up question...", color = Color(0xFF6B6893), fontSize = 12.sp) },
                                modifier = Modifier.weight(1f),
                                singleLine = true,
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = Color(0xFF8B5CF6),
                                    unfocusedBorderColor = Color(0xFF28264A),
                                    focusedTextColor = Color.White,
                                    unfocusedTextColor = Color.White
                                )
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            IconButton(
                                onClick = {
                                    if (followUpText.isNotBlank()) {
                                        val q = followUpText
                                        followUpText = ""
                                        executeAiQuery(activeAiToolTitle, q)
                                    }
                                },
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFF8B5CF6))
                            ) {
                                Icon(Icons.Default.Send, null, tint = Color.White, modifier = Modifier.size(16.dp))
                            }
                        }
                    }
                }
            }
        }
    }
}

// ================= TOP BAR HEADER =================
@Composable
private fun TopBarHeader(
    currentSubView: VeHubSubView,
    onOpenDrawer: () -> Unit,
    onBackToDashboard: () -> Unit,
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit,
    isSearchActive: Boolean,
    onToggleSearch: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (currentSubView != VeHubSubView.DASHBOARD) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(
                    onClick = onBackToDashboard,
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF1B1A38))
                ) {
                    Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = Color.White)
                }
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = when (currentSubView) {
                        VeHubSubView.SUBJECTS -> "Subjects"
                        VeHubSubView.AI_TOOLS -> "AI Study Tools"
                        VeHubSubView.PLANNER -> "Daily Planner"
                        VeHubSubView.PROGRESS -> "Progress & Analytics"
                        else -> "VEHub"
                    },
                    color = Color.White,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            IconButton(onClick = onToggleSearch) {
                Icon(
                    imageVector = if (isSearchActive) Icons.Default.Close else Icons.Default.Search,
                    contentDescription = "Search",
                    tint = Color.White
                )
            }
        } else {
            // Main Dashboard Header (Exact match to Image Left)
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(
                    onClick = onOpenDrawer,
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(Icons.Default.Menu, contentDescription = "Menu", tint = Color.White)
                }
                Spacer(modifier = Modifier.width(6.dp))
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "VEHub",
                            color = Color.White,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.ExtraBold
                        )
                    }
                    Text(
                        text = "Your Study Hub ✨",
                        color = Color(0xFFA855F7),
                        fontSize = 11.5.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Bell Notification Icon with Unread Purple Indicator
                Box {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF161530))
                            .border(1.dp, Color(0xFF28264A), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Default.Notifications,
                            contentDescription = "Notifications",
                            tint = Color.White,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    Box(
                        modifier = Modifier
                            .size(9.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF8B5CF6))
                            .align(Alignment.TopEnd)
                    )
                }

                // User Avatar Profile (Rahul)
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(
                            Brush.linearGradient(
                                colors = listOf(Color(0xFF8B5CF6), Color(0xFFEC4899))
                            )
                        )
                        .padding(2.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF2D1B4E)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "👨‍🎓",
                        fontSize = 18.sp
                    )
                }
            }
        }
    }

    // Expandable Search Bar
    AnimatedVisibility(visible = isSearchActive) {
        OutlinedTextField(
            value = searchQuery,
            onValueChange = onSearchQueryChange,
            placeholder = { Text("Search subjects or tools...", color = Color(0xFF6B6893), fontSize = 12.5.sp) },
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
}

// ================= MAIN DASHBOARD CONTENT =================
@Composable
private fun DashboardMainContent(
    plannerTasks: List<StudyTask>,
    onToggleTask: (StudyTask) -> Unit,
    onOpenSubjects: () -> Unit,
    onOpenAiTools: () -> Unit,
    onOpenPlanner: () -> Unit,
    onOpenProgress: () -> Unit,
    onOpenAskVed: () -> Unit,
    onSelectAiTool: (HubAiTool) -> Unit,
    subjectsList: List<HubSubject>,
    aiToolsList: List<HubAiTool>,
    studyResourcesList: List<HubResource>,
    recentActivityList: List<HubActivityItem>,
    vedInputQuery: String,
    onVedInputQueryChange: (String) -> Unit,
    onSendVedInput: () -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // 1. Greeting Banner with Ask VED Pill
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Good Morning, Rahul! 👋",
                        color = Color.White,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "Let's make today productive.",
                        color = Color(0xFFA09EC0),
                        fontSize = 12.5.sp
                    )
                }

                // Purple "Ask VED ✨" Pill Button
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(20.dp))
                        .background(Color(0xFF8B5CF6))
                        .clickable { onOpenAskVed() }
                        .padding(horizontal = 14.dp, vertical = 8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.AutoAwesome,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Ask VED",
                            color = Color.White,
                            fontSize = 12.5.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }

        // 2. Today's Goal & Streak Card (Matching Image Left)
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color(0xFF14132B))
                    .border(1.dp, Color(0xFF28264A), RoundedCornerShape(16.dp))
                    .padding(14.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Left: Today's Goal
                Row(
                    modifier = Modifier.weight(1.3f),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(Color(0xFF8B5CF6).copy(alpha = 0.2f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("🎯", fontSize = 18.sp)
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Today's Goal",
                            color = Color(0xFFA09EC0),
                            fontSize = 11.5.sp
                        )
                        Text(
                            text = "4h 30m / 6h",
                            color = Color.White,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        // Progress Bar
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .height(6.dp)
                                    .clip(RoundedCornerShape(3.dp))
                                    .background(Color(0xFF28264A))
                            ) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxHeight()
                                        .fillMaxWidth(0.75f)
                                        .clip(RoundedCornerShape(3.dp))
                                        .background(
                                            Brush.horizontalGradient(
                                                colors = listOf(Color(0xFF8B5CF6), Color(0xFF06B6D4))
                                            )
                                        )
                                )
                            }
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "75%",
                                color = Color(0xFFA09EC0),
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.width(12.dp))
                Box(
                    modifier = Modifier
                        .width(1.dp)
                        .height(40.dp)
                        .background(Color(0xFF28264A))
                )
                Spacer(modifier = Modifier.width(12.dp))

                // Right: Day Streak
                Row(
                    modifier = Modifier.weight(0.9f),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "28",
                                color = Color.White,
                                fontSize = 20.sp,
                                fontWeight = FontWeight.ExtraBold
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("🔥", fontSize = 16.sp)
                        }
                        Text(
                            text = "Day Streak",
                            color = Color(0xFFA09EC0),
                            fontSize = 11.sp
                        )
                    }
                }
            }
        }

        // 3. Continue Learning Card
        item {
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Continue Learning",
                        color = Color.White,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "See All >",
                        color = Color(0xFF8B5CF6),
                        fontSize = 12.5.sp,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.clickable { onOpenSubjects() }
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(Color(0xFF14132B))
                        .border(1.dp, Color(0xFF383363), RoundedCornerShape(16.dp))
                        .padding(14.dp)
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
                                    .size(44.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(
                                        Brush.linearGradient(
                                            colors = listOf(Color(0xFF7C3AED), Color(0xFF3B82F6))
                                        )
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Default.Diamond, contentDescription = null, tint = Color.White, modifier = Modifier.size(22.dp))
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = "Electrostatics",
                                    color = Color.White,
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "Physics • Chapter 4",
                                    color = Color(0xFFA09EC0),
                                    fontSize = 12.sp
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = "🕒 Last opened 35 min ago",
                                    color = Color(0xFF6B6893),
                                    fontSize = 11.sp
                                )
                            }
                        }

                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(20.dp))
                                .background(Color(0xFF6366F1))
                                .clickable { onOpenSubjects() }
                                .padding(horizontal = 14.dp, vertical = 8.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.PlayArrow, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Continue", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }

        // 4. Subjects Row
        item {
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Subjects",
                        color = Color.White,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "View All",
                        color = Color(0xFF8B5CF6),
                        fontSize = 12.5.sp,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.clickable { onOpenSubjects() }
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    subjectsList.take(4).forEach { subject ->
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(14.dp))
                                .background(Color(0xFF14132B))
                                .border(1.dp, Color(0xFF28264A), RoundedCornerShape(14.dp))
                                .clickable { onOpenSubjects() }
                                .padding(10.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Box(
                                    modifier = Modifier
                                        .size(36.dp)
                                        .clip(RoundedCornerShape(10.dp))
                                        .background(subject.color.copy(alpha = 0.2f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(subject.icon, contentDescription = null, tint = subject.color, modifier = Modifier.size(20.dp))
                                }
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = subject.title,
                                    color = Color.White,
                                    fontSize = 11.5.sp,
                                    fontWeight = FontWeight.Bold,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Text(
                                    text = "${subject.completedChapters} Chapters",
                                    color = Color(0xFF6B6893),
                                    fontSize = 10.sp
                                )
                            }
                        }
                    }
                }
            }
        }

        // 5. AI Study Tools Row
        item {
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "AI Study Tools",
                        color = Color.White,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "View All",
                        color = Color(0xFF8B5CF6),
                        fontSize = 12.5.sp,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.clickable { onOpenAiTools() }
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(aiToolsList) { tool ->
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(14.dp))
                                .background(Color(0xFF14132B))
                                .border(1.dp, Color(0xFF28264A), RoundedCornerShape(14.dp))
                                .clickable { onSelectAiTool(tool) }
                                .padding(horizontal = 12.dp, vertical = 10.dp)
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Box(
                                    modifier = Modifier
                                        .size(38.dp)
                                        .clip(RoundedCornerShape(10.dp))
                                        .background(tool.iconBgColor.copy(alpha = 0.2f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(tool.icon, contentDescription = null, tint = tool.iconBgColor, modifier = Modifier.size(20.dp))
                                }
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = tool.title,
                                    color = Color.White,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }
                    }
                }
            }
        }

        // 6. Study Resources Grid (2x3)
        item {
            Column {
                Text(
                    text = "Study Resources",
                    color = Color.White,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(8.dp))

                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    studyResourcesList.chunked(3).forEach { rowItems ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            rowItems.forEach { res ->
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(Color(0xFF14132B))
                                        .border(1.dp, Color(0xFF28264A), RoundedCornerShape(12.dp))
                                        .padding(10.dp)
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Box(
                                            modifier = Modifier
                                                .size(30.dp)
                                                .clip(RoundedCornerShape(8.dp))
                                                .background(res.accentColor.copy(alpha = 0.2f)),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(res.icon, contentDescription = null, tint = res.accentColor, modifier = Modifier.size(16.dp))
                                        }
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Column {
                                            Text(
                                                text = res.title,
                                                color = Color.White,
                                                fontSize = 11.5.sp,
                                                fontWeight = FontWeight.Bold,
                                                maxLines = 1
                                            )
                                            Text(
                                                text = res.countText,
                                                color = Color(0xFF6B6893),
                                                fontSize = 10.sp
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // 7. Today's Planner
        item {
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Today's Planner",
                        color = Color.White,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "View Planner >",
                        color = Color(0xFF8B5CF6),
                        fontSize = 12.5.sp,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.clickable { onOpenPlanner() }
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(Color(0xFF14132B))
                        .border(1.dp, Color(0xFF28264A), RoundedCornerShape(16.dp))
                        .padding(14.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(
                            modifier = Modifier.weight(1f),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            plannerTasks.take(3).forEach { task ->
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.clickable { onToggleTask(task) }
                                ) {
                                    Icon(
                                        imageVector = if (task.isCompleted) Icons.Default.CheckCircle else Icons.Default.RadioButtonUnchecked,
                                        contentDescription = null,
                                        tint = if (task.isCompleted) Color(0xFF10B981) else Color(0xFF6B6893),
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Column {
                                        Text(
                                            text = task.title,
                                            color = if (task.isCompleted) Color(0xFF6B6893) else Color.White,
                                            fontSize = 12.5.sp,
                                            fontWeight = FontWeight.SemiBold
                                        )
                                        Text(
                                            text = task.dueDate,
                                            color = Color(0xFF6B6893),
                                            fontSize = 10.5.sp
                                        )
                                    }
                                }
                            }
                        }

                        // 3D Purple Calendar Graphic Illustration
                        Box(
                            modifier = Modifier
                                .size(64.dp)
                                .clip(RoundedCornerShape(14.dp))
                                .background(
                                    Brush.linearGradient(
                                        colors = listOf(Color(0xFF8B5CF6), Color(0xFF6366F1))
                                    )
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.CalendarMonth, contentDescription = null, tint = Color.White, modifier = Modifier.size(34.dp))
                        }
                    }
                }
            }
        }

        // 8. Performance Summary & VED Recommends
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Left: Performance Summary Card
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(16.dp))
                        .background(Color(0xFF14132B))
                        .border(1.dp, Color(0xFF28264A), RoundedCornerShape(16.dp))
                        .padding(12.dp)
                ) {
                    Column {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Performance", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(Color(0xFF28264A))
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text("This Week ▾", color = Color(0xFFA09EC0), fontSize = 9.5.sp)
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Text("Study Time: 32h 15m", color = Color(0xFFA09EC0), fontSize = 10.5.sp)
                        Text("Accuracy: 91%", color = Color(0xFF10B981), fontSize = 11.sp, fontWeight = FontWeight.Bold)

                        Spacer(modifier = Modifier.height(10.dp))

                        // Mini Bar Graph Illustration
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(32.dp),
                            horizontalArrangement = Arrangement.SpaceEvenly,
                            verticalAlignment = Alignment.Bottom
                        ) {
                            listOf(0.4f, 0.7f, 0.5f, 0.9f, 0.6f, 0.8f, 0.75f).forEach { ratio ->
                                Box(
                                    modifier = Modifier
                                        .width(6.dp)
                                        .fillMaxHeight(ratio)
                                        .clip(RoundedCornerShape(3.dp))
                                        .background(Color(0xFF8B5CF6))
                                )
                            }
                        }
                    }
                }

                // Right: VED Recommends Card
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(16.dp))
                        .background(Color(0xFF14132B))
                        .border(1.dp, Color(0xFF28264A), RoundedCornerShape(16.dp))
                        .padding(12.dp)
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text("VED Recommends", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)

                        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text("⚛", fontSize = 11.sp)
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Continue Ch 4", color = Color.White, fontSize = 10.5.sp, fontWeight = FontWeight.SemiBold, modifier = Modifier.weight(1f))
                                Icon(Icons.Default.ChevronRight, null, tint = Color(0xFF8B5CF6), modifier = Modifier.size(12.dp))
                            }
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text("📘", fontSize = 11.sp)
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Revise Formula Sheet", color = Color.White, fontSize = 10.5.sp, fontWeight = FontWeight.SemiBold, modifier = Modifier.weight(1f))
                                Icon(Icons.Default.ChevronRight, null, tint = Color(0xFF8B5CF6), modifier = Modifier.size(12.dp))
                            }
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text("📝", fontSize = 11.sp)
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Solve 25 PYQs", color = Color.White, fontSize = 10.5.sp, fontWeight = FontWeight.SemiBold, modifier = Modifier.weight(1f))
                                Icon(Icons.Default.ChevronRight, null, tint = Color(0xFF8B5CF6), modifier = Modifier.size(12.dp))
                            }
                        }
                    }
                }
            }
        }

        // 9. Recent Activity List
        item {
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Recent Activity",
                        color = Color.White,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "View All >",
                        color = Color(0xFF8B5CF6),
                        fontSize = 12.5.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    recentActivityList.forEach { item ->
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(Color(0xFF14132B))
                                .padding(10.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(
                                        modifier = Modifier
                                            .size(32.dp)
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(Color(0xFF8B5CF6).copy(alpha = 0.2f)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(item.icon, null, tint = Color(0xFF8B5CF6), modifier = Modifier.size(16.dp))
                                    }
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Column {
                                        Text(item.title, color = Color.White, fontSize = 12.5.sp, fontWeight = FontWeight.SemiBold)
                                        Text(item.timestamp, color = Color(0xFF6B6893), fontSize = 10.5.sp)
                                    }
                                }

                                Icon(Icons.Default.MoreVert, null, tint = Color(0xFF6B6893), modifier = Modifier.size(16.dp))
                            }
                        }
                    }
                }
            }
        }

        // 10. Docked / Floating Bottom Input Bar ("Ask VED")
        item {
            Spacer(modifier = Modifier.height(4.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(24.dp))
                    .background(Color(0xFF14132B))
                    .border(1.dp, Color(0xFF383363), RoundedCornerShape(24.dp))
                    .padding(horizontal = 14.dp, vertical = 6.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = vedInputQuery,
                        onValueChange = onVedInputQueryChange,
                        placeholder = { Text("Ask VED anything about your studies...", color = Color(0xFF6B6893), fontSize = 12.sp) },
                        modifier = Modifier.weight(1f),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color.Transparent,
                            unfocusedBorderColor = Color.Transparent,
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White
                        ),
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
                        keyboardActions = KeyboardActions(onSend = { onSendVedInput() })
                    )

                    IconButton(onClick = { onOpenAskVed() }) {
                        Icon(Icons.Default.Mic, contentDescription = "Voice", tint = Color(0xFFA09EC0), modifier = Modifier.size(18.dp))
                    }

                    Box(
                        modifier = Modifier
                            .size(34.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF8B5CF6))
                            .clickable { onSendVedInput() },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.Send, contentDescription = "Send", tint = Color.White, modifier = Modifier.size(15.dp))
                    }
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

// ================= SUBJECTS DETAIL CONTENT (Top-Right Phone) =================
@Composable
private fun SubjectsDetailContent(
    subjects: List<HubSubject>,
    onSubjectClick: (HubSubject) -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
        contentPadding = PaddingValues(bottom = 24.dp)
    ) {
        items(subjects) { subject ->
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color(0xFF14132B))
                    .border(1.dp, Color(0xFF28264A), RoundedCornerShape(16.dp))
                    .clickable { onSubjectClick(subject) }
                    .padding(14.dp)
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
                                .size(42.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(subject.color.copy(alpha = 0.2f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(subject.icon, null, tint = subject.color, modifier = Modifier.size(22.dp))
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(subject.title, color = Color.White, fontSize = 15.sp, fontWeight = FontWeight.Bold)
                            Text("${subject.completedChapters} / ${subject.totalChapters} Chapters", color = Color(0xFFA09EC0), fontSize = 12.sp)
                            Spacer(modifier = Modifier.height(6.dp))

                            // Chapter Progress Bar
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth(0.85f)
                                    .height(4.dp)
                                    .clip(RoundedCornerShape(2.dp))
                                    .background(Color(0xFF28264A))
                            ) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxHeight()
                                        .fillMaxWidth(subject.progressPercent / 100f)
                                        .clip(RoundedCornerShape(2.dp))
                                        .background(subject.color)
                                )
                            }
                        }
                    }

                    // Percentage Pill Badge
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color(0xFF28264A))
                            .padding(horizontal = 10.dp, vertical = 5.dp)
                    ) {
                        Text("${subject.progressPercent}%", color = Color.White, fontSize = 11.5.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

// ================= AI TOOLS DETAIL CONTENT (Bottom-Right Phone) =================
@Composable
private fun AiToolsDetailContent(
    aiTools: List<HubAiTool>,
    recentChats: List<HubActivityItem>,
    onSelectTool: (HubAiTool) -> Unit,
    onSelectChat: (HubActivityItem) -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
        contentPadding = PaddingValues(bottom = 24.dp)
    ) {
        item {
            Text("AI Study Tools", color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold)
        }

        // 2-Column Grid of AI Tool Cards
        item {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                aiTools.chunked(2).forEach { rowTools ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        rowTools.forEach { tool ->
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(14.dp))
                                    .background(Color(0xFF14132B))
                                    .border(1.dp, Color(0xFF28264A), RoundedCornerShape(14.dp))
                                    .clickable { onSelectTool(tool) }
                                    .padding(12.dp)
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(
                                        modifier = Modifier
                                            .size(36.dp)
                                            .clip(RoundedCornerShape(10.dp))
                                            .background(tool.iconBgColor.copy(alpha = 0.2f)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(tool.icon, null, tint = tool.iconBgColor, modifier = Modifier.size(18.dp))
                                    }
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Column {
                                        Text(tool.title, color = Color.White, fontSize = 12.5.sp, fontWeight = FontWeight.Bold)
                                        Text(tool.description, color = Color(0xFF6B6893), fontSize = 10.sp)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // Recent Chats Section
        item {
            Spacer(modifier = Modifier.height(6.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Recent Chats", color = Color.White, fontSize = 15.sp, fontWeight = FontWeight.Bold)
                Text("View All", color = Color(0xFF8B5CF6), fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
            }
        }

        items(recentChats) { chat ->
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(0xFF14132B))
                    .clickable { onSelectChat(chat) }
                    .padding(12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .clip(CircleShape)
                                .background(Color(0xFF8B5CF6).copy(alpha = 0.2f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.ChatBubble, null, tint = Color(0xFF8B5CF6), modifier = Modifier.size(16.dp))
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(chat.title, color = Color.White, fontSize = 12.5.sp, fontWeight = FontWeight.SemiBold)
                            Text(chat.timestamp, color = Color(0xFF6B6893), fontSize = 10.5.sp)
                        }
                    }

                    Icon(Icons.Default.MoreVert, null, tint = Color(0xFF6B6893), modifier = Modifier.size(16.dp))
                }
            }
        }
    }
}

// ================= PLANNER DETAIL CONTENT =================
@Composable
private fun PlannerDetailContent(
    plannerTasks: List<StudyTask>,
    onToggleTask: (StudyTask) -> Unit,
    onAddTask: (String, String, String) -> Unit
) {
    var newTaskTitle by remember { mutableStateOf("") }
    var newTaskSubject by remember { mutableStateOf("") }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        contentPadding = PaddingValues(bottom = 24.dp)
    ) {
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(Color(0xFF14132B))
                    .padding(14.dp)
            ) {
                Column {
                    Text("Add Daily Study Schedule", color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = newTaskTitle,
                        onValueChange = { newTaskTitle = it },
                        placeholder = { Text("Task (e.g. Solve 25 Physics MCQs)", color = Color(0xFF6B6893), fontSize = 12.sp) },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color(0xFF8B5CF6),
                            unfocusedBorderColor = Color(0xFF28264A),
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White
                        )
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedTextField(
                            value = newTaskSubject,
                            onValueChange = { newTaskSubject = it },
                            placeholder = { Text("Subject", color = Color(0xFF6B6893), fontSize = 12.sp) },
                            modifier = Modifier.weight(1f),
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = Color(0xFF8B5CF6),
                                unfocusedBorderColor = Color(0xFF28264A),
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White
                            )
                        )
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(10.dp))
                                .background(Color(0xFF8B5CF6))
                                .clickable {
                                    if (newTaskTitle.isNotBlank()) {
                                        onAddTask(newTaskTitle, newTaskSubject.ifBlank { "General" }, "Today")
                                        newTaskTitle = ""
                                        newTaskSubject = ""
                                    }
                                }
                                .padding(horizontal = 16.dp, vertical = 12.dp)
                        ) {
                            Text("Add", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        }
                    }
                }
            }
        }

        item {
            Text("Scheduled Tasks", color = Color.White, fontSize = 15.sp, fontWeight = FontWeight.Bold)
        }

        items(plannerTasks) { task ->
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(0xFF14132B))
                    .clickable { onToggleTask(task) }
                    .padding(12.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = if (task.isCompleted) Icons.Default.CheckCircle else Icons.Default.RadioButtonUnchecked,
                        contentDescription = null,
                        tint = if (task.isCompleted) Color(0xFF10B981) else Color(0xFF6B6893)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(task.title, color = if (task.isCompleted) Color(0xFF6B6893) else Color.White, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                        Text("${task.subject} • ${task.dueDate}", color = Color(0xFF6B6893), fontSize = 11.sp)
                    }
                }
            }
        }
    }
}

// ================= PROGRESS DETAIL CONTENT =================
@Composable
private fun ProgressDetailContent() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(Color(0xFF14132B))
                .padding(16.dp)
        ) {
            Column {
                Text("Weekly Study Progress", color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(10.dp))
                Text("Total Hours: 32h 15m", color = Color(0xFF8B5CF6), fontSize = 14.sp, fontWeight = FontWeight.ExtraBold)
                Text("Goal Met: 85% of weekly targets", color = Color(0xFF10B981), fontSize = 12.sp)

                Spacer(modifier = Modifier.height(16.dp))

                // Chart Bars Representation
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(100.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.Bottom
                ) {
                    val days = listOf("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun")
                    val heights = listOf(0.6f, 0.8f, 0.4f, 0.95f, 0.7f, 0.85f, 0.5f)

                    days.zip(heights).forEach { (day, h) ->
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Box(
                                modifier = Modifier
                                    .width(12.dp)
                                    .fillMaxHeight(h)
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(
                                        Brush.verticalGradient(
                                            colors = listOf(Color(0xFFEC4899), Color(0xFF8B5CF6))
                                        )
                                    )
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(day, color = Color(0xFF6B6893), fontSize = 10.sp)
                        }
                    }
                }
            }
        }
    }
}

// ================= BOTTOM SUB-NAVIGATION BAR =================
@Composable
private fun VeHubBottomNavBar(
    currentSubView: VeHubSubView,
    onSelectSubView: (VeHubSubView) -> Unit,
    onOpenVedAssistant: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFF0F0E22))
            .border(1.dp, Color(0xFF28264A))
            .padding(vertical = 6.dp, horizontal = 12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // 1. Home
            BottomNavItem(
                icon = Icons.Default.Home,
                label = "Home",
                isSelected = currentSubView == VeHubSubView.DASHBOARD,
                onClick = { onSelectSubView(VeHubSubView.DASHBOARD) }
            )

            // 2. Subjects
            BottomNavItem(
                icon = Icons.Default.Book,
                label = "Subjects",
                isSelected = currentSubView == VeHubSubView.SUBJECTS,
                onClick = { onSelectSubView(VeHubSubView.SUBJECTS) }
            )

            // 3. Center Glowing VED Pulse Button
            Box(
                modifier = Modifier
                    .size(46.dp)
                    .clip(CircleShape)
                    .background(
                        Brush.linearGradient(
                            colors = listOf(Color(0xFF8B5CF6), Color(0xFFEC4899))
                        )
                    )
                    .clickable { onOpenVedAssistant() },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.AutoAwesome,
                    contentDescription = "VED Assistant",
                    tint = Color.White,
                    modifier = Modifier.size(24.dp)
                )
            }

            // 4. Planner
            BottomNavItem(
                icon = Icons.Default.CalendarMonth,
                label = "Planner",
                isSelected = currentSubView == VeHubSubView.PLANNER,
                onClick = { onSelectSubView(VeHubSubView.PLANNER) }
            )

            // 5. Progress
            BottomNavItem(
                icon = Icons.Default.Analytics,
                label = "Progress",
                isSelected = currentSubView == VeHubSubView.PROGRESS,
                onClick = { onSelectSubView(VeHubSubView.PROGRESS) }
            )
        }
    }
}

@Composable
private fun BottomNavItem(
    icon: ImageVector,
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .clickable { onClick() }
            .padding(horizontal = 8.dp, vertical = 2.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            tint = if (isSelected) Color(0xFF8B5CF6) else Color(0xFF6B6893),
            modifier = Modifier.size(20.dp)
        )
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = label,
            color = if (isSelected) Color(0xFF8B5CF6) else Color(0xFF6B6893),
            fontSize = 10.sp,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
        )
    }
}
