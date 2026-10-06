package com.example.caprichoapp.testutil

import com.example.caprichoapp.domain.model.Profile
import com.example.caprichoapp.domain.model.ProfileDraft
import com.example.caprichoapp.domain.repository.AuthRepository
import com.example.caprichoapp.domain.repository.AuthState
import com.example.caprichoapp.domain.repository.ProfileRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow

class FakeAuthRepository(initial: AuthState = AuthState.SignedOut) : AuthRepository {
    val state = MutableStateFlow(initial)
    override val authState: Flow<AuthState> = state

    override suspend fun signInAnonymously(): Result<Unit> {
        state.value = AuthState.SignedIn("u1")
        return Result.success(Unit)
    }

    override suspend fun signOut(): Result<Unit> {
        state.value = AuthState.SignedOut
        return Result.success(Unit)
    }
}

class FakeProfileRepository(
    var stored: Profile? = null,
    var failFetch: Boolean = false,
    var failSave: Boolean = false,
) : ProfileRepository {
    var saveCalls = 0
        private set
    var lastDraft: ProfileDraft? = null
        private set

    override suspend fun fetchProfile(): Result<Profile?> =
        if (failFetch) Result.failure(Exception("sin red")) else Result.success(stored)

    override suspend fun saveProfile(draft: ProfileDraft): Result<Profile> {
        saveCalls++
        lastDraft = draft
        if (failSave) return Result.failure(Exception("sin red"))
        val profile = Profile(
            id = "u1",
            displayName = draft.displayName,
            monthlySalary = draft.monthlySalary,
            nickname = draft.nickname,
        )
        stored = profile
        return Result.success(profile)
    }
}
