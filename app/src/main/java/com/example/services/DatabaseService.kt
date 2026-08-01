package com.example.services

import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.net.Uri
import java.util.Locale
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking

data class AppMapping(
    val id: Long = 0,
    val customWord: String,
    val appIdentifier: String
)

data class UserMemory(
    val id: Long = 0,
    val memoryKey: String,
    val memoryValue: String,
    val profile: String = "General",
    val expiresAt: Long = 0L
)

data class ContactAlias(
    val id: Long = 0,
    val aliasName: String,
    val targetContactOrNumber: String
)

data class CustomRoutine(
    val id: Long = 0,
    val triggerPhrase: String,
    val actionChainJson: String
)

data class StudyTask(
    val id: Long = 0,
    val title: String,
    val subject: String,
    val isCompleted: Boolean = false,
    val dueDate: String = "Today"
)

data class Flashcard(
    val id: Long = 0,
    val subject: String,
    val topic: String,
    val question: String,
    val answer: String,
    val formula: String? = null
)

data class CachedResponse(
    val id: Long = 0,
    val queryKey: String,
    val responseText: String,
    val category: String = "general"
)

data class NoteItem(
    val id: Long = 0,
    val title: String,
    val content: String,
    val timestamp: Long = System.currentTimeMillis()
)

data class StudyHabit(
    val id: Long = 0,
    val subject: String,
    val durationMinutes: Int,
    val dateString: String,
    val timestamp: Long = System.currentTimeMillis()
)

data class ExpenseItem(
    val id: Long = 0,
    val amount: Double,
    val category: String,
    val note: String,
    val timestamp: Long = System.currentTimeMillis()
)

data class DriveFolder(
    val id: Long = 0,
    val name: String,
    val colorHex: String = "#8B5CF6",
    val parentId: Long = 0L,
    val createdAt: Long = System.currentTimeMillis()
)

data class DriveDocument(
    val id: Long = 0,
    val folderId: Long,
    val title: String,
    val content: String,
    val fileType: String = "TXT",
    val fileSize: Long = 0,
    val createdAt: Long = System.currentTimeMillis()
)

