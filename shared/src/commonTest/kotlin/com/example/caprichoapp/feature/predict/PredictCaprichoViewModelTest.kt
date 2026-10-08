package com.example.caprichoapp.feature.predict

import com.example.caprichoapp.data.repository.InMemoryGoalRepository
import com.example.caprichoapp.domain.model.Durability
import com.example.caprichoapp.domain.model.Profile
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
        val vm = PredictCaprichoViewModel(fakeProfileRepo, fakeGoalRepo)

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
        val vm = PredictCaprichoViewModel(FakeProfileRepository(), InMemoryGoalRepository())

        assertEquals(1, vm.state.value.installments)
        assertEquals(CaprichoTiming.THIS_MONTH, vm.state.value.timing)
        assertEquals(Durability.FLEETING, vm.state.value.durability)
    }

    @Test
    fun `el monto no acepta ceros a la izquierda`() = runTest {
        val vm = PredictCaprichoViewModel(FakeProfileRepository(), InMemoryGoalRepository())

        vm.onDigitInput('0')
        assertEquals("", vm.state.value.rawAmount)

        "1500".forEach { vm.onDigitInput(it) }
        assertEquals("1500", vm.state.value.rawAmount)
    }
}
