package com.example.data.room

import kotlinx.coroutines.flow.Flow

class AiContextRepository(
    private val contextDao: ConversationContextDao,
    private val patternDao: UserInteractionPatternDao,
    private val commandDao: CustomTextCommandDao? = null,
    private val settingDao: VedraUserSettingDao? = null
) {
    val allContextsFlow: Flow<List<ConversationContextEntity>> = contextDao.getAllContextsFlow()
    val topPatternsFlow: Flow<List<UserInteractionPatternEntity>> = patternDao.getAllPatternsFlow()
    val allCustomCommandsFlow: Flow<List<CustomTextCommandEntity>>? = commandDao?.getAllCommandsFlow()
    val allUserSettingsFlow: Flow<List<VedraUserSettingEntity>>? = settingDao?.getAllSettingsFlow()

    suspend fun saveUserSetting(key: String, value: String) {
        if (settingDao == null || key.isBlank()) return
        val entity = VedraUserSettingEntity(
            settingKey = key.trim(),
            settingValue = value,
            lastUpdated = System.currentTimeMillis()
        )
        settingDao.insertOrUpdateSetting(entity)
    }

    suspend fun getUserSetting(key: String): String? {
        if (settingDao == null || key.isBlank()) return null
        return settingDao.getSettingValue(key.trim())
    }

    suspend fun saveCustomCommand(
        commandText: String,
        actionType: String,
        targetPayload: String,
        description: String = ""
    ): Long {
        if (commandDao == null || commandText.isBlank()) return -1L
        val entity = CustomTextCommandEntity(
            commandText = commandText.trim().lowercase(),
            actionType = actionType,
            targetPayload = targetPayload.trim(),
            description = description,
            isEnabled = true,
            createdAt = System.currentTimeMillis()
        )
        return commandDao.insertCommand(entity)
    }

    suspend fun findCustomCommand(query: String): CustomTextCommandEntity? {
        if (commandDao == null || query.isBlank()) return null
        return commandDao.findCommand(query.trim())
    }

    suspend fun getAllCustomCommands(): List<CustomTextCommandEntity> {
        if (commandDao == null) return emptyList()
        return commandDao.getAllCommands()
    }

    suspend fun deleteCustomCommand(id: Long) {
        commandDao?.deleteCommand(id)
    }

    suspend fun deleteContext(id: Long) {
        contextDao.deleteContext(id)
    }

    suspend fun recordConversation(
        userPrompt: String,
        aiResponse: String,
        engine: String = "Offline Native VEDRA AI",
        sessionId: String = "default"
    ) {
        if (userPrompt.isBlank()) return

        val cleanPrompt = userPrompt.trim()
        val extractedKeywords = extractKeywords(cleanPrompt)
        val sentiment = analyzeSentiment(cleanPrompt)

        val entity = ConversationContextEntity(
            sessionId = sessionId,
            userPrompt = cleanPrompt,
            aiResponse = aiResponse,
            aiEngine = engine,
            sentiment = sentiment,
            keywords = extractedKeywords.joinToString(", "),
            timestamp = System.currentTimeMillis()
        )
        contextDao.insertContext(entity)

        // Automatically record interaction pattern for high-frequency terms
        for (kw in extractedKeywords.take(3)) {
            recordInteractionPattern("keyword_usage", kw, "Used in query: $cleanPrompt")
        }
    }

    suspend fun recordInteractionPattern(
        actionType: String,
        targetKey: String,
        contextNote: String = ""
    ) {
        if (targetKey.isBlank()) return
        val cleanKey = targetKey.trim().lowercase()

        val existing = patternDao.getPattern(actionType, cleanKey)
        if (existing != null) {
            val updated = existing.copy(
                frequencyCount = existing.frequencyCount + 1,
                lastUsedTimestamp = System.currentTimeMillis(),
                contextNote = if (contextNote.isNotBlank()) contextNote else existing.contextNote
            )
            patternDao.insertOrUpdatePattern(updated)
        } else {
            val newPattern = UserInteractionPatternEntity(
                actionType = actionType,
                targetKey = cleanKey,
                frequencyCount = 1,
                lastUsedTimestamp = System.currentTimeMillis(),
                contextNote = contextNote
            )
            patternDao.insertOrUpdatePattern(newPattern)
        }
    }

    suspend fun getLearnedContextForPrompt(userQuery: String): String? {
        val cleanQuery = userQuery.trim().lowercase()
        if (cleanQuery.isBlank()) return null

        // 1. Direct search in Room conversation contexts
        val directMatches = contextDao.searchContexts(cleanQuery)
        if (directMatches.isNotEmpty()) {
            val best = directMatches.first()
            return "🧠 [Room Database Context Match]:\n${best.aiResponse}"
        }

        // 2. Keyword overlap search
        val stopWords = setOf("a", "an", "the", "is", "are", "was", "were", "what", "how", "why", "who", "where", "can", "you", "tell", "me", "about", "for", "in", "on", "to", "with", "do", "does", "did", "please", "vedra", "ved")
        val keywords = cleanQuery.split(Regex("\\W+"))
            .filter { it.length > 2 && it !in stopWords }

        if (keywords.isNotEmpty()) {
            for (kw in keywords) {
                val kwMatches = contextDao.searchContexts(kw)
                if (kwMatches.isNotEmpty()) {
                    val match = kwMatches.first()
                    return "🧠 [Learned Context for '$kw']:\n${match.aiResponse}"
                }
            }
        }

        return null
    }

    suspend fun buildOfflineKnowledgeSummary(): String {
        val recent = contextDao.getRecentContexts(10)
        val patterns = patternDao.getTopPatterns(5)

        if (recent.isEmpty() && patterns.isEmpty()) return ""

        val sb = StringBuilder()
        if (patterns.isNotEmpty()) {
            sb.append("User Interaction Patterns: ")
            sb.append(patterns.joinToString("; ") { "${it.targetKey} (${it.frequencyCount}x)" })
            sb.append("\n")
        }
        if (recent.isNotEmpty()) {
            sb.append("Recent Learned Contexts: ")
            sb.append(recent.take(5).joinToString(" | ") { "\"${it.userPrompt}\" -> \"${it.aiResponse.take(40)}...\"" })
        }
        return sb.toString()
    }

    suspend fun clearAllRoomLogs() {
        contextDao.clearAllContexts()
        patternDao.clearAllPatterns()
    }

    private fun extractKeywords(text: String): List<String> {
        val stopWords = setOf("a", "an", "the", "is", "are", "was", "were", "what", "how", "why", "who", "where", "can", "you", "tell", "me", "about", "for", "in", "on", "to", "with", "do", "does", "did", "please", "vedra", "ved", "this", "that", "there", "have", "has", "had")
        return text.lowercase()
            .split(Regex("\\W+"))
            .filter { it.length > 2 && it !in stopWords }
            .distinct()
    }

    private fun analyzeSentiment(text: String): String {
        val lower = text.lowercase()
        return when {
            lower.contains("thank") || lower.contains("great") || lower.contains("good") || lower.contains("awesome") || lower.contains("nice") -> "positive"
            lower.contains("wrong") || lower.contains("bad") || lower.contains("error") || lower.contains("failed") || lower.contains("not working") -> "negative"
            lower.contains("open") || lower.contains("turn") || lower.contains("launch") || lower.contains("play") || lower.contains("calculate") -> "action_command"
            lower.contains("?") || lower.contains("what") || lower.contains("how") || lower.contains("why") -> "inquiry"
            else -> "neutral"
        }
    }
}
