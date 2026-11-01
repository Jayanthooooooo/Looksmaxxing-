package com.example.ui.screens.skintools

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.LumoraApp
import com.example.data.local.entity.HabitEntity
import com.example.domain.skin.ConflictCheckResult
import com.example.domain.skin.RoutineBuildResult
import com.example.domain.skin.SkinIngredient
import com.example.domain.skin.SunscreenGuidance
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class SkinToolsUiState(
    val selectedTab: Int = 0,
    val searchQuery: String = "",
    val allIngredients: List<SkinIngredient> = emptyList(),
    val selectedIngredientIds: Set<String> = setOf("retinol", "vitamin_c", "hyaluronic_acid", "ceramides"),
    val isTeen: Boolean = false,
    val isPregnant: Boolean = false,
    val skinType: String = "Combination",
    val selectedConcerns: List<String> = listOf("Acne", "Hyperpigmentation"),
    val conflicts: List<ConflictCheckResult> = emptyList(),
    val routine: RoutineBuildResult? = null,
    val isOutdoors: Boolean = true,
    val uvLevel: String = "High",
    val sunGuidance: SunscreenGuidance? = null,
    val habitSyncSuccess: Boolean = false
)

class SkinToolsViewModel(application: Application) : AndroidViewModel(application) {
    private val app = application as LumoraApp
    private val repo = app.container.skinToolsRepository
    private val habitRepo = app.container.habitRepository

    private val _uiState = MutableStateFlow(SkinToolsUiState())
    val uiState: StateFlow<SkinToolsUiState> = _uiState.asStateFlow()

    init {
        loadData()
    }

    private fun loadData() {
        viewModelScope.launch {
            val ingredients = repo.getIngredients()
            _uiState.value = _uiState.value.copy(allIngredients = ingredients)
            recalculateAll()
        }
    }

    fun setSelectedTab(tabIndex: Int) {
        _uiState.value = _uiState.value.copy(selectedTab = tabIndex)
    }

    fun setSearchQuery(query: String) {
        _uiState.value = _uiState.value.copy(searchQuery = query)
    }

    fun toggleIngredient(id: String) {
        val current = _uiState.value.selectedIngredientIds.toMutableSet()
        if (current.contains(id)) {
            current.remove(id)
        } else {
            current.add(id)
        }
        _uiState.value = _uiState.value.copy(selectedIngredientIds = current)
        recalculateAll()
    }

    fun setIsTeen(isTeen: Boolean) {
        _uiState.value = _uiState.value.copy(isTeen = isTeen)
        recalculateAll()
    }

    fun setIsPregnant(isPregnant: Boolean) {
        _uiState.value = _uiState.value.copy(isPregnant = isPregnant)
        recalculateAll()
    }

    fun setSkinType(skinType: String) {
        _uiState.value = _uiState.value.copy(skinType = skinType)
        recalculateAll()
    }

    fun setIsOutdoors(isOutdoors: Boolean) {
        _uiState.value = _uiState.value.copy(isOutdoors = isOutdoors)
        updateSunGuidance()
    }

    fun setUvLevel(uvLevel: String) {
        _uiState.value = _uiState.value.copy(uvLevel = uvLevel)
        updateSunGuidance()
    }

    private fun recalculateAll() {
        viewModelScope.launch {
            val state = _uiState.value
            val selectedList = state.selectedIngredientIds.toList()

            val conflicts = repo.checkConflicts(selectedList, state.isTeen, state.isPregnant)
            val routine = repo.buildRoutine(state.skinType, state.selectedConcerns, selectedList, state.isTeen)
            val sunGuidance = repo.getSunGuidance(state.isOutdoors, state.uvLevel)

            _uiState.value = state.copy(
                conflicts = conflicts,
                routine = routine,
                sunGuidance = sunGuidance
            )
        }
    }

    private fun updateSunGuidance() {
        val state = _uiState.value
        val sunGuidance = repo.getSunGuidance(state.isOutdoors, state.uvLevel)
        _uiState.value = state.copy(sunGuidance = sunGuidance)
    }

    fun addRoutineToDailyHabits() {
        viewModelScope.launch {
            val routine = _uiState.value.routine ?: return@launch
            val habits = mutableListOf<HabitEntity>()

            habits.add(
                HabitEntity(
                    id = "habit_skin_am_routine",
                    label = "Morning Skin Routine & Sunscreen",
                    pillar = "Skin",
                    scheduleJson = "[\"MON\",\"TUE\",\"WED\",\"THU\",\"FRI\",\"SAT\",\"SUN\"]",
                    active = true,
                    orderIdx = 20
                )
            )

            habits.add(
                HabitEntity(
                    id = "habit_skin_pm_routine",
                    label = "Evening Cleanse & Actives",
                    pillar = "Skin",
                    scheduleJson = "[\"MON\",\"TUE\",\"WED\",\"THU\",\"FRI\",\"SAT\",\"SUN\"]",
                    active = true,
                    orderIdx = 21
                )
            )

            if (routine.startSlowPhases.isNotEmpty()) {
                val activeName = routine.startSlowPhases.first().activeName
                habits.add(
                    HabitEntity(
                        id = "habit_skin_start_slow",
                        label = "Start Slow: $activeName (2x/wk)",
                        pillar = "Skin",
                        scheduleJson = "[\"TUE\",\"FRI\"]",
                        active = true,
                        orderIdx = 22
                    )
                )
            }

            habits.forEach { habitRepo.saveHabit(it) }
            _uiState.value = _uiState.value.copy(habitSyncSuccess = true)
        }
    }
}
