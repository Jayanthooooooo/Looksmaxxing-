package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "scans")
data class ScanEntity(
    @PrimaryKey val id: String,
    val createdAt: Long,
    val kind: String, // "front" or "side"
    val engineVersion: String,
    val qualityScore: Int,
    val qualityFlagsJson: String,
    val metricsJson: String,
    val indicesJson: String,
    val skinJson: String,
    val photoFileId: String?,
    val note: String,
    val syncedAt: Long?,
    val updatedAt: Long,
    val deleted: Boolean = false
)
