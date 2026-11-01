package com.example.data.repo

import android.content.Context
import com.example.domain.skin.ConflictCheckResult
import com.example.domain.skin.ConflictRule
import com.example.domain.skin.RoutineBuildResult
import com.example.domain.skin.SkinIngredient
import com.example.domain.skin.SkinIngredientEngine
import com.example.domain.skin.SunscreenGuidance
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class SkinToolsRepository(private val context: Context) {

    private var cachedIngredients: List<SkinIngredient>? = null
    private var cachedRules: List<ConflictRule>? = null

    suspend fun getIngredients(): List<SkinIngredient> = withContext(Dispatchers.IO) {
        if (cachedIngredients == null) {
            loadFromAssets()
        }
        cachedIngredients ?: emptyList()
    }

    suspend fun getRules(): List<ConflictRule> = withContext(Dispatchers.IO) {
        if (cachedRules == null) {
            loadFromAssets()
        }
        cachedRules ?: emptyList()
    }

    private fun loadFromAssets() {
        val jsonStr = context.assets.open("ingredients.json").bufferedReader().use { it.readText() }
        val (ingredients, rules) = SkinIngredientEngine.parseData(jsonStr)
        cachedIngredients = ingredients
        cachedRules = rules
    }

    suspend fun checkConflicts(
        selectedIds: List<String>,
        isTeen: Boolean = false,
        isPregnant: Boolean = false
    ): List<ConflictCheckResult> = withContext(Dispatchers.Default) {
        val all = getIngredients()
        val rules = getRules()
        SkinIngredientEngine.checkConflicts(selectedIds, all, rules, isTeen, isPregnant)
    }

    suspend fun buildRoutine(
        skinType: String,
        concerns: List<String>,
        selectedIds: List<String>,
        isTeen: Boolean = false
    ): RoutineBuildResult = withContext(Dispatchers.Default) {
        val all = getIngredients()
        SkinIngredientEngine.buildRoutine(skinType, concerns, selectedIds, all, isTeen)
    }

    fun getSunGuidance(isOutdoors: Boolean, uvLevel: String): SunscreenGuidance {
        return SkinIngredientEngine.calculateSunGuidance(isOutdoors, uvLevel)
    }
}
