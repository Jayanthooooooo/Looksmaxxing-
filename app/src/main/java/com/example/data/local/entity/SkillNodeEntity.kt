package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "skill_nodes")
data class SkillNodeEntity(
    @PrimaryKey val id: String,
    val name: String,
    val description: String,
    val status: String, // LOCKED, IN_PROGRESS, DONE
    val requirementType: String, // e.g., HABIT, SCAN, PHOTO
    val requirementValue: Int, // e.g., days streak or count
    val proofPhotoPath: String? = null
)
