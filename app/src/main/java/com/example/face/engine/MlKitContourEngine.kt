package com.example.face.engine

import android.graphics.Bitmap
import com.example.core.Logger
import com.example.face.quality.Point3D
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.face.FaceContour
import com.google.mlkit.vision.face.FaceDetection
import com.google.mlkit.vision.face.FaceDetectorOptions
import kotlinx.coroutines.tasks.await

class MlKitContourEngine : FaceLandmarkEngine {

    private val detector by lazy {
        val options = FaceDetectorOptions.Builder()
            .setContourMode(FaceDetectorOptions.CONTOUR_MODE_ALL)
            .setPerformanceMode(FaceDetectorOptions.PERFORMANCE_MODE_FAST)
            .build()
        FaceDetection.getClient(options)
    }

    override suspend fun analyse(bitmap: Bitmap): LandmarkResult {
        return try {
            val inputImage = InputImage.fromBitmap(bitmap, 0)
            val faces = detector.process(inputImage).await()

            if (faces.isEmpty()) {
                return LandmarkResult(emptyMap(), "mlkit-contour-fallback", isFallback = true, faceCount = 0)
            }

            val face = faces.first()
            val landmarkMap = mutableMapOf<Int, Point3D>()

            // Map standard contours to canonical indices:
            // 33: right eye outer, 263: left eye outer
            // 133: right eye inner, 362: left eye inner
            // 1: nose tip
            // 61: mouth left, 291: mouth right
            // 152: chin
            // 10: forehead top

            val leftEyeContour = face.getContour(FaceContour.LEFT_EYE)?.points
            val rightEyeContour = face.getContour(FaceContour.RIGHT_EYE)?.points
            val noseBridgeContour = face.getContour(FaceContour.NOSE_BRIDGE)?.points
            val noseBottomContour = face.getContour(FaceContour.NOSE_BOTTOM)?.points
            val upperLipContour = face.getContour(FaceContour.UPPER_LIP_TOP)?.points
            val lowerLipContour = face.getContour(FaceContour.LOWER_LIP_BOTTOM)?.points
            val faceContour = face.getContour(FaceContour.FACE)?.points

            if (rightEyeContour != null && rightEyeContour.isNotEmpty()) {
                val outer = rightEyeContour.first()
                val inner = rightEyeContour[rightEyeContour.size / 2]
                landmarkMap[33] = Point3D(outer.x, outer.y)
                landmarkMap[133] = Point3D(inner.x, inner.y)
            }

            if (leftEyeContour != null && leftEyeContour.isNotEmpty()) {
                val inner = leftEyeContour.first()
                val outer = leftEyeContour[leftEyeContour.size / 2]
                landmarkMap[362] = Point3D(inner.x, inner.y)
                landmarkMap[263] = Point3D(outer.x, outer.y)
            }

            if (noseBottomContour != null && noseBottomContour.isNotEmpty()) {
                val tip = noseBottomContour[noseBottomContour.size / 2]
                landmarkMap[1] = Point3D(tip.x, tip.y)
            }

            if (upperLipContour != null && upperLipContour.isNotEmpty()) {
                val left = upperLipContour.first()
                val right = upperLipContour.last()
                val topCenter = upperLipContour[upperLipContour.size / 2]
                landmarkMap[61] = Point3D(left.x, left.y)
                landmarkMap[291] = Point3D(right.x, right.y)
                landmarkMap[0] = Point3D(topCenter.x, topCenter.y)
            }

            if (lowerLipContour != null && lowerLipContour.isNotEmpty()) {
                val bottomCenter = lowerLipContour[lowerLipContour.size / 2]
                landmarkMap[17] = Point3D(bottomCenter.x, bottomCenter.y)
            }

            if (faceContour != null && faceContour.isNotEmpty()) {
                val chin = faceContour[faceContour.size / 2]
                val cheekLeft = faceContour[faceContour.size / 4]
                val cheekRight = faceContour[faceContour.size * 3 / 4]
                val foreheadTop = faceContour.first()

                landmarkMap[152] = Point3D(chin.x, chin.y)
                landmarkMap[234] = Point3D(cheekLeft.x, cheekLeft.y)
                landmarkMap[454] = Point3D(cheekRight.x, cheekRight.y)
                landmarkMap[10] = Point3D(foreheadTop.x, foreheadTop.y)
            }

            LandmarkResult(
                landmarks = landmarkMap,
                engineName = "mlkit-contour-fallback",
                isFallback = true,
                faceCount = faces.size
            )
        } catch (e: Exception) {
            Logger.e("Contour fallback detection failed", e)
            LandmarkResult(emptyMap(), "mlkit-contour-fallback", isFallback = true, faceCount = 0)
        }
    }
}
