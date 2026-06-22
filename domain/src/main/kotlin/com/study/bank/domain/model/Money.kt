package com.study.bank.domain.model

import java.math.BigDecimal
import java.math.RoundingMode

/** Monetary amount in a specific [Currency]; arithmetic is same-currency only. */
class Money private constructor(
    val amount: BigDecimal,
    val currency: Currency,
) : Comparable<Money> {

    operator fun plus(other: Money): Money {
        requireSameCurrency(other)
        return of(amount.add(other.amount), currency)
    }

    operator fun minus(other: Money): Money {
        requireSameCurrency(other)
        return of(amount.subtract(other.amount), currency)
    }

    fun times(multiplier: BigDecimal, mode: RoundingMode): Money =
        of(amount.multiply(multiplier).setScale(currency.exponent, mode), currency)

    fun div(divisor: BigDecimal, mode: RoundingMode): Money =
        of(amount.divide(divisor, currency.exponent, mode), currency)

    override fun compareTo(other: Money): Int {
        requireSameCurrency(other)
        return amount.compareTo(other.amount)
    }

    fun isPositive(): Boolean = amount.signum() > 0

    private fun requireSameCurrency(other: Money) {
        require(currency == other.currency) {
            "Cannot operate across currencies: ${currency.code} vs ${other.currency.code}"
        }
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is Money) return false
        return amount == other.amount && currency == other.currency
    }

    override fun hashCode(): Int = 31 * amount.hashCode() + currency.hashCode()

    override fun toString(): String = "${currency.code} $amount"

    companion object {
        fun of(amount: BigDecimal, currency: Currency): Money {
            require(amount.scale() <= currency.exponent) {
                "Amount $amount has more fraction digits than ${currency.code} allows (${currency.exponent})"
            }
            return Money(amount.setScale(currency.exponent), currency)
        }

        fun of(amount: Long, currency: Currency): Money =
            of(BigDecimal.valueOf(amount), currency)

        fun of(amount: String, currency: Currency): Money =
            of(BigDecimal(amount), currency)

        /**
         * 통화의 최소단위(minor unit) 정수로부터 [Money]를 만든다. 소수점 키 없이 정수만
         * 누적하는 키패드 입력에서 통화별 소수 자릿수를 복원하는 표준 변환이다.
         * 예) `ofMinor(10050, USD)` → `$100.50`, `ofMinor(100, KRW)` → `₩100`.
         */
        fun ofMinor(minorAmount: Long, currency: Currency): Money =
            of(BigDecimal.valueOf(minorAmount).movePointLeft(currency.exponent), currency)

        fun zero(currency: Currency): Money = of(BigDecimal.ZERO, currency)
    }
}
