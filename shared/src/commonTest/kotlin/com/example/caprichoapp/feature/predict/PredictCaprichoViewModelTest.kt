package com.example.caprichoapp.feature.predict

import com.example.caprichoapp.data.repository.InMemoryGoalRepository
import com.example.caprichoapp.domain.model.Durability
import com.example.caprichoapp.domain.model.Goal
import com.example.caprichoapp.domain.model.Profile
import com.example.caprichoapp.domain.model.Category
import com.example.caprichoapp.domain.model.ExpenseKind
import com.example.caprichoapp.testutil.FakeCategoryRepository
import com.example.caprichoapp.testutil.FakeExpenseRepository
import com.example.caprichoapp.testutil.FakeProfileRepository
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
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class PredictCaprichoViewModelTest {

    @BeforeTest
    fun setUp() = Dispatchers.setMain(UnconfinedTestDispatcher())

    @AfterTest
    fun tearDown() = Dispatchers.resetMain()

    @Test
    fun testPredictCaprichoFlowStepsAndDiagnosis() = runTest {
        val fakeProfileRepo = FakeProfileRepository(
            stored = Profile("user1", "Gabo", 1_000_000.0, "Gabo")
        )
        val fakeGoalRepo = InMemoryGoalRepository()
        val vm = PredictCaprichoViewModel(fakeProfileRepo, fakeGoalRepo, FakeExpenseRepository(), FakeCategoryRepository())

        // Step 1: Input amount "50000"
        assertEquals(1, vm.state.value.step)
        "50000".forEach { vm.onDigitInput(it) }
        assertEquals("50000", vm.state.value.rawAmount)
        assertEquals(50000.0, vm.state.value.amount)
        assertTrue(vm.state.value.isStepValid)

        // Advance to Step 2
        vm.onNextStep()
        assertEquals(2, vm.state.value.step)

        // Step 2: Select 3 Installments
        vm.onInstallmentsSelected(3)
        assertEquals(3, vm.state.value.installments)

        // Advance to Step 3
        vm.onNextStep()
        assertEquals(3, vm.state.value.step)

        // Step 3: Select THIS_MONTH
        vm.onTimingSelected(CaprichoTiming.THIS_MONTH)
        assertEquals(CaprichoTiming.THIS_MONTH, vm.state.value.timing)

        // Advance to Step 4
        vm.onNextStep()
        assertEquals(4, vm.state.value.step)

        // Step 4: Select High Durability
        vm.onDurabilitySelected(Durability.HIGH)
        assertEquals(Durability.HIGH, vm.state.value.durability)

        // Advance to Step 5 (Diagnosis)
        vm.onNextStep()
        assertEquals(5, vm.state.value.step)

        val diagnosis = vm.state.value.diagnosis
        assertNotNull(diagnosis)
        assertEquals(RecommendationVerdict.GREAT_CAPRICHO, diagnosis.verdict)
        assertEquals(16666.666666666668, diagnosis.monthlyPayment, 0.1)

        // Test save as goal
        vm.saveAsGoal("Zapatillas Pro")
        assertTrue(vm.state.value.isGoalSaved)
        val goals = fakeGoalRepo.getGoals().getOrNull()
        assertNotNull(goals)
        assertEquals(1, goals.size)
        assertEquals("Zapatillas Pro", goals.first().title)
        assertEquals(50000.0, goals.first().targetAmount)
    }

    @Test
    fun `por defecto viene elegido este mes, fugaz y contado`() = runTest {
        val vm = PredictCaprichoViewModel(FakeProfileRepository(), InMemoryGoalRepository(), FakeExpenseRepository(), FakeCategoryRepository())

        assertEquals(1, vm.state.value.installments)
        assertEquals(CaprichoTiming.THIS_MONTH, vm.state.value.timing)
        assertEquals(Durability.FLEETING, vm.state.value.durability)
    }

    @Test
    fun `el monto no acepta ceros a la izquierda`() = runTest {
        val vm = PredictCaprichoViewModel(FakeProfileRepository(), InMemoryGoalRepository(), FakeExpenseRepository(), FakeCategoryRepository())

        vm.onDigitInput('0')
        assertEquals("", vm.state.value.rawAmount)

        "1500".forEach { vm.onDigitInput(it) }
        assertEquals("1500", vm.state.value.rawAmount)
    }

    @Test
    fun `el diagnostico trae todas las metas ordenadas de la mas afectada a la menos`() = runTest {
        val goals = InMemoryGoalRepository()
        // Con un capricho de 100.000: moto 10% del total, auto 2%, bici 25%
        goals.addGoal(Goal(id = "auto", title = "Auto", targetAmount = 5_000_000.0, durability = Durability.HIGH))
        goals.addGoal(Goal(id = "bici", title = "Bici", targetAmount = 400_000.0, durability = Durability.HIGH))
        goals.addGoal(Goal(id = "moto", title = "Moto", targetAmount = 1_000_000.0, durability = Durability.HIGH))
        val vm = PredictCaprichoViewModel(FakeProfileRepository(), goals, FakeExpenseRepository(), FakeCategoryRepository())

        "100000".forEach { vm.onDigitInput(it) }
        repeat(4) { vm.onNextStep() }

        val ids = vm.state.value.diagnosis!!.goalImpacts.map { it.goalId }
        assertEquals(listOf("bici", "moto", "auto"), ids)
    }

    private fun viewModelWith(expenses: FakeExpenseRepository, goals: InMemoryGoalRepository = InMemoryGoalRepository()) =
        PredictCaprichoViewModel(FakeProfileRepository(), goals, expenses, FakeCategoryRepository())

    @Test
    fun `al contado se puede guardar como gasto con nombre y categoria`() = runTest {
        val expenses = FakeExpenseRepository()
        val vm = viewModelWith(expenses)
        "240000".forEach { vm.onDigitInput(it) }
        vm.onDurabilitySelected(Durability.HIGH)
        repeat(4) { vm.onNextStep() }

        assertTrue(vm.state.value.canSaveAsExpense)
        vm.saveAsExpense("  Zapatillas ", Category(id = "c1", name = "Ropa"))

        assertTrue(vm.state.value.isExpenseSaved)
        val saved = expenses.added.single()
        assertEquals("Zapatillas", saved.title)
        assertEquals(240000.0, saved.amount)
        assertEquals(1, saved.installments)
        assertEquals("c1", saved.categoryId)
        assertEquals("Ropa", saved.categoryName)
        assertEquals(Durability.HIGH, saved.durability)
        assertEquals(ExpenseKind.ONE_OFF, saved.kind)
    }

    @Test
    fun `en cuotas no se puede guardar como gasto`() = runTest {
        val expenses = FakeExpenseRepository()
        val vm = viewModelWith(expenses)
        "240000".forEach { vm.onDigitInput(it) }
        vm.onInstallmentsSelected(3)
        repeat(4) { vm.onNextStep() }

        assertFalse(vm.state.value.canSaveAsExpense)
        vm.showSaveExpenseDialog(true)
        assertFalse(vm.state.value.showSaveExpenseDialog)
        vm.saveAsExpense("Zapatillas", Category(name = "Ropa"))

        assertFalse(vm.state.value.isExpenseSaved)
        assertTrue(expenses.added.isEmpty())
    }

    @Test
    fun `si falla guardar el gasto queda el error y se puede reintentar`() = runTest {
        val expenses = FakeExpenseRepository(failWith = "sin red")
        val vm = viewModelWith(expenses)
        "1000".forEach { vm.onDigitInput(it) }
        repeat(4) { vm.onNextStep() }

        vm.saveAsExpense("Café", Category(name = "Comida"))
        assertFalse(vm.state.value.isExpenseSaved)
        assertEquals("sin red", vm.state.value.expenseSaveError)

        expenses.failWith = null
        vm.saveAsExpense("Café", Category(name = "Comida"))
        assertTrue(vm.state.value.isExpenseSaved)
    }

    @Test
    fun `meta y gasto son excluyentes`() = runTest {
        val expenses = FakeExpenseRepository()
        val goals = InMemoryGoalRepository()
        val vm = viewModelWith(expenses, goals)
        "5000".forEach { vm.onDigitInput(it) }
        repeat(4) { vm.onNextStep() }

        vm.saveAsGoal("Meta")
        vm.saveAsExpense("Gasto", Category(name = "Otros"))
        assertTrue(expenses.added.isEmpty())

        val vm2 = viewModelWith(FakeExpenseRepository(), InMemoryGoalRepository().also { })
        "5000".forEach { vm2.onDigitInput(it) }
        repeat(4) { vm2.onNextStep() }
        vm2.saveAsExpense("Gasto", Category(name = "Otros"))
        vm2.saveAsGoal("Meta")
        assertFalse(vm2.state.value.isGoalSaved)
    }
}
