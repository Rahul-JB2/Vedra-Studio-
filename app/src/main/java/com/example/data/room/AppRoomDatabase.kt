package com.example.data.room

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [
        ConversationContextEntity::class,
        UserInteractionPatternEntity::class,
        CustomTextCommandEntity::class,
        VedraUserSettingEntity::class
    ],
    version = 3,
    exportSchema = false
)
abstract class AppRoomDatabase : RoomDatabase() {

    abstract fun conversationContextDao(): ConversationContextDao
    abstract fun userInteractionPatternDao(): UserInteractionPatternDao
    abstract fun customTextCommandDao(): CustomTextCommandDao
    abstract fun vedraUserSettingDao(): VedraUserSettingDao

    companion object {
        @Volatile
        private var INSTANCE: AppRoomDatabase? = null

        fun getDatabase(context: Context): AppRoomDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppRoomDatabase::class.java,
                    "vedra_room_context.db"
                )
                .fallbackToDestructiveMigration()
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
