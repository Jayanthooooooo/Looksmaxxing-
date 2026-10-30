package com.example.face.quality

import android.graphics.Rect
import com.example.core.CaptureConfig
import com.example.domain.model.QualityReport
import kotlin.math.abs

data class GateEvaluation(
    val isReady: Boolean,
    val flag: String? = null
)

class CaptureQualityEvaluator {

    fun evaluatePose(yaw: Float, pitch: Float, roll: Float, isSideScan: Boolean = false): GateEvaluation {
        return if (!isSideScan) {
            val yawOk = abs(yaw) <= CaptureConfig.MAX_FRONT_YAW_DEG
            val pitchOk = abs(pitch) <= CaptureConfig.MAX_FRONT_PITCH_DEG
            val rollOk = abs(roll) <= CaptureConfig.MAX_FRONT_ROLL_DEG
            if (yawOk && pitchOk && rollOk) {
                GateEvaluation(true)
            } else {
                GateEvaluation(false, "Head turned or tilted. Look directly at camera.")
            }
        } else {
            val yawOk = yaw in CaptureConfig.MIN_SIDE_YAW_DEG..CaptureConfig.MAX_SIDE_YAW_DEG ||
                        yaw in (-CaptureConfig.MAX_SIDE_YAW_DEG)..(-CaptureConfig.MIN_SIDE_YAW_DEG)
            val pitchOk = abs(pitch) <= 12f
            if (yawOk && pitchOk) {
                GateEvaluation(true)
            } else {
                GateEvaluation(false, "Turn profile until yaw is 70-90 degrees.")
            }
        }
    }

    fun evaluateDistance(boundingBox: Rect, frameWidth: Int, frameHeight: Int): GateEvaluation {
        if (frameWidth <= 0 || frameHeight <= 0) return GateEvaluation(false, "Invalid frame")
        val faceWidthRatio = boundingBox.width().toFloat() / frameWidth.toFloat()

        return when {
            faceWidthRatio < CaptureConfig.MIN_FACE_WIDTH_RATIO -> GateEvaluation(false, "Move phone closer")
            faceWidthRatio > CaptureConfig.MAX_FACE_WIDTH_RATIO -> GateEvaluation(false, "Move phone slightly further")
            boundingBox.left < 0 || boundingBox.top < 0 || boundingBox.right > frameWidth || boundingBox.bottom > frameHeight -> {
                GateEvaluation(false, "Face cut off by frame edge")
            }
            else -> GateEvaluation(true)
        }
    }

    fun evaluateLighting(meanLuma: Float, leftRightAsymmetry: Float): GateEvaluation {
        return when {
            meanLuma < CaptureConfig.MIN_MEAN_LUMA -> GateEvaluation(false, "Lighting too dark. Face a natural light source.")
            meanLuma > CaptureConfig.MAX_MEAN_LUMA -> GateEvaluation(false, "Lighting too bright or direct glare.")
            leftRightAsymmetry > CaptureConfig.MAX_LUMA_ASYMMETRY -> GateEvaluation(false, "Uneven lighting on one side of face.")
            else -> GateEvaluation(true)
        }
    }

    fun evaluateExpression(leftEyeOpen: Float?, rightEyeOpen: Float?, smileProb: Float?): GateEvaluation {
        val leftEyeOk = leftEyeOpen == null || leftEyeOpen >= CaptureConfig.MIN_EYE_OPEN_PROB
        val rightEyeOk = rightEyeOpen == null || rightEyeOpen >= CaptureConfig.MIN_EYE_OPEN_PROB
        val smileOk = smileProb == null || smileProb <= CaptureConfig.MAX_SMILE_PROB

        return when {
            !leftEyeOk || !rightEyeOk -> GateEvaluation(false, "Eyes closed or squinting.")
            !smileOk -> GateEvaluation(false, "Relax expression into neutral resting pose.")
            else -> GateEvaluation(true)
        }
    }

    fun evaluateStillness(centerXHistory: List<Float>, centerYHistory: List<Float>): GateEvaluation {
        if (centerXHistory.size < 2 || centerYHistory.size < 2) return GateEvaluation(true)

        val xDelta = centerXHistory.maxOrNull()!! - centerXHistory.minOrNull()!!
        val yDelta = centerYHistory.maxOrNull()!! - centerYHistory.minOrNull()!!
        val maxDrift = maxOf(xDelta, yDelta)

        return if (maxDrift <= CaptureConfig.MAX_STILLNESS_DRIFT_RATIO) {
            GateEvaluation(true)
        } else {
            GateEvaluation(false, "Hold phone steady.")
        }
    }

    fun computeOverallQualityReport(
        pose: GateEvaluation,
        distance: GateEvaluation,
        lighting: GateEvaluation,
        expression: GateEvaluation,
        stillness: GateEvaluation,
        faceCount: Int
    ): QualityReport {
        val flags = mutableListOf<String>()
        val perGate = mutableMapOf<String, Boolean>()

        perGate["pose"] = pose.isReady
        pose.flag?.let { flags.add(it) }

        perGate["distance"] = distance.isReady
        distance.flag?.let { flags.add(it) }

        perGate["lighting"] = lighting.isReady
        lighting.flag?.let { flags.add(it) }

        perGate["expression"] = expression.isReady
        expression.flag?.let { flags.add(it) }

        perGate["stillness"] = stillness.isReady
        stillness.flag?.let { flags.add(it) }

        if (faceCount == 0) {
            flags.add("We couldn't find a face. Choose a clear photo looking at the camera.")
            return QualityReport(score = 0, flags = flags, perGate = perGate)
        } else if (faceCount > 1) {
            flags.add("More than one face is in this photo.")
            return QualityReport(score = 0, flags = flags, perGate = perGate)
        }

        var score = 100
        if (!pose.isReady) score -= 25
        if (!distance.isReady) score -= 20
        if (!lighting.isReady) score -= 25
        if (!expression.isReady) score -= 15
        if (!stillness.isReady) score -= 15

        return QualityReport(
            score = score.coerceIn(0, 100),
            flags = flags,
            perGate = perGate
        )
    }
}
