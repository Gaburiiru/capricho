package com.example.caprichoapp.core.util

/** 1700000 -> "1.700.000" */
fun Long.formatThousands(): String =
    toString().reversed().chunked(3).joinToString(".").reversed()
