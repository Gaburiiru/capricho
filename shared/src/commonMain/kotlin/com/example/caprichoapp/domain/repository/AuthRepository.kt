package com.example.caprichoapp.domain.repository

import kotlinx.coroutines.flow.Flow

sealed interface AuthState {
    data object Loading : AuthState
    data class SignedIn(val userId: String) : AuthState
    data object SignedOut : AuthState
}

/** Qué pasó al crear una cuenta con correo. */
enum class SignUpOutcome {
    /** La cuenta quedó creada y con sesión iniciada. */
    SIGNED_IN,

    /** La cuenta se creó, pero Supabase pide confirmar el correo antes de dejar entrar. */
    EMAIL_CONFIRMATION_REQUIRED,
}

interface AuthRepository {
    val authState: Flow<AuthState>

    /** Sesión temporal: sin correo ni contraseña, no se puede recuperar al cerrarla. */
    suspend fun signInAnonymously(): Result<Unit>
    suspend fun signUpWithEmail(email: String, password: String): Result<SignUpOutcome>
    suspend fun signInWithEmail(email: String, password: String): Result<Unit>
    suspend fun signOut(): Result<Unit>
}