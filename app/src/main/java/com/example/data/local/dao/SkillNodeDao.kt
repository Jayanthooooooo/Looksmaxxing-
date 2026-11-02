package com.example.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.data.local.entity.SkillNodeEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface SkillNodeDao {
    @Query("SELECT * FROM skill_nodes")
    fun getAllNodes(): Flow<List<SkillNodeEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNode(node: SkillNodeEntity)

    @Query("UPDATE skill_nodes SET status = :status WHERE id = :id")
    suspend fun updateNodeStatus(id: String, status: String)
}
