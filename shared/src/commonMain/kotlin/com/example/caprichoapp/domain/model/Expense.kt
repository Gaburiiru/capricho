package com.example.caprichoapp.domain.model
import kotlinx.datetime.LocalDate

data class Expense(
    val id: String,
    val categoryId: String,
    val title: String?,
    val amount: Double,
    val installments: Int = 1,
    val durability: Durability,
    val kind: ExpenseKind,
    val spentAt: LocalDate,
)