package com.example.caprichoapp.domain.validation

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class AccountValidatorTest {

    @Test
    fun `correo vacio o mal escrito es invalido`() {
        assertEquals(EmailError.BLANK, AccountValidator.validateEmail("   "))
        assertEquals(EmailError.INVALID, AccountValidator.validateEmail("gabo"))
        assertEquals(EmailError.INVALID, AccountValidator.validateEmail("gabo@mail"))
        assertEquals(EmailError.INVALID, AccountValidator.validateEmail("ga bo@mail.com"))
    }

    @Test
    fun `correo valido`() {
        assertNull(AccountValidator.validateEmail("gabo@mail.com"))
        assertNull(AccountValidator.validateEmail("  gabo.perez+capricho@mail.com.ar "))
    }

    @Test
    fun `el correo se normaliza sin espacios y en minusculas`() {
        assertEquals("gabo@mail.com", AccountValidator.normalizeEmail("  Gabo@Mail.COM "))
    }

    @Test
    fun `contrasena de alta exige largo minimo`() {
        assertEquals(PasswordError.EMPTY, AccountValidator.validatePassword(""))
        assertEquals(PasswordError.TOO_SHORT, AccountValidator.validatePassword("12345"))
        assertNull(AccountValidator.validatePassword("123456"))
        assertEquals(PasswordError.TOO_LONG, AccountValidator.validatePassword("a".repeat(73)))
    }

    @Test
    fun `al ingresar solo se exige que no este vacia`() {
        assertEquals(PasswordError.EMPTY, AccountValidator.validateLoginPassword(""))
        assertNull(AccountValidator.validateLoginPassword("123"))
    }
}
