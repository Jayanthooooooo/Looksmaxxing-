package com.example.face.engine

import android.graphics.Bitmap
import com.example.face.quality.Point3D

data class LandmarkResult(
    val landmarks: Map<Int, Point3D>,
    val engineName: String,
    val isFallback: Boolean,
    val faceCount: Int = 1
)

interface FaceLandmarkEngine {
    suspend fun analyse(bitmap: Bitmap): LandmarkResult
}
