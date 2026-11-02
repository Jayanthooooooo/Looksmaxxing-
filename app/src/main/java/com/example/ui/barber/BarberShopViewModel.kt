package com.example.ui.barber

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.dao.BarberShopDao
import com.example.data.local.entity.BarberShopItemEntity
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class BarberShopViewModel(private val barberShopDao: BarberShopDao) : ViewModel() {
    val items: StateFlow<List<BarberShopItemEntity>> = barberShopDao.getAllItems()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun equipItem(id: String) {
        viewModelScope.launch {
            barberShopDao.updateEquippedStatus(id, true)
        }
    }
}
