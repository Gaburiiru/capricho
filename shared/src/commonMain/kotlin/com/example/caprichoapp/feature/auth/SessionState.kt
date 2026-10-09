package com.example.caprichoapp.feature.auth

import com.example.caprichoapp.domain.model.Profile

/** Estado global de la sesión: combina "¿hay usuario?", con "¿hizo el onboarding?". */
sealed interface SessionState {
    data object Loading : SessionState
    data object SignedOut : SessionState
    data object NeedsOnboarding : SessionState
    data class Ready(val profile: Profile) : SessionState

    /** Hay sesión, pero no se pudo consultar el perfil (por ejemplo, sin internet). */
    data object ProfileError : SessionState
}
