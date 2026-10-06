package com.example.caprichoapp.domain.validation

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class ProfileValidatorTest {

    // ---- nombre ----
    @Test
    fun `nombre vacio es invalido`() =
        assertEquals(TextError.BLANK, ProfileValidator.validateName(""))

    @Test
    fun `nombre solo con espacios es invalido`() =
        assertEquals(TextError.BLANK, ProfileValidator.validateName("   "))

    @Test
    fun `nombre normal es valido`() = assertNull(ProfileValidator.validateName("Lucía"))

    @Test
    fun `nombre justo en el limite es valido`() =
        assertNull(ProfileValidator.validateName("a".repeat(ProfileValidator.MAX_NAME_LENGTH)))

    @Test
    fun `nombre mas largo que el limite es invalido`() =
        assertEquals(
            TextError.TOO_LONG,
            ProfileValidator.validateName("a".repeat(ProfileValidator.MAX_NAME_LENGTH + 1)),
        )

    // ---- apodo ----
    @Test
    fun `apodo vacio es valido porque es opcional`() =
        assertNull(ProfileValidator.validateNickname(""))

    @Test
    fun `apodo demasiado largo es invalido`() =
        assertEquals(
            TextError.TOO_LONG,
            ProfileValidator.validateNickname("a".repeat(ProfileValidator.MAX_NICKNAME_LENGTH + 1)),
        )

    // ---- sueldo ----
    @Test
    fun `sueldo vacio es invalido`() =
        assertEquals(SalaryError.INVALID, ProfileValidator.validateSalary(""))

    @Test
    fun `sueldo con letras es invalido`() =
        assertEquals(SalaryError.INVALID, ProfileValidator.validateSalary("abc"))

    @Test
    fun `sueldo NaN o infinito es invalido`() {
        assertEquals(SalaryError.INVALID, ProfileValidator.validateSalary("NaN"))
        assertEquals(SalaryError.INVALID, ProfileValidator.validateSalary("Infinity"))
    }

    @Test
    fun `sueldo cero no es positivo`() =
        assertEquals(SalaryError.NOT_POSITIVE, ProfileValidator.validateSalary("0"))

    @Test
    fun `sueldo negativo no es positivo`() =
        assertEquals(SalaryError.NOT_POSITIVE, ProfileValidator.validateSalary("-100"))

    @Test
    fun `sueldo normal es valido`() = assertNull(ProfileValidator.validateSalary("1700000"))

    @Test
    fun `sueldo justo en el maximo es valido y por encima es demasiado grande`() {
        assertNull(ProfileValidator.validateSalary("1000000000"))
        assertEquals(SalaryError.TOO_LARGE, ProfileValidator.validateSalary("1000000001"))
    }
}
