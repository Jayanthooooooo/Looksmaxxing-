package com.example.ui.theme

import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

object LumoraColors {
    // Dark-first Palette (AI Beauty-Tech)
    val DarkBackground = Color(0xFF0B0D1A)
    val DarkSurface = Color(0xFF14172B)
    val DarkSurfaceVariant = Color(0xFF1E223D)
    val DarkOnSurface = Color(0xFFF0F3FF)
    val DarkOnSurfaceMuted = Color(0xFF9EA6C8)

    val VioletPrimary = Color(0xFF7C4DFF)
    val MagentaPrimary = Color(0xFFFF4D9D)
    val SecondaryCyan = Color(0xFF22E5FF)
    val SuccessMint = Color(0xFF3DF5A7)
    val WarningAmber = Color(0xFFFFC857)
    val ErrorCoral = Color(0xFFFF4D4D)
    val DarkOutline = Color(0xFF2D3356)

    // Matching Light Palette
    val LightBackground = Color(0xFFF5F6FC)
    val LightSurface = Color(0xFFFFFFFF)
    val LightSurfaceVariant = Color(0xFFE9ECF8)
    val LightOnSurface = Color(0xFF0B0D1A)
    val LightOnSurfaceMuted = Color(0xFF5A6282)

    val LightPrimary = Color(0xFF6C38FF)
    val LightOnPrimary = Color(0xFFFFFFFF)
    val LightAccent = Color(0xFFE6007A)
    val LightSuccess = Color(0xFF00B871)
    val LightCaution = Color(0xFFD98A00)
    val LightError = Color(0xFFDC2626)
    val LightOutline = Color(0xFFD1D6EE)

    // Reusable Gradients
    val PrimaryGradient = Brush.horizontalGradient(listOf(VioletPrimary, MagentaPrimary))
    val AccentGradient = Brush.horizontalGradient(listOf(MagentaPrimary, SecondaryCyan))
    val GlowGradient = Brush.radialGradient(listOf(VioletPrimary.copy(alpha = 0.4f), Color.Transparent))
    val GlassBorder = Brush.linearGradient(listOf(Color.White.copy(alpha = 0.25f), Color.White.copy(alpha = 0.05f)))
    val GlassBackground = Color.White.copy(alpha = 0.07f)
}
