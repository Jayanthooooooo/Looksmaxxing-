package com.example.skin

import com.example.domain.model.SkinAnalysisResult
import com.example.domain.model.SkinDescriptor
import kotlin.math.roundToInt

class SkinScoreEngine {

    fun computeResult(
        stats: SkinStats,
        geminiDescriptors: Map<String, SkinDescriptor>? = null,
        needsClinicianFlag: Boolean = false,
        clinicianNote: String? = null
    ): SkinAnalysisResult {
        // Deterministic baseline clarity score:
        // 40% weight to tone evenness, 30% to low redness, 30% to surface smoothness
        val baseScore = (
            0.40f * stats.evennessIndex +
            0.30f * (100f - stats.rednessIndex) +
            0.30f * stats.smoothnessIndex
        )

        // Bounded adjustment from Gemini descriptors (max ±5 points)
        var adjustment = 0f
        if (geminiDescriptors != null) {
            geminiDescriptors.forEach { (_, desc) ->
                when (desc.severity.lowercase()) {
                    "none" -> adjustment += 1.0f
                    "mild" -> adjustment += 0.0f
                    "moderate" -> adjustment -= 1.0f
                    "marked" -> adjustment -= 2.0f
                }
            }
        }
        val boundedAdjustment = adjustment.coerceIn(-5f, 5f)
        val finalClarityIndex = (baseScore + boundedAdjustment).roundToInt().coerceIn(40, 98)

        // Build descriptors (use Gemini if available, else derive from quantitative stats)
        val descriptors = mutableMapOf<String, SkinDescriptor>()

        if (geminiDescriptors != null && geminiDescriptors.isNotEmpty()) {
            descriptors.putAll(geminiDescriptors)
        } else {
            // Local fallback descriptors derived purely from quantitative ROI stats
            val evennessSeverity = when {
                stats.evennessIndex >= 85f -> "none"
                stats.evennessIndex >= 72f -> "mild"
                stats.evennessIndex >= 60f -> "moderate"
                else -> "marked"
            }
            descriptors["Tone Evenness"] = SkinDescriptor(evennessSeverity, "high")

            val rednessSeverity = when {
                stats.rednessIndex < 15f -> "none"
                stats.rednessIndex < 35f -> "mild"
                stats.rednessIndex < 55f -> "moderate"
                else -> "marked"
            }
            descriptors["Cheek Redness Proxy"] = SkinDescriptor(rednessSeverity, "high")

            val textureSeverity = when {
                stats.smoothnessIndex >= 85f -> "none"
                stats.smoothnessIndex >= 72f -> "mild"
                stats.smoothnessIndex >= 60f -> "moderate"
                else -> "marked"
            }
            descriptors["Surface Texture Proxy"] = SkinDescriptor(textureSeverity, "medium")

            val undereyeSeverity = when {
                stats.undereyeContrast < 4f -> "none"
                stats.undereyeContrast < 8f -> "mild"
                stats.undereyeContrast < 13f -> "moderate"
                else -> "marked"
            }
            descriptors["Under-eye Contrast"] = SkinDescriptor(undereyeSeverity, "medium")
        }

        val components = mapOf(
            "evenness" to stats.evennessIndex,
            "redness_index" to stats.rednessIndex,
            "smoothness" to stats.smoothnessIndex,
            "undereye_contrast" to stats.undereyeContrast
        )

        val notes = listOf(
            "Photographic reading derived from optical CIE L*a*b* color distributions.",
            "Changes in lighting, time of day, or room temperature can influence optical readings."
        )

        return SkinAnalysisResult(
            clarityIndex = finalClarityIndex,
            descriptors = descriptors,
            components = components,
            needsClinician = needsClinicianFlag,
            clinicianReason = clinicianNote,
            notes = notes
        )
    }
}
