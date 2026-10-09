package com.example.caprichoapp.feature.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.caprichoapp.domain.repository.AuthRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class LoginUiState(
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
)

class LoginViewModel(
    private val authRepository: AuthRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(LoginUiState())
    val uiState: StateFlow<LoginUiState> = _uiState.asStateFlow()

    fun onSkip() {
        _uiState.update { LoginUiState(isLoading = true) }
        viewModelScope.launch {
            authRepository.signInAnonymously().onFailure {
                _uiState.update {
                    LoginUiState(
                        errorMessage = "No pudimos entrar. Revisá tu conexión e intentá de nuevo.",
                    )
                }
            }
            // Si sale bien, authState cambia y el NavHost navega solo
        }
    }
}