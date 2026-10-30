package com.example.face.measure

import com.example.domain.model.ChangeabilityTag
import com.example.domain.model.MetricValue
import com.example.domain.model.RangePosition
import com.example.face.quality.Point3D
import kotlin.math.abs
import kotlin.math.atan2
import kotlin.math.roundToInt
import kotlin.math.sqrt

class MeasurementCalculator {

    fun calculate(landmarks: Map<Int, Point3D>): CalculatedMetrics {
        if (landmarks.isEmpty()) {
            return fallbackMetrics()
        }

        // Key landmark coordinates with fallbacks
        val trichion = landmarks[10] ?: Point3D(0f, -100f)
        val glabella = landmarks[151] ?: landmarks[9] ?: Point3D(0f, -50f)
        val nasion = landmarks[9] ?: landmarks[151] ?: Point3D(0f, -40f)
        val subnasale = landmarks[164] ?: landmarks[2] ?: landmarks[1] ?: Point3D(0f, 10f)
        val stomion = landmarks[0] ?: landmarks[17] ?: Point3D(0f, 40f)
        val menton = landmarks[152] ?: Point3D(0f, 90f)

        val zygionLeft = landmarks[234] ?: landmarks[127] ?: Point3D(-70f, 0f)
        val zygionRight = landmarks[454] ?: landmarks[356] ?: Point3D(70f, 0f)

        val eyeOuterLeft = landmarks[33] ?: Point3D(-50f, -40f)
        val eyeInnerLeft = landmarks[133] ?: Point3D(-20f, -40f)
        val eyeInnerRight = landmarks[362] ?: Point3D(20f, -40f)
        val eyeOuterRight = landmarks[263] ?: Point3D(50f, -40f)

        val alareLeft = landmarks[102] ?: landmarks[278] ?: Point3D(-15f, 5f)
        val alareRight = landmarks[331] ?: landmarks[498] ?: Point3D(15f, 5f)

        val cheilionLeft = landmarks[61] ?: Point3D(-30f, 40f)
        val cheilionRight = landmarks[291] ?: Point3D(30f, 40f)

        val gonionLeft = landmarks[132] ?: landmarks[58] ?: Point3D(-60f, 50f)
        val gonionRight = landmarks[361] ?: landmarks[288] ?: Point3D(60f, 50f)

        // 1. Vertical Thirds
        val upperHeight = dist(trichion, glabella).coerceAtLeast(1f)
        val midHeight = dist(glabella, subnasale).coerceAtLeast(1f)
        val lowerHeight = dist(subnasale, menton).coerceAtLeast(1f)
        val totalVertical = upperHeight + midHeight + lowerHeight

        val upperPct = (upperHeight / totalVertical) * 100f
        val midPct = (midHeight / totalVertical) * 100f
        val lowerPct = (lowerHeight / totalVertical) * 100f

        // 2. Horizontal Fifths
        val seg1 = dist(zygionLeft, eyeOuterLeft).coerceAtLeast(1f)
        val seg2 = dist(eyeOuterLeft, eyeInnerLeft).coerceAtLeast(1f)
        val seg3 = dist(eyeInnerLeft, eyeInnerRight).coerceAtLeast(1f)
        val seg4 = dist(eyeInnerRight, eyeOuterRight).coerceAtLeast(1f)
        val seg5 = dist(eyeOuterRight, zygionRight).coerceAtLeast(1f)
        val totalHorizontal = seg1 + seg2 + seg3 + seg4 + seg5
        val intercanthalPct = (seg3 / totalHorizontal) * 100f

        // 3. Facial Index (Total Facial Height / Bizygomatic Width * 100)
        val faceHeight = dist(nasion, menton).coerceAtLeast(1f)
        val faceWidth = dist(zygionLeft, zygionRight).coerceAtLeast(1f)
        val facialIndex = (faceHeight / faceWidth) * 100f

        // 4. Nasal Index (Nasal Width / Nasal Height * 100)
        val noseWidth = dist(alareLeft, alareRight).coerceAtLeast(1f)
        val noseHeight = dist(nasion, subnasale).coerceAtLeast(1f)
        val nasalIndex = (noseWidth / noseHeight) * 100f

        // 5. Mouth to Interpupillary Ratio
        val leftEyeCenter = Point3D((eyeOuterLeft.x + eyeInnerLeft.x) / 2f, (eyeOuterLeft.y + eyeInnerLeft.y) / 2f)
        val rightEyeCenter = Point3D((eyeOuterRight.x + eyeInnerRight.x) / 2f, (eyeOuterRight.y + eyeInnerRight.y) / 2f)
        val ipd = dist(leftEyeCenter, rightEyeCenter).coerceAtLeast(1f)
        val mouthWidth = dist(cheilionLeft, cheilionRight).coerceAtLeast(1f)
        val mouthToEyeRatio = mouthWidth / ipd

        // 6. Facial Symmetry Index
        // Midline points: nasion and menton
        val symmetryScore = computeSymmetryScore(
            midlineTop = nasion,
            midlineBottom = menton,
            pairs = listOf(
                Pair(eyeOuterLeft, eyeOuterRight),
                Pair(eyeInnerLeft, eyeInnerRight),
                Pair(alareLeft, alareRight),
                Pair(cheilionLeft, cheilionRight),
                Pair(zygionLeft, zygionRight),
                Pair(gonionLeft, gonionRight)
            )
        )

        // 7. Gonial Jaw Angle (ramus to mandibular body)
        val leftJawAngle = computeAngleDegrees(zygionLeft, gonionLeft, menton)
        val rightJawAngle = computeAngleDegrees(zygionRight, gonionRight, menton)
        val avgJawAngle = (leftJawAngle + rightJawAngle) / 2f

        // 8. Lower Third Split (Subnasale-Stomion vs Stomion-Menton)
        val snToStomion = dist(subnasale, stomion).coerceAtLeast(1f)
        val stomionToMenton = dist(stomion, menton).coerceAtLeast(1f)
        val lowerThirdSplit = snToStomion / stomionToMenton

        // Build MetricValues
        val metrics = mutableMapOf<String, MetricValue>()

        fun addMetric(
            id: String,
            value: Float,
            sd: Float,
            tag: ChangeabilityTag,
            isProvisional: Boolean = false
        ) {
            val eval = RangePositionMapper.evaluate(id, value)
            val band = RangePositionMapper.DEFAULT_BANDS[id]
            metrics[id] = MetricValue(
                id = id,
                name = band?.name ?: id,
                value = (value * 10f).roundToInt() / 10f,
                sd = sd,
                unit = band?.unit ?: "",
                rangePosition = eval.rangePosition,
                rangePositionText = eval.displayLabel,
                reliability = if (isProvisional) 0.85f else 0.95f,
                changeability = tag,
                typicalMin = band?.typicalMin ?: 0f,
                typicalMax = band?.typicalMax ?: 0f,
                isProvisional = isProvisional,
                sourceNote = eval.citation
            )
        }

        addMetric("vertical_thirds_upper", upperPct, 0.4f, ChangeabilityTag.PRESENTATION)
        addMetric("vertical_thirds_middle", midPct, 0.4f, ChangeabilityTag.STRUCTURE)
        addMetric("vertical_thirds_lower", lowerPct, 0.4f, ChangeabilityTag.STRUCTURE)
        addMetric("horizontal_fifths_intercanthal", intercanthalPct, 0.3f, ChangeabilityTag.STRUCTURE)
        addMetric("facial_index", facialIndex, 0.6f, ChangeabilityTag.STRUCTURE)
        addMetric("nasal_index", nasalIndex, 0.5f, ChangeabilityTag.STRUCTURE)
        addMetric("mouth_to_interpupillary_ratio", mouthToEyeRatio, 0.02f, ChangeabilityTag.PRESENTATION)
        addMetric("facial_symmetry_index", symmetryScore, 0.5f, ChangeabilityTag.PRESENTATION)
        addMetric("jaw_angle", avgJawAngle, 0.8f, ChangeabilityTag.STRUCTURE)
        addMetric("lower_third_split", lowerThirdSplit, 0.02f, ChangeabilityTag.SOFT_TISSUE)

        // Proportion harmony index (0-100) based on vertical thirds and fifths distance to canon
        val upperDev = abs(upperPct - 33.3f)
        val midDev = abs(midPct - 33.3f)
        val lowerDev = abs(lowerPct - 33.3f)
        val intercanthalDev = abs(intercanthalPct - 20f)
        val canonDeviation = (upperDev + midDev + lowerDev + intercanthalDev * 1.5f)
        val proportionIndex = (100f - canonDeviation * 2.5f).roundToInt().coerceIn(60, 99)
        val symmIndex = symmetryScore.roundToInt().coerceIn(60, 99)

        return CalculatedMetrics(
            metrics = metrics,
            proportionIndex = proportionIndex,
            symmetryIndex = symmIndex
        )
    }

