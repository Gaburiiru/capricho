package com.example.caprichoapp.core.designsystem

import androidx.compose.material3.Typography
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import caprichoapp.shared.generated.resources.Res
import caprichoapp.shared.generated.resources.Silkscreen_Bold
import caprichoapp.shared.generated.resources.Silkscreen_Regular
import caprichoapp.shared.generated.resources.Nunito_Bold
import caprichoapp.shared.generated.resources.Nunito_ExtraBold
import caprichoapp.shared.generated.resources.Nunito_Regular
import caprichoapp.shared.generated.resources.Nunito_SemiBold
import org.jetbrains.compose.resources.Font

@Composable
fun nunitoFamily() = FontFamily(
    Font(Res.font.Nunito_Regular, FontWeight.Normal),
    Font(Res.font.Nunito_SemiBold, FontWeight.SemiBold),
    Font(Res.font.Nunito_Bold, FontWeight.Bold),
    Font(Res.font.Nunito_ExtraBold, FontWeight.ExtraBold),
)

/** Fuente pixel (Silkscreen, SIL OFL) para detalles estilo años 90: pantalla del bicho, botones, etiquetas. */
@Composable
fun pixelFontFamily() = FontFamily(
    Font(Res.font.Silkscreen_Regular, FontWeight.Normal),
    Font(Res.font.Silkscreen_Bold, FontWeight.Bold),
)

/**
 * Escala tipográfica "pixel". Regla de uso:
 *  - Silkscreen (pixel): títulos, botones, números grandes y etiquetas CORTAS.
 *  - Nunito: todo texto de lectura (descripciones, ayudas, datos).
 * Así los textos largos se leen bien y el estilo retro queda en los detalles.
 */
@Immutable
data class PixelTextStyles(
    val title: TextStyle,     // pregunta de cada paso
    val button: TextStyle,    // botones principales
    val pillTitle: TextStyle, // opciones en lista
    val tileNumber: TextStyle,// número grande de las fichas (cuotas)
    val tileLabel: TextStyle, // rótulo de las fichas
    val keypad: TextStyle,    // teclas del teclado numérico
    val tag: TextStyle,       // etiquetas cortas en mayúsculas
    val lcd: TextStyle,       // texto de la pantalla del bicho
    val display: TextStyle,   // monto grande (el tamaño se ajusta al largo)
)

fun pixelTextStyles(pixel: FontFamily) = PixelTextStyles(
    title = TextStyle(fontFamily = pixel, fontWeight = FontWeight.Bold, fontSize = 22.sp, lineHeight = 30.sp),
    button = TextStyle(fontFamily = pixel, fontWeight = FontWeight.Bold, fontSize = 16.sp, lineHeight = 20.sp),
    pillTitle = TextStyle(fontFamily = pixel, fontWeight = FontWeight.Bold, fontSize = 18.sp, lineHeight = 24.sp),
    tileNumber = TextStyle(fontFamily = pixel, fontWeight = FontWeight.Bold, fontSize = 36.sp, lineHeight = 40.sp),
    tileLabel = TextStyle(fontFamily = pixel, fontWeight = FontWeight.Normal, fontSize = 14.sp, lineHeight = 18.sp, letterSpacing = 1.sp),
    keypad = TextStyle(fontFamily = pixel, fontWeight = FontWeight.Bold, fontSize = 26.sp, lineHeight = 32.sp),
    tag = TextStyle(fontFamily = pixel, fontWeight = FontWeight.Bold, fontSize = 13.sp, lineHeight = 16.sp, letterSpacing = 1.5.sp),
    lcd = TextStyle(fontFamily = pixel, fontWeight = FontWeight.Normal, fontSize = 15.sp, lineHeight = 22.sp),
    display = TextStyle(fontFamily = pixel, fontWeight = FontWeight.Normal, fontSize = 48.sp, lineHeight = 54.sp),
)

val LocalPixelTextStyles = staticCompositionLocalOf { pixelTextStyles(FontFamily.Monospace) }

@Composable
fun caprichoTypography(): Typography {
    val nunito = nunitoFamily()
    return Typography(
        displayLarge = TextStyle(fontFamily = nunito, fontWeight = FontWeight.ExtraBold, fontSize = 56.sp, lineHeight = 64.sp),
        headlineMedium = TextStyle(fontFamily = nunito, fontWeight = FontWeight.Bold, fontSize = 28.sp, lineHeight = 36.sp),
        titleLarge = TextStyle(fontFamily = nunito, fontWeight = FontWeight.Bold, fontSize = 22.sp, lineHeight = 28.sp),
        titleMedium = TextStyle(fontFamily = nunito, fontWeight = FontWeight.SemiBold, fontSize = 16.sp, lineHeight = 24.sp),
        bodyLarge = TextStyle(fontFamily = nunito, fontWeight = FontWeight.Normal, fontSize = 16.sp, lineHeight = 24.sp),
        bodyMedium = TextStyle(fontFamily = nunito, fontWeight = FontWeight.Normal, fontSize = 14.sp, lineHeight = 20.sp),
        labelLarge = TextStyle(fontFamily = nunito, fontWeight = FontWeight.SemiBold, fontSize = 14.sp, lineHeight = 20.sp),
    )
}