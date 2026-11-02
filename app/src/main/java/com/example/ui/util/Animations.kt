package com.example.ui.util

import android.content.Context
import android.provider.Settings
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.AnimationSpec
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.ui.theme.LumoraColors
import kotlin.math.cos
import kotlin.math.sin

val LocalAnimationsEnabled = staticCompositionLocalOf { true }

fun isSystemAnimationsEnabled(context: Context): Boolean {
    return try {
        val scale = Settings.Global.getFloat(
            context.contentResolver,
            Settings.Global.ANIMATOR_DURATION_SCALE,
            1.0f
        )
        scale > 0f
    } catch (_: Exception) {
        true
    }
}

/**
 * Press feedback scale modifier: scale to 0.97 with spring on press
 */
fun Modifier.pressScaleEffect(
    enabled: Boolean = true
): Modifier = composed {
    val animationsEnabled = LocalAnimationsEnabled.current
    if (!enabled || !animationsEnabled) return@composed this

    var isPressed by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.97f else 1.0f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "pressScale"
    )

    this
        .graphicsLayer {
            scaleX = scale
            scaleY = scale
        }
        .pointerInput(Unit) {
            detectTapGestures(
                onPress = {
                    isPressed = true
                    tryAwaitRelease()
                    isPressed = false
                }
            )
        }
}

/**
 * Screen transitions: 240 ms fade plus 16 dp slide
 */
object ScreenTransitions {
    val enter: EnterTransition = fadeIn(animationSpec = tween(durationMillis = 240)) +
            slideInHorizontally(
                initialOffsetX = { fullWidth -> (16 * (fullWidth / 360).coerceAtLeast(1)) },
                animationSpec = tween(durationMillis = 240)
            )

    val exit: ExitTransition = fadeOut(animationSpec = tween(durationMillis = 240)) +
            slideOutHorizontally(
                targetOffsetX = { fullWidth -> -(16 * (fullWidth / 360).coerceAtLeast(1)) },
                animationSpec = tween(durationMillis = 240)
            )

    val popEnter: EnterTransition = fadeIn(animationSpec = tween(durationMillis = 240)) +
            slideInHorizontally(
                initialOffsetX = { fullWidth -> -(16 * (fullWidth / 360).coerceAtLeast(1)) },
                animationSpec = tween(durationMillis = 240)
            )

    val popExit: ExitTransition = fadeOut(animationSpec = tween(durationMillis = 240)) +
            slideOutHorizontally(
                targetOffsetX = { fullWidth -> (16 * (fullWidth / 360).coerceAtLeast(1)) },
                animationSpec = tween(durationMillis = 240)
            )
}

/**
 * Count up animated text for scores, XP, streak
 */
@Composable
fun AnimatedCountUpText(
    targetValue: Int,
    modifier: Modifier = Modifier,
    style: TextStyle = MaterialTheme.typography.headlineMedium,
    color: Color = Color.White,
    suffix: String = ""
) {
    val animationsEnabled = LocalAnimationsEnabled.current
    val animatable = remember { Animatable(if (animationsEnabled) 0f else targetValue.toFloat()) }

    LaunchedEffect(targetValue, animationsEnabled) {
        if (animationsEnabled) {
            animatable.snapTo(0f)
            animatable.animateTo(
                targetValue = targetValue.toFloat(),
                animationSpec = tween(durationMillis = 480, easing = LinearEasing)
            )
        } else {
            animatable.snapTo(targetValue.toFloat())
        }
    }

    Text(
        text = "${animatable.value.toInt()}$suffix",
        style = style,
        color = color,
        modifier = modifier
    )
}

/**
 * Staggered list item reveal (40 ms stagger)
 */
fun Modifier.staggeredItemReveal(
    index: Int
): Modifier = composed {
    val animationsEnabled = LocalAnimationsEnabled.current
    if (!animationsEnabled) return@composed this

    val delayMillis = (index * 40).coerceAtMost(360)
    val alphaAnim = remember { Animatable(0f) }
    val translationYAnim = remember { Animatable(16f) }

    LaunchedEffect(index) {
        kotlinx.coroutines.delay(delayMillis.toLong())
        alphaAnim.animateTo(1f, tween(durationMillis = 240))
    }

    LaunchedEffect(index) {
        kotlinx.coroutines.delay(delayMillis.toLong())
        translationYAnim.animateTo(0f, tween(durationMillis = 240))
    }

    this.graphicsLayer {
        alpha = alphaAnim.value
        translationY = translationYAnim.value * density
    }
}

/**
 * Scan button gold pulse glow modifier
 */
fun Modifier.goldPulseGlow(
    enabled: Boolean = true
): Modifier = composed {
    val animationsEnabled = LocalAnimationsEnabled.current
    if (!enabled || !animationsEnabled) return@composed this

    val infiniteTransition = rememberInfiniteTransition(label = "goldGlow")
    val glowAlpha by infiniteTransition.animateFloat(
        initialValue = 0.25f,
        targetValue = 0.7f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1600, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "glowAlpha"
    )

    this.shadow(
        elevation = (12 * glowAlpha).dp,
        shape = androidx.compose.foundation.shape.CircleShape,
        ambientColor = LumoraColors.GoldAccent,
        spotColor = LumoraColors.GoldAccent
    )
}

/**
 * Skeleton loading shimmer modifier
 */
fun Modifier.shimmerLoading(): Modifier = composed {
    val animationsEnabled = LocalAnimationsEnabled.current
    if (!animationsEnabled) return@composed this.background(Color.White.copy(alpha = 0.08f))

    val transition = rememberInfiniteTransition(label = "shimmer")
    val translateAnim by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1000f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1200, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "shimmerTranslate"
    )

    val brush = Brush.linearGradient(
        colors = listOf(
            Color.White.copy(alpha = 0.05f),
            Color.White.copy(alpha = 0.18f),
            Color.White.copy(alpha = 0.05f)
        ),
        start = Offset(translateAnim - 200f, translateAnim - 200f),
        end = Offset(translateAnim, translateAnim)
    )

    this.background(brush)
}

/**
 * Short gold sparkle burst animation for habit completion / skill unlock
 */
@Composable
fun SparkleBurstAnimation(
    trigger: Boolean,
    modifier: Modifier = Modifier,
    onComplete: () -> Unit = {}
) {
    val animationsEnabled = LocalAnimationsEnabled.current
    if (!trigger || !animationsEnabled) return

    val anim = remember { Animatable(0f) }

    LaunchedEffect(trigger) {
        anim.snapTo(0f)
        anim.animateTo(1f, tween(durationMillis = 500))
        onComplete()
    }

    val progress = anim.value
    val alpha = if (progress < 0.7f) 1f else (1f - (progress - 0.7f) / 0.3f)

    Canvas(modifier = modifier.size(60.dp)) {
        val center = Offset(size.width / 2, size.height / 2)
        val particleCount = 8
        val maxRadius = size.width / 2f * progress

        for (i in 0 until particleCount) {
            val angle = Math.toRadians((i * (360.0 / particleCount)))
            val x = center.x + (maxRadius * cos(angle)).toFloat()
            val y = center.y + (maxRadius * sin(angle)).toFloat()
            val particleRadius = (3.dp.toPx() * (1f - progress * 0.5f))

            drawCircle(
                color = LumoraColors.GoldAccent.copy(alpha = alpha.coerceIn(0f, 1f)),
                radius = particleRadius,
                center = Offset(x, y)
            )
        }
    }
}
