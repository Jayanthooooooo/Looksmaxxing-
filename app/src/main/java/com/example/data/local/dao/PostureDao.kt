package com.example.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.data.local.entity.PostureLogEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface PostureDao {
    @Query("SELECT * FROM posture_logs ORDER BY createdAt DESC")
    fun observeAllPostureLogs(): Flow<List<PostureLogEntity>>

    @Query("SELECT * FROM posture_logs ORDER BY createdAt DESC LIMIT 1")
    suspend fun getLatestPostureLog(): PostureLogEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPostureLog(log: PostureLogEntity)

    @Query("DELETE FROM posture_logs WHERE id = :id")
    suspend fun deletePostureLog(id: String)
}
