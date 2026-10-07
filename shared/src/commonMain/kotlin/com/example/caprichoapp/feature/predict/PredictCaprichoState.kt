package com.example.caprichoapp.feature.predict

import com.example.caprichoapp.domain.model.Durability

enum class CaprichoTiming {
    THIS_MONTH,
    NEXT_MONTH,
    UNKNOWN,
}

enum class RecommendationVerdict {
    GREAT_CAPRICHO,
    MODERATE_RISK,
    HEAVY_CAPRICHO,
}

data class GoalImpactInfo(
    val goalId: String,
    val goalTitle: String,
    val impactPercent: Double,
    val goalRemainingAmount: Double,
)

data class CaprichoDiagnosis(
    val verdict: RecommendationVerdict,
    val mascotMessage: String,
    val monthlyPayment: Double,
    val salaryImpactPercent: Double,
    val isInstallment: Boolean,
    val durationLabel: String,
    val recommendationSummary: String,
    val goalImpacts: List<GoalImpactInfo> = emptyList(),
)

data class PredictCaprichoState(
    val step: Int = 1,
    val rawAmount: String = "",
    val installments: Int = 1,
    val timing: CaprichoTiming? = null,
    val durability: Durability? = null,
    val diagnosis: CaprichoDiagnosis? = null,
    val showSaveGoalDialog: Boolean = false,
    val isSavingGoal: Boolean = false,
    val isGoalSaved: Boolean = false,
    val goalSaveError: String? = null,
) {
    val amount: Double
        get() = rawAmount.toDoubleOrNull() ?: 0.0

    val isStepValid: Boolean
        get() = when (step) {
            1 -> amount > 0.0
            2 -> installments >= 1
            3 -> timing != null
            4 -> durability != null
            5 -> diagnosis != null
            else -> false
        }
}