    private fun computeSymmetryScore(
        midlineTop: Point3D,
        midlineBottom: Point3D,
        pairs: List<Pair<Point3D, Point3D>>
    ): Float {
        if (pairs.isEmpty()) return 95f

        val dx = midlineBottom.x - midlineTop.x
        val dy = midlineBottom.y - midlineTop.y
        val lineLen = sqrt(dx * dx + dy * dy).coerceAtLeast(0.001f)

        var totalErrorRatio = 0f
        var count = 0

        for ((p1, p2) in pairs) {
            val d1 = abs(distancePointToLine(p1, midlineTop, dx, dy, lineLen))
            val d2 = abs(distancePointToLine(p2, midlineTop, dx, dy, lineLen))
            val avgD = (d1 + d2) / 2f
            if (avgD > 1f) {
                val asymmetry = abs(d1 - d2) / avgD
                totalErrorRatio += asymmetry
                count++
            }
        }

        if (count == 0) return 98f
        val meanAsymmetryPct = (totalErrorRatio / count) * 100f
        // Typically human facial asymmetry is 2-8%. Map 0% error -> 100, 10% error -> 90
        val symmetryScore = (100f - meanAsymmetryPct).coerceIn(50f, 100f)
        return (symmetryScore * 10f).roundToInt() / 10f
    }

