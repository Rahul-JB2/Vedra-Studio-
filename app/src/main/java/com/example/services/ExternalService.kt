package com.example.services

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.view.WindowManager

object ExternalService {

    fun openNavigation(context: Context, destination: String): String {
        return try {
            val encoded = Uri.encode(destination)
            val uri = Uri.parse("geo:0,0?q=$encoded")
            val mapIntent = Intent(Intent.ACTION_VIEW, uri).apply {
                setPackage("com.google.android.apps.maps")
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            if (mapIntent.resolveActivity(context.packageManager) != null) {
                context.startActivity(mapIntent)
            } else {
                val webIntent = Intent(Intent.ACTION_VIEW, Uri.parse("https://www.google.com/maps/search/?api=1&query=$encoded")).apply {
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                context.startActivity(webIntent)
            }
            "Opening navigation to '$destination'..."
        } catch (e: Exception) {
            "Unable to launch maps for '$destination'."
        }
    }

    fun sendEmail(context: Context, recipient: String, subject: String, body: String): String {
        return try {
            val intent = Intent(Intent.ACTION_SENDTO).apply {
                data = Uri.parse("mailto:")
                putExtra(Intent.EXTRA_EMAIL, arrayOf(recipient))
                putExtra(Intent.EXTRA_SUBJECT, subject)
                putExtra(Intent.EXTRA_TEXT, body)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
            "Opening email composer for '$recipient'..."
        } catch (e: Exception) {
            "No email app found on device."
        }
    }

    fun searchWeb(context: Context, query: String): String {
        return try {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://www.google.com/search?q=${Uri.encode(query)}")).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
            "Searching web for '$query'..."
        } catch (e: Exception) {
            "Unable to perform web search."
        }
    }

    fun openYouTube(context: Context, searchQuery: String): String {
        return try {
            val trimmed = searchQuery.trim()
            val pm = context.packageManager
            val isYtInstalled = pm.getLaunchIntentForPackage("com.google.android.youtube") != null ||
                    pm.getLaunchIntentForPackage("com.google.android.youtube.tv") != null

            if (trimmed.isBlank()) {
                val launchIntent = pm.getLaunchIntentForPackage("com.google.android.youtube")
                if (launchIntent != null) {
                    launchIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    context.startActivity(launchIntent)
                    return "Opening YouTube app..."
                } else {
                    val webIntent = Intent(Intent.ACTION_VIEW, Uri.parse("https://www.youtube.com")).apply {
                        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    }
                    context.startActivity(webIntent)
                    return "Opening YouTube in browser..."
                }
            }

            val encoded = Uri.encode(trimmed)
            val searchUri = Uri.parse("https://www.youtube.com/results?search_query=$encoded")
            val intent = Intent(Intent.ACTION_VIEW, searchUri).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                if (isYtInstalled) {
                    setPackage("com.google.android.youtube")
                }
            }

            context.startActivity(intent)
            "Opening YouTube and searching for '$trimmed'..."
        } catch (e: Exception) {
            try {
                val encoded = Uri.encode(searchQuery.trim())
                val webIntent = Intent(Intent.ACTION_VIEW, Uri.parse("https://www.youtube.com/results?search_query=$encoded")).apply {
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                context.startActivity(webIntent)
                "Searching YouTube for '$searchQuery'..."
            } catch (ex: Exception) {
                "Unable to open YouTube: ${ex.localizedMessage}"
            }
        }
    }

    fun handleYouTubeIntent(context: Context, text: String): UtilityResult? {
        val lower = text.trim().lowercase()

        // 1. YouTube explicitly mentioned
        if (lower.contains("youtube") || lower.contains("yt ") || lower.endsWith(" yt") || lower == "yt") {
            if (lower == "open youtube" || lower == "launch youtube" || lower == "start youtube" || lower == "youtube" || lower == "yt") {
                val msg = openYouTube(context, "")
                return UtilityResult(true, msg, "YOUTUBE")
            }

            var query = text
                .replace("open youtube and search for ", "", ignoreCase = true)
                .replace("open youtube and search ", "", ignoreCase = true)
                .replace("open youtube search for ", "", ignoreCase = true)
                .replace("open youtube search ", "", ignoreCase = true)
                .replace("open youtube and play ", "", ignoreCase = true)
                .replace("open youtube play ", "", ignoreCase = true)
                .replace("open youtube and ", "", ignoreCase = true)
                .replace("open youtube ", "", ignoreCase = true)
                .replace("launch youtube ", "", ignoreCase = true)
                .replace("start youtube ", "", ignoreCase = true)
                .replace("youtube ", "", ignoreCase = true)
                .replace("search for ", "", ignoreCase = true)
                .replace("search ", "", ignoreCase = true)
                .replace("play ", "", ignoreCase = true)
                .replace("on youtube", "", ignoreCase = true)
                .replace("in youtube", "", ignoreCase = true)
                .replace("via youtube", "", ignoreCase = true)
                .replace("on yt", "", ignoreCase = true)
                .trim()

            val msg = openYouTube(context, query)
            return UtilityResult(true, msg, "YOUTUBE")
        }

        // 2. Playlist + Episode Commands: e.g. "open tmkoc playlist and play episode 5", "open tmkoc and play episode 5", "play episode 5 of tmkoc"
        if ((lower.contains("episode") || lower.contains("playlist")) && (lower.contains("play") || lower.contains("open") || lower.contains("show"))) {
            var clean = text
                .replace("open ", "", ignoreCase = true)
                .replace("show ", "", ignoreCase = true)
                .replace("and play ", " ", ignoreCase = true)
                .replace("and ", " ", ignoreCase = true)
                .replace("play ", " ", ignoreCase = true)
                .trim()

            if (clean.contains(" of ", ignoreCase = true)) {
                val parts = clean.split(" of ", ignoreCase = true)
                if (parts.size >= 2) {
                    clean = "${parts[1].trim()} ${parts[0].trim()}"
                }
            }

            val msg = openYouTube(context, clean)
            return UtilityResult(true, msg, "YOUTUBE")
        }

        return null
    }

    fun shareText(context: Context, title: String, text: String): String {
        return try {
            val sendIntent = Intent().apply {
                action = Intent.ACTION_SEND
                putExtra(Intent.EXTRA_TEXT, text)
                type = "text/plain"
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            val shareIntent = Intent.createChooser(sendIntent, title).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(shareIntent)
            "Sharing note/text..."
        } catch (e: Exception) {
            "Unable to launch share dialog."
        }
    }

    fun setScreenBrightness(activity: Activity?, percentage: Int): String {
        if (activity == null) return "Activity context required for brightness."
        return try {
            val clamped = percentage.coerceIn(5, 100)
            val layoutParams = activity.window.attributes
            layoutParams.screenBrightness = clamped / 100f
            activity.window.attributes = layoutParams
            "Brightness set to $clamped%."
        } catch (e: Exception) {
            "Unable to change screen brightness."
        }
    }
}
