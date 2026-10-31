package com.example.repo

import com.example.core.AppDispatchers
import com.example.data.local.dao.HabitDao
import com.example.data.local.entity.HabitEntity
import com.example.data.local.entity.HabitLogEntity
import com.example.data.repo.HabitRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class Stage5RealDataUnitTest {

    private lateinit var testDispatchers: AppDispatchers
    private lateinit var fakeHabitDao: FakeHabitDao
    private lateinit var habitRepository: HabitRepository

    @Before
    fun setUp() {
        val testDispatcher = UnconfinedTestDispatcher()
        testDispatchers = AppDispatchers(
            main = testDispatcher,
            io = testDispatcher,
            default = testDispatcher
        )
        fakeHabitDao = FakeHabitDao()
        habitRepository = HabitRepository(fakeHabitDao, testDispatchers)
    }

    @Test
    fun testInitializeDefaultHabits() = runTest {
        habitRepository.initializeDefaultHabitsIfEmpty()
        val habits = fakeHabitDao.getAllHabits()
        assertEquals(3, habits.size)
        assertEquals("Gentle AM Cleanser", habits[0].label)
    }

    @Test
    fun testToggleHabitAndStreakCalculation() = runTest {
        val todayStr = habitRepository.todayDateString
        habitRepository.toggleHabit("habit_spf", todayStr, true)
        
        val logs = fakeHabitDao.getLogsForDate(todayStr)
        assertEquals(1, logs.size)
        assertEquals(true, logs[0].done)

        val streak = habitRepository.calculateStreakDays()
        assertEquals(1, streak)
    }

    private class FakeHabitDao : HabitDao {
        private val habitsList = mutableListOf<HabitEntity>()
        private val logsList = mutableListOf<HabitLogEntity>()

        override fun observeActiveHabits(): Flow<List<HabitEntity>> = flowOf(habitsList)

        override suspend fun getAllHabits(): List<HabitEntity> = habitsList

        override suspend fun insertOrUpdateHabit(habit: HabitEntity) {
            habitsList.removeAll { it.id == habit.id }
            habitsList.add(habit)
        }

        override suspend fun deleteHabit(habitId: String) {
            habitsList.removeAll { it.id == habitId }
        }

        override suspend fun clearAllHabits() {
            habitsList.clear()
        }

        override fun observeLogsForDate(date: String): Flow<List<HabitLogEntity>> {
            return flowOf(logsList.filter { it.date == date })
        }

        override suspend fun getLogsForDate(date: String): List<HabitLogEntity> {
            return logsList.filter { it.date == date }
        }

        override suspend fun insertOrUpdateLog(log: HabitLogEntity) {
            logsList.removeAll { it.date == log.date && it.habitId == log.habitId }
            logsList.add(log)
        }

        override suspend fun getAllCompletedLogs(): List<HabitLogEntity> {
            return logsList.filter { it.done }
        }

        override suspend fun clearAllLogs() {
            logsList.clear()
        }
    }
}
