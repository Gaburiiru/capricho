package com.example.caprichoapp.domain.model

import kotlin.test.Test
import kotlin.test.assertEquals

class ProfileTest {
    private fun profile(nickname: String?) = Profile(
        id = "1",
        displayName = "Lucía Gómez",
        monthlySalary = 1_700_000.0,
        nickname = nickname,
    )

    @Test
    fun `shownName usa el apodo si existe`() =
        assertEquals("Lu", profile("Lu").shownName)

    @Test
    fun `shownName usa el nombre si no hay apodo`() =
        assertEquals("Lucía Gómez", profile(null).shownName)

    @Test
    fun `shownName ignora un apodo en blanco`() =
        assertEquals("Lucía Gómez", profile("  ").shownName)
}
