package com.example.caprichoapp.feature.history

import com.example.caprichoapp.domain.model.Category
import com.example.caprichoapp.domain.model.Expense
import com.example.caprichoapp.domain.model.Profile
import com.example.caprichoapp.domain.model.ProfileDraft
import com.example.caprichoapp.domain.repository.CategoryRepository
import com.example.caprichoapp.domain.repository.ExpenseRepository
import com.example.caprichoapp.domain.repository.ProfileRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlinx.datetime.LocalDate
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class HistoryViewModelTest {

    @BeforeTest
    fun setUp() = Dispatchers.setMain(UnconfinedTestDispatcher())

    @AfterTest
    fun tearDown() = Dispatchers.resetMain()

    @Test
    fun testLoadsCategoriesAndDisplaysExpenseValue() = runTest {
        val categoryRepo = object : CategoryRepository {
            override suspend fun getCategories(): Result<List<Category>> = Result.success(
                listOf(
                    Category(id = "cat-1", name = "Comida"),
                    Category(id = "cat-2", name = "Transporte"),
                ),
            )
        }

        val expenseRepo = object : ExpenseRepository {
            override suspend fun getExpenses(): Result<List<Expense>> = Result.success(
                listOf(
                    Expense(
                        id = "exp-1",
                        categoryId = "cat-1",
                        categoryName = "Comida",
                        title = "Super",
                        amount = 4200.0,
                        spentAt = LocalDate(2024, 1, 10),
                    ),
                ),
            )

            override suspend fun addExpense(expense: Expense): Result<Unit> = Result.success(Unit)
            override suspend fun updateExpense(expense: Expense): Result<Unit> = Result.success(Unit)
            override suspend fun deleteExpense(expenseId: String): Result<Unit> = Result.success(Unit)
        }

        val profileRepo = object : ProfileRepository {
            override suspend fun fetchProfile(): Result<Profile?> = Result.success(Profile(
                id = "user-1",
                displayName = "Test",
                monthlySalary = 60000.0,
            ))

            override suspend fun saveProfile(draft: ProfileDraft): Result<Profile> = Result.failure(NotImplementedError())
        }

        val vm = HistoryViewModel(expenseRepo, profileRepo, categoryRepo)

        val categories = vm.state.value.categories
        assertTrue(categories.any { it.name == "Comida" })
        assertTrue(categories.any { it.name == "Transporte" })

        val amount = vm.state.value.expenses.sumOf { it.amount }
        assertEquals(4200.0, amount)
    }

    @Test
    fun testDisplaysExpenseLoadFailureEvenWhenProfileLoads() = runTest {
        val expenseRepo = object : ExpenseRepository {
            override suspend fun getExpenses(): Result<List<Expense>> = Result.failure(Exception("Error al consultar expenses"))
            override suspend fun addExpense(expense: Expense): Result<Unit> = Result.success(Unit)
            override suspend fun updateExpense(expense: Expense): Result<Unit> = Result.success(Unit)
            override suspend fun deleteExpense(expenseId: String): Result<Unit> = Result.success(Unit)
        }
        val profileRepo = object : ProfileRepository {
            override suspend fun fetchProfile(): Result<Profile?> = Result.success(
                Profile(id = "user-1", displayName = "Test", monthlySalary = 60000.0),
            )

            override suspend fun saveProfile(draft: ProfileDraft): Result<Profile> = Result.failure(NotImplementedError())
        }
        val categoryRepo = object : CategoryRepository {
            override suspend fun getCategories(): Result<List<Category>> = Result.success(emptyList())
        }

        val vm = HistoryViewModel(expenseRepo, profileRepo, categoryRepo)

        assertEquals("Error al consultar expenses", vm.state.value.error)
    }
}
