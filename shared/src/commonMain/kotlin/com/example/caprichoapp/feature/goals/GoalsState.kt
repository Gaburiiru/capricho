package com.example.caprichoapp.feature.goals

import com.example.caprichoapp.domain.model.Goal

sealed interface GoalsUiState {
    data object Loading : GoalsUiState
    data class Success(val goals: List<Goal>) : GoalsUiState {
        /** Metas que todavía no se alcanzaron: van arriba de todo. */
        val inProgress: List<Goal> = goals.filter { !it.isAchieved }

        /** Metas ya cumplidas: van abajo, en su propia sección. */
        val achieved: List<Goal> = goals.filter { it.isAchieved }
    }
    data class Error(val message: String) : GoalsUiState
}
