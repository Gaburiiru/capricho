package com.example.caprichoapp.domain.repository

import com.example.caprichoapp.domain.model.Expense

interface ExpenseRepository {
    suspend fun getExpenses(): Result<List<Expense>>
    suspend fun addExpense(expense: Expense): Result<Unit>
    suspend fun updateExpense(expense: Expense): Result<Unit>
    suspend fun deleteExpense(expenseId: String): Result<Unit>
}
