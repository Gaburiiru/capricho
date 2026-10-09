package com.example.caprichoapp.feature.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.caprichoapp.domain.model.Profile
import com.example.caprichoapp.domain.repository.AuthRepository
import com.example.caprichoapp.domain.repository.AuthState
import com.example.caprichoapp.domain.repository.ProfileRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch

class SessionViewModel(
    private val authRepository: AuthRepository,
    private val profileRepository: ProfileRepository,
) : ViewModel() {

    private val _sessionState = MutableStateFlow<SessionState>(SessionState.Loading)
    val sessionState: StateFlow<SessionState> = _sessionState.asStateFlow()

    private var signedIn = false

    init {
        viewModelScope.launch {
            authRepository.authState
                .distinctUntilChanged() // evita recargar el perfil en cada refresco de token
                .collectLatest { auth ->
                    when (auth) {
                        is AuthState.Loading -> _sessionState.value = SessionState.Loading
                        is AuthState.SignedOut -> {
                            signedIn = false
                            _sessionState.value = SessionState.SignedOut
                        }
                        is AuthState.SignedIn -> {
                            signedIn = true
                            loadProfile()
                        }
                    }
                }
        }
    }

    fun retry() {
        if (signedIn) viewModelScope.launch { loadProfile() }
    }

    /** Lo llama el onboarding al guardar: evita volver a consultar el perfil. */
    fun onProfileSaved(profile: Profile) {
        _sessionState.value = SessionState.Ready(profile)
    }

    fun signOut() {
        viewModelScope.launch { authRepository.signOut() }
    }

    private suspend fun loadProfile() {
        _sessionState.value = SessionState.Loading
        profileRepository.fetchProfile().fold(
            onSuccess = { profile ->
                _sessionState.value =
                    if (profile == null) SessionState.NeedsOnboarding else SessionState.Ready(profile)
            },
            onFailure = { _sessionState.value = SessionState.ProfileError },
        )
    }
}
