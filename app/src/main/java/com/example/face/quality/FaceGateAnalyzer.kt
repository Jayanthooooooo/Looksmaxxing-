package com.example.face.quality

import android.graphics.Rect
import androidx.annotation.OptIn
import androidx.camera.core.ExperimentalGetImage
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import com.example.core.CaptureConfig
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.face.Face
import com.google.mlkit.vision.face.FaceDetection
import com.google.mlkit.vision.face.FaceDetectorOptions
import kotlin.math.abs

data class GateFrame(
    val faceFound: Boolean = false,
    val faceCount: Int = 0,
    val allPass: Boolean = false,
    val hint: String = "",
    val poseReady: Boolean = false,
    val distanceReady: Boolean = false,
    val lightReady: Boolean = false,
    val expressionReady: Boolean = false,
    val steadyReady: Boolean = false,
    val consecutivePassCount: Int = 0,
    val boundingBox: Rect? = null,
    val yaw: Float = 0f,
    val pitch: Float = 0f,
    val roll: Float = 0f,
    val meanLuma: Float = 0f,
    val lumaAsymmetry: Float = 0f,
    val faceWidthPct: Float = 0f,
    val eyeOpenLeft: Float? = null,
    val eyeOpenRight: Float? = null,
    val smileProb: Float? = null,
    val drift: Float = 0f
)

