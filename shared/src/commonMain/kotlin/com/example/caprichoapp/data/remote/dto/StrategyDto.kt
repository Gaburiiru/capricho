package com.example.caprichoapp.data.remote.dto

import com.example.caprichoapp.domain.model.StrategyResponse
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class StrategyRequest(
    @SerialName("monthlySalary") val monthlySalary: Double,
    @SerialName("targetGoal") val targetGoal: GoalPayloadDto,
    @SerialName("expenses") val expenses: List<ExpensePayloadDto>,
)

@Serializable
data class GoalPayloadDto(
    val title: String,
    @SerialName("target_amount") val targetAmount: Double,
    @SerialName("saved_amount") val savedAmount: Double,
    val installments: Int? = 1,
    val durability: String? = null,
)

@Serializable
data class ExpensePayloadDto(
    val title: String? = null,
    val amount: Double,
    val kind: String? = null,
    @SerialName("category_name") val categoryName: String? = null,
)

@Serializable
data class StrategyResponseDto(
    val summary: String,
    val tips: List<String>,
    @SerialName("estimatedSavings") val estimatedSavings: String? = null,
)

@Serializable
data class ExpenseDto(
    val id: String? = null,
    @SerialName("user_id") val userId: String? = null,
    val title: String? = null,
    val amount: Double,
    // Sin valor por defecto a propósito: kotlinx.serialization NO envía los campos que valen
    // su default, y la columna `durability` es NOT NULL sin default en la base. Con el default
    // "MEDIUM" el insert salía sin esa columna y Postgres lo rechazaba.
    val installments: Int,
    val durability: String,
    val kind: String? = null,
    @SerialName("category_id") val categoryId: String? = null,
    @SerialName("spent_at") val spentAt: String? = null,
)

@Serializable
data class CategoryDto(
    val id: String,
    val name: String,
    val icon: String? = null,
    @SerialName("user_id") val userId: String? = null,
)

fun StrategyResponseDto.toDomain(): StrategyResponse = StrategyResponse(
    summary = summary,
    tips = tips,
    estimatedSavings = estimatedSavings,
)
