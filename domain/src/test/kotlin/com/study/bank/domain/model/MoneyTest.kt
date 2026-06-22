package com.study.bank.domain.model

import java.math.BigDecimal
import java.math.RoundingMode
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
    inner class OfMinor {

        @Test
        fun `USD minor units are read as cents`() {
            assertEquals(Money.of("100.50", Currency.USD), Money.ofMinor(10_050, Currency.USD))
        }

        @Test
        fun `USD sub-dollar minor units keep the cents`() {
            assertEquals(Money.of("0.05", Currency.USD), Money.ofMinor(5, Currency.USD))
        }

        @Test
        fun `KRW minor unit equals its major unit`() {
            assertEquals(Money.of(100, Currency.KRW), Money.ofMinor(100, Currency.KRW))
        }

        @Test
        fun `EUR minor units are read as cents`() {
            assertEquals(Money.of("3245.80", Currency.EUR), Money.ofMinor(324_580, Currency.EUR))
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

    @Nested
    inner class Times {

        @Test
        fun `times multiplies amount by an integer scalar within currency precision`() {
            assertEquals(
                Money.of(300, Currency.KRW),
                Money.of(100, Currency.KRW).times(BigDecimal("3"), RoundingMode.HALF_UP),
            )
        }

        @Test
        fun `times applies HALF_UP when product exceeds currency precision`() {
            assertEquals(
                Money.of(1, Currency.KRW),
                Money.of(1, Currency.KRW).times(BigDecimal("0.5"), RoundingMode.HALF_UP),
            )
        }

        @Test
        fun `times applies HALF_DOWN when product exceeds currency precision`() {
            assertEquals(
                Money.zero(Currency.KRW),
                Money.of(1, Currency.KRW).times(BigDecimal("0.5"), RoundingMode.HALF_DOWN),
            )
        }

        @Test
        fun `times applies FLOOR when product exceeds currency precision`() {
            assertEquals(
                Money.of(33, Currency.KRW),
                Money.of(100, Currency.KRW).times(BigDecimal("0.333"), RoundingMode.FLOOR),
            )
        }

        @Test
        fun `times applies CEILING when product exceeds currency precision`() {
            assertEquals(
                Money.of(34, Currency.KRW),
                Money.of(100, Currency.KRW).times(BigDecimal("0.333"), RoundingMode.CEILING),
            )
        }

        @Test
        fun `times applies HALF_UP for USD fractional product`() {
            assertEquals(
                Money.of(BigDecimal("16.67"), Currency.USD),
                Money.of(BigDecimal("33.33"), Currency.USD).times(BigDecimal("0.5"), RoundingMode.HALF_UP),
            )
        }

        @Test
        fun `times by one returns the same amount`() {
            assertEquals(
                Money.of(100, Currency.KRW),
                Money.of(100, Currency.KRW).times(BigDecimal.ONE, RoundingMode.HALF_UP),
            )
        }

        @Test
        fun `times by zero returns zero`() {
            assertEquals(
                Money.zero(Currency.KRW),
                Money.of(100, Currency.KRW).times(BigDecimal.ZERO, RoundingMode.HALF_UP),
            )
        }

        @Test
        fun `times by negative scalar flips the sign`() {
            assertEquals(
                Money.of(BigDecimal("-100"), Currency.KRW),
                Money.of(100, Currency.KRW).times(BigDecimal("-1"), RoundingMode.HALF_UP),
            )
        }

        @Test
        fun `times with UNNECESSARY throws when product exceeds currency precision`() {
            assertFailsWith<ArithmeticException> {
                Money.of(1, Currency.KRW).times(BigDecimal("0.5"), RoundingMode.UNNECESSARY)
            }
        }

        @Test
        fun `times preserves currency`() {
            val result = Money.of(100, Currency.USD).times(BigDecimal("2"), RoundingMode.HALF_UP)
            assertEquals(Currency.USD, result.currency)
        }
    }

    @Nested
    inner class Div {

        @Test
        fun `div divides amount evenly within currency precision`() {
            assertEquals(
                Money.of(50, Currency.KRW),
                Money.of(100, Currency.KRW).div(BigDecimal("2"), RoundingMode.HALF_UP),
            )
        }

        @Test
        fun `div applies HALF_UP for USD non-terminating result`() {
            assertEquals(
                Money.of(BigDecimal("33.33"), Currency.USD),
                Money.of(100, Currency.USD).div(BigDecimal("3"), RoundingMode.HALF_UP),
            )
        }

        @Test
        fun `div applies FLOOR for KRW non-terminating result`() {
            assertEquals(
                Money.of(33, Currency.KRW),
                Money.of(100, Currency.KRW).div(BigDecimal("3"), RoundingMode.FLOOR),
            )
        }

        @Test
        fun `div applies CEILING for KRW non-terminating result`() {
            assertEquals(
                Money.of(34, Currency.KRW),
                Money.of(100, Currency.KRW).div(BigDecimal("3"), RoundingMode.CEILING),
            )
        }

        @Test
        fun `div applies HALF_DOWN for KRW non-terminating result`() {
            assertEquals(
                Money.of(33, Currency.KRW),
                Money.of(100, Currency.KRW).div(BigDecimal("3"), RoundingMode.HALF_DOWN),
            )
        }

        @Test
        fun `div by one returns the same amount`() {
            assertEquals(
                Money.of(100, Currency.KRW),
                Money.of(100, Currency.KRW).div(BigDecimal.ONE, RoundingMode.HALF_UP),
            )
        }

        @Test
        fun `div by zero throws ArithmeticException`() {
            assertFailsWith<ArithmeticException> {
                Money.of(100, Currency.KRW).div(BigDecimal.ZERO, RoundingMode.HALF_UP)
            }
        }

        @Test
        fun `div with UNNECESSARY throws when result needs rounding`() {
            assertFailsWith<ArithmeticException> {
                Money.of(100, Currency.KRW).div(BigDecimal("3"), RoundingMode.UNNECESSARY)
            }
        }

        @Test
        fun `div by negative divisor flips the sign`() {
            assertEquals(
                Money.of(BigDecimal("-50"), Currency.KRW),
                Money.of(100, Currency.KRW).div(BigDecimal("-2"), RoundingMode.HALF_UP),
            )
        }

        @Test
        fun `div preserves currency`() {
            val result = Money.of(100, Currency.USD).div(BigDecimal("4"), RoundingMode.HALF_UP)
            assertEquals(Currency.USD, result.currency)
        }
    }
}
