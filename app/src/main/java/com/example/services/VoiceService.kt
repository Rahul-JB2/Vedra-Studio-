package com.example.services

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import androidx.compose.runtime.mutableStateOf
import java.util.Locale

class VoiceService(private val context: Context) : TextToSpeech.OnInitListener {

    private var tts: TextToSpeech? = TextToSpeech(context, this)
    private var speechRecognizer: SpeechRecognizer? = null

    val isTtsReady = mutableStateOf(false)
    val isListening = mutableStateOf(false)
    val isSpeaking = mutableStateOf(false)
    val isMuted = mutableStateOf(false)
    val isContinuousMode = mutableStateOf(false)
    val isPaused = mutableStateOf(false)
    val isWakeWordActive = mutableStateOf(false)
    val wakeWordStatus = mutableStateOf("Standby")
    val lastRecognizedText = mutableStateOf("")

    val speechPitch = mutableStateOf(1.0f)
    val speechRate = mutableStateOf(1.0f)
    val currentLanguage = mutableStateOf(Locale.US)

    private var currentOnComplete: (() -> Unit)? = null
    private var lastUtteranceId: String = ""

    init {
        if (SpeechRecognizer.isRecognitionAvailable(context)) {
            speechRecognizer = SpeechRecognizer.createSpeechRecognizer(context)
        }
    }

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            isTtsReady.value = true
            tts?.language = currentLanguage.value
            tts?.setPitch(speechPitch.value)
            tts?.setSpeechRate(speechRate.value)

            tts?.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
                override fun onStart(utteranceId: String?) {
                    isSpeaking.value = true
                }

                override fun onDone(utteranceId: String?) {
                    if (utteranceId == lastUtteranceId) {
                        isSpeaking.value = false
                        val cb = currentOnComplete
                        currentOnComplete = null
                        cb?.invoke()
                    }
                }

