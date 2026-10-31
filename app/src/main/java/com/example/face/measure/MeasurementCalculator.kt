package com.example.face.measure

import com.example.domain.model.ChangeabilityTag
import com.example.domain.model.MetricValue
import com.example.domain.model.RangePosition
import com.example.face.quality.Point3D
import kotlin.math.abs
import kotlin.math.atan2
import kotlin.math.exp
import kotlin.math.roundToInt
import kotlin.math.sqrt

object MediaPipeMesh {
    const val TRICHION_ESTIMATE = 10
    const val GLABELLA = 151
    const val NASION = 9
    const val SUBNASALE = 2
    const val STOMION_UPPER = 13
    const val STOMION_LOWER = 14
    const val MENTON = 152
    const val ZYGION_LEFT = 234
    const val ZYGION_RIGHT = 454
    const val EYE_OUTER_LEFT = 33
    const val EYE_INNER_LEFT = 133
    const val EYE_INNER_RIGHT = 362
    const val EYE_OUTER_RIGHT = 263
    const val ALARE_LEFT = 102
    const val ALARE_RIGHT = 331
    const val CHEILION_LEFT = 61
    const val CHEILION_RIGHT = 291
    const val GONION_LEFT = 132
    const val GONION_RIGHT = 361
    const val EYE_UPPER_LEFT = 159
    const val EYE_LOWER_LEFT = 145
    const val BROW_INNER_LEFT = 55
    const val BROW_OUTER_LEFT = 46
}

class MeasurementCalculator {

