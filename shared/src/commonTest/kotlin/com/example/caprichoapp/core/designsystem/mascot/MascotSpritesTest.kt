package com.example.caprichoapp.core.designsystem.mascot

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class MascotSpritesTest {

    @Test
    fun `todos los frames de todos los estados tienen las mismas dimensiones`() {
        MascotMood.entries.forEach { mood ->
            MascotSprites.framesFor(mood).forEach { frame ->
                assertEquals(MascotSprites.HEIGHT, frame.rows.size, "alto en $mood")
                frame.rows.forEachIndexed { i, row ->
                    assertEquals(MascotSprites.WIDTH, row.length, "ancho de la fila $i en $mood")
                }
            }
        }
    }

    @Test
    fun `los sprites solo usan caracteres de la paleta`() {
        MascotMood.entries.forEach { mood ->
            MascotSprites.framesFor(mood).forEach { frame ->
                frame.rows.forEach { row ->
                    row.forEach { char ->
                        assertTrue(char in MascotSprites.VALID_CHARS, "caracter '$char' inválido en $mood")
                    }
                }
            }
        }
    }

    @Test
    fun `cada estado tiene frames y duraciones positivas`() {
        MascotMood.entries.forEach { mood ->
            val frames = MascotSprites.framesFor(mood)
            assertTrue(frames.isNotEmpty(), "sin frames en $mood")
            assertTrue(frames.all { it.durationMs > 0 }, "duración inválida en $mood")
        }
    }
}
