package com.example.caprichoapp.feature.onboarding

import com.example.caprichoapp.domain.validation.SalaryError
import com.example.caprichoapp.domain.validation.TextError
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
import kotlin.test.assertNull

@OptIn(ExperimentalCoroutinesApi::class)
class OnboardingViewModelTest {

    @BeforeTest
    fun setUp() = Dispatchers.setMain(UnconfinedTestDispatcher())

    @AfterTest
    fun tearDown() = Dispatchers.resetMain()

    @Test
    fun `con campos vacios muestra errores y no guarda`() = runTest {
        val repo = FakeProfileRepository()
        val vm = OnboardingViewModel(repo)

        vm.onSave()

        val state = vm.uiState.value
        assertEquals(TextError.BLANK, state.nameError)
        assertEquals(SalaryError.INVALID, state.salaryError)
        assertEquals(0, repo.saveCalls)
    }

    @Test
    fun `sueldo cero no se guarda`() = runTest {
        val repo = FakeProfileRepository()
        val vm = OnboardingViewModel(repo)

        vm.onNameChange("Lucía")
        vm.onSalaryChange("0")
        vm.onSave()

        assertEquals(SalaryError.NOT_POSITIVE, vm.uiState.value.salaryError)
        assertEquals(0, repo.saveCalls)
    }

    @Test
    fun `el sueldo solo acepta digitos`() = runTest {
        val vm = OnboardingViewModel(FakeProfileRepository())

        vm.onSalaryChange("1.7a,00")

        assertEquals("1700", vm.uiState.value.salary)
    }

    @Test
    fun `datos validos guardan el perfil normalizado`() = runTest {
        val repo = FakeProfileRepository()
        val vm = OnboardingViewModel(repo)

        vm.onNameChange("  Lucía  ")
        vm.onNicknameChange("   ")
        vm.onSalaryChange("1700000")
        vm.onSave()

        val saved = vm.uiState.value.savedProfile
        assertNotNull(saved)
        assertEquals("Lucía", repo.lastDraft?.displayName)
        assertNull(repo.lastDraft?.nickname) // apodo en blanco -> null
        assertEquals(1_700_000.0, repo.lastDraft?.monthlySalary)
        assertFalse(vm.uiState.value.isSaving)
    }

    @Test
    fun `con apodo la interfaz usa el apodo`() = runTest {
        val vm = OnboardingViewModel(FakeProfileRepository())

        vm.onNameChange("Lucía Gómez")
        vm.onNicknameChange("Lu")
        vm.onSalaryChange("1700000")
        vm.onSave()

        assertEquals("Lu", vm.uiState.value.savedProfile?.shownName)
    }

    @Test
    fun `si falla el guardado muestra error y permite reintentar`() = runTest {
        val repo = FakeProfileRepository(failSave = true)
        val vm = OnboardingViewModel(repo)

        vm.onNameChange("Lucía")
        vm.onSalaryChange("1700000")
        vm.onSave()

        assertNotNull(vm.uiState.value.saveError)
        assertNull(vm.uiState.value.savedProfile)
        assertFalse(vm.uiState.value.isSaving)

        repo.failSave = false
        vm.onSave()

        assertNull(vm.uiState.value.saveError)
        assertNotNull(vm.uiState.value.savedProfile)
    }

    @Test
    fun `sueldo bajo pide confirmacion y no guarda hasta confirmar`() = runTest {
        val repo = FakeProfileRepository()
        val vm = OnboardingViewModel(repo)
        vm.onNameChange("Lucía")
        vm.onSalaryChange("9000")

        vm.onSave()

        assertEquals(9_000.0, vm.uiState.value.lowSalaryToConfirm)
        assertEquals(0, repo.saveCalls)

        vm.onConfirmLowSalary()

        assertEquals(null, vm.uiState.value.lowSalaryToConfirm)
        assertEquals(1, repo.saveCalls)
        assertEquals(9_000.0, repo.lastDraft?.monthlySalary)
    }

    @Test
    fun `cancelar la confirmacion no guarda y conserva lo escrito`() = runTest {
        val repo = FakeProfileRepository()
        val vm = OnboardingViewModel(repo)
        vm.onNameChange("Lucía")
        vm.onSalaryChange("9000")
        vm.onSave()

        vm.onDismissLowSalary()

        assertEquals(null, vm.uiState.value.lowSalaryToConfirm)
        assertEquals("9000", vm.uiState.value.salary)
        assertEquals(0, repo.saveCalls)
    }

    @Test
    fun `sueldo normal guarda directo sin pedir confirmacion`() = runTest {
        val repo = FakeProfileRepository()
        val vm = OnboardingViewModel(repo)
        vm.onNameChange("Lucía")
        vm.onSalaryChange("10000")

        vm.onSave()

        assertEquals(null, vm.uiState.value.lowSalaryToConfirm)
        assertEquals(1, repo.saveCalls)
    }
}
