package com.example.face.measure

import com.example.domain.model.RangePosition

object RangePositionMapper {

    val CITATION = "Farkas, L. G., et al. (1985). Anthropometric facial proportions; Porras et al. (2017)."

    val DEFAULT_BANDS = mapOf(
        "vertical_thirds_upper" to ReferenceBand(
            id = "vertical_thirds_upper",
            name = "Upper Third (Forehead)",
            description = "Estimated hairline to glabella as percentage of facial height.",
            unit = "%",
            canonicalValue = 33.3f,
            typicalMin = 30.0f,
            typicalMax = 36.6f,
            noiseFloor = 1.5f,
            ideal = 33.3f,
            tolerance = 3.5f
        ),
        "vertical_thirds_middle" to ReferenceBand(
            id = "vertical_thirds_middle",
            name = "Middle Third (Midface)",
            description = "Glabella to subnasale as percentage of facial height.",
            unit = "%",
            canonicalValue = 33.3f,
            typicalMin = 30.0f,
            typicalMax = 36.6f,
            noiseFloor = 1.5f,
            ideal = 33.3f,
            tolerance = 3.5f
        ),
        "vertical_thirds_lower" to ReferenceBand(
            id = "vertical_thirds_lower",
            name = "Lower Third (Lower Face)",
            description = "Subnasale to menton as percentage of facial height.",
            unit = "%",
            canonicalValue = 33.3f,
            typicalMin = 30.0f,
            typicalMax = 36.6f,
            noiseFloor = 1.5f,
            ideal = 33.3f,
            tolerance = 3.5f
        ),
        "horizontal_fifths_intercanthal" to ReferenceBand(
            id = "horizontal_fifths_intercanthal",
            name = "Intercanthal Width",
            description = "Inner eye corner distance as proportion of bizygomatic width.",
            unit = "%",
            canonicalValue = 20.0f,
            typicalMin = 17.5f,
            typicalMax = 22.5f,
            noiseFloor = 1.2f,
            ideal = 20.0f,
            tolerance = 2.5f
        ),
        "facial_index" to ReferenceBand(
            id = "facial_index",
            name = "Facial Index",
            description = "Total facial height to bizygomatic width ratio.",
            unit = "ratio",
            canonicalValue = 86.0f,
            typicalMin = 82.0f,
            typicalMax = 90.0f,
            noiseFloor = 1.8f,
            ideal = 86.0f,
            tolerance = 4.0f
        ),
        "nasal_index" to ReferenceBand(
            id = "nasal_index",
            name = "Nasal Index",
            description = "Nasal width to nasal height ratio.",
            unit = "ratio",
            canonicalValue = 72.0f,
            typicalMin = 65.0f,
            typicalMax = 80.0f,
            noiseFloor = 2.0f,
            ideal = 72.0f,
            tolerance = 6.0f
        ),
        "mouth_to_interpupillary_ratio" to ReferenceBand(
            id = "mouth_to_interpupillary_ratio",
            name = "Mouth-to-Eye Proportion",
            description = "Cheilion distance divided by interpupillary distance.",
            unit = "ratio",
            canonicalValue = 1.00f,
            typicalMin = 0.92f,
            typicalMax = 1.08f,
            noiseFloor = 0.03f,
            ideal = 1.00f,
            tolerance = 0.08f
        ),
        "facial_symmetry_index" to ReferenceBand(
            id = "facial_symmetry_index",
            name = "Facial Symmetry Index",
            description = "Bilateral balance score across paired landmarks.",
            unit = "/100",
            canonicalValue = 100.0f,
            typicalMin = 92.0f,
            typicalMax = 99.0f,
            noiseFloor = 1.5f,
            ideal = 100.0f,
            tolerance = 5.0f
        ),
        "jaw_angle" to ReferenceBand(
            id = "jaw_angle",
            name = "Gonial Jaw Angle",
            description = "Mandibular angle between ramus and body.",
            unit = "°",
            canonicalValue = 122.0f,
            typicalMin = 115.0f,
            typicalMax = 130.0f,
            noiseFloor = 2.0f,
            ideal = 122.0f,
            tolerance = 7.0f
        ),
        "lower_third_split" to ReferenceBand(
            id = "lower_third_split",
            name = "Lower Third Sub-ratio",
            description = "Upper lip height to chin height.",
            unit = "ratio",
            canonicalValue = 0.50f,
            typicalMin = 0.42f,
            typicalMax = 0.58f,
            noiseFloor = 0.03f,
            ideal = 0.50f,
            tolerance = 0.08f
        ),
        "canthal_tilt" to ReferenceBand(
            id = "canthal_tilt",
            name = "Canthal Tilt",
            description = "Angle of line connecting inner and outer canthus relative to horizontal.",
            unit = "°",
            canonicalValue = 6.0f,
            typicalMin = 3.0f,
            typicalMax = 10.0f,
            noiseFloor = 1.0f,
            ideal = 6.0f,
            tolerance = 3.0f
        ),
        "eye_spacing" to ReferenceBand(
            id = "eye_spacing",
            name = "Eye Spacing Ratio",
            description = "Intercanthal width divided by eye fissure width.",
            unit = "ratio",
            canonicalValue = 1.0f,
            typicalMin = 0.9f,
            typicalMax = 1.1f,
            noiseFloor = 0.05f,
            ideal = 1.0f,
            tolerance = 0.10f
        ),
        "eye_aspect_ratio" to ReferenceBand(
            id = "eye_aspect_ratio",
            name = "Eye Aspect Ratio",
            description = "Vertical height to horizontal width of eye aperture.",
            unit = "ratio",
            canonicalValue = 0.30f,
            typicalMin = 0.22f,
            typicalMax = 0.38f,
            noiseFloor = 0.03f,
            ideal = 0.30f,
            tolerance = 0.05f
        ),
        "brow_to_eye_distance" to ReferenceBand(
            id = "brow_to_eye_distance",
            name = "Brow-to-Eye Distance",
            description = "Vertical distance from brow to upper eyelid relative to eye height.",
            unit = "ratio",
            canonicalValue = 0.22f,
            typicalMin = 0.16f,
            typicalMax = 0.28f,
            noiseFloor = 0.04f,
            ideal = 0.22f,
            tolerance = 0.04f
        ),
        "lip_ratio" to ReferenceBand(
            id = "lip_ratio",
            name = "Lip Height Ratio",
            description = "Upper lip vermillion height to lower lip height.",
            unit = "ratio",
            canonicalValue = 0.60f,
            typicalMin = 0.45f,
            typicalMax = 0.75f,
            noiseFloor = 0.05f,
            ideal = 0.60f,
            tolerance = 0.10f
        ),
        "chin_to_philtrum_ratio" to ReferenceBand(
            id = "chin_to_philtrum_ratio",
            name = "Chin-to-Philtrum Ratio",
            description = "Philtrum length relative to chin height.",
            unit = "ratio",
            canonicalValue = 0.50f,
            typicalMin = 0.38f,
            typicalMax = 0.62f,
            noiseFloor = 0.04f,
            ideal = 0.50f,
            tolerance = 0.08f
        ),
        "jaw_to_cheekbone_ratio" to ReferenceBand(
            id = "jaw_to_cheekbone_ratio",
            name = "Jaw-to-Cheekbone Width",
            description = "Gonial width divided by bizygomatic width.",
            unit = "ratio",
            canonicalValue = 0.80f,
            typicalMin = 0.72f,
            typicalMax = 0.88f,
            noiseFloor = 0.03f,
            ideal = 0.80f,
            tolerance = 0.08f
        ),
        "nose_to_intercanthal_ratio" to ReferenceBand(
            id = "nose_to_intercanthal_ratio",
            name = "Nose-to-Intercanthal Width",
            description = "Nasal alar width divided by intercanthal distance.",
            unit = "ratio",
            canonicalValue = 1.00f,
            typicalMin = 0.88f,
            typicalMax = 1.12f,
            noiseFloor = 0.04f,
            ideal = 1.00f,
            tolerance = 0.10f
        ),
        "face_wh_ratio" to ReferenceBand(
            id = "face_wh_ratio",
            name = "Face Width-to-Height Ratio",
            description = "Bizygomatic width divided by upper facial height.",
            unit = "ratio",
            canonicalValue = 1.95f,
            typicalMin = 1.75f,
            typicalMax = 2.15f,
            noiseFloor = 0.06f,
            ideal = 1.95f,
            tolerance = 0.15f
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
