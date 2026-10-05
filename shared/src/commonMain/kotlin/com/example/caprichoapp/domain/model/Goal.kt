package com.example.caprichoapp.domain.model

data class Goal(
    val id: String,
    val title: String,
    val targetAmount: Double,
    val savedAmount: Double,
    val installments: Int = 1,
    val durability: Durability,
    val status: GoalStatus = GoalStatus.ACTIVE,
) {
    val remaining: Double get() = (targetAmount - savedAmount).coerceAtLeast(0.0)
    val isAchieved: Boolean get() = savedAmount >= targetAmount
}