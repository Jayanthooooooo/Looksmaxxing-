package com.example.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.data.local.entity.BarberShopItemEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface BarberShopDao {
    @Query("SELECT * FROM barber_shop_items")
    fun getAllItems(): Flow<List<BarberShopItemEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertItem(item: BarberShopItemEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertItems(items: List<BarberShopItemEntity>)

    @Query("UPDATE barber_shop_items SET isEquipped = :isEquipped WHERE id = :id")
    suspend fun updateEquippedStatus(id: String, isEquipped: Boolean)
}
