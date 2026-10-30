package com.example.face.quality

data class Point3D(val x: Float, val y: Float, val z: Float = 0f)

class LandmarkAggregator {

    /**
     * Aggregates a burst of landmark frames by computing the coordinate-wise median for each landmark index.
     * @param burst List of landmark maps, where each map maps landmark index to Point3D.
     * @return Map of landmark index to aggregated median Point3D.
     */
    fun aggregateMedianBurst(burst: List<Map<Int, Point3D>>): Map<Int, Point3D> {
        if (burst.isEmpty()) return emptyMap()
        if (burst.size == 1) return burst.first()

        val allIndices = burst.first().keys
        val result = mutableMapOf<Int, Point3D>()

        for (idx in allIndices) {
            val xs = burst.mapNotNull { it[idx]?.x }.sorted()
            val ys = burst.mapNotNull { it[idx]?.y }.sorted()
            val zs = burst.mapNotNull { it[idx]?.z }.sorted()

            if (xs.isNotEmpty() && ys.isNotEmpty()) {
                val medianX = xs[xs.size / 2]
                val medianY = ys[ys.size / 2]
                val medianZ = if (zs.isNotEmpty()) zs[zs.size / 2] else 0f
                result[idx] = Point3D(medianX, medianY, medianZ)
            }
        }

        return result
    }
}
