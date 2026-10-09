package com.example.caprichoapp.domain.model

data class Profile(
    val id: String,
    val displayName: String,
    val monthlySalary: Double,
    val nickname: String? = null,
) {
    /** Nombre que se muestra en la interfaz: el apodo si existe, si no el nombre. */
    val shownName: String
        get() = nickname?.takeIf { it.isNotBlank() } ?: displayName
}
