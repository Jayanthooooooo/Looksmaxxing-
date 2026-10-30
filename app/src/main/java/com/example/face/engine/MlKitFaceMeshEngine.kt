package com.example.face.engine

import android.graphics.Bitmap
import com.example.core.Logger
import com.example.face.quality.Point3D
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.facemesh.FaceMeshDetection
import com.google.mlkit.vision.facemesh.FaceMeshDetectorOptions
import kotlinx.coroutines.tasks.await

class MlKitFaceMeshEngine : FaceLandmarkEngine {

    private val detector by lazy {
        val options = FaceMeshDetectorOptions.Builder()
            .setUseCase(FaceMeshDetectorOptions.FACE_MESH)
            .build()
        FaceMeshDetection.getClient(options)
    }

    override suspend fun analyse(bitmap: Bitmap): LandmarkResult {
        return try {
            val inputImage = InputImage.fromBitmap(bitmap, 0)
            val faceMeshes = detector.process(inputImage).await()

            if (faceMeshes.isEmpty()) {
                return LandmarkResult(emptyMap(), "mlkit-face-mesh", isFallback = false, faceCount = 0)
            }

            val primaryMesh = faceMeshes.first()
            val landmarkMap = mutableMapOf<Int, Point3D>()

            primaryMesh.allPoints.forEach { point ->
                val p = point.position
                landmarkMap[point.index] = Point3D(p.x, p.y, p.z)
            }

            LandmarkResult(
                landmarks = landmarkMap,
                engineName = "mlkit-face-mesh",
                isFallback = false,
                faceCount = faceMeshes.size
            )
        } catch (e: Exception) {
            Logger.w("FaceMesh detection failed or unsupported: ${e.message}")
            LandmarkResult(emptyMap(), "mlkit-face-mesh", isFallback = false, faceCount = 0)
        }
    }
}
