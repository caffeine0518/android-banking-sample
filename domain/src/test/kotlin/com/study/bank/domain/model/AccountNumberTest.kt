package com.study.bank.domain.model

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class AccountNumberTest {

    @Test
    fun `valid number with hyphens`() {
        assertEquals("1000-1234-5678", AccountNumber("1000-1234-5678").value)
    }

    @Test
    fun `valid number without hyphens`() {
        assertEquals("100012345678", AccountNumber("100012345678").value)
    }

    @Test
    fun `blank is rejected`() {
        assertFailsWith<IllegalArgumentException> { AccountNumber("") }
    }

    @Test
    fun `too few digits is rejected`() {
        assertFailsWith<IllegalArgumentException> { AccountNumber("123-456") }
    }

    @Test
    fun `too many digits is rejected`() {
        assertFailsWith<IllegalArgumentException> { AccountNumber("123456789012345") }
    }

    @Test
    fun `non-digit and non-hyphen chars are rejected`() {
        assertFailsWith<IllegalArgumentException> { AccountNumber("1000-abcd-5678") }
    }
}
