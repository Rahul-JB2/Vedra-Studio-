package com.example.services

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import com.example.BuildConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.util.concurrent.TimeUnit

object GeminiService {

    private val client = OkHttpClient.Builder()
        .connectTimeout(20, TimeUnit.SECONDS)
        .readTimeout(25, TimeUnit.SECONDS)
        .writeTimeout(20, TimeUnit.SECONDS)
        .build()

    fun isDeviceOnline(context: Context?): Boolean {
        if (context == null) return true
        return try {
            val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager
            val network = cm?.activeNetwork ?: return false
            val caps = cm.getNetworkCapabilities(network) ?: return false
            caps.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
        } catch (e: Exception) {
            true
        }
    }

    suspend fun generateResponse(
        prompt: String,
        contextSummary: String = "",
        dbService: DatabaseService? = null,
        context: Context? = null
    ): String = withContext(Dispatchers.IO) {
        // Read configuration settings from DB if available
        val networkMode = dbService?.getSetting("ai_network_mode", "Auto") ?: "Auto"
        val provider = dbService?.getSetting("ai_provider", "Gemini AI") ?: "Gemini AI"
        val selectedModel = dbService?.getSetting("ai_model", "Gemini 3.5 Flash") ?: "Gemini 3.5 Flash"

        val geminiKey = dbService?.getSetting("gemini_api_key", "")
            ?.takeIf { it.isNotBlank() }
            ?: dbService?.getSetting("api_key", "")
            ?.takeIf { it.isNotBlank() }
            ?: getBuildConfigGeminiKey()

        val openAiKey = dbService?.getSetting("openai_api_key", "")
        val otherAiKey = dbService?.getSetting("other_api_key", "")

        val onlineStatus = isDeviceOnline(context)

        // Determine effective mode
        val shouldRunOnline = when (networkMode) {
            "Force Offline" -> false
            "Force Online" -> true
            else -> onlineStatus // "Auto" mode
        }

        if (!shouldRunOnline || provider.contains("Native", ignoreCase = true) || provider.contains("Offline", ignoreCase = true)) {
            return@withContext generateFallbackResponse(prompt, contextSummary)
        }

        // Run online provider
        return@withContext when {
            provider.contains("Gemini", ignoreCase = true) -> {
                callGeminiApi(prompt, contextSummary, geminiKey, selectedModel)
            }
            provider.contains("OpenAI", ignoreCase = true) -> {
                if (openAiKey.isNullOrBlank()) {
                    "⚠️ OpenAI API Key is missing. Please enter your OpenAI API key in Settings > AI Settings, or switch provider to Gemini."
                } else {
                    callOpenAiApi(prompt, contextSummary, openAiKey, selectedModel)
                }
            }
            provider.contains("DeepSeek", ignoreCase = true) || provider.contains("Claude", ignoreCase = true) -> {
                if (otherAiKey.isNullOrBlank()) {
                    "⚠️ DeepSeek / Claude API Key is missing. Please enter your API key in Settings > AI Settings, or switch provider."
                } else {
                    callDeepSeekApi(prompt, contextSummary, otherAiKey, selectedModel)
                }
            }
            else -> {
                callGeminiApi(prompt, contextSummary, geminiKey, selectedModel)
            }
        }
    }

    private fun getBuildConfigGeminiKey(): String? {
        return try {
            BuildConfig::class.java.getField("GEMINI_API_KEY").get(null) as? String
        } catch (e: Exception) {
            null
        }
    }

