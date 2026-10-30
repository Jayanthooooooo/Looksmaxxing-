package com.example.face.measure

import com.example.domain.model.RangePosition

object RangePositionMapper {

    val CITATION = "Farkas, L. G., et al. (1985). Anthropometric facial proportions in healthy North American Caucasians."

    val DEFAULT_BANDS = mapOf(
        "vertical_thirds_upper" to ReferenceBand(
            id = "vertical_thirds_upper",
            name = "Upper Third (Forehead)",
            description = "Hairline (trichion) to brow line (glabella) as a percentage of total facial height.",
            unit = "%",
            canonicalValue = 33.3f,
            typicalMin = 30.0f,
            typicalMax = 36.6f,
            noiseFloor = 1.5f
        ),
        "vertical_thirds_middle" to ReferenceBand(
            id = "vertical_thirds_middle",
            name = "Middle Third (Midface)",
            description = "Glabella to subnasale as a percentage of total facial height.",
            unit = "%",
            canonicalValue = 33.3f,
            typicalMin = 30.0f,
            typicalMax = 36.6f,
            noiseFloor = 1.5f
        ),
        "vertical_thirds_lower" to ReferenceBand(
            id = "vertical_thirds_lower",
            name = "Lower Third (Lower Face)",
            description = "Subnasale to menton (chin tip) as a percentage of total facial height.",
            unit = "%",
            canonicalValue = 33.3f,
            typicalMin = 30.0f,
            typicalMax = 36.6f,
            noiseFloor = 1.5f
        ),
        "horizontal_fifths_intercanthal" to ReferenceBand(
            id = "horizontal_fifths_intercanthal",
            name = "Intercanthal Width (Fifth)",
            description = "Inner eye corner distance as a proportion of total bizygomatic face width.",
            unit = "%",
            canonicalValue = 20.0f,
            typicalMin = 17.5f,
            typicalMax = 22.5f,
            noiseFloor = 1.2f
        ),
        "facial_index" to ReferenceBand(
            id = "facial_index",
            name = "Facial Index (Morphology)",
            description = "Total facial height to bizygomatic width ratio (Nasion-Menton / Zygion-Zygion * 100).",
            unit = "ratio",
            canonicalValue = 86.0f,
            typicalMin = 82.0f,
            typicalMax = 90.0f,
            noiseFloor = 1.8f
        ),
        "nasal_index" to ReferenceBand(
            id = "nasal_index",
            name = "Nasal Index",
            description = "Nasal width to nasal height ratio (Alare-Alare / Nasion-Subnasale * 100).",
            unit = "ratio",
            canonicalValue = 72.0f,
            typicalMin = 65.0f,
            typicalMax = 80.0f,
            noiseFloor = 2.0f
        ),
        "mouth_to_interpupillary_ratio" to ReferenceBand(
            id = "mouth_to_interpupillary_ratio",
            name = "Mouth-to-Eye Proportion",
            description = "Cheilion distance divided by interpupillary distance.",
            unit = "ratio",
            canonicalValue = 1.00f,
            typicalMin = 0.92f,
            typicalMax = 1.08f,
            noiseFloor = 0.03f
        ),
        "facial_symmetry_index" to ReferenceBand(
            id = "facial_symmetry_index",
            name = "Facial Symmetry Index",
            description = "Bilateral balance score across paired landmarks relative to mid-sagittal plane.",
            unit = "/100",
            canonicalValue = 100.0f,
            typicalMin = 92.0f,
            typicalMax = 99.0f,
            noiseFloor = 1.5f
        ),
        "jaw_angle" to ReferenceBand(
            id = "jaw_angle",
            name = "Gonial Jaw Angle",
            description = "Mandibular angle between ramus and mandibular body.",
            unit = "°",
            canonicalValue = 122.0f,
            typicalMin = 115.0f,
            typicalMax = 130.0f,
            noiseFloor = 2.0f
        ),
        "lower_third_split" to ReferenceBand(
            id = "lower_third_split",
            name = "Lower Third Sub-ratio",
            description = "Upper lip height (Subnasale-Stomion) to chin height (Stomion-Menton). Canonical ratio is 1:2 (0.50).",
            unit = "ratio",
            canonicalValue = 0.50f,
            typicalMin = 0.42f,
            typicalMax = 0.58f,
            noiseFloor = 0.03f
        )
    )

    fun evaluate(metricId: String, value: Float): BandPosition {
        val band = DEFAULT_BANDS[metricId] ?: return BandPosition(
            category = BandCategory.TYPICAL,
            rangePosition = RangePosition.DESCRIPTIVE,
            relativePosition = 0.5f,
            displayLabel = "Descriptive measurement",
            citation = CITATION
        )

        val margin = (band.typicalMax - band.typicalMin) * 0.25f
        val category = when {
            value < band.typicalMin -> BandCategory.BELOW_TYPICAL
            value > band.typicalMax -> BandCategory.ABOVE_TYPICAL
            else -> BandCategory.TYPICAL
        }

        val rangePosition = when {
            value in band.typicalMin..band.typicalMax -> RangePosition.WITHIN
            value in (band.typicalMin - margin)..(band.typicalMax + margin) -> RangePosition.NEAR
            else -> RangePosition.OUTSIDE
        }

        val span = (band.typicalMax - band.typicalMin).coerceAtLeast(0.001f)
        val relative = ((value - band.typicalMin) / span).coerceIn(0f, 1f)

        val label = when (category) {
            BandCategory.TYPICAL -> "Within typical anthropometric band (${band.typicalMin} - ${band.typicalMax}${band.unit})"
            BandCategory.BELOW_TYPICAL -> "Below typical reference range (${band.typicalMin} - ${band.typicalMax}${band.unit})"
            BandCategory.ABOVE_TYPICAL -> "Above typical reference range (${band.typicalMin} - ${band.typicalMax}${band.unit})"
        }

        return BandPosition(
            category = category,
            rangePosition = rangePosition,
            relativePosition = relative,
            displayLabel = label,
            citation = CITATION
        )
    }
}
