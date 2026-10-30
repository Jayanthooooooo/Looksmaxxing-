package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "metric_noise")
data class MetricNoiseEntity(
    @PrimaryKey val metricId: String,
    val sd: Float,
    val n: Int
)

@Entity(tableName = "chat_threads")
data class ChatThreadEntity(
    @PrimaryKey val id: String,
    val title: String,
    val updatedAt: Long
)

@Entity(
    tableName = "chat_messages"
)
data class ChatMessageEntity(
    @PrimaryKey val id: String,
    val threadId: String,
    val role: String, // "user", "assistant", "system"
    val text: String,
    val createdAt: Long,
    val reported: Boolean = false
)

@Entity(tableName = "habits")
data class HabitEntity(
    @PrimaryKey val id: String,
    val label: String,
    val pillar: String, // "Skin", "Grooming", "Habits", "Posture/Sleep"
    val scheduleJson: String,
    val active: Boolean = true,
    val orderIdx: Int = 0
)

@Entity(
    tableName = "habit_logs",
    primaryKeys = ["date", "habitId"]
)
data class HabitLogEntity(
    val date: String, // yyyyMMdd
    val habitId: String,
    val done: Boolean
)

@Entity(tableName = "reminder_prefs")
data class ReminderPrefEntity(
    @PrimaryKey val channel: String, // "scan_reminder", "daily_habits", "weekly_report"
    val enabled: Boolean,
    val timeMinutes: Int // minutes from midnight
)

@Entity(tableName = "scan_drafts")
data class ScanDraftEntity(
    @PrimaryKey val id: String,
    val stateJson: String
)
