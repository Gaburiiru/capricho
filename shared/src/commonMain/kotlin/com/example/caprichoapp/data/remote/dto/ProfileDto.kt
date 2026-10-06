package com.example.caprichoapp.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/** Espejo de la tabla `profiles` de Supabase. */
@Serializable
data class ProfileDto(
    val id: String,
    @SerialName("display_name") val displayName: String,
    val nickname: String? = null,
    @SerialName("monthly_salary") val monthlySalary: Double,
)
