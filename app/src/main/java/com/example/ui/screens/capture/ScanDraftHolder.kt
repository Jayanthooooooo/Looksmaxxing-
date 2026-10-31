package com.example.ui.screens.capture

import android.graphics.Bitmap
import android.graphics.Rect
import com.example.domain.model.QualityReport
import com.example.face.quality.Point3D

object ScanDraftHolder {
    var capturedBitmap: Bitmap? = null
    var aggregatedLandmarks: Map<Int, Point3D> = emptyMap()
    var faceBounds: Rect? = null
    var scanKind: String = "front"
    var qualityReport: QualityReport? = null

    fun clear() {
        capturedBitmap = null
        aggregatedLandmarks = emptyMap()
        faceBounds = null
        qualityReport = null
        scanKind = "front"
    }
}
