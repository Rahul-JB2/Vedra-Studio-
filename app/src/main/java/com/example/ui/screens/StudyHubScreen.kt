package com.example.ui.screens

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.RadioButtonUnchecked
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material3.CircularProgressIndicator
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
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.services.DatabaseService
import com.example.services.ExternalService
import com.example.services.Flashcard
import androidx.compose.material.icons.filled.Mic
import com.example.services.NotificationService
import com.example.services.StudyHabit
import com.example.services.StudyService
import com.example.services.StudyTask
import com.example.ui.components.CustomButton
import com.example.ui.components.CustomCard
import com.example.ui.components.CustomInput
import com.example.ui.components.CustomModal
import com.example.ui.theme.Spacing
import com.example.ui.theme.VedraBackground
import com.example.ui.theme.VedraBlueAccent
import com.example.ui.theme.VedraBorder
import com.example.ui.theme.VedraCyanAccent
import com.example.ui.theme.VedraOnlineGreen
import com.example.ui.theme.VedraPinkAccent
import com.example.ui.theme.VedraPurplePrimary
import com.example.ui.theme.VedraPurpleSecondary
import com.example.ui.theme.VedraSurface
import com.example.ui.theme.VedraSurfaceVariant
import com.example.ui.theme.VedraTextMuted
import com.example.ui.theme.VedraTextPrimary
import com.example.ui.theme.VedraTextSecondary
import kotlinx.coroutines.launch

@Composable
fun StudyHubScreen(
    dbService: DatabaseService,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    var activeSubTab by remember { mutableIntStateOf(0) } // 0: Planner, 1: Flashcards, 2: AI Solve & PDF

    val studyTasks = remember { mutableStateListOf<StudyTask>() }
    val flashcards = remember { mutableStateListOf<Flashcard>() }
    val studyHabits = remember { mutableStateListOf<StudyHabit>() }

    var streakCount by remember { mutableIntStateOf(0) }
    var weeklyMinutes by remember { mutableIntStateOf(0) }

    // Habit Modal State
    var isAddHabitModalOpen by remember { mutableStateOf(false) }
    var inputHabitSubject by remember { mutableStateOf("") }
    var inputHabitMinutes by remember { mutableStateOf("") }

    // Flashcard Flip State
    var currentCardIndex by remember { mutableIntStateOf(0) }
    var isAnswerVisible by remember { mutableStateOf(false) }

    // Task Modal State
    var isAddTaskModalOpen by remember { mutableStateOf(false) }
    var newTaskTitle by remember { mutableStateOf("") }
    var newTaskSubject by remember { mutableStateOf("") }

    // Image & PDF Pickers
    var selectedImageUri by remember { mutableStateOf<Uri?>(null) }
    var selectedPdfUri by remember { mutableStateOf<Uri?>(null) }
    var pdfQuestionText by remember { mutableStateOf("") }

    var isAiAnalyzing by remember { mutableStateOf(false) }
    var aiSolutionResult by remember { mutableStateOf<String?>(null) }
    var ocrExtractedText by remember { mutableStateOf("") }

    // Lecture Recording & Auto-Flashcard Pipeline
    var isLectureRecording by remember { mutableStateOf(false) }
    var inputLectureTitle by remember { mutableStateOf("") }
    var lectureSummaryText by remember { mutableStateOf<String?>(null) }

    // Focus Lock Mode
    var isFocusModeActive by remember { mutableStateOf(false) }
    var focusDurationMinutes by remember { mutableIntStateOf(45) }
    var focusNudgesCount by remember { mutableIntStateOf(0) }

    fun refreshData() {
        studyTasks.clear()
        studyTasks.addAll(dbService.getAllStudyTasks())

        flashcards.clear()
        flashcards.addAll(dbService.getAllFlashcards())

        studyHabits.clear()
        studyHabits.addAll(dbService.getAllStudyHabits())

        streakCount = dbService.calculateStudyStreak()
        weeklyMinutes = dbService.getTotalStudyMinutesThisWeek()
    }

    LaunchedEffect(Unit) {
        refreshData()
    }

    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        selectedImageUri = uri
        aiSolutionResult = null
    }

    val pdfPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        selectedPdfUri = uri
        aiSolutionResult = null
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(VedraBackground)
            .padding(horizontal = Spacing.medium),
        contentPadding = PaddingValues(vertical = Spacing.medium),
        verticalArrangement = Arrangement.spacedBy(Spacing.medium)
    ) {
        // Content deleted as requested
    }

    // Modal for Logging Study Habit
    CustomModal(
        visible = isAddHabitModalOpen,
        title = "Log Study Session",
        onDismissRequest = { isAddHabitModalOpen = false }
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(Spacing.medium)) {
            CustomInput(
                value = inputHabitSubject,
                onValueChange = { inputHabitSubject = it },
                placeholder = "Subject (e.g. Physics, Mechanics, Chemistry)"
            )
            CustomInput(
                value = inputHabitMinutes,
                onValueChange = { inputHabitMinutes = it },
                placeholder = "Duration in minutes (e.g. 120)"
            )
            CustomButton(
                text = "Log Session",
                onClick = {
                    val mins = inputHabitMinutes.toIntOrNull() ?: 60
                    if (inputHabitSubject.isNotBlank()) {
                        dbService.logStudyHabit(inputHabitSubject, mins)
                        refreshData()
                        isAddHabitModalOpen = false
                    }
                },
                modifier = Modifier.fillMaxWidth()
            )
        }
    }

    // Modal for Adding Study Task
    CustomModal(
        visible = isAddTaskModalOpen,
        title = "Add Daily Study Goal",
        onDismissRequest = { isAddTaskModalOpen = false }
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(Spacing.medium)) {
            CustomInput(
                value = newTaskTitle,
                onValueChange = { newTaskTitle = it },
                placeholder = "Goal Title (e.g. Solve 20 Physics MCQs)"
            )
            CustomInput(
                value = newTaskSubject,
                onValueChange = { newTaskSubject = it },
                placeholder = "Subject (e.g. Physics, Chemistry, Math)"
            )
            CustomButton(
                text = "Save Goal",
                onClick = {
                    if (newTaskTitle.isNotBlank()) {
                        dbService.addStudyTask(newTaskTitle, newTaskSubject.ifBlank { "General" })
                        refreshData()
                        isAddTaskModalOpen = false
                    }
                },
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}