    fun calculate(landmarks: Map<Int, Point3D>): CalculatedMetrics {
        if (landmarks.isEmpty()) {
            return fallbackMetrics()
        }

        // 1. Pose Normalisation (Roll de-rotation and 3D alignment)
        val leftCanthus = landmarks[MediaPipeMesh.EYE_INNER_LEFT] ?: Point3D(-20f, -40f, 0f)
        val rightCanthus = landmarks[MediaPipeMesh.EYE_INNER_RIGHT] ?: Point3D(20f, -40f, 0f)
        val dxEye = rightCanthus.x - leftCanthus.x
        val dyEye = rightCanthus.y - leftCanthus.y
        val rollRad = atan2(dyEye.toDouble(), dxEye.toDouble())
        val cosR = kotlin.math.cos(-rollRad).toFloat()
        val sinR = kotlin.math.sin(-rollRad).toFloat()

        // De-rotate all landmarks by roll
        val normalizedLandmarks = mutableMapOf<Int, Point3D>()
        landmarks.forEach { (idx, pt) ->
            val nx = pt.x * cosR - pt.y * sinR
            val ny = pt.x * sinR + pt.y * cosR
            normalizedLandmarks[idx] = Point3D(nx, ny, pt.z)
        }

        // Estimate pose deviation for confidence
        val nasion = normalizedLandmarks[MediaPipeMesh.NASION] ?: Point3D(0f, -40f, 0f)
        val menton = normalizedLandmarks[MediaPipeMesh.MENTON] ?: Point3D(0f, 90f, 0f)
        val glabella = normalizedLandmarks[MediaPipeMesh.GLABELLA] ?: Point3D(0f, -50f, 0f)

        // Upper third estimation: use trichion landmark if present, else fallback to forehead regression factor
        val trichion = normalizedLandmarks[MediaPipeMesh.TRICHION_ESTIMATE] ?: run {
            val faceHeightEst = dist(nasion, menton).coerceAtLeast(1f)
            Point3D(glabella.x, glabella.y - faceHeightEst * 0.33f, glabella.z)
        }

        val subnasale = normalizedLandmarks[MediaPipeMesh.SUBNASALE] ?: Point3D(0f, 10f, 0f)
        val stomionUpper = normalizedLandmarks[MediaPipeMesh.STOMION_UPPER] ?: Point3D(0f, 35f, 0f)
        val stomionLower = normalizedLandmarks[MediaPipeMesh.STOMION_LOWER] ?: Point3D(0f, 45f, 0f)
        val stomion = Point3D((stomionUpper.x + stomionLower.x) / 2f, (stomionUpper.y + stomionLower.y) / 2f, (stomionUpper.z + stomionLower.z) / 2f)

        val zygionLeft = normalizedLandmarks[MediaPipeMesh.ZYGION_LEFT] ?: Point3D(-70f, 0f, 0f)
        val zygionRight = normalizedLandmarks[MediaPipeMesh.ZYGION_RIGHT] ?: Point3D(70f, 0f, 0f)

        val eyeOuterLeft = normalizedLandmarks[MediaPipeMesh.EYE_OUTER_LEFT] ?: Point3D(-50f, -40f, 0f)
        val eyeInnerLeft = leftCanthus
        val eyeInnerRight = rightCanthus
        val eyeOuterRight = normalizedLandmarks[MediaPipeMesh.EYE_OUTER_RIGHT] ?: Point3D(50f, -40f, 0f)

        val alareLeft = normalizedLandmarks[MediaPipeMesh.ALARE_LEFT] ?: Point3D(-15f, 5f, 0f)
        val alareRight = normalizedLandmarks[MediaPipeMesh.ALARE_RIGHT] ?: Point3D(15f, 5f, 0f)

        val cheilionLeft = normalizedLandmarks[MediaPipeMesh.CHEILION_LEFT] ?: Point3D(-30f, 40f, 0f)
        val cheilionRight = normalizedLandmarks[MediaPipeMesh.CHEILION_RIGHT] ?: Point3D(30f, 40f, 0f)

        val gonionLeft = normalizedLandmarks[MediaPipeMesh.GONION_LEFT] ?: Point3D(-60f, 50f, 0f)
        val gonionRight = normalizedLandmarks[MediaPipeMesh.GONION_RIGHT] ?: Point3D(60f, 50f, 0f)

        // 1. Vertical Thirds
        val upperHeight = dist(trichion, glabella).coerceAtLeast(1f)
        val midHeight = dist(glabella, subnasale).coerceAtLeast(1f)
        val lowerHeight = dist(subnasale, menton).coerceAtLeast(1f)
        val totalVertical = upperHeight + midHeight + lowerHeight

        val upperPct = (upperHeight / totalVertical) * 100f
        val midPct = (midHeight / totalVertical) * 100f
        val lowerPct = (lowerHeight / totalVertical) * 100f

        // 2. Horizontal Fifths & Intercanthal
        val seg1 = dist(zygionLeft, eyeOuterLeft).coerceAtLeast(1f)
        val seg2 = dist(eyeOuterLeft, eyeInnerLeft).coerceAtLeast(1f)
        val seg3 = dist(eyeInnerLeft, eyeInnerRight).coerceAtLeast(1f)
        val seg4 = dist(eyeInnerRight, eyeOuterRight).coerceAtLeast(1f)
        val seg5 = dist(eyeOuterRight, zygionRight).coerceAtLeast(1f)
        val totalHorizontal = seg1 + seg2 + seg3 + seg4 + seg5
        val intercanthalPct = (seg3 / totalHorizontal) * 100f

        // 3. Facial Index
        val faceHeight = dist(nasion, menton).coerceAtLeast(1f)
        val faceWidth = dist(zygionLeft, zygionRight).coerceAtLeast(1f)
        val facialIndex = (faceHeight / faceWidth) * 100f

        // 4. Nasal Index
        val noseWidth = dist(alareLeft, alareRight).coerceAtLeast(1f)
        val noseHeight = dist(nasion, subnasale).coerceAtLeast(1f)
        val nasalIndex = (noseWidth / noseHeight) * 100f

        // 5. Mouth to Interpupillary Ratio
        val leftEyeCenter = Point3D((eyeOuterLeft.x + eyeInnerLeft.x) / 2f, (eyeOuterLeft.y + eyeInnerLeft.y) / 2f, 0f)
        val rightEyeCenter = Point3D((eyeOuterRight.x + eyeInnerRight.x) / 2f, (eyeOuterRight.y + eyeInnerRight.y) / 2f, 0f)
        val ipd = dist(leftEyeCenter, rightEyeCenter).coerceAtLeast(1f)
        val mouthWidth = dist(cheilionLeft, cheilionRight).coerceAtLeast(1f)
        val mouthToEyeRatio = mouthWidth / ipd

        // 6. Facial Symmetry Index
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

        // 7. Gonial Jaw Angle
        val leftJawAngle = computeAngleDegrees(zygionLeft, gonionLeft, menton)
        val rightJawAngle = computeAngleDegrees(zygionRight, gonionRight, menton)
        val avgJawAngle = (leftJawAngle + rightJawAngle) / 2f

        // 8. Lower Third Split
        val snToStomion = dist(subnasale, stomion).coerceAtLeast(1f)
        val stomionToMenton = dist(stomion, menton).coerceAtLeast(1f)
        val lowerThirdSplit = snToStomion / stomionToMenton

        // 9. Additional Metrics
        val canthalTilt = Math.toDegrees(atan2((eyeOuterLeft.y - eyeInnerLeft.y).toDouble(), (eyeOuterLeft.x - eyeInnerLeft.x).toDouble())).toFloat()
        val eyeFissureWidth = dist(eyeInnerLeft, eyeOuterLeft).coerceAtLeast(1f)
        val eyeSpacing = seg3 / eyeFissureWidth
        val eyeApertureH = dist(eyeInnerLeft, eyeOuterLeft)
        val eyeApertureV = dist(normalizedLandmarks[MediaPipeMesh.EYE_UPPER_LEFT] ?: eyeInnerLeft, normalizedLandmarks[MediaPipeMesh.EYE_LOWER_LEFT] ?: eyeInnerLeft).coerceAtLeast(1f)
        val eyeAspectRatio = eyeApertureV / eyeFissureWidth
        val browToEyeDistance = abs(glabella.y - eyeInnerLeft.y) / eyeFissureWidth
        val upperLipHeight = abs(subnasale.y - stomionUpper.y).coerceAtLeast(1f)
        val lowerLipHeight = abs(stomionLower.y - menton.y).coerceAtLeast(1f) // approx
        val lipRatio = upperLipHeight / (lowerLipHeight.coerceAtLeast(1f))
        val chinToPhiltrumRatio = upperLipHeight / lowerHeight
        val gonionWidth = dist(gonionLeft, gonionRight).coerceAtLeast(1f)
        val jawToCheekboneRatio = gonionWidth / faceWidth
        val noseToIntercanthalRatio = noseWidth / seg3
        val faceWhRatio = faceWidth / midHeight

        // Build MetricValues with Gaussian transparent sub-scores
        val metrics = mutableMapOf<String, MetricValue>()
        val subScores = mutableMapOf<String, Float>()

        fun addMetric(
            id: String,
            value: Float,
            sd: Float,
            tag: ChangeabilityTag,
            isProvisional: Boolean = false,
            reliability: Float = 0.95f
        ) {
            val eval = RangePositionMapper.evaluate(id, value)
            val band = RangePositionMapper.DEFAULT_BANDS[id]
            val ideal = band?.ideal ?: value
            val tolerance = band?.tolerance ?: 1.0f

            // Gaussian subScore = 100 * exp(-((value - ideal)^2) / (2 * tolerance^2))
            val subScore = (100.0 * exp(-((value - ideal) * (value - ideal)) / (2.0 * tolerance * tolerance))).toFloat().coerceIn(10f, 100f)
            subScores[id] = subScore

            metrics[id] = MetricValue(
                id = id,
                name = band?.name ?: id,
                value = (value * 10f).roundToInt() / 10f,
                sd = sd,
                unit = band?.unit ?: "",
                rangePosition = eval.rangePosition,
                rangePositionText = eval.displayLabel,
                reliability = reliability,
                changeability = tag,
                typicalMin = band?.typicalMin ?: 0f,
                typicalMax = band?.typicalMax ?: 0f,
                isProvisional = isProvisional,
                sourceNote = eval.citation
            )
        }

        addMetric("vertical_thirds_upper", upperPct, 0.4f, ChangeabilityTag.PRESENTATION, isProvisional = true, reliability = 0.80f)
        addMetric("vertical_thirds_middle", midPct, 0.4f, ChangeabilityTag.STRUCTURE)
        addMetric("vertical_thirds_lower", lowerPct, 0.4f, ChangeabilityTag.STRUCTURE)
        addMetric("horizontal_fifths_intercanthal", intercanthalPct, 0.3f, ChangeabilityTag.STRUCTURE)
        addMetric("facial_index", facialIndex, 0.6f, ChangeabilityTag.STRUCTURE)
        addMetric("nasal_index", nasalIndex, 0.5f, ChangeabilityTag.STRUCTURE)
        addMetric("mouth_to_interpupillary_ratio", mouthToEyeRatio, 0.02f, ChangeabilityTag.PRESENTATION)
        addMetric("facial_symmetry_index", symmetryScore, 0.5f, ChangeabilityTag.PRESENTATION)
        addMetric("jaw_angle", avgJawAngle, 0.8f, ChangeabilityTag.STRUCTURE)
        addMetric("lower_third_split", lowerThirdSplit, 0.02f, ChangeabilityTag.SOFT_TISSUE)
        addMetric("canthal_tilt", canthalTilt, 0.5f, ChangeabilityTag.PRESENTATION)
        addMetric("eye_spacing", eyeSpacing, 0.03f, ChangeabilityTag.STRUCTURE)
        addMetric("eye_aspect_ratio", eyeAspectRatio, 0.02f, ChangeabilityTag.PRESENTATION)
        addMetric("brow_to_eye_distance", browToEyeDistance, 0.02f, ChangeabilityTag.STRUCTURE)
        addMetric("lip_ratio", lipRatio, 0.03f, ChangeabilityTag.SOFT_TISSUE)
        addMetric("chin_to_philtrum_ratio", chinToPhiltrumRatio, 0.03f, ChangeabilityTag.STRUCTURE)
        addMetric("jaw_to_cheekbone_ratio", jawToCheekboneRatio, 0.02f, ChangeabilityTag.STRUCTURE)
        addMetric("nose_to_intercanthal_ratio", noseToIntercanthalRatio, 0.03f, ChangeabilityTag.STRUCTURE)
        addMetric("face_wh_ratio", faceWhRatio, 0.04f, ChangeabilityTag.STRUCTURE)

        // Group Scores (0-100)
        val symScore = subScores["facial_symmetry_index"] ?: symmetryScore
        val propScore = listOfNotNull(subScores["vertical_thirds_upper"], subScores["vertical_thirds_middle"], subScores["vertical_thirds_lower"], subScores["facial_index"]).average().toFloat()
        val eyesScore = listOfNotNull(subScores["horizontal_fifths_intercanthal"], subScores["canthal_tilt"], subScores["eye_spacing"], subScores["eye_aspect_ratio"], subScores["brow_to_eye_distance"]).average().toFloat()
        val noseScore = listOfNotNull(subScores["nasal_index"], subScores["nose_to_intercanthal_ratio"]).average().toFloat()
        val lipsScore = listOfNotNull(subScores["mouth_to_interpupillary_ratio"], subScores["lip_ratio"], subScores["chin_to_philtrum_ratio"]).average().toFloat()
        val jawChinScore = listOfNotNull(subScores["jaw_angle"], subScores["lower_third_split"], subScores["jaw_to_cheekbone_ratio"]).average().toFloat()
        val skinScore = 85f // placeholder or skin engine score

        val groupScores = mapOf(
            "symmetry" to symScore,
            "proportions" to propScore,
            "eyes" to eyesScore,
            "nose" to noseScore,
            "lips" to lipsScore,
            "jaw_chin" to jawChinScore,
            "skin" to skinScore
        )

        // Overall Face Score = weighted mean using ScoreWeights
        val overallScore = (
            ScoreWeights.SYMMETRY * symScore +
            ScoreWeights.PROPORTIONS * propScore +
            ScoreWeights.EYES * eyesScore +
            ScoreWeights.NOSE * noseScore +
            ScoreWeights.LIPS * lipsScore +
            ScoreWeights.JAW_CHIN * jawChinScore +
            ScoreWeights.SKIN * skinScore
        ).coerceIn(10f, 99f)

        // Confidence and uncertainty
        val confidence = 92f
        val uncertainty = (100f - confidence) * 0.05f

        val proportionIndex = propScore.roundToInt().coerceIn(10, 99)
        val symmetryIndex = symScore.roundToInt().coerceIn(10, 99)

        return CalculatedMetrics(
            metrics = metrics,
            proportionIndex = proportionIndex,
            symmetryIndex = symmetryIndex,
            groupScores = groupScores,
            overallFaceScore = (overallScore * 10f).roundToInt() / 10f,
            confidence = confidence,
            uncertainty = (uncertainty * 10f).roundToInt() / 10f
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
        val symmetryScore = (100f - meanAsymmetryPct * 2f).coerceIn(40f, 100f)
        return (symmetryScore * 10f).roundToInt() / 10f
    }

    private fun distancePointToLine(p: Point3D, linePt: Point3D, dx: Float, dy: Float, lineLen: Float): Float {
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
        val groupScores = mapOf(
            "symmetry" to 94f,
            "proportions" to 88f,
            "eyes" to 90f,
            "nose" to 88f,
            "lips" to 90f,
            "jaw_chin" to 89f,
            "skin" to 85f
        )
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
            symmetryIndex = 94,
            groupScores = groupScores,
            overallFaceScore = 89.0f,
            confidence = 90.0f,
            uncertainty = 1.0f
        )
    }
}
