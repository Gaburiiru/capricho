package com.example.caprichoapp.testutil

import com.example.caprichoapp.domain.model.Category
import com.example.caprichoapp.domain.model.Expense
import com.example.caprichoapp.domain.model.Profile
import com.example.caprichoapp.domain.model.ProfileDraft
import com.example.caprichoapp.domain.repository.AuthRepository
import com.example.caprichoapp.domain.repository.AuthState
import com.example.caprichoapp.domain.repository.CategoryRepository
import com.example.caprichoapp.domain.repository.ExpenseRepository
import com.example.caprichoapp.domain.repository.ProfileRepository
import com.example.caprichoapp.domain.repository.SignUpOutcome
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

    /** Si no es null, el alta y el ingreso con correo fallan con este error. */
    var failWith: Throwable? = null
    var signUpOutcome = SignUpOutcome.SIGNED_IN
    val signUps = mutableListOf<Pair<String, String>>()
    val signIns = mutableListOf<Pair<String, String>>()

    override suspend fun signUpWithEmail(email: String, password: String): Result<SignUpOutcome> {
        failWith?.let { return Result.failure(it) }
        signUps += email to password
        if (signUpOutcome == SignUpOutcome.SIGNED_IN) state.value = AuthState.SignedIn("u1")
        return Result.success(signUpOutcome)
    }

    override suspend fun signInWithEmail(email: String, password: String): Result<Unit> {
        failWith?.let { return Result.failure(it) }
        signIns += email to password
        state.value = AuthState.SignedIn("u1")
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

class FakeExpenseRepository(var failWith: String? = null) : ExpenseRepository {
    val added = mutableListOf<Expense>()

    override suspend fun getExpenses(): Result<List<Expense>> = Result.success(added.toList())

    override suspend fun addExpense(expense: Expense): Result<Unit> {
        failWith?.let { return Result.failure(Exception(it)) }
        added += expense
        return Result.success(Unit)
    }

    override suspend fun updateExpense(expense: Expense): Result<Unit> = Result.success(Unit)

    override suspend fun deleteExpense(expenseId: String): Result<Unit> = Result.success(Unit)
}

class FakeCategoryRepository(
    private val categories: List<Category> = listOf(Category(id = "c1", name = "Ropa"), Category(id = "c2", name = "Otros")),
) : CategoryRepository {
    override suspend fun getCategories(): Result<List<Category>> = Result.success(categories)
}
