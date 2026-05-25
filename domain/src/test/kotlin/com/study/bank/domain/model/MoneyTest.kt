package com.study.bank.domain.model

import java.math.BigDecimal
import org.junit.jupiter.api.Nested
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNotEquals
import kotlin.test.assertTrue

class MoneyTest {

    @Nested
    inner class Construction {

        @Test
        fun `zero creates a zero amount for the given currency`() {
            val zero = Money.zero(Currency.KRW)
            assertEquals(BigDecimal("0"), zero.amount)
            assertEquals(Currency.KRW, zero.currency)
        }

        @Test
        fun `zero of different currencies are not equal`() {
            assertNotEquals(Money.zero(Currency.KRW), Money.zero(Currency.USD))
        }

        @Test
        fun `negative amount is allowed`() {
            val money = Money.of(BigDecimal("-1"), Currency.KRW)
            assertEquals(BigDecimal("-1"), money.amount)
        }

        @Test
        fun `KRW rejects fractional amount`() {
            assertFailsWith<IllegalArgumentException> {
                Money.of(BigDecimal("100.5"), Currency.KRW)
            }
        }

        @Test
        fun `USD amount is normalized to two decimal places`() {
            val money = Money.of(100, Currency.USD)
            assertEquals(BigDecimal("100.00"), money.amount)
        }

        @Test
        fun `USD rejects amount with more precision than currency allows`() {
            assertFailsWith<IllegalArgumentException> {
                Money.of(BigDecimal("100.123"), Currency.USD)
            }
        }

        @Test
        fun `equality treats scaled and unscaled inputs as the same`() {
            assertEquals(Money.of("100", Currency.USD), Money.of("100.00", Currency.USD))
        }

        @Test
        fun `same numeric amount across different currencies is not equal`() {
            assertNotEquals(Money.of(100, Currency.KRW), Money.of(100, Currency.USD))
        }
    }

    @Nested
    inner class Plus {

        @Test
        fun `plus adds amounts of the same currency`() {
            assertEquals(
                Money.of(300, Currency.KRW),
                Money.of(100, Currency.KRW) + Money.of(200, Currency.KRW),
            )
        }

        @Test
        fun `plus across different currencies is rejected`() {
            assertFailsWith<IllegalArgumentException> {
                Money.of(100, Currency.KRW) + Money.of(100, Currency.USD)
            }
        }
    }

    @Nested
    inner class Minus {

        @Test
        fun `minus subtracts amounts of the same currency`() {
            assertEquals(
                Money.of(50, Currency.KRW),
                Money.of(200, Currency.KRW) - Money.of(150, Currency.KRW),
            )
        }

        @Test
        fun `minus to exactly zero is allowed`() {
            assertEquals(
                Money.zero(Currency.KRW),
                Money.of(100, Currency.KRW) - Money.of(100, Currency.KRW),
            )
        }

        @Test
        fun `minus can produce a negative result`() {
            assertEquals(
                Money.of(BigDecimal("-100"), Currency.KRW),
                Money.of(100, Currency.KRW) - Money.of(200, Currency.KRW),
            )
        }

        @Test
        fun `minus across different currencies is rejected`() {
            assertFailsWith<IllegalArgumentException> {
                Money.of(100, Currency.KRW) - Money.of(50, Currency.USD)
            }
        }
    }

    @Nested
    inner class Comparison {

        @Test
        fun `compareTo orders amounts of the same currency`() {
            assertTrue(Money.of(100, Currency.KRW) < Money.of(200, Currency.KRW))
            assertTrue(Money.of(200, Currency.KRW) > Money.of(100, Currency.KRW))
            assertEquals(0, Money.of(100, Currency.KRW).compareTo(Money.of(100, Currency.KRW)))
        }

        @Test
        fun `compareTo across different currencies is rejected`() {
            assertFailsWith<IllegalArgumentException> {
                Money.of(100, Currency.KRW).compareTo(Money.of(100, Currency.USD))
            }
        }
    }

    @Nested
    inner class Predicates {

        @Test
        fun `isPositive is true for positive amount`() {
            assertTrue(Money.of(1, Currency.KRW).isPositive())
        }

        @Test
        fun `isPositive is false for zero amount`() {
            assertEquals(false, Money.zero(Currency.KRW).isPositive())
        }

        @Test
        fun `isPositive is false for negative amount`() {
            assertEquals(false, Money.of(BigDecimal("-1"), Currency.KRW).isPositive())
        }
    }
}
