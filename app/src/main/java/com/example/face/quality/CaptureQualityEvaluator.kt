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
                GateEvaluation(false, "Look directly at the camera.")
            }
        } else {
            val yawOk = abs(yaw) in CaptureConfig.MIN_SIDE_YAW_DEG..CaptureConfig.MAX_SIDE_YAW_DEG
            val pitchOk = abs(pitch) <= 12f
            if (yawOk && pitchOk) {
                GateEvaluation(true)
            } else {
                GateEvaluation(false, "Turn profile to 45° angle.")
            }
        }
    }

    fun evaluateDistance(boundingBox: Rect, frameWidth: Int, frameHeight: Int): GateEvaluation {
        if (frameWidth <= 0 || frameHeight <= 0) return GateEvaluation(false, "Invalid frame")
        val bWidth = boundingBox.right - boundingBox.left
        val faceWidthRatio = bWidth.toFloat() / frameWidth.toFloat()

        val cx = (boundingBox.left + boundingBox.right) / 2f
        val cy = (boundingBox.top + boundingBox.bottom) / 2f
        val centerDx = abs(cx - frameWidth / 2f)
        val centerDy = abs(cy - frameHeight / 2f)

        val marginCutoff = boundingBox.left < frameWidth * 0.03f ||
                boundingBox.top < frameHeight * 0.03f ||
                boundingBox.right > frameWidth * 0.97f ||
                boundingBox.bottom > frameHeight * 0.97f

        val centerOff = centerDx > frameWidth * 0.12f || centerDy > frameHeight * 0.12f

        return when {
            faceWidthRatio < CaptureConfig.MIN_FACE_WIDTH_RATIO -> GateEvaluation(false, "Move phone closer")
            faceWidthRatio > CaptureConfig.MAX_FACE_WIDTH_RATIO -> GateEvaluation(false, "Move phone slightly further")
            marginCutoff || centerOff -> GateEvaluation(false, "Centre your face in the oval")
            else -> GateEvaluation(true)
        }
    }

    fun evaluateLighting(meanLuma: Float, leftRightAsymmetry: Float): GateEvaluation {
        return when {
            meanLuma < CaptureConfig.MIN_MEAN_LUMA -> GateEvaluation(false, "Lighting too dark. Face a light source.")
            meanLuma > CaptureConfig.MAX_MEAN_LUMA -> GateEvaluation(false, "Lighting too bright or direct glare.")
            leftRightAsymmetry > CaptureConfig.MAX_LUMA_ASYMMETRY -> GateEvaluation(false, "Uneven lighting. Face light evenly.")
            else -> GateEvaluation(true)
        }
    }

    fun evaluateExpression(leftEyeOpen: Float?, rightEyeOpen: Float?, smileProb: Float?): GateEvaluation {
        if (leftEyeOpen == null || rightEyeOpen == null || smileProb == null) {
            return GateEvaluation(false, "Face not clearly visible")
        }
        val leftEyeOk = leftEyeOpen > CaptureConfig.MIN_EYE_OPEN_PROB
        val rightEyeOk = rightEyeOpen > CaptureConfig.MIN_EYE_OPEN_PROB
        val smileOk = smileProb < CaptureConfig.MAX_SMILE_PROB

        return when {
            !leftEyeOk || !rightEyeOk -> GateEvaluation(false, "Open eyes naturally.")
            !smileOk -> GateEvaluation(false, "Keep a neutral expression.")
            else -> GateEvaluation(true)
        }
    }

    fun evaluateStillness(centerXHistory: List<Float>, centerYHistory: List<Float>): GateEvaluation {
        if (centerXHistory.size < 2 || centerYHistory.size < 2) return GateEvaluation(true)

        val xDelta = (centerXHistory.maxOrNull() ?: 0f) - (centerXHistory.minOrNull() ?: 0f)
        val yDelta = (centerYHistory.maxOrNull() ?: 0f) - (centerYHistory.minOrNull() ?: 0f)
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
