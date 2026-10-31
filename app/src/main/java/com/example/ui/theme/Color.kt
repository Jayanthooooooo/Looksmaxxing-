package com.example.ui.theme

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

object LumoraColors {
    // Near-black dark-only palette (#0B0B0F)
    val DarkBackground = Color(0xFF0B0B0F)
    val DarkSurface = Color(0xFF141419)
    val DarkSurfaceVariant = Color(0xFF1B1B22)
    val DarkOnSurface = Color(0xFFF4F4F6)
    val DarkOnSurfaceMuted = Color(0x99F4F4F6) // 60% opacity

    // Champagne gold accent (#D9C59B)
    val ChampagneGold = Color(0xFFD9C59B)
    val ChampagneGoldLight = Color(0xFFEDE0C8)
    val ChampagneGoldDark = Color(0xFFB5A075)

    // Legacy/Semantic mapping for seamless compatibility
    val VioletPrimary = ChampagneGold
    val MagentaPrimary = Color(0xFFE8D7B5)
    val SecondaryCyan = ChampagneGold
    val SuccessMint = Color(0xFF4ADE80)
    val WarningAmber = Color(0xFFFBBF24)
    val ErrorCoral = Color(0xFFF87171)
    val DarkOutline = Color(0x24FFFFFF)

    // Matching Light Palette (retained for backward compatibility, mapped to dark-first tokens)
    val LightBackground = Color(0xFF0B0B0F)
    val LightSurface = Color(0xFF141419)
    val LightSurfaceVariant = Color(0xFF1B1B22)
    val LightOnSurface = Color(0xFFF4F4F6)
    val LightOnSurfaceMuted = Color(0x99F4F4F6)

    val LightPrimary = ChampagneGold
    val LightOnPrimary = Color(0xFF0B0B0F)
    val LightAccent = ChampagneGold
    val LightSuccess = Color(0xFF4ADE80)
    val LightCaution = Color(0xFFFBBF24)
    val LightError = Color(0xFFF87171)
    val LightOutline = Color(0x24FFFFFF)

    // Warm-brown radial glow gradient from top right
    val WarmBrownGlow = Color(0xFF422B1B)
    val TopRightWarmGlow = Brush.radialGradient(
        colors = listOf(
            Color(0x594A301E),
            Color(0x26332014),
            Color.Transparent
        ),
        center = Offset(Float.POSITIVE_INFINITY, 0f),
        radius = 1200f
    )

    // Reusable Gradients & Glass Tokens
    val PrimaryGradient = Brush.horizontalGradient(listOf(ChampagneGold, ChampagneGoldLight))
    val AccentGradient = Brush.horizontalGradient(listOf(ChampagneGoldLight, ChampagneGold))
    val GlowGradient = TopRightWarmGlow

    // Translucent dark cards with 1dp subtle border
    val GlassBorder = Brush.linearGradient(
        colors = listOf(Color.White.copy(alpha = 0.14f), Color.White.copy(alpha = 0.05f))
    )
    val GlassBackground = Color.White.copy(alpha = 0.08f)
}
