package com.example.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.data.local.entity.ChatMessageEntity
import com.example.data.local.entity.ChatThreadEntity
import com.example.data.local.entity.HabitEntity
import com.example.data.local.entity.HabitLogEntity
import com.example.data.local.entity.MetricNoiseEntity
import com.example.data.local.entity.ReminderPrefEntity
import com.example.data.local.entity.ScanDraftEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface MetricNoiseDao {
    @Query("SELECT * FROM metric_noise")
    suspend fun getAllNoise(): List<MetricNoiseEntity>

    @Query("SELECT * FROM metric_noise WHERE metricId = :metricId LIMIT 1")
    suspend fun getNoiseForMetric(metricId: String): MetricNoiseEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun setNoise(entity: MetricNoiseEntity)
}

@Dao
interface ChatDao {
    @Query("SELECT * FROM chat_threads ORDER BY updatedAt DESC")
    fun observeAllThreads(): Flow<List<ChatThreadEntity>>

    @Query("SELECT * FROM chat_threads WHERE id = :threadId LIMIT 1")
    suspend fun getThread(threadId: String): ChatThreadEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateThread(thread: ChatThreadEntity)

    @Query("DELETE FROM chat_threads WHERE id = :threadId")
    suspend fun deleteThread(threadId: String)

    @Query("DELETE FROM chat_threads")
    suspend fun clearAllThreads()

    @Query("SELECT * FROM chat_messages WHERE threadId = :threadId ORDER BY createdAt ASC")
    fun observeMessages(threadId: String): Flow<List<ChatMessageEntity>>

    @Query("SELECT * FROM chat_messages WHERE threadId = :threadId ORDER BY createdAt DESC LIMIT 10")
    suspend fun getLastTenMessages(threadId: String): List<ChatMessageEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMessage(message: ChatMessageEntity)

    @Query("UPDATE chat_messages SET reported = 1 WHERE id = :messageId")
    suspend fun markReported(messageId: String)

    @Query("DELETE FROM chat_messages WHERE threadId = :threadId")
    suspend fun deleteMessagesForThread(threadId: String)

    @Query("DELETE FROM chat_messages")
    suspend fun clearAllMessages()
}

@Dao
interface HabitDao {
    @Query("SELECT * FROM habits WHERE active = 1 ORDER BY orderIdx ASC")
    fun observeActiveHabits(): Flow<List<HabitEntity>>

    @Query("SELECT * FROM habits ORDER BY orderIdx ASC")
    suspend fun getAllHabits(): List<HabitEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateHabit(habit: HabitEntity)

    @Query("DELETE FROM habits WHERE id = :habitId")
    suspend fun deleteHabit(habitId: String)

    @Query("DELETE FROM habits")
    suspend fun clearAllHabits()

    @Query("SELECT * FROM habit_logs WHERE date = :date")
    fun observeLogsForDate(date: String): Flow<List<HabitLogEntity>>

    @Query("SELECT * FROM habit_logs WHERE date = :date")
    suspend fun getLogsForDate(date: String): List<HabitLogEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateLog(log: HabitLogEntity)

    @Query("SELECT * FROM habit_logs WHERE done = 1 ORDER BY date DESC")
    suspend fun getAllCompletedLogs(): List<HabitLogEntity>

    @Query("DELETE FROM habit_logs")
    suspend fun clearAllLogs()
}

@Dao
interface ReminderPrefDao {
    @Query("SELECT * FROM reminder_prefs")
    fun observeAllReminderPrefs(): Flow<List<ReminderPrefEntity>>

    @Query("SELECT * FROM reminder_prefs WHERE channel = :channel LIMIT 1")
    suspend fun getPref(channel: String): ReminderPrefEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun setPref(pref: ReminderPrefEntity)
}

@Dao
interface ScanDraftDao {
    @Query("SELECT * FROM scan_drafts WHERE id = :id LIMIT 1")
    suspend fun getDraft(id: String): ScanDraftEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveDraft(draft: ScanDraftEntity)

    @Query("DELETE FROM scan_drafts WHERE id = :id")
    suspend fun deleteDraft(id: String)

    @Query("DELETE FROM scan_drafts")
    suspend fun clearDrafts()
}
