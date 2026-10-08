package com.example.caprichoapp.core.util

/** Cantidad máxima de dígitos que acepta un monto (hasta 999.999.999). */
const val MAX_AMOUNT_DIGITS = 9

/**
 * Agrega un dígito a un monto en texto plano ("5000" + '0' -> "50000").
 * Ignora lo que no es dígito, los ceros a la izquierda ("" + '0' -> "") y respeta el máximo.
 */
fun String.appendAmountDigit(digit: Char, maxDigits: Int = MAX_AMOUNT_DIGITS): String = when {
    !digit.isDigit() -> this
    length >= maxDigits -> this
    isEmpty() && digit == '0' -> this
    else -> this + digit
}

/** "1700000" -> "1.700.000"; vacío o inválido -> "0". */
fun String.formatAmount(): String = toLongOrNull()?.formatThousands() ?: "0"

/** Suma [extra] a un monto en texto plano respetando el máximo de dígitos. */
fun String.plusAmount(extra: Long, maxDigits: Int = MAX_AMOUNT_DIGITS): String {
    val total = (toLongOrNull() ?: 0L) + extra
    val text = total.toString()
    return if (total <= 0L || text.length > maxDigits) this else text
}

/** 3472.24 -> "3.472,2" (un decimal, coma decimal y puntos de miles). */
fun Double.formatPercent(): String {
    val tenths = (this * 10).toLong()
    return "${(tenths / 10).formatThousands()},${tenths % 10}"
}
