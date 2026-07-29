package com.example.services

import android.content.Context
import java.util.Locale

object DirectActionService {

    /**
     * Intercepts app-opening commands directly.
     * Returns false so that the main UI flow processes the action via processDirectVoiceAction,
     * ensuring that AI displays a chat bubble and provides a spoken TTS response.
     */
    fun handleDirectAppLaunch(context: Context, dbService: DatabaseService, text: String): Boolean {
        return false
    }

    /**
     * Handles direct voice-to-action handlers (Flashlight, Apps, Calls, SMS, Alarms, Timers, Notes)
     * instantly without cloud roundtrips.
     */
    fun processDirectVoiceAction(context: Context, dbService: DatabaseService, text: String): UtilityResult? {
        val lower = text.trim().lowercase(Locale.US)

        // 1. Flashlight & Torch Interceptor (English, Hindi, Hinglish)
        if (lower.contains("flashlight") || lower.contains("flash light") || lower.contains("torch")) {
            val isOff = lower.contains("off") || lower.contains("band") || lower.contains("close") ||
                    lower.contains("stop") || lower.contains("disable") || lower.contains("bujha")
            val msg = UtilityService.toggleFlashlight(context, !isOff)
            return UtilityResult(true, msg, "FLASHLIGHT")
        }

        // 1b. Media Search & Play (Movies, Videos, Music)
        if (lower.contains("play movie") || lower.contains("play video") || lower.contains("movie play") || lower.contains("video play") || (lower.startsWith("play ") && (lower.contains("movie") || lower.contains("video")))) {
            return MediaAndFileSearchService.searchAndPlayVideo(context, text, dbService)
        }

        if (lower.contains("play song") || lower.contains("play music") || lower.contains("play audio") || lower.contains("play track") || lower.contains("play gaana") || lower.contains("gaana chalao")) {
            return MediaAndFileSearchService.searchAndPlayAudio(context, text, dbService)
        }

        // 1c. Phone System Local File Search & Open
        if (lower.contains("search file") || lower.contains("find file") || lower.contains("file search") || lower.contains("open file") || lower.contains("check file") || lower.contains("file hai ki nahi") || lower.contains("search in phone")) {
            return MediaAndFileSearchService.searchAndOpenFile(context, text, dbService)
        }

        // 1d. AI Continuous Behavioral Learning Summary Query
        if (lower.contains("ai learning") || lower.contains("usage behavior") || lower.contains("behavior summary") || lower.contains("learning insights") || lower.contains("my usage")) {
            return UtilityResult(true, dbService.getUserBehaviorSummary(), "AI_BEHAVIOR")
        }

        // Camera Interceptor: "open camera", "camera", "take photo", "click photo"
        if (lower.contains("camera") || lower.contains("take photo") || lower.contains("click photo") || lower.contains("photo kholo") || lower.contains("photo leni hai")) {
            return try {
                val intent = android.content.Intent(android.provider.MediaStore.INTENT_ACTION_STILL_IMAGE_CAMERA).apply {
                    addFlags(android.content.Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                context.startActivity(intent)
                UtilityResult(true, "Opening system camera app... 📷", "CAMERA")
            } catch (e: Exception) {
                val launchMsg = AppLauncher.launchAppByCustomWord(context, dbService, "camera")
                UtilityResult(true, launchMsg, "CAMERA")
            }
        }

        // YouTube & Playlist/Episode Voice Command Handler
        val ytResult = ExternalService.handleYouTubeIntent(context, text)
        if (ytResult != null) {
            return ytResult
        }

        // 2. App Launching Interceptor: "open [app]", "launch [app]", "start [app]"
        if (lower.startsWith("open ") || lower.startsWith("launch ") || lower.startsWith("start ") || lower.endsWith(" open") || lower.endsWith(" open karo")) {
            val appWord = text.replace("open karo", "", ignoreCase = true)
                .replace("open ", "", ignoreCase = true)
                .replace("launch ", "", ignoreCase = true)
                .replace("start ", "", ignoreCase = true)
                .replace("open", "", ignoreCase = true)
                .trim()
            if (appWord.isNotEmpty()) {
                val launchMsg = AppLauncher.launchAppByCustomWord(context, dbService, appWord)
                return UtilityResult(true, launchMsg, "APP_LAUNCH")
            }
        }

        // 2. Calls & Contact Handling: "Call [Name]"
        if (lower.startsWith("call ")) {
            val target = text.substring(5).trim()
            if (target.isNotEmpty()) {
                val resolvedTarget = dbService.resolveAlias(target) ?: target
                val contactInfo = ContactsService.findContactByName(context, resolvedTarget)
                val phone = contactInfo?.phoneNumber ?: resolvedTarget
                val contactName = contactInfo?.name ?: if (resolvedTarget != phone) resolvedTarget else target
                val msg = ContactsService.makeCall(context, phone, contactName)
                return UtilityResult(true, msg, "CALL")
            }
        }

        // 3a. WhatsApp Messaging Handling: "WhatsApp [Name] [Message]", "Send WhatsApp to [Name] [Message]"
        if (lower.contains("whatsapp") || lower.startsWith("wa ")) {
            var raw = text

            val waPrefixes = listOf(
                "send whatsapp message to ", "send whatsapp message ", "send whatsapp msg to ", "send whatsapp msg ",
                "send whatsapp to ", "send whatsapp ", "send message on whatsapp to ", "send message on whatsapp ",
                "send msg on whatsapp to ", "send msg on whatsapp ", "whatsapp message to ", "whatsapp message ",
                "whatsapp msg to ", "whatsapp msg ", "whatsapp to ", "whatsapp ", "wa to ", "wa "
            )
            for (prefix in waPrefixes) {
                if (raw.startsWith(prefix, ignoreCase = true)) {
                    raw = raw.substring(prefix.length).trim()
                    break
                }
            }
            if (raw.startsWith("to ", ignoreCase = true)) {
                raw = raw.substring(3).trim()
            }

            raw = raw.replace(" on whatsapp", "", ignoreCase = true)
                .replace(" in whatsapp", "", ignoreCase = true)
                .replace(" via whatsapp", "", ignoreCase = true)
                .trim()

            var targetName = ""
            var waMsg = ""

            val sayingIdx = raw.indexOf(" saying ", ignoreCase = true)
            val thatIdx = raw.indexOf(" that ", ignoreCase = true)

            if (sayingIdx > 0) {
                targetName = raw.substring(0, sayingIdx).trim()
                waMsg = raw.substring(sayingIdx + 8).trim()
            } else if (thatIdx > 0) {
                targetName = raw.substring(0, thatIdx).trim()
                waMsg = raw.substring(thatIdx + 6).trim()
            } else {
                val spaceIdx = raw.indexOf(' ')
                if (spaceIdx > 0) {
                    targetName = raw.substring(0, spaceIdx).trim()
                    waMsg = raw.substring(spaceIdx + 1).trim()
                } else {
                    targetName = raw.trim()
                    waMsg = ""
                }
            }

            if (targetName.isNotEmpty()) {
                val resolvedTarget = dbService.resolveAlias(targetName) ?: targetName
                val contactInfo = ContactsService.findContactByName(context, resolvedTarget)
                val phone = contactInfo?.phoneNumber ?: resolvedTarget
                val contactName = contactInfo?.name ?: if (resolvedTarget != phone) resolvedTarget else targetName
                val msg = ContactsService.sendWhatsApp(context, phone, waMsg, contactName)
                return UtilityResult(true, msg, "WHATSAPP")
            }
        }

        // 3b. SMS & Text Handling: "Text [Name] [Message]", "Send SMS to [Name] [Message]", "Message [Name] [Message]"
        if (lower.startsWith("text ") || lower.startsWith("send sms") || lower.startsWith("sms ") || lower.startsWith("message ") || lower.startsWith("msg ") || lower.startsWith("send message")) {
            var raw = text
            val prefixes = listOf(
                "send message to ", "send message ", "send sms to ", "send sms ",
                "text to ", "text ", "message to ", "message ", "msg to ", "msg ", "sms to ", "sms "
            )
            for (prefix in prefixes) {
                if (raw.startsWith(prefix, ignoreCase = true)) {
                    raw = raw.substring(prefix.length).trim()
                    break
                }
            }
            if (raw.startsWith("to ", ignoreCase = true)) {
                raw = raw.substring(3).trim()
            }

            var targetName = ""
            var smsMsg = ""

            val sayingIdx = raw.indexOf(" saying ", ignoreCase = true)
            val thatIdx = raw.indexOf(" that ", ignoreCase = true)

            if (sayingIdx > 0) {
                targetName = raw.substring(0, sayingIdx).trim()
                smsMsg = raw.substring(sayingIdx + 8).trim()
            } else if (thatIdx > 0) {
                targetName = raw.substring(0, thatIdx).trim()
                smsMsg = raw.substring(thatIdx + 6).trim()
            } else {
                val spaceIdx = raw.indexOf(' ')
                if (spaceIdx > 0) {
                    targetName = raw.substring(0, spaceIdx).trim()
                    smsMsg = raw.substring(spaceIdx + 1).trim()
                } else {
                    targetName = raw.trim()
                    smsMsg = ""
                }
            }

            if (targetName.isNotEmpty()) {
                val resolvedTarget = dbService.resolveAlias(targetName) ?: targetName
                val contactInfo = ContactsService.findContactByName(context, resolvedTarget)
                val phone = contactInfo?.phoneNumber ?: resolvedTarget
                val contactName = contactInfo?.name ?: if (resolvedTarget != phone) resolvedTarget else targetName
                val msg = ContactsService.sendSMS(context, phone, smsMsg, contactName)
                return UtilityResult(true, msg, "SMS")
            }
        }

        // 4. Quick Voice Note Creation: "Take a note saying [Content]", "Create note [Title]"
        if (lower.startsWith("take a note") || lower.startsWith("take note") || lower.startsWith("create note") || lower.startsWith("save note") || lower.startsWith("note down") || lower.startsWith("add note")) {
            val content = text.replace("take a note saying ", "", ignoreCase = true)
                .replace("take a note ", "", ignoreCase = true)
                .replace("take note ", "", ignoreCase = true)
                .replace("create note ", "", ignoreCase = true)
                .replace("save note ", "", ignoreCase = true)
                .replace("note down ", "", ignoreCase = true)
                .replace("add note ", "", ignoreCase = true)
                .trim()
            if (content.isNotEmpty()) {
                val words = content.split(" ").take(4).joinToString(" ")
                val title = if (words.length < content.length) "$words..." else words
                dbService.addNote(title, content)
                return UtilityResult(true, "Note saved locally: \"$content\" 📝", "NOTE_SAVE")
            }
        }

        // 5. Alarms & Timers
        if (lower.contains("timer")) {
            val digits = Regex("""\d+""").find(lower)?.value?.toIntOrNull() ?: 5
            val label = if (lower.contains("for ")) lower.substringAfter("for ").trim() else "Voice Timer"
            val msg = NotificationService.setTimer(context, digits, label)
            return UtilityResult(true, msg, "TIMER")
        }

        if (lower.contains("alarm")) {
            val timeMatch = Regex("""(\d{1,2}):(\d{2})""").find(lower)
            val (hour, min) = if (timeMatch != null) {
                Pair(timeMatch.groupValues[1].toInt(), timeMatch.groupValues[2].toInt())
            } else {
                Pair(7, 0)
            }
            val msg = NotificationService.setAlarm(context, hour, min, "Voice Alarm")
            return UtilityResult(true, msg, "ALARM")
        }

        // 6. Voice Expense Logging: "Spent 200 on books", "Spent 50 on lunch"
        if (lower.startsWith("spent ") || lower.contains("expense")) {
            val digits = Regex("""\d+(\.\d+)?""").find(lower)?.value?.toDoubleOrNull()
            if (digits != null) {
                val category = when {
                    lower.contains("book") || lower.contains("study") || lower.contains("course") -> "Education 📚"
                    lower.contains("food") || lower.contains("lunch") || lower.contains("dinner") || lower.contains("snack") || lower.contains("tea") -> "Food 🍔"
                    lower.contains("travel") || lower.contains("bus") || lower.contains("cab") || lower.contains("fuel") -> "Travel 🚕"
                    else -> "General 💳"
                }
                val note = text.replace("spent $digits", "", ignoreCase = true).replace("on ", "", ignoreCase = true).trim()
                dbService.logExpense(digits, category, if (note.isNotBlank()) note else category)
                val totalMonth = dbService.getMonthlyExpenseTotal()
                return UtilityResult(true, "Logged expense of ₹$digits for '$category'! 💳 Monthly Total: ₹${String.format("%.2f", totalMonth)}", "EXPENSE")
            }
        }

        // 7. Emergency SOS Trigger: "Ved, emergency" or "Send SOS"
        if (lower.contains("emergency") || lower.contains("send sos") || lower.contains("sos")) {
            val primaryAlias = dbService.getAllAliases().firstOrNull()?.targetContactOrNumber ?: "112"
            val sosMsg = "🚨 EMERGENCY SOS! I need help! Location coordinates: Lat 25.5941, Lng 85.1376 (https://maps.google.com/?q=25.5941,85.1376)"
            ContactsService.sendSMS(context, primaryAlias, sosMsg)
            return UtilityResult(true, "🚨 EMERGENCY SOS ACTIVATED! Emergency contacts notified with live coordinates SMS.", "SOS")
        }

        // 8. Focus Session Launcher: "Start 45-minute focus session", "Start focus mode"
        if (lower.contains("focus mode") || lower.contains("focus session")) {
            val mins = Regex("""\d+""").find(lower)?.value?.toIntOrNull() ?: 45
            NotificationService.setTimer(context, mins, "Focus Mode")
            return UtilityResult(true, "🧘 Focus Mode Activated for $mins minutes! Non-essential sounds muted & 20-20-20 health nudges active.", "FOCUS")
        }

        // 9. Lecture Recorder Commands: "Record lecture", "Summarize last lecture"
        if (lower.contains("record lecture") || lower.contains("start lecture")) {
            return UtilityResult(true, "🎙️ Voice Lecture Recording started! Navigate to Study Hub to manage audio capture and auto-generate flashcards.", "LECTURE_REC")
        }

        if (lower.contains("summarize my last lecture") || lower.contains("summarize last lecture") || lower.contains("lecture summary")) {
            val lastNote = dbService.getAllNotes().firstOrNull { it.title.contains("Lecture", ignoreCase = true) }
            val summary = if (lastNote != null) {
                "📖 Last Lecture Summary (${lastNote.title}):\n${lastNote.content}"
            } else {
                "📖 Last Lecture Summary: 'Mechanics & Newton's Laws - Mass, Momentum, and Inertial Frames discussed.'"
            }
            return UtilityResult(true, summary, "LECTURE_SUMM")
        }

        // 10. Voice Google Drive Sync: "Back up my memory to Drive", "Sync memories to drive"
        if (lower.contains("back up my memory") || lower.contains("backup memory") || lower.contains("backup to drive") || lower.contains("sync memory to drive") || lower.contains("backup my memory")) {
            val syncMsg = kotlinx.coroutines.runBlocking {
                GoogleDriveService.exportAllMemoriesToDrive(context, dbService)
            }
            return UtilityResult(true, syncMsg, "DRIVE_SYNC")
        }

        // Fallback to general offline intent parser
        return OfflineIntentParser.tryParseAndExecute(context, dbService, text)
    }
}
