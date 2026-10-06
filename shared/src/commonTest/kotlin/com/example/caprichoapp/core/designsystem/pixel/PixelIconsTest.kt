package com.example.caprichoapp.core.designsystem.pixel

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class PixelIconsTest {

    @Test
    fun `cada ícono tiene filas del mismo ancho y solo usa la tinta`() {
        PixelIcon.entries.forEach { icon ->
            val width = icon.rows.first().length
            icon.rows.forEachIndexed { i, row ->
                assertEquals(width, row.length, "ancho de la fila $i en $icon")
                assertTrue(row.all { it == 'X' || it == '.' }, "caracter inválido en $icon")
            }
        }
    }

    @Test
    fun `los íconos de la barra inferior son cuadrados de 9x9`() {
        listOf(PixelIcon.Home, PixelIcon.History, PixelIcon.Goals, PixelIcon.Profile).forEach { icon ->
            assertEquals(9, icon.rows.size, "alto de $icon")
            assertTrue(icon.rows.all { it.length == 9 }, "ancho de $icon")
        }
    }
}
