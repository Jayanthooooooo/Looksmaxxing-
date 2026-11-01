package com.example.domain.skin

import org.json.JSONObject

object SkinIngredientEngine {

    fun parseData(jsonString: String): Pair<List<SkinIngredient>, List<ConflictRule>> {
        val root = JSONObject(jsonString)
        val ingArray = root.getJSONArray("ingredients")
        val ingredients = mutableListOf<SkinIngredient>()
        for (i in 0 until ingArray.length()) {
            val obj = ingArray.getJSONObject(i)
            ingredients.add(
                SkinIngredient(
                    id = obj.getString("id"),
                    name = obj.getString("name"),
                    category = obj.getString("category"),
                    whatItDoes = obj.getString("whatItDoes"),
                    bestTime = obj.getString("bestTime"),
                    startFrequency = obj.getString("startFrequency"),
                    commonIrritation = obj.getString("commonIrritation"),
                    isTeenRestricted = obj.optBoolean("isTeenRestricted", false),
                    isPregnancyRestricted = obj.optBoolean("isPregnancyRestricted", false)
                )
            )
        }

        val ruleArray = root.getJSONArray("conflictRules")
        val rules = mutableListOf<ConflictRule>()
        for (i in 0 until ruleArray.length()) {
            val obj = ruleArray.getJSONObject(i)
            val statusStr = obj.getString("status")
            val status = when (statusStr) {
                "Fine together" -> ConflictStatus.FINE_TOGETHER
                "Use with care" -> ConflictStatus.USE_WITH_CARE
                "Better separated" -> ConflictStatus.BETTER_SEPARATED
                else -> ConflictStatus.USE_WITH_CARE
            }
            rules.add(
                ConflictRule(
                    id1 = obj.getString("id1"),
                    id2 = obj.getString("id2"),
                    status = status,
                    reason = obj.getString("reason"),
                    saferWay = obj.getString("saferWay")
                )
            )
        }

        return Pair(ingredients, rules)
    }

    fun checkConflicts(
        selectedIds: List<String>,
        allIngredients: List<SkinIngredient>,
        rules: List<ConflictRule>,
        isTeen: Boolean = false,
        isPregnant: Boolean = false
    ): List<ConflictCheckResult> {
        val ingredientMap = allIngredients.associateBy { it.id }
        val results = mutableListOf<ConflictCheckResult>()

        val filteredSelected = selectedIds.filter { id ->
            val ing = ingredientMap[id] ?: return@filter true
            if (isTeen && ing.isTeenRestricted) false else true
        }

        for (i in 0 until filteredSelected.size) {
            for (j in i + 1 until filteredSelected.size) {
                val id1 = filteredSelected[i]
                val id2 = filteredSelected[j]
                val ing1 = ingredientMap[id1] ?: continue
                val ing2 = ingredientMap[id2] ?: continue

                val rule = rules.firstOrNull {
                    (it.id1 == id1 && it.id2 == id2) || (it.id1 == id2 && it.id2 == id1)
                }

                if (rule != null) {
                    results.add(
                        ConflictCheckResult(
                            ingredient1 = ing1,
                            ingredient2 = ing2,
                            status = rule.status,
                            reason = rule.reason,
                            saferWay = rule.saferWay
                        )
                    )
                } else {
                    val isActive1 = ing1.category in listOf("Exfoliant", "Retinoid", "Acne Active")
                    val isActive2 = ing2.category in listOf("Exfoliant", "Retinoid", "Acne Active")

                    if (isActive1 && isActive2) {
                        results.add(
                            ConflictCheckResult(
                                ingredient1 = ing1,
                                ingredient2 = ing2,
                                status = ConflictStatus.USE_WITH_CARE,
                                reason = "Combining multiple active exfoliants or acne treatments increases dry patches and barrier irritation risk.",
                                saferWay = "Introduce one active at a time and alternate usage days."
                            )
                        )
                    } else {
                        results.add(
                            ConflictCheckResult(
                                ingredient1 = ing1,
                                ingredient2 = ing2,
                                status = ConflictStatus.FINE_TOGETHER,
                                reason = "No known adverse ingredient interaction.",
                                saferWay = "Can be safely used together in your routine."
                            )
                        )
                    }
                }
            }
        }

        return results
    }

