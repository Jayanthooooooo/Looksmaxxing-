package com.example.face.posture

import kotlin.math.abs
import kotlin.math.atan2

enum class PostureBand(val displayName: String, val description: String) {
    NEUTRAL("Neutral", "Good alignment of ear over shoulder."),
    SLIGHTLY_FORWARD("Slightly forward", "Mild forward head position. Exercises may help balance posture."),
    FORWARD("Forward", "Noticeable forward head position. Ergonomic breaks and exercises may help.")
}

enum class PostureConfidence {
    HIGH, MEDIUM, LOW
}

data class PostureLandmarkPoint(
    val x: Float,
    val y: Float,
    val confidence: Float = 1.0f
)

data class PosturePoseInput(
    val ear: PostureLandmarkPoint?,
    val shoulder: PostureLandmarkPoint?,
    val hip: PostureLandmarkPoint?,
    val overallConfidence: Float = 0.9f
)

data class PostureAnalysisResult(
    val band: PostureBand?,
    val craniovertebralAngle: Float,
    val forwardOffsetRatio: Float,
    val confidence: PostureConfidence,
    val reasoning: String,
    val errorMessage: String? = null,
    val retakeTip: String? = null
)

object PostureEngine {
    private const val RETAKE_TIP = "Stand side-on, camera at chest height, 2 metres away, arms relaxed, shoulders and ears visible."

    fun analyze(input: PosturePoseInput?): PostureAnalysisResult {
        if (input == null || input.ear == null || input.shoulder == null || input.hip == null) {
            return PostureAnalysisResult(
                band = null,
                craniovertebralAngle = 0f,
                forwardOffsetRatio = 0f,
                confidence = PostureConfidence.LOW,
                reasoning = "Missing required body landmarks (ear, shoulder, hip).",
                errorMessage = "Not enough data",
                retakeTip = RETAKE_TIP
            )
        }

        val ear = input.ear
        val shoulder = input.shoulder
        val hip = input.hip

        val minConf = minOf(ear.confidence, shoulder.confidence, hip.confidence, input.overallConfidence)
        if (minConf < 0.5f) {
            return PostureAnalysisResult(
                band = null,
                craniovertebralAngle = 0f,
                forwardOffsetRatio = 0f,
                confidence = PostureConfidence.LOW,
                reasoning = "Landmark detection confidence is below reliable threshold.",
                errorMessage = "Not enough data",
                retakeTip = RETAKE_TIP
            )
        }

        val torsoHeight = abs(hip.y - shoulder.y)
        if (torsoHeight < 10f) {
            return PostureAnalysisResult(
                band = null,
                craniovertebralAngle = 0f,
                forwardOffsetRatio = 0f,
                confidence = PostureConfidence.LOW,
                reasoning = "Torso height is too small. Ensure full upper body is visible.",
                errorMessage = "Not enough data",
                retakeTip = RETAKE_TIP
            )
        }

        val dx = abs(ear.x - shoulder.x)
        val dy = abs(shoulder.y - ear.y)

        val angleRad = atan2(dy, dx)
        val angleDeg = (angleRad * 180.0 / Math.PI).toFloat()

        val forwardOffsetRatio = dx / torsoHeight

        val band = when {
            angleDeg >= 50f && forwardOffsetRatio <= 0.12f -> PostureBand.NEUTRAL
            angleDeg >= 42f && forwardOffsetRatio <= 0.20f -> PostureBand.SLIGHTLY_FORWARD
            else -> PostureBand.FORWARD
        }

        val confLevel = when {
            minConf >= 0.85f -> PostureConfidence.HIGH
            minConf >= 0.65f -> PostureConfidence.MEDIUM
            else -> PostureConfidence.LOW
        }

        val reasoning = when (band) {
            PostureBand.NEUTRAL -> "Craniovertebral angle of ${angleDeg.toInt()}° indicates healthy ear-to-shoulder alignment."
            PostureBand.SLIGHTLY_FORWARD -> "Craniovertebral angle of ${angleDeg.toInt()}° indicates a slight forward tilt of the neck."
            PostureBand.FORWARD -> "Craniovertebral angle of ${angleDeg.toInt()}° indicates a noticeable forward head extension."
        }

        return PostureAnalysisResult(
            band = band,
            craniovertebralAngle = angleDeg,
            forwardOffsetRatio = forwardOffsetRatio,
            confidence = confLevel,
            reasoning = reasoning,
            errorMessage = null,
            retakeTip = null
        )
    }
}
