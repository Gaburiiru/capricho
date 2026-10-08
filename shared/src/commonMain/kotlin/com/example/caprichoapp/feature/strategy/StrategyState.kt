package com.example.caprichoapp.feature.strategy

import com.example.caprichoapp.domain.model.StrategyResponse

sealed interface StrategyUiState {
    data object Loading : StrategyUiState
    data class Success(val data: StrategyResponse) : StrategyUiState
    data class Error(val message: String) : StrategyUiState
}
