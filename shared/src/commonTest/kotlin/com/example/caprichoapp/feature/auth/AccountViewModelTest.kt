package com.example.caprichoapp.feature.auth

import com.example.caprichoapp.domain.repository.AuthState
import com.example.caprichoapp.domain.repository.SignUpOutcome
import com.example.caprichoapp.domain.validation.EmailError
import com.example.caprichoapp.domain.validation.PasswordError
import com.example.caprichoapp.testutil.FakeAuthRepository
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
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class AccountViewModelTest {

    @BeforeTest
    fun setUp() = Dispatchers.setMain(UnconfinedTestDispatcher())

    @AfterTest
    fun tearDown() = Dispatchers.resetMain()

    private fun AccountViewModel.fillSignUp(salary: String = "1500000") {
        onNameChange("Lucía")
        onNicknameChange("Lu")
        onSalaryChange(salary)
        onEmailChange("  Lucia@Mail.com ")
        onPasswordChange("secreto1")
    }

    @Test
    fun `crear cuenta guarda el perfil y avisa para seguir al home`() = runTest {
        val auth = FakeAuthRepository()
        val profiles = FakeProfileRepository()
        val vm = AccountViewModel(auth, profiles)

        vm.fillSignUp()
        vm.onSubmit()

        assertEquals(listOf("lucia@mail.com" to "secreto1"), auth.signUps)
        assertEquals(1, profiles.saveCalls)
        assertEquals("Lucía", profiles.lastDraft?.displayName)
        assertEquals("Lu", profiles.lastDraft?.nickname)
        assertEquals(1_500_000.0, profiles.lastDraft?.monthlySalary)
        assertNotNull(vm.uiState.value.savedProfile)
        assertFalse(vm.uiState.value.isSubmitting)
    }

    @Test
    fun `sin datos validos no se crea la cuenta`() = runTest {
        val auth = FakeAuthRepository()
        val vm = AccountViewModel(auth, FakeProfileRepository())

        vm.onEmailChange("mal")
        vm.onPasswordChange("123")
        vm.onSubmit()

        val state = vm.uiState.value
        assertNotNull(state.nameError)
        assertNotNull(state.salaryError)
        assertEquals(EmailError.INVALID, state.emailError)
        assertEquals(PasswordError.TOO_SHORT, state.passwordError)
        assertTrue(auth.signUps.isEmpty())
    }

    @Test
    fun `un sueldo muy bajo pide confirmacion antes de crear la cuenta`() = runTest {
        val auth = FakeAuthRepository()
        val vm = AccountViewModel(auth, FakeProfileRepository())

        vm.fillSignUp(salary = "5000")
        vm.onSubmit()
        assertEquals(5000.0, vm.uiState.value.lowSalaryToConfirm)
        assertTrue(auth.signUps.isEmpty())

        vm.onConfirmLowSalary()
        assertEquals(1, auth.signUps.size)
        assertNull(vm.uiState.value.lowSalaryToConfirm)
    }

    @Test
    fun `correo ya registrado muestra un mensaje claro`() = runTest {
        val auth = FakeAuthRepository().apply { failWith = Exception("User already registered") }
        val vm = AccountViewModel(auth, FakeProfileRepository())

        vm.fillSignUp()
        vm.onSubmit()

        assertEquals("Ese correo ya tiene una cuenta. Ingresá con tu contraseña.", vm.uiState.value.errorMessage)
        assertNull(vm.uiState.value.savedProfile)
        assertFalse(vm.uiState.value.isSubmitting)
    }

    @Test
    fun `si Supabase pide confirmar el correo pasa a ingresar con un aviso`() = runTest {
        val auth = FakeAuthRepository().apply { signUpOutcome = SignUpOutcome.EMAIL_CONFIRMATION_REQUIRED }
        val profiles = FakeProfileRepository()
        val vm = AccountViewModel(auth, profiles)

        vm.fillSignUp()
        vm.onSubmit()

        assertEquals(AccountMode.SIGN_IN, vm.uiState.value.mode)
        assertNotNull(vm.uiState.value.infoMessage)
        assertEquals("", vm.uiState.value.password)
        assertEquals(0, profiles.saveCalls)
    }

    @Test
    fun `si falla guardar el perfil se cierra la sesion y se invita a ingresar`() = runTest {
        val auth = FakeAuthRepository()
        val vm = AccountViewModel(auth, FakeProfileRepository(failSave = true))

        vm.fillSignUp()
        vm.onSubmit()

        assertEquals(AuthState.SignedOut, auth.state.value)
        assertEquals(AccountMode.SIGN_IN, vm.uiState.value.mode)
        assertNotNull(vm.uiState.value.errorMessage)
        assertNull(vm.uiState.value.savedProfile)
    }

    @Test
    fun `ingresar usa solo correo y contrasena`() = runTest {
        val auth = FakeAuthRepository()
        val vm = AccountViewModel(auth, FakeProfileRepository())

        vm.onModeChange(AccountMode.SIGN_IN)
        vm.onEmailChange("Lucia@Mail.com")
        vm.onPasswordChange("secreto1")
        vm.onSubmit()

        assertEquals(listOf("lucia@mail.com" to "secreto1"), auth.signIns)
        assertEquals(AuthState.SignedIn("u1"), auth.state.value)
    }

    @Test
    fun `credenciales incorrectas muestran su mensaje`() = runTest {
        val auth = FakeAuthRepository().apply { failWith = Exception("Invalid login credentials") }
        val vm = AccountViewModel(auth, FakeProfileRepository())

        vm.onModeChange(AccountMode.SIGN_IN)
        vm.onEmailChange("lucia@mail.com")
        vm.onPasswordChange("mala")
        vm.onSubmit()

        assertEquals("Correo o contraseña incorrectos.", vm.uiState.value.errorMessage)
    }

    @Test
    fun `ingresar sin contrasena marca el error`() = runTest {
        val auth = FakeAuthRepository()
        val vm = AccountViewModel(auth, FakeProfileRepository())

        vm.onModeChange(AccountMode.SIGN_IN)
        vm.onEmailChange("lucia@mail.com")
        vm.onSubmit()

        assertEquals(PasswordError.EMPTY, vm.uiState.value.passwordError)
        assertTrue(auth.signIns.isEmpty())
    }
}
