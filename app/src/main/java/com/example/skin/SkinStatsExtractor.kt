package com.example.skin

import android.graphics.Bitmap
import android.graphics.Color
import kotlin.math.abs
import kotlin.math.pow
import kotlin.math.roundToInt
import kotlin.math.sqrt

data class SkinStats(
    val meanL: Float,
    val meanA: Float,
    val meanB: Float,
    val lumaStdDev: Float,
    val rednessElevation: Float,
    val textureVariance: Float,
    val undereyeContrast: Float,
    val evennessIndex: Float, // 0 - 100
    val rednessIndex: Float,  // 0 - 100 (0 = normal tone, higher = elevated redness)
    val smoothnessIndex: Float // 0 - 100
)

class SkinStatsExtractor {

    /**
     * Extracts cosmetic surface statistics from facial bitmap regions.
     * Computes CIE L*a*b* color spaces to isolate lightness (L) from chromatic redness (a*).
     */
    fun extract(bitmap: Bitmap, faceBounds: android.graphics.Rect? = null): SkinStats {
        val width = bitmap.width
        val height = bitmap.height
        if (width <= 0 || height <= 0) {
            return fallbackStats()
        }

        val bounds = faceBounds ?: android.graphics.Rect(
            (width * 0.2f).roundToInt(),
            (height * 0.2f).roundToInt(),
            (width * 0.8f).roundToInt(),
            (height * 0.8f).roundToInt()
        )

        // Sample points across forehead, cheeks, and chin ROIs
        val sampleL = mutableListOf<Float>()
        val sampleA = mutableListOf<Float>()
        val sampleB = mutableListOf<Float>()
        val gradientDiffs = mutableListOf<Float>()

        val step = maxOf(2, (bounds.width() / 40))
        val startX = bounds.left.coerceIn(0, width - 1)
        val endX = bounds.right.coerceIn(0, width - 1)
        val startY = bounds.top.coerceIn(0, height - 1)
        val endY = bounds.bottom.coerceIn(0, height - 1)

        for (y in startY until endY step step) {
            for (x in startX until endX step step) {
                val pixel = bitmap.getPixel(x, y)
                val r = Color.red(pixel)
                val g = Color.green(pixel)
                val b = Color.blue(pixel)

                // Filter out non-skin extremes (e.g. black hair or white glare)
                val luma = 0.299f * r + 0.587f * g + 0.114f * b
                if (luma in 30f..235f) {
                    val lab = rgbToLab(r, g, b)
                    sampleL.add(lab[0])
                    sampleA.add(lab[1])
                    sampleB.add(lab[2])

                    // High-frequency neighbor gradient for texture proxy
                    if (x + 1 < width) {
                        val neighbor = bitmap.getPixel(x + 1, y)
                        val nLuma = 0.299f * Color.red(neighbor) + 0.587f * Color.green(neighbor) + 0.114f * Color.blue(neighbor)
                        gradientDiffs.add(abs(luma - nLuma))
                    }
                }
            }
        }

        if (sampleL.isEmpty()) {
            return fallbackStats()
        }

        val meanL = sampleL.average().toFloat()
        val meanA = sampleA.average().toFloat()
        val meanB = sampleB.average().toFloat()

        // Standard deviation of L channel represents tone evenness
        val varL = sampleL.map { (it - meanL).pow(2) }.average().toFloat()
        val lumaStdDev = sqrt(varL)

        // Redness proxy: deviation of a* channel from typical baseline (typical Caucasian/Asian/Afro-Caribbean skin a* is 10-22)
        val rednessElevation = (meanA - 14f).coerceAtLeast(0f)

        // Texture variance: average micro-gradient
        val avgGradient = if (gradientDiffs.isNotEmpty()) gradientDiffs.average().toFloat() else 4f

        // Under-eye contrast estimation: compare upper cheek to infraorbital band
        val cheekY = (bounds.top + bounds.height() * 0.55f).toInt().coerceIn(0, height - 1)
        val undereyeY = (bounds.top + bounds.height() * 0.42f).toInt().coerceIn(0, height - 1)
        val cheekX = (bounds.left + bounds.width() * 0.35f).toInt().coerceIn(0, width - 1)

        val cheekPix = bitmap.getPixel(cheekX, cheekY)
        val undereyePix = bitmap.getPixel(cheekX, undereyeY)
        val cheekL = rgbToLab(Color.red(cheekPix), Color.green(cheekPix), Color.blue(cheekPix))[0]
        val undereyeL = rgbToLab(Color.red(undereyePix), Color.green(undereyePix), Color.blue(undereyePix))[0]
        val undereyeContrast = (cheekL - undereyeL).coerceAtLeast(0f)

        // Map to 0-100 scales
        // Evenness: stdDev 3-15 -> 100 to 50
        val evennessIndex = (100f - (lumaStdDev * 3.5f)).coerceIn(50f, 98f)
        // Redness: elevation 0-15 -> 0 to 100
        val rednessIndex = (rednessElevation * 6.5f).coerceIn(0f, 100f)
        // Smoothness: gradient 2-12 -> 100 to 50
        val smoothnessIndex = (100f - (avgGradient * 4.5f)).coerceIn(50f, 98f)

        return SkinStats(
            meanL = meanL,
            meanA = meanA,
            meanB = meanB,
            lumaStdDev = lumaStdDev,
            rednessElevation = rednessElevation,
            textureVariance = avgGradient,
            undereyeContrast = undereyeContrast,
            evennessIndex = evennessIndex,
            rednessIndex = rednessIndex,
            smoothnessIndex = smoothnessIndex
        )
    }

    /**
     * Converts sRGB [0-255] to CIE 1976 L*a*b*.
     */
    private fun rgbToLab(r: Int, g: Int, b: Int): FloatArray {
        // 1. Convert sRGB to linear RGB
        var rL = r / 255.0
        var gL = g / 255.0
        var bL = b / 255.0

        rL = if (rL > 0.04045) ((rL + 0.055) / 1.055).pow(2.4) else rL / 12.92
        gL = if (gL > 0.04045) ((gL + 0.055) / 1.055).pow(2.4) else gL / 12.92
        bL = if (bL > 0.04045) ((bL + 0.055) / 1.055).pow(2.4) else bL / 12.92

        // 2. Linear RGB to XYZ (D65 standard illuminant)
        val x = (rL * 0.4124 + gL * 0.3576 + bL * 0.1805) / 0.95047
        val y = (rL * 0.2126 + gL * 0.7152 + bL * 0.0722) / 1.00000
        val z = (rL * 0.0193 + gL * 0.1192 + bL * 0.9505) / 1.08883

        // 3. XYZ to L*a*b*
        fun f(t: Double): Double = if (t > 0.008856) t.pow(1.0 / 3.0) else 7.787 * t + 16.0 / 116.0

        val fx = f(x)
        val fy = f(y)
        val fz = f(z)

        val L = (116.0 * fy - 16.0).toFloat().coerceIn(0f, 100f)
        val a = (500.0 * (fx - fy)).toFloat()
        val bVal = (200.0 * (fy - fz)).toFloat()

        return floatArrayOf(L, a, bVal)
    }

    private fun fallbackStats() = SkinStats(
        meanL = 65f,
        meanA = 15f,
        meanB = 18f,
        lumaStdDev = 5f,
        rednessElevation = 2f,
        textureVariance = 3.5f,
        undereyeContrast = 4f,
        evennessIndex = 82f,
        rednessIndex = 14f,
        smoothnessIndex = 84f
    )
}
