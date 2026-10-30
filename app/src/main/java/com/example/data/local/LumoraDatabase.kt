package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.data.local.dao.ChatDao
import com.example.data.local.dao.HabitDao
import com.example.data.local.dao.MetricNoiseDao
import com.example.data.local.dao.ReminderPrefDao
import com.example.data.local.dao.ScanDao
import com.example.data.local.dao.ScanDraftDao
import com.example.data.local.entity.ChatMessageEntity
import com.example.data.local.entity.ChatThreadEntity
import com.example.data.local.entity.HabitEntity
import com.example.data.local.entity.HabitLogEntity
import com.example.data.local.entity.MetricNoiseEntity
import com.example.data.local.entity.ReminderPrefEntity
import com.example.data.local.entity.ScanDraftEntity
import com.example.data.local.entity.ScanEntity

@Database(
    entities = [
        ScanEntity::class,
        MetricNoiseEntity::class,
        ChatThreadEntity::class,
        ChatMessageEntity::class,
        HabitEntity::class,
        HabitLogEntity::class,
        ReminderPrefEntity::class,
        ScanDraftEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class LumoraDatabase : RoomDatabase() {
    abstract fun scanDao(): ScanDao
    abstract fun metricNoiseDao(): MetricNoiseDao
    abstract fun chatDao(): ChatDao
    abstract fun habitDao(): HabitDao
    abstract fun reminderPrefDao(): ReminderPrefDao
    abstract fun scanDraftDao(): ScanDraftDao

    companion object {
        @Volatile
        private var INSTANCE: LumoraDatabase? = null

        fun getInstance(context: Context): LumoraDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    LumoraDatabase::class.java,
                    "lumora.db"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}
