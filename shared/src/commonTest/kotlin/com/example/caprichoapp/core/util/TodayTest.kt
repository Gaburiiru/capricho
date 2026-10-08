package com.example.caprichoapp.core.util

import kotlinx.datetime.LocalDate
import kotlin.test.Test
import kotlin.test.assertTrue

class TodayTest {
    @Test
    fun `today devuelve la fecha actual y no una fija`() {
        assertTrue(today() > LocalDate(2025, 1, 1))
    }
}
