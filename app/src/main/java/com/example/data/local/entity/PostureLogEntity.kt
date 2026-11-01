package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "posture_logs")
data class PostureLogEntity(
    @PrimaryKey val id: String,
    val createdAt: Long,
    val craniovertebralAngle: Float,
    val forwardOffsetRatio: Float,
    val bandName: String,
    val note: String = ""
)
