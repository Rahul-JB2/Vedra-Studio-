package com.example.data.room

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface ConversationContextDao {

    @Query("SELECT * FROM conversation_contexts ORDER BY timestamp DESC")
    fun getAllContextsFlow(): Flow<List<ConversationContextEntity>>

    @Query("SELECT * FROM conversation_contexts ORDER BY timestamp DESC LIMIT :limit")
    suspend fun getRecentContexts(limit: Int = 20): List<ConversationContextEntity>

    @Query("SELECT * FROM conversation_contexts WHERE userPrompt LIKE '%' || :query || '%' OR aiResponse LIKE '%' || :query || '%' OR keywords LIKE '%' || :query || '%' ORDER BY timestamp DESC")
    suspend fun searchContexts(query: String): List<ConversationContextEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertContext(context: ConversationContextEntity): Long

    @Query("DELETE FROM conversation_contexts WHERE id = :id")
    suspend fun deleteContext(id: Long)

    @Query("DELETE FROM conversation_contexts")
    suspend fun clearAllContexts()
}

@Dao
interface UserInteractionPatternDao {

    @Query("SELECT * FROM user_interaction_patterns ORDER BY frequencyCount DESC, lastUsedTimestamp DESC")
    fun getAllPatternsFlow(): Flow<List<UserInteractionPatternEntity>>

    @Query("SELECT * FROM user_interaction_patterns WHERE actionType = :actionType AND targetKey = :targetKey LIMIT 1")
    suspend fun getPattern(actionType: String, targetKey: String): UserInteractionPatternEntity?

    @Query("SELECT * FROM user_interaction_patterns ORDER BY frequencyCount DESC LIMIT :limit")
    suspend fun getTopPatterns(limit: Int = 10): List<UserInteractionPatternEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdatePattern(pattern: UserInteractionPatternEntity): Long

    @Query("DELETE FROM user_interaction_patterns WHERE id = :id")
    suspend fun deletePattern(id: Long)

    @Query("DELETE FROM user_interaction_patterns")
    suspend fun clearAllPatterns()
}

@Dao
interface CustomTextCommandDao {

    @Query("SELECT * FROM custom_text_commands ORDER BY createdAt DESC")
    fun getAllCommandsFlow(): Flow<List<CustomTextCommandEntity>>

    @Query("SELECT * FROM custom_text_commands ORDER BY createdAt DESC")
    suspend fun getAllCommands(): List<CustomTextCommandEntity>

    @Query("SELECT * FROM custom_text_commands WHERE LOWER(commandText) = LOWER(:commandText) AND isEnabled = 1 LIMIT 1")
    suspend fun findCommand(commandText: String): CustomTextCommandEntity?

    @Query("SELECT * FROM custom_text_commands WHERE LOWER(commandText) LIKE '%' || LOWER(:query) || '%' OR LOWER(targetPayload) LIKE '%' || LOWER(:query) || '%' ORDER BY createdAt DESC")
    suspend fun searchCommands(query: String): List<CustomTextCommandEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCommand(command: CustomTextCommandEntity): Long

    @Query("DELETE FROM custom_text_commands WHERE id = :id")
    suspend fun deleteCommand(id: Long)

    @Query("DELETE FROM custom_text_commands")
    suspend fun clearAllCommands()
}

@Dao
interface VedraUserSettingDao {

    @Query("SELECT * FROM vedra_user_settings ORDER BY settingKey ASC")
    fun getAllSettingsFlow(): Flow<List<VedraUserSettingEntity>>

    @Query("SELECT settingValue FROM vedra_user_settings WHERE settingKey = :key LIMIT 1")
    suspend fun getSettingValue(key: String): String?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateSetting(setting: VedraUserSettingEntity)

    @Query("DELETE FROM vedra_user_settings WHERE settingKey = :key")
    suspend fun deleteSetting(key: String)

    @Query("DELETE FROM vedra_user_settings")
    suspend fun clearAllSettings()
}
