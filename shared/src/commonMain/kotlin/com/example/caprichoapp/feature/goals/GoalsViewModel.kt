package com.example.caprichoapp.feature.goals

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.caprichoapp.domain.model.Durability
import com.example.caprichoapp.domain.model.Goal
import com.example.caprichoapp.domain.model.GoalStatus
import com.example.caprichoapp.domain.repository.GoalRepository
import com.example.caprichoapp.domain.repository.StrategyRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class GoalsViewModel(
    private val goalRepository: GoalRepository,
    private val strategyRepository: StrategyRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow<GoalsUiState>(GoalsUiState.Loading)
    val uiState: StateFlow<GoalsUiState> = _uiState.asStateFlow()

    private val _showAddDialog = MutableStateFlow(false)
    val showAddDialog: StateFlow<Boolean> = _showAddDialog.asStateFlow()

    private val _selectedGoalForDetail = MutableStateFlow<Goal?>(null)
    val selectedGoalForDetail: StateFlow<Goal?> = _selectedGoalForDetail.asStateFlow()

    private val _goalToDelete = MutableStateFlow<Goal?>(null)
    val goalToDelete: StateFlow<Goal?> = _goalToDelete.asStateFlow()

    private val _canGenerateStrategy = MutableStateFlow(false)
    val canGenerateStrategy: StateFlow<Boolean> = _canGenerateStrategy.asStateFlow()

    init {
        loadGoals()
    }

    fun loadGoals() {
        viewModelScope.launch {
            _uiState.value = GoalsUiState.Loading
            goalRepository.getGoals()
                .onSuccess { goals ->
                    _uiState.value = GoalsUiState.Success(goals)
                    strategyRepository.hasStoredExpenses()
                        .onSuccess { hasExpenses -> _canGenerateStrategy.value = hasExpenses }
                        .onFailure { _canGenerateStrategy.value = false }
                }
                .onFailure { error ->
                    _uiState.value = GoalsUiState.Error(error.message ?: "Error al cargar las metas")
                    _canGenerateStrategy.value = false
                }
        }
    }

    fun onAddGoalClick() {
        _showAddDialog.value = true
    }

    fun onDismissAddDialog() {
        _showAddDialog.value = false
    }

    fun onGoalClick(goal: Goal) {
        _selectedGoalForDetail.value = goal
    }

    fun onCloseDetailDialog() {
        _selectedGoalForDetail.value = null
    }

    fun onRequestDeleteGoal(goal: Goal) {
        _goalToDelete.value = goal
    }

    fun onCancelDeleteGoal() {
        _goalToDelete.value = null
    }

    fun addGoal(title: String, targetAmount: Double, installments: Int, durability: Durability) {
        viewModelScope.launch {
            val goal = Goal(
                title = title.trim(),
                targetAmount = targetAmount,
                savedAmount = 0.0,
                installments = installments,
                durability = durability,
                status = GoalStatus.ACTIVE,
            )
            goalRepository.addGoal(goal)
                .onSuccess {
                    _showAddDialog.value = false
                    loadGoals()
                }
        }
    }

    fun addSavings(goal: Goal, addedAmount: Double) {
        if (addedAmount <= 0.0) return
        viewModelScope.launch {
            val updatedSaved = goal.savedAmount + addedAmount
            val isNowAchieved = updatedSaved >= goal.targetAmount
            val updatedGoal = goal.copy(
                savedAmount = updatedSaved,
                status = if (isNowAchieved) GoalStatus.ACHIEVED else goal.status,
            )
            goalRepository.updateGoal(updatedGoal)
                .onSuccess {
                    _selectedGoalForDetail.value = updatedGoal
                    loadGoals()
                }
        }
    }

    fun confirmDeleteGoal() {
        val target = _goalToDelete.value ?: return
        viewModelScope.launch {
            goalRepository.deleteGoal(target.id)
                .onSuccess {
                    _goalToDelete.value = null
                    if (_selectedGoalForDetail.value?.id == target.id) {
                        _selectedGoalForDetail.value = null
                    }
                    loadGoals()
                }
        }
    }
}
