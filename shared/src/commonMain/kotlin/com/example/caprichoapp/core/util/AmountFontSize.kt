package com.example.caprichoapp.core.util

private const val WIDTH_BUDGET = 310
private const val MIN_SP = 24
private const val MAX_SP = 52

/**
 * Tamaño (en sp) del monto grande según cuántos caracteres tiene, para que entre
 * en una sola línea: pocos dígitos se ven enormes y los montos largos se achican.
 */
fun amountFontSizeSp(charCount: Int): Int =
    (WIDTH_BUDGET / charCount.coerceAtLeast(1)).coerceIn(MIN_SP, MAX_SP)
