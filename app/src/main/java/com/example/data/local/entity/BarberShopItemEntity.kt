package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "barber_shop_items")
data class BarberShopItemEntity(
    @PrimaryKey val id: String,
    val name: String,
    val type: String, // HAIRSTYLE, BEARD, GROOMING
    val isEquipped: Boolean = false,
    val description: String = ""
)
