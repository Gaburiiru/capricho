package com.example.caprichoapp.feature.history

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.caprichoapp.core.util.today
import com.example.caprichoapp.domain.model.Category
import com.example.caprichoapp.domain.model.Expense
import com.example.caprichoapp.domain.model.ExpenseKind
import com.example.caprichoapp.domain.model.Durability
import com.example.caprichoapp.domain.repository.CategoryRepository
import com.example.caprichoapp.domain.repository.ExpenseRepository
import com.example.caprichoapp.domain.repository.ProfileRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.datetime.LocalDate

class HistoryViewModel(
    private val expenseRepository: ExpenseRepository,
    private val profileRepository: ProfileRepository,
    private val categoryRepository: CategoryRepository,
) : ViewModel() {

    private val _state = MutableStateFlow(HistoryUiState())
    val state: StateFlow<HistoryUiState> = _state.asStateFlow()

    init {
        loadExpenses()
        loadCategories()
    }

    fun loadCategories() {
        viewModelScope.launch {
            val categories = categoryRepository.getCategories().getOrElse { emptyList() }
            _state.update { it.copy(categories = categories) }
        }
    }

    fun loadExpenses() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }

            val profileResult = profileRepository.fetchProfile()
            val expensesResult = expenseRepository.getExpenses()
            val profile = profileResult.getOrNull()
            val expenses = expensesResult.getOrElse { emptyList() }
                .sortedWith(compareByDescending<Expense> { it.spentAt }.thenByDescending { it.id })
            val error = expensesResult.exceptionOrNull()?.message
                ?: profileResult.exceptionOrNull()?.message
                ?: if (expenses.isEmpty() && profile == null) {
                    "No pudimos cargar tu perfil ni tus gastos."
                } else {
                    null
                }

            _state.update {
                it.copy(
                    isLoading = false,
                    expenses = expenses,
                    monthlySalary = profile?.monthlySalary ?: 0.0,
                    error = error,
                )
            }
        }
    }

    fun addExpense(expense: Expense) = save("No se pudo guardar el gasto.") {
        expenseRepository.addExpense(expense)
    }

    fun updateExpense(expense: Expense) = save("No se pudo actualizar el gasto.") {
        expenseRepository.updateExpense(expense)
    }

    fun clearSaveError() {
        _state.update { it.copy(saveError = null) }
    }

    /**
     * Guarda y recién ahí avisa a la pantalla (savedCount) para que cierre el diálogo.
     * Si falla, el diálogo queda abierto con el motivo real, sin perder lo que escribió el usuario.
     */
    private fun save(fallbackMessage: String, action: suspend () -> Result<Unit>) {
        if (_state.value.isSaving) return
        viewModelScope.launch {
            _state.update { it.copy(isSaving = true, saveError = null) }
            action()
                .onSuccess {
                    _state.update { it.copy(isSaving = false, savedCount = it.savedCount + 1) }
                    loadExpenses()
                }
                .onFailure { error ->
                    _state.update {
                        it.copy(isSaving = false, saveError = error.message ?: fallbackMessage)
                    }
                }
        }
    }

    fun deleteExpense(expenseId: String) {
        viewModelScope.launch {
            expenseRepository.deleteExpense(expenseId)
                .onSuccess { loadExpenses() }
                .onFailure { error ->
                    _state.update { it.copy(error = error.message ?: "No se pudo eliminar el gasto.") }
                }
        }
    }

    fun buildExpense(
        title: String,
        amount: Double,
        categoryName: String,
        kind: ExpenseKind,
        durability: Durability,
        spentAt: LocalDate = today(),
        id: String = "",
    ): Expense = Expense(
        id = id,
        categoryName = categoryName,
        title = title.ifBlank { "Gasto" },
        amount = amount,
        kind = kind,
        durability = durability,
        spentAt = spentAt,
    )
}
