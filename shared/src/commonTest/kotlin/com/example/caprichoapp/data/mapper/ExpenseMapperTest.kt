package com.example.caprichoapp.data.mapper

import com.example.caprichoapp.domain.model.Expense
import kotlinx.datetime.LocalDate
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class ExpenseMapperTest {

    @Test
    fun expensePayloadMatchesSupabaseExpensesColumns() {
        val payload = Expense(
            categoryId = "category-uuid",
            categoryName = "Comida",
            title = "Supermercado",
            amount = 1250.0,
            spentAt = LocalDate(2026, 10, 7),
        ).toDto("user-uuid")
        val json = Json.encodeToString(payload)

        assertTrue("\"category_id\":\"category-uuid\"" in json)
        assertFalse("category_name" in json)
        assertTrue("\"user_id\":\"user-uuid\"" in json)
    }
}