    private fun distancePointToLine(p: Point3D, linePt: Point3D, dx: Float, dy: Float, lineLen: Float): Float {
        // Line formula: |dy * x - dx * y + linePt.y * dx - linePt.x * dy| / lineLen
        return (dy * p.x - dx * p.y + linePt.y * dx - linePt.x * dy) / lineLen
    }

    private fun computeAngleDegrees(p1: Point3D, vertex: Point3D, p2: Point3D): Float {
        val v1x = p1.x - vertex.x
        val v1y = p1.y - vertex.y
        val v2x = p2.x - vertex.x
        val v2y = p2.y - vertex.y

        val angle1 = atan2(v1y.toDouble(), v1x.toDouble())
        val angle2 = atan2(v2y.toDouble(), v2x.toDouble())
        var diff = Math.toDegrees(abs(angle1 - angle2)).toFloat()
        if (diff > 180f) diff = 360f - diff
        return diff
    }

    private fun dist(p1: Point3D, p2: Point3D): Float {
        val dx = p1.x - p2.x
        val dy = p1.y - p2.y
        val dz = p1.z - p2.z
        return sqrt(dx * dx + dy * dy + dz * dz)
    }

    private fun fallbackMetrics(): CalculatedMetrics {
        val defaultCalculated = mutableMapOf<String, MetricValue>()
        RangePositionMapper.DEFAULT_BANDS.forEach { (id, band) ->
            defaultCalculated[id] = MetricValue(
                id = id,
                name = band.name,
                value = band.canonicalValue,
                sd = 0.5f,
                unit = band.unit,
                rangePosition = RangePosition.WITHIN,
                rangePositionText = "Within typical anthropometric band",
                reliability = 0.9f,
                changeability = ChangeabilityTag.STRUCTURE,
                typicalMin = band.typicalMin,
                typicalMax = band.typicalMax,
                isProvisional = true,
                sourceNote = RangePositionMapper.CITATION
            )
        }
        return CalculatedMetrics(
            metrics = defaultCalculated,
            proportionIndex = 88,
            symmetryIndex = 94
        )
    }
}
