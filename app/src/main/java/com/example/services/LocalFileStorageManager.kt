package com.example.services

import android.content.Context
import android.os.Environment
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class SystemFileItem(
    val name: String,
    val path: String,
    val sizeString: String,
    val lastModifiedString: String,
    val isDirectory: Boolean,
    val fileExtension: String,
    val itemCount: Int = 0
)

data class SystemStorageInfo(
    val currentPath: String,
    val parentPath: String?,
    val items: List<SystemFileItem>
)

object LocalFileStorageManager {

    private const val VEDRA_DIR_NAME = "vedra_data"
    private const val VEDRIVE_FILES_DIR_NAME = "vedrive_documents"

    private fun getVedraDataDir(context: Context): File {
        val dir = File(context.filesDir, VEDRA_DIR_NAME)
        if (!dir.exists()) {
            dir.mkdirs()
        }
        return dir
    }

    private fun getVedriveDocumentsDir(context: Context): File {
        val dir = File(getVedraDataDir(context), VEDRIVE_FILES_DIR_NAME)
        if (!dir.exists()) {
            dir.mkdirs()
        }
        return dir
    }

    // ================= 1. CHAT HISTORY LOCAL PERSISTENCE =================

    fun syncChatHistoryToLocalFiles(context: Context, chatList: List<ChatHistoryItem>) {
        try {
            val dir = getVedraDataDir(context)
            
            // 1. JSON Persistence: chat_history.json
            val jsonArray = JSONArray()
            chatList.forEach { chat ->
                val obj = JSONObject().apply {
                    put("id", chat.id)
                    put("session_title", chat.sessionTitle)
                    put("user_text", chat.userText)
                    put("ved_response", chat.vedResponse)
                    put("timestamp", chat.timestamp)
                    put("formatted_date", SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US).format(Date(chat.timestamp)))
                }
                jsonArray.put(obj)
            }
            val jsonFile = File(dir, "chat_history.json")
            FileOutputStream(jsonFile).use { it.write(jsonArray.toString(2).toByteArray(Charsets.UTF_8)) }

            // 2. Readable Text File: chat_history.txt
            val txtFile = File(dir, "chat_history.txt")
            val sb = StringBuilder()
            sb.append("=========================================\n")
            sb.append("VEDRA AI ASSISTANT - LOCAL CHAT HISTORY\n")
            sb.append("Total Sessions Saved: ${chatList.size}\n")
            sb.append("Last Updated: ${SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US).format(Date())}\n")
            sb.append("=========================================\n\n")

            chatList.forEachIndexed { index, chat ->
                val dateStr = SimpleDateFormat("MMM dd, yyyy - hh:mm a", Locale.US).format(Date(chat.timestamp))
                sb.append("-----------------------------------------\n")
                sb.append("SESSION #${chat.id} | $dateStr\n")
                sb.append("TITLE: ${chat.sessionTitle}\n")
                sb.append("USER: ${chat.userText}\n")
                sb.append("VEDRA: ${chat.vedResponse}\n\n")
            }

            FileOutputStream(txtFile).use { it.write(sb.toString().toByteArray(Charsets.UTF_8)) }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun readLocalChatHistoryJson(context: Context): String {
        return try {
            val jsonFile = File(getVedraDataDir(context), "chat_history.json")
            if (jsonFile.exists()) {
                jsonFile.readText(Charsets.UTF_8)
            } else {
                "[]"
            }
        } catch (e: Exception) {
            "[]"
        }
    }

    // ================= 2. VEDM-T KNOWLEDGE LOCAL PERSISTENCE =================

    fun syncVedmtToLocalFiles(context: Context, docs: List<DriveDocument>) {
        try {
            val dir = getVedraDataDir(context)
            val jsonArray = JSONArray()
            docs.forEach { doc ->
                val obj = JSONObject().apply {
                    put("id", doc.id)
                    put("folder_id", doc.folderId)
                    put("title", doc.title)
                    put("content", doc.content)
                    put("file_type", doc.fileType)
                    put("file_size", doc.fileSize)
                    put("created_at", doc.createdAt)
                }
                jsonArray.put(obj)
            }
            val jsonFile = File(dir, "vedmt_store.json")
            FileOutputStream(jsonFile).use { it.write(jsonArray.toString(2).toByteArray(Charsets.UTF_8)) }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    // ================= 3. VEDRIVE DOCUMENT PHYSICAL LOCAL FILE STORAGE =================

    fun saveDriveDocumentToLocalFile(context: Context, title: String, content: String, fileType: String): File? {
        return try {
            val dir = getVedriveDocumentsDir(context)
            val ext = when (fileType.uppercase(Locale.US)) {
                "PDF" -> "pdf"
                "JSON" -> "json"
                "DOC", "DOCX" -> "doc"
                "CODE", "KT", "PY", "JAVA", "CPP" -> "code"
                "PNG", "JPG", "IMG" -> "img"
                else -> "txt"
            }
            val sanitizedTitle = title.replace(Regex("""[^a-zA-Z0-9_\-\.]"""), "_")
            val fileName = if (sanitizedTitle.endsWith(".$ext", ignoreCase = true)) sanitizedTitle else "$sanitizedTitle.$ext"
            val file = File(dir, fileName)
            FileOutputStream(file).use { it.write(content.toByteArray(Charsets.UTF_8)) }
            file
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    fun deleteLocalDriveDocumentFile(context: Context, title: String): Boolean {
        return try {
            val dir = getVedriveDocumentsDir(context)
            val sanitizedTitle = title.replace(Regex("""[^a-zA-Z0-9_\-\.]"""), "_")
            var deleted = false
            dir.listFiles()?.forEach { f ->
                if (f.name.contains(sanitizedTitle, ignoreCase = true)) {
                    if (f.delete()) deleted = true
                }
            }
            deleted
        } catch (e: Exception) {
            false
        }
    }

    // ================= 4. SYSTEM STORAGE / FILE MANAGER BROWSER =================

    fun getSystemStorageFoldersAndFiles(context: Context, targetDirectoryPath: String? = null): SystemStorageInfo {
        val rootDir = if (!targetDirectoryPath.isNull_or_blank()) {
            File(targetDirectoryPath)
        } else {
            getVedraDataDir(context)
        }

        val activeDir = if (rootDir.exists() && rootDir.isDirectory) rootDir else context.filesDir
        val parentPath = activeDir.parentFile?.absolutePath

        val items = mutableListOf<SystemFileItem>()

        // Add standard root folders if browsing top-level app storage
        if (activeDir.absolutePath == getVedraDataDir(context).absolutePath) {
            val vedriveDocsDir = getVedriveDocumentsDir(context)
            items.add(
                SystemFileItem(
                    name = "VEDrive Documents",
                    path = vedriveDocsDir.absolutePath,
                    sizeString = "${vedriveDocsDir.listFiles()?.size ?: 0} files",
                    lastModifiedString = "System Dir",
                    isDirectory = true,
                    fileExtension = "FOLDER",
                    itemCount = vedriveDocsDir.listFiles()?.size ?: 0
                )
            )

            val extFilesDir = context.getExternalFilesDir(null)
            if (extFilesDir != null) {
                items.add(
                    SystemFileItem(
                        name = "External App Storage",
                        path = extFilesDir.absolutePath,
                        sizeString = "${extFilesDir.listFiles()?.size ?: 0} items",
                        lastModifiedString = "External Dir",
                        isDirectory = true,
                        fileExtension = "FOLDER",
                        itemCount = extFilesDir.listFiles()?.size ?: 0
                    )
                )
            }
        }

        try {
            val filesList = activeDir.listFiles() ?: emptyArray()
            filesList.sortedWith(compareBy({ !it.isDirectory }, { it.name.lowercase() })).forEach { file ->
                // Skip if duplicate of root item
                if (file.name == VEDRIVE_FILES_DIR_NAME && activeDir.absolutePath == getVedraDataDir(context).absolutePath) {
                    return@forEach
                }

                val isDir = file.isDirectory
                val sizeStr = if (isDir) {
                    val count = file.listFiles()?.size ?: 0
                    "$count items"
                } else {
                    formatFileSize(file.length())
                }
                val dateStr = SimpleDateFormat("dd MMM, hh:mm a", Locale.US).format(Date(file.lastModified()))
                val ext = if (isDir) "FOLDER" else file.extension.uppercase(Locale.US).ifEmpty { "FILE" }

                items.add(
                    SystemFileItem(
                        name = file.name,
                        path = file.absolutePath,
                        sizeString = sizeStr,
                        lastModifiedString = dateStr,
                        isDirectory = isDir,
                        fileExtension = ext,
                        itemCount = if (isDir) (file.listFiles()?.size ?: 0) else 0
                    )
                )
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        return SystemStorageInfo(
            currentPath = activeDir.absolutePath,
            parentPath = if (activeDir.absolutePath != context.filesDir.absolutePath) parentPath else null,
            items = items
        )
    }

    fun createSystemStorageFile(directoryPath: String, fileName: String, content: String): Boolean {
        return try {
            val dir = File(directoryPath)
            if (!dir.exists()) dir.mkdirs()
            val file = File(dir, fileName)
            FileOutputStream(file).use { it.write(content.toByteArray(Charsets.UTF_8)) }
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    private fun String?.isNull_or_blank(): Boolean {
        return this == null || this.isBlank()
    }

    private fun formatFileSize(bytes: Long): String {
        return when {
            bytes < 1024 -> "$bytes B"
            bytes < 1024 * 1024 -> String.format(Locale.US, "%.1f KB", bytes / 1024.0)
            else -> String.format(Locale.US, "%.1f MB", bytes / (1024.0 * 1024.0))
        }
    }
}
