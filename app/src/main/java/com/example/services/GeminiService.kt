package com.example.services

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
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(20, TimeUnit.SECONDS)
        .build()

    suspend fun generateResponse(prompt: String, contextSummary: String = ""): String = withContext(Dispatchers.IO) {
        val apiKey = try { BuildConfig::class.java.getField("GEMINI_API_KEY").get(null) as? String } catch (e: Exception) { null }

        if (!apiKey.isNullOrBlank() && apiKey != "MY_GEMINI_API_KEY") {
            try {
                val url = "https://generativelanguage.googleapis.com/v1beta/models/gemini-1.5-flash:generateContent?key=$apiKey"
                
                val systemPrompt = StringBuilder()
                systemPrompt.append("""
                    # SYSTEM INSTRUCTION: VEDRA AI ENGINE (v2.0)
                    You are VEDRA, an ultra-responsive, intelligent Personal AI Assistant and Study Companion built for mobile device integration and competitive exam preparation (JEE & Boards).
                    - Tone: Natural, confident, direct, grounded, and concise.
                    - Attitude: Helpful peer, never a robotic or overly verbose lecturer.
                    - Language: English (or Hinglish/Hindi if explicitly spoken/written by user).

                    RESPONSE LENGTH & WORD LIMIT MATRIX:
                    1. Direct OS Action ("Open WhatsApp", "Turn on Flashlight", "Call Mom"): 1 to 8 words. Include action tag like [ACTION: OPEN_APP, app: "..."] or [ACTION: CALL, contact: "..."] if applicable + minimal confirmation.
                    2. Voice Mode (TTS): 15 to 40 words. Smooth spoken prose, NO markdown/bullet symbols.
                    3. Quick Query / Fact: 10 to 30 words. Single crisp sentence or 2 bullet points max.
                    4. Study / JEE Question: 80 to 200 words. Formula first (BLUF), bullet points, bold key terms.
                    5. General Chat: 15 to 35 words. Conversational, friendly, direct.

                    RULES:
                    - Bottom-Line-Up-Front (BLUF): Put core answer or formula in VERY FIRST sentence.
                    - Zero Fluff: NEVER start with "Sure!", "As an AI...", "Here is what I found...", "Great question!".
                    - Visual Scannability: Use **Bold** for key variables, bullet points for lists, and LaTeX for math.

                    """.trimIndent())

                if (contextSummary.isNotBlank()) {
                    systemPrompt.append("\n[USER CONTEXT MEMORY & PROFILE]\n")
                    systemPrompt.append(contextSummary)
                    systemPrompt.append("\n[END USER CONTEXT]\n")
                }
                systemPrompt.append("\nUser query: $prompt")

                val jsonBody = JSONObject().apply {
                    put("contents", JSONArray().apply {
                        put(JSONObject().apply {
                            put("parts", JSONArray().apply {
                                put(JSONObject().apply {
                                    put("text", systemPrompt.toString())
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
                                    if (text.isNotBlank()) return@withContext text.trim()
                                }
                            }
                        }
                    }
                }
            } catch (e: Exception) {
                // Fallback to offline assistant smart responses
            }
        }

        // Offline / fallback response engine for VEDRA
        return@withContext generateFallbackResponse(prompt, contextSummary)
    }

    private fun generateFallbackResponse(prompt: String, contextSummary: String = ""): String {
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
                "I analyzed your prompt: \"$prompt\". I can help you launch apps, toggle flashlight, calculate math, copy clipboard items, convert units, and manage custom commands!"
        }
    }
}