    fun buildRoutine(
        skinType: String,
        concerns: List<String>,
        selectedIds: List<String>,
        allIngredients: List<SkinIngredient>,
        isTeen: Boolean = false
    ): RoutineBuildResult {
        val ingMap = allIngredients.associateBy { it.id }
        val available = selectedIds.mapNotNull { ingMap[it] }.filter { ing ->
            if (isTeen && ing.isTeenRestricted) false else true
        }

        val activeCategories = setOf("Exfoliant", "Retinoid", "Acne Active", "Targeted Active")
        val activeIngredients = available.filter { it.category in activeCategories }
        val activeCount = activeIngredients.size

        val activeWarning = if (activeCount > 2) {
            "Caution: You have selected $activeCount strong active ingredients. Introducing multiple potent actives at once can damage your moisture barrier. Follow the 2-week Start Slow schedule below."
        } else null

        // AM Steps: 1. Cleanse -> 2. Treat (AM actives) -> 3. Moisturise -> 4. Sunscreen
        val amTreat = available.filter { it.bestTime == "AM" || it.bestTime == "Either" }
        val amSteps = mutableListOf<RoutineStep>()
        amSteps.add(
            RoutineStep(
                order = 1,
                stepType = "Cleanse",
                title = "Gentle Morning Cleanser",
                description = "Cleanse face with lukewarm water or a mild hydrating cleanser to remove overnight oils.",
                timeOfDay = "AM"
            )
        )
        if (amTreat.isNotEmpty()) {
            val names = amTreat.joinToString(", ") { it.name }
            amSteps.add(
                RoutineStep(
                    order = 2,
                    stepType = "Treat",
                    title = "AM Treatment",
                    description = "Apply serum / treatment: $names.",
                    isActive = amTreat.any { it.category in activeCategories },
                    timeOfDay = "AM"
                )
            )
        }
        amSteps.add(
            RoutineStep(
                order = 3,
                stepType = "Moisturise",
                title = "Hydrating Moisturizer",
                description = "Apply a gel or lotion moisturizer formulated for $skinType skin to lock in moisture.",
                timeOfDay = "AM"
            )
        )
        amSteps.add(
            RoutineStep(
                order = 4,
                stepType = "Sunscreen",
                title = "Broad-Spectrum Sunscreen (SPF 30+)",
                description = "Final essential AM step. Apply 2 finger lengths of sunscreen to face and neck every morning.",
                timeOfDay = "AM"
            )
        )

        // PM Steps: 1. Cleanse -> 2. Treat (PM actives) -> 3. Moisturise
        val pmTreat = available.filter { it.bestTime == "PM" || it.bestTime == "Either" }
        val pmSteps = mutableListOf<RoutineStep>()
        pmSteps.add(
            RoutineStep(
                order = 1,
                stepType = "Cleanse",
                title = "PM Double Cleanse",
                description = "Use an oil-based cleanser followed by a gentle foaming cleanser to thoroughly dissolve sunscreen, makeup, and pollution.",
                timeOfDay = "PM"
            )
        )
        if (pmTreat.isNotEmpty()) {
            val names = pmTreat.joinToString(", ") { it.name }
            pmSteps.add(
                RoutineStep(
                    order = 2,
                    stepType = "Treat",
                    title = "PM Targeted Treatment",
                    description = "Apply night treatment: $names.",
                    isActive = pmTreat.any { it.category in activeCategories },
                    timeOfDay = "PM"
                )
            )
        }
        pmSteps.add(
            RoutineStep(
                order = 3,
                stepType = "Moisturise",
                title = "Barrier Repair Cream",
                description = "Apply a richer night cream with ceramides or fatty acids to support cell renewal overnight.",
                timeOfDay = "PM"
            )
        )

        // Start Slow Schedule: introduce 1 new active every 2 weeks
        val startSlowPhases = mutableListOf<StartSlowPhase>()
        if (activeIngredients.isNotEmpty()) {
            activeIngredients.forEachIndexed { index, ing ->
                val startWeek = (index * 2) + 1
                val endWeek = (index * 2) + 2
                val weekRange = "Weeks $startWeek–$endWeek"
                val freq = if (index == 0) "2 nights per week" else "1-2 nights per week (alternate days)"
                startSlowPhases.add(
                    StartSlowPhase(
                        weeks = weekRange,
                        activeName = ing.name,
                        frequency = freq,
                        tip = "Patch test behind ear for 24h. Monitor for redness or flaking before increasing frequency."
                    )
                )
            }
        } else {
            startSlowPhases.add(
                StartSlowPhase(
                    weeks = "Weeks 1–2",
                    activeName = "Hydrating Essentials",
                    frequency = "Daily AM & PM",
                    tip = "Establish your core Cleanse, Moisturise, and Sunscreen base before adding potent actives."
                )
            )
        }

        return RoutineBuildResult(
            amSteps = amSteps,
            pmSteps = pmSteps,
            activeCount = activeCount,
            activeWarning = activeWarning,
            patchTestReminder = "Apply a small amount behind ear or on inner forearm for 24h before full face application.",
            startSlowPhases = startSlowPhases
        )
    }

