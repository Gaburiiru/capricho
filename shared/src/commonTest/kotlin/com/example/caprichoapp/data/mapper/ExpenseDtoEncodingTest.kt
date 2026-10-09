package com.example.caprichoapp.data.mapper

import com.example.caprichoapp.domain.model.Durability
import com.example.caprichoapp.domain.model.Expense
import com.example.caprichoapp.domain.model.ExpenseKind
import kotlinx.datetime.LocalDate
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

/**
 * Regresión: kotlinx.serialization no envía los campos que valen su default.
 * `durability` es NOT NULL sin default en la base, así que el JSON del insert
 * tiene que llevarla SIEMPRE, incluso con el valor "común" (MEDIUM).
 */
class ExpenseDtoEncodingTest {

    private fun newExpenseJson(durability: Durability, installments: Int = 1): String =
        Json.encodeToString(
            Expense(
                title = "Supermercado",
                amount = 1250.0,
                installments = installments,
                durability = durability,
                kind = ExpenseKind.ONE_OFF,
                spentAt = LocalDate(2026, 10, 7),
            ).toDto("user-uuid"),
        )

    @Test
    fun `la durabilidad media se envia igual`() {
        assertTrue("\"durability\":\"MEDIUM\"" in newExpenseJson(Durability.MEDIUM))
    }

    @Test
    fun `todas las durabilidades se envian`() {
        Durability.entries.forEach { durability ->
            assertTrue("\"durability\":\"${durability.name}\"" in newExpenseJson(durability), "falta $durability")
        }
    }

    @Test
    fun `las cuotas se envian aunque sea un solo pago`() {
        assertTrue("\"installments\":1" in newExpenseJson(Durability.MEDIUM, installments = 1))
    }

    @Test
    fun `un gasto nuevo no manda id para que lo genere la base`() {
        assertFalse("\"id\"" in newExpenseJson(Durability.MEDIUM))
    }

    @Test
    fun `lleva la fecha y el tipo`() {
        val json = newExpenseJson(Durability.MEDIUM)
        assertTrue("\"spent_at\":\"2026-10-07\"" in json)
        assertTrue("\"kind\":\"ONE_OFF\"" in json)
    }
}
