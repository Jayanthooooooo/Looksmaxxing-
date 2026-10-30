package com.example.face.measure

import kotlin.math.abs

object NoiseFloorEvaluator {

    /**
     * Checks if a measured delta between two scans exceeds the measurement noise floor.
     * Prevents false impressions of physical changes caused by micro-rotation, lighting, or sensor jitter.
     */
    fun evaluate(
        metricId: String,
        baselineValue: Float,
        currentValue: Float,
        userCustomSd: Float? = null
    ): NoiseFloorCheck {
        val band = RangePositionMapper.DEFAULT_BANDS[metricId]
        val name = band?.name ?: metricId
        val unit = band?.unit ?: ""
        
        // If user has historical scan variance (e.g. from repeated burst scans), use max of standard band noise and 2*SD
        val noiseFloor = if (userCustomSd != null && userCustomSd > 0f) {
            maxOf(band?.noiseFloor ?: 1.5f, userCustomSd * 2f)
        } else {
            band?.noiseFloor ?: 1.5f
        }

        val delta = currentValue - baselineValue
        val isSignificant = abs(delta) >= noiseFloor

        val description = if (!isSignificant) {
            "Change of ${String.format("%.1f", delta)}$unit is within the measurement noise floor (±${String.format("%.1f", noiseFloor)}$unit). Photo variations do not indicate physical structural change."
        } else {
            val direction = if (delta > 0) "increase" else "decrease"
            "Statistically significant photographic $direction of ${String.format("%.1f", abs(delta))}$unit (exceeds noise floor of ±${String.format("%.1f", noiseFloor)}$unit)."
        }

        return NoiseFloorCheck(
            metricId = metricId,
            metricName = name,
            delta = delta,
            noiseFloor = noiseFloor,
            isSignificant = isSignificant,
            description = description
        )
    }

    /**
     * Evaluates all common metrics between two scans.
     */
    fun evaluateAll(
        baselineMetrics: Map<String, Float>,
        currentMetrics: Map<String, Float>,
        noiseEstimates: Map<String, Float> = emptyMap()
    ): List<NoiseFloorCheck> {
        val results = mutableListOf<NoiseFloorCheck>()
        baselineMetrics.forEach { (metricId, baselineVal) ->
            currentMetrics[metricId]?.let { currentVal ->
                val customSd = noiseEstimates[metricId]
                results.add(evaluate(metricId, baselineVal, currentVal, customSd))
            }
        }
        return results
    }
}
