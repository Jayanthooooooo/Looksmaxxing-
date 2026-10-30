package com.example.face.quality

import kotlin.math.PI

/**
 * 1€ Filter (One Euro Filter) for noise reduction and jitter suppression with low lag.
 * Casiez, G., Roussel, N. and Vogel, F. (2012).
 */
class OneEuroFilter(
    private val minCutoff: Double = 1.0,
    private val beta: Double = 0.007,
    private val dCutoff: Double = 1.0
) {
    private var xPrev: Double? = null
    private var dxPrev: Double = 0.0
    private var tPrev: Long? = null

    fun filter(x: Double, timestampMs: Long): Double {
        if (tPrev == null || xPrev == null) {
            xPrev = x
            dxPrev = 0.0
            tPrev = timestampMs
            return x
        }

        val dt = (timestampMs - tPrev!!).toDouble() / 1000.0
        tPrev = timestampMs

        if (dt <= 0.0) return xPrev!!

        val dx = (x - xPrev!!) / dt
        val edx = exponentialSmoothing(dx, dxPrev, alpha(dt, dCutoff))
        dxPrev = edx

        val cutoff = minCutoff + beta * kotlin.math.abs(edx)
        val filteredX = exponentialSmoothing(x, xPrev!!, alpha(dt, cutoff))
        xPrev = filteredX

        return filteredX
    }

    private fun alpha(dt: Double, cutoff: Double): Double {
        val tau = 1.0 / (2.0 * PI * cutoff)
        return 1.0 / (1.0 + tau / dt)
    }

    private fun exponentialSmoothing(value: Double, prevValue: Double, alpha: Double): Double {
        return alpha * value + (1.0 - alpha) * prevValue
    }

    fun reset() {
        xPrev = null
        dxPrev = 0.0
        tPrev = null
    }
}
