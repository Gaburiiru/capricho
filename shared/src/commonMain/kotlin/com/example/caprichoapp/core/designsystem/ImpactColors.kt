package com.example.caprichoapp.core.designsystem

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

@Immutable
data class ImpactColors(
    val good: Color,
    val warning: Color,
    val heavy: Color,
)

val DarkImpactColors = ImpactColors(
    good = Color(0xFF34D399),
    warning = Color(0xFFFBBF24),
    heavy = Color(0xFFF87171),
)

val LightImpactColors = ImpactColors(
    good = Color(0xFF0F7A55),
    warning = Color(0xFF8A5A00),
    heavy = Color(0xFFB3261E),
)

val LocalImpactColors = staticCompositionLocalOf { DarkImpactColors }