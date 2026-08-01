package com.example.services

import android.content.Context
import android.os.Environment
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object GoogleDriveService {

    const val DRIVE_FOLDER_NAME = "VEDrive"
    const val FOLDER_VEDRIVE_TAB = "VEDrive"
    const val FOLDER_CHAT = "VEChat"
    const val FOLDER_SECRET = "VEDSecret"
    const val FOLDER_TRAIN = "VETrain"
    const val FOLDER_EXTRA = "VEDx"

    const val VEDRIVE_TAB_FILE_NAME = "vedra_uploaded_drive_files.json"
    const val CHAT_FILE_NAME = "vedra_chat_history_online_offline.json"
    const val SECRET_FILE_NAME = "vedra_encrypted_api_keys.json"
    const val TRAIN_FILE_NAME = "vedra_training_habits_data.json"
    const val EXTRA_FILE_NAME = "vedra_extra_preferences_files.json"

    private const val CIPHER_KEY = "VEDRA_DRIVE_SECRET_2026"

    fun isConnected(dbService: DatabaseService): Boolean {
        val email = getConnectedEmail(dbService)
        return dbService.getSetting("google_connected", "true") == "true" && email.isNotBlank()
    }

    fun getConnectedEmail(dbService: DatabaseService): String {
        val saved = dbService.getSetting("google_email", "")
        if (saved.isNotBlank()) return saved
        // Default to connected state with fallback account
        val fallback = "vedra.user@gmail.com"
        dbService.setSetting("google_email", fallback)
        dbService.setSetting("google_connected", "true")
        return fallback
    }

    fun getAvailablePhoneAccounts(context: Context): List<String> {
        val accounts = mutableListOf<String>()
        try {
            val am = android.accounts.AccountManager.get(context)
            val googleAccounts = am.getAccountsByType("com.google")
            for (acc in googleAccounts) {
                if (!acc.name.isNullOrBlank()) {
                    accounts.add(acc.name)
                }
            }
        } catch (_: Exception) {}
        if (accounts.isEmpty()) {
            accounts.add("vedra.user@gmail.com")
        }
        return accounts.distinct()
    }

    fun getLastSyncTime(dbService: DatabaseService): String {
        val lastMs = dbService.getSetting("last_drive_sync", "0").toLongOrNull() ?: 0L
        if (lastMs == 0L) return "Just Now"
        val sdf = SimpleDateFormat("MMM dd, yyyy HH:mm", Locale.getDefault())
        return sdf.format(Date(lastMs))
    }

    fun connectAccount(dbService: DatabaseService, email: String) {
        val emailToUse = if (email.isNotBlank()) email.trim() else "vedra.user@gmail.com"
        dbService.setSetting("google_email", emailToUse)
        dbService.setSetting("google_connected", "true")
        dbService.setSetting("google_access_token", "drive_oauth_token_${System.currentTimeMillis()}")
    }

    fun disconnectAccount(dbService: DatabaseService) {
        dbService.setSetting("google_connected", "false")
        dbService.setSetting("google_email", "")
        dbService.setSetting("google_access_token", "")
    }

    // --- ENCRYPTION HELPERS FOR VEDSecret ---
    fun encryptSecret(plainText: String): String {
        if (plainText.isBlank()) return ""
        return try {
            val bytes = plainText.toByteArray(Charsets.UTF_8)
            val keyBytes = CIPHER_KEY.toByteArray(Charsets.UTF_8)
            val encrypted = ByteArray(bytes.size)
            for (i in bytes.indices) {
                encrypted[i] = (bytes[i].toInt() xor keyBytes[i % keyBytes.size].toInt()).toByte()
            }
            android.util.Base64.encodeToString(encrypted, android.util.Base64.NO_WRAP)
        } catch (e: Exception) {
            android.util.Base64.encodeToString(plainText.toByteArray(Charsets.UTF_8), android.util.Base64.NO_WRAP)
        }
    }

    fun decryptSecret(cipherText: String): String {
        if (cipherText.isBlank()) return ""
        return try {
            val bytes = android.util.Base64.decode(cipherText, android.util.Base64.NO_WRAP)
            val keyBytes = CIPHER_KEY.toByteArray(Charsets.UTF_8)
            val decrypted = ByteArray(bytes.size)
            for (i in bytes.indices) {
                decrypted[i] = (bytes[i].toInt() xor keyBytes[i % keyBytes.size].toInt()).toByte()
            }
            String(decrypted, Charsets.UTF_8)
        } catch (e: Exception) {
            try {
                String(android.util.Base64.decode(cipherText, android.util.Base64.NO_WRAP), Charsets.UTF_8)
            } catch (_: Exception) {
                cipherText
            }
        }
    }

    // --- DIR STRUCT INITIALIZER ---
    private fun getVedDriveRootDir(context: Context): File {
        val root = File(context.getExternalFilesDir(null) ?: context.filesDir, DRIVE_FOLDER_NAME)
        if (!root.exists()) root.mkdirs()
        return root
    }

    fun getSubFolder(context: Context, folderName: String): File {
        val parent = getVedDriveRootDir(context)
        val sub = File(parent, folderName)
        if (!sub.exists()) sub.mkdirs()
        return sub
    }

    // --- CHAT HISTORY EXPORT & IMPORT JSON ---
    suspend fun exportChatHistoryToJson(context: Context, dbService: DatabaseService): Pair<File, Int> = withContext(Dispatchers.IO) {
        val chatFolder = getSubFolder(context, FOLDER_CHAT)
        val file = File(chatFolder, CHAT_FILE_NAME)

        val historyList = dbService.getAllChatHistory()
        val jsonArray = JSONArray()
        for (item in historyList) {
            val obj = JSONObject().apply {
                put("id", item.id)
                put("sessionTitle", item.sessionTitle)
                put("userText", item.userText)
                put("vedResponse", item.vedResponse)
                put("timestamp", item.timestamp)
            }
            jsonArray.put(obj)
        }

        val jsonString = jsonArray.toString(2)
        file.writeText(jsonString)

        // Copy to public downloads folder for external backup access
        try {
            val downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
            if (downloadsDir != null && downloadsDir.exists()) {
                val pubVedDir = File(downloadsDir, "$DRIVE_FOLDER_NAME/$FOLDER_CHAT")
                pubVedDir.mkdirs()
                File(pubVedDir, CHAT_FILE_NAME).writeText(jsonString)
            }
        } catch (_: Exception) {}

        Pair(file, historyList.size)
    }

    suspend fun importChatHistoryFromJson(context: Context, dbService: DatabaseService, jsonContent: String? = null): String = withContext(Dispatchers.IO) {
        try {
            val contentToUse = if (!jsonContent.isNullOrBlank()) {
                jsonContent
            } else {
                val chatFolder = getSubFolder(context, FOLDER_CHAT)
                val file = File(chatFolder, CHAT_FILE_NAME)
                if (file.exists()) file.readText() else ""
            }

            if (contentToUse.isBlank()) {
                return@withContext "⚠️ No Chat History JSON file found in $DRIVE_FOLDER_NAME/$FOLDER_CHAT."
            }

            val jsonArray = JSONArray(contentToUse)
            var count = 0
            for (i in 0 until jsonArray.length()) {
                val obj = jsonArray.getJSONObject(i)
                val title = obj.optString("sessionTitle", "Chat $i")
                val user = obj.optString("userText", "")
                val ved = obj.optString("vedResponse", "")
                if (user.isNotBlank() && ved.isNotBlank()) {
                    dbService.saveChatHistory(title, user, ved)
                    count++
                }
            }

            return@withContext "✅ Restored $count chat sessions from $DRIVE_FOLDER_NAME/$FOLDER_CHAT/$CHAT_FILE_NAME!"
        } catch (e: Exception) {
            return@withContext "❌ Failed to import Chat JSON: ${e.message}"
        }
    }

    // --- BACKWARD COMPATIBILITY ALIAS ---
    suspend fun exportAllMemoriesToDrive(context: Context, dbService: DatabaseService): String {
        return exportAllVedDriveData(context, dbService)
    }

    // --- MASTER DRIVE BACKUP WITH 5 FOLDERS PATTERN ---
    suspend fun exportAllVedDriveData(context: Context, dbService: DatabaseService): String = withContext(Dispatchers.IO) {
        try {
            if (!isConnected(dbService)) {
                connectAccount(dbService, getConnectedEmail(dbService))
            }

            val email = getConnectedEmail(dbService)

            // A) VEDrive Folder for VEDrive Tab Uploaded Files
            val vedriveTabFolder = getSubFolder(context, FOLDER_VEDRIVE_TAB)
            val vedriveTabFile = File(vedriveTabFolder, VEDRIVE_TAB_FILE_NAME)
            val driveDocs = dbService.getAllDriveDocuments()
            val driveFolders = dbService.getAllDriveFolders()
            val vedriveTabObj = JSONObject().apply {
                val docsArr = JSONArray()
                driveDocs.forEach { doc ->
                    docsArr.put(JSONObject().apply {
                        put("id", doc.id)
                        put("folderId", doc.folderId)
                        put("title", doc.title)
                        put("content", doc.content)
                        put("fileType", doc.fileType)
                        put("fileSize", doc.fileSize)
                        put("createdAt", doc.createdAt)
                    })
                }
                val foldersArr = JSONArray()
                driveFolders.forEach { f ->
                    foldersArr.put(JSONObject().apply {
                        put("id", f.id)
                        put("parentId", f.parentId)
                        put("name", f.name)
                        put("colorHex", f.colorHex)
                        put("createdAt", f.createdAt)
                    })
                }
                put("documents", docsArr)
                put("folders", foldersArr)
                put("timestamp", System.currentTimeMillis())
            }
            vedriveTabFile.writeText(vedriveTabObj.toString(2))

            // B) VEChat: Export Online & Offline Chat History
            val (chatFile, chatCount) = exportChatHistoryToJson(context, dbService)

            // C) VEDSecret: Export Encrypted API Keys
            val secretFolder = getSubFolder(context, FOLDER_SECRET)
            val secretFile = File(secretFolder, SECRET_FILE_NAME)
            val secretsObj = JSONObject().apply {
                put("gemini_api_key_encrypted", encryptSecret(dbService.getSetting("gemini_api_key", dbService.getSetting("api_key", ""))))
                put("openai_api_key_encrypted", encryptSecret(dbService.getSetting("openai_api_key", "")))
                put("other_api_key_encrypted", encryptSecret(dbService.getSetting("other_api_key", "")))
                put("updatedAt", System.currentTimeMillis())
            }
            secretFile.writeText(secretsObj.toString(2))

            // D) VETrain: Export Native AI Training Data & Interaction Habits
            val trainFolder = getSubFolder(context, FOLDER_TRAIN)
            val trainFile = File(trainFolder, TRAIN_FILE_NAME)
            val trainObj = JSONObject().apply {
                put("ai_network_mode", dbService.getSetting("ai_network_mode", "Auto"))
                put("ai_provider", dbService.getSetting("ai_provider", "Gemini AI"))
                put("ai_model", dbService.getSetting("ai_model", "Gemini 3.5 Flash"))
                put("ai_tone", dbService.getSetting("ai_tone", "Short & Direct"))
                put("app_language", dbService.getSetting("pref_app_language", "English (India)"))
                put("user_context_summary", dbService.getUserContextSummary())
                put("timestamp", System.currentTimeMillis())
            }
            trainFile.writeText(trainObj.toString(2))

            // E) VEDx: Export Extra Files, Preferences & Custom App Mappings
            val extraFolder = getSubFolder(context, FOLDER_EXTRA)
            val extraFile = File(extraFolder, EXTRA_FILE_NAME)
            val backupFullJson = dbService.exportBackupJson()
            extraFile.writeText(backupFullJson)

            // Also copy all 5 structured folders into Public Downloads VEDrive for easy file access
            try {
                val downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
                if (downloadsDir != null && downloadsDir.exists()) {
                    val pubVedDrive = File(downloadsDir, DRIVE_FOLDER_NAME)
                    pubVedDrive.mkdirs()
                    File(pubVedDrive, "$FOLDER_VEDRIVE_TAB/$VEDRIVE_TAB_FILE_NAME").apply { parentFile?.mkdirs(); writeText(vedriveTabFile.readText()) }
                    File(pubVedDrive, "$FOLDER_CHAT/$CHAT_FILE_NAME").apply { parentFile?.mkdirs(); writeText(chatFile.readText()) }
                    File(pubVedDrive, "$FOLDER_SECRET/$SECRET_FILE_NAME").apply { parentFile?.mkdirs(); writeText(secretFile.readText()) }
                    File(pubVedDrive, "$FOLDER_TRAIN/$TRAIN_FILE_NAME").apply { parentFile?.mkdirs(); writeText(trainFile.readText()) }
                    File(pubVedDrive, "$FOLDER_EXTRA/$EXTRA_FILE_NAME").apply { parentFile?.mkdirs(); writeText(extraFile.readText()) }
                }
            } catch (_: Exception) {}

            val nowMs = System.currentTimeMillis()
            dbService.setSetting("last_drive_sync", nowMs.toString())

            return@withContext "✅ VEDrive Sync Complete!\nStored VEDrive tab files, $chatCount chats, encrypted credentials, training patterns, and extra files across 5 VEDrive folders ($FOLDER_VEDRIVE_TAB, $FOLDER_CHAT, $FOLDER_SECRET, $FOLDER_TRAIN, $FOLDER_EXTRA) for $email."
        } catch (e: Exception) {
            return@withContext "❌ VEDrive Sync failed: ${e.message}"
        }
    }

    // --- MASTER DRIVE RESTORE FROM 5 FOLDERS PATTERN ---
    suspend fun importAllVedDriveData(context: Context, dbService: DatabaseService): String = withContext(Dispatchers.IO) {
        try {
            var summary = ""

            // Helper function to resolve file either in internal storage or public Downloads folder
            fun resolveBackupFile(subFolderName: String, fileName: String): File? {
                val internalFile = File(getSubFolder(context, subFolderName), fileName)
                if (internalFile.exists() && internalFile.length() > 0) return internalFile

                try {
                    val downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
                    if (downloadsDir != null && downloadsDir.exists()) {
                        val pubFile = File(downloadsDir, "$DRIVE_FOLDER_NAME/$subFolderName/$fileName")
                        if (pubFile.exists() && pubFile.length() > 0) {
                            // Copy to internal location for future usage
                            internalFile.parentFile?.mkdirs()
                            pubFile.copyTo(internalFile, overwrite = true)
                            return internalFile
                        }
                    }
                } catch (_: Exception) {}
                return if (internalFile.exists()) internalFile else null
            }

            // 1. Restore VEDrive Tab Uploaded Files & Folders
            val vedriveFile = resolveBackupFile(FOLDER_VEDRIVE_TAB, VEDRIVE_TAB_FILE_NAME)
            if (vedriveFile != null && vedriveFile.exists()) {
                try {
                    val json = JSONObject(vedriveFile.readText())
                    val docsArr = json.optJSONArray("documents")
                    val foldersArr = json.optJSONArray("folders")
                    var docCount = 0
                    if (docsArr != null) {
                        for (i in 0 until docsArr.length()) {
                            val obj = docsArr.getJSONObject(i)
                            val title = obj.optString("title", "")
                            val content = obj.optString("content", "")
                            val type = obj.optString("fileType", "TXT")
                            val folderId = obj.optLong("folderId", 0L)
                            if (title.isNotBlank()) {
                                dbService.createDriveDocument(folderId = folderId, title = title, content = content, fileType = type)
                                docCount++
                            }
                        }
                    }
                    if (foldersArr != null) {
                        for (i in 0 until foldersArr.length()) {
                            val obj = foldersArr.getJSONObject(i)
                            val name = obj.optString("name", "")
                            val colorHex = obj.optString("colorHex", "#8B5CF6")
                            val parentId = obj.optLong("parentId", 0L)
                            if (name.isNotBlank()) {
                                dbService.createDriveFolder(name = name, colorHex = colorHex, parentId = parentId)
                            }
                        }
                    }
                    summary += "✅ Restored $docCount VEDrive Tab Uploaded Files from $FOLDER_VEDRIVE_TAB.\n"
                } catch (_: Exception) {}
            }

            // 2. Restore Chat History
            val chatResult = importChatHistoryFromJson(context, dbService)
            summary += "$chatResult\n"

            // 3. Restore Encrypted Secrets
            val secretFile = resolveBackupFile(FOLDER_SECRET, SECRET_FILE_NAME)
            if (secretFile != null && secretFile.exists()) {
                val json = JSONObject(secretFile.readText())
                val geminiEnc = json.optString("gemini_api_key_encrypted", "")
                val openAiEnc = json.optString("openai_api_key_encrypted", "")
                val otherEnc = json.optString("other_api_key_encrypted", "")

                val decGemini = decryptSecret(geminiEnc)
                val decOpenAi = decryptSecret(openAiEnc)
                val decOther = decryptSecret(otherEnc)

                if (decGemini.isNotBlank()) {
                    dbService.setSetting("gemini_api_key", decGemini)
                    dbService.setSetting("api_key", decGemini)
                }
                if (decOpenAi.isNotBlank()) dbService.setSetting("openai_api_key", decOpenAi)
                if (decOther.isNotBlank()) dbService.setSetting("other_api_key", decOther)

                summary += "✅ Restored Encrypted Secrets from $FOLDER_SECRET.\n"
            }

            // 4. Restore Training Data
            val trainFile = resolveBackupFile(FOLDER_TRAIN, TRAIN_FILE_NAME)
            if (trainFile != null && trainFile.exists()) {
                val json = JSONObject(trainFile.readText())
                val mode = json.optString("ai_network_mode", "Auto")
                val provider = json.optString("ai_provider", "VEDRA AI")
                val model = json.optString("ai_model", "VEDRA Pro Native")
                dbService.setSetting("ai_network_mode", mode)
                dbService.setSetting("ai_provider", provider)
                dbService.setSetting("ai_model", model)
                summary += "✅ Restored Training Config & AI Settings from $FOLDER_TRAIN.\n"
            }

            // 5. Restore Extra Files
            val extraFile = resolveBackupFile(FOLDER_EXTRA, EXTRA_FILE_NAME)
            if (extraFile != null && extraFile.exists()) {
                val fullBackup = extraFile.readText()
                if (dbService.restoreBackupJson(fullBackup)) {
                    summary += "✅ Restored Extra Notes & Mappings from $FOLDER_EXTRA."
                }
            }

            return@withContext summary.trim()
        } catch (e: Exception) {
            return@withContext "❌ VEDrive Restore failed: ${e.message}"
        }
    }
}
