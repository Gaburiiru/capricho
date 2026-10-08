package com.example.caprichoapp.feature.history

import com.example.caprichoapp.domain.model.Category
import com.example.caprichoapp.domain.model.Expense
import com.example.caprichoapp.domain.repository.CategoryRepository
import com.example.caprichoapp.domain.repository.ExpenseRepository
import com.example.caprichoapp.testutil.FakeProfileRepository
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
import kotlin.test.assertFalse
import kotlin.test.assertNull

@OptIn(ExperimentalCoroutinesApi::class)
class HistoryViewModelSaveTest {

    private class FakeExpenseRepository : ExpenseRepository {
        val stored = mutableListOf<Expense>()
        var failWith: String? = null
        var addCalls = 0

        override suspend fun getExpenses(): Result<List<Expense>> = Result.success(stored.toList())

        override suspend fun addExpense(expense: Expense): Result<Unit> {
            addCalls++
            failWith?.let { return Result.failure(Exception(it)) }
            stored += expense.copy(id = "id-${stored.size + 1}")
            return Result.success(Unit)
        }

        override suspend fun updateExpense(expense: Expense): Result<Unit> = Result.success(Unit)
        override suspend fun deleteExpense(expenseId: String): Result<Unit> = Result.success(Unit)
    }

    private val categoryRepo = object : CategoryRepository {
        override suspend fun getCategories(): Result<List<Category>> = Result.success(emptyList())
    }

    private val newExpense = Expense(title = "Super", amount = 4200.0, spentAt = LocalDate(2026, 10, 7))

    @BeforeTest
    fun setUp() = Dispatchers.setMain(UnconfinedTestDispatcher())

    @AfterTest
    fun tearDown() = Dispatchers.resetMain()

    private fun viewModel(repo: FakeExpenseRepository) =
        HistoryViewModel(repo, FakeProfileRepository(), categoryRepo)

    @Test
    fun `al guardar un gasto se avisa a la pantalla y se recarga la lista`() = runTest {
        val repo = FakeExpenseRepository()
        val vm = viewModel(repo)

        vm.addExpense(newExpense)

        val state = vm.state.value
        assertEquals(1, state.savedCount)
        assertFalse(state.isSaving)
        assertNull(state.saveError)
        assertEquals(1, state.expenses.size)
        assertEquals(4200.0, state.expenses.first().amount)
    }

    @Test
    fun `si falla el guardado muestra el motivo y no cierra el dialogo`() = runTest {
        val repo = FakeExpenseRepository().apply {
            failWith = "null value in column \"durability\" violates not-null constraint"
        }
        val vm = viewModel(repo)

        vm.addExpense(newExpense)

        val state = vm.state.value
        assertEquals(0, state.savedCount) // la pantalla no cierra el diálogo
        assertFalse(state.isSaving)
        assertEquals("null value in column \"durability\" violates not-null constraint", state.saveError)
        assertEquals(0, state.expenses.size)
    }

    @Test
    fun `tras un fallo se puede reintentar y el error se limpia`() = runTest {
        val repo = FakeExpenseRepository().apply { failWith = "sin red" }
        val vm = viewModel(repo)

        vm.addExpense(newExpense)
        repo.failWith = null
        vm.addExpense(newExpense)

        val state = vm.state.value
        assertNull(state.saveError)
        assertEquals(1, state.savedCount)
        assertEquals(2, repo.addCalls)
    }

    @Test
    fun `clearSaveError borra el mensaje`() = runTest {
        val repo = FakeExpenseRepository().apply { failWith = "sin red" }
        val vm = viewModel(repo)
        vm.addExpense(newExpense)

        vm.clearSaveError()

        assertNull(vm.state.value.saveError)
    }
}
