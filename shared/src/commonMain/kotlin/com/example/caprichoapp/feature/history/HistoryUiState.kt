package com.example.caprichoapp.feature.history

import com.example.caprichoapp.domain.model.Category
import com.example.caprichoapp.domain.model.Expense

data class HistoryUiState(
    val isLoading: Boolean = false,
    val expenses: List<Expense> = emptyList(),
    val categories: List<Category> = emptyList(),
    val monthlySalary: Double = 0.0,
    val error: String? = null,
    /** Hay un alta o edición de gasto en curso. */
    val isSaving: Boolean = false,
    /** Error del último intento de guardar; se muestra dentro del diálogo. */
    val saveError: String? = null,
    /** Se incrementa en cada guardado exitoso para que la pantalla cierre el diálogo. */
    val savedCount: Int = 0,
)
