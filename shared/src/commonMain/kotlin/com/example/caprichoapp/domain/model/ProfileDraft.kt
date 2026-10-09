package com.example.caprichoapp.domain.model

/** Datos que carga el usuario en el onboarding, ya validados y normalizados. */
data class ProfileDraft(
    val displayName: String,
    val nickname: String?,
    val monthlySalary: Double,
)
