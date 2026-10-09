package com.example.caprichoapp.core.designsystem.pixel

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Outline
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.LayoutDirection

/**
 * Rectángulo con esquinas "escalonadas" de dos escalones, típico del pixel art.
 * [step] es el tamaño de cada escalón.
 */
class PixelCutShape(private val step: Dp) : Shape {
    override fun createOutline(
        size: Size,
        layoutDirection: LayoutDirection,
        density: Density,
    ): Outline {
        val c = with(density) { step.toPx() }
        val w = size.width
        val h = size.height
        // Si el componente es muy chico, los escalones se achican para no cruzarse
        val s = minOf(c, w / 4f, h / 4f)
        val path = Path().apply {
            moveTo(2 * s, 0f)
            lineTo(w - 2 * s, 0f)
            lineTo(w - 2 * s, s); lineTo(w - s, s); lineTo(w - s, 2 * s); lineTo(w, 2 * s)
            lineTo(w, h - 2 * s)
            lineTo(w - s, h - 2 * s); lineTo(w - s, h - s); lineTo(w - 2 * s, h - s); lineTo(w - 2 * s, h)
            lineTo(2 * s, h)
            lineTo(2 * s, h - s); lineTo(s, h - s); lineTo(s, h - 2 * s); lineTo(0f, h - 2 * s)
            lineTo(0f, 2 * s)
            lineTo(s, 2 * s); lineTo(s, s); lineTo(2 * s, s)
            close()
        }
        return Outline.Generic(path)
    }
}

/** ¿El tema actual es oscuro? Se deduce de la luminancia del fondo. */
@Composable
fun isDarkTheme(): Boolean = MaterialTheme.colorScheme.background.luminance() < 0.5f

/** Colores del "bicho virtual" para ambos temas. */
data class TamagotchiColors(
    val shell: Color,
    val shellDark: Color,
    val lcdBackground: Color,
    val lcdInk: Color,
    val shadow: Color,
)

@Composable
fun tamagotchiColors(): TamagotchiColors {
    val c = MaterialTheme.colorScheme
    return if (isDarkTheme()) {
        TamagotchiColors(
            shell = c.secondary,
            shellDark = c.onSecondary,
            lcdBackground = c.background,
            lcdInk = c.primary,
            shadow = c.secondary.copy(alpha = 0.25f),
        )
    } else {
        TamagotchiColors(
            shell = Color(0xFFFF9DB5),
            shellDark = c.secondary,
            lcdBackground = c.primaryContainer,
            lcdInk = c.onPrimaryContainer,
            shadow = Color(0xFF8C2A4B), // sombra dura opaca: el plástico se ve con volumen (antes: 35 % de opacidad)
        )
    }
}
