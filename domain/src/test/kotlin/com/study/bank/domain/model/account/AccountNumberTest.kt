package com.study.bank.domain.model.account

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class AccountNumberTest {

    @Test
    fun `digits-only Korean account is preserved`() {
        assertEquals("100012345678", AccountNumber("100012345678").value)
    }

    @Test
    fun `KFTC masked format is accepted as-is`() {
        assertEquals("1000-12-***6789", AccountNumber("1000-12-***6789").value)
    }

    @Test
    fun `IBAN-style alphanumeric is accepted`() {
        assertEquals("GB29NWBK60161331926819", AccountNumber("GB29NWBK60161331926819").value)
    }

    @Test
    fun `blank is rejected`() {
        assertFailsWith<IllegalArgumentException> { AccountNumber("") }
    }

    @Test
    fun `whitespace-only is rejected`() {
        assertFailsWith<IllegalArgumentException> { AccountNumber("   ") }
    }

    @Test
    fun `internal whitespace is rejected`() {
        assertFailsWith<IllegalArgumentException> { AccountNumber("GB29 NWBK 6016 1331") }
    }

    @Test
    fun `over 50 chars is rejected`() {
        val tooLong = "1".repeat(51)
        assertFailsWith<IllegalArgumentException> { AccountNumber(tooLong) }
    }
}
