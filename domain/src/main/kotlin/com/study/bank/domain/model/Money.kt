package com.study.bank.domain.model

import java.math.BigDecimal

/** Non-negative monetary amount in a specific [Currency]; arithmetic is same-currency only. */
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
        val result = amount.subtract(other.amount)
        require(result.signum() >= 0) {
            "Cannot subtract $other from $this (result would be negative)"
        }
        return of(result, currency)
    }

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
            require(amount.signum() >= 0) { "Money cannot be negative: $amount" }
            require(amount.scale() <= currency.exponent) {
                "Amount $amount has more fraction digits than ${currency.code} allows (${currency.exponent})"
            }
            return Money(amount.setScale(currency.exponent), currency)
        }

        fun of(amount: Long, currency: Currency): Money =
            of(BigDecimal.valueOf(amount), currency)

        fun of(amount: String, currency: Currency): Money =
            of(BigDecimal(amount), currency)

        fun zero(currency: Currency): Money = of(BigDecimal.ZERO, currency)
    }
}
