package com.example.caprichoapp.core.designsystem

import androidx.compose.ui.text.font.FontFamily
import kotlin.test.Test
import kotlin.test.assertTrue

class PixelTextStylesTest {

    private val styles = pixelTextStyles(FontFamily.Monospace)

    private val all = mapOf(
        "title" to styles.title,
        "button" to styles.button,
        "pillTitle" to styles.pillTitle,
        "tileNumber" to styles.tileNumber,
        "tileLabel" to styles.tileLabel,
        "keypad" to styles.keypad,
        "tag" to styles.tag,
        "lcd" to styles.lcd,
        "display" to styles.display,
    )

    @Test
    fun `ningun estilo pixel es chico`() {
        all.forEach { (name, style) ->
            assertTrue(style.fontSize.value >= 13f, "$name es demasiado chico: ${style.fontSize}")
        }
    }

    @Test
    fun `el interlineado siempre es mayor que el tamano`() {
        all.forEach { (name, style) ->
            assertTrue(style.lineHeight.value > style.fontSize.value, "$name tiene poco interlineado")
        }
    }

    @Test
    fun `los titulos son mas grandes que los textos de lectura`() {
        assertTrue(styles.title.fontSize.value > styles.lcd.fontSize.value)
        assertTrue(styles.pillTitle.fontSize.value > styles.tag.fontSize.value)
    }
}
