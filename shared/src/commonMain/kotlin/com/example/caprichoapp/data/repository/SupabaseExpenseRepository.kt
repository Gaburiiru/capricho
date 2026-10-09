package com.example.caprichoapp.data.repository

import com.example.caprichoapp.core.util.runCatchingCancellable
import com.example.caprichoapp.data.mapper.toDomain
import com.example.caprichoapp.data.mapper.toDto
import com.example.caprichoapp.data.remote.dto.CategoryDto
import com.example.caprichoapp.data.remote.dto.ExpenseDto
import com.example.caprichoapp.domain.model.Expense
import com.example.caprichoapp.domain.repository.ExpenseRepository
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.postgrest.from

class SupabaseExpenseRepository(
    private val supabase: SupabaseClient,
) : ExpenseRepository {

    override suspend fun getExpenses(): Result<List<Expense>> = runCatchingCancellable {
        val userId = requireUserId()
        val categories = supabase.from("categories")
            .select()
            .decodeList<CategoryDto>()
            .associateBy { it.id }

        supabase.from(TABLE)
            .select { filter { eq("user_id", userId) } }
            .decodeList<ExpenseDto>()
            .map { dto ->
                val categoryName = dto.categoryId?.let { categories[it]?.name }
                dto.toDomain(categoryName)
            }
            .sortedByDescending { it.spentAt }
    }

    override suspend fun addExpense(expense: Expense): Result<Unit> = runCatchingCancellable {
        val userId = requireUserId()
        supabase.from(TABLE).insert(expense.toDto(userId))
        Unit
    }

    override suspend fun updateExpense(expense: Expense): Result<Unit> = runCatchingCancellable {
        val userId = requireUserId()
        supabase.from(TABLE)
            .update(expense.toDto(userId)) {
                filter {
                    eq("id", expense.id)
                    eq("user_id", userId)
                }
            }
        Unit
    }

    override suspend fun deleteExpense(expenseId: String): Result<Unit> = runCatchingCancellable {
        val userId = requireUserId()
        supabase.from(TABLE)
            .delete {
                filter {
                    eq("id", expenseId)
                    eq("user_id", userId)
                }
            }
        Unit
    }

    private fun requireUserId(): String =
        supabase.auth.currentUserOrNull()?.id
            ?: throw IllegalStateException("No hay una sesión activa")

    private companion object {
        const val TABLE = "expenses"
    }
}