    fun calculateSunGuidance(isOutdoors: Boolean, uvLevel: String): SunscreenGuidance {
        return when {
            uvLevel.equals("Very high", ignoreCase = true) -> {
                SunscreenGuidance(
                    uvLevel = "Very High (UV 8-10+)",
                    isOutdoors = isOutdoors,
                    title = "Extreme UV Protection Needed",
                    recommendedSpf = "SPF 50+ Broad-Spectrum Mineral/Chemical",
                    reapplyInterval = if (isOutdoors) "Every 80 minutes (or immediately after swimming/sweating)" else "Reapply at lunchtime / before leaving indoors",
                    mainTip = if (isOutdoors) "Seek shade between 10am and 4pm. Wear UV400 sunglasses and a wide-brimmed hat." else "Apply SPF in the morning even indoors if near large windows or skylights.",
                    details = "Very high UV rays accelerate collagen breakdown and hyperpigmentation."
                )
            }
            uvLevel.equals("High", ignoreCase = true) -> {
                SunscreenGuidance(
                    uvLevel = "High (UV 6-7)",
                    isOutdoors = isOutdoors,
                    title = "High UV Protection Recommended",
                    recommendedSpf = "SPF 30 to SPF 50 Broad-Spectrum",
                    reapplyInterval = if (isOutdoors) "Every 2 hours while outdoors" else "Reapply before heading outside",
                    mainTip = if (isOutdoors) "Wear protective clothing and reapply sunscreen regularly." else "Morning application protects against indirect UV and blue light.",
                    details = "High UV index causes sunburn in 20-30 minutes without sun protection."
                )
            }
            uvLevel.equals("Moderate", ignoreCase = true) -> {
                SunscreenGuidance(
                    uvLevel = "Moderate (UV 3-5)",
                    isOutdoors = isOutdoors,
                    title = "Moderate UV Shield",
                    recommendedSpf = "SPF 30 Broad-Spectrum",
                    reapplyInterval = if (isOutdoors) "Every 2-3 hours" else "Morning application sufficient",
                    mainTip = if (isOutdoors) "Wear SPF 30 daily. Seek shade during peak midday hours." else "Standard morning application covers indoor routine.",
                    details = "Moderate UV exposure still contributes to photoaging over time."
                )
            }
            else -> { // Low
                SunscreenGuidance(
                    uvLevel = "Low (UV 1-2)",
                    isOutdoors = isOutdoors,
                    title = "Low UV Defense",
                    recommendedSpf = "SPF 15 to 30",
                    reapplyInterval = "Morning application",
                    mainTip = "Minimal sun hazard, but daily SPF maintains hyperpigmentation progress.",
                    details = "Low UV days require baseline protection if using exfoliating acids or retinoids."
                )
            }
        }
    }
}
