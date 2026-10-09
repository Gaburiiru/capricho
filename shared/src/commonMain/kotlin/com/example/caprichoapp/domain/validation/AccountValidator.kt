package com.example.caprichoapp.domain.validation

enum class EmailError { BLANK, INVALID }

enum class PasswordError { EMPTY, TOO_SHORT, TOO_LONG }

/** Reglas de validación de la cuenta (correo y contraseña). Kotlin puro, sin textos de UI. */
object AccountValidator {
    const val MIN_PASSWORD_LENGTH = 6 // mínimo por defecto de Supabase Auth
    const val MAX_PASSWORD_LENGTH = 72
    const val MAX_EMAIL_LENGTH = 80

    private val EMAIL_REGEX = Regex("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$")

    /** El correo se guarda siempre sin espacios y en minúsculas, así el login no depende de cómo lo escribió. */
    fun normalizeEmail(raw: String): String = raw.trim().lowercase()

    fun validateEmail(raw: String): EmailError? {
        val email = raw.trim()
        return when {
            email.isEmpty() -> EmailError.BLANK
            !EMAIL_REGEX.matches(email) -> EmailError.INVALID
            else -> null
        }
    }

    /** Al crear la cuenta: exige el largo mínimo. */
    fun validatePassword(password: String): PasswordError? = when {
        password.isEmpty() -> PasswordError.EMPTY
        password.length < MIN_PASSWORD_LENGTH -> PasswordError.TOO_SHORT
        password.length > MAX_PASSWORD_LENGTH -> PasswordError.TOO_LONG
        else -> null
    }

    /** Al ingresar: solo que no esté vacía (no se bloquea a nadie por una regla de largo). */
    fun validateLoginPassword(password: String): PasswordError? =
        if (password.isEmpty()) PasswordError.EMPTY else null
}
