package com.example.services

import android.content.Context

object OfflineIntentParser {

    fun tryParseAndExecute(context: Context, dbService: DatabaseService, text: String): UtilityResult? {
        val lower = text.trim().lowercase()

        // 1. Study Habit Logging & Streak Queries
        if (lower.contains("log") && (lower.contains("study") || lower.contains("hours") || lower.contains("mins") || lower.contains("minutes"))) {
            val digits = Regex("""\d+""").find(lower)?.value?.toIntOrNull() ?: 1
            val durationMinutes = if (lower.contains("hour") || lower.contains("hr")) digits * 60 else digits
            val subject = when {
                lower.contains("physics") -> "Physics"
                lower.contains("mechanics") -> "Mechanics"
                lower.contains("math") || lower.contains("maths") -> "Mathematics"
                lower.contains("chem") -> "Chemistry"
                lower.contains("bio") -> "Biology"
                lower.contains("code") || lower.contains("coding") -> "Coding"
                else -> "General Study"
            }
            dbService.logStudyHabit(subject, durationMinutes)
            val streak = dbService.calculateStudyStreak()
            return UtilityResult(true, "Logged $durationMinutes mins of $subject study! 🔥 Current Streak: $streak days", "HABIT_LOG")
        }

        if (lower.contains("streak") || lower.contains("study streak")) {
            val streak = dbService.calculateStudyStreak()
            val totalMins = dbService.getTotalStudyMinutesThisWeek()
            val hours = totalMins / 60
            val mins = totalMins % 60
            return UtilityResult(true, "🔥 Current Study Streak: $streak days!\n⏱️ Weekly Study Time: ${hours}h ${mins}m", "HABIT_STREAK")
        }

        // 2. Hardware, Volume, Music, Notes, Alarms, App Launcher via UtilityService
        val localUtil = UtilityService.parseAndExecuteLocalCommand(context, dbService, text)
        if (localUtil.isHandled) {
            return localUtil
        }

        // 3. Offline VEDM-T Knowledge Store & Local Search Engine
        val vedmResult = dbService.searchVedmTKnowledge(text, context)
        if (vedmResult != null) {
            return UtilityResult(true, vedmResult, "VEDM_T_KNOWLEDGE")
        }

        val localSearchResult = dbService.searchOfflineContent(text)
        if (localSearchResult != null) {
            return UtilityResult(true, localSearchResult, "OFFLINE_SEARCH")
        }

        return null
    }
}
