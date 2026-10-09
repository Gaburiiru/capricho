package com.example.caprichoapp.domain.validation

enum class TextError { BLANK, TOO_LONG }

enum class SalaryError { INVALID, NOT_POSITIVE, TOO_LARGE }

/** Reglas de validación del perfil. Kotlin puro: sin textos de UI, solo tipos de error. */
object ProfileValidator {
    const val MAX_NAME_LENGTH = 40
    const val MAX_NICKNAME_LENGTH = 24
    const val MAX_SALARY = 1_000_000_000.0

    /** Por debajo de este monto se pide confirmar el sueldo (probable error de tipeo). */
    const val LOW_SALARY_THRESHOLD = 10_000.0

    fun validateName(name: String): TextError? {
        val trimmed = name.trim()
        return when {
            trimmed.isEmpty() -> TextError.BLANK
            trimmed.length > MAX_NAME_LENGTH -> TextError.TOO_LONG
            else -> null
        }
    }

    /** El apodo es opcional: vacío es válido. */
    fun validateNickname(nickname: String): TextError? =
        if (nickname.trim().length > MAX_NICKNAME_LENGTH) TextError.TOO_LONG else null

    fun validateSalary(raw: String): SalaryError? {
        val value = raw.trim().toDoubleOrNull()?.takeIf { it.isFinite() }
            ?: return SalaryError.INVALID
        return when {
            value <= 0.0 -> SalaryError.NOT_POSITIVE
            value > MAX_SALARY -> SalaryError.TOO_LARGE
            else -> null
        }
    }

    /**
     * Sueldo válido pero sospechosamente bajo (de 0 a [LOW_SALARY_THRESHOLD], sin incluirlo):
     * no se bloquea, solo se pide confirmación. Devuelve el monto o null si no hace falta.
     */
    fun lowSalaryToConfirm(raw: String): Double? {
        if (validateSalary(raw) != null) return null
        return raw.trim().toDouble().takeIf { it < LOW_SALARY_THRESHOLD }
    }
}