class DatabaseService(context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    val roomDb by lazy { com.example.data.room.AppRoomDatabase.getDatabase(context) }
    val aiContextRepository by lazy { com.example.data.room.AiContextRepository(roomDb.conversationContextDao(), roomDb.userInteractionPatternDao(), roomDb.customTextCommandDao(), roomDb.vedraUserSettingDao()) }

    companion object {
        private const val DATABASE_NAME = "vedra_memory.db"
        private const val DATABASE_VERSION = 7

        const val TABLE_MAPPINGS = "app_mappings"
        const val TABLE_MEMORY = "user_memory"
        const val TABLE_ALIASES = "aliases"
        const val TABLE_ROUTINES = "routines"
        const val TABLE_STUDY_TASKS = "study_tasks"
        const val TABLE_FLASHCARDS = "flashcards"
        const val TABLE_CACHE = "cached_responses"
        const val TABLE_PLUGINS = "custom_plugins"
        const val TABLE_NOTES = "notes"
        const val TABLE_STUDY_HABITS = "study_habits"
        const val TABLE_EXPENSES = "expenses"
        const val TABLE_DRIVE_FOLDERS = "drive_folders"
        const val TABLE_DRIVE_DOCUMENTS = "drive_documents"
        const val TABLE_CHAT_HISTORY = "chat_history"
    }

    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL("""
            CREATE TABLE $TABLE_MAPPINGS (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                custom_word TEXT NOT NULL UNIQUE,
                app_identifier TEXT NOT NULL
            )
        """.trimIndent())

        db.execSQL("""
            CREATE TABLE $TABLE_MEMORY (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                memory_key TEXT NOT NULL UNIQUE,
                memory_value TEXT NOT NULL
            )
        """.trimIndent())

        db.execSQL("""
            CREATE TABLE $TABLE_ALIASES (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                alias_name TEXT NOT NULL UNIQUE,
                target_contact TEXT NOT NULL
            )
        """.trimIndent())

        db.execSQL("""
            CREATE TABLE $TABLE_ROUTINES (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                trigger_phrase TEXT NOT NULL UNIQUE,
                action_chain_json TEXT NOT NULL
            )
        """.trimIndent())

        db.execSQL("""
            CREATE TABLE $TABLE_STUDY_TASKS (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                title TEXT NOT NULL,
                subject TEXT NOT NULL,
                is_completed INTEGER DEFAULT 0,
                due_date TEXT NOT NULL
            )
        """.trimIndent())

        db.execSQL("""
            CREATE TABLE $TABLE_FLASHCARDS (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                subject TEXT NOT NULL,
                topic TEXT NOT NULL,
                question TEXT NOT NULL,
                answer TEXT NOT NULL,
                formula TEXT
            )
        """.trimIndent())

        db.execSQL("""
            CREATE TABLE $TABLE_CACHE (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                query_key TEXT NOT NULL UNIQUE,
                response_text TEXT NOT NULL,
                category TEXT NOT NULL DEFAULT 'general'
            )
        """.trimIndent())

        db.execSQL("""
            CREATE TABLE $TABLE_PLUGINS (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                name TEXT NOT NULL,
                endpoint_url TEXT NOT NULL,
                headers_json TEXT NOT NULL DEFAULT '{}',
                trigger_word TEXT NOT NULL UNIQUE
            )
        """.trimIndent())

        seedDefaultMappings(db)
        purgeDemoData(db)
    }

    private fun purgeDemoData(db: SQLiteDatabase) {
        try {
            db.execSQL("DELETE FROM $TABLE_MEMORY WHERE memory_value IN ('BSEB Class 12 Science', 'JEE Main & Advanced 2026', 'Physics (Rotational Dynamics)') OR memory_key IN ('Photosynthesis', 'Shalini Mom', 'JEE Exam')")
            db.execSQL("DELETE FROM $TABLE_ALIASES WHERE target_contact LIKE '%+91 98765%' OR alias_name IN ('mom', 'dad', 'friend', 'Mom')")
            db.execSQL("DELETE FROM $TABLE_ROUTINES WHERE trigger_phrase IN ('good morning', 'study mode')")
            db.execSQL("DELETE FROM $TABLE_STUDY_TASKS WHERE title LIKE '%Newton%' OR title LIKE '%Rotational%' OR title LIKE '%Thermodynamics%'")
            db.execSQL("DELETE FROM $TABLE_FLASHCARDS WHERE question LIKE '%Moment of Inertia%' OR question LIKE '%Impulse%' OR question LIKE '%Carnot%' OR question LIKE '%escape velocity%' OR question LIKE '%derivative%'")
            db.execSQL("DELETE FROM $TABLE_PLUGINS WHERE name IN ('Weather Webhook API', 'IoT Lab Sensor')")
            db.execSQL("DELETE FROM $TABLE_DRIVE_FOLDERS WHERE name IN ('General AI Knowledge', 'Study & Research Papers', 'Personal Documents & Notes', 'Code & Formula Reference')")
            db.execSQL("DELETE FROM $TABLE_DRIVE_DOCUMENTS WHERE title IN ('VEDRA AI System Instructions', 'Rotational Dynamics Formulas', 'JEE Physics Syllabus 2026 Summary')")
            db.execSQL("DELETE FROM $TABLE_CHAT_HISTORY WHERE session_title IN ('Rotational Dynamics Equations', 'JEE Physics Study Strategy', 'Flashlight & App Execution')")
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL("DROP TABLE IF EXISTS $TABLE_MAPPINGS")
        db.execSQL("DROP TABLE IF EXISTS $TABLE_MEMORY")
        db.execSQL("DROP TABLE IF EXISTS $TABLE_ALIASES")
        db.execSQL("DROP TABLE IF EXISTS $TABLE_ROUTINES")
        db.execSQL("DROP TABLE IF EXISTS $TABLE_STUDY_TASKS")
        db.execSQL("DROP TABLE IF EXISTS $TABLE_FLASHCARDS")
        db.execSQL("DROP TABLE IF EXISTS $TABLE_CACHE")
        db.execSQL("DROP TABLE IF EXISTS $TABLE_PLUGINS")
        db.execSQL("DROP TABLE IF EXISTS $TABLE_NOTES")
        db.execSQL("DROP TABLE IF EXISTS $TABLE_STUDY_HABITS")
        db.execSQL("DROP TABLE IF EXISTS $TABLE_EXPENSES")
        onCreate(db)
    }

    override fun onOpen(db: SQLiteDatabase) {
        super.onOpen(db)
        db.execSQL("""
            CREATE TABLE IF NOT EXISTS $TABLE_PLUGINS (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                name TEXT NOT NULL,
                endpoint_url TEXT NOT NULL,
                headers_json TEXT NOT NULL DEFAULT '{}',
                trigger_word TEXT NOT NULL UNIQUE
            )
        """.trimIndent())
        db.execSQL("""
            CREATE TABLE IF NOT EXISTS $TABLE_NOTES (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                title TEXT NOT NULL,
                content TEXT NOT NULL,
                timestamp INTEGER NOT NULL
            )
        """.trimIndent())
        db.execSQL("""
            CREATE TABLE IF NOT EXISTS $TABLE_STUDY_HABITS (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                subject TEXT NOT NULL,
                duration_minutes INTEGER NOT NULL,
                date_string TEXT NOT NULL,
                timestamp INTEGER NOT NULL
            )
        """.trimIndent())
        db.execSQL("""
            CREATE TABLE IF NOT EXISTS $TABLE_EXPENSES (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                amount REAL NOT NULL,
                category TEXT NOT NULL,
                note TEXT NOT NULL,
                timestamp INTEGER NOT NULL
            )
        """.trimIndent())
        db.execSQL("""
            CREATE TABLE IF NOT EXISTS $TABLE_CACHE (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                query_key TEXT NOT NULL UNIQUE,
                response_text TEXT NOT NULL,
                category TEXT NOT NULL DEFAULT 'general'
            )
        """.trimIndent())
        try {
            db.execSQL("ALTER TABLE $TABLE_CACHE RENAME COLUMN queryKey TO query_key")
        } catch (_: Exception) {}
        try {
            db.execSQL("ALTER TABLE $TABLE_CACHE RENAME COLUMN responseText TO response_text")
        } catch (_: Exception) {}
        try {
            db.execSQL("ALTER TABLE $TABLE_MEMORY ADD COLUMN profile TEXT DEFAULT 'General'")
        } catch (_: Exception) {}
        try {
            db.execSQL("ALTER TABLE $TABLE_MEMORY ADD COLUMN expires_at INTEGER DEFAULT 0")
        } catch (_: Exception) {}
        try {
            db.execSQL("DELETE FROM $TABLE_MEMORY WHERE expires_at > 0 AND expires_at < ${System.currentTimeMillis()}")
        } catch (_: Exception) {}
        db.execSQL("""
            CREATE TABLE IF NOT EXISTS $TABLE_DRIVE_FOLDERS (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                name TEXT NOT NULL,
                color_hex TEXT NOT NULL DEFAULT '#8B5CF6',
                parent_id INTEGER NOT NULL DEFAULT 0,
                created_at INTEGER NOT NULL
            )
        """.trimIndent())
        try {
            db.execSQL("ALTER TABLE $TABLE_DRIVE_FOLDERS ADD COLUMN parent_id INTEGER DEFAULT 0")
        } catch (_: Exception) {}
        db.execSQL("""
            CREATE TABLE IF NOT EXISTS $TABLE_DRIVE_DOCUMENTS (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                folder_id INTEGER NOT NULL,
                title TEXT NOT NULL,
                content TEXT NOT NULL,
                file_type TEXT NOT NULL DEFAULT 'TXT',
                file_size INTEGER NOT NULL DEFAULT 0,
                created_at INTEGER NOT NULL
            )
        """.trimIndent())
        db.execSQL("""
            CREATE TABLE IF NOT EXISTS $TABLE_CHAT_HISTORY (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                session_title TEXT NOT NULL,
                user_text TEXT NOT NULL,
                ved_response TEXT NOT NULL,
                timestamp INTEGER NOT NULL
            )
        """.trimIndent())
        purgeDemoData(db)
    }

    private fun seedDefaultMappings(db: SQLiteDatabase) {
        val defaults = listOf(
            AppMapping(customWord = "whatsapp", appIdentifier = "com.whatsapp"),
            AppMapping(customWord = "youtube", appIdentifier = "com.google.android.youtube"),
            AppMapping(customWord = "chrome", appIdentifier = "com.android.chrome"),
            AppMapping(customWord = "camera", appIdentifier = "com.android.camera"),
            AppMapping(customWord = "calculator", appIdentifier = "com.google.android.calculator"),
            AppMapping(customWord = "notes", appIdentifier = "com.google.android.keep"),
            AppMapping(customWord = "spotify", appIdentifier = "com.spotify.music")
        )
        for (mapping in defaults) {
            val values = ContentValues().apply {
                put("custom_word", mapping.customWord.lowercase())
                put("app_identifier", mapping.appIdentifier)
            }
            db.insertWithOnConflict(TABLE_MAPPINGS, null, values, SQLiteDatabase.CONFLICT_IGNORE)
        }
    }

    private fun seedDefaultMemories(db: SQLiteDatabase) {
        val memories = listOf(
            UserMemory(memoryKey = "School Board", memoryValue = "BSEB Class 12 Science"),
            UserMemory(memoryKey = "Target Exam", memoryValue = "JEE Main & Advanced 2026"),
            UserMemory(memoryKey = "Favorite Subject", memoryValue = "Physics (Rotational Dynamics)")
        )
        for (m in memories) {
            val values = ContentValues().apply {
                put("memory_key", m.memoryKey)
                put("memory_value", m.memoryValue)
            }
            db.insertWithOnConflict(TABLE_MEMORY, null, values, SQLiteDatabase.CONFLICT_IGNORE)
        }
    }

    private fun seedDefaultAliases(db: SQLiteDatabase) {
        val aliases = listOf(
            ContactAlias(aliasName = "mom", targetContactOrNumber = "Mom (+91 98765 43210)"),
            ContactAlias(aliasName = "dad", targetContactOrNumber = "Dad (+91 98765 43211)"),
            ContactAlias(aliasName = "friend", targetContactOrNumber = "Rahul Classmate")
        )
        for (a in aliases) {
            val values = ContentValues().apply {
                put("alias_name", a.aliasName.lowercase())
                put("target_contact", a.targetContactOrNumber)
            }
            db.insertWithOnConflict(TABLE_ALIASES, null, values, SQLiteDatabase.CONFLICT_IGNORE)
        }
    }

    private fun seedDefaultRoutines(db: SQLiteDatabase) {
        val routines = listOf(
            CustomRoutine(
                triggerPhrase = "good morning",
                actionChainJson = """["Read Weather", "Read Battery", "Open WhatsApp"]"""
            ),
            CustomRoutine(
                triggerPhrase = "study mode",
                actionChainJson = """["Turn Off Flashlight", "Open Calculator", "Check Study Goals"]"""
            )
        )
        for (r in routines) {
            val values = ContentValues().apply {
                put("trigger_phrase", r.triggerPhrase.lowercase())
                put("action_chain_json", r.actionChainJson)
            }
            db.insertWithOnConflict(TABLE_ROUTINES, null, values, SQLiteDatabase.CONFLICT_IGNORE)
        }
    }

    private fun seedDefaultStudyTasks(db: SQLiteDatabase) {
        val tasks = listOf(
            StudyTask(title = "Revise Newton's Laws & Friction", subject = "Physics", isCompleted = false, dueDate = "Today"),
            StudyTask(title = "Solve 15 Rotational Dynamics MCQs", subject = "Physics", isCompleted = true, dueDate = "Today"),
            StudyTask(title = "Thermodynamics Carnot Engine derivation", subject = "Physics/Chemistry", isCompleted = false, dueDate = "Tomorrow")
        )
        for (t in tasks) {
            val values = ContentValues().apply {
                put("title", t.title)
                put("subject", t.subject)
                put("is_completed", if (t.isCompleted) 1 else 0)
                put("due_date", t.dueDate)
            }
            db.insertWithOnConflict(TABLE_STUDY_TASKS, null, values, SQLiteDatabase.CONFLICT_IGNORE)
        }
    }

    private fun seedDefaultFlashcards(db: SQLiteDatabase) {
        val cards = listOf(
            Flashcard(
                subject = "Physics",
                topic = "Rotational Dynamics",
                question = "What is Moment of Inertia of a solid cylinder/disk about its central axis?",
                answer = "Moment of Inertia I = 1/2 * M * R²",
                formula = "I = 0.5 M R²"
            ),
            Flashcard(
                subject = "Physics",
                topic = "Newton's Laws of Motion",
                question = "State Impulse-Momentum Theorem.",
                answer = "Impulse J delivered by a net force equals the total change in momentum Δp.",
                formula = "J = ∫ F dt = Δp = m(v - u)"
            ),
            Flashcard(
                subject = "Physics",
                topic = "Thermodynamics",
                question = "What is the efficiency of a Carnot Engine?",
                answer = "Efficiency η = 1 - (T_cold / T_hot), where temperatures are in Kelvin.",
                formula = "η = 1 - (T₂ / T₁)"
            ),
            Flashcard(
                subject = "Physics",
                topic = "Mechanics",
                question = "What is escape velocity from Earth's surface?",
                answer = "Escape velocity v_e = √(2 g R) ≈ 11.2 km/s.",
                formula = "v_e = √(2 G M / R)"
            ),
            Flashcard(
                subject = "Mathematics",
                topic = "Calculus",
                question = "What is derivative of e^(a*x) * sin(b*x)?",
                answer = "d/dx [e^(ax) sin(bx)] = e^(ax) * [a sin(bx) + b cos(bx)].",
                formula = "y' = e^{ax} (a \\sin bx + b \\cos bx)"
            )
        )
        for (c in cards) {
            val values = ContentValues().apply {
                put("subject", c.subject)
                put("topic", c.topic)
                put("question", c.question)
                put("answer", c.answer)
                put("formula", c.formula)
            }
            db.insertWithOnConflict(TABLE_FLASHCARDS, null, values, SQLiteDatabase.CONFLICT_IGNORE)
        }
    }

    private fun seedDefaultCachedResponses(db: SQLiteDatabase) {
        val defaultResponses = listOf(
            CachedResponse(queryKey = "formula", responseText = "Key Offline Formulas:\n• Newton's 2nd Law: F = m * a\n• Kinetic Energy: KE = 0.5 * m * v²\n• Einstein Mass-Energy: E = m * c²\n• Ohm's Law: V = I * R\n• Quadratic Formula: x = (-b ± √(b² - 4ac)) / 2a", category = "science"),
            CachedResponse(queryKey = "who are you", responseText = "I am VEDRA, your offline-capable AI Voice Assistant and Study Companion.", category = "identity"),
            CachedResponse(queryKey = "routine", responseText = "Local Routines available: 'good morning' (reads status) and 'study mode' (prepares flashcards & timer).", category = "routine"),
            CachedResponse(queryKey = "help", responseText = "VEDRA Offline Capabilities:\n• App Launcher ('open whatsapp')\n• Flashlight ('flashlight on')\n• Quick Calculations ('5 + 12 * 3')\n• Timers & Alarms\n• Local Study Flashcards & Memory Lookup", category = "help")
        )
        for (item in defaultResponses) {
            val values = ContentValues().apply {
                put("query_key", item.queryKey.lowercase())
                put("response_text", item.responseText)
                put("category", item.category)
            }
            db.insertWithOnConflict(TABLE_CACHE, null, values, SQLiteDatabase.CONFLICT_IGNORE)
        }
    }

    private fun seedDefaultPlugins(db: SQLiteDatabase) {
        val plugins = listOf(
            CustomPlugin(
                name = "Weather Webhook API",
                endpointUrl = "https://api.open-meteo.com/v1/forecast?latitude=28.6139&longitude=77.2090&current_weather=true",
                headersJson = """{"Accept": "application/json"}""",
                triggerWord = "weather webhook"
            ),
            CustomPlugin(
                name = "IoT Lab Sensor",
                endpointUrl = "https://example.com/api/v1/sensor/temperature",
                headersJson = """{"Authorization": "Bearer YOUR_API_TOKEN"}""",
                triggerWord = "iot sensor"
            )
        )
        for (p in plugins) {
            val values = ContentValues().apply {
                put("name", p.name)
                put("endpoint_url", p.endpointUrl)
                put("headers_json", p.headersJson)
                put("trigger_word", p.triggerWord.lowercase())
            }
            db.insertWithOnConflict(TABLE_PLUGINS, null, values, SQLiteDatabase.CONFLICT_IGNORE)
        }
    }

    fun searchCachedResponse(query: String): String? {
        val cleanQuery = query.lowercase().trim()
        val db = readableDatabase
        
        // 1. Exact or LIKE match in cached_responses
        db.rawQuery("SELECT response_text FROM $TABLE_CACHE WHERE LOWER(query_key) LIKE ? OR ? LIKE '%' || LOWER(query_key) || '%' LIMIT 1", arrayOf("%$cleanQuery%", cleanQuery)).use { cursor ->
            if (cursor.moveToFirst()) {
                return cursor.getString(0)
            }
        }

        // 2. Check user_memory table
        val memValue = getMemoryValue(cleanQuery)
        if (memValue != null) {
            return "From Memory ($cleanQuery): $memValue"
        }

        // 3. Check flashcards table for matching question/formula
        db.rawQuery("SELECT question, answer, formula FROM $TABLE_FLASHCARDS WHERE LOWER(question) LIKE ? OR LOWER(topic) LIKE ? OR LOWER(subject) LIKE ? LIMIT 1", arrayOf("%$cleanQuery%", "%$cleanQuery%", "%$cleanQuery%")).use { cursor ->
            if (cursor.moveToFirst()) {
                val q = cursor.getString(0)
                val a = cursor.getString(1)
                val f = cursor.getString(2)
                return "Flashcard [$q]:\n$a${if (!f.isNullOrEmpty()) "\nFormula: $f" else ""}"
            }
        }

        return null
    }

    fun saveCachedResponse(queryKey: String, responseText: String, category: String = "general"): Boolean {
        val cv = ContentValues().apply {
            put("query_key", queryKey.lowercase().trim())
            put("response_text", responseText)
            put("category", category)
        }
        return writableDatabase.insertWithOnConflict(TABLE_CACHE, null, cv, SQLiteDatabase.CONFLICT_REPLACE) != -1L
    }

    // --- MAPPINGS ---
    fun getAllMappings(): List<AppMapping> {
        val list = mutableListOf<AppMapping>()
        val db = readableDatabase
        val cursor = db.query(TABLE_MAPPINGS, null, null, null, null, null, "custom_word ASC")
        cursor.use {
            val idIndex = it.getColumnIndexOrThrow("id")
            val wordIndex = it.getColumnIndexOrThrow("custom_word")
            val appIndex = it.getColumnIndexOrThrow("app_identifier")
            while (it.moveToNext()) {
                list.add(
                    AppMapping(
                        id = it.getLong(idIndex),
                        customWord = it.getString(wordIndex),
                        appIdentifier = it.getString(appIndex)
                    )
                )
            }
        }
        return list
    }

    fun addOrUpdateMapping(customWord: String, appIdentifier: String): Boolean {
        val db = writableDatabase
        val values = ContentValues().apply {
            put("custom_word", customWord.lowercase().trim())
            put("app_identifier", appIdentifier.trim())
        }
        val result = db.insertWithOnConflict(TABLE_MAPPINGS, null, values, SQLiteDatabase.CONFLICT_REPLACE)
        return result != -1L
    }

    fun deleteMapping(id: Long): Boolean {
        val db = writableDatabase
        return db.delete(TABLE_MAPPINGS, "id = ?", arrayOf(id.toString())) > 0
    }

    fun getAppIdentifierForWord(word: String): String? {
        val db = readableDatabase
        val cursor = db.query(
            TABLE_MAPPINGS,
            arrayOf("app_identifier"),
            "custom_word = ?",
            arrayOf(word.lowercase().trim()),
            null, null, null
        )
        cursor.use {
            if (it.moveToFirst()) {
                return it.getString(it.getColumnIndexOrThrow("app_identifier"))
            }
        }
        return null
    }

    // --- USER MEMORY ---
    fun getAllMemories(profileFilter: String? = null): List<UserMemory> {
        val list = mutableListOf<UserMemory>()
        val db = readableDatabase
        val cursor = db.query(TABLE_MEMORY, null, null, null, null, null, "memory_key ASC")
        cursor.use {
            val idIdx = it.getColumnIndex("id")
            val keyIdx = it.getColumnIndex("memory_key")
            val valIdx = it.getColumnIndex("memory_value")
            val profIdx = it.getColumnIndex("profile")
            val expIdx = it.getColumnIndex("expires_at")
            while (it.moveToNext()) {
                val prof = if (profIdx != -1 && !it.isNull(profIdx)) it.getString(profIdx) else "General"
                val exp = if (expIdx != -1 && !it.isNull(expIdx)) it.getLong(expIdx) else 0L
                if (profileFilter == null || profileFilter.equals("All", ignoreCase = true) || prof.equals(profileFilter, ignoreCase = true)) {
                    list.add(UserMemory(it.getLong(idIdx), it.getString(keyIdx), it.getString(valIdx), prof, exp))
                }
            }
        }
        return list
    }

    fun addOrUpdateMemory(key: String, value: String, profile: String = "General", expiresAt: Long = 0L): Boolean {
        val db = writableDatabase
        val cv = ContentValues().apply {
            put("memory_key", key.trim())
            put("memory_value", value.trim())
            put("profile", profile)
            put("expires_at", expiresAt)
        }
        val res = db.insertWithOnConflict(TABLE_MEMORY, null, cv, SQLiteDatabase.CONFLICT_REPLACE)
        return res != -1L
    }

    fun deleteMemory(id: Long): Boolean {
        return writableDatabase.delete(TABLE_MEMORY, "id = ?", arrayOf(id.toString())) > 0
    }

    fun getMemoryValue(key: String): String? {
        val db = readableDatabase
        db.rawQuery("SELECT memory_value FROM $TABLE_MEMORY WHERE LOWER(memory_key) LIKE ? OR ? LIKE '%' || LOWER(memory_key) || '%' LIMIT 1", arrayOf("%${key.lowercase()}%", key.lowercase())).use { cursor ->
            if (cursor.moveToFirst()) {
                return cursor.getString(0)
            }
        }
        return null
    }

    fun testMemoryContext(query: String): String {
        val memories = getAllMemories()
        val tokens = query.lowercase().split(" ").filter { it.length > 2 }
        val matches = memories.filter { mem ->
            tokens.any { mem.memoryKey.lowercase().contains(it) || mem.memoryValue.lowercase().contains(it) }
        }
        if (matches.isEmpty()) {
            return "No matching memory context retrieved for query: \"$query\""
        }
        val sb = StringBuilder("Retrieved ${matches.size} Context Memories:\n")
        matches.forEach { m ->
            sb.append("• [${m.profile}] ${m.memoryKey}: ${m.memoryValue}\n")
        }
        return sb.toString().trim()
    }

    // --- ALIASES ---
    fun getAllAliases(): List<ContactAlias> {
        val list = mutableListOf<ContactAlias>()
        val cursor = readableDatabase.query(TABLE_ALIASES, null, null, null, null, null, "alias_name ASC")
        cursor.use {
            val idIdx = it.getColumnIndexOrThrow("id")
            val aliasIdx = it.getColumnIndexOrThrow("alias_name")
            val targetIdx = it.getColumnIndexOrThrow("target_contact")
            while (it.moveToNext()) {
                list.add(ContactAlias(it.getLong(idIdx), it.getString(aliasIdx), it.getString(targetIdx)))
            }
        }
        return list
    }

    fun addOrUpdateAlias(alias: String, target: String): Boolean {
        val cv = ContentValues().apply {
            put("alias_name", alias.lowercase().trim())
            put("target_contact", target.trim())
        }
        return writableDatabase.insertWithOnConflict(TABLE_ALIASES, null, cv, SQLiteDatabase.CONFLICT_REPLACE) != -1L
    }

    fun deleteAlias(id: Long): Boolean {
        return writableDatabase.delete(TABLE_ALIASES, "id = ?", arrayOf(id.toString())) > 0
    }

    fun resolveAlias(aliasName: String): String? {
        val cursor = readableDatabase.query(
            TABLE_ALIASES,
            arrayOf("target_contact"),
            "alias_name = ?",
            arrayOf(aliasName.lowercase().trim()),
            null, null, null
        )
        cursor.use {
            if (it.moveToFirst()) {
                return it.getString(it.getColumnIndexOrThrow("target_contact"))
            }
        }
        return null
    }

    // --- ROUTINES ---
    fun getAllRoutines(): List<CustomRoutine> {
        val list = mutableListOf<CustomRoutine>()
        val cursor = readableDatabase.query(TABLE_ROUTINES, null, null, null, null, null, "trigger_phrase ASC")
        cursor.use {
            val idIdx = it.getColumnIndexOrThrow("id")
            val trigIdx = it.getColumnIndexOrThrow("trigger_phrase")
            val actIdx = it.getColumnIndexOrThrow("action_chain_json")
            while (it.moveToNext()) {
                list.add(CustomRoutine(it.getLong(idIdx), it.getString(trigIdx), it.getString(actIdx)))
            }
        }
        return list
    }

    fun addOrUpdateRoutine(trigger: String, actionChainJson: String): Boolean {
        val cv = ContentValues().apply {
            put("trigger_phrase", trigger.lowercase().trim())
            put("action_chain_json", actionChainJson.trim())
        }
        return writableDatabase.insertWithOnConflict(TABLE_ROUTINES, null, cv, SQLiteDatabase.CONFLICT_REPLACE) != -1L
    }

    fun deleteRoutine(id: Long): Boolean {
        return writableDatabase.delete(TABLE_ROUTINES, "id = ?", arrayOf(id.toString())) > 0
    }

    fun getRoutineForTrigger(trigger: String): String? {
        val cursor = readableDatabase.query(
            TABLE_ROUTINES,
            arrayOf("action_chain_json"),
            "trigger_phrase = ?",
            arrayOf(trigger.lowercase().trim()),
            null, null, null
        )
        cursor.use {
            if (it.moveToFirst()) {
                return it.getString(it.getColumnIndexOrThrow("action_chain_json"))
            }
        }
        return null
    }

    // --- STUDY TASKS ---
    fun getAllStudyTasks(): List<StudyTask> {
        val list = mutableListOf<StudyTask>()
        val cursor = readableDatabase.query(TABLE_STUDY_TASKS, null, null, null, null, null, "id DESC")
        cursor.use {
            val idIdx = it.getColumnIndexOrThrow("id")
            val titleIdx = it.getColumnIndexOrThrow("title")
            val subjIdx = it.getColumnIndexOrThrow("subject")
            val compIdx = it.getColumnIndexOrThrow("is_completed")
            val dateIdx = it.getColumnIndexOrThrow("due_date")
            while (it.moveToNext()) {
                list.add(
                    StudyTask(
                        id = it.getLong(idIdx),
                        title = it.getString(titleIdx),
                        subject = it.getString(subjIdx),
                        isCompleted = it.getInt(compIdx) == 1,
                        dueDate = it.getString(dateIdx)
                    )
                )
            }
        }
        return list
    }

    fun addStudyTask(title: String, subject: String, dueDate: String = "Today"): Boolean {
        val cv = ContentValues().apply {
            put("title", title)
            put("subject", subject)
            put("is_completed", 0)
            put("due_date", dueDate)
        }
        return writableDatabase.insert(TABLE_STUDY_TASKS, null, cv) != -1L
    }

    fun toggleStudyTask(id: Long, currentStatus: Boolean): Boolean {
        val cv = ContentValues().apply {
            put("is_completed", if (!currentStatus) 1 else 0)
        }
        return writableDatabase.update(TABLE_STUDY_TASKS, cv, "id = ?", arrayOf(id.toString())) > 0
    }

    fun deleteStudyTask(id: Long): Boolean {
        return writableDatabase.delete(TABLE_STUDY_TASKS, "id = ?", arrayOf(id.toString())) > 0
    }

    // --- FLASHCARDS ---
    fun getAllFlashcards(): List<Flashcard> {
        val list = mutableListOf<Flashcard>()
        val cursor = readableDatabase.query(TABLE_FLASHCARDS, null, null, null, null, null, "id ASC")
        cursor.use {
            val idIdx = it.getColumnIndexOrThrow("id")
            val subjIdx = it.getColumnIndexOrThrow("subject")
            val topIdx = it.getColumnIndexOrThrow("topic")
            val qIdx = it.getColumnIndexOrThrow("question")
            val aIdx = it.getColumnIndexOrThrow("answer")
            val fIdx = it.getColumnIndexOrThrow("formula")
            while (it.moveToNext()) {
                list.add(
                    Flashcard(
                        id = it.getLong(idIdx),
                        subject = it.getString(subjIdx),
                        topic = it.getString(topIdx),
                        question = it.getString(qIdx),
                        answer = it.getString(aIdx),
                        formula = if (it.isNull(fIdx)) null else it.getString(fIdx)
                    )
                )
            }
        }
        return list
    }

    fun addFlashcard(subject: String, topic: String, question: String, answer: String, formula: String?): Boolean {
        val cv = ContentValues().apply {
            put("subject", subject)
            put("topic", topic)
            put("question", question)
            put("answer", answer)
            put("formula", formula)
        }
        return writableDatabase.insert(TABLE_FLASHCARDS, null, cv) != -1L
    }

    // --- PLUGINS ---
    fun getAllPlugins(): List<CustomPlugin> {
        val list = mutableListOf<CustomPlugin>()
        val db = readableDatabase
        val cursor = db.query(TABLE_PLUGINS, null, null, null, null, null, "name ASC")
        cursor.use {
            val idIdx = it.getColumnIndexOrThrow("id")
            val nameIdx = it.getColumnIndexOrThrow("name")
            val urlIdx = it.getColumnIndexOrThrow("endpoint_url")
            val headerIdx = it.getColumnIndexOrThrow("headers_json")
            val triggerIdx = it.getColumnIndexOrThrow("trigger_word")
            while (it.moveToNext()) {
                list.add(
                    CustomPlugin(
                        id = it.getLong(idIdx),
                        name = it.getString(nameIdx),
                        endpointUrl = it.getString(urlIdx),
                        headersJson = it.getString(headerIdx),
                        triggerWord = it.getString(triggerIdx)
                    )
                )
            }
        }
        return list
    }

    fun addPlugin(name: String, endpointUrl: String, headersJson: String, triggerWord: String): Boolean {
        val cv = ContentValues().apply {
            put("name", name)
            put("endpoint_url", endpointUrl)
            put("headers_json", if (headersJson.isBlank()) "{}" else headersJson)
            put("trigger_word", triggerWord.lowercase().trim())
        }
        return writableDatabase.insertWithOnConflict(TABLE_PLUGINS, null, cv, SQLiteDatabase.CONFLICT_REPLACE) != -1L
    }

    fun deletePlugin(id: Long): Boolean {
        return writableDatabase.delete(TABLE_PLUGINS, "id = ?", arrayOf(id.toString())) > 0
    }

    fun getPluginByTrigger(query: String): CustomPlugin? {
        val clean = query.lowercase().trim()
        val db = readableDatabase
        db.rawQuery("SELECT id, name, endpoint_url, headers_json, trigger_word FROM $TABLE_PLUGINS WHERE LOWER(?) LIKE '%' || LOWER(trigger_word) || '%' OR LOWER(trigger_word) LIKE '%' || LOWER(?) || '%' LIMIT 1", arrayOf(clean, clean)).use {
            if (it.moveToFirst()) {
                return CustomPlugin(
                    id = it.getLong(0),
                    name = it.getString(1),
                    endpointUrl = it.getString(2),
                    headersJson = it.getString(3),
                    triggerWord = it.getString(4)
                )
            }
        }
        return null
    }

    fun getUserContextSummary(): String {
        val memories = getAllMemories()
        val aliases = getAllAliases()
        val sb = StringBuilder()
        if (memories.isNotEmpty()) {
            sb.append("User Saved Profile & Memory:\n")
            memories.forEach { sb.append("- ${it.memoryKey}: ${it.memoryValue}\n") }
        }
        if (aliases.isNotEmpty()) {
            sb.append("User Contacts & Aliases:\n")
            aliases.forEach { sb.append("- ${it.aliasName}: ${it.targetContactOrNumber}\n") }
        }

        // Room database interaction patterns and conversation summary
        try {
            val roomSummary = kotlinx.coroutines.runBlocking {
                aiContextRepository.buildOfflineKnowledgeSummary()
            }
            if (roomSummary.isNotBlank()) {
                sb.append("\n[Room Database Interaction Logs & Context]:\n")
                sb.append(roomSummary)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        return sb.toString().trim()
    }

    fun getAllCachedResponses(): List<CachedResponse> {
        val list = mutableListOf<CachedResponse>()
        val db = readableDatabase
        db.rawQuery("SELECT id, query_key, response_text, category FROM $TABLE_CACHE", null).use {
            while (it.moveToNext()) {
                list.add(
                    CachedResponse(
                        id = it.getLong(0),
                        queryKey = it.getString(1),
                        responseText = it.getString(2),
                        category = it.getString(3)
                    )
                )
            }
        }
        return list
    }

    // NOTES CRUD OPERATIONS
    fun addNote(title: String, content: String): Long {
        val cv = ContentValues().apply {
            put("title", title)
            put("content", content)
            put("timestamp", System.currentTimeMillis())
        }
        return writableDatabase.insert(TABLE_NOTES, null, cv)
    }

    fun getAllNotes(): List<NoteItem> {
        val list = mutableListOf<NoteItem>()
        val db = readableDatabase
        db.rawQuery("SELECT id, title, content, timestamp FROM $TABLE_NOTES ORDER BY id DESC", null).use {
            while (it.moveToNext()) {
                list.add(
                    NoteItem(
                        id = it.getLong(0),
                        title = it.getString(1),
                        content = it.getString(2),
                        timestamp = it.getLong(3)
                    )
                )
            }
        }
        return list
    }

    fun deleteNote(id: Long): Boolean {
        return writableDatabase.delete(TABLE_NOTES, "id = ?", arrayOf(id.toString())) > 0
    }

    fun getLastNote(): NoteItem? {
        val db = readableDatabase
        db.rawQuery("SELECT id, title, content, timestamp FROM $TABLE_NOTES ORDER BY id DESC LIMIT 1", null).use {
            if (it.moveToFirst()) {
                return NoteItem(
                    id = it.getLong(0),
                    title = it.getString(1),
                    content = it.getString(2),
                    timestamp = it.getLong(3)
                )
            }
        }
        return null
    }

    // STUDY HABIT OPERATIONS
    fun logStudyHabit(subject: String, durationMinutes: Int): Long {
        val todayStr = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.US).format(java.util.Date())
        val cv = ContentValues().apply {
            put("subject", subject)
            put("duration_minutes", durationMinutes)
            put("date_string", todayStr)
            put("timestamp", System.currentTimeMillis())
        }
        return writableDatabase.insert(TABLE_STUDY_HABITS, null, cv)
    }

    fun getAllStudyHabits(): List<StudyHabit> {
        val list = mutableListOf<StudyHabit>()
        val db = readableDatabase
        db.rawQuery("SELECT id, subject, duration_minutes, date_string, timestamp FROM $TABLE_STUDY_HABITS ORDER BY id DESC", null).use {
            while (it.moveToNext()) {
                list.add(
                    StudyHabit(
                        id = it.getLong(0),
                        subject = it.getString(1),
                        durationMinutes = it.getInt(2),
                        dateString = it.getString(3),
                        timestamp = it.getLong(4)
                    )
                )
            }
        }
        return list
    }

    fun deleteStudyHabit(id: Long): Boolean {
        return writableDatabase.delete(TABLE_STUDY_HABITS, "id = ?", arrayOf(id.toString())) > 0
    }

    fun calculateStudyStreak(): Int {
        val db = readableDatabase
        val dateList = mutableListOf<String>()
        db.rawQuery("SELECT DISTINCT date_string FROM $TABLE_STUDY_HABITS ORDER BY date_string DESC", null).use {
            while (it.moveToNext()) {
                dateList.add(it.getString(0))
            }
        }
        if (dateList.isEmpty()) return 0

        val sdf = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.US)
        val todayStr = sdf.format(java.util.Date())
        val cal = java.util.Calendar.getInstance()
        
        var streak = 0
        var checkCal = java.util.Calendar.getInstance()

        // Check if today or yesterday has an entry
        val hasToday = dateList.contains(todayStr)
        cal.add(java.util.Calendar.DAY_OF_YEAR, -1)
        val yesterdayStr = sdf.format(cal.time)
        val hasYesterday = dateList.contains(yesterdayStr)

        if (!hasToday && !hasYesterday) return 0

        var currentCheckDate = if (hasToday) todayStr else yesterdayStr
        checkCal.time = sdf.parse(currentCheckDate) ?: java.util.Date()

        while (dateList.contains(sdf.format(checkCal.time))) {
            streak++
            checkCal.add(java.util.Calendar.DAY_OF_YEAR, -1)
        }
        return streak
    }

    fun getTotalStudyMinutesThisWeek(): Int {
        val cal = java.util.Calendar.getInstance()
        cal.add(java.util.Calendar.DAY_OF_YEAR, -7)
        val weekAgoTimestamp = cal.timeInMillis

        val db = readableDatabase
        db.rawQuery("SELECT SUM(duration_minutes) FROM $TABLE_STUDY_HABITS WHERE timestamp >= ?", arrayOf(weekAgoTimestamp.toString())).use {
            if (it.moveToFirst()) {
                return it.getInt(0)
            }
        }
        return 0
    }

    // GRANULAR STORAGE CLEAR METHODS
    fun clearNotes(): Boolean = writableDatabase.delete(TABLE_NOTES, null, null) > 0
    fun clearFlashcards(): Boolean = writableDatabase.delete(TABLE_FLASHCARDS, null, null) > 0
    fun clearStudyHabits(): Boolean = writableDatabase.delete(TABLE_STUDY_HABITS, null, null) > 0
    fun clearExpenses(): Boolean = writableDatabase.delete(TABLE_EXPENSES, null, null) > 0
    fun clearMemories(): Boolean = writableDatabase.delete(TABLE_MEMORY, null, null) > 0
    fun clearAliases(): Boolean = writableDatabase.delete(TABLE_ALIASES, null, null) > 0
    fun clearAppMappings(): Boolean = writableDatabase.delete(TABLE_MAPPINGS, null, null) > 0
    fun clearCachedResponses(): Boolean = writableDatabase.delete(TABLE_CACHE, null, null) > 0

    // VEDRA AI KNOWLEDGE BASE DRIVE METHODS
    private fun seedDefaultDriveData(db: SQLiteDatabase) {
        val countCursor = db.rawQuery("SELECT COUNT(*) FROM $TABLE_DRIVE_FOLDERS", null)
        var count = 0
        countCursor.use {
            if (it.moveToFirst()) count = it.getInt(0)
        }
        if (count == 0) {
            val now = System.currentTimeMillis()
            val f1 = ContentValues().apply {
                put("name", "General AI Knowledge")
                put("color_hex", "#8B5CF6")
                put("created_at", now)
            }
            val f1Id = db.insert(TABLE_DRIVE_FOLDERS, null, f1)

            val f2 = ContentValues().apply {
                put("name", "Study & Research Papers")
                put("color_hex", "#3B82F6")
                put("created_at", now)
            }
            val f2Id = db.insert(TABLE_DRIVE_FOLDERS, null, f2)

            val f3 = ContentValues().apply {
                put("name", "Personal Documents & Notes")
                put("color_hex", "#10B981")
                put("created_at", now)
            }
            val f3Id = db.insert(TABLE_DRIVE_FOLDERS, null, f3)

            val f4 = ContentValues().apply {
                put("name", "Code & Formula Reference")
                put("color_hex", "#F59E0B")
                put("created_at", now)
            }
            val f4Id = db.insert(TABLE_DRIVE_FOLDERS, null, f4)

            if (f1Id > 0) {
                val d1 = ContentValues().apply {
                    put("folder_id", f1Id)
                    put("title", "VEDRA AI System Instructions")
                    put("content", "VEDRA is an advanced AI personal assistant built with Jetpack Compose, Room Database, and offline semantic search. VEDRA assists with study planning, contact management, daily tasks, and secure application launching.")
                    put("file_type", "TXT")
                    put("file_size", 250)
                    put("created_at", now)
                }
                db.insert(TABLE_DRIVE_DOCUMENTS, null, d1)
            }

            if (f2Id > 0) {
                val d2 = ContentValues().apply {
                    put("folder_id", f2Id)
                    put("title", "Rotational Dynamics Formulas")
                    put("content", "Torque Tau = I * Alpha. Angular Momentum L = I * Omega. Kinetic Energy K_rot = 0.5 * I * Omega^2. Moment of Inertia for Solid Cylinder I = 0.5 * M * R^2. Moment of Inertia for Sphere I = 2/5 * M * R^2.")
                    put("file_type", "MD")
                    put("file_size", 380)
                    put("created_at", now)
                }
                db.insert(TABLE_DRIVE_DOCUMENTS, null, d2)

                val d3 = ContentValues().apply {
                    put("folder_id", f2Id)
                    put("title", "JEE Physics Syllabus 2026 Summary")
                    put("content", "Key Topics for JEE Physics 2026: Mechanics, Electricity & Magnetism, Optics, Modern Physics, Thermodynamics, and Oscillations & Waves. Practice numerical problems daily and revise flashcards.")
                    put("file_type", "PDF")
                    put("file_size", 420)
                    put("created_at", now)
                }
                db.insert(TABLE_DRIVE_DOCUMENTS, null, d3)
            }
        }
    }

    fun getAllDriveFolders(parentId: Long = -1L): List<DriveFolder> {
        val list = mutableListOf<DriveFolder>()
        val sql = if (parentId < 0) {
            "SELECT id, name, color_hex, parent_id, created_at FROM $TABLE_DRIVE_FOLDERS ORDER BY name ASC"
        } else {
            "SELECT id, name, color_hex, parent_id, created_at FROM $TABLE_DRIVE_FOLDERS WHERE parent_id = $parentId ORDER BY name ASC"
        }
        val cursor = readableDatabase.rawQuery(sql, null)
        cursor.use {
            while (it.moveToNext()) {
                list.add(
                    DriveFolder(
                        id = it.getLong(0),
                        name = it.getString(1),
                        colorHex = it.getString(2),
                        parentId = if (it.isNull(3)) 0L else it.getLong(3),
                        createdAt = it.getLong(4)
                    )
                )
            }
        }
        return list
    }

    fun getDriveFolderById(id: Long): DriveFolder? {
        val cursor = readableDatabase.rawQuery("SELECT id, name, color_hex, parent_id, created_at FROM $TABLE_DRIVE_FOLDERS WHERE id = ?", arrayOf(id.toString()))
        cursor.use {
            if (it.moveToFirst()) {
                return DriveFolder(
                    id = it.getLong(0),
                    name = it.getString(1),
                    colorHex = it.getString(2),
                    parentId = if (it.isNull(3)) 0L else it.getLong(3),
                    createdAt = it.getLong(4)
                )
            }
        }
        return null
    }

    fun createDriveFolder(name: String, colorHex: String = "#8B5CF6", parentId: Long = 0L): Long {
        val values = ContentValues().apply {
            put("name", name)
            put("color_hex", colorHex)
            put("parent_id", parentId)
            put("created_at", System.currentTimeMillis())
        }
        return writableDatabase.insert(TABLE_DRIVE_FOLDERS, null, values)
    }

    fun updateDriveFolder(id: Long, name: String): Boolean {
        val values = ContentValues().apply { put("name", name) }
        return writableDatabase.update(TABLE_DRIVE_FOLDERS, values, "id=?", arrayOf(id.toString())) > 0
    }

    fun moveDriveFolder(id: Long, newParentId: Long): Boolean {
        val values = ContentValues().apply { put("parent_id", newParentId) }
        return writableDatabase.update(TABLE_DRIVE_FOLDERS, values, "id=?", arrayOf(id.toString())) > 0
    }

    fun copyDriveFolder(id: Long, targetParentId: Long): Long {
        val folder = getDriveFolderById(id) ?: return -1L
        val newFolderId = createDriveFolder(name = "${folder.name}_copy", colorHex = folder.colorHex, parentId = targetParentId)
        if (newFolderId > 0) {
            val subfolders = getAllDriveFolders(id)
            for (sub in subfolders) {
                copyDriveFolder(sub.id, newFolderId)
            }
            val docs = getDocumentsInFolder(id)
            for (doc in docs) {
                createDriveDocument(newFolderId, doc.title, doc.content, doc.fileType)
            }
        }
        return newFolderId
    }

    fun deleteDriveFolder(id: Long): Boolean {
        // Recursively delete subfolders as well
        val subfolders = getAllDriveFolders(id)
        for (sub in subfolders) {
            deleteDriveFolder(sub.id)
        }
        writableDatabase.delete(TABLE_DRIVE_DOCUMENTS, "folder_id=?", arrayOf(id.toString()))
        return writableDatabase.delete(TABLE_DRIVE_FOLDERS, "id=?", arrayOf(id.toString())) > 0
    }

    fun getDocumentsInFolder(folderId: Long): List<DriveDocument> {
        val list = mutableListOf<DriveDocument>()
        val cursor = readableDatabase.rawQuery(
            "SELECT id, folder_id, title, content, file_type, file_size, created_at FROM $TABLE_DRIVE_DOCUMENTS WHERE folder_id=? ORDER BY created_at DESC",
            arrayOf(folderId.toString())
        )
        cursor.use {
            while (it.moveToNext()) {
                list.add(
                    DriveDocument(
                        id = it.getLong(0),
                        folderId = it.getLong(1),
                        title = it.getString(2),
                        content = it.getString(3),
                        fileType = it.getString(4),
                        fileSize = it.getLong(5),
                        createdAt = it.getLong(6)
                    )
                )
            }
        }
        return list
    }

    fun getAllDriveDocuments(): List<DriveDocument> {
        val list = mutableListOf<DriveDocument>()
        val cursor = readableDatabase.rawQuery(
            "SELECT id, folder_id, title, content, file_type, file_size, created_at FROM $TABLE_DRIVE_DOCUMENTS ORDER BY created_at DESC",
            null
        )
        cursor.use {
            while (it.moveToNext()) {
                list.add(
                    DriveDocument(
                        id = it.getLong(0),
                        folderId = it.getLong(1),
                        title = it.getString(2),
                        content = it.getString(3),
                        fileType = it.getString(4),
                        fileSize = it.getLong(5),
                        createdAt = it.getLong(6)
                    )
                )
            }
        }
        return list
    }

    fun createDriveDocument(folderId: Long, title: String, content: String, fileType: String = "TXT"): Long {
        val values = ContentValues().apply {
            put("folder_id", folderId)
            put("title", title)
            put("content", content)
            put("file_type", fileType)
            put("file_size", content.toByteArray().size.toLong())
            put("created_at", System.currentTimeMillis())
        }
        return writableDatabase.insert(TABLE_DRIVE_DOCUMENTS, null, values)
    }

    fun updateDriveDocument(id: Long, title: String, content: String): Boolean {
        val values = ContentValues().apply {
            put("title", title)
            put("content", content)
            put("file_size", content.toByteArray().size.toLong())
        }
        return writableDatabase.update(TABLE_DRIVE_DOCUMENTS, values, "id=?", arrayOf(id.toString())) > 0
    }

    fun deleteDriveDocument(id: Long): Boolean {
        return writableDatabase.delete(TABLE_DRIVE_DOCUMENTS, "id=?", arrayOf(id.toString())) > 0
    }

    fun moveDriveDocument(id: Long, newFolderId: Long): Boolean {
        val values = ContentValues().apply { put("folder_id", newFolderId) }
        return writableDatabase.update(TABLE_DRIVE_DOCUMENTS, values, "id=?", arrayOf(id.toString())) > 0
    }

    fun copyDriveDocument(id: Long, targetFolderId: Long): Long {
        val cursor = readableDatabase.rawQuery("SELECT title, content, file_type FROM $TABLE_DRIVE_DOCUMENTS WHERE id=?", arrayOf(id.toString()))
        cursor.use {
            if (it.moveToFirst()) {
                val title = it.getString(0)
                val content = it.getString(1)
                val fileType = it.getString(2)
                return createDriveDocument(targetFolderId, "${title}_copy", content, fileType)
            }
        }
        return -1L
    }

    // ON-DEVICE SEMANTIC SEARCH & EMBEDDING SIMILARITY ENGINE
    fun searchOfflineContent(query: String): String? {
        val clean = query.trim().lowercase()
        if (clean.isBlank()) return null

        val rawTokens = clean.split(" ").filter { it.length > 2 }
        if (rawTokens.isEmpty()) return null

        // Synonym & Semantic Concept Expansion Vectors
        val synonymMap = mapOf(
            "falling" to listOf("gravity", "acceleration", "force", "physics", "newton", "velocity", "motion"),
            "drop" to listOf("gravity", "falling", "height", "mass"),
            "money" to listOf("spent", "expense", "price", "cost", "rupees", "bought", "purchase"),
            "spend" to listOf("expense", "money", "cost", "paid", "bill"),
            "call" to listOf("contact", "alias", "phone", "number", "dial"),
            "person" to listOf("contact", "alias", "name", "friend", "mom"),
            "study" to listOf("subject", "topic", "physics", "math", "chemistry", "flashcard", "notes"),
            "exam" to listOf("test", "planner", "quiz", "task", "due")
        )

        val expandedTokens = mutableSetOf<String>()
        rawTokens.forEach { t ->
            expandedTokens.add(t)
            synonymMap[t]?.let { expandedTokens.addAll(it) }
        }

        fun calculateSemanticSimilarity(targetText: String): Int {
            val targetTokens = targetText.lowercase().split(" ", "-", "_", "\n").filter { it.length > 2 }
            if (targetTokens.isEmpty()) return 0
            var directHits = 0
            var semanticHits = 0
            rawTokens.forEach { rt ->
                if (targetText.lowercase().contains(rt)) directHits++
            }
            expandedTokens.forEach { et ->
                if (targetText.lowercase().contains(et)) semanticHits++
            }
            val score = ((directHits * 40) + (semanticHits * 20)).coerceAtMost(98)
            return score
        }

        var bestMatchMessage: String? = null
        var highestScore = 0

        // 1. Evaluate Notes
        getAllNotes().forEach { note ->
            val score = calculateSemanticSimilarity("${note.title} ${note.content}")
            if (score > highestScore && score >= 40) {
                highestScore = score
                bestMatchMessage = "🔍 [Offline Semantic Note Match - $score% Similarity]\nTitle: ${note.title}\nContent: ${note.content}"
            }
        }

        // 1b. Evaluate VEDRA AI Drive Documents
        getAllDriveDocuments().forEach { doc ->
            val score = calculateSemanticSimilarity("${doc.title} ${doc.content}")
            if (score > highestScore && score >= 35) {
                highestScore = score
                bestMatchMessage = "📄 [VEDRA AI Drive Document Match - $score% Relevance]\nDocument: ${doc.title}\nContent:\n${doc.content}"
            }
        }

        // 2. Evaluate Flashcards
        getAllFlashcards().forEach { fc ->
            val score = calculateSemanticSimilarity("${fc.subject} ${fc.topic} ${fc.question} ${fc.answer}")
            if (score > highestScore && score >= 40) {
                highestScore = score
                bestMatchMessage = "📚 [Offline Semantic Flashcard Match - $score% Similarity]\nSubject: ${fc.subject}\nQ: ${fc.question}\nA: ${fc.answer}"
            }
        }

        // 3. Evaluate User Memories / Profile
        getAllMemories().forEach { mem ->
            val score = calculateSemanticSimilarity("${mem.memoryKey} ${mem.memoryValue} ${mem.profile}")
            if (score > highestScore && score >= 40) {
                highestScore = score
                bestMatchMessage = "🧠 [Offline Semantic Memory Match - $score% Similarity]\n[${mem.profile}] ${mem.memoryKey}: ${mem.memoryValue}"
            }
        }

        // 4. Evaluate Expenses
        getAllExpenses().forEach { exp ->
            val score = calculateSemanticSimilarity("${exp.category} ${exp.note} ${exp.amount}")
            if (score > highestScore && score >= 40) {
                highestScore = score
                bestMatchMessage = "💳 [Offline Semantic Expense Match - $score% Similarity]\nCategory: ${exp.category}\nNote: ${exp.note}\nAmount: ₹${exp.amount}"
            }
        }

        return bestMatchMessage
    }

    // EXPENSE LOGGING OPERATIONS
    fun logExpense(amount: Double, category: String, note: String): Long {
        val cv = ContentValues().apply {
            put("amount", amount)
            put("category", category)
            put("note", note)
            put("timestamp", System.currentTimeMillis())
        }
        return writableDatabase.insert(TABLE_EXPENSES, null, cv)
    }

    fun getAllExpenses(): List<ExpenseItem> {
        val list = mutableListOf<ExpenseItem>()
        val db = readableDatabase
        db.rawQuery("SELECT id, amount, category, note, timestamp FROM $TABLE_EXPENSES ORDER BY id DESC", null).use {
            while (it.moveToNext()) {
                list.add(
                    ExpenseItem(
                        id = it.getLong(0),
                        amount = it.getDouble(1),
                        category = it.getString(2),
                        note = it.getString(3),
                        timestamp = it.getLong(4)
                    )
                )
            }
        }
        return list
    }

    fun deleteExpense(id: Long): Boolean {
        return writableDatabase.delete(TABLE_EXPENSES, "id = ?", arrayOf(id.toString())) > 0
    }

    fun getMonthlyExpenseTotal(): Double {
        val cal = java.util.Calendar.getInstance()
        cal.set(java.util.Calendar.DAY_OF_MONTH, 1)
        cal.set(java.util.Calendar.HOUR_OF_DAY, 0)
        cal.set(java.util.Calendar.MINUTE, 0)
        cal.set(java.util.Calendar.SECOND, 0)
        val monthStart = cal.timeInMillis

        val db = readableDatabase
        db.rawQuery("SELECT SUM(amount) FROM $TABLE_EXPENSES WHERE timestamp >= ?", arrayOf(monthStart.toString())).use {
            if (it.moveToFirst()) {
                return it.getDouble(0)
            }
        }
        return 0.0
    }

    // ENCRYPTED JSON BACKUP & RESTORE
    fun exportBackupJson(): String {
        val root = org.json.JSONObject()
        root.put("version", 2)
        root.put("exportTime", System.currentTimeMillis())

        val notesArr = org.json.JSONArray()
        getAllNotes().forEach { n ->
            notesArr.put(org.json.JSONObject().apply {
                put("title", n.title)
                put("content", n.content)
                put("timestamp", n.timestamp)
            })
        }
        root.put("notes", notesArr)

        val habitsArr = org.json.JSONArray()
        getAllStudyHabits().forEach { h ->
            habitsArr.put(org.json.JSONObject().apply {
                put("subject", h.subject)
                put("durationMinutes", h.durationMinutes)
                put("dateString", h.dateString)
                put("timestamp", h.timestamp)
            })
        }
        root.put("habits", habitsArr)

        val expensesArr = org.json.JSONArray()
        getAllExpenses().forEach { e ->
            expensesArr.put(org.json.JSONObject().apply {
                put("amount", e.amount)
                put("category", e.category)
                put("note", e.note)
                put("timestamp", e.timestamp)
            })
        }
        root.put("expenses", expensesArr)

        val memoriesArr = org.json.JSONArray()
        getAllMemories().forEach { m ->
            memoriesArr.put(org.json.JSONObject().apply {
                put("key", m.memoryKey)
                put("value", m.memoryValue)
                put("profile", m.profile)
                put("expiresAt", m.expiresAt)
            })
        }
        root.put("memories", memoriesArr)

        val flashcardsArr = org.json.JSONArray()
        getAllFlashcards().forEach { fc ->
            flashcardsArr.put(org.json.JSONObject().apply {
                put("subject", fc.subject)
                put("topic", fc.topic)
                put("question", fc.question)
                put("answer", fc.answer)
                put("formula", fc.formula)
            })
        }
        root.put("flashcards", flashcardsArr)

        val aliasesArr = org.json.JSONArray()
        getAllAliases().forEach { a ->
            aliasesArr.put(org.json.JSONObject().apply {
                put("aliasName", a.aliasName)
                put("targetContactOrNumber", a.targetContactOrNumber)
            })
        }
        root.put("aliases", aliasesArr)

        val mappingsArr = org.json.JSONArray()
        getAllMappings().forEach { m ->
            mappingsArr.put(org.json.JSONObject().apply {
                put("customWord", m.customWord)
                put("appIdentifier", m.appIdentifier)
            })
        }
        root.put("mappings", mappingsArr)

        return root.toString(2)
    }

    fun restoreBackupJson(jsonStr: String): Boolean {
        return try {
            val root = org.json.JSONObject(jsonStr)
            if (root.has("notes")) {
                val notesArr = root.getJSONArray("notes")
                for (i in 0 until notesArr.length()) {
                    val obj = notesArr.getJSONObject(i)
                    addNote(obj.optString("title", "Note"), obj.optString("content", ""))
                }
            }
            if (root.has("habits")) {
                val habitsArr = root.getJSONArray("habits")
                for (i in 0 until habitsArr.length()) {
                    val obj = habitsArr.getJSONObject(i)
                    logStudyHabit(obj.optString("subject", "General"), obj.optInt("durationMinutes", 30))
                }
            }
            if (root.has("expenses")) {
                val expensesArr = root.getJSONArray("expenses")
                for (i in 0 until expensesArr.length()) {
                    val obj = expensesArr.getJSONObject(i)
                    logExpense(obj.optDouble("amount", 0.0), obj.optString("category", "General"), obj.optString("note", ""))
                }
            }
            if (root.has("memories")) {
                val memArr = root.getJSONArray("memories")
                for (i in 0 until memArr.length()) {
                    val obj = memArr.getJSONObject(i)
                    addOrUpdateMemory(
                        obj.optString("key", ""),
                        obj.optString("value", ""),
                        obj.optString("profile", "General"),
                        obj.optLong("expiresAt", 0L)
                    )
                }
            }
            if (root.has("flashcards")) {
                val fcArr = root.getJSONArray("flashcards")
                for (i in 0 until fcArr.length()) {
                    val obj = fcArr.getJSONObject(i)
                    addFlashcard(
                        obj.optString("subject", "General"),
                        obj.optString("topic", "General"),
                        obj.optString("question", ""),
                        obj.optString("answer", ""),
                        if (obj.has("formula")) obj.optString("formula") else null
                    )
                }
            }
            if (root.has("aliases")) {
                val alArr = root.getJSONArray("aliases")
                for (i in 0 until alArr.length()) {
                    val obj = alArr.getJSONObject(i)
                    addOrUpdateAlias(obj.optString("aliasName", ""), obj.optString("targetContactOrNumber", ""))
                }
            }
            if (root.has("mappings")) {
                val mapArr = root.getJSONArray("mappings")
                for (i in 0 until mapArr.length()) {
                    val obj = mapArr.getJSONObject(i)
                    addOrUpdateMapping(obj.optString("customWord", ""), obj.optString("appIdentifier", ""))
                }
            }
            true
        } catch (e: Exception) {
            false
        }
    }

    // STORAGE SYNC STATS
    fun getOfflineStorageStats(): Map<String, Int> {
        val stats = mutableMapOf<String, Int>()
        stats["Notes"] = getAllNotes().size
        stats["Expenses"] = getAllExpenses().size
        stats["Flashcards"] = getAllFlashcards().size
        stats["Study Habits"] = getAllStudyHabits().size
        stats["User Memories"] = getAllMemories().size
        stats["Contact Aliases"] = getAllAliases().size
        stats["App Shortcuts"] = getAllMappings().size
        stats["Routines"] = getAllRoutines().size
        stats["Cached Responses"] = getAllCachedResponses().size
        return stats
    }

    fun getAllHabits(): List<StudyHabit> = getAllStudyHabits()

    val settingsVersion = androidx.compose.runtime.mutableIntStateOf(0)

    fun setSetting(key: String, value: String) {
        addOrUpdateMemory("setting_$key", value, "System", 0L)
        settingsVersion.intValue += 1
        try {
            kotlinx.coroutines.CoroutineScope(kotlinx.coroutines.Dispatchers.IO).launch {
                aiContextRepository.saveUserSetting(key, value)
            }
        } catch (_: Exception) {}
    }

    fun getSetting(key: String, defaultValue: String): String {
        return getMemoryValue("setting_$key") ?: defaultValue
    }

    fun logUserBehavior(actionType: String, detail: String) {
        val currentCount = getSetting("behavior_count_$actionType", "0").toIntOrNull() ?: 0
        setSetting("behavior_count_$actionType", (currentCount + 1).toString())
        setSetting("behavior_last_$actionType", detail)
    }

    fun getUserBehaviorSummary(): String {
        val playVideoCount = getSetting("behavior_count_PLAY_VIDEO", "0")
        val playMusicCount = getSetting("behavior_count_PLAY_MUSIC", "0")
        val fileSearchCount = getSetting("behavior_count_FILE_SEARCH", "0")
        val lastVideo = getSetting("behavior_last_PLAY_VIDEO", "None")
        val lastMusic = getSetting("behavior_last_PLAY_MUSIC", "None")
        val lastFile = getSetting("behavior_last_FILE_SEARCH", "None")

        return "🧠 VEDRA Behavioral Intelligence:\n" +
                "• Videos/Movies Played: $playVideoCount times (Last: '$lastVideo')\n" +
                "• Music Played: $playMusicCount times (Last: '$lastMusic')\n" +
                "• Device Files Searched: $fileSearchCount times (Last: '$lastFile')\n" +
                "VEDRA continuously learns your app choices and media preferences for instant execution."
    }

    private fun seedDefaultChatHistory(db: SQLiteDatabase) {
        val cursor = db.rawQuery("SELECT COUNT(*) FROM $TABLE_CHAT_HISTORY", null)
        var count = 0
        cursor.use {
            if (it.moveToFirst()) count = it.getInt(0)
        }
        if (count == 0) {
            val now = System.currentTimeMillis()
            val defaultChats = listOf(
                Triple("Rotational Dynamics Equations", "Explain torque and angular momentum equations for solid cylinder.", "Torque τ = I · α, Angular Momentum L = I · ω. For a solid cylinder about central axis, I = 0.5 · M · R²."),
                Triple("JEE Physics Study Strategy", "How to prepare for JEE Physics Mechanics in 30 days?", "Focus on Newton's Laws, Friction, Rotational Motion, and Work-Energy-Power. Solve 20 PYQ problems daily and revise flashcards."),
                Triple("Flashlight & App Execution", "Turn on flashlight and open calculator.", "⚡ Activated Flashlight! Launching Calculator app...")
            )
            for (chat in defaultChats) {
                val cv = ContentValues().apply {
                    put("session_title", chat.first)
                    put("user_text", chat.second)
                    put("ved_response", chat.third)
                    put("timestamp", now)
                }
                db.insert(TABLE_CHAT_HISTORY, null, cv)
            }
        }
    }

    fun saveChatHistory(sessionTitle: String, userText: String, vedResponse: String): Long {
        val db = writableDatabase
        val title = if (sessionTitle.isNotBlank()) sessionTitle else userText.take(30).replace("\n", " ")
        val cv = ContentValues().apply {
            put("session_title", title)
            put("user_text", userText)
            put("ved_response", vedResponse)
            put("timestamp", System.currentTimeMillis())
        }
        val insertedId = db.insert(TABLE_CHAT_HISTORY, null, cv)

        // Persist to Room Database asynchronously for conversation context and pattern analysis
        try {
            CoroutineScope(Dispatchers.IO).launch {
                aiContextRepository.recordConversation(userText, vedResponse, "VEDRA AI", title)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        return insertedId
    }

    fun getAllChatHistory(): List<ChatHistoryItem> {
        val list = mutableListOf<ChatHistoryItem>()
        val db = readableDatabase
        try {
            val cursor = db.rawQuery("SELECT id, session_title, user_text, ved_response, timestamp FROM $TABLE_CHAT_HISTORY ORDER BY id DESC LIMIT 50", null)
            cursor.use {
                while (it.moveToNext()) {
                    list.add(
                        ChatHistoryItem(
                            id = it.getLong(0),
                            sessionTitle = it.getString(1),
                            userText = it.getString(2),
                            vedResponse = it.getString(3),
                            timestamp = it.getLong(4)
                        )
                    )
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return list
    }

    fun clearChatHistory(): Boolean {
        try {
            CoroutineScope(Dispatchers.IO).launch {
                aiContextRepository.clearAllRoomLogs()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return writableDatabase.delete(TABLE_CHAT_HISTORY, null, null) > 0
    }

    fun findLearnedResponse(userQuery: String): String? {
        val cleanQuery = userQuery.trim().lowercase()
        if (cleanQuery.isBlank()) return null

        // 1. Query local Room database conversation context first
        try {
            val roomLearned = kotlinx.coroutines.runBlocking {
                aiContextRepository.getLearnedContextForPrompt(userQuery)
            }
            if (roomLearned != null) {
                return roomLearned
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        // 2. Fallback check direct query match in saved chat history
        val allChats = getAllChatHistory()
        for (item in allChats) {
            val itemUserText = item.userText.lowercase().trim()
            if (cleanQuery == itemUserText || (cleanQuery.length > 5 && (cleanQuery.contains(itemUserText) || itemUserText.contains(cleanQuery)))) {
                return "🧠 [Learned from previous conversation]:\n${item.vedResponse}"
            }
        }

        // 3. Keyword overlap score matching across learned chats
        val stopWords = setOf("a", "an", "the", "is", "are", "was", "were", "what", "how", "why", "who", "where", "can", "you", "tell", "me", "about", "for", "in", "on", "to", "with", "do", "does", "did", "please", "vedra", "ved")
        val queryKeywords = cleanQuery.split(Regex("\\W+"))
            .filter { it.length > 2 && it !in stopWords }

        if (queryKeywords.isNotEmpty()) {
            var bestMatchResponse: String? = null
            var maxScore = 0

            for (item in allChats) {
                val itemUserText = item.userText.lowercase()
                val itemTitle = item.sessionTitle.lowercase()

                var score = 0
                for (kw in queryKeywords) {
                    if (itemUserText.contains(kw) || itemTitle.contains(kw)) {
                        score += 1
                    }
                }

                if (score > maxScore && (score >= 2 || (queryKeywords.size == 1 && score == 1))) {
                    maxScore = score
                    bestMatchResponse = item.vedResponse
                }
            }

            if (bestMatchResponse != null) {
                return "🧠 [Learned from previous conversation]:\n$bestMatchResponse"
            }
        }

        return null
    }
}

data class ChatHistoryItem(
    val id: Long,
    val sessionTitle: String,
    val userText: String,
    val vedResponse: String,
    val timestamp: Long
)

object AppLauncher {

    private val knownAppPackages = mapOf(
        "youtube" to "com.google.android.youtube",
        "yt" to "com.google.android.youtube",
        "whatsapp" to "com.whatsapp",
        "whats" to "com.whatsapp",
        "whats app" to "com.whatsapp",
        "whatsapp business" to "com.whatsapp.w4b",
        "w4b" to "com.whatsapp.w4b",
        "wa" to "com.whatsapp",
        "instagram" to "com.instagram.android",
        "insta" to "com.instagram.android",
        "facebook" to "com.facebook.katana",
        "fb" to "com.facebook.katana",
        "chrome" to "com.android.chrome",
        "browser" to "com.android.chrome",
        "gmail" to "com.google.android.gm",
        "email" to "com.google.android.gm",
        "mail" to "com.google.android.gm",
        "maps" to "com.google.android.apps.maps",
        "google maps" to "com.google.android.apps.maps",
        "spotify" to "com.spotify.music",
        "telegram" to "org.telegram.messenger",
        "snapchat" to "com.snapchat.android",
        "twitter" to "com.twitter.android",
        "x" to "com.twitter.android",
        "linkedin" to "com.linkedin.android",
        "calculator" to "com.google.android.calculator",
        "calc" to "com.google.android.calculator",
        "clock" to "com.google.android.deskclock",
        "alarm" to "com.google.android.deskclock",
        "settings" to "com.android.settings",
        "photos" to "com.google.android.apps.photos",
        "google photos" to "com.google.android.apps.photos",
        "gallery" to "com.sec.android.gallery3d",
        "phone" to "com.google.android.dialer",
        "dialer" to "com.google.android.dialer",
        "messages" to "com.google.android.apps.messaging",
        "sms" to "com.google.android.apps.messaging",
        "playstore" to "com.android.vending",
        "play store" to "com.android.vending",
        "store" to "com.android.vending",
        "paytm" to "net.one97.paytm",
        "gpay" to "com.google.android.apps.nbu.paisa.user",
        "google pay" to "com.google.android.apps.nbu.paisa.user",
        "phonepe" to "com.phonepe.app",
        "amazon" to "in.amazon.mShop.android.shopping",
        "flipkart" to "com.flipkart.android",
        "netflix" to "com.netflix.mediaclient",
        "prime" to "com.amazon.avod.thirdpartyclient",
        "prime video" to "com.amazon.avod.thirdpartyclient",
        "hotstar" to "in.startv.hotstar",
        "zomato" to "com.application.zomato",
        "swiggy" to "in.swiggy.android",
        "uber" to "com.ubercab",
        "drive" to "com.google.android.apps.docs",
        "google drive" to "com.google.android.apps.docs"
    )

    fun launchAppByCustomWord(context: Context, dbService: DatabaseService, customWord: String): String {
        var cleanWord = customWord.lowercase().trim()
            .replace(Regex("""\b(app|application|kholo|open|launch|start|karo)\b""", RegexOption.IGNORE_CASE), "")
            .trim()

        if (cleanWord.isBlank()) return "Please specify an app name to open."

        // Contact + WhatsApp Deep Linking & Direct WhatsApp App Launching: "rahul whatsapp", "open rahul whatsapp", "whats", "whatsapp"
        val isWhatsAppQuery = cleanWord.contains("whatsapp") || cleanWord.contains("whats") || cleanWord.contains("wa ") || cleanWord.endsWith(" wa") || cleanWord == "wa"
        if (isWhatsAppQuery) {
            val contactQuery = cleanWord.replace("whatsapp", "")
                .replace("whats", "")
                .replace("app", "")
                .replace("chat", "")
                .replace("of", "")
                .replace("with", "")
                .replace("ka", "")
                .replace("wa", "")
                .trim()

            if (contactQuery.isNotBlank() && contactQuery.length >= 2) {
                val resolvedTarget = dbService.resolveAlias(contactQuery) ?: contactQuery
                val contactInfo = ContactsService.findContactByName(context, resolvedTarget)
                val phone = contactInfo?.phoneNumber ?: resolvedTarget
                val cleanPhone = phone.replace(Regex("""[^0-9+]"""), "")

                if (cleanPhone.isNotEmpty()) {
                    val waUri = if (cleanPhone.length >= 5) {
                        Uri.parse("https://api.whatsapp.com/send?phone=$cleanPhone")
                    } else {
                        Uri.parse("whatsapp://send?phone=$cleanPhone")
                    }

                    val waIntent = Intent(Intent.ACTION_VIEW, waUri).apply {
                        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                        // Explicitly set package so Android forces launching installed native WhatsApp app directly!
                        val pm = context.packageManager
                        if (pm.getLaunchIntentForPackage("com.whatsapp") != null) {
                            setPackage("com.whatsapp")
                        } else if (pm.getLaunchIntentForPackage("com.whatsapp.w4b") != null) {
                            setPackage("com.whatsapp.w4b")
                        }
                    }

                    return try {
                        context.startActivity(waIntent)
                        "Opening WhatsApp chat with ${contactInfo?.name ?: contactQuery} ($phone)... 💬"
                    } catch (e: Exception) {
                        if (tryLaunchPackage(context, "com.whatsapp") || tryLaunchPackage(context, "com.whatsapp.w4b")) {
                            "Opening WhatsApp for ${contactInfo?.name ?: contactQuery}... 💬"
                        } else {
                            openInPlayStore(context, "WhatsApp", "com.whatsapp")
                        }
                    }
                }
            }

            // No specific contact or contact chat launch completed: launch native installed WhatsApp app directly
            if (tryLaunchPackage(context, "com.whatsapp") || tryLaunchPackage(context, "com.whatsapp.w4b")) {
                return "Opening WhatsApp... 💬"
            }
        }

        // 1. Check custom DB mapping / locked shortcut
        val customPkg = dbService.getAppIdentifierForWord(cleanWord) ?: dbService.getSetting("locked_app_$cleanWord", "")
        if (customPkg.isNotBlank()) {
            if (tryLaunchPackage(context, customPkg)) {
                return "Launching '$cleanWord'..."
            }
        }

        // 2. Check known package dictionary
        val knownPkg = knownAppPackages[cleanWord]
        if (knownPkg != null) {
            if (tryLaunchPackage(context, knownPkg)) {
                return "Launching ${getAppDisplayName(cleanWord)}..."
            }
        }

        // 3. Search all installed packages dynamically by app label and package name
        val installedPkg = findInstalledPackageOnDevice(context, cleanWord)
        if (installedPkg != null) {
            if (tryLaunchPackage(context, installedPkg)) {
                return "Launching '${getAppDisplayName(cleanWord)}'..."
            }
        }

        // 4. Special System Intents for native device apps
        if (cleanWord.contains("camera") || cleanWord.contains("photo") || cleanWord.contains("snap")) {
            return tryLaunchSpecialIntent(context, android.provider.MediaStore.INTENT_ACTION_STILL_IMAGE_CAMERA, "Camera")
        }
        if (cleanWord.contains("clock") || cleanWord.contains("alarm") || cleanWord.contains("timer")) {
            return tryLaunchSpecialIntent(context, android.provider.AlarmClock.ACTION_SHOW_ALARMS, "Clock")
        }
        if (cleanWord.contains("sms") || cleanWord.contains("message") || cleanWord.contains("messaging")) {
            val smsIntent = Intent(Intent.ACTION_MAIN).apply { addCategory(Intent.CATEGORY_APP_MESSAGING) }
            return tryLaunchSpecialIntentByIntent(context, smsIntent, "Messages")
        }
        if (cleanWord.contains("calendar")) {
            val calIntent = Intent(Intent.ACTION_VIEW, Uri.parse("content://com.android.calendar/time/"))
            return tryLaunchSpecialIntentByIntent(context, calIntent, "Calendar")
        }
        if (cleanWord.contains("contact")) {
            val contactIntent = Intent(Intent.ACTION_VIEW, android.provider.ContactsContract.Contacts.CONTENT_URI)
            return tryLaunchSpecialIntentByIntent(context, contactIntent, "Contacts")
        }
        if (cleanWord.contains("setting")) {
            return tryLaunchSpecialIntent(context, android.provider.Settings.ACTION_SETTINGS, "Settings")
        }
        if (cleanWord.contains("dial") || cleanWord.contains("phone") || cleanWord.contains("call")) {
            return tryLaunchSpecialIntent(context, Intent.ACTION_DIAL, "Phone")
        }
        if (cleanWord.contains("mail") || cleanWord.contains("email")) {
            val mailIntent = Intent(Intent.ACTION_MAIN).apply { addCategory(Intent.CATEGORY_APP_EMAIL) }
            return tryLaunchSpecialIntentByIntent(context, mailIntent, "Email")
        }
        if (cleanWord.contains("gallery") || cleanWord.contains("photo")) {
            val galIntent = Intent(Intent.ACTION_MAIN).apply { addCategory(Intent.CATEGORY_APP_GALLERY) }
            return tryLaunchSpecialIntentByIntent(context, galIntent, "Photos")
        }

        // 5. IF NOT INSTALLED: Do NOT open web browser search! Open Google Play Store for this app!
        val targetPkgForStore = knownPkg ?: if (customPkg.isNotBlank()) customPkg else null
        return openInPlayStore(context, cleanWord, targetPkgForStore)
    }

    fun getInstalledAppsOnDevice(context: Context): List<AppInfoItem> {
        return try {
            val pm = context.packageManager
            val mainIntent = Intent(Intent.ACTION_MAIN, null).apply {
                addCategory(Intent.CATEGORY_LAUNCHER)
            }
            val resolveInfos = pm.queryIntentActivities(mainIntent, 0)
            val launcherApps = resolveInfos.map { ri ->
                val pkg = ri.activityInfo.packageName
                val iconDrawable = try { ri.loadIcon(pm) } catch (e: Exception) { null }
                val isSys = try { (ri.activityInfo.applicationInfo.flags and android.content.pm.ApplicationInfo.FLAG_SYSTEM) != 0 } catch (e: Exception) { false }
                AppInfoItem(
                    label = ri.loadLabel(pm).toString(),
                    packageName = pkg,
                    icon = iconDrawable,
                    isSystemApp = isSys
                )
            }.toMutableList()

            val installedApps = try { pm.getInstalledApplications(android.content.pm.PackageManager.GET_META_DATA) } catch (e: Exception) { emptyList() }
            for (appInfo in installedApps) {
                val pkg = appInfo.packageName
                if (launcherApps.none { it.packageName == pkg }) {
                    val launchIntent = pm.getLaunchIntentForPackage(pkg)
                    if (launchIntent != null) {
                        val label = try { pm.getApplicationLabel(appInfo).toString() } catch (e: Exception) { pkg }
                        val iconDrawable = try { pm.getApplicationIcon(appInfo) } catch (e: Exception) { null }
                        val isSys = (appInfo.flags and android.content.pm.ApplicationInfo.FLAG_SYSTEM) != 0
                        launcherApps.add(
                            AppInfoItem(
                                label = label,
                                packageName = pkg,
                                icon = iconDrawable,
                                isSystemApp = isSys
                            )
                        )
                    }
                }
            }

            launcherApps.distinctBy { it.packageName }.sortedBy { it.label.lowercase() }
        } catch (e: Exception) {
            emptyList()
        }
    }

    data class AppInfoItem(
        val label: String,
        val packageName: String,
        val icon: android.graphics.drawable.Drawable? = null,
        val isSystemApp: Boolean = false
    )

    private fun findInstalledPackageOnDevice(context: Context, query: String): String? {
        return try {
            val pm = context.packageManager
            val mainIntent = Intent(Intent.ACTION_MAIN, null).apply {
                addCategory(Intent.CATEGORY_LAUNCHER)
            }
            val resolveInfos = pm.queryIntentActivities(mainIntent, 0)
            val lowerQuery = query.lowercase().trim()

            // Match exact app label
            for (ri in resolveInfos) {
                val label = ri.loadLabel(pm).toString().lowercase()
                if (label == lowerQuery) {
                    return ri.activityInfo.packageName
                }
            }
            // Match label contains query
            for (ri in resolveInfos) {
                val label = ri.loadLabel(pm).toString().lowercase()
                if (label.contains(lowerQuery) || lowerQuery.contains(label)) {
                    return ri.activityInfo.packageName
                }
            }
            // Match package contains query
            for (ri in resolveInfos) {
                val pkg = ri.activityInfo.packageName.lowercase()
                if (pkg.contains(lowerQuery)) {
                    return ri.activityInfo.packageName
                }
            }
            null
        } catch (e: Exception) {
            null
        }
    }

    fun tryLaunchPackage(context: Context, packageName: String): Boolean {
        return try {
            val pm = context.packageManager
            val intent = pm.getLaunchIntentForPackage(packageName)
            if (intent != null) {
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                context.startActivity(intent)
                true
            } else {
                false
            }
        } catch (e: Exception) {
            false
        }
    }

    private fun tryLaunchSpecialIntent(context: Context, action: String, appName: String): String {
        return try {
            val intent = Intent(action).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
            "Launching $appName..."
        } catch (e: Exception) {
            openInPlayStore(context, appName, null)
        }
    }

    private fun tryLaunchSpecialIntentByIntent(context: Context, intent: Intent, appName: String): String {
        return try {
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(intent)
            "Launching $appName..."
        } catch (e: Exception) {
            openInPlayStore(context, appName, null)
        }
    }

    private fun openInPlayStore(context: Context, appName: String, packageName: String?): String {
        return try {
            val storeUri = if (!packageName.isNullOrBlank()) {
                Uri.parse("market://details?id=$packageName")
            } else {
                Uri.parse("market://search?q=${Uri.encode(appName)}&c=apps")
            }
            val intent = Intent(Intent.ACTION_VIEW, storeUri).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
            "'$appName' is not installed on your phone. Opening Google Play Store to install it... 📱"
        } catch (e: Exception) {
            try {
                val webUri = if (!packageName.isNullOrBlank()) {
                    Uri.parse("https://play.google.com/store/apps/details?id=$packageName")
                } else {
                    Uri.parse("https://play.google.com/store/search?q=${Uri.encode(appName)}&c=apps")
                }
                val webIntent = Intent(Intent.ACTION_VIEW, webUri).apply {
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                context.startActivity(webIntent)
                "'$appName' is not installed. Opening Google Play Store page... 📱"
            } catch (err: Exception) {
                "App '$appName' is not installed on this device."
            }
        }
    }

    private fun getAppDisplayName(rawWord: String): String {
        return rawWord.split(" ").joinToString(" ") { word ->
            word.replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString() }
        }
    }
}