class FaceGateAnalyzer(
    private val kind: String, // "front" or "side"
    private val onFrame: (GateFrame) -> Unit
) : ImageAnalysis.Analyzer {

    private val evaluator = CaptureQualityEvaluator()

    private val detector = FaceDetection.getClient(
        FaceDetectorOptions.Builder()
            .setPerformanceMode(FaceDetectorOptions.PERFORMANCE_MODE_FAST)
            .setClassificationMode(FaceDetectorOptions.CLASSIFICATION_MODE_ALL)
            .setMinFaceSize(0.25f)
            .build()
    )

    private data class HistoryPoint(val timeMs: Long, val x: Float, val y: Float)
    private val stillnessHistory = mutableListOf<HistoryPoint>()
    private var consecutivePassFrames = 0

    @OptIn(ExperimentalGetImage::class)
    override fun analyze(imageProxy: ImageProxy) {
        val mediaImage = imageProxy.image
        if (mediaImage == null) {
            imageProxy.close()
            return
        }

        val rotation = imageProxy.imageInfo.rotationDegrees
        val inputImage = InputImage.fromMediaImage(mediaImage, rotation)

        val uprightW = if (rotation == 90 || rotation == 270) imageProxy.height else imageProxy.width
        val uprightH = if (rotation == 90 || rotation == 270) imageProxy.width else imageProxy.height

        val rawW = imageProxy.width
        val rawH = imageProxy.height

        // Extract luminance plane before async processing
        val yPlane = imageProxy.planes.firstOrNull()
        val yBuffer = yPlane?.buffer?.duplicate()
        val rowStride = yPlane?.rowStride ?: rawW
        val pixelStride = yPlane?.pixelStride ?: 1

        detector.process(inputImage)
            .addOnSuccessListener { faces ->
                val now = System.currentTimeMillis()

                if (faces.isEmpty()) {
                    consecutivePassFrames = 0
                    stillnessHistory.clear()
                    onFrame(
                        GateFrame(
                            faceFound = false,
                            faceCount = 0,
                            allPass = false,
                            hint = "No face found"
                        )
                    )
                    return@addOnSuccessListener
                }

                if (faces.size > 1) {
                    consecutivePassFrames = 0
                    stillnessHistory.clear()
                    onFrame(
                        GateFrame(
                            faceFound = true,
                            faceCount = faces.size,
                            allPass = false,
                            hint = "Only one person in frame"
                        )
                    )
                    return@addOnSuccessListener
                }

                val face = faces[0]
                val bounds = face.boundingBox

                // 1. Distance gate
                val distEval = evaluator.evaluateDistance(bounds, uprightW, uprightH)
                val faceWidthPct = (bounds.width().toFloat() / uprightW.toFloat()) * 100f

                // 2. Pose gate
                val yaw = face.headEulerAngleY
                val pitch = face.headEulerAngleX
                val roll = face.headEulerAngleZ
                val poseEval = evaluator.evaluatePose(yaw, pitch, roll, isSideScan = (kind == "side"))

                // 3. Lighting gate (32x32 grid sampling inside face bounding box on Y-plane)
                var meanLuma = 120f
                var asymmetry = 0.05f
                if (yBuffer != null) {
                    val bLeft = bounds.left.coerceIn(0, uprightW - 1)
                    val bTop = bounds.top.coerceIn(0, uprightH - 1)
                    val bRight = bounds.right.coerceIn(bLeft + 1, uprightW)
                    val bBottom = bounds.bottom.coerceIn(bTop + 1, uprightH)

                    var leftSum = 0.0
                    var rightSum = 0.0
                    val limit = yBuffer.limit()

                    for (row in 0 until 32) {
                        val uY = bTop + ((row + 0.5f) / 32f * (bBottom - bTop)).toInt().coerceIn(0, uprightH - 1)
                        for (col in 0 until 32) {
                            val uX = bLeft + ((col + 0.5f) / 32f * (bRight - bLeft)).toInt().coerceIn(0, uprightW - 1)

                            val (rX, rY) = when (rotation) {
                                90 -> Pair(uY, rawH - 1 - uX)
                                180 -> Pair(rawW - 1 - uX, rawH - 1 - uY)
                                270 -> Pair(rawW - 1 - uY, uX)
                                else -> Pair(uX, uY)
                            }

                            val safeRx = rX.coerceIn(0, rawW - 1)
                            val safeRy = rY.coerceIn(0, rawH - 1)
                            val offset = safeRy * rowStride + safeRx * pixelStride
                            val luma = if (offset in 0 until limit) {
                                (yBuffer.get(offset).toInt() and 0xFF).toFloat()
                            } else {
                                128f
                            }

                            if (col < 16) {
                                leftSum += luma
                            } else {
                                rightSum += luma
                            }
                        }
                    }
                    val leftMean = (leftSum / 512.0).toFloat()
                    val rightMean = (rightSum / 512.0).toFloat()
                    meanLuma = (leftMean + rightMean) / 2f
                    asymmetry = if (meanLuma > 0f) abs(leftMean - rightMean) / meanLuma else 0f
                }
                val lightEval = evaluator.evaluateLighting(meanLuma, asymmetry)

                // 4. Expression gate
                val leftEye = face.leftEyeOpenProbability
                val rightEye = face.rightEyeOpenProbability
                val smile = face.smilingProbability
                val exprEval = evaluator.evaluateExpression(leftEye, rightEye, smile)

                // 5. Stillness gate (600ms window, require >= 500ms data, max drift < 0.015)
                val cXNorm = bounds.centerX().toFloat() / uprightW.toFloat()
                val cYNorm = bounds.centerY().toFloat() / uprightH.toFloat()
                stillnessHistory.removeAll { it.timeMs < now - CaptureConfig.STILLNESS_WINDOW_MS }
                stillnessHistory.add(HistoryPoint(now, cXNorm, cYNorm))

                val oldest = stillnessHistory.firstOrNull()
                val hasEnoughTime = oldest != null && (now - oldest.timeMs) >= 500L
                val drift: Float
                val steadyReady: Boolean
                val steadyHint: String?

                if (hasEnoughTime && stillnessHistory.size >= 2) {
                    val xMin = stillnessHistory.minOf { it.x }
                    val xMax = stillnessHistory.maxOf { it.x }
                    val yMin = stillnessHistory.minOf { it.y }
                    val yMax = stillnessHistory.maxOf { it.y }
                    drift = maxOf(xMax - xMin, yMax - yMin)
                    if (drift <= CaptureConfig.MAX_STILLNESS_DRIFT_RATIO) {
                        steadyReady = true
                        steadyHint = null
                    } else {
                        steadyReady = false
                        steadyHint = "Hold phone steady."
                    }
                } else {
                    drift = 0f
                    steadyReady = false
                    steadyHint = "Hold still..."
                }

                val allPass = distEval.isReady && poseEval.isReady && lightEval.isReady && exprEval.isReady && steadyReady
                if (allPass) {
                    consecutivePassFrames++
                } else {
                    consecutivePassFrames = 0
                }

                // Pick first failing gate hint
                val hint = when {
                    !distEval.isReady -> distEval.flag ?: "Centre your face in the oval"
                    !poseEval.isReady -> poseEval.flag ?: "Look directly at the camera."
                    !lightEval.isReady -> lightEval.flag ?: "Adjust lighting."
                    !exprEval.isReady -> exprEval.flag ?: "Keep a neutral expression."
                    !steadyReady -> steadyHint ?: "Hold phone steady."
                    else -> "Hold still..."
                }

                onFrame(
                    GateFrame(
                        faceFound = true,
                        faceCount = 1,
                        allPass = allPass,
                        hint = hint,
                        poseReady = poseEval.isReady,
                        distanceReady = distEval.isReady,
                        lightReady = lightEval.isReady,
                        expressionReady = exprEval.isReady,
                        steadyReady = steadyReady,
                        consecutivePassCount = consecutivePassFrames,
                        boundingBox = bounds,
                        yaw = yaw,
                        pitch = pitch,
                        roll = roll,
                        meanLuma = meanLuma,
                        lumaAsymmetry = asymmetry,
                        faceWidthPct = faceWidthPct,
                        eyeOpenLeft = leftEye,
                        eyeOpenRight = rightEye,
                        smileProb = smile,
                        drift = drift
                    )
                )
            }
            .addOnFailureListener {
                consecutivePassFrames = 0
                stillnessHistory.clear()
                onFrame(
                    GateFrame(
                        faceFound = false,
                        allPass = false,
                        hint = "Detection error"
                    )
                )
            }
            .addOnCompleteListener {
                imageProxy.close()
            }
    }
}
