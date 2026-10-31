package com.example.ui.components

import android.view.HapticFeedbackConstants
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForwardIos
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.LumoraColors

@Composable
fun GlassCard(
    modifier: Modifier = Modifier,
    shape: Shape = RoundedCornerShape(24.dp),
    content: @Composable ColumnScope.() -> Unit
) {
    Box(
        modifier = modifier
            .clip(shape)
            .background(LumoraColors.GlassBackground)
            .border(1.dp, LumoraColors.GlassBorder, shape)
            .padding(20.dp)
    ) {
        Column {
            content()
        }
    }
}

@Composable
fun GradientButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    gradient: Brush = LumoraColors.PrimaryGradient,
    enabled: Boolean = true
) {
    val view = LocalView.current
    var isPressed by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(
        targetValue = if (isPressed && enabled) 0.96f else 1f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
        label = "button_scale"
    )

    Box(
        modifier = modifier
            .scale(scale)
            .clip(CircleShape)
            .background(if (enabled) gradient else androidx.compose.ui.graphics.SolidColor(Color.White.copy(alpha = 0.1f)))
            .pointerInput(enabled) {
                if (enabled) {
                    detectTapGestures(
                        onPress = {
                            isPressed = true
                            try {
                                view.performHapticFeedback(android.view.HapticFeedbackConstants.VIRTUAL_KEY)
                            } catch (e: Exception) { }
                            tryAwaitRelease()
                            isPressed = false
                        },
                        onTap = { onClick() }
                    )
                }
            }
            .padding(vertical = 16.dp, horizontal = 32.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            color = if (enabled) Color.White else Color.White.copy(alpha = 0.3f),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
fun ScoreRing(
    score: Int,
    modifier: Modifier = Modifier,
    size: Dp = 160.dp,
    strokeWidth: Dp = 12.dp
) {
    val animatedScore by animateIntAsState(
        targetValue = score,
        animationSpec = tween(durationMillis = 1500, easing = FastOutSlowInEasing),
        label = "score_count"
    )

    val sweepAngle by animateFloatAsState(
        targetValue = (score / 100f) * 360f,
        animationSpec = tween(durationMillis = 1500, easing = FastOutSlowInEasing),
        label = "score_sweep"
    )

    Box(contentAlignment = Alignment.Center, modifier = modifier.size(size)) {
        androidx.compose.foundation.Canvas(modifier = Modifier.size(size)) {
            // Background track
            drawCircle(
                color = Color.White.copy(alpha = 0.05f),
                style = Stroke(width = strokeWidth.toPx())
            )
            
            // Progress arc
            drawArc(
                brush = LumoraColors.PrimaryGradient,
                startAngle = -90f,
                sweepAngle = sweepAngle,
                useCenter = false,
                style = Stroke(width = strokeWidth.toPx(), cap = StrokeCap.Round)
            )
        }
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = "$animatedScore",
                style = MaterialTheme.typography.displayLarge.copy(
                    fontSize = 48.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            )
            Text(
                text = "OVERALL",
                style = MaterialTheme.typography.labelSmall,
                color = Color.White.copy(alpha = 0.6f)
            )
        }
    }
}

@Composable
fun SubScoreBar(
    label: String,
    score: Float, // 0.0 to 1.0
    color: Color,
    modifier: Modifier = Modifier
) {
    val animatedProgress by animateFloatAsState(
        targetValue = score,
        animationSpec = tween(durationMillis = 1000, easing = FastOutSlowInEasing),
        label = "subscore_progress"
    )

    Column(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(label, style = MaterialTheme.typography.bodySmall, color = Color.White.copy(alpha = 0.7f))
            Text("${(score * 100).toInt()}%", style = MaterialTheme.typography.bodySmall, color = color, fontWeight = FontWeight.Bold)
        }
        Spacer(modifier = Modifier.height(6.dp))
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(8.dp)
                .clip(CircleShape)
                .background(Color.White.copy(alpha = 0.05f))
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(animatedProgress)
                    .fillMaxHeight()
                    .background(color, CircleShape)
            )
        }
    }
}

@Composable
fun ShimmerItem(
    modifier: Modifier = Modifier,
    height: Dp = 100.dp,
    shape: Shape = RoundedCornerShape(16.dp)
) {
    val transition = rememberInfiniteTransition(label = "shimmer")
    val translateAnim by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1000f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1200, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "shimmer_translate"
    )

    val brush = Brush.linearGradient(
        colors = listOf(
            Color.White.copy(alpha = 0.05f),
            Color.White.copy(alpha = 0.15f),
            Color.White.copy(alpha = 0.05f),
        ),
        start = Offset.Zero,
        end = Offset(x = translateAnim, y = translateAnim)
    )

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(height)
            .clip(shape)
            .background(brush)
    )
}

@Composable
fun GlowingScanButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val transition = rememberInfiniteTransition(label = "glow")
    val glowScale by transition.animateFloat(
        initialValue = 1f,
        targetValue = 1.2f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "glow_scale"
    )

    Box(contentAlignment = Alignment.Center, modifier = modifier) {
        // Outer glow
        Box(
            modifier = Modifier
                .size(72.dp)
                .scale(glowScale)
                .background(
                    Brush.radialGradient(
                        colors = listOf(LumoraColors.SecondaryCyan.copy(alpha = 0.3f), Color.Transparent)
                    ),
                    CircleShape
                )
        )
        // Button
        Surface(
            modifier = Modifier
                .size(64.dp)
                .clickable(onClick = onClick),
            shape = CircleShape,
            color = Color.Transparent,
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(LumoraColors.PrimaryGradient),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    painter = androidx.compose.ui.res.painterResource(id = android.R.drawable.ic_menu_camera),
                    contentDescription = "Scan",
                    tint = Color.White,
                    modifier = Modifier.size(32.dp)
                )
            }
        }
    }
}
@Composable
fun MenuNavigationCard(
    title: String,
    subtitle: String,
    icon: ImageVector,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    testTag: String = "menu_card"
) {
    GlassCard(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .testTag(testTag),
        shape = RoundedCornerShape(20.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color.White.copy(alpha = 0.05f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = LumoraColors.SecondaryCyan,
                        modifier = Modifier.size(22.dp)
                    )
                }
                Spacer(modifier = Modifier.width(16.dp))
                Column {
                    Text(text = title, style = MaterialTheme.typography.titleSmall, color = Color.White)
                    Text(text = subtitle, style = MaterialTheme.typography.bodySmall, color = Color.White.copy(alpha = 0.5f))
                }
            }
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowForwardIos,
                contentDescription = null,
                tint = Color.White.copy(alpha = 0.3f),
                modifier = Modifier.size(16.dp)
            )
        }
    }
}
