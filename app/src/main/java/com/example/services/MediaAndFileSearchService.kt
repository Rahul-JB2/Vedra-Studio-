package com.example.services

import android.content.ContentResolver
import android.content.ContentUris
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.MediaStore
import java.net.URLEncoder
import java.util.Locale

object MediaAndFileSearchService {

    data class MediaItem(
        val id: Long,
        val uri: Uri,
        val title: String,
        val mimeType: String,
        val path: String?
    )

    /**
     * Search local device video storage for a movie or video matching the query.
     * If found, launches the video using installed system video player.
     * If not found, launches YouTube or default installed video player search.
     */
    fun searchAndPlayVideo(context: Context, query: String, dbService: DatabaseService? = null): UtilityResult {
        val cleanQuery = query.replace("play movie", "", ignoreCase = true)
            .replace("play video", "", ignoreCase = true)
            .replace("play", "", ignoreCase = true)
            .replace("movie", "", ignoreCase = true)
            .replace("video", "", ignoreCase = true)
            .replace("kholo", "", ignoreCase = true)
            .replace("chalao", "", ignoreCase = true)
            .trim()

        if (cleanQuery.isBlank()) {
            return UtilityResult(true, "Please specify a movie or video name to play.", "VIDEO_SEARCH")
        }

        dbService?.logUserBehavior("PLAY_VIDEO", cleanQuery)

        val localVideo = findLocalVideo(context, cleanQuery)
        if (localVideo != null) {
            return try {
                val intent = Intent(Intent.ACTION_VIEW).apply {
                    setDataAndType(localVideo.uri, localVideo.mimeType.ifEmpty { "video/*" })
                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                context.startActivity(intent)
                UtilityResult(
                    isHandled = true,
                    responseMessage = "Found '${localVideo.title}' in phone storage! Opening in video player... 🎬",
                    actionType = "PLAY_VIDEO"
                )
            } catch (e: Exception) {
                UtilityResult(true, "Found '${localVideo.title}' on phone, but no compatible video player app was found.", "PLAY_VIDEO")
            }
        }

        // Not found locally: Launch installed video player / YouTube search
        return try {
            val encoded = URLEncoder.encode(cleanQuery, "UTF-8")
            val ytUri = Uri.parse("https://www.youtube.com/results?search_query=$encoded")
            val intent = Intent(Intent.ACTION_VIEW, ytUri).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
            UtilityResult(
                isHandled = true,
                responseMessage = "No video/movie named '$cleanQuery' was found in local phone storage. Searching '$cleanQuery' in default video player / YouTube... 🎬",
                actionType = "PLAY_VIDEO"
            )
        } catch (e: Exception) {
            UtilityResult(true, "No video/movie named '$cleanQuery' was found in your device storage.", "PLAY_VIDEO")
        }
    }

    /**
     * Search local device music/audio storage for a song or track matching query.
     * If found, plays via installed music player.
     * If not found, opens Spotify/default music app search.
     */
    fun searchAndPlayAudio(context: Context, query: String, dbService: DatabaseService? = null): UtilityResult {
        val cleanQuery = query.replace("play song", "", ignoreCase = true)
            .replace("play music", "", ignoreCase = true)
            .replace("play audio", "", ignoreCase = true)
            .replace("play track", "", ignoreCase = true)
            .replace("play", "", ignoreCase = true)
            .replace("gaana", "", ignoreCase = true)
            .replace("song", "", ignoreCase = true)
            .replace("music", "", ignoreCase = true)
            .trim()

        if (cleanQuery.isBlank()) {
            return UtilityResult(true, "Please specify a song or music name to play.", "AUDIO_SEARCH")
        }

        dbService?.logUserBehavior("PLAY_MUSIC", cleanQuery)

        val localAudio = findLocalAudio(context, cleanQuery)
        if (localAudio != null) {
            return try {
                val intent = Intent(Intent.ACTION_VIEW).apply {
                    setDataAndType(localAudio.uri, localAudio.mimeType.ifEmpty { "audio/*" })
                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                context.startActivity(intent)
                UtilityResult(
                    isHandled = true,
                    responseMessage = "Found '${localAudio.title}' in phone storage! Playing in music player... 🎵",
                    actionType = "PLAY_MUSIC"
                )
            } catch (e: Exception) {
                UtilityResult(true, "Found '${localAudio.title}' on phone storage.", "PLAY_MUSIC")
            }
        }

        // Not found locally: Open Spotify/Music App
        val spotifyMsg = UtilityService.openMusicOrSpotify(context, cleanQuery)
        return UtilityResult(
            isHandled = true,
            responseMessage = "No song named '$cleanQuery' found in local phone storage. $spotifyMsg",
            actionType = "PLAY_MUSIC"
        )
    }

    /**
     * Searches device storage for any file (documents, pdfs, images, zip, etc.) matching the filename.
     * Opens it with the user's preferred installed file viewer if found.
     */
    fun searchAndOpenFile(context: Context, query: String, dbService: DatabaseService? = null): UtilityResult {
        val cleanQuery = query.replace("search file", "", ignoreCase = true)
            .replace("find file", "", ignoreCase = true)
            .replace("file search", "", ignoreCase = true)
            .replace("open file", "", ignoreCase = true)
            .replace("check file", "", ignoreCase = true)
            .replace("file hai ki nahi", "", ignoreCase = true)
            .replace("search for file", "", ignoreCase = true)
            .replace("search", "", ignoreCase = true)
            .replace("file", "", ignoreCase = true)
            .replace("kholo", "", ignoreCase = true)
            .trim()

        if (cleanQuery.isBlank()) {
            return UtilityResult(true, "Please specify a file name to search in phone storage.", "FILE_SEARCH")
        }

        dbService?.logUserBehavior("FILE_SEARCH", cleanQuery)

        val foundFile = findLocalFile(context, cleanQuery)
        if (foundFile != null) {
            return try {
                val intent = Intent(Intent.ACTION_VIEW).apply {
                    setDataAndType(foundFile.uri, foundFile.mimeType.ifEmpty { "*/*" })
                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                context.startActivity(intent)
                UtilityResult(
                    isHandled = true,
                    responseMessage = "Found file '${foundFile.title}' in phone storage! Opening file... 📄",
                    actionType = "FILE_SEARCH"
                )
            } catch (e: Exception) {
                UtilityResult(
                    isHandled = true,
                    responseMessage = "Found file '${foundFile.title}' in phone storage at path: ${foundFile.path ?: "Storage"} 📄",
                    actionType = "FILE_SEARCH"
                )
            }
        }

        return UtilityResult(
            isHandled = true,
            responseMessage = "Searched phone system: No file matching '$cleanQuery' was found in your device storage.",
            actionType = "FILE_SEARCH"
        )
    }

    private fun findLocalVideo(context: Context, titleQuery: String): MediaItem? {
        val resolver: ContentResolver = context.contentResolver
        val uri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI
        val projection = arrayOf(
            MediaStore.Video.Media._ID,
            MediaStore.Video.Media.TITLE,
            MediaStore.Video.Media.DISPLAY_NAME,
            MediaStore.Video.Media.MIME_TYPE,
            MediaStore.Video.Media.DATA
        )

        val selection = "${MediaStore.Video.Media.TITLE} LIKE ? OR ${MediaStore.Video.Media.DISPLAY_NAME} LIKE ?"
        val selectionArgs = arrayOf("%$titleQuery%", "%$titleQuery%")

        try {
            resolver.query(uri, projection, selection, selectionArgs, "${MediaStore.Video.Media.DATE_MODIFIED} DESC")?.use { cursor ->
                if (cursor.moveToFirst()) {
                    val id = cursor.getLong(cursor.getColumnIndexOrThrow(MediaStore.Video.Media._ID))
                    val title = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Video.Media.DISPLAY_NAME))
                        ?: cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Video.Media.TITLE)) ?: titleQuery
                    val mimeType = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Video.Media.MIME_TYPE)) ?: "video/*"
                    val path = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Video.Media.DATA))
                    val contentUri = ContentUris.withAppendedId(MediaStore.Video.Media.EXTERNAL_CONTENT_URI, id)
                    return MediaItem(id, contentUri, title, mimeType, path)
                }
            }
        } catch (e: Exception) {
            // Permission or cursor exception handled safely
        }
        return null
    }

    private fun findLocalAudio(context: Context, titleQuery: String): MediaItem? {
        val resolver: ContentResolver = context.contentResolver
        val uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
        val projection = arrayOf(
            MediaStore.Audio.Media._ID,
            MediaStore.Audio.Media.TITLE,
            MediaStore.Audio.Media.DISPLAY_NAME,
            MediaStore.Audio.Media.MIME_TYPE,
            MediaStore.Audio.Media.DATA
        )

        val selection = "${MediaStore.Audio.Media.TITLE} LIKE ? OR ${MediaStore.Audio.Media.DISPLAY_NAME} LIKE ?"
        val selectionArgs = arrayOf("%$titleQuery%", "%$titleQuery%")

        try {
            resolver.query(uri, projection, selection, selectionArgs, "${MediaStore.Audio.Media.DATE_MODIFIED} DESC")?.use { cursor ->
                if (cursor.moveToFirst()) {
                    val id = cursor.getLong(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media._ID))
                    val title = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DISPLAY_NAME))
                        ?: cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.TITLE)) ?: titleQuery
                    val mimeType = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.MIME_TYPE)) ?: "audio/*"
                    val path = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DATA))
                    val contentUri = ContentUris.withAppendedId(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, id)
                    return MediaItem(id, contentUri, title, mimeType, path)
                }
            }
        } catch (e: Exception) {
            // Permission or cursor exception handled safely
        }
        return null
    }

    private fun findLocalFile(context: Context, fileQuery: String): MediaItem? {
        val resolver: ContentResolver = context.contentResolver
        val uri = MediaStore.Files.getContentUri("external")
        val projection = arrayOf(
            MediaStore.Files.FileColumns._ID,
            MediaStore.Files.FileColumns.DISPLAY_NAME,
            MediaStore.Files.FileColumns.MIME_TYPE,
            MediaStore.Files.FileColumns.DATA
        )

        val selection = "${MediaStore.Files.FileColumns.DISPLAY_NAME} LIKE ?"
        val selectionArgs = arrayOf("%$fileQuery%")

        try {
            resolver.query(uri, projection, selection, selectionArgs, "${MediaStore.Files.FileColumns.DATE_MODIFIED} DESC")?.use { cursor ->
                if (cursor.moveToFirst()) {
                    val id = cursor.getLong(cursor.getColumnIndexOrThrow(MediaStore.Files.FileColumns._ID))
                    val name = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Files.FileColumns.DISPLAY_NAME)) ?: fileQuery
                    val mimeType = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Files.FileColumns.MIME_TYPE)) ?: "*/*"
                    val path = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Files.FileColumns.DATA))
                    val contentUri = ContentUris.withAppendedId(MediaStore.Files.getContentUri("external"), id)
                    return MediaItem(id, contentUri, name, mimeType, path)
                }
            }
        } catch (e: Exception) {
            // Permission or cursor exception handled safely
        }
        return null
    }
}
