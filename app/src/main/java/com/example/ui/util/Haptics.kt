package com.example.ui.util

import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.provider.Settings
import androidx.compose.foundation.clickable
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.Role

val LocalHapticsEnabled = staticCompositionLocalOf { true }

object Haptics {

    private fun getVibrator(context: Context): Vibrator? {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val vibratorManager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
            vibratorManager?.defaultVibrator
        } else {
            @Suppress("DEPRECATION")
            context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
        }
    }

    private fun vibrate(context: Context, enabled: Boolean, action: (Vibrator) -> Unit) {
        if (!enabled) return
        try {
            val vibrator = getVibrator(context) ?: return
            if (vibrator.hasVibrator()) {
                action(vibrator)
            }
        } catch (_: Exception) {
            // Fallback safe
        }
    }

    fun tap(context: Context, enabled: Boolean = true) {
        vibrate(context, enabled) { vibrator ->
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                vibrator.vibrate(VibrationEffect.createPredefined(VibrationEffect.EFFECT_CLICK))
            } else {
                vibrator.vibrate(VibrationEffect.createOneShot(12L, 100))
            }
        }
    }

    fun select(context: Context, enabled: Boolean = true) {
        vibrate(context, enabled) { vibrator ->
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                vibrator.vibrate(VibrationEffect.createPredefined(VibrationEffect.EFFECT_TICK))
            } else {
                vibrator.vibrate(VibrationEffect.createOneShot(8L, 60))
            }
        }
    }

    fun success(context: Context, enabled: Boolean = true) {
        vibrate(context, enabled) { vibrator ->
            val timings = longArrayOf(0, 15, 50, 20)
            val amplitudes = intArrayOf(0, 120, 0, 255)
            vibrator.vibrate(VibrationEffect.createWaveform(timings, amplitudes, -1))
        }
    }

    fun warning(context: Context, enabled: Boolean = true) {
        vibrate(context, enabled) { vibrator ->
            vibrator.vibrate(VibrationEffect.createOneShot(120L, 200))
        }
    }

    fun heavy(context: Context, enabled: Boolean = true) {
        vibrate(context, enabled) { vibrator ->
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                vibrator.vibrate(VibrationEffect.createPredefined(VibrationEffect.EFFECT_HEAVY_CLICK))
            } else {
                vibrator.vibrate(VibrationEffect.createOneShot(45L, 255))
            }
        }
    }
}

fun Modifier.hapticClickable(
    enabled: Boolean = true,
    hapticsEnabled: Boolean = true,
    onClickLabel: String? = null,
    role: Role? = null,
    onClick: () -> Unit
): Modifier = composed {
    val context = LocalContext.current
    val globalHaptics = LocalHapticsEnabled.current
    this.clickable(
        enabled = enabled,
        onClickLabel = onClickLabel,
        role = role
    ) {
        if (hapticsEnabled && globalHaptics) {
            Haptics.tap(context)
        }
        onClick()
    }
}
