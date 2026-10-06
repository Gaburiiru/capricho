package com.example.caprichoapp.core.util

import kotlin.test.Test
import kotlin.test.assertEquals

class MoneyFormatTest {
    @Test
    fun `formatea miles con punto`() = assertEquals("1.700.000", 1_700_000L.formatThousands())

    @Test
    fun `numeros chicos quedan sin separador`() {
        assertEquals("0", 0L.formatThousands())
        assertEquals("999", 999L.formatThousands())
    }

    @Test
    fun `justo en mil agrega el separador`() = assertEquals("1.000", 1_000L.formatThousands())
}
