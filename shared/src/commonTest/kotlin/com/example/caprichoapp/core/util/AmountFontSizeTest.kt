package com.example.caprichoapp.core.util

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class AmountFontSizeTest {

    @Test
    fun `pocos caracteres dan el tamano maximo`() {
        assertEquals(52, amountFontSizeSp(3)) // "$ 0"
        assertEquals(52, amountFontSizeSp(0))
    }

    @Test
    fun `montos tipicos se ajustan al largo`() {
        assertEquals(44, amountFontSizeSp(7))  // "$ 5.000"
        assertEquals(34, amountFontSizeSp(9))  // "$ 120.000"
        assertEquals(28, amountFontSizeSp(11)) // "$ 1.700.000"
        assertEquals(25, amountFontSizeSp(12)) // "$ 99.999.999", el máximo que permite el teclado
    }

    @Test
    fun `nunca baja del minimo legible`() {
        assertEquals(24, amountFontSizeSp(40))
    }

    @Test
    fun `cuantos mas caracteres nunca mas grande`() {
        var previous = Int.MAX_VALUE
        for (chars in 1..30) {
            val size = amountFontSizeSp(chars)
            assertTrue(size <= previous, "el tamaño creció en $chars caracteres")
            previous = size
        }
    }
}
