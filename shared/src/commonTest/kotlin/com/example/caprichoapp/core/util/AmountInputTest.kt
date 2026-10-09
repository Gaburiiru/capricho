package com.example.caprichoapp.core.util

import kotlin.test.Test
import kotlin.test.assertEquals

class AmountInputTest {

    @Test
    fun `agrega digitos al final`() {
        assertEquals("5000", "500".appendAmountDigit('0'))
        assertEquals("12", "1".appendAmountDigit('2'))
    }

    @Test
    fun `no permite ceros a la izquierda`() {
        assertEquals("", "".appendAmountDigit('0'))
        assertEquals("5", "".appendAmountDigit('5'))
        assertEquals("50", "5".appendAmountDigit('0'))
    }

    @Test
    fun `ignora lo que no es digito`() {
        assertEquals("12", "12".appendAmountDigit('a'))
        assertEquals("12", "12".appendAmountDigit('.'))
    }

    @Test
    fun `respeta el maximo de digitos`() {
        assertEquals("12345678", "12345678".appendAmountDigit('9', maxDigits = 8))
        assertEquals("123456789", "12345678".appendAmountDigit('9'))
        assertEquals("123456789", "123456789".appendAmountDigit('0'))
    }

    @Test
    fun `formatea con puntos de miles`() {
        assertEquals("0", "".formatAmount())
        assertEquals("500", "500".formatAmount())
        assertEquals("1.700.000", "1700000".formatAmount())
    }

    @Test
    fun `suma un monto rapido sin pasarse del maximo`() {
        assertEquals("5000", "".plusAmount(5_000))
        assertEquals("15000", "10000".plusAmount(5_000))
        assertEquals("999999999", "999999999".plusAmount(1))
    }

    @Test
    fun `formatea porcentajes con coma decimal y miles`() {
        assertEquals("14,0", 14.0.formatPercent())
        assertEquals("3.472,2", 3472.24.formatPercent())
        assertEquals("0,5", 0.55.formatPercent())
    }

    @Test
    fun `porcentajes enormes se topean en mas de 999`() {
        assertEquals(">999", 200_000.0.formatPercentCapped())
        assertEquals(">999", 1_000.0.formatPercentCapped())
    }

    @Test
    fun `porcentajes normales no se topean y llevan puntos de miles`() {
        assertEquals("999,9", 999.9.formatPercentCapped())
        assertEquals("14,0", 14.0.formatPercentCapped())
    }
}
