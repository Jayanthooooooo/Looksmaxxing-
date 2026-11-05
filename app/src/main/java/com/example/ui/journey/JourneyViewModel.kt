package com.example.ui.journey

import android.graphics.Bitmap
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.PhotoVault
import com.example.data.local.dao.HabitDao
import com.example.data.local.dao.PostureDao
import com.example.data.local.dao.ScanDao
import com.example.data.local.dao.SkillNodeDao
import com.example.data.local.entity.HabitEntity
import com.example.data.local.entity.ScanEntity
import com.example.data.local.entity.SkillNodeEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.temporal.IsoFields

class JourneyViewModel(
    private val skillNodeDao: SkillNodeDao,
    private val habitDao: HabitDao,
    private val scanDao: ScanDao,
    private val postureDao: PostureDao,
    val photoVault: PhotoVault
) : ViewModel() {

    private val habitNodeMapping = mapOf(
        "skincare_7" to setOf("habit_am_cleanser", "habit_pm_hydration", "habit_skin_am_routine", "habit_skin_pm_routine"),
        "sunscreen_14" to setOf("habit_spf"),
        "sleep_7" to setOf("habit_sleep"),
        "hydration_7" to setOf("habit_water"),
        "posture_streak_3" to setOf("habit_chin_tucks", "habit_screen_resets")
    )

    private val defaultNodes = listOf(
        SkillNodeEntity("first_face_scan", "First Face Scan", "Perform your initial facial structure scan.", "IN_PROGRESS", "SCAN", 1),
        SkillNodeEntity("skincare_7", "Skincare Routine 7 days", "Maintain skincare habit consistency for 7 days.", "IN_PROGRESS", "HABIT", 7),
        SkillNodeEntity("sleep_7", "Sleep 7 days", "Log optimal sleep for 7 days in a row.", "IN_PROGRESS", "HABIT", 7),
        SkillNodeEntity("posture_check", "Posture Check", "Complete your first ergonomic posture analysis.", "IN_PROGRESS", "SCAN", 1),
        SkillNodeEntity("haircut_photo", "Haircut", "Upload photo proof of a fresh haircut.", "IN_PROGRESS", "PHOTO", 1),
        SkillNodeEntity("beard_grooming_photo", "Beard Grooming", "Upload photo proof of neat beard lining or clean shave.", "IN_PROGRESS", "PHOTO", 1),
        SkillNodeEntity("hairstyle_change_photo", "Hairstyle Change", "Upload photo proof trying a new hairstyle suited to your face shape.", "IN_PROGRESS", "PHOTO", 1),
        SkillNodeEntity("sunscreen_14", "Sunscreen 14 days", "Apply daily broad-spectrum SPF for 14 days.", "IN_PROGRESS", "HABIT", 14),
        SkillNodeEntity("weekly_rescan", "Weekly Rescan", "Complete 3 facial scans across 3 weeks to track trends.", "IN_PROGRESS", "SCAN", 3),
        SkillNodeEntity("hydration_7", "Hydration Routine", "Log water intake habit for 7 days.", "IN_PROGRESS", "HABIT", 7),
        SkillNodeEntity("posture_streak_3", "Posture Alignment", "Complete 3 daily posture habit checks.", "IN_PROGRESS", "HABIT", 3),
        SkillNodeEntity("style_consult_photo", "Style Consultation", "Upload photo proof trying recommended eyewear or hair style.", "IN_PROGRESS", "PHOTO", 1),
        SkillNodeEntity("barber_visit_photo", "Barber Visit", "Upload photo proof after a professional grooming session.", "IN_PROGRESS", "PHOTO", 1),
        SkillNodeEntity("sun_protection_photo", "Sun Protection Check", "Upload photo proof wearing SPF or sun hat outdoors.", "IN_PROGRESS", "PHOTO", 1),
        SkillNodeEntity("scan_master_5", "Consistent Tracker", "Complete 5 total face scans over time.", "IN_PROGRESS", "SCAN", 5)
    )

    private val _snackbarEvent = MutableSharedFlow<String>()
    val snackbarEvent: SharedFlow<String> = _snackbarEvent.asSharedFlow()

    private val _nodeProgress = MutableStateFlow<Map<String, String>>(emptyMap())
    val nodeProgress: StateFlow<Map<String, String>> = _nodeProgress.asStateFlow()

    init {
        viewModelScope.launch(Dispatchers.IO) {
            val existingHabits = habitDao.getAllHabits()
            val existingIds = existingHabits.map { it.id }.toSet()
            val dailyScheduleJson = "[\"MON\",\"TUE\",\"WED\",\"THU\",\"FRI\",\"SAT\",\"SUN\"]"

            if ("habit_sleep" !in existingIds) {
                habitDao.insertOrUpdateHabit(
                    HabitEntity(
                        id = "habit_sleep",
                        label = "Sleep 7+ hours",
                        pillar = "Posture/Sleep",
                        scheduleJson = dailyScheduleJson,
                        active = true,
                        orderIdx = existingHabits.size
                    )
                )
            }

            if ("habit_water" !in existingIds) {
                habitDao.insertOrUpdateHabit(
                    HabitEntity(
                        id = "habit_water",
                        label = "Drink 2-3 L water",
                        pillar = "Habits",
                        scheduleJson = dailyScheduleJson,
                        active = true,
                        orderIdx = existingHabits.size + 1
                    )
                )
            }
        }
    }

    val skillNodes: StateFlow<List<SkillNodeEntity>> = combine(
        skillNodeDao.getAllNodes(),
        habitDao.observeAllCompletedLogs(),
        scanDao.observeAllActiveScans(),
        postureDao.observeAllPostureLogs()
    ) { nodes, habitLogs, scans, postureLogs ->
        if (nodes.isEmpty()) {
            skillNodeDao.insertNodes(defaultNodes)
            return@combine defaultNodes
        }

        val progressMap = mutableMapOf<String, String>()

        nodes.forEach { node ->
            when (node.requirementType) {
                "HABIT" -> {
                    val mappedHabitIds = habitNodeMapping[node.id]
                    val target = node.requirementValue
                    if (mappedHabitIds != null) {
                        val doneDates = habitLogs
                            .filter { it.done && it.habitId in mappedHabitIds }
                            .map { it.date }
                            .toSet()
                        val bestStreak = calculateBestStreak(doneDates)
                        val current = if (node.status == "DONE") target else minOf(bestStreak, target)
                        progressMap[node.id] = "$current / $target days"

                        if (node.status != "DONE" && bestStreak >= target) {
                            skillNodeDao.updateNodeStatus(node.id, "DONE")
                        }
                    } else {
                        // If a node id has no mapping, leave it IN_PROGRESS. Never guess.
                        val current = if (node.status == "DONE") target else 0
                        progressMap[node.id] = "$current / $target days"
                    }
                }
                "SCAN" -> {
                    val target = node.requirementValue
                    when (node.id) {
                        "posture_check" -> {
                            val count = postureLogs.size
                            val current = if (node.status == "DONE") target else minOf(count, target)
                            progressMap[node.id] = "$current / $target scans"

                            if (node.status != "DONE" && count >= 1) {
                                skillNodeDao.updateNodeStatus(node.id, "DONE")
                            }
                        }
                        "weekly_rescan" -> {
                            val distinctWeeks = calculateDistinctIsoWeeks(scans)
                            val current = if (node.status == "DONE") target else minOf(distinctWeeks, target)
                            progressMap[node.id] = "$current / $target scans"

                            if (node.status != "DONE" && distinctWeeks >= 3) {
                                skillNodeDao.updateNodeStatus(node.id, "DONE")
                            }
                        }
                        else -> {
                            val count = scans.size
                            val current = if (node.status == "DONE") target else minOf(count, target)
                            progressMap[node.id] = "$current / $target scans"

                            if (node.status != "DONE" && count >= target) {
                                skillNodeDao.updateNodeStatus(node.id, "DONE")
                            }
                        }
                    }
                }
                "PHOTO" -> {
                    // Photo proof node progress is not text-based
                }
            }
        }

        _nodeProgress.value = progressMap
        nodes
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

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

    private fun calculateDistinctIsoWeeks(scans: List<ScanEntity>): Int {
        if (scans.isEmpty()) return 0
        val zone = ZoneId.systemDefault()
        return scans.map { scan ->
            val zdt = Instant.ofEpochMilli(scan.createdAt).atZone(zone)
            val week = zdt.get(IsoFields.WEEK_OF_WEEK_BASED_YEAR)
            val year = zdt.get(IsoFields.WEEK_BASED_YEAR)
            "$year-$week"
        }.distinct().size
    }

    fun saveProofPhoto(nodeId: String, bitmap: Bitmap, onSuccess: (() -> Unit)? = null) {
        viewModelScope.launch(Dispatchers.IO) {
            val maxDim = 1280
            val width = bitmap.width
            val height = bitmap.height
            val scaledBitmap = if (width > maxDim || height > maxDim) {
                val scale = maxDim.toFloat() / maxOf(width, height)
                val targetWidth = (width * scale).toInt().coerceAtLeast(1)
                val targetHeight = (height * scale).toInt().coerceAtLeast(1)
                Bitmap.createScaledBitmap(bitmap, targetWidth, targetHeight, true)
            } else {
                bitmap
            }

            val fileId = "proof_$nodeId"
            val file = photoVault.saveEncryptedPhoto(fileId, scaledBitmap)
            if (file != null) {
                skillNodeDao.updateNodeProofAndStatus(nodeId, "DONE", fileId)
                withContext(Dispatchers.Main) {
                    onSuccess?.invoke()
                }
            } else {
                _snackbarEvent.emit("Could not save photo")
            }
        }
    }

    fun removeProofPhoto(nodeId: String) {
        viewModelScope.launch(Dispatchers.IO) {
            val fileId = "proof_$nodeId"
            photoVault.deletePhoto(fileId)
            skillNodeDao.updateNodeProofAndStatus(nodeId, "IN_PROGRESS", null)
        }
    }
}
