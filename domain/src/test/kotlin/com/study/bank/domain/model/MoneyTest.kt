package com.study.bank.domain.model

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue

class MoneyTest {

    @Test
    fun `ZERO is zero`() {
        assertEquals(0L, Money.ZERO.amount)
    }

    @Test
    fun `negative amount is rejected`() {
        assertFailsWith<IllegalArgumentException> { Money(-1) }
    }

    @Test
    fun `plus adds amounts`() {
        assertEquals(Money(300), Money(100) + Money(200))
    }

    @Test
    fun `minus subtracts amounts`() {
        assertEquals(Money(50), Money(200) - Money(150))
    }

    @Test
    fun `minus to exactly zero is allowed`() {
        assertEquals(Money.ZERO, Money(100) - Money(100))
    }

    @Test
    fun `minus throws when result would be negative`() {
        assertFailsWith<IllegalArgumentException> { Money(100) - Money(200) }
    }

    @Test
    fun `compareTo less than`() {
        assertTrue(Money(100) < Money(200))
    }

    @Test
    fun `compareTo greater than`() {
        assertTrue(Money(200) > Money(100))
    }

    @Test
    fun `compareTo equal`() {
        assertEquals(0, Money(100).compareTo(Money(100)))
    }

    @Test
    fun `plus overflow throws ArithmeticException`() {
        assertFailsWith<ArithmeticException> { Money(Long.MAX_VALUE) + Money(1) }
    }
}
