package com.study.bank.domain.model

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class CurrencyTest {

    @Test
    fun `KRW has zero exponent`() {
        assertEquals("KRW", Currency.KRW.code)
        assertEquals(0, Currency.KRW.exponent)
    }

    @Test
    fun `USD has two exponent`() {
        assertEquals("USD", Currency.USD.code)
        assertEquals(2, Currency.USD.exponent)
    }

    @Test
    fun `JPY has zero exponent`() {
        assertEquals(0, Currency.JPY.exponent)
    }

    @Test
    fun `EUR has two exponent`() {
        assertEquals(2, Currency.EUR.exponent)
    }

    @Test
    fun `byCode resolves supported currency`() {
        assertEquals(Currency.KRW, Currency.byCode("KRW"))
        assertEquals(Currency.USD, Currency.byCode("USD"))
    }

    @Test
    fun `byCode returns null for unknown code`() {
        assertNull(Currency.byCode("XYZ"))
    }
}
