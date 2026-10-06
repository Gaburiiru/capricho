package com.example.caprichoapp.data.repository

import com.example.caprichoapp.core.util.runCatchingCancellable
import com.example.caprichoapp.domain.repository.AuthRepository
import com.example.caprichoapp.domain.repository.AuthState
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.auth.status.SessionStatus
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class SupabaseAuthRepository(
    private val supabase: SupabaseClient,
) : AuthRepository {

    override val authState: Flow<AuthState> =
        supabase.auth.sessionStatus.map { status ->
            when (status) {
                is SessionStatus.Initializing -> AuthState.Loading
                is SessionStatus.Authenticated ->
                    AuthState.SignedIn(status.session.user?.id.orEmpty())
                is SessionStatus.NotAuthenticated -> AuthState.SignedOut
                // Sin red al refrescar: si todavía hay usuario, no lo echamos
                is SessionStatus.RefreshFailure ->
                    supabase.auth.currentUserOrNull()
                        ?.let { AuthState.SignedIn(it.id) }
                        ?: AuthState.SignedOut
            }
        }

    override suspend fun signInAnonymously(): Result<Unit> =
        runCatchingCancellable { supabase.auth.signInAnonymously(); Unit }

    override suspend fun signOut(): Result<Unit> =
        runCatchingCancellable { supabase.auth.signOut() }
}