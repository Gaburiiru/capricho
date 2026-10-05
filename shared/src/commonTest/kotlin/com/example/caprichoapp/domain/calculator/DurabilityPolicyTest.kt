package com.example.caprichoapp.domain.calculator

import com.example.caprichoapp.domain.model.Durability
import com.example.caprichoapp.domain.model.ImpactLevel
import kotlin.test.Test
import kotlin.test.assertEquals

class DurabilityPolicyTest {

    @Test
    fun `gasto fugaz de 3 por ciento es GOOD`() {
        assertEquals(ImpactLevel.GOOD, DurabilityPolicy.levelFor(0.03, Durability.FLEETING))
    }

    @Test
    fun `gasto fugaz de 7 por ciento es WARNING`() {
        assertEquals(ImpactLevel.WARNING, DurabilityPolicy.levelFor(0.07, Durability.FLEETING))
    }

    @Test
    fun `gasto fugaz de 15 por ciento es HEAVY`() {
        assertEquals(ImpactLevel.HEAVY, DurabilityPolicy.levelFor(0.15, Durability.FLEETING))
    }

    @Test
    fun `el mismo 15 por ciento pesa distinto segun la durabilidad`() {
        assertEquals(ImpactLevel.HEAVY, DurabilityPolicy.levelFor(0.15, Durability.FLEETING))
        assertEquals(ImpactLevel.WARNING, DurabilityPolicy.levelFor(0.15, Durability.MEDIUM))
        assertEquals(ImpactLevel.GOOD, DurabilityPolicy.levelFor(0.15, Durability.HIGH))
    }

    @Test
    fun `justo en el umbral verde ya es WARNING`() {
        assertEquals(ImpactLevel.WARNING, DurabilityPolicy.levelFor(0.05, Durability.FLEETING))
    }

    @Test
    fun `justo en el umbral amarillo sigue siendo WARNING`() {
        assertEquals(ImpactLevel.WARNING, DurabilityPolicy.levelFor(0.10, Durability.FLEETING))
    }

    @Test
    fun `impacto cero es GOOD`() {
        assertEquals(ImpactLevel.GOOD, DurabilityPolicy.levelFor(0.0, Durability.FLEETING))
    }
}