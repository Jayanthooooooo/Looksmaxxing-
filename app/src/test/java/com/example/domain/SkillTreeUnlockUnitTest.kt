package com.example.domain

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.temporal.IsoFields

class SkillTreeUnlockUnitTest {

    private val habitNodeMapping = mapOf(
        "skincare_7" to setOf("habit_am_cleanser", "habit_pm_hydration", "habit_skin_am_routine", "habit_skin_pm_routine"),
        "sunscreen_14" to setOf("habit_spf"),
        "sleep_7" to setOf("habit_sleep"),
        "hydration_7" to setOf("habit_water"),
        "posture_streak_3" to setOf("habit_chin_tucks", "habit_screen_resets")
    )

    private fun calculateBestStreak(dateStrings: Set<String>): Int {
        if (dateStrings.isEmpty()) return 0
        val dates = dateStrings.mapNotNull { str ->
            try {
                LocalDate.parse(str, DateTimeFormatter.BASIC_ISO_DATE)
            } catch (_: Exception) {
                null
            }
        }.distinct().sorted()

        if (dates.isEmpty()) return 0
        var maxStreak = 1
        var currentStreak = 1
        for (i in 1 until dates.size) {
            val prev = dates[i - 1]
            val curr = dates[i]
            if (curr == prev.plusDays(1)) {
                currentStreak++
                if (currentStreak > maxStreak) {
                    maxStreak = currentStreak
                }
            } else if (curr != prev) {
                currentStreak = 1
            }
        }
        return maxStreak
    }

    private fun calculateDistinctIsoWeeks(timestamps: List<Long>): Int {
        if (timestamps.isEmpty()) return 0
        val zone = ZoneId.systemDefault()
        return timestamps.map { ts ->
            val zdt = Instant.ofEpochMilli(ts).atZone(zone)
            val week = zdt.get(IsoFields.WEEK_OF_WEEK_BASED_YEAR)
            val year = zdt.get(IsoFields.WEEK_BASED_YEAR)
            "$year-$week"
        }.distinct().size
    }

    @Test
    fun testHabitNodeMappingContainsAllRequiredKeys() {
        assertEquals(5, habitNodeMapping.size)
        assertTrue(habitNodeMapping["skincare_7"]!!.contains("habit_am_cleanser"))
        assertTrue(habitNodeMapping["skincare_7"]!!.contains("habit_pm_hydration"))
        assertTrue(habitNodeMapping["skincare_7"]!!.contains("habit_skin_am_routine"))
        assertTrue(habitNodeMapping["skincare_7"]!!.contains("habit_skin_pm_routine"))

        assertEquals(setOf("habit_spf"), habitNodeMapping["sunscreen_14"])
        assertEquals(setOf("habit_sleep"), habitNodeMapping["sleep_7"])
        assertEquals(setOf("habit_water"), habitNodeMapping["hydration_7"])
        assertEquals(setOf("habit_chin_tucks", "habit_screen_resets"), habitNodeMapping["posture_streak_3"])

        // Unknown node has no mapping
        assertFalse(habitNodeMapping.containsKey("unknown_habit"))
    }

    @Test
    fun testStreakCalculationConsecutiveDays() {
        val dates = setOf("20261101", "20261102", "20261103", "20261104", "20261105", "20261106", "20261107")
        val streak = calculateBestStreak(dates)
        assertEquals(7, streak)
    }

    @Test
    fun testStreakCalculationNonConsecutiveDays() {
        // 7 total logs but broken up: best streak is only 3
        val dates = setOf("20261101", "20261102", "20261103", "20261106", "20261107", "20261110", "20261111")
        val streak = calculateBestStreak(dates)
        assertEquals(3, streak)
    }

    @Test
    fun testStreakCalculationMultipleLogsSameDayCountOnce() {
        // Set deduplicates same-day logs
        val dates = setOf("20261101", "20261102")
        val streak = calculateBestStreak(dates)
        assertEquals(2, streak)
    }

    @Test
    fun testWeeklyRescanDistinctCalendarWeeks() {
        val now = 1793793600000L // arbitrary base time in ms
        val oneHour = 3600_000L
        val oneWeek = 7 * 24 * 3600_000L

        // 5 scans in same week -> 1 calendar week
        val sameWeekScans = listOf(now, now + oneHour, now + 2 * oneHour, now + 3 * oneHour, now + 4 * oneHour)
        assertEquals(1, calculateDistinctIsoWeeks(sameWeekScans))

        // 3 scans across 3 different weeks -> 3 calendar weeks
        val threeWeekScans = listOf(now, now + oneWeek, now + 2 * oneWeek)
        assertEquals(3, calculateDistinctIsoWeeks(threeWeekScans))
    }
}
