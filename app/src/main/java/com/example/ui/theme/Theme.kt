package com.example.ui.theme

import android.app.Activity
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.unit.dp
import androidx.core.view.WindowCompat

// Pure dark-only luxury palette
private val DarkColorScheme = darkColorScheme(
    primary = LumoraColors.ChampagneGold,
    onPrimary = Color(0xFF0B0B0F),
    primaryContainer = Color(0x24D9C59B),
    onPrimaryContainer = LumoraColors.ChampagneGold,
    secondary = LumoraColors.ChampagneGold,
    onSecondary = Color(0xFF0B0B0F),
    secondaryContainer = Color(0x18FFFFFF),
    onSecondaryContainer = LumoraColors.DarkOnSurface,
    tertiary = LumoraColors.WarningAmber,
    background = LumoraColors.DarkBackground,
    onBackground = LumoraColors.DarkOnSurface,
    surface = LumoraColors.DarkSurface,
    onSurface = LumoraColors.DarkOnSurface,
    surfaceVariant = LumoraColors.DarkSurfaceVariant,
    onSurfaceVariant = LumoraColors.DarkOnSurfaceMuted,
    outline = Color.White.copy(alpha = 0.12f),
    outlineVariant = Color.White.copy(alpha = 0.06f),
    error = LumoraColors.ErrorCoral
)

val LumoraShapes = Shapes(
    small = RoundedCornerShape(12.dp),
    medium = RoundedCornerShape(16.dp),
    large = RoundedCornerShape(20.dp), // 20dp cards
    extraLarge = RoundedCornerShape(28.dp)
)

@Composable
fun LumoraTheme(
    darkTheme: Boolean = true, // Dark only as requested
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as? Activity)?.window
            if (window != null) {
                // Keep status bar and navigation bar dark
                window.statusBarColor = LumoraColors.DarkBackground.toArgb()
                window.navigationBarColor = LumoraColors.DarkBackground.toArgb()
                WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = false
                WindowCompat.getInsetsController(window, view).isAppearanceLightNavigationBars = false
            }
        }
    }

    MaterialTheme(
        colorScheme = DarkColorScheme,
        typography = LumoraTypography,
        shapes = LumoraShapes
    ) {
        // Enforce background near-black #0B0B0F with a soft warm-brown radial glow at the top right
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(LumoraColors.DarkBackground)
                .background(LumoraColors.TopRightWarmGlow)
        ) {
            content()
        }
    }
}