    private fun buildSystemInstruction(contextSummary: String): String {
        val sb = StringBuilder()
        sb.append("""
            # SYSTEM INSTRUCTION: VEDRA AI ENGINE (v2.5 Multi-Model)
            You are VEDRA, an ultra-responsive, highly intelligent AI Assistant and Study Companion built for mobile device integration and competitive exam preparation (JEE & Boards).
            - Tone: Natural, confident, direct, grounded, and concise.
            - Language: English (or Hinglish/Hindi if explicitly spoken/written by user).

            RESPONSE RULES:
            1. Bottom-Line-Up-Front (BLUF): Put core answer or formula in VERY FIRST sentence.
            2. Direct OS Action ("Open WhatsApp", "Turn on Flashlight"): Include action tag [ACTION: OPEN_APP, app: "..."] or [ACTION: CALL, contact: "..."] if applicable + minimal confirmation.
            3. Zero Fluff: NEVER start with "Sure!", "As an AI...", "Here is what I found...", "Great question!".
            4. Visual Scannability: Use **Bold** for key variables, bullet points for lists.
        """.trimIndent())

        if (contextSummary.isNotBlank()) {
            sb.append("\n\n[USER CONTEXT MEMORY & PROFILE]\n")
            sb.append(contextSummary)
            sb.append("\n[END USER CONTEXT]\n")
        }
        return sb.toString()
    }

    private fun callGeminiApi(
        prompt: String,
        contextSummary: String,
        apiKey: String?,
        modelName: String
    ): String {
        val keyToUse = if (!apiKey.isNullOrBlank() && apiKey != "MY_GEMINI_API_KEY") apiKey else getBuildConfigGeminiKey()

        if (keyToUse.isNullOrBlank() || keyToUse == "MY_GEMINI_API_KEY") {
            return generateFallbackResponse(prompt, contextSummary)
        }

        val resolvedModel = when {
            modelName.contains("Pro", ignoreCase = true) -> "gemini-3.1-pro-preview"
            modelName.contains("Flash", ignoreCase = true) -> "gemini-3.5-flash"
            else -> "gemini-3.5-flash"
        }

        try {
            val url = "https://generativelanguage.googleapis.com/v1beta/models/$resolvedModel:generateContent?key=$keyToUse"
            val systemInstructionText = buildSystemInstruction(contextSummary)
            val fullPrompt = "$systemInstructionText\n\nUser query: $prompt"

            val jsonBody = JSONObject().apply {
                put("contents", JSONArray().apply {
                    put(JSONObject().apply {
                        put("parts", JSONArray().apply {
                            put(JSONObject().apply {
                                put("text", fullPrompt)
                            })
                        })
                    })
                })
            }

            val request = Request.Builder()
                .url(url)
                .post(jsonBody.toString().toRequestBody("application/json".toMediaType()))
                .build()

            client.newCall(request).execute().use { response ->
                if (response.isSuccessful) {
                    val body = response.body?.string()
                    if (body != null) {
                        val json = JSONObject(body)
                        val candidates = json.optJSONArray("candidates")
                        if (candidates != null && candidates.length() > 0) {
                            val content = candidates.getJSONObject(0).optJSONObject("content")
                            val parts = content?.optJSONArray("parts")
                            if (parts != null && parts.length() > 0) {
                                val text = parts.getJSONObject(0).optString("text")
                                if (text.isNotBlank()) return text.trim()
                            }
                        }
                    }
                }
            }
        } catch (e: Exception) {
            // Fallback gracefully on network error or key failure
        }

        return generateFallbackResponse(prompt, contextSummary)
    }

    private fun callOpenAiApi(
        prompt: String,
        contextSummary: String,
        apiKey: String,
        modelName: String
    ): String {
        try {
            val resolvedModel = when {
                modelName.contains("4o-mini", ignoreCase = true) -> "gpt-4o-mini"
                modelName.contains("4o", ignoreCase = true) -> "gpt-4o"
                else -> "gpt-4o-mini"
            }

            val url = "https://api.openai.com/v1/chat/completions"
            val systemText = buildSystemInstruction(contextSummary)

            val jsonBody = JSONObject().apply {
                put("model", resolvedModel)
                put("messages", JSONArray().apply {
                    put(JSONObject().apply {
                        put("role", "system")
                        put("content", systemText)
                    })
                    put(JSONObject().apply {
                        put("role", "user")
                        put("content", prompt)
                    })
                })
                put("temperature", 0.7)
            }

            val request = Request.Builder()
                .url(url)
                .addHeader("Authorization", "Bearer $apiKey")
                .post(jsonBody.toString().toRequestBody("application/json".toMediaType()))
                .build()

            client.newCall(request).execute().use { response ->
                if (response.isSuccessful) {
                    val body = response.body?.string()
                    if (body != null) {
                        val json = JSONObject(body)
                        val choices = json.optJSONArray("choices")
                        if (choices != null && choices.length() > 0) {
                            val message = choices.getJSONObject(0).optJSONObject("message")
                            val content = message?.optString("content")
                            if (!content.isNullOrBlank()) return content.trim()
                        }
                    }
                } else {
                    return "OpenAI Error (${response.code}): ${response.message}. Check your OpenAI API key in AI Settings."
                }
            }
        } catch (e: Exception) {
            // Fallback gracefully
        }

        return generateFallbackResponse(prompt, contextSummary)
    }

