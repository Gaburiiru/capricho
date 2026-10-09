package com.example.caprichoapp.feature.strategy

import com.example.caprichoapp.domain.model.StrategyResponse
import com.example.caprichoapp.domain.repository.StrategyRepository
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain

@OptIn(ExperimentalCoroutinesApi::class)
class StrategyViewModelTest {

    @BeforeTest
    fun setUp() = Dispatchers.setMain(UnconfinedTestDispatcher())

    @AfterTest
    fun tearDown() = Dispatchers.resetMain()

    private val sampleStrategy = StrategyResponse(
        summary = "Ahorrá un poco más por semana.",
        tips = listOf("Reduce gastos hormiga", "Ajusta tus metas"),
        estimatedSavings = "\$12.000",
    )

    private class FakeStrategyRepository(
        var hasExpenses: Result<Boolean> = Result.success(true),
        var strategy: Result<StrategyResponse>,
    ) : StrategyRepository {
        var strategyCalls = 0

        override suspend fun getSavingsStrategy(goalId: String): Result<StrategyResponse> {
            strategyCalls++
            return strategy
        }

        override suspend fun hasStoredExpenses(): Result<Boolean> = hasExpenses
    }

    @Test
    fun `loadStrategy sets the strategy returned by the repository`() = runTest {
        val repo = FakeStrategyRepository(strategy = Result.success(sampleStrategy))

        val vm = StrategyViewModel(goalId = "goal-1", repository = repo)
        vm.loadStrategy()

        val state = vm.uiState.value
        assertTrue(state is StrategyUiState.Success)
        assertEquals("Ahorrá un poco más por semana.", state.data.summary)
    }

    @Test
    fun `without stored expenses it shows the no-expenses message and does not call the API`() = runTest {
        val repo = FakeStrategyRepository(
            hasExpenses = Result.success(false),
            strategy = Result.success(sampleStrategy),
        )

        val vm = StrategyViewModel(goalId = "goal-1", repository = repo)

        val state = vm.uiState.value
        assertTrue(state is StrategyUiState.Error)
        assertEquals(StrategyViewModel.NO_EXPENSES_MESSAGE, state.message)
        assertEquals(0, repo.strategyCalls)
    }

    @Test
    fun `repository failure shows the friendly message and never the raw error`() = runTest {
        val rawError = "Illegal input: Fields [summary, tips] are required for type with serial name"
        val repo = FakeStrategyRepository(strategy = Result.failure(IllegalStateException(rawError)))

        val vm = StrategyViewModel(goalId = "goal-1", repository = repo)

        val state = vm.uiState.value
        assertTrue(state is StrategyUiState.Error)
        assertEquals(StrategyViewModel.GENERIC_ERROR_MESSAGE, state.message)
        assertFalse(state.message.contains("Fields"))
    }

    @Test
    fun `failure while checking expenses also shows the friendly message`() = runTest {
        val repo = FakeStrategyRepository(
            hasExpenses = Result.failure(RuntimeException("Unable to resolve host")),
            strategy = Result.success(sampleStrategy),
        )

        val vm = StrategyViewModel(goalId = "goal-1", repository = repo)

        val state = vm.uiState.value
        assertTrue(state is StrategyUiState.Error)
        assertEquals(StrategyViewModel.GENERIC_ERROR_MESSAGE, state.message)
    }

    @Test
    fun `retry after a failure loads the strategy`() = runTest {
        val repo = FakeStrategyRepository(strategy = Result.failure(IllegalStateException("boom")))
        val vm = StrategyViewModel(goalId = "goal-1", repository = repo)
        assertTrue(vm.uiState.value is StrategyUiState.Error)

        repo.strategy = Result.success(sampleStrategy)
        vm.loadStrategy()

        assertTrue(vm.uiState.value is StrategyUiState.Success)
    }
}
