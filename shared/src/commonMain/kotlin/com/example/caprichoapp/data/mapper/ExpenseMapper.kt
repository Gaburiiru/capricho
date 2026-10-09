package com.example.caprichoapp.data.mapper

import com.example.caprichoapp.data.remote.dto.ExpenseDto
import com.example.caprichoapp.domain.model.Durability
import com.example.caprichoapp.domain.model.Expense
import com.example.caprichoapp.domain.model.ExpenseKind
import kotlinx.datetime.LocalDate

fun ExpenseDto.toDomain(defaultCategoryName: String? = null): Expense = Expense(
    id = id.orEmpty(),
    categoryId = categoryId,
    categoryName = defaultCategoryName ?: "Otros",
    title = title,
    amount = amount,
    installments = installments,
    durability = runCatching { Durability.valueOf(durability) }.getOrDefault(Durability.MEDIUM),
    kind = runCatching { ExpenseKind.valueOf(kind ?: "ONE_OFF") }.getOrDefault(ExpenseKind.ONE_OFF),
    spentAt = runCatching { LocalDate.parse(spentAt ?: "2024-01-01") }.getOrDefault(LocalDate(2024, 1, 1)),
)

fun Expense.toDto(userId: String? = null): ExpenseDto = ExpenseDto(
    id = id.ifBlank { null },
    userId = userId,
    categoryId = categoryId,
    title = title,
    amount = amount,
    installments = installments,
    durability = durability.name,
    kind = kind.name,
    spentAt = spentAt.toString(),
)
