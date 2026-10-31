package com.example.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.local.entity.ScanEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ScanDao {
    @Query("SELECT * FROM scans WHERE deleted = 0 ORDER BY createdAt DESC")
    fun observeAllActiveScans(): Flow<List<ScanEntity>>

    @Query("SELECT * FROM scans WHERE deleted = 0 ORDER BY createdAt DESC")
    suspend fun getAllActiveScansSync(): List<ScanEntity>


    @Query("SELECT * FROM scans WHERE deleted = 0 AND kind = :kind ORDER BY createdAt DESC")
    fun observeActiveScansByKind(kind: String): Flow<List<ScanEntity>>

    @Query("SELECT * FROM scans WHERE id = :id LIMIT 1")
    suspend fun getScanById(id: String): ScanEntity?

    @Query("SELECT * FROM scans WHERE id = :id LIMIT 1")
    fun observeScanById(id: String): Flow<ScanEntity?>

    @Query("SELECT * FROM scans WHERE deleted = 0 ORDER BY createdAt DESC LIMIT 1")
    fun observeLatestActiveScan(): Flow<ScanEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateScan(scan: ScanEntity)

    @Update
    suspend fun updateScan(scan: ScanEntity)

    @Query("UPDATE scans SET deleted = 1, updatedAt = :updatedAt WHERE id = :id")
    suspend fun markDeleted(id: String, updatedAt: Long)

    @Query("DELETE FROM scans WHERE id = :id")
    suspend fun deletePermanently(id: String)

    @Query("DELETE FROM scans")
    suspend fun clearAll()

    @Query("UPDATE scans SET photoFileId = NULL, updatedAt = :updatedAt WHERE id = :id")
    suspend fun clearPhoto(id: String, updatedAt: Long)

    @Query("UPDATE scans SET photoFileId = NULL, updatedAt = :updatedAt")
    suspend fun clearAllPhotos(updatedAt: Long)
}
