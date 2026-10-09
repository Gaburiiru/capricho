package com.example.caprichoapp.domain.calculator

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import com.example.caprichoapp.domain.model.Durability
import com.example.caprichoapp.domain.model.Goal
import kotlin.test.assertNull
import kotlin.test.assertTrue

class ImpactCalculatorTest {
    private fun goal(target: Double, saved: Double) = Goal(
        id = "1",
        title = "Amarok",
        targetAmount = target,
        savedAmount = saved,
        durability = Durability.HIGH,
    )

    @Test
    fun `gasto inmediato de 170000 con sueldo de 1700000 es 10 por ciento`() {
        val result = ImpactCalculator.salaryImpact(170_000.0, 1, 1_700_000.0)
        assertEquals(0.10, result, absoluteTolerance = 0.0001)
    }

    @Test
    fun `en 12 cuotas el impacto mensual se divide por 12`() {
        val result = ImpactCalculator.salaryImpact(1_200_000.0, 12, 1_000_000.0)
        assertEquals(0.10, result, absoluteTolerance = 0.0001)
    }

    @Test
    fun `sueldo cero lanza error`() {
        assertFailsWith<IllegalArgumentException> {
            ImpactCalculator.salaryImpact(1000.0, 1, 0.0)
        }
    }

    @Test
    fun `cuotas menores a 1 lanza error`() {
        assertFailsWith<IllegalArgumentException> {
            ImpactCalculator.monthlyAmount(1000.0, 0)
        }
    }
    @Test
    fun `monto cero da impacto cero`() {
        assertEquals(0.0, ImpactCalculator.salaryImpact(0.0, 1, 1_000_000.0))
    }

    @Test
    fun `un gasto mayor al sueldo supera el 100 por ciento`() {
        val result = ImpactCalculator.salaryImpact(2_000_000.0, 1, 1_000_000.0)
        assertEquals(2.0, result, absoluteTolerance = 0.0001)
    }

    @Test
    fun `una cuota equivale a pago inmediato`() {
        assertEquals(500.0, ImpactCalculator.monthlyAmount(500.0, 1))
    }

    @Test
    fun `un gasto de 200 sobre una meta que falta 1000 es 20 por ciento`() {
        val result = ImpactCalculator.goalImpact(200.0, goal(target = 1000.0, saved = 0.0))
        assertEquals(0.20, result, absoluteTolerance = 0.0001)
    }

    @Test
    fun `el impacto sobre la meta usa lo que falta y no el total`() {
        // Faltan 400 (1000 - 600), así que 200 es el 50%
        val result = ImpactCalculator.goalImpact(200.0, goal(target = 1000.0, saved = 600.0))
        assertEquals(0.50, result, absoluteTolerance = 0.0001)
    }

    @Test
    fun `meta ya cumplida da impacto cero`() {
        val result = ImpactCalculator.goalImpact(200.0, goal(target = 1000.0, saved = 1000.0))
        assertEquals(0.0, result)
    }

    @Test
    fun `meta ahorrada de mas da impacto cero y no negativo`() {
        val result = ImpactCalculator.goalImpact(200.0, goal(target = 1000.0, saved = 1500.0))
        assertEquals(0.0, result)
    }

    @Test
    fun `gasto mayor a lo que falta supera el 100 por ciento`() {
        val result = ImpactCalculator.goalImpact(800.0, goal(target = 1000.0, saved = 600.0))
        assertEquals(2.0, result, absoluteTolerance = 0.0001)
    }

    @Test
    fun `monto cero sobre una meta da impacto cero`() {
        val result = ImpactCalculator.goalImpact(0.0, goal(target = 1000.0, saved = 0.0))
        assertEquals(0.0, result)
    }

    // ---- goalGapNow / goalGapAfter ----

    @Test
    fun `sin ahorro te falta el 100 por ciento de la meta`() {
        assertEquals(1.0, ImpactCalculator.goalGapNow(goal(target = 2_000.0, saved = 0.0)), absoluteTolerance = 0.0001)
    }

    @Test
    fun `con gasto de 1000 sobre una meta de 2000 sin ahorro te faltaria el 150 por ciento`() {
        val result = ImpactCalculator.goalGapAfter(1_000.0, goal(target = 2_000.0, saved = 0.0))
        assertEquals(1.5, result, absoluteTolerance = 0.0001)
    }

    @Test
    fun `al ahorrar mas, lo que te faltaria con el gasto baja`() {
        val sinAhorro = ImpactCalculator.goalGapAfter(1_000.0, goal(target = 2_000.0, saved = 0.0))
        val conAhorro = ImpactCalculator.goalGapAfter(1_000.0, goal(target = 2_000.0, saved = 1_000.0))
        assertEquals(1.0, conAhorro, absoluteTolerance = 0.0001)
        assertTrue(conAhorro < sinAhorro)
    }

    @Test
    fun `meta cumplida no tiene distancia ni antes ni despues`() {
        val done = goal(target = 1_000.0, saved = 1_000.0)
        assertEquals(0.0, ImpactCalculator.goalGapNow(done))
        assertEquals(0.0, ImpactCalculator.goalGapAfter(500.0, done))
    }

    // ---- delayInMonths ----

    @Test
    fun `atraso en meses es monto sobre capacidad de ahorro`() {
        val result = ImpactCalculator.delayInMonths(200.0, 100.0)
        assertEquals(2.0, result!!, absoluteTolerance = 0.0001)
    }

    @Test
    fun `sin capacidad de ahorro no hay atraso calculable`() {
        assertNull(ImpactCalculator.delayInMonths(200.0, 0.0))
    }

    @Test
    fun `capacidad de ahorro negativa tampoco permite calcular atraso`() {
        assertNull(ImpactCalculator.delayInMonths(200.0, -50.0))
    }

    @Test
    fun `monto cero no genera atraso`() {
        val result = ImpactCalculator.delayInMonths(0.0, 100.0)
        assertEquals(0.0, result!!, absoluteTolerance = 0.0001)
    }
}