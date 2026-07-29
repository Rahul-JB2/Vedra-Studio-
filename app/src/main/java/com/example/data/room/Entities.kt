package com.example.data.room

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "conversation_contexts")
data class ConversationContextEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val sessionId: String = "default",
    val userPrompt: String,
    val aiResponse: String,
    val aiEngine: String = "Offline Native VEDRA AI",
    val sentiment: String = "neutral",
    val keywords: String = "",
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "user_interaction_patterns")
data class UserInteractionPatternEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val actionType: String, // e.g. "frequent_query", "app_launch", "mode_switch", "voice_command"
    val targetKey: String,  // e.g. "whatsapp", "flashlight", "jee_physics", "gemini_toggle"
    val frequencyCount: Int = 1,
    val lastUsedTimestamp: Long = System.currentTimeMillis(),
    val contextNote: String = ""
)
