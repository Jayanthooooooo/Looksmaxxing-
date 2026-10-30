package com.example.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp

data class TrendPoint(
    val dateLabel: String,
    val value: Float
)

@Composable
fun TrendChart(
    title: String,
    points: List<TrendPoint>,
    modifier: Modifier = Modifier,
    unit: String = "",
    lineColor: Color = MaterialTheme.colorScheme.primary
) {
    val accessibleSummary = if (points.isEmpty()) {
        "No data points recorded yet."
    } else {
        val minVal = points.minOf { it.value }
        val maxVal = points.maxOf { it.value }
        val latestVal = points.last().value
        "$title line chart: ${points.size} entries. Latest value $latestVal$unit. Min: $minVal, Max: $maxVal."
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(MaterialTheme.colorScheme.surface)
            .padding(16.dp)
            .semantics { contentDescription = accessibleSummary }
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleSmall,
            color = MaterialTheme.colorScheme.onSurface
        )
        Spacer(modifier = Modifier.height(12.dp))

        if (points.size < 2) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(140.dp),
                contentAlignment = androidx.compose.ui.Alignment.Center
            ) {
                Text(
                    text = if (points.isEmpty()) "Take scans over time to see trends" else "Scan again to start trend tracking",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        } else {
            Canvas(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(140.dp)
            ) {
                val width = size.width
                val height = size.height

                val minVal = points.minOf { it.value }
                val maxVal = points.maxOf { it.value }
                val span = (maxVal - minVal).coerceAtLeast(0.001f)

                val stepX = width / (points.size - 1)
                val path = Path()

                points.forEachIndexed { index, point ->
                    val x = index * stepX
                    val normalizedY = (point.value - minVal) / span
                    val y = height - (normalizedY * (height - 30.dp.toPx()) + 15.dp.toPx())

                    if (index == 0) {
                        path.moveTo(x, y)
                    } else {
                        path.lineTo(x, y)
                    }

                    // Draw point circle
                    drawCircle(
                        color = lineColor,
                        radius = 4.dp.toPx(),
                        center = Offset(x, y)
                    )
                }

                // Draw line
                drawPath(
                    path = path,
                    color = lineColor,
                    style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round)
                )
            }
        }
    }
}
