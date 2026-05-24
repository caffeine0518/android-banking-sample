package com.study.bank.domain.model.account

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class AccountNumberTest {

    @Test
    fun `valid digits-only number`() {
        assertEquals("100012345678", AccountNumber("100012345678").value)
    }

    @Test
    fun `blank is rejected`() {
        assertFailsWith<IllegalArgumentException> { AccountNumber("") }
    }

    @Test
    fun `too few digits is rejected`() {
        assertFailsWith<IllegalArgumentException> { AccountNumber("123456") }
    }

    @Test
    fun `too many digits is rejected`() {
        assertFailsWith<IllegalArgumentException> { AccountNumber("123456789012345") }
    }

    @Test
    fun `hyphens are rejected`() {
        assertFailsWith<IllegalArgumentException> { AccountNumber("1000-1234-5678") }
    }

    @Test
    fun `non-digit chars are rejected`() {
        assertFailsWith<IllegalArgumentException> { AccountNumber("1000abcd5678") }
    }
}
