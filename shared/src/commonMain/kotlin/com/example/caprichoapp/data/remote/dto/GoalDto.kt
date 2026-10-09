package com.example.caprichoapp.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/** Espejo de la tabla `goals` de Supabase. */
@Serializable
data class GoalDto(
    val id: String? = null,
    @SerialName("user_id") val userId: String? = null,
    val title: String,
    @SerialName("target_amount") val targetAmount: Double,
    @SerialName("saved_amount") val savedAmount: Double = 0.0,
    val installments: Int = 1,
    val durability: String,
    val status: String = "ACTIVE",
)
