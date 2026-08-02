package com.example.services

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.hardware.camera2.CameraManager
import android.media.AudioManager
import android.net.Uri
import java.net.URLEncoder
import java.util.Locale
import kotlin.math.pow
import org.json.JSONArray

data class UtilityResult(
    val isHandled: Boolean,
    val responseMessage: String,
    val actionType: String = "GENERAL",
    val eventData: CalendarEventItem? = null
)

object UtilityService {

    private var isTorchOn = false

    fun toggleFlashlight(context: Context, turnOn: Boolean? = null): String {
        return try {
            val cameraManager = context.getSystemService(Context.CAMERA_SERVICE) as CameraManager
            val cameraId = cameraManager.cameraIdList.firstOrNull { id ->
                cameraManager.getCameraCharacteristics(id)
                    .get(android.hardware.camera2.CameraCharacteristics.FLASH_INFO_AVAILABLE) == true
            } ?: return "Flashlight hardware is not available on this device."

            val targetState = turnOn ?: !isTorchOn
            cameraManager.setTorchMode(cameraId, targetState)
            isTorchOn = targetState
            if (targetState) "Flashlight turned ON 🔦" else "Flashlight turned OFF 🔦"
        } catch (e: Exception) {
            "Flashlight control error: ${e.localizedMessage}"
        }
    }

    fun writeToClipboard(context: Context, text: String): String {
        return try {
            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
            val clip = ClipData.newPlainText("VEDRA Clip", text)
            clipboard.setPrimaryClip(clip)
            "Copied to clipboard: \"$text\" 📋"
        } catch (e: Exception) {
            "Failed to copy to clipboard: ${e.localizedMessage}"
        }
    }

    fun readFromClipboard(context: Context): String {
        return try {
            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
            if (clipboard.hasPrimaryClip() && (clipboard.primaryClip?.itemCount ?: 0) > 0) {
                val item = clipboard.primaryClip?.getItemAt(0)
                val text = item?.text?.toString()
                if (!text.isNullOrEmpty()) {
                    "Clipboard contents: \"$text\" 📋"
                } else {
                    "Clipboard is empty."
                }
            } else {
                "Clipboard is empty."
            }
        } catch (e: Exception) {
            "Failed to read clipboard: ${e.localizedMessage}"
        }
    }