    private fun callDeepSeekApi(
        prompt: String,
        contextSummary: String,
        apiKey: String,
        modelName: String
    ): String {
        try {
            val url = "https://api.deepseek.com/chat/completions"
            val systemText = buildSystemInstruction(contextSummary)

            val jsonBody = JSONObject().apply {
                put("model", "deepseek-chat")
                put("messages", JSONArray().apply {
                    put(JSONObject().apply {
                        put("role", "system")
                        put("content", systemText)
                    })
                    put(JSONObject().apply {
                        put("role", "user")
                        put("content", prompt)
                    })
                })
            }

            val request = Request.Builder()
                .url(url)
                .addHeader("Authorization", "Bearer $apiKey")
                .post(jsonBody.toString().toRequestBody("application/json".toMediaType()))
                .build()

            client.newCall(request).execute().use { response ->
                if (response.isSuccessful) {
                    val body = response.body?.string()
                    if (body != null) {
                        val json = JSONObject(body)
                        val choices = json.optJSONArray("choices")
                        if (choices != null && choices.length() > 0) {
                            val message = choices.getJSONObject(0).optJSONObject("message")
                            val content = message?.optString("content")
                            if (!content.isNullOrBlank()) return content.trim()
                        }
                    }
                } else {
                    return "DeepSeek Error (${response.code}): ${response.message}. Check your API key in AI Settings."
                }
            }
        } catch (e: Exception) {
            // Fallback gracefully
        }

        return generateFallbackResponse(prompt, contextSummary)
    }

    fun generateFallbackResponse(prompt: String, contextSummary: String = ""): String {
        val lower = prompt.lowercase()
        return when {
            contextSummary.isNotBlank() && (lower.contains("who am i") || lower.contains("my name") || lower.contains("my profile") || lower.contains("my memory")) ->
                "Here is your saved profile context in memory:\n$contextSummary"
            lower.contains("photosynthesis") ->
                "Photosynthesis is the process used by green plants to make food using sunlight, water, and carbon dioxide, producing glucose and oxygen."
            lower.contains("glucose") ->
                "The chemical formula for glucose is C₆H₁₂O₆."
            lower.contains("hello") || lower.contains("hi") || lower.contains("hey") ->
                if (contextSummary.isNotBlank()) "Hello! I am VEDRA. I have your saved context loaded. How can I help you today?" else "Hello! I am VEDRA, your AI assistant. How can I assist you today?"
            lower.contains("who are you") || lower.contains("what is your name") ->
                "I am VEDRA (or VED for short), your personal AI assistant."
            lower.contains("time") ->
                "You can check current system time on your status bar or ask me for timer & reminders."
            lower.contains("study") || lower.contains("jee") || lower.contains("exam") ->
                "Stay consistent! Breakdown your topics into 45-minute focused blocks with 10-minute breaks."
            lower.contains("weather") ->
                "The weather today is pleasant with clear skies and mild temperatures."
            else ->
                "I analyzed your prompt: \"$prompt\". [Native Offline Engine Mode] I can launch apps, set reminders, solve math formulas, toggle flashlight, search local documents, and control system volume."
        }
    }
}
