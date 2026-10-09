package com.example.caprichoapp.feature.predict

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.caprichoapp.core.util.appendAmountDigit
import com.example.caprichoapp.core.util.today
import com.example.caprichoapp.domain.calculator.ImpactCalculator
import com.example.caprichoapp.domain.model.Category
import com.example.caprichoapp.domain.model.Durability
import com.example.caprichoapp.domain.model.Expense
import com.example.caprichoapp.domain.model.ExpenseKind
import com.example.caprichoapp.domain.model.Goal
import com.example.caprichoapp.domain.model.GoalStatus
import com.example.caprichoapp.domain.model.Profile
import com.example.caprichoapp.domain.repository.CategoryRepository
import com.example.caprichoapp.domain.repository.ExpenseRepository
import com.example.caprichoapp.domain.repository.GoalRepository
import com.example.caprichoapp.domain.repository.ProfileRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/** El monto del capricho llega hasta 99.999.999. */
private const val MAX_CAPRICHO_DIGITS = 8

/** Fracción -> porcentaje con un decimal (0.774 -> 77.4). */
private fun Double.toPercentOneDecimal(): Double = (this * 1000).toInt() / 10.0

class PredictCaprichoViewModel(
    private val profileRepository: ProfileRepository,
    private val goalRepository: GoalRepository,
    private val expenseRepository: ExpenseRepository,
    private val categoryRepository: CategoryRepository,
) : ViewModel() {

    private val _state = MutableStateFlow(PredictCaprichoState())
    val state: StateFlow<PredictCaprichoState> = _state.asStateFlow()

    private var userProfile: Profile? = null

    init {
        viewModelScope.launch {
            userProfile = profileRepository.fetchProfile().getOrNull()
        }
        viewModelScope.launch {
            val categories = categoryRepository.getCategories().getOrElse { emptyList() }
            _state.update { it.copy(categories = categories) }
        }
    }

    fun onDigitInput(digit: Char) {
        _state.update { current ->
            current.copy(rawAmount = current.rawAmount.appendAmountDigit(digit, MAX_CAPRICHO_DIGITS))
        }
    }

    fun onClearInput() {
        _state.update { it.copy(rawAmount = "") }
    }

    fun onDeleteInput() {
        _state.update { current ->
            if (current.rawAmount.isNotEmpty()) {
                current.copy(rawAmount = current.rawAmount.dropLast(1))
            } else current
        }
    }

    fun onInstallmentsSelected(installments: Int) {
        _state.update { it.copy(installments = installments) }
    }

    fun onTimingSelected(timing: CaprichoTiming) {
        _state.update { it.copy(timing = timing) }
    }

    fun onDurabilitySelected(durability: Durability) {
        _state.update { it.copy(durability = durability) }
    }

    fun onNextStep() {
        val current = _state.value
        val nextStep = current.step + 1
        if (nextStep == 5) {
            viewModelScope.launch {
                val diagnosis = buildDiagnosis(current)
                _state.update {
                    it.copy(step = 5, diagnosis = diagnosis)
                }
            }
        } else {
            _state.update { current.copy(step = nextStep.coerceAtMost(5)) }
        }
    }

    fun onPreviousStep() {
        _state.update { current ->
            current.copy(step = (current.step - 1).coerceAtLeast(1))
        }
    }

    fun showSaveGoalDialog(show: Boolean) {
        _state.update { it.copy(showSaveGoalDialog = show, goalSaveError = null) }
    }

    fun showSaveExpenseDialog(show: Boolean) {
        // Sin contado no hay gasto (ver PredictCaprichoState.canSaveAsExpense)
        if (show && !_state.value.canSaveAsExpense) return
        _state.update { it.copy(showSaveExpenseDialog = show, expenseSaveError = null) }
    }

    /**
     * Guarda el capricho como gasto (solo contado). Meta y gasto son excluyentes: una vez
     * guardado como uno, no se puede guardar también como el otro.
     */
    fun saveAsExpense(title: String, category: Category) {
        val current = _state.value
        if (title.isBlank() || !current.canSaveAsExpense) return
        if (current.isSavingExpense || current.isExpenseSaved || current.isGoalSaved) return

        val expense = Expense(
            categoryId = category.id.ifBlank { null },
            categoryName = category.name,
            title = title.trim(),
            amount = current.amount,
            installments = 1,
            durability = current.durability ?: Durability.FLEETING,
            kind = ExpenseKind.ONE_OFF,
            spentAt = today(),
        )
        viewModelScope.launch {
            _state.update { it.copy(isSavingExpense = true, expenseSaveError = null) }
            expenseRepository.addExpense(expense)
                .onSuccess {
                    _state.update {
                        it.copy(
                            isSavingExpense = false,
                            isExpenseSaved = true,
                            showSaveExpenseDialog = false,
                        )
                    }
                }
                .onFailure { error ->
                    _state.update {
                        it.copy(
                            isSavingExpense = false,
                            expenseSaveError = error.message ?: "Error al guardar el gasto",
                        )
                    }
                }
        }
    }

    fun saveAsGoal(title: String) {
        if (title.isBlank()) return
        val current = _state.value
        if (current.isExpenseSaved) return
        val goal = Goal(
            title = title.trim(),
            targetAmount = current.amount,
            savedAmount = 0.0,
            installments = current.installments,
            durability = current.durability ?: Durability.FLEETING,
            status = GoalStatus.ACTIVE,
        )
        viewModelScope.launch {
            _state.update { it.copy(isSavingGoal = true, goalSaveError = null) }
            goalRepository.addGoal(goal)
                .onSuccess {
                    _state.update {
                        it.copy(
                            isSavingGoal = false,
                            isGoalSaved = true,
                            showSaveGoalDialog = false,
                        )
                    }
                }
                .onFailure { error ->
                    _state.update {
                        it.copy(
                            isSavingGoal = false,
                            goalSaveError = error.message ?: "Error al guardar meta",
                        )
                    }
                }
        }
    }

    private suspend fun buildDiagnosis(state: PredictCaprichoState): CaprichoDiagnosis {
        val salary = userProfile?.monthlySalary ?: 500000.0 // Default si no se cargó sueldo
        val monthlyAmount = ImpactCalculator.monthlyAmount(state.amount, state.installments)
        val impactFraction = if (salary > 0) ImpactCalculator.salaryImpact(state.amount, state.installments, salary) else 0.1
        val impactPercent = impactFraction * 100.0

        val activeGoals = goalRepository.getGoals().getOrDefault(emptyList())
            .filter { !it.isAchieved && it.status == GoalStatus.ACTIVE }

        // La meta que más te aleja (mayor salto de distancia, en puntos de porcentaje) va primero
        val goalImpacts = activeGoals.sortedByDescending { goal ->
            ImpactCalculator.goalGapAfter(state.amount, goal) - ImpactCalculator.goalGapNow(goal)
        }.map { goal ->
            GoalImpactInfo(
                goalId = goal.id,
                goalTitle = goal.title,
                gapNowPercent = ImpactCalculator.goalGapNow(goal).toPercentOneDecimal(),
                gapAfterPercent = ImpactCalculator.goalGapAfter(state.amount, goal).toPercentOneDecimal(),
                goalRemainingAmount = goal.remaining,
            )
        }

        val durability = state.durability ?: Durability.FLEETING
        val isInstallment = state.installments > 1

        val (verdict, message, summary) = when {
            impactPercent > 150.0 -> Triple(
                RecommendationVerdict.HEAVY_CAPRICHO,
                "¡LOCURA TOTAL! ¡Este gasto supera el 150% de tu sueldo mensual! ¡Tus finanzas van a volar por los aires!",
                "Peligro financiero extremo: el costo sobrepasa enormemente tu capacidad de pago mensual.",
            )
            impactPercent > 100.0 -> Triple(
                RecommendationVerdict.HEAVY_CAPRICHO,
                "¡PÁNICO! Este capricho supera el 100% de tu sueldo del mes. ¡Superás completamente tu presupuesto!",
                "Alerta crítica: necesitarías más de un sueldo entero para cubrir este egreso.",
            )
            durability == Durability.HIGH && impactPercent <= 15.0 -> Triple(
                RecommendationVerdict.GREAT_CAPRICHO,
                "¡Excelente inversión! Te va a durar un montón y no desarma tu bolsillo.",
                "Un capricho duradero y totalmente accesible según tu presupuesto mensual.",
            )
            durability == Durability.FLEETING && (impactPercent > 10.0 || isInstallment) -> Triple(
                RecommendationVerdict.HEAVY_CAPRICHO,
                "¡Atención! Es algo efímero que te va a costar bastante o vas a seguir pagando cuando ya haya desaparecido.",
                "Cuidado: financiar o gastar una fracción alta de tu sueldo en algo fugaz suele generar arrepentimiento.",
            )
            durability == Durability.MEDIUM && impactPercent <= 20.0 -> Triple(
                RecommendationVerdict.GREAT_CAPRICHO,
                "¡Buen balance! Es un gusto razonable que vas a disfrutar un buen tiempo.",
                "Capricho equilibrado: la utilidad justifica el costo en tu presupuesto.",
            )
            impactPercent > 25.0 -> Triple(
                RecommendationVerdict.HEAVY_CAPRICHO,
                "¡Ojo con este capricho! Representa más del 25% de tu sueldo mensual.",
                "Impacto alto en tu presupuesto. Considerá postergarlo o juntar un fondo previo.",
            )
            else -> Triple(
                RecommendationVerdict.MODERATE_RISK,
                "Es un capricho manejable, pero estate atento a tus otros gastos del mes.",
                "Impacto moderado: te recomendamos revisar tu margen de ahorro antes de confirmar.",
            )
        }

        val durationLabel = when (durability) {
            Durability.FLEETING -> "Fugaz / Efímero"
            Durability.MEDIUM -> "Uso medio"
            Durability.HIGH -> "Alta"
        }

        return CaprichoDiagnosis(
            verdict = verdict,
            mascotMessage = message,
            monthlyPayment = monthlyAmount,
            salaryImpactPercent = impactPercent,
            isInstallment = isInstallment,
            durationLabel = durationLabel,
            recommendationSummary = summary,
            goalImpacts = goalImpacts,
        )
    }
}
