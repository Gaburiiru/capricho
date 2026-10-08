package com.example.caprichoapp.feature.goals

import com.example.caprichoapp.data.repository.InMemoryGoalRepository
import com.example.caprichoapp.domain.model.Durability
import com.example.caprichoapp.domain.model.GoalStatus
import com.example.caprichoapp.domain.model.StrategyResponse
import com.example.caprichoapp.domain.repository.StrategyRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class GoalsViewModelTest {

    @BeforeTest
    fun setUp() = Dispatchers.setMain(UnconfinedTestDispatcher())

    @AfterTest
    fun tearDown() = Dispatchers.resetMain()

    @Test
    fun testAddUpdateAndDeleteGoal() = runTest {
        val repo = InMemoryGoalRepository()
        val strategyRepo = object : StrategyRepository {
            override suspend fun getSavingsStrategy(goalId: String): Result<StrategyResponse> =
                Result.failure(NotImplementedError())

            override suspend fun hasStoredExpenses(): Result<Boolean> = Result.success(true)
        }
        val vm = GoalsViewModel(repo, strategyRepo)

        // Initially success with empty list
        val initialState = vm.uiState.value
        assertTrue(initialState is GoalsUiState.Success)
        assertEquals(0, initialState.goals.size)

        // Add a goal
        vm.addGoal("Sillón Gamer", 100000.0, 6, Durability.HIGH)

        val afterAddState = vm.uiState.value as GoalsUiState.Success
        assertEquals(1, afterAddState.goals.size)
        val goal = afterAddState.goals.first()
        assertEquals("Sillón Gamer", goal.title)
        assertEquals(100000.0, goal.targetAmount)
        assertEquals(0.0, goal.savedAmount)

        // Add savings
        vm.addSavings(goal, 50000.0)
        val afterSavingsState = vm.uiState.value as GoalsUiState.Success
        val updatedGoal = afterSavingsState.goals.first()
        assertEquals(50000.0, updatedGoal.savedAmount)
        assertEquals(GoalStatus.ACTIVE, updatedGoal.status)

        // Add savings to complete goal
        vm.addSavings(updatedGoal, 50000.0)
        val completedState = vm.uiState.value as GoalsUiState.Success
        val achievedGoal = completedState.goals.first()
        assertEquals(100000.0, achievedGoal.savedAmount)
        assertEquals(GoalStatus.ACHIEVED, achievedGoal.status)

        // Request and confirm deletion
        vm.onRequestDeleteGoal(achievedGoal)
        assertEquals(achievedGoal, vm.goalToDelete.value)

        vm.confirmDeleteGoal()

        val finalState = vm.uiState.value as GoalsUiState.Success
        assertEquals(0, finalState.goals.size)
    }

    @Test
    fun testEditGoalCapsTargetAtSavedAmount() = runTest {
        val repo = InMemoryGoalRepository()
        val strategyRepo = object : StrategyRepository {
            override suspend fun getSavingsStrategy(goalId: String): Result<StrategyResponse> =
                Result.failure(NotImplementedError())

            override suspend fun hasStoredExpenses(): Result<Boolean> = Result.success(true)
        }
        val vm = GoalsViewModel(repo, strategyRepo)

        vm.addGoal("Moto", 2_000_000.0, 6, Durability.HIGH)
        val created = (vm.uiState.value as GoalsUiState.Success).goals.first()
        vm.addSavings(created, 1_000_000.0)
        val withSavings = (vm.uiState.value as GoalsUiState.Success).goals.first()

        // Intenta bajar el objetivo a 900.000, pero ya hay 1.000.000 ahorrado
        vm.updateGoal(withSavings, "Moto", 900_000.0, 6, Durability.HIGH)

        val edited = (vm.uiState.value as GoalsUiState.Success).goals.first()
        assertEquals(1_000_000.0, edited.targetAmount)
        assertEquals(GoalStatus.ACHIEVED, edited.status)
        assertEquals(1, (vm.uiState.value as GoalsUiState.Success).achieved.size)
        assertEquals(0, (vm.uiState.value as GoalsUiState.Success).inProgress.size)
    }

    @Test
    fun testAchievedGoalCannotBeEdited() = runTest {
        val repo = InMemoryGoalRepository()
        val strategyRepo = object : StrategyRepository {
            override suspend fun getSavingsStrategy(goalId: String): Result<StrategyResponse> =
                Result.failure(NotImplementedError())

            override suspend fun hasStoredExpenses(): Result<Boolean> = Result.success(true)
        }
        val vm = GoalsViewModel(repo, strategyRepo)

        vm.addGoal("Moto", 100_000.0, 6, Durability.HIGH)
        val created = (vm.uiState.value as GoalsUiState.Success).goals.first()
        vm.addSavings(created, 100_000.0)
        val achieved = (vm.uiState.value as GoalsUiState.Success).goals.first()

        vm.onEditGoalClick(achieved)
        assertEquals(null, vm.goalToEdit.value)

        vm.updateGoal(achieved, "Otra", 500_000.0, 1, Durability.FLEETING)
        val after = (vm.uiState.value as GoalsUiState.Success).goals.first()
        assertEquals("Moto", after.title)
        assertEquals(100_000.0, after.targetAmount)
    }
}
