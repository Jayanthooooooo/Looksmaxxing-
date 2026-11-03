package com.example.ui.journey

import android.graphics.Bitmap
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.PhotoVault
import com.example.data.local.dao.HabitDao
import com.example.data.local.dao.ScanDao
import com.example.data.local.dao.SkillNodeDao
import com.example.data.local.entity.SkillNodeEntity
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class JourneyViewModel(
    private val skillNodeDao: SkillNodeDao,
    private val habitDao: HabitDao,
    private val scanDao: ScanDao,
    val photoVault: PhotoVault
) : ViewModel() {

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

    val skillNodes: StateFlow<List<SkillNodeEntity>> = combine(
        skillNodeDao.getAllNodes(),
        habitDao.observeAllCompletedLogs(),
        scanDao.observeAllActiveScans()
    ) { nodes, habitLogs, scans ->
        if (nodes.isEmpty()) {
            skillNodeDao.insertNodes(defaultNodes)
            return@combine defaultNodes
        }

        val scanCount = scans.size
        val habitDoneCounts = habitLogs.groupBy { it.habitId }.mapValues { it.value.size }

        nodes.forEach { node ->
            if (node.status != "DONE") {
                if (node.requirementType == "SCAN" && scanCount >= node.requirementValue) {
                    skillNodeDao.updateNodeStatus(node.id, "DONE")
                } else if (node.requirementType == "HABIT") {
                    val habitCount = habitDoneCounts[node.id] ?: habitDoneCounts.values.maxOrNull() ?: 0
                    if (habitCount >= node.requirementValue) {
                        skillNodeDao.updateNodeStatus(node.id, "DONE")
                    }
                }
            }
        }

        nodes
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun saveProofPhoto(nodeId: String, bitmap: Bitmap) {
        viewModelScope.launch {
            val fileId = "proof_$nodeId"
            val file = photoVault.saveEncryptedPhoto(fileId, bitmap)
            if (file != null) {
                skillNodeDao.updateNodeProofAndStatus(nodeId, "DONE", fileId)
            }
        }
    }

    fun removeProofPhoto(nodeId: String) {
        viewModelScope.launch {
            val fileId = "proof_$nodeId"
            photoVault.deletePhoto(fileId)
            skillNodeDao.updateNodeProofAndStatus(nodeId, "IN_PROGRESS", null)
        }
    }
}
