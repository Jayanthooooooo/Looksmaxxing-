package com.example.ui.barber

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.dao.BarberShopDao
import com.example.data.local.entity.BarberShopItemEntity
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class BarberShopViewModel(private val barberShopDao: BarberShopDao) : ViewModel() {

    private val seedItems = listOf(
        BarberShopItemEntity("skin_fade", "Low/Mid Skin Fade", "HAIRSTYLE", false, "Clean taper on the sides tapering down to skin."),
        BarberShopItemEntity("textured_fringe", "Textured Crop / Fringe", "HAIRSTYLE", false, "Short textured top with forward styling for high forehead balancing."),
        BarberShopItemEntity("buzz_cut", "Uniform Buzz Cut", "HAIRSTYLE", false, "Short, clean all-around cut emphasizing facial structure."),
        BarberShopItemEntity("side_part", "Classic Side Part", "HAIRSTYLE", false, "Neat tapered sides with defined comb-over side part."),
        BarberShopItemEntity("beard_lining", "Beard Line-Up & Taper", "BEARD", false, "Crisp cheek line and neck cleanup with faded sideburns."),
        BarberShopItemEntity("stubble_trim", "Heavy Stubble (3-5mm)", "BEARD", false, "Even stubble length defining jawline shadow without bulk."),
        BarberShopItemEntity("eyebrow_cleanup", "Eyebrow Arch Cleanup", "GROOMING", false, "Trimming stray hairs between brows and defining natural arch."),
        BarberShopItemEntity("hairline_edge", "Natural Hairline Shape-Up", "GROOMING", false, "Trimming sharp natural edges around temples and nape.")
    )

    val items: StateFlow<List<BarberShopItemEntity>> = barberShopDao.getAllItems()
        .onEach { list ->
            if (list.isEmpty()) {
                barberShopDao.insertItems(seedItems)
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun toggleTriedStatus(id: String, currentStatus: Boolean) {
        viewModelScope.launch {
            barberShopDao.updateEquippedStatus(id, !currentStatus)
        }
    }
}
