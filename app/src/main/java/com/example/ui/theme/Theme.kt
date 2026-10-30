package com.example.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.unit.dp
import androidx.core.view.WindowCompat

private val LightColorScheme = lightColorScheme(
    primary = LumoraColors.LightPrimary,
    onPrimary = LumoraColors.LightOnPrimary,
    primaryContainer = LumoraColors.LightSurfaceVariant,
    onPrimaryContainer = LumoraColors.LightOnSurface,
    secondary = LumoraColors.LightAccent,
    onSecondary = LumoraColors.LightOnPrimary,
    tertiary = LumoraColors.LightCaution,
    background = LumoraColors.LightBackground,
    onBackground = LumoraColors.LightOnSurface,
    surface = LumoraColors.LightSurface,
    onSurface = LumoraColors.LightOnSurface,
    surfaceVariant = LumoraColors.LightSurfaceVariant,
    onSurfaceVariant = LumoraColors.LightOnSurfaceMuted,
    outline = LumoraColors.LightOutline,
    error = LumoraColors.LightError
)

private val DarkColorScheme = darkColorScheme(
    primary = LumoraColors.DarkPrimary,
    onPrimary = LumoraColors.DarkOnPrimary,
    primaryContainer = LumoraColors.DarkSurfaceVariant,
    onPrimaryContainer = LumoraColors.DarkOnSurface,
    secondary = LumoraColors.DarkAccent,
    onSecondary = LumoraColors.DarkOnPrimary,
    tertiary = LumoraColors.DarkCaution,
    background = LumoraColors.DarkBackground,
    onBackground = LumoraColors.DarkOnSurface,
    surface = LumoraColors.DarkSurface,
    onSurface = LumoraColors.DarkOnSurface,
    surfaceVariant = LumoraColors.DarkSurfaceVariant,
    onSurfaceVariant = LumoraColors.DarkOnSurfaceMuted,
    outline = LumoraColors.DarkOutline,
    error = LumoraColors.DarkError
)

val LumoraShapes = Shapes(
    small = RoundedCornerShape(12.dp),
    medium = RoundedCornerShape(20.dp),
    large = RoundedCornerShape(28.dp),
    extraLarge = RoundedCornerShape(32.dp)
)

@Composable
fun LumoraTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as? Activity)?.window
            if (window != null) {
                window.statusBarColor = colorScheme.background.toArgb()
                window.navigationBarColor = colorScheme.background.toArgb()
                WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
                WindowCompat.getInsetsController(window, view).isAppearanceLightNavigationBars = !darkTheme
            }
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = LumoraTypography,
        shapes = LumoraShapes,
        content = content
    )
}
