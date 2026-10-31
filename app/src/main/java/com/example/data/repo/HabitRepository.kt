package com.example.data.repo

import com.example.core.AppDispatchers
import com.example.data.local.dao.HabitDao
import com.example.data.local.entity.HabitEntity
import com.example.data.local.entity.HabitLogEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class HabitRepository(
    private val habitDao: HabitDao,
    private val dispatchers: AppDispatchers
) {
    private val dateFormat = SimpleDateFormat("yyyyMMdd", Locale.US)

    val todayDateString: String
        get() = dateFormat.format(Date())

    fun observeActiveHabits(): Flow<List<HabitEntity>> {
        return habitDao.observeActiveHabits()
    }

    fun observeLogsForDate(dateStr: String): Flow<List<HabitLogEntity>> {
        return habitDao.observeLogsForDate(dateStr)
    }

    suspend fun toggleHabit(habitId: String, dateStr: String, done: Boolean) = withContext(dispatchers.io) {
        val updatedLog = HabitLogEntity(
            date = dateStr,
            habitId = habitId,
            done = done
        )
        habitDao.insertOrUpdateLog(updatedLog)
    }

    suspend fun initializeDefaultHabitsIfEmpty() = withContext(dispatchers.io) {
        val habits = habitDao.getAllHabits()
        if (habits.isEmpty()) {
            val defaultHabits = listOf(
                HabitEntity(
                    id = "habit_am_cleanser",
                    label = "Gentle AM Cleanser",
                    pillar = "Skin",
                    scheduleJson = "[\"MON\",\"TUE\",\"WED\",\"THU\",\"FRI\",\"SAT\",\"SUN\"]",
                    active = true,
                    orderIdx = 0
                ),
                HabitEntity(
                    id = "habit_spf",
                    label = "Broad Spectrum Sunscreen SPF 30+",
                    pillar = "Skin",
                    scheduleJson = "[\"MON\",\"TUE\",\"WED\",\"THU\",\"FRI\",\"SAT\",\"SUN\"]",
                    active = true,
                    orderIdx = 1
                ),
                HabitEntity(
                    id = "habit_pm_hydration",
                    label = "PM Barrier Hydration & Moisturiser",
                    pillar = "Skin",
                    scheduleJson = "[\"MON\",\"TUE\",\"WED\",\"THU\",\"FRI\",\"SAT\",\"SUN\"]",
                    active = true,
                    orderIdx = 2
                )
            )
            defaultHabits.forEach { habitDao.insertOrUpdateHabit(it) }
        }
    }

    suspend fun calculateStreakDays(): Int = withContext(dispatchers.io) {
        val logs = habitDao.getAllCompletedLogs()
        if (logs.isEmpty()) return@withContext 0

        val completedDates = logs.map { it.date }.toSet()
        var streak = 0
        val calendar = java.util.Calendar.getInstance()

        for (i in 0..365) {
            val dateStr = dateFormat.format(calendar.time)
            if (completedDates.contains(dateStr)) {
                streak++
            } else if (i > 0) {
                break
            }
            calendar.add(java.util.Calendar.DAY_OF_YEAR, -1)
        }
        return@withContext streak
    }
}
