package com.example.ui.components

import android.graphics.Bitmap
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.CompareArrows
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.drawscope.clipRect
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import kotlin.math.roundToInt

import androidx.compose.foundation.border
import androidx.compose.ui.text.style.TextAlign
import com.example.ui.theme.LumoraColors

@Composable
fun BeforeAfterSlider(
    beforeBitmap: Bitmap?,
    afterBitmap: Bitmap?,
    beforeLabel: String = "Before",
    afterLabel: String = "After",
    modifier: Modifier = Modifier
) {
    var splitFraction by remember { mutableFloatStateOf(0.5f) }

    BoxWithConstraints(
        modifier = modifier
            .fillMaxWidth()
            .height(340.dp)
            .clip(RoundedCornerShape(28.dp))
            .background(LumoraColors.DarkSurfaceVariant)
            .testTag("before_after_slider")
            .semantics {
                contentDescription = "Comparison slider. $beforeLabel on left, $afterLabel on right. Split position: ${(splitFraction * 100).toInt()}%."
            }
    ) {
        val widthPx = constraints.maxWidth.toFloat()
        val splitX = widthPx * splitFraction

        if (beforeBitmap != null && afterBitmap != null) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val imgBefore = beforeBitmap.asImageBitmap()
                val imgAfter = afterBitmap.asImageBitmap()

                // Draw after image full
                drawImage(imgAfter, dstSize = androidx.compose.ui.unit.IntSize(size.width.toInt(), size.height.toInt()))

                // Clip left side and draw before image
                clipRect(left = 0f, top = 0f, right = splitX, bottom = size.height) {
                    drawImage(imgBefore, dstSize = androidx.compose.ui.unit.IntSize(size.width.toInt(), size.height.toInt()))
                }

                // Divider line
                drawLine(
                    color = Color.White.copy(alpha = 0.8f),
                    start = Offset(splitX, 0f),
                    end = Offset(splitX, size.height),
                    strokeWidth = 2.dp.toPx()
                )
            }
        } else {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(
                    text = "Historical photos required for visual comparison",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.White.copy(alpha = 0.4f),
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(horizontal = 40.dp)
                )
            }
        }

        // Draggable handle
        Box(
            modifier = Modifier
                .offset { IntOffset((splitX - 24.dp.toPx()).roundToInt(), (constraints.maxHeight / 2 - 24.dp.toPx()).roundToInt()) }
                .size(48.dp)
                .clip(CircleShape)
                .background(LumoraColors.PrimaryGradient)
                .border(2.dp, Color.White.copy(alpha = 0.2f), CircleShape)
                .pointerInput(Unit) {
                    detectDragGestures { change, dragAmount ->
                        change.consume()
                        val newSplit = (splitX + dragAmount.x) / widthPx
                        splitFraction = newSplit.coerceIn(0.05f, 0.95f)
                    }
                },
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.CompareArrows,
                contentDescription = "Drag to compare photos",
                tint = Color.White,
                modifier = Modifier.size(24.dp)
            )
        }

        // Labels
        Box(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(16.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(Color.Black.copy(alpha = 0.5f))
                .padding(horizontal = 12.dp, vertical = 6.dp)
        ) {
            Text(text = beforeLabel, style = MaterialTheme.typography.labelMedium, color = Color.White)
        }

        Box(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(Color.Black.copy(alpha = 0.5f))
                .padding(horizontal = 12.dp, vertical = 6.dp)
        ) {
            Text(text = afterLabel, style = MaterialTheme.typography.labelMedium, color = Color.White)
        }
    }
}
