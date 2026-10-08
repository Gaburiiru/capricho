package com.example.caprichoapp.domain.model

import kotlinx.datetime.LocalDate

data class Expense(
    val id: String = "",
    val categoryId: String? = null,
    val categoryName: String = "Otros",
    val title: String? = null,
    val amount: Double,
    val installments: Int = 1,
    val durability: Durability = Durability.MEDIUM,
    val kind: ExpenseKind = ExpenseKind.ONE_OFF,
    val spentAt: LocalDate,
)