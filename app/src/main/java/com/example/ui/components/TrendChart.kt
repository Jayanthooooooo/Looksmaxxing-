package com.example.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.LumoraColors

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
    lineColor: Color = LumoraColors.SecondaryCyan
) {
    val accessibleSummary = if (points.isEmpty()) {
        "No data points recorded yet."
    } else {
        val minVal = points.minOf { it.value }
        val maxVal = points.maxOf { it.value }
        val latestVal = points.last().value
        "$title line chart: ${points.size} entries. Latest value $latestVal$unit. Min: $minVal, Max: $maxVal."
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .background(LumoraColors.GlassBackground)
            .border(1.dp, LumoraColors.GlassBorder, RoundedCornerShape(24.dp))
            .padding(20.dp)
            .semantics { contentDescription = accessibleSummary }
    ) {
        Column {
            Text(
                text = title,
                style = MaterialTheme.typography.titleSmall.copy(
                    letterSpacing = 1.sp,
                    color = Color.White.copy(alpha = 0.6f)
                )
            )
            Spacer(modifier = Modifier.height(16.dp))

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
                        color = Color.White.copy(alpha = 0.4f)
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
                    val span = (maxVal - minVal).coerceAtLeast(10f) // Keep some span for visual

                    val stepX = width / (points.size - 1)
                    val path = Path()

                    points.forEachIndexed { index, point ->
                        val x = index * stepX
                        val normalizedY = (point.value - minVal) / span
                        val y = height - (normalizedY * (height - 40.dp.toPx()) + 20.dp.toPx())

                        if (index == 0) {
                            path.moveTo(x, y)
                        } else {
                            path.lineTo(x, y)
                        }

                        // Draw point circle with glow
                        drawCircle(
                            color = lineColor.copy(alpha = 0.3f),
                            radius = 8.dp.toPx(),
                            center = Offset(x, y)
                        )
                        drawCircle(
                            color = lineColor,
                            radius = 4.dp.toPx(),
                            center = Offset(x, y)
                        )
                    }

                    // Draw line with gradient or solid
                    drawPath(
                        path = path,
                        color = lineColor,
                        style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round)
                    )
                }
            }
        }
    }
}
