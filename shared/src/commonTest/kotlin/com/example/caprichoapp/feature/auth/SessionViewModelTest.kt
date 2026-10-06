package com.example.caprichoapp.feature.auth

import com.example.caprichoapp.domain.model.Profile
import com.example.caprichoapp.domain.repository.AuthState
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

@OptIn(ExperimentalCoroutinesApi::class)
class SessionViewModelTest {

    private val profile = Profile(id = "u1", displayName = "Lucía", monthlySalary = 1_700_000.0)

    @BeforeTest
    fun setUp() = Dispatchers.setMain(UnconfinedTestDispatcher())

    @AfterTest
    fun tearDown() = Dispatchers.resetMain()

    @Test
    fun `sin sesion el estado es SignedOut`() = runTest {
        val vm = SessionViewModel(FakeAuthRepository(AuthState.SignedOut), FakeProfileRepository())
        assertEquals(SessionState.SignedOut, vm.sessionState.value)
    }

    @Test
    fun `con sesion y sin perfil pide onboarding`() = runTest {
        val vm = SessionViewModel(
            FakeAuthRepository(AuthState.SignedIn("u1")),
            FakeProfileRepository(stored = null),
        )
        assertEquals(SessionState.NeedsOnboarding, vm.sessionState.value)
    }

    @Test
    fun `con sesion y con perfil queda listo`() = runTest {
        val vm = SessionViewModel(
            FakeAuthRepository(AuthState.SignedIn("u1")),
            FakeProfileRepository(stored = profile),
        )
        assertEquals(SessionState.Ready(profile), vm.sessionState.value)
    }

    @Test
    fun `si falla la carga del perfil no se manda al onboarding y se puede reintentar`() = runTest {
        val repo = FakeProfileRepository(stored = profile, failFetch = true)
        val vm = SessionViewModel(FakeAuthRepository(AuthState.SignedIn("u1")), repo)
        assertEquals(SessionState.ProfileError, vm.sessionState.value)

        repo.failFetch = false
        vm.retry()

        assertEquals(SessionState.Ready(profile), vm.sessionState.value)
    }

    @Test
    fun `al guardar el onboarding pasa a Ready`() = runTest {
        val vm = SessionViewModel(
            FakeAuthRepository(AuthState.SignedIn("u1")),
            FakeProfileRepository(stored = null),
        )

        vm.onProfileSaved(profile)

        assertEquals(SessionState.Ready(profile), vm.sessionState.value)
    }

    @Test
    fun `al cerrar sesion vuelve a SignedOut`() = runTest {
        val auth = FakeAuthRepository(AuthState.SignedIn("u1"))
        val vm = SessionViewModel(auth, FakeProfileRepository(stored = profile))

        vm.signOut()

        assertEquals(SessionState.SignedOut, vm.sessionState.value)
    }
}