                @Deprecated("Deprecated in Java")
                override fun onError(utteranceId: String?) {
                    if (utteranceId == lastUtteranceId) {
                        isSpeaking.value = false
                        val cb = currentOnComplete
                        currentOnComplete = null
                        cb?.invoke()
                    }
                }
            })
        } else {
            isTtsReady.value = false
        }
    }

    fun cleanTextForSpeech(rawText: String): String {
        if (rawText.isBlank()) return ""

        var cleaned = rawText
            // Remove ACTION and IMAGE tags like [ACTION: CALL, contact: "Mom"]
            .replace(Regex("\\[ACTION:[^\\]]+\\]"), "")
            .replace(Regex("\\[IMAGE:[^\\]]+\\]"), "")
            .replace(Regex("\\[REPLY:[^\\]]+\\]"), "")

        // Replace triple backtick code blocks with simple description
        cleaned = cleaned.replace(Regex("```[\\s\\S]*?```"), "Code snippet skipped.")

        // Clean inline code backticks
        cleaned = cleaned.replace(Regex("`([^`]+)`"), "$1")

        // Clean URLs
        cleaned = cleaned.replace(Regex("https?://\\S+"), "link")

        // Clean Markdown styling
        cleaned = cleaned
            .replace(Regex("\\*\\*([^*]+)\\*\\*"), "$1") // Bold **
            .replace(Regex("\\*([^*]+)\\*"), "$1")       // Italic *
            .replace(Regex("__([^_]+)__"), "$1")         // Bold __
            .replace(Regex("_([^_]+)_"), "$1")           // Italic _
            .replace(Regex("~~([^~]+)~~"), "$1")         // Strikethrough
            .replace(Regex("(?m)^#+\\s*"), "")           // Headers
            .replace(Regex("(?m)^[*\\-]\\s+"), "")       // Bullet points
            .replace(Regex("(?m)^\\d+\\.\\s+"), "")      // Numbered lists

        // Normalize spaces and line breaks
        cleaned = cleaned.replace(Regex("\\n+"), ". ")
        cleaned = cleaned.replace(Regex("\\s+"), " ")

        return cleaned.trim()
    }

    fun setLocale(locale: Locale): Boolean {
        currentLanguage.value = locale
        val result = tts?.setLanguage(locale)
        return result != TextToSpeech.LANG_MISSING_DATA && result != TextToSpeech.LANG_NOT_SUPPORTED
    }

    fun setPitchAndRate(pitch: Float, rate: Float) {
        speechPitch.value = pitch.coerceIn(0.5f, 2.0f)
        speechRate.value = rate.coerceIn(0.5f, 2.0f)
        tts?.setPitch(speechPitch.value)
        tts?.setSpeechRate(speechRate.value)
    }

    fun toggleMute(): Boolean {
        isMuted.value = !isMuted.value
        if (isMuted.value) {
            stopSpeaking()
        }
        return isMuted.value
    }

    fun syncSettings(dbService: DatabaseService) {
        val speed = dbService.getSetting("voice_speed", "1.0").toFloatOrNull() ?: 1.0f
        val pitch = dbService.getSetting("voice_pitch", "1.0").toFloatOrNull() ?: 1.0f
        val langStr = dbService.getSetting("pref_app_language", "English (India)")
        
        speechPitch.value = pitch.coerceIn(0.5f, 2.0f)
        speechRate.value = speed.coerceIn(0.5f, 2.0f)
        
        val locale = when {
            langStr.contains("Hindi", ignoreCase = true) -> Locale("hi", "IN")
            langStr.contains("Hinglish", ignoreCase = true) -> Locale("hi", "IN")
            langStr.contains("Spanish", ignoreCase = true) -> Locale("es", "ES")
            langStr.contains("French", ignoreCase = true) -> Locale("fr", "FR")
            langStr.contains("Bengali", ignoreCase = true) -> Locale("bn", "IN")
            langStr.contains("Marathi", ignoreCase = true) -> Locale("mr", "IN")
            langStr.contains("Tamil", ignoreCase = true) -> Locale("ta", "IN")
            langStr.contains("Telugu", ignoreCase = true) -> Locale("te", "IN")
            else -> Locale("en", "IN")
        }
        currentLanguage.value = locale

        tts?.setPitch(speechPitch.value)
        tts?.setSpeechRate(speechRate.value)
        tts?.language = currentLanguage.value
    }

    fun speak(text: String, dbService: DatabaseService? = null, onComplete: (() -> Unit)? = null) {
        if (dbService != null) {
            syncSettings(dbService)
        }
        if (isMuted.value || text.isBlank()) {
            onComplete?.invoke()
            return
        }

        val cleanedText = cleanTextForSpeech(text)
        if (cleanedText.isBlank()) {
            onComplete?.invoke()
            return
        }

        stopListening()
        stopSpeaking()

        isSpeaking.value = true
        currentOnComplete = onComplete

        // Split long text into manageable sentence chunks for smooth playback
        val chunks = cleanedText.split(Regex("(?<=[.!?])\\s+")).filter { it.isNotBlank() }
        if (chunks.isEmpty()) {
            isSpeaking.value = false
            onComplete?.invoke()
            return
        }

        val baseUtteranceId = "VED_TTS_${System.currentTimeMillis()}"
        lastUtteranceId = "${baseUtteranceId}_${chunks.size - 1}"

        for (index in chunks.indices) {
            val chunkUtteranceId = "${baseUtteranceId}_$index"
            val queueMode = if (index == 0) TextToSpeech.QUEUE_FLUSH else TextToSpeech.QUEUE_ADD

            val params = Bundle().apply {
                putString(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, chunkUtteranceId)
            }

            tts?.speak(chunks[index], queueMode, params, chunkUtteranceId)
        }
    }

    fun stopSpeaking() {
        tts?.stop()
        isSpeaking.value = false
        currentOnComplete = null
    }

    fun startListening(onResult: (String) -> Unit, onError: (String) -> Unit) {
        if (speechRecognizer == null) {
            onError("Speech recognition not available on this device.")
            return
        }

        stopSpeaking()
        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault())
            putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
        }

        speechRecognizer?.setRecognitionListener(object : RecognitionListener {
            override fun onReadyForSpeech(params: Bundle?) {
                isListening.value = true
            }

            override fun onBeginningOfSpeech() {}
            override fun onRmsChanged(rmsdB: Float) {}
            override fun onBufferReceived(buffer: ByteArray?) {}
            override fun onEndOfSpeech() {
                isListening.value = false
            }

            override fun onError(error: Int) {
                isListening.value = false
                val errorMsg = when (error) {
                    SpeechRecognizer.ERROR_NO_MATCH -> "No speech detected."
                    SpeechRecognizer.ERROR_SPEECH_TIMEOUT -> "Speech timeout."
                    else -> "Voice input paused."
                }
                onError(errorMsg)
            }

            override fun onResults(results: Bundle?) {
                isListening.value = false
                val matches = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                if (!matches.isNullOrEmpty()) {
                    val spokenText = matches[0]
                    lastRecognizedText.value = spokenText
                    onResult(spokenText)
                }
            }

            override fun onPartialResults(partialResults: Bundle?) {
                val matches = partialResults?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                if (!matches.isNullOrEmpty()) {
                    lastRecognizedText.value = matches[0]
                }
            }

            override fun onEvent(eventType: Int, params: Bundle?) {}
        })

        speechRecognizer?.startListening(intent)
    }

    fun stopListening() {
        if (isListening.value) {
            speechRecognizer?.stopListening()
            isListening.value = false
        }
    }

    fun startWakeWordDetection(onWakeWordTriggered: (String) -> Unit) {
        if (speechRecognizer == null) return
        stopListening()
        stopSpeaking()

        isWakeWordActive.value = true
        wakeWordStatus.value = "Listening for 'Hey VEDRA'..."

        val wakeWords = listOf("hey vedra", "vedra", "ok vedra", "hi vedra", "hey ved")
        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault())
            putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
        }

        speechRecognizer?.setRecognitionListener(object : RecognitionListener {
            override fun onReadyForSpeech(params: Bundle?) {}
            override fun onBeginningOfSpeech() {}
            override fun onRmsChanged(rmsdB: Float) {}
            override fun onBufferReceived(buffer: ByteArray?) {}
            override fun onEndOfSpeech() {}

            override fun onError(error: Int) {
                // Auto-restart wake word listener if still active
                if (isWakeWordActive.value) {
                    try {
                        speechRecognizer?.startListening(intent)
                    } catch (_: Exception) {}
                }
            }

            private fun checkMatches(matches: ArrayList<String>?) {
                if (matches.isNullOrEmpty() || !isWakeWordActive.value) return
                for (match in matches) {
                    val lower = match.lowercase()
                    if (wakeWords.any { lower.contains(it) }) {
                        isWakeWordActive.value = false
                        wakeWordStatus.value = "Wake Word Detected!"
                        stopListening()
                        onWakeWordTriggered(match)
                        break
                    }
                }
            }

            override fun onResults(results: Bundle?) {
                val matches = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                checkMatches(matches)
                if (isWakeWordActive.value) {
                    try {
                        speechRecognizer?.startListening(intent)
                    } catch (_: Exception) {}
                }
            }

            override fun onPartialResults(partialResults: Bundle?) {
                val matches = partialResults?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                checkMatches(matches)
            }

            override fun onEvent(eventType: Int, params: Bundle?) {}
        })

        try {
            speechRecognizer?.startListening(intent)
        } catch (_: Exception) {}
    }

    fun stopWakeWordDetection() {
        isWakeWordActive.value = false
        wakeWordStatus.value = "Standby"
        stopListening()
    }

    fun shutdown() {
        stopWakeWordDetection()
        speechRecognizer?.destroy()
        tts?.stop()
        tts?.shutdown()
    }
}

