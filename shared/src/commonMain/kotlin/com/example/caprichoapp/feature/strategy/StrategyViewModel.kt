package com.example.caprichoapp.feature.strategy

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.caprichoapp.domain.repository.StrategyRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class StrategyViewModel(
    private val goalId: String,
    private val repository: StrategyRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow<StrategyUiState>(StrategyUiState.Loading)
    val uiState: StateFlow<StrategyUiState> = _uiState.asStateFlow()

    init {
        loadStrategy()
    }

    fun loadStrategy() {
        viewModelScope.launch {
            _uiState.value = StrategyUiState.Loading

            repository.hasStoredExpenses()
                .onSuccess { hasExpenses ->
                    if (!hasExpenses) {
                        _uiState.value = StrategyUiState.Error(NO_EXPENSES_MESSAGE)
                        return@onSuccess
                    }

                    repository.getSavingsStrategy(goalId)
                        .onSuccess { strategy ->
                            _uiState.value = StrategyUiState.Success(strategy)
                        }
                        .onFailure { error ->
                            logFailure("getSavingsStrategy", error)
                            _uiState.value = StrategyUiState.Error(GENERIC_ERROR_MESSAGE)
                        }
                }
                .onFailure { error ->
                    logFailure("hasStoredExpenses", error)
                    _uiState.value = StrategyUiState.Error(GENERIC_ERROR_MESSAGE)
                }
        }
    }

    // El detalle técnico va solo al log; al usuario nunca le mostramos mensajes crudos
    // (serialización, red, etc.).
    private fun logFailure(step: String, error: Throwable) {
        println("StrategyViewModel: falló $step -> ${error::class.simpleName}: ${error.message}")
    }

    companion object {
        const val GENERIC_ERROR_MESSAGE = "Hubo un problema al generar tu estrategia. Volvé a intentarlo más tarde."
        const val NO_EXPENSES_MESSAGE = "Necesitás cargar al menos un gasto para generar una estrategia."
    }
}
