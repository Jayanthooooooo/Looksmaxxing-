package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.data.local.dao.BookmarkDao
import com.example.data.local.dao.ChatDao
import com.example.data.local.dao.HabitDao
import com.example.data.local.dao.MetricNoiseDao
import com.example.data.local.dao.ReminderPrefDao
import com.example.data.local.dao.ScanDao
import com.example.data.local.dao.ScanDraftDao
import com.example.data.local.entity.ArticleBookmarkEntity
import com.example.data.local.entity.ChatMessageEntity
import com.example.data.local.entity.ChatThreadEntity
import com.example.data.local.entity.HabitEntity
import com.example.data.local.entity.HabitLogEntity
import com.example.data.local.entity.MetricNoiseEntity
import com.example.data.local.entity.ReminderPrefEntity
import com.example.data.local.entity.ScanDraftEntity
import com.example.data.local.entity.ScanEntity

import com.example.data.local.dao.PostureDao
import com.example.data.local.entity.PostureLogEntity
import com.example.data.local.dao.SkillNodeDao
import com.example.data.local.entity.SkillNodeEntity
import com.example.data.local.dao.BarberShopDao
import com.example.data.local.entity.BarberShopItemEntity

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

@Database(
    entities = [
        ScanEntity::class,
        MetricNoiseEntity::class,
        ChatThreadEntity::class,
        ChatMessageEntity::class,
        HabitEntity::class,
        HabitLogEntity::class,
        ReminderPrefEntity::class,
        ScanDraftEntity::class,
        ArticleBookmarkEntity::class,
        PostureLogEntity::class,
        SkillNodeEntity::class,
        BarberShopItemEntity::class
    ],
    version = 6,
    exportSchema = false
)
abstract class LumoraDatabase : RoomDatabase() {
    abstract fun scanDao(): ScanDao
    abstract fun metricNoiseDao(): MetricNoiseDao
    abstract fun chatDao(): ChatDao
    abstract fun habitDao(): HabitDao
    abstract fun reminderPrefDao(): ReminderPrefDao
    abstract fun scanDraftDao(): ScanDraftDao
    abstract fun bookmarkDao(): BookmarkDao
    abstract fun postureDao(): PostureDao
    abstract fun skillNodeDao(): SkillNodeDao
    abstract fun barberShopDao(): BarberShopDao

    companion object {
        @Volatile
        private var INSTANCE: LumoraDatabase? = null

        val MIGRATION_5_6 = object : Migration(5, 6) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE skill_nodes ADD COLUMN proofPhotoPath TEXT DEFAULT NULL")
                db.execSQL("ALTER TABLE barber_shop_items ADD COLUMN description TEXT NOT NULL DEFAULT ''")
            }
        }

        fun getInstance(context: Context): LumoraDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    LumoraDatabase::class.java,
                    "lumora.db"
                )
                    .fallbackToDestructiveMigrationFrom(true, 1, 2, 3, 4)
                    .addMigrations(MIGRATION_5_6)
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