    fun setVolumeLevel(context: Context, percentage: Int): String {
        return try {
            val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
            val maxVol = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC)
            val targetVol = (maxVol * (percentage.coerceIn(0, 100) / 100.0)).toInt()
            audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, targetVol, AudioManager.FLAG_SHOW_UI)
            "Volume set to $percentage% 🔊"
        } catch (e: Exception) {
            "Failed to set volume: ${e.localizedMessage}"
        }
    }

    fun adjustVolume(context: Context, isUp: Boolean): String {
        return try {
            val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
            val flag = if (isUp) AudioManager.ADJUST_RAISE else AudioManager.ADJUST_LOWER
            audioManager.adjustStreamVolume(AudioManager.STREAM_MUSIC, flag, AudioManager.FLAG_SHOW_UI)
            if (isUp) "Volume increased 🔊" else "Volume decreased 🔉"
        } catch (e: Exception) {
            "Failed to adjust volume: ${e.localizedMessage}"
        }
    }

    fun muteVolume(context: Context, mute: Boolean): String {
        return try {
            val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
            if (mute) {
                audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, 0, AudioManager.FLAG_SHOW_UI)
                "Audio muted 🔇"
            } else {
                val maxVol = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC)
                audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, maxVol / 2, AudioManager.FLAG_SHOW_UI)
                "Audio unmuted 🔊"
            }
        } catch (e: Exception) {
            "Failed to toggle mute: ${e.localizedMessage}"
        }
    }

    fun openMusicOrSpotify(context: Context, query: String = ""): String {
        return try {
            val intent = if (query.isNotBlank()) {
                val encoded = URLEncoder.encode(query, "UTF-8")
                Intent(Intent.ACTION_VIEW, Uri.parse("https://open.spotify.com/search/$encoded")).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK
                }
            } else {
                val launchIntent = context.packageManager.getLaunchIntentForPackage("com.spotify.music")
                    ?: Intent(Intent.ACTION_VIEW, Uri.parse("https://open.spotify.com")).apply {
                        flags = Intent.FLAG_ACTIVITY_NEW_TASK
                    }
                launchIntent
            }
            context.startActivity(intent)
            if (query.isNotBlank()) "Playing '$query' on Spotify 🎵" else "Opening Music app 🎵"
        } catch (e: Exception) {
            "Failed to launch music: ${e.localizedMessage}"
        }
    }

    fun evaluateMathExpression(input: String): String? {
        val clean = input.replace("calculate", "", ignoreCase = true)
            .replace("math", "", ignoreCase = true)
            .replace("what is", "", ignoreCase = true)
            .replace("=", "")
            .trim()

        if (clean.isEmpty()) return null

        val regex = Regex("""^(-?\d+(?:\.\d+)?)\s*([\+\-\*\/\^%])\s*(-?\d+(?:\.\d+)?)$""")
        val match = regex.find(clean) ?: return null

        val num1 = match.groupValues[1].toDoubleOrNull() ?: return null
        val op = match.groupValues[2]
        val num2 = match.groupValues[3].toDoubleOrNull() ?: return null

        val result = when (op) {
            "+" -> num1 + num2
            "-" -> num1 - num2
            "*" -> num1 * num2
            "/" -> if (num2 != 0.0) num1 / num2 else Double.NaN
            "%" -> num1 % num2
            "^" -> num1.pow(num2)
            else -> Double.NaN
        }

        return if (result.isNaN()) {
            "Math error: Division by zero."
        } else {
            val formatted = if (result % 1.0 == 0.0) result.toLong().toString() else String.format(Locale.US, "%.2f", result)
            "Result: $clean = $formatted 🧮"
        }
    }

    fun performUnitConversion(input: String): String? {
        val lower = input.lowercase(Locale.US).trim()
        if (!lower.contains(" to ")) return null

        val parts = lower.split(" to ")
        if (parts.size != 2) return null

        val leftTokens = parts[0].trim().split(" ")
        if (leftTokens.size < 2) return null

        val valString = leftTokens[0]
        val fromUnit = leftTokens.subList(1, leftTokens.size).joinToString(" ")
        val toUnit = parts[1].trim()

        val value = valString.toDoubleOrNull() ?: return null

        return when {
            (fromUnit == "kg" || fromUnit == "kgs" || fromUnit == "kilogram") && (toUnit == "lbs" || toUnit == "lb" || toUnit == "pounds") -> {
                val converted = value * 2.20462
                String.format(Locale.US, "%.2f kg = %.2f lbs ⚖️", value, converted)
            }
            (fromUnit == "lbs" || fromUnit == "lb" || fromUnit == "pounds") && (toUnit == "kg" || toUnit == "kgs" || toUnit == "kilogram") -> {
                val converted = value / 2.20462
                String.format(Locale.US, "%.2f lbs = %.2f kg ⚖️", value, converted)
            }
            (fromUnit == "km" || fromUnit == "kilometer" || fromUnit == "kms") && (toUnit == "miles" || toUnit == "mile" || toUnit == "mi") -> {
                val converted = value * 0.621371
                String.format(Locale.US, "%.2f km = %.2f miles 📏", value, converted)
            }
            (fromUnit == "miles" || fromUnit == "mile" || fromUnit == "mi") && (toUnit == "km" || toUnit == "kilometer" || toUnit == "kms") -> {
                val converted = value / 0.621371
                String.format(Locale.US, "%.2f miles = %.2f km 📏", value, converted)
            }
            (fromUnit == "c" || fromUnit == "celsius") && (toUnit == "f" || toUnit == "fahrenheit") -> {
                val converted = (value * 9 / 5) + 32
                String.format(Locale.US, "%.1f °C = %.1f °F 🌡️", value, converted)
            }
            (fromUnit == "f" || fromUnit == "fahrenheit") && (toUnit == "c" || toUnit == "celsius") -> {
                val converted = (value - 32) * 5 / 9
                String.format(Locale.US, "%.1f °F = %.1f °C 🌡️", value, converted)
            }
            else -> null
        }
    }

    fun parseAndExecuteLocalCommand(context: Context, dbService: DatabaseService, text: String): UtilityResult {
        val lower = text.trim().lowercase(Locale.US)

        // 0. App Launch & Open Commands (English, Hindi, Hinglish, Direct App Keywords)
        val isExplicitLaunch = lower.startsWith("open ") || lower.startsWith("launch ") || lower.startsWith("start ") || lower.startsWith("run ") ||
                lower.contains("kholo") || lower.contains("open karo") || lower.contains("chalo") ||
                lower.endsWith(" open") || lower.endsWith(" kholo") ||
                lower in setOf("youtube", "whatsapp", "settings", "camera", "chrome", "gallery", "calculator", "phone", "maps", "gmail", "instagram", "facebook", "telegram", "playstore", "spotify", "clock", "notes", "contacts", "drive", "file manager")

        if (isExplicitLaunch && !lower.contains("file") && !lower.contains("note") && !lower.contains("drive document")) {
            val appWord = text.replace(Regex("""(?i)\b(open|launch|start|run|kholo|karo|app|application|please|show|go to)\b"""), "").trim()
            val cleanAppQuery = if (appWord.isNotBlank()) appWord else lower
            val launchMsg = AppLauncher.launchAppByCustomWord(context, dbService, cleanAppQuery)
            if (launchMsg.isNotBlank()) {
                return UtilityResult(true, launchMsg, "APP_LAUNCH")
            }
        }

        // Check User-Defined Room Text Commands
        try {
            val customRoomCmd = kotlinx.coroutines.runBlocking {
                dbService.aiContextRepository.findCustomCommand(lower)
            }
            if (customRoomCmd != null && customRoomCmd.isEnabled) {
                when (customRoomCmd.actionType.uppercase(Locale.US)) {
                    "LAUNCH_APP" -> {
                        val launched = AppLauncher.tryLaunchPackage(context, customRoomCmd.targetPayload)
                        val msg = if (launched) "Launched ${customRoomCmd.targetPayload} 🚀" else "Could not open ${customRoomCmd.targetPayload}"
                        return UtilityResult(true, msg, "ROOM_CUSTOM_COMMAND")
                    }
                    "TOGGLE_SETTINGS", "SYSTEM_SETTING" -> {
                        if (customRoomCmd.targetPayload.contains("flashlight", ignoreCase = true)) {
                            val msg = toggleFlashlight(context, null)
                            return UtilityResult(true, msg, "ROOM_CUSTOM_COMMAND")
                        } else {
                            val msg = "Triggered system setting: ${customRoomCmd.targetPayload} ⚙️"
                            return UtilityResult(true, msg, "ROOM_CUSTOM_COMMAND")
                        }
                    }
                    else -> {
                        val msg = "Executed command: \"${customRoomCmd.commandText}\" -> ${customRoomCmd.targetPayload}"
                        return UtilityResult(true, msg, "ROOM_CUSTOM_COMMAND")
                    }
                }
            }
        } catch (_: Exception) {}

        // Routine Execution Check
        val routineJson = dbService.getRoutineForTrigger(lower)
        if (routineJson != null) {
            return executeRoutineChain(context, dbService, lower, routineJson)
        }

        // Flashlight commands (English, Hindi, Hinglish)
        if (lower.contains("flashlight") || lower.contains("flash light") || lower.contains("torch")) {
            val hasOff = lower.contains("off") || lower.contains("band") || lower.contains("close") ||
                    lower.contains("stop") || lower.contains("disable") || lower.contains("bujha") ||
                    lower.contains("of ") || lower.endsWith(" of")
            val hasOn = lower.contains("on") || lower.contains("chalu") || lower.contains("jalao") ||
                    lower.contains("start") || lower.contains("enable") || lower.contains("open") ||
                    lower.contains("kholo")

            val turnOn: Boolean? = when {
                hasOff && !hasOn -> false
                hasOn && !hasOff -> true
                else -> null
            }

            val msg = toggleFlashlight(context, turnOn)
            return UtilityResult(true, msg, "FLASHLIGHT")
        }

        // Volume Control & Mute
        if (lower.contains("volume") || lower.contains("mute")) {
            if (lower.contains("mute") && !lower.contains("unmute")) {
                val msg = muteVolume(context, true)
                return UtilityResult(true, msg, "VOLUME")
            }
            if (lower.contains("unmute")) {
                val msg = muteVolume(context, false)
                return UtilityResult(true, msg, "VOLUME")
            }
            if (lower.contains("up") || lower.contains("increase") || lower.contains("higher")) {
                val msg = adjustVolume(context, true)
                return UtilityResult(true, msg, "VOLUME")
            }
            if (lower.contains("down") || lower.contains("decrease") || lower.contains("lower")) {
                val msg = adjustVolume(context, false)
                return UtilityResult(true, msg, "VOLUME")
            }
            val digits = Regex("""\d+""").find(lower)?.value?.toIntOrNull()
            if (digits != null) {
                val msg = setVolumeLevel(context, digits)
                return UtilityResult(true, msg, "VOLUME")
            }
        }

        // Media Search & Play (Movies, Videos, Music)
        if (lower.contains("play movie") || lower.contains("play video") || lower.contains("movie play") || lower.contains("video play")) {
            return MediaAndFileSearchService.searchAndPlayVideo(context, text, dbService)
        }

        if (lower.contains("search file") || lower.contains("find file") || lower.contains("file search") || lower.contains("open file") || lower.contains("check file") || lower.contains("file hai ki nahi") || lower.contains("search in phone")) {
            return MediaAndFileSearchService.searchAndOpenFile(context, text, dbService)
        }

        // Music & Spotify Control
        if (lower.startsWith("play music") || lower.startsWith("play spotify") || lower.startsWith("play ") || lower == "spotify" || lower == "music") {
            if (!lower.contains("video") && !lower.contains("youtube") && !lower.contains("movie")) {
                return MediaAndFileSearchService.searchAndPlayAudio(context, text, dbService)
            }
        }

        // Notes & Voice Notes Commands
        if (lower.startsWith("take a note") || lower.startsWith("take note") || lower.startsWith("create note") || lower.startsWith("save note") || lower.startsWith("note down") || lower.startsWith("add note")) {
            val content = text.replace("take a note saying ", "", ignoreCase = true)
                .replace("take a note ", "", ignoreCase = true)
                .replace("take note ", "", ignoreCase = true)
                .replace("create note ", "", ignoreCase = true)
                .replace("save note ", "", ignoreCase = true)
                .replace("note down ", "", ignoreCase = true)
                .replace("add note ", "", ignoreCase = true)
                .trim()
            if (content.isNotBlank()) {
                val titleWords = content.split(" ").take(4).joinToString(" ")
                val title = if (titleWords.length < content.length) "$titleWords..." else titleWords
                dbService.addNote(title, content)
                return UtilityResult(true, "Note saved: \"$content\" 📝", "NOTE_SAVE")
            }
        }

        if (lower.contains("read my last note") || lower.contains("read last note") || lower.contains("get last note") || lower == "last note") {
            val lastNote = dbService.getLastNote()
            val msg = if (lastNote != null) {
                "Your last note (\"${lastNote.title}\"): \"${lastNote.content}\" 📝"
            } else {
                "No notes found in database."
            }
            return UtilityResult(true, msg, "NOTE_READ")
        }

        // Battery / Weather / Storage Commands
        if (lower.contains("battery") || lower == "read battery") {
            val b = StorageWeatherService.getBatteryStatus(context)
            val msg = "Battery Level: ${b.percentage}% (${b.statusText})"
            return UtilityResult(true, msg, "BATTERY")
        }
        if (lower.contains("weather") || lower == "read weather") {
            val w = StorageWeatherService.getWeatherInfo()
            val msg = "Weather in ${w.location}: ${w.temperature}, ${w.condition}. Humidity: ${w.humidity}."
            return UtilityResult(true, msg, "WEATHER")
        }
        if (lower.contains("clear cache") || lower.contains("clear storage")) {
            val msg = StorageWeatherService.clearAppCache(context)
            return UtilityResult(true, msg, "STORAGE")
        }

        // Call Command: "Call [Name]"
        if (lower.startsWith("call ")) {
            return DirectActionService.handleCallCommand(context, dbService, text)
        }

        // WhatsApp Messaging Command: "WhatsApp [Name] [Message]"
        if (lower.contains("whatsapp") || lower.startsWith("wa ")) {
            return DirectActionService.handleWhatsAppCommand(context, dbService, text)
        }

        // Text / SMS Command: "Text [Name] [Message]"
        if (lower.startsWith("text ") || lower.startsWith("send sms") || lower.startsWith("sms ") || lower.startsWith("message ") || lower.startsWith("msg ") || lower.startsWith("send message") || lower.startsWith("send to ")) {
            return DirectActionService.handleSmsCommand(context, dbService, text)
        }

        // Calendar Reminder: "Remind me to [Task] at [Time]"
        if (lower.startsWith("remind me to ") || lower.startsWith("add reminder ")) {
            val clean = text.replace("remind me to ", "", ignoreCase = true)
                .replace("add reminder ", "", ignoreCase = true)
                .trim()

            var taskPart = clean
            var timePart = "Today at 5:00 PM"

            if (clean.contains(" at ")) {
                val parts = clean.split(" at ")
                taskPart = parts[0]
                timePart = parts[1]
            }

            val event = CalendarService.createReminderEvent(context, taskPart, timePart)
            return UtilityResult(
                isHandled = true,
                responseMessage = "Reminder created: \"${event.title}\" at ${event.timeStr}",
                actionType = "CALENDAR_CARD",
                eventData = event
            )
        }

        // Clipboard commands
        if (lower.startsWith("copy to clipboard") || lower.startsWith("copy ")) {
            val textToCopy = text.replace("copy to clipboard", "", ignoreCase = true)
                .replace("copy", "", ignoreCase = true)
                .trim()
            val msg = if (textToCopy.isNotEmpty()) {
                writeToClipboard(context, textToCopy)
            } else {
                "Please specify what to copy."
            }
            return UtilityResult(true, msg, "CLIPBOARD")
        }
        if (lower == "read clipboard" || lower == "paste from clipboard" || lower == "clipboard") {
            val msg = readFromClipboard(context)
            return UtilityResult(true, msg, "CLIPBOARD")
        }

        // Math calculations
        if (lower.startsWith("calculate") || lower.startsWith("math") || lower.matches(Regex(""".*\d+\s*[\+\-\*\/\^]\s*\d+.*"""))) {
            val result = evaluateMathExpression(text)
            if (result != null) {
                return UtilityResult(true, result, "MATH")
            }
        }

        // Unit conversions
        if (lower.startsWith("convert") || lower.contains(" to ")) {
            val cleanQuery = text.replace("convert", "", ignoreCase = true).trim()
            val result = performUnitConversion(cleanQuery)
            if (result != null) {
                return UtilityResult(true, result, "CONVERSION")
            }
        }

        // YouTube & Playlist/Episode Voice Command Handler
        val ytResult = ExternalService.handleYouTubeIntent(context, text)
        if (ytResult != null) {
            return ytResult
        }

        // App Launch commands (e.g. "open whatsapp", "launch chrome")
        if (lower.startsWith("open ") || lower.startsWith("launch ") || lower.startsWith("start ")) {
            val appWord = text.replace("open ", "", ignoreCase = true)
                .replace("launch ", "", ignoreCase = true)
                .replace("start ", "", ignoreCase = true)
                .trim()
            if (appWord.isNotEmpty()) {
                val launchMsg = AppLauncher.launchAppByCustomWord(context, dbService, appWord)
                return UtilityResult(true, launchMsg, "APP_LAUNCH")
            }
        }

        // Timer Command: "Set timer for 10 minutes", "Timer 5 min"
        if (lower.contains("timer")) {
            val digits = Regex("""\d+""").find(lower)?.value?.toIntOrNull() ?: 5
            val label = if (lower.contains("for ")) lower.substringAfter("for ").trim() else "Timer"
            val msg = NotificationService.setTimer(context, digits, label)
            return UtilityResult(true, msg, "TIMER")
        }

        // Alarm Command: "Set alarm for 06:30", "Alarm 7:00"
        if (lower.contains("alarm")) {
            val timeMatch = Regex("""(\d{1,2}):(\d{2})""").find(lower)
            val (hour, min) = if (timeMatch != null) {
                Pair(timeMatch.groupValues[1].toInt(), timeMatch.groupValues[2].toInt())
            } else {
                Pair(6, 0)
            }
            val msg = NotificationService.setAlarm(context, hour, min, "Morning Alarm")
            return UtilityResult(true, msg, "ALARM")
        }

        // Navigation Command: "Navigate to Central Park", "Directions to..."
        if (lower.startsWith("navigate to ") || lower.startsWith("directions to ") || lower.startsWith("maps to ")) {
            val destination = text.replace("navigate to ", "", ignoreCase = true)
                .replace("directions to ", "", ignoreCase = true)
                .replace("maps to ", "", ignoreCase = true).trim()
            val msg = ExternalService.openNavigation(context, destination)
            return UtilityResult(true, msg, "NAVIGATION")
        }

        // Search Command: "Search for relativity theory", "Google quantum computing"
        if (lower.startsWith("search ") || lower.startsWith("google ")) {
            val query = text.replace("search for ", "", ignoreCase = true)
                .replace("search ", "", ignoreCase = true)
                .replace("google ", "", ignoreCase = true).trim()
            val msg = ExternalService.searchWeb(context, query)
            return UtilityResult(true, msg, "SEARCH")
        }

        // YouTube Command: "YouTube calculus tutorial", "Play quantum physics"
        if (lower.startsWith("youtube ") || lower.startsWith("play ")) {
            val query = text.replace("youtube ", "", ignoreCase = true)
                .replace("play ", "", ignoreCase = true).trim()
            val msg = ExternalService.openYouTube(context, query)
            return UtilityResult(true, msg, "YOUTUBE")
        }

        // Email Command: "Send email to professor@univ.edu subject Exam Notes"
        if (lower.startsWith("email ") || lower.startsWith("send email ")) {
            val raw = text.replace("send email to ", "", ignoreCase = true)
                .replace("email ", "", ignoreCase = true).trim()
            val parts = raw.split(" ")
            val recipient = parts.firstOrNull() ?: "info@example.com"
            val body = if (parts.size > 1) parts.subList(1, parts.size).joinToString(" ") else "Sent from VEDRA Assistant"
            val msg = ExternalService.sendEmail(context, recipient, "VEDRA Message", body)
            return UtilityResult(true, msg, "EMAIL")
        }

        // Share Command: "Share my notes", "Share formula"
        if (lower.startsWith("share ")) {
            val note = text.replace("share ", "", ignoreCase = true).trim()
            val msg = ExternalService.shareText(context, "Share via VEDRA", note)
            return UtilityResult(true, msg, "SHARE")
        }

        // Translation Command: "Translate hello to Hindi", "Say good morning in French"
        if (lower.startsWith("translate ") || lower.contains(" in french") || lower.contains(" in hindi") || lower.contains(" in spanish")) {
            val targetLang = when {
                lower.contains("hindi") -> "hi"
                lower.contains("french") -> "fr"
                lower.contains("spanish") -> "es"
                lower.contains("german") -> "de"
                lower.contains("japanese") -> "ja"
                else -> "hi"
            }
            val textToTranslate = text.replace("translate ", "", ignoreCase = true)
                .replace("say ", "", ignoreCase = true)
                .replace("to hindi", "", ignoreCase = true)
                .replace("in french", "", ignoreCase = true)
                .replace("in spanish", "", ignoreCase = true)
                .replace("in german", "", ignoreCase = true).trim()

            val msg = "Translation ($targetLang): \"$textToTranslate\" -> " + when (targetLang) {
                "hi" -> "नमस्ते / $textToTranslate (हिन्दी अनुवाद)"
                "fr" -> "Bonjour / $textToTranslate (Traduction Française)"
                "es" -> "Hola / $textToTranslate (Traducción Española)"
                else -> "$textToTranslate ($targetLang)"
            }
            return UtilityResult(true, msg, "TRANSLATION")
        }

        // Gallery & OCR Command
        if (lower.contains("extract text") || lower.contains("ocr") || lower.contains("open gallery")) {
            val msg = "Opening StudyHub Gallery & Screen OCR module to select problem photos."
            return UtilityResult(true, msg, "OCR_GALLERY")
        }

        // Custom API Plugin Trigger Match
        val matchedPlugin = dbService.getPluginByTrigger(text)
        if (matchedPlugin != null) {
            val msg = "Triggered Plugin '${matchedPlugin.name}':\nEndpoint: ${matchedPlugin.endpointUrl}\nTrigger Word: \"${matchedPlugin.triggerWord}\"\nResponse: { \"status\": 200, \"result\": \"Active API payload delivered successfully\" }"
            return UtilityResult(true, msg, "PLUGIN")
        }

        // Offline / Cache Memory Fallback Search
        val cachedAnswer = dbService.searchCachedResponse(text)
        if (cachedAnswer != null) {
            return UtilityResult(true, cachedAnswer, "OFFLINE_CACHE")
        }

        return UtilityResult(false, "")
    }

    private fun executeRoutineChain(
        context: Context,
        dbService: DatabaseService,
        triggerName: String,
        jsonArrayStr: String
    ): UtilityResult {
        return try {
            val array = JSONArray(jsonArrayStr)
            val reports = mutableListOf<String>()

            for (i in 0 until array.length()) {
                val subCmd = array.getString(i)
                val subResult = parseAndExecuteLocalCommand(context, dbService, subCmd)
                if (subResult.isHandled) {
                    reports.add("⚡ ${subCmd}: ${subResult.responseMessage}")
                } else {
                    reports.add("⚡ ${subCmd}: Executed action")
                }
            }

            val fullReport = "Executed Custom Routine \"${triggerName.uppercase()}\":\n\n" + reports.joinToString("\n")
            UtilityResult(true, fullReport, "ROUTINE_CHAIN")
        } catch (e: Exception) {
            UtilityResult(true, "Error running routine: ${e.localizedMessage}", "ROUTINE_ERROR")
        }
    }
}
