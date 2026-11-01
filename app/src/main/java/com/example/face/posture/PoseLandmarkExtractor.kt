package com.example.face.posture

import android.graphics.Bitmap
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.pose.PoseDetection
import com.google.mlkit.vision.pose.PoseLandmark
import com.google.mlkit.vision.pose.defaults.PoseDetectorOptions
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume

interface PoseLandmarkExtractor {
    suspend fun extractPoseLandmarks(bitmap: Bitmap): PosturePoseInput?
}

class MlKitPoseLandmarkExtractor : PoseLandmarkExtractor {
    override suspend fun extractPoseLandmarks(bitmap: Bitmap): PosturePoseInput? = suspendCancellableCoroutine { continuation ->
        try {
            val options = PoseDetectorOptions.Builder()
                .setDetectorMode(PoseDetectorOptions.SINGLE_IMAGE_MODE)
                .build()
            val detector = PoseDetection.getClient(options)
            val image = InputImage.fromBitmap(bitmap, 0)

            detector.process(image)
                .addOnSuccessListener { pose ->
                    val leftEar = pose.getPoseLandmark(PoseLandmark.LEFT_EAR)
                    val leftShoulder = pose.getPoseLandmark(PoseLandmark.LEFT_SHOULDER)
                    val leftHip = pose.getPoseLandmark(PoseLandmark.LEFT_HIP)

                    val rightEar = pose.getPoseLandmark(PoseLandmark.RIGHT_EAR)
                    val rightShoulder = pose.getPoseLandmark(PoseLandmark.RIGHT_SHOULDER)
                    val rightHip = pose.getPoseLandmark(PoseLandmark.RIGHT_HIP)

                    val leftAvgConf = if (leftEar != null && leftShoulder != null && leftHip != null) {
                        (leftEar.inFrameLikelihood + leftShoulder.inFrameLikelihood + leftHip.inFrameLikelihood) / 3f
                    } else 0f

                    val rightAvgConf = if (rightEar != null && rightShoulder != null && rightHip != null) {
                        (rightEar.inFrameLikelihood + rightShoulder.inFrameLikelihood + rightHip.inFrameLikelihood) / 3f
                    } else 0f

                    val isLeft = leftAvgConf >= rightAvgConf
                    val selectedAvgConf = if (isLeft) leftAvgConf else rightAvgConf

                    if (selectedAvgConf < 0.5f) {
                        detector.close()
                        continuation.resume(null)
                        return@addOnSuccessListener
                    }

                    val ear = if (isLeft) leftEar else rightEar
                    val shoulder = if (isLeft) leftShoulder else rightShoulder
                    val hip = if (isLeft) leftHip else rightHip

                    if (ear == null || shoulder == null || hip == null ||
                        ear.inFrameLikelihood < 0.5f ||
                        shoulder.inFrameLikelihood < 0.5f ||
                        hip.inFrameLikelihood < 0.5f
                    ) {
                        detector.close()
                        continuation.resume(null)
                        return@addOnSuccessListener
                    }

                    val input = PosturePoseInput(
                        ear = PostureLandmarkPoint(ear.position.x, ear.position.y, ear.inFrameLikelihood),
                        shoulder = PostureLandmarkPoint(shoulder.position.x, shoulder.position.y, shoulder.inFrameLikelihood),
                        hip = PostureLandmarkPoint(hip.position.x, hip.position.y, hip.inFrameLikelihood),
                        overallConfidence = selectedAvgConf
                    )

                    detector.close()
                    continuation.resume(input)
                }
                .addOnFailureListener {
                    detector.close()
                    continuation.resume(null)
                }
        } catch (e: Exception) {
            continuation.resume(null)
        }
    }
}
