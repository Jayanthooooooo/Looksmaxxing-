package com.example.domain.skin

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class SkinToolsUnitTest {

    private val sampleIngredients = listOf(
        SkinIngredient("retinol", "Retinol", "Retinoid", "Smooths lines", "PM", "1x/wk", "Dryness", isTeenRestricted = true, isPregnancyRestricted = true),
        SkinIngredient("retinaldehyde", "Retinal", "Retinoid", "Texturing", "PM", "1x/wk", "Dryness", isTeenRestricted = true, isPregnancyRestricted = true),
        SkinIngredient("tretinoin", "Tretinoin", "Retinoid", "Prescription", "PM", "2x/wk", "Peeling", isTeenRestricted = true, isPregnancyRestricted = true),
        SkinIngredient("glycolic_acid", "Glycolic Acid", "Exfoliant", "Exfoliates", "PM", "2x/wk", "Stinging", isTeenRestricted = true),
        SkinIngredient("salicylic_acid", "Salicylic Acid", "Exfoliant", "Pores", "PM", "2x/wk", "Dryness"),
        SkinIngredient("benzoyl_peroxide", "Benzoyl Peroxide", "Acne Active", "Bacteria", "AM", "2x/wk", "Bleaching", isTeenRestricted = true),
        SkinIngredient("vitamin_c", "Vitamin C", "Antioxidant", "Brightens", "AM", "Daily", "Tingling"),
        SkinIngredient("niacinamide", "Niacinamide", "Barrier Support", "Oil control", "Either", "Daily", "Flushing"),
        SkinIngredient("copper_peptides", "Copper Peptides", "Peptides", "Firming", "PM", "Daily", "Mild redness"),
        SkinIngredient("azelaic_acid", "Azelaic Acid", "Targeted Active", "Redness", "Either", "Daily", "Tingling"),
        SkinIngredient("bakuchiol", "Bakuchiol", "Botanical Active", "Gentle smooth", "Either", "Daily", "None"),
        SkinIngredient("hyaluronic_acid", "Hyaluronic Acid", "Hydrator", "Plumps", "Either", "Daily", "None"),
        SkinIngredient("ceramides", "Ceramides", "Barrier Support", "Repairs", "Either", "Daily", "None")
    )

    private val sampleRules = listOf(
        ConflictRule("retinol", "vitamin_c", ConflictStatus.BETTER_SEPARATED, "pH difference", "AM/PM split"),
        ConflictRule("retinol", "glycolic_acid", ConflictStatus.BETTER_SEPARATED, "Over-exfoliation", "Alternate nights"),
        ConflictRule("retinol", "salicylic_acid", ConflictStatus.BETTER_SEPARATED, "Barrier disruption", "Separate days"),
        ConflictRule("retinol", "benzoyl_peroxide", ConflictStatus.BETTER_SEPARATED, "Oxidation", "Separate AM/PM"),
        ConflictRule("benzoyl_peroxide", "salicylic_acid", ConflictStatus.USE_WITH_CARE, "Dryness", "Alternate days"),
        ConflictRule("vitamin_c", "glycolic_acid", ConflictStatus.USE_WITH_CARE, "Destabilization", "AM/PM split"),
        ConflictRule("vitamin_c", "niacinamide", ConflictStatus.FINE_TOGETHER, "Compatible", "Apply C then Niacinamide"),
        ConflictRule("copper_peptides", "retinol", ConflictStatus.USE_WITH_CARE, "Oxidation", "Peptides AM, Retinol PM"),
        ConflictRule("copper_peptides", "vitamin_c", ConflictStatus.BETTER_SEPARATED, "Chelation", "C AM, Peptides PM"),
        ConflictRule("azelaic_acid", "niacinamide", ConflictStatus.FINE_TOGETHER, "Synergistic", "Layer together"),
        ConflictRule("glycolic_acid", "salicylic_acid", ConflictStatus.USE_WITH_CARE, "Over-exfoliation", "Alternate days"),
        ConflictRule("bakuchiol", "hyaluronic_acid", ConflictStatus.FINE_TOGETHER, "Hydrating pair", "Apply HA then Bakuchiol"),
        ConflictRule("hyaluronic_acid", "ceramides", ConflictStatus.FINE_TOGETHER, "Barrier pair", "HA then Ceramides"),
        ConflictRule("benzoyl_peroxide", "tretinoin", ConflictStatus.BETTER_SEPARATED, "Degradation", "BP AM, Tretinoin PM")
    )

    @Test
    fun testIngredientPairConflictsAtLeast12Pairs() {
        val pairsToTest = listOf(
            Pair("retinol", "vitamin_c") to ConflictStatus.BETTER_SEPARATED,
            Pair("retinol", "glycolic_acid") to ConflictStatus.BETTER_SEPARATED,
            Pair("retinol", "salicylic_acid") to ConflictStatus.BETTER_SEPARATED,
            Pair("retinol", "benzoyl_peroxide") to ConflictStatus.BETTER_SEPARATED,
            Pair("benzoyl_peroxide", "salicylic_acid") to ConflictStatus.USE_WITH_CARE,
            Pair("vitamin_c", "glycolic_acid") to ConflictStatus.USE_WITH_CARE,
            Pair("vitamin_c", "niacinamide") to ConflictStatus.FINE_TOGETHER,
            Pair("copper_peptides", "retinol") to ConflictStatus.USE_WITH_CARE,
            Pair("copper_peptides", "vitamin_c") to ConflictStatus.BETTER_SEPARATED,
            Pair("azelaic_acid", "niacinamide") to ConflictStatus.FINE_TOGETHER,
            Pair("glycolic_acid", "salicylic_acid") to ConflictStatus.USE_WITH_CARE,
            Pair("bakuchiol", "hyaluronic_acid") to ConflictStatus.FINE_TOGETHER,
            Pair("hyaluronic_acid", "ceramides") to ConflictStatus.FINE_TOGETHER,
            Pair("benzoyl_peroxide", "tretinoin") to ConflictStatus.BETTER_SEPARATED
        )

        assertTrue("Must test at least 12 pairs", pairsToTest.size >= 12)

        pairsToTest.forEach { (pair, expectedStatus) ->
            val results = SkinIngredientEngine.checkConflicts(
                selectedIds = listOf(pair.first, pair.second),
                allIngredients = sampleIngredients,
                rules = sampleRules,
                isTeen = false
            )

            assertEquals("Should find 1 conflict result for pair ${pair.first} + ${pair.second}", 1, results.size)
            assertEquals("Status mismatch for ${pair.first} + ${pair.second}", expectedStatus, results.first().status)
        }
    }

    @Test
    fun testRoutineStepOrderingRules() {
        val selected = listOf("vitamin_c", "retinol", "hyaluronic_acid", "ceramides")
        val routine = SkinIngredientEngine.buildRoutine(
            skinType = "Combination",
            concerns = listOf("Acne"),
            selectedIds = selected,
            allIngredients = sampleIngredients,
            isTeen = false
        )

        // AM Steps: Cleanse (1), Treat (2), Moisturise (3), Sunscreen (4)
        assertEquals(4, routine.amSteps.size)
        assertEquals("Cleanse", routine.amSteps[0].stepType)
        assertEquals("Treat", routine.amSteps[1].stepType)
        assertEquals("Moisturise", routine.amSteps[2].stepType)
        assertEquals("Sunscreen", routine.amSteps[3].stepType)

        // PM Steps: Cleanse (1), Treat (2), Moisturise (3)
        assertEquals(3, routine.pmSteps.size)
        assertEquals("Cleanse", routine.pmSteps[0].stepType)
        assertEquals("Treat", routine.pmSteps[1].stepType)
        assertEquals("Moisturise", routine.pmSteps[2].stepType)
    }

    @Test
    fun testStartSlowScheduleGeneration() {
        val selected = listOf("retinol", "glycolic_acid", "salicylic_acid")
        val routine = SkinIngredientEngine.buildRoutine(
            skinType = "Oily",
            concerns = listOf("Acne"),
            selectedIds = selected,
            allIngredients = sampleIngredients,
            isTeen = false
        )

        val phases = routine.startSlowPhases
        assertEquals(3, phases.size)
        assertEquals("Weeks 1–2", phases[0].weeks)
        assertEquals("Weeks 3–4", phases[1].weeks)
        assertEquals("Weeks 5–6", phases[2].weeks)
    }

    @Test
    fun testTeenFiltering() {
        val selected = listOf("retinol", "glycolic_acid", "hyaluronic_acid", "ceramides")
        val routineTeen = SkinIngredientEngine.buildRoutine(
            skinType = "Sensitive",
            concerns = listOf("Barrier"),
            selectedIds = selected,
            allIngredients = sampleIngredients,
            isTeen = true
        )

        // Retinol and Glycolic Acid should be excluded for teen mode
        val activeCountTeen = routineTeen.activeCount
        assertEquals(0, activeCountTeen)
        assertNull(routineTeen.activeWarning)
    }

    @Test
    fun testActiveWarningTriggering() {
        val selected = listOf("retinol", "glycolic_acid", "salicylic_acid")
        val routine = SkinIngredientEngine.buildRoutine(
            skinType = "Normal",
            concerns = listOf("Texture"),
            selectedIds = selected,
            allIngredients = sampleIngredients,
            isTeen = false
        )

        assertEquals(3, routine.activeCount)
        assertNotNull(routine.activeWarning)
        assertTrue(routine.activeWarning!!.contains("Caution"))
    }

    @Test
    fun testSunscreenGuidance() {
        val outdoorHigh = SkinIngredientEngine.calculateSunGuidance(isOutdoors = true, uvLevel = "High")
        val indoorHigh = SkinIngredientEngine.calculateSunGuidance(isOutdoors = false, uvLevel = "High")

        assertTrue(outdoorHigh.reapplyInterval.contains("2 hours"))
        assertTrue(indoorHigh.reapplyInterval.contains("before heading outside"))
    }
}
