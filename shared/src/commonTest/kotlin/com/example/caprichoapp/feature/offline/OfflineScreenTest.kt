package com.example.caprichoapp.feature.offline

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class OfflineScreenTest {

    @Test
    fun `todos los frames de la señal tienen las mismas dimensiones`() {
        (0..3).forEach { level ->
            val frame = wifiFrame(level)
            assertEquals(WIFI_FULL.size, frame.size, "alto en nivel $level")
            frame.forEachIndexed { i, row -> assertEquals(WIFI_FULL[i].length, row.length, "fila $i nivel $level") }
        }
    }

    @Test
    fun `la señal solo usa los caracteres de la paleta`() {
        (0..3).forEach { level ->
            wifiFrame(level).forEach { row -> assertTrue(row.all { it in ".XD" }) }
        }
    }

    @Test
    fun `la señal se enciende de a un arco hasta quedar completa`() {
        fun lit(level: Int) = wifiFrame(level).sumOf { row -> row.count { it == 'X' } }
        assertTrue(lit(0) < lit(1) && lit(1) < lit(2) && lit(2) < lit(3))
        assertEquals(WIFI_FULL.sumOf { row -> row.count { it == 'X' } }, lit(3))
    }
}
