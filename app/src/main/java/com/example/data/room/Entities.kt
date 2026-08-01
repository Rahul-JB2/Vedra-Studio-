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

@Entity(tableName = "custom_text_commands")
data class CustomTextCommandEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val commandText: String,    // e.g. "my whatsapp", "study mode", "night light"
    val actionType: String,     // e.g. "LAUNCH_APP", "SYSTEM_SETTING", "VOICE_ACTION"
    val targetPayload: String,  // e.g. "com.whatsapp", "flashlight", "open study hub"
    val description: String = "",
    val isEnabled: Boolean = true,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "vedra_user_settings")
data class VedraUserSettingEntity(
    @PrimaryKey val settingKey: String,
    val settingValue: String,
    val lastUpdated: Long = System.currentTimeMillis()
)

@Entity(tableName = "voice_command_mappings")
data class VoiceCommandMappingEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val voiceTriggerPhrase: String,      // e.g. "open study notes", "take a picture", "call mom", "launch whatsapp"
    val actionType: String,             // e.g. "LAUNCH_APP", "SYSTEM_ACTION", "AI_PROMPT", "NAVIGATION", "UTILITY"
    val targetPackageOrAction: String,  // e.g. "com.whatsapp", "CAMERA_CAPTURE", "NAVIGATE_STUDY_HUB", "TOGGLE_FLASHLIGHT"
    val actionParametersJson: String = "{}", // e.g. {"contact": "Mom"}
    val description: String = "",
    val isEnabled: Boolean = true,
    val priority: Int = 1,
    val createdTimestamp: Long = System.currentTimeMillis()
)
