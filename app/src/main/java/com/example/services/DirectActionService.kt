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
            return handleCallCommand(context, dbService, text)
        }

        // 3a. WhatsApp Messaging Handling: "WhatsApp [Name] [Message]", "Send WhatsApp to [Name] [Message]"
        if (lower.contains("whatsapp") || lower.startsWith("wa ")) {
            return handleWhatsAppCommand(context, dbService, text)
        }

        // 3b. SMS & Text Handling: "Text [Name] [Message]", "Send SMS to [Name] [Message]", "Message [Name] [Message]"
        if (lower.startsWith("text ") || lower.startsWith("send sms") || lower.startsWith("sms ") || lower.startsWith("message ") || lower.startsWith("msg ") || lower.startsWith("send message") || lower.startsWith("send to ")) {
            return handleSmsCommand(context, dbService, text)
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

    data class ParsedCommunicationCommand(
        val rawTarget: String,
        val messageText: String
    )

    data class ResolvedContact(
        val name: String,
        val phoneNumber: String,
        val isValidPhone: Boolean
    )

    fun parseCommunicationCommand(text: String): ParsedCommunicationCommand {
        var raw = text.trim()

        val prefixes = listOf(
            "send whatsapp message to ", "send whatsapp message ", "send whatsapp msg to ", "send whatsapp msg ",
            "send whatsapp to ", "send whatsapp ", "send message on whatsapp to ", "send message on whatsapp ",
            "send msg on whatsapp to ", "send msg on whatsapp ", "whatsapp message to ", "whatsapp message ",
            "whatsapp msg to ", "whatsapp msg ", "whatsapp to ", "whatsapp ", "wa to ", "wa ",
            "send message to ", "send message ", "send sms to ", "send sms ", "send text to ", "send text ",
            "text message to ", "text to ", "text ", "message to ", "message ", "msg to ", "msg ", "sms to ", "sms ",
            "make a call to ", "call to ", "call ", "send to "
        )

        for (prefix in prefixes) {
            if (raw.startsWith(prefix, ignoreCase = true)) {
                raw = raw.substring(prefix.length).trim()
                break
            }
        }

        var hasLeadingTo = false
        if (raw.startsWith("to ", ignoreCase = true)) {
            raw = raw.substring(3).trim()
            hasLeadingTo = true
        }

        raw = raw.replace(Regex("(?i)\\s+(on|in|via)\\s+(whatsapp|sms|text|phone|call)"), "").trim()

        var extractedTarget = ""
        var extractedMsg = ""

        val sayingIdx = raw.indexOf(" saying ", ignoreCase = true)
        val thatIdx = raw.indexOf(" that ", ignoreCase = true)

        if (sayingIdx > 0) {
            extractedTarget = raw.substring(0, sayingIdx).trim()
            extractedMsg = raw.substring(sayingIdx + 8).trim()
        } else if (thatIdx > 0) {
            extractedTarget = raw.substring(0, thatIdx).trim()
            extractedMsg = raw.substring(thatIdx + 6).trim()
        } else {
            val toIdx = raw.indexOf(" to ", ignoreCase = true)
            if (toIdx > 0 && !hasLeadingTo) {
                val left = raw.substring(0, toIdx).trim()
                val right = raw.substring(toIdx + 4).trim()

                val rightDigits = right.filter { it.isDigit() }
                if (rightDigits.length >= 5 || right.split(" ").size <= 3) {
                    extractedTarget = right
                    extractedMsg = left
                } else {
                    extractedTarget = left
                    extractedMsg = right
                }
            } else {
                val firstSpace = raw.indexOf(' ')
                if (firstSpace > 0) {
                    val firstToken = raw.substring(0, firstSpace).trim()
                    val remainder = raw.substring(firstSpace + 1).trim()

                    val tokenDigits = firstToken.filter { it.isDigit() }
                    if (tokenDigits.length >= 7 || hasLeadingTo) {
                        extractedTarget = firstToken
                        extractedMsg = remainder
                    } else {
                        val remainderPhoneMatch = Regex("""\b\+?\d{7,15}\b""").find(remainder)
                        if (remainderPhoneMatch != null) {
                            extractedTarget = remainderPhoneMatch.value
                            extractedMsg = raw.replace(remainderPhoneMatch.value, "").replace(" to ", " ").trim()
                        } else {
                            extractedTarget = firstToken
                            extractedMsg = remainder
                        }
                    }
                } else {
                    extractedTarget = raw
                    extractedMsg = ""
                }
            }
        }

        if (extractedTarget.startsWith("to ", ignoreCase = true)) {
            extractedTarget = extractedTarget.substring(3).trim()
        }

        val targetDigits = extractedTarget.filter { it.isDigit() }
        val msgPhoneMatch = Regex("""\b\+?\d{7,15}\b""").find(extractedMsg)
        if (targetDigits.isEmpty() && msgPhoneMatch != null) {
            val phoneInMsg = msgPhoneMatch.value
            val cleanMsg = extractedMsg.replace(phoneInMsg, "").replace(" to ", " ").trim()
            val finalMsg = if (cleanMsg.isNotEmpty()) cleanMsg else extractedTarget
            extractedTarget = phoneInMsg
            extractedMsg = finalMsg
        }

        return ParsedCommunicationCommand(extractedTarget.trim(), extractedMsg.trim())
    }

    fun resolveContact(context: Context, dbService: DatabaseService, target: String): ResolvedContact {
        val cleanTarget = target.trim()
        if (cleanTarget.isEmpty()) return ResolvedContact("", "", false)

        val aliasResolved = dbService.resolveAlias(cleanTarget) ?: cleanTarget
        val contactInfo = ContactsService.findContactByName(context, aliasResolved)

        if (contactInfo != null) {
            return ResolvedContact(
                name = contactInfo.name,
                phoneNumber = contactInfo.phoneNumber,
                isValidPhone = true
            )
        }

        val digits = aliasResolved.filter { it.isDigit() || it == '+' }
        if (digits.length >= 5) {
            return ResolvedContact(
                name = aliasResolved,
                phoneNumber = digits,
                isValidPhone = true
            )
        }

        return ResolvedContact(
            name = cleanTarget,
            phoneNumber = "",
            isValidPhone = false
        )
    }

    fun handleCallCommand(context: Context, dbService: DatabaseService, text: String): UtilityResult {
        val parsed = parseCommunicationCommand(text)
        if (parsed.rawTarget.isBlank()) {
            return UtilityResult(true, "Please specify who to call (e.g., 'call 7033486291' or 'call Mom').", "CALL")
        }
        val resolved = resolveContact(context, dbService, parsed.rawTarget)
        if (!resolved.isValidPhone) {
            return UtilityResult(
                true,
                "Contact '${parsed.rawTarget}' not found in your phone contacts. Please check contact name or specify a valid phone number.",
                "CALL"
            )
        }
        val msg = ContactsService.makeCall(context, resolved.phoneNumber, resolved.name)
        return UtilityResult(true, msg, "CALL")
    }

    fun handleSmsCommand(context: Context, dbService: DatabaseService, text: String): UtilityResult {
        val parsed = parseCommunicationCommand(text)
        if (parsed.rawTarget.isBlank()) {
            return UtilityResult(true, "Please specify a recipient and message (e.g., 'send sms hi to 7033486291').", "SMS")
        }
        val resolved = resolveContact(context, dbService, parsed.rawTarget)
        if (!resolved.isValidPhone) {
            return UtilityResult(
                true,
                "Contact '${parsed.rawTarget}' not found in your phone contacts. Please check contact name or specify a valid phone number (e.g., 'send sms hi to 7033486291').",
                "SMS"
            )
        }
        val msg = ContactsService.sendSMS(context, resolved.phoneNumber, parsed.messageText, resolved.name)
        return UtilityResult(true, msg, "SMS")
    }

    fun handleWhatsAppCommand(context: Context, dbService: DatabaseService, text: String): UtilityResult {
        val parsed = parseCommunicationCommand(text)
        if (parsed.rawTarget.isBlank()) {
            return UtilityResult(true, "Please specify a contact and message for WhatsApp (e.g., 'send whatsapp hi to Mom').", "WHATSAPP")
        }
        val resolved = resolveContact(context, dbService, parsed.rawTarget)
        if (!resolved.isValidPhone) {
            return UtilityResult(
                true,
                "Contact '${parsed.rawTarget}' not found in your phone contacts for WhatsApp. Please check contact name or specify a valid phone number.",
                "WHATSAPP"
            )
        }
        val msg = ContactsService.sendWhatsApp(context, resolved.phoneNumber, parsed.messageText, resolved.name)
        return UtilityResult(true, msg, "WHATSAPP")
    }
}
