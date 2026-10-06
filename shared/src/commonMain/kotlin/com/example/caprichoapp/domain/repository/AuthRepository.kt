package com.example.caprichoapp.domain.repository

import kotlinx.coroutines.flow.Flow

sealed interface AuthState {
    data object Loading : AuthState
    data class SignedIn(val userId: String) : AuthState
    data object SignedOut : AuthState
}

interface AuthRepository {
    val authState: Flow<AuthState>
    suspend fun signInAnonymously(): Result<Unit>
    suspend fun signOut(): Result<Unit>
}